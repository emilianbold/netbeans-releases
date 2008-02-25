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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;

/**
 *
 * @author Nick Krasilnikov
 */
public class IncludeResolverImpl extends CsmIncludeResolver {

    public IncludeResolverImpl() {
    }

    @Override
    public String getIncludeDirective(CsmFile currentFile, CsmObject item) {
        if (CsmKindUtilities.isOffsetable(item)) {
            CsmFile file = ((CsmOffsetable) item).getContainingFile();
            if (file.equals(currentFile) || file.isHeaderFile()) {
                return getIncludeDerectiveByFile(currentFile, item);
            } else if (file.isSourceFile() && CsmKindUtilities.isGlobalVariable(item)) {
                Collection<CsmOffsetableDeclaration> decls = file.getProject().findDeclarations(((CsmVariable) item).getUniqueName() + " (EXTERN)"); // NOI18N
                if (!decls.isEmpty()) {
                    return getIncludeDerectiveByFile(currentFile, decls.iterator().next());
                }
            }
        } else {
            System.err.println("not yet handled object " + item);
        }
        return ""; // NOI18N
    }

    // Generates "#include *" string for item
    private String getIncludeDerectiveByFile(CsmFile currentFile, CsmObject item) {
        if (CsmKindUtilities.isOffsetable(item)) {
            if (currentFile instanceof FileImpl) {
                NativeFileItem nativeFile = ((FileImpl) currentFile).getNativeFileItem();
                String incFilePath = ((CsmOffsetable) item).getContainingFile().getAbsolutePath().toString();

                StringBuilder includeDirective = new StringBuilder("#include "); // NOI18N

                if (nativeFile != null) {
                    if (isSystemHeader(currentFile, ((CsmOffsetable) item).getContainingFile())) {
                        String bestSystemPath = getRelativePath(nativeFile.getSystemIncludePaths(), incFilePath);
                        if (!bestSystemPath.equals("")) { // NOI18N
                            includeDirective.append("<"); // NOI18N
                            includeDirective.append(incFilePath.substring(bestSystemPath.length() + 1));
                            includeDirective.append(">"); // NOI18N
                            return includeDirective.toString();
                        }
                    } else {
                        includeDirective.append("\""); // NOI18N
                        String projectPath = currentFile.getAbsolutePath().toString().substring(0,
                                currentFile.getAbsolutePath().toString().length() - currentFile.getName().toString().length() - 1);
                        if (!incFilePath.startsWith(projectPath)) {
                            projectPath = ""; // NOI18N
                        }
                        String bestUserPath = getRelativePath(nativeFile.getUserIncludePaths(), incFilePath);
                        if (bestUserPath.length() < projectPath.length()) {
                            includeDirective.append(incFilePath.substring(projectPath.length() + 1));
                        } else {
                            includeDirective.append(incFilePath.substring(bestUserPath.length() + 1));
                        }
                        if (!bestUserPath.equals("") || !projectPath.equals("")) // NOI18N
                        {
                            includeDirective.append("\""); // NOI18N
                            return includeDirective.toString();
                        }
                    }
                } else {
                    String projectPath = currentFile.getAbsolutePath().toString().substring(0, currentFile.getAbsolutePath().toString().length() - currentFile.getName().toString().length());
                    if (incFilePath.startsWith(projectPath)) {
                        includeDirective.append("\""); // NOI18N
                        includeDirective.append(incFilePath.substring(projectPath.length()));
                        includeDirective.append("\""); // NOI18N
                        return includeDirective.toString();
                    }
                }
            } else {
                System.err.println("not handled file instance " + currentFile);
            }
        } else {
            System.err.println("not yet handled object " + item);
        }
        return ""; // NOI18N
    }

    // Returns relative path for file from list of paths
    private String getRelativePath(List<String> paths, String filePath) {
        String goodPath = ""; // NOI18N
        for (String path : paths) {
            if (filePath.startsWith(path)) {
                if (goodPath.length() < path.length()) {
                    goodPath = path;
                }
            }
        }
        return goodPath;
    }

    @Override
    public boolean isObjectVisible(CsmFile currentFile, CsmObject item) {
        if (CsmKindUtilities.isOffsetable(item)) {
            CsmFile file = ((CsmOffsetable) item).getContainingFile();
             if (!file.equals(currentFile)) {
                if (file.isHeaderFile()) {
                    HashSet<CsmFile> scannedfiles = new HashSet();
                    if (isFileVisibleInIncludeFiles(currentFile.getIncludes(), file, scannedfiles)) {
                        return true;
                    }
                } else if (file.isSourceFile() && CsmKindUtilities.isGlobalVariable(item)) {
                    HashSet<CsmProject> scannedprojects = new HashSet();
                    if (isVariableVisible(currentFile, file.getProject(), (CsmVariable) item, scannedprojects)) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        } else {
            System.err.println("not yet handled object " + item);
        }
        return false;
    }

    // Says is variable visible in current file
    private boolean isVariableVisible(CsmFile currentFile, CsmProject project, CsmVariable var, HashSet<CsmProject> scannedProjects) {
        if (scannedProjects.contains(project)) {
            return false;
        }
        scannedProjects.add(project);
        if (isVariableDeclarationsVisible(currentFile, project.findDeclarations(var.getUniqueName() + " (EXTERN)"))) { // NOI18N
            return true;
        }
        if (isVariableDeclarationsVisible(currentFile, project.findDeclarations(var.getUniqueName()))) {
            return true;
        }
        for (CsmProject lib : project.getLibraries()) {
            if (isVariableVisible(currentFile, lib, var, scannedProjects)) {
                return true;
            }
        }
        return false;
    }

    // Says is at least one of variable declarations visible in current file
    private boolean isVariableDeclarationsVisible(CsmFile currentFile, Collection<CsmOffsetableDeclaration> decls) {
        for (CsmOffsetableDeclaration decl : decls) {
            HashSet<CsmFile> scannedFiles = new HashSet();
            if(decl.getContainingFile().equals(currentFile)) {
                return true;
            } else if (isFileVisibleInIncludeFiles(currentFile.getIncludes(), decl.getContainingFile(), scannedFiles)) {
                return true;
            }
        }
        return false;
    }

    // Says is file visible in includes
    private boolean isFileVisibleInIncludeFiles(Collection<CsmInclude> includes, CsmFile file, HashSet<CsmFile> scannedFiles) {
        for (CsmInclude inc : includes) {
            CsmFile incFile = inc.getIncludeFile();
            if (incFile != null) {
                if (!scannedFiles.contains(incFile)) {
                    scannedFiles.add(incFile);
                    if (file.equals(incFile)) {
                        return true;
                    }
                    if (isFileVisibleInIncludeFiles(incFile.getIncludes(), file, scannedFiles)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSystemHeader(CsmFile currentFile, CsmFile header) {
        return !(currentFile.getProject().equals(header.getProject()));
    }
}
