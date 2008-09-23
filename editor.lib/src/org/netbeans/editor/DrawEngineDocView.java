/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.editor;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.lib.editor.view.GapDocumentView;
import org.netbeans.editor.view.spi.LockView;

/**
 * View of the whole document supporting the code folding.
 *
 * @author Miloslav Metelka
 */
/* package */ class DrawEngineDocView extends GapDocumentView
implements FoldHierarchyListener, PropertyChangeListener {
    
    private static final boolean debugRebuild
        = Boolean.getBoolean("netbeans.debug.editor.view.rebuild"); // NOI18N

    private FoldHierarchy foldHierarchy;
    /** Editor UI listening to */
    private EditorUI editorUI;
    
    private Iterator collapsedFoldIterator;
    private Fold collapsedFold;
    private int collapsedFoldStartOffset;
    private int collapsedFoldEndOffset;
    
    private boolean collapsedFoldsInPresentViews;
    
    private boolean estimatedSpanResetInitiated;
    
    DrawEngineDocView(Element elem) {
        super(elem);
        
        setEstimatedSpan(true);
    }
    
    public void setParent(View parent) {
        if (parent != null) { // start listening
            JTextComponent component = (JTextComponent)parent.getContainer();
            foldHierarchy = FoldHierarchy.get(component);
            foldHierarchy.addFoldHierarchyListener(this);
            TextUI tui = component.getUI();
            if (tui instanceof BaseTextUI){
                editorUI = ((BaseTextUI)tui).getEditorUI();
                if (editorUI!=null){
                    editorUI.addPropertyChangeListener(this);
                }
            }
        }

        super.setParent(parent);
        
        if (parent == null) {
            foldHierarchy.removeFoldHierarchyListener(this);
            foldHierarchy = null;
            if (editorUI!=null){
                editorUI.removePropertyChangeListener(this);
                editorUI = null;
            }
        }
    }
    
    protected void attachListeners(){
        if (foldHierarchy != null) {
        }
    }
    
    private FoldHierarchy getFoldHierarchy() {
        return foldHierarchy;
    }
    
    protected boolean useCustomReloadChildren() {
        return true;
    }

    /**
     * Find next collapsed fold in the given offset range.
     * @param lastCollapsedFold last collapsed fold returned by this method.
     * @param startOffset starting offset of the area in which the collapsed folds
     *  should be searched.
     * @param endOffset ending offset of the area in which the collapsed folds
     *  should be searched.
     */
    protected Fold nextCollapsedFold() {
        while (true) {
            Fold fold = collapsedFoldIterator.hasNext() ? (Fold)collapsedFoldIterator.next() : null;

            // Check whether the fold is not past the doc
            if (fold != null) {
                collapsedFoldStartOffset = fold.getStartOffset();
                collapsedFoldEndOffset = fold.getEndOffset();
                /* Ignore the empty folds as they would make up
                 * no visible view anyway.
                 * Although the fold hierarchy removes the empty views
                 * automatically it may happen that the document listener
                 * that the fold hierarchy attaches may not be notified yet.
                 */
                if (collapsedFoldStartOffset == collapsedFoldEndOffset) {
                    if (debugRebuild) {
                        /*DEBUG*/System.err.println(
                            "GapBoxView.nextCollapsedFold(): ignored empty fold " // NOI18N
                            + fold
                        );
                    }
                    continue; // skip empty fold
                }

                if (collapsedFoldEndOffset > getDocument().getLength()) {
                    /* The fold is past the end of the document.
                     * If a document is going to be switched in the component
                     * the view hierarchy may be notified sooner
                     * than fold hierarchy about that change which
                     * can lead to this state.
                     * That fold is ignored together with the rest of the folds
                     * that would follow it.
                     */
                    fold = null;
                }
            }

            if (fold != null) {
                collapsedFoldsInPresentViews = true;
            }

            return fold;
        }
    }
    
    /**
     * Extra initialization for custom reload of children.
     */
    protected void initCustomReloadChildren(FoldHierarchy hierarchy,
    int startOffset, int endOffset) {
        collapsedFoldIterator = FoldUtilities.collapsedFoldIterator(hierarchy, startOffset, endOffset);
        collapsedFold = nextCollapsedFold();
    }

    /**
     * Free any resources required for custom reload of children.
     */
    protected void finishCustomReloadChildren(FoldHierarchy hierarchy) {
        collapsedFoldIterator = null;
        collapsedFold = null;
    }

    protected void customReloadChildren(int index, int removeLength, int startOffset, int endOffset) {
        // if removing all the views reset the flag
        if (index == 0 && removeLength == getViewCount()) {
            collapsedFoldsInPresentViews = false; // suppose there will be no folds in line views
        }

        FoldHierarchy hierarchy = getFoldHierarchy();
        // Assuming the document lock was already acquired
        if (hierarchy != null) {
            hierarchy.lock();
            try {
                initCustomReloadChildren(hierarchy, startOffset, endOffset);

                super.customReloadChildren(index, removeLength, startOffset, endOffset);

                finishCustomReloadChildren(hierarchy);

            } finally {
                hierarchy.unlock();
            }
        }
    }
        
    protected View createCustomView(ViewFactory f,
    int startOffset, int maxEndOffset, int elementIndex) {
        if (elementIndex == -1) {
            throw new IllegalStateException("Need underlying line element structure"); // NOI18N
        }
        
        View view = null;

        Element elem = getElement();
        Element lineElem = elem.getElement(elementIndex);
        boolean createCollapsed = (collapsedFold != null);

        if (createCollapsed) { // collapsedFold != null
            int lineElemEndOffset = lineElem.getEndOffset();
            createCollapsed = (collapsedFoldStartOffset < lineElemEndOffset);
            if (createCollapsed) { // need to find end of collapsed area
                Element firstLineElem = lineElem;
                List foldAndEndLineElemList = new ArrayList();

                while (true) {
                    int collapsedFoldEndOffset = collapsedFold.getEndOffset();
                    // Find line element index of the line in which the collapsed fold ends
                    while (collapsedFoldEndOffset > lineElemEndOffset) {
                        elementIndex++;
                        lineElem = elem.getElement(elementIndex);
                        lineElemEndOffset = lineElem.getEndOffset();
                    }

                    foldAndEndLineElemList.add(collapsedFold);
                    foldAndEndLineElemList.add(lineElem);

                    collapsedFold = nextCollapsedFold();

                    // No more collapsed or next collapsed does not start on current line
                    if (collapsedFold == null || collapsedFoldStartOffset >= lineElemEndOffset) {
                        break;
                    }
                }
                
                // Create the multi-line-view with collapsed fold(s)
                view = new FoldMultiLineView(firstLineElem, foldAndEndLineElemList);
            }
        }
        
        if (!createCollapsed) {
            view = f.create(lineElem);
        }
     
        return view;
    }            

    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        LockView lockView = LockView.get(this);
        lockView.lock();
        try {
            layoutLock();
            try {
                FoldHierarchy hierarchy = (FoldHierarchy)evt.getSource();
                if (hierarchy.getComponent().getDocument() != lockView.getDocument()) {
                    // Comonent already has a different document assigned
                    // so this view will be abandoned anyway => do not rebuild
                    // the current chilren because of this change
                    return;
                }

                boolean rebuildViews = true;
                int affectedStartOffset = evt.getAffectedStartOffset();
                int affectedEndOffset = evt.getAffectedEndOffset();

                // Check whether it is not a case when there were
                // no collapsed folds before and no collapsed folds now
                if (!collapsedFoldsInPresentViews) { // no collapsed folds previously
                    // TODO Could Integer.MAX_VALUE be used?
                    if (FoldUtilities.findCollapsedFold(hierarchy,
                        affectedStartOffset, affectedEndOffset) == null
                    ) { // no collapsed folds => no need to rebuild
                        rebuildViews = false;
                    }
                }

                if (rebuildViews) {
                    /**
                     * Check the affected offsets against the current document boundaries
                     */
                    int docLength = getDocument().getLength();
                    int rebuildStartOffset = Math.min(affectedStartOffset, docLength);
                    int rebuildEndOffset = Math.min(affectedEndOffset, docLength);
                    offsetRebuild(rebuildStartOffset, rebuildEndOffset);
                }
            } finally {
                updateLayout();
                layoutUnlock();
            }
        } finally {
            lockView.unlock();
        }
    }

    public void paint(Graphics g, Shape allocation) {
        java.awt.Component c = getContainer();
        if (c instanceof javax.swing.text.JTextComponent){
            TextUI textUI = ((javax.swing.text.JTextComponent)c).getUI();
            if (textUI instanceof BaseTextUI){
                ((BaseTextUI) textUI).getEditorUI().paint(g);
            }
        }

        super.paint(g, allocation);

        // #114712 - set the color to foreground so that the JTextComponent.ComposedTextCaret.paint()
        // does not render white-on-white.
        if (c != null) {
            g.setColor(c.getForeground());
        }
    }
    
    public void setSize(float width, float height) {
        super.setSize(width, height);

        /* #69446 - disabled estimated span reset
        // Schedule estimated span reset
        if (!estimatedSpanResetInitiated && isEstimatedSpan()) {
            estimatedSpanResetInitiated = true;
            Timer timer = new Timer(4000, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    AbstractDocument doc = (AbstractDocument)getDocument();
                    if (doc!=null) {
                        doc.readLock();
                        try {
                            LockView lockView = LockView.get(DrawEngineDocView.this);
                            if (lockView != null) { // if there is no lock view no async layout is done
                                lockView.lock();
                                try {
                                    setEstimatedSpan(false);
                                } finally {
                                    lockView.unlock();
                                }
                            }
                        } finally {
                            doc.readUnlock();
                        }
                    }
                }
            });
            
            timer.setRepeats(false);
            timer.start();
        }
         */
    }

    protected boolean isChildrenResizeDisabled() {
        return true;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        JTextComponent component = (JTextComponent)getContainer();
        if (component==null || evt==null || 
            (!EditorUI.LINE_HEIGHT_CHANGED_PROP.equals(evt.getPropertyName()) &&
             !EditorUI.TAB_SIZE_CHANGED_PROP.equals(evt.getPropertyName())
            )
        ) {
            return;
        }
        
        AbstractDocument doc = (AbstractDocument)getDocument();
        if (doc!=null) {
            doc.readLock();
            try{
                LockView lockView = LockView.get(this);
                lockView.lock();
                try {
                    rebuild(0, getViewCount());
                } finally {
                    lockView.unlock();
                }
            } finally {
                doc.readUnlock();
            }
        component.revalidate();
        }
    }
    
    public int getYFromPos(int offset, Shape a) {
        int index = getViewIndex(offset);
        if (index >= 0) {
            Shape ca = getChildAllocation(index, a);
            return (ca instanceof Rectangle)
                    ? ((Rectangle)ca).y
                    : (ca != null) ? ca.getBounds().y : 0;
        }
        return 0;
    }
    
}
