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
package org.netbeans.modules.dlight.api.support;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.openide.windows.InputOutput;

/**
 * This class is configuration class used as constructor 
 * parameter when creating {@link org.netbeans.modules.dlight.api.support.NativeExecutableTarget}
 */
public final class NativeExecutableTargetConfiguration {

    private final String cmd;
    private final String[] args;
    private final Map<String, String> env;
    private String workingDirectory;
    private boolean isSubstitutable;
    private ExternalTerminal externalTerminal = null;
    private InputOutput io;
    private boolean x11forwarding;
    private final Map<String, String> info = new ConcurrentHashMap<String, String>();
    private ExecutionEnvironment execEnv;
    private LineConvertorFactory outConvertorFactory;
    private LineConvertorFactory errConvertorFactory;

    /**
     * Creates new configuration for {@link org.netbeans.modules.dlight.api.support.NativeExecutableTarget}
     * @param cmd command line
     * @param args arguments to run
     * @param env enviroment variables as a map &lt;name, value&gt;
     */
    public NativeExecutableTargetConfiguration(String cmd, String[] args, Map<String, String> env) {
        this.cmd = cmd;
        this.args = args;
        this.env = new HashMap<String, String>();
        if (env != null) {
            this.env.putAll(env);
        }
    }

    /**
     * Sets wotking directory
     * @param workingDirectory working directory to start target
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Sets host to run executable target at,
     * if host is not set up, localhost is used
     * @param host host to run target at
     */
    public void setExecutionEnvironment(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    public String putInfo(String name, String value) {
        return info.put(name, value);
    }

    /**
     * Enables or disables X11 forwarding.
     *
     * @param forwarding  pass <code>true</code> to enable X11 forwarding,
     *      or <code>false</code> to disable
     */
    public void setX11Forwarding(boolean forwarding) {
        this.x11forwarding = forwarding;
    }

    /*
     * use it to specify Output line convertor.
     * Will NOT be used in case external terminal is defined!
     */
    public void setOutConvertorFactory(LineConvertorFactory lineConvertorFactory) {
        this.outConvertorFactory = lineConvertorFactory;
    }

    LineConvertorFactory getOutConvertorFactory() {
        return outConvertorFactory;
    }

    /*
     * use it to specify Error line convertor.
     * Will NOT be used in case external terminal is defined!
     */
    public void setErrConvertorFactory(LineConvertorFactory lineConvertorFactory) {
        this.errConvertorFactory = lineConvertorFactory;
    }

    LineConvertorFactory getErrConvertorFactory() {
        return errConvertorFactory;
    }

    /**
     * use it to specify if you would like to run Target in external terminal
     * <p>
     * @param terminal terminal specification
     */
    public void useExternalTerminal(/*@NullAllowed*/ExternalTerminal terminal) {
        this.externalTerminal = terminal;
    }

    public void setIO(InputOutput io) {
        this.io = io;
    }

    InputOutput getIO() {
        return io;
    }

    /**
     *
     * @param isSubstitutable
     */
    public void setSubstitutable(boolean isSubstitutable) {
        this.isSubstitutable = isSubstitutable;
    }

    ExecutionEnvironment getExecutionEvnitoment() {
        return execEnv == null ? ExecutionEnvironmentFactory.getLocal() : execEnv;
    }

    ExternalTerminal getExternalTerminal() {
        return externalTerminal;
    }

    String getCmd() {
        return cmd;
    }

    String[] getArgs() {
        return args;
    }

    Map<String, String> getEnv() {
        return env;
    }

    String getWorkingDirectory() {
        return workingDirectory;
    }

    boolean getSubstitutable() {
        return isSubstitutable;
    }

    Map<String, String> getInfo() {
        return info;
    }

    boolean getX11Forwarding() {
        return x11forwarding;
    }
}
