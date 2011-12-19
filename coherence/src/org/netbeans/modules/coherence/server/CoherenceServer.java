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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * This class represents Coherence server and supports actions for running or stopping it, holds its state.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class CoherenceServer {

    private static final Logger LOGGER = Logger.getLogger(CoherenceServer.class.getCanonicalName());

    private final CoherenceProperties coherenceProperties;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /** GuardedBy("this") */
    private final AtomicBoolean starting = new AtomicBoolean(false);

    /** GuardedBy("this") */
    private Future<Integer> runningTask;

    private static enum ServerState {
        /** Started server which is not still fully running. */
        STARTING,
        /** Running server. */
        RUNNING,
        /** Stopped server. */
        STOPPED
    }

    /**
     * Adds listener for getting events about Coherence server state changes.
     * @param listener listener to add
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes listener.
     * @param listener listener to remove
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    /**
     * Creates instance of new Coherence server.
     * @param coherenceProperties properties for the Coherence server
     */
    public CoherenceServer(CoherenceProperties coherenceProperties) {
        this.coherenceProperties = coherenceProperties;
    }

    /**
     * Starts this Coherence server.
     */
    public synchronized void start() {
        LOGGER.log(Level.INFO, "Starting Coherence server: {0}", getServerDisplayName());
        starting.set(true);
        ExecutionService startService = getStartService();
        runningTask = startService.run();
        changeSupport.fireChange();
    }

    /**
     * Restarts this Coherence server.
     */
    public synchronized void restart() {
        LOGGER.log(Level.INFO, "Restarting Coherence server: {0}", getServerDisplayName());
        stop();
        start();
    }

    /**
     * Stops this Coherence server.
     */
    public synchronized void stop() {
        LOGGER.log(Level.INFO, "Stopping Coherence server: {0}", getServerDisplayName());
        starting.set(false);
        runningTask.cancel(true);
        changeSupport.fireChange();
    }

    /**
     * Says whether this Coherence server is running or not.
     * @return {@code true} when the server runs, {@code false} otherwise
     */
    public synchronized boolean isRunning() {
        if (runningTask != null) {
            boolean state = !runningTask.isDone() && !starting.get();
            LOGGER.log(Level.FINEST, "Coherence server {0} isRunning()={1}", new Object[]{getServerDisplayName(), state});
            return state;
        }
        return false;
    }

    /**
    * Says whether this Coherence server is starting or not.
    * @return {@code true} when the server is starting, {@code false} otherwise
    */
    public synchronized boolean isStarting() {
        if (runningTask != null) {
            boolean state = !runningTask.isDone() && starting.get();
            LOGGER.log(Level.FINEST, "Coherence server {0} isStarting()={1}", new Object[]{getServerDisplayName(), state});
            return state;
        }
        return false;
    }

    /**
     * Says whether is Coherence server engaged or not.
     * @return {@code true} when the server is engaged (running or starting), {@code false} otherwise
     */
    public boolean isEngaged() {
        return isStarting() || isRunning();
    }

    /**
     * Gets Coherence instance properties.
     * @return Coherence instance properties
     */
    public CoherenceProperties getCoherenceProperties() {
        return coherenceProperties;
    }

    private String getServerDisplayName() {
        return coherenceProperties.getDisplayName();
    }

    private synchronized void switchFromStartingToRunningState() {
        starting.set(false);
        changeSupport.fireChange();
    }

    private ExecutionService getStartService() {
        ExecutionDescriptor descriptor = new ExecutionDescriptor();
        descriptor = descriptor.frontWindow(true).inputVisible(true).outLineBased(true).
                outProcessorFactory(new CoherenceInputProcessorFactory());

        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(getJavaBinary()).redirectErrorStream(true);
        processBuilder = processBuilder.addArgument("-server").addArgument("-showversion"); //NOI18N

        // appending classpath elements
        processBuilder = appendClassPathItems(processBuilder);
        // appending java flags
        processBuilder = appendCustomProperties(CoherenceModuleProperties.PROP_JAVA_FLAGS, processBuilder);
        // appending server properties
        processBuilder = appendServerProperties(processBuilder);
        // appending java flags
        processBuilder = appendCustomProperties(CoherenceModuleProperties.PROP_CUSTOM_PROPERTIES, processBuilder);

        processBuilder = processBuilder.addArgument("com.tangosol.net.DefaultCacheServer"); //NOI18N

        return ExecutionService.newService(processBuilder, descriptor,
                NbBundle.getMessage(CoherenceServer.class, "LBL_CoherenceServerOutputTab", getServerDisplayName())); //NOI18N
    }

    private static String getJavaBinary() {
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection<FileObject> folders = platform.getInstallFolders();
        String javaBinary = Utilities.isWindows() ? "java.exe" : "java"; // NOI18N
        if (folders.size() > 0) {
            FileObject folder = folders.iterator().next();
            File file = FileUtil.toFile(folder);
            if (file != null) {
                javaBinary = file.getAbsolutePath() + File.separator
                        + "bin" + File.separator //NOI18N
                        + (Utilities.isWindows() ? "java.exe" : "java"); // NOI18N
            }
        }
        return javaBinary;
    }

    private ExternalProcessBuilder appendClassPathItems(ExternalProcessBuilder builder) {
        StringBuilder sbClasspath = new StringBuilder();
        if (!coherenceProperties.getClasspath().isEmpty()) {
            for (String cp : coherenceProperties.getClasspath().split(CoherenceModuleProperties.CLASSPATH_SEPARATOR)) {
                sbClasspath.append(cp).append(File.pathSeparator);
            }
        }
        if (sbClasspath.length() > 0) {
            builder = builder.addArgument("-cp").addArgument(sbClasspath.toString()); //NOI18N
        }
        return builder;
    }

    private ExternalProcessBuilder appendCustomProperties(String propertyName, ExternalProcessBuilder builder) {
        String propertyValue = coherenceProperties.getProperty(propertyName); //NOI18N
        if (!propertyValue.trim().isEmpty()) {
            StringTokenizer st = new StringTokenizer(propertyValue, " \t;:\n"); //NOI18N
            String val;
            while (st.hasMoreTokens()) {
                val = st.nextToken();
                if (val != null && val.length() > 0) {
                    LOGGER.log(Level.FINE, "Appened property {0} = {1}", new Object[]{propertyName, propertyValue});
                    builder = builder.addArgument(val);
                }
            }
        }
        return builder;
    }

    private ExternalProcessBuilder appendServerProperties(ExternalProcessBuilder builder) {
        StringBuilder serverProperties = new StringBuilder("Appened server properties: \n"); //NOI18N
        for (CoherenceServerProperty property : CoherenceProperties.SERVER_PROPERTIES) {
            if (property.getPropertyName().startsWith("tangosol.")) { //NOI18N
                String propValue = coherenceProperties.getProperty(property.getPropertyName()); //NOI18N
                if (!propValue.trim().isEmpty()) {
                    serverProperties.append(
                            MessageFormat.format("{0} = {1} \n", property.getPropertyName(), propValue)); //NOI18N
                    builder = builder.addArgument("-D" + property.getPropertyName() + "=" + propValue); //NOI18N
                }
            }
        }
        LOGGER.log(Level.FINE, serverProperties.toString());
        return builder;
    }

    private class CoherenceInputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.proxy(
                    new CoherenceDefaultInputProcessor(defaultProcessor),
                    InputProcessors.bridge(new LastLineProcessor()));
        }
    }

    private static class CoherenceDefaultInputProcessor implements InputProcessor {

        private InputProcessor defaultProcessor;

        public CoherenceDefaultInputProcessor(InputProcessor defaultProcessor) {
            this.defaultProcessor = defaultProcessor;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            defaultProcessor.processInput(chars);
        }

        @Override
        public void reset() throws IOException {
            defaultProcessor.reset();
        }

        @Override
        public void close() throws IOException {
            defaultProcessor.processInput(
                    NbBundle.getMessage(CoherenceServer.class, "MSG_CoherenceServerTerminated").toCharArray()); //NOI18N
            defaultProcessor.close();
        }
    }

    private class LastLineProcessor implements LineProcessor {

        private final Pattern STACK_TRACE_PATTERN = Pattern.compile("^\\s+at.*$"); // NOI18N
        private String last = "";

        @Override
        public synchronized void processLine(String line) {
            if (line.length() != 0 && !STACK_TRACE_PATTERN.matcher(line).matches()) {
                last = line;
                if (line.startsWith("Started")) { //NOI18N
                    switchFromStartingToRunningState();
                }
            }
        }

        public synchronized String getLastLine() {
            return last;
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }
    }
}
