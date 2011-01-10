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

package org.netbeans.modules.git.client;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
public abstract class GitProgressSupport implements Runnable, Cancellable, ProgressMonitor {
    private volatile boolean canceled;

    private static final Logger LOG = Logger.getLogger(GitProgressSupport.class.getName());

    private ProgressHandle progressHandle = null;
    private String displayName = ""; // NOI18N
    private String originalDisplayName;
    private File repositoryRoot;
    private RequestProcessor.Task task;
    private GitClient gitClient;
    private OutputLogger logger;

    public RequestProcessor.Task start (RequestProcessor rp, File repositoryRoot, String displayName) {
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
        }
    }

    protected abstract void perform ();

    @Override
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
    
    protected void setDisplayName (String displayName) {
        this.displayName = displayName;
        setProgress();
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
        getLogger().output("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName); // NOI18N
    }

    protected void finishProgress () {
        getProgressHandle().finish();
        if (isCanceled() == false) {
            getLogger().output("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName + " " + org.openide.util.NbBundle.getMessage(GitProgressSupport.class, "MSG_Progress_Finished")); // NOI18N
        } else {
            getLogger().output("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + originalDisplayName + " " + org.openide.util.NbBundle.getMessage(GitProgressSupport.class, "MSG_Progress_Canceled")); // NOI18N
        }
    }

    protected File getRepositoryRoot () {
        return repositoryRoot;
    }

    protected void setProgress (String progressMessage) {
        if (progressHandle != null) {
            setProgressMessage(progressHandle, NbBundle.getMessage(GitProgressSupport.class, "LBL_Progress", new Object[] { displayName, progressMessage })); // NOI18N
        }
    }

    protected GitClient getClient () throws GitException {
        if (gitClient == null) {
            gitClient = Git.getInstance().getClient(repositoryRoot, this);
        }
        return gitClient;
    }

    void setRepositoryStateBlocked (File repository, boolean blocked) {
        if (repository == null) {
            throw new IllegalArgumentException("Trying to block/unblock progress on null repository"); //NOI18N
        }
        if (blocked) {
            setProgress(NbBundle.getMessage(GitProgressSupport.class, "LBL_RepositoryBlocked", repository.getName()));
        } else {
            setProgress();
        }
    }

    @Override
    public void started (String command) {
        LOG.log(Level.FINE, "command started: {0}", command); //NOI18N
        getLogger().output(command);
    }

    @Override
    public void finished() {
        LOG.log(Level.FINE, "command finished"); //NOI18N
    }

    @Override
    public void preparationsFailed (String message) {
        LOG.log(Level.FINE, "command could not start: {0}", message); //NOI18N
        getLogger().output("command could not start: " + message);
    }

    @Override
    public void notifyError(String message) {
        LOG.log(Level.FINE, "error: {0}", message); //NOI18N
        getLogger().output("error: " + message);
    }

    @Override
    public void notifyWarning(String message) {
        LOG.log(Level.FINE, "warning: {0}", message); //NOI18N
        getLogger().output("warning: " + message);
    }

    public void outputInRed(String message) {
        LOG.log(Level.FINE, message); //NOI18N
        getLogger().outputInRed(message);
    }
    
    public void output(String message) {
        LOG.log(Level.FINE, message); //NOI18N
        getLogger().output(message);
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

    public class DefaultFileListener implements FileListener {
        String lastNotified;
        private final File[] roots;

        public DefaultFileListener (File[] roots) {
            this.roots = roots;
        }

        @Override
        public void notifyFile (File file, String relativePathToRoot) {
            getLogger().outputFile(relativePathToRoot, file, 0);

            String directChildPath = getDirectChildPath(file, relativePathToRoot, roots);
            if (!directChildPath.isEmpty() && !directChildPath.equals(lastNotified)) {
                setProgress(repositoryRoot.getName() + "/" + directChildPath); //NOI18N
            }
        }

        private String getDirectChildPath (File file, String relativePath, File[] roots) {
            File directChild = null;
            String directChildPath = relativePath;
            while (directChild == null && !directChildPath.isEmpty()) {
                for (File root : roots) {
                    directChild = getDirectChild(file, root);
                    if (directChild != null) {
                        break;
                    }
                }
                if (directChild == null) {
                    file = file.getParentFile();
                    directChildPath = directChildPath.substring(0, directChildPath.lastIndexOf("/")); //NOI18N
                }
            }
            return directChildPath;
        }

        private File getDirectChild (File file, File root) {
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
