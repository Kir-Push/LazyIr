package com.example.buhalo.lazyir.modules.shareManager;

import android.os.Environment;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.Exception.ParseError;
import com.example.buhalo.lazyir.Exception.TcpError;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
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

    public static final String SHARE_T = "ShareModule";
    @Deprecated
    public static final String SHARE_TYPE = "share_type";
    public static final String SETUP_SERVER_AND_SEND_ME_PORT = "setup server and send me port";
    public static final String CONNECT_TO_ME_AND_RECEIVE_FILES = "connect to me and receive files"; // first arg port,second number of files - others files
    public static final String GET_PATH = "get path";  // actually it means list files on path;
    public static final String SEND_PATH = "post path";
    public static final String GET_FILE = "get file";
    public static final String POST_FILE = "post file";
    public static final String GET_DIRECTORY = "get directory";
    public static final String POST_DIRECORY = "post directory";


    public static final String FIRST_ARG_NUMBER_OF_FILES ="nf {?,?}";// first ? wil be you curr file, second overral files ( which you need to receive


    private Socket fileSocket;
    private InputStream in;
    private String serverPath;
    private String clientPath;
    private boolean waitingForServerAnswerForConnect = false;

    private boolean waitResponse;

    private List<FileWrap> responseList;
    private String rootPathFromServer;
    private String lastRootPathFromServer;

    @Override
    public void execute(NetworkPackage np) {
 //todo there is parse package // and actually think about threads
        System.out.println("share module execute  " + np.getData() + " " + waitResponse);
        if(np.getData().equals(SEND_PATH))
        {
            responseList = receiveFromServer(np);
            System.out.println("response list size " + responseList.size());
        }
        else if(np.getData().equals(CONNECT_TO_ME_AND_RECEIVE_FILES))
        {
            ParseDownload(np);
        }
    }

    public List<FileWrap> getFilesList(String path)
    {
        List<FileWrap> fileList = new ArrayList<>();
        Log.d("ShareModule", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files == null)
        {
            return fileList;
        }
        Log.d("ShareModule", "Size: "+ files.length);
        fileList.add(new FileWrap(false,false,"....."));
        for(int i =0;i<files.length;i++)
        {
            fileList.add(new FileWrap(false,files[i].isFile(),files[i].getName()));
        }
        return fileList;
    }

    public List<FileWrap> getFilesListFromServer(String path)
    {
        NetworkPackage np = new NetworkPackage();
        ArrayList<String> args = new ArrayList<>();
        args.add(path);
        np.setArgs(args);
        String fromTypeAndData;
        try {
           fromTypeAndData = np.createFromTypeAndData(SHARE_T, GET_PATH);
            TcpConnectionManager.getInstance().sendCommandToServer(MainActivity.selected_id,fromTypeAndData); // todo doit it throught background service not to call directly blja and check for errors
            responseList = null;
            waitResponse = true;
            int count =0;
            while(responseList == null || responseList.size() == 0) //todo only FOR TESTING FUCKCCCC IT EROR BEEN
            {
                if(count > 10)
                {
                    break;
                }
                System.out.println("SSSSSS " + responseList);
                Thread.sleep(200);
                count++;
            }
            waitResponse = false;
            if(path.equals("root") && responseList != null)
            {
                rootPathFromServer = lastRootPathFromServer;
            }
            if(responseList == null)
            {
                responseList = new ArrayList<>();
                responseList.add(new FileWrap(false,false,"....."));
            }
            return responseList;
        } catch (ParseError | TcpError | InterruptedException Error) {
            Error.printStackTrace();
        }
        return null;
    }

    public String getRootPath()
    {
        return Environment.getExternalStorageDirectory().toString();
    }

    public String receivedPackage(NetworkPackage np) // todo you need handle buffer overflow if in folder to many files // or simly set limit on size and not load more, in future you create more better
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

    public List<FileWrap> receiveFromServer(NetworkPackage np) // todo// when server send command file send ( or file request) client (and server) creates socket at other port , and receive or send file(eto pozvolit prinimatj fail v background i prodolzatj vipolnatj ostalnie komandi)
                                                               //todo// tcpConnectionManager pustj zanimaetsa toljo priemom comand, novij socket pustj budet v etom module(eto isklju4itelnjo ego rabota) = no zapuskaj 4erez background service -- posilaj emu intent s classom (statservice or so) i komandoj, i
                                                                //todo// on vizovet класс создаст поток и выйдет как работы из сервиса чтоб можно было работать в свернутом приложении!;
    {
        List<FileWrap> fileWraps = new ArrayList<>();
        List<String> args = np.getArgs();
        fileWraps.add(new FileWrap(false,false,"....."));
        int count=0;
        for(String arg : args)
        {
            count++;
            if(count == 1)
            {
                continue;
            }
            if(count == 2)
            {
                lastRootPathFromServer = arg;
                continue;
            }
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

    public synchronized File downloadFile(String path,String fileName)
    {
        if(fileSocket == null || !fileSocket.isConnected())
        {
            return null;
        }
        File file = new File(path,fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] bytes = new byte[16*1024];
            int count;
            while ((count = in.read(bytes)) > 0) {
                System.out.println("write " + count);

                    if(new String(bytes,0,count).equals("endFile!!!$$$!!!"))
                    {
                        System.out.println("file break    count "  + count);
                        System.out.println(bytes);
                        break;
                    }

                out.write(bytes, 0, count);
            }
            System.out.println("I'm in download file jaja   " + path + "   " + fileName);
            out.close();
           // in.close();//todo you close it in outer method
        } catch (IOException e) {
           Log.e("ShareModule",e.toString());
        }
        return file;
    }

    public List<File> ParseDownload(NetworkPackage np) // if np.data == CONNECT_TO_ME_AND_RECEIVE_FILES
    {
        List<String> args = np.getArgs();
        if(args.size() < 3)
        {
            return null;
        }
 //todo create thread
        int port = Integer.parseInt(args.get(1));
        String secondArgFirst = args.get(0);
        // todo handle this second arg;
        List<FileWrap> fileWraps = new ArrayList<>();
        for(int i = 2;i<args.size();i++)
        {
            String arg = args.get(i);
            String[] split = arg.split(">>");
            if(split.length > 1)
            {
                if(split[1].equals("file"))
                fileWraps.add(new FileWrap(true,split[1].equals("file"),split[0]));
            }
        }

        String id = np.getId();
        Device device = Device.getConnectedDevices().get(id);
        List<File> fileList = null;
        try {
            fileSocket = new Socket(device.getIp(),port);
            in = fileSocket.getInputStream();
            fileList = downloadFiles(clientPath, fileWraps, null);
            in.close();
            fileSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // todo ай я спать;
        return fileList;
    }

    public List<File> downloadFiles(String path,List<FileWrap> fileWraps,String externalPath)
    {
        List<File> downloadedFiles = new ArrayList<>(); //todo dodelatj


        for(FileWrap fileWrap : fileWraps)
        {
        //    if(fileWrap.isFile()) {//
            System.out.println("tut");
                downloadedFiles.add(downloadFile(path, fileWrap.getPath()));
       //     }
        }
        return downloadedFiles;
    }

    public String getRootPathFromServer() {
        return rootPathFromServer;
    }

    public void setRootPathFromServer(String rootPathFromServer) {
        this.rootPathFromServer = rootPathFromServer;
    }

    public void startDownloading(String currPath, String currPaths, List<FileWrap> files) {
        NetworkPackage np = new NetworkPackage();
        List<String> args = new ArrayList<>();
        args.add(currPaths);
        for(FileWrap fileWrap : files)
        {
            String fileOrNot;
            if(fileWrap.isFile())
            {
                fileOrNot = "file";
            }
            else
            {
                fileOrNot = "dir";
            }
            args.add(fileWrap.getPath()+">>"+fileOrNot);
        }
        np.setArgs(args);
        try {
            String fromTypeAndData = np.createFromTypeAndData(SHARE_T, SETUP_SERVER_AND_SEND_ME_PORT);
            TcpConnectionManager.getInstance().sendCommandToServer(MainActivity.selected_id,fromTypeAndData);
        } catch (ParseError|TcpError error) {
            Log.e("ShareModule",error.toString());
        }
        serverPath = currPaths;
        clientPath = currPath;
        waitingForServerAnswerForConnect = true;
    }
}
