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
package org.netbeans.modules.cnd.remote.experimental;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * proto
 *
 * @author Sergey Grinev
 */
public class RemoteableExtExecution {

    private static class Instantiator {

        static final RemoteableExtExecution instance = new RemoteableExtExecution();
    }

    public static RemoteableExtExecution getInstance() {
        return Instantiator.instance;
    }

    public void test() {
        //runCommandLocally("ls"); //NOI18N
        runCommandRemotely("ls"); //NOI18N
    }

    private void runCommandRemotely(final String command) {
        ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true).controllable(true);
        RemoteProcessBuilder processBuilder = new RemoteProcessBuilder(command);
        ExecutionService service = ExecutionService.newService(processBuilder, descriptor, "running " + command); //NOI18N
        final Future<Integer> task = service.run();

//        RequestProcessor.getDefault().post(new Runnable() {
//
//            public void run() {
//                try {
//                    //System.err.println(command + " returned next result: " + task.get());
//                } catch (InterruptedException ex) {
//                    Exceptions.printStackTrace(ex);
//                } catch (ExecutionException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        });

    }

    private void runCommandLocally(final String command) {
        ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true).controllable(true);
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(command);
        ExecutionService service = ExecutionService.newService(processBuilder, descriptor, "running " + command); //NOI18N
        final Future<Integer> task = service.run();

        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                try {
                    System.err.println(command + " returned next result: " + task.get());
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

    }
}
