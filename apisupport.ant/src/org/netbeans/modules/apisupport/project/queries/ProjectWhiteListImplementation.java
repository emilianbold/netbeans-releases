/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.project.queries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkozeny
 */
public class ProjectWhiteListImplementation implements WhiteListQueryImplementation {

    private Project project;

    public ProjectWhiteListImplementation(NbModuleProject project) {
        this.project = project;
    }

    @Override
    public WhiteListImplementation getWhiteList(FileObject file) {
        List<String> publicPackages = new ArrayList<String>();
        List<String> allPackages = new ArrayList<String>();
        List<String> privatePackages = new ArrayList<String>();
        if (project != null) {
            file = project.getProjectDirectory();
        }

        ClassPath classPath = ClassPath.getClassPath(file, ClassPath.COMPILE);
        for (ClassPath.Entry entryIter : classPath.entries()) {
            String publicPackagesStr = null;
            JarFile jar = null;
            FileObject archiveFile = null;
            FileObject entryFile = entryIter.getRoot();
            if (entryFile != null) {
                archiveFile = FileUtil.getArchiveFile(entryFile);
            } else {
                continue;
            }
            try {
                if (archiveFile != null) {
                    jar = new JarFile(FileUtil.toFile(archiveFile));
                } else {
                    continue;
                }

                publicPackagesStr = jar.getManifest().getMainAttributes().getValue("OpenIDE-Module-Public-Packages");

                if (publicPackagesStr != null) {
                    StringTokenizer tokenizer = new StringTokenizer(publicPackagesStr, ", ");
                    while (tokenizer.hasMoreElements()) {
                        String packageIter = tokenizer.nextToken();
                        if (packageIter.endsWith("*")) {
                            packageIter = packageIter.substring(0, packageIter.length() - 1);
                        }
                        if (!publicPackages.contains(packageIter)) {
                            publicPackages.add(packageIter);
                        }
                    }
                }

                for (Enumeration<JarEntry> list = jar.entries(); list.hasMoreElements();) {
                    JarEntry entry = list.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String packageName = entry.getName();
                        packageName = packageName.substring(0, packageName.length() - 6);
                        packageName = packageName.replaceAll("/", ".");

                        if (!allPackages.contains(packageName)) {
                            allPackages.add(packageName);
                        }
                    }

                }

            } catch (IOException e) {
            } finally {
                try {
                    jar.close();
                } catch (IOException ex) {
                }
            }


        }

        for (String allPkgIter : allPackages) {
            boolean contains = false;
            for (String publicPkgIter : publicPackages) {
                if (allPkgIter.startsWith(publicPkgIter)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                privatePackages.add(allPkgIter);
            }
        }

        final List<String> privatePackageList = new ArrayList<String>(privatePackages);

        return new WhiteListImplementation() {
            @Override
            public WhiteListQuery.Result check(ElementHandle<?> element, WhiteListQuery.Operation operation) {
                if (element != null && (element.getKind().isClass() || element.getKind().isInterface())) {
                    String qualifiedName = element.getQualifiedName();
                    for (String privatePkgIter : privatePackageList) {
                        if (qualifiedName.equals(privatePkgIter)) {
                            List<WhiteListQuery.RuleDescription> descs = new ArrayList<WhiteListQuery.RuleDescription>();
                            descs.add(new WhiteListQuery.RuleDescription("Private package dependency access", "Element comes from private package of spec version dependency", ""));
                            return new WhiteListQuery.Result(descs);
                        }
                    }
                }
                return null;
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
            }
        };
    }
}
