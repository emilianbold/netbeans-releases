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
import java.net.URI;
import java.net.URL;
import java.util.*;
import org.netbeans.modules.j2ee.common.project.classpath.ClassPathSupport;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk, Radko Najman, David Konecny
 */
public class ProjectProperties {
    
    /** XXX to be deleted when introduced in AntPropertyHeleper API
     */    
    public static String getAntPropertyName( String property ) {
        if ( property != null && 
             property.startsWith( "${" ) && // NOI18N
             property.endsWith( "}" ) ) { // NOI18N
            return property.substring( 2, property.length() - 1 ); 
        }
        else {
            return property;
        }
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
    
}
