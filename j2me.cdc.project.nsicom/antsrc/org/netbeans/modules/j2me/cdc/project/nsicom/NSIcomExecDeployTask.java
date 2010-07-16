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

package org.netbeans.modules.j2me.cdc.project.nsicom;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import org.netbeans.mobility.activesync.*;
import org.openide.util.Exceptions;

/**
 * @author suchys
 */
public class NSIcomExecDeployTask extends Task {
    
    private List filesets = new LinkedList(); // List<FileSet>
    private File home;
    private String mainclass;
    private String args;
    private String jvmargs;
    private String device;
    private String profile;
    private boolean xlet;
    private boolean applet;
    
    private boolean verbose;
    private String hostIP;
    
    //for debugger
    private String debuggerAddressProperty;
    private boolean debug;
    
    private boolean runOnDevice;
    private String remoteVMLocation;
    private String remoteDataLocation;
    
    private boolean deploy;
    //out stream
    protected PrintWriter fos = null;
    
    //activesync
    private ActiveSyncOps activeSync;
   
    private RemoteFile remoteFolder = null; //not a property
    
    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }    
    
    public void execute() throws BuildException {
        if (deploy){
            runOnDevice = true; //deploy is always on device operation
        }
        
        if (runOnDevice){
            try {
                //URL location = ActiveSyncOps.class.getClass().getProtectionDomain().getCodeSource().getLocation();
                activeSync = ActiveSyncOps.getDefault();
                log("Library nbactivesync.dll loaded correctly", Project.MSG_VERBOSE); //NOI18N only internal debug
            } catch( Throwable e){
                throw new BuildException("Library nbactivesync.dll can not be loaded!"); //NOI18N
            }

            checkActiveSync();
        }        

        List arguments = new ArrayList();
        if (!runOnDevice){
            String os = System.getProperty("os.name");//NOI18N
            if (os.toLowerCase().indexOf("windows") >= 0) {//NOI18N
                arguments.add(home + File.separator + "bin" + File.separator + "pJSCP.exe");//NOI18N
            } else {
                throw new BuildException("Only Windows version is supported!");//NOI18N
            }
        } else {
            if (!deploy) { //if not deploying, verify VM
                verifyRemoteVM();
            }
                
            if (remoteDataLocation == null || remoteDataLocation.length() == 0) throw new BuildException("Remote folder must be set!");     //NOI18N       
        }   
        
        if (jvmargs != null && jvmargs.trim().length() != 0){
            StringTokenizer st = new StringTokenizer(jvmargs, " ");
            while(st.hasMoreTokens()){
                arguments.add(st.nextToken());
            }
        }
        
        if (!runOnDevice){ //add TCK argument to not to stop VM and wait for key in case of exception
            arguments.add("-TCK");
        }
        //if you are running in debug mode or on device, on device the atribute is mandatory
        if (debug || runOnDevice) {
            arguments.add("-mon"); //NOI18N //run monitoring (mandatoty for on device VM
            if (hostIP != null && hostIP.length() != 0){
                arguments.add(hostIP);
            }
            if (runOnDevice){
                arguments.add("\'-Of" + remoteDataLocation + "\'"); //NOI18N  //create log file (only device)
            }
        } else {
            arguments.add("-Ob"); //NOI18N //sent output to console (only emulator)
        }
        
        if (verbose)
            arguments.add("-verbose");  //NOI18N                   

        if (!xlet && !applet){ //for main
            arguments.add("-classpath");//NOI18N
            arguments.add(createClassPath());
            arguments.add(mainclass);
            appendArguments(arguments);            
        } else if (xlet) { //xlet
            assert true : "Xlet execution is not supported!"; //NOI18N
        } else { //applet
            arguments.add("-av"); //NOI18N //run applet argument   
            //no handling for arguments in generateHtml!!
            //will copy classes to remote device as well by call generateHtml(..)
            String html = generateHtml();
            File f = new File(this.getProject().getBaseDir().toString() + "/build", "applet.html"); //NOI18N
            try {
                FileOutputStream dos = new FileOutputStream(f);
                dos.write(html.getBytes());
                dos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (!runOnDevice){
                arguments.add(f.toString());
            } else {
                try {
                    remoteFolder = createRemoteFolder(remoteDataLocation);
                } catch (Exception e){
                    throw new BuildException("Remote folder can not be created!"); //NOI18N
                }
                try {
                    RemoteFile remoteHtmlItem = new RemoteFile(remoteFolder.getFullPath(), "applet.html"); //NOI18N
                    if (remoteHtmlItem.exists()){
                        activeSync.delete(remoteHtmlItem);
                    }
                    activeSync.copyToDevice(f, remoteHtmlItem);
                    arguments.add("\'" + remoteHtmlItem.getFullPath() + "\'");
                } catch (IOException ioEx){
                    throw new BuildException("Can not create applet.html"); //NOI18N
                }
            }
        }
        
        if (!deploy){ //deployment does not execute
            String[] arg = (String[]) arguments.toArray(new String[0]);
            getProject().log("Application arguments:", Project.MSG_VERBOSE); //NOI18N only internal debug
            for (int i = 0; i < arg.length; i++){
                getProject().log("'" + arg[i] + "'", Project.MSG_VERBOSE); //NOI18N only internal debug
            }
            
            try {
                if (!runOnDevice){
                    Process p = null;
                    try {
                        p = Runtime.getRuntime().exec(arg);
                        StreamReader inputReader =
                                new StreamReader(p.getInputStream(), Project.MSG_INFO);
                        StreamReader errorReader =
                                new StreamReader(p.getErrorStream(), Project.MSG_WARN);

                        // starts pumping away the generated output/error
                        inputReader.start();
                        errorReader.start();

                        // Wait for everything to finish
                        p.waitFor();
                        inputReader.join();
                        errorReader.join();
                    } catch (ThreadDeath td){
                        p.destroy();
                        throw td;
                    } finally {
                        // close the output file if required
                        logFlush();
                    }
                    if (p.exitValue() != 0)
                        throw new BuildException("Emulator execution failed!"); //NOI18N
                } else {
                    //clean up log file
                    RemoteFile root = activeSync.getRootFilesystems()[0];
                    //RemoteFile log = new RemoteFile(root.getFullPath(), "jspcout.txt");//NOI18N
                    RemoteFile log = new RemoteFile(remoteFolder.getFullPath(), "jspcout.txt");
                    try {
                        if (log.exists()){
                            activeSync.delete(log);
                        }
                    } catch (Exception e){
                        //ignore
                        log("Can not delete log file", Project.MSG_VERBOSE); //NOI18N only internal debug
                    }
                    ProcessHandler handler = null;
                    try {
                        handler = new ProcessHandler(remoteDataLocation, arg, remoteFolder);
                        handler.start();
                        try {
                            handler.join();
                        } catch (InterruptedException ie){
                        }
                        if (handler.getExitCode() != 0){
                            throw handler.getCause();
                        }
                    } catch (ThreadDeath threadDeath) { //ant task has been shut down
                        if (handler.getProcess() != null)
                            handler.destroyRemoteProcess();
                        throw threadDeath;
                    }
                }
            } catch (IOException ex) {
                throw new BuildException("Emulator execution failed!");//NOI18N
            } catch (InterruptedException ex) {
                throw new BuildException("Emulator execution failed!");//NOI18N
            }
        }
    }

    private String generateHtml() throws BuildException {    
        //assert true : "Not finished yet.";
        StringWriter sw = new StringWriter(); 
        PrintWriter pw = null;
        pw = new PrintWriter(sw);//new FileWriter(appletHtml));
        pw.println("<HTML>"); //NOI18N
        pw.println("<HEAD>"); //NOI18N
        pw.println("<TITLE> A Testing Program for " + mainclass + " </TITLE>"); //NOI18N
        pw.println("</HEAD>"); //NOI18N
        pw.println("<BODY>"); //NOI18N
        pw.println("<APPLET CODE=\"" + mainclass + ".class\" WIDTH=250 HEIGHT=350>"); //NOI18N
        //will copy classes to remote device as well
        pw.println("<PARAM NAME=ARCHIVE VALUE=" + createClassPath() + "/>");//NOI18N
        pw.println("</APPLET>"); //NOI18N
        pw.println("</BODY>"); //NOI18N
        pw.println("</HTML>"); //NOI18N
        pw.flush();
        return sw.getBuffer().toString();
    }

    private String createClassPath(){
        Iterator it = filesets.iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for (int i = 0; i < files.length; i++){
                if (!runOnDevice){
                    if (!applet){ //look at different classpath quotation for PC and device run
                        sb.append("\"" + basedir.getAbsolutePath() + File.separatorChar + files[i] + "\""); //NOI18N
                    } else {
                        sb.append("../dist/" + files[i]);  //NOI18N       
                    }
                    if (i+1 < files.length){
                        sb.append(";");
                    }
                } else {
                    //for on device we must copy classpath items to remote destination                    
                    try {
                        remoteFolder = createRemoteFolder(remoteDataLocation);
                    } catch (Exception e){
                        throw new BuildException("Remote folder can not be created!"); //NOI18N
                    }
                    try {
                        RemoteFile remoteCpItem = new RemoteFile(remoteFolder.getFullPath(), files[i]);
                        if (remoteCpItem.exists()){
                            activeSync.delete(remoteCpItem);
                        }
                        activeSync.copyToDevice(new File(basedir.getAbsolutePath(), files[i]), remoteCpItem);
                        log("Copy classpath element " + files[i] + " into " + remoteFolder.getFullPath(), Project.MSG_VERBOSE); //NOI18N only internal debug
                    } catch (IOException ex) {
                        throw new BuildException("Can not copy classpath " + files[i] + " item to remote device!"); //NOI18N
                    }
                    
                    if (!applet){//look at different classpath quotation for PC and device run
                        sb.append("\'" + remoteFolder.getFullPath() + File.separatorChar + files[i] + "\'"); //NOI18N
                    } else {
                        sb.append(files[i]);//this will not work for more than one file                  
                    }
                    if (i+1 < files.length){
                        sb.append(";"); //NOI18N
                    }                    
                }
            }
        }
        return sb.toString();
    }
    
    private void appendArguments(List args){
        if (this.args == null || this.args.length() == 0) return;
        StringTokenizer st = new StringTokenizer(this.args, " "); //NOI18N
        while (st.hasMoreTokens()) {
            //args.add("-D" + st.nextToken());
            args.add(st.nextToken());
        }
    }

    private void outputLog(String line, int messageLevel) {
        if (fos == null) {
            log(line, messageLevel);
        } else {
            fos.println(line);
        }
    }
    
    private void logFlush() {
        if (fos != null) {
            fos.close();
        }
    }

//** remote device operations
    private void checkActiveSync() throws BuildException {
        if(!activeSync.isAvailable()) throw new BuildException("ActiveSync is not installed!"); //NOI18N
        log("Active sync available", Project.MSG_VERBOSE); //NOI18N only internal debug

        try {
            if(!activeSync.isDeviceConnected()) throw new BuildException("Device is not connected!"); //NOI18N
            log("Device connected", Project.MSG_VERBOSE); //NOI18N only internal debug            
        } catch (ActiveSyncException ex) {
            throw new BuildException("Error during connecting to remote device!"); //NOI18N
        }
    }

    private void verifyRemoteVM() throws BuildException {
        if (remoteVMLocation == null || remoteVMLocation.length() == 0) throw new BuildException("Remote VM location must be set!"); //NOI18N
        
        RemoteFile remoteVM = new RemoteFile(remoteVMLocation, ""); //NOI18N
        if (!remoteVM.exists() || remoteVM.isDirectory()) throw new BuildException("Remote VM does not exist in specified location: " + remoteVM.getFullPath()); //NOI18N
        log("Remote VM OK", Project.MSG_VERBOSE); //NOI18N only internal debug
        
    }
    
    private RemoteFile createRemoteFolder(String name) throws ActiveSyncException, IOException{
        StringTokenizer st = new StringTokenizer(name, "/\\"); //NOI18N
        RemoteFile root = activeSync.getRootFilesystems()[0];
        while (st.hasMoreElements()) {
            name = st.nextToken();
            
            if (name.length() > 0) {
                RemoteFile next = new RemoteFile(root.getFullPath(), name);
                if (!next.exists()) {
                    next = activeSync.createNewDirectory(root.getFullPath(), name);
                }                
                root = next;
            }
        }
        log("Remote folder successfuly created", Project.MSG_VERBOSE); //NOI18N only internal debug
        return root;
    }

    private class RemoteLogReader extends Thread {
        private RemoteFile logFile;
        private boolean running = true;
        
        RemoteLogReader(RemoteFile logFile){
            this.logFile = logFile;
            setDaemon(true);
        }
        
        public void run(){
            BufferedReader br = null;
            while(running){
                if (br == null && !new RemoteFile(logFile.getFullPath(), "jspcout.txt").exists()){
                    try {
                        Thread.sleep(50); //wait for VM to create log
                    } catch (InterruptedException ex) {
                    }
                    continue;
                }
                try {
                    if (br == null){
                        InputStream is = activeSync.getRemoteInputStream(new RemoteFile(logFile.getFullPath(), "jspcout.txt"));
                        if (is != null){
                            br = new BufferedReader(new InputStreamReader(is));
                        } 
                    } else {
                        String line = br.readLine();
                        if (line != null){
                            log(line);
                        }
                    }
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        public void finish(){
            running = false;
        }
    }
    
//** end
    
    public File getHome() {
        return home;
    }
    
    public void setHome(File home) {
        this.home = home;
    }
    
    public String getMainclass() {
        return mainclass;
    }
    
    public void setMainclass(String mainclass) {
        this.mainclass = mainclass;
    }
    
    public String getArgs() {
        return args;
    }
    
    public void setArgs(String args) {
        this.args = args;
    }
    
    public String getJvmargs() {
        return jvmargs;
    }
    
    public void setJvmargs(String jvmargs) {
        this.jvmargs = jvmargs;
    }
    
    public String getDevice() {
        return device;
    }
    
    public void setDevice(String device) {
        this.device = device;
    }
    
    public boolean isXlet() {
        return xlet;
    }
    
    public void setXlet(boolean xlet) {
        this.xlet = xlet;
    }
    
    public boolean isApplet() {
        return applet;
    }
    
    public void setApplet(boolean applet) {
        this.applet = applet;
    }
    
    public String getDebuggerAddressProperty() {
        return debuggerAddressProperty;
    }
    
    public void setDebuggerAddressProperty(String debuggerAddressProperty) {
        this.debuggerAddressProperty = debuggerAddressProperty;
    }
    
    public boolean isDebug() {
        return debug;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    class StreamReader extends Thread {
        private BufferedReader din;
        private int messageLevel;
        private boolean endOfStream = false;
        private int SLEEP_TIME = 5;
        
        public StreamReader(InputStream is, int messageLevel) {
            this.din = new BufferedReader(new InputStreamReader(is));
            this.messageLevel = messageLevel;
        }
        
        public void pumpStream() throws IOException {
            if (!endOfStream) {
                String line = din.readLine();
                
                if (line != null) {
                    outputLog(line, messageLevel);
                } else {
                    endOfStream = true;
                }
            }
        }
        
        public void run() {
            try {
                try {
                    while (!endOfStream) {
                        pumpStream();
                        sleep(SLEEP_TIME);
                    }
                } catch (InterruptedException ie) {
                }
                din.close();
            } catch (IOException ioe) {
            }
        }
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public boolean isRunOnDevice() {
        return runOnDevice;
    }

    public void setRunOnDevice(boolean runOnDevice) {
        this.runOnDevice = runOnDevice;
    }

    public String getRemoteVMLocation() {
        return remoteVMLocation;
    }

    public void setRemoteVMLocation(String remoteVMLocation) {
        this.remoteVMLocation = remoteVMLocation;
    }

    public String getRemoteDataLocation() {
        return remoteDataLocation;
    }

    public void setRemoteDataLocation(String remoteDataLocation) {
        this.remoteDataLocation = remoteDataLocation;
    }

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
    }
    
    class ProcessHandler extends Thread {

        private RemoteProcess process;
        private int exitCode = -1;
        private IOException cause = null;
        private RemoteFile remoteFolder;
        private String[] vmArguments;
        private String vmLocation;
        private RemoteLogReader remoteReader = null;
        
        public ProcessHandler(String vmLocation, String[] vmArguments, RemoteFile remoteFolder) {
            this.vmLocation = vmLocation;
            this.vmArguments = vmArguments;
            this.remoteFolder = remoteFolder;
            setDaemon(true);
        }

        public void run(){
            try {
                process = activeSync.executeRemoteProcess(remoteVMLocation, vmArguments);
                remoteReader = new RemoteLogReader(remoteFolder);
                remoteReader.start();
                exitCode = activeSync.waitFor(process);
            } catch (ActiveSyncException asEx){
                cause = asEx;
            } catch (IOException ioEx){
                cause = ioEx;
            } finally {
                if (remoteReader != null)
                    remoteReader.finish();
            }
        }

        public int getExitCode() {
            return exitCode;
        }

        public IOException getCause() {
            return cause;
        }

        public RemoteProcess getProcess() {
            return process;
        }

        private void destroyRemoteProcess() {
            try {
                if (process != null)
                    activeSync.destroyRemoteProcess(process);
            } catch (ActiveSyncException ex) {
            } catch (IllegalArgumentException ex) {
            } finally {
                if (remoteReader != null)
                    remoteReader.finish();
            }
        }
    }
}
