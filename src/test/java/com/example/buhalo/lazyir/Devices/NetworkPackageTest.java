package com.example.buhalo.lazyir.devices;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Created by buhalo on 20.11.17.
 */
@RunWith(Parameterized.class)
public class NetworkPackageTest {
    String type = "testData";
    String type2 = "testData2";
    String type3 = "testData3";
    String data = "data";
    String data3 = "data3";
    String data2 = "data2";

    public void clearCache(){
        try {
            Field counter = NetworkPackage.Cacher.class.getDeclaredField("usableCounter");
            Field cacher = NetworkPackage.Cacher.class.getDeclaredField("networkPackageCache");
            counter.setAccessible(true);
            cacher.setAccessible(true);
            Object count = counter.get(null);
            ((ConcurrentHashMap<Integer, NetworkPackage>)count).clear();
            Object cahce = cacher.get(null);
            ((ConcurrentHashMap<Integer, NetworkPackage>)cahce).clear();

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getHash() throws Exception {
       assertNotEquals( NetworkPackage.getHash(type,data),NetworkPackage.getHash(type2,data));
        assertNotEquals(NetworkPackage.getHash(type2,data2),NetworkPackage.getHash(type2,data));
        assertNotEquals(NetworkPackage.getHash(type3,data3),NetworkPackage.getHash(type,data3));
    }


    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[10][0]);
    }

    // test my simple caching for networkPackages
    @Test
    public void getOrCreatePackage() throws Exception {
        clearCache();
        NetworkPackage np = NetworkPackage.Cacher.getOrCreatePackage(type, data);
        NetworkPackage np2 = NetworkPackage.Cacher.getOrCreatePackage(type, data);
        NetworkPackage np5 =  NetworkPackage.Cacher.getOrCreatePackage(type, data2);
        NetworkPackage np6 =  NetworkPackage.Cacher.getOrCreatePackage(type2, data);
        NetworkPackage np3 = null;
        for(int i = 0;i<10;i++) {
            np3 = NetworkPackage.Cacher.getOrCreatePackage(type, data);
        }
        long before =  System.nanoTime();
        NetworkPackage np4 = NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type,data);
        NetworkPackage.Cacher.getOrCreatePackage(type, data);
        long after =  System.nanoTime();
        long aa1 = after-before;
        long before2 =  System.nanoTime();
        NetworkPackage np8 = NetworkPackage.Cacher.getOrCreatePackage(data3,data);
        NetworkPackage.Cacher.getOrCreatePackage(data3,data);
        NetworkPackage.Cacher.getOrCreatePackage(data3,data);
        NetworkPackage.Cacher.getOrCreatePackage(data3,data);
        NetworkPackage.Cacher.getOrCreatePackage(data3,data2);
        NetworkPackage.Cacher.getOrCreatePackage(data3,data2);
        NetworkPackage.Cacher.getOrCreatePackage(data3,data2);
        NetworkPackage.Cacher.getOrCreatePackage(type,data3);
        NetworkPackage.Cacher.getOrCreatePackage(type,data3);
        NetworkPackage.Cacher.getOrCreatePackage(type,data3);
        NetworkPackage.Cacher.getOrCreatePackage(type,data2);
        NetworkPackage.Cacher.getOrCreatePackage(type,data2);
        NetworkPackage.Cacher.getOrCreatePackage(type,data2);
        long after2 =  System.nanoTime();
        long aa2 = after2-before2;
        assertTrue(aa2 > aa1);
        assertTrue(np != np2);
        assertTrue(np3 == np4);
        assertTrue(np!= np5);
        assertTrue(np6 != np);
        assertTrue(np6 != np3);
        assertTrue(np5 != np4);
        assertTrue(np4 == NetworkPackage.Cacher.getOrCreatePackage(type, data));
        String typeT = null;
        String dataT = null;
        for(int ii = 0;ii<25;ii++) {
             typeT = Integer.toString(new Random().nextInt());
             dataT = Integer.toString(new Random().nextInt());
            for (int i = 0; i < 50; i++) {
                np3 = NetworkPackage.Cacher.getOrCreatePackage(typeT, dataT);
            }

        }
        assertTrue(np3 == NetworkPackage.Cacher.getOrCreatePackage(typeT,dataT));
        assertTrue(np3 != np4);
        assertTrue(np4 != NetworkPackage.Cacher.getOrCreatePackage(type, data));
    }

}