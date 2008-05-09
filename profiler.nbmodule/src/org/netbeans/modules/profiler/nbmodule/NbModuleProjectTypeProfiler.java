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

package org.netbeans.modules.profiler.nbmodule;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.marker.CompositeMarker;
import org.netbeans.lib.profiler.marker.Marker;
import org.netbeans.lib.profiler.marker.MethodMarker;
import org.netbeans.lib.profiler.results.cpu.marking.HierarchicalMark;
import org.netbeans.lib.profiler.results.cpu.marking.Mark;
import org.netbeans.modules.profiler.AbstractProjectTypeProfiler;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;


/**
 * A class providing basic support for profiling free-form projects.
 *
 * @author Tomas Hurka
 * @author Ian Formanek
 */
public final class NbModuleProjectTypeProfiler extends AbstractProjectTypeProfiler {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants

    // -----
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

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HierarchicalMark projectRoot;
    private Marker marker;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isFileObjectSupported(final Project project, final FileObject fo) {
        return SourceUtils.isTest(fo); // profile single only for tests
    }

    @Override
    public HierarchicalMark getMarkHierarchyRoot() {
        if (projectRoot == null) {
            projectRoot = new HierarchicalMark(Mark.DEFAULT_ID, PROJECT_CATEGORY, null); // NOI18N
        }

        return projectRoot;
    }

    public Marker getMethodMarker(Project project) {
        setupMarks(project);

        return marker;
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
        final AuxiliaryConfiguration aux = (AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class);

        if (aux == null) {
            ProfilerLogger.severe("Auxiliary Configuration is null for Project: " + project); // NOI18N

            return false;
        }

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
        final AuxiliaryConfiguration aux = (AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class);
        FileObject projectDir = project.getProjectDirectory();

        if (aux.getConfigurationFragment("data", NBMODULE_SUITE_PROJECT_NAMESPACE, true) != null) { // NOI18N
                                                                                                    // NetBeans suite
                                                                                                    // ask first subproject for its JavaPlatform

            SubprojectProvider spp = (SubprojectProvider) project.getLookup().lookup(SubprojectProvider.class);
            Set subProjects;

            if (ProfilerLogger.isDebug()) {
                ProfilerLogger.debug("NB Suite " + projectDir.getPath());
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
            ProfilerLogger.debug("Boot CP " + bootCp);
        }

        if (ProfilerLogger.isDebug()) {
            ProfilerLogger.debug("File " + projectDir.getPath());
        }

        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(null, new Specification("j2se", null)); // NOI18N

        for (int i = 0; i < platforms.length; i++) {
            JavaPlatform platform = platforms[i];

            if (bootCpEntries.equals(platform.getBootstrapLibraries().entries())) {
                if (ProfilerLogger.isDebug()) {
                    ProfilerLogger.debug("Platform " + platform.getDisplayName());
                }

                return platform;
            }
        }

        if (ProfilerLogger.isDebug()) {
            ProfilerLogger.debug("Platform null");
        }

        return null;
    }

    public boolean checkProjectCanBeProfiled(final Project project, final FileObject profiledClassFile) {
        return true; // no check performed in nbmodule project
    }

    public boolean checkProjectIsModifiedForProfiler(final Project project) {
        return true;
    }

    public void configurePropertiesForProfiling(final Properties props, final Project project, final FileObject profiledClassFile) {
        if (profiledClassFile != null) {
            final String profiledClass = SourceUtils.getToplevelClassName(profiledClassFile);
            props.setProperty("profile.class", profiledClass); //NOI18N
        }

        // not applicable for NBM projects
    }

    private void setupMarks(final Project project) {
//        PackageMarker pMarker = new PackageMarker();
        MethodMarker mMarker = new MethodMarker();

        HierarchicalMark uiMark = new HierarchicalMark("UI", "Generic UI", getMarkHierarchyRoot()); // NOI18N
        HierarchicalMark listenerMark = new HierarchicalMark("UI/LISTENER", LISTENERS_CATEGORY, uiMark); // NOI18N
        HierarchicalMark painterMark = new HierarchicalMark("UI/PAINTER", PAINTERS_CATEGORY, uiMark); // NOI18N
        HierarchicalMark ioMark = new HierarchicalMark("IO", IO_CATEGORY, getMarkHierarchyRoot()); // NOI18N
        HierarchicalMark fileMark = new HierarchicalMark("IO/FILE", FILES_CATEGORY, ioMark); // NOI18N
        HierarchicalMark socketMark = new HierarchicalMark("IO/SOCKET", SOCKETS_CATEGORY, ioMark); // NOI18N

        String[] listenerIfcs = new String[] {
                                    "java.awt.event.ActionListener", // NOI18N
        "java.awt.event.AdjustmentListener", // NOI18N
        "java.awt.event.AWTEventListener", // NOI18N
        "java.awt.event.ComponentListener", // NOI18N
        "java.awt.event.ContainerListener", // NOI18N
        "java.awt.event.FocusListener", // NOI18N
        "java.awt.event.HierarchyBoundsListener", // NOI18N
        "java.awt.event.HierarchyListener", // NOI18N
        "java.awt.event.InputMethodListener", // NOI18N
        "java.awt.event.InputMethodListener", // NOI18N
        "java.awt.event.ItemListener", // NOI18N
        "java.awt.event.KeyListener", // NOI18N
        "java.awt.event.MouseListener", // NOI18N
        "java.awt.event.MouseMotionListener", // NOI18N
        "java.awt.event.MouseWheelListener", // NOI18N
        "java.awt.event.WindowFocusListener", // NOI18N
        "java.awt.event.WindowListener", // NOI18N
        "java.awt.event.WindowStateListener", // NOI18N
        "java.awt.event.TextListener", // NOI18N
        "javax.swing.event.AncestorListener", // NOI18N
        "javax.swing.event.CaretListener", // NOI18N
        "javax.swing.event.CellEditorListener", // NOI18N
        "javax.swing.event.ChangeListener", // NOI18N
        "javax.swing.event.DocumentListener", // NOI18N
        "javax.swing.event.HyperlinkListener", // NOI18N
        "javax.swing.event.InternalFrameListener", // NOI18N
        "javax.swing.event.ListDataListener", // NOI18N
        "javax.swing.event.ListSelectionListener", // NOI18N
        "javax.swing.event.MenuDragMouseListener", // NOI18N
        "javax.swing.event.MenuKeyListener", // NOI18N
        "javax.swing.event.MenuListener", // NOI18N
        "javax.swing.event.MouseInputListener", // NOI18N
        "javax.swing.event.PopupMenuListener", // NOI18N
        "javax.swing.event.TableColumnModelListener", // NOI18N
        "javax.swing.event.TableModelListener", // NOI18N
        "javax.swing.event.TreeExpansionListener", // NOI18N
        "javax.swing.event.TreeModelListener", // NOI18N
        "javax.swing.event.TreeSelectionListener", // NOI18N
        "javax.swing.event.TreeWillExpandListener", // NOI18N
        "javax.swing.event.UndoableEditListener" // NOI18N
                                };

        addInterfaceMarkers(mMarker, listenerIfcs, listenerMark, project);
        addInterfaceMarker(mMarker, "java.awt.LightweightDispatcher", new String[] { "dispatchEvent" }, true, listenerMark,
                           project); // NOI18N
        addInterfaceMarker(mMarker, "javax.swing.JComponent",
                           new String[] {
                               "repaint", "paint", "paintBorder", "paintChildren", "paintComponent", "paintImmediately", "print",
                               "printAll", "printBorder", "printChildren", "printComponent"
                           }, true, painterMark, project); // NOI18N
        addInterfaceMarker(mMarker, "java.awt.Component", new String[] { "paint", "paintAll", "print", "printAll" }, true,
                           painterMark, project); // NOI18N

        String[] ioFileClasses = new String[] {
                                     "java.io.FileInputStream", // NOI18N
        "java.io.FileOuptutStream", // NOI18N
        "java.io.FileReader", // NOI18N
        "java.io.FileWriter" // NOI18N
                                 };
        String[] ioSocketClasses = new String[] { "java.nio.SocketChannel" // NOI18N
                                   };

        String[] ioFileRestrictMethods = new String[] {
                                             "read", // NOI18N
        "write", // NOI18N
        "reset", // NOI18N
        "skip", // NOI18N
        "flush" // NOI18N
                                         };
        String[] ioSocketRestrictMethods = new String[] { "open", // NOI18N
            "read", // NOI18N
            "write" // NOI18N
                                           };

        addInterfaceMarkers(mMarker,
                            new String[] {
                                "java.io.InputStreamReader", "java.io.OutputStreamWriter", "java.io.InputStream",
                                "java.io.OutputStream"
                            }, ioFileRestrictMethods, true, ioMark, project); // NOI18N
        addInterfaceMarkers(mMarker, ioFileClasses, ioFileRestrictMethods, true, fileMark, project);
        addInterfaceMarkers(mMarker, ioSocketClasses, ioSocketRestrictMethods, true, socketMark, project);

        marker = new CompositeMarker();
        ((CompositeMarker) marker).addMarker(mMarker);
//        ((CompositeMarker) marker).addMarker(pMarker);
    }
}
