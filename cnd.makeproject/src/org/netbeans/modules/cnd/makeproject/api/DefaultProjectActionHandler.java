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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.ui.SelectExecutablePanel;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public class DefaultProjectActionHandler implements ActionListener {
    private static CustomProjectActionHandlerProvider customBuildActionHandlerProvider = null;
    private static CustomProjectActionHandlerProvider customRunActionHandlerProvider = null;
    private static CustomProjectActionHandlerProvider customDebugActionHandlerProvider = null;
    private CustomProjectActionHandler customActionHandler = null;
    
    private static DefaultProjectActionHandler instance = null;
    
    public static DefaultProjectActionHandler getInstance() {
        if (instance == null)
            instance = new DefaultProjectActionHandler();
        return instance;
    }
    
    public void setCustomBuildActionHandlerProvider(CustomProjectActionHandlerProvider customBuildActionHandlerProvider) {
        this.customBuildActionHandlerProvider = customBuildActionHandlerProvider;
    }
    
    public void setCustomRunActionHandlerProvider(CustomProjectActionHandlerProvider customRunActionHandlerProvider) {
        this.customRunActionHandlerProvider = customRunActionHandlerProvider;
    }
    
    public void setCustomDebugActionHandlerProvider(CustomProjectActionHandlerProvider customDebugActionHandlerProvider) {
        this.customDebugActionHandlerProvider = customDebugActionHandlerProvider;
    }
    
    public void setCustomActionHandlerProvider(CustomProjectActionHandler customActionHandlerProvider) {
        this.customActionHandler = customActionHandlerProvider;
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        ProjectActionEvent[] paes = (ProjectActionEvent[])actionEvent.getSource();
        new HandleEvents(paes).go();
    }
    
    private static InputOutput mainTab = null;
    private static HandleEvents mainTabHandler = null;
    private static HashMap<String, Integer> tabMap = new HashMap();
    
    class HandleEvents implements ExecutionListener {
        private InputOutput reuseTab = null;
        private ProjectActionEvent[] paes;
        private String tabName;
        int currentAction = 0;
        
        private String getTabName(ProjectActionEvent[] paes) {
            String projectName = ProjectUtils.getInformation(paes[0].getProject()).getName();
            String tabName = projectName + " ("; // NOI18N
            for (int i = 0; i < paes.length; i++) {
                if (i >= 2) {
                    tabName += "..."; // NOI18N
                    break;
                }
                tabName += paes[i].getActionName();
                if (i < paes.length-1)
                    tabName += ", "; // NOI18N
            }
            tabName += ")"; // NOI18N
            return tabName;
        }
        
        private String getTabName(ProjectActionEvent pae) {
            String projectName = ProjectUtils.getInformation(pae.getProject()).getName();
            String tabName = projectName + " ("; // NOI18N
            tabName += pae.getActionName();
            tabName += ")"; // NOI18N
            return tabName;
        }
        
        private InputOutput getTab() {
            if (reuseTab != null)
                return reuseTab;
            else {
                InputOutput tab = IOProvider.getDefault().getIO(getTabName(paes[currentAction]), false);
                try {
                    tab.getOut().reset();
                } catch (IOException ioe) {
                }
                return tab;
            }
        }
        
        public HandleEvents(ProjectActionEvent[] paes) {
            this.paes = paes;
            currentAction = 0;
            
            if (MakeOptions.getInstance().getReuse()) {
                if (mainTabHandler == null && mainTab != null /*&& !mainTab.isClosed()*/) {
                    mainTab.closeInputOutput();
                    mainTab = null;
                }
//                if (mainTab != null && mainTab.isClosed()) {
//                    mainTabHandler = null;
//                    mainTab = null;
//                }
                tabName = getTabName(paes);
                Integer i = tabMap.get(tabName);
                if (i == null) {
                    i = new Integer(1);
                    tabMap.put(tabName, i);
                } else {
                    tabMap.put(tabName, ++i);
                }
                InputOutput tab = IOProvider.getDefault().getIO(tabName, i != 1);
                try {
                    tab.getOut().reset();
                } catch (IOException ioe) {
                }
                if (mainTabHandler == null) {
                    reuseTab = tab;
                    mainTab = reuseTab;
                    mainTabHandler = this;
                }
            }
        }
        
        public void go() {
            if (currentAction >= paes.length)
                return;
            
            final ProjectActionEvent pae = paes[currentAction];
            String rcfile = null;
            
            // Validate executable
            if (pae.getID() == ProjectActionEvent.RUN ||
                    pae.getID() == ProjectActionEvent.DEBUG ||
                    pae.getID() == ProjectActionEvent.DEBUG_LOAD_ONLY ||
                    pae.getID() == ProjectActionEvent.DEBUG_STEPINTO ||
                    pae.getID() == ProjectActionEvent.CUSTOM_ACTION) {
                if (!checkExecutable(pae))
                    return;
            }
            
            if ((pae.getID() == ProjectActionEvent.BUILD ||
                    pae.getID() == ProjectActionEvent.CLEAN) &&
                    customBuildActionHandlerProvider != null) {
                CustomProjectActionHandler ah = customBuildActionHandlerProvider.factoryCreate();
                ah.addExecutionListener(this);
                ah.execute(pae, getTab());
            } else if (pae.getID() == ProjectActionEvent.RUN &&
                    customRunActionHandlerProvider != null) {
                CustomProjectActionHandler ah = customRunActionHandlerProvider.factoryCreate();
                ah.addExecutionListener(this);
                ah.execute(pae, getTab());
            } else if ((pae.getID() == ProjectActionEvent.DEBUG ||
                    pae.getID() == ProjectActionEvent.DEBUG_LOAD_ONLY ||
                    pae.getID() == ProjectActionEvent.DEBUG_STEPINTO) &&
                    customDebugActionHandlerProvider != null) {
                CustomProjectActionHandler ah = customDebugActionHandlerProvider.factoryCreate();
                ah.addExecutionListener(this);
                ah.execute(pae, getTab());
            } else if (pae.getID() == ProjectActionEvent.RUN ||
                    pae.getID() == ProjectActionEvent.BUILD ||
                    pae.getID() == ProjectActionEvent.CLEAN) {
                String exe = IpeUtils.quoteIfNecessary(pae.getExecutable());
                String args = pae.getProfile().getArgsFlat();
                String[] env = pae.getProfile().getEnvironment().getenv();
                boolean showInput = pae.getID() == ProjectActionEvent.RUN;
                
                if (pae.getID() == ProjectActionEvent.RUN) {
                    int conType = pae.getProfile().getConsoleType().getValue();
                    if (conType == RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                        args = pae.getProfile().getArgsFlat();
                        exe = IpeUtils.quoteIfNecessary(pae.getExecutable());
                    } else if (pae.getProfile().getTerminalType() == null) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(DefaultProjectActionHandler.class, "Err_NoTermFound"))); // NOI18N
                    } else {
                        showInput = false;
                        if (conType == RunProfile.CONSOLE_TYPE_DEFAULT) {
                            conType = pae.getProfile().getDefaultConsoleType();
                        }
                        if (conType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
                            try {
                                rcfile = File.createTempFile("nbcnd_rc", "").getAbsolutePath(); // NOI18N
                            } catch (IOException ex) {
                            }
                            if (pae.getProfile().getTerminalPath().indexOf("gnome-terminal") != -1) { // NOI18N
                                /* gnome-terminal has differnt quoting rules... */
                                StringBuffer b = new StringBuffer();
                                for (int i = 0; i < args.length(); i++) {
                                    if (args.charAt(i) == '"') {
                                        b.append("\\\""); // NOI18N
                                    } else {
                                        b.append(args.charAt(i));
                                    }
                                }
                                args = b.toString();
                            }
                            args = MessageFormat.format(pae.getProfile().getTerminalOptions(), rcfile, exe, args);
                            exe = pae.getProfile().getTerminalPath();
                        }
                    }
                } else { // Build or Clean
                    String[] env1 = new String[env.length + 1];
                    String csname = ((MakeConfiguration) pae.getConfiguration()).getCompilerSet().getOption();
                    String csdname = ((MakeConfiguration) pae.getConfiguration()).getCompilerSet().getName();
                    String csdirs = CompilerSetManager.getDefault().getCompilerSet(csname, csdname).getDirectory();
                    boolean gotpath = false;
                    String pathname = Path.getPathName() + '=';
                    int i;
                    for (i = 0; i < env.length; i++) {
                        if (env[i].startsWith(pathname)) {
                            env1[i] = pathname + csdirs + File.pathSeparator + env[i].substring(5); // NOI18N
                            gotpath = true;
                        } else {
                            env1[i] = env[i];
                        }
                    }
                    if (!gotpath) {
                        env1[i] = pathname + csdirs + File.pathSeparator + CppSettings.getDefault().getPath();
                    }
                    env = env1;
                }
                NativeExecutor projectExecutor =  new NativeExecutor(
                        pae.getProfile().getRunDirectory(),
                        exe, args, env,
                        pae.getTabName(),
                        pae.getActionName(),
                        pae.getID() == ProjectActionEvent.BUILD,
                        showInput);
                projectExecutor.addExecutionListener(this);
                if (rcfile != null) {
                    projectExecutor.setExitValueOverride(rcfile);
                }
                try {
                    projectExecutor.execute(getTab());
                } catch (java.io.IOException ioe) {
                }
            } else if (pae.getID() == ProjectActionEvent.CUSTOM_ACTION) {
                customActionHandler.execute(pae, getTab());
            } else if (pae.getID() == ProjectActionEvent.DEBUG ||
                    pae.getID() == ProjectActionEvent.DEBUG_STEPINTO ||
                    pae.getID() == ProjectActionEvent.DEBUG_LOAD_ONLY) {
                System.err.println("No built-in debugging"); // NOI18N
            } else {
                assert false;
            }
        }
        
        public void executionStarted() {
            // Nothing
        }
        
        public void executionFinished(int rc) {
            Integer i = tabMap.get(tabName);
            if (i != null)
                tabMap.put(tabName, --i);
            if (paes[currentAction].getID() == ProjectActionEvent.BUILD || paes[currentAction].getID() == ProjectActionEvent.CLEAN) {
                // Refresh all files
                try {
                    FileObject projectFileObject = paes[currentAction].getProject().getProjectDirectory();
                    projectFileObject.getFileSystem().refresh(false);
                } catch (Exception e) {
                }
            }
            if (currentAction >= paes.length-1 || rc != 0) {
                if (mainTabHandler == this)
                    mainTabHandler = null;
                return;
            }
            if (rc == 0) {
                currentAction++;
                go();
            }
        }
        
        private boolean checkExecutable(ProjectActionEvent pae) {
            // Check if something is specified
            String executable = pae.getExecutable();
            if (executable.length() == 0) {
                String errormsg;
                if (((MakeConfiguration)pae.getConfiguration()).isMakefileConfiguration()) {
                    SelectExecutablePanel panel = new SelectExecutablePanel((MakeConfiguration)pae.getConfiguration());
                    DialogDescriptor descriptor = new DialogDescriptor(panel, getString("SELECT_EXECUTABLE"));
                    panel.setDialogDescriptor(descriptor);
                    DialogDisplayer.getDefault().notify(descriptor);
                    if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                        // Set executable in configuration
                        MakeConfiguration makeConfiguration = (MakeConfiguration)pae.getConfiguration();
                        executable = panel.getExecutable();
                        executable = FilePathAdaptor.naturalize(executable);
                        executable = IpeUtils.toRelativePath(makeConfiguration.getBaseDir(), executable);
                        executable = FilePathAdaptor.normalize(executable);
                        makeConfiguration.getMakefileConfiguration().getOutput().setValue(executable);
                        // Mark the project 'modified'
                        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)pae.getProject().getLookup().lookup(ConfigurationDescriptorProvider.class );
                        if (pdp != null)
                            pdp.getConfigurationDescriptor().setModified();
                        // Set executable in pae
                        if (pae.getID() == ProjectActionEvent.RUN) {
                            executable = FilePathAdaptor.naturalize(executable);
                            pae.setExecutable(executable);
                        }
                        else {
                            pae.setExecutable(makeConfiguration.getMakefileConfiguration().getAbsOutput());
                        }
                    }
                    else
                        return false;
                } else {
                    errormsg = getString("NO_BUILD_RESULT"); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                return false;
                }
            }
            if (IpeUtils.isPathAbsolute(executable)) {
                    // FIXUP: getExecutable should really return fully qualified name to executable including .exe
                    // but it is too late to change now. For now try both with and without.
                File file = new File(executable);
                    if (!file.exists())
                    file = new File(executable + ".exe"); // NOI18N
                    if (!file.exists() || file.isDirectory()) {
                        String errormsg = getString("EXECUTABLE_DOESNT_EXISTS", pae.getExecutable()); // NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                        return false;
                    }
                }
            return true;
        }
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getBundle(DefaultProjectActionHandler.class).getString(s);
    }
    private static String getString(String s, String arg) {
        return NbBundle.getMessage(DefaultProjectActionHandler.class, s, arg);
    }
}
