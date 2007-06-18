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
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;

/**
 * Handles package rename.
 *
 * @author Erno Mononen
 */
public class WebXmlPackageRename extends BaseWebXmlRename{
    
    /**
     * The folder or package being renamed.
     */
    private final FileObject pkg;
    private final RenameRefactoring rename;
    
    public WebXmlPackageRename(FileObject webDD, WebApp webModel, FileObject pkg, RenameRefactoring rename) {
        super(webDD, webModel);
        this.pkg = pkg;
        this.rename = rename;
    }
    
    protected List<RenameItem> getRenameItems() {
        List<RenameItem> result = new ArrayList<RenameItem>();
        List<FileObject> fos = new ArrayList<FileObject>();
        RefactoringUtil.collectChildren(pkg, fos);
        for (FileObject each : fos){
            String oldFqn = RefactoringUtil.getQualifiedName(each);
            String newFqn = RefactoringUtil.constructNewName(each, rename);
            result.add(new RenameItem(newFqn, oldFqn));
        }
        return result;
    }
    
    protected AbstractRefactoring getRefactoring() {
        return rename;
    }
    
}
