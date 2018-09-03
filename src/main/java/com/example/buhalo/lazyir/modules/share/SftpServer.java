package com.example.buhalo.lazyir.modules.share;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.buhalo.lazyir.service.settings.SettingService;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.SshFile;
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;


public class SftpServer {
    private static final String TAG = "SftpServer";
    private  SshServer sshd;
    @Getter @Setter
    private String user;
    @Getter @Setter
     private String pass;
    public int setupSftpServer(SettingService settingService, Context context){
        int port = Integer.parseInt(settingService.getValue("Sftp-port")); // 9000
        SecurityUtils.setRegisterBouncyCastle(false);
        sshd = SshServer.setUpDefaultServer();
        sshd.setKeyExchangeFactories(Arrays.asList(
                new DHG14.Factory(),
                new DHG1.Factory()));
        AbstractGeneratorHostKeyProvider hostKeyProvider = new SimpleGeneratorHostKeyProvider(context.getFilesDir() + "hostkey2.ser"); //here added getfilesdir
        hostKeyProvider.setAlgorithm("RSA");
        sshd.setKeyPairProvider(hostKeyProvider);
        sshd.setCommandFactory(new ScpCommandFactory());
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);
        sshd.setFileSystemFactory(new AndroidFileSystemFactory(context));
        user = android.os.Build.MODEL;
        pass = generatePass();
        SimplePasswordAuthenticator passAuth = new SimplePasswordAuthenticator();
        passAuth.setPassword(pass);
        passAuth.setUser(user);
        sshd.setPasswordAuthenticator(passAuth);
        boolean tryStartShh = true;
        while(tryStartShh && port < 9010) {
            try {
                sshd.setPort(port);
                sshd.start();
                tryStartShh = false;
            } catch (IOException e) {
                Log.e(TAG,"error while start sshd port: " + port);
                stopSftpServer();
                port++;
            }
        }
        return port;
    }

    private String generatePass() {
        SecureRandom random = new SecureRandom();
        return  new BigInteger(64, random).toString(32);
    }

    public void stopSftpServer() {
        try {
            if(sshd != null) {
                sshd.stop(true);
            }
        } catch (Exception e) {
            Log.e(TAG,"error while stopping sshd",e);
        }
    }


    static class AndroidFileSystemFactory implements FileSystemFactory {

        private final Context context;
        AndroidFileSystemFactory(Context context) {
            this.context = context;
        }
        @Override
        public FileSystemView createFileSystemView(final Session username) {
            return new AndroidFileSystemView(username.getUsername(), context);
        }
    }

    static class AndroidFileSystemView extends NativeFileSystemView {

        private final String userName;
        private final Context context;

        AndroidFileSystemView(final String userName, Context context) {
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

        private final Context context;
        private final File file;

        AndroidSshFile(final File file, final String userName, Context context) {
            super(file.getAbsolutePath(), file, userName);
            this.context = context;
            this.file = file;
        }

        @Override
        public boolean delete() {
            boolean ret = super.delete();
            if (ret) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(file));
                context.sendBroadcast(mediaScanIntent);
            }
            return ret;

        }

        @Override
        public boolean create() throws IOException {
            boolean ret = super.create();
            if (ret) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(file));
                context.sendBroadcast(mediaScanIntent);
            }
            return ret;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            AndroidSshFile that = (AndroidSshFile) o;
            return Objects.equals(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName());
        }
    }

    static class SimplePasswordAuthenticator implements PasswordAuthenticator {

        @Setter
        private String password;
        @Setter
        private String user;
        @Override
        public boolean authenticate(String user, String password, ServerSession session) {
            return user.equals(this.user) && password.equals(this.password);
        }
    }


}
