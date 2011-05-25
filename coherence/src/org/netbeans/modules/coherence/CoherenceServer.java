/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class CoherenceServer {

    private static Logger logger = Logger.getLogger(CoherenceServer.class.getCanonicalName());
    private static ResourceBundle bundle = NbBundle.getBundle(CoherenceServer.class);
    private Properties serverProps = null;
    public static final String EXCEPTION_IN_MAIN = "Exception in thread \"main\"";
    private boolean running = false;

    private CoherenceServer() {
        serverProps = null;
    }

    public CoherenceServer(Properties serverProps) {
        this.serverProps = serverProps;
    }

    public boolean start() {
        return start(serverProps);
    }
    Thread serverThread = null;

    public boolean start(final Properties serverProps) {
        running = true;
        serverThread = new Thread() {

            Process process = null;
            InputOutput io = null;

            @Override
            public void run() {
                String propValue = null;
                try {
//            ProcessBuilder processBuilder = new ProcessBuilder("java -server -showversion com.tangosol.net.DefaultCacheServer");
//            ProcessBuilder processBuilder = new ProcessBuilder("C:/Software/java/jdk1.6/bin/java");
                    List<String> cmd = new ArrayList<String>();
                    cmd.add("java");
                    cmd.add("-server");
                    cmd.add("-showversion");
                    // Get ClassPath Elements
                    String coherenceCP = serverProps.getProperty("coherence.classpath");
                    String additionalCP = serverProps.getProperty("additional.classpath");
                    StringBuilder sbClasspath = new StringBuilder();
                    if (coherenceCP != null) {
                        sbClasspath.append(coherenceCP);
                        sbClasspath.append(System.getProperty("path.separator", ""));
                    }
                    if (additionalCP != null) {
                        sbClasspath.append(additionalCP);
                    }
                    if (sbClasspath.length() > 0) {
                        cmd.add("-cp");
                        cmd.add(sbClasspath.toString());
                    }
                    io = IOProvider.getDefault().getIO("Coherence Server " + serverProps.getProperty(ServerPropertyFileManager.SERVERNAME_KEY) + " (Run)", true);

                    String javaFlags = serverProps.getProperty("java.flags");
                    if (javaFlags != null && javaFlags.trim().length() > 0) {
                        StringTokenizer st = new StringTokenizer(javaFlags, " \t;:\n");
                        String val = null;
                        while (st.hasMoreTokens()) {
                            val = st.nextToken();
                            if (val != null && val.length() > 0) {
                                cmd.add(val);
                            }
                        }
                    }

                    for (Object key : serverProps.keySet()) {
                        if (key.toString().startsWith("tangosol.")) {
                            propValue = serverProps.getProperty(key.toString());
                            io.getOut().println(key.toString() + " = " + propValue);
                            if (propValue != null && propValue.trim().length() > 0) {
                                cmd.add("-D" + key.toString() + "=" + propValue);
                            }
                        }
                    }

                    String customProperties = serverProps.getProperty("custom.properties");
                    if (customProperties != null && customProperties.trim().length() > 0) {
                        StringTokenizer st = new StringTokenizer(customProperties, " \t;:\n");
                        String val = null;
                        while (st.hasMoreTokens()) {
                            val = st.nextToken();
                            if (val != null && val.length() > 0) {
                                cmd.add(val);
                            }
                        }
                    }

                    cmd.add("com.tangosol.net.DefaultCacheServer");
                    logger.log(Level.INFO, "*** APH-I1 : Command " + cmd);
                    ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                    processBuilder.redirectErrorStream(true);
                    process = processBuilder.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line = null;
                    io = IOProvider.getDefault().getIO("Coherence Server " + serverProps.getProperty(ServerPropertyFileManager.SERVERNAME_KEY) + " (Run)", false);
                    io.getOut().println("************************************************************************************");
                    io.getOut().println("Start Command : " + cmd.toString().replace(",", "").replace("[", "").replace("]", ""));
                    io.getOut().println("************************************************************************************");
                    while ((line = br.readLine()) != null) {
                        io.getOut().println(line);
                        if (line != null && line.indexOf(EXCEPTION_IN_MAIN) >= 0) {
                            interrupt();
                            break;
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    logger.log(Level.INFO, "*** APH-I1 : Thread.run() Finished");
                    setRunning(false);
                }
            }

            @Override
            public void interrupt() {
                super.interrupt();
                process.destroy();
                setRunning(false);
                io.getOut().println("******************* Server Terminated *******************");
            }

            @Override
            protected void finalize() throws Throwable {
                try {
                    io.getOut().flush();
                } catch (Exception e) {
                }
                try {
                    io.getOut().close();
                } catch (Exception e) {
                }
                super.finalize();
            }
        };

        serverThread.start();
        running = serverThread.isAlive();

        return running;
    }

    public boolean stop() {
        return stop(serverProps);
    }

    public boolean stop(Properties serverProps) {
        running = false;
        serverThread.checkAccess();
        serverThread.interrupt();

        if (serverThread.isInterrupted()) {
            logger.log(Level.INFO, "Thread interupted " + serverThread.getState());
        } else {
            logger.log(Level.INFO, "Thread NOT interupted " + serverThread.getState());
            serverThread.stop();
        }

        serverThread = null;

        return running;
    }

    public boolean isRunning() {
        if (serverThread != null) {
            running = serverThread.isAlive();
            logger.log(Level.FINE, "*** APH-I1 : isRunning() isAlive " + serverThread.isAlive());
            logger.log(Level.FINE, "*** APH-I1 : isRunning() getState " + serverThread.getState());
            logger.log(Level.FINE, "*** APH-I1 : isRunning() isInterrupted " + serverThread.isInterrupted());
        } else {
            running = false;
        }

        return running;
    }

    protected void setRunning(boolean running) {
        this.running = running;
    }
    
}
