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
package org.netbeans.modules.dlight.terminal.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOVisibility;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Vladimir Voskresensky
 */
abstract class TerminalAction extends AbstractAction implements Presenter.Toolbar {

    private static final RequestProcessor RP = new RequestProcessor("Terminal Action RP", 100); // NOI18N

    public TerminalAction(String name, String descr, ImageIcon icon) {
        putValue(Action.NAME, name);
        putValue(Action.SMALL_ICON, icon);
        putValue(Action.SHORT_DESCRIPTION, descr);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
        instance.open();
        instance.requestActive();
        final IOContainer ioContainer = instance.getIOContainer();
        final IOProvider term = IOProvider.get("Terminal"); // NOI18N
        if (term != null) {
            final ExecutionEnvironment env = getEnvironment();
            final AtomicBoolean destroyed = new AtomicBoolean(false);
            if (env != null) {
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        if (SwingUtilities.isEventDispatchThread()) {
                            instance.requestActive();
                        } else {
                            doWork();
                        }
                    }

                    private void doWork() {
                        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                            try {
                                ConnectionManager.getInstance().connectTo(env);
                            } catch (IOException ex) {
                                if (!destroyed.get()) {
                                    String error = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                                    String msg = NbBundle.getMessage(TerminalAction.class, "TerminalAction.FailedToStart.text", error); // NOI18N
                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                                }
                                return;
                            } catch (CancellationException ex) {
                                return;
                            }
                        }

                        final HostInfo hostInfo;
                        try {
                            hostInfo = HostInfoUtils.getHostInfo(env);
                            boolean isSupported = PtySupport.isSupportedFor(env);
                            if (!isSupported) {
                                if (!TerminalContainerTopComponent.SILENT_MODE_COMMAND.equals(e.getActionCommand())) {
                                    String message;

                                    if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                                        message = NbBundle.getMessage(TerminalAction.class, "LocalTerminalNotSupported.error.nocygwin"); // NOI18N
                                    } else {
                                        message = NbBundle.getMessage(TerminalAction.class, "LocalTerminalNotSupported.error"); // NOI18N
                                    }

                                    NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
                                    DialogDisplayer.getDefault().notify(nd);
                                }
                                return;
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                            return;
                        } catch (CancellationException ex) {
                            Exceptions.printStackTrace(ex);
                            return;
                        }

                        final AtomicReference<InputOutput> ioRef = new AtomicReference<InputOutput>();
                        try {
                            ioRef.set(term.getIO(env.getDisplayName(), null, ioContainer));

                            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
                            npb.addNativeProcessListener(new NativeProcessListener(ioRef.get(), destroyed));

                            String shell = hostInfo.getLoginShell();
//                            npb.setWorkingDirectory("${HOME}");
                            npb.setExecutable(shell);
                            NativeExecutionDescriptor descr;
                            descr = new NativeExecutionDescriptor().controllable(true).frontWindow(true).inputVisible(false).inputOutput(ioRef.get());
                            descr.postExecution(new Runnable() {

                                @Override
                                public void run() {
                                    ioRef.get().closeInputOutput();
                                }
                            });
                            NativeExecutionService es = NativeExecutionService.newService(npb, descr, "Terminal Emulator"); // NOI18N
                            Future<Integer> result = es.run();
                            // ask terminal to become active
                            SwingUtilities.invokeLater(this);

                            try {
                                // if terminal can not be started then ExecutionException should be thrown
                                // wait one second to see if terminal can not be started. otherwise it's OK to exit by TimeOut
                                result.get(1, TimeUnit.SECONDS);
                            } catch (TimeoutException ex) {
                                // we should be there
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                if (!destroyed.get()) {
                                    String error = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                                    String msg = NbBundle.getMessage(TerminalAction.class, "TerminalAction.FailedToStart.text", error); // NOI18N
                                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                                }
                            }
                        } catch (CancellationException ex) {
                            Exceptions.printStackTrace(ex);
                            reportInIO(ioRef.get(), ex);
                        }
                    }

                    private void reportInIO(InputOutput io, Exception ex) {
                        if (io != null && ex != null) {
                            io.getErr().print(ex.getLocalizedMessage());
                        }
                    }
                };
                RP.post(runnable);
            }
        }
    }

    @Override
    public Component getToolbarPresenter() {
        JButton button = new JButton(this);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setText(null);
        button.putClientProperty("hideActionText", Boolean.TRUE); // NOI18N
        Object icon = getValue(Action.SMALL_ICON);
        if (!(icon instanceof Icon)) {
            throw new IllegalStateException("No icon provided for " + this); // NOI18N
        }
        button.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) icon));
        return button;
    }

    protected abstract ExecutionEnvironment getEnvironment();

    private final static class NativeProcessListener implements ChangeListener, PropertyChangeListener {

        private final AtomicReference<NativeProcess> processRef;
        private final AtomicBoolean destroyed;

        public NativeProcessListener(InputOutput io, AtomicBoolean destroyed) {
            assert destroyed != null;
            this.destroyed = destroyed;
            this.processRef = new AtomicReference<NativeProcess>();
            IONotifier.addPropertyChangeListener(io, WeakListeners.propertyChange(NativeProcessListener.this, io));
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            NativeProcess process = processRef.get();
            if (process == null && e.getSource() instanceof NativeProcess) {
                processRef.compareAndSet(null, (NativeProcess) e.getSource());
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (IOVisibility.PROP_VISIBILITY.equals(evt.getPropertyName()) && Boolean.FALSE.equals(evt.getNewValue())) {
                if (destroyed.compareAndSet(false, true)) {
                    // term is closing => destroy process
                    NativeProcess proc = processRef.get();
                    if (proc != null) {
                        proc.destroy();
                    }
                }
            }
        }
    }
}
