/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.railsprojects.plugins;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 * Class which handles plugin interactions - executing plugin, installing, uninstalling, etc.
 *
 * @todo Use the new ExecutionService to do process manapluginent.
 * @todo Consolidate with the GemManager
 *
 * @author Tor Norbye
 */
public class PluginManager {

    private RailsProject project;
    /** Share over invocations of the dialog since these are slow to compute */
    private List<Plugin> installed;
    
    /** Share over invocations of the dialog since these are ESPECIALLY slow to compute */
    private static List<Plugin> available;
    private static List<Plugin> cachedAvailable;
    
    public PluginManager(RailsProject project) {
        this.project = project;
    }

    private FileObject getPluginDir() {
        FileObject pluginDirPath = project.getProjectDirectory().getFileObject("vendor/plugins"); // NOI18N
        
        return pluginDirPath;
    }
    /**
     * Return null if there are no problems running plugin. Otherwise return
     * an error message which describes the problem.
     */
    public String getPluginProblem() {
        FileObject pluginDirPath = getPluginDir();
        
        if (pluginDirPath == null) {
            // edge case, misconfiguration? plugin tool is installed but repository is not found
            return NbBundle.getMessage(PluginAction.class, "CannotFindPluginRepository");
        }
        
        File pluginDir = FileUtil.toFile(pluginDirPath);
        
        if (!pluginDir.canWrite()) {
            return NbBundle.getMessage(PluginAction.class, "PluginNotWritable");
        }
        
        return null;
    }
    
    /**
     * Checks whether a plugin with the given name is installed in the plugin
     * repository used by the current Rails project
     *
     * @param pluginName name of a plugin to be checked
     * @return <tt>true</tt> if installed; <tt>false</tt> otherwise
     */
    public boolean isPluginInstalled(final String pluginName) {
        FileObject dir = getPluginDir();
        return dir != null && dir.getFileObject(pluginName) != null;
    }
    
    /** WARNING: slow call! Synchronous plugin execution (unless refresh==false)! */
    public List<Plugin> getInstalledPlugins(boolean refresh, String sourceRepository, List<String> lines) {
        if (refresh || (installed == null) || (installed.size() == 0)) {
            installed = new ArrayList<Plugin>(40);
            refreshList(installed, sourceRepository, true, lines);
        }
        
        return installed;
    }
    
    /** WARNING: slow call! Synchronous plugin execution! */
    public List<Plugin> getAvailablePlugins(boolean refresh, String sourceRepository, List<String> lines) {
        if (refresh || (available == null) || (available.size() == 0)) {
            available = new ArrayList<Plugin>(300);
            refreshList(available, sourceRepository, false, lines);
            
            if (available.size() > 1) {
                updateCachedList(lines);
            }
        }
        
        return available;
    }
    
    public boolean hasUptodateAvailableList() {
        // Turned off caching
        //return available != null;
        return false;
    }
    
    public List<Plugin> getCachedAvailablePlugins() {
        // Turned off
        if (true) {
            return null;
        }

        if (available != null) {
            return available;
        }
        
        if (cachedAvailable != null) {
            return cachedAvailable;
        }
        
        cachedAvailable = getCachedList();
        
        return cachedAvailable;
    }
    
    private void refreshList(final List<Plugin> list, String sourceRepository, final boolean local, final List<String> lines) {
        list.clear();
        
        // Install the given plugin
        List<String> argList = new ArrayList<String>();
        
        if (sourceRepository != null) {
            argList.add("-s"); // NOI18N
            argList.add(sourceRepository);
        }
        
        if (local) {
            argList.add("--local"); // NOI18N
        } else {
            argList.add("--remote"); // NOI18N
        }
        
        String[] args = argList.toArray(new String[argList.size()]);
        boolean ok = pluginRunner("list", null, false, null, null, null, null, null, lines, args); // NOI18N
        
        if (ok) {
            parsePluginList(lines, list, local);
            
            // Sort the list
            Collections.sort(list);
        }
    }
    
    private void parsePluginList(List<String> lines, List<Plugin> pluginList, boolean local) {
        Plugin plugin = null;
        boolean listStarted = false;
        
        for (String line : lines) {
            // Pretty simple format - lines simply have the name on the left, and optionally
            // a repository on the right.
            // However, with the verbose flag there's more output. Even though I'm not
            // using --verbose right now, prepare for it in case it's being picked up by
            // user configuration files etc.           
            if (line.trim().length() == 0 || line.startsWith("/") || line.startsWith("Discovering plugins in ")) { // NOI18N
                continue;
            }

            StringBuilder sb = new StringBuilder();
            int i = 0;
            int length = line.length();
            for (; i < length; i++) {
                char c = line.charAt(i);
                if (Character.isWhitespace(c)) {
                    break;
                }
                sb.append(c);
            }
            String name = sb.toString();
            for (; i < length; i++) {
                if (Character.isWhitespace(line.charAt(i))) {
                    break;
                }
            }
            // Skip whitespace
            while (i < length && Character.isWhitespace(line.charAt(i))) {
                i++;
            }
            String repository = null;
            if (i < length) {
                sb = new StringBuilder();
                for (; i < length; i++) {
                    char c = line.charAt(i);
                    if (Character.isWhitespace(c)) {
                        break;
                    }
                    sb.append(c);
                }
                if (sb.length() > 0) {
                    repository = sb.toString();
                }
            }
            
            plugin = new Plugin(name, repository);
            pluginList.add(plugin);
        }
    }
    
    /** Non-blocking plugin executor which also provides progress UI etc. */
    private void asynchPluginRunner(final JComponent parent, final String description,
            final String successMessage, final String failureMessage, final List<String> lines,
            final Runnable successCompletionTask, final String command, final String... commandArgs) {
        final Cursor originalCursor = parent.getCursor();
        Cursor busy = Utilities.createProgressCursor(parent);
        parent.setCursor(busy);
        
        final ProgressHandle progressHandle = null;
        final boolean interactive = true;
        final JButton closeButton = new JButton(NbBundle.getMessage(PluginManager.class, "CTL_Close"));
        final JButton cancelButton =
                new JButton(NbBundle.getMessage(PluginManager.class, "CTL_Cancel"));
        closeButton.getAccessibleContext()
                .setAccessibleDescription(NbBundle.getMessage(PluginManager.class, "AD_Close"));
        
        Object[] options = new Object[] { closeButton, cancelButton };
        closeButton.setEnabled(false);
        
        final PluginProgressPanel progress =
                new PluginProgressPanel(NbBundle.getMessage(PluginManager.class, "PluginPleaseWait"));
        DialogDescriptor descriptor =
                new DialogDescriptor(progress, description, true, options, closeButton,
                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(PluginPanel.class), null); // NOI18N
        descriptor.setModal(true);
        
        final Process[] processHolder = new Process[1];
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                dlg.setVisible(false);
                dlg.dispose();
                parent.setCursor(originalCursor);
            }
        });
        
        Runnable runner =
                new Runnable() {
            public void run() {
                try {
                    boolean succeeded =
                            pluginRunner(command, progressHandle, interactive, description,
                            successMessage, failureMessage, progress, processHolder, lines,
                            commandArgs);
                    
                    closeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    
                    progress.done(succeeded ? successMessage : failureMessage);
                    
                    if (succeeded && (successCompletionTask != null)) {
                        successCompletionTask.run();
                    }
                } finally {
                    parent.setCursor(originalCursor);
                }
            }
        };
        
        RequestProcessor.getDefault().post(runner, 50);
        
        dlg.setVisible(true);
        
        if ((descriptor.getValue() == DialogDescriptor.CANCEL_OPTION) ||
                (descriptor.getValue() == cancelButton)) {
            parent.setCursor(originalCursor);
            cancelButton.setEnabled(false);
            
            Process process = processHolder[0];
            
            if (process != null) {
                process.destroy();
                dlg.setVisible(false);
                dlg.dispose();
            }
        }
    }
    
    private boolean pluginRunner(String command, ProgressHandle progressHandle, boolean interactive,
            String description, String successMessage, String failureMessage,
            PluginProgressPanel progressPanel, Process[] processHolder, List<String> lines,
            String... commandArgs) {
        // Install the given plugin
        String pluginCmd = "script" + File.separator + "plugin"; // NOI18N
        
        List<String> argList = new ArrayList<String>();
        
        File cmd = new File(RubyInstallation.getInstance().getRuby());
        
        if (!cmd.getName().startsWith("jruby") || RubyExecution.LAUNCH_JRUBY_SCRIPT) { // NOI18N
            argList.add(cmd.getPath());
        }
        
        String rubyHome = cmd.getParentFile().getParent();
        String cmdName = cmd.getName();
        argList.addAll(RubyExecution.getRubyArgs(rubyHome, cmdName));
        
        argList.add(pluginCmd);
        argList.add(command);
        
        for (String arg : commandArgs) {
            argList.add(arg);
        }
        
        String[] args = argList.toArray(new String[argList.size()]);
        ProcessBuilder pb = new ProcessBuilder(args);
        File pwd = FileUtil.toFile(project.getProjectDirectory());
        pb.directory(pwd);
        pb.redirectErrorStream(true);
        
        // PATH additions for JRuby etc.
        Map<String, String> env = pb.environment();
        new RubyExecution(new ExecutionDescriptor("plugin", pb.directory()).cmd(cmd)).setupProcessEnvironment(env); // NOI18N
        
        // Proxy
        String proxy = getNetbeansHttpProxy();
        
        if (proxy != null) {
            // This unfortunately does not work -- plugins blows up. Looks like
            // a RubyPlugins bug.
            // ERROR:  While executing plugin ... (NoMethodError)
            //    undefined method `[]=' for #<Plugin::ConfigFile:0xb6c763 @hash={} ,@args=["--remote", "-p", "http://foo.bar:8080"] ,@config_file_name=nil ,@verbose=true>
            //argList.add("--http-proxy"); // NOI18N
            //argList.add(proxy);
            // (If you uncomment the above, move it up above the args = argList.toArray line)
            //
            // Running plugins list -p or --http-proxy triggers this so for now
            // work around with environment variables instead - which still work
            if ((env.get("HTTP_PROXY") == null) && (env.get("http_proxy") == null)) { // NOI18N
                env.put("HTTP_PROXY", proxy); // NOI18N
            }
            
            // PENDING - what if proxy was null so the user has TURNED off proxies while
            // there is still an environment variable set - should we honor their
            // environment, or honor their NetBeans proxy settings (e.g. unset HTTP_PROXY
            // in the environment before launching plugin?
        }
        
        if (lines == null) {
            lines = new ArrayList<String>(40);
        }
        
        int exitCode = -1;
        
        try {
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
                String message = "*** Plugin Process Killed ***\n"; // NOI18N
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
                    String message = "*** Plugin Process Killed ***\n"; // NOI18N
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
     * Install the given plugin.
     *
     * @param plugin Plugin description for the plugin to be installed. Only the name is relevant.
     * @param parent For asynchronous tasks, provide a parent JComponent that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the plugin task asynchronously - returning immediately and running the plugin task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    plugin output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the plugin task completes normally, this task will be run at the end.
     * @param rdoc If true, generate rdoc as part of the installation
     * @param ri If true, generate ri data as part of the installation
     * @param version If non null, install the specified version rather than the latest available version
     */
    public boolean install(Plugin[] plugins, JComponent parent, ProgressHandle progressHandle,
           String sourceRepository, boolean svnExternals, boolean svnCheckout, String revision, boolean asynchronous,
            Runnable asyncCompletionTask) {
        // Install the given plugin
        List<String> argList = new ArrayList<String>();
        
        if (sourceRepository != null) {
            argList.add("-s"); // NOI18N
            argList.add(sourceRepository);
        }
        
        //argList.add("--verbose"); // NOI18N

        for (Plugin plugin : plugins) {
            argList.add(plugin.getName());
        }
        
        if (svnExternals) {
            argList.add("--externals"); // NOI18N
        } else if (svnCheckout) {
            argList.add("--checkout"); // NOI18N
        }
        
        if (revision != null && (svnExternals || svnCheckout)) {
            argList.add("--revision"); // NOI18N
            argList.add(revision);
        }
        
        String[] args = argList.toArray(new String[argList.size()]);
        
        String title = NbBundle.getMessage(PluginManager.class, "Installation");
        String success = NbBundle.getMessage(PluginManager.class, "InstallationOk");
        String failure = NbBundle.getMessage(PluginManager.class, "InstallationFailed");
        String pluginCmd = "install"; // NOI18N
        
        if (asynchronous) {
            asynchPluginRunner(parent, title, success, failure, null, asyncCompletionTask, pluginCmd, args);
            
            return false;
        } else {
            boolean ok =
                    pluginRunner(pluginCmd, progressHandle, true, title, success, failure, null, null, null,
                    args);
            
            return ok;
        }
    }
    
    /**
     * Uninstall the given plugin.
     *
     * @param plugin Plugin description for the plugin to be uninstalled. Only the name is relevant.
     * @param parent For asynchronous tasks, provide a parent JComponent that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the plugin task asynchronously - returning immediately and running the plugin task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    plugin output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the plugin task completes normally, this task will be run at the end.
     */
    public boolean uninstall(Plugin[] plugins, String sourceRepository, JComponent parent, ProgressHandle progressHandle,
            boolean asynchronous, Runnable asyncCompletionTask) {
        // Install the given plugin
        List<String> argList = new ArrayList<String>();
        
        if (sourceRepository != null) {
            argList.add("-s"); // NOI18N
            argList.add(sourceRepository);
        }
        
        // This string is replaced in the loop below, one gem at a time as we iterate over the
        // deletion results
        int nameIndex = argList.size();
        argList.add("placeholder"); // NOI18N

        //argList.add("--verbose"); // NOI18N
        
        String[] args = argList.toArray(new String[argList.size()]);
        String title = NbBundle.getMessage(PluginManager.class, "Uninstallation");
        String success = NbBundle.getMessage(PluginManager.class, "UninstallationOk");
        String failure = NbBundle.getMessage(PluginManager.class, "UninstallationFailed");
        String pluginCmd = "remove"; // NOI18N
        
        if (asynchronous) {
            for (Plugin plugin : plugins) {
                args[nameIndex] = plugin.getName();
                asynchPluginRunner(parent, title, success, failure, null, asyncCompletionTask, pluginCmd,
                        args);
            }
            
            return false;
        } else {
            boolean ok = true;
            
            for (Plugin plugin : plugins) {
                args[nameIndex] = plugin.getName();
                
                if (!pluginRunner(pluginCmd, progressHandle, true, title, success, failure, null, null,
                        null, args)) {
                    ok = false;
                }
            }
            
            return ok;
        }
    }
    
    /**
     * Update the given plugin, or all plugins if plugin == null
     *
     * @param plugin Plugin description for the plugin to be uninstalled. Only the name is relevant. If null, all installed plugins
     *    will be updated.
     * @param parent For asynchronous tasks, provide a parent JComponent that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the plugin task asynchronously - returning immediately and running the plugin task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    plugin output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the plugin task completes normally, this task will be run at the end.
     */
    public boolean update(Plugin[] plugins, String revision, String sourceRepository, JComponent parent, ProgressHandle progressHandle,
            boolean asynchronous, Runnable asyncCompletionTask) {
        // Install the given plugin
        List<String> argList = new ArrayList<String>();
        
        if (sourceRepository != null) {
            argList.add("-s"); // NOI18N
            argList.add(sourceRepository);
        }
        
        //argList.add("--verbose"); // NOI18N

        // If you specify a revision, only specify a single plugin
        assert revision == null || revision.length() == 0 || plugins.length == 1;
        
        if (plugins != null) {
            for (Plugin plugin : plugins) {
                argList.add(plugin.getName());
            }
        }
        
        if (revision != null) {
            argList.add("--revision"); // NOI18N
            argList.add(revision);
        }
        

        String[] args = argList.toArray(new String[argList.size()]);
        
        String title = NbBundle.getMessage(PluginManager.class, "Update");
        String success = NbBundle.getMessage(PluginManager.class, "UpdateOk");
        String failure = NbBundle.getMessage(PluginManager.class, "UpdateFailed");
        String pluginCmd = "update"; // NOI18N
        
        if (asynchronous) {
            asynchPluginRunner(parent, title, success, failure, null, asyncCompletionTask, pluginCmd, args);
            
            return false;
        } else {
            boolean ok =
                    pluginRunner(pluginCmd, progressHandle, true, title, success, failure, null, null, null,
                    args);
            
            return ok;
        }
    }
    
    public boolean removeRepositories(String[] repositories, JComponent parent, ProgressHandle progressHandle,
            boolean asynchronous, Runnable asyncCompletionTask) {
        return modifyRepositories("unsource", repositories, parent, progressHandle, asynchronous, asyncCompletionTask); // NOI18N
    }

    public boolean addRepositories(String[] repositories, JComponent parent, ProgressHandle progressHandle,
            boolean asynchronous, Runnable asyncCompletionTask) {
        return modifyRepositories("source", repositories, parent, progressHandle, asynchronous, asyncCompletionTask); // NOI18N
    }
    
    public boolean modifyRepositories(String pluginCmd, String[] repositories, JComponent parent, ProgressHandle progressHandle,
            boolean asynchronous, Runnable asyncCompletionTask) {
        // Install the given plugin
        List<String> argList = new ArrayList<String>();
        
        //argList.add("--verbose"); // NOI18N

        // If you specify a revision, only specify a single plugin
        
        for (String repository : repositories) {
            argList.add(repository);
        }
        
        String[] args = argList.toArray(new String[argList.size()]);
        
        String title = NbBundle.getMessage(PluginManager.class, "ModifySource");
        String success = NbBundle.getMessage(PluginManager.class, "ModifySourceOk");
        String failure = NbBundle.getMessage(PluginManager.class, "ModifySourceFailed");
        
        if (asynchronous) {
            asynchPluginRunner(parent, title, success, failure, null, asyncCompletionTask, pluginCmd, args);
            
            return false;
        } else {
            boolean ok =
                    pluginRunner(pluginCmd, progressHandle, true, title, success, failure, null, null, null,
                    args);
            
            return ok;
        }
    }
    
    
    /**
     * Reads property detected by native launcher (core/launcher).
     * Implemented for Windows and GNOME.
     * This was copied from "detectNetbeansHttpProxy in subversion/** /ProxyDescriptor.java.
     */
    private static String getNetbeansHttpProxy() {
        String host = System.getProperty("http.proxyHost"); // NOI18N
        
        if (host == null) {
            return null;
        }
        
        String portHttp = System.getProperty("http.proxyPort"); // NOI18N
        int port;
        
        try {
            port = Integer.parseInt(portHttp);
        } catch (NumberFormatException e) {
            port = 8080;
        }
        
        // Plugin requires "http://" in front of the port name if it's not already there
        if (host.indexOf(':') == -1) {
            host = "http://" + host; // NOI18N
        }
        
        return host + ":" + port; // NOI18N
    }
    
    public List<String> getRepositories(boolean local) {
        return local ? getLocalRepositories() : getRemoteRepositories();
    }

    private List<String> getRemoteRepositories() {
        List<String> lines = new ArrayList<String>(150);
        
        // Install the given plugin
        List<String> argList = new ArrayList<String>();
        
        argList.add("--list"); // NOI18N
        
        String[] args = argList.toArray(new String[argList.size()]);
        boolean ok = pluginRunner("discover", null, false, null, null, null, null, null, lines, args); // NOI18N

        if (ok) {
            return lines;
        } else {
            return Collections.emptyList();
        }
    }
    
    private List<String> getLocalRepositories() {
        List<String> lines = new ArrayList<String>(150);
        
        // Install the given plugin
        List<String> argList = new ArrayList<String>();
        
        //argList.add("--check"); // NOI18N
        
        String[] args = argList.toArray(new String[argList.size()]);
        boolean ok = pluginRunner("sources", null, false, null, null, null, null, null, lines, args); // NOI18N

        if (ok) {
            return lines;
        } else {
            return Collections.emptyList();
        }
    }
    
    private List<Plugin> getCachedList() {
        synchronized (PluginManager.class) {
            BufferedReader is = null;
            
            try {
                File cacheFile = getCacheFile();
                
                if (!cacheFile.exists()) {
                    return null;
                }
                
                List<String> lines = new ArrayList<String>(5000);
                is = new BufferedReader(new FileReader(getCacheFile()));
                
                while (true) {
                    String line = is.readLine();
                    
                    if (line == null) {
                        break;
                    }
                    
                    lines.add(line);
                }
                
                List<Plugin> list = new ArrayList<Plugin>();
                parsePluginList(lines, list, false);
                
                return list;
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                
                return null;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }
    }
    
    private void updateCachedList(List<String> lines) {
        // Disabled for now
        if (true) {
            return;
        }

        synchronized (PluginManager.class) {
            PrintWriter os = null;
            
            try {
                File cacheFile = getCacheFile();
                
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
                
                os = new PrintWriter(new BufferedWriter(new FileWriter(getCacheFile())));
                
                for (String line : lines) {
                    os.println(line);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        }
    }
    
    private static File getCacheFile() {
        return new File(getCacheFolder(), "remotePlugins.txt"); // NOI18N
    }
    
    private static File getCacheFolder() {
        final String nbUserProp = System.getProperty("netbeans.user"); // NOI18N
        assert nbUserProp != null;
        
        final File nbUserDir = new File(nbUserProp);
        File cacheFolder =
                FileUtil.normalizeFile(new File(nbUserDir,
                "var" + File.separator + "cache" + File.separatorChar)); // NOI18N
        
        if (!cacheFolder.exists()) {
            boolean created = cacheFolder.mkdirs();
            assert created : "Cannot create cache folder"; //NOI18N
        } else {
            assert cacheFolder.isDirectory() && cacheFolder.canRead() && cacheFolder.canWrite();
        }
        
        return cacheFolder;
    }
}
