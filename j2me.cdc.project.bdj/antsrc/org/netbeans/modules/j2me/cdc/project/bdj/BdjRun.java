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

package org.netbeans.modules.j2me.cdc.project.bdj;

// IMPORTANT! You need to compile this class against ant.jar.
// The easiest way to do this is to add ${ant.core.lib} to your project's classpath.
// For example, for a plain Java project with no other dependencies, set in project.properties:
// javac.classpath=${ant.core.lib}
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;

/**
 * @author jsvec
 */
public class BdjRun extends Task {

    private File platformHome;
    private File deploymentRoot;
    private String jvmargs;
    private String args;
    
    //debugger controls
    private boolean debug;
    private String debuggerAddressProperty;
    //out stream
    protected PrintWriter fos = null;

    public 
    @Override
    void execute() throws BuildException {
        String executable = null;
        String image = null;
        
        File[] files= platformHome.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory()){
                if (file.getName().startsWith("uDigital Theatre.exe")){
                    executable = file.getAbsolutePath();
                    image = deploymentRoot.getAbsolutePath() + File.separatorChar + "BDMV" + File.separatorChar + "index.bdmv";
                    break;
                } else if (file.getName().startsWith("PowerDVD.exe")){
                    executable = file.getAbsolutePath();
                    image = deploymentRoot.getAbsolutePath();
                    break;
                } else if (file.getName().startsWith("WinDVD.exe")){
                    executable = file.getAbsolutePath();
                    image = deploymentRoot.getAbsolutePath();
                    break;
                } else if (file.getName().startsWith("sunbdjlauncher")){
                    executeSunBdjLauncher();
                    return;
                }
            }
        }

        if (executable == null){
            throw new BuildException("Can not find any supported executable");
        }
        String[] arg = new String[]{executable, image};
        
        log("Execution arguments: ", Project.MSG_VERBOSE);
        for (int i = 0; i < arg.length; i++) {
            log(arg[i], Project.MSG_VERBOSE);            
        }
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

    private void executeSunBdjLauncher() throws BuildException {
        String libPrefix = new File(getPlatformHome(), "lib").getAbsolutePath();
        StringBuilder classpath = new StringBuilder(3 * libPrefix.length());
        
        classpath.append(libPrefix);
        classpath.append(File.separator);
        classpath.append("bd-j__bdj-emulator.jar");

        classpath.append(File.pathSeparator);

        classpath.append(libPrefix);
        classpath.append(File.separator);
        classpath.append("bd-j__blu-ray-generator.jar");

        Java java = new Java(this);
        java.setFork(true);
        java.setClasspath(new Path(getProject(), classpath.toString()));
        if (debug){
            String debugAddress = null;
            try {
                debugAddress = Integer.toString(this.determineFreePort());
            } catch (IOException e) {
                throw new BuildException(e);
            }
            if (debuggerAddressProperty != null) {
                this.getProject().setProperty(debuggerAddressProperty, debugAddress);
            }

            java.createJvmarg().setValue("-Xdebug");
            java.createJvmarg().setLine("-Xrunjdwp:transport=dt_socket,server=y,address=" + debugAddress);
        }        
        java.createArg().setFile(deploymentRoot);
        java.setClassname("com.sun.jme.bdj.Launcher");
        java.execute();
    }

    public File getDeploymentRoot() {
        return deploymentRoot;
    }

    public void setDeploymentRoot(File deploymentRoot) {
        this.deploymentRoot = deploymentRoot;
    }

    public File getPlatformHome() {
        return platformHome;
    }

    public void setPlatformHome(File platformHome) {
        this.platformHome = platformHome;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getDebuggeraddressproperty() {
        return debuggerAddressProperty;
    }

    public void setDebuggeraddressproperty(String debuggeraddressproperty) {
        this.debuggerAddressProperty = debuggeraddressproperty;
    }

    public String getJvmargs() {
        return jvmargs;
    }

    public void setJvmargs(String jvmargs) {
        this.jvmargs = jvmargs;
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
}
