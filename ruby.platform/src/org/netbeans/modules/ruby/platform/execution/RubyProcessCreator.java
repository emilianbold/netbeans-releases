/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.platform.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.ruby.platform.spi.RubyDebuggerImplementation;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * A helper class for migrating from the ruby execution API to the new execution API (extexecution).
 * Contains a lot of copy-pasted code from <code>RubyExecution</code> (which will eventually
 * be removed).
 *
 * @author Erno Mononen
 */
public final class RubyProcessCreator implements Callable<Process> {

    private static final Logger LOGGER = Logger.getLogger(RubyProcessCreator.class.getName());
    
    /** When not set (the default) do stdio syncing for native Ruby binaries */
    private static final boolean SYNC_RUBY_STDIO = System.getProperty("ruby.no.sync-stdio") == null; // NOI18N


    /** When not set (the default) bypass the JRuby launcher unix/ba-file scripts and launch VM directly */
    private static final boolean LAUNCH_JRUBY_SCRIPT = System.getProperty("ruby.use.jruby.script") != null; // NOI18N
    
    private final RubyExecutionDescriptor descriptor;
    private final String charsetName;

    public RubyProcessCreator(RubyExecutionDescriptor descriptor) {
        this(descriptor, descriptor.getEncoding());
    }

    public RubyProcessCreator(RubyExecutionDescriptor descriptor,
            String charsetName) {

        if (descriptor.getCmd() == null) {
            descriptor.cmd(descriptor.getPlatform().getInterpreterFile());
        }

        descriptor.addBinPath(true);
        this.descriptor = descriptor;
        this.charsetName = charsetName;
    }

    public boolean isAbleToCreateProcess() {
        if (descriptor.debug) {
            RubyDebuggerImplementation debugger = Lookup.getDefault().lookup(RubyDebuggerImplementation.class);
            if (debugger == null) {
                LOGGER.severe("RubyDebuggerImplementation implementation is not available"); // NOI18N
                return false;
            }
            debugger.describeProcess(descriptor);
            return debugger.prepare();
        }
        return true;
    }

    @Override
    public Process call() throws Exception {
        if (descriptor.debug) {
            RubyDebuggerImplementation debugger = Lookup.getDefault().lookup(RubyDebuggerImplementation.class);
            if (debugger == null) {
                throw new IllegalStateException("RubyDebuggerImplementation implementation is not available."); // NOI18N
            }
            debugger.describeProcess(descriptor);
            if (!debugger.prepare()) {
                throw new IllegalStateException("Cannot prepare application to debug. Should be checked before."); // NOI18N
            }
            return debugger.debug();
        }
        ExternalProcessBuilder builder = null;
        List<? extends String> args = buildArgs();
        if (!descriptor.cmd.getName().startsWith("jruby") || LAUNCH_JRUBY_SCRIPT) { // NOI18N
            builder = new ExternalProcessBuilder(descriptor.cmd.getPath());
        } else {
            builder = new ExternalProcessBuilder(args.get(0));
            args.remove(0);
        }

        for (String arg : args) {
            if (arg != null) {
                builder = builder.addArgument(arg);
            }
        }
        if (descriptor.getPwd() != null) {
            builder = builder.workingDirectory(descriptor.getPwd());
        }
        for (Entry<String, String> entry : descriptor.getAdditionalEnvironment().entrySet()) {
            builder = builder.addEnvironmentVariable(entry.getKey(), entry.getValue());
        }

        return builder.call();
    }

    /**
     * Retruns list of default arguments and options from the descriptor's
     * <code>initialArgs</code>, <code>script</code> and
     * <code>additionalArgs</code> in that order.
     */
    private List<? extends String> getCommonArgs() {
        List<String> argvList = new ArrayList<String>();
        File cmd = descriptor.cmd;
        assert cmd != null;

        if (descriptor.getInitialArgs() != null) {
            argvList.addAll(Arrays.asList(descriptor.getInitialArgs()));
        }

        if (descriptor.getScriptPrefix() != null) {
            argvList.add(descriptor.getScriptPrefix());
        }

        if (descriptor.script != null) {
            argvList.add(descriptor.script);
        }

        if (descriptor.getAdditionalArgs() != null) {
            argvList.addAll(Arrays.asList(descriptor.getAdditionalArgs()));
        }
        return argvList;
    }

    protected List<? extends String> buildArgs() {
        List<String> argvList = new ArrayList<String>();
        String rubyHome = descriptor.getCmd().getParentFile().getParent();
        String cmdName = descriptor.getCmd().getName();
        if (descriptor.isRunThroughRuby()) {
            argvList.addAll(ExecutionUtils.getRubyArgs(rubyHome, cmdName, descriptor, charsetName));
        }
        argvList.addAll(getCommonArgs());
        return argvList;
    }

}
