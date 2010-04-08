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

package org.netbeans.modules.profiler.nbmodule;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.modules.profiler.AbstractProjectTypeProfiler;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Element;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.integration.IntegrationUtils;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;
import org.openide.xml.XMLUtil;


/**
 * A class providing basic support for profiling free-form projects.
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.ProjectTypeProfiler.class)
public final class NbModuleProjectTypeProfiler extends AbstractProjectTypeProfiler {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String NBMODULE_PROJECT_NAMESPACE_2 = "http://www.netbeans.org/ns/nb-module-project/2"; // NOI18N
    private static final String NBMODULE_PROJECT_NAMESPACE_3 = "http://www.netbeans.org/ns/nb-module-project/3"; // NOI18N
    private static final String NBMODULE_SUITE_PROJECT_NAMESPACE = "http://www.netbeans.org/ns/nb-module-suite-project/1"; // NOI18N
    private static final String PROJECT_CATEGORY = NbBundle.getMessage(NbModuleProjectTypeProfiler.class,
                                                                       "NbModuleProjectTypeProfiler_ProjectCategory"); // NOI18N
    private static final String LISTENERS_CATEGORY = NbBundle.getMessage(NbModuleProjectTypeProfiler.class,
                                                                         "NbModuleProjectTypeProfiler_ListenersCategory"); // NOI18N
    private static final String PAINTERS_CATEGORY = NbBundle.getMessage(NbModuleProjectTypeProfiler.class,
                                                                        "NbModuleProjectTypeProfiler_PaintersCategory"); // NOI18N
    private static final String IO_CATEGORY = NbBundle.getMessage(NbModuleProjectTypeProfiler.class,
                                                                  "NbModuleProjectTypeProfiler_IoCategory"); // NOI18N
    private static final String FILES_CATEGORY = NbBundle.getMessage(NbModuleProjectTypeProfiler.class,
                                                                     "NbModuleProjectTypeProfiler_FilesCategory"); // NOI18N
    private static final String SOCKETS_CATEGORY = NbBundle.getMessage(NbModuleProjectTypeProfiler.class,
                                                                       "NbModuleProjectTypeProfiler_SocketsCategory"); // NOI18N
    // -----

    private static final String TEST_TYPE_UNIT = "unit"; // NOI18N
    private static final String TEST_TYPE_QA_FUNCTIONAL = "qa-functional"; // NOI18N

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isFileObjectSupported(final Project project, final FileObject fo) {
        return SourceUtils.isTest(fo); // profile single only for tests
    }

    public String getProfilerTargetName(final Project project, final FileObject buildScript, final int type,
                                        final FileObject profiledClass) {
        switch (type) {
            case TARGET_PROFILE:
                return "profile"; //NOI18N
            case TARGET_PROFILE_TEST_SINGLE:
                return "profile-test-single-nb"; //NOI18N
            default:
                return null; // not applicable for NBM projects
        }
    }

    // --- ProjectTypeProfiler implementation ------------------------------------------------------------------------------
    public boolean isProfilingSupported(final Project project) {
        final AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);

        Element e = aux.getConfigurationFragment("data", NBMODULE_PROJECT_NAMESPACE_2, true); // NOI18N

        if (e == null) {
            e = aux.getConfigurationFragment("data", NBMODULE_PROJECT_NAMESPACE_3, true); // NOI18N
        }

        if (e == null) {
            e = aux.getConfigurationFragment("data", NBMODULE_SUITE_PROJECT_NAMESPACE, true); // NOI18N
        }

        return (e != null);
    }

    public FileObject getProjectBuildScript(Project project) {
        return project.getProjectDirectory().getFileObject("build.xml"); //NOI18N
    }

    public JavaPlatform getProjectJavaPlatform(Project project) {
        final AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        FileObject projectDir = project.getProjectDirectory();

        if (aux.getConfigurationFragment("data", NBMODULE_SUITE_PROJECT_NAMESPACE, true) != null) { // NOI18N
                                                                                                    // NetBeans suite
                                                                                                    // ask first subproject for its JavaPlatform

            SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
            Set subProjects;

            if (ProfilerLogger.isDebug()) {
                ProfilerLogger.debug("NB Suite " + projectDir.getPath()); //NOI18N
            }

            if (spp == null) {
                return null;
            }

            subProjects = spp.getSubprojects();

            if (subProjects.isEmpty()) {
                return null;
            }

            return getProjectJavaPlatform((Project) subProjects.iterator().next());
        }

        ClassPath bootCp = ClassPath.getClassPath(projectDir, ClassPath.BOOT);
        List bootCpEntries = bootCp.entries();

        if (ProfilerLogger.isDebug()) {
            ProfilerLogger.debug("Boot CP " + bootCp); //NOI18N
        }

        if (ProfilerLogger.isDebug()) {
            ProfilerLogger.debug("File " + projectDir.getPath()); //NOI18N
        }

        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification("j2se", null)); // NOI18N

        for (int i = 0; i < platforms.length; i++) {
            JavaPlatform platform = platforms[i];

            if (bootCpEntries.equals(platform.getBootstrapLibraries().entries())) {
                if (ProfilerLogger.isDebug()) {
                    ProfilerLogger.debug("Platform " + platform.getDisplayName()); //NOI18N
                }

                return platform;
            }
        }

        if (ProfilerLogger.isDebug()) {
            ProfilerLogger.debug("Platform null"); //NOI18N
        }

        return null;
    }

    public boolean checkProjectCanBeProfiled(final Project project, final FileObject profiledClassFile) {
        return true; // no check performed in nbmodule project
    }

    public boolean checkProjectIsModifiedForProfiler(final Project project) {
        return true;
    }

    private static String getTestType(FileObject testFile) {
        String testPath = testFile.getPath();
        if (testPath.contains(TEST_TYPE_QA_FUNCTIONAL)) return TEST_TYPE_QA_FUNCTIONAL;
        else return TEST_TYPE_UNIT;
    }

    public void configurePropertiesForProfiling(final Properties props, final Project project, final FileObject profiledClassFile) {
        if (profiledClassFile != null) {
            final String profiledClass = SourceUtils.getToplevelClassName(profiledClassFile);
            props.setProperty("profile.class", profiledClass); //NOI18N
            // Set for all cases (incl. Profile Project, Profile File) but should only
            // be taken into account when profiling single test
            props.setProperty("test.type", getTestType(profiledClassFile)); //NOI18N
        }
        
        String agentArg = props.getProperty("profiler.info.jvmargs.agent"); //NOI18N
        if (agentArg.indexOf(' ') != -1) { //NOI18N
            if (Utilities.isUnix()) {
                // Profiler is installed in directory with space on Unix (Linux, Solaris, Mac OS X)
                // create temporary link in /tmp directory and use it instead of directory with space
                String libsDir = Profiler.getDefault().getLibsDir();
                props.setProperty("profiler.info.jvmargs.agent", IntegrationUtils.fixLibsDirPath(libsDir, agentArg)); //NOI18N
            } else if (Utilities.isWindows() && isNbSourceModule(project)) {
                // Profiler is installed in directory with space on Windows
                // surround the whole -agentpath argument with quotes for NB source module
                agentArg = "\"" + agentArg + "\""; //NOI18N
                props.setProperty("profiler.info.jvmargs.agent", agentArg); //NOI18N
            }
        }
    }
    
    /**
     * Returns true if the provided Project is a NB source module, false otherwise.
     * 
     * @param project Project to be checked.
     * @return true if the provided Project is a NB source module, false otherwise.
     */
    private boolean isNbSourceModule(Project project) {
        // Resolve project.xml
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        
        // Guess the namespace
        String namespace = NBMODULE_PROJECT_NAMESPACE_3;
        // Try to resolve nb-module-project/3 (current version in NB sources)
        Element e = aux.getConfigurationFragment("data", namespace, true); // NOI18N
        // Not a nb-module-project/3, can still be nb-module-project/2 or a suite
        if (e == null) {
            // Try to resolve nb-module-project/2 (just for compatibility)
            namespace = NBMODULE_PROJECT_NAMESPACE_2;
            e = aux.getConfigurationFragment("data", namespace, true); // NOI18N
            // Project is a NB module suite - not a NB source module
            if (e == null) return false;
        }
        
        // Module is a NB module suite component, not a NB source module
        if (XMLUtil.findElement(e, "suite-component", namespace) != null) return false; // NOI18N
        
        // Module is a NB module suite component, not a NB source module
        if (XMLUtil.findElement(e, "standalone", namespace) != null) return false; // NOI18N
        
        // Module is a NB source module (neither suite component nor standalone)
        return true;
    }
}
