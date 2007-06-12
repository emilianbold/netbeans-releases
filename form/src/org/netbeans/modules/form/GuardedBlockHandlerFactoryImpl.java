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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandler;
import org.netbeans.modules.refactoring.spi.GuardedBlockHandlerFactory;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;

/**
 * Used by java refactoring to delegate changes in guarded blocks. Registered
 * in META-INF/services. Creates one GuardedBlockHandlerImpl instance per
 * refactoring (so it can handle more forms).
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

    // -----
    
    private static class GuardedBlockHandlerImpl implements GuardedBlockHandler {
        private RefactoringInfo refInfo;
        private Map<FileObject, GuardedBlockUpdate> guardedUpdates;

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
            FormRefactoringUpdate update = refInfo.getUpdateForFile(changedFile);
            update.setGaurdedCodeChanging(true);

            boolean preloadForm = false;
            boolean canRegenerate = false;

            if (refInfo.getPrimaryFile().equals(changedFile) && refInfo.isForm()) {
                // the change started in this form
                switch (refInfo.getChangeType()) {
                case VARIABLE_RENAME: // renaming field or local variable of initComponents
                case CLASS_RENAME: // renaming form class, need to regenarate use of MyForm.this
                case EVENT_HANDLER_RENAME: // renaming event handler - change the method and calls
                    preloadForm = true;
                    canRegenerate = true;
                    break;
                case CLASS_MOVE:
                    // can't preload the form - it must be loaded and
                    // regenareted *after* moved to the new location
                    canRegenerate = true;
                }
            } else { // change originated in another class
                if (first) {
                    // add the preview element for the overall guarded block change
                    // (for direct form change it was added by our plugin)
                    replacements.add(update.getPreviewElement());
                    first = false;
                }
                // other changes may render the form unloadable (missing component
                // classes), will change the .form file directly...
            }

            // load the form in advance to be sure it can be loaded
            if (preloadForm && !update.prepareForm(true)) {
                return new Problem(true, "Error loading form. Cannot update generated code.");
            }

            if (!canRegenerate) { // guarded block gets changed but it is not safe to load the form
                // remember the change and modify the guarded block directly later
                ModificationResult.Difference diff = proposedChange.getLookup().lookup(ModificationResult.Difference.class);
                if (diff != null) {
                    GuardedBlockUpdate gbUpdate;
                    if (guardedUpdates == null) {
                        guardedUpdates = new HashMap<FileObject, GuardedBlockUpdate>();
                        gbUpdate = null;
                    } else {
                        gbUpdate = guardedUpdates.get(changedFile);
                    }
                    if (gbUpdate == null) {
                        gbUpdate = new GuardedBlockUpdate(
                                update.getFormDataObject().getFormEditorSupport());
                        guardedUpdates.put(changedFile, gbUpdate);
                    }
                    gbUpdate.addChange(diff);
                    transactions.add(gbUpdate);
                }
            }

            // we must add some transaction or element (even if it can be redundant)
            // so it looks like we care about this guarded block change...
            transactions.add(update);

            return null;
        }
    }

    // -----

    /**
     * A transaction for updating guarded blocks directly with changes that came
     * from java refactoring. I.e. no regenerating by form editor.
     */
    private static class GuardedBlockUpdate implements Transaction {
        private FormEditorSupport formEditorSupport;
        private List<GuardedBlockInfo> guardedInfos; // there can be multiple guarded blocks affected

        GuardedBlockUpdate(FormEditorSupport fes) {
            this.formEditorSupport = fes;
            guardedInfos = new ArrayList<GuardedBlockInfo>(2);
            guardedInfos.add(new GuardedBlockInfo(fes.getInitComponentSection()));
            guardedInfos.add(new GuardedBlockInfo(fes.getVariablesSection()));
        }

        void addChange(ModificationResult.Difference diff) {
            for (GuardedBlockInfo block : guardedInfos) {
                if (block.containsPosition(diff)) {
                    block.addChange(diff);
                    break;
                }
            }
        }

        public void commit() {
            for (GuardedBlockInfo block : guardedInfos) {
                String newText = block.getNewSectionText();
                if (newText != null) {
                    formEditorSupport.getGuardedSectionManager()
                        .findSimpleSection(block.getName())
                            .setText(newText);
                }
            }
        }

        public void rollback() {
            // rollback not needed - should be reverted by java refactoring as a whole file
/*            for (GuardedBlockInfo block : guardedInfos) {
                formEditorSupport.getGuardedSectionManager()
                    .findSimpleSection(block.getName())
                        .setText(block.originalText);
            } */
        }
    }

    /**
     * Collects all changes for one guarded block.
     */
    private static class GuardedBlockInfo {
        private String blockName;
        private int originalPosition;
        private String originalText;

        /**
         * Represents one change in the guarded block.
         */
        private static class ChangeInfo implements Comparable<ChangeInfo> {
            private int startPos;
            private int length;
            private String newText;
            ChangeInfo(int startPos, int len, String newText) {
                this.startPos = startPos;
                this.length = len;
                this.newText = newText;
            }

            public int compareTo(ChangeInfo ch) {
                return startPos - ch.startPos;
            }
        }

        private Set<ChangeInfo> changes = new TreeSet<ChangeInfo>();

        GuardedBlockInfo(SimpleSection section) {
            blockName = section.getName();
            originalPosition = section.getStartPosition().getOffset();
            originalText = section.getText();
        }

        boolean containsPosition(ModificationResult.Difference diff) {
            int pos = diff.getStartPosition().getOffset();
            return pos >= originalPosition && pos < originalPosition + originalText.length();
        }

        void addChange(ModificationResult.Difference diff) {
            changes.add(new ChangeInfo(
                    diff.getStartPosition().getOffset() - originalPosition,
                    diff.getOldText().length(),
                    diff.getNewText()));
        }

        String getName() {
            return blockName;
        }

        String getNewSectionText() {
            if (changes.size() > 0) {
                StringBuilder buf = new StringBuilder();
                int lastOrigPos = 0;
                for (ChangeInfo change : changes) {
                    buf.append(originalText.substring(lastOrigPos, change.startPos));
                    buf.append(change.newText);
                    lastOrigPos = change.startPos + change.length;
                }
                buf.append(originalText.substring(lastOrigPos));
                return buf.toString();
            } else {
                return null;
            }
        }
    }

}
