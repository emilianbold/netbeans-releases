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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.nativeexecution.access.NativeTaskConfigAccessor;

/**
 *
 */
public class NativeTaskConfig {
    private ExecutionEnvironment execEnv;
    private String cmd;
    private String workingDirectory;
    private final ArrayList<String> arguments = new ArrayList<String>();
    private final Map<String, String> envVariables =
            new HashMap<String, String>();

    static {
        NativeTaskConfigAccessor.setDefault(new NativeTaskConfigAccessorImpl());
    }

    public NativeTaskConfig(ExecutionEnvironment execEnv, String cmd) {
        this.execEnv = execEnv;
        this.cmd = cmd;
    }

    public NativeTaskConfig(String cmd) {
        this(new ExecutionEnvironment(), cmd);
    }

    private NativeTaskConfig(NativeTaskConfig cfg) {
        execEnv = cfg.execEnv;
        cmd = cfg.cmd;
        workingDirectory = cfg.workingDirectory;
        envVariables.putAll(cfg.envVariables);
        arguments.addAll(cfg.arguments);
    }

    private String getCommandLine() {
        StringBuilder sb = new StringBuilder(cmd);
        
        if (!arguments.isEmpty()) {
            for (String arg : arguments) {
                sb.append(' ').append(arg);
            }
        }

        return sb.toString().trim();
    }

    public NativeTaskConfig setWorkingDirectory(String workingDirectory) {
        NativeTaskConfig result = new NativeTaskConfig(this);
        result.workingDirectory = workingDirectory;
        return result;
    }

    public NativeTaskConfig addEnvironmentVariable(String name, String value) {
        NativeTaskConfig result = new NativeTaskConfig(this);
        result.envVariables.put(name, value);
        return result;
    }

    public NativeTaskConfig setArguments(String ... arguments) {
        NativeTaskConfig result = new NativeTaskConfig(this);

        for (String arg : arguments) {
            result.arguments.add(arg);
        }
        
        return result;
    }

    private static class NativeTaskConfigAccessorImpl
            extends NativeTaskConfigAccessor {

        @Override
        public String getWorkingDirectory(NativeTaskConfig config) {
            return config.workingDirectory;
        }

        @Override
        public ExecutionEnvironment getExecutionEnvironment(
                NativeTaskConfig config) {
            return config.execEnv;
        }

        @Override
        public Map<String, String> getEnvVariables(NativeTaskConfig config) {
            return config.envVariables;
        }

        @Override
        public String getCommandLine(NativeTaskConfig config) {
            return config.getCommandLine();
        }

    }
}
