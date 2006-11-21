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

import com.sun.source.tree.*;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.*;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class MoveTransformer extends SearchVisitor {

    private FileObject originalFolder;
    private MoveRefactoringPlugin move;
    private Set<Element> elementsToImport = new HashSet();
    private boolean isThisFileMoving;
    private boolean isThisFileReferencingOldPackage = false;
    

    public MoveTransformer(WorkingCopy workingCopy, MoveRefactoringPlugin move) {
        super(workingCopy);
        originalFolder = workingCopy.getFileObject().getParent();
        this.move = move;
        isThisFileMoving = move.filesToMove.contains(workingCopy.getFileObject());
    }
    
    @Override
    public Tree visitMemberSelect(MemberSelectTree node, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (el!=null) {
            if (isElementMoving(el)) {
                Tree nju = make.MemberSelect(make.Identifier(move.getTargetPackageName(move.filesToMove.get(index))), el);
                workingCopy.rewrite(node, nju);
            }
        }
        return super.visitMemberSelect(node, p);
    }
    
    
    @Override 
    public Tree visitIdentifier(IdentifierTree node, Element p) {
        Element el = workingCopy.getTrees().getElement(getCurrentPath());
        if (el!=null) {
            if (!isThisFileMoving) {
                if (isElementMoving(el)) {
                    elementsToImport.add(el);
                }
            } else {
                if (!isThisFileReferencingOldPackage && (!isElementMoving(el) && isTopLevelClass(el)) && getPackageOf(el).toString().equals(RetoucheUtils.getPackageName(workingCopy.getFileObject().getParent()))) {
                    isThisFileReferencingOldPackage = true;
                }
            }
        }
        
        return super.visitIdentifier(node, p);
    }
    private PackageElement getPackageOf(Element el) {
        //return workingCopy.getElements().getPackageOf(el);
        while (el.getKind() != ElementKind.PACKAGE) 
            el = el.getEnclosingElement();
        return (PackageElement) el;
    }
    
    
    
    private boolean isThisFileReferencedbyOldPackage() {
        Set<FileObject> references = new HashSet(move.whoReferences.get(workingCopy.getFileObject()));
        references.removeAll(move.filesToMove);
        for (FileObject file:references) {
            if (file.getParent().equals(originalFolder))
                return true;
        }
        return false;
    }
    
//    private boolean isThisFileReferencingOldPackage() {
//        //TODO: correctly implement
//        return true;
//    }
    
    private int index;
    private boolean isElementMoving(Element el) {
        index=0;
        for (ElementHandle handle:move.classes) {
            index++;
            if (handle.signatureEquals(el)) {
                return true;
            }
        }
        index=-1;
        return false;
    }
    
    private boolean isTopLevelClass(Element el) {
        return (el.getKind().isClass() || 
                el.getKind().isInterface()) &&
                el.getEnclosingElement().getKind() == ElementKind.PACKAGE;
    }
    
    @Override
    public Tree visitCompilationUnit(CompilationUnitTree node, Element p) {
        Tree result = super.visitCompilationUnit(node, p);
        if (isThisFileMoving) {
            //change package statement
            workingCopy.rewrite(node.getPackageName(), make.Identifier(move.getTargetPackageName(workingCopy.getFileObject())));
            if (isThisFileReferencingOldPackage) {
                //add import to old package
                node = insertImport(node, node.getPackageName().toString() + ".*");
            }
        }
        for (Element el:elementsToImport) {
            FileObject fo = SourceUtils.getFile(el, workingCopy.getClasspathInfo());
            node = insertImport(node, move.getTargetPackageName(fo) + "." +el.getSimpleName());
        }
        return result;
    }
    
    private CompilationUnitTree insertImport(CompilationUnitTree node, String imp) {
        for (ImportTree tree: node.getImports()) {
            if (tree.getQualifiedIdentifier().toString().equals(imp)) 
                return node;
        }
        CompilationUnitTree nju = make.insertCompUnitImport(node, 0, make.Import(make.Identifier(imp), false));
        workingCopy.rewrite(node, nju);
        return nju;
    }
    
}
