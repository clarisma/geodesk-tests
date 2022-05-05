package com.geodesk.benchmark;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.Map;
import java.util.TreeMap;

public class SystemStats
{
    public static Map<String,String> get()
    {
        Map<String,String> stats = new TreeMap<>();
        stats.put("java-version", System.getProperty("java.version"));
        stats.put("java-vm", System.getProperty("java.vm.name"));

        stats.put("os", System.getProperty("os.name"));
        stats.put("logical-cores", String.valueOf(Runtime.getRuntime().availableProcessors()));

        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        stats.put("memory", hardware.getMemory().toString());
        stats.put("cpu", hardware.getProcessor().getProcessorIdentifier().getName());
        stats.put("physical-cores", String.valueOf(hardware.getProcessor().getPhysicalProcessorCount()));
        stats.put("clockspeed-ghz", String.valueOf(
            (double)hardware.getProcessor().getProcessorIdentifier().getVendorFreq() /
                1000000000.0));
        return stats;
    }

    public static void main(String[] args)
    {
        System.out.println(get());
    }
}
