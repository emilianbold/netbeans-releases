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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.jconsole;

import java.awt.event.ActionEvent;
import java.io.IOException;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.HelpCtx;

/**
 *
 * Lauch JConsole action
 *
 * @author jfdenise
 * @author Tomas Hurka
 */
public class LaunchAction extends AbstractAction {

    JConsoleIO jconsole;

    class RunAction implements Runnable {

        public RunAction() {
        }

        public void run() {
            try {
                JConsoleSettings settings = JConsoleSettings.getDefault();
                String cp = settings.NETBEANS_CLASS_PATH;
                String settingsCP = settings.getClassPath();

                if (settingsCP != null) {
                    cp = cp + File.pathSeparator + settingsCP;
                }

                String url = settings.getDefaultUrl() == null ? "" : settings.getDefaultUrl();// NOI18N
                String polling = String.valueOf(settings.getPolling());
                String vmOptions = settings.getVMOptions() == null ? "" : settings.getVMOptions();// NOI18N
                String tile = !settings.getTile() ? "-notile" : "";// NOI18N
                String javahome = System.getProperty("jdk.home");// NOI18N
                String pluginsPath = settings.getPluginsPath();
                String otherArgs = settings.getOtherArgs();

                String classpath = cp + File.pathSeparator + javahome + File.separator + "lib" + // NOI18N
                        File.separator + "jconsole.jar";// NOI18N
                boolean jdk6 = JConsoleSettings.isNetBeansJVMGreaterThanJDK15();
                final List<String> arguments = new ArrayList<String>();
                final String executable = javahome + File.separator + "bin" + File.separator + "java";
                ExternalProcessBuilder extProcess = new ExternalProcessBuilder(executable);
                if (vmOptions != null && !vmOptions.equals("")) {
                    arguments.add(vmOptions);
                }
                arguments.add("-classpath");// NOI18N

                arguments.add(classpath);
                arguments.add("sun.tools.jconsole.JConsole");// NOI18N
                if (jdk6) {
                    if (pluginsPath != null && !pluginsPath.equals("")) { // NOI18N
                        arguments.add("-pluginpath");// NOI18N
                        arguments.add(pluginsPath);
                    }
                }

                arguments.add("-interval=" + polling);// NOI18N

                if (tile != null && !tile.equals("")) { // NOI18N
                    arguments.add(tile);
                }

                if (otherArgs != null && !otherArgs.equals("")) {// NOI18N
                    arguments.add(otherArgs);
                }
                if (url != null && !url.equals("")) {// NOI18N
                    arguments.add(url);
                }

                final String msg1 = NbBundle.getMessage(LaunchAction.class, "LBL_ActionStartingMessage");// NOI18N

                for (String arg : arguments) {
                    extProcess = extProcess.addArgument(arg);
                }

                jconsole = new JConsoleIO(msg1, executable, arguments);
                ExecutionDescriptor descriptor = new ExecutionDescriptor().optionsPath(OptionsDisplayer.ADVANCED + "/JConsole").
                        inputVisible(true).
                        controllable(true).
                        showProgress(true).
                        outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

                    public InputProcessor newInputProcessor(final InputProcessor defaultProcessor) {
                        jconsole.setDefaultProcessor(defaultProcessor);
                        return jconsole;
                    }
                });

                ExecutionService executionService = ExecutionService.newService(new JConsoleCallable(extProcess), descriptor, NbBundle.getMessage(LaunchAction.class, "LBL_OutputName"));
                Future<Integer> task = executionService.run();

                task.get();
            } catch (Exception e) {
                if (!(e instanceof CancellationException)) {
                    jconsole.message(e.toString());
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    public LaunchAction() {
        putValue(Action.NAME,
                NbBundle.getMessage(LaunchAction.class, "LBL_ActionName")); // NOI18N
        putValue(Action.SHORT_DESCRIPTION,
                NbBundle.getMessage(LaunchAction.class, "HINT_StartJConsole")); // NOI18N
        putValue(
                "iconBase", // NOI18N
                "org/netbeans/modules/jmx/jconsole/resources/console.png" //NOI18N
                );

        //Needed in Tools|Options|...| ToolBars action icons
        putValue(
                Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/jmx/jconsole/resources/console.png", false));
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    protected boolean asynchronous() {
        return false;
    }

    //public synchronized void performAction() {
    public synchronized void actionPerformed(ActionEvent evt) {
        if (isStarted()) {
            String msg = NbBundle.getMessage(LaunchAction.class, "LBL_ActionAlreadyStartedMessage");// NOI18N
            if (jconsole != null) {
                jconsole.message(msg);
            }
            return;
        }
        RunAction action = new RunAction();
        RequestProcessor.getDefault().post(action);
    }

    synchronized boolean isStarted() {
        return started;
    }

    private synchronized void started() {
        started = true;
    }

    synchronized void stopped() {
        if (!started) {
            return;
        }
        started = false;
        jconsole = null;
    }
    private boolean started;

    private class JConsoleIO implements InputProcessor {

        private InputProcessor defaultProcessor;
        private List<String> buffer;

        JConsoleIO(String msg1, String executable, List<String> arguments) {
            buffer = new ArrayList();
            message(msg1);
            message(executable);
            for (String arg : arguments) {
                message(arg);
            }

        }

        private final void message(String text) {
            if (defaultProcessor == null) {
                buffer.add(text);
            } else {
                try {
                    defaultProcessor.processInput(text.concat("\n").toCharArray());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        public void processInput(char[] chars) throws IOException {
            defaultProcessor.processInput(chars);
        }

        public void reset() throws IOException {
            defaultProcessor.reset();
        }

        public void close() throws IOException {
            String msg = NbBundle.getMessage(LaunchAction.class, "LBL_ActionStoppedMessage");// NOI18N
            message(msg);
            defaultProcessor.close();
            stopped();
        }

        private void setDefaultProcessor(InputProcessor processor) {
            defaultProcessor = processor;
            for (String msg : buffer) {
                message(msg);
            }
        }
    }

    private class JConsoleCallable implements Callable<Process> {

        Callable<Process> extProc;

        JConsoleCallable(Callable<Process> callable) {
            extProc = callable;
        }

        public Process call() throws Exception {
            Process p = extProc.call();
            String msg2 = NbBundle.getMessage(LaunchAction.class, "LBL_ActionStartedMessage");// NOI18N

            started();
            jconsole.message(msg2);
            return p;
        }
    }
}

