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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Bharath Ravi Kumar
 */
public class UseSupertypeTest extends RefactoringTestCase {
    private TreePathHandle subtypeTreeHandle;
    private ElementHandle supertypeElementHandle;
    
    public UseSupertypeTest(String testName){
        super(testName);
    }

    public void testSimpleUST() throws Exception{
        FileObject superTypeFileObject = getFileInProject("default","src/ustpkg/A.java" );
        FileObject subTypeFileObject = getFileInProject("default","src/ustpkg/B.java" );
        JavaSource javaSrcSubType = JavaSource.forFileObject(subTypeFileObject);
        TreePathResolver subTypeSelector = new TreePathResolver(new TopClassSelector(0));
        javaSrcSubType.runUserActionTask(subTypeSelector, true);
        subtypeTreeHandle = subTypeSelector.tph;
        
        JavaSource javaSrcSuperType = JavaSource.forFileObject(superTypeFileObject);
        TreePathResolver superTypeSelector = new TreePathResolver(new TopClassSelector(0));
        javaSrcSuperType.runUserActionTask(superTypeSelector, true);
        TreePathHandle superTypeTreeHandle = superTypeSelector.tph;
        setSuperTypeElementHandle(superTypeTreeHandle, javaSrcSuperType);
        
        final UseSuperTypeRefactoring useSuperTypeRefactoring = new UseSuperTypeRefactoring(subtypeTreeHandle);
        perform(useSuperTypeRefactoring,new ParameterSetter() {
            public void setParameters() {
                useSuperTypeRefactoring.setTargetSuperType(supertypeElementHandle);
            }
        });        
        
    }

    private void setSuperTypeElementHandle(final TreePathHandle superTypeTreePathHandle, 
            JavaSource javaSrcSuperType) {
        try {
            javaSrcSuperType.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {

                }
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        supertypeElementHandle = ElementHandle.create(
                                controller.getTrees().getElement(superTypeTreePathHandle.resolve(controller)));
                        
                    }
            }, true);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
