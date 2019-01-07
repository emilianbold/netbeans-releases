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
package org.netbeans.modules.mercurial.remote;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.remote.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
public class WorkingCopyInfo {

    /**
     * Fired when a working copy parents change
     */
    public static final String PROPERTY_WORKING_COPY_PARENT = WorkingCopyInfo.class.getName() + ".workingCopyParents"; //NOI18N

    /**
     * Fired when the current branch name changes
     */
    public static final String PROPERTY_CURRENT_BRANCH = WorkingCopyInfo.class.getName() + ".currBranch"; //NOI18N

    private static final WeakHashMap<VCSFileProxy, WorkingCopyInfo> cache = new WeakHashMap<>(5);
    private static final Logger LOG = Logger.getLogger(WorkingCopyInfo.class.getName());
    private static final RequestProcessor rp = new RequestProcessor("WorkingCopyInfo", 1, true); //NOI18N
    private static final RequestProcessor.Task refreshTask = rp.create(new RepositoryRefreshTask());
    private static final Set<WorkingCopyInfo> repositoriesToRefresh = new HashSet<>(2);
    private final WeakReference<VCSFileProxy> rootRef;
    private final PropertyChangeSupport propertyChangeSupport;
    private HgLogMessage[] parents = new HgLogMessage[0];
    private String branch = HgBranch.DEFAULT_NAME;

    private WorkingCopyInfo (VCSFileProxy root) {
        rootRef = new WeakReference<>(root);
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Do NOT call from EDT
     * @param repositoryRoot existing repository root
     * @return null if repositoryRoot is not an existing git repository
     */
    public static WorkingCopyInfo getInstance (VCSFileProxy repositoryRoot) {
        WorkingCopyInfo info = null;
        // this should return alwaus the same instance, so the cache can be implemented as a weak map.
        VCSFileProxy repositoryRootSingleInstance = Mercurial.getInstance().getRepositoryRoot(repositoryRoot);
        if (repositoryRoot.equals(repositoryRootSingleInstance)) {
            boolean refresh = false;
            synchronized (cache) {
                info = cache.get(repositoryRootSingleInstance);
                if (info == null) {
                    cache.put(repositoryRootSingleInstance, info = new WorkingCopyInfo(repositoryRootSingleInstance));
                    refresh = true;
                }
            }
            if (refresh) {
                info.refresh();
            }
        }
        return info;
    }

    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Do NOT call from EDT
     * @return
     */
    public void refresh () {
        assert !java.awt.EventQueue.isDispatchThread();
        VCSFileProxy root = rootRef.get();
        try {
            if (root == null) {
                LOG.log(Level.WARNING, "refresh (): root is null, it has been collected in the meantime"); //NOI18N
            } else {
                LOG.log(Level.FINE, "refresh (): starting for {0}", root); //NOI18N
                List<HgLogMessage> parentInfo = HgCommand.getParents(root, null, null);
                setParents(parentInfo);
                String branch = HgCommand.getBranch(root);
                setBranch(branch);
            }
        } catch (HgException.HgCommandCanceledException ex) {
            // nothing
        } catch (HgException ex) {
            Level level = root.exists() ? Level.INFO : Level.FINE; // do not polute the message log with messages concerning temporary or deleted repositories
            LOG.log(level, null, ex);
        }
    }

    public static void refreshAsync (VCSFileProxy repositoryRoot) {
        WorkingCopyInfo info = null;
        synchronized (cache) {
            info = cache.get(repositoryRoot);
        }
        if (info != null) {
            boolean start = false;
            synchronized (repositoriesToRefresh) {
                start = repositoriesToRefresh.add(info);
            }
            if (start) {
                LOG.log(Level.FINE, "Planning refresh for {0}", repositoryRoot); //NOI18N
                refreshTask.schedule(3000);
            }
        }
    }

    public HgLogMessage[] getWorkingCopyParents () {
        return parents;
    }

    public String getCurrentBranch () {
        return branch;
    }

    private void setParents (List<HgLogMessage> parents) {
        HgLogMessage[] oldParents = this.parents;
        boolean changed = oldParents.length != parents.size();
        if (!changed) {
            for (HgLogMessage newParent : parents) {
                boolean contains = false;
                for (HgLogMessage oldParent : oldParents) {
                    if (oldParent.getCSetShortID().equals(newParent.getCSetShortID())
                            && oldParent.getTags().length == newParent.getTags().length
                            && new HashSet<>(Arrays.asList(oldParent.getTags())).equals(new HashSet<>(Arrays.asList(newParent.getTags())))) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            HgLogMessage[] newParents = parents.toArray(new HgLogMessage[parents.size()]);
            this.parents = newParents;
            propertyChangeSupport.firePropertyChange(PROPERTY_WORKING_COPY_PARENT, oldParents, newParents);
        }
    }
    
    private void setBranch (String branch) {
        if (branch == null) {
            branch = HgBranch.DEFAULT_NAME;
        }
        String oldBranch = this.branch;
        this.branch = branch;
        if (!oldBranch.equals(this.branch)) {
            propertyChangeSupport.firePropertyChange(PROPERTY_CURRENT_BRANCH, oldBranch, branch);
        }
    }

    private static class RepositoryRefreshTask implements Runnable {
        @Override
        public void run() {
            WorkingCopyInfo info;
            while ((info = getNextRepositoryInfo()) != null) {
                info.refresh();
            }
        }

        private WorkingCopyInfo getNextRepositoryInfo () {
            WorkingCopyInfo info = null;
            synchronized (repositoriesToRefresh) {
                Iterator<WorkingCopyInfo> it = repositoriesToRefresh.iterator();
                if (it.hasNext()) {
                    info = it.next();
                    it.remove();
                }
            }
            return info;
        }
    }
}
