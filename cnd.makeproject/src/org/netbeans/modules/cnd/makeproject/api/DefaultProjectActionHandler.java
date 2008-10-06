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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.remote.CommandProvider;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.BuildAction;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.makeproject.ui.SelectExecutablePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

public class DefaultProjectActionHandler implements ActionListener {
    private CustomProjectActionHandlerProvider customActionHandlerProvider = null;
    private CustomProjectActionHandler customActionHandler = null;
    
    private static DefaultProjectActionHandler instance = null;
    
    public static DefaultProjectActionHandler getInstance() {
        if (instance == null)
            instance = new DefaultProjectActionHandler();
        return instance;
    }
    
    /*
     * @deprecated. Register via services using org.netbeans.modules.cnd.makeproject.api.CustomProjectActionHandlerProvider
     */ 
    public void setCustomDebugActionHandlerProvider(CustomProjectActionHandlerProvider customDebugActionHandlerProvider) {
        customActionHandlerProvider = customDebugActionHandlerProvider;
    }
    
    public CustomProjectActionHandlerProvider getCustomDebugActionHandlerProvider() {
        // First try old-style registration (deprecated)
        if (customActionHandlerProvider != null) {
            return customActionHandlerProvider;
        }
        // Then try services
        Lookup.Template template = new Lookup.Template(CustomProjectActionHandlerProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(template);
        Collection collection = result.allInstances();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object caop = iterator.next();
            if (caop instanceof CustomProjectActionHandlerProvider) {
                customActionHandlerProvider = (CustomProjectActionHandlerProvider)caop;
                if (customActionHandlerProvider.getClass().getName().contains("dbx")) { // NOI18N
                    // prefer dbx over gdb ....
                    break;
                }
            }
        }
        return customActionHandlerProvider;
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
    private static ArrayList tabNames = new ArrayList();
    
    class HandleEvents implements ExecutionListener {
        private InputOutput ioTab = null;
        private ProjectActionEvent[] paes;
        private String tabName;
        private String tabNameSeq;
        int currentAction = 0;
        private ExecutorTask executorTask = null;
        private NativeExecutor projectExecutor = null;
        private StopAction sa = null;
        private RerunAction ra = null;
        List<BuildAction> additional;
        private ProgressHandle progressHandle = null;
        private final Object lock = new Object();
        
        private String getTabName(ProjectActionEvent[] paes) {
            String projectName = ProjectUtils.getInformation(paes[0].getProject()).getDisplayName();
            StringBuilder name = new StringBuilder(projectName);
            name.append(" ("); // NOI18N
            for (int i = 0; i < paes.length; i++) {
                if (i >= 2) {
                    name.append("..."); // NOI18N
                    break;
                }
                name.append( paes[i].getActionName() );
                if (i < paes.length-1)
                    name.append( ", " ); // NOI18N
            }
            name.append( ")" ); // NOI18N
            if (paes.length > 0) {
                MakeConfiguration conf = (MakeConfiguration) paes[0].getConfiguration();
                if (!conf.getDevelopmentHost().isLocalhost()) {
                    String hkey = conf.getDevelopmentHost().getName();
                    name.append(" - ").append(hkey); //NOI18N
                }
            }
            return name.toString();
        }
        
        private InputOutput getTab() {
            return ioTab;
        }
        
        private ProgressHandle createPogressHandle() {
            ProgressHandle handle = ProgressHandleFactory.createHandle(tabNameSeq, new Cancellable() {
                public boolean cancel() {
                    sa.actionPerformed(null);
                    return true;
                }
            }, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    getTab().select();
                }
            });
            handle.setInitialDelay(0);
            return handle;
        }
        
        private ProgressHandle createPogressHandleNoCancel() {
            ProgressHandle handle = ProgressHandleFactory.createHandle(tabNameSeq,
            new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    getTab().select();
                }
            });
            handle.setInitialDelay(0);
            return handle;
        }
        
        private InputOutput getIOTab(String name, boolean reuse) {
            sa = new StopAction(this);
            ra = new RerunAction(this);
            List<Action> list = new ArrayList<Action>();
            list.add(sa);
            list.add(ra);
            additional = BuildActionsProvider.getDefault().getActions(name, paes);
            list.addAll(additional);
            InputOutput tab;
            if (reuse) {
                tab = IOProvider.getDefault().getIO(name, false); // This will (sometimes!) find an existing one.
                tab.closeInputOutput(); // Close it...
            }
            tab = IOProvider.getDefault().getIO(name, list.toArray(new Action[list.size()])); // Create a new ...
            try {
                tab.getOut().reset();
            } catch (IOException ioe) {
            }
            
            progressHandle = createPogressHandle();
            progressHandle.start();
        
            return tab;
        }
        
        public HandleEvents(ProjectActionEvent[] paes) {
            this.paes = paes;
            currentAction = 0;
            
            if (MakeOptions.getInstance().getReuse()) {
                synchronized(lock) {
                    if (mainTabHandler == null && mainTab != null /*&& !mainTab.isClosed()*/) {
                        mainTab.closeInputOutput();
                        mainTab = null;
                    }
                    tabName = getTabName(paes);
                    tabNameSeq = tabName;
                    if (tabNames.contains(tabName)) {
                        int seq = 2;
                        while (true) {
                            tabNameSeq = tabName + " #" + seq; // NOI18N
                            if (!tabNames.contains(tabNameSeq)) {
                                break;
                            }
                            seq++;
                        }
                    }
                    tabNames.add(tabNameSeq);
                    ioTab = getIOTab(tabNameSeq, true);
                    if (mainTabHandler == null) {
                        mainTab = ioTab;
                        mainTabHandler = this;
                    }
                }
            }
            else {
                tabName = getTabName(paes);
                tabNameSeq = tabName;
                ioTab = getIOTab(tabName, false);
            }
        }
        
        public void reRun() {
            currentAction = 0;
            getTab().closeInputOutput();
            synchronized(lock) {
                tabNames.add(tabNameSeq);
            }
            try {
                getTab().getOut().reset();
            } catch (IOException ioe) {
            }
            progressHandle = createPogressHandle();
            progressHandle.start();
            go();
        }
        
        public void go() {
            executorTask = null;
            sa.setEnabled(false);
            ra.setEnabled(false);
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
                if (!checkExecutable(pae)) {
                    progressHandle.finish();
                    return;
                }
            }
            
            if ((pae.getID() == ProjectActionEvent.DEBUG ||
                    pae.getID() == ProjectActionEvent.DEBUG_LOAD_ONLY ||
                    pae.getID() == ProjectActionEvent.DEBUG_STEPINTO) &&
                    getCustomDebugActionHandlerProvider() != null) {
                // See 130827
                progressHandle.finish();
                progressHandle = createPogressHandleNoCancel();
                progressHandle.start();
                CustomProjectActionHandler ah = getCustomDebugActionHandlerProvider().factoryCreate();
                ah.addExecutionListener(this);
                ah.execute(pae, getTab());
            } else if (pae.getID() == ProjectActionEvent.RUN ||
                    pae.getID() == ProjectActionEvent.BUILD ||
                    pae.getID() == ProjectActionEvent.CLEAN) {
                String exe = IpeUtils.quoteIfNecessary(pae.getExecutable());
                String args = pae.getProfile().getArgsFlat();
                String[] env = pae.getProfile().getEnvironment().getenv();
                boolean showInput = pae.getID() == ProjectActionEvent.RUN;
                MakeConfiguration conf = (MakeConfiguration) pae.getConfiguration();
                String key = conf.getDevelopmentHost().getName();
                
                if (!conf.getDevelopmentHost().isLocalhost()) {
                    // Make sure the project root is visible remotely
                    String basedir = pae.getProfile().getBaseDir();
                    PathMap mapper = HostInfoProvider.getDefault().getMapper(key);
                    if (!mapper.isRemote(basedir, true)) {
//                        mapper.showUI();
//                        if (!mapper.isRemote(basedir)) {
//                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
//                                    NbBundle.getMessage(DefaultProjectActionHandler.class, "Err_CannotRunLocalProjectRemotely")));
                            progressHandle.finish();
                            return;
//                        }
                    }
                    //CompilerSetManager rcsm = CompilerSetManager.getDefault(key);
                }
                
                PlatformInfo pi = PlatformInfo.getDefault(conf.getDevelopmentHost().getName());

                boolean unbuffer = false;
                if (pae.getID() == ProjectActionEvent.RUN) {
                    int conType = pae.getProfile().getConsoleType().getValue();
                    if (pae.getProfile().getTerminalType() == null || pae.getProfile().getTerminalPath() == null) { 
                        String errmsg;
                        if (Utilities.isMac())
                            errmsg = getString("Err_NoTermFoundMacOSX");
                        else
                            errmsg = getString("Err_NoTermFound");
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errmsg));
                        conType = RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW;
                    }
                    if (!conf.getDevelopmentHost().isLocalhost()) {
                        //TODO: only output window for remote for now
                        conType = RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW;
                    }
                    if (conType == RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                        if (HostInfoProvider.getDefault().getPlatform(key) == PlatformTypes.PLATFORM_WINDOWS) {
                            // we need to run the application under cmd on windows
                            exe = "cmd.exe"; // NOI18N
                            args = "/c " + IpeUtils.quoteIfNecessary(pae.getExecutable()) + " " + pae.getProfile().getArgsFlat(); // NOI18N
                        } else {
                            exe = IpeUtils.quoteIfNecessary(pae.getExecutable());
                            args = pae.getProfile().getArgsFlat();
                        }
                        unbuffer = true;
                    } else {
                        showInput = false;
                        if (conType == RunProfile.CONSOLE_TYPE_DEFAULT) {
                            conType = RunProfile.getDefaultConsoleType();
                        }
                        if (conType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
                            try {
                                rcfile = File.createTempFile("nbcnd_rc", "").getAbsolutePath(); // NOI18N
                            } catch (IOException ex) {
                            }
                            String args2;
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
                                args2 = b.toString();
                            } else {
                                args2 = "";
                            }
                            args = MessageFormat.format(pae.getProfile().getTerminalOptions(), rcfile, exe, args, args2);
                            exe = pae.getProfile().getTerminalPath();
                        }
                        // See 130827
                        progressHandle.finish();
                        progressHandle = createPogressHandleNoCancel();
                        progressHandle.start();
                    }
                    // Append compilerset base to run path. (IZ 120836)
                    ArrayList<String> env1 = new ArrayList<String>();
                    CompilerSet cs = conf.getCompilerSet().getCompilerSet();
                    if (cs != null) {
                        String csdirs = cs.getDirectory();
                        String commands = cs.getCompilerFlavor().getCommandFolder(conf.getPlatform().getValue());
                        if (commands != null && commands.length()>0) {
                            // Also add msys to path. Thet's where sh, mkdir, ... are.
                            csdirs = csdirs + pi.pathSeparator() + commands;
                        }
                        boolean gotpath = false;
                        String pathname = pi.getPathName() + '=';
                        int i;
                        for (i = 0; i < env.length; i++) {
                            if (env[i].startsWith(pathname)) {
                                env1.add(env[i] + pi.pathSeparator() + csdirs); // NOI18N
                                gotpath = true;
                            } else {
                                env1.add(env[i]);
                            }
                        }
                        if (!gotpath) {
                            env1.add(pathname + pi.getPathAsString() + pi.pathSeparator() + csdirs);
                        }
                        env = env1.toArray(new String[env1.size()]);
                    }
                } else { // Build or Clean
                    String[] env1 = new String[env.length + 1];
                    String csdirs = conf.getCompilerSet().getCompilerSet().getDirectory();
                    String commands = conf.getCompilerSet().getCompilerSet().getCompilerFlavor().getCommandFolder(conf.getPlatform().getValue());
                    if (commands != null && commands.length()>0) {
                        // Also add msys to path. Thet's where sh, mkdir, ... are.
                        csdirs = csdirs + pi.pathSeparator() + commands;
                    }
                    boolean gotpath = false;
                    String pathname = pi.getPathName() + '=';
                    int i;
                    for (i = 0; i < env.length; i++) {
                        if (env[i].startsWith(pathname)) {
                            env1[i] = pathname + csdirs + pi.pathSeparator() + env[i].substring(5); // NOI18N
                            gotpath = true;
                        } else {
                            env1[i] = env[i];
                        }
                    }
                    if (!gotpath) {
                        String defaultPath = conf.getPlatformInfo().getPathAsString();
                        env1[i] = pathname + csdirs + pi.pathSeparator() + defaultPath;
                    }
                    env = env1;
                }
                projectExecutor =  new NativeExecutor(
                        key,
                        pae.getProfile().getRunDirectory(),
                        exe, args, env,
                        pae.getTabName(),
                        pae.getActionName(),
                        pae.getID() == ProjectActionEvent.BUILD,
                        showInput,
                        unbuffer);
                projectExecutor.addExecutionListener(this);
                if (rcfile != null) {
                    projectExecutor.setExitValueOverride(rcfile);
                }
                try {
                    sa.setEnabled(pae.getID() != ProjectActionEvent.RUN || showInput);
                    ra.setEnabled(false);
                    executorTask = projectExecutor.execute(getTab());
                } catch (java.io.IOException ioe) {
                }
            } else if (pae.getID() == ProjectActionEvent.CUSTOM_ACTION) {
                progressHandle.finish();
                progressHandle = createPogressHandleNoCancel();
                progressHandle.start();
                customActionHandler.addExecutionListener(this);
                customActionHandler.execute(pae, getTab());
            } else if (pae.getID() == ProjectActionEvent.DEBUG ||
                    pae.getID() == ProjectActionEvent.DEBUG_STEPINTO ||
                    pae.getID() == ProjectActionEvent.DEBUG_LOAD_ONLY) {
                System.err.println("No built-in debugging"); // NOI18N
            } else {
                assert false;
            }
        }
        
        public ExecutorTask getExecutorTask() {
            return executorTask;
        }
        
        public NativeExecutor getNativeExecutor() {
            return projectExecutor;
        }
        
        public void executionStarted() {
            if (additional != null) {
                for(BuildAction action : additional){
                    action.setStep(currentAction);
                    action.executionStarted();
                }
            }
        }
        
        public void executionFinished(int rc) {
            if (additional != null) {
                for(Action action : additional){
                    ((ExecutionListener)action).executionFinished(rc);
                }
            }
            if (paes[currentAction].getID() == ProjectActionEvent.BUILD || paes[currentAction].getID() == ProjectActionEvent.CLEAN) {
                // Refresh all files
                try {
                    FileObject projectFileObject = paes[currentAction].getProject().getProjectDirectory();
                    projectFileObject.getFileSystem().refresh(false);
                    MakeLogicalViewProvider.refreshBrokenItems(paes[currentAction].getProject());
                } catch (Exception e) {
                }
            }
            if (currentAction >= paes.length-1 || rc != 0) {
                synchronized(lock) {
                    if (mainTabHandler == this)
                        mainTabHandler = null;
                    tabNames.remove(tabNameSeq);
                }
                sa.setEnabled(false);
                ra.setEnabled(true);
                progressHandle.finish();
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
                        ConfigurationDescriptorProvider pdp = pae.getProject().getLookup().lookup(ConfigurationDescriptorProvider.class);
                        if (pdp != null)
                            pdp.getConfigurationDescriptor().setModified();
                        // Set executable in pae
                        if (pae.getID() == ProjectActionEvent.RUN) {
                            // Next block is commented out due to IZ120794
                            /*CompilerSet compilerSet = CompilerSetManager.getDefault(makeConfiguration.getDevelopmentHost().getName()).getCompilerSet(makeConfiguration.getCompilerSet().getValue());
                            if (compilerSet != null && compilerSet.getCompilerFlavor() != CompilerFlavor.MinGW) {
                                // IZ 120352
                                executable = FilePathAdaptor.naturalize(executable);
                            }*/
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
                Configuration conf = pae.getConfiguration();
                boolean ok = true;
                
                if (conf instanceof MakeConfiguration && !((MakeConfiguration) conf).getDevelopmentHost().isLocalhost()) {
                    ok = verifyRemoteExecutable(((MakeConfiguration) conf).getDevelopmentHost().getName(), executable);
                } else {
                    // FIXUP: getExecutable should really return fully qualified name to executable including .exe
                    // but it is too late to change now. For now try both with and without.
                    File file = new File(executable);
                    if (!file.exists()) {
                        file = new File(executable + ".exe"); // NOI18N
                    }
                    if (!file.exists() || file.isDirectory()) {
                        ok = false;
                    }
                }
                if (!ok) {
                    String errormsg = getString("EXECUTABLE_DOESNT_EXISTS", pae.getExecutable()); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Verify a remote executable exists, is executable, and is not a directory.
     * 
     * @param hkey The remote host
     * @param executable The file to remotely check
     * @return true if executable exists and is an executable, otherwise false
     */
    private boolean verifyRemoteExecutable(String hkey, String executable) {
        CommandProvider cmd = Lookup.getDefault().lookup(CommandProvider.class);
        if (cmd != null) {
            return cmd.run(hkey, "test -x " + executable + " -a -f " + executable, null) == 0; // NOI18N
        }
        return false;
    }

    private static final class StopAction extends AbstractAction {
        HandleEvents handleEvents;

        public StopAction(HandleEvents handleEvents) {
            this.handleEvents = handleEvents;
            //System.out.println("handleEvents 1 " + handleEvents);
            //setEnabled(false); // initially, until ready
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(DefaultProjectActionHandler.class.getResource("/org/netbeans/modules/cnd/makeproject/ui/resources/stop.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return getString("TargetExecutor.StopAction.stop");
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled())
                return;
            setEnabled(false);
            if (handleEvents.getExecutorTask() != null) {
                handleEvents.getNativeExecutor().stop();
                handleEvents.getExecutorTask().stop();
            }
        }

    }

    private static final class RerunAction extends AbstractAction {
        HandleEvents handleEvents;

        public RerunAction(HandleEvents handleEvents) {
            this.handleEvents = handleEvents;
        }

        @Override
        public Object getValue(String key) {
            if (key.equals(Action.SMALL_ICON)) {
                return new ImageIcon(DefaultProjectActionHandler.class.getResource("/org/netbeans/modules/cnd/makeproject/ui/resources/rerun.png"));
            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
                return getString("TargetExecutor.RerunAction.rerun");
            } else {
                return super.getValue(key);
            }
        }

        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            handleEvents.reRun();
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
