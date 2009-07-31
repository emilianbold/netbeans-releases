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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.ruby.rubyproject;

import org.netbeans.modules.ruby.rubyproject.rake.RakeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.codecoverage.RubyCoverageProvider;
import org.netbeans.modules.ruby.rubyproject.queries.RubyProjectEncodingQueryImpl;
import org.netbeans.modules.ruby.spi.project.support.rake.FilterPropertyProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.GeneratedFilesHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyProvider;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyUtils;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectEvent;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectListener;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class RubyBaseProject implements Project, RakeProjectListener {

    static {
        // initialize the logging levels -- see #151976
        RubyLoggingOption.initLoggers();
    }

    /**
     * Ruby package root sources type.
     * @see org.netbeans.api.project.Sources
     */
    public static final String SOURCES_TYPE_RUBY = "ruby"; // NOI18N

    protected final RakeProjectHelper helper;
    private final PropertyEvaluator eval;
    protected final ReferenceHelper refHelper;
    protected final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    protected final UpdateHelper updateHelper;
    protected final RubyProjectEncodingQueryImpl encodingQueryImpl;
    private final String projectConfigurationNamespace;
    private CopyOnWriteArrayList<PlatformChangeListener> platformCLs;

    protected RubyBaseProject(final RakeProjectHelper helper, final String projectConfigurationNamespace) {
        this.helper = helper;
        this.projectConfigurationNamespace = projectConfigurationNamespace;
        eval = createEvaluator();
        encodingQueryImpl = new RubyProjectEncodingQueryImpl(eval);
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        genFilesHelper = new GeneratedFilesHelper(helper);
        updateHelper = new UpdateHelper(this, this.helper, aux, this.genFilesHelper,
                UpdateHelper.createDefaultNotifier(), projectConfigurationNamespace);
        lookup = createLookup(aux, helper.createAuxiliaryProperties(), new Info(), new ProjectOpenedHookImpl());
        helper.addRakeProjectListener(this);
        platformCLs = new CopyOnWriteArrayList<PlatformChangeListener>();
    }

    protected abstract Icon getIcon();

    protected abstract Lookup createLookup(AuxiliaryConfiguration aux, AuxiliaryProperties auxProperties,
            ProjectInformation info, ProjectOpenedHook projectOpenedHook);
    
    protected abstract void registerClassPath();
    
    protected abstract void unregisterClassPath();

    /**
     * Helper method delegating to {@link RubyPlatform#platformFor(Project)}.
     * 
     * @return platform for this project; might be <tt>null</tt>
     */
    public RubyPlatform getPlatform() {
        return RubyPlatform.platformFor(this);
    }

    private PropertyEvaluator createEvaluator() {
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        // Adapted from APH.getStandardPropertyEvaluator (delegates to ProjectProperties):
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(RubyConfigurationProvider.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(RakeProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(RubyConfigurationProvider.CONFIG_PROPS_PATH),
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper), // NOI18N
                helper.getPropertyProvider(RakeProjectHelper.PRIVATE_PROPERTIES_PATH),
                PropertyUtils.userPropertiesProvider(baseEval2,
                "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper), // NOI18N
                helper.getPropertyProvider(RakeProjectHelper.PROJECT_PROPERTIES_PATH));
    }

    private boolean hasRakeFile() {
        return getRakeFile() != null;
    }
    
    FileObject getRakeFile() {
        return RakeSupport.findRakeFile(this);
    }

    /**
     * @return the source roots of this project.
     */
    public abstract FileObject[] getSourceRootFiles();
    
    /**
     * @return the test source roots of this project.
     */
    public abstract FileObject[] getTestSourceRootFiles();

    
    public PropertyEvaluator evaluator() {
        return eval;
    }

    public ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public UpdateHelper getUpdateHelper() {
        return this.updateHelper;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public RakeProjectHelper getRakeProjectHelper() {
        return helper;
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    /**
     * Set the given platform as active for this project and stores in in the
     * project's metadata. Automatically requires ProjectManager's mutex write
     * access.
     * 
     * @param platform platform to be used
     * @throws java.io.IOException when platform cannot be stored
     */
    public void changeAndStorePlatform(final RubyPlatform platform) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    EditableProperties props = helper.getProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH);
                    SharedRubyProjectProperties.storePlatform(props, platform);
                    helper.putProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH, props); // #47609
                    // and save the project
                    ProjectManager.getDefault().saveProject(RubyBaseProject.this);
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }

    public void configurationXmlChanged(RakeProjectEvent ev) {
        if (ev.getPath().equals(RakeProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    public void propertiesChanged(RakeProjectEvent ev) {
        // XXX platform *might be* changed. Likely cache platform in the field.
        // Now it is always read through evaluator. Cf. #getPlatform()
        for (PlatformChangeListener platformCL : platformCLs) {
            platformCL.platformChanged();
        }
    }

    // Currently unused (but see #47230):
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(projectConfigurationNamespace, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(projectConfigurationNamespace, "name"); // NOI18N

                    data.insertBefore(nameEl, /* OK if null */ data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    public void addPlatformChangeListener(final PlatformChangeListener platformChangeListener) {
        platformCLs.add(platformChangeListener);
    }

    public void removePlatformChangeListener(final PlatformChangeListener platformChangeListener) {
        platformCLs.remove(platformChangeListener);
    }

    /** Mainly for unit tests. */
    protected void open() {
        registerClassPath();
        FileObject rakeFile = getRakeFile();
        if (rakeFile != null) {
            rakeFile.addFileChangeListener(new FileChangeAdapter() {
                public @Override void fileChanged(FileEvent fe) { updateRakeTasks(); }
                public @Override void fileDeleted(FileEvent fe) { updateRakeTasks(); }
                public @Override void fileRenamed(FileRenameEvent fe) { updateRakeTasks(); }
            });
            updateRakeTasks();
        }
    }

    private void updateRakeTasks() {
        RubyPlatform platform = getPlatform();
        if (hasRakeFile() && platform != null && platform.hasValidRake(false)) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    RakeSupport.refreshTasks(RubyBaseProject.this, false);
                }
            });
        }
    }
    
    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {

        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final RakeProjectHelper helper;

        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, RakeProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }

        public void propertyChange(PropertyChangeEvent ev) {
            if (RubyConfigurationProvider.PROP_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }

        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, RakeProjectHelper helper) {
            String config = baseEval.getProperty(RubyConfigurationProvider.PROP_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            } else {
                return PropertyUtils.fixedPropertyProvider(Collections.<String, String>emptyMap());
            }
        }
    }

    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }
        
        public String getDisplayName() {
            return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
                public String run() {
                    Element data = updateHelper.getPrimaryConfigurationData(true);
                    // XXX replace by XMLUtil when that has findElement, findText, etc.
                    NodeList nl = data.getElementsByTagNameNS(RubyBaseProject.this.projectConfigurationNamespace, "name"); // NOI18N
                    if (nl.getLength() == 1) {
                        nl = nl.item(0).getChildNodes();
                        if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            return ((Text) nl.item(0)).getNodeValue();
                        }
                    }
                    return "???"; // NOI18N
                }
            });
        }
        
        public Icon getIcon() {
            return RubyBaseProject.this.getIcon();
        }
        
        public Project getProject() {
            return RubyBaseProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }

    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {

        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            open();

            // Ensure that code coverage is initialized in case it's enabled...
            RubyCoverageProvider provider = RubyCoverageProvider.get(RubyBaseProject.this);
            if (provider.isEnabled()) {
                provider.notifyProjectOpened();
            }
        }
        
        protected void projectClosed() {
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(RubyBaseProject.this);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            unregisterClassPath();
        }
    }
}
