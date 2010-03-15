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
package org.netbeans.modules.nativeexecution.api.execution;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.modules.terminal.api.IOEmulation;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.util.RequestProcessor;

/**
 * This is a wrapper over an <tt>Executionservice</tt> that handles running
 * NativeProcesses in a terminal output window.
 *
 * It also can be used for running in an output windows - in this case it just
 * delegares execution to the <tt>ExecutionService</tt>
 *
 * @see ExecutionService
 * @see NativeExecutionDescriptor
 *
 * @author ak119685
 */
public final class NativeExecutionService {

    private final NativeProcessBuilder processBuilder;
    private final String displayName;
    private final NativeExecutionDescriptor descriptor;

    private NativeExecutionService(NativeProcessBuilder processBuilder, String displayName, NativeExecutionDescriptor descriptor) {
        this.processBuilder = processBuilder;
        this.displayName = displayName;
        this.descriptor = descriptor;
    }

    public static NativeExecutionService newService(NativeProcessBuilder processBuilder,
            NativeExecutionDescriptor descriptor, String displayName) {
        return new NativeExecutionService(processBuilder, displayName, descriptor);
    }

    public Future<Integer> run() {
        if (IOTerm.isSupported(descriptor.inputOutput)) {
            return runTerm();
        } else {
            return runRegular();
        }
    }

    private Future<Integer> runTerm() {
        processBuilder.setUsePty(true);

        if (IOEmulation.isSupported(descriptor.inputOutput)) {
            processBuilder.getEnvironment().put("TERM", IOEmulation.getEmulation(descriptor.inputOutput)); // NOI18N
        } else {
            processBuilder.getEnvironment().put("TERM", "dumb"); // NOI18N
        }

        FutureTask<Integer> runTask = new FutureTask<Integer>(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                try {
                    final NativeProcess process = processBuilder.call();

                    if (descriptor.frontWindow) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                descriptor.inputOutput.select();
                            }
                        });
                        
                    }

                    PtySupport.connect(descriptor.inputOutput, process);
                    return process.waitFor();
                } finally {
                    if (descriptor.postExecution != null) {
                        descriptor.postExecution.run();
                    }
                }
            }
        });

        RequestProcessor.getDefault().post(runTask);

        return runTask;
    }

    private Future<Integer> runRegular() {
            ExecutionDescriptor descr = new ExecutionDescriptor()
                    .controllable(descriptor.controllable)
                    .frontWindow(descriptor.frontWindow)
                    .inputVisible(descriptor.inputVisible)
                    .inputOutput(descriptor.inputOutput)
                    .outLineBased(descriptor.outLineBased)
                    .showProgress(descriptor.showProgress)
                    .postExecution(descriptor.postExecution)
                    .noReset(descriptor.noReset)
                    .errConvertorFactory(descriptor.errConvertorFactory)
                    .outConvertorFactory(descriptor.outConvertorFactory);

        ExecutionService es = ExecutionService.newService(processBuilder, descr, displayName);
        return es.run();
    }
}
