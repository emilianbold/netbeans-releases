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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class ChangeParamsTransformer extends RefactoringVisitor {

    private Set<ElementHandle<ExecutableElement>> allMethods;
    /** refactored element is a synthetic default constructor */
    private boolean synthConstructor;

    public ChangeParamsTransformer(ChangeParametersRefactoring refactoring, Set<ElementHandle<ExecutableElement>> am) {
        this.refactoring = refactoring;
        this.allMethods = am;
    }

    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        synthConstructor = this.workingCopy.getElementUtilities().isSynthetic(p);
        return super.visitCompilationUnit(node, p);
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
    
    private List<ExpressionTree> getNewArguments(List<? extends ExpressionTree> currentArguments) {
        List<ExpressionTree> arguments = new ArrayList();
        ParameterInfo[] pi = refactoring.getParameterInfo();
        for (int i = 0; i < pi.length; i++) {
            int originalIndex = pi[i].getOriginalIndex();
            ExpressionTree vt;
            if (originalIndex < 0) {
                String value = pi[i].getDefaultValue();
                SourcePositions pos[] = new SourcePositions[1];
                vt = workingCopy.getTreeUtilities().parseExpression(value, pos);
            } else {
                if (i == pi.length - 1 && pi[i].getType().endsWith("...")) { // NOI18N
                    // last param is vararg, so copy all remaining arguments
                    for (int j = originalIndex; j < currentArguments.size(); j++) {
                        arguments.add(currentArguments.get(j));
                    }
                    break;
                } else {
                    vt = currentArguments.get(originalIndex);
                }
            }
            arguments.add(vt);
        }
        return arguments;
    }

    @Override
    public Tree visitMethodInvocation(MethodInvocationTree tree, Element p) {
        if (p.getKind() == ElementKind.CONSTRUCTOR || !workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            if (el!=null) {
                if (isMethodMatch(el)) {
                    List<ExpressionTree> arguments = getNewArguments(tree.getArguments());
                    
                    MethodInvocationTree nju = make.MethodInvocation(
                            (List<ExpressionTree>)tree.getTypeArguments(),
                            tree.getMethodSelect(),
                            arguments);
                    
                    if (p.getKind() == ElementKind.CONSTRUCTOR && workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
                        rewriteSyntheticConstructor(nju);
                    } else {
                        // rewrite existing super(); statement
                        rewrite(tree, nju);
                    }
                }
            }
        }
        return super.visitMethodInvocation(tree, p);
    }

    /** workaround to rewrite synthetic super(); statement */
    private void rewriteSyntheticConstructor(MethodInvocationTree nju) {
        TreePath constructorPath = getCurrentPath();
        while (constructorPath != null && constructorPath.getLeaf().getKind() != Tree.Kind.METHOD) {
            constructorPath = constructorPath.getParentPath();
        }
        if (constructorPath != null) {
            MethodTree constrTree = (MethodTree) constructorPath.getLeaf();
            BlockTree body = constrTree.getBody();
            body = make.removeBlockStatement(body, 0);
            body = make.insertBlockStatement(body, 0, make.ExpressionStatement(nju));
            if (workingCopy.getTreeUtilities().isSynthetic(constructorPath)) {
                // in case of synthetic default constructor declaration the whole constructor has to be rewritten
                MethodTree njuConstructor = make.Method(
                        make.Modifiers(constrTree.getModifiers().getFlags(),
                        constrTree.getModifiers().getAnnotations()),
                        constrTree.getName(),
                        constrTree.getReturnType(),
                        constrTree.getTypeParameters(),
                        constrTree.getParameters(),
                        constrTree.getThrows(),
                        body,
                        (ExpressionTree) constrTree.getDefaultValue());
                rewrite(constrTree, njuConstructor);
            } else {
                // declared default constructor => body rewrite is sufficient
                rewrite(constrTree.getBody(), body);
            }
        }
    }
    
    @Override
    public Tree visitMethod(MethodTree tree, Element p) {
        renameDeclIfMatch(getCurrentPath(), tree, p);
        return super.visitMethod(tree, p);
    }

    ChangeParametersRefactoring refactoring;
    private void renameDeclIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (!synthConstructor && workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        MethodTree current = (MethodTree) tree;
        Element el = workingCopy.getTrees().getElement(path);
        if (isMethodMatch(el)) {
            
            List<? extends VariableTree> currentParameters = current.getParameters();
            List<VariableTree> newParameters = new ArrayList<VariableTree>();
            
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

            //Compute new imports
            for (VariableTree vt : newParameters) {
                Set<ElementHandle<TypeElement>> declaredTypes = workingCopy.getClasspathInfo().getClassIndex().getDeclaredTypes(vt.getType().toString(), NameKind.SIMPLE_NAME, EnumSet.allOf(ClassIndex.SearchScope.class));
                Set<ElementHandle<TypeElement>> declaredTypesMirr = new HashSet<ElementHandle<TypeElement>>(declaredTypes);
                TypeElement type = null;

                //remove private types
                //TODO: and possibly package private?
                for (ElementHandle<TypeElement> typeName : declaredTypes) {
                    TypeElement te = workingCopy.getElements().getTypeElement(typeName.getQualifiedName());

                    if (te == null) {
                        Logger.getLogger(ChangeParamsTransformer.class.getName()).log(Level.INFO, "Cannot resolve type element \"" + typeName + "\".");
                        continue;
                    }
                    if (te.getModifiers().contains(Modifier.PRIVATE)) {
                        declaredTypesMirr.remove(typeName);
                    }

                }

                if (declaredTypesMirr.size() == 1) { //creates import if there is just one proposed type
                    ElementHandle<TypeElement> typeName = declaredTypesMirr.iterator().next();
                    TypeElement te = workingCopy.getElements().getTypeElement(typeName.getQualifiedName());

                    if (te == null) {
                        Logger.getLogger(ChangeParamsTransformer.class.getName()).log(Level.INFO, "Cannot resolve type element \"" + typeName + "\".");
                        continue;
                    }
                    type = te;
                }

                if (type != null) {
                    PackageElement packageOf = workingCopy.getElements().getPackageOf(type);
                    if (packageOf.getQualifiedName().toString().equals("java.lang")) {
                        continue;
                    }
                    try {
                        SourceUtils.resolveImport(workingCopy, path, type.getQualifiedName().toString());
                    } catch (NullPointerException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

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
                if (baseMethod.equals(method) || workingCopy.getElements().overrides((ExecutableElement)method, baseMethod, workingCopy.getElementUtilities().enclosingTypeElement(baseMethod))) {
                    return true;
                }
            }
        }
        return false;
    }
}
