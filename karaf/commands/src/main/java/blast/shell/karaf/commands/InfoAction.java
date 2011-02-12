/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package blast.shell.karaf.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.AbstractAction;
import org.fusesource.jansi.Ansi;

import java.lang.management.*;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Command(scope = "shell", name = "info", description = "Prints system informations")
public class InfoAction extends AbstractAction {
    private String appName = "Karaf";

    Map<String, String> systemPropertyToDisplayName = new LinkedHashMap<String, String>();

    private NumberFormat fmtI = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.ENGLISH));
    private NumberFormat fmtD = new DecimalFormat("###,##0.000", new DecimalFormatSymbols(Locale.ENGLISH));

    protected Object doExecute() throws Exception {
        int maxNameLen;

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();

        //
        // print Karaf informations
        //
        maxNameLen = 25;
        System.out.println(appName);

        System.out.println();

        for (Map.Entry<String, String> entry : systemPropertyToDisplayName.entrySet()) {
            printValue(entry.getValue(), maxNameLen, System.getProperty(entry.getKey()));
        }
        System.out.println("JVM");
        printValue("Java Virtual Machine", maxNameLen, runtime.getVmName() + " version " + runtime.getVmVersion());
        printValue("Version", maxNameLen, System.getProperty("java.version"));
        printValue("Vendor", maxNameLen, runtime.getVmVendor());
        printValue("Uptime", maxNameLen, printDuration(runtime.getUptime()));
        Long processCpuTime = getProcessCpuTime(os);
        if (processCpuTime != null) {
            printValue("Process CPU time", maxNameLen, printDuration(processCpuTime / 1000000));
        } else {
            printValue("Process CPU time", maxNameLen, "(unavailable)");
        }
        printValue("Total compile time", maxNameLen, printDuration(ManagementFactory.getCompilationMXBean().getTotalCompilationTime()));

        System.out.println("Threads");
        printValue("Live threads", maxNameLen, Integer.toString(threads.getThreadCount()));
        printValue("Daemon threads", maxNameLen, Integer.toString(threads.getDaemonThreadCount()));
        printValue("Peak", maxNameLen, Integer.toString(threads.getPeakThreadCount()));
        printValue("Total started", maxNameLen, Long.toString(threads.getTotalStartedThreadCount()));

        System.out.println("Memory");
        printValue("Current heap size", maxNameLen, printSizeInKb(mem.getHeapMemoryUsage().getUsed()));
        printValue("Maximum heap size", maxNameLen, printSizeInKb(mem.getHeapMemoryUsage().getMax()));
        printValue("Committed heap size", maxNameLen, printSizeInKb(mem.getHeapMemoryUsage().getCommitted()));
        printValue("Pending objects", maxNameLen, Integer.toString(mem.getObjectPendingFinalizationCount()));
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            String val = "Name = '" + gc.getName() + "', Collections = " + gc.getCollectionCount() + ", Time = " + printDuration(gc.getCollectionTime());
            printValue("Garbage collector", maxNameLen, val);
        }

        System.out.println("Classes");
        printValue("Current classes loaded", maxNameLen, printLong(cl.getLoadedClassCount()));
        printValue("Total classes loaded", maxNameLen, printLong(cl.getTotalLoadedClassCount()));
        printValue("Total classes unloaded", maxNameLen, printLong(cl.getUnloadedClassCount()));

        System.out.println("Host system");
        printValue("OS", maxNameLen, os.getName() + " version " + os.getVersion());
        printValue("Architecture", maxNameLen, os.getArch());
        printValue("Processors", maxNameLen, Integer.toString(os.getAvailableProcessors()));
        printValue("Load average", maxNameLen, printDouble(os.getSystemLoadAverage()));
        try {
            try {
                this.getClass().getClassLoader().loadClass("com.sun.management.OperatingSystemMXBean");
                com.sun.management.OperatingSystemMXBean sunOs = (com.sun.management.OperatingSystemMXBean) os;
                printValue("Total physical memory", maxNameLen, printSizeInKb(sunOs.getTotalPhysicalMemorySize()));
                printValue("Free physical memory", maxNameLen, printSizeInKb(sunOs.getFreePhysicalMemorySize()));
                printValue("Committed virtual memory", maxNameLen, printSizeInKb(sunOs.getCommittedVirtualMemorySize()));
                printValue("Total swap space", maxNameLen, printSizeInKb(sunOs.getTotalSwapSpaceSize()));
                printValue("Free swap space", maxNameLen, printSizeInKb(sunOs.getFreeSwapSpaceSize()));
            } catch (ClassNotFoundException e) {
                // we're not on a Sun JVM
                try {
                    printValue("Total physical memory", maxNameLen, printSizeInKb(getSunOsValueAsLong(os, "getTotalPhysicalMemorySize")));
                    printValue("Free physical memory", maxNameLen, printSizeInKb(getSunOsValueAsLong(os, "getFreePhysicalMemorySize")));
                    printValue("Committed virtual memory", maxNameLen, printSizeInKb(getSunOsValueAsLong(os, "getCommittedVirtualMemorySize")));
                    printValue("Total swap space", maxNameLen, printSizeInKb(getSunOsValueAsLong(os, "getTotalSwapSpaceSize")));
                    printValue("Free swap space", maxNameLen, printSizeInKb(getSunOsValueAsLong(os, "getFreeSwapSpaceSize")));
                } catch (Throwable t) {
                    printValue("Memory information unavailable", maxNameLen, "(" + t.getMessage() + ")");
                }

            }
        } catch (Throwable t) {
            printValue("Memory information unavailable", maxNameLen, "(" + t.getMessage() + ")");
        }


        return null;
    }

    private Long getProcessCpuTime(OperatingSystemMXBean os) {
        Long processCpuTime = null;
        try {
            this.getClass().getClassLoader().loadClass("com.sun.management.OperatingSystemMXBean");
            com.sun.management.OperatingSystemMXBean sunOs = (com.sun.management.OperatingSystemMXBean) os;
            processCpuTime = sunOs.getProcessCpuTime();
        } catch (Throwable t) {
            try {
                processCpuTime = getSunOsValueAsLong(os, "getProcessCpuTime");
            } catch (Throwable t2) {
                // ignore.  We'll report this metric as unavailable.
            }
        }
        return processCpuTime;
    }

    /**
     * Oddly enough, this doesn't work on Mac OS's; it throws an exception with message:
     * Class blast.shell.karaf.commands.InfoAction can not access a member of class com.sun.management.UnixOperatingSystem with modifiers "public native"
     *
     * @param os
     * @param name
     * @return
     * @throws Exception
     */
    private long getSunOsValueAsLong(OperatingSystemMXBean os, String name) throws Exception {
        Method mth = os.getClass().getMethod(name);
        return (Long) mth.invoke(os);
    }

    private String printLong(long i) {
        return fmtI.format(i);
    }

    private String printSizeInKb(double size) {
        return fmtI.format((long) (size / 1024)) + " kbytes";
    }

    private String printDouble(double d) {
        return fmtD.format(d);
    }

    private String printDuration(double uptime) {
        uptime /= 1000;
        if (uptime < 60) {
            return fmtD.format(uptime) + " seconds";
        }
        uptime /= 60;
        if (uptime < 60) {
            long minutes = (long) uptime;
            String s = fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            return s;
        }
        uptime /= 60;
        if (uptime < 24) {
            long hours = (long) uptime;
            long minutes = (long) ((uptime - hours) * 60);
            String s = fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
            if (minutes != 0) {
                s += " " + fmtI.format(minutes) + (minutes > 1 ? " minutes" : "minute");
            }
            return s;
        }
        uptime /= 24;
        long days = (long) uptime;
        long hours = (long) ((uptime - days) * 60);
        String s = fmtI.format(days) + (days > 1 ? " days" : " day");
        if (hours != 0) {
            s += " " + fmtI.format(hours) + (hours > 1 ? " hours" : "hour");
        }
        return s;
    }

    void printSysValue(String prop, int pad) {
        printValue(prop, pad, System.getProperty(prop));
    }

    void printValue(String name, int pad, String value) {
        System.out.println(Ansi.ansi().a("  ")
                .a(Ansi.Attribute.INTENSITY_BOLD).a(name).a(spaces(pad - name.length())).a(Ansi.Attribute.RESET)
                .a("   ").a(value).toString());
    }

    String spaces(int nb) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nb; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void addSystemProperty(String propertyKey, String displayName) {
        this.systemPropertyToDisplayName.put(propertyKey, displayName);
    }

    public void setSystemPropertyToDisplayName(Map<String, String> systemPropertyToDisplayName) {
        this.systemPropertyToDisplayName = systemPropertyToDisplayName;
    }
}
