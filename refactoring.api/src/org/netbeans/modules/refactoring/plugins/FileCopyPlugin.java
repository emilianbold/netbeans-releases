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

import java.io.IOException;
import java.net.URL;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

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
        elements.add(refactoring, new CopyFile(refactoring.getRefactoringSource().lookup(FileObject.class), elements.getSession()));
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
    
    private class CopyFile extends SimpleRefactoringElementImplementation {
        
        private FileObject fo;
        private RefactoringSession session;
        private DataObject newOne;
        public CopyFile(FileObject fo, RefactoringSession session) {
            this.fo = fo;
            this.session = session;
        }
        public String getText() {            
            return NbBundle.getMessage(FileCopyPlugin.class, "TXT_CopyFile") + fo.getNameExt();
        }
        
        public String getDisplayText() {
            return getText();
        }
        
        public void performChange() {
            try {
                FileObject fo = FileHandlingFactory.getOrCreateFolder(refactoring.getTarget().lookup(URL.class));
                FileObject source = refactoring.getRefactoringSource().lookup(FileObject.class);
                DataObject dob = DataObject.find(source);
                newOne = dob.copy(DataFolder.findFolder(fo));
                newOne.rename(refactoring.getNewName());
                refactoring.getContext().add(newOne.getPrimaryFile());
            } catch (Exception ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        
        @Override
        public void undoChange() {
            try {
                newOne.delete();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getParentFile() {
            return fo;
        }
        
        public PositionBounds getPosition() {
            return null;
        }
    }
}
