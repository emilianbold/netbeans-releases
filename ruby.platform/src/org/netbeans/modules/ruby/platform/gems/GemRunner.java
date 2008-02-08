/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.ExecutionService;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

final class GemRunner {
    
    private static final Logger LOGGER = Logger.getLogger(GemRunner.class.getName());

    private RubyPlatform platform;
    private ArrayList<String> output;
    
    GemRunner(final RubyPlatform platform) {
        this.platform = platform;
    }

    boolean fetchBoth() {
        return gemRunner("list", null, null, "--both");
    }

    boolean fetchRemote() {
        return gemRunner("list", null, null, "--remote");
    }

    boolean fetchLocal() {
        return gemRunner("list", null, null, "--local");
    }
    
    boolean install(final List<String> gemNames, boolean rdoc, boolean ri, boolean includeDeps,
            String version) {
        return install(gemNames, rdoc, ri, includeDeps, version, null, null);
    }

    boolean installAsynchronously(List<String> gemNames, boolean rdoc, boolean ri,
            boolean includeDeps, String version, Runnable asyncCompletionTask, Component parent) {
        return install(gemNames, rdoc, ri, includeDeps, version, asyncCompletionTask, parent);
    }

    boolean update(final List<String> gemNames, boolean rdoc, boolean ri) {
        return update(gemNames, rdoc, ri, null, null);
    }

    boolean updateAsynchronously(List<String> gemNames, boolean rdoc, boolean ri,
            Runnable asyncCompletionTask, Component parent) {
        return update(gemNames, rdoc, ri, asyncCompletionTask, parent);
    }

    boolean uninstall(final List<String> gemNames) {
        return uninstall(gemNames, null, null);
    }
    
    boolean uninstallAsynchronously(List<String> gemNames, Runnable asyncCompletionTask, Component parent) {
        return uninstall(gemNames, asyncCompletionTask, parent);
    }

    private boolean install(final List<String> gemNames, boolean rdoc, boolean ri, boolean includeDeps,
            String version, Runnable asyncCompletionTask, Component parent) {
        List<String> argList = new ArrayList<String>();

        for (String gemname : gemNames) {
            argList.add(gemname);
        }

        //argList.add("--verbose"); // NOI18N
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
        
        argList.add("--version"); // NOI18N
        
        if ((version != null) && (version.length() > 0)) {
            argList.add(version);
        } else {
            argList.add("> 0"); // NOI18N
        }
        
        String[] args = argList.toArray(new String[argList.size()]);

        String gemCmd = "install"; // NOI18N
        if (asyncCompletionTask != null) {
            String title = NbBundle.getMessage(GemManager.class, "Installation");
            String success = NbBundle.getMessage(GemManager.class, "InstallationOk");
            String failure = NbBundle.getMessage(GemManager.class, "InstallationFailed");
            asynchGemRunner(parent, title, success, failure, asyncCompletionTask, gemCmd, args);
            return false;
        } else {
            return gemRunner(gemCmd, null, null, args);
        }
    }

    private boolean update(final List<String> gemNames, boolean rdoc, boolean ri,
            Runnable asyncCompletionTask, Component parent) {
        List<String> argList = new ArrayList<String>();

        if (gemNames != null) {
            for (String gemName : gemNames) {
                argList.add(gemName);
            }
        }

        argList.add("--verbose"); // NOI18N
        if (!rdoc) {
            argList.add("--no-rdoc"); // NOI18N
        }
        
        if (!ri) {
            argList.add("--no-ri"); // NOI18N
        }
        
        includeDeps(argList);

        String[] args = argList.toArray(new String[argList.size()]);

        String gemCmd = "update"; // NOI18N
        if (asyncCompletionTask != null) {
            String title = NbBundle.getMessage(GemManager.class, "Update");
            String success = NbBundle.getMessage(GemManager.class, "UpdateOk");
            String failure = NbBundle.getMessage(GemManager.class, "UpdateFailed");
            asynchGemRunner(parent, title, success, failure, asyncCompletionTask, gemCmd, args);
            return false;
        } else {
            return gemRunner(gemCmd, null, null, args);
        }
    }

    private boolean uninstall(final List<String> gemNames, Runnable asyncCompletionTask, Component parent) {
        List<String> argList = new ArrayList<String>();

        // This string is replaced in the loop below, one gem at a time as we iterate over the
        // deletion results
        int nameIndex = argList.size();
        argList.add("placeholder"); // NOI18N

        //argList.add("--verbose"); // NOI18N
        argList.add("--all"); // NOI18N
        argList.add("--executables"); // NOI18N
        argList.add("--ignore-dependencies"); // NOI18N

        String[] args = argList.toArray(new String[argList.size()]);
        String gemCmd = "uninstall"; // NOI18N

        if (asyncCompletionTask != null) {
            String title = NbBundle.getMessage(GemManager.class, "Uninstallation");
            String success = NbBundle.getMessage(GemManager.class, "UninstallationOk");
            String failure = NbBundle.getMessage(GemManager.class, "UninstallationFailed");
            for (String gem : gemNames) {
                args[nameIndex] = gem;
                asynchGemRunner(parent, title, success, failure, asyncCompletionTask, gemCmd, args);
            }
            return false;
        } else {
            boolean ok = true;
            for (String gem : gemNames) {
                args[nameIndex] = gem;
                ok = gemRunner(gemCmd, null, null, args);
            }
            return ok;
        }
    }
    
    ArrayList<String> getOutput() {
        return output;
    }

    private void includeDeps(List<String> argList) {
        // -y and --include-dependencies is deprecated since 0.9.5 (and automatic)
        if (GemManager.compareGemVersions(platform.getInfo().getGemVersion(), "0.9.5") < 0) { // NOI18N
            argList.add("--include-dependencies"); // NOI18N
        }
    }

    private boolean gemRunner(String gemCommand, GemProgressPanel progressPanel,
            Process[] processHolder, String... commandArgs) {

        // Install the given gem
        List<String> argList = new ArrayList<String>();
        
        File cmd = new File(platform.getInterpreter());
        
        if (!cmd.getName().startsWith("jruby") || RubyExecution.LAUNCH_JRUBY_SCRIPT) { // NOI18N
            argList.add(cmd.getPath());
        }
        
        argList.addAll(RubyExecution.getRubyArgs(platform));
        
        argList.add(platform.getGemManager().getGemTool());
        argList.add(gemCommand);
        
        for (String arg : commandArgs) {
            argList.add(arg);
        }
        
        String[] args = argList.toArray(new String[argList.size()]);
        ProcessBuilder pb = new ProcessBuilder(args);
        GemManager.adjustEnvironment(platform, pb.environment());
        pb.directory(cmd.getParentFile());
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
        new RubyExecution(new ExecutionDescriptor(platform, "gem", pb.directory()).cmd(cmd)).setupProcessEnvironment(pb.environment()); // NOI18N
        
        if (output == null) {
            output = new ArrayList<String>(40);
        }
        
        int exitCode = -1;
        
        try {
            ExecutionService.logProcess(pb);
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
        
        boolean succeeded = exitCode == 0;
        
        return succeeded;
    }

        /** Non-blocking gem executor which also provides progress UI etc. */
    private void asynchGemRunner(final Component parent, final String description,
            final String successMessage, final String failureMessage,
            final Runnable successCompletionTask, final String gemCommand,
            final String... commandArgs) {
        final Cursor originalCursor;
        if (parent != null) {
            originalCursor = parent.getCursor();
            Cursor busy = Utilities.createProgressCursor(parent);
            parent.setCursor(busy);
        } else {
            originalCursor = null;
        }
        
        final JButton closeButton = new JButton(NbBundle.getMessage(GemManager.class, "CTL_Close"));
        final JButton cancelButton =
                new JButton(NbBundle.getMessage(GemManager.class, "CTL_Cancel"));
        closeButton.getAccessibleContext()
                .setAccessibleDescription(NbBundle.getMessage(GemManager.class, "AD_Close"));
        
        Object[] options = new Object[] { closeButton, cancelButton };
        closeButton.setEnabled(false);
        
        final GemProgressPanel progress =
                new GemProgressPanel(NbBundle.getMessage(GemManager.class, "GemPleaseWait"));
        progress.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(GemManager.class, "GemProgressPanel.AccessibleContext.accessibleDescription"));

        DialogDescriptor descriptor =
                new DialogDescriptor(progress, description, true, options, closeButton,
                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(GemManager.class), null); // NOI18N
        descriptor.setModal(true);
        
        final Process[] processHolder = new Process[1];
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);

        
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                dlg.setVisible(false);
                dlg.dispose();
                if (parent != null) parent.setCursor(originalCursor);
            }
        });
        
        Runnable runner =
                new Runnable() {
            public void run() {
                try {
                    boolean succeeded =
                            gemRunner(gemCommand, progress, processHolder, commandArgs);
                    
                    closeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    
                    progress.done(succeeded ? successMessage : failureMessage);
                    
                    if (succeeded && (successCompletionTask != null)) {
                        successCompletionTask.run();
                    }
                } finally {
                    if (parent != null) parent.setCursor(originalCursor);
                }
            }
        };
        
        RequestProcessor.getDefault().post(runner, 50);
        
        dlg.setVisible(true);
        
        if ((descriptor.getValue() == DialogDescriptor.CANCEL_OPTION) ||
                (descriptor.getValue() == cancelButton)) {
            if (parent != null) parent.setCursor(originalCursor);
            cancelButton.setEnabled(false);
            
            Process process = processHolder[0];
            
            if (process != null) {
                process.destroy();
                dlg.setVisible(false);
                dlg.dispose();
            }
        }
    }
    

}
