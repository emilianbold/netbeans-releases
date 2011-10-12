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

package org.netbeans.modules.git;

import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.client.GitClientInvocationHandler;
import org.netbeans.modules.git.client.GitCanceledException;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.progress.ProgressMonitor.DefaultProgressMonitor;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
public class GitClientInvocationHandlerTest extends AbstractGitTestCase {
    private Logger indexingLogger;
    private Logger invocationHandlerLogger;

    public GitClientInvocationHandlerTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IndexingBridge bridge = IndexingBridge.getInstance();
        Field f = IndexingBridge.class.getDeclaredField("LOG");
        f.setAccessible(true);
        indexingLogger = (Logger) f.get(bridge);
        f = GitClientInvocationHandler.class.getDeclaredField("LOG");
        f.setAccessible(true);
        invocationHandlerLogger = (Logger) f.get(GitClientInvocationHandler.class);
    }

    /**
     * tests that we don't miss any command that needs to run in indexing bridge.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's a command running in the IB. If it is and we miss the command,
     * the IDE might start scanning during the command execution.
     * @throws Exception
     */
    public void testMethodsRunningInIndexingBridge () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "clean",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getTags",
                "getUser",
                "ignore",
                "init",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "unignore"));
        Set<String> indexingBridgeMethods = new HashSet<String>(Arrays.asList(
                "checkout",
                "checkoutRevision",
                "merge",
                "pull",
                "remove",
                "reset",
                "revert",
                "clean"));
        Field f = GitClientInvocationHandler.class.getDeclaredField("INDEXING_BRIDGE_COMMANDS");
        f.setAccessible(true);
        Set<String> actualIBCommands = (Set<String>) f.get(GitClientInvocationHandler.class);

        Method[] methods = GitClient.class.getDeclaredMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (indexingBridgeMethods.contains(methodName)) {
                assertTrue(methodName, actualIBCommands.contains(methodName));
                indexingBridgeMethods.remove(methodName);
            }
        }
        assertTrue(indexingBridgeMethods.isEmpty());
    }

    /**
     * tests that we don't miss any read-only commands.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's read-only.
     * If it is and we miss the command, the support will refresh index timestamp when it's not supposed to.
     * @throws Exception
     */
    public void testIndexReadOnlyMethods () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "clean",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getTags",
                "getUser",
                "ignore",
                "init",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "unignore"));
        Set<String> readOnlyMethods = new HashSet<String>(Arrays.asList(
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getTags",
                "getUser",
                "ignore",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "removeNotificationListener",
                "removeRemote",
                "setCallback",
                "setRemote",
                "push",
                "unignore"));
        Field f = GitClientInvocationHandler.class.getDeclaredField("WORKING_TREE_READ_ONLY_COMMANDS");
        f.setAccessible(true);
        Set<String> actualReadOnlyMethods = (Set<String>) f.get(GitClientInvocationHandler.class);

        Method[] methods = GitClient.class.getDeclaredMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (readOnlyMethods.contains(methodName)) {
                assertTrue(methodName, actualReadOnlyMethods.contains(methodName));
                readOnlyMethods.remove(methodName);
            }
        }
        assertTrue(readOnlyMethods.isEmpty());
    }

    /**
     * tests that we don't miss any command that results in a need to refresh repository info.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's a command after which we should refresh the repository info (current branch, head and stuff).
     * @throws Exception
     */
    public void testMethodsNeedingRepositoryInfoRefresh () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "clean",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getTags",
                "getUser",
                "init",
                "ignore",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "unignore"));
        Set<String> expectedMethods = new HashSet<String>(Arrays.asList(
                "checkout",
                "checkoutRevision",
                "commit",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "fetch",
                "merge",
                "pull",
                "reset",
                "removeRemote",
                "revert",
                "setRemote"));
        Field f = GitClientInvocationHandler.class.getDeclaredField("NEED_REPOSITORY_REFRESH_COMMANDS");
        f.setAccessible(true);
        Set<String> actualMethods = (Set<String>) f.get(GitClientInvocationHandler.class);

        Method[] methods = GitClient.class.getDeclaredMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (expectedMethods.contains(methodName)) {
                assertTrue(methodName, actualMethods.contains(methodName));
                expectedMethods.remove(methodName);
            }
        }
        assertTrue(expectedMethods.isEmpty());
    }

    /**
     * tests that we don't miss any network commands.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's a network one. If it is and we miss the command, 
     * the NbAuthenticator might pop up an undesired auth dialog (#200692).
     * @throws Exception
     */
    public void testNetworkMethods () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "clean",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getTags",
                "getUser",
                "init",
                "ignore",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "unignore"));
        Set<String> networkMethods = new HashSet<String>(Arrays.asList(
                "fetch",
                "listRemoteBranches",
                "listRemoteTags",
                "pull",
                "push"));
        Field f = GitClientInvocationHandler.class.getDeclaredField("NETWORK_COMMANDS");
        f.setAccessible(true);
        Set<String> actualNetworkCommands = (Set<String>) f.get(GitClientInvocationHandler.class);

        Method[] methods = GitClient.class.getDeclaredMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (networkMethods.contains(methodName)) {
                assertTrue(methodName, actualNetworkCommands.contains(methodName));
                networkMethods.remove(methodName);
            }
        }
        assertTrue(networkMethods.isEmpty());
    }

    public void testIndexingBridge () throws Exception {
        indexingLogger.setLevel(Level.ALL);
        LogHandler h = new LogHandler();
        indexingLogger.addHandler(h);

        GitClient client = Git.getInstance().getClient(repositoryLocation);

        File folder = new File(repositoryLocation, "folder");
        File file = new File(folder, "file");
        folder.mkdirs();
        file.createNewFile();
        h.reset();
        client.add(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        client.commit(new File[] { file }, "aaa", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        client.copyAfter(file, new File(folder, "file2"), ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        client.getStatus(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        File anotherRepo = new File(repositoryLocation.getParentFile(), "wc2");
        anotherRepo.mkdirs();
        GitClient client2 = Git.getInstance().getClient(anotherRepo);
        client2.init(ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);

        h.reset();
        h.setExpectedParents(new File[] { file.getParentFile() });
        client.remove(new File[] { file }, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(h.bridgeAccessed);
        assertTrue(h.expectedParents.isEmpty());

        h.reset();
        write(file, "aaa");
        client.rename(file, new File(folder, "file2"), false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertFalse(h.bridgeAccessed);
    }

    /**
     * tests that we don't miss any parallelizable commands.
     * If a method is added to GitClient, we NEED to evaluate and decide if it's parallelizable. If it is and we miss the command, the IDE might get blocked.
     * @throws Exception
     */
    public void testExclusiveMethods () throws Exception {
        Set<String> allTestedMethods = new HashSet<String>(Arrays.asList(
                "add",
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "checkout",
                "checkoutRevision",
                "clean",
                "commit",
                "copyAfter",
                "createBranch",
                "createTag",
                "deleteBranch",
                "deleteTag",
                "exportCommit",
                "exportDiff",
                "fetch",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getTags",
                "getUser",
                "init",
                "ignore",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "merge",
                "pull",
                "push",
                "remove",
                "removeNotificationListener",
                "removeRemote",
                "rename",
                "reset",
                "revert",
                "setCallback",
                "setRemote",
                "unignore"));
        Set<String> parallelizableMethods = new HashSet<String>(Arrays.asList(
                "addNotificationListener",
                "blame",
                "catFile",
                "catIndexEntry",
                "exportCommit",
                "exportDiff",
                "getBranches",
                "getCommonAncestor",
                "getConflicts",
                "getPreviousRevision",
                "getRemote",
                "getRemotes",
                "getRepositoryState",
                "getStatus",
                "getTags",
                "getUser",
                "listModifiedIndexEntries",
                "listRemoteBranches",
                "listRemoteTags",
                "log",
                "removeNotificationListener",
                "removeRemote",
                "setCallback",
                "setRemote"));
        Field f = GitClientInvocationHandler.class.getDeclaredField("PARALLELIZABLE_COMMANDS");
        f.setAccessible(true);
        Set<String> actualParallelizableCommands = (Set<String>) f.get(GitClientInvocationHandler.class);

        Method[] methods = GitClient.class.getDeclaredMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method m : methods) {
            String methodName = m.getName();
            assertTrue(methodName, allTestedMethods.contains(methodName));
            if (parallelizableMethods.contains(methodName)) {
                assertTrue(methodName, actualParallelizableCommands.contains(methodName));
                parallelizableMethods.remove(methodName);
            }
        }
        assertTrue(parallelizableMethods.isEmpty());
    }

    public void testExclusiveAccess () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitListener m = new InhibitListener();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testDoNotBlockIndexing () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitListener m = new InhibitListener();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        invocationHandlerLogger.setLevel(Level.ALL);
        LogHandler handler = new LogHandler();
        invocationHandlerLogger.addHandler(handler);
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.remove(new File[] { file }, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        assertFalse(handler.indexingBridgeCalled);
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testPallalelizableCommands () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitListener m = new InhibitListener();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.getStatus(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        t2.join(5000);
        assertEquals(Thread.State.TERMINATED, t2.getState());
        m.cont = true;
        t1.join();
        assertNull(exs[0]);
        assertNull(exs[1]);
    }

    public void testCancelCommand () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final InhibitListener m = new InhibitListener();
        final Exception[] exs = new Exception[2];
        final DefaultProgressMonitor pm = new DefaultProgressMonitor();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file, file2 }, pm);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        pm.cancel();
        m.cont = true;
        t1.join();
        assertTrue(pm.isCanceled());
        assertEquals(1, m.count);
    }

    public void testCancelWaitingOnBlockedRepository () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final Exception[] exs = new Exception[2];
        final InhibitListener m = new InhibitListener();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.add(new File[] { file }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    exs[1] = ex;
                }
            }
        });
        t1.start();
        m.waitAtBarrier();
        t2.start();
        Thread.sleep(5000);
        assertEquals(Thread.State.BLOCKED, t2.getState());
        t2.interrupt();
        m.cont = true;
        t1.join();
        t2.join();
        assertNull(exs[0]);
        assertTrue(exs[1] instanceof GitCanceledException);
    }

    public void testCancelSupport () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final InhibitListener m = new InhibitListener();
        final Exception[] exs = new Exception[2];
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            public void perform () {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file, file2 }, this);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        };
        Task t = supp.start(Git.getInstance().getRequestProcessor(repositoryLocation), repositoryLocation, "Git Add");
        m.waitAtBarrier();
        supp.cancel();
        m.cont = true;
        t.waitFinished();
        assertTrue(supp.isCanceled());
        assertEquals(1, m.count);
    }

    public void testSupportDisplayNames () throws Exception {
        final File file = new File(repositoryLocation, "aaa");
        file.createNewFile();
        final File file2 = new File(repositoryLocation, "aaa2");
        file2.createNewFile();
        final File file3 = new File(repositoryLocation, "aaa3");
        file3.createNewFile();

        final GitClient client = Git.getInstance().getClient(repositoryLocation);
        final FileListener[] ms = new FileListener[1];
        final Exception[] exs = new Exception[2];
        ProgressLogHandler h = new ProgressLogHandler();
        Logger log = Logger.getLogger(GitProgressSupport.class.getName());
        log.addHandler(h);
        log.setLevel(Level.ALL);
        RequestProcessor rp = Git.getInstance().getRequestProcessor(repositoryLocation);

        final boolean[] flags = new boolean[6];

        Task preceedingTask = rp.post(new Runnable() {
            @Override
            public void run () {
                // barrier
                flags[0] = true;
                // wait for asserts
                while (!flags[1]) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {}
                }
            }
        });

        final InhibitListener m = new InhibitListener();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.addNotificationListener(m);
                    client.add(new File[] { file3 }, ProgressMonitor.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        m.cont = false;
        thread.start();

        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            public void perform () {
                FileListener list;
                ms[0] = list = new FileListener () {
                    @Override
                    public void notifyFile(File file, String relativePathToRoot) {
                        // barrier
                        flags[4] = true;
                        // wait for asserts
                        while (!flags[5]) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {}
                        }
                        setProgress(file.getName());
                    }
                };
                try {
                    // barrier
                    flags[2] = true;
                    // wait for asserts
                    while (!flags[3]) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {}
                    }
                    GitClient client = getClient();
                    client.addNotificationListener(list);
                    client.add(new File[] { file, file2 }, this);
                } catch (GitException ex) {
                    exs[0] = ex;
                }
            }
        };
        List<String> expectedMessages = new LinkedList<String>();
        expectedMessages.add("Git Add - Queued");
        Task t = supp.start(rp, repositoryLocation, "Git Add");
        assertEquals(expectedMessages, h.progressMessages);
        flags[1] = true;
        preceedingTask.waitFinished();
        expectedMessages.add("Git Add");
        for (int i = 0; i < 100; ++i) {
            if (flags[2]) break;
            Thread.sleep(100);
        }
        assertTrue(flags[2]);
        assertEquals(expectedMessages, h.progressMessages);
        flags[3] = true;

        expectedMessages.add("Git Add - Queued on " + repositoryLocation.getName());
        for (int i = 0; i < 100; ++i) {
            if (expectedMessages.equals(h.progressMessages)) break;
            Thread.sleep(100);
        }
        assertEquals(expectedMessages, h.progressMessages);
        m.cont = true;
        thread.join();

        for (int i = 0; i < 100; ++i) {
            if (flags[4]) break;
            Thread.sleep(100);
        }
        assertTrue(flags[4]);
        expectedMessages.add("Git Add");
        assertEquals(expectedMessages, h.progressMessages);
        flags[5] = true;

        t.waitFinished();
        expectedMessages.add("Git Add - " + file.getName());
        expectedMessages.add("Git Add - " + file2.getName());
        assertEquals(expectedMessages, h.progressMessages);
    }

    private static class InhibitListener implements FileListener {
        private boolean cont;
        private boolean barrierAccessed;
        private int count;
        @Override
        public void notifyFile (File file, String relativePathToRoot) {
            barrierAccessed = true;
            ++count;
            while (!cont) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }

        private void waitAtBarrier() throws InterruptedException {
            for (int i = 0; i < 100; ++i) {
                if (barrierAccessed) {
                    break;
                }
                Thread.sleep(100);
            }
            assertTrue(barrierAccessed);
        }
    }

    private class LogHandler extends Handler {

        boolean bridgeAccessed;
        private HashSet<File> expectedParents;
        private boolean indexingBridgeCalled;

        @Override
        public void publish (LogRecord record) {
            if (record.getLoggerName().equals(indexingLogger.getName())) {
                bridgeAccessed = true;
                for (File f : expectedParents) {
                    if (record.getMessage().equals("scheduling for fs refresh: [" + f + "]")) {
                        expectedParents.remove(f);
                        break;
                    }
                }
            } else if (record.getLoggerName().equals(invocationHandlerLogger.getName())) {
                if (record.getMessage().contains("Running command in indexing bridge")) {
                    indexingBridgeCalled = true;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void reset() {
            bridgeAccessed = false;
            indexingBridgeCalled = false;
        }

        private void setExpectedParents(File[] files) {
            this.expectedParents = new HashSet<File>(Arrays.asList(files));
        }
    }

    private static class ProgressLogHandler extends Handler {

        List<String> progressMessages = new LinkedList<String>();

        @Override
        public void publish (LogRecord record) {
            if (record.getMessage().equals("New status of progress: {0}")) {
                progressMessages.add((String) record.getParameters()[0]);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
