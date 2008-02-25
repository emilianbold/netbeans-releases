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

package org.netbeans.modules.editor.completion;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.openide.util.Utilities;


/**
 * Completion popup - either completion, documentation or tooltip
 * popup implementations.
 *
 *  @author Dusan Balek, Miloslav Metelka
 */
abstract class CompletionLayoutPopup {
    
    private CompletionLayout layout;
    
    private Popup popup;
    
    /** Bounds at which the visible popup has. */
    private Rectangle popupBounds;

    private JComponent contentComponent;
    
    private int anchorOffset;
    
    private Rectangle anchorOffsetBounds;
    
    private boolean displayAboveCaret;
    
    private Rectangle screenBounds;
    
    private boolean preferDisplayAboveCaret;
    
    private boolean showRetainedPreferredSize;
    
    public final boolean isVisible() {
        return (popup != null);
    }
    
    public final boolean isActive() {
        return (contentComponent != null);
    }
    
    public final void hide() {
        if (isVisible()) {
            popup.hide();
            popup = null;
            popupBounds = null;
            contentComponent = null;
            anchorOffset = -1;
            // Reset screen bounds as well to not cache too long
            screenBounds = null;
        }
    }
    
    public final boolean isDisplayAboveCaret() {
        return displayAboveCaret;
    }
    
    public final Rectangle getPopupBounds() {
        return popupBounds;
    }
    
    final void setLayout(CompletionLayout layout) {
        assert (layout != null);
        this.layout = layout;
    }
    
    final void setPreferDisplayAboveCaret(boolean preferDisplayAboveCaret) {
        this.preferDisplayAboveCaret = preferDisplayAboveCaret;
    }
    
    final void setContentComponent(JComponent contentComponent) {
        assert (contentComponent != null);
        this.contentComponent = contentComponent;
    }
    
    final void setAnchorOffset(int anchorOffset) {
        this.anchorOffset = anchorOffset;
        anchorOffsetBounds = null;
    }
    
    final Rectangle getScreenBounds() {
        if (screenBounds == null) {
	    JTextComponent editorComponent = getEditorComponent();
            GraphicsConfiguration configuration = editorComponent != null
                    ? editorComponent.getGraphicsConfiguration() : null;
            screenBounds = configuration != null
                    ? configuration.getBounds() : new Rectangle();
        }
        return screenBounds;
    }
    
    final int getAnchorOffset() {
	int offset = anchorOffset;
	if (offset == -1) {
	    // Get caret position
	    JTextComponent editorComponent = getEditorComponent();
	    if (editorComponent != null) {
		offset = editorComponent.getSelectionStart();
	    }
	}
	return offset;
    }
    
    final JComponent getContentComponent() {
        return contentComponent;
    }
    
    final Dimension getPreferredSize() {
        JComponent comp = getContentComponent();
        return (comp == null) ? new Dimension(0,0) : comp.getPreferredSize();
    }
    
    final void resetPreferredSize() {
        JComponent comp = getContentComponent();
        if (comp == null){
            return;
        }
        comp.setPreferredSize(null);
    }
    
    final boolean isShowRetainedPreferredSize() {
        return showRetainedPreferredSize;
    }
    
    final CompletionLayout getLayout() {
        return layout;
    }
    
    final JTextComponent getEditorComponent() {
        return layout.getEditorComponent();
    }
    
    protected int getAnchorHorizontalShift() {
        return 0;
    }

    final Rectangle getAnchorOffsetBounds() {
	JTextComponent editorComponent = getEditorComponent();
	if (editorComponent == null) {
	    return new Rectangle();
	}
        if (anchorOffsetBounds == null){ 
            int anchorOffset = getAnchorOffset();
            try {
                anchorOffsetBounds = editorComponent.modelToView(anchorOffset);
                if (anchorOffsetBounds != null){
                    anchorOffsetBounds.x -= getAnchorHorizontalShift();
                } else {
                    anchorOffsetBounds = new Rectangle(); // use empty rectangle
                }
            } catch (BadLocationException e) {
                anchorOffsetBounds = new Rectangle(); // use empty rectangle
            }
            Point anchorOffsetPoint = anchorOffsetBounds.getLocation();
            SwingUtilities.convertPointToScreen(anchorOffsetPoint, editorComponent);
            anchorOffsetBounds.setLocation(anchorOffsetPoint);
        }
        return anchorOffsetBounds;
    }
    
    final Popup getPopup() {
        return popup;
    }
    
    /**
     * Find bounds of the popup based on knowledge of the preferred size
     * of the content component and the preference of the displaying
     * of the popup either above or below the occupied bounds.
     *
     * @param occupiedBounds bounds of the rectangle above or below which
     *   the bounds should be found.
     * @param aboveOccupiedBounds whether the bounds should be found for position
     *   above or below the occupied bounds.
     * @return rectangle with absolute screen bounds of the popup.
     */
    private Rectangle findPopupBounds(Rectangle occupiedBounds, boolean aboveOccupiedBounds) {
        Dimension prefSize = getPreferredSize();
        Rectangle screen = getScreenBounds();
        Rectangle popupBounds = new Rectangle();
        
        popupBounds.x = Math.min(occupiedBounds.x,
                (screen.x + screen.width) - prefSize.width);
        popupBounds.x = Math.max(popupBounds.x, screen.x);
        popupBounds.width = Math.min(prefSize.width, screen.width);
        
        if (aboveOccupiedBounds) {
            popupBounds.height = Math.min(prefSize.height,
                    occupiedBounds.y - screen.y - CompletionLayout.POPUP_VERTICAL_GAP);
            popupBounds.y = occupiedBounds.y - CompletionLayout.POPUP_VERTICAL_GAP - popupBounds.height;
        } else { // below caret
            popupBounds.y = occupiedBounds.y
                    + occupiedBounds.height + CompletionLayout.POPUP_VERTICAL_GAP;
            popupBounds.height = Math.min(prefSize.height,
                    (screen.y + screen.height) - popupBounds.y);
        }
        return popupBounds;
    }
    
    /**
     * Create and display the popup at the given bounds.
     *
     * @param popupBounds location and size of the popup.
     * @param displayAboveCaret whether the popup is displayed above the anchor
     *  bounds or below them (it does not be right above them).
     */
    private void show(Rectangle popupBounds, boolean displayAboveCaret) {
        // Hide the original popup if exists
        if (popup != null) {
            popup.hide();
            popup = null;
        }
        
        // Explicitly set the preferred size
        Dimension origPrefSize = getPreferredSize();
        Dimension newPrefSize = popupBounds.getSize();
        JComponent contComp = getContentComponent();
        if (contComp == null){
            return;
        }
        contComp.setPreferredSize(newPrefSize);
        showRetainedPreferredSize = newPrefSize.equals(origPrefSize);

        PopupFactory factory = PopupFactory.getSharedInstance();
        // Lightweight completion popups don't work well on the Mac - trying
        // to click on its scrollbars etc. will cause the window to be hidden,
        // so force a heavyweight parent by passing in owner==null. (#96717)
        JTextComponent owner = Utilities.isMac() ? null : layout.getEditorComponent();

        // #76648: Autocomplete box is too close to text
        if(displayAboveCaret && Utilities.isMac()) {
            popupBounds.y -= 10;
        }
        
        popup = factory.getPopup(owner, contComp, popupBounds.x, popupBounds.y);
        popup.show();

        this.popupBounds = popupBounds;
        this.displayAboveCaret = displayAboveCaret;
    }
    
    /**
     * Show the popup along the anchor bounds and take
     * the preferred location (above or below caret) into account.
     */
    void showAlongAnchorBounds() {
        showAlongOccupiedBounds(getAnchorOffsetBounds());
    }
    
    void showAlongAnchorBounds(boolean aboveCaret) {
        showAlongOccupiedBounds(getAnchorOffsetBounds(), aboveCaret);
    }
    
    /**
     * Show the popup along the anchor bounds and take
     * the preferred location (above or below caret) into account.
     */
    void showAlongOccupiedBounds(Rectangle occupiedBounds) {
        boolean aboveCaret;
        if (isEnoughSpace(occupiedBounds, preferDisplayAboveCaret)) {
            aboveCaret = preferDisplayAboveCaret;
        } else { // not enough space at preferred location
            // Choose the location with more space
            aboveCaret = isMoreSpaceAbove(occupiedBounds);
        }
        Rectangle bounds = findPopupBounds(occupiedBounds, aboveCaret);
        show(bounds, aboveCaret);
    }
    
    void showAlongOccupiedBounds(Rectangle occupiedBounds, boolean aboveCaret) {
        Rectangle bounds = findPopupBounds(occupiedBounds, aboveCaret);
        show(bounds, aboveCaret);
    }
    
    boolean isMoreSpaceAbove(Rectangle bounds) {
        Rectangle screen = getScreenBounds();
        int above = bounds.y - screen.y;
        int below = (screen.y + screen.height) - (bounds.y + bounds.height);
        return (above > below);
    }
    
    /**
     * Check whether there is enough space for this popup
     * on its preferred location related to caret.
     */
    boolean isEnoughSpace(Rectangle occupiedBounds) {
        return isEnoughSpace(occupiedBounds, preferDisplayAboveCaret);
    }
    
    /**
     * Check whether there is enough space for this popup above
     * or below the given occupied bounds.
     * 
     * @param occupiedBounds bounds above or below which the available
     *  space should be determined.
     * @param aboveOccupiedBounds whether the space should be checked above
     *  or below the occupiedBounds.
     * @return true if there is enough space for the preferred size of this popup
     *  on the requested side or false if not.
     */
    boolean isEnoughSpace(Rectangle occupiedBounds, boolean aboveOccupiedBounds) {
        Rectangle screen = getScreenBounds();
        int freeHeight = aboveOccupiedBounds
            ? occupiedBounds.y - screen.y
            : (screen.y + screen.height) - (occupiedBounds.y + occupiedBounds.height);
        Dimension prefSize = getPreferredSize();
        return (prefSize.height < freeHeight);
    }
    
    boolean isEnoughSpace(boolean aboveCaret) {
        return isEnoughSpace(getAnchorOffsetBounds(), aboveCaret);
    }
    
    public boolean isOverlapped(Rectangle bounds) {
        return isVisible() ? popupBounds.intersects(bounds) : false;
    }

    public boolean isOverlapped(CompletionLayoutPopup popup) {
        return popup.isVisible() ? isOverlapped(popup.getPopupBounds()) : false;
    }
    
    public Rectangle unionBounds(Rectangle bounds) {
        return isVisible() ? bounds.union(getPopupBounds()) : bounds;
    }

    public abstract void processKeyEvent(KeyEvent evt);
}
