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
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.MemberInfo.Group;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class PullUpTransformer extends RefactoringVisitor {

    private MemberInfo<ElementHandle<? extends Element>>[] members;
    private Element targetType;
    private PullUpRefactoring refactoring;
    public PullUpTransformer(PullUpRefactoring refactoring) {
        this.refactoring = refactoring;
        this.members = refactoring.getMembers();
    }
    
    @Override
    public void setWorkingCopy(WorkingCopy copy) throws ToPhaseException {
        super.setWorkingCopy(copy);
        this.targetType = refactoring.getTargetType().resolve(copy);
    }

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy); // helper
        boolean classIsAbstract = el.getKind().isInterface();
        ClassTree njuClass = tree;
        if (el.equals(targetType)) {
            //target type
            //add members
            List<String> imports = new ArrayList<String>();
            for (int i = 0; i<members.length; i++) {
                if (members[i].getGroup()==MemberInfo.Group.IMPLEMENTS) {
                    Element member = members[i].getElementHandle().resolve(workingCopy);
                    imports.add(member.asType().toString()); //add may-be necessary import
                    njuClass = make.addClassImplementsClause(njuClass, make.Identifier(members[i].getElementHandle().resolve(workingCopy)));
                    rewrite(tree, njuClass);
                } else {
                    if (members[i].isMakeAbstract()) {
                        
                        if (!classIsAbstract) {
                            classIsAbstract = true;
                            Set<Modifier> mod = new HashSet<Modifier>(njuClass.getModifiers().getFlags());
                            mod.add(Modifier.ABSTRACT);
                            mod.remove(Modifier.FINAL);
                            ModifiersTree modifiers = make.Modifiers(mod);
                            rewrite(njuClass.getModifiers(), modifiers);
                        }
                        
                        Element methodElm = members[i].getElementHandle().resolve(workingCopy);
                        MethodTree method = (MethodTree) workingCopy.getTrees().getTree(methodElm);
                        Set<Modifier> mod = new HashSet<Modifier>(method.getModifiers().getFlags());
                        mod.add(Modifier.ABSTRACT);
                        mod.remove(Modifier.FINAL);
                        // abstract method cannot be synchronized
                        mod.remove(Modifier.SYNCHRONIZED);
                        if (el.getKind().isInterface()) {
                            mod.remove(Modifier.PUBLIC);
                            mod.remove(Modifier.PROTECTED);
                            mod.remove(Modifier.PRIVATE);
                            mod.remove(Modifier.ABSTRACT);
                        }
                        if (mod.contains(Modifier.PRIVATE)) {
                            mod.remove(Modifier.PRIVATE);
                            mod.add(Modifier.PROTECTED);
                        }
                        MethodTree nju = make.Method(
                                make.Modifiers(mod),
                                method.getName(),
                                method.getReturnType(),
                                method.getTypeParameters(),
                                method.getParameters(),
                                method.getThrows(),
                                (BlockTree) null,
                                (ExpressionTree)method.getDefaultValue());
                        nju = genUtils.importFQNs(nju);
                        RetoucheUtils.copyJavadoc(methodElm, nju, workingCopy);
                        njuClass = genUtils.insertClassMember(njuClass, nju);
                        rewrite(tree, njuClass);
                    } else {                        
                        Element methodElm = members[i].getElementHandle().resolve(workingCopy);
                        TreePath mpath = workingCopy.getTrees().getPath(members[i].getElementHandle().resolve(workingCopy));
                        Tree newMethodTree = genUtils.importComments(mpath.getLeaf(), mpath.getCompilationUnit());
                        newMethodTree = genUtils.importFQNs(newMethodTree);
                        
                        if (methodElm.getModifiers().contains(Modifier.PRIVATE)) {
                            if (members[i].getGroup() == Group.METHOD) {
                                MethodTree oldOne = (MethodTree) newMethodTree;
                                MethodTree m = make.Method(
                                        make.addModifiersModifier(make.removeModifiersModifier(oldOne.getModifiers(), Modifier.PRIVATE), Modifier.PROTECTED),
                                        oldOne.getName(),
                                        oldOne.getReturnType(),
                                        oldOne.getTypeParameters(),
                                        oldOne.getParameters(),
                                        oldOne.getThrows(),
                                        oldOne.getBody(),
                                        (ExpressionTree) oldOne.getDefaultValue());
                                RetoucheUtils.copyJavadoc(methodElm, m, workingCopy);
                                njuClass = genUtils.insertClassMember(njuClass, m);
                            } else if (members[i].getGroup() == Group.FIELD) {
                                VariableTree oldOne = (VariableTree) newMethodTree;
                                VariableTree m = make.Variable(
                                        make.addModifiersModifier(make.removeModifiersModifier(oldOne.getModifiers(), Modifier.PRIVATE), Modifier.PROTECTED),
                                        oldOne.getName(),
                                        oldOne.getType(),
                                        oldOne.getInitializer());
                                RetoucheUtils.copyJavadoc(methodElm, m, workingCopy);
                                njuClass = genUtils.insertClassMember(njuClass, m);
                            } else if (members[i].getGroup() == Group.TYPE) {
                                ClassTree oldOne = (ClassTree) newMethodTree;
                                Tree m = null;
                                switch (methodElm.getKind()) {
                                    case CLASS:
                                        m = make.Class(
                                                make.addModifiersModifier(make.removeModifiersModifier(oldOne.getModifiers(), Modifier.PRIVATE), Modifier.PROTECTED),
                                                oldOne.getSimpleName(),
                                                oldOne.getTypeParameters(),
                                                oldOne.getExtendsClause(),
                                                oldOne.getImplementsClause(),
                                                oldOne.getMembers());
                                        break;
                                    case INTERFACE:
                                        m = make.Interface(
                                                make.addModifiersModifier(make.removeModifiersModifier(oldOne.getModifiers(), Modifier.PRIVATE), Modifier.PROTECTED),
                                                oldOne.getSimpleName(),
                                                oldOne.getTypeParameters(),
                                                oldOne.getImplementsClause(),
                                                oldOne.getMembers());
                                        break;
                                    case ANNOTATION_TYPE:
                                        m = make.AnnotationType(
                                                make.addModifiersModifier(make.removeModifiersModifier(oldOne.getModifiers(), Modifier.PRIVATE), Modifier.PROTECTED),
                                                oldOne.getSimpleName(),
                                                oldOne.getMembers());
                                        break;
                                    case ENUM:
                                        m = make.Enum(
                                                make.addModifiersModifier(make.removeModifiersModifier(oldOne.getModifiers(), Modifier.PRIVATE), Modifier.PROTECTED),
                                                oldOne.getSimpleName(),
                                                oldOne.getImplementsClause(),
                                                oldOne.getMembers());
                                        break;
                                }
                                                                
                                RetoucheUtils.copyJavadoc(methodElm, m, workingCopy);
                                njuClass = genUtils.insertClassMember(njuClass, m);
                                
                            }
                        } else {
                            njuClass = genUtils.insertClassMember(njuClass, newMethodTree);
                        }
                        rewrite(tree, njuClass);
                        if (methodElm.getModifiers().contains(Modifier.ABSTRACT)  && !classIsAbstract) {
                            classIsAbstract = true;
                            Set<Modifier> mod = new HashSet<Modifier>(tree.getModifiers().getFlags());
                            mod.add(Modifier.ABSTRACT);
                            mod.remove(Modifier.FINAL);
                            ModifiersTree modifiers = make.Modifiers(mod);
                            rewrite(tree.getModifiers(), modifiers);
                        }
                    }
                }
            }
            try {
                if (imports.size() > 0) {
                    CompilationUnitTree newCut = RetoucheUtils.addImports(workingCopy.getCompilationUnit(), imports, make);
                    rewrite(workingCopy.getCompilationUnit(), newCut);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            for (int i=0; i<members.length; i++) {
                if (members[i].getGroup()==MemberInfo.Group.IMPLEMENTS) {
                    for (Tree t:njuClass.getImplementsClause()) {
                        Element currentInterface = workingCopy.getTrees().getElement(TreePath.getPath(getCurrentPath(), t));
                        if (currentInterface.equals(members[i].getElementHandle().resolve(workingCopy))) {
                            njuClass = make.removeClassImplementsClause(njuClass, t);
                            rewrite(tree, njuClass);
                        }
                    }
                } else {
                    Element current = workingCopy.getTrees().getElement(getCurrentPath());
                    Element currentMember = members[i].getElementHandle().resolve(workingCopy);
                    if (currentMember.getEnclosingElement().equals(current)) {
                        if (classIsAbstract || !members[i].isMakeAbstract()
                                || (currentMember.getModifiers().contains(Modifier.ABSTRACT) && targetType.getKind().isInterface())) {
                            // in case of interface always remove pulled method
                            njuClass = make.removeClassMember(njuClass, workingCopy.getTrees().getTree(currentMember));
                            rewrite(tree, njuClass);
                        } else if (members[i].isMakeAbstract() && currentMember.getModifiers().contains(Modifier.PRIVATE)) {
                            MethodTree method = (MethodTree) workingCopy.getTrees().getTree(currentMember);
                            ModifiersTree mods = make.removeModifiersModifier(method.getModifiers(), Modifier.PRIVATE);
                            mods = make.addModifiersModifier(mods, targetType.getKind().isInterface() ? Modifier.PUBLIC:Modifier.PROTECTED);
                            rewrite(method.getModifiers(), mods);
                        }
                    }
                }
            }
        }
        return super.visitClass(tree, p);
    }
    
}
