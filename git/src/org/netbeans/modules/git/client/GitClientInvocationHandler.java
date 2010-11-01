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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.versioning.util.IndexingBridge;

/**
 *
 * @author ondra
 */
public class GitClientInvocationHandler implements InvocationHandler {
    private final GitClient client;
    private final File repositoryRoot;
    private static final HashSet<String> PARALLELIZABLE_COMMANDS = new HashSet<String>(Arrays.asList("addNotificationListener", "catFile", "getBranches", "getStatus", "removeNotificationListener")); //NOI18N
    private static final HashSet<String> INDEXING_BRIDGE_COMMANDS = new HashSet<String>(Arrays.asList("checkout", "remove", "reset")); //NOI18N
    private static final HashSet<String> WORKING_TREE_READ_ONLY_COMMANDS = new HashSet<String>(Arrays.asList("addNotificationListener", "catFile", "getBranches", "getStatus", "removeNotificationListener")); //NOI18N
    private static final Logger LOG = Logger.getLogger(GitClientInvocationHandler.class.getName());
    private GitProgressSupport progressSupport;

    public GitClientInvocationHandler (GitClient client, File repositoryRoot) {
        this.client = client;
        this.repositoryRoot = repositoryRoot;
    }

    @Override
    public Object invoke (final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            if (isExclusiveRepositoryAccess(method)) {
                LOG.log(Level.FINER, "Running an exclusive command: {0} on {1}", new Object[] { method.getName(), repositoryRoot.getAbsolutePath() }); //NOI18N
                GitProgressSupport supp = progressSupport;
                if (supp != null) {
                    supp.setRepositoryStateBlocked(repositoryRoot, true);
                }
                synchronized (repositoryRoot) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    if (supp != null) {
                        LOG.log(Level.FINEST, "Repository unblocked: {0}", repositoryRoot); //NOI18N
                        supp.setRepositoryStateBlocked(repositoryRoot, false);
                    }
                    return invokeIntern(proxy, method, args);
                }
            } else {
                LOG.log(Level.FINER, "Running a parallelizable command: {0} on {1}", new Object[] { method.getName(), repositoryRoot.getAbsolutePath() }); //NOI18N
                return invokeIntern(proxy, method, args);
            }
        } catch (InterruptedException ex) {
            throw new GitCanceledException(ex);
        }
    }

    private Object invokeIntern (final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    boolean refreshIndexTimestamp = modifiesWorkingTree(method);
                    long t = 0;
                    if (LOG.isLoggable(Level.FINE)) {
                        t = System.currentTimeMillis();
                        LOG.log(Level.FINE, "Starting a git command: [{0}] on repository [{1}]", new Object[] { method.getName(), repositoryRoot.getAbsolutePath() }); //NOI18N
                    }
                    try {
                        return invokeClientMethod(method, args);
                    } finally {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Git command finished: [{0}] on repository [{1}], lasted {2} ms", new Object[]{method.getName(), repositoryRoot.getAbsolutePath(), System.currentTimeMillis() - t}); //NOI18N
                        }
                        if (refreshIndexTimestamp) {
                            LOG.log(Level.FINER, "Refreshing index timestamp after: {0} on {1}", new Object[] { method.getName(), repositoryRoot.getAbsolutePath() }); //NOI18N
                            Git.getInstance().refreshWorkingCopyTimestamp(repositoryRoot);
                        }
                    }
                }
            };
            if (runsWithBlockedIndexing(method)) {
                File[] fileArgs = getFileArguments(args);
                LOG.log(Level.FINER, "Running command in indexing bridge: {0} on {1}", new Object[] { method.getName(), repositoryRoot.getAbsolutePath() }); //NOI18N
                return IndexingBridge.getInstance().runWithoutIndexing(callable, fileArgs.length > 0 ? fileArgs : new File[] { repositoryRoot });
            } else {
                return callable.call();
            }
        } catch (InvocationTargetException ex) {
            if (ex.getCause() != null) {
                throw ex.getCause();
            } else {
                throw ex;
            }
        }
    }

    private boolean isExclusiveRepositoryAccess (Method method) {
        return !PARALLELIZABLE_COMMANDS.contains(method.getName());
    }

    private Object invokeClientMethod (Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(client, args);
    }

    private File[] getFileArguments (Object[] args) {
        List<File> files = new LinkedList<File>();
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof File) {
                    files.add((File) arg);
                } else if (arg instanceof File[]) {
                    File[] fs = (File[]) arg;
                    files.addAll(Arrays.asList(fs));
                }
            }
        }
        return files.toArray(new File[files.size()]);
    }

    private boolean runsWithBlockedIndexing (Method method) {
        return INDEXING_BRIDGE_COMMANDS.contains(method.getName());
    }

    private boolean modifiesWorkingTree (Method method) {
        return !WORKING_TREE_READ_ONLY_COMMANDS.contains(method.getName());
    }

    public void setProgressSupport (GitProgressSupport progressSupport) {
        this.progressSupport = progressSupport;
    }
}
