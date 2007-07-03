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

package org.netbeans.modules.refactoring.ruby.plugins;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.refactoring.ruby.RetoucheUtils;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.ruby.RubyElementCtx;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.ruby.RubyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public class RubyRefactoringsFactory implements RefactoringPluginFactory {
   
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        FileObject file = look.lookup(FileObject.class);
        NonRecursiveFolder folder = look.lookup(NonRecursiveFolder.class);
        RubyElementCtx handle = look.lookup(RubyElementCtx.class);
        if (refactoring instanceof WhereUsedQuery) {
            if (handle!=null) {
                return new RubyWhereUsedQueryPlugin((WhereUsedQuery) refactoring);
            }
        } else if (refactoring instanceof RenameRefactoring) {
            if (handle!=null || ((file!=null) && RubyUtils.isRubyOrRhtmlFile(file))) {
                //rename java file, class, method etc..
                return new RenameRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (file!=null && RetoucheUtils.isOnSourceClasspath(file) && file.isFolder()) {
                //rename folder
                return new MoveRefactoringPlugin((RenameRefactoring)refactoring);
            } else if (folder!=null && RetoucheUtils.isOnSourceClasspath(folder.getFolder())) {
                //rename package
                return new MoveRefactoringPlugin((RenameRefactoring)refactoring);
            }
        } else if (refactoring instanceof MoveRefactoring) {
            return new MoveRefactoringPlugin((MoveRefactoring) refactoring);
//        } else if (refactoring instanceof ExtractInterfaceRefactoring) {
//            return new ExtractInterfaceRefactoringPlugin((ExtractInterfaceRefactoring) refactoring);
        }
        return null;
    }
}
