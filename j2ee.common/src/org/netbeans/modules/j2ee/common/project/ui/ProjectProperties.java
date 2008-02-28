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

package org.netbeans.modules.j2ee.common.project.ui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Radko Najman, David Konecny
 */
public final class ProjectProperties {

    public static final String JAVA_EE_5 = "1.5"; // NOI18N
    public static final String J2EE_1_4 = "1.4"; // NOI18N
    public static final String J2EE_1_3 = "1.3"; // NOI18N

    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    public static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath"; // NOI18N
    public static final String RUN_CLASSPATH = "run.classpath"; // NOI18N
    public static final String RUN_TEST_CLASSPATH = "run.test.classpath"; // NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; //NOI18N
    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N

    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String[] WELL_KNOWN_PATHS = new String[] {
        "${" + JAVAC_CLASSPATH + "}", // NOI18N
        "${" + JAVAC_TEST_CLASSPATH + "}", // NOI18N
        "${" + RUN_CLASSPATH + "}", // NOI18N
        "${" + RUN_TEST_CLASSPATH + "}", // NOI18N
        "${" + BUILD_CLASSES_DIR + "}", // NOI18N
        "${" + BUILD_TEST_CLASSES_DIR + "}" // NOI18N
    };    
   
    // Prefixes and suffixes of classpath
    public static final String ANT_ARTIFACT_PREFIX = "${reference."; // NOI18N

    private static String RESOURCE_ICON_JAR = "org/netbeans/modules/j2ee/common/project/ui/resources/jar.gif"; //NOI18N
    private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/j2ee/common/project/ui/resources/libraries.gif"; //NOI18N
    private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/j2ee/common/project/ui/resources/projectDependencies.gif"; //NOI18N
    private static String RESOURCE_ICON_BROKEN_BADGE = "org/netbeans/modules/j2ee/common/project/ui/resources/brokenProjectBadge.gif"; //NOI18N
    private static String RESOURCE_ICON_SOURCE_BADGE = "org/netbeans/modules/j2ee/common/project/ui/resources/jarSourceBadge.png"; //NOI18N
    private static String RESOURCE_ICON_JAVADOC_BADGE = "org/netbeans/modules/j2ee/common/project/ui/resources/jarJavadocBadge.png"; //NOI18N
    private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/j2ee/common/project/ui/resources/referencedClasspath.gif"; //NOI18N
        
        
    public static ImageIcon ICON_JAR = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_JAR ) );
    public static ImageIcon ICON_LIBRARY = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_LIBRARY ) );
    public static ImageIcon ICON_ARTIFACT  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_ARTIFACT ) );
    public static ImageIcon ICON_BROKEN_BADGE  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_BROKEN_BADGE ) );
    public static ImageIcon ICON_JAVADOC_BADGE  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_JAVADOC_BADGE ) );
    public static ImageIcon ICON_SOURCE_BADGE  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_SOURCE_BADGE ) );
    public static ImageIcon ICON_CLASSPATH  = new ImageIcon( Utilities.loadImage( RESOURCE_ICON_CLASSPATH ) );
    
    /** Store locations of libraries in the classpath param that have more the one
     * file into the properties in the following format:
     * 
     * <ul>
     * <li>libs.foo.classpath.libdir.1=C:/foo
     * <li>libs.foo.classpath.libdirs=1
     * <li>libs.foo.classpath.libfile.1=C:/bar/a.jar
     * <li>libs.foo.classpath.libfile.2=C:/bar/b.jar
     * <li>libs.foo.classpath.libfiles=1
     * </ul>
     * This is needed for the Ant copy task as it cannot copy more the one file
     * and it needs different handling for files and directories.
     * <br>
     * It removes all properties that match this format that were in the {@link #properties}
     * but are not in the {@link #classpath}.
     */
    public static void storeLibrariesLocations (Iterator<ClassPathSupport.Item> classpath, EditableProperties props, FileObject projectFolder) {
        ArrayList exLibs = new ArrayList ();
        Iterator propKeys = props.keySet().iterator();
        while (propKeys.hasNext()) {
            String key = (String) propKeys.next();
            if (key.endsWith(".libdirs") || key.endsWith(".libfiles") || //NOI18N
                    (key.indexOf(".libdir.") > 0) || (key.indexOf(".libfile.") > 0)) { //NOI18N
                exLibs.add(key);
            }
        }
        while (classpath.hasNext()) {
            ClassPathSupport.Item item = (ClassPathSupport.Item)classpath.next();
            ArrayList<String> files = new ArrayList<String>();
            ArrayList<String> dirs = new ArrayList<String>();
            getFilesForItem (item, files, dirs, projectFolder);
            String key;
            if (files.size() > 1 || (files.size()>0 && dirs.size()>0)) {
                String ref = item.getType() == ClassPathSupport.Item.TYPE_LIBRARY ? item.getRaw() : item.getReference();
                for (int i = 0; i < files.size(); i++) {
                    String path = files.get(i);
                    key = CommonProjectUtils.getAntPropertyName(ref)+".libfile." + (i+1); //NOI18N
                    props.setProperty (key, "" + path); //NOI18N
                    exLibs.remove(key);
                }
            }
            if (dirs.size() > 1 || (files.size()>0 && dirs.size()>0)) {
                String ref = item.getType() == ClassPathSupport.Item.TYPE_LIBRARY ? item.getRaw() : item.getReference();
                for (int i = 0; i < dirs.size(); i++) {
                    String path = dirs.get(i);
                    key = CommonProjectUtils.getAntPropertyName(ref)+".libdir." + (i+1); //NOI18N
                    props.setProperty (key, "" + path); //NOI18N
                    exLibs.remove(key);
                }
            }
        }
        Iterator unused = exLibs.iterator();
        while (unused.hasNext()) {
            props.remove(unused.next());
        }
    }
    
    public static void storeLibrariesLocations (final Project project, final AntProjectHelper helper,
            final ClassPathSupport cs, final String[] libUpdaterProperties) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                // intentionally pass null to itemsList() - we do not need additional info to be read
                HashSet set = new HashSet();
                for (String property : libUpdaterProperties) {
                    List wmLibs = cs.itemsList(props.getProperty(property),  null);
                    set.addAll(wmLibs);
                }
                ProjectProperties.storeLibrariesLocations(set.iterator(), props, project.getProjectDirectory());
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
    
    public static final void getFilesForItem (ClassPathSupport.Item item, List<String> files, List<String> dirs, FileObject projectFolder) {
        if (item.isBroken()) {
            return ;
        }
        if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
            List<URL> roots = item.getLibrary().getContent("classpath");  //NOI18N
            for (URL rootUrl : roots) {
                FileObject root = LibrariesSupport.resolveLibraryEntryFileObject(item.getLibrary().getManager().getLocation(), rootUrl);
                
                //file inside library is broken
                if (root == null)
                    continue;
                
                if ("jar".equals(rootUrl.getProtocol())) {  //NOI18N
                    root = FileUtil.getArchiveFile (root);
                }
                File f = FileUtil.toFile(root);
                String path;
                // if global library use absolute path otherwise relative
                if (item.getLibrary().getManager().getLocation() == null) {
                    path = f.getPath();
                } else {
                    path = PropertyUtils.relativizeFile(FileUtil.toFile(projectFolder), FileUtil.toFile(root));
                }
                if (f != null) {
                    if (f.isFile()) {
                        files.add(path); 
                    } else {
                        dirs.add(path);
                    }
                }
            }
        }
        if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
            File root = item.getResolvedFile();
            if (root != null) {
                if (root.isFile()) {
                    files.add(item.getFilePath()); 
                } else {
                    dirs.add(item.getFilePath());
                }
            }
        }
        if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
            String artifactFolder = item.getArtifact().getScriptLocation().getParent();
            URI roots[] = item.getArtifact().getArtifactLocations();
            for (int i = 0; i < roots.length; i++) {
                String root = artifactFolder + File.separator + roots [i];
                if (root.endsWith(File.separator)) {
                    dirs.add(root);
                } else {
                    files.add(root);
                }
            }
        }
    }

    public static ListCellRenderer createClassPathListRendered(PropertyEvaluator evaluator, FileObject projectFolder) {
        return new ClassPathListCellRenderer(evaluator, projectFolder);
    }
    
    public static TableCellRenderer createClassPathTableRendered(PropertyEvaluator evaluator, FileObject projectFolder) {
        return new ClassPathListCellRenderer.ClassPathTableCellRenderer(evaluator, projectFolder);
    }
    
    /**
     * Returns <code>true</code> if the server library is used for j2ee instead
     * of the classpath pointing to the server installation.
     *
     * @param projectProperties project properties
     * @param j2eePlatformClasspathProperty name of the classpath property
     * @return <code>true</code> if the server library is used for j2ee instead
     *             of the classpath pointing to the server installation
     */
    public static boolean isUsingServerLibrary(EditableProperties projectProperties, String j2eePlatformClasspathProperty) {
        String value = projectProperties.getProperty(j2eePlatformClasspathProperty);
        return (value != null && !"".equals(value.trim()));
    }
}
