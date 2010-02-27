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
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 *
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver.class)
public final class IncludeResolverImpl extends CsmIncludeResolver {
    private final Set<String> standardHeaders;

    public IncludeResolverImpl() {
        standardHeaders = new HashSet<String>();
        
        // C++ headers
        standardHeaders.add("algorithm"); // NOI18N
        standardHeaders.add("bitset"); // NOI18N
        standardHeaders.add("complex"); // NOI18N
        standardHeaders.add("deque"); // NOI18N
        standardHeaders.add("exception"); // NOI18N
        standardHeaders.add("fstream"); // NOI18N
        standardHeaders.add("functional"); // NOI18N
        standardHeaders.add("iomanip"); // NOI18N
        standardHeaders.add("ios"); // NOI18N
        standardHeaders.add("iosfwd"); // NOI18N
        standardHeaders.add("iostream"); // NOI18N
      //standardHeaders.add("istream"); // NOI18N
        standardHeaders.add("iterator"); // NOI18N
        standardHeaders.add("limits"); // NOI18N
        standardHeaders.add("list"); // NOI18N
        standardHeaders.add("locale"); // NOI18N
        standardHeaders.add("map"); // NOI18N
        standardHeaders.add("memory"); // NOI18N
        standardHeaders.add("new"); // NOI18N
        standardHeaders.add("numeric"); // NOI18N
      //standardHeaders.add("ostream"); // NOI18N
        standardHeaders.add("queue"); // NOI18N
        standardHeaders.add("set"); // NOI18N
        standardHeaders.add("sstream"); // NOI18N
        standardHeaders.add("stack"); // NOI18N
        standardHeaders.add("stdexcept"); // NOI18N
        standardHeaders.add("streambuf"); // NOI18N
        standardHeaders.add("string"); // NOI18N
        standardHeaders.add("typeinfo"); // NOI18N
        standardHeaders.add("utility"); // NOI18N
        standardHeaders.add("valarray"); // NOI18N
        standardHeaders.add("vector"); // NOI18N

        // C++ headers for C
        standardHeaders.add("cassert"); // NOI18N
        standardHeaders.add("cctype"); // NOI18N
        standardHeaders.add("cerrno"); // NOI18N
        standardHeaders.add("cfloat"); // NOI18N
        standardHeaders.add("ciso646"); // NOI18N
        standardHeaders.add("climits"); // NOI18N
        standardHeaders.add("clocale"); // NOI18N
        standardHeaders.add("cmath"); // NOI18N
        standardHeaders.add("csetjmp"); // NOI18N
        standardHeaders.add("csignal"); // NOI18N
        standardHeaders.add("cstdarg"); // NOI18N
        standardHeaders.add("cstddef"); // NOI18N
        standardHeaders.add("cstdio"); // NOI18N
        standardHeaders.add("cstdlib"); // NOI18N
        standardHeaders.add("cstring"); // NOI18N
        standardHeaders.add("ctime"); // NOI18N
        standardHeaders.add("cwchar"); // NOI18N
        standardHeaders.add("cwctype"); // NOI18N
                       
        // C headers
        standardHeaders.add("assert.h"); // NOI18N
        standardHeaders.add("ctype.h"); // NOI18N
        standardHeaders.add("errno.h"); // NOI18N
        standardHeaders.add("float.h"); // NOI18N
        standardHeaders.add("iso646.h"); // NOI18N
        standardHeaders.add("limits.h"); // NOI18N
        standardHeaders.add("locale.h"); // NOI18N
        standardHeaders.add("math.h"); // NOI18N
        standardHeaders.add("setjmp.h"); // NOI18N
        standardHeaders.add("signal.h"); // NOI18N
        standardHeaders.add("stdarg.h"); // NOI18N
        standardHeaders.add("stddef.h"); // NOI18N
        standardHeaders.add("stdio.h"); // NOI18N
        standardHeaders.add("stdlib.h"); // NOI18N
        standardHeaders.add("string.h"); // NOI18N
        standardHeaders.add("time.h"); // NOI18N
        standardHeaders.add("wchar.h"); // NOI18N
        standardHeaders.add("wctype.h"); // NOI18N
    }

    @Override
    public String getIncludeDirective(CsmFile currentFile, CsmObject item) {
        if (CsmKindUtilities.isOffsetable(item)) {
            CsmFile file = ((CsmOffsetable) item).getContainingFile();
            if (file.equals(currentFile) || file.isHeaderFile()) {
                return getIncludeDerectiveByFile(currentFile, item).replace('\\', '/'); // NOI18N;
            } else if (file.isSourceFile() && CsmKindUtilities.isGlobalVariable(item)) {
                Collection<CsmOffsetableDeclaration> decls = file.getProject().findDeclarations(((CsmVariable) item).getUniqueName() + " (EXTERN)"); // NOI18N
                if (!decls.isEmpty()) {
                    return getIncludeDerectiveByFile(currentFile, decls.iterator().next()).replace('\\', '/'); // NOI18N;
                }
            }
        } else if (!CsmKindUtilities.isNamespace(item)) {
            System.err.println("not yet handled object " + item);
        }
        return ""; // NOI18N
    }

    // Says is header standard or not
    private boolean isStandardHeader(List<String> sysIncsPaths, CsmFile header) {
        String bestSystemPath = getRelativePath(sysIncsPaths, header.getAbsolutePath().toString());
        return standardHeaders.contains(header.getAbsolutePath().toString().substring(bestSystemPath.length() + 1));
    }
    
    // Returns standard header if it exists
    private CsmFile getStandardHeaderIfExists(CsmFile currentFile, List<String> sysIncsPaths, CsmFile file, HashSet<CsmFile> scannedFiles) {
        if (scannedFiles.contains(file) || !isSystemHeader(currentFile, file)) {
            return null;
        }
        scannedFiles.add(file);
        if(isStandardHeader(sysIncsPaths, file)) {
            return file;
        }
        CsmIncludeHierarchyResolver ihr = CsmIncludeHierarchyResolver.getDefault();
        Collection<CsmFile> files = ihr.getFiles(file);
        for (CsmFile f : files) {
            CsmFile stdHeader = getStandardHeaderIfExists(currentFile, sysIncsPaths, f, scannedFiles);
            if(stdHeader != null) {
               return stdHeader; 
            }
        }
        return null;
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
                        // check is this file included into standard header
                        HashSet<CsmFile> scannedFiles = new HashSet<CsmFile>();
                        CsmFile stdHeader = getStandardHeaderIfExists(currentFile, nativeFile.getSystemIncludePaths(), ((CsmOffsetable) item).getContainingFile(), scannedFiles);
                        if(stdHeader != null) {
                            incFilePath = stdHeader.getAbsolutePath().toString();
                        }
                        String bestSystemPath = getRelativePath(nativeFile.getSystemIncludePaths(), incFilePath);
                        if (!bestSystemPath.equals("")) { // NOI18N
                            includeDirective.append("<"); // NOI18N
                            includeDirective.append(CndPathUtilitities.toRelativePath(bestSystemPath, incFilePath));
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
                            includeDirective.append(CndPathUtilitities.toRelativePath(projectPath, incFilePath));
                        } else {
                            includeDirective.append(CndPathUtilitities.toRelativePath(bestUserPath, incFilePath));
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
        } else if (!CsmKindUtilities.isNamespace(item)) {
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
            if (file != null) {
                if (!file.equals(currentFile)) {
                    if (file.isHeaderFile()) {
                        if (((ProjectBase) currentFile.getProject()).getGraphStorage().isFileIncluded(currentFile, file)) {
                            return true;
                        }
                    //HashSet<CsmFile> scannedfiles = new HashSet<CsmFile>();
                    //if (isFileVisibleInIncludeFiles(currentFile.getIncludes(), file, scannedfiles)) {
                    //    return true;
                    //}
                    } else if (file.isSourceFile() && CsmKindUtilities.isGlobalVariable(item)) {
                        HashSet<CsmProject> scannedprojects = new HashSet<CsmProject>();
                        if (isVariableVisible(currentFile, file.getProject(), (CsmVariable) item, scannedprojects)) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else if (!CsmKindUtilities.isNamespace(item)) {
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
            if(decl.getContainingFile().equals(currentFile)) {
                return true;
            }
            if (((ProjectBase)currentFile.getProject()).getGraphStorage().isFileIncluded(currentFile, decl.getContainingFile())){
                return true;
            }
            //HashSet<CsmFile> scannedFiles = new HashSet<CsmFile>();
            //if (isFileVisibleInIncludeFiles(currentFile.getIncludes(), decl.getContainingFile(), scannedFiles)) {
            //    return true;
            //}
        }
        return false;
    }

    // Says is file visible in includes
//    private boolean isFileVisibleInIncludeFiles(Collection<CsmInclude> includes, CsmFile file, HashSet<CsmFile> scannedFiles) {
//        for (CsmInclude inc : includes) {
//            CsmFile incFile = inc.getIncludeFile();
//            if (incFile != null) {
//                if (!scannedFiles.contains(incFile)) {
//                    scannedFiles.add(incFile);
//                    if (file.equals(incFile)) {
//                        return true;
//                    }
//                    if (isFileVisibleInIncludeFiles(incFile.getIncludes(), file, scannedFiles)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    private boolean isSystemHeader(CsmFile currentFile, CsmFile header) {
        return !(currentFile.getProject().equals(header.getProject()));
    }
}
