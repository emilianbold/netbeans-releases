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
package org.netbeans.modules.coherence.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.properties.InstanceProperties;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * This class represents Coherence server and supports actions for runnning or
 * stopping it, holds its state.
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class CoherenceServer {

    /**
     * Directory inside Coherence platform where are libraries placed.
     */
    public static final String PLATFORM_LIB_DIR = "lib"; //NOI18N
    /**
     * Directory inside Coherence platform where is documentation placed.
     */
    public static final String PLATFORM_DOC_DIR = "doc"; //NOI18N
    /**
     * Directory inside Coherence platform where are binaries placed.
     */
    public static final String PLATFORM_BIN_DIR = "bin"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(CoherenceServer.class.getCanonicalName());
    private final InstanceProperties instanceProperties;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private Thread serverThread = null;
    private boolean running = false;
    private static final String EXCEPTION_IN_MAIN = "Exception in thread \"main\"";

    void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public static enum ServerState {

        STARTING,
        RUNNING,
        STOPPED
    }

    public CoherenceServer(InstanceProperties instanceProperties) {
        this.instanceProperties = instanceProperties;
    }

    public boolean start() {
        return start(instanceProperties);
    }

    public boolean start(final InstanceProperties instanceProperties) {
        running = true;
        serverThread = new Thread() {

            Process process = null;
            InputOutput io = null;

            @Override
            public void run() {
                changeSupport.fireChange();
                String propValue = null;
                try {
//            ProcessBuilder processBuilder = new ProcessBuilder("java -server -showversion com.tangosol.net.DefaultCacheServer");
//            ProcessBuilder processBuilder = new ProcessBuilder("C:/Software/java/jdk1.6/bin/java");
                    List<String> cmd = new ArrayList<String>();
                    cmd.add("java");
                    cmd.add("-server");
                    cmd.add("-showversion");
                    // Get ClassPath Elements
                    String coherenceCP = instanceProperties.getString(CoherenceProperties.PROP_COHERENCE_CLASSPATH, "");
                    String additionalCP = instanceProperties.getString(CoherenceProperties.PROP_ADDITIONAL_CLASSPATH, "");
                    StringBuilder sbClasspath = new StringBuilder();
                    if (coherenceCP != null) {
                        sbClasspath.append(coherenceCP);
                        sbClasspath.append(File.pathSeparator);
                    }
                    if (additionalCP != null) {
                        for (String cp : additionalCP.split(CoherenceProperties.CLASSPATH_SEPARATOR)) {
                            sbClasspath.append(cp).append(File.pathSeparator);
                        }
                    }
                    if (sbClasspath.length() > 0) {
                        cmd.add("-cp");
                        cmd.add(sbClasspath.toString());
                    }
                    io = IOProvider.getDefault().getIO(instanceProperties.getString(CoherenceProperties.PROP_DISPLAY_NAME,
                            CoherenceProperties.DISPLAY_NAME_DEFAULT) + " (Run)", true); //NOI18N
                    String javaFlags = instanceProperties.getString(CoherenceProperties.PROP_JAVA_FLAGS, "");
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

                    for (CoherenceServerProperty property : CoherenceProperties.SERVER_PROPERTIES) {
                        if (property.getPropertyName().startsWith("tangosol.")) {
                            propValue = instanceProperties.getString(property.getPropertyName(), "");
                            io.getOut().println(property.getPropertyName() + " = " + propValue);
                            if (propValue != null && propValue.trim().length() > 0) {
                                cmd.add("-D" + property.getPropertyName() + "=" + propValue);
                            }
                        }
                    }

                    String customProperties = instanceProperties.getString(CoherenceProperties.PROP_CUSTOM_PROPERTIES, "");
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
                    LOGGER.log(Level.INFO, "*** APH-I1 : Command {0}", cmd);
                    ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                    processBuilder.redirectErrorStream(true);
                    process = processBuilder.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line = null;
                    io = IOProvider.getDefault().getIO(instanceProperties.getString(CoherenceProperties.PROP_DISPLAY_NAME,
                            CoherenceProperties.DISPLAY_NAME_DEFAULT) + " (Run)", false); //NOI18N
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
                    LOGGER.log(Level.INFO, "*** APH-I1 : Thread.run() Finished");
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
        return stop(instanceProperties);
    }

    public boolean stop(InstanceProperties instanceProperties) {
        changeSupport.fireChange();
        running = false;
        serverThread.checkAccess();
        serverThread.interrupt();

        if (serverThread.isInterrupted()) {
            LOGGER.log(Level.INFO, "Thread interupted {0}", serverThread.getState());
        } else {
            LOGGER.log(Level.INFO, "Thread NOT interupted {0}", serverThread.getState());
            serverThread.stop();
        }

        serverThread = null;

        return running;
    }

    public boolean isRunning() {
        if (serverThread != null) {
            running = serverThread.isAlive();
            LOGGER.log(Level.FINE, "*** APH-I1 : isRunning() isAlive {0}", serverThread.isAlive());
            LOGGER.log(Level.FINE, "*** APH-I1 : isRunning() getState {0}", serverThread.getState());
            LOGGER.log(Level.FINE, "*** APH-I1 : isRunning() isInterrupted {0}", serverThread.isInterrupted());
        } else {
            running = false;
        }

        return running;
    }

    protected void setRunning(boolean running) {
        this.running = running;
    }

    public InstanceProperties getInstanceProperties() {
        return instanceProperties;
    }
}
