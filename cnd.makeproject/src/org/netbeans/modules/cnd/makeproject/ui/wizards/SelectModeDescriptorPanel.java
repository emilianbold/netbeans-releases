/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectWizardPanels;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectWizardPanels.NamedPanel;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectWizardPanels.WizardStorage;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class SelectModeDescriptorPanel implements ProjectWizardPanels.MakeModePanel<WizardDescriptor>, NamedPanel, ChangeListener {

    private WizardDescriptor wizardDescriptor;
    private SelectModePanel component;
    private String name;
    private final MyWizardStorage wizardStorage;
    private boolean isValid = false;

    public SelectModeDescriptorPanel() {
        name = NbBundle.getMessage(SelectModePanel.class, "SelectModeName"); // NOI18N
        wizardStorage = new MyWizardStorage();
    }

    @Override
    public String getName() {
        return name;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SelectModePanel getComponent() {
        if (component == null) {
            component = new SelectModePanel(this);
      	    component.setName(name);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewMakeWizardP0"); // NOI18N
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    private void validate(){
        isValid = component.valid();
        fireChangeEvent();
    }

    private void setMode(boolean isSimple) {
        if (isSimple) {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_SIMPLE_MODE, Boolean.TRUE);
        } else {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_SIMPLE_MODE, Boolean.FALSE);
        }
        validate();
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void invalidate() {
        isValid = false;
        fireChangeEvent();
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    WizardDescriptor getWizardDescriptor(){
        return wizardDescriptor;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        String[] res;
        Object o = component.getClientProperty(WizardDescriptor.PROP_CONTENT_DATA);
        String[] names = (String[]) o;
        if (Boolean.TRUE.equals(wizardDescriptor.getProperty(WizardConstants.PROPERTY_SIMPLE_MODE))){
            res = new String[]{names[0]};
        } else {
            res = new String[]{names[0], "..."}; // NOI18N
        }
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, res);
      	fireChangeEvent();
    }

    @Override
    public boolean isFinishPanel() {
        return  Boolean.TRUE.equals(wizardDescriptor.getProperty(WizardConstants.PROPERTY_SIMPLE_MODE));
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        if (wizardDescriptor.getProperty(WizardConstants.PROPERTY_SIMPLE_MODE) == null) {
            wizardDescriptor.putProperty(WizardConstants.PROPERTY_SIMPLE_MODE, Boolean.TRUE);
        }
        getComponent().read(wizardDescriptor);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
    }

    @Override
    public WizardStorage getWizardStorage(){
        return wizardStorage;
    }

    @Override
    public void setFinishPanel(boolean isFinishPanel) {
    }

    boolean isFullRemote() {
        return wizardDescriptor.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV) != null;
    }

    private class MyWizardStorage implements WizardStorage {
        private String projectPath = ""; // NOI18N
        private FileObject sourceFileObject;
        private String flags = ""; // NOI18N
        private boolean setMain = true;
        private boolean buildProject = true;
        private CompilerSet cs;
        private boolean defaultCompilerSet;
        private ExecutionEnvironment buildEnv;
        private ExecutionEnvironment sourceEnv;
        private FileObject makefileFO;

        public MyWizardStorage() {
            buildEnv = ServerList.getDefaultRecord().getExecutionEnvironment();
            sourceEnv = NewProjectWizardUtils.getDefaultSourceEnvironment();
        }

        @Override
        public WizardDescriptor getAdapter() {
            return new WizardDescriptorAdapter(this);
        }
        
        /**
         * @return the path
         */
        @Override
        public void setMode(boolean isSimple) {
            SelectModeDescriptorPanel.this.setMode(isSimple);
        }

        /**
         * @return the path
         */
        @Override
        public String getProjectPath() {
            return projectPath;
        }

        @Override
        public FileObject getSourcesFileObject() {
            return sourceFileObject;
        }

        /**
         * @param path the path to set
         */
        @Override
        public void setProjectPath(String path) {
            this.projectPath = path.trim();
            validate();
        }

        @Override
        public void setSourcesFileObject(FileObject fileObject) {
            this.sourceFileObject = fileObject;
            validate();
        }

        @Override
        public String getConfigure(){
            //if (projectPath.length() == 0) {
            //    return null;
            //}
            if (sourceFileObject != null) {
                return ConfigureUtils.findConfigureScript(sourceFileObject);
            } else {
                if (wizardDescriptor != null) {
                    ExecutionEnvironment env = (ExecutionEnvironment) wizardDescriptor.getProperty(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV);
                    if (env == null) {
                        env = ExecutionEnvironmentFactory.getLocal();
                    }
                    FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
                    return ConfigureUtils.findConfigureScript(fileSystem.findResource(projectPath));
                }
            }
            return null;
        }

        @Override
        public String getMake(){
            return (makefileFO == null) ? null : makefileFO.getPath();
        }

        @Override
        public void setMake(FileObject makefileFO) {
            this.makefileFO = makefileFO;
        }

        /**
         * @return the flags
         */
        @Override
        public String getFlags() {
            return flags;
        }

        /**
         * @return the flags
         */
        @Override
        public String getRealFlags() {
            return ConfigureUtils.getConfigureArguments(buildEnv, cs, getConfigure(), flags);
        }

        /**
         * @param flags the flags to set
         */
        @Override
        public void setFlags(String flags) {
            this.flags = flags;
            validate();
        }

        /**
         * @return the setMain
         */
        @Override
        public boolean isSetMain() {
            return setMain;
        }

        /**
         * @param setMain the setMain to set
         */
        @Override
        public void setSetMain(boolean setMain) {
            this.setMain = setMain;
            validate();
        }

        /**
         * @return the buildProject
         */
        @Override
        public boolean isBuildProject() {
            return buildProject;
        }

        /**
         * @param buildProject the buildProject to set
         */
        @Override
        public void setBuildProject(boolean buildProject) {
            this.buildProject = buildProject;
            validate();
        }

        @Override
        public void setCompilerSet(CompilerSet cs) {
            this.cs = cs;
        }

        @Override
        public CompilerSet getCompilerSet() {
            return cs;
        }

        @Override
        public void setExecutionEnvironment(ExecutionEnvironment ee) {
            this.buildEnv = ee;
        }

        @Override
        public ExecutionEnvironment getExecutionEnvironment() {
            return buildEnv;
        }

        @Override
        public ExecutionEnvironment getSourceExecutionEnvironment() {
            return sourceEnv;
        }

        @Override
        public void setSourceExecutionEnvironment(ExecutionEnvironment sourceEnv) {
            this.sourceEnv = sourceEnv;
        }

        @Override
        public void setDefaultCompilerSet(boolean defaultCompilerSet) {
            this.defaultCompilerSet = defaultCompilerSet;
        }

        @Override
        public boolean isDefaultCompilerSet() {
            return defaultCompilerSet;
        }
    }

    private static class WizardDescriptorAdapter extends WizardDescriptor{
        private WizardStorage storage;
        public WizardDescriptorAdapter(WizardStorage storage) {
            this.storage = storage;
        }
        @Override
        public synchronized Object getProperty(String name) {
            if ("realFlags".equals(name)) { // NOI18N
                return storage.getRealFlags();
            } else if ("buildProject".equals(name)) { // NOI18N
                if (storage.isBuildProject()) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if ("setMain".equals(name)) { // NOI18N
                if (storage.isSetMain()) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            } else if (WizardConstants.PROPERTY_SIMPLE_MODE.equals(name)) { // NOI18N
                return Boolean.TRUE;
            } else if (WizardConstants.PROPERTY_USER_MAKEFILE_PATH.equals(name)) { // NOI18N
                return storage.getMake();
            } else if (WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH.equals(name)) { // NOI18N
                return storage.getConfigure();
            } else if (WizardConstants.PROPERTY_HOST_UID.equals(name)) { // NOI18N
                return ExecutionEnvironmentFactory.toUniqueID(storage.getExecutionEnvironment());
            } else if (WizardConstants.PROPERTY_SOURCE_HOST_ENV.equals(name)) {
                return storage.getSourceExecutionEnvironment();
            } else if (WizardConstants.PROPERTY_TOOLCHAIN.equals(name)) { // NOI18N
                return storage.getCompilerSet();
            } else if (WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.equals(name)) { // NOI18N
                return storage.isDefaultCompilerSet();
            } else if (WizardConstants.PROPERTY_NATIVE_PROJ_DIR.equals(name)) { // NOI18N
                return storage.getSourcesFileObject().getPath();
            } else if (WizardConstants.PROPERTY_NATIVE_PROJ_FO.equals(name)) { // NOI18N
                return storage.getSourcesFileObject();
            } else if (WizardConstants.PROPERTY_PROJECT_FOLDER.equals(name)) { // NOI18N
                try {
                    return new FSPath(storage.getSourcesFileObject().getFileSystem(), storage.getSourcesFileObject().getPath());
                } catch (FileStateInvalidException ex) {
                }
            }
            return super.getProperty(name);
        }
    }
}
