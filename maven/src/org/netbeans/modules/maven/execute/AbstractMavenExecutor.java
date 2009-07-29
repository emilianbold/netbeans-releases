/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
import org.apache.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import org.netbeans.modules.maven.spi.lifecycle.MavenBuildPlanSupport;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;

/**
 * common code for MAvenExecutors, sharing tabs and actions..
 * @author mkleint
 */
public abstract class AbstractMavenExecutor extends OutputTabMaintainer implements MavenExecutor, Cancellable {

    protected RunConfig config;
    protected ReRunAction rerun;
    protected ReRunAction rerunDebug;
    protected StopAction stop;
    protected BuildPlanAction buildPlan;
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
                rerun.setEnabled(false);
                rerunDebug.setEnabled(false);
                if (AbstractMavenExecutor.this instanceof MavenCommandLineExecutor) {
                    buildPlan.setEnabled(false);
                } else {
                    buildPlan.setEnabled(true);
                }
                stop.setEnabled(true);
            }
        });
    }

    protected final void actionStatesAtFinish() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                rerun.setEnabled(true);
                rerunDebug.setEnabled(true);
                stop.setEnabled(false);
            }
        });
    }

    @Override
    protected void reassignAdditionalContext(Iterator vals) {
        rerun = (ReRunAction) vals.next();
        rerunDebug = (ReRunAction) vals.next();
        stop = (StopAction) vals.next();
        buildPlan = (BuildPlanAction) vals.next();
        rerun.setConfig(config);
        rerunDebug.setConfig(config);
        buildPlan.setConfig(config);
        stop.setExecutor(this);
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

    @Override
    protected final Collection createContext() {
        Collection col = super.createContext();
        col.add(rerun);
        col.add(rerunDebug);
        col.add(stop);
        col.add(buildPlan);
        return col;
    }

    @Override
    protected Action[] createNewTabActions() {
        rerun = new ReRunAction(false);
        rerunDebug = new ReRunAction(true);
        stop = new StopAction();
        buildPlan = new BuildPlanAction();
        rerun.setConfig(config);
        rerunDebug.setConfig(config);
        buildPlan.setConfig(config);
        stop.setExecutor(this);
        Action[] actions;
        if (! isEmbedded()) {
            actions = new Action[]{
                rerun,
                rerunDebug,
                stop
            };
        } else {
            actions = new Action[]{
                rerun,
                rerunDebug,
                buildPlan,
                stop
            };
        }
        return actions;
    }

    protected boolean isEmbedded() {
        return false;
    }

    static class ReRunAction extends AbstractAction {

        private RunConfig config;
        private boolean debug;

        public ReRunAction(boolean debug) {
            this.debug = debug;
            this.putValue(Action.SMALL_ICON, debug ? ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/refreshdebug.png", false) : //NOI18N
                    ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/refresh.png", false));//NOI18N

            putValue(Action.NAME, debug ? NbBundle.getMessage(AbstractMavenExecutor.class, "TXT_Rerun_extra") : NbBundle.getMessage(AbstractMavenExecutor.class, "TXT_Rerun"));
            putValue(Action.SHORT_DESCRIPTION, debug ? NbBundle.getMessage(AbstractMavenExecutor.class, "TIP_Rerun_Extra") : NbBundle.getMessage(AbstractMavenExecutor.class, "TIP_Rerun"));
            setEnabled(false);

        }

        void setConfig(RunConfig config) {
            this.config = config;
        }

        public void actionPerformed(ActionEvent e) {
            if (debug) {
                RunGoalsPanel pnl = new RunGoalsPanel();
                DialogDescriptor dd = new DialogDescriptor(pnl, org.openide.util.NbBundle.getMessage(AbstractMavenExecutor.class, "TIT_Run_maven"));
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

        StopAction() {
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/stop.png", false)); //NOi18N

            putValue(Action.NAME, NbBundle.getMessage(AbstractMavenExecutor.class, "TXT_Stop_execution"));
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(AbstractMavenExecutor.class, "TIP_Stop_Execution"));
            setEnabled(false);
        }

        void setExecutor(AbstractMavenExecutor ex) {
            exec = ex;
        }

        public void actionPerformed(ActionEvent e) {
            exec.cancel();
        }
    }

    static class BuildPlanAction extends AbstractAction {

        private MavenEmbedder embedder;
        private RunConfig config;
        private MavenBuildPlanSupport mbps;

        BuildPlanAction() {
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/maven/execute/buildplangoals.png", false)); //NOi18N

            putValue(Action.NAME, NbBundle.getMessage(AbstractMavenExecutor.class, "TXT_Build_Plan"));
            putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(AbstractMavenExecutor.class, "TIP_Build_Plan_tip"));
            mbps = Lookup.getDefault().lookup(MavenBuildPlanSupport.class);
            setEnabled(false);
        }

        @Override
        public boolean isEnabled() {
            return mbps != null && config!=null && config.getProject()!=null
                    && super.isEnabled();
        }

        public void setConfig(RunConfig config) {
            this.config = config;
        }

        public void setEmbedder(MavenEmbedder embedder) {
            this.embedder = embedder;
            setEnabled(embedder != null);
        }

        public void actionPerformed(ActionEvent e) {
            //
            if (embedder != null && config != null && config.getProject() != null) {
                NbMavenProject prj = config.getProject().getLookup().lookup(NbMavenProject.class);
                mbps.openBuildPlanView(embedder,
                        prj.getMavenProject(),
                        config.getGoals().toArray(new String[0]));
            }
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
