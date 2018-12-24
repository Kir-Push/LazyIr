package com.example.buhalo.lazyir.di;

import android.content.Context;

import com.example.buhalo.lazyir.AppBoot;
import com.example.buhalo.lazyir.api.DtoSerializer;
import com.example.buhalo.lazyir.api.MessageFactory;
import com.example.buhalo.lazyir.db.DBHelper;
import com.example.buhalo.lazyir.modules.ModuleFactory;
import com.example.buhalo.lazyir.modules.notification.CallSmsUtils;
import com.example.buhalo.lazyir.modules.notification.NotificationUtils;
import com.example.buhalo.lazyir.service.dto.NetworkDtoRegister;
import com.example.buhalo.lazyir.service.network.tcp.PairService;
import com.example.buhalo.lazyir.service.network.tcp.TcpConnectionManager;
import com.example.buhalo.lazyir.service.network.udp.UdpBroadcastManager;
import com.example.buhalo.lazyir.service.settings.SettingService;
import com.example.buhalo.lazyir.view.GuiCommunicator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ServiceModule {

    @Provides
    @Singleton
    public Context provideContect(AppBoot application){
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public PairService providePairService(DBHelper dbHelper, GuiCommunicator guiCommunicator, MessageFactory messageFactory){
        return new PairService(dbHelper,guiCommunicator,messageFactory);
    }

    @Provides
    @Singleton
    public DBHelper provideDbHelper(CallSmsUtils callSmsUtils,Context context){
        return new DBHelper(callSmsUtils,context);
    }

    @Provides
    @Singleton
    public GuiCommunicator provideGuiCommunicator(){
        return new GuiCommunicator();
    }

    @Provides
    @Singleton
    public MessageFactory provideMessageFactory(DtoSerializer dtoSerializer){
        return new MessageFactory(dtoSerializer);
    }

    @Provides
    @Singleton
    public DtoSerializer provideDtoSerializer(ModuleFactory moduleFactory, NetworkDtoRegister dtoRegister){
        return new DtoSerializer(moduleFactory,dtoRegister);
    }

    @Provides
    @Singleton
    public ModuleFactory provideModuleFactory(){
        return new ModuleFactory();
    }

    @Provides
    @Singleton
    public NetworkDtoRegister provideDtoRegister(){
        return new NetworkDtoRegister();
    }

    @Provides
    @Singleton
    public TcpConnectionManager provideTcpManager(MessageFactory messageFactory,ModuleFactory moduleFactory,PairService pairService,DBHelper dbHelper) {
        return new TcpConnectionManager(messageFactory,moduleFactory,pairService,dbHelper);
    }

    @Provides
    @Singleton
    public UdpBroadcastManager provideUdpManager(TcpConnectionManager tcp, MessageFactory messageFactory, SettingService settingService, Context context){
        return new UdpBroadcastManager(tcp,messageFactory,settingService,context);
    }

    @Provides
    @Singleton
    public SettingService provideSettingManager(Context context){
        return new SettingService(context);
    }

    @Provides
    @Singleton
    public NotificationUtils provideNotificationUtils(Context context){
        return new NotificationUtils(context);
    }

    @Provides
    @Singleton
    public CallSmsUtils provideSmsUtils(Context context,NotificationUtils notificationUtils){
        return new CallSmsUtils(context, notificationUtils);
    }
}
