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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.common;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 * @since 1.21
 */
public final class SharabilityUtility {

    public static final String DEFAULT_LIBRARIES_FILENAME = "nblibraries.properties";

    private SharabilityUtility() {
        super();
    }

    public static String getLibraryLocation(String librariesDir) {
        String librariesDefinition = librariesDir;
        if (librariesDefinition != null) {
            if (!librariesDefinition.endsWith(File.separator)) {
                librariesDefinition += File.separatorChar;
            }
            librariesDefinition += SharabilityUtility.DEFAULT_LIBRARIES_FILENAME;
        }
        return librariesDefinition;
    }

    public static String[] getServerInstances(File location, Library library) {
        if (library == null) {
            return new String[] {};
        }

        String name = library.getName();
        final Deployment deployment = Deployment.getDefault();
        List<String> instances = new ArrayList<String>();
        for (String id : deployment.getServerInstanceIDs()) {
            String propertyId = PropertyUtils.getUsablePropertyName(
                    deployment.getServerDisplayName(deployment.getServerID(id)));

            if (name.startsWith(propertyId)) {
                String suffix = name.substring(propertyId.length());
                if ("".equals(suffix) || suffix.matches("-\\d+")) { // NOI18N
                    instances.add(id);
                }
            }
        }
        return instances.toArray(new String[instances.size()]);
    }

    /**
     * Method makes sure that sharable project always has a correct version of 
     * CopyLibs library. As described in issue 146736 CopyLibs library
     * was enhanced in NetBeans version 6.5 and needs to be automatically upgraded
     * which is ensured by this method as well.
     * @since org.netbeans.modules.java.api.common/1 1.5
     */
    public static void makeSureProjectHasCopyLibsLibrary(final AntProjectHelper helper, final ReferenceHelper refHelper) {
        if (!helper.isSharableProject() || refHelper.getProjectLibraryManager() == null) {
            return;
        }
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run()  {
                Library lib = refHelper.getProjectLibraryManager().getLibrary("CopyLibs");
                if (lib == null) {
                    try {
                        refHelper.copyLibrary(LibraryManager.getDefault().getLibrary("CopyLibs")); // NOI18N
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    // #146736 - check that NB6.5 version of CopyLibs is available:
                    List<URL> roots = lib.getContent("classpath"); // NOI18N
                    // CopyFiles.class was not present in NB 6.1
                    boolean version61 = org.netbeans.spi.java.classpath.support.ClassPathSupport.
                            createClassPath(roots.toArray(new URL[roots.size()])).
                            findResource("org/netbeans/modules/java/j2seproject/copylibstask/CopyFiles.class") == null; // NOI18N
                    if (!version61) {
                        return;
                    }
                    // update 6.1 version of CopyLibs library to the latest one:
                    try {
                        refHelper.getProjectLibraryManager().removeLibrary(lib);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    // perform removal of library files in separate try/catch:
                    // if removal fails we can still add CopyLibs library
                    try {
                        FileObject parent = null;
                        for (URL u : roots) {
                            URL u2 = FileUtil.getArchiveFile(u);
                            if (u2 != null) {
                                u = u2;
                            }
                            FileObject fo = URLMapper.findFileObject(u);
                            if (fo != null) {
                                if (parent == null) {
                                    parent = fo.getParent();
                                }
                                fo.delete();
                            }
                        }
                        if (parent != null && parent.getChildren().length == 0 && parent.getNameExt().equals("CopyLibs")) { // NOI18N
                            parent.delete();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    try {
                        // this should recreate latest version of library
                        refHelper.copyLibrary(LibraryManager.getDefault().getLibrary("CopyLibs")); // NOI18N
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }
    
}
