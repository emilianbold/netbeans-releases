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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.ExecutionService;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Class which handles gem interactions - executing gem, installing, uninstalling, etc.
 *
 * @todo Use the new ExecutionService to do process management.
 *
 * @author Tor Norbye
 */
public final class GemManager {

    /** Share over invocations of the dialog since these are slow to compute */
    private static List<Gem> installed;
    
    /** Share over invocations of the dialog since these are ESPECIALLY slow to compute */
    private static List<Gem> available;
    
    private static String getGemMissingMessage() {
        if (Utilities.isMac() && "/usr/bin/ruby".equals(RubyInstallation.getInstance().getRuby())) { // NOI18N
            String version = System.getProperty("os.version"); // NOI18N
            if (version == null || version.startsWith("10.4")) { // Only a problem on Tiger // NOI18N
                return NbBundle.getMessage(GemAction.class, "GemMissingMac");
            }
        }
        return NbBundle.getMessage(GemAction.class, "GemMissing");
    }
    
    /**
     * Return null if there are no problems running gem. Otherwise return
     * an error message which describes the problem.
     */
    public static String getGemProblem() {
        String gem = RubyInstallation.getInstance().getGem();
        
        if (gem == null) {
            return getGemMissingMessage();
        }
        
        String gemDirPath = RubyInstallation.getInstance().getRubyLibGemDir();
        if (gemDirPath == null) {
            // edge case, misconfiguration? gem tool is installed but repository is not found
            return NbBundle.getMessage(GemAction.class, "CannotFindGemRepository");
        }

        File gemDir = new File(gemDirPath);
        
        if (!gemDir.exists()) {
            // Is this possible? (Installing gems, but no gems installed yet
            return null;
        }
        
        if (!gemDir.canWrite()) {
            return NbBundle.getMessage(GemAction.class, "GemNotWritable");
        }
        
        return null;
    }
    
    /**
     * Checks whether a gem with the given name is installed in the gem
     * repository used by the currently set Ruby interpreter.
     *
     * @param gemName name of a gem to be checked
     * @return <tt>true</tt> if installed; <tt>false</tt> otherwise
     */
    public static boolean isGemInstalled(final String gemName) {
        return RubyInstallation.getInstance().getVersion(gemName) != null;
    }
    
    /**
     * Checks whether a gem with the given name and the given version is
     * installed in the gem repository used by the currently set Ruby
     * interpreter.
     *
     * @param gemName name of a gem to be checked
     * @param version version of the gem to be checked
     * @return <tt>true</tt> if installed; <tt>false</tt> otherwise
     */
    public static boolean isGemInstalled(final String gemName, final String version) {
        String currVersion = RubyInstallation.getInstance().getVersion(gemName);
        return currVersion != null && RubyInstallation.compareGemVersions(version, currVersion) <= 0;
    }

    public List<String> reload() {
        List<String> errors = new ArrayList<String>(500);
        installed = new ArrayList<Gem>();
        available = new ArrayList<Gem>();
        refreshList(installed, available, errors);
        return errors;
    }
    
    public List<Gem> getInstalledGems() {
        return installed;
    }
    
    /**
     * <em>WARNING:</em> Slow! Synchronous gem execution.
     * 
     * @param errors list to which the errors, which happen during gems
     *        reload, will be accumulated 
     */
    public List<Gem> reloadInstalledGems(List<String> errors) {
        installed = new ArrayList<Gem>(40);
        refreshList(installed, null, errors);
        return installed;
    }
    
    public boolean haveGem() {
        return RubyInstallation.getInstance().getGem() != null;
    }
    
    public List<Gem> getAvailableGems() {
        return available;
    }
    
    /** WARNING: Slow! Synchronous gem execution. */
    public List<Gem> reloadAvailableGems(List<String> lines) {
        available = new ArrayList<Gem>(300);
        refreshList(null, available, lines);
        return available;
    }
    
    public boolean hasUptodateAvailableList() {
        return available != null;
    }

    /**
     * @param errors list to which the errors, which happen during gems
     *        reload, will be accumulated 
     */
    private void refreshList(final List<Gem> localList, final List<Gem> remoteList, final List<String> errors) {
        if (localList != null) {
            localList.clear();
        }
        if (remoteList != null) {
            remoteList.clear();
        }
        
        // Install the given gem
        List<String> argList = new ArrayList<String>();
        
        if (localList != null && remoteList != null) {
            argList.add("--both"); // NOI18N
        } else if (localList != null) {
            argList.add("--local"); // NOI18N
        } else {
            assert remoteList != null;
            argList.add("--remote"); // NOI18N
        }
        
        String[] args = argList.toArray(new String[argList.size()]);
        List<String> lines = new ArrayList<String>(3000);
        boolean ok = gemRunner("list", null, false, null, null, null, null, null, lines, args); // NOI18N
        
        if (ok) {
            parseGemList(lines, localList, remoteList);
            
            // Sort the list
            if (localList != null) {
                Collections.sort(localList);
            }
            if (remoteList != null) {
                Collections.sort(remoteList);
            }
        } else {
            // Produce the error list
            boolean inErrors = false;
            for (String line : lines) {
                if (inErrors) {
                    errors.add(line);
                } else if (line.startsWith("***") || line.startsWith(" ") || line.trim().length() == 0) { // NOI18N
                    continue;
                } else if (!line.matches("[a-zA-Z\\-]+ \\(([0-9., ])+\\)\\s?")) { // NOI18N
                    errors.add(line);
                    inErrors = true;
                }
            }
        }
    }
    
    private void parseGemList(List<String> lines, List<Gem> localList, List<Gem> remoteList) { 
        Gem gem = null;
        boolean listStarted = false;
        boolean inLocal = false;
        boolean inRemote = false;
        
        for (String line : lines) {
            if (line.length() == 0) {
                gem = null;
                
                continue;
            }
            
            if (line.startsWith("*** REMOTE GEMS")) { // NOI18N
                inRemote = true;
                inLocal = false;
                listStarted = true;
                gem = null;
                continue;
            } else if (line.startsWith("*** LOCAL GEMS")) { // NOI18N
                inRemote = false;
                inLocal = true;
                listStarted = true;
                gem = null;
                continue;
            }
            
            if (!listStarted) {
                // Skip status messages etc.
                continue;
            }
            
            if (Character.isWhitespace(line.charAt(0))) {
                if (gem != null) {
                    String description = line.trim();
                    
                    if (gem.getDescription() == null) {
                        gem.setDescription(description);
                    } else {
                        gem.setDescription(gem.getDescription() + " " + description); // NOI18N
                    }
                }
            } else {
                if (line.charAt(0) == '.') {
                    continue;
                }
                
                // Should be a gem - but could be an error message!
                int versionIndex = line.indexOf('(');
                
                if (versionIndex != -1) {
                    String name = line.substring(0, versionIndex).trim();
                    int endIndex = line.indexOf(')');
                    String versions;
                    
                    if (endIndex != -1) {
                        versions = line.substring(versionIndex + 1, endIndex);
                    } else {
                        versions = line.substring(versionIndex);
                    }
                    
                    gem = new Gem(name, inLocal ? versions : null, inLocal ? null : versions);
                    if (inLocal) {
                        localList.add(gem);
                    } else {
                        assert inRemote;
                        remoteList.add(gem);
                    }
                } else {
                    gem = null;
                }
            }
        }
    }
    
    /** Non-blocking gem executor which also provides progress UI etc. */
    private void asynchGemRunner(final Component parent, final String description,
            final String successMessage, final String failureMessage, final List<String> lines,
            final Runnable successCompletionTask, final String command, final String... commandArgs) {
        final Cursor originalCursor;
        if (parent != null) {
            originalCursor = parent.getCursor();
            Cursor busy = Utilities.createProgressCursor(parent);
            parent.setCursor(busy);
        } else {
            originalCursor = null;
        }
        
        final ProgressHandle progressHandle = null;
        final boolean interactive = true;
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
                            gemRunner(command, progressHandle, interactive, description,
                            successMessage, failureMessage, progress, processHolder, lines,
                            commandArgs);
                    
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
    
    private boolean gemRunner(String command, ProgressHandle progressHandle, boolean interactive,
            String description, String successMessage, String failureMessage,
            GemProgressPanel progressPanel, Process[] processHolder, List<String> lines,
            String... commandArgs) {
        String gemProblem = GemManager.getGemProblem();
        if (gemProblem != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(gemProblem, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return false;
        }
        
        // Install the given gem
        String gemCmd = RubyInstallation.getInstance().getGem();
        List<String> argList = new ArrayList<String>();
        
        File cmd = new File(RubyInstallation.getInstance().getRuby());
        
        if (!cmd.getName().startsWith("jruby") || RubyExecution.LAUNCH_JRUBY_SCRIPT) { // NOI18N
            argList.add(cmd.getPath());
        }
        
        String rubyHome = cmd.getParentFile().getParent();
        String cmdName = cmd.getName();
        argList.addAll(RubyExecution.getRubyArgs(rubyHome, cmdName));
        
        argList.add(gemCmd);
        argList.add(command);
        
        for (String arg : commandArgs) {
            argList.add(arg);
        }
        
        String[] args = argList.toArray(new String[argList.size()]);
        ProcessBuilder pb = new ProcessBuilder(args);
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
        new RubyExecution(new ExecutionDescriptor("gem", pb.directory()).cmd(cmd)).setupProcessEnvironment(pb.environment()); // NOI18N
        
        if (lines == null) {
            lines = new ArrayList<String>(40);
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
                    
                    lines.add(line);
                }
            } catch (IOException ioe) {
                // When we cancel we call Process.destroy which may quite possibly
                // raise an IO Exception in this thread reading text out of the
                // process. Silently ignore that.
                String message = "*** Gem Process Killed ***\n"; // NOI18N
                lines.add(message);
                
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
                        
                        lines.add(line);
                    }
                } catch (IOException ioe) {
                    // When we cancel we call Process.destroy which may quite possibly
                    // raise an IO Exception in this thread reading text out of the
                    // process. Silently ignore that.
                    String message = "*** Gem Process Killed ***\n"; // NOI18N
                    lines.add(message);
                    
                    if (progressPanel != null) {
                        progressPanel.appendOutput(message);
                    }
                }
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        } catch (InterruptedException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        boolean succeeded = exitCode == 0;
        
        return succeeded;
    }
    
    /**
     * Install the latest version of the given gem with dependencies and refresh
     * IDE caches accordingly after the gem is installed.
     *
     * @param gem gem to install
     * @param rdoc if true, generate rdoc as part of the installation
     * @param ri if true, generate ri data as part of the installation
     */
    public void installGem(final String gem, final boolean rdoc, final boolean ri) {
        final Gem[] gems = new Gem[] {
            new Gem(gem, null, null)
        };
        Runnable installationComplete = new Runnable() {
            public void run() {
                RubyInstallation.getInstance().recomputeRoots();
            }
        };
        new GemManager().install(gems, null, null, rdoc, ri, null, true, true, installationComplete);
    }

    /**
     * Install the given gems.
     *
     * @param gem Gem description for the gem to be installed. Only the name is relevant.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     * @param rdoc If true, generate rdoc as part of the installation
     * @param ri If true, generate ri data as part of the installation
     * @param version If non null, install the specified version rather than the latest available version
     */
    public boolean install(Gem[] gems, Component parent, ProgressHandle progressHandle,
            boolean rdoc, boolean ri, String version, boolean includeDeps, boolean asynchronous,
            Runnable asyncCompletionTask) {
        // Install the given gem
        List<String> argList = new ArrayList<String>();
        
        for (Gem gem : gems) {
            argList.add(gem.getName());
        }
        
        //argList.add("--verbose"); // NOI18N
        if (!rdoc) {
            argList.add("--no-rdoc"); // NOI18N
        }
        
        if (!ri) {
            argList.add("--no-ri"); // NOI18N
        }
        
        if (includeDeps) {
            argList.add("--include-dependencies"); // NOI18N
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
        
        String title = NbBundle.getMessage(GemManager.class, "Installation");
        String success = NbBundle.getMessage(GemManager.class, "InstallationOk");
        String failure = NbBundle.getMessage(GemManager.class, "InstallationFailed");
        String gemCmd = "install"; // NOI18N
        
        if (asynchronous) {
            asynchGemRunner(parent, title, success, failure, null, asyncCompletionTask, gemCmd, args);
            
            return false;
        } else {
            boolean ok =
                    gemRunner(gemCmd, progressHandle, true, title, success, failure, null, null, null,
                    args);
            
            return ok;
        }
    }
    
    /**
     * Uninstall the given gem.
     *
     * @param gem Gem description for the gem to be uninstalled. Only the name is relevant.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     */
    public boolean uninstall(Gem[] gems, Component parent, ProgressHandle progressHandle,
            boolean asynchronous, Runnable asyncCompletionTask) {
        // Install the given gem
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
        String title = NbBundle.getMessage(GemManager.class, "Uninstallation");
        String success = NbBundle.getMessage(GemManager.class, "UninstallationOk");
        String failure = NbBundle.getMessage(GemManager.class, "UninstallationFailed");
        String gemCmd = "uninstall"; // NOI18N
        
        if (asynchronous) {
            for (Gem gem : gems) {
                args[nameIndex] = gem.getName();
                asynchGemRunner(parent, title, success, failure, null, asyncCompletionTask, gemCmd,
                        args);
            }
            
            return false;
        } else {
            boolean ok = true;
            
            for (Gem gem : gems) {
                args[nameIndex] = gem.getName();
                
                if (!gemRunner(gemCmd, progressHandle, true, title, success, failure, null, null,
                        null, args)) {
                    ok = false;
                }
            }
            
            return ok;
        }
    }
    
    /**
     * Update the given gem, or all gems if gem == null
     *
     * @param gem Gem description for the gem to be uninstalled. Only the name is relevant. If null, all installed gems
     *    will be updated.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     */
    public boolean update(Gem[] gems, Component parent, ProgressHandle progressHandle,
            boolean rdoc, boolean ri, boolean asynchronous, Runnable asyncCompletionTask) {
        // Install the given gem
        List<String> argList = new ArrayList<String>();
        
        if (gems != null) {
            for (Gem gem : gems) {
                argList.add(gem.getName());
            }
        }
        
        argList.add("--verbose"); // NOI18N
        
        if (!rdoc) {
            argList.add("--no-rdoc"); // NOI18N
        }
        
        if (!ri) {
            argList.add("--no-ri"); // NOI18N
        }
        
        argList.add("--include-dependencies"); // NOI18N
        
        String[] args = argList.toArray(new String[argList.size()]);
        
        String title = NbBundle.getMessage(GemManager.class, "Update");
        String success = NbBundle.getMessage(GemManager.class, "UpdateOk");
        String failure = NbBundle.getMessage(GemManager.class, "UpdateFailed");
        String gemCmd = "update"; // NOI18N
        
        if (asynchronous) {
            asynchGemRunner(parent, title, success, failure, null, asyncCompletionTask, gemCmd, args);
            
            return false;
        } else {
            boolean ok =
                    gemRunner(gemCmd, progressHandle, true, title, success, failure, null, null, null,
                    args);
            
            return ok;
        }
    }
    
}
