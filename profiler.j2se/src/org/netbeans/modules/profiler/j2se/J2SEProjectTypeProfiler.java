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

package org.netbeans.modules.profiler.j2se;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.modules.profiler.AbstractProjectTypeProfiler;
import org.netbeans.modules.profiler.ui.ProfilerDialogs;
import org.netbeans.modules.profiler.utils.AppletSupport;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.*;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;


/**
 * @author Tomas Hurka
 * @author Ian Formanek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.ProjectTypeProfiler.class)
public final class J2SEProjectTypeProfiler extends AbstractProjectTypeProfiler {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

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
    private static final String MODIFY_BUILDSCRIPT_CAPTION = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                                 "J2SEProjectTypeProfiler_ModifyBuildScriptCaption"); // NOI18N
    private static final String MODIFY_BUILDSCRIPT_MSG = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                             "J2SEProjectTypeProfiler_ModifyBuildScriptMsg"); // NOI18N
    private static final String REGENERATE_BUILDSCRIPT_MSG = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                                 "J2SEProjectTypeProfiler_RegenerateBuildScriptMsg"); // NOI18N
    private static final String CANNOT_FIND_BUILDSCRIPT_MSG = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                                  "J2SEProjectTypeProfiler_CannotFindBuildScriptMsg"); // NOI18N
    private static final String CANNOT_BACKUP_BUILDSCRIPT_MSG = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                                    "J2SEProjectTypeProfiler_CannotBackupBuildScriptMsg"); // NOI18N
    private static final String MODIFY_BUILDSCRIPT_MANUALLY_MSG = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                                      "J2SEProjectTypeProfiler_ModifyBuildScriptManuallyMsg"); // NOI18N
    private static final String PROJECT_CATEGORY = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                       "J2SEProjectTypeProfiler_ProjectCategory"); // NOI18N
    private static final String LISTENERS_CATEGORY = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                         "J2SEProjectTypeProfiler_ListenersCategory"); // NOI18N
    private static final String PAINTERS_CATEGORY = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                        "J2SEProjectTypeProfiler_PaintersCategory"); // NOI18N
    private static final String IO_CATEGORY = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                  "J2SEProjectTypeProfiler_IoCategory"); // NOI18N
    private static final String FILES_CATEGORY = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                     "J2SEProjectTypeProfiler_FilesCategory"); // NOI18N
    private static final String SOCKETS_CATEGORY = NbBundle.getMessage(J2SEProjectTypeProfiler.class,
                                                                       "J2SEProjectTypeProfiler_SocketsCategory"); // NOI18N
                                                                                                                   // -----
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler.j2se"); // NOI18N
    private static final String J2SE_PROJECT_NAMESPACE_40 = "http://www.netbeans.org/ns/j2se-project/1"; // NOI18N
    private static final String J2SE_PROJECT_NAMESPACE_41 = "http://www.netbeans.org/ns/j2se-project/2"; // NOI18N
    private static final String J2SE_PROJECT_NAMESPACE_50 = "http://www.netbeans.org/ns/j2se-project/3"; // NOI18N
    private static final String STANDARD_IMPORT_STRING = "<import file=\"nbproject/build-impl.xml\"/>"; // NOI18N
    private static final String PROFILER_IMPORT_STRING = "<import file=\"nbproject/profiler-build-impl.xml\"/>"; // NOI18N
    private static final String PROFILE_VERSION_ATTRIBUTE = "version"; // NOI18N
    private static final String VERSION_NUMBER = "0.9.1"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    private String mainClassSetManually = null; // used for case when the main class is not set in project and user is prompted for it

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isFileObjectSupported(final Project project, final FileObject fo) {
        if (!"java".equals(fo.getExt()) && !"class".equals(fo.getExt())) {
            return false; // NOI18N
        }

        return SourceUtils.isRunnable(fo);
    }

    public String getProfilerTargetName(final Project project, final FileObject buildScript, final int type,
                                        final FileObject profiledClassFile) {
        switch (type) {
            case TARGET_PROFILE:
                return "profile"; // NOI18N
            case TARGET_PROFILE_SINGLE:

                if (SourceUtils.isApplet(profiledClassFile)) {
                    return "profile-applet"; // NOI18N
                } else {
                    return "profile-single"; // NOI18N
                }
            case TARGET_PROFILE_TEST:
                return null; // not currently supported // "profile-test"; // NOI18N
            case TARGET_PROFILE_TEST_SINGLE:
                return "profile-test-single"; // NOI18N
            default:
                return null;
        }
    }

    // --- ProjectTypeProfiler implementation ------------------------------------------------------------------------------
    public boolean isProfilingSupported(final Project project) {
        final AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);

        Element e = aux.getConfigurationFragment("data", J2SE_PROJECT_NAMESPACE_40, true); // NOI18N

        if (e == null) {
            e = aux.getConfigurationFragment("data", J2SE_PROJECT_NAMESPACE_41, true); // NOI18N
        }

        if (e == null) {
            e = aux.getConfigurationFragment("data", J2SE_PROJECT_NAMESPACE_50, true); // NOI18N
        }

        return (e != null);
    }

    public JavaPlatform getProjectJavaPlatform(Project project) {
        PropertyEvaluator props = getProjectProperties(project);
        String platformName = props.getProperty("platform.active"); // NOI18N

        if (platformName == null) {
            return null; // not provided for some reason
        }

        JavaPlatformManager jpm = JavaPlatformManager.getDefault();

        if (platformName.equals("default_platform")) {
            return jpm.getDefaultPlatform(); // NOI18N
        }

        JavaPlatform[] platforms = jpm.getPlatforms(null, new Specification("j2se", null)); // NOI18N

        for (int i = 0; i < platforms.length; i++) {
            JavaPlatform platform = platforms[i];
            String antName = (String) platform.getProperties().get("platform.ant.name"); // NOI18N

            if (antName.equals(platformName)) {
                return platform;
            }
        }

        return null;
    }

    public boolean checkProjectCanBeProfiled(final Project project, final FileObject profiledClassFile) {
        if (profiledClassFile == null) {
            final PropertyEvaluator pp = getProjectProperties(project);
            String profiledClass = pp.getProperty("main.class"); // NOI18N

            if ((profiledClass == null) || "".equals(profiledClass)
                    || (SourceUtils.findFileObjectByClassName(profiledClass, project) == null)) { // NOI18N
                mainClassSetManually = ProjectUtilities.selectMainClass(project, null, ProjectUtilities.getProjectName(project),
                                                                        -1);

                //        Profiler.getDefault().displayError("No class to profile. To set up main class for a Project, go to \n" +
                //            "Project | Properties and select the main class in the Running Project section.");
                if (mainClassSetManually == null) {
                    return false;
                }
            }

            // the following code to check the main class is way too slow to perform here
            /*      if (profiledClass != null && !"".equals(profiledClass)) {
               final FileObject fo = SourceUtilities.findFileForClass(new String[] { profiledClass, "" }, true);
               if (fo == null) res = false;
               else res = (SourceUtilities.hasMainMethod(fo) || SourceUtilities.isApplet(fo));
               } */
            return true;
        } else {
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
                                                            NotifyDescriptor.INFORMATION_MESSAGE,
                                                            new Object[] {
                                                                NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION
                                                            }, NotifyDescriptor.OK_OPTION)) != NotifyDescriptor.OK_OPTION) {
            return false; // cancelled by the user
        }

        // we are going to regenerate the build script in one of 3 cases:
        // 1. it has not been generated yet
        // 2. the profiler version has been changed (see above)
        // 3. the stylesheet changed (usually should be caught by 2.)
        final GeneratedFilesHelper gfh = new GeneratedFilesHelper(project.getProjectDirectory());
        int flags = gfh.getBuildScriptState("nbproject/profiler-build-impl.xml",
                                            J2SEProjectTypeProfiler.class.getResource("profiler-build-impl.xsl")); // NOI18N

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
                                                      J2SEProjectTypeProfiler.class.getResource("profiler-build-impl.xsl")); // NOI18N
            } catch (IOException e1) {
                err.notify(ErrorManager.WARNING, e1);

                return false;
            }
        }

        // store info about profiler with project's auxiliary configuration
        final Element profilerFragment = XMLUtil.createDocument("ignore", null, null, null)
                                                .createElementNS(ProjectUtilities.PROFILER_NAME_SPACE, "data"); // NOI18N
        profilerFragment.setAttribute(PROFILE_VERSION_ATTRIBUTE, VERSION_NUMBER);
        ProjectUtils.getAuxiliaryConfiguration(project).putConfigurationFragment(profilerFragment, false);

        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException e1) {
            err.notify(e1);
            ProfilerLogger.log(e1);

            return false;
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

        final FileObject buildFile = ProjectUtilities.findBuildFile(project); // NOI18N
        FileLock lock = null;
        OutputStreamWriter writer = null;

        try {
            lock = buildFile.lock();
            writer = new OutputStreamWriter(buildFile.getOutputStream(lock), "UTF-8"); // NOI18N // According to Issue 65557, build.xml uses UTF-8, not default encoding!
            writer.write(newDataBuffer.toString());
        } catch (FileNotFoundException e1) {
            ProfilerLogger.log(e1);
            err.notify(e1);
        } catch (IOException e1) {
            ProfilerLogger.log(e1);
            err.notify(e1);
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
        if (profiledClassFile == null) {
            if (mainClassSetManually != null) {
                props.put("main.class", mainClassSetManually); // NOI18N
                mainClassSetManually = null;
            }
        } else {
            // In case the class to profile is explicitely selected (profile-single)
            // 1. specify profiled class name
            if (SourceUtils.isApplet(profiledClassFile)) {
                String jvmargs = props.getProperty("run.jvmargs"); // NOI18N

                URL url = null;

                // do this only when security policy is not set manually
                if ((jvmargs == null) || !(jvmargs.indexOf("java.security.policy") > 0)) { //NOI18N

                    PropertyEvaluator projectProps = getProjectProperties(project);
                    String buildDirProp = projectProps.getProperty("build.dir"); //NOI18N
                                                                                 // TODO [M9] what if buildDirProp is null?

                    FileObject buildFolder = ProjectUtilities.getOrCreateBuildFolder(project, buildDirProp);

                    AppletSupport.generateSecurityPolicy(project.getProjectDirectory(), buildFolder);

                    if ((jvmargs == null) || (jvmargs.length() == 0)) {
                        props.setProperty("run.jvmargs",
                                          "-Djava.security.policy=" + FileUtil.toFile(buildFolder).getPath() + File.separator
                                          + "applet.policy"); //NOI18N
                    } else {
                        props.setProperty("run.jvmargs",
                                          jvmargs + " -Djava.security.policy=" + FileUtil.toFile(buildFolder).getPath()
                                          + File.separator + "applet.policy"); //NOI18N
                    }
                }

                if (profiledClassFile.existsExt("html") || profiledClassFile.existsExt("HTML")) { //NOI18N
                    url = ProjectUtilities.copyAppletHTML(project, getProjectProperties(project), profiledClassFile, "html"); //NOI18N
                } else {
                    url = ProjectUtilities.generateAppletHTML(project, getProjectProperties(project), profiledClassFile);
                }

                if (url == null) {
                    return; // TODO: fail?
                }

                props.setProperty("applet.url", url.toString()); // NOI18N
            } else {
                final String profiledClass = SourceUtils.getToplevelClassName(profiledClassFile);
                props.setProperty("profile.class", profiledClass); //NOI18N
            }

            // 2. include it in javac.includes so that the compile-single picks it up
            final String clazz = FileUtil.getRelativePath(ProjectUtilities.getRootOf(ProjectUtilities.getSourceRoots(project),
                                                                                     profiledClassFile), profiledClassFile);
            props.setProperty("javac.includes", clazz); //NOI18N
        }
    }

    public void setupProjectSessionSettings(final Project project, final SessionSettings ss) {
        final PropertyEvaluator pp = getProjectProperties(project);

        if (mainClassSetManually == null) {
            String mainClass = pp.getProperty("main.class"); // NOI18N
            ss.setMainClass((mainClass != null) ? mainClass : ""); // NOI18N
        } else {
            ss.setMainClass(mainClassSetManually);
        }

        // is this all really necessary???
        String appArgs = pp.getProperty("application.args"); // NOI18N
        ss.setMainArgs((appArgs != null) ? appArgs : ""); // NOI18N

        String runCP = pp.getProperty("run.classpath"); // NOI18N
        ss.setMainClassPath((runCP != null) ? runCP : ""); // NOI18N

        String jvmArgs = pp.getProperty("run.jvmargs"); // NOI18N
        ss.setJVMArgs((jvmArgs != null) ? jvmArgs : ""); // NOI18N
    }

    public boolean supportsSettingsOverride() {
        return true; // supported for J2SE project
    }

    public boolean supportsUnintegrate(Project project) {
        return true;
    }

    public void unintegrateProfiler(Project project) {
        ProjectUtilities.unintegrateProfiler(project);
    }

    // --- Private methods -------------------------------------------------------------------------------------------------
    private PropertyEvaluator getProjectProperties(final Project project) {
        final Properties privateProps = new Properties();
        final Properties projectProps = new Properties();
        final Properties userPropsProps = new Properties();
        final Properties configProps = new Properties();

        final FileObject privatePropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        final FileObject projectPropsFile = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final File userPropsFile = InstalledFileLocator.getDefault().locate("build.properties", null, false); // NOI18N
        final FileObject configPropsFile = project.getProjectDirectory().getFileObject("nbproject/private/config.properties"); // NOI18N
        final FileObject configPropsDir = project.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N

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

                    if ((configPropsDir != null) && (configPropsFile != null)) {
                        try {
                            InputStream is = configPropsFile.getInputStream();
                            Properties activeConfigProps = new Properties();

                            try {
                                activeConfigProps.load(is);

                                String activeConfig = activeConfigProps.getProperty("config"); // NOI18N

                                if ((activeConfig != null) && (activeConfig.length() > 0)) {
                                    FileObject configSpecPropFile = configPropsDir.getFileObject(activeConfig + ".properties"); // NOI18N

                                    if (configSpecPropFile != null) {
                                        InputStream configSpecIn = configSpecPropFile.getInputStream();
                                        try {
                                            configProps.load(configSpecIn);
                                        } finally {
                                            configSpecIn.close();
                                        }
                                    }
                                }
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
                                                                             new MyPropertyProvider(configProps),
                                                                             new MyPropertyProvider(privateProps),
                                                                             new MyPropertyProvider(userPropsProps),
                                                                             new MyPropertyProvider(projectProps)
                                                                         });

        return pe;
    }

//    private void setupMarks(final Project project) {
//        PackageMarker pMarker = new PackageMarker();
//        MethodMarker mMarker = new MethodMarker();
//
//        HierarchicalMark uiMark = new HierarchicalMark("UI", "Generic UI", getMarkHierarchyRoot()); // NOI18N
//        HierarchicalMark listenerMark = new HierarchicalMark("UI/LISTENER", LISTENERS_CATEGORY, uiMark); // NOI18N
//        HierarchicalMark painterMark = new HierarchicalMark("UI/PAINTER", PAINTERS_CATEGORY, uiMark); // NOI18N
//        HierarchicalMark ioMark = new HierarchicalMark("IO", IO_CATEGORY, getMarkHierarchyRoot()); // NOI18N
//        HierarchicalMark fileMark = new HierarchicalMark("IO/FILE", FILES_CATEGORY, ioMark); // NOI18N
//        HierarchicalMark socketMark = new HierarchicalMark("IO/SOCKET", SOCKETS_CATEGORY, ioMark); // NOI18N
//
//        String[] listenerIfcs = new String[] {
//                                    "java.awt.event.ActionListener", // NOI18N
//        "java.awt.event.AdjustmentListener", // NOI18N
//        "java.awt.event.AWTEventListener", // NOI18N
//        "java.awt.event.ComponentListener", // NOI18N
//        "java.awt.event.ContainerListener", // NOI18N
//        "java.awt.event.FocusListener", // NOI18N
//        "java.awt.event.HierarchyBoundsListener", // NOI18N
//        "java.awt.event.HierarchyListener", // NOI18N
//        "java.awt.event.InputMethodListener", // NOI18N
//        "java.awt.event.InputMethodListener", // NOI18N
//        "java.awt.event.ItemListener", // NOI18N
//        "java.awt.event.KeyListener", // NOI18N
//        "java.awt.event.MouseListener", // NOI18N
//        "java.awt.event.MouseMotionListener", // NOI18N
//        "java.awt.event.MouseWheelListener", // NOI18N
//        "java.awt.event.WindowFocusListener", // NOI18N
//        "java.awt.event.WindowListener", // NOI18N
//        "java.awt.event.WindowStateListener", // NOI18N
//        "java.awt.event.TextListener", // NOI18N
//        "javax.swing.event.AncestorListener", // NOI18N
//        "javax.swing.event.CaretListener", // NOI18N
//        "javax.swing.event.CellEditorListener", // NOI18N
//        "javax.swing.event.ChangeListener", // NOI18N
//        "javax.swing.event.DocumentListener", // NOI18N
//        "javax.swing.event.HyperlinkListener", // NOI18N
//        "javax.swing.event.InternalFrameListener", // NOI18N
//        "javax.swing.event.ListDataListener", // NOI18N
//        "javax.swing.event.ListSelectionListener", // NOI18N
//        "javax.swing.event.MenuDragMouseListener", // NOI18N
//        "javax.swing.event.MenuKeyListener", // NOI18N
//        "javax.swing.event.MenuListener", // NOI18N
//        "javax.swing.event.MouseInputListener", // NOI18N
//        "javax.swing.event.PopupMenuListener", // NOI18N
//        "javax.swing.event.TableColumnModelListener", // NOI18N
//        "javax.swing.event.TableModelListener", // NOI18N
//        "javax.swing.event.TreeExpansionListener", // NOI18N
//        "javax.swing.event.TreeModelListener", // NOI18N
//        "javax.swing.event.TreeSelectionListener", // NOI18N
//        "javax.swing.event.TreeWillExpandListener", // NOI18N
//        "javax.swing.event.UndoableEditListener" // NOI18N
//                                };
//
//        addInterfaceMarkers(mMarker, listenerIfcs, listenerMark, project);
//        addInterfaceMarker(mMarker, "java.awt.LightweightDispatcher", new String[] { "dispatchEvent" }, true, listenerMark,
//                           project); // NOI18N
//        addInterfaceMarker(mMarker, "javax.swing.JComponent",
//                           new String[] {
//                               "repaint", "paint", "paintBorder", "paintChildren", "paintComponent", "paintImmediately", "print",
//                               "printAll", "printBorder", "printChildren", "printComponent"
//                           }, true, painterMark, project); // NOI18N
//        addInterfaceMarker(mMarker, "java.awt.Component", new String[] { "paint", "paintAll", "print", "printAll" }, true,
//                           painterMark, project); // NOI18N
//
//        String[] ioFileClasses = new String[] {
//                                     "java.io.FileInputStream", // NOI18N
//        "java.io.FileOuptutStream", // NOI18N
//        "java.io.FileReader", // NOI18N
//        "java.io.FileWriter" // NOI18N
//                                 };
//        String[] ioSocketClasses = new String[] { "java.nio.SocketChannel" // NOI18N
//                                   };
//
//        String[] ioFileRestrictMethods = new String[] {
//                                             "read", // NOI18N
//        "write", // NOI18N
//        "reset", // NOI18N
//        "skip", // NOI18N
//        "flush" // NOI18N
//                                         };
//        String[] ioSocketRestrictMethods = new String[] { "open", // NOI18N
//            "read", // NOI18N
//            "write" // NOI18N
//                                           };
//
//        addInterfaceMarkers(mMarker,
//                            new String[] {
//                                "java.io.InputStreamReader", "java.io.OutputStreamWriter", "java.io.InputStream",
//                                "java.io.OutputStream"
//                            }, ioFileRestrictMethods, true, ioMark, project); // NOI18N
//        addInterfaceMarkers(mMarker, ioFileClasses, ioFileRestrictMethods, true, fileMark, project);
//        addInterfaceMarkers(mMarker, ioSocketClasses, ioSocketRestrictMethods, true, socketMark, project);
//
//        marker = new CompositeMarker();
//        ((CompositeMarker) marker).addMarker(mMarker);
//        ((CompositeMarker) marker).addMarker(pMarker);
//    }
}
