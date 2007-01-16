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
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;

/**
 *
 * @author  Jan Becicka
 */
public class FileMovePlugin implements RefactoringPlugin {
    private MoveRefactoring refactoring;
    
    /** Creates a new instance of WhereUsedQuery */
    public FileMovePlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    public Problem preCheck() {
        return null;
    }
    
    public Problem prepare(RefactoringElementsBag elements) {
        for (Object o: refactoring.getRefactoredObjects()) {
            if (o instanceof FileObject) {
                elements.add(refactoring, new MoveFile((FileObject) o, elements));
            }
        }
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
    
    private class MoveFile extends SimpleRefactoringElementImpl {
        
        private FileObject fo;
        private RefactoringElementsBag session;
        public MoveFile(FileObject fo, RefactoringElementsBag session) {
            this.fo = fo;
            this.session = session;
        }
        public String getText() {
            return "Move file " + fo.getNameExt();
        }

        public String getDisplayText() {
            return getText();
        }

        public void performChange() {
            session.registerFileChange(new Transaction() {
                DataFolder sourceFolder;
                DataObject source;
                public void commit() {
                    try {
                        FileObject target = FileHandlingFactory.getOrCreateFolder((URL) refactoring.getTarget());
                        DataFolder targetFolder = DataFolder.findFolder(target);
                        source = DataObject.find(fo);
                        sourceFolder = source.getFolder();
                        source.move(targetFolder);
                    } catch (DataObjectNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                public void rollback() {
                    try {
                        source.move(sourceFolder);
                    } catch (DataObjectNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
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
