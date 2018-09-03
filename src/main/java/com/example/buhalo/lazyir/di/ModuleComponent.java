package com.example.buhalo.lazyir.di;

import com.example.buhalo.lazyir.di.scope.ModuleScope;
import com.example.buhalo.lazyir.modules.battery.Battery;
import com.example.buhalo.lazyir.modules.clipboard.ClipBoard;
import com.example.buhalo.lazyir.modules.dbus.Mpris;
import com.example.buhalo.lazyir.modules.memory.Memory;
import com.example.buhalo.lazyir.modules.notification.call.CallModule;
import com.example.buhalo.lazyir.modules.notification.messengers.Messengers;
import com.example.buhalo.lazyir.modules.notification.notifications.ShowNotification;
import com.example.buhalo.lazyir.modules.notification.sms.SmsModule;
import com.example.buhalo.lazyir.modules.ping.Ping;
import com.example.buhalo.lazyir.modules.notification.reminder.Reminder;
import com.example.buhalo.lazyir.modules.sendcommand.SendCommand;
import com.example.buhalo.lazyir.modules.share.ShareModule;
import com.example.buhalo.lazyir.modules.touch.TouchControl;

import dagger.Subcomponent;

@Subcomponent()
@ModuleScope()
public interface ModuleComponent {

    Battery provideBattery();
    ClipBoard provideClipBoard();
    SendCommand provideSendCommand();
    Mpris provideMpris();
    Memory provideMemory();
    CallModule provideCallModule();
    Messengers provideMessengers();
    ShowNotification provideShowNotification();
    SmsModule provideSmsModule();
    Ping providePing();
    Reminder provideReminder();
    ShareModule provideShareModule();
    TouchControl provideTouchControl();
}