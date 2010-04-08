/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.platform.gems;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.execution.ExecutionUtils;
import org.netbeans.modules.ruby.platform.RubyPreferences;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.Sudo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

final class GemRunner {

    private static final Logger LOGGER = Logger.getLogger(GemRunner.class.getName());

    private final RubyPlatform platform;
    private List<String> output;
    private File pwd;

    GemRunner(final RubyPlatform platform) {
        this.platform = platform;
        assert platform.hasRubyGemsInstalled() : "GemRunner cannot be used fro platform without RubyGems: " + platform;
    }

    /**
     * Compound options based on passed <em>custom</em> ones, defaults, and
     * those stored in preferences.
     */
    private String[] getOptions(final String... custom) {
        List<String> options = new ArrayList<String>();
        options.addAll(Arrays.asList(custom));
        if (RubyPreferences.shallFetchGemDescriptions()) {
            options.add("--details"); // NOI18N
        }
        
        if (!platform.getGemManager().hasAncientRubyGemsVersion() &&
                RubyPreferences.shallFetchAllVersions()) {
            options.add("--all"); // NOI18N
        }
        return options.toArray(new String[options.size()]);
    }

    boolean fetchBoth() {
        return runGemTool("list", false, getOptions("--both")); // NOI18N
    }

    boolean fetchRemote() {
        return runGemTool("list", false, getOptions("--remote")); // NOI18N
    }

    boolean fetchLocal() {
        return runGemTool("list", false, getOptions("--local")); // NOI18N
    }

    boolean install(final List<String> gemNames, boolean rdoc, boolean ri, boolean includeDeps,
            String version) {
        return installRemote(gemNames, rdoc, ri, includeDeps, version, null, null);
    }

    boolean installLocal(File gem, boolean rdoc, boolean ri) {
        return installLocal(gem, rdoc, ri, null, null);
    }

    boolean installAsynchronously(List<String> gemNames, boolean rdoc, boolean ri,
            boolean includeDeps, String version, Runnable asyncCompletionTask, Component parent) {
        return installRemote(gemNames, rdoc, ri, includeDeps, version, asyncCompletionTask, parent);
    }

    boolean installLocalAsynchronously(File gem, boolean rdoc, boolean ri,
            Runnable asyncCompletionTask, Component parent) {
        return installLocal(gem, rdoc, ri, asyncCompletionTask, parent);
    }

    boolean update(final List<String> gemNames, boolean rdoc, boolean ri, boolean includeDependencies) {
        return update(gemNames, rdoc, ri, includeDependencies, null, null);
    }

    boolean updateAsynchronously(List<String> gemNames, boolean rdoc, boolean ri, boolean includeDependencies,
            Runnable asyncCompletionTask, Component parent) {
        return update(gemNames, rdoc, ri, includeDependencies, asyncCompletionTask, parent);
    }

    boolean uninstall(final List<GemInstallInfo> gems) {
        return uninstall(gems, null, null);
    }

    boolean uninstallAsynchronously(List<GemInstallInfo> gems, Runnable asyncCompletionTask, Component parent) {
        return uninstall(gems, asyncCompletionTask, parent);
    }

    private boolean install(final List<String> gems, boolean rdoc, boolean ri, boolean includeDeps,
            String version, Runnable asyncCompletionTask, Component parent) {
        List<String> argList = new ArrayList<String>();

        for (String gem : gems) {
            argList.add(gem);
        }

        if (!rdoc) {
            argList.add("--no-rdoc"); // NOI18N
        }

        if (!ri) {
            argList.add("--no-ri"); // NOI18N
        }

        if (includeDeps) {
            includeDeps(argList);
        } else {
            argList.add("--ignore-dependencies"); // NOI18N
        }

        if ((version != null) && (version.length() > 0)) {
            argList.add("--version"); // NOI18N
            argList.add(version);
        }

        String[] args = argList.toArray(new String[argList.size()]);

        String gemCmd = "install"; // NOI18N
        if (asyncCompletionTask != null) {
            String title = NbBundle.getMessage(GemRunner.class, "Installation");
            String success = NbBundle.getMessage(GemRunner.class, "InstallationOk");
            String failure = NbBundle.getMessage(GemRunner.class, "InstallationFailed");
            asynchGemRunner(parent, title, success, failure, asyncCompletionTask, gemCmd, args);
            return false;
        } else {
            return runGemTool(gemCmd, args);
        }
    }

    private boolean installRemote(final List<String> gemNames, boolean rdoc, boolean ri, boolean includeDeps,
            String version, Runnable asyncCompletionTask, Component parent) {
        return install(gemNames, rdoc, ri, includeDeps, version, asyncCompletionTask, parent);
    }

    private boolean installLocal(final File gem, boolean rdoc,
            boolean ri, Runnable asyncCompletionTask, Component parent) {
        // XXX make 'includeDeps' customizable
        this.pwd = gem.getParentFile();
        return install(Collections.singletonList(gem.getName()), rdoc, ri, false, null, asyncCompletionTask, parent);
    }

    private boolean update(final List<String> gemNames, boolean rdoc, boolean ri,
            boolean includeDependencies, Runnable asyncCompletionTask, Component parent) {

        List<String> argList = new ArrayList<String>();

        if (gemNames != null) {
            for (String gemName : gemNames) {
                argList.add(gemName);
            }
        }

        if (RubyPreferences.isGemVerboseOutput()) {
            argList.add("--verbose"); // NOI18N
        }
        
        if (!rdoc) {
            argList.add("--no-rdoc"); // NOI18N
        }

        if (!ri) {
            argList.add("--no-ri"); // NOI18N
        }

        if (includeDependencies) {
            argList.add("--include-dependencies"); //NOI18N
        }

        includeDeps(argList);

        String[] args = argList.toArray(new String[argList.size()]);

        String gemCmd = "update"; // NOI18N
        if (asyncCompletionTask != null) {
            String title = NbBundle.getMessage(GemRunner.class, "Update");
            String success = NbBundle.getMessage(GemRunner.class, "UpdateOk");
            String failure = NbBundle.getMessage(GemRunner.class, "UpdateFailed");
            asynchGemRunner(parent, title, success, failure, asyncCompletionTask, gemCmd, args);
            return false;
        } else {
            return runGemTool(gemCmd, args);
        }
    }

    private static String[] uninstallArgsFor(GemInstallInfo info) {
        List<String> argList = new ArrayList<String>();
        argList.add(info.getName());
        if (info.getVersion() == null) {
            argList.add("--all"); // NOI18N
        } else {
            argList.add("-v " + info.getVersion()); // NOI18N
        }
        argList.add("--executables"); // NOI18N
        if (info.isIgnoreDependencies()) {
            argList.add("--ignore-dependencies"); // NOI18N
        }
        return argList.toArray(new String[argList.size()]);
    }

    private boolean uninstall(final List<GemInstallInfo> gems, Runnable asyncCompletionTask, Component parent) {
        final String gemCmd = "uninstall"; // NOI18N

        if (asyncCompletionTask != null) {
            String title = NbBundle.getMessage(GemRunner.class, "Uninstallation");
            String success = NbBundle.getMessage(GemRunner.class, "UninstallationOk");
            String failure = NbBundle.getMessage(GemRunner.class, "UninstallationFailed");
            for (GemInstallInfo gem : gems) {
                asynchGemRunner(parent, title, success, failure, asyncCompletionTask, gemCmd, uninstallArgsFor(gem));
            }
            return false;
        } else {
            boolean ok = true;
            for (GemInstallInfo gem : gems) {
                ok = runGemTool(gemCmd, uninstallArgsFor(gem));
            }
            return ok;
        }
    }

    List<String> getOutput() {
        return output;
    }

    private void includeDeps(List<String> argList) {
        // -y and --include-dependencies is deprecated since 0.9.5 (and automatic)
        if (Util.compareVersions(platform.getInfo().getGemVersion(), "0.9.5") < 0) { // NOI18N
            argList.add("--include-dependencies"); // NOI18N
        }
    }

    private boolean runGemTool(String gemCommand, boolean needsWrite, String... commandArgs) {
        return runGemTool(gemCommand, needsWrite, null, null, commandArgs);
    }

    private boolean runGemTool(String gemCommand, String... commandArgs) {
        return runGemTool(gemCommand, true, commandArgs);
    }

    /**
     * Runs <em>gem</em> tools.
     *
     * @param gemCommand like, <em>list</em>, <em>update</em>, <em>install</em>, ...
     * @param needsWrite whether the gem tool will need a write access to the
     *        reprository (migth trigger sudo)
     * @param progressPanel {@link GemProgressPanel} isntance
     * @param processHolder will put a {@link Process} instance into the first
     *        element
     * @param commandArgs argument to the <tt>gemCommand</tt>
     * @return whether underlaying <em>gem</em> tool succeeded
     */
    private boolean runGemTool(String gemCommand, boolean needsWrite,
            GemProgressPanel progressPanel, Process[] processHolder,
            String... commandArgs) {

        // Install the given gem
        List<String> argList = new ArrayList<String>();

        File cmd = new File(platform.getInterpreter());

        if (!cmd.getName().startsWith("jruby") || ExecutionUtils.launchJRubyScript()) { // NOI18N
            argList.add(cmd.getPath());
        }

        argList.addAll(ExecutionUtils.getRubyArgs(platform));

        GemManager gemManager = platform.getGemManager();
        assert gemManager != null : "gemManager cannot be null";

        // be sure gem tool finds RubyGems libraries. Seems that sudo tools have
        // problem with inheriting the process environment (RUBYLIB)
        // I've not found any switches to force the RUBYLIB to be inherited.
        File gemToolDir = new File(platform.getGemTool()).getParentFile().getParentFile();
        // always use forward slashes in the load path, even on Win
        argList.add("-I" + gemToolDir + "/" + "lib"); // NOI18N

        argList.add(platform.getGemTool());
        argList.add(gemCommand);

        for (String arg : commandArgs) {
            argList.add(arg);
        }

        if (needsWrite && !gemManager.isGemHomeWritable()) {
            String message = NbBundle.getMessage(GemRunner.class, "GemRunner.message.for.sudo");
            Sudo sudo = new Sudo(argList, message);
            argList = sudo.createCommand();
        }

        String[] args = argList.toArray(new String[argList.size()]);

        ProcessBuilder pb = new ProcessBuilder(args);
        GemManager.adjustEnvironment(platform, pb.environment());
        pb.directory(pwd == null ? cmd.getParentFile() : pwd);
        pb.redirectErrorStream(true);

        // TODO: Following unfortunately does not work -- gems blows up. Looks
        // like a RubyGems bug.
        // ERROR:  While executing gem ... (NoMethodError)
        //    undefined method `[]=' for #<Gem::ConfigFile:0xb6c763 @hash={} ,@args=["--remote", "-p", "http://foo.bar:8080"] ,@config_file_name=nil ,@verbose=true>
        //argList.add("--http-proxy"); // NOI18N
        //argList.add(proxy);
        // (If you uncomment the above, move it up above the args = argList.toArray line)
        Util.adjustProxy(pb);

        // PATH additions for JRuby etc.
        RubyExecutionDescriptor descriptor = new RubyExecutionDescriptor(platform, "gem", pb.directory()).cmd(cmd);
        ExecutionUtils.setupProcessEnvironment(pb.environment(), descriptor.getCmd().getParent(), descriptor.getAppendJdkToPath());

        if (output == null) {
            output = new ArrayList<String>(40);
        }

        int exitCode = -1;

        try {
            ExecutionUtils.logProcess(pb);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            if (processHolder != null) {
                processHolder[0] = process;
            }

            InputStream is = process.getInputStream();

            if (progressPanel != null) {
                progressPanel.setProcessInput(process.getOutputStream());
            }

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            try {
                while (true) {
                    line = br.readLine();

                    if (line == null) {
                        break;
                    }

                    if (progressPanel != null) {
                        // Add "\n" ?
                        progressPanel.appendOutput(line);
                    }

                    output.add(line);
                }
            } catch (IOException ioe) {
                // When we cancel we call Process.destroy which may quite possibly
                // raise an IO Exception in this thread reading text out of the
                // process. Silently ignore that.
                String message = "*** Gem Process Killed ***\n"; // NOI18N
                output.add(message);

                if (progressPanel != null) {
                    progressPanel.appendOutput(message);
                }
            }

            exitCode = process.waitFor();

            if (exitCode != 0) {
                try {
                    // This might not be necessary now that I'm
                    // calling ProcessBuilder.redirectErrorStream(true)
                    // but better safe than sorry
                    is = process.getErrorStream();
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);

                    while ((line = br.readLine()) != null) {
                        if (progressPanel != null) {
                            // Add "\n" ?
                            progressPanel.appendOutput(line);
                        }

                        output.add(line);
                    }
                } catch (IOException ioe) {
                    // When we cancel we call Process.destroy which may quite possibly
                    // raise an IO Exception in this thread reading text out of the
                    // process. Silently ignore that.
                    String message = "*** Gem Process Killed ***\n"; // NOI18N
                    output.add(message);

                    if (progressPanel != null) {
                        progressPanel.appendOutput(message);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        LOGGER.finer("Process finished with exit code: " + exitCode);
        boolean succeeded = exitCode == 0;

        return succeeded;
    }

    /** Non-blocking gem executor which also provides progress UI etc. */
    private void asynchGemRunner(final Component parent, final String description,
            final String successMessage, final String failureMessage,
            final Runnable successCompletionTask, final String gemCommand,
            final String... commandArgs) {
        if (!EventQueue.isDispatchThread()) {
            throw new AssertionError("#asynchGemRunner must be called from EDT");
        }
        
        final Cursor originalCursor;
        if (parent != null) {
            originalCursor = parent.getCursor();
            Cursor busy = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            parent.setCursor(busy);
        } else {
            originalCursor = null;
        }

        final JButton closeButton = new JButton(NbBundle.getMessage(GemRunner.class, "CTL_Close"));
        final JButton cancelButton =
                new JButton(NbBundle.getMessage(GemRunner.class, "CTL_Cancel"));
        closeButton.getAccessibleContext()
                .setAccessibleDescription(NbBundle.getMessage(GemRunner.class, "AD_Close"));

        Object[] options = new Object[] { closeButton, cancelButton };
        closeButton.setEnabled(false);

        final GemProgressPanel progress =
                new GemProgressPanel(NbBundle.getMessage(GemRunner.class, "GemPleaseWait"));
        progress.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(GemRunner.class, "GemProgressPanel.AccessibleContext.accessibleDescription"));

        DialogDescriptor descriptor =
                new DialogDescriptor(progress, description, true, options, closeButton,
                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(GemRunner.class), null); // NOI18N
        descriptor.setModal(true);

        final Process[] processHolder = new Process[1];
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                dlg.setVisible(false);
                dlg.dispose();
                resetCursor(parent, originalCursor);
            }
        });

        Runnable runner = new Runnable() {
            public void run() {
                try {
                    final boolean succeeded =
                            runGemTool(gemCommand, true, progress, processHolder, commandArgs);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            closeButton.setEnabled(true);
                            cancelButton.setEnabled(false);
                            progress.done(succeeded ? successMessage : failureMessage);
                        }
                    });
                    if (succeeded && (successCompletionTask != null)) {
                        successCompletionTask.run();
                    }
                } finally {
                    resetCursor(parent, originalCursor);
                }
            }
        };

        RequestProcessor.getDefault().post(runner, 50);

        dlg.setVisible(true);

        if ((descriptor.getValue() == DialogDescriptor.CANCEL_OPTION) ||
                (descriptor.getValue() == cancelButton)) {
            resetCursor(parent, originalCursor);
            cancelButton.setEnabled(false);

            Process process = processHolder[0];

            if (process != null) {
                process.destroy();
                dlg.setVisible(false);
                dlg.dispose();
            }
        }
    }

    private static void resetCursor(final Component parent, final Cursor originalCursor) {
        if (parent != null) {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        resetCursor(parent, originalCursor);
                    }
                });
                return;
            }
            parent.setCursor(originalCursor);
        }
    }
}
