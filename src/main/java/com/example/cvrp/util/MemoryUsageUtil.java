package com.example.cvrp.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class MemoryUsageUtil {

    public static long getUsedMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        return heapMemoryUsage.getUsed();
    }

    public static void forceGarbageCollection() {
        System.gc();
        System.runFinalization();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.gc();
    }

}
