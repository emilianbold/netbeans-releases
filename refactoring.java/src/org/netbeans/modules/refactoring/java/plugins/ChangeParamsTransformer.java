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

package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;

/**
 *
 * @author Jan Becicka
 */
public class ChangeParamsTransformer extends RefactoringVisitor {

    private Set<ElementHandle<ExecutableElement>> allMethods;

    public ChangeParamsTransformer(ChangeParametersRefactoring refactoring, Set<ElementHandle<ExecutableElement>> am) {
        this.refactoring = refactoring;
        this.allMethods = am;
    }
    
    @Override
    public Tree visitNewClass(NewClassTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el!=null) {
                if (isMethodMatch(el)) {
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments());
                    NewClassTree nju = make.NewClass(tree.getEnclosingExpression(),
                            (List<ExpressionTree>)tree.getTypeArguments(),
                            tree.getIdentifier(),
                            arguments,
                            tree.getClassBody());
                    rewrite(tree, nju);
                }
            }
        }
        return super.visitNewClass(tree, p);
    }
    
    private List<ExpressionTree> getNewArguments(List<? extends ExpressionTree> originalArguments) {
        List<ExpressionTree> arguments = new ArrayList();
        ParameterInfo[] pi = refactoring.getParameterInfo();
        int origArgsCount = originalArguments.size();
        int maxOrigIndex = -1;
        for (int index = 0; index < pi.length; index++) {
            int originalIndex = pi[index].getOriginalIndex();
            if (originalIndex >= origArgsCount) {
                break;
            }
            ExpressionTree vt;
            if (originalIndex <0) {
                String value = pi[index].getDefaultValue();
                SourcePositions pos[] = new SourcePositions[1];
                vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
            } else {
                maxOrigIndex = Math.max(originalIndex, maxOrigIndex);
                vt = originalArguments.get(originalIndex);
            }
            arguments.add(vt);
        }
        for (int index = maxOrigIndex + 1; index < origArgsCount; index++) {
            arguments.add(originalArguments.get(index));
        }
        return arguments;
    }
    

    public Tree visitMethodInvocation(MethodInvocationTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el!=null) {
                if (isMethodMatch(el)) {
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments());
                    
                    MethodInvocationTree nju = make.MethodInvocation(
                            (List<ExpressionTree>)tree.getTypeArguments(),
                            tree.getMethodSelect(),
                            arguments);
                    rewrite(tree, nju);
                }
            }
        }
        return super.visitMethodInvocation(tree, p);
    }
    
    
    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    ChangeParametersRefactoring refactoring;
    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        MethodTree current = (MethodTree) tree;
        Element el = workingCopy.getTrees().getElement(path);
        if (isMethodMatch(el)) {
            
            List<? extends VariableTree> currentParameters = current.getParameters();
            List<VariableTree> newParameters = new ArrayList();
            
            ParameterInfo[] p = refactoring.getParameterInfo();
            for (int i=0; i<p.length; i++) {
                int originalIndex = p[i].getOriginalIndex();
                VariableTree vt;
                if (originalIndex <0) {
                    vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), p[i].getName(),make.Identifier(p[i].getType()), null);
                } else {
                    vt = currentParameters.get(p[i].getOriginalIndex());
                }
                newParameters.add(vt);
            }
            Set<Modifier> modifiers = new HashSet(refactoring.getModifiers());
            if (!el.getModifiers().contains(Modifier.ABSTRACT)) {
                modifiers.remove(Modifier.ABSTRACT);
            }
            ClassTree enclosingClass = (ClassTree) workingCopy.getTrees().getTree(el.getEnclosingElement());                                
            if(workingCopy.getTreeUtilities().isInterface(enclosingClass)) modifiers.remove(Modifier.ABSTRACT);
            MethodTree nju = make.Method(
                    make.Modifiers(modifiers, current.getModifiers().getAnnotations()),
                    current.getName(),
                    current.getReturnType(),
                    current.getTypeParameters(),
                    newParameters,
                    current.getThrows(),
                    current.getBody(),
                    (ExpressionTree) current.getDefaultValue());
            rewrite(tree, nju);
            return;
        }
    }
    
    private boolean isMethodMatch(Element method) {
        if ((method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) && allMethods !=null) {
            for (ElementHandle<ExecutableElement> mh: allMethods) {
                ExecutableElement baseMethod =  mh.resolve(workingCopy);
                if (baseMethod==null) {
                    Logger.getLogger("org.netbeans.modules.refactoring.java").info("ChangeParamsTransformer cannot resolve " + mh);
                    continue;
                }
                if (baseMethod.equals(method) || workingCopy.getElements().overrides((ExecutableElement)method, baseMethod, SourceUtils.getEnclosingTypeElement(baseMethod))) {
                    return true;
                }
            }
        }
        return false;
    }
}
