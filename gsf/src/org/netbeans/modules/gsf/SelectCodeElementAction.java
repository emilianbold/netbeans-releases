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

package org.netbeans.modules.gsf;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.retouche.source.CompilationController;
import org.netbeans.api.retouche.source.Phase;
import org.netbeans.api.retouche.source.Source;
import org.netbeans.editor.BaseAction;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Code selection according to syntax tree.
 *
 * TODO: javadoc selection
 *
 * @author Miloslav Metelka, Jan Pokorsky
 */
final class SelectCodeElementAction extends BaseAction {

    private boolean selectNext;

    /**
     * Construct new action that selects next/previous code elements
     * according to the language model.
     * <br>
     *
     * @param name name of the action (should be one of
     *  <br>
     *  <code>JavaKit.selectNextElementAction</code>
     *  <code>JavaKit.selectPreviousElementAction</code>
     * @param selectNext <code>true</code> if the next element should be selected.
     *  <code>False</code> if the previous element should be selected.
     */
    public SelectCodeElementAction(String name, boolean selectNext) {
        super(name);
        this.selectNext = selectNext;
        String desc = getShortDescription();
        if (desc != null) {
            putValue(SHORT_DESCRIPTION, desc);
        }
    }
        
    public String getShortDescription(){
        String name = (String)getValue(Action.NAME);
        if (name == null) return null;
        String shortDesc;
        try {
            shortDesc = NbBundle.getBundle(GsfEditorKitFactory.class).getString(name); // NOI18N
        }catch (MissingResourceException mre){
            shortDesc = name;
        }
        return shortDesc;
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            int selectionStartOffset = target.getSelectionStart();
            int selectionEndOffset = target.getSelectionEnd();
            if (selectionEndOffset > selectionStartOffset || selectNext) {
                SelectionHandler handler = (SelectionHandler)target.getClientProperty(SelectionHandler.class);
                if (handler == null) {
                    handler = new SelectionHandler(target);
                    target.addCaretListener(handler);
                    // No need to remove the listener above as the handler
                    // is stored is the client-property of the component itself
                    target.putClientProperty(SelectionHandler.class, handler);
                }
                
                if (selectNext) { // select next element
                    handler.selectNext();
                } else { // select previous
                    handler.selectPrevious();
                }
            }
        }
    }

    private static final class SelectionHandler implements CaretListener, CancellableTask<CompilationController>, Runnable {
        
        private JTextComponent target;
        private SelectionInfo[] selectionInfos;
        private int selIndex = -1;
        private boolean ignoreNextCaretUpdate;

        SelectionHandler(JTextComponent target) {
            this.target = target;
        }

        public void selectNext() {
            if (selectionInfos == null) {
                Source js = Source.forDocument(target.getDocument());
                try {
                    js.runUserActionTask(this, true);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            
            run();
        }

        public synchronized void selectPrevious() {
            if (selIndex == -1) {
                // Try to figure out the selected AST index based on the editor selection
                selIndex = computeSelIndex(false);
            }
            if (selIndex > 0) {
                select(selectionInfos[--selIndex]);
            }
        }

        private void select(SelectionInfo selectionInfo) {
            Caret caret = target.getCaret();
            markIgnoreNextCaretUpdate();
            caret.setDot(selectionInfo.getStartOffset());
            markIgnoreNextCaretUpdate();
            caret.moveDot(selectionInfo.getEndOffset());
        }
        
        private void markIgnoreNextCaretUpdate() {
            ignoreNextCaretUpdate = true;
        }
        
        public void caretUpdate(CaretEvent e) {
            if (!ignoreNextCaretUpdate) {
                synchronized (this) {
                    selectionInfos = null;
                    selIndex = -1;
                }
            }
            ignoreNextCaretUpdate = false;
        }

        public void cancel() {
        }

        public void run(CompilationController cc) {
            try {
                cc.toPhase(Phase.RESOLVED);
                selectionInfos = initSelectionPath(target, cc);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        private SelectionInfo[] initSelectionPath(JTextComponent target, CompilationController ci) {
            Language language = ci.getLanguage();
            if (language.getBracketCompletion() != null) {
                List<OffsetRange> ranges = language.getBracketCompletion().findLogicalRanges(ci, target.getCaretPosition());
                SelectionInfo[] result = new SelectionInfo[ranges.size()];
                for (int i = 0; i < ranges.size(); i++) {
                    OffsetRange range = ranges.get(i);
                    result[i] = new SelectionInfo(range.getStart(), range.getEnd());
                }
                return result;
            } else {
                return new SelectionInfo[0];
            }
        }
        
        private int computeSelIndex(boolean inner) {
            Caret caret = target.getCaret();
            if (selectionInfos != null && caret != null && caret.getDot() != caret.getMark()) {
                int dot = caret.getDot();
                int mark = caret.getMark();
                int start = Math.min(dot,mark);
                //int end = Math.max(dot,mark);
                for (int i = 0; i < selectionInfos.length; i++) {
                    if (selectionInfos[i].getStartOffset() == start) {
                        // TODO - check end offset too
                        return i;
                    }
                }
                // No exact match - look at the editor selection and find the range
                // that most closely surround the selection (if inner is true, go
                // for the inner one, otherwise the outer)
                for (int i = selectionInfos.length-2; i >= 0; i--) {
                    if (selectionInfos[i].getStartOffset() > start &&
                            selectionInfos[i+1].getStartOffset() < start) {
                        return inner ? i : i-1;
                    }
                }
            }
            
            return selIndex;
        }

        public void run() {
            if (selIndex == -1) {
                // Try to figure out the selected AST index based on the editor selection
                selIndex = computeSelIndex(true);
            }
            if (selIndex < selectionInfos.length - 1) {
                select(selectionInfos[++selIndex]);
            }
        }

    }
    
    // This looks a lot like an OffsetRange! Just reuse my own OffsetRange class?
    private static final class SelectionInfo {
        
        private int startOffset;
        private int endOffset;
        
        SelectionInfo(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public int getStartOffset() {
            return startOffset;
        }
        
        public int getEndOffset() {
            return endOffset;
        }
        
    }
}
