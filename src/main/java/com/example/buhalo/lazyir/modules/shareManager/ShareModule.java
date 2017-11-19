package com.example.buhalo.lazyir.modules.shareManager;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.buhalo.lazyir.Devices.Device;
import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.MainActivity;
import com.example.buhalo.lazyir.modules.Module;
import com.example.buhalo.lazyir.service.BackgroundService;
import com.example.buhalo.lazyir.service.TcpConnectionManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.example.buhalo.lazyir.modules.shareManager.ftpServer.ftpServerOn;

/**
 * Created by buhalo on 05.03.17.
 */
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
    public static final String PORT = "port";


    public static final String FIRST_ARG_NUMBER_OF_FILES ="nf {?,?}";// first ? wil be you curr file, second overral files ( which you need to receive
    public static final String CONNECT_TO_ME_AND_RECEIVE_FILES_REVERSE = "CONNECT_TO_ME_AND_RECEIVE_FILES_REVERSE";


    private Socket fileSocket;
    private DataInputStream in;
    private String serverPath;
    private String clientPath;
    private boolean waitingForServerAnswerForConnect = false;
    private static SftpServer sftpServer;
    private static ftpServer ftpServer;
    private static boolean sftServerOn = false;
    private static int port = 0;
    private static int portFtp = 0;

    private boolean waitResponse;

    private List<FileWrap> responseList;
    private String rootPathFromServer;
    private String lastRootPathFromServer;

    @Override
    public void execute(NetworkPackage np) {
        if(np.getData().equals(SEND_PATH))
        {
            responseList = receiveFromServer(np);
        }
        else if(np.getData().equals(CONNECT_TO_ME_AND_RECEIVE_FILES))
        {
            ParseDownload(np);
        }
        else if(np.getData().equals(SETUP_SERVER_AND_SEND_ME_PORT))
        {
            if(np.getValue("os").equals("nix"))
            setupSftp(np,context.getApplicationContext());
            else if(np.getValue("os").equals("win"))
            setupftp(np,context.getApplicationContext());
        }
    }

    @Override
    public void endWork() {

    }

    private void setupftp(NetworkPackage np, Context context) {
        if(ftpServer == null)
        {
            ftpServer = new ftpServer();
        }
        NetworkPackage pack = NetworkPackage.Cacher.getOrCreatePackage(SHARE_T,CONNECT_TO_ME_AND_RECEIVE_FILES);
        portFtp = ftpServer.setupFtpServer(context,np);
        if(portFtp == 0)
            portFtp = 9000;
        pack.setValue(PORT,Integer.toString(portFtp));
        String userName = ftpServer.getUser(np.getId());
        pack.setValue("userName",userName);
        pack.setValue("pass",ftpServer.getPass(userName));
        BackgroundService.sendToDevice(np.getId(),pack.getMessage());
    }

    public static void setupSftp(NetworkPackage np, Context context) {
        if (sftpServer == null) {
            sftpServer = new SftpServer();
        }
        NetworkPackage pack = NetworkPackage.Cacher.getOrCreatePackage(SHARE_T,CONNECT_TO_ME_AND_RECEIVE_FILES);
        if(!sftServerOn) {
            port = sftpServer.setupSftpServer(context);
            sftServerOn = true;
        }
        if(port == 0)
            port = 9000;
        pack.setValue(PORT,Integer.toString(port));
        pack.setValue("userName",SftpServer.USER);
        pack.setValue("pass",sftpServer.pass);
        BackgroundService.sendToDevice(np.getId(),pack.getMessage());
    }

    public static void stopSftpServer()
    {
        if(sftpServer!= null)
        {
            sftpServer.stopSftpServer();
         //   sftpServer = null;
        }
        sftServerOn = false;
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
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SHARE_T, GET_PATH);
        FileWrap fileWrap = new FileWrap(false,false,path);
        FileWraps fileWraps = new FileWraps();
        fileWraps.addCommand(fileWrap);
        np.setObject(NetworkPackage.N_OBJECT,fileWraps);
        String fromTypeAndData;
        try {
           fromTypeAndData = np.getMessage();
            BackgroundService.sendToDevice(MainActivity.getSelected_id(),fromTypeAndData);
            responseList = null;
            waitResponse = true;
            int count =0;
            while(responseList == null || responseList.size() == 0)
            {
                if(count > 10)
                {
                    break;
                }
                Thread.sleep(500);
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
        } catch (InterruptedException Error) {
            Error.printStackTrace();
        }
        return null;
    }

    public String getRootPath()
    {
        return Environment.getExternalStorageDirectory().toString();
    }

    public String receivedPackage(NetworkPackage np)
    {

        return "";
    }


    public List<FileWrap> receiveFromServer(NetworkPackage np)
    {
        List<FileWrap> fileWraps = new ArrayList<>();
        List<FileWrap> args = np.getObject(NetworkPackage.N_OBJECT,FileWraps.class).getFiles();
        fileWraps.add(new FileWrap(false,false,"....."));
        int count=0;
        for(FileWrap arg : args)
        {
            count++;
            if(count == 1)
            {
                continue;
            }
            if(count == 2)
            {
                lastRootPathFromServer = arg.getPath();
                continue;
            }
            fileWraps.add(new FileWrap(true,arg.isFile(),arg.getPath()));
        }
        return fileWraps;
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
            String name = in.readUTF();
            long fileSize = in.readLong();
            while (fileSize > 0 && (count = in.read(bytes, 0, (int)Math.min(bytes.length, fileSize))) != -1) {
                System.out.println("write " + count);

                final int finalCount = count;
//                ((Activity)device.getContext().getApplicationContext()).runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(device.getContext(),"Download: " + finalCount,Toast.LENGTH_SHORT).show();
//                    }
//                });


                    if(new String(bytes,0,count).equals("endFile!!!$$$!!!"))
                    {
                        System.out.println("file break    count "  + count);
                        System.out.println(bytes);
                        break;
                    }

                out.write(bytes, 0, count);
                fileSize -= count;
            }
            System.out.println("I'm in download file jaja   " + path + "   " + fileName);
          //  Toast.makeText(device.getContext(),"Download: Done",Toast.LENGTH_SHORT).show();
            out.close();
        } catch (IOException e) {
           Log.e("ShareModule",e.toString());
        }
        return file;
    }

    private List<File> fileList;

    public List<File> ParseDownload(final NetworkPackage np) // if np.data == CONNECT_TO_ME_AND_RECEIVE_FILES
    {
        final List<FileWrap> args = np.getObject(NetworkPackage.N_OBJECT,FileWraps.class).getFiles();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int port = Integer.parseInt(np.getValue(PORT));
                String secondArgFirst = args.get(0).getPath();
//                List<FileWrap> fileWraps = new ArrayList<>();
//                for(int i = 0;i<args.size();i++)
//                {
//                    String arg = args.get(i).getPath();
//                        if(args.get(i).isFile()){
//                            fileWraps.add(new FileWrap(true,args.get(i).isFile(),args.get(i).getPath()));
//                    }
//                }

                String id = np.getId();
                Device device = Device.getConnectedDevices().get(id);
                try {
                    fileSocket = new Socket(device.getSocket().getInetAddress(),port);
                    in = new DataInputStream(new BufferedInputStream(fileSocket.getInputStream()));
                    fileList= downloadFiles(clientPath, args, null);
                    in.close();
                    fileSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return fileList;
    }

    public List<File> downloadFiles(String path,List<FileWrap> fileWraps,String externalPath)
    {
        List<File> downloadedFiles = new ArrayList<>();


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
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(SHARE_T, SETUP_SERVER_AND_SEND_ME_PORT);
        List<FileWrap> args = new ArrayList<>();
        args.add(new FileWrap(false,false,currPaths));
        for(FileWrap fileWrap : files)
        {

            args.add(fileWrap);
        }
        FileWraps fileWraps = new FileWraps(args);
        np.setObject(NetworkPackage.N_OBJECT,fileWraps);
        String fromTypeAndData = np.getMessage();
        BackgroundService.sendToDevice(MainActivity.getSelected_id(),fromTypeAndData);
        serverPath = currPaths;
        clientPath = currPath;
        waitingForServerAnswerForConnect = true;
    }

    public void startSending(String currPaths, String currPath, final List<FileWrap> checked) {

        serverPath = currPaths;
        clientPath = currPath;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket serverSocket  = new ServerSocket(5675)) {
                    fileSocket = serverSocket.accept();
                    System.out.println("commected " + fileSocket.getLocalAddress());
                    String currPath = checked.get(0).getPath();
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fileSocket.getOutputStream()));
                    for(int i = 0; i< checked.size(); i++)
                    {
                        String s = checked.get(i).getPath();
                         System.out.println(currPath +"/" +  s);
                        File file = new File(clientPath,s);
                        try {
                            FileInputStream in = new FileInputStream(file);
                            byte[] bytes = new byte[16*1024];
                            int count;
                            out.writeUTF(s);
                            System.out.println("Write +" + s);
                            out.writeLong(file.length());
                            while ((count = in.read(bytes)) > 0) {
                                out.write(bytes, 0, count);
                                out.flush();
                            //    Toast.makeText(device.getContext(),"Sending:" + count,Toast.LENGTH_SHORT).show();

                            }
                            in.close();
                          //  Toast.makeText(device.getContext(),"Sending: Done",Toast.LENGTH_SHORT).show();
                            //   System.out.println("endFile!!!$$$!!!".getBytes());

                        } catch (IOException e) {
                            Log.e("ShareModule",e.toString());
                        }
                    }
                    if(out != null)
                        out.close();

                }catch (IOException e)
                {
                    Log.e("SHAREMODULE",e.toString());
                }
            }
        }).start();
        try {
            NetworkPackage napa = NetworkPackage.Cacher.getOrCreatePackage(SHARE_T, CONNECT_TO_ME_AND_RECEIVE_FILES_REVERSE);
            napa.setValue(PORT,"5675");
            napa.setObject(NetworkPackage.N_OBJECT,new FileWraps(checked));
            napa.setValue("Whom",clientPath);
            napa.setValue("Where",serverPath);
            String fromTypeAndData = napa.getMessage();
            BackgroundService.sendToDevice(device.getId(),fromTypeAndData);
        } catch (Exception error) {
            Log.e("SHAREMANAGER",error.toString());
        }
    }
 }

