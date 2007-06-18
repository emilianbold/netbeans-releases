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
package org.netbeans.modules.web.refactoring.rename;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;

/**
 * Handles package and folder rename in tld files.
 *
 * @author Erno Mononen
 */
public class TldPackageRename extends BaseTldRename{
    
    public TldPackageRename(RenameRefactoring rename, FileObject source) {
        super(rename, source);
    }

    protected List<RenameItem> getAffectedClasses() {
        List<FileObject> affectedClasses = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(source, affectedClasses);
        List<RenameItem> result = new ArrayList<RenameItem>();
        for (FileObject affected : affectedClasses){
            String oldName = RefactoringUtil.getQualifiedName(affected);
            String newName = RefactoringUtil.constructNewName(affected, rename);
            result.add(new RenameItem(newName, oldName));
        }
        return result;
    }
    
}
