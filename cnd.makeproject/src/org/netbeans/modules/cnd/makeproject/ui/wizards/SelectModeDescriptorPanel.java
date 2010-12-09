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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class SelectModeDescriptorPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, NewMakeProjectWizardIterator.Name, ChangeListener {

    private WizardDescriptor wizardDescriptor;
    private SelectModePanel component;
    private String name;
    private final WizardStorage wizardStorage;
    private boolean isValid = false;
    private final boolean fullRemote;

    public SelectModeDescriptorPanel(boolean fullRemote) {
        name = NbBundle.getMessage(SelectModePanel.class, "SelectModeName"); // NOI18N
        this.fullRemote = fullRemote;
        wizardStorage = new WizardStorage(fullRemote);
    }

    @Override
    public String getName() {
        return name;
    }


    public boolean isFullRemote() {
        return fullRemote;
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

    public WizardStorage getWizardStorage(){
        return wizardStorage;
    }

    public class WizardStorage {
        private String path = ""; // NOI18N
        private FileObject fileObject;
        private String flags = ""; // NOI18N
        private boolean setMain = true;
        private boolean buildProject = true;
        private CompilerSet cs;
        private boolean defaultCompilerSet;
        private ExecutionEnvironment env;
        private final boolean fullRemote;
        private FileObject makefileFO;

        public WizardStorage(boolean fullRemote) {
            this.fullRemote = fullRemote;
            env = ServerList.getDefaultRecord().getExecutionEnvironment();
        }

        /**
         * @return the path
         */
        public void setMode(boolean isSimple) {
            SelectModeDescriptorPanel.this.setMode(isSimple);
        }

        public boolean isFullRemote() {
            return fullRemote;
        }

        /**
         * @return the path
         */
        public String getProjectPath() {
            return path;
        }

        public FileObject getSourcesFileObject() {
            return fileObject;
        }

        /**
         * @param path the path to set
         */
        public void setProjectPath(String path) {
            this.path = path.trim();
            validate();
        }

        public void setSourcesFileObject(FileObject fileObject) {
            this.fileObject = fileObject;
            validate();
        }

        public String getConfigure(){
            if (path.length() == 0) {
                return null;
            }
            if (fileObject != null) {
                return ConfigureUtils.findConfigureScript(fileObject);
            } else {
                return ConfigureUtils.findConfigureScript(path);
            }
        }

        public String getMake(){
            return (makefileFO == null) ? null : makefileFO.getPath();
        }

        public void setMake(FileObject makefileFO) {
            this.makefileFO = makefileFO;
        }

        /**
         * @return the flags
         */
        public String getFlags() {
            return flags;
        }

        /**
         * @return the flags
         */
        public String getRealFlags() {
            return ConfigureUtils.getConfigureArguments(env, cs, getConfigure(), flags);
        }

        /**
         * @param flags the flags to set
         */
        public void setFlags(String flags) {
            this.flags = flags;
            validate();
        }

        /**
         * @return the setMain
         */
        public boolean isSetMain() {
            return setMain;
        }

        /**
         * @param setMain the setMain to set
         */
        public void setSetMain(boolean setMain) {
            this.setMain = setMain;
            validate();
        }

        /**
         * @return the buildProject
         */
        public boolean isBuildProject() {
            return buildProject;
        }

        /**
         * @param buildProject the buildProject to set
         */
        public void setBuildProject(boolean buildProject) {
            this.buildProject = buildProject;
            validate();
        }

        void setCompilerSet(CompilerSet cs) {
            this.cs = cs;
        }

        void setExecutionEnvironment(ExecutionEnvironment ee) {
            this.env = ee;
        }

        void setDefaultCompilerSet(boolean defaultCompilerSet) {
            this.defaultCompilerSet = defaultCompilerSet;
        }
    }

    public static class WizardDescriptorAdapter extends WizardDescriptor{
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
                return ExecutionEnvironmentFactory.toUniqueID(storage.env);
            } else if (WizardConstants.PROPERTY_TOOLCHAIN.equals(name)) { // NOI18N
                return storage.cs;
            } else if (WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.equals(name)) { // NOI18N
                return storage.defaultCompilerSet;
            } else if (/*XXX Define somewhere*/WizardConstants.PROPERTY_FULL_REMOTE.equals(name)) { // NOI18N
                return storage.fullRemote;
            } else if (/*XXX Define somewhere*/WizardConstants.PROPERTY_NATIVE_PROJ_DIR.equals(name)) { // NOI18N
                return storage.getSourcesFileObject().getPath();
            } else if (/*XXX Define somewhere*/WizardConstants.PROPERTY_NATIVE_PROJ_FO.equals(name)) { // NOI18N
                return storage.getSourcesFileObject();
            } else if (/*XXX Define somewhere*/WizardConstants.PROPERTY_PROJECT_FOLDER.equals(name)) { // NOI18N
                //Object o = super.getProperty(name);
                return new File(storage.getProjectPath());
            }
            return super.getProperty(name);
        }
    }
}

