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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public class DefaultProjectActionHandler implements ActionListener {
    private CustomProjectActionHandlerProvider customBuildActionHandlerProvider = null;
    private CustomProjectActionHandlerProvider customRunActionHandlerProvider = null;
    private CustomProjectActionHandlerProvider customDebugActionHandlerProvider = null;
    
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
    
    public void actionPerformed(ActionEvent actionEvent) {
        ProjectActionEvent[] paes = (ProjectActionEvent[])actionEvent.getSource();
        new HandleEvents(paes).go();
    }
    
    private static InputOutput mainTab = null;
    private static HandleEvents mainTabHandler = null;
    
    class HandleEvents implements ExecutionListener {
        private InputOutput reuseTab = null;
        private ProjectActionEvent[] paes;
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
                reuseTab = IOProvider.getDefault().getIO(getTabName(paes), false);
                try {
                    reuseTab.getOut().reset();
                } catch (IOException ioe) {
                }
                if (mainTabHandler == null) {
                    mainTab = reuseTab;
                    mainTabHandler = this;
                }
            }
        }
        
        public void go() {
            if (currentAction >= paes.length)
                return;
            
            final ProjectActionEvent pae = paes[currentAction];
            
            // Validate executable
            if (pae.getID() == ProjectActionEvent.RUN ||
                    pae.getID() == ProjectActionEvent.DEBUG ||
                    pae.getID() == ProjectActionEvent.DEBUG_LOAD_ONLY ||
                    pae.getID() == ProjectActionEvent.DEBUG_STEPINTO) {
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
                
                if (pae.getID() == ProjectActionEvent.RUN) {
                    int conType = pae.getProfile().getConsoleType().getValue();
                    if (conType == RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                        args = pae.getProfile().getArgsFlat();
                        exe = IpeUtils.quoteIfNecessary(pae.getExecutable());
                    } else if (pae.getProfile().getTerminalType() == null) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(DefaultProjectActionHandler.class, "Err_NoTermFound"))); // NOI18N
                    } else {
                        if (conType == RunProfile.CONSOLE_TYPE_DEFAULT) {
                            conType = pae.getProfile().getDefaultConsoleType();
                        }
                        if (conType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
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
                            args = MessageFormat.format(pae.getProfile().getTerminalOptions(), exe, args);
                            exe = pae.getProfile().getTerminalPath();
                        }
                    }
                }
                NativeExecutor projectExecutor =  new NativeExecutor(
                        pae.getProfile().getRunDirectory(),
                        exe, args,
                        pae.getProfile().getEnvironment().getenv(),
                        pae.getTabName(),
                        pae.getActionName(),
                        pae.getID() == ProjectActionEvent.BUILD,
                        pae.getID() == ProjectActionEvent.RUN);
                projectExecutor.addExecutionListener(this);
                try {
                    projectExecutor.execute(getTab());
                } catch (java.io.IOException ioe) {
                }
            } else if (pae.getID() == ProjectActionEvent.DEBUG ||
                    pae.getID() == ProjectActionEvent.DEBUG_STEPINTO ||
                    pae.getID() == ProjectActionEvent.DEBUG_LOAD_ONLY) {
                System.err.println("No built-in debugging");
            } else {
                assert false;
            }
        }
        
        public void executionStarted() {
            // Nothing
        }
        
        public void executionFinished(int rc) {
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
            if (pae.getExecutable().length() == 0) {
                String errormsg;
                if (((MakeConfiguration)pae.getConfiguration()).isMakefileConfiguration()) {
                    errormsg = getString("NO_BUILD_RESULT_MAKE"); // NOI18N
                } else {
                    errormsg = getString("NO_BUILD_RESULT"); // NOI18N
                }
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                return false;
            } else {
                if (IpeUtils.isPathAbsolute(pae.getExecutable())) {
                    // FIXUP: getExecutable should really return fully qualified name to executable including .exe
                    // but it is too late to change now. For now try both with and without.
                    File file = new File(pae.getExecutable());
                    if (!file.exists())
                        file = new File(pae.getExecutable() + ".exe"); // NOI18N
                    if (!file.exists() || file.isDirectory()) {
                        String errormsg = getString("EXECUTABLE_DOESNT_EXISTS", pae.getExecutable()); // NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                        return false;
                    }
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
