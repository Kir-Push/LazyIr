package com.example.buhalo.lazyir.modules.shareManager;

import android.os.Environment;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.Exception.ParseError;
import com.example.buhalo.lazyir.Exception.TcpError;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by buhalo on 05.03.17.
 */
// in sharemodule different format, first argumert is json data of folder
    //todo create special parse in networkpackage for sharemodule, because json may contain :: itself, and you need check first letters to Sharemodule type and command  and parsing other string to json
    // second TODO maybe create all network packages to json? it will be cool and good;
public class ShareModule extends Module {

    @Deprecated
    public static final String SHARE_TYPE = "share_type";
    public static final String SETUP_SERVER_AND_SEND_ME_PORT = "setup server and send me port";
    public static final String GET_PATH = "get path";  // actually it means list files on path;
    public static final String SEND_PATH = "post path";
    public static final String GET_FILE = "get file";
    public static final String POST_FILE = "post file";
    public static final String GET_DIRECTORY = "get directory";
    public static final String POST_DIRECORY = "post directory";

    @Override
    public void execute(NetworkPackage np) {

    }

    public List<File> getFilesList(String path)
    {
        List<File> fileList = new ArrayList<>();
        Log.d("ShareModule", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files == null)
        {
            return fileList;
        }
        Log.d("ShareModule", "Size: "+ files.length);
        fileList.addAll(Arrays.asList(files));
        return fileList;
    }

    public String getRootPath()
    {
        return Environment.getExternalStorageDirectory().toString();
    }

    public String receivedPackage(NetworkPackage np) // todo you need handle buffer overflow if in folder to many files
    {
        return "";
    }
    public void sendGetPathToServer(FileWrap fw)
    {
        NetworkPackage np = new NetworkPackage();
        ArrayList<String> strings = new ArrayList<>();
        String fileorNot;
        if(fw.isFile())
        {
            fileorNot = "file";
        }
        else
        {
            fileorNot = "dir";
        }
        strings.add(fw.getPath()+">>"+fileorNot);
        np.setArgs(strings);
        try {
            String fromTypeAndData = np.createFromTypeAndData(ShareModule.class.getSimpleName(), GET_PATH);
            TcpConnectionManager.getInstance().sendCommandToServer(device.getId(),fromTypeAndData);
        } catch (ParseError | TcpError error) {
           Log.e("ShareModule",error.getMessage());
        }
    }

    public List<FileWrap> receiveFromServer(NetworkPackage np)
    {
        List<FileWrap> fileWraps = new ArrayList<>();
        List<String> args = np.getArgs();
        for(String arg : args)
        {
            String[] split = arg.split(">>");
            if(split.length > 1)
            {
                fileWraps.add(new FileWrap(true,split[1].equals("file"),split[0]));
            }
        }
        return fileWraps;
    }

    public void sendPathToServer(List<FileWrap> sendFiles)
    {
        List<String> args = new ArrayList<>();
        NetworkPackage np = new NetworkPackage();
        for(FileWrap fw : sendFiles)
        {
            String fileDir = fw.getPath();
            if(fw.isFile())
            {
                fileDir += ">>file";
            }
            else
            {
                fileDir += ">>dir";
            }
            args.add(fileDir);
        }
        String command = null;
        try {
            np.setArgs(args);
            command = np.createFromTypeAndData(ShareModule.class.getSimpleName(),SEND_PATH);
            TcpConnectionManager.getInstance().sendCommandToServer(device.getId(),command);
        } catch (ParseError | TcpError error) {
            Log.e("ShareModule",error.getMessage());
        }
    }

    public void serverWantPath(String path)
    {

    }
}
