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

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.api.JavaWhereUsedQuery;
import org.netbeans.modules.refactoring.spi.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class JavaRefactoringsFactory implements RefactoringPluginFactory {
   
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof JavaWhereUsedQuery) {
            return new JavaWhereUsedQueryPlugin((JavaWhereUsedQuery) refactoring);
        } else if (refactoring instanceof WhereUsedQuery) {
            Object o = ((WhereUsedQuery)refactoring).getRefactoredObject();
            if (o instanceof TreePathHandle) {
                JavaWhereUsedQuery q = new JavaWhereUsedQuery((TreePathHandle)o);
                q.setFindUsages(true);
                q.setSearchFromBaseClass(true);
                return new JavaWhereUsedQueryPlugin(q);
            }
        } else if (refactoring instanceof RenameRefactoring) {
            Object o = ((RenameRefactoring) refactoring).getRefactoredObject();
            if (o instanceof TreePathHandle || o instanceof FileObject && RetoucheUtils.isRefactorable(((FileObject) o))) {
                //rename java file, class, method etc..
                return new RenameRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (o instanceof FileObject && RetoucheUtils.isOnSourceClasspath((FileObject)o) && ((FileObject)o).isFolder()) {
                //rename folder
                return new MoveRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (o instanceof NonRecursiveFolder && RetoucheUtils.isOnSourceClasspath(((NonRecursiveFolder)o).getFolder())) {
                //rename package
                return new MoveRefactoringPlugin((RenameRefactoring)refactoring);
            }
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            if (checkSafeDelete(((SafeDeleteRefactoring)refactoring).getRefactoredObjects())) {
                return new SafeDeleteRefactoringPlugin((SafeDeleteRefactoring)refactoring);
            }
        } else if (refactoring instanceof MoveRefactoring) {
            return new MoveRefactoringPlugin((MoveRefactoring) refactoring);
        }
        return null;
    }

    private boolean checkSafeDelete(Object[] object) {
        if (object.getClass().isAssignableFrom(FileObject[].class)) {
            for (FileObject f:(FileObject[])object) {
                if (!RetoucheUtils.isJavaFile(f)) {
                    return false;
                }
            }
            return true;
        } else if (object.getClass().isAssignableFrom(TreePathHandle[].class)) {
            return true;
        }
        return false;
    }
}
