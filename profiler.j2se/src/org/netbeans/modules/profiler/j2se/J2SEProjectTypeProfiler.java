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
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.modules.profiler.AbstractProjectTypeProfiler;
import org.netbeans.modules.profiler.utils.AppletSupport;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.netbeans.spi.project.support.ant.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;
import org.netbeans.spi.project.ProjectServiceProvider;


/**
 * @author Tomas Hurka
 * @author Ian Formanek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.ProjectTypeProfiler.class, 
                        projectType="org-netbeans-modules-java-j2seproject")
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
    // I18N String constants                                                                                                                   // -----
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler.j2se"); // NOI18N

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
        return true;
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
                    || (SourceUtils.resolveClassByName(profiledClass, project) == null)) { // NOI18N
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
}
