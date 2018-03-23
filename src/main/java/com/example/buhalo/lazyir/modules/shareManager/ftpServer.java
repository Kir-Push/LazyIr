package com.example.buhalo.lazyir.modules.shareManager;

import android.content.Context;
import android.util.Log;

import com.example.buhalo.lazyir.Devices.NetworkPackage;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFileSystemView;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.UserManagerFactory;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.PropertiesUserManager;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by buhalo on 21.05.17.
 */

public class ftpServer {

    private FtpServer server;
    private int port;
    private static HashMap<String,String> user;
    private static HashMap<String,String> pass;
    public static boolean ftpServerOn = false;

    public int setupFtpServer(Context context, NetworkPackage np) {
        if(ftpServerOn) {
           return port;
        }
        user = new HashMap<>();
        pass = new HashMap<>();
        generateUserPass(np.getId());
        //fisrt try 9056 port
        port = 9056;
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port);
        NativeFileSystemFactory fileFactory = new NativeFileSystemFactory();
        serverFactory.setFileSystem(fileFactory);
        serverFactory.addListener("default", factory.createListener());
        serverFactory.setUserManager(new AndroidUserManager());
        server = serverFactory.createServer();
        try {
            server.start();
            ftpServerOn = true;
        } catch (FtpException e) {
            Log.e("Ftp",e.toString());
        }

        return port;
    }

    private void generateUserPass(String id) {
        if(!user.containsKey(id))
        {
            String usr = generateString();
            user.put(id,usr);
            pass.put(usr,generateString());
        }
    }

    private String generateString() {
        SecureRandom random = new SecureRandom();
        return  new BigInteger(64, random).toString(32);
    }



    public String getUser(String id) {
        return user.get(id);
    }

    public String getPass(String userName) {
        return pass.get(userName);
    }

    public void removeUsr(String id)
    {
        String usr = user.remove(id);
        if(usr != null)
        {
            pass.remove(usr);
        }
    }

    public void stopFtp()
    {
        ftpServerOn = false;
        server.stop();
        user.clear();
        pass.clear();
        user = null;
        pass = null;
    }

   static class AndroidUserManager  extends AbstractUserManager
   {

       public AndroidUserManager() {
           super();
       }


       @Override
       public User getUserByName(String userName) {
           if (!doesExist(userName)) {
               return null;
           }


           BaseUser user = new BaseUser();
           user.setName(userName);
        //   user.setPassword(pass.get(userName));
           user.setEnabled(true);
           user.setHomeDirectory("/");

           List<Authority> authorities = new ArrayList<Authority>();
           authorities.add(new WritePermission());


           int maxLogin = 0;
           int maxLoginPerIP = 0;
           authorities.add(new ConcurrentLoginPermission(maxLogin, maxLoginPerIP));

           int uploadRate = 0;
           int downloadRate = 0;
           authorities.add(new TransferRatePermission(downloadRate, uploadRate));

           user.setAuthorities(authorities);

           user.setMaxIdleTime(60);

           return user;
       }

       @Override
       public String[] getAllUserNames(){
           if(user.values().size() == 0)
           {
               return new String[0];
           }
           return (String[]) user.values().toArray();
       }

       @Override
       public void delete(String username) {
           pass.remove(username);
           String searchedKey = "";
           for (Map.Entry<String, String> stringStringEntry : user.entrySet()) {
               if(stringStringEntry.getValue().equals(username))
               {
                   searchedKey = stringStringEntry.getKey();
                   break;
               }
           }
           user.remove(searchedKey);

       }

       @Override
       public void save(User user)  {
         //? todo
       }

       @Override
       public boolean doesExist(String username) {
           return user.containsValue(username);
       }

       @Override
       public User authenticate(Authentication authentication) throws AuthenticationFailedException {
           if (authentication instanceof UsernamePasswordAuthentication) {
               UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;

               String user = upauth.getUsername();
               String password = upauth.getPassword();

               if (user == null) {
                   throw new AuthenticationFailedException("Authentication failed");
               }

               if (password == null) {
                   password = "";
               }

               User retrievedUsr = getUserByName(user);
               String storedPassword = retrievedUsr != null ? pass.get(user) : null;

               if (storedPassword == null) {
                   // user does not exist
                   throw new AuthenticationFailedException("Authentication failed");
               }

               if (storedPassword.equals(password)) {
                   return retrievedUsr;
               } else {
                   throw new AuthenticationFailedException("Authentication failed");
               }

           } else if (authentication instanceof AnonymousAuthentication) {
               if (doesExist("anonymous")) {
                   return getUserByName("anonymous");
               } else {
                   throw new AuthenticationFailedException("Authentication failed");
               }
           } else {
               throw new IllegalArgumentException(
                       "Authentication not supported by this user manager");
           }
       }
   }

}
