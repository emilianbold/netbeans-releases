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

package org.netbeans.modules.ruby.debugger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.debugger.Util.FastDebugInstallationResult;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpointManager;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.platform.spi.RubyDebuggerImplementation;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.rubyforge.debugcommons.RubyDebuggerFactory;
import org.rubyforge.debugcommons.RubyDebuggerException;
import org.rubyforge.debugcommons.RubyDebuggerProxy;

import static org.netbeans.modules.ruby.debugger.Util.FastDebugInstallationResult.*;

/**
 * Implementation of {@link RubyDebuggerImplementation} SPI, providing an entry
 * to point to the Ruby debugging.
 */
public final class RubyDebugger implements RubyDebuggerImplementation {
    
    private static final String PATH_TO_CLASSIC_DEBUG_DIR;
    
    private ExecutionDescriptor descriptor;
    
    private RubySession rubySession;
    
    static {
        String path = "ruby/debug-commons-0.9.5/classic-debug.rb"; // NOI18N
        File classicDebug = InstalledFileLocator.getDefault().locate(
                path, "org.netbeans.modules.ruby.debugger", false); // NOI18N
        if (classicDebug == null || !classicDebug.isFile()) {
            throw new IllegalStateException("Cannot locate classic debugger in NetBeans Ruby cluster (" + path + ')'); // NOI18N
        }
        PATH_TO_CLASSIC_DEBUG_DIR = classicDebug.getParentFile().getAbsolutePath();
    }
    
    public void describeProcess(ExecutionDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    public boolean canDebug() {
        return !descriptor.getPlatform().isRubinius();
    }
    
    public Process debug() {
        Process p = null;
        try {
            rubySession = startDebugging(descriptor);
            if (rubySession != null) {
                rubySession.getProxy().startDebugging(RubyBreakpointManager.getBreakpoints());
                p = rubySession.getProxy().getDebugTarged().getProcess();
            }
        } catch (IOException e) {
            getFinishAction().run();
            problemOccurred(e);
        } catch (RubyDebuggerException e) {
            getFinishAction().run();
            problemOccurred(e);
        }
        return p;
    }

    /** @see RubyDebuggerImplementation#getFinishAction */
    public Runnable getFinishAction() {
        return new Runnable() {
            public void run() {
                if (rubySession != null) { // #131563
                    rubySession.getActionProvider().doAction(ActionsManager.ACTION_KILL);
                    rubySession = null;
                }
            }
        };
    }

    private static void problemOccurred(final Exception e) {
        Util.showWarning(NbBundle.getMessage(RubyDebugger.class, "RubyDebugger.startup.problem", e.getMessage()));
    }
    
    /**
     * Starts debugging of the given script using the given interpreter.
     *
     * @param descriptor description of the process to be debugged
     * @return debugger {@link java.lang.Process process}. Might be
     *         <tt>null</tt> if debugging cannot be started for some reason.
     *         E.g. interpreter cannot be obtained from preferences.
     * @throws java.io.IOException
     * @throws org.rubyforge.debugcommons.RubyDebuggerException
     */
    static RubySession startDebugging(final ExecutionDescriptor descriptor)
            throws IOException, RubyDebuggerException {
        final RubyPlatform platform = descriptor.getPlatform();
        boolean jrubySet = platform.isJRuby();

        if (!checkAndTuneSettings(descriptor)) {
            return null;
        }
        
        RubyDebuggerFactory.Descriptor debugDesc = new RubyDebuggerFactory.Descriptor();
        debugDesc.useDefaultPort(false);
        debugDesc.setJRuby(jrubySet);
        debugDesc.setScriptPath(descriptor.getScript());
        
        if(descriptor.useInterpreter()) {
            List<String> additionalOptions = new ArrayList<String>();
            if (descriptor.getInitialArgs() != null) {
                additionalOptions.addAll(Arrays.asList(descriptor.getInitialArgs()));
            }
            if (jrubySet && descriptor.getJVMArguments() != null) {
                for (String jvmArg : descriptor.getJVMArguments()) {
                    additionalOptions.add("-J" + jvmArg); // NOI18N
                }
            }
            if (!additionalOptions.isEmpty()) {
                debugDesc.setAdditionalOptions(additionalOptions);
            }
        }
        
        debugDesc.setScriptArguments(descriptor.getAdditionalArgs());
        debugDesc.setSynchronizedOutput(true);
        if (descriptor.getPwd() != null) {
            debugDesc.setBaseDirectory(descriptor.getPwd());
        }
        Map<String, String> env = new HashMap<String, String>();
        GemManager.adjustEnvironment(platform, env);
        if (descriptor.getAdditionalEnvironment() != null) {
            env.putAll(descriptor.getAdditionalEnvironment());
        }
        if (jrubySet) {
            env.putAll(getJRubyEnvironment(descriptor));
        }
        debugDesc.setEnvironment(env);
        RubyDebuggerProxy proxy;
        int timeout = Integer.getInteger("org.netbeans.modules.ruby.debugger.timeout", 15); // NOI18N
        Util.finer("Using timeout: " + timeout + 's'); // NOI18N
        String interpreter = platform.getInterpreter();
        if (!platform.hasFastDebuggerInstalled()) {
            Util.LOGGER.fine("Running classic(slow) debugger...");
            proxy = RubyDebuggerFactory.startClassicDebugger(debugDesc,
                    PATH_TO_CLASSIC_DEBUG_DIR, interpreter, timeout);
        } else { // ruby-debug
            String version = platform.getLatestAvailableValidRDebugIDEVersions();
            debugDesc.setRubyDebugIDEVersion(version);
            Util.LOGGER.fine("Running fast debugger...");
            File rDebugF = new File(Util.findRDebugExecutable(platform));
            
            if(descriptor.useInterpreter()) {
                proxy = RubyDebuggerFactory.startRubyDebug(debugDesc,
                        rDebugF.getAbsolutePath(), interpreter, timeout);
            } else {
                List<String> cmd = new ArrayList<String>(20);
                cmd.add(descriptor.getCmd().getAbsolutePath());
                cmd.addAll(Arrays.asList(descriptor.getInitialArgs()));
                proxy = RubyDebuggerFactory.startRubyDebug(
                        debugDesc, cmd, rDebugF.getAbsolutePath(), timeout);
            }
        }
        
        return intializeIDEDebuggerEngine(proxy, descriptor.getFileLocator());
    }

    private static Map<String, String> getJRubyEnvironment(final ExecutionDescriptor descriptor) {
        Map<String, String> env = new HashMap<String, String>();
        if (descriptor.getClassPath() != null) {
            env.put("CLASSPATH", descriptor.getClassPath()); // NOI18N
        }
        return env;
    }

    /** Package private for unit test. */
    static boolean checkAndTuneSettings(final ExecutionDescriptor descriptor) {
        final RubyPlatform platform = descriptor.getPlatform();
        if (platform.isRubinius()) { // no debugger support for Rubinius yet
            return false;
        }
        assert platform.isValid() : platform + " is a valid platform";

        boolean jrubySet = platform.isJRuby();
        
        boolean fastDebuggerRequired = descriptor.isFastDebugRequired();

        // Offers to install only for fast native Ruby debugger. Installation
        // does not work for jruby ruby-debug-base yet.
        if (!jrubySet) {
            FastDebugInstallationResult result = Util.offerToInstallFastDebugger(platform);
            if (result == CANCELLED || result == FAILED) {
                return false;
            }
        }
        
        if (fastDebuggerRequired) { // NOI18N
            FastDebugInstallationResult result = Util.ensureRubyDebuggerIsPresent(
                    platform, true, "RubyDebugger.wrong.fast.debugger.required"); // NOI18N
            if (result != INSTALLED) {
                if (jrubySet) {
                    Util.showMessage(NbBundle.getMessage(RubyDebugger.class,
                            "RubyDebugger.instructionsToInstallJRubyDebugger", // NOI18N
                            platform.getFastDebuggerProblemsInHTML()));
                }
                return false;
            }
        }

        if (platform.hasFastDebuggerInstalled()) {
            FastDebugInstallationResult result = Util.ensureRubyDebuggerIsPresent(
                    platform, true, "RubyDebugger.requiredMessage"); // NOI18N
            if (result != INSTALLED) {
                return false;
            }
            String rDebugPath = Util.findRDebugExecutable(platform);
            if (rDebugPath == null) {
                Util.showMessage(NbBundle.getMessage(RubyDebugger.class,
                        "RubyDebugger.wrong.rdebug-ide", // NOI18N
                        platform.getInfo().getLongDescription(),
                        platform.getInterpreter(),
                        Util.rdebugPattern()));
                return false;
            }
        }
        return true;
    }
    
    private static RubySession intializeIDEDebuggerEngine(final RubyDebuggerProxy proxy, final FileLocator fileLocator) {
        RubySession rubySession = new RubySession(proxy, fileLocator);
        SessionProvider sp = rubySession.createSessionProvider();
        DebuggerInfo di = DebuggerInfo.create(
                "RubyDebuggerInfo", new Object[] { sp, rubySession }); // NOI18N
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        DebuggerEngine[] de = dm.startDebugging(di);
        assert de.length == 1 : "one debugger engine";
        Session session = de[0].lookupFirst(null, Session.class);
        assert session != null : "non-null Session in the lookup";
        rubySession.setSession(session);
        
        RubyDebuggerActionProvider provider =
                de[0].lookupFirst(null, RubyDebuggerActionProvider.class);
        assert provider != null;
        rubySession.setActionProvider(provider);
        proxy.addRubyDebugEventListener(provider);
        return rubySession;
    }
    
}
