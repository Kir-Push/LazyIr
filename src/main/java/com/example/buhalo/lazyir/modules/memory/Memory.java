package com.example.buhalo.lazyir.modules.memory;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.api.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Environment.MEDIA_BAD_REMOVAL;
import static android.os.Environment.MEDIA_NOFS;
import static android.os.Environment.MEDIA_REMOVED;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;


public class Memory extends Module {
    private static final String TAG = "Memory";
    public enum api{
        GET_FREE_MEM,
        GET_CRT
    }

    @Inject
    public Memory(MessageFactory messageFactory, Context context) {
        super(messageFactory, context);
    }

    @Override
    public void execute(NetworkPackage np) {
        MemoryDto dto = (MemoryDto) np.getData();
        api command = Memory.api.valueOf(dto.getCommand());
        switch (command){
            case GET_FREE_MEM:
                sendFreeMem();
                break;
            case GET_CRT:
                sendCrt();
                break;
            default:
                break;
        }
    }

    private void sendCrt() {
        ActivityManager actManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if(actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }
        int cpu = getCpuLoad();
        long ram = getAllRam(memInfo);
        long freeRam = getFreeRam(memInfo);
        boolean lowMem = getLowMem(memInfo);
        double temp = getTemp();
        CRTEntity crtEntity = new CRTEntity(cpu,freeRam,ram,temp, lowMem);
        String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, new MemoryDto(api.GET_CRT.name(), crtEntity));
        sendMsg(message);
    }


    private void sendFreeMem() {
        long freeMem = getFreeMemory();
        long allMemory = getAllMemory();
        List<MemPair> extMemory = getExtMemory();
        MemoryEntity memoryEntity = new MemoryEntity(allMemory,freeMem,extMemory);
        String message = messageFactory.createMessage(this.getClass().getSimpleName(), true, new MemoryDto(api.GET_CRT.name(), memoryEntity));
        sendMsg(message);
    }

    private double getTemp() {
        return -1;
    }

    private long getAllRam(ActivityManager.MemoryInfo memoryInfo) {
        return memoryInfo.totalMem;
    }

    private long getFreeRam(ActivityManager.MemoryInfo memoryInfo) {
        return memoryInfo.availMem;
    }

    private boolean getLowMem(ActivityManager.MemoryInfo memInfo) {
        return memInfo.lowMemory;
    }

    /*
    https://stackoverflow.com/questions/23261306/how-get-average-cpu-usage-in-android
    * */
    private int getCpuLoad() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            // only for versions fewer than Oreo, in which acces to proc/stat was denied
            try( RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r")) {
                String load = reader.readLine();
                String[] toks = load.split(" ");
                long idle1 = Long.parseLong(toks[5]);
                long total1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]) +
                        Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                        + Long.parseLong(toks[7]) + Long.parseLong(toks[8]) + Long.parseLong(toks[9]);
                Thread.sleep(50);

                reader.seek(0);
                load = reader.readLine();
                toks = load.split(" ");
                long idle2 = Long.parseLong(toks[5]);
                long total2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4]) +
                        Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                        + Long.parseLong(toks[7]) + Long.parseLong(toks[8]) + Long.parseLong(toks[9]);
                if ((total2 - total1) == 0)
                    return 0;
                if (idle2 - idle1 == 0)
                    return 100;
                return (int) (100 - (((float) (idle2 - idle1) / (total2 - total1)) * 100));
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Error in getCpuLoad", e);
                Thread.currentThread().interrupt();
            }
        }
            return 0;
    }



    private List<MemPair> getExtMemory() {
        List<MemPair> result = new ArrayList<>();
        String internalPath =    getExternalStorageDirectory().getPath();
        File[] externalFilesDirs = context.getExternalFilesDirs(null);
        for (File externalFilesDir : externalFilesDirs) {
            if(externalFilesDir != null) {
                String state = getExternalStorageState(externalFilesDir);
                if (state.equals(MEDIA_REMOVED) || state.equals(MEDIA_NOFS) || state.equals(MEDIA_BAD_REMOVAL) || externalFilesDir.getPath().startsWith(internalPath)) {
                    continue;
                }
                String path = externalFilesDir.getPath();
                long freeSpace = externalFilesDir.getFreeSpace();
                long space = externalFilesDir.getTotalSpace();
                result.add(new MemPair(path, state, space, freeSpace));
            }
        }
        return result;
    }

    private long getAllMemory() {
        File internalStorageFile= context.getFilesDir();
        return internalStorageFile.getTotalSpace();
    }

    private long getFreeMemory() {
        File internalStorageFile= context.getFilesDir();
        return internalStorageFile.getFreeSpace();
    }

    @Override
    public void endWork() {
        //nothing to do here
    }
}
