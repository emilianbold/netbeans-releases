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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.remote.client;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.ui.output.OutputLogger;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public abstract class GitProgressSupport implements Runnable, Cancellable {
    private volatile boolean canceled;

    private static final Logger LOG = Logger.getLogger(GitProgressSupport.class.getName());

    private ProgressHandle progressHandle = null;
    private String displayName = ""; // NOI18N
    private String originalDisplayName;
    private VCSFileProxy repositoryRoot;
    private RequestProcessor.Task task;
    private GitClient gitClient;
    private OutputLogger logger;
    private final ProgressMonitorImpl progressMonitor = new ProgressMonitorImpl();
    private boolean error;
    private int steps;
    private int unitsProcessed;
    private ProgressDelegate progress;

    public GitProgressSupport () {
        this(1);
    }
    
    public GitProgressSupport (int steps) {
        this.steps = steps;
    }
    
    public RequestProcessor.Task start (RequestProcessor rp, VCSFileProxy repositoryRoot, String displayName) {
        this.error = false;
        this.originalDisplayName = displayName;
        setDisplayName(displayName);
        this.repositoryRoot = repositoryRoot;
        startProgress();
        setProgressQueued();
        task = rp.post(this);
        return task;
    }

    @Override
    public void run() {
        setProgress();
        performIntern();
    }

    protected void performIntern () {
        try {
            LOG.log(Level.FINE, "Start - {0}", originalDisplayName); //NOI18N
            if(!canceled) {
                perform();
            }
        } finally {
            LOG.log(Level.FINE, "End - {0}", originalDisplayName); //NOI18N
            finishProgress();
            getLogger().closeLog();
            if (gitClient != null) {
                gitClient.release();
            }
        }
    }

    protected abstract void perform ();

    public synchronized boolean isCanceled () {
        return canceled;
    }

    @Override
    public synchronized boolean cancel () {
        if (canceled) {
            return false;
        }
        if (task != null) {
            if (task.cancel()) {
                finishProgress();
            }
        }
        return canceled = true;
    }

    public JComponent getProgressComponent() {
        return ProgressHandleFactory.createProgressComponent(getProgressHandle());
    }

    public RequestProcessor.Task getTask () {
        return task;
    }
    
    public final boolean isFinished () {
        return task != null && task.isFinished();
    }
    
    public final boolean isFinishedSuccessfully () {
        return isFinished() && !isCanceled() && !error;
    }

    public final boolean isError () {
        return error;
    }
    
    protected String getDisplayName () {
        return displayName;
    }
    
    protected final void setError (boolean error) {
        this.error = error;
    }
    
    protected void setDisplayName (String displayName) {
        this.displayName = displayName;
        setProgress();
    }

    protected final void setDisplayName (String displayName, int units) {
        updateProgress(units);
        setDisplayName(displayName);
    }
    
    protected final void updateProgress (int units) {
        assert steps > 1;
        unitsProcessed += units;
        if (progressHandle != null) {
            progressHandle.progress(100 * unitsProcessed);
        }
    }
    
    protected final ProgressDelegate getProgress () {
        if (progress == null) {
            progress = new ProgressDelegate(this);
        }
        return progress;
    }

    private void setProgressQueued () {
        if(progressHandle!=null) {
            setProgressMessage(progressHandle, NbBundle.getMessage(GitProgressSupport.class, "LBL_Queued", displayName)); // NOI18N
        }
    }

    private void setProgress () {
        if(progressHandle!=null) {
            setProgressMessage(progressHandle, displayName);
        }
    }

    protected ProgressHandle getProgressHandle () {
        if (progressHandle == null) {
            Action openAction = getLogger().getOpenOutputAction();
            if (openAction == null) {
                progressHandle = ProgressHandleFactory.createHandle(displayName, this);
            } else {
                progressHandle = ProgressHandleFactory.createHandle(displayName, this, openAction);
            }
        }
        return progressHandle;
    }

    protected void startProgress () {
        getProgressHandle().start();
        getLogger().outputLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName); // NOI18N
        if (steps > 1) {
            progressHandle.switchToDeterminate(steps * 100);
        }
    }

    protected void finishProgress () {
        getProgressHandle().finish();
        if (isCanceled() == false) {
            getLogger().outputLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName + " " + org.openide.util.NbBundle.getMessage(GitProgressSupport.class, "MSG_Progress_Finished")); // NOI18N
        } else {
            getLogger().outputLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName + " " + org.openide.util.NbBundle.getMessage(GitProgressSupport.class, "MSG_Progress_Canceled")); // NOI18N
        }
    }

    protected VCSFileProxy getRepositoryRoot () {
        return repositoryRoot;
    }

    public void setProgress (String progressMessage) {
        if (progressHandle != null) {
            setProgressMessage(progressHandle, progressMessage == null ? displayName
                    : NbBundle.getMessage(GitProgressSupport.class, "LBL_Progress", new Object[] { displayName, progressMessage })); // NOI18N
        }
    }

    protected GitClient getClient () throws GitException {
        if (gitClient == null) {
            gitClient = Git.getInstance().getClient(repositoryRoot, this);
        }
        return gitClient;
    }

    void setRepositoryStateBlocked (VCSFileProxy repository, boolean blocked) {
        if (repository == null) {
            throw new IllegalArgumentException("Trying to block/unblock progress on null repository"); //NOI18N
        }
        if (blocked) {
            setProgress(NbBundle.getMessage(GitProgressSupport.class, "LBL_RepositoryBlocked", repository.getName()));
        } else {
            setProgress();
        }
    }

    public void outputInRed(String message) {
        LOG.log(Level.FINE, message); //NOI18N
        getLogger().outputInRed(message);
    }
    
    public void output(String message) {
        LOG.log(Level.FINE, message); //NOI18N
        getLogger().outputLine(message);
    }
    
    private void setProgressMessage (ProgressHandle progressHandle, String message) {
        LOG.log(Level.FINER, "New status of progress: {0}", message);
        progressHandle.progress(message);
    }

    public OutputLogger getLogger () {
        if (logger == null) {
            logger = OutputLogger.getLogger(repositoryRoot);
        }
        return logger;
    }

    public final ProgressMonitor getProgressMonitor () {
        return progressMonitor;
    }
    
    private final class ProgressMonitorImpl extends ProgressMonitor {
        private int currentTaskTotalUnits;
        private String currentTaskTitle;
        private int currentTaskWorked;
        private Cancellable cancellable;

        @Override
        public synchronized final boolean isCanceled () {
            return GitProgressSupport.this.isCanceled();
        }

        @Override
        public synchronized final void setCancelDelegate(Cancellable c) {
            cancellable = c;
        }

        @Override
        public synchronized final boolean cancel() {
            if (cancellable != null) {
                cancellable.cancel();
            }
            return GitProgressSupport.this.cancel();
        }
        
        @Override
        public void started (String command) {
            LOG.log(Level.FINE, "command started: {0}", command); //NOI18N
            getLogger().outputLine(command);
        }

        @Override
        public void finished() {
            LOG.log(Level.FINE, "command finished"); //NOI18N
        }

        @Override
        public void preparationsFailed (String message) {
            LOG.log(Level.FINE, "command could not start: {0}", message); //NOI18N
            getLogger().outputLine("command could not start: " + message);
        }

        @Override
        public void notifyError(String message) {
            LOG.log(Level.FINE, "error: {0}", message); //NOI18N
            getLogger().outputLine("error: " + message);
        }

        @Override
        public void notifyWarning(String message) {
            LOG.log(Level.FINE, "warning: {0}", message); //NOI18N
            getLogger().outputLine("warning: " + message);
        }

        @Override
        public void notifyMessage (String message) {
            getLogger().outputLine(message);
        }

        @Override
        public void beginTask (String title, int totalWork) {
            currentTaskTotalUnits = totalWork;
            currentTaskTitle = title;
            currentTaskWorked = 0;
            updateTaskProgress();
        }

        @Override
        public void updateTaskState (int completed) {
            currentTaskWorked += completed;
            updateTaskProgress();
        }

        @Override
        public void endTask () {
            currentTaskTotalUnits = 0;
            currentTaskTitle = null;
            currentTaskWorked = 0;
            updateTaskProgress();
        }

        @NbBundle.Messages({
            "# {0} - task title", "# {1} - task progress", "MSG_Progress.task={0} ({1}%)"
        })
        private void updateTaskProgress () {
            String message = currentTaskTitle;
            if (currentTaskTitle != null && currentTaskTotalUnits > 0 && currentTaskWorked <= currentTaskTotalUnits) {
                message = Bundle.MSG_Progress_task(currentTaskTitle, (currentTaskWorked * 100) / currentTaskTotalUnits);
            }
            setProgress(message);
        }
    }

    public class DefaultFileListener implements FileListener {
        String lastNotified;
        private final VCSFileProxy[] roots;

        public DefaultFileListener (VCSFileProxy[] roots) {
            this.roots = roots;
        }

        @Override
        public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
            getLogger().outputFile(relativePathToRoot, file, 0);

            String directChildPath = getDirectChildPath(file, relativePathToRoot);
            if (!directChildPath.isEmpty() && !directChildPath.equals(lastNotified)) {
                setProgress(repositoryRoot.getName() + "/" + directChildPath); //NOI18N
            }
        }

        private String getDirectChildPath (VCSFileProxy file, String relativePath) {
            if (roots == null || roots.length == 0) {
                // there's no file filter
                return relativePath;
            }
            VCSFileProxy directChild = null;
            String directChildPath = relativePath;
            while (directChild == null && !directChildPath.isEmpty()) {
                for (VCSFileProxy root : roots) {
                    directChild = getDirectChild(file, root);
                    if (directChild != null) {
                        break;
                    }
                }
                if (directChild == null) {
                    file = file.getParentFile();
                    int pos = directChildPath.lastIndexOf('/'); //NOI18N
                    if (pos == -1) {
                        if (LOG.isLoggable(Level.INFO)) {
                            LOG.log(Level.WARNING, "Suspicious notified file: {0} - {1} for {2}", new Object[] { file, relativePath, Arrays.asList(roots) } ); //NOI18N
                            LOG.log(Level.INFO, null, new Exception("Suspicious notify call") ); //NOI18N
                        }
                        directChildPath = ""; //NOI18N
                    } else {
                        directChildPath = directChildPath.substring(0, pos);
                    }
                }
            }
            return directChildPath;
        }

        private VCSFileProxy getDirectChild (VCSFileProxy file, VCSFileProxy root) {
            return (file.equals(root) || root.equals(file.getParentFile())) ? file : null;
        }
    }
    
    public abstract static class NoOutputLogging extends GitProgressSupport {
        OutputLogger logger;
                
        @Override
        public final OutputLogger getLogger () {
            if (logger == null) {
                logger = OutputLogger.getLogger(null);
            }
            return logger;
        }
    }
}
