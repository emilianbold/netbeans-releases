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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.api.ExtractSuperclassRefactoring;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.java.api.PushDownRefactoring;
import org.netbeans.modules.refactoring.java.api.UseSuperTypeRefactoring;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring;
import org.netbeans.modules.refactoring.spi.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public class JavaRefactoringsFactory implements RefactoringPluginFactory {
   
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        FileObject file = look.lookup(FileObject.class);
        NonRecursiveFolder folder = look.lookup(NonRecursiveFolder.class);
        TreePathHandle handle = look.lookup(TreePathHandle.class);
        if (refactoring instanceof WhereUsedQuery) {
            if (handle!=null) {
                return new JavaWhereUsedQueryPlugin((WhereUsedQuery) refactoring);
            }
        } else if (refactoring instanceof RenameRefactoring) {
            if (handle!=null || ((file!=null) && RetoucheUtils.isJavaFile(file))) {
                //rename java file, class, method etc..
                return new RenameRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (file!=null && RetoucheUtils.isOnSourceClasspath(file) && file.isFolder()) {
                //rename folder
                return new MoveRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (folder!=null && RetoucheUtils.isOnSourceClasspath(folder.getFolder())) {
                //rename package
                return new MoveRefactoringPlugin((RenameRefactoring)refactoring);
            }
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            //TODO: should be implemented better
            if (checkSafeDelete(refactoring.getRefactoringSource())) {
                return new SafeDeleteRefactoringPlugin((SafeDeleteRefactoring)refactoring);
            }
        } else if (refactoring instanceof MoveRefactoring) {
            return new MoveRefactoringPlugin((MoveRefactoring) refactoring);
        } else if (refactoring instanceof SingleCopyRefactoring) {
            if (checkCopy(refactoring.getRefactoringSource())) {
                return new CopyClassRefactoringPlugin((SingleCopyRefactoring) refactoring);
            }
        } else if (refactoring instanceof ExtractInterfaceRefactoring) {
            return new ExtractInterfaceRefactoringPlugin((ExtractInterfaceRefactoring) refactoring);
        } else if (refactoring instanceof ExtractSuperclassRefactoring) {
            return new ExtractSuperclassRefactoringPlugin((ExtractSuperclassRefactoring) refactoring);
        } else if (refactoring instanceof PullUpRefactoring) {
            return new PullUpRefactoringPlugin((PullUpRefactoring)refactoring);
        } else if (refactoring instanceof PushDownRefactoring) {
            return new PushDownRefactoringPlugin((PushDownRefactoring) refactoring);
        } else if (refactoring instanceof UseSuperTypeRefactoring) {
            return new UseSuperTypeRefactoringPlugin((UseSuperTypeRefactoring) refactoring);
        } else if (refactoring instanceof InnerToOuterRefactoring) {
            return new InnerToOuterRefactoringPlugin((InnerToOuterRefactoring) refactoring);
        } else if (refactoring instanceof ChangeParametersRefactoring) {
            return new ChangeParametersPlugin((ChangeParametersRefactoring) refactoring);
        } else if (refactoring instanceof EncapsulateFieldRefactoring) {
            return new EncapsulateFieldRefactoringPlugin((EncapsulateFieldRefactoring) refactoring);
        } else if (refactoring instanceof EncapsulateFieldsRefactoring) {
            return new EncapsulateFieldsPlugin((EncapsulateFieldsRefactoring) refactoring);
        }
        return null;
    }

    //TODO: should be implemented better
    private boolean checkSafeDelete(Lookup object) {
        boolean a=false;
        for (FileObject f:object.lookupAll(FileObject.class)) {
            a=true;
            if (!RetoucheUtils.isJavaFile(f)) {
                return false;
            }
        }
        if (object.lookup(TreePathHandle.class)!=null)
            return true;
        
        return a;
    }
    
    private boolean checkCopy(Lookup object) {
        FileObject f=object.lookup(FileObject.class);
        if (f!=null && RetoucheUtils.isJavaFile(f))
            return true;
        return false;
    }

}
