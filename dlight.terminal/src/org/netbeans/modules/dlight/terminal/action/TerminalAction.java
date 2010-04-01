/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.terminal.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionDescriptor;
import org.netbeans.modules.nativeexecution.api.execution.NativeExecutionService;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Vladimir Voskresensky
 */
abstract class TerminalAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
        instance.open();
        instance.requestActive();
        final IOContainer ioContainer = instance.getIOContainer();
        final IOProvider term = IOProvider.get("Terminal"); // NOI18N
        if (term != null) {
            final ExecutionEnvironment env = getEnvironment();
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
                                String error = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                                String msg = NbBundle.getMessage(TerminalAction.class, "TerminalAction.FailedToStart.text", error); // NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                                return;
                            } catch (CancellationException ex) {
                                return;
                            }
                        }

                        try {
                            final InputOutput io = term.getIO(env.getDisplayName(), getActions(), ioContainer);
                            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);

                            npb.addNativeProcessListener(new ChangeListener() {

                                @Override
                                public void stateChanged(ChangeEvent e) {
                                    NativeProcessChangeEvent ev = (NativeProcessChangeEvent) e;
//                                    System.out.println("Process' " + e.getSource() + " state is " + ev.state);
                                }
                            });

                            final HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                            String shell = hostInfo.getShell();
//                            npb.setWorkingDirectory("${HOME}");
                            npb.setExecutable(shell);
                            NativeExecutionDescriptor descr;
                            descr = new NativeExecutionDescriptor().controllable(true).frontWindow(true).inputVisible(false).inputOutput(io);
                            NativeExecutionService es = NativeExecutionService.newService(npb, descr, "Terminal Emulator"); // NOI18N
                            Future<Integer> result = es.run();
                            // ask terminal to become active
                            SwingUtilities.invokeLater(this);

                            try {
                                result.get();
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ExecutionException ex) {
                                String error = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                                String msg = NbBundle.getMessage(TerminalAction.class, "TerminalAction.FailedToStart.text", error); // NOI18N
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (CancellationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
                RequestProcessor.getDefault().post(runnable);
            }
        }
    }

    protected abstract ExecutionEnvironment getEnvironment();
    private static Action[] actions;

    private synchronized static Action[] getActions() {
        if (actions == null) {
            List<? extends Action> termActions = Utilities.actionsForPath("Actions/Terminal");// NOI18N
            actions = termActions.toArray(new Action[termActions.size()]);
        }
        return actions;
    }
}
