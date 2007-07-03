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
import javax.lang.model.element.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class CopyTransformer extends RefactoringVisitor {
    
    private String newName;
    private boolean insertImport;
    private String oldPackage;
    private String oldName;
    private String newPackage;

    public CopyTransformer(WorkingCopy workingCopy, String oldName, String newName, boolean insertImport, String oldPackage) {
        try {
            setWorkingCopy(workingCopy);
            this.newName = newName;
            this.insertImport = insertImport;
            this.oldPackage = oldPackage;
            this.oldName = oldName;
            this.newPackage = RetoucheUtils.getPackageName(workingCopy.getFileObject().getParent());
        } catch (ToPhaseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Override
    public Tree visitCompilationUnit(CompilationUnitTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            if (insertImport) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                Tree tree2 = make.insertCompUnitImport(tree, 0, make.Import(make.Identifier(oldPackage + ".*"), false));
                rewrite(tree, tree2);
            }
        }
        return super.visitCompilationUnit(tree, p);
    }         

    @Override
    public Tree visitClass(ClassTree tree, Element p) {
        if (!workingCopy.getTreeUtilities().isSynthetic(getCurrentPath())) {
            TypeElement currentClass = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            if (!currentClass.getNestingKind().isNested() && (tree.getSimpleName().toString().endsWith("_1")|| tree.getSimpleName().toString().equals(oldName))) {
                Tree nju = make.setLabel(tree, newName);
                rewrite(tree, nju);
            }
        }
        return super.visitClass(tree, p);
    }
    
    @Override
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitIdentifier(node, p);
    }

    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        renameUsageIfMatch(getCurrentPath(), node,p);
        return super.visitMemberSelect(node, p);
    }
    
    private void renameUsageIfMatch(TreePath path, Tree tree, Element elementToFind) {
        if (workingCopy.getTreeUtilities().isSynthetic(path))
            return;
        Element el = workingCopy.getTrees().getElement(path);
        if (el==null)
            return;
        
        if ((el instanceof TypeElement) && ((TypeElement) el).getQualifiedName().toString().equals(newPackage+"."+oldName)) {
            Tree nju = make.setLabel(tree, newName);
            rewrite(tree, nju);
        }
    }
    
}
