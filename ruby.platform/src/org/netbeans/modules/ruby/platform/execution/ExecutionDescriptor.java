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
package org.netbeans.modules.ruby.platform.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * An ExecutionDescriptor describes a program to be executed, the arguments
 * and environment to be used, as well as preferences such as whether the
 * running process should be in the background (no progress bar), and so on.
 *
 * @author Tor Norbye
 */
public class ExecutionDescriptor {
    
    File pwd;
    File cmd;
    boolean addBinPath;
    boolean inputVisible;
    private String displayName;
    Runnable postBuildAction;

    private final RubyPlatform platform;
    private FileLocator fileLocator;
    String script;
    private Map<String, String> additionalEnv;
    private String[] additionalArgs;
    private String initialArgs;
    private String jrubyProps;
    private FileObject fileObject;
    private String classPath;
    boolean showProgress = true;
    boolean showSuspended;
    boolean frontWindow = true;
    boolean debug;
    private boolean fastDebugRequired;
    private boolean appendJdkToPath;
    List<OutputRecognizer> outputRecognizers = new ArrayList<OutputRecognizer>();

    public ExecutionDescriptor(final RubyPlatform platform) {
        this(platform, null, null);
    }

    public ExecutionDescriptor(final RubyPlatform platform, final String displayName, final File pwd) {
        this(platform, displayName, pwd, null);
    }
    
    public ExecutionDescriptor(final RubyPlatform platform, final String displayName, final File pwd, final String script) {
        this.platform = platform;
        this.displayName = displayName;
        this.pwd = pwd;
        this.script = script;
        assert (pwd == null) || pwd.isDirectory() : pwd + " is a directory";
        if (platform.hasRubyGemsInstalled()) {
            Map<String, String> env = new HashMap<String, String>();
            GemManager.adjustEnvironment(platform, env);
            addAdditionalEnv(env);
        }
        if (platform.isJRuby()) {
            Map<String, String> env = new HashMap<String, String>();
            String home = platform.getHome().getAbsolutePath();
            env.put("JRUBY_HOME", home); // NOI18N
            env.put("JRUBY_BASE", home); // NOI18N
            env.put("JAVA_HOME", RubyExecution.getJavaHome()); // NOI18N
            addAdditionalEnv(env);
        }
    }
    
    public ExecutionDescriptor cmd(final File cmd) {
        this.cmd = cmd;
        assert (cmd != null) && cmd.isFile() : cmd + " must be a file";
        return this;
    }

    public ExecutionDescriptor postBuild(Runnable postBuildAction) {
        this.postBuildAction = postBuildAction;
        return this;
    }

    public ExecutionDescriptor fileLocator(FileLocator fileLocator) {
        this.fileLocator = fileLocator;
        return this;
    }

    /** Set FileObject associated with this execution (typically the source file).
     * This is not injected in the argument list in any way, but for example
     * the Rerun action will get disabled if this file is deleted.
     */
    public ExecutionDescriptor fileObject(FileObject fileObject) {
        this.fileObject = fileObject;
        return this;
    }

    public ExecutionDescriptor addStandardRecognizers() {
        outputRecognizers.addAll(RubyExecution.getStandardRubyRecognizers());
        return this;
    }

    public ExecutionDescriptor addOutputRecognizer(OutputRecognizer recognizer) {
        outputRecognizers.add(recognizer);
        return this;
    }

    public ExecutionDescriptor allowInput() {
        this.inputVisible = true;
        return this;
    }

    public ExecutionDescriptor showProgress(boolean showProgress) {
        this.showProgress = showProgress;
        return this;
    }

    public ExecutionDescriptor showSuspended(boolean showSuspended) {
        this.showSuspended = showSuspended;
        return this;
    }

    /**
     * Arguments that will be appended <em>AFTER</em> the target. Usually
     * arguments and options to the Ruby script (target, application, ..)
     * itself.
     */
    public ExecutionDescriptor additionalArgs(final String... additionalArgs) {
        this.additionalArgs = additionalArgs;
        return this;
    }

    /**
     * Arguments that will be parsed and prepended <em>BEFORE</em> the target.
     * Usually arguments and options for the Ruby interpreter.
     */
    public ExecutionDescriptor initialArgs(String initialArgs) {
        this.initialArgs = initialArgs;
        return this;
    }
    
    public ExecutionDescriptor jrubyProperties(final String jrubyProps) {
        this.jrubyProps = jrubyProps;
        return this;
    }

    public ExecutionDescriptor addBinPath(boolean addBinPath) {
        this.addBinPath = addBinPath;
        return this;
    }
    
    public ExecutionDescriptor frontWindow(boolean frontWindow) {
        this.frontWindow = frontWindow;
        return this;
    }
    
    public ExecutionDescriptor debug(boolean debug) {
        this.debug = debug;
        return this;
    }
    
    public ExecutionDescriptor fastDebugRequired(boolean fastDebugRequired) {
        this.fastDebugRequired = fastDebugRequired;
        return this;
    }

    /**
     * Builder property which sets whether the JDK should be added to the PATH
     * for the executed process. The default is false. If it is set, it will be
     * added at the end of the PATH, so any existing JDKs on the PATH will take
     * precedence.
     * @param addJdkToPath Whether the JDK should be appended to the path.
     */
    public ExecutionDescriptor appendJdkToPath(boolean appendJdkToPath) {
        this.appendJdkToPath = appendJdkToPath;
        return this;
    }

    /** Extra class path to be used in case the execution process is a VM */
    public ExecutionDescriptor classPath(String classPath) {
        this.classPath = classPath;
        return this;
    }

    String getDisplayName() {
        return debug ? displayName + " (debug)" : displayName; // NOI18N
    }

    public RubyPlatform getPlatform() {
        return platform;
    }
    
    public File getCmd() {
        return cmd;
    }
    
    public String getScript() {
        return script;
    }
    
    /**
     * Arguments to be appended <em>AFTER</em> the target. Usually arguments and
     * options to the Ruby script (target, application, ..) itself.
     */
    public String[] getAdditionalArgs() {
        return additionalArgs;
    }
    
    /**
     * Arguments to be prepended <em>BEFORE</em> the target. Usually arguments
     * and options for the Ruby interpreter.
     */
    public String[] getInitialArgs() {
        return initialArgs == null ? null : Utilities.parseParameters(initialArgs);
    }
    
    /** Properties to be passed to the JVM running the JRuby process. */
    public String[] getJRubyProps() {
        return jrubyProps == null ? null : Utilities.parseParameters(jrubyProps);
    }
    
    public File getPwd() {
        return pwd;
    }

    public boolean isFastDebugRequired() {
        return fastDebugRequired;
    }
    
    public String getClassPath() {
        return classPath;
    }

    public FileLocator getFileLocator() {
        return fileLocator;
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }
    
    /**
     * Should the JDK be appended to the PATH?
     * @return True iff the JDK should be appended to the PATH.
     */
    public boolean getAppendJdkToPath() {
        return appendJdkToPath;
    }

    public void addAdditionalEnv(Map<String, String> additionalEnv) {
        if (this.additionalEnv == null) {
            this.additionalEnv = new HashMap<String, String>();
        }
        this.additionalEnv.putAll(additionalEnv);
    }

    public Map<String, String> getAdditionalEnvironment() {
        return additionalEnv;
    }
}
