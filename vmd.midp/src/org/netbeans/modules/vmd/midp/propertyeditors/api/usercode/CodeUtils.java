/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midp.propertyeditors.api.usercode;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.openide.ErrorManager;

/**
 *
 * @author Anton Chechel
 */
public final class CodeUtils {

    private CodeUtils() {
    }
    
    public static int getMethodOffset(DataObjectContext context) {
        final int[] offset = new int[1];
        JavaSource javaSource = JavaSource.forFileObject(context.getDataObject().getPrimaryFile());

        try {
            javaSource.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController compilationController) throws Exception {
                    compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    CompilationUnitTree unitTree = compilationController.getCompilationUnit();
                    java.util.List<? extends TypeElement> types = compilationController.getTopLevelElements();
                    if (types.size() > 0) {
                        TypeElement type = types.get(0);
                        java.util.List<ExecutableElement> methods = ElementFilter.methodsIn(type.getEnclosedElements());
                        for (ExecutableElement method : methods) {
                            if (!method.getModifiers().contains(Modifier.STATIC)) {
                                Tree methodTree = compilationController.getTrees().getTree(method);
                                if (methodTree.getKind() == Kind.METHOD) {
                                    Tree tree = ((MethodTree) methodTree).getBody();
                                    if (tree != null) {
                                        offset[0] = (int) compilationController.getTrees().getSourcePositions().getStartPosition(unitTree, tree) + 1;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return offset[0];
    }

}
