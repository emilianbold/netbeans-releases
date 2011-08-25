/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import static org.netbeans.modules.maven.execute.Bundle.*;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;

/**
 * common code for MAvenExecutors, sharing tabs and actions..
 * @author mkleint
 */
public abstract class AbstractMavenExecutor extends OutputTabMaintainer<AbstractMavenExecutor.TabContext> implements MavenExecutor, Cancellable {

    static final class TabContext {
        ReRunAction rerun;
        ReRunAction rerunDebug;
        StopAction stop;
        @Override protected TabContext clone() {
            TabContext c = new TabContext();
            c.rerun = rerun;
            c.rerunDebug = rerunDebug;
            c.stop = stop;
            return c;
        }
    }

    @Override protected Class<TabContext> tabContextType() {
        return TabContext.class;
    }

    protected RunConfig config;
    private TabContext tabContext = new TabContext();
    private List<String> messages = new ArrayList<String>();
    private List<OutputListener> listeners = new ArrayList<OutputListener>();
    protected ExecutorTask task;
    private static final Set<String> forbidden = new HashSet<String>();
    protected MavenItem item;
    protected final Object SEMAPHORE = new Object();

    static {
        forbidden.add("netbeans.logger.console"); //NOI18N
        forbidden.add("java.util.logging.config.class"); //NOI18N
        forbidden.add("netbeans.autoupdate.language"); //NOI18N
        forbidden.add("netbeans.dirs"); //NOI18N
        forbidden.add("netbeans.home"); //NOI18N
        forbidden.add("sun.awt.exception.handler"); //NOI18N
        forbidden.add("org.openide.TopManager.GUI"); //NOI18N
        forbidden.add("org.openide.major.version"); //NOI18N
        forbidden.add("netbeans.autoupdate.variant"); //NOI18N
        forbidden.add("netbeans.dynamic.classpath"); //NOI18N
        forbidden.add("netbeans.autoupdate.country"); //NOI18N
        forbidden.add("netbeans.hash.code"); //NOI18N
        forbidden.add("org.openide.TopManager"); //NOI18N
        forbidden.add("org.openide.version"); //NOI18N
        forbidden.add("netbeans.buildnumber"); //NOI18N
        forbidden.add("javax.xml.parsers.DocumentBuilderFactory"); //NOI18N
        forbidden.add("javax.xml.parsers.SAXParserFactory"); //NOI18N
        forbidden.add("rave.build"); //NOI18N
        forbidden.add("netbeans.accept_license_class"); //NOI18N
        forbidden.add("rave.version"); //NOI18N
        forbidden.add("netbeans.autoupdate.version"); //NOI18N
        forbidden.add("netbeans.importclass"); //NOI18N
        forbidden.add("netbeans.user"); //NOI18N
//        forbidden.add("java.class.path");
//        forbidden.add("https.nonProxyHosts");

    }
    
    protected AbstractMavenExecutor(RunConfig conf) {
        super(conf.getExecutionName());
        config = conf;

    }


    public final void setTask(ExecutorTask task) {
        synchronized (SEMAPHORE) {
            this.task = task;
            this.item = new MavenItem();
            SEMAPHORE.notifyAll();
        }
    }

    public final void addInitialMessage(String line, OutputListener listener) {
        messages.add(line);
        listeners.add(listener);
    }

    protected final void processInitialMessage() {
        Iterator<String> it1 = messages.iterator();
        Iterator<OutputListener> it2 = listeners.iterator();
        InputOutput ioput = getInputOutput();
        try {
            while (it1.hasNext()) {
                OutputListener ol = it2.next();
                if (ol != null) {
                    ioput.getErr().println(it1.next(), ol, true);
                } else {
                    ioput.getErr().println(it1.next());
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected final void actionStatesAtStart() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tabContext.rerun.setEnabled(false);
                tabContext.rerunDebug.setEnabled(false);
                tabContext.stop.setEnabled(true);
            }
        });
    }

    protected final void actionStatesAtFinish() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tabContext.rerun.setEnabled(true);
                tabContext.rerunDebug.setEnabled(true);
                tabContext.stop.setEnabled(false);
            }
        });
    }

    @Override
    protected void reassignAdditionalContext(TabContext tabContext) {
        this.tabContext = tabContext;
        tabContext.rerun.setConfig(config);
        tabContext.rerunDebug.setConfig(config);
        tabContext.stop.setExecutor(this);
    }

    public static final Properties excludeNetBeansProperties(Properties props) {
        Properties toRet = new Properties();
        Enumeration<String> en = (Enumeration<String>) props.propertyNames();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            if (!forbidden.contains(key)) {
                toRet.put(key, props.getProperty(key));
            }

        }
        return toRet;
    }

    @Override protected final TabContext createContext() {
        return tabContext.clone();
    }

    @Override protected Action[] createNewTabActions() {
        tabContext.rerun = new ReRunAction(false);
        tabContext.rerunDebug = new ReRunAction(true);
        tabContext.stop = new StopAction();
        tabContext.rerun.setConfig(config);
        tabContext.rerunDebug.setConfig(config);
        tabContext.stop.setExecutor(this);
        return new Action[] {
            tabContext.rerun,
            tabContext.rerunDebug,
            tabContext.stop,
        };
    }

    static class ReRunAction extends AbstractAction {

        private RunConfig config;
        private boolean debug;

        @Messages({
            "TXT_Rerun_extra=Re-run with different parameters",
            "TXT_Rerun=Re-run the goals.",
            "TIP_Rerun_Extra=Re-run with different parameters",
            "TIP_Rerun=Re-run the goals."
        })
        ReRunAction(boolean debug) {
            this.debug = debug;
            this.putValue(Action.SMALL_ICON, debug ? ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/refreshdebug.png", false) : //NOI18N
                    ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/refresh.png", false));//NOI18N

            putValue(Action.NAME, debug ? TXT_Rerun_extra() : TXT_Rerun());
            putValue(Action.SHORT_DESCRIPTION, debug ? TIP_Rerun_Extra() : TIP_Rerun());
            setEnabled(false);

        }

        void setConfig(RunConfig config) {
            this.config = config;
        }

        @Messages("TIT_Run_maven=Run Maven")
        @Override public void actionPerformed(ActionEvent e) {
            if (debug) {
                RunGoalsPanel pnl = new RunGoalsPanel();
                DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Run_maven());
                pnl.readConfig(config);
                Object retValue = DialogDisplayer.getDefault().notify(dd);
                if (retValue == DialogDescriptor.OK_OPTION) {
                    BeanRunConfig newConfig = new BeanRunConfig(config);
                    pnl.applyValues(newConfig);
                    RunUtils.executeMaven(newConfig);
                }
            } else {
                RunConfig newConfig = new BeanRunConfig(config);
                RunUtils.executeMaven(newConfig);
            }
        //TODO the waiting on tasks won't work..
        }
    }

    static class StopAction extends AbstractAction {

        private AbstractMavenExecutor exec;

        @Messages({
            "TXT_Stop_execution=Stop execution",
            "TIP_Stop_Execution=Stop the currently executing build"
        })
        StopAction() {
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/stop.png", false)); //NOi18N

            putValue(Action.NAME, TXT_Stop_execution());
            putValue(Action.SHORT_DESCRIPTION, TIP_Stop_Execution());
            setEnabled(false);
        }

        void setExecutor(AbstractMavenExecutor ex) {
            exec = ex;
        }

        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    exec.cancel();
                }
            });
        }
    }

    private class MavenItem implements BuildExecutionSupport.Item {

        public String getDisplayName() {
            return config.getTaskDisplayName();
        }

        public void repeatExecution() {
            RunUtils.executeMaven(config);
        }

        public boolean isRunning() {
            return !task.isFinished();
        }

        public void stopRunning() {
            AbstractMavenExecutor.this.cancel();
        }

    }

}
