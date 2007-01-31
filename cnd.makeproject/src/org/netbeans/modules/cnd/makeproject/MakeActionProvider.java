/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Set;
import javax.swing.JButton;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.RunDialogPanel;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.ui.utils.ConfSelectorPanel;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerRootNodeProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Action provider of the Make project. This is the place where to do
 * strange things to Make actions. E.g. compile-single.
 */
public class MakeActionProvider implements ActionProvider {
    // Commands available from Make project
    public static final String COMMAND_BATCH_BUILD = "batch_build"; // NOI18N
    public static final String COMMAND_DEBUG_LOAD_ONLY = "debug.load.only"; // NOI18N
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DEBUG_LOAD_ONLY,
        COMMAND_DEBUG_SINGLE,
        COMMAND_BATCH_BUILD,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,

        
    };
    
    // Project
    MakeProject project;
    
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    
    // Project Descriptor
    ConfigurationDescriptor projectDescriptor = null;
    
    /** Map from commands to ant targets */
    Map/*<String,String[]>*/ commands;
    Map/*<String,String[]>*/ commandsNoBuild;
    
    public MakeActionProvider( MakeProject project, AntProjectHelper antProjectHelper ) {
        
        commands = new HashMap();
        commands.put(COMMAND_BUILD, new String[] {"save", "build"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"save", "clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"save", "clean", "build"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"save", "build", "run"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"save", "build", "debug"}); // NOI18N
        commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"save", "build", "debug-stepinto"}); // NOI18N
        commands.put(COMMAND_DEBUG_LOAD_ONLY, new String[] {"save", "build", "debug-load-only"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"save", "compile-single"}); // NOI18N
        commandsNoBuild = new HashMap();
        commandsNoBuild.put(COMMAND_BUILD, new String[] {"save", "build"}); // NOI18N
        commandsNoBuild.put(COMMAND_CLEAN, new String[] {"save", "clean"}); // NOI18N
        commandsNoBuild.put(COMMAND_REBUILD, new String[] {"save", "clean", "build"}); // NOI18N
        commandsNoBuild.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commandsNoBuild.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commandsNoBuild.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N
        commandsNoBuild.put(COMMAND_DEBUG_LOAD_ONLY, new String[] {"debug-load-only"}); // NOI18N
        
        this.antProjectHelper = antProjectHelper;
        this.project = project;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    private MakeConfigurationDescriptor getProjectDescriptor() {
        if (projectDescriptor == null) {
            ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
            projectDescriptor = pdp.getConfigurationDescriptor();
        }
        return (MakeConfigurationDescriptor)projectDescriptor;
    }
    
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    public void invokeAction( String command, Lookup context) throws IllegalArgumentException {
        // Basic info
        ProjectInformation info = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class );
        String projectName = info.getDisplayName();
        MakeConfigurationDescriptor pd = getProjectDescriptor();
        MakeConfiguration conf = (MakeConfiguration)pd.getConfs().getActive();
        
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }

        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }

        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }

        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }
        
        // Add actions to do
        Vector actionEvents = new Vector();
        if (command.equals(COMMAND_BATCH_BUILD)) {
            BatchConfigurationSelector batchConfigurationSelector = new BatchConfigurationSelector(pd.getConfs().getConfs());
            String batchCommand = batchConfigurationSelector.getCommand();
            Configuration[] confs = batchConfigurationSelector.getSelectedConfs();
            if (batchCommand != null && confs != null) {
                for (int i = 0; i < confs.length; i++)
                    addAction(actionEvents, projectName, pd, (MakeConfiguration)confs[i], batchCommand, context);
            } else {
                // Close button
                return;
            }
        } else {
            addAction(actionEvents, projectName, pd, conf, command, context);
        }
        
        // Execute actions
        if (actionEvents.size() > 0)
        ProjectActionSupport.fireActionPerformed((ProjectActionEvent[])actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]));
    }
    
    class BatchConfigurationSelector implements ActionListener {
        private JButton buildButton = new JButton(getString("BuildButton"));
        private JButton rebuildButton = new JButton(getString("CleanBuildButton"));
        private JButton cleanButton = new JButton(getString("CleanButton"));
        private JButton closeButton = new JButton(getString("CloseButton"));
        private ConfSelectorPanel confSelectorPanel;
        private String command = null;
        private Dialog dialog = null;
        
        BatchConfigurationSelector(Configuration[] confs) {
            confSelectorPanel = new ConfSelectorPanel(getString("CheckLabel"), getString("CheckLabelMn").charAt(0), confs, new JButton[] {buildButton, rebuildButton, cleanButton});
            
            buildButton.setMnemonic(getString("BuildButtonMn").charAt(0));
            buildButton.getAccessibleContext().setAccessibleDescription(getString("BuildButtonAD"));
            buildButton.addActionListener(this);
            rebuildButton.setMnemonic(getString("CleanBuildButtonMn").charAt(0));
            rebuildButton.addActionListener(this);
            rebuildButton.getAccessibleContext().setAccessibleDescription(getString("CleanBuildButtonAD"));
            cleanButton.setMnemonic(getString("CleanButtonMn").charAt(0));
            cleanButton.addActionListener(this);
            cleanButton.getAccessibleContext().setAccessibleDescription(getString("CleanButtonAD"));
            closeButton.getAccessibleContext().setAccessibleDescription(getString("CloseButtonAD"));
            // Show the dialog
            DialogDescriptor dd = new DialogDescriptor(confSelectorPanel, getString("BatchBuildTitle"), true, new Object[] {closeButton}, closeButton, 0, null, null);
            //DialogDisplayer.getDefault().notify(dd);
            dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.getAccessibleContext().setAccessibleDescription(getString("BatchBuildDialogAD"));
            dialog.setVisible(true);
        }
        
        public Configuration[] getSelectedConfs() {
            return confSelectorPanel.getSelectedConfs();
        }
        
        public String getCommand() {
            return command;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == buildButton)
                command = COMMAND_BUILD;
            else if (evt.getSource() == rebuildButton)
                command = COMMAND_REBUILD;
            else if (evt.getSource() == cleanButton)
                command = COMMAND_CLEAN;
            else
                assert false;
            dialog.dispose();
        }
    }
    
    public void addAction(Vector actionEvents, String projectName, MakeConfigurationDescriptor pd, MakeConfiguration conf, String command, Lookup context) throws IllegalArgumentException {
        String[] targetNames;
        
        
        targetNames = getTargetNames(command, context);
        if (targetNames == null) {
            return;
        }
        if (targetNames.length == 0) {
            targetNames = null;
        }
        
        for (int i = 0; i < targetNames.length; i++) {
            String targetName = targetNames[i];
            int actionEvent;
            if (targetName.equals("build")) // NOI18N
                actionEvent = ProjectActionEvent.BUILD;
            else if (targetName.equals("clean")) // NOI18N
                actionEvent = ProjectActionEvent.CLEAN;
            else if (targetName.equals("compile-single")) // NOI18N
                actionEvent = ProjectActionEvent.BUILD;
            else if (targetName.equals("run")) // NOI18N
                actionEvent = ProjectActionEvent.RUN;
            else if (targetName.equals("run-single")) // NOI18N
                actionEvent = ProjectActionEvent.RUN;
            else if (targetName.equals("debug")) // NOI18N
                actionEvent = ProjectActionEvent.DEBUG;
            else if (targetName.equals("debug-stepinto")) // NOI18N
                actionEvent = ProjectActionEvent.DEBUG_STEPINTO;
            else if (targetName.equals("debug-load-only")) // NOI18N
                actionEvent = ProjectActionEvent.DEBUG_LOAD_ONLY;
            else {
                // All others
                actionEvent = ProjectActionEvent.RUN;
            }
            
            if (targetName.equals("save")) { // NOI18N
                // Save all files and projects
                if (MakeOptions.getInstance().getSave())
                    LifecycleManager.getDefault().saveAll();
                if (!ProjectSupport.saveAllProjects(getString("NeedToSaveAllText"))) // NOI18N
                    return;
            } else if (targetName.equals("run") || targetName.equals("debug") || targetName.equals("debug-stepinto") || targetName.equals("debug-load-only")) { // NOI18N
                if (conf.isMakefileConfiguration()) {
                    String path;
                    if (targetName.equals("run")) { // NOI18N
                        // naturalize if relative
                        path = conf.getMakefileConfiguration().getOutput().getValue();
                        if (path.length() > 0 && !IpeUtils.isPathAbsolute(path)) {
                            // make path relative to run working directory
                            path = conf.getMakefileConfiguration().getAbsOutput();
                            path = FilePathAdaptor.naturalize(path);
                            path = IpeUtils.toRelativePath(conf.getProfile().getRunDirectory(), path);
                            path = FilePathAdaptor.naturalize(path);
                        }
                    } else {
                        // Always absolute
                        path = conf.getMakefileConfiguration().getAbsOutput();
                    }
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionEvent,
                            projectName + " (" + targetName + ")", // NOI18N
                            path,
                            conf,
                            null,
                            false);
                    actionEvents.add(projectActionEvent);
                    RunDialogPanel.addElementToExecutablePicklist(path);
                } else if (conf.isLibraryConfiguration()) {
                    // Should never get here...
                    return;
                } else if (conf.isCompileConfiguration()) {
                    RunProfile runProfile = null;
                    // On windows we need to add paths to dynamic libraries from subprojects to PATH
                    if (Platforms.getPlatform(conf.getPlatform().getValue()).getId() == Platform.PLATFORM_WINDOWS) {
                        runProfile = conf.getProfile().cloneProfile();
                        Set subProjectOutputLocations = conf.getSubProjectOutputLocations();
                        String path = ""; // NOI18N
                        Iterator iter = subProjectOutputLocations.iterator();
                        while (iter.hasNext()) {
                            String location = FilePathAdaptor.naturalize((String)iter.next());
                            path = location + ";" + path; // NOI18N
                        }
                        if (System.getProperty("Env-PATH") != null) { // IZ 77324 // NOI18N
                            path = path + ";" + System.getProperty("Env-PATH"); // NOI18N
                        }
                        if (!path.equals("")) { // NOI18N
                            runProfile.getEnvironment().putenv("PATH", path); // NOI18N
                        // } else { // no need to set empty path
                        }
                    }
                    
                    MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                    String path;
                    if (targetName.equals("run")) { // NOI18N
                        // naturalize if relative
                        path = makeArtifact.getOutput();
                        if (!IpeUtils.isPathAbsolute(path)) {
                            // make path relative to run working directory
                            path = makeArtifact.getWorkingDirectory() + "/" + path; // NOI18N
                            path = FilePathAdaptor.naturalize(path);
                            path = IpeUtils.toRelativePath(conf.getProfile().getRunDirectory(), path);
                            path = FilePathAdaptor.naturalize(path);
                        }
                    } else {
                        // Always absolute
                        path = IpeUtils.toAbsolutePath(conf.getBaseDir(), makeArtifact.getOutput());
                    }
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionEvent,
                            projectName + " (" + targetName + ")", // NOI18N
                            path,
                            conf,
                            runProfile,
                            false);
                    actionEvents.add(projectActionEvent);
                    RunDialogPanel.addElementToExecutablePicklist(path);
                } else {
                    assert false;
                }
            } else if (targetName.equals("run-single") || targetName.equals("debug-single")) { // NOI18N
                // FIXUP: not sure this is used...
                if (conf.isMakefileConfiguration()) {
                    Iterator it = context.lookup(new Lookup.Template(DataObject.class)).allInstances().iterator();
                    DataObject d = (DataObject)it.next();
                    String path = FileUtil.toFile(d.getPrimaryFile()).getPath();
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                            project,
                            actionEvent,
                            projectName + " (" + "run" + ")", // NOI18N
                            path,
                            conf,
                            null,
                            false);
                    actionEvents.add(projectActionEvent);
                    RunDialogPanel.addElementToExecutablePicklist(path);
                } else {
                    assert false;
                }
            } else if (targetName.equals("build")) { // NOI18N
                MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                String buildCommand = makeArtifact.getBuildCommand(MakeOptions.getInstance().getMakeCommand(), "");  // NOI18N
                String args = ""; // NOI18N
                int index = buildCommand.indexOf(' '); // NOI18N
                if (index > 0) {
                    args = buildCommand.substring(index+1);
                    buildCommand = buildCommand.substring(0, index);
                }
                RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory());
                profile.setArgs(args);
                ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                        project,
                        actionEvent,
                        projectName + " (" + targetName + ")", // NOI18N
                        buildCommand,
                        null,
                        profile,
                        true);
                actionEvents.add(projectActionEvent);
            } else if (targetName.equals("clean")) { // NOI18N
                MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                String buildCommand = makeArtifact.getCleanCommand(MakeOptions.getInstance().getMakeCommand(), ""); // NOI18N
                String args = ""; // NOI18N
                int index = buildCommand.indexOf(' '); // NOI18N
                if (index > 0) {
                    args = buildCommand.substring(index+1);
                    buildCommand = buildCommand.substring(0, index);
                }
                RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory());
                profile.setArgs(args);
                ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                        project,
                        actionEvent,
                        projectName + " (" + targetName + ")", // NOI18N
                        buildCommand,
                        null,
                        profile,
                        true);
                actionEvents.add(projectActionEvent);
            } else if (targetName.equals("compile-single")) { // NOI18N
                Iterator it = context.lookup(new Lookup.Template(Node.class)).allInstances().iterator();
                while (it.hasNext()) {
                    Node node = (Node)it.next();
                    Item item = getNoteItem(node); // NOI18N
                    if (item == null)
                        return;
                    ItemConfiguration itemConfiguration = (ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(item.getPath()));
                    if (itemConfiguration == null)
                        return;
                    if (itemConfiguration.getExcluded().getValue())
                        return;;
                        if (itemConfiguration.getTool() == Tool.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified())
                            return;
                        MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                        String outputFile = null;
                        if (itemConfiguration.getTool() == Tool.CCompiler) {
                            CCompilerConfiguration cCompilerConfiguration = itemConfiguration.getCCompilerConfiguration();
                            outputFile = cCompilerConfiguration.getOutputFile(item.getPath(true), conf);
                        } else if (itemConfiguration.getTool() == Tool.CCCompiler) {
                            CCCompilerConfiguration ccCompilerConfiguration = itemConfiguration.getCCCompilerConfiguration();
                            outputFile = ccCompilerConfiguration.getOutputFile(item.getPath(true), conf);
                        } else if (itemConfiguration.getTool() == Tool.FortranCompiler) {
                            FortranCompilerConfiguration fortranCompilerConfiguration = itemConfiguration.getFortranCompilerConfiguration();
                            outputFile = fortranCompilerConfiguration.getOutputFile(item.getPath(true), conf);
                        } else if (itemConfiguration.getTool() == Tool.CustomTool) {
                            CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                            outputFile = customToolConfiguration.getOutputs().getValue();
                        }
                        // Clean command
                        String commandLine = "rm -rf " + outputFile; // NOI18N
                        String args = ""; // NOI18N
                        int index = commandLine.indexOf(' '); // NOI18N
                        if (index > 0) {
                            args = commandLine.substring(index+1);
                            commandLine = commandLine.substring(0, index);
                        }
                        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory());
                        profile.setArgs(args);
                        ProjectActionEvent projectActionEvent = new ProjectActionEvent(
                                project,
                                ProjectActionEvent.CLEAN,
                                projectName + " (" + "clean" + ")", // NOI18N
                                commandLine,
                                null,
                                profile,
                                true);
                        actionEvents.add(projectActionEvent);
                        // Build commandLine
                        commandLine = MakeOptions.getInstance().getMakeCommand() + " -f nbproject" + '/' + "Makefile-" + conf.getName() + ".mk " + outputFile; // Unix path // NOI18N
                        args = ""; // NOI18N
                        index = commandLine.indexOf(' '); // NOI18N
                        if (index > 0) {
                            args = commandLine.substring(index+1);
                            commandLine = commandLine.substring(0, index);
                        }
                        // Add the build commandLine
                        profile = new RunProfile(makeArtifact.getWorkingDirectory());
                        profile.setArgs(args);
                        projectActionEvent = new ProjectActionEvent(
                                project,
                                actionEvent,
                                projectName + " (" + targetName + ")", // NOI18N
                                commandLine,
                                null,
                                profile,
                                true);
                        actionEvents.add(projectActionEvent);
                }
            }
        }
    }
    
    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    String[] getTargetNames(String command, Lookup context) throws IllegalArgumentException {
        String[] targetNames = new String[0];
        if ( command.equals( COMMAND_COMPILE_SINGLE ) ) {
            targetNames = (String[])commands.get(command);
        } else if (command.equals(COMMAND_RUN) || command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_STEP_INTO)|| command.equals(COMMAND_DEBUG_LOAD_ONLY)) {
            ConfigurationDescriptor pd = getProjectDescriptor();
            MakeConfiguration conf = (MakeConfiguration)pd.getConfs().getActive();
            RunProfile profile = (RunProfile) conf.getAuxObject(RunProfile.PROFILE_ID);
            if (profile == null) // See IZ 89349
                return null;
            if (profile.getBuildFirst())
                targetNames = (String[])commands.get(command);
            else
                targetNames = (String[])commandsNoBuild.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else if (command.equals(COMMAND_RUN_SINGLE) || command.equals(COMMAND_DEBUG_SINGLE)) {
            targetNames = (String[])commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else {
            targetNames = (String[])commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }
        return targetNames;
    }
    
    
    public boolean isActionEnabled( String command, Lookup context ) {
        if (getProjectDescriptor() == null)
            return false;
        if (!(getProjectDescriptor().getConfs().getActive() instanceof MakeConfiguration))
            return false;
        MakeConfiguration conf = (MakeConfiguration)getProjectDescriptor().getConfs().getActive();
        if (command.equals(COMMAND_CLEAN)) {
            return true;
        } else if (command.equals(COMMAND_BUILD)) {
            return true;
        } else if (command.equals(COMMAND_BATCH_BUILD)) {
            return true;
        } else if (command.equals(COMMAND_REBUILD)) {
            return true;
        } else if (command.equals(COMMAND_RUN)) {
            return !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG)) {
            return hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG_STEP_INTO)) {
            return hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG_LOAD_ONLY)) {
            return hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_COMPILE_SINGLE)) {
            boolean enabled = true;
            Iterator it = context.lookup(new Lookup.Template(Node.class)).allInstances().iterator();
            while (it.hasNext()) {
                Node node = (Node)it.next();
                Item item = getNoteItem(node);
                if (item == null)
                    return false;
                ItemConfiguration itemConfiguration = (ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(item.getPath()));
                if (itemConfiguration == null)
                    return false;
                if (itemConfiguration.getExcluded().getValue())
                    return false;;
                    if (itemConfiguration.getTool() == Tool.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified())
                        return false;
            }
            return enabled;
        } else if (command.equals(COMMAND_DELETE) ||
                command.equals(COMMAND_COPY) ||
                command.equals(COMMAND_MOVE) ||
                command.equals(COMMAND_RENAME)) {
            return true;
        } else {
            return false;
        }
    }
    
    private Item getNoteItem(Node node) {
        Item item = (Item) node.getValue("Item"); // NOI18N
        if (item == null) {
            // try to find Item in associated data object if any
            try {
                File file = FileUtil.toFile(((DataObject)node.getCookie(DataObject.class)).getPrimaryFile());
                item = getProjectDescriptor().findItemByFile(file);
            } catch (NullPointerException ex) {
                // not found item
            }
        }  
        return item;
    }
    
    private static boolean hasDebugger() {
        return CustomizerRootNodeProvider.getInstance().getCustomizerNode("Debug") != null; // NOI18N
    }
    
    // Private methods -----------------------------------------------------
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(MakeActionProvider.class);
        }
        return bundle.getString(s);
    }
}
