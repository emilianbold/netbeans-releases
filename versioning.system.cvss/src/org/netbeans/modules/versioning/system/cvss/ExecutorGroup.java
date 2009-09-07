/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.system.cvss;

import org.openide.util.Cancellable;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import javax.swing.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.system.cvss.util.Utils;

/**
 * Support for actions that run multiple commands.
 * Represents context that carry data shared by
 * action commands executors. It can manage execution
 * in multiple ClientRuntimes (threads).
 *
 * <p>Implements shared progress, logging support
 * and cancelling.
 *
 * TODO add consolidated error reporting, nowadays, multiple errors from backgroud thread can popup
 *
 * @author Petr Kuzel
 */
public final class ExecutorGroup extends AbstractAction implements Cancellable {

    private final String name;
    private final boolean abortOnExecutorFailure;    
    public boolean executed;
    private boolean cancelled;
    private List<Cancellable> cancellables  = new ArrayList<Cancellable>(2);
    private List executors = new ArrayList(2);
    private List<ExecutorSupport> cleanups  = new ArrayList<ExecutorSupport>(2);
    /** ClientRuntime => CommandRunnale*/
    private Map queues = new HashMap();
    /** ClientRuntimes*/
    private Set started = new HashSet();
    /**
     * Porgress handle is never created if the group is non-interactive.
     */
    private ProgressHandle progressHandle;
    private long dataCounter;
    private boolean hasBarrier;
    private boolean failed;
    private boolean executingCleanup;
    private boolean nonInteractive;

    /**
     * Creates new group.
     *
     * @param displayName
     * Defines prefered display name - localized string that should highlight
     * group purpose (i.e. in English use verb in gerund).
     * E.g. <code>UpdateCommand</code> used to refresh statuses should
     * be named "Refreshing Status" rather than "cvs -N update",
     * "Updating" or "Status Refresh".
     */
    public ExecutorGroup(String displayName) {
        this(displayName, true);
    }

    public ExecutorGroup(String displayName, boolean abortOnExecutorFailure) {
        name = displayName;
        this.abortOnExecutorFailure = abortOnExecutorFailure;
    }
    
    /**
     * Defines group display name.
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Starts associated progress if not yet started. Allows to share
     * progress with execution preparation phase (cache ops).
     *
     * @param details progress detail messag eor null
     */
    public synchronized void progress(String details) {
        if (nonInteractive) return;
        if (progressHandle == null) {
            progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ExecutorGroup.class, "BK2001", name), this, this);
            progressHandle.start();
        }

        if (details != null) {
            progressHandle.progress(details);
        }
    }


    /**
     * Called by ExecutorSupport on enqueue.
     * Pairs with finished.
     *
     * @param queue processign queue or null for all
     * @param id identifier paired with {@link #finished}
     */
    synchronized void enqueued(ClientRuntime queue, Object id) {
        progress(null);
        if (progressHandle != null && started.size() == 0) {
            progressHandle.setDisplayName(NbBundle.getMessage(ExecutorGroup.class, "BK2005", name));
            progressHandle.progress(NbBundle.getMessage(ExecutorGroup.class, "BK1007"));
            progressHandle.switchToDeterminate(100);
            progressHandle.progress(1);
        }

        Collection keys;
        if (queue == null) {
            keys = queues.keySet();
        } else {
            keys = Collections.singleton(queue);
        }

        Iterator it = keys.iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Set commands = (Set) queues.get(key);
            if (commands == null) {
                commands = new HashSet();
            }
            commands.add(id);
            queues.put(key, commands);
        }
    }

    /**
     * Called by ExecutorSupport on start.
     */
    synchronized void started(ClientRuntime queue) {

        if (progressHandle != null) {
            progressHandle.switchToIndeterminate();
            progressHandle.setDisplayName(NbBundle.getMessage(ExecutorGroup.class, "BK2001", name));
        }

        if (!nonInteractive && started.add(queue)) {
            String msg = NbBundle.getMessage(ExecutorGroup.class, "BK1001", new Date(), getDisplayName());
            String sep = NbBundle.getMessage(ExecutorGroup.class, "BK1000");
            String header = "\n" + sep + "\n" + msg + "\n"; // NOI18N
            queue.log(header);
        }
        log("Start - " + name);
    }

    /**
     * Called by ExecutorSupport after processing.
     *
     * @param queue processign queue or null for all
     * @param id identifier paired with {@link #enqueued(ClientRuntime, Object)}
     */
    synchronized void finished(ClientRuntime queue, Object id) {

        Collection keys;
        if (queue == null) {
            keys = new HashSet(queues.keySet());
        } else {
            keys = Collections.singleton(queue);
        }

        boolean finished = executed; // TODO how to tip true for non-executed?
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            Object key = it.next();

            Set commands = (Set) queues.get(key);
            if (commands != null) {
                commands.remove(id);
                if (commands.isEmpty()) {
                    queues.remove(key);
                    if (executed && queues.isEmpty() && progressHandle != null) {
                        progressHandle.finish();
                        progressHandle = null;
                    }
                    log("End - " + name);
                }
                finished &= commands.isEmpty();
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "null commands for {0}, all commands {1}", new Object[] {key, queues}); //NOI18N
            }
        }

        if (finished) {
            logFinished(queue);
        }
    }

    private void logFinished(ClientRuntime queue) {
        if (nonInteractive) return;
        Collection consoles;
        if (queue == null) {
            consoles = started;
        } else {
            consoles = Collections.singleton(queue);
        }

        String msg;
        if (isCancelled()) {
            msg = NbBundle.getMessage(ExecutorGroup.class, "BK1006", new Date(), getDisplayName());
        } else {
            msg = NbBundle.getMessage(ExecutorGroup.class, "BK1002", new Date(), getDisplayName());
        }

        Iterator it2 = consoles.iterator();
        while (it2.hasNext()) {
            ClientRuntime console = (ClientRuntime) it2.next();
            console.log(msg + "\n"); // NOI18N
            console.flushLog();
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isFailed() {
        return failed;
    }

    /**
     * User cancel comming from Progress UI.
     * Must not be called by internals.
     */
    public boolean cancel() {
        cancelled = true;
        fail();
        return true;
    }

    /**
     * A command in group failed. Stop all pending commands if abortOnExecutorFailure is set.
     */
    public void fail() {
        if (!abortOnExecutorFailure) return;
        failed = true;
        Iterator it;
        synchronized(cancellables) {
            it = new ArrayList<Cancellable>(cancellables).iterator();
        }
        while (it.hasNext()) {
            try {
                Cancellable cancellable = (Cancellable) it.next();
                cancellable.cancel();
            } catch (RuntimeException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        synchronized(executors) {
            it = new ArrayList(executors).iterator();
        }
        while (it.hasNext()) {
            try {
                Object elem = it.next();
                if(elem instanceof ExecutorSupport) {
                    ((ExecutorSupport) elem).getTask().cancel();
                }
            } catch (RuntimeException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }        
        synchronized(this) {
            if (progressHandle != null) {
                progressHandle.finish();
                progressHandle = null;
            }
        }
    }

    /**
     * Add a cancelaable in chain of cancellable performers.
     */
    public void addCancellable(Cancellable cancellable) {
        synchronized(cancellables) {
            cancellables.add(cancellable);
        }
    }

    public void removeCancellable(Cancellable cancellable) {
        synchronized(cancellables) {
            cancellables.remove(cancellable);
        }
    }

    /**
     * Add executor into this group.
     */
    public synchronized void addExecutor(ExecutorSupport executor) {
        assert executed == false;
        executor.joinGroup(this);  // XXX third party code executed under lock
        executors.add(executor);
    }

    /**
     * Add executors into this group.
     * @param executors groupable or <code>null</code>
     */
    public final synchronized void addExecutors(ExecutorSupport[] executors) {
        if (executors == null) {
            return;
        } else {
            for (int i = 0; i < executors.length; i++) {
                ExecutorSupport support = executors[i];
                addExecutor(support);
            }
        }
    }

    /**
     * Group execution blocks on this barier until
     * all previously added Groupable finishes (succesfuly or with fail).
     *
     * <p>Warning: Groups with barries have blocking {@link #execute},
     * there is assert banning to execute such group from UI thread.
     */
    public synchronized void addBarrier(Runnable action) {
        assert executed == false;
        ExecutorGroupBar bar = new ExecutorGroupBar(executors, action);
        bar.joinGroup(this);
        executors.add(bar);
        hasBarrier = true;
    }

    /**
     * Can be added only from barrier action!
     */
    public synchronized void addCleanups(ExecutorSupport[] executors) {
        if (executors == null) {
            return;
        } else {
            for (int i = 0; i < executors.length; i++) {
                ExecutorSupport support = executors[i];
                addCleanup(support);
            }
        }
    }

    /**
     * Can be added only from barrier action!
     */
    public synchronized void addCleanup(ExecutorSupport executor) {
        assert executingCleanup == false;
        executor.joinGroup(this);
        cleanups.add(executor);
    }

    /**
     * Asynchronously executes all added executors. Executors
     * are grouped according to CVSRoot and serialized in
     * particular ClientRuntime (thread) queue. It maintains
     *
     * <p>Warning:
     * <ul>
     * <li>It becomes blocking if group contains barriers (there is UI thread assert).
     * <li>Do not call {@link ExecutorSupport#execute} if you
     * use grouping.
     * </ul>
     */
    public void execute() {
        assert (SwingUtilities.isEventDispatchThread() && hasBarrier) == false;

        synchronized(this) {
            executed = true;
        }
        Iterator it = executors.iterator();
        int i = 0;
        while (it.hasNext()) {
            Groupable support = (Groupable) it.next();
            try {
                support.execute();
            } catch (Error err) {
                ErrorManager.getDefault().notify(err);
                fail();
            } catch (RuntimeException ex) {
                ErrorManager.getDefault().notify(ex);
                fail();
            }
            i++;
            if (failed) break;
        }

        // cleanup actions

        synchronized(this) {
            executingCleanup = true;
        }
        it = cleanups.iterator();
        while (it.hasNext()) {
            Groupable support = (Groupable) it.next();
            support.execute();
            i++;
        }

        synchronized(this) {
            if (i == 0 && progressHandle != null) {  // kill progress provoked by progress()
                progressHandle.finish();
                progressHandle = null;
            }
        }
    }

    /**
     * Allows clients that execute grouped suppors
     * synchronously communicate their assumtion that
     * all supporst in group have been executed. It's
     * time for progress cleanup and logging finished
     * messages.
     */
    public synchronized void executed() {
        if (executed == false) {
            if (progressHandle != null) {
                progressHandle.finish();
                progressHandle = null;
            }
            logFinished(null);
        }
    }

    synchronized void increaseDataCounter(long bytes) {
        dataCounter += bytes;
        if (progressHandle != null) {  // dangling event from zombie worker thread
            progressHandle.progress(NbBundle.getMessage(ExecutorGroup.class, "BK2002", name, format(dataCounter)));
        }
    }

    private static String format(long counter) {
        if (counter < 1024*16) {
            return NbBundle.getMessage(ExecutorGroup.class, "BK2003", new Long(counter));
        }
        counter /= 1024;
        return NbBundle.getMessage(ExecutorGroup.class, "BK2004", new Long(counter));

        // do not go to megabytes as user want to see CHANGING number
        // it can be solved by average speed in last 5sec, as it drops to zero
        // something is wrong
    }

    /**
     * Link action. Take random output, inmost coses one anyway.
     */
    public void actionPerformed(ActionEvent e) {
        if (queues != null) {
            Set keys = queues.keySet();
            if (keys.isEmpty() == false) {
                ClientRuntime queue = (ClientRuntime) keys.iterator().next();
                queue.focusLog();
            }
        }
    }

    public void setNonInteractive(boolean nonInteractive) {
        this.nonInteractive = nonInteractive;
    }

    public static interface Groupable {

        /**
         * Notifies the Groupable that it is a part of
         * given execution chain.
         *
         * <p> Must be called before {@link #execute}
         */
        void joinGroup(ExecutorGroup group);

        /** Execute custom code. */
        void execute();
    }

    private static void log(String msg) {
        Utils.logT9Y(msg);
//        CVS.LOG.log(Level.FINE, msg);
    }
}
