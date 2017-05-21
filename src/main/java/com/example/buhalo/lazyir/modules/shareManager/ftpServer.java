package com.example.buhalo.lazyir.modules.shareManager;

import android.content.Context;
import android.util.Log;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFileSystemView;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.UserManagerFactory;
import org.apache.ftpserver.usermanager.impl.PropertiesUserManager;

import java.io.File;

/**
 * Created by buhalo on 21.05.17.
 */

public class ftpServer {

    private FtpServer server;
    private int port;

    public int setupFtpServer(Context context)
    {
        //fisrt try 9056 port
        port = 9056;
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);
        NativeFileSystemFactory fileFactory = new NativeFileSystemFactory();
        serverFactory.setFileSystem(fileFactory);
        UserManagerFactory userFactory = new PropertiesUserManagerFactory();

        UserManager user = new PropertiesUserManager() {
            @Override
            public User getUserByName(String username) throws FtpException {
                return null;
            }

            @Override
            public String[] getAllUserNames() throws FtpException {
                return new String[0];
            }

            @Override
            public void delete(String username) throws FtpException {

            }

            @Override
            public void save(User user) throws FtpException {

            }

            @Override
            public boolean doesExist(String username) throws FtpException {
                return false;
            }

            @Override
            public User authenticate(Authentication authentication) throws AuthenticationFailedException {
                return null;
            }

            @Override
            public String getAdminName() throws FtpException {
                return null;
            }

            @Override
            public boolean isAdmin(String username) throws FtpException {
                return false;
            }
        }
        // replace the default listener
        serverFactory.addListener("default", factory.createListener());
        server = serverFactory.createServer();
        try {
            server.start();
        } catch (FtpException e) {
            Log.e("Ftp",e.toString());
        }

        return port;
    }

    static class AndroidFileSystemFactory extends NativeFileSystemFactory
    {
        final private Context context;

        AndroidFileSystemFactory(Context context) {
            this.context = context;
        }

        @Override
        public FileSystemView createFileSystemView(User user) throws FtpException {
            return new AndroidFileSystemView(user,context);
        }
    }

    static class AndroidFileSystemView extends NativeFileSystemView
    {
        final private Context context;
        final private User user;

        protected AndroidFileSystemView(User user,Context context) throws FtpException {
            super(user);
            this.user = user;
            this.context = context;
        }

        public AndroidFileSystemView(User user, boolean caseInsensitive,Context context) throws FtpException {
            super(user, caseInsensitive);
            this.user = user;
            this.context = context;
        }

        @Override
        public FtpFile getFile(String file) {
            File fileObj = new File(file);
            return new AndroidFtpFile(file,fileObj,user,context);
        }
    }

    static class AndroidFtpFile extends NativeFtpFile {

        final private Context context;
        final private File file;

        public AndroidFtpFile(String fileName, File file, User user,Context context) {
            super(fileName, file, user);
            this.context = context;
            this.file = file;
        }




        @Override
        public boolean delete() {
            return super.delete();
        }
    }

}
