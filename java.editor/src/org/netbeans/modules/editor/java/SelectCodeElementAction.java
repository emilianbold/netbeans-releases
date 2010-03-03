/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.openide.util.Exceptions;
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
            shortDesc = NbBundle.getBundle(JavaKit.class).getString(name); // NOI18N
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
                    handler = new SelectionHandler(target, getShortDescription());
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

    private static final class SelectionHandler implements CaretListener, Task<CompilationController>, Runnable {
        
        private JTextComponent target;
        private String name;
        private SelectionInfo[] selectionInfos;
        private int selIndex = -1;
        private boolean ignoreNextCaretUpdate;
        private AtomicBoolean cancel;

        SelectionHandler(JTextComponent target, String name) {
            this.target = target;
        }

        public void selectNext() {
            if (selectionInfos == null) {
                final JavaSource js = JavaSource.forDocument(target.getDocument());
                cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    public void run() {
                        try {
                            js.runUserActionTask(SelectionHandler.this, true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }, name, cancel, false);
            }            
            run();
        }

        public synchronized void selectPrevious() {
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


        public void run(CompilationController cc) {
            try {
                if (cancel != null && cancel.get())
                    return;
                cc.toPhase(Phase.RESOLVED);
                if (cancel != null && cancel.get())
                    return;
                selectionInfos = initSelectionPath(target, cc);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        private SelectionInfo[] initSelectionPath(JTextComponent target, CompilationController ci) {
            List<SelectionInfo> positions = new ArrayList<SelectionInfo>();
            SourcePositions sp = ci.getTrees().getSourcePositions();
            TreePath tp = ci.getTreeUtilities().pathFor(target.getCaretPosition());
            for (Tree tree: tp) {
                int startPos = (int)sp.getStartPosition(tp.getCompilationUnit(), tree);
                int endPos = (int)sp.getEndPosition(tp.getCompilationUnit(), tree);
                positions.add(new SelectionInfo(startPos, endPos));
            }
            return positions.toArray(new SelectionInfo[positions.size()]);
        }

        public void run() {
            if (selIndex < selectionInfos.length - 1) {
                select(selectionInfos[++selIndex]);
            }
        }

    }
    
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
