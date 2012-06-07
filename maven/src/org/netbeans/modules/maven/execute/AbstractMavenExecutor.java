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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import static org.netbeans.modules.maven.execute.Bundle.*;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import org.netbeans.modules.maven.options.MavenOptionController;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
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

    public static final class TabContext {
        ReRunAction rerun;
        ReRunAction rerunDebug;
        ResumeAction resume;
        StopAction stop;
        OptionsAction options;
        @Override protected TabContext clone() {
            TabContext c = new TabContext();
            c.rerun = rerun;
            c.rerunDebug = rerunDebug;
            c.resume = resume;
            c.stop = stop;
            c.options = options;
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


    @Override public final void setTask(ExecutorTask task) {
        synchronized (SEMAPHORE) {
            this.task = task;
            this.item = new MavenItem();
            SEMAPHORE.notifyAll();
        }
    }

    @Override public final void addInitialMessage(String line, OutputListener listener) {
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
            @Override public void run() {
                tabContext.rerun.setEnabled(false);
                tabContext.rerunDebug.setEnabled(false);
                tabContext.resume.setFinder(null);
                tabContext.stop.setEnabled(true);
            }
        });
    }

    protected interface ResumeFromFinder {
        @CheckForNull NbMavenProject find(@NonNull Project root);
    }

    protected final void actionStatesAtFinish(final @NullAllowed ResumeFromFinder resumeFromFinder) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                tabContext.rerun.setEnabled(true);
                tabContext.rerunDebug.setEnabled(true);
                tabContext.resume.setFinder(resumeFromFinder);
                tabContext.stop.setEnabled(false);
            }
        });
    }

    @Override
    protected void reassignAdditionalContext(TabContext tabContext) {
        this.tabContext = tabContext;
        tabContext.rerun.setConfig(config);
        tabContext.rerunDebug.setConfig(config);
        tabContext.resume.setConfig(config);
        tabContext.stop.setExecutor(this);
    }

    @SuppressWarnings("element-type-mismatch")
    public static Properties excludeNetBeansProperties(Properties props) {
        Properties toRet = new Properties();
        for (Map.Entry<Object,Object> entry : props.entrySet()) {
            if (!forbidden.contains(entry.getKey())) {
                toRet.put(entry.getKey(), entry.getValue());
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
        tabContext.resume = new ResumeAction();
        tabContext.stop = new StopAction();
        tabContext.options = new OptionsAction();
        tabContext.rerun.setConfig(config);
        tabContext.rerunDebug.setConfig(config);
        tabContext.resume.setConfig(config);
        tabContext.stop.setExecutor(this);
        return new Action[] {
            tabContext.rerun,
            tabContext.rerunDebug,
            tabContext.resume,
            tabContext.stop,
            tabContext.options,
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

    private static class ResumeAction extends AbstractAction {

        private static final RequestProcessor RP = new RequestProcessor(ResumeAction.class);
        private RunConfig config;
        private ResumeFromFinder finder;

        @Messages("TIP_resume=Resume build starting from failed submodule.")
        ResumeAction() {
            setEnabled(false);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/forward.png", true));
            putValue(SHORT_DESCRIPTION, TIP_resume());
        }

        void setConfig(RunConfig config) {
            this.config = config;
        }

        void setFinder(ResumeFromFinder finder) {
            this.finder = finder;
            setEnabled(finder != null);
        }

        @Messages({
            "ResumeAction_scanning=Searching for faulty module",
            "ResumeAction_could_not_find_module=Could not determine module from which to resume build."
        })
        @Override public void actionPerformed(ActionEvent e) {
            final Project p = config.getProject();
            if (p == null) {
                setFinder(null);
                StatusDisplayer.getDefault().setStatusText(ResumeAction_could_not_find_module());
                return;
            }
            final AtomicReference<Thread> t = new AtomicReference<Thread>();
            final ProgressHandle handle = ProgressHandleFactory.createHandle(ResumeAction_scanning(), new Cancellable() {
                @Override public boolean cancel() {
                    Thread _t = t.get();
                    if (_t != null) {
                        _t.interrupt();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            RP.post(new Runnable() {
                @Override public void run() {
                    t.set(Thread.currentThread());
                    handle.start();
                    NbMavenProject nbmp;
                    try {
                        nbmp = finder.find(p);
                    } finally {
                        handle.finish();
                    }
                    t.set(null);
                    if (nbmp == null || NbMavenProject.isErrorPlaceholder(nbmp.getMavenProject())) {
                        setFinder(null);
                        StatusDisplayer.getDefault().setStatusText(ResumeAction_could_not_find_module());
                        return;
                    }
                    File root = config.getExecutionDirectory();
                    File module = nbmp.getMavenProject().getBasedir();
                    String rel = root != null && module != null ? FileUtilities.relativizeFile(root, module) : null;
                    String id = rel != null ? rel : nbmp.getMavenProject().getGroupId() + ':' + nbmp.getMavenProject().getArtifactId();
                    BeanRunConfig newConfig = new BeanRunConfig(config);
                    List<String> goals = new ArrayList<String>(config.getGoals());
                    int rf = goals.indexOf("--resume-from");
                    if (rf != -1) {
                        goals.set(rf + 1, id);
                    } else {
                        goals.add(0, "--resume-from");
                        goals.add(1, id);
                    }
                    newConfig.setGoals(goals);
                    RunUtils.executeMaven(newConfig);
                }
            });
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

        @Override public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            RequestProcessor.getDefault().post(new Runnable() {
                @Override public void run() {
                    exec.cancel();
                }
            });
        }
    }

    private static final class OptionsAction extends AbstractAction {

        @Messages("LBL_OptionsAction=Maven Settings")
        OptionsAction() {
            super(LBL_OptionsAction(), ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/options.png", true));
            putValue(Action.SHORT_DESCRIPTION, LBL_OptionsAction());
        }

        @Override public void actionPerformed(ActionEvent e) {
            OptionsDisplayer.getDefault().open(JavaOptions.JAVA + "/" + MavenOptionController.OPTIONS_SUBPATH);
        }

    }

    protected class MavenItem implements BuildExecutionSupport.Item {

        @Override public String getDisplayName() {
            return config.getTaskDisplayName();
        }

        @Override public void repeatExecution() {
            RunUtils.executeMaven(config);
        }

        @Override public boolean isRunning() {
            return !task.isFinished();
        }

        @Override public void stopRunning() {
            AbstractMavenExecutor.this.cancel();
        }

    }

}
