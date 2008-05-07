/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.groovy.grailsproject.execution;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * <p>An ExecutionService takes an {@link ExecutionDescriptor} and executes it.
 * It will execute the program with an associated I/O window, with stop and
 * restart buttons. It will also obey various descriptor properties such as
 * whether or not to show a progress bar.
 * <p>
 * All launched processes will be killed on exit. Possibly I could make this
 * optional or at least ask the user.
 * </p>
 *
 * @todo Add a Restart button which accomplishes both a stop and a restart
 *
 * @author Tor Norbye, Petr Hejl
 */
public class ExecutionService {

    public static final Logger LOGGER = Logger.getLogger(ExecutionService.class.getName());

    static {
        Thread t = new Thread() {

            @Override
            public void run() {
                ExecutionService.killAll();
            }
        };

        Runtime.getRuntime().addShutdownHook(t);
    }

    /** Display names of currently active processes. */
    private static final Set<String> ACTIVE_DISPLAY_NAMES = new HashSet<String>();

    /** Set of currently active processes. */
    private static final Set<ExecutionService> RUNNING_PROCESSES = new HashSet<ExecutionService>();

    private static final RequestProcessor PROCESSOR = new RequestProcessor("Execution service", 10, true); // NOI18N

    private InputOutput io;
    private StopAction stopAction;
    private RerunAction rerunAction;
    private final Callable<Process> processCreator;
    private final Descriptor descriptor;
    private final String originalDisplayName;
    private String displayName; // May be tweaked from descriptor to deal with duplicate running same-name processes

    private boolean rerun;

    public ExecutionService(Callable<Process> processCreator, String displayName, Descriptor descriptor) {
        this.processCreator = processCreator;
        this.originalDisplayName = displayName;
        this.descriptor = descriptor;
    }

    public void kill() {
        // temp logging to track down #131628
        LOGGER.log(Level.FINE, "Killing " + this.displayName + " " + this);
        if (stopAction != null) {
            //LOGGER.log(Level.FINE, "StopAction: " + stopAction);
            //stopAction.actionPerformed(null);
            Process process = stopAction.getProcess();
            stopAction.setProcess(null);
            if (process != null) {
                LOGGER.log(Level.FINE, "Destroying process: " + process);
                process.destroy();
            }
        }
    }

    public static void killAll() {
        for (ExecutionService service : RUNNING_PROCESSES) {
            service.kill();
        }
    }

    Task rerun() {
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        rerun = true;
        return run();
    }

    public Task run() {
        if (!rerun) {
            // try to find free output windows
            synchronized (this) {
                if (io == null) {
                    FreeIOHandler freeIO = FreeIOHandler.findFreeIO(originalDisplayName);
                    if (freeIO != null) {
                        io = freeIO.getIO();
                        displayName = freeIO.getDisplayName();
                        stopAction = freeIO.getStopAction();
                        rerunAction = freeIO.getRerunAction();
                        if (descriptor.isFrontWindow()) {
                            io.select();
                        }
                    }
                }
            }

            if (io == null) { // free IO was not found, create new one
                displayName = getNonActiveDisplayName(originalDisplayName);

                stopAction = new StopAction();
                rerunAction = new RerunAction(this, descriptor.getFileObject());

                io = IOProvider.getDefault().getIO(displayName, new Action[]{rerunAction, stopAction});

                try {
                    io.getOut().reset();
                } catch (IOException exc) {
                    ErrorManager.getDefault().notify(exc);
                }

                // Note - do this AFTER the reset() call above; if not, weird bugs occur
                io.setErrSeparated(false);

                // Open I/O window now. This should probably be configurable.
                if (descriptor.isFrontWindow()) {
                    io.select();
                }
            }
        }

        ACTIVE_DISPLAY_NAMES.add(displayName);
        io.setInputVisible(descriptor.isInputVisible());

        Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        Process process = processCreator.call();

                        RUNNING_PROCESSES.add(ExecutionService.this);
                        stopAction.setProcess(process);
                        runIO(stopAction, process, io, descriptor.getOutputSnooper(),
                                descriptor.getFileObject());

                        try {
                            process.waitFor();
                        } catch (InterruptedException ex) {
                            process.destroy();
                        }
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            };

        final ProgressHandle handle;

        if (descriptor.showProgress() || descriptor.showSuspended()) {
            handle =
                ProgressHandleFactory.createHandle(displayName,
                    new Cancellable() {
                        public boolean cancel() {
                            stopAction.actionPerformed(null);

                            return true;
                        }
                    },
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            io.select();
                        }
                    });
            handle.start();
            handle.switchToIndeterminate();

            if (descriptor.showSuspended()) {
                handle.suspend(NbBundle.getMessage(ExecutionService.class, "Running"));
            }
        } else {
            handle = null;
        }

        stopAction.setEnabled(true);
        rerunAction.setEnabled(false);

        Task task = PROCESSOR.post(runnable);

        task.addTaskListener(new TaskListener() {
                public void taskFinished(Task task) {
                    RUNNING_PROCESSES.remove(ExecutionService.this);

                    if (io != null) {
                        FreeIOHandler.addFreeIO(io, displayName, stopAction, rerunAction);
                    }

                    ACTIVE_DISPLAY_NAMES.remove(displayName);

                    if (descriptor.getPostExecution() != null) {
                        descriptor.getPostExecution().run();
                    }

                    if (handle != null) {
                        handle.finish();
                    }

                    stopAction.setEnabled(false);
                    rerunAction.setEnabled(true);

                    Process process = stopAction.getProcess();
                    stopAction.setProcess(null);
                    if (process != null) {
                        process.destroy();
                    }
                }
            });

        return task;
    }

    private static void runIO(final StopAction sa, Process process, InputOutput io,
        LineSnooper snooper, FileObject toRefresh) {

        Thread inputThread = null;
        Thread errorThread = null;
        try {

            // FIXME will be repaced with output API
            inputThread = new StreamInputThread(process.getOutputStream(), io.getIn());
            errorThread = new StreamRedirectThread(process.getErrorStream(), io.getErr());
            inputThread.start();
            errorThread.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            try {
                String lineString;
                // FIXME use the output API
                while ((lineString = reader.readLine()) != null) {
                    if (snooper != null) {
                        snooper.lineFilter(lineString);
                    }
                    io.getOut().println(lineString);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
                io.getOut().close();
            }

            process.waitFor();
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, "Exiting thread", ex);
            process.destroy();
        } finally {
            if (inputThread != null) {
                inputThread.interrupt();
            }
            if (errorThread != null) {
                errorThread.interrupt();
            }
            FileUtil.refreshFor(FileUtil.toFile(toRefresh));
        }
    }

    static boolean isAppropriateName(String base, String toMatch) {
        if (!toMatch.startsWith(base)) {
            return false;
        }
        return toMatch.substring(base.length()).matches("^(\\ #[0-9]+)?$"); // NOI18N
    }

    private static String getNonActiveDisplayName(final String displayNameBase) {
        String nonActiveDN = displayNameBase;
        if (ACTIVE_DISPLAY_NAMES.contains(nonActiveDN)) {
            // Uniquify: "prj (targ) #2", "prj (targ) #3", etc.
            int i = 2;
            String testdn;

            do {
                testdn = NbBundle.getMessage(ExecutionService.class, "Uniquified", nonActiveDN, i++);
            } while (ACTIVE_DISPLAY_NAMES.contains(testdn));

            nonActiveDN = testdn;
        }
        assert !ACTIVE_DISPLAY_NAMES.contains(nonActiveDN);
        return nonActiveDN;
    }

}
