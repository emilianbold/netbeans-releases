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

import java.net.URL;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.ui.UI;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;

/**
 *
 * @author  Jan Becicka
 */
public class FileCopyPlugin implements RefactoringPlugin {
    private SingleCopyRefactoring refactoring;
    
    /** Creates a new instance of WhereUsedQuery */
    public FileCopyPlugin(SingleCopyRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    public Problem preCheck() {
        return null;
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        elements.add(refactoring, new CopyFile((FileObject) refactoring.getRefactoredObject(), elements.getSession()));
        return null;
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    public Problem checkParameters() {
        return null;
    }
    
    public void cancelRequest() {
    }
    
    private class CopyFile extends SimpleRefactoringElementImpl {
        
        private FileObject fo;
        private RefactoringSession session;
        public CopyFile(FileObject fo, RefactoringSession session) {
            this.fo = fo;
            this.session = session;
        }
        public String getText() {
            return "Copy file " + fo.getNameExt();
        }
        
        public String getDisplayText() {
            return getText();
        }
        
        public void performChange() {
            try {
                FileObject fo = UI.getOrCreateFolder((URL)refactoring.getTarget());
                FileObject source = (FileObject) refactoring.getRefactoredObject();
                DataObject dob = DataObject.find(source);
                DataObject d = dob.copy(DataFolder.findFolder(fo));
                d.rename(refactoring.getNewName());
                refactoring.getContext().add(d.getPrimaryFile());
            } catch (Exception ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        
        public Object getComposite() {
            return fo;
        }
        
        public FileObject getParentFile() {
            return fo;
        }
        
        public PositionBounds getPosition() {
            return null;
        }
    }
}
