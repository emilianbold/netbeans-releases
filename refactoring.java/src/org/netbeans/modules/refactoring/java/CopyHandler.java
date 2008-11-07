/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationEvent.Copy;
import org.openide.loaders.OperationEvent.Move;
import org.openide.loaders.OperationEvent.Rename;
import org.openide.loaders.OperationListener;
import org.openide.util.Exceptions;

/**
 * This is a temporary solution to preserve old functionality of java data
 * objects (disabled within #141093) until there will be full copy refactoring
 * support (#74265) in the refactoring modules.
 *
 * The present implementation updates package and top level class declarations.
 *
 * @author Jan Pokorsky
 */
final class CopyHandler implements OperationListener {
    
    private static final CopyHandler INSTANCE = new CopyHandler();
    
    public static CopyHandler getInstance() {
        return INSTANCE;
    }
    
    public void register() {
        DataLoaderPool.getDefault().addOperationListener(this);
    }

    private static void renameFO(final JavaSource javaSource, 
            final String packageName, 
            final String newName, 
            final String originalName) throws IOException {

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree compilationUnitTree = workingCopy.getCompilationUnit();
                // change the package when file was move to different dir.
                CompilationUnitTree cutCopy = make.CompilationUnit(
                        "".equals(packageName) ? null : make.Identifier(packageName), // NOI18N
                        compilationUnitTree.getImports(),
                        compilationUnitTree.getTypeDecls(),
                        compilationUnitTree.getSourceFile()
                );
                workingCopy.rewrite(compilationUnitTree, cutCopy);
                // rename also the top level class...
                if (originalName != null && !originalName.equals(newName)) {
                    for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
                        if (Tree.Kind.CLASS == typeDecl.getKind()) {
                            ClassTree clazz = (ClassTree) typeDecl;
                            if (originalName.contentEquals(clazz.getSimpleName())) {
                                Tree copy = make.setLabel(typeDecl, newName);
                                workingCopy.rewrite(typeDecl, copy);
                            }
                        }
                    }
                }
            }                
        };
        javaSource.runModificationTask(task).commit();
    }
    
    // singleton
    private CopyHandler() {
    }

    public void operationCopy(Copy ev) {
        FileObject copyFO = ev.getObject().getPrimaryFile();
        FileObject origFO = ev.getOriginalDataObject().getPrimaryFile();
        JavaSource js = JavaSource.forFileObject(copyFO);
        if (js == null) {
            return;
        }
        if ("application/x-class-file".equals(FileUtil.getMIMEType(copyFO)) //NOI18N
                || "class".equals(copyFO.getExt())) { //NOI18N
            // #151288: JavaSource may exist even for .class file
            return;
        }
        ClassPath cp = ClassPath.getClassPath(copyFO, ClassPath.SOURCE);
        if (cp == null) {
            return;
        }
        String pkgName = cp.getResourceName(copyFO.getParent(), '.', false);
        try {
            renameFO(js, pkgName, copyFO.getName(), origFO.getName());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void operationPostCreate(OperationEvent ev) {
        // ignore
    }

    public void operationMove(Move ev) {
        // ignore
    }

    public void operationDelete(OperationEvent ev) {
        // ignore
    }

    public void operationRename(Rename ev) {
        // ignore
    }

    public void operationCreateShadow(Copy ev) {
        // ignore
    }

    public void operationCreateFromTemplate(Copy ev) {
        // ignore
    }

}
