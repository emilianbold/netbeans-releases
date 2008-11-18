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
package org.netbeans.modules.mobility.project.ui;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * If a library is referred to by one or more
 * mobility projects, but does not actually exist on disk, it can register a
 * FileMonitor.  This is used to update the nodes' broken/unbroken status.
 * <p/>
 * Uses a polling mechanism instead of FileChangeListeners, since as of 6.5
 * FileChangeListeners are unreliable for firing events due to external events
 * (such as an ant task building the project).
 * Polls the existence of the file on a background thread every INTERVAL
 * milliseconds, and compares exists() and lastModified() with previously
 * known values.
 * <p/>
 * Instances of FileMonitor are weakly referenced and must be held or implemented
 * on the long-lived object that is interested in notifications about file changes.
 *
 * @author Tim Boudreau
 */
class MissingFileDetector {

    static int INTERVAL = 15000;
    static MissingFileDetector INSTANCE = new MissingFileDetector();
    private final Map<File, OneFileItem> monitorsToFiles = new ConcurrentHashMap<File, OneFileItem>();
    static boolean unitTest;
    volatile boolean firstRun;
    private volatile boolean listeningToMainWindow;
    private final RunnerAndWindowHandler windowRunner = new RunnerAndWindowHandler();
    private final RequestProcessor.Task task = RequestProcessor.getDefault().create(windowRunner);
    volatile boolean active; //for unit tests

    private MissingFileDetector() {
        assert unitTest || INSTANCE == null;
        task.setPriority(Thread.MIN_PRIORITY);
        if (!unitTest) {
            findMainWindow();
        }
    }

    static void reset() { //for unit tests
        INSTANCE = new MissingFileDetector();
    }

    private void scheduleTask() {
        active = true;
        task.schedule(INTERVAL);
    }

    private void cancelTask() {
        active = false;
        task.cancel();
    }

    /**
     * Inner class to handle listening and runnable so these interfaces are
     * not visible in MissingFileDetector's class signature.
     */
    private class RunnerAndWindowHandler extends WindowAdapter implements Runnable {

        @Override
        public void windowActivated(WindowEvent e) {
            scheduleTask();
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            cancelTask();
        }

        public void run() {
            prune();
            if (!isEmpty() && mainWindowIsActive()) {
                scheduleTask();
            }
            for (File file : monitorsToFiles.keySet()) {
                OneFileItem item = monitorsToFiles.get(file);
                item.poll();
            }
        }
    }

    private boolean mainWindowIsActive() {
        return unitTest ? true : mainWindow == null ? false : mainWindow.isActive();
    }

    private synchronized Window getMainWindow() {
        return mainWindow;
    }

    private synchronized void setMainWindow(Window w) {
        mainWindow = w;
    }

    private void findMainWindow() {
        firstRun = false;
        if (!unitTest) {
            Runnable r = new Runnable() {
                public void run() {
                    Window w = WindowManager.getDefault().getMainWindow();
                    setMainWindow (w);
                    if (shouldBeListening) {
                        w.addWindowListener(windowRunner);
                        scheduleTask();
                    }
                }
            };
            if (EventQueue.isDispatchThread()) {
                r.run();
            } else {
                EventQueue.invokeLater(r);
            }
        }
    }

    public static void register(File file, FileMonitor monitor) {
        if (file == null) {
            throw new NullPointerException("null file");
        }
        if (monitor == null) {
            throw new NullPointerException("Null monitor");
        }
        INSTANCE._register(file, monitor);
    }

    public static void unregister(File file, FileMonitor monitor) {
        if (file == null) {
            throw new NullPointerException("null file");
        }
        if (monitor == null) {
            throw new NullPointerException("Null monitor");
        }
        INSTANCE._unregister(file, monitor);
    }

    private void remove(File file) {
        monitorsToFiles.remove(file);
        prune();
    }

    void _register(File file, FileMonitor monitor) {
        if (firstRun) {
            findMainWindow();
        }
        if (!unitTest && !listeningToMainWindow) {
            startListening();
        }
        scheduleTask();
        OneFileItem item = monitorsToFiles.get(file);
        if (item == null) {
            item = new OneFileItem(file);
            monitorsToFiles.put(file, item);
        }
        item.add(monitor);
    }

    void _unregister(File file, FileMonitor monitor) {
        OneFileItem item = monitorsToFiles.get(file);
        if (item != null) {
            item.remove(monitor);
            if (item.notifyMonitorsAndCleanUpReferences(null)) {
                monitorsToFiles.remove(file);
            }
        }
        prune();
    }
    private Window mainWindow;
    private volatile boolean shouldBeListening;

    private void startListening() {
        shouldBeListening = true;
        if (!listeningToMainWindow && !unitTest) {
            listeningToMainWindow = true;
            Window w = getMainWindow();
            if (w != null) {
                w.addWindowListener(windowRunner);
            }
        }
        scheduleTask();
    }

    private void stopListening() {
        shouldBeListening = false;
        if (!unitTest && !listeningToMainWindow) {
            listeningToMainWindow = false;
            Window w = getMainWindow();
            if (w != null) {
                w.removeWindowListener(windowRunner);
            }
        }
        cancelTask();
    }

    private boolean isEmpty() {
        return monitorsToFiles.isEmpty();
    }

    private void prune() {
        for (File file : new HashSet<File>(monitorsToFiles.keySet())) {
            OneFileItem c = monitorsToFiles.get(file);
            if (c.notifyMonitorsAndCleanUpReferences(null)) { //all weak references dead
                monitorsToFiles.remove(file);
            }
        }
        if (isEmpty()) {
            cancelTask();
            stopListening();
        }
    }

    private final class OneFileItem {

        private final File file;
        private final List<OneMonitorItem> monitors =
                Collections.synchronizedList(new LinkedList<OneMonitorItem>());
        private boolean exists;
        private long timestamp;

        OneFileItem(File file) {
            if (file == null) {
                throw new NullPointerException("Null file");
            }
            this.file = FileUtil.normalizeFile(file);
            timestamp = file.exists() ? file.lastModified() : 0L;
            exists = file.exists();
            startListening();
        }

        private void notifyFileChanged() {
            if (notifyMonitorsAndCleanUpReferences(new MonitorRunnableImpl(RunnableKinds.CHANGED))) {
                MissingFileDetector.this.remove(file);
            }
        }

        private void notifyFileCreated() {
            if (notifyMonitorsAndCleanUpReferences(new MonitorRunnableImpl(RunnableKinds.CREATED))) {
                MissingFileDetector.this.remove(file);
            }
        }

        private void notifyFileDeleted() {
            if (notifyMonitorsAndCleanUpReferences(new MonitorRunnableImpl(RunnableKinds.DELETED))) {
                MissingFileDetector.this.remove(file);
            }
        }

        public boolean exists() {
            return file.exists();
        }

        public void add(FileMonitor monitor) {
            assert monitor != null : "Monitor null";
            boolean assertions = false;
            assert assertions = true;
            notifyMonitorsAndCleanUpReferences(null); //prune dead items
            if (assertions) {
                OneMonitorItem other = checkDuplicate(monitor);
                if (other != null) {
                    throw new IllegalArgumentException("File monitor added " +
                            "twice: " + monitor + "\nStack trace for when first added " +
                            "listed below as cause", other.exception);
                }
            }
            monitors.add(new OneMonitorItem(monitor));
        }

        public boolean remove(FileMonitor monitor) {
            assert monitor != null : "Null monitor";
            for (Iterator<OneMonitorItem> it = monitors.iterator(); it.hasNext();) {
                OneMonitorItem item = it.next();
                FileMonitor other = item.get();
                if (other == null || monitor.equals(other)) {
                    it.remove();
                }
            }
            return monitors.isEmpty();
        }

        private OneMonitorItem checkDuplicate(FileMonitor monitor) {
            for (Iterator<OneMonitorItem> it = monitors.iterator(); it.hasNext();) {
                OneMonitorItem item = it.next();
                FileMonitor other = item.get();
                if (other == null) {
                    it.remove();
                } else {
                    if (monitor.equals(other)) {
                        return item;
                    }
                }
            }
            return null;
        }

        /**
         * Runs the passed MonitorRunnable against all FileMonitors registered
         * for this file, one by one.  Removes any dead WeakReferences to
         * FileMonitors that have been garbage collected.
         *
         * @param toRun A MonitorRunnable to execute against each FileMonitor
         * (if any).  May be null if you just want to clear all dead
         * references.
         *
         * @return true if this OneFileItem no longer has any FileMonitors to
         * notify (in which case it can be discarded)
         */
        private boolean notifyMonitorsAndCleanUpReferences(MonitorRunnable toRun) {
            List<FileMonitor> toNotify = null;
            for (Iterator<OneMonitorItem> it = monitors.iterator(); it.hasNext();) {
                OneMonitorItem item = it.next();
                FileMonitor monitor = item.get();
                if (monitor == null) {
                    it.remove();
                } else if (toRun != null) {
                    if (toNotify == null) {
                        toNotify = new LinkedList<FileMonitor>();
                    }
                    toNotify.add(monitor);
                }
            }
            if (toRun != null && !toNotify.isEmpty()) {
                EventQueue.invokeLater(new Notifier(toNotify, toRun));
            }
            return monitors.isEmpty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final OneFileItem other = (OneFileItem) obj;
            if (this.file != other.file && !this.file.equals(other.file)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.file != null ? this.file.hashCode() : 0);
            return hash;
        }

        private void poll() {
            boolean oldExists = exists;
            long oldTimestamp = timestamp;
            if (oldExists != (exists = file.exists())) {
                if (exists) {
                    notifyFileCreated();
                } else {
                    timestamp = 0;
                    notifyFileDeleted();
                }
            } else if (oldTimestamp != (timestamp = file.lastModified())) {
                notifyFileChanged();
            }
            this.exists = file.exists();
        }
    }

    private static final class Notifier implements Runnable {

        private final List<? extends FileMonitor> monitors;
        private final MonitorRunnable notifier;

        private Notifier(List<? extends FileMonitor> monitors, MonitorRunnable notifier) {
            this.monitors = monitors;
            this.notifier = notifier;
        }

        public void run() {
            for (FileMonitor monitor : monitors) {
                notifier.run(monitor);
            }
        }
    }

    private interface MonitorRunnable {

        public void run(FileMonitor monitor);
    }

    private enum RunnableKinds {

        CREATED, CHANGED, DELETED
    }

    private static final class MonitorRunnableImpl implements MonitorRunnable {

        private final RunnableKinds kind;

        private MonitorRunnableImpl(RunnableKinds kind) {
            this.kind = kind;
        }

        public void run(FileMonitor monitor) {
            switch (kind) {
                case CHANGED:
                    monitor.fileChanged();
                    break;
                case CREATED:
                    monitor.fileCreated();
                    break;
                case DELETED:
                    monitor.fileDeleted();
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    private static final class OneMonitorItem extends WeakReference<FileMonitor> {

        private final Exception exception;

        OneMonitorItem(FileMonitor monitor) {
            super(monitor);
            boolean assertions = false;
            assert assertions = true;
            if (assertions) {
                exception = new Exception();
            } else {
                exception = null;
            }
        }
    }
}
