/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.libs.bugtracking;

import java.io.File;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.commons.net.WebUtil;
import org.eclipse.mylyn.internal.tasks.core.RepositoryExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityManager;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataStore;
import org.eclipse.mylyn.internal.tasks.core.externalization.ExternalizationManager;
import org.eclipse.mylyn.internal.tasks.core.externalization.IExternalizationParticipant;
import org.eclipse.mylyn.internal.tasks.core.sync.SynchronizationSession;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * Mylyn specific runtime
 *
 * @author Tomas Stupka
 */
public class BugtrackingRuntime {

    private static BugtrackingRuntime instance;

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.libs.bugtracking.BugtrackingRuntime"); // NOI18N

    private final String            DATA_DIRECTORY = "bugtracking";             // NOI18N
    private File                    cacheStore;
    private ExternalizationManager  externalizationManager;
    private TaskRepositoryManager   taskRepositoryManager;
    private TaskDataStore           taskDataStore;
    private TaskDataManager         taskDataManager;
    private SynchronizationSession  synchronizationSession;

    public synchronized static BugtrackingRuntime getInstance() {
        if(instance == null) {
            instance = new BugtrackingRuntime();
            instance.init();
        }
        return instance;
    }

    private void init() {
        initCacheStore();
        if(SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    initWebUtil();
                }
            });
        } else {
            initWebUtil();
        }
        // XXX this is dummy
        taskRepositoryManager = new TaskRepositoryManager();

        taskDataStore = new TaskDataStore(taskRepositoryManager);
        TaskList tl = new TaskList();
        TaskActivityManager tam = new TaskActivityManager(taskRepositoryManager, tl);
        taskDataManager = new TaskDataManager(taskDataStore, taskRepositoryManager, tl, tam);
        taskDataManager.setDataPath(BugtrackingRuntime.getInstance().getCacheStore().getAbsolutePath());
        synchronizationSession = new SynchronizationSession(taskDataManager);

        externalizationManager = new ExternalizationManager(cacheStore.getAbsolutePath());

        IExternalizationParticipant repositoryParticipant = new RepositoryExternalizationParticipant(externalizationManager, taskRepositoryManager);
        externalizationManager.addParticipant(repositoryParticipant);
    }

    private void initWebUtil() {
        WebUtil.init();
    }

    public void addRepositoryConnector(AbstractRepositoryConnector rc) {
        taskRepositoryManager.addRepositoryConnector(rc);
    }

    public SynchronizationSession getSynchronizationSession() {
        return synchronizationSession;
    }

    public TaskRepositoryManager getTaskRepositoryManager() {
        return taskRepositoryManager;
    }

    public TaskDataManager getTaskDataManager() {
        return taskDataManager;
    }

    public File getCacheStore() {
        return cacheStore;
    }

    private void initCacheStore() {
        String userDir = System.getProperty("netbeans.user");                   // NOI18N
        if (userDir != null) {
            cacheStore = new File(new File(new File (userDir, "var"), "cache"), DATA_DIRECTORY); // NOI18N
        } else {
            File cachedir = FileUtil.toFile(org.openide.filesystems.Repository.getDefault().getDefaultFileSystem().getRoot());
            cacheStore = new File(cachedir, DATA_DIRECTORY);
        }
        cacheStore.mkdirs();
    }
}
