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
package org.netbeans.modules.refactoring.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Jan Becicka
 */
public class FileHandlingFactory implements RefactoringPluginFactory {
   
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        if (refactoring instanceof RenameRefactoring) {
            Object o = ((RenameRefactoring)refactoring).getRefactoredObject();
            if (o instanceof FileObject) {
                return new FileRenamePlugin((RenameRefactoring) refactoring);
            }
        } else if (refactoring instanceof MoveRefactoring) {
            Object[] o = ((MoveRefactoring)refactoring).getRefactoredObjects();
            if (o[0] instanceof FileObject) {
                return new FileMovePlugin((MoveRefactoring) refactoring);
            }
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            Object[] o = ((SafeDeleteRefactoring)refactoring).getRefactoredObjects();
            if (o[0] instanceof FileObject) {
                return new FileDeletePlugin((SafeDeleteRefactoring) refactoring);
            }
        } else if (refactoring instanceof SingleCopyRefactoring) {
            Object o = ((SingleCopyRefactoring) refactoring).getRefactoredObject();
            if (o instanceof FileObject) {
                return new FileCopyPlugin((SingleCopyRefactoring) refactoring);
            }
        }
        return null;
    }
    
        /**
     * creates or finds FileObject according to 
     * @param url
     * @return FileObject
     */
    static FileObject getOrCreateFolder(URL url) throws IOException {
        try {
            FileObject result = URLMapper.findFileObject(url);
            if (result != null)
                return result;
            File f = new File(url.toURI());
            
            result = FileUtil.createFolder(f);
            return result;
        } catch (URISyntaxException ex) {
            throw (IOException) new IOException().initCause(ex);
        }
    }

}
