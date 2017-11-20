package com.example.buhalo.lazyir.modules.clipBoard;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.buhalo.lazyir.Devices.NetworkPackage;
import com.example.buhalo.lazyir.modules.Module;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;

/**
 * Created by buhalo on 20.11.17.
 */
@RunWith(AndroidJUnit4.class)
public class ClipBoardTest {
    private String data = "testData";
    private String data2 = "testData2";

    private void mockContext(Context context,Object obj){
        Field contextField = null;
        try {
            contextField = Module.class.getDeclaredField("context");
            contextField.setAccessible(true);
            contextField.set(obj,context);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void execute() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        ClipBoard cp = new ClipBoard();
        mockContext(appContext,cp);
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage("ClipBoard", "receive");
        final ClipData.Item[] item = new ClipData.Item[1];
        final ClipData.Item[] item2 = new ClipData.Item[1];
        final String[] text = new String[1];
        final String[] text2 = new String[1];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
      ()-> {
          final ClipboardManager clipboard = (ClipboardManager) appContext.getSystemService(Context.CLIPBOARD_SERVICE);
          np.setValue("text", data);
          cp.execute(np);
          ClipData clipData = clipboard.getPrimaryClip();
          item[0] = clipData.getItemAt(0);
          text[0] = item[0].getText().toString();
          np.setValue("text", data2);
          cp.execute(np);
          ClipData clipData2 = clipboard.getPrimaryClip();
          item2[0] = clipData2.getItemAt(0);
          text2[0] = item2[0].getText().toString();
      });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertNotNull(item[0]);
        assertNotNull(item[0].getText());
        assertNotNull(item2[0]);
        assertNotNull(item2[0].getText());
        assertEquals(text[0], data);
        assertEquals(text2[0],data2);
        assertNotEquals(text2[0], text[0]);
    }

}