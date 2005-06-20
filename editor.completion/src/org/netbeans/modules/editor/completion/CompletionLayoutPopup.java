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
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;


/**
 * Completion popup - either completion, documentation or tooltip
 * popup implementations.
 *
 *  @author Dusan Balek, Miloslav Metelka
 */
class CompletionLayoutPopup {
    
    private CompletionLayout layout;
    
    private Popup popup;
    
    /** Bounds at which the visible popup has. */
    private Rectangle popupBounds;

    private JComponent contentComponent;
    
    private int anchorOffset;
    
    private Rectangle anchorOffsetBounds;
    
    private boolean displayAboveCaret;
    
    private Rectangle screenBounds;
    
    public boolean isVisible() {
        return (popup != null);
    }
    
    public final boolean isActive() {
        return (contentComponent != null);
    }
    
    public void hide() {
        if (isVisible()) {
            popup.hide();
            popup = null;
            contentComponent = null;
            anchorOffset = -1;
            // Reset screen bounds as well to not cache too long
            screenBounds = null;
        }
    }
    
    public boolean isDisplayAboveCaret() {
        return displayAboveCaret;
    }
    
    public Rectangle getPopupBounds() {
        return popupBounds;
    }
    
    void setLayout(CompletionLayout layout) {
        assert (layout != null);
        this.layout = layout;
    }
    
    protected void setContentComponent(JComponent contentComponent) {
        assert (contentComponent != null);
        this.contentComponent = contentComponent;
    }
    
    protected void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
        anchorOffsetBounds = null;
    }
    
    protected Rectangle getScreenBounds() {
        if (screenBounds == null) {
            screenBounds = getEditorComponent().getGraphicsConfiguration().getBounds();
        }
        return screenBounds;
    }
    
    protected int getAnchorOffset() {
        return (anchorOffset != -1)
            ? anchorOffset
            : getEditorComponent().getCaretPosition();
    }
    
    protected final JComponent getContentComponent() {
        return contentComponent;
    }
    
    Dimension getPreferredSize() {
        return getContentComponent().getPreferredSize();
    }
    
    protected CompletionLayout getLayout() {
        return layout;
    }
    
    protected JTextComponent getEditorComponent() {
        return layout.getEditorComponent();
    }
    
    protected int getAnchorHorizontalShift() {
        return 0;
    }

    protected Rectangle getAnchorOffsetBounds() {
        if (anchorOffsetBounds == null){ 
            int anchorOffset = getAnchorOffset();
            try {
                anchorOffsetBounds = getEditorComponent().modelToView(anchorOffset);
                anchorOffsetBounds.x -= getAnchorHorizontalShift();
            } catch (BadLocationException e) {
                anchorOffsetBounds = new Rectangle(); // use empty rectangle
            }
            Point anchorOffsetPoint = anchorOffsetBounds.getLocation();
            SwingUtilities.convertPointToScreen(anchorOffsetPoint, getEditorComponent());
            anchorOffsetBounds.setLocation(anchorOffsetPoint);
        }
        return anchorOffsetBounds;
    }
    
    protected void resetContentPreferredSize() {
        getContentComponent().setPreferredSize(null);
    }
    
    protected Popup getPopup() {
        return popup;
    }
    
    /**
     * Find bounds of the popup based on knowledge of the preferred size
     * of the content component and the preference of the displaying
     * of the popup either above or below the origin rectangle.
     *
     * @param originBounds bounds of the rectangle above or below which
     *   the bounds should be found.
     * @param aboveOrigin whether the bounds should be found for position
     *   above or below the origin rectangle.
     * @return rectangle with absolute screen bounds of the popup.
     */
    final Rectangle findPopupBounds(Rectangle originBounds, boolean aboveOrigin) {
        Dimension prefSize = getPreferredSize();
        Rectangle screen = getScreenBounds();
        Rectangle popupBounds = new Rectangle();
        
        popupBounds.x = Math.min(originBounds.x,
                (screen.x + screen.width) - prefSize.width);
        popupBounds.x = Math.max(popupBounds.x, 0);
        popupBounds.width = Math.min(prefSize.width, screen.width);
        
        if (aboveOrigin) {
            popupBounds.height = Math.min(prefSize.height,
                    originBounds.y - CompletionLayout.POPUP_VERTICAL_GAP);
            popupBounds.y = originBounds.y - CompletionLayout.POPUP_VERTICAL_GAP - popupBounds.height;
        } else { // below caret
            popupBounds.y = originBounds.y
                    + originBounds.height + CompletionLayout.POPUP_VERTICAL_GAP;
            popupBounds.height = Math.min(prefSize.height,
                    (screenBounds.y + screenBounds.height) - popupBounds.y);
        }
        return popupBounds;
    }
    
    protected void showPopup(Rectangle popupBounds, boolean displayAboveCaret) {
        // Hide the original popup if exists
        if (popup != null) {
            popup.hide();
            popup = null;
        }
        
        // Explicitly set the preferred size
        contentComponent.setPreferredSize(popupBounds.getSize());

        PopupFactory factory = PopupFactory.getSharedInstance();
        // Create popup without explicit parent window
        popup = factory.getPopup(null, contentComponent,
                popupBounds.x, popupBounds.y);
        popup.show();

        this.popupBounds = popupBounds;
        this.displayAboveCaret = displayAboveCaret;
    }
    
    void showAtOptimalBounds() {
        boolean aboveCaret = isMoreSpaceAbove(getAnchorOffsetBounds());
        Rectangle bounds = findPopupBounds(getAnchorOffsetBounds(), aboveCaret);
        showPopup(bounds, aboveCaret);
    }
    
    void showAboveOrBelowCaret(boolean aboveCaret) {
        Rectangle bounds = findPopupBounds(getAnchorOffsetBounds(), aboveCaret);
        showPopup(bounds, aboveCaret);
    }
    
    boolean isMoreSpaceAbove(Rectangle bounds) {
        Rectangle screen = getScreenBounds();
        int above = bounds.y - screen.y;
        int below = (screen.y + screen.height) - (bounds.y + bounds.height);
        return (above > below);
    }
    
    boolean isEnoughSpace(Rectangle originBounds, boolean aboveOrigin) {
        Rectangle screen = getScreenBounds();
        int freeHeight = aboveOrigin
            ? originBounds.y - screen.y
            : (screen.y + screen.height) - (originBounds.y + originBounds.height);
        Dimension prefSize = getPreferredSize();
        return (prefSize.height < freeHeight);
    }

}
