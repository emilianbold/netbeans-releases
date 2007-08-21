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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;

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
    
    public void setWorkingCopy(WorkingCopy copy) throws ToPhaseException {
        super.setWorkingCopy(copy);
        this.targetType = refactoring.getTargetType().resolve(copy);
    }

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        boolean classIsAbstract = el.getKind().isInterface();
        ClassTree njuClass = tree;
        if (el.equals(targetType)) {
            //target type
            //add members
            for (int i = 0; i<members.length; i++) {
                if (members[i].getGroup()==MemberInfo.Group.IMPLEMENTS) {
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
                        
                        
                        MethodTree method = (MethodTree) workingCopy.getTrees().getTree(members[i].getElementHandle().resolve(workingCopy));
                        Set<Modifier> mod = new HashSet<Modifier>(method.getModifiers().getFlags());
                        mod.add(Modifier.ABSTRACT);
                        mod.remove(Modifier.FINAL);
                        if (el.getKind().isInterface()) {
                            mod.remove(Modifier.PUBLIC);
                            mod.remove(Modifier.PROTECTED);
                            mod.remove(Modifier.PRIVATE);
                            mod.remove(Modifier.ABSTRACT);
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
                        njuClass = make.addClassMember(njuClass, nju);
                        rewrite(tree, njuClass);
                    } else {
                        njuClass = make.addClassMember(njuClass, workingCopy.getTrees().getTree(members[i].getElementHandle().resolve(workingCopy)));
                        rewrite(tree, njuClass);
                    }
                }
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
                        if (!members[i].isMakeAbstract()) {
                            njuClass = make.removeClassMember(njuClass, workingCopy.getTrees().getTree(currentMember));
                            rewrite(tree, njuClass);
                        }
                    }
                }
            }
        }
        return super.visitClass(tree, p);
    }
    
}
