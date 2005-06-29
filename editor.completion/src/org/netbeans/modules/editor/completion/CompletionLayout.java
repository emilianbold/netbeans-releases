/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.completion;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 * Layout of the completion, documentation and tooltip popup windows.
 *
 * @author Dusan Balek, Miloslav Metelka
 */
public final class CompletionLayout {
    
    public static final int COMPLETION_ITEM_HEIGHT = 16;
    
    /**
     * The minimum factor of the completion scrollpane height
     * below which the completion popup will be recreated.
     */
    private static final float COMPLETION_WIDTH_THRESHOLD = 0.5f;
    
    /**
     * The minimum factor of the completion scrollpane height
     * below which the completion popup will be recreated.
     */
    private static final float COMPLETION_HEIGHT_THRESHOLD = 0.7f;
    
    /**
     * Visual shift of the completion window to the left
     * so that the text in the rendered completion items.aligns horizontally
     * with the text in the document.
     */
    private static final int COMPLETION_ANCHOR_HORIZONTAL_SHIFT = 25;
    
    /**
     * Gap between caret and the displayed popup.
     */
    static final int POPUP_VERTICAL_GAP = 1;

    private JTextComponent editorComponent;

    private final CompletionPopup completionPopup;
    private final DocPopup docPopup;
    private final TipPopup tipPopup;
    
    CompletionLayout() {
        completionPopup = new CompletionPopup();
        completionPopup.setLayout(this);
        completionPopup.setPreferDisplayAboveCaret(true);
        docPopup = new DocPopup();
        docPopup.setLayout(this);
        docPopup.setPreferDisplayAboveCaret(false);
        tipPopup = new TipPopup();
        tipPopup.setLayout(this);
        tipPopup.setPreferDisplayAboveCaret(true);
    }
    
    public JTextComponent getEditorComponent() {
        return editorComponent;
    }

    public void setEditorComponent(JTextComponent editorComponent) {
        hideAll();
        this.editorComponent = editorComponent;
    }

    private void hideAll() {
        completionPopup.hide();
        docPopup.hide();
        tipPopup.hide();
    }

    public void showCompletion(List data, String title, int anchorOffset,
    ListSelectionListener listSelectionListener) {
        completionPopup.show(data, title, anchorOffset, listSelectionListener);
    }
    
    public boolean hideCompletion() {
        if (completionPopup.isVisible()) {
            completionPopup.hide();
            return true;
        } else { // not visible
            return false;
        }
    }
    
    public boolean isCompletionVisible() {
        return completionPopup.isVisible();
    }
    
    public CompletionItem getSelectedCompletionItem() {
        return completionPopup.getSelectedCompletionItem();
    }
    
    public void completionProcessKeyEvent(KeyEvent evt) {
        completionPopup.completionProcessKeyEvent(evt);
    }

    public void showDocumentation(CompletionDocumentation doc, int anchorOffset) {
        docPopup.show(doc, anchorOffset);
    }
    
    public boolean hideDocumentation() {
        if (docPopup.isVisible()) {
            docPopup.hide();
            return true;
        } else { // not visible
            return false;
        }
    }
    
    public boolean isDocumentationVisible() {
        return docPopup.isVisible();
    }
    
    public void clearDocumentationHistory() {
        docPopup.clearHistory();
    }
    
    public void documentationProcessKeyEvent(KeyEvent evt) {
        docPopup.documentationProcessKeyEvent(evt);
    }

    
    public void showToolTip(JToolTip toolTip, int anchorOffset) {
        tipPopup.show(toolTip, anchorOffset);
    }
    
    public boolean hideToolTip() {
        if (tipPopup.isVisible()) {
            tipPopup.hide();
            return true;
        } else { // not visible
            return false;
        }
    }
    
    public boolean isToolTipVisible() {
        return tipPopup.isVisible();
    }

    /**
     * Layout either of the copmletion, documentation or tooltip popup.
     * <br>
     * This method can be called recursively to update other popups
     * once certain popup was updated.
     *
     * <p>
     * The rules for the displayment are the following:
     * <ul>
     *  <li> The tooltip popup should be above caret if there is enough space.
     *  <li> The completion popup should be above caret if there is enough space
     *       and the tooltip window is not displayed.
     *  <li> If both tooltip and completion popups are visible then vertically
     *       each should be on opposite side of the anchor bounds (caret).
     *  <li> Documentation should be preferrably shrinked if there is not enough
     *       vertical space.
     *  <li> Documentation anchoring should be aligned with completion.
     * </ul>
     */
    void updateLayout(CompletionLayoutPopup popup) {
        // Make sure the popup returns its natural preferred size
        popup.resetPreferredSize();

        if (popup == completionPopup) { // completion popup
            if (isToolTipVisible()) {
                // Display on opposite side than tooltip
                boolean wantAboveCaret = !tipPopup.isDisplayAboveCaret();
                if (completionPopup.isEnoughSpace(wantAboveCaret)) {
                    completionPopup.showAlongAnchorBounds(wantAboveCaret);
                } else { // not enough space -> show on same side
                    Rectangle occupiedBounds = popup.getAnchorOffsetBounds();
                    occupiedBounds = tipPopup.unionBounds(occupiedBounds);
                    completionPopup.showAlongOccupiedBounds(occupiedBounds,
                            tipPopup.isDisplayAboveCaret());
                }
                
            } else { // tooltip not visible
                popup.showAlongAnchorBounds();
            }
            
            // Update docPopup layout if necessary
            if (docPopup.isVisible()
                && (docPopup.isOverlapped(popup) || docPopup.isOverlapped(tipPopup)
                    || docPopup.getAnchorOffset() != completionPopup.getAnchorOffset()
                    || !docPopup.isShowRetainedPreferredSize())
            ) {
                updateLayout(docPopup);
            }
            
        } else if (popup == docPopup) { // documentation popup
            if (isCompletionVisible()) {
                // Documentation must sync anchoring with completion
                popup.setAnchorOffset(completionPopup.getAnchorOffset());
            }
            
            Rectangle occupiedBounds = popup.getAnchorOffsetBounds();
            occupiedBounds = tipPopup.unionBounds(completionPopup.unionBounds(occupiedBounds));
            docPopup.showAlongOccupiedBounds(occupiedBounds);

        } else if (popup == tipPopup) { // tooltip popup
            popup.showAlongAnchorBounds(); // show possibly above the caret
            if (completionPopup.isOverlapped(popup) || docPopup.isOverlapped(popup)) {
                // docPopup layout will be handled as part of completion popup layout
                updateLayout(completionPopup);
            }
        }
    }
    
    private static final class CompletionPopup extends CompletionLayoutPopup {
        
        private CompletionScrollPane getCompletionScrollPane() {
            return (CompletionScrollPane)getContentComponent();
        }

        public void show(List data, String title, int anchorOffset,
        ListSelectionListener listSelectionListener) {
            
            Dimension lastSize;
            int lastAnchorOffset = getAnchorOffset();

            if (isVisible()) {
                lastSize = getContentComponent().getSize();
                resetPreferredSize();

            } else { // not yet visible => create completion scrollpane
                lastSize = new Dimension(0, 0); // no last size => use (0,0)

                setContentComponent(new CompletionScrollPane(
                    getEditorComponent(), listSelectionListener,
                    new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                            if (SwingUtilities.isLeftMouseButton(evt)) {
                                if (getEditorComponent() != null && evt.getClickCount() == 2 ) {
                                    CompletionItem selectedItem
                                            = getCompletionScrollPane().getSelectedCompletionItem();
                                    if (selectedItem != null) {
                                        selectedItem.defaultAction(getEditorComponent());
                                    }
                                }
                            }
                        }
                    }
                ));
            }

            // Set the new data
            getCompletionScrollPane().setData(data, title);
            setAnchorOffset(anchorOffset);

            Dimension prefSize = getPreferredSize();

            boolean changePopupSize;
            if (isVisible()) {
                changePopupSize = (prefSize.height > lastSize.height)
                                       || (prefSize.width > lastSize.width)
                    || prefSize.height < lastSize.height * COMPLETION_HEIGHT_THRESHOLD
                    || prefSize.width < lastSize.width * COMPLETION_WIDTH_THRESHOLD
                    || anchorOffset != lastAnchorOffset;

            } else { // not visible yet
                changePopupSize = true;
            }

            if (changePopupSize) {
                // Do not change the popup's above/below caret positioning
                // when the popup is already displayed
                getLayout().updateLayout(this);
                
            } // otherwise present popup size will be retained
        }

        public CompletionItem getSelectedCompletionItem() {
            return isVisible() ? getCompletionScrollPane().getSelectedCompletionItem() : null;
        }

        public void completionProcessKeyEvent(KeyEvent evt) {
            if (isVisible()) {
                Object actionMapKey = getCompletionScrollPane().getInputMap().get(
                        KeyStroke.getKeyStrokeForEvent(evt));
                
                if (actionMapKey != null) {
                    Action action = getCompletionScrollPane().getActionMap().get(actionMapKey);
                    if (action != null) {
                        action.actionPerformed(new ActionEvent(getCompletionScrollPane(), 0, null));
                        evt.consume();
                    }
                }
            }
        }

        protected int getAnchorHorizontalShift() {
            return COMPLETION_ANCHOR_HORIZONTAL_SHIFT;
        }

    }
    
    private static final class DocPopup extends CompletionLayoutPopup {
        
        private DocumentationScrollPane getDocumentationScrollPane() {
            return (DocumentationScrollPane)getContentComponent();
        }
        
        protected void show(CompletionDocumentation doc, int anchorOffset) {
            if (!isVisible()) { // documentation already visible
                setContentComponent(new DocumentationScrollPane(getEditorComponent()));
            }
            
            getDocumentationScrollPane().setData(doc);
            
            if (!isVisible()) { // do not check for size as it should remain the same
                // Set anchoring only if not displayed yet because completion
                // may have overriden the anchoring
                setAnchorOffset(anchorOffset);
                getLayout().updateLayout(this);
            } // otherwise leave present doc displayed
        }

        public void documentationProcessKeyEvent(KeyEvent evt) {
            if (isVisible()) {
                getDocumentationScrollPane().processKeyEvt(evt);
            }
        }
        
        public void clearHistory() {
            if (isVisible()) {
                getDocumentationScrollPane().clearHistory();
            }
        }

        protected int getAnchorHorizontalShift() {
            return COMPLETION_ANCHOR_HORIZONTAL_SHIFT;
        }

    }
    
    private static final class TipPopup extends CompletionLayoutPopup {
        
        protected void show(JToolTip toolTip, int anchorOffset) {
            Dimension lastSize;
            if (isVisible()) { // tooltip already visible
                lastSize = getContentComponent().getSize();
                resetPreferredSize();
            } else { // documentation not visible yet
                lastSize = new Dimension(0, 0);
            }
            
            setContentComponent(toolTip);
            setAnchorOffset(anchorOffset);

            // Check whether doc is visible and if so then display
            // on the opposite side
            if (!getPreferredSize().equals(lastSize)) { // preferred sizes differ
                getLayout().updateLayout(this);
            }
        }

    }
    
}
