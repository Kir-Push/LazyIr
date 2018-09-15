package com.example.buhalo.lazyir.service.network.tcp;

import android.content.Context;
import android.util.Log;

import com.annimon.stream.Stream;
import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.bus.events.MainActivityCommand;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.device.Device;
import com.example.buhalo.lazyir.device.ModuleSetting;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.service.BackgroundUtil;
import com.example.buhalo.lazyir.service.dto.TcpDto;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.network.udp.UdpBroadcastManager;
import com.example.buhalo.lazyir.view.UiCmds;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import lombok.Setter;
import lombok.Synchronized;

import static com.example.buhalo.lazyir.service.BackgroundServiceCmds.ON_DEVICE_DISCONNECTED;
import static com.example.buhalo.lazyir.service.network.tcp.TcpConnectionManager.api.INTRODUCE;
import static com.example.buhalo.lazyir.view.UiCmds.SET_SELECTED_ID;


/*
* class for connection work, it represent device connection, and pass messages to modules if needed.
* */
public class ConnectionThread implements Runnable {
    private static final String TAG = "ConnectionThread";
    private Socket connection;
    private Context context;
    private String deviceId;
    @Setter
    private boolean connectionRun;
    private BufferedReader in;
    private PrintWriter out;
    private ScheduledFuture<?> timerFuture;
    private MessageFactory messageFactory;
    private ModuleFactory moduleFactory;
    private PairService pairService;
    private DBHelper dbHelper;

    ConnectionThread(Socket socket, Context context, MessageFactory messageFactory, ModuleFactory moduleFactory, PairService pairService,DBHelper dbHelper) throws SocketException {
        this.connection = socket;
        this.context = context;
        this.messageFactory = messageFactory;
        this.moduleFactory = moduleFactory;
        this.pairService = pairService;
        this.dbHelper = dbHelper;
        // enabling keepAlive and timeout to close socket,
        // Android has some battery safe methods and act not fully predictable, you don't know,
        // when he run netowork code in background, so you need wait more.
        connection.setKeepAlive(true);
        connection.setSoTimeout(30000);
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             PrintWriter output = new PrintWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            in = input;
            out = output;
            setConnectionRun(true);
            while (isConnected()) {
                String clientCommand = in.readLine();
                if(clientCommand == null || !isConnected()){
                    return;
                }
                if (deviceId != null && clientCommand.equalsIgnoreCase(TcpConnectionManager.api.PING.name())) {
                    setDevicePing(true);
                    ping();
                    continue;
                }
                NetworkPackage np = messageFactory.parseMessage(clientCommand);
                determineWhatTodo(np);
            }
        } catch (IOException e) {
            Log.e(TAG, "error in connectionThread: " + deviceId, e);
        } finally {
            setConnectionRun(false);
            closeConnection(BackgroundUtil.getDevice(deviceId));
        }
    }

    private void ping() {
        printToOut(TcpConnectionManager.api.PING.name());
    }
    /*
   most important method based on package type determined what to do
   eat exception, we don't want to interrupt connection with device, so if something go wrong
   forgot, because if Device always send wrong cmd's TCP_PING won't be executed, and device
   will be disconnected on next pingCheck
   * */
    private void determineWhatTodo(NetworkPackage np) {
        try {
            String type = np.getType();
            boolean module = np.isModule();
            if (module) {
                setDevicePing(true); // most used case, order to specific modules
                commandFromClient(np); // modules may have specific values on json, on this stage we need to know only data & type values and id
            } else if (type.equalsIgnoreCase(TcpConnectionManager.api.TCP.name())) {
                receivedBaseCommand(np);
            }
        } catch (Exception e) {
            Log.e(TAG,"Error in DetermineWhatToDo  with package - " + np, e);
        }
    }

    private void receivedBaseCommand(NetworkPackage np) {
        TcpDto dto = (TcpDto) np.getData();
        TcpConnectionManager.api command = TcpConnectionManager.api.valueOf(dto.getCommand());
        // when deviceId null, first command need to be introduce
        if (deviceId == null && !command.equals(INTRODUCE)) {
            return;
        }
        switch (command) {
            case INTRODUCE:
                newConnectedDevice(np);
                break;
            case PAIR:
                pairService.receivePairRequest(np,context);
                break;
            case UNPAIR:
                pairService.receivePairSignal(np,context);
                break;
            case PAIR_RESULT:
                pairService.receivePairSignal(np,context);
                break;
            default:
                setDevicePing(true); // don't know command, but still have signal from device
                break;
        }
    }

    // instantiate new device, put it in map and check in db if it paired.
    // after that send introduce package
    private void newConnectedDevice(NetworkPackage np) {
            // if device not null - you already know about device, so no introduction
            if (deviceId != null) {
                return;
            }
        TcpDto dto = (TcpDto) np.getData();
        deviceId = np.getId();
        Device dv = BackgroundUtil.getDevice(deviceId);
        if (dv != null) {
            dv.closeConnection();
        }

        Device device = new Device(deviceId, np.getName(), connection.getInetAddress(), this, dto.getModuleSettings(), moduleFactory);
        device.enableModules();
        BackgroundUtil.addDeviceToConnected(deviceId, device);
        UdpBroadcastManager.getConnectedUdp().remove(deviceId);
        EventBus.getDefault().post(new MainActivityCommand(UiCmds.UPDATE_ACTIVITY,null));
        sendIntroduce();
    }

    private void commandFromClient(NetworkPackage np) {
        try {
            Device device = BackgroundUtil.getDevice(np.getId());
            if(device == null || !device.isPaired()) {
                return;
            }
            String moduleType = np.getType();
            ModuleSetting myModuleSetting = BackgroundUtil.getMyEnabledModules().get(moduleType);
            if (myModuleSetting == null || !myModuleSetting.isEnabled() || deviceInIgnore(myModuleSetting)) {
                return;
            }
            Module module = device.getEnabledModules().get(moduleType);
            if (module != null) {
                module.execute(np);
            }
        } catch (Exception e) {
            Log.e(TAG,"error in commandFromClient", e);
        }
    }

    private boolean deviceInIgnore(ModuleSetting myModuleSetting) {
        for (String s : myModuleSetting.getIgnoredId()) {    // isWorkOnly change ignore list to white list, so if s equal device id - when isWorkOnly true - it has in white list and return false
            if (s.equals(deviceId)) {                       // if isWorkOnly false so it is ignore list so if s equals id we must return true
                return !myModuleSetting.isWorkOnly();
            }
        }
        return false;
    }

    // get myId and send introduce package to device
    private void sendIntroduce() {
        String temp = String.valueOf(BackgroundUtil.getMyId().hashCode());
        TcpDto dto = new TcpDto(INTRODUCE.name(), temp);
        Collection<ModuleSetting> values = BackgroundUtil.getMyEnabledModules().values();
        dto.setModuleSettings(new ArrayList<>(values));
        String message = messageFactory.createMessage(TcpConnectionManager.api.TCP.name(), false, dto);
        printToOut(message);
    }

    // printing method,
    // it's print to server over tcp connection
    // locked to avoiding concurrency error's
    @Synchronized
    public void printToOut(String message) {
        if (out == null) {
            return;
        }
        out.println(message);
        out.flush();
    }


    @Synchronized
    private void closeConnection(Device device) {
        try {
            if (timerFuture != null && !timerFuture.isDone()) {
                timerFuture.cancel(true);
            }
            clearResources();
            if(device != null) {
                ConcurrentHashMap<String, Module> enabledModules = device.getEnabledModules();
                if(enabledModules != null) {
                    Stream.of(enabledModules.values()).forEach(Module::endWork);
                }
            }
            BackgroundUtil.removeDeviceConnected(deviceId);
            BackgroundUtil.addCommand(ON_DEVICE_DISCONNECTED,context);
            UdpBroadcastManager.getConnectedUdp().remove(deviceId);
        }catch (Exception e) {
            Log.e(TAG,"error in closeConnection id: " + deviceId,e);
        }
        finally {
            Log.d(TAG,String.format("%s stopped connection", deviceId));
        }
    }

    @Synchronized
    public void clearResources(){
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (connection != null) {
                connection.close();
            }
        }catch (IOException e){
            Log.e(TAG,"error in clearResources",e);
        }
    }

    //check if connected
    @Synchronized
    public boolean isConnected() {
        return connection != null && out != null && in != null
                && connectionRun && connection.isConnected() && !connection.isClosed()
                && !connection.isInputShutdown() && !connection.isOutputShutdown();
    }


    public Context getContext() {
        return context;
    }

    private void setDevicePing(boolean devicePing) {
        Device device = BackgroundUtil.getDevice(deviceId);
        if (device != null) {
            device.setAnswer(devicePing);
        }
    }
}
