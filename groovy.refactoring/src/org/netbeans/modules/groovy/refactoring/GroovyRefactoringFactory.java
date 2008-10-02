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

package org.netbeans.modules.groovy.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class GroovyRefactoringFactory implements RefactoringPluginFactory {

    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {

        NonRecursiveFolder pkg = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        FileObject sourceFO = refactoring.getRefactoringSource().lookup(FileObject.class);
        TreePathHandle handle = resolveTreePathHandle(refactoring);
        GroovyRefactoringElement element = refactoring.getRefactoringSource().lookup(GroovyRefactoringElement.class);

        boolean javaPackage = pkg != null && RefactoringUtil.isOnSourceClasspath(pkg.getFolder());
        boolean folder = sourceFO != null && sourceFO.isFolder();

        if (sourceFO == null){
            if (handle != null){
                sourceFO = handle.getFileObject();
            } else if (pkg != null){
                sourceFO = pkg.getFolder();
            } else if (element != null) {
                sourceFO = element.getFileObject();
            }
        }

        if (sourceFO == null){
            return null;
        }

        boolean supportedFile = sourceFO != null && Utils.isInGroovyProject(sourceFO) &&
                (RefactoringUtil.isJavaFile(sourceFO) || Utils.isGroovyFile(sourceFO));


        String clazz = null;
        if (handle != null) {
            clazz = resolveClass(handle);
        } else if (element != null) {
            clazz = element.getDefClass();
        }

        // if we have a java file, the class name should be resolvable
        // unless it is an empty java file - see #130933
        if (supportedFile && clazz == null) {
            return null;
        }

        List<GroovyRefactoring> refactorings = new ArrayList<GroovyRefactoring>();

        if (refactoring instanceof WhereUsedQuery && supportedFile){
            WhereUsedQuery whereUsedQuery = (WhereUsedQuery) refactoring;
            refactorings.add(new GroovyWhereUsed(sourceFO, clazz, whereUsedQuery));
        }
        
        return refactorings.isEmpty() ? null : new GroovyRefactoringPlugin(refactorings);
    }

    private TreePathHandle resolveTreePathHandle(final AbstractRefactoring refactoring){

        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            return tph;
        }

        FileObject sourceFO = refactoring.getRefactoringSource().lookup(FileObject.class);
        if (sourceFO == null || !RefactoringUtil.isJavaFile(sourceFO)){
            return null;
        }
        final TreePathHandle[] result = new TreePathHandle[1];
        try{

            JavaSource source = JavaSource.forFileObject(sourceFO);

            source.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController co) throws Exception {
                    co.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = co.getCompilationUnit();
                    if (cut.getTypeDecls().isEmpty()){
                        return;
                    }
                    result[0] = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
                }

            }, true);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }

        return result[0];
    }

    /**
     * @return the fully qualified name of the class that the given
     * TreePathHandle represents or null if the FQN could not be resolved.
     */
    private String resolveClass(final TreePathHandle treePathHandle){
        if(treePathHandle == null){
            return null;
        }

        final String[] result = new String[1];

        try{
            JavaSource source = JavaSource.forFileObject(treePathHandle.getFileObject());

            if(source == null) {
                return null;
            }

            source.runUserActionTask(new CancellableTask<CompilationController>() {

                public void cancel() {
                }

                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    Element element = treePathHandle.resolveElement(parameter);
                    result[0] = element.asType().toString();
                }
            }, true);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }

}
