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
package org.netbeans.modules.nativeexecution.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess.Listener;

/**
 *
 */
public class NativeProcessInfo {

    private final ExecutionEnvironment execEnv;
    private final String command;
    private final List<String> arguments = new ArrayList<String>();
    private final Map<String, String> envVariables =
            new HashMap<String, String>();
    private String workingDirectory;
    private Collection<Listener> listeners = null;

    public NativeProcessInfo(NativeProcessInfo info) {
        this(info.execEnv, info.command);
        workingDirectory = info.workingDirectory;
        envVariables.putAll(info.envVariables);
        arguments.addAll(info.arguments);

        if (info.listeners != null) {
            listeners = new ArrayList<Listener>(info.listeners);
        }
    }

    public NativeProcessInfo(ExecutionEnvironment execEnv, String command) {
        this.execEnv = execEnv;
        this.command = command;
    }

    public void addNativeProcessListener(Listener listener) {
        if (listeners == null) {
            listeners = new ArrayList<Listener>();
        }

        listeners.add(listener);
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void addEnvironmentVariable(String name, String value) {
        envVariables.put(name, value);
    }

    public void addEnvironmentVariables(Map<String, String> envs) {
        envVariables.putAll(envs);
    }

    public void setArguments(String... arguments) {
        this.arguments.clear();
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public String getCommandLine() {
        StringBuilder sb = new StringBuilder(command);

        if (!arguments.isEmpty()) {
            for (String arg : arguments) {
                sb.append(' ').append(arg);
            }
        }

        return sb.toString().trim();
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    public Collection<Listener> getListeners() {
        return listeners;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public Map<String, String> getEnvVariables() {
        return envVariables;
    }
}
