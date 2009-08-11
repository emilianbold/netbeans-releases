/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2me.cdc.project.savaje;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author suchys
 */
public class SunToolkitEmulatorExecTask extends Task {
    
    private List filesets = new LinkedList(); // List<FileSet>
    private File home;
    private String mainclass;
    private String args;
    private String jvmargs;
    private String device;
    private String profile;
    private boolean xlet;
    private boolean applet;
    //for debugger
    private String debuggerAddressProperty;
    private boolean debug;
    //out stream
    protected PrintWriter fos = null;

    public void addFileset(FileSet fs) {
        filesets.add(fs);
    }    
    
    public void execute() throws BuildException {
        List<String> arguments = new ArrayList<String>();
        String os = System.getProperty("os.name");
        if (os.toLowerCase().indexOf("windows") >= 0) {
            arguments.add(home + File.separator + "bin" + File.separator + "emulator.exe");
        } else {
            arguments.add(home + File.separator + "bin" + File.separator + "emulator");
        }

        if (jvmargs != null && jvmargs.trim().length() != 0){
            String[] params = Commandline.translateCommandline(jvmargs);
            arguments.addAll(Arrays.asList(params));
        }
        
        assert device != null;
        arguments.add("-Xdevice:" + device);
        assert profile != null;
        arguments.add("-Xapi:" + profile);
        //debugger
        if (debug) {
            arguments.add("-Xdebug");
            String debugAddress = null;
            try {
                debugAddress = Integer.toString(this.determineFreePort());
            } catch (IOException e) {
                throw new BuildException(e);
            }
            arguments.add("-Xrunjdwp:transport=dt_socket,address=" + debugAddress + ",server=y,suspend=y");            
            if (debuggerAddressProperty != null) {
                this.getProject().setProperty(debuggerAddressProperty, debugAddress);
            }
        }
        
        if (!xlet && !applet){ //for main
            arguments.add("-classpath");
            arguments.add(createPath());
            arguments.add(mainclass);
            appendArguments(arguments);            
        } else if (xlet) { //xlet
            arguments.add("-Xxlet");
            StringTokenizer xlets = new StringTokenizer(mainclass, ";");
            while(xlets.hasMoreElements()){
                arguments.add("-name");
                arguments.add((String)xlets.nextElement());
                arguments.add("-path");
                arguments.add(createPath());                
                if (args != null && args.length() != 0){
                    arguments.add("-args");
                    appendArguments(arguments);
                }
            }            
        } else { //applet
            assert true : "Applet is not supported because there is no Personal Profile implemented!";
            generateHtml();
            arguments.add("-Xapplet:"); //xxx path to applet html file
        }

        String[] arg = (String[]) arguments.toArray(new String[0]);
        getProject().log("Application arguments:", Project.MSG_VERBOSE);
        for (int i = 0; i < arg.length; i++)
            getProject().log("'" + arg[i] + "'", Project.MSG_VERBOSE);
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
            p.destroy();
            
            // close the output file if required
            logFlush();
            
            if (p.exitValue() != 0)
                throw new BuildException("Emulator execution failed!");
        } catch (IOException ex) {
            throw new BuildException("Emulator execution failed!");
        } catch (InterruptedException ex) {
            throw new BuildException("Emulator execution failed!");
        }
    }

    private void generateHtml() throws BuildException {    
        assert true : "Not finished yet.";
        File f = new File(home, "epoc32\\winscw\\c\\private\\applet.html");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(f));
            pw.println("<HTML>"); 
            pw.println("<HEAD>"); 
            pw.println("<TITLE> A Testing Program for " + mainclass + " </TITLE>"); 
            pw.println("</HEAD>"); 
            pw.println("<BODY>"); 
            pw.println("<APPLET CODE=\"" + mainclass + ".class\" WIDTH=240 HEIGHT=320>"); 
            assert true : "Params are not allowed";
            pw.println("</APPLET>"); 
            pw.println("</BODY>");
            pw.println("</HTML>");      
        } catch (IOException ex) {
            throw new BuildException("HTML can not be writen");
        } finally {
            if (pw != null)
                pw.close();
        }
    }

    private String createPath(){
        Iterator it = filesets.iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            FileSet fs = (FileSet)it.next();
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            File basedir = ds.getBasedir();
            String[] files = ds.getIncludedFiles();
            for (int i = 0; i < files.length; i++){
                sb.append("\"" + basedir.getAbsolutePath() + File.separatorChar + files[i] + "\"");
                if (i+1 < files.length){
                    sb.append(";");
                }
            }
        }
        return sb.toString();
    }
    
    private void appendArguments(List args){
        if (this.args == null || this.args.length() == 0) return;
        StringTokenizer st = new StringTokenizer(this.args, " ");
        while (st.hasMoreTokens()) {
            //args.add("-D" + st.nextToken());
            args.add(st.nextToken());
        }
    }

    /**
     * Finds a free port to be used for listening for debugger connection.
     * @return free port number
     * @throws IOException
     */
    private int determineFreePort() throws IOException {
        Socket sock = new Socket();
        sock.bind(null);
        int port = sock.getLocalPort();
        sock.close();
        return port;
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
}
