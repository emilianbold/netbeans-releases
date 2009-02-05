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
package org.netbeans.modules.nativeexecution.api;

import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.impl.LocalNativeProcess;
import java.util.concurrent.Callable;
import org.netbeans.modules.nativeexecution.api.impl.NativeProcessInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess.Listener;
import org.netbeans.modules.nativeexecution.api.impl.RemoteNativeProcess;
import org.netbeans.modules.nativeexecution.api.impl.TerminalLocalNativeProcess;

/**
 * Utility class for external process creation.
 * <p>
 * Depending on {@link NativeProcessConfiguration} and {@link ExecutionControl}
 */
public final class NativeProcessBuilder implements Callable<Process> {

    private NativeProcessInfo info = null;
    private boolean useExternalTerminal = false;
    private NativeProcess process = null;

    public NativeProcessBuilder(
            final ExecutionEnvironment execEnv,
            final String command) {
        info = new NativeProcessInfo(execEnv, command);
    }

    public NativeProcessBuilder(final String command) {
        this(new ExecutionEnvironment(), command);
    }

    private NativeProcessBuilder(NativeProcessBuilder b) {
        info = new NativeProcessInfo(b.info);
        useExternalTerminal = b.useExternalTerminal;
    }

    public NativeProcessBuilder addNativeProcessListener(Listener listener) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.addNativeProcessListener(listener);
        return result;
    }

    public NativeProcess call() throws IOException {
        if (info.getExecutionEnvironment().isRemote()) {
            process = new RemoteNativeProcess(info);
        } else {
            if (useExternalTerminal == true) {
                process = new TerminalLocalNativeProcess(info);
            } else {
                process = new LocalNativeProcess(info);
            }
        }

        return process;
    }

    public NativeProcessBuilder setWorkingDirectory(String workingDirectory) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.setWorkingDirectory(workingDirectory);
        return result;
    }

    public NativeProcessBuilder addEnvironmentVariable(String name, String value) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.addEnvironmentVariable(name, value);
        return result;
    }

    public NativeProcessBuilder setArguments(String... arguments) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.info.setArguments(arguments);
        return result;
    }

    public NativeProcessBuilder useExternalTerminal(boolean useExternalTerminal) {
        NativeProcessBuilder result = new NativeProcessBuilder(this);
        result.useExternalTerminal = useExternalTerminal;
        return result;
    }
}
