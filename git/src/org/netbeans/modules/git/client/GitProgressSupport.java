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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
public abstract class GitProgressSupport implements Runnable, Cancellable {
    private volatile boolean canceled;

    private static final Logger LOG = Logger.getLogger(GitProgressSupport.class.getName());

    private ProgressHandle progressHandle = null;
    private String displayName = ""; // NOI18N
    private File repositoryRoot;
    private RequestProcessor.Task task;
    private ProgressMonitor progressMonitor;
    private GitClient gitClient;

    public RequestProcessor.Task start (RequestProcessor rp, File repositoryRoot, String displayName) {
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
//            log("Start - " + displayName); // NOI18N
            if(!canceled) {
                perform();
            }
        } finally {
//            log("End - " + displayName); // NOI18N
            finnishProgress();
//            getLogger().closeLog();
        }
    }

    protected abstract void perform ();

    public synchronized boolean isCanceled () {
        return canceled;
    }

    @Override
    public synchronized boolean cancel () {
        if (progressMonitor != null) {
            if (!progressMonitor.cancel()) {
                return false;
            }
        }
        if (canceled) {
            return false;
        }
        if (task != null) {
            task.cancel();
        }
        return canceled = true;
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
        if(progressHandle==null) {
            progressHandle = ProgressHandleFactory.createHandle(displayName, this);
        }
        return progressHandle;
    }

    protected void startProgress () {
        getProgressHandle().start();
    }

    protected void finnishProgress () {
        getProgressHandle().finish();
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

    protected void setProgressMonitor (ProgressMonitor monitor) {
        this.progressMonitor = monitor;
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

    public class DefaultProgressMonitor extends ProgressMonitor.DefaultProgressMonitor {
        
    }

    private void setProgressMessage (ProgressHandle progressHandle, String message) {
        LOG.log(Level.FINER, "New status of progress: {0}", message);
        progressHandle.progress(message);
    }
}
