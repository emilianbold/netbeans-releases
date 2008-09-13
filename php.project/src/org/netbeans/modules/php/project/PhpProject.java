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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.project;

import org.netbeans.modules.php.project.util.CopySupport;
import org.netbeans.modules.php.project.ui.logicalview.PhpLogicalViewProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.api.classpath.GlobalPathRegistry;
import org.netbeans.modules.php.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * @author ads, Tomas Mysik
 */
public class PhpProject implements Project, AntProjectListener {

    public static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics.php"; //NOI18N

    private static final Icon PROJECT_ICON = new ImageIcon(
            ImageUtilities.loadImage("org/netbeans/modules/php/project/ui/resources/phpProject.png")); // NOI18N

    final AntProjectHelper helper;
    private final ReferenceHelper refHelper;
    private final PropertyEvaluator eval;
    // @GuardedBy(this)
    private FileObject sourcesDirectory;
    private Lookup lookup;

    PhpProject(AntProjectHelper helper) {
        assert helper != null;

        this.helper = helper;
        AuxiliaryConfiguration configuration = helper.createAuxiliaryConfiguration();
        eval = createEvaluator();
        refHelper = new ReferenceHelper(helper, configuration, getEvaluator());
        helper.addAntProjectListener(this);
        initLookup(configuration);
    }

    public Lookup getLookup() {
        return lookup;
    }

    PropertyEvaluator getEvaluator() {
        return eval;
    }

    void addWeakPropertyEvaluatorListener(PropertyChangeListener listener) {
        eval.addPropertyChangeListener(WeakListeners.propertyChange(listener, eval));
    }

    private PropertyEvaluator createEvaluator() {
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        // Adapted from APH.getStandardPropertyEvaluator (delegates to ProjectProperties):
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(PhpConfigurationProvider.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(PhpConfigurationProvider.CONFIG_PROPS_PATH),
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }

    public FileObject getProjectDirectory() {
        return getHelper().getProjectDirectory();
    }

    synchronized FileObject getSourcesDirectory() {
        if (sourcesDirectory == null) {
            sourcesDirectory = resolveSourcesDirectory();
        }
        assert sourcesDirectory != null : "Sources directory cannot be null";
        return sourcesDirectory;
    }

    private FileObject resolveSourcesDirectory() {
        // get the first source root
        //  in fact, there should *always* be only 1 source root but see #141200, #141204 or #141229
        FileObject[] sourceObjects = Utils.getSourceObjects(this);
        if (sourceObjects.length > 0) {
            return sourceObjects[0];
        }
        // #144371 - source folder probably deleted => so:
        // #145477 (project sharability):
        //  1. try to restore it - if it fails, then
        //  2. set it to the project directory in *PRIVATE* properties (and save it)
        //      => warn user about impossibility of creating src dir and *remove it in project closed hook*!!!
        String projectName = getName();
        File srcDir = FileUtil.normalizeFile(new File(helper.resolvePath(eval.getProperty(PhpProjectProperties.SRC_DIR))));
        if (srcDir.mkdirs()) {
            // original sources restored
            informUser(projectName, NbBundle.getMessage(PhpProject.class, "MSG_SourcesFolderRestored", srcDir.getAbsolutePath()), NotifyDescriptor.INFORMATION_MESSAGE);
            return FileUtil.toFileObject(srcDir);
        }
        // temporary set sources to project directory, do not store it anywhere
        informUser(projectName, NbBundle.getMessage(PhpProject.class, "MSG_SourcesFolderTemporaryToProjectDirectory", srcDir.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE);
        return helper.getProjectDirectory();
    }

    private void informUser(String title, String message, int type) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                message,
                title,
                NotifyDescriptor.DEFAULT_OPTION,
                type,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION));
    }

    public void configurationXmlChanged(AntProjectEvent event) {
        /*
         *  The code below is standart and copied f.e. from MakeProject
         */
        if (event.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info) getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // We are interested only to listen to changes in sources.
        // PhpSources will do it itself
        /*
         * Also copied from  MakeProject
         */
        //  currently ignored (probably better to listen to evaluator() if you need to)
    }

    /*
     * Copied from MakeProject.
     */
    public String getName() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                Element data = getHelper().getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1
                            && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }

    /*
     * Copied from MakeProject.
     */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                Element data = getHelper().getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(
                            PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                getHelper().putPrimaryConfigurationData(data, true);
            }
        });
    }

    public AntProjectHelper getHelper() {
        return helper;
    }

    CopySupport getCopySupport() {
        return getLookup().lookup(CopySupport.class);
    }

    private void initLookup(AuxiliaryConfiguration configuration) {
        PhpSources phpSources = new PhpSources(getHelper(), getEvaluator());

        lookup = Lookups.fixed(new Object[] {
                this,
                CopySupport.getInstance(),
                new Info(),
                configuration,
                new PhpOpenedHook(),
                new PhpActionProvider(this),
                new PhpConfigurationProvider(this),
                helper.createCacheDirectoryProvider(),
                helper.createAuxiliaryProperties(),
                new ClassPathProviderImpl(getHelper(), getEvaluator(), phpSources),
                new PhpLogicalViewProvider(this),
                new CustomizerProviderImpl(this),
                getHelper().createSharabilityQuery(getEvaluator(),
                    new String[] {"${" + PhpProjectProperties.SRC_DIR + "}"} , new String[] {}), // NOI18N
                new PhpProjectOperations(this) ,
                new PhpProjectEncodingQueryImpl(getEvaluator()),
                new PhpTemplates(),
                phpSources,
                getHelper(),
                getEvaluator()
                // ?? getRefHelper()
        });
    }

    public ReferenceHelper getRefHelper() {
        return refHelper;
    }

    private final class Info implements ProjectInformation {
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener  listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public String getDisplayName() {
            return PhpProject.this.getName();
        }

        public Icon getIcon() {
            return PROJECT_ICON;
        }

        public String getName() {
            return PhpProject.this.getName();
        }

        public Project getProject() {
            return PhpProject.this;
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        void firePropertyChange(String prop) {
            propertyChangeSupport.firePropertyChange(prop , null, null);
        }
    }

    private final class PhpOpenedHook extends ProjectOpenedHook {
        protected void projectOpened() {
            // #139159 - we need to hold sources FO to prevent gc
            getSourcesDirectory();

            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));

            final CopySupport copySupport = getCopySupport();
            if (copySupport != null) {
                copySupport.projectOpened(PhpProject.this);
            }
        }

        protected void projectClosed() {
            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));

            final CopySupport copySupport = getCopySupport();
            if (copySupport != null) {
                copySupport.projectClosed(PhpProject.this);
            }

            // clear reference so the next time the project is opened we can check it again
            synchronized (PhpProject.this) {
                sourcesDirectory = null;
            }

            try {
                ProjectManager.getDefault().saveProject(PhpProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent ev) {
            if (PhpConfigurationProvider.PROP_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            String config = baseEval.getProperty(PhpConfigurationProvider.PROP_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            }
            return PropertyUtils.fixedPropertyProvider(Collections.<String, String>emptyMap());
        }
    }
}
