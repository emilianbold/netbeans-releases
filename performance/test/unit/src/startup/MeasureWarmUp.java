/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package startup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import startup.MeasureIDEStartupTime.ThreadReader;

/**
 * Measure warm up time by org.netbeans.core.perftool.StartLog.
 * Number of starts with new userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat.with.new.userdir </code>
 * <br> and number of starts with old userdir is defined by property
 * <br> <code> org.netbeans.performance.repeat </code>
 *
 * @author Marian.Mirilovic@sun.com
 */
public class MeasureWarmUp extends MeasureIDEStartupTime {
    
    protected static final String warmup = "Warmup running ";
    protected static final String warmup_finished = "Warmup finished, took ";
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public MeasureWarmUp(java.lang.String testName) {
        super(testName);
    }
    
    /** Testing start of IDE with measurement of the startup time.
     * @throws IOException
     */
    public void testWarmUp() throws IOException {
        for (int i=1; i <= repeat; i++) {
            runIDEandMeasureWarmUp(getMeasureFile(i), getUserdirFile(i),5000);
        }
    }
    
    /** Run IDE and read measured time from file
     *
     * @param measureFile file where the time of window system painting is stored
     * @throws java.io.IOException
     * @return startup time
     */
    private void runIDEandMeasureWarmUp(File measureFile, File userdir, long timeout) throws IOException {
        runIDEWarmUp(getIdeHome(),userdir,measureFile,timeout);
        Hashtable measuredValues = parseMeasuredValues(measureFile);
        
        if(measuredValues==null)
            fail("It isn't possible measure Warm Up.");
        
        java.util.Iterator iter = measuredValues.keySet().iterator();
        String name;
        long value;
        while(iter.hasNext()){
            name = (String)iter.next();
            value = ((Long)measuredValues.get(name)).longValue();
            
            System.out.println(name+"="+value);
            reportPerformance(name,value,"ms",1);
        }
    }
    
    
    /** Creates and executes the command line for running IDE.
     * @param ideHome IDE home directory
     * @param userdir User directory
     * @param measureFile file where measured time is stored
     * @throws IOException
     */
    private static void runIDEWarmUp(File ideHome, File userdir, File measureFile, long timeout) throws IOException {
        
        //check <userdir>/lock file
        if(new File(userdir, "lock").exists())
            fail("Original Userdir is locked!");
        
        //add guitracker on classpath
        String classpath = System.getProperty("performance.testutilities.dist.jar");
        
        // create jdkhome switch
        String jdkhome = System.getProperty("java.home");
        if(jdkhome.endsWith("jre"))
            jdkhome = jdkhome.substring(0, jdkhome.length()-4);
        
        String platform = getPlatform();
        File ideBinDir = new File(ideHome,"bin");
        String cmd;
        if (platform.equals(WINDOWS)) {
            cmd = (new File(ideBinDir,"netbeans.exe")).getAbsolutePath();
        } else {
            cmd = (new File(ideBinDir,"netbeans")).getAbsolutePath();
        }
        // add other argumens
        // guiltracker lib
        cmd += " --cp:a "+classpath;
        // userdir
        cmd += " --userdir "+userdir.getAbsolutePath();
        // get jdkhome path
        cmd += " --jdkhome "+jdkhome;
        // netbeans full hack
        cmd += " -J-Dnetbeans.full.hack=true";
        // measure argument
        cmd += " -J-Dorg.netbeans.log.startup.logfile="+measureFile.getAbsolutePath();
        // measure argument - we have to set this one to ommit repaint of memory toolbar (see openide/actions/GarbageCollectAction)
        cmd += " -J-Dorg.netbeans.log.startup=tests";
        // close the IDE after startup
        cmd += " -J-Dnetbeans.close=true";
        // close the IDE after warmup
        cmd += " -J-Dnetbeans.warm.close=true";
        // wait after startup, need to set longer time for complex startup because rescan rises
        cmd += " -J-Dorg.netbeans.performance.waitafterstartup="+timeout;
        // disable rescaning after startup
        //        cmd += " -J-Dnetbeans.javacore.noscan=true";
        
        System.out.println("Running: "+cmd);
        
        Runtime runtime = Runtime.getRuntime();
        
        // need to create out and err handlers
        Process ideProcess = runtime.exec(cmd,null,ideBinDir);
        
        // track out and errs from ide - the last parameter is PrintStream where the
        // streams are copied - currently set to null, so it does not hit performance much
        ThreadReader sout = new ThreadReader(ideProcess.getInputStream(), null);
        ThreadReader serr = new ThreadReader(ideProcess.getErrorStream(), null);
        try {
            int exitStatus = ideProcess.waitFor();
            System.out.println("IDE exited with status = "+exitStatus);
        } catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
            IOException ioe = new IOException("Caught InterruptedException :"+ie.getMessage());
            ioe.initCause(ie);
            throw ioe;
        }
    }
    
    /** Parse logged startup time from the file.
     * @param measuredFile file where the startup time is stored
     * @return measured startup time
     */
    protected static Hashtable parseMeasuredValues(File measuredFile) {
        Hashtable measuredValues = new Hashtable();
        
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(measuredFile));
            String readLine, name="", value="";
            int begin;
            while((readLine = br.readLine())!=null){
                try {
                    if((begin=readLine.indexOf(warmup))!=-1){ // @10741 - Warmup running org.netbeans.core.ui.DnDWarmUpTask dT=53
                        name = readLine.substring(begin+warmup.length(), readLine.indexOf(" ",begin+warmup.length()));
                        value = readLine.substring(readLine.indexOf("=")+1);
                        measuredValues.put(name, new Long(value));
                    }else if((begin=readLine.indexOf(warmup_finished))!=-1){ // @12059 - Warmup finished, took 1459ms
                        name = "Warmup finished";
                        value = readLine.substring(readLine.indexOf("took ")+"took ".length(),readLine.indexOf("ms"));
                        measuredValues.put(name, new Long(value));
                    }
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace(System.err);
                    return null;
                }
            }
            return measuredValues;
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace(System.err);
                    return null;
                }
            }
        }
    }
    
}
