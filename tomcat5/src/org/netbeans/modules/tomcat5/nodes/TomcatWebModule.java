/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.nodes;

import java.util.Comparator;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.CommandType;
import org.netbeans.modules.tomcat5.TomcatModule;
import org.netbeans.modules.tomcat5.nodes.actions.TomcatWebModuleCookie;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 * @author Petr Hejl
 */
public class TomcatWebModule implements TomcatWebModuleCookie {

    /** Simple comparator for sorting nodes by name. */
    public static final Comparator<TomcatWebModule> TOMCAT_WEB_MODULE_COMPARATOR = new Comparator<TomcatWebModule>() {

        public int compare(TomcatWebModule wm1, TomcatWebModule wm2) {
            return wm1.getTomcatModule ().getModuleID().compareTo(wm2.getTomcatModule ().getModuleID());
        }
    };

    private final TomcatModule tomcatModule;
    private final TomcatManager manager;

    private volatile boolean isRunning;

    private Node node;

    private final TargetModuleID[] target;


    /** Creates a new instance of TomcatWebModule */
    public TomcatWebModule(DeploymentManager manager, TomcatModule tomcatModule, boolean isRunning) {
        this.tomcatModule = tomcatModule;
        this.manager = (TomcatManager)manager;
        this.isRunning = isRunning;
        target = new TargetModuleID[]{tomcatModule};
    }

    public TomcatModule getTomcatModule () {
        return tomcatModule;
    }

    public void setRepresentedNode(Node node) {
        this.node = node;
    }

    public Node getRepresentedNode () {
        return node;
    }

    public DeploymentManager getDeploymentManager() {
        return manager;
    }

    /**
     * Undeploys the web application described by this module.
     *
     * @return task in which the undeployment itself is processed. When the
     *             task is finished it implicate that undeployment is finished
     *             (failed or completed).
     */
    public Task undeploy() {
        return RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_UNDEPLOY",  // NOI18N
                    new Object [] { getTomcatModule().getPath() }));

                ProgressObject po = manager.undeploy(target);
                TomcatProgressListener listener = new TomcatProgressListener(po);
                po.addProgressListener(listener);
                listener.updateState();

                CompletionWait wait = new CompletionWait(po);
                wait.init();
                wait.waitFinished();
            }
        }, 0);
    }

    public void start() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STARTING",  // NOI18N
                    new Object [] { getTomcatModule().getPath() }));
                ProgressObject po = manager.start(target);
                TomcatProgressListener listener = new TomcatProgressListener(po);
                po.addProgressListener(listener);
                listener.updateState();
            }
        }, 0);
    }

    public void stop() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(TomcatWebModule.class, "MSG_START_STOPPING",  // NOI18N
                    new Object [] { getTomcatModule ().getPath() }));
                ProgressObject po = manager.stop(target);
                TomcatProgressListener listener = new TomcatProgressListener(po);
                po.addProgressListener(listener);
                listener.updateState();
            }
        }, 0);
    }

    public boolean isRunning() {
        return isRunning;
    }


    private String constructDisplayName(){
        if (isRunning())
            return getTomcatModule ().getPath();
        else
            return getTomcatModule ().getPath() + " [" + NbBundle.getMessage(TomcatWebModuleNode.class, "LBL_Stopped")  // NOI18N
               +  "]";
    }

    /**
     * Opens the log file defined for this web moudel in the ouput window.
     */
    public void openLog() {
        manager.logManager().openContextLog(tomcatModule);
    }

    /**
     * Returns <code>true</code> if there is a logger defined for this module,
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if there is a logger defined for this module,
     *         <code>false</code> otherwise.
     */
    public boolean hasLogger() {
         return manager.logManager().hasContextLogger(tomcatModule);
    }

    private class TomcatProgressListener implements ProgressListener {

        private final ProgressObject progressObject;

        private boolean finished;

        public TomcatProgressListener(ProgressObject progressObject) {
            this.progressObject = progressObject;
        }

        public void handleProgressEvent(ProgressEvent progressEvent) {
            updateState();
        }

        public synchronized void updateState() {
            if (finished) {
                return;
            }

            DeploymentStatus deployStatus = progressObject.getDeploymentStatus();
            if (deployStatus == null) {
                return;
            }

            if (deployStatus.isCompleted() || deployStatus.isFailed()) {
                finished = true;
            }

            if (deployStatus.getState() == StateType.COMPLETED) {
                CommandType command = deployStatus.getCommand();

                if (command == CommandType.START || command == CommandType.STOP) {
                        StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                        if (command == CommandType.START) {
                            isRunning = true;
                        } else {
                            isRunning = false;
                        }
                        node.setDisplayName(constructDisplayName());
                } else if (command == CommandType.UNDEPLOY) {
                    StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
                }
            } else if (deployStatus.getState() == StateType.FAILED) {
                NotifyDescriptor notDesc = new NotifyDescriptor.Message(
                        deployStatus.getMessage(),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(notDesc);
                StatusDisplayer.getDefault().setStatusText(deployStatus.getMessage());
            }
        }
    }


    /**
     * Helper class for blocking wait until the deployment manager operation
     * gets finished.
     * <p>
     * The class is <i>thread safe</i>.
     *
     * @author Petr Hejl
     */
    private static class CompletionWait implements ProgressListener {

        private final ProgressObject progressObject;

        private boolean completed;

        /**
         * Constructs the CompletionWait object that will wait for
         * given ProgressObject.
         *
         * @param progressObject object that we want to wait for
         *             must not be <code>null</code>
         */
        public CompletionWait(ProgressObject progressObject) {
            Parameters.notNull("progressObject", progressObject);

            this.progressObject = progressObject;
        }

        /**
         * Initialize this object. Until calling this method any thread that
         * has called {@link #waitFinished()} will wait unconditionaly (does not
         * matter what is the state of the ProgressObject.
         */
        public void init() {
            synchronized (this) {
                progressObject.addProgressListener(this);
                // to be sure we didn't missed the state
                handleProgressEvent(null);
            }
        }

        /**
         * Handles the progress. May lead to notifying threads waiting in
         * {@link #waitFinished()}.
         *
         * @param evt event to handle
         */
        public void handleProgressEvent(ProgressEvent evt) {
            synchronized (this) {
                DeploymentStatus status = progressObject.getDeploymentStatus();
                if (status.isCompleted() || status.isFailed()) {
                    completed = true;
                    notifyAll();
                }
            }
        }

        /**
         * Block the calling thread until the progress object indicates the
         * competion or failure. If the task described by ProgressObject is
         * already finished returns immediately.
         */
        public void waitFinished() {
            synchronized (this) {
                if (completed) {
                    return;
                }

                while (!completed) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // don't response to interrupt
                    }
                }
            }
        }
    }
}
