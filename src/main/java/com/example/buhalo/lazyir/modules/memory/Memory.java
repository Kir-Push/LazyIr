package com.example.buhalo.lazyir.modules.memory;

import android.app.ActivityManager;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.os.Environment.MEDIA_BAD_REMOVAL;
import static android.os.Environment.MEDIA_NOFS;
import static android.os.Environment.MEDIA_REMOVED;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

/**
 * Created by buhalo on 30.01.18.
 */

public class Memory extends Module {
    private final static String GET_FREE_MEM = "getFreeMem";
    private final static String GET_CRT = "getCrt";

    @Override
    public void execute(NetworkPackage np) {
        String data = np.getData();
        switch (data){
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
        ActivityManager actManager = (ActivityManager) BackgroundService.getAppContext().getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if(actManager != null)
        actManager.getMemoryInfo(memInfo);
        int cpu = getCpuLoad();
        long ram = getAllRam(memInfo);
        long freeRam = getFreeRam(memInfo);
        boolean lowMem = getLowMem(memInfo);
        double temp = getTemp();
        CRTEntity crtEntity = new CRTEntity(cpu,freeRam,ram,temp, lowMem);
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(Memory.class.getSimpleName(),GET_CRT);
        np.setObject(NetworkPackage.N_OBJECT,crtEntity);
        sendMsg(np.getMessage());
    }


    private void sendFreeMem() {
        long freeMem = getFreeMemory();
        long allMemory = getAllMemory();
        List<MemPair> extMemory = getExtMemory();
        MemoryEntity memoryEntity = new MemoryEntity(allMemory,freeMem,extMemory);
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(Memory.class.getSimpleName(),GET_FREE_MEM);
        np.setObject(NetworkPackage.N_OBJECT,memoryEntity);
        sendMsg(np.getMessage());
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
            try {
                RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
                String load = reader.readLine();

                String[] toks = load.split(" ");

                long idle1 =Long.parseLong(toks[5]);
                long total1 = Long.parseLong(toks[2])+ Long.parseLong(toks[3]) + Long.parseLong(toks[4]) +
                        Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                        + Long.parseLong(toks[7]) + Long.parseLong(toks[8]) + Long.parseLong(toks[9]);

                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    Log.e("Memory module","Error in getCpuLoad",e);}

                reader.seek(0);
                load = reader.readLine();
                reader.close();

                toks = load.split(" ");

                long idle2 =Long.parseLong(toks[5]);
                long total2 = Long.parseLong(toks[2])+ Long.parseLong(toks[3]) + Long.parseLong(toks[4]) +
                        Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                        + Long.parseLong(toks[7]) + Long.parseLong(toks[8]) + Long.parseLong(toks[9]);

                if ((total2 - total1) == 0)
                    return 0;
                if(idle2 - idle1 == 0)
                    return 100;
                return (int) (100-(((float)(idle2 - idle1) / ((total2 - total1)))*100));

            } catch (IOException e) {
                Log.e("Memory module","Error in getCpuLoad",e);
            }
            return 0;
    }



    private List<MemPair> getExtMemory() {
        List<MemPair> result = new ArrayList<>();
        String internalPath =    getExternalStorageDirectory().getPath();
        File[] externalFilesDirs = BackgroundService.getAppContext().getExternalFilesDirs(null);
        for (File externalFilesDir : externalFilesDirs) {
            if(externalFilesDir == null)
                continue;
            String state = getExternalStorageState(externalFilesDir);
            if(state.equals(MEDIA_REMOVED) || state.equals(MEDIA_NOFS) || state.equals(MEDIA_BAD_REMOVAL))
                continue;
            if(externalFilesDir.getPath().startsWith(internalPath))
                continue;
            String path = externalFilesDir.getPath();
            long freeSpace = externalFilesDir.getFreeSpace();
            long space = externalFilesDir.getTotalSpace();
            result.add(new MemPair(path,space,freeSpace,state));
        }
        return result;
    }

    private long getAllMemory() {
        File internalStorageFile= BackgroundService.getAppContext().getFilesDir();
        return internalStorageFile.getTotalSpace();
    }

    private long getFreeMemory() {
        File internalStorageFile= BackgroundService.getAppContext().getFilesDir();
        return internalStorageFile.getFreeSpace();
    }
}
