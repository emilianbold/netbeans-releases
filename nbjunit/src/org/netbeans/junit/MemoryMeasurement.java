/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * MemoryMeasurement.java
 *
 * Created on August 7, 2003, 4:53 PM
 */

package org.netbeans.junit;
import java.io.*;

/** Class with static methods for measuring memory footprint of a process. Since
 * this class required platform dependent code, there have to be a dll library
 * called 'lib.memory-measurement.win32.dll' present in a home directory of
 * NbJUnit (set by nbjunit.home property in either junit.properties file or
 * present as a system property).
 *
 * Please note, methods available in this class return a system specific values for
 * each supported platforms. The meanings of values are:
 *
 * On Solaris: resident memory size
 * On Linux: VmSize (virtual memory size)
 * On Windows: pagefile usage
 * @author mb115822
 */
public class MemoryMeasurement {
    
    /** only static methods **/
    private MemoryMeasurement() {
    }
    
    /** Name of the system property, which contains PID of running IDE. This property
     * is set by ide executor of XTest framework.
     */    
    public static final String IDE_PID_SYSTEM_PROPERTY = "netbeans.pid";
    
    /** Gets memory footprint of NetBeans IDE. This methods requires system property
     * 'netbeans.pid' to contain PID of the IDE process.
     * @throws MemoryMeasurementFailedException When measurement cannot be performed
     * @return memory size value
     */    
    public static long getIdeMemoryFootPrint() throws MemoryMeasurementFailedException {
        // pid should be stored as netbeans.pid system variabl
        String idePidString = System.getProperty(IDE_PID_SYSTEM_PROPERTY);        
        if (idePidString != null) {
            try {
                //System.out.println("Got idePidString = "+idePidString);
                long idePid = Long.parseLong(idePidString);
                //System.out.println("Got idePId = "+idePid);
                return getProcessMemoryFootPrint(idePid);
            } catch (NumberFormatException nfe) {
                // this should not happen -                 
            }
        }
        // unsuccessfull - throw MemoryMeasurementFailedException
        throw new MemoryMeasurementFailedException("Cannot get IDE PID - obtained value: "+idePidString);
    }
    
    /** Gets memory footprint of a process identified by PID. On each platform this
     * methods returns a platform specific value.
     * @param pid process identification for the process, which size is to be measured
     * @throws MemoryMeasurementFailedException When measurement cannot be performed
     * @return memory size value
     */    
    public static long getProcessMemoryFootPrint(long pid) throws MemoryMeasurementFailedException {
        String platform = getPlatform();
        //System.out.println("PLATFORM = "+getPlatform());
        if (platform.equals(SOLARIS)) {
            // call solaris method
            return getProcessMemoryFootPrintOnLinux(pid);
        } else if (platform.equals(LINUX)) {
            // call linux method
            return getProcessMemoryFootPrintOnSolaris(pid);
        } else if (platform.equals(WINDOWS)) {
            // call windows method
            return getProcessMemoryFootPrintOnWindows(pid);
        }
        // unsupported platform - cannot measure memory
        throw new MemoryMeasurementFailedException("Cannot measure memory on unsupported platform "+platform);
    }
    
    
    /** */    
    private static final long UNKNOWN_VALUE = -1;
   
    private static final String SOLARIS = "solaris";
    private static final String LINUX   = "linux";
    private static final String WINDOWS = "win32";
    private static final String UNKNOWN = "unknown";
    
    private static final String [][] SUPPORTED_PLATFORMS = {
        {"SunOS",SOLARIS},
        {"Linux",LINUX},
        {"Windows NT",WINDOWS},
        {"Windows 2000",WINDOWS},
        {"Windows XP",WINDOWS}
    };
    
    private static String getPlatform() throws MemoryMeasurementFailedException {
        String osName = System.getProperty("os.name");
        for (int i=0; i < SUPPORTED_PLATFORMS.length; i++) {
            if (SUPPORTED_PLATFORMS[i][0].equalsIgnoreCase(osName)) {
                return SUPPORTED_PLATFORMS[i][1];
            }
        }
        throw new MemoryMeasurementFailedException("MemoryMeasurement does not support this operating system: "+osName);
    }
    
    // os depednent implementations
    // linux
    private static long getProcessMemoryFootPrintOnLinux(long pid) throws MemoryMeasurementFailedException {
        String command="/bin/sh -c \"cat /proc/"+pid+"status | grep VmSize | sed -e 's/VmSize: *\\t* *//' | sed -e 's/ .*//'\"";
        try {
            Process process = Runtime.getRuntime().exec(command);
            return getOutputValue(process);
        } catch (IOException ioe) {
            throw new MemoryMeasurementFailedException("MemoryMeasurement failed, reason:"+ioe.getMessage(),ioe);
        }
    }
    
    // solaris
    private static long getProcessMemoryFootPrintOnSolaris(long pid) throws MemoryMeasurementFailedException {
        String command="/bin/sh -c \"pmap -x "+pid+" | grep \\^total | sed -e 's/.*Kb *//' | sed -e 's/ .*//'\"";
        try {
            Process process = Runtime.getRuntime().exec(command);
            return getOutputValue(process);
        } catch (IOException ioe) {
             throw new MemoryMeasurementFailedException("MemoryMeasurement failed, reason:"+ioe.getMessage(),ioe);
        }
    }    
    
    
    
    // woknous
    private static long getProcessMemoryFootPrintOnWindows(long pid) throws MemoryMeasurementFailedException {
        loadMemoryMeasurementLibrary();
        long value = getProcessMemoryFootPrintNative(pid);
        if (value == UNKNOWN_VALUE) {
            // there was some problem when measuring the foot print
            throw new MemoryMeasurementFailedException("Memory measurement call to native library failed - could not measure memory of process with pid "+pid+".");
        } else {
            // everything seems to be ok
            return value;
        }
    }    
    
    private static native long getProcessMemoryFootPrintNative(long pid);
    
    private static long getOutputValue(Process process) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));        
        String outputString = br.readLine();
        try {
           return Long.parseLong(outputString);
        } catch (NumberFormatException nfe) {
            throw new IOException("Process returned value '"+outputString+"', which cannot be converted to a number. Reason: "+nfe.getMessage());
        }

    }
    
    // load the library (if applicable) 
    private static boolean libraryLoaded = false;
    private static void loadMemoryMeasurementLibrary() throws MemoryMeasurementFailedException {        
        if (!libraryLoaded) {
            try {
                File dllLibrary = new File(Manager.getNbJUnitHome(),"lib.memory-measurement.win32.dll");
                //System.out.println("Libray path:"+dllLibrary);
                System.load(dllLibrary.getAbsolutePath());
                libraryLoaded = true;
               //System.out.println("Libraru loaded");
            } catch (IOException ioe) {
                throw new MemoryMeasurementFailedException("Cannot load native memory measurement library lib.memory-measurement.win32.dll, reason: "
                                        +ioe.getMessage(),ioe);
            } catch (UnsatisfiedLinkError ule) {
                // cannot load the library ....
                throw new MemoryMeasurementFailedException("Cannot load native memory measurement library lib.memory-measurement.win32.dll, reason: "
                                        +ule.getMessage(),ule);                
            }
        }
        
    }
    
}
