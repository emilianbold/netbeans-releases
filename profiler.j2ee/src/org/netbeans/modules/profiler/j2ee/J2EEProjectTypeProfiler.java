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

package org.netbeans.modules.profiler.j2ee;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.lib.profiler.common.filters.SimpleFilter;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.marker.MethodMarker;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.utils.MiscUtils;
import org.netbeans.lib.profiler.utils.formatting.Formattable;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.profiler.AbstractProjectTypeProfiler;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.ProfilerIDESettings;
import org.netbeans.modules.profiler.actions.JavaPlatformSelector;
import org.netbeans.modules.profiler.ui.ProfilerDialogs;
import org.netbeans.modules.profiler.ui.stp.DefaultSettingsConfigurator;
import org.netbeans.modules.profiler.ui.stp.SelectProfilingTask;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.*;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;
import org.netbeans.modules.profiler.utils.ProjectUtilities;


/**
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.ProjectTypeProfiler.class)
public final class J2EEProjectTypeProfiler extends AbstractProjectTypeProfiler {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class JSPNameFormatter implements org.netbeans.lib.profiler.utils.formatting.MethodNameFormatter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Formattable formatMethodName(final SourceCodeSelection sourceCodeSelection) {
            return new Formattable() {
                    public String toFormatted() {
                        String name = WebProjectUtils.getJSPPath(sourceCodeSelection);

                        return name;
                    }
                };
        }

        public Formattable formatMethodName(final String className, final String methodName, final String signature) {
            return new Formattable() {
                    public String toFormatted() {
                        ClientUtils.SourceCodeSelection tmpSelection = new ClientUtils.SourceCodeSelection(className, methodName,
                                                                                                           signature);
                        String name = WebProjectUtils.getJSPPath(tmpSelection);

                        return name;
                    }
                };
        }
    }

    private static class MyPropertyProvider implements PropertyProvider {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Properties props;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private MyPropertyProvider(Properties props) {
            this.props = props;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Map /*<String,String>*/ getProperties() {
            return props;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String MODIFY_BUILDSCRIPT_CAPTION = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                             "J2EEProjectTypeProfiler_ModifyBuildScriptCaption"); // NOI18N
    private static final String MODIFY_BUILDSCRIPT_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                             "J2EEProjectTypeProfiler_ModifyBuildScriptMsg"); // NOI18N
    private static final String REGENERATE_BUILDSCRIPT_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                 "J2EEProjectTypeProfiler_RegenerateBuildScriptMsg"); // NOI18N
    private static final String CANNOT_FIND_BUILDSCRIPT_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                  "J2EEProjectTypeProfiler_CannotFindBuildScriptMsg"); // NOI18N
    private static final String CANNOT_BACKUP_BUILDSCRIPT_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                    "J2EEProjectTypeProfiler_CannotBackupBuildScriptMsg"); // NOI18N
    private static final String MODIFY_BUILDSCRIPT_MANUALLY_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                      "J2EEProjectTypeProfiler_ModifyBuildScriptManuallyMsg"); // NOI18N
    private static final String PROFILING_NOT_SUPPORTED_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                  "J2EEProjectTypeProfiler_ProfilingNotSupportedMsg"); // NOI18N
    private static final String SKIP_BUTTON_NAME = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                       "J2EEProjectTypeProfiler_SkipButtonName"); // NOI18N
    private static final String NO_SERVER_FOUND_MSG = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                          "J2EEProjectTypeProfiler_NoServerFoundMsg"); // NOI18N
    private static final String PROJECT_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                       "J2EEProjectTypeProfiler_ProjectCategory"); // NOI18N
    private static final String WEB_CONTAINER_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                             "J2EEProjectTypeProfiler_WebContainerCategory"); // NOI18N
    private static final String SOAP_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class, "J2EEProjectTypeProfiler_SOAPCategory"); // NOI18N

    private static final String SOAP_PROTOCOL_PARSING_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class, "J2EEProjectTypeProfiler_SOAPProtocolParsingCategory"); // NOI18N

    private static final String SOAP_SERIALIZATION_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class, "J2EEProjectTypeProfiler_SOAPSerialization"); // NOI18N

    private static final String SOAP_ENDPOINT_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class, "J2EEProjectTypeProfiler_SOAPEndpointCategory"); // NOI18N

    private static final String SOAP_REPLY_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class, "J2EEProjectTypeProfiler_SOAPReplyCategory"); // NOI18N

    private static final String LIFECYCLE_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                         "J2EEProjectTypeProfiler_LifecycleCategory"); // NOI18N
    private static final String EXECUTIVE_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                         "J2EEProjectTypeProfiler_ExecutiveCategory"); // NOI18N
    private static final String JSP_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                   "J2EEProjectTypeProfiler_JspCategory"); // NOI18N
    private static final String TAGS_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                    "J2EEProjectTypeProfiler_TagsCategory"); // NOI18N
    private static final String SERVLETS_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                        "J2EEProjectTypeProfiler_ServletsCategory"); // NOI18N
    private static final String FILTERS_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                       "J2EEProjectTypeProfiler_FiltersCategory"); // NOI18N
    private static final String LISTENERS_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                         "J2EEProjectTypeProfiler_ListenersCategory"); // NOI18N
    private static final String EJB_CONTAINER_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                             "J2EEProjectTypeProfiler_EjbContainerCategory"); // NOI18N
    private static final String POOLING_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                       "J2EEProjectTypeProfiler_PoolingCategory"); // NOI18N
    private static final String CONTAINER_CALLBACKS_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                   "J2EEProjectTypeProfiler_ContainerCallbacksCategory"); // NOI18N
    private static final String PERSISTENCE_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                           "J2EEProjectTypeProfiler_PersistenceCategory"); // NOI18N
    private static final String JDBC_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                    "J2EEProjectTypeProfiler_JdbcCategory"); // NOI18N
    private static final String CONNECTION_MGMT_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                               "J2EEProjectTypeProfiler_ConnectionMgmtCategory"); // NOI18N
    private static final String STATEMENTS_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                          "J2EEProjectTypeProfiler_StatementsCategory"); // NOI18N
    private static final String JPA_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                   "J2EEProjectTypeProfiler_JpaCategory"); // NOI18N
    private static final String HIBERNATE_CATEGORY = NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                         "J2EEProjectTypeProfiler_HibernateCategory"); // NOI18N
                                                                                                                       // -----
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler.j2ee"); // NOI18N
    private static final String J2EE_WEBPROJECT_NAMESPACE_40 = "http://www.netbeans.org/ns/web-project/1"; // NOI18N
    private static final String J2EE_WEBPROJECT_NAMESPACE_4x = "http://www.netbeans.org/ns/web-project/2"; // NOI18N
    private static final String J2EE_WEBPROJECT_NAMESPACE_41 = "http://www.netbeans.org/ns/web-project/3"; // NOI18N
    private static final String J2EE_WEBPROJECT_NAMESPACE_50 = "http://www.netbeans.org/ns/web-project/3"; // NOI18N // same as for NB 41
    private static final String J2EE_EJBJARPROJECT_NAMESPACE_50 = "http://www.netbeans.org/ns/j2ee-ejbjarproject/3"; // NOI18N
    private static final String J2EE_EARPROJECT_NAMESPACE_50 = "http://www.netbeans.org/ns/j2ee-earproject/2"; // NOI18N
    private static final String STANDARD_IMPORT_STRING = "<import file=\"nbproject/build-impl.xml\"/>"; // NOI18N
    private static final String PROFILER_IMPORT_STRING = "<import file=\"nbproject/profiler-build-impl.xml\"/>"; // NOI18N
    private static final String PROFILE_VERSION_ATTRIBUTE = "version"; // NOI18N
    private static final String VERSION_NUMBER = "0.4"; // NOI18N

    // not very clean, consider implementing differently!
    // stores last generated agent ID
    private static int lastAgentID = -1;

    // stores last used agent port
    private static int lastAgentPort = 5140;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private SelectProfilingTask.SettingsConfigurator configurator;
    private LoadGenPanel loadGenConfig = null;

    private String loadGenPath = null;
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(LoadGenPanel.PATH)) {
                loadGenPath = (String) evt.getNewValue();
            }
        }
    };


    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public J2EEProjectTypeProfiler() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static boolean isEjbProject(final Project project) {
        final AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);

        Element e = aux.getConfigurationFragment("data", J2EE_EJBJARPROJECT_NAMESPACE_50, true); // NOI18N // is EJB Project in NB50?

        if (e == null) {
            return false; // not EJB Project
        }

        return true;
    }

    public static boolean isEnterpriseAppProject(final Project project) {
        final AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);

        Element e = aux.getConfigurationFragment("data", J2EE_EARPROJECT_NAMESPACE_50, true); // NOI18N // is Enterprise App Project in NB50?

        if (e == null) {
            return false; // not Enterprise App Project
        }

        return true;
    }

    public static int getLastAgentID() {
        return lastAgentID;
    }

    public static int getLastAgentPort() {
        return lastAgentPort;
    }

    public static String getServerInstanceID(final Project project) {
        J2eeModuleProvider serverInstanceModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);

        if (serverInstanceModuleProvider == null) {
            return null;
        }

        return serverInstanceModuleProvider.getServerInstanceID();
    }

    // --- ProjectTypeProfiler implementation ------------------------------------------------------------------------------
    public static boolean isSupportedProject(final Project project) {
        return isWebProject(project) || isEjbProject(project) || isEnterpriseAppProject(project);
    }

    public static boolean isWebProject(final Project project) {
        final AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);

        Element e = aux.getConfigurationFragment("data", J2EE_WEBPROJECT_NAMESPACE_50, true); // NOI18N // is Web Project in NB50?

        if (e == null) {
            e = aux.getConfigurationFragment("data", J2EE_WEBPROJECT_NAMESPACE_40, true); // NOI18N // is Web Project in NB40?
        }

        if (e == null) {
            e = aux.getConfigurationFragment("data", J2EE_WEBPROJECT_NAMESPACE_4x, true); // NOI18N // is Web Project in NB4x?
        }

        if (e == null) {
            e = aux.getConfigurationFragment("data", J2EE_WEBPROJECT_NAMESPACE_41, true); // NOI18N // is Web Project in NB41?
        }

        if (e == null) {
            return false; // not Web Project
        }

        return true;
    }

//    public boolean isFileObjectSupported(final Project project, final FileObject fo) {
//        return ((WebProjectUtils.isJSP(fo) && WebProjectUtils.isWebDocumentSource(fo, project)) // jsp from /web directory
//               || (WebProjectUtils.isHttpServlet(fo) && WebProjectUtils.isWebJavaSource(fo, project)
//                  && WebProjectUtils.isMappedServlet(fo, project, true))); // mapped servlet from /src directory
//    }

    public String getProfilerTargetName(final Project project, final FileObject buildScript, final int type,
                                        final FileObject profiledClass) {
        switch (type) {
            case TARGET_PROFILE:
                return "profile-j2ee"; // NOI18N
            case TARGET_PROFILE_TEST:
                return null; // not currently supported // "profile-test"; // NOI18N
            case TARGET_PROFILE_TEST_SINGLE:
                return "profile-test-single"; // NOI18N
            default:
                return null;
        }
    }

    public boolean isProfilingSupported(final Project project) {
        return isSupportedProject(project);
    }

    public JavaPlatform getProjectJavaPlatform(Project project) {
        String serverInstanceID = getServerInstanceID(project);

        if (serverInstanceID == null) {
            return null;
        }

        return getServerJavaPlatform(serverInstanceID);
    }

    public static JavaPlatform getServerJavaPlatform(String serverInstanceID) {
        J2eePlatform j2eePlatform = getJ2eePlatform(serverInstanceID);

        if (j2eePlatform == null) {
            return null;
        }

        return j2eePlatform.getJavaPlatform();
    }

    public JComponent getAdditionalConfigurationComponent(Project project) {
        if (loadGenConfig == null) {
            //      Set<String> extSet = new HashSet<String>();
            //      extSet.add("jmx");
            loadGenConfig = new LoadGenPanel();
            //      loadGenConfig.setStartDir(FileUtil.toFile(project.getProjectDirectory()));
            //      loadGenConfig.setSupportedExtensions(extSet);
            loadGenConfig.addPropertyChangeListener(LoadGenPanel.PATH, WeakListeners.propertyChange(pcl, loadGenConfig));

            //      loadGenPath = loadGenConfig.getSelectedScript();
        }

        loadGenConfig.attach(project);

        return loadGenConfig;
    }

//    public ClientUtils.SourceCodeSelection[] getDefaultRootMethods(Project project, FileObject profiledClassFile,
//                                                                   boolean profileUnderlyingFramework,
//                                                                   String[][] projectPackagesDescr) {
//        if (profileUnderlyingFramework) {
//            return new ClientUtils.SourceCodeSelection[0]; // Server doesn't know its main method
//        } else {
//            // Profile Project or Profile Single
//            if (profiledClassFile == null) {
//                // Profile Project, extract root methods from the project
//                Set<ClientUtils.SourceCodeSelection> roots = new HashSet<ClientUtils.SourceCodeSelection>(Arrays.asList(ProjectUtilities
//                                                                                                                        .getProjectDefaultRoots(project,
//                                                                                                                                                projectPackagesDescr)));
//                ClientUtils.SourceCodeSelection[] jsps = WebProjectUtils.getJSPRootMethods(project, true); // TODO: needs to be computed also for subprojects!
//                roots.addAll(Arrays.asList(jsps));
//
//                return roots.toArray(new ClientUtils.SourceCodeSelection[roots.size()]);
//            } else {
//                // Profile Single, provide correct root methods
//                if (WebProjectUtils.isJSP(profiledClassFile)) {
//                    // TODO: create list of jsp-specific methods (execute & all used Beans)
//                    return ProjectUtilities.getProjectDefaultRoots(project, projectPackagesDescr);
//                } else {
//                    String profiledClass = SourceUtils.getToplevelClassName(profiledClassFile);
//
//                    return new ClientUtils.SourceCodeSelection[] { new ClientUtils.SourceCodeSelection(profiledClass, "<all>", "") }; // NOI18N // Covers all innerclasses incl. anonymous innerclasses
//                }
//            }
//        }
//    }

    public SelectProfilingTask.SettingsConfigurator getSettingsConfigurator() {
        if (configurator == null) {
            configurator = new DefaultSettingsConfigurator() {
                    public LoadGenPanel getCustomSettingsPanel() {
                        if (isAttach() || isModify()) {
                            return null; // TODO: would be better to show LoadGenPanel disabled for Modify Profile
                        }

                        if (loadGenConfig == null) {
                            loadGenConfig = new LoadGenPanel();
                            loadGenConfig.addPropertyChangeListener(LoadGenPanel.PATH,
                                                                    WeakListeners.propertyChange(pcl, loadGenConfig));
                        }

                        loadGenConfig.attach(getProject());

                        return loadGenConfig;
                    }

                    public void loadCustomSettings(Properties properties) {
                        if (loadGenConfig != null) {
                            loadGenConfig.loadCustomSettings(properties);
                        }
                    }
                    ;
                    public void storeCustomSettings(Properties properties) {
                        if (loadGenConfig != null) {
                            loadGenConfig.storeCustomSettings(properties);
                        }
                    }
                    ;
                };
        }

        return configurator;
    }

    public boolean checkProjectCanBeProfiled(final Project project, final FileObject profiledClassFile) {
        // Unsupported project type
        if (!isProfilingSupported(project)) {
            return false;
        }

        // Check if server supports profiling
        J2eePlatform j2eePlatform = getJ2eePlatform(project);

        if (j2eePlatform == null) {
            NetBeansProfiler.getDefaultNB().displayError(NO_SERVER_FOUND_MSG);

            return false;
        }

        if (!j2eePlatform.supportsProfiling()) {
            // Server doesn't support profiling
            ProfilerDialogs.notify(new NotifyDescriptor.Message(PROFILING_NOT_SUPPORTED_MSG, NotifyDescriptor.WARNING_MESSAGE));

            return false;
        }

        // Web Project running on supported server
        if (profiledClassFile == null) {
            // Profile project
            return true;
        } else {
            // Profile single
            return isFileObjectSupported(project, profiledClassFile);
        }
    }

    public boolean checkProjectIsModifiedForProfiler(final Project project) {
        if (ProjectUtilities.isProfilerIntegrated(project)) {
            return true; // already modified by this version, nothing more to do
        }

        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        String caption = MessageFormat.format(MODIFY_BUILDSCRIPT_CAPTION, new Object[] { projectName });
        String message = MessageFormat.format(MODIFY_BUILDSCRIPT_MSG, new Object[] { projectName, "build-before-profiler.xml" }); // NOI18N
        if (ProfilerDialogs.notify(new NotifyDescriptor(message, caption, NotifyDescriptor.OK_CANCEL_OPTION,
                                                        NotifyDescriptor.INFORMATION_MESSAGE, new Object[] { NotifyDescriptor.OK_OPTION,
                                                        NotifyDescriptor.CANCEL_OPTION }, NotifyDescriptor.OK_OPTION)) != NotifyDescriptor.OK_OPTION) {
            return false; // cancelled by the user
        }

        // not yet modified for profiler => create profiler-build-impl & modify build.xml and project.xml
        final Element profilerFragment = XMLUtil.createDocument("ignore", null, null, null)
                                                .createElementNS(ProjectUtilities.PROFILER_NAME_SPACE, "data"); // NOI18N
        profilerFragment.setAttribute(PROFILE_VERSION_ATTRIBUTE, VERSION_NUMBER); // NOI18N

        ProjectUtils.getAuxiliaryConfiguration(project).putConfigurationFragment(profilerFragment, false);

        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException e1) {
            err.notify(e1);
            ProfilerLogger.log(e1);

            return false;
        }

        // we are going to regenerate the build script in one of 3 cases:
        // 1. it has not been generated yet
        // 2. the profiler version has been changed (see above)
        // 3. the stylesheet changed (usually should be caught by 2.)
        final GeneratedFilesHelper gfh = new GeneratedFilesHelper(project.getProjectDirectory());
        int flags = gfh.getBuildScriptState("nbproject/profiler-build-impl.xml",
                                            J2EEProjectTypeProfiler.class.getResource("profiler-build-impl.xsl")); // NOI18N

        if (((flags & GeneratedFilesHelper.FLAG_MISSING) != 0) || ((flags & GeneratedFilesHelper.FLAG_OLD_STYLESHEET) != 0)) {
            try {
                if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0) {
                    if (ProfilerDialogs.notify(new NotifyDescriptor.Confirmation(MessageFormat.format(REGENERATE_BUILDSCRIPT_MSG,
                                                                                                          new Object[] {
                                                                                                              "profiler-build-impl.xml"
                                                                                                          }), // NOI18N
                                                                                     NotifyDescriptor.OK_CANCEL_OPTION)) != NotifyDescriptor.OK_OPTION) {
                        return false;
                    }
                }

                gfh.generateBuildScriptFromStylesheet("nbproject/profiler-build-impl.xml",
                                                      J2EEProjectTypeProfiler.class.getResource("profiler-build-impl.xsl")); // NOI18N
            } catch (IOException e1) {
                err.notify(ErrorManager.WARNING, e1);

                return false;
            }
        }

        final String buildScript = ProjectUtilities.getProjectBuildScript(project);

        if (buildScript == null) {
            ProfilerDialogs.notify(new NotifyDescriptor.Message(MessageFormat.format(CANNOT_FIND_BUILDSCRIPT_MSG,
                                                                                     new Object[] { "build.xml" }), // NOI18N
                                                                NotifyDescriptor.ERROR_MESSAGE));

            return false;
        }

        if (!ProjectUtilities.backupBuildScript(project)) {
            if (ProfilerDialogs.notify(new NotifyDescriptor.Confirmation(CANNOT_BACKUP_BUILDSCRIPT_MSG,
                                                                             NotifyDescriptor.OK_CANCEL_OPTION,
                                                                             NotifyDescriptor.WARNING_MESSAGE)) != NotifyDescriptor.OK_OPTION) {
                return false; // cancelled by the user
            }
        }

        final StringBuffer newDataBuffer = new StringBuffer(buildScript.length() + 200);
        final int importIndex = buildScript.indexOf(STANDARD_IMPORT_STRING);

        if (importIndex == -1) {
            // notify the user that the build script cannot be modified, and he should perform the change himself
            ProfilerDialogs.notify(new NotifyDescriptor.Message(MessageFormat.format(MODIFY_BUILDSCRIPT_MANUALLY_MSG,
                                                                                     new Object[] {
                                                                                         "build.xml",
                                                                                         "<import file=\"nbproject/profiler-build-impl.xml\"/>"
                                                                                     }), // NOI18N
                                                                NotifyDescriptor.WARNING_MESSAGE));

            return false;
        }

        String indent = ""; // NOI18N
        int idx = importIndex - 1;

        while (idx >= 0) {
            if (buildScript.charAt(idx) == ' ') {
                indent = " " + indent; // NOI18N
            } else if (buildScript.charAt(idx) == '\t') {
                indent = "\t" + indent; // NOI18N
            } else {
                break;
            }

            idx--;
        }

        newDataBuffer.append(buildScript.substring(0, importIndex + STANDARD_IMPORT_STRING.length() + 1));
        newDataBuffer.append("\n"); // NOI18N
        newDataBuffer.append(indent);
        newDataBuffer.append(PROFILER_IMPORT_STRING);
        newDataBuffer.append(buildScript.substring(importIndex + STANDARD_IMPORT_STRING.length() + 1));

        final FileObject buildFile = getProjectBuildScript(project);
        FileLock lock = null;
        OutputStreamWriter writer = null;

        try {
            lock = buildFile.lock();
            writer = new OutputStreamWriter(buildFile.getOutputStream(lock), "UTF-8"); // NOI18N // According to Issue 65557, build.xml uses UTF-8, not default encoding!
            writer.write(newDataBuffer.toString());
        } catch (FileNotFoundException e1) {
            err.notify(e1);
            ProfilerLogger.log(e1);
        } catch (IOException e1) {
            err.notify(e1);
            ProfilerLogger.log(e1);
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                }
            }
        }

        return true;
    }

    public void configurePropertiesForProfiling(final Properties props, final Project project, final FileObject profiledClassFile) {
        initAntPlatform(project, props);
        // set forceRestart
        props.setProperty("profiler.j2ee.serverForceRestart", "true"); // NOI18N
                                                                       // set timeout

        props.setProperty("profiler.j2ee.serverStartupTimeout", "300000"); // NOI18N
                                                                           // set agent id

        props.setProperty("profiler.j2ee.agentID", "-Dnbprofiler.agentid=" + new Integer(generateAgentID()).toString()); // NOI18N // sets lastAgentID
                                                                                                                         // redirect profiler.info.jvmargs to profiler.info.jvmargs.extra
                                                                                                                         // NOTE: disabled as a workaround for Issue 102323, needs to be fixed in order to restore the OOME detection functionality!

        String jvmArgs = props.getProperty("profiler.info.jvmargs"); // NOI18N

        if ((jvmArgs != null) && (jvmArgs.trim().length() > 0)) {
            props.setProperty("profiler.info.jvmargs.extra", jvmArgs);
        }

        // fix agent startup arguments
        JavaPlatform javaPlatform = getJavaPlatformFromAntName(project, props);
        props.setProperty("profiler.info.javaPlatform", javaPlatform.getProperties().get("platform.ant.name")); // set the used platform ant property

        String javaVersion = IDEUtils.getPlatformJDKVersion(javaPlatform);
        String localPlatform = IntegrationUtils.getLocalPlatform(IDEUtils.getPlatformArchitecture(javaPlatform));

        if (javaVersion.equals(CommonConstants.JDK_15_STRING)) {
            // JDK 1.5 used
            props.setProperty("profiler.info.jvmargs.agent", // NOI18N
                              IntegrationUtils.getProfilerAgentCommandLineArgs(localPlatform, IntegrationUtils.PLATFORM_JAVA_50,
                                                                               false,
                                                                               ProfilerIDESettings.getInstance().getPortNo()));
        } else {
            // JDK 1.6 or later used
            props.setProperty("profiler.info.jvmargs.agent", // NOI18N
                              IntegrationUtils.getProfilerAgentCommandLineArgs(localPlatform, IntegrationUtils.PLATFORM_JAVA_60,
                                                                               false,
                                                                               ProfilerIDESettings.getInstance().getPortNo()));
        }

        generateAgentPort(); // sets lastAgentPort

        if (loadGenPath != null) {
            props.setProperty("profiler.loadgen.path", loadGenPath); // TODO factor out "profiler.loadgen.path" to a constant
        }

        if (profiledClassFile == null) {
            return;
        }

        if (WebProjectUtils.isJSP(profiledClassFile)) {
            props.put("client.urlPart", WebProjectUtils.getJSPFileContext(project, profiledClassFile, false)); // NOI18N
        } else if (WebProjectUtils.isHttpServlet(profiledClassFile)) {
            String servletAddress = null;
            Collection<Document> ddos = WebProjectUtils.getDeploymentDescriptorDocuments(project, true);

            for (Document dd : ddos) {
                String mapping = WebProjectUtils.getServletMapping(profiledClassFile, dd);

                if ((mapping != null) && (mapping.length() > 0)) {
                    servletAddress = mapping;

                    break;
                }
            }

            if (servletAddress != null) {
                ServletUriPanel uriPanel = new ServletUriPanel(servletAddress);
                DialogDescriptor desc = new DialogDescriptor(uriPanel,
                                                             org.openide.util.NbBundle.getMessage(J2EEProjectTypeProfiler.class,
                                                                                                  "TTL_setServletExecutionUri"),
                                                             true, // NOI18N
                                                             new Object[] {
                                                                 DialogDescriptor.OK_OPTION,
                                                                 new javax.swing.JButton(SKIP_BUTTON_NAME) {
                        public java.awt.Dimension getPreferredSize() {
                            return new java.awt.Dimension(super.getPreferredSize().width + 16, super.getPreferredSize().height);
                        }
                    }
                                                             }, DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN, null,
                                                             null);
                Object res = ProfilerDialogs.notify(desc);

                if (res.equals(NotifyDescriptor.YES_OPTION)) {
                    servletAddress = uriPanel.getServletUri();
                }

                props.put("client.urlPart", servletAddress); // NOI18N
            }
        }
        String profiledClass = SourceUtils.getToplevelClassName(profiledClassFile);
        props.setProperty("profile.class", profiledClass); //NOI18N
        // include it in javac.includes so that the compile-single picks it up
        final String clazz = FileUtil.getRelativePath(ProjectUtilities.getRootOf(
                ProjectUtilities.getSourceRoots(project),profiledClassFile), 
                profiledClassFile);
        props.setProperty("javac.includes", clazz); //NOI18N
    }

    // --- Profiler SPI support --------------------------------------------------------------------------------------------
    public static int generateAgentID() {
        int newAgentID = generateAgentNumber(); // generate new agent ID

        while (newAgentID == lastAgentID) {
            newAgentID = generateAgentNumber(); // ensure that it's different from previous ID
        }

        lastAgentID = newAgentID; // assign new agent ID

        return getLastAgentID();
    }

    public static JavaPlatform generateAgentJavaPlatform(String serverInstanceID) {
        JavaPlatform platform = IDEUtils.getJavaPlatformByName(ProfilerIDESettings.getInstance().getJavaPlatformForProfiling());
        JavaPlatform projectPlatform = getServerJavaPlatform(serverInstanceID);

        if (platform == null) { // should use the one defined in project
            platform = projectPlatform;

            if ((platform == null) || !MiscUtils.isSupportedJVM(platform.getSystemProperties())) {
                platform = JavaPlatformSelector.getDefault().selectPlatformToUse();

                if (platform == null) {
                    return null;
                }
            }
        }

        return platform;
    }

    public static int generateAgentPort() {
        lastAgentPort = ProfilerIDESettings.getInstance().getPortNo(); // should be reimplemented, may return different port than the passed by AntActions to target JVM

        return getLastAgentPort();
    }

//    public SimpleFilter computePredefinedInstrumentationFilter(Project project, SimpleFilter predefinedInstrFilter,
//                                                               String[][] projectPackagesDescr) {
//        SimpleFilter retValue;
//
//        retValue = super.computePredefinedInstrumentationFilter(project, predefinedInstrFilter, projectPackagesDescr);
//
//        boolean recurse = predefinedInstrFilter == ProjectUtilities.FILTER_PROJECT_SUBPROJECTS_ONLY;
//        ClientUtils.SourceCodeSelection[] jspMethods = WebProjectUtils.getJSPRootMethods(project, recurse); // TODO: needs to be computed also for subprojects!
//
//        if (jspMethods != null) {
//            StringBuffer buffer = new StringBuffer(jspMethods.length * 30);
//
//            if (retValue != null) {
//                buffer.append(retValue.getFilterValue()).append(' '); // NOI18N
//            }
//
//            for (int i = 0; i < jspMethods.length; i++) {
//                buffer.append(jspMethods[i].getClassName()).append(' '); // NOI18N
//            }
//
//            retValue.setFilterValue(buffer.toString().trim());
//        }
//
//        return retValue;
//    }

    public void setupProjectSessionSettings(final Project project, final SessionSettings ss) {
        // settings required for code fragment profiling
        final PropertyEvaluator pp = getProjectProperties(project);
        ss.setMainClass(""); // NOI18N

        String appArgs = pp.getProperty("application.args"); // NOI18N
        ss.setMainArgs((appArgs != null) ? appArgs : ""); // NOI18N

        String runCP = pp.getProperty("build.classes.dir"); // NOI18N
        ss.setMainClassPath((runCP != null)
                            ? MiscUtils.getAbsoluteFilePath(runCP,
                                                            FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath()) : ""); // NOI18N
        ss.setJVMArgs(""); // NOI18N
        ss.setWorkingDir(""); // NOI18N
    }

    public boolean supportsSettingsOverride() {
        return true; // supported for J2EE project
    }

    public boolean supportsUnintegrate(Project project) {
        return true;
    }

    public void unintegrateProfiler(Project project) {
        ProjectUtilities.unintegrateProfiler(project);
    }

    private static J2eePlatform getJ2eePlatform(final Project project) {
        String serverInstanceID = getServerInstanceID(project);

        if (serverInstanceID == null) {
            return null;
        }

        return getJ2eePlatform(serverInstanceID);
    }

    private static J2eePlatform getJ2eePlatform(String serverInstanceID) {
        return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
    }

    private static JavaPlatform getJavaPlatformFromAntName(Project project, Properties props) {
        String javaPlatformAntName = props.getProperty("profiler.info.javaPlatform"); // NOI18N
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();

        if (javaPlatformAntName.equals("default_platform")) {
            return jpm.getDefaultPlatform();
        }

        JavaPlatform[] platforms = jpm.getPlatforms(null, new Specification("j2se", null)); //NOI18N

        for (int i = 0; i < platforms.length; i++) {
            JavaPlatform platform = platforms[i];
            String antName = platform.getProperties().get("platform.ant.name"); // NOI18N

            if (antName.equals(javaPlatformAntName)) {
                return platform;
            }
        }

        return null;
    }

    // --- Private methods -------------------------------------------------------------------------------------------------
    private static int generateAgentNumber() {
        return (int) (Math.random() * (float) Integer.MAX_VALUE);
    }

    private static void initAntPlatform(Project project, Properties props) {
        String javaPlatformAntName = props.getProperty("profiler.info.javaPlatform"); // NOI18N

        if (javaPlatformAntName == null) {
            JavaPlatform platform = null;
            J2eePlatform j2eepf = getJ2eePlatform(project); // try to get the J2EE Platform

            if (j2eepf == null) {
                platform = JavaPlatformManager.getDefault().getDefaultPlatform(); // no J2EE Platform sepcified; use the IDE default JVM platform
            } else {
                platform = j2eepf.getJavaPlatform(); // use the J2EE Platform specified JVM platform
            }

            props.setProperty("profiler.info.javaPlatform", platform.getProperties().get("platform.ant.name")); // set the used platform ant property
        }
    }

    private PropertyEvaluator getProjectProperties(final Project project) {
        final Properties privateProps = new Properties();
        final Properties projectProps = new Properties();
        final Properties userPropsProps = new Properties();

        final FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final FileObject projectPropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final File userPropsFile = InstalledFileLocator.getDefault().locate("build.properties", null, false); // NOI18N

        ProjectManager.mutex().readAccess(new Runnable() {
                public void run() {
                    // the order is 1. private, 2. project, 3. user to reflect how Ant handles property definitions (immutable, once set property value cannot be changed)
                    if (privatePropsFile != null) {
                        try {
                            final InputStream is = privatePropsFile.getInputStream();

                            try {
                                privateProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }

                    if (projectPropsFile != null) {
                        try {
                            final InputStream is = projectPropsFile.getInputStream();

                            try {
                                projectProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }

                    if (userPropsFile != null) {
                        try {
                            final InputStream is = new BufferedInputStream(new FileInputStream(userPropsFile));

                            try {
                                userPropsProps.load(is);
                            } finally {
                                is.close();
                            }
                        } catch (IOException e) {
                            err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                }
            });

        PropertyEvaluator pe = PropertyUtils.sequentialPropertyEvaluator(null,
                                                                         new PropertyProvider[] {
                                                                             new J2EEProjectTypeProfiler.MyPropertyProvider(privateProps),
                                                                             new J2EEProjectTypeProfiler.MyPropertyProvider(userPropsProps),
                                                                             new J2EEProjectTypeProfiler.MyPropertyProvider(projectProps)
                                                                         });

        return pe;
    }

    private void addJspMarker(MethodMarker marker, Mark mark, Project project) {
        ClientUtils.SourceCodeSelection[] jspmethods = WebProjectUtils.getJSPRootMethods(project, true);

        if (jspmethods != null) {
            for (int i = 0; i < jspmethods.length; i++) {
                marker.addMethodMark(jspmethods[i].getClassName(), jspmethods[i].getMethodName(),
                                     jspmethods[i].getMethodSignature(), mark);
            }
        }
    }

//    private void setupMarks(final Project project) {
//        MarkerMethodBuilder builder = project.getLookup().lookup(MarkerMethodBuilder.class);
//        marker = builder.buildMarker(project);
//        ClassMarker cMarker = new ClassMarker();
//        MethodMarker mMarker = new MethodMarker();
//        PackageMarker pMarker = new PackageMarker();
//
//        String[] lcMethodNames = new String[] { "init", "destroy" }; // NOI18N
//        HierarchicalMark webMark = new HierarchicalMark("WEB", WEB_CONTAINER_CATEGORY, getMarkHierarchyRoot()); // NOI18N
//        HierarchicalMark webLifecycleMark = new HierarchicalMark("WEB/LIFECYCLE", LIFECYCLE_CATEGORY, webMark); // NOI18N
//        HierarchicalMark webExecMark = new HierarchicalMark("WEB/EXECUTION", EXECUTIVE_CATEGORY, webMark); // NOI18N
//        HierarchicalMark jspExecMark = new HierarchicalMark("WEB/EXECUTION/JSP", JSP_CATEGORY, webExecMark); // NOI18N
//        HierarchicalMark jspTagExecMark = new HierarchicalMark("WEB/EXECUTION/TAG", TAGS_CATEGORY, webExecMark); // NOI18N
//        HierarchicalMark servletExecMark = new HierarchicalMark("WEB/EXECUTION/SERVLET", SERVLETS_CATEGORY, webExecMark); // NOI18N
//        HierarchicalMark filterExecMark = new HierarchicalMark("WEB/EXECUTION/FILTER", FILTERS_CATEGORY, webExecMark); // NOI18N
//        HierarchicalMark webListenerMark = new HierarchicalMark("WEB/LISTENER", LISTENERS_CATEGORY, webMark); // NOI18N
//        HierarchicalMark ejbMark = new HierarchicalMark("EJB", EJB_CONTAINER_CATEGORY, getMarkHierarchyRoot()); // NOI18N
//        HierarchicalMark ejbPoolMark = new HierarchicalMark("EJB/POOL/CALLBACK", POOLING_CATEGORY, ejbMark); // NOI18N
//        HierarchicalMark ejbLifecycleMark = new HierarchicalMark("EJB/LIFECYCLE/CALLBACK", CONTAINER_CALLBACKS_CATEGORY, ejbMark); // NOI18N
//        HierarchicalMark ejbPersistenceMark = new HierarchicalMark("EJB/PERSISTENCE/CALLBACK", PERSISTENCE_CATEGORY, ejbMark); // NOI18N
//        HierarchicalMark ejbExecutionMarks = new HierarchicalMark("EJB/EXECUTION", EXECUTIVE_CATEGORY, ejbMark); // NOI18N
//        HierarchicalMark dbMark = new HierarchicalMark("DB", PERSISTENCE_CATEGORY, getMarkHierarchyRoot()); // NOI18N
//        HierarchicalMark jdbcMark = new HierarchicalMark("DB/JDBC", JDBC_CATEGORY, dbMark); // NOI18N
//        HierarchicalMark dbConnectionMark = new HierarchicalMark("DB/CONN", CONNECTION_MGMT_CATEGORY, jdbcMark); // NOI18N
//        HierarchicalMark dbStatementsMark = new HierarchicalMark("DB/EXEC", STATEMENTS_CATEGORY, jdbcMark); // NOI18N
//        HierarchicalMark jpaMark = new HierarchicalMark("DB/JPA", JPA_CATEGORY, dbMark); // NOI18N
//        HierarchicalMark hibernateMark = new HierarchicalMark("DB/HIB", HIBERNATE_CATEGORY, dbMark); // NOI18N
//        HierarchicalMark soapMark = new HierarchicalMark("SOAP", SOAP_CATEGORY, getMarkHierarchyRoot()); //NOI18N
//        HierarchicalMark soapParsingMark = new HierarchicalMark("SOAP/PARSING", SOAP_PROTOCOL_PARSING_CATEGORY, soapMark); // NOI18N
//        HierarchicalMark soapSerializationMark = new HierarchicalMark("SOAP/SERIALIZATION", SOAP_SERIALIZATION_CATEGORY, soapMark); // NOI18N
//        HierarchicalMark soapEndpointMark = new HierarchicalMark("SOAP/ENDPOINT", SOAP_ENDPOINT_CATEGORY, soapMark); // NOI18N
//        HierarchicalMark soapReplyMark = new HierarchicalMark("SOAP/REPLY", SOAP_REPLY_CATEGORY, soapMark); // NOI18N
//
//        addInterfaceMarker(mMarker, "javax.servlet.Servlet", lcMethodNames, true, webLifecycleMark, project); // NOI18N
//        addInterfaceMarkers(mMarker, new String[] { "javax.servlet.ServletConfig", // NOI18N
//            "javax.servlet.FilterConfig" }, // NOI18N
//                            webLifecycleMark, project);
//
//        addInterfaceMarker(mMarker, "javax.servlet.Filter", lcMethodNames, true, webLifecycleMark, project); // NOI18N
//
//        addInterfaceMarker(mMarker, "javax.servlet.ServletInputStream", webListenerMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.servlet.ServletOutputStream", webListenerMark, project); // NOI18N
//
//        addInterfaceMarker(mMarker, "javax.servlet.Filter", lcMethodNames, false, filterExecMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.servlet.Servlet", lcMethodNames, false, servletExecMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.servlet.FilterChain", filterExecMark, project); // NOI18N
//
//        addJspMarker(mMarker, jspExecMark, project);
//
//        addInterfaceMarker(mMarker, "javax.servlet.jsp.tagext.Tag", jspTagExecMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.servlet.jsp.tagext.TagSupport", jspTagExecMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.servlet.jsp.tagext.BodyTagSupport", jspTagExecMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.servlet.jsp.tagext.SimpleTagSupport", jspTagExecMark, project); // NOI18N
//
//        addInterfaceMarkers(mMarker,
//                            new String[] {
//                                "javax.servlet.http.HttpSessionListener", // NOI18N
//        "javax.servlet.http.HttpSessionAttributeListener", // NOI18N
//        "javax.servlet.http.HttpSessionActivationListener", // NOI18N
//        "javax.servlet.ServletContextListener", // NOI18N
//        "javax.servlet.ServletContextAttributeListener", // NOI18N
//        "javax.servlet.ServletRequestListener", // NOI18N
//        "javax.servlet.ServletRequestAttributeListener"
//                            }, // NOI18N
//                            webListenerMark, project);
//
//        addInterfaceMarker(mMarker, "javax.ejb.SessionBean", new String[] { "ejbActivate", "ejbPassivate" }, true, ejbPoolMark,
//                           project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.ejb.EntityBean", new String[] { "ejbActivate", "ejbPassivate" }, true, ejbPoolMark,
//                           project); // NOI18N
//
//        addInterfaceMarker(mMarker, "javax.ejb.SessionBean", new String[] { "ejbRemove", "setSessionContext" }, true,
//                           ejbLifecycleMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.ejb.EntityBean",
//                           new String[] { "ejbRemove", "setEntityContext", "unsetEntityContext" }, true, ejbLifecycleMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.ejb.MessageDrivenBean", new String[] { "ejbRemove", "setMessageDrivenContext" }, true,
//                           ejbLifecycleMark, project); // NOI18N
//
//        addInterfaceMarker(mMarker, "javax.ejb.EntityBean", new String[] { "ejbLoad", "ejbStore" }, true, ejbPersistenceMark,
//                           project); // NOI18N
//
//        addInterfaceMarker(mMarker, "java.sql.DriverManager", new String[] { "getConnection" }, true, dbConnectionMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "java.sql.Connection", dbConnectionMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "java.sql.DataSource", dbConnectionMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "java.sql.Statement", dbStatementsMark, project); // NOI18N
//
//        addInterfaceMarker(mMarker, "javax.persistence.EntityManager", jpaMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.persistence.Query", jpaMark, project); // NOI18N
//
//        addInterfaceMarkers(mMarker,
//                            new String[] {
//                                "org.hibernate.impl.SessionImpl", // NOI18N
//        "org.hibernate.impl.AbstractQueryImpl", // NOI18N
//        "org.hibernate.impl.FetchingScrollableResultsImpl", // NOI18N
//        "org.hibernate.impl.FilterImpl", // NOI18N
//        "org.hibernate.impl.CriteriaImpl", // NOI18N
//        "org.hibernate.impl.IteratorImpl", // NOI18N
//        "org.hibernate.impl.QueryImpl", // NOI18N
//        "org.hibernate.impl.ScrollableResultsImpl", // NOI18N
//        "org.hibernate.impl.SessionFactoryImpl", // NOI18N
//        "org.hibernate.impl.SessionFactoryObjectFactory", // NOI18N
//        "org.hibernate.impl.SQLQueryImpl", // NOI18N
//        "org.hibernate.impl.StatelessSessionImpl" // NOI18N
//                            }, hibernateMark, project);
//
//        pMarker.addPackageMark("org.hibernate.impl", hibernateMark); // NOI18N
//
//        marker = new CompositeMarker();
//        ((CompositeMarker) marker).addMarker(mMarker);
//        ((CompositeMarker) marker).addMarker(pMarker);
//
//        MethodNameFormatterFactory.getDefault().registerFormatter(jspExecMark, new JSPNameFormatter());
//    }
}
