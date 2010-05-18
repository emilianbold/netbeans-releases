/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.performance.j2ee.memory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import java.util.StringTokenizer;

/**
 * Measure memory footprint, by checking size of memory occupied by runide process.
 * On Windows platform used <b>pslist.exe</b>, on Unix platform used <b>ps</b>. 
 * Output of these commands is parsed and measured memory size
 * (windows - "MEM", unix - "RES", "RSS") presents as measured memory footprint.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureBaselineMemoryFootprint extends org.netbeans.junit.NbPerformanceTestCase{
    
    /** Used platform. */
    private static String platform;
    
    /** IDE process PID. */
    private static long pid;
    
    /** Ouput file where is logged ouput from ps command. */
    private static String PS_OUTPUT_FILENAME = "psOutput.txt";
    
    private static final String UNIX = "unix";
    private static final String WINDOWS = "windows";
    private static final String UNKNOWN = "unknown";
    
    private static final String [][] SUPPORTED_PLATFORMS = {
        {"Linux,i386",UNIX},
        {"SunOS,sparc",UNIX},
        {"Windows_NT,x86",WINDOWS},
        {"Windows_2000,x86",WINDOWS},
        {"Windows_XP,x86",WINDOWS},
        {"Windows_95,x86",WINDOWS},
        {"Windows_98,x86",WINDOWS},
        {"Windows_Me,x86",WINDOWS}
    };
    
    public static final String suiteName="J2EE Memory suite";    
    
    /** Define testcase
     * @param testName name of the testcase
     */    
    public MeasureBaselineMemoryFootprint(String testName) {
        super(testName);
    }
    
    /** Measure baseline memory footprint */
    public void testMemoryFootprintAfterStart(){
        long memory;
        try {
            memory = getMemoryConsumption();
            
            if(memory>0){
                reportPerformance("Memory Consumption After Start", memory , "kB", 1);
            }else
                fail("Measured value = "+memory+"kB - it's wrong value!");
            
        }catch(Exception exc){
            exc.printStackTrace(getLog());
            fail("Exception rises during measurement : "+exc.toString());
        }
    }
    
    
    /**
     * Measure memory footprint. Looks for file [xtest.tmpdir]/ide.pid
     * to IDE process PID which is used by utils to measure memory.
     * @throws IOException if [xtest.tmpdir]/ide.pid file doesn't exist
     * @return return measured memory footprint
     */
    private long getMemoryConsumption() throws IOException {
        String workdir = System.getProperty("xtest.tmpdir");
        
        File ideRunning;
        log("Start");
        
        if (workdir!=null) {
            log("Workdir {"+workdir+"}.");
            workdir = workdir.substring(0,workdir.lastIndexOf(File.separator));
            
            // create flag file indicating running tests
            ideRunning = new File(workdir,"ide.pid");
            log("Looking for file {"+ideRunning.getAbsolutePath()+"}.");
            
            if (ideRunning.exists()) {
                log("PID file exists.");
                try {
                    LineNumberReader reader = new LineNumberReader(new FileReader(ideRunning));
                    String line = reader.readLine();
                    if (line != null) {
                        try {
                            pid = Long.parseLong(line);
                            log("Measure memory footprint of process with PID="+pid);
                            long measuredMemory = measureMemory();
                            Thread.sleep(2000);
                            return measuredMemory;
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace(getLog());
                            fail("Cannot parse PID written in the ide.pid file: "+line+"- cannot measure.");
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace(getLog());
                    fail("IOException when reading PID from ide.pid file - cannot measure");
                } catch (Exception e) {
                    e.printStackTrace(getLog());
                    fail("Exception when trying to measure IDE's used memory");
                }
            } else {
                fail("Cannot find file containing PID of running IDE ("+ideRunning.getAbsolutePath()+") - cannot measure");
            }
        } else {
            fail("xtest.workdir property is not specified - cannot measure");
        }
        fail("Wrong state");
        return 0;
    }
    
    
    /**
     * Run appropriate command against used platform.
     * @return return measured memory footprint
     */
    private long measureMemory(){
        platform = getPlatform();
        
        log("Platform="+platform);
        
        if (platform.equals(UNIX))
            return psOnUnix();
        else if (platform.equals(WINDOWS))
            return psOnWindows();
        else
            fail("Unsupported platform!");
        
        return 0;
    }
    
    /**
     * Execute appropriate command and save output as file psOutput.txt.
     * @param psCommand command to be runned (using platform dependent util)
     */
    private void executePsCommand(String psCommand){
        log("Ecexute command: ["+psCommand+"].");
        
        try {
            Process ps = Runtime.getRuntime().exec(psCommand);
            
            StringBuffer buffer = new StringBuffer();
            BufferedReader dataInput = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String line;
            
            while ((line = dataInput.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');
            }
            
            getLog(PS_OUTPUT_FILENAME).print(buffer.toString());
            ps.waitFor();
            
            log("ps command exit value = "+ps.exitValue());
        } catch (InterruptedException ie) {
            ie.printStackTrace(getLog());
            log("InterruptedException when ps :"+ie.toString());
        } catch (IOException ioe){
            ioe.printStackTrace(getLog());
            log("None output from command ps, exception arise "+ioe.toString());
        }
    }
    
    
    /**
     * Execute commands :
     * <pre>
     * ps -A -o pid,comm,rss
     * </pre>
     * and save output as file psOutput.txt.
     * @param psCommand command to be runned (using platform depend util)
     */
    private long psOnUnix(){
        long returnValue = 0;
  
        executePsCommand("ps -A -o pid,comm,rss");
        returnValue = parsePsFile();
        
        return returnValue;
    }
    
    /**
     * Execute commands :
     * <pre>
     * pslist.exe
     * </pre>
     * and save output as file psOutput.txt.
     * @param psCommand command to be runned (using platform depend util)
     */
    private long psOnWindows(){
        String xtestHome = System.getProperty("xtest.tmpdir");
        if (xtestHome != null) {
            File psFile = new File(xtestHome,"pslist.exe");
            String psPath = psFile.getAbsolutePath();
            String psCommand = psPath;
            executePsCommand(psCommand);
            return parsePsFile();
        } else {
            fail("xtest.home system property not set - cannot find ps distributed with XTest on windows");
        }
        return 0;
    }
    
    
    /**
     * Parse file (created as output from ps command) and looks for line with appropriate pid.
     * File can be found (if exists) after test run in working directory. If file has not been created
     * return 0. If work dir doesn't exist or output file exists but line with appropriate PID doens't
     * exists there -> test fails.
     * @return measured memory - parsed output from command ps
     */
    private long parsePsFile(){
        String workDirPath = "";
        
        try {
            workDirPath = getWorkDir().getAbsolutePath();
        }catch(IOException ioe){
            ioe.printStackTrace(getLog());
            fail("It isn't possible to get work directory, arise exception :"+ioe.toString());
        }
        
        try {
            File psOutput = new File(workDirPath, PS_OUTPUT_FILENAME);
            log("Parse file "+psOutput.getAbsolutePath());
            
            BufferedReader reader = new BufferedReader(new FileReader(psOutput));
            String line;
            
            while((line = reader.readLine()) != null){
                log("\t Line=["+line+"]");
                long memory = getMemory(line);
                if(memory!=0)
                    return memory;
            }
            
//            fail("Cannot find line with PID in output from ps command!");
            
        } catch(IOException ioe){
            ioe.printStackTrace(getLog());
            log("None output from ps command, arise exception :"+ioe.toString());
        }
        
        return 0;
    }
    
    
    /** Get used memory size parsed from one line of output from command ps .
     * Transform memory size to [kB]. Type of parser depends on used platform.
     * @param line line from command ps's output file
     * @return measured memory
     */
    private long getMemory(String line){
        StringTokenizer st = new StringTokenizer(line);
        String line_pid,line_mem;
        long memory = 0;
        long ppid;
        
        if(line.length()>0){
            if (platform.equals(UNIX)) {
                line_pid = st.nextToken();
                try {
                    ppid = Long.parseLong(line_pid);
                }catch(NumberFormatException exc){
                    return 0;
                }
                
                log("\t proces pid="+ppid + " looking for pid="+pid);
                
                if(pid == ppid){
                    st.nextToken();
                    line_mem = st.nextToken();
                    memory = Long.parseLong(line_mem);
                }
                
            } else if (platform.equals(WINDOWS)) {
                st.nextToken();
                line_pid = st.nextToken();
                try {
                    ppid = Long.parseLong(line_pid);
                }catch(NumberFormatException exc){
                    return 0;
                }
                
                log("\t proces pid="+ppid + " looking for pid="+pid);
                if(pid == ppid){
                    for(int i=0;i<3;i++)
                        st.nextToken();
                    line_mem = st.nextToken();
                    
                    memory = Long.parseLong(line_mem);
                }
            } else {
                fail("Unsupported platform!");
            }
            
        }
        log("Memory="+memory);
        return memory;
    }
    
    /**
     * Get platform on which the code is executed.
     * @return platform identification string
     */
    private static String getPlatform() {
        
        String platformString=(System.getProperty("os.name","")+","+
                        /*
                        System.getProperty("os.version","")+","+
                         */
        System.getProperty("os.arch","")).replace(' ','_');
        for (int i=0; i<SUPPORTED_PLATFORMS.length; i++) {
            if (platformString.equalsIgnoreCase(SUPPORTED_PLATFORMS[i][0])) {
                return SUPPORTED_PLATFORMS[i][1];
            }
        }
        return UNKNOWN;
    }
    
    
}
