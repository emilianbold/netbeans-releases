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

package org.netbeans.modules.form;

import java.util.Collection;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;

/**
 * Used by java refactoring to delegate changes in guarded blocks. Registered
 * in META-INF.
 * 
 * @author Tomas Pavek
 */
public class GuardedBlockHandlerFactoryImpl implements GuardedBlockHandlerFactory {
    
    public GuardedBlockHandlerFactoryImpl() {
    }
    
    public GuardedBlockHandler createInstance(AbstractRefactoring refactoring) {
        RefactoringInfo refInfo = refactoring.getContext().lookup(RefactoringInfo.class);
        return new GuardedBlockHandlerImpl(refInfo);
    }

    private static class GuardedBlockHandlerImpl implements GuardedBlockHandler {

        private RefactoringInfo refInfo;

        private boolean first = true;

        public GuardedBlockHandlerImpl(RefactoringInfo refInfo) {
            this.refInfo = refInfo;
        }

        public Problem handleChange(RefactoringElementImplementation proposedChange,
                                    Collection<RefactoringElementImplementation> replacements,
                                    Collection<Transaction> transactions) {
            if (refInfo == null) {
                return null; // unsupported
            }

            FileObject changedFile = proposedChange.getParentFile();
            FormRefactoringUpdate update = refInfo.getUpdateForFile(changedFile, true);
            boolean preloadForm = false;

            if (refInfo.getPrimaryFile().equals(changedFile)) {
                switch (refInfo.getChangeType()) {
                case VARIABLE_RENAME: // renaming field or local variable of initComponents
                case CLASS_RENAME: // renaming form class, need to regenarate use of MyForm.this
                case EVENT_HANDLER_RENAME: // renaming event handler - change the method and calls
                    preloadForm = true;
                    break;
                }
            }
            // other changes may render the form unloadable (missing component classes)
            // TODO: close design if opened (switch to source) for such forms
            // will change the .form file directly...

            // load the form in advance to be sure it can be loaded
            if (preloadForm && !update.loadForm()) {
                return new Problem(true, "Error loading form. Cannot update generated code.");
            }

            // but otherwise do nothing - just make sure the update transaction is registered
            transactions.add(update);

            if (first) {
                // add one placeholder "refactoring element" that will represent
                // the guarded code update in the refactoring preview
                replacements.add(new PlaceholderRefactoringElement(proposedChange.getParentFile()));
                first = false; // one is enough...
            }

            return null;
        }
    }

    private static class PlaceholderRefactoringElement extends SimpleRefactoringElementImplementation {
        private FileObject file;
        PlaceholderRefactoringElement(FileObject fo) {
            file = fo;
        }

        public String getText() {
            return "Guarded update";
        }

        public String getDisplayText() {
            return "Update generated code in guarded blocks";
        }

        public void performChange() {
        }

        public Object getComposite() {
            return getParentFile();
        }

        public FileObject getParentFile() {
            return file;
        }

        public PositionBounds getPosition() {
            return null;
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }
}
