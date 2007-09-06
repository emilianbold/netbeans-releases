/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.ui.warmup;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/** Task executed early after startup to log diagnostic data about 
 * JVM - memory settings, JIT type, some hardware information.
 *
 * @author  Radim Kubacki
 */
public final class DiagnosticTask implements Runnable {

    public DiagnosticTask() {}
    
    /** Performs DnD pre-heat.
     */
    public void run() {
        String diagInfo = logParams();
        Logger.getLogger(DiagnosticTask.class.getName()).info(diagInfo);
        logEnv();
    }
    
    private void logEnv() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            // w/o dependency on Sun's JDK
            // long freeMem = ((com.sun.management.OperatingSystemMXBean)osBean).getTotalPhysicalMemorySize();
            Method m = osBean.getClass().getMethod("getTotalPhysicalMemorySize");
            m.setAccessible(true);
            long freeMem = (Long)m.invoke(osBean);
            Logger.getLogger(DiagnosticTask.class.getName()).log(Level.INFO, "Total memory {0}", freeMem);

            LogRecord lr = new LogRecord(Level.INFO, "MEMORY");
            lr.setResourceBundle(NbBundle.getBundle(DiagnosticTask.class));
            lr.setParameters(new Object[] {freeMem});
            Logger.getLogger("org.netbeans.ui.performance").log(lr);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(DiagnosticTask.class.getName()).log(Level.INFO, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(DiagnosticTask.class.getName()).log(Level.INFO, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DiagnosticTask.class.getName()).log(Level.INFO, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DiagnosticTask.class.getName()).log(Level.INFO, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(DiagnosticTask.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private void logMemoryUsage(StringBuilder sb, MemoryUsage usage, String label) {
//        long used, commited;
        long init, max;
        init = usage.getInit();
//        used = usage.getUsed();
//        commited = usage.getCommitted();
        max = usage.getMax();
//        sb.append(label).append(" usage: used ").append(formatBytes(used)) // NOI18N
//                .append(" of ").append(formatBytes(commited)); // NOI18N
        sb.append(label).append(" usage: initial ").append(formatBytes(init)) // NOI18N
                .append(" maximum ").append(formatBytes(max)).append('\n'); // NOI18N
    }
    
    /** Format the number to readable string using kB or MB.
     */
    private String formatBytes(long bytes) {
        if (bytes  > 1024L * 1024L) {
            return MessageFormat.format("{0,number,0.0MB}", bytes / 1024.0 / 1024.0); // NOI18N
        } else if (bytes  > 1024L) {
            return MessageFormat.format("{0,number,0.0kB}", bytes / 1024.0); // NOI18N
        } else {
            return MessageFormat.format("{0,number,0b}", bytes); // NOI18N
        }
    }

    private String formatTime(long time) {
        StringBuilder sb = new StringBuilder();
        if (time  > 1000L * 60L * 60L * 24L) {
            sb.append(MessageFormat.format("{0,number,0d}", time/ (1000L * 60L * 60L * 24L))); // NOI18N
            time %= 1000L * 60L * 60L * 24L;
        } 
        if (time  > 1000L * 60L * 60L || sb.length() > 0) {
            sb.append(MessageFormat.format("{0,number,0h}", time/ (1000L * 60L * 60L))); // NOI18N
            time %= 1000L * 60L * 60L;
        } 
        if (time  > 1000L * 60L || sb.length() > 0) {
            sb.append(MessageFormat.format("{0,number,0m}", time/ (1000L * 60L))); // NOI18N
            time %= 1000L * 60L;
        }
        sb.append(MessageFormat.format("{0,number,0s}", time/ 1000L)); // NOI18N
        return sb.toString();
    }

    private String logParams() {
        StringBuilder sb = new StringBuilder(500);
        sb.append("Diagnostic information\n");
        try {
            RuntimeMXBean         rmBean     = ManagementFactory.getRuntimeMXBean();
            CompilationMXBean     cmpMBean   = ManagementFactory.getCompilationMXBean();
//            ThreadMXBean          tmBean     = ManagementFactory.getThreadMXBean();
            MemoryMXBean          memoryBean = ManagementFactory.getMemoryMXBean();
            ClassLoadingMXBean    clMBean    = ManagementFactory.getClassLoadingMXBean();

//            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//            ObjectName hsDiag = new ObjectName("com.sun.management:name=HotSpotDiagnostic");
//            com.sun.management.OperatingSystemMXBean sunOSMBean  =
//               ManagementFactory.getSunOperatingSystemMXBean();

//            Map<String, String> props = new TreeMap<String, String>(rmBean.getSystemProperties());
//            System.out.println("System properties");
//            for (Map.Entry<String, String> entry: props.entrySet()) {
//                System.out.println("Property: "+entry.getKey()+" Value: "+entry.getValue());
//            }
            sb.append("Input arguments:");
            for (String s: rmBean.getInputArguments()) {
                sb.append("\n\t").append(s);
            }
            
            sb.append("\nCompiler: "+cmpMBean.getName()).append('\n');
            
            // Memory
            MemoryUsage usage = memoryBean.getHeapMemoryUsage();
            logMemoryUsage(sb, usage, "Heap memory");
            usage = memoryBean.getNonHeapMemoryUsage();
            logMemoryUsage(sb, usage, "Non heap memory");
            for (GarbageCollectorMXBean gcMBean: ManagementFactory.getGarbageCollectorMXBeans()) {
                sb.append("Garbage collector: ").append(gcMBean.getName())
                        .append(" (Collections=").append(gcMBean.getCollectionCount())
                        .append(" Total time spent=").append(formatTime(gcMBean.getCollectionTime()))
                        .append(")\n");
            }
            
            // classes
            int clsLoaded;
            long clsTotal, clsUnloaded;
            clsLoaded = clMBean.getLoadedClassCount();
            clsTotal = clMBean.getTotalLoadedClassCount();
            clsUnloaded = clMBean.getUnloadedClassCount();
            sb.append("Classes: loaded=").append(clsLoaded)
                    .append(" total loaded=").append(clsTotal)
                    .append(" unloaded ").append(clsUnloaded).append('\n');

//        } catch (MalformedObjectNameException ex) {
//            Logger.getLogger("global").log(Level.WARNING, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(DiagnosticTask.class.getName()).log(Level.WARNING, null, ex);
        }
        return sb.toString();
    }
    
}
