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

package org.netbeans.modules.java.api.common.project;

import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Radko Najman, David Konecny
 */
public final class ProjectProperties {

    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    public static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath"; // NOI18N
    public static final String RUN_CLASSPATH = "run.classpath"; // NOI18N
    public static final String RUN_TEST_CLASSPATH = "run.test.classpath"; // NOI18N
    public static final String BUILD_CLASSES_DIR = "build.classes.dir"; //NOI18N
    public static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N

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

    private static String RESOURCE_ICON_JAR = "org/netbeans/modules/java/api/common/project/ui/resources/jar.gif"; //NOI18N
    private static String RESOURCE_ICON_LIBRARY = "org/netbeans/modules/java/api/common/project/ui/resources/libraries.gif"; //NOI18N
    private static String RESOURCE_ICON_ARTIFACT = "org/netbeans/modules/java/api/common/project/ui/resources/projectDependencies.gif"; //NOI18N
    private static String RESOURCE_ICON_BROKEN_BADGE = "org/netbeans/modules/java/api/common/project/ui/resources/brokenProjectBadge.gif"; //NOI18N
    private static String RESOURCE_ICON_SOURCE_BADGE = "org/netbeans/modules/java/api/common/project/ui/resources/jarSourceBadge.png"; //NOI18N
    private static String RESOURCE_ICON_JAVADOC_BADGE = "org/netbeans/modules/java/api/common/project/ui/resources/jarJavadocBadge.png"; //NOI18N
    private static String RESOURCE_ICON_CLASSPATH = "org/netbeans/modules/java/api/common/project/ui/resources/referencedClasspath.gif"; //NOI18N
        
        
    public static ImageIcon ICON_JAR = new ImageIcon( ImageUtilities.loadImage( RESOURCE_ICON_JAR ) );
    public static ImageIcon ICON_LIBRARY = new ImageIcon( ImageUtilities.loadImage( RESOURCE_ICON_LIBRARY ) );
    public static ImageIcon ICON_ARTIFACT  = new ImageIcon( ImageUtilities.loadImage( RESOURCE_ICON_ARTIFACT ) );
    public static ImageIcon ICON_BROKEN_BADGE  = new ImageIcon( ImageUtilities.loadImage( RESOURCE_ICON_BROKEN_BADGE ) );
    public static ImageIcon ICON_JAVADOC_BADGE  = new ImageIcon( ImageUtilities.loadImage( RESOURCE_ICON_JAVADOC_BADGE ) );
    public static ImageIcon ICON_SOURCE_BADGE  = new ImageIcon( ImageUtilities.loadImage( RESOURCE_ICON_SOURCE_BADGE ) );
    public static ImageIcon ICON_CLASSPATH  = new ImageIcon( ImageUtilities.loadImage( RESOURCE_ICON_CLASSPATH ) );

    public static final String INCLUDES = "includes"; // NOI18N
    public static final String EXCLUDES = "excludes"; // NOI18N

}
