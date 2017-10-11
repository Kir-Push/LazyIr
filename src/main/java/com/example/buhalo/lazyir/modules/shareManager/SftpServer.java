package com.example.buhalo.lazyir.modules.shareManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.filesystem.NativeFileSystemView;
import org.apache.sshd.server.filesystem.NativeSshFile;
import org.apache.sshd.server.kex.DHG1;
import org.apache.sshd.server.kex.DHG14;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by buhalo on 01.04.17.
 */

public class SftpServer {

    private  SshServer sshd;
    public static String USER;
    public String pass;
    public int setupSftpServer(Context context){
        int countTry = 0;
        int port = 9000;
        SecurityUtils.setRegisterBouncyCastle(false);
        sshd = SshServer.setUpDefaultServer();
     //   sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));

        sshd.setKeyExchangeFactories(Arrays.asList(
                new DHG14.Factory(),
                new DHG1.Factory()));

        AbstractGeneratorHostKeyProvider hostKeyProvider =
                new SimpleGeneratorHostKeyProvider(context.getFilesDir() + "hostkey2.ser"); //here added getfilesdir
        hostKeyProvider.setAlgorithm("RSA");
        sshd.setKeyPairProvider(hostKeyProvider);

//        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
//        userAuthFactories.add(new UserAuthNone.Factory());
    //    sshd.setUserAuthFactories(userAuthFactories);

        sshd.setCommandFactory(new ScpCommandFactory());

        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);
        sshd.setFileSystemFactory(new AndroidFileSystemFactory(context));
        USER = android.os.Build.MODEL;
        pass = generatePass();
        SimplePasswordAuthenticator passAuth = new SimplePasswordAuthenticator();
        passAuth.password = pass;
        sshd.setPasswordAuthenticator(passAuth);
        boolean tryStartShh = true;
        while(tryStartShh) {
            try {
                sshd.setPort(port);
                sshd.start();
                tryStartShh = false;
            } catch (Exception e) {
                Log.e("Sftp", e.toString());
                try {
                    sshd.stop(true);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                countTry++;
                Log.e("Sftp", "error, another tryin number: " + countTry);
                if (countTry < 10) {
                    port++;
                    tryStartShh = true;
                }
                else
                {
                    tryStartShh = false;
                }
            }
        }
        return port;
    }

    private String generatePass() {
        SecureRandom random = new SecureRandom();
        return  new BigInteger(64, random).toString(32);
    }

    public void stopSftpServer()
    {
        try {
            sshd.stop(true);
        } catch (InterruptedException e) {
            Log.e("Sftp",e.toString());
        }
    }


    static class AndroidFileSystemFactory implements FileSystemFactory {

        final private Context context;

        public AndroidFileSystemFactory(Context context) {
            this.context = context;
        }

        @Override
        public FileSystemView createFileSystemView(final Session username) {
            return new AndroidFileSystemView(username.getUsername(), context);
        }
    }

    static class AndroidFileSystemView extends NativeFileSystemView {

        final private String userName;
        final private Context context;

        public AndroidFileSystemView(final String userName, Context context) {
            super(userName, true);
            this.userName = userName;
            this.context = context;
        }

        @Override
        protected SshFile getFile(final String dir, final String file) {
            File fileObj = new File(dir, file);
            return new AndroidSshFile(fileObj, userName, context);
        }
    }

    static class AndroidSshFile extends NativeSshFile {

        final private Context context;
        final private File file;

        public AndroidSshFile(final File file, final String userName, Context context) {
            super(file.getAbsolutePath(), file, userName);
            this.context = context;
            this.file = file;
        }

        @Override
        public boolean delete() {
            //Log.e("Sshd", "deleting file");
            boolean ret = super.delete();
            if (ret) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(file));
                context.sendBroadcast(mediaScanIntent);
             //   MediaStoreHelper.indexFile(context, Uri.fromFile(file));
            }
            return ret;

        }

        @Override
        public boolean create() throws IOException {
            //Log.e("Sshd", "creating file");
            boolean ret = super.create();
            if (ret) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(file));
                context.sendBroadcast(mediaScanIntent);
            }
            return ret;

        }
    }

    static class SimplePasswordAuthenticator implements PasswordAuthenticator {

        public String password;

        @Override
        public boolean authenticate(String user, String password, ServerSession session) {
            return user.equals(SftpServer.USER) && password.equals(this.password);
        }
    }


}
