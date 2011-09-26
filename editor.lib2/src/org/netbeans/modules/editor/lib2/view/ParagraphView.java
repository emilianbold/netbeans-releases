/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;


/**
 * View of a visual line.
 * <br/>
 * It is capable to do a word-wrapping (see {@link ParagraphWrapView}.
 * <br/>
 * It is not tight to any element (its element is null).
 * Its contained views may span multiple lines (e.g. in case of code folding).
 * 
 * @author Miloslav Metelka
 */

public final class ParagraphView extends EditorView implements EditorView.Parent {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ParagraphView.level=FINE
    private static final Logger LOG = Logger.getLogger(ParagraphView.class.getName());
    
    /**
     * Total preferred width of this view.
     * If children are currently not initialized this value may present last height.
     */
    private float width; // 24=super + 4 = 28 bytes
    
    /**
     * Total preferred height of this view.
     * If children are currently not initialized this value may present last height.
     */
    private float height; // 28 + 4 = 32 bytes

    private Position startPos; // 32 + 4 = 36 bytes
    
    /**
     * Total length of the paragraph view.
     */
    private int length; // 36 + 4 = 40 bytes
    
    ParagraphViewChildren children; // 40 + 4 = 44 bytes

    public ParagraphView(Position startPos) {
        super(null);
        this.startPos = startPos;
    }

    @Override
    public int getStartOffset() {
        return startPos.getOffset();
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    @Override
    public int getLength() { // Total length of contained child views
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
    
    @Override
    public int getRawEndOffset() {
        return -1;
    }

    @Override
    public void setRawEndOffset(int rawOffset) {
        throw new IllegalStateException("setRawOffset() must not be called on ParagraphView."); // NOI18N
    }
    
    float getWidth() {
        return width;
    }
    
    void setWidth(float width) {
        this.width = width;
    }
    
    void resetWidth() {
        this.width = 0f;
        if (children != null) {
            children.resetWidth();
        }
    }
    
    float getHeight() {
        return height;
    }
    
    void setHeight(float height) {
        this.height = height;
    }
    
    @Override
    public float getPreferredSpan(int axis) {
        return (axis == View.X_AXIS) ? width : height;
    }
    
    /**
     * Returns the number of child views of this view.
     *
     * @return the number of views &gt;= 0
     * @see #getView(int)
     */
    @Override
    public final int getViewCount() {
        return (children != null) ? children.size() : 0;
    }

    /**
     * Returns the view in this container with the particular index.
     *
     * @param index index of the desired view, &gt;= 0 and &lt; getViewCount()
     * @return the view at index <code>index</code>
     */
    @Override
    public View getView(int index) {
        return (children != null) ? children.get(index) : null;
    }

    public final EditorView getEditorView(int index) {
        return children.get(index);
    }

    @Override
    public AttributeSet getAttributes() {
        return null;
    }

    void releaseChildren() {
        children = null;
    }
    
    /*
     * Replaces child views.
     *
     * @param index the starting index into the child views >= 0
     * @param length the number of existing views to replace >= 0
     * @param views the child views to insert
     */
    @Override
    public void replace(int index, int length, View[] views) {
        replace(index, length, views, 0);
    }

    void replace(int index, int length, View[] views, int offsetDelta) {
        if (children == null) {
            assert (length == 0) : "Attempt to remove from null children length=" + length; // NOI18N
            children = new ParagraphViewChildren(views.length);
        }
        children.replace(this, index, length, views, offsetDelta);
    }

    /**
     * Child views can call this on the parent to indicate that
     * the preference has changed and should be reconsidered
     * for layout.
     *
     * @param childView the child view of this view or null to signal
     *  change in this view.
     * @param widthChange true if the width preference has changed
     * @param heightChange true if the height preference has changed
     * @see javax.swing.JComponent#revalidate
     */
    @Override
    public void preferenceChanged(View childView, boolean widthChange, boolean heightChange) {
        if (childView == null) { // notify parent about this view change
            View parent = getParent();
            if (parent != null) {
                parent.preferenceChanged(this, widthChange, heightChange);
            }
        } else { // Child of this view has changed
            if (children != null) { // Ignore possible stale notification
                children.preferenceChanged(this, (EditorView)childView, widthChange, heightChange);
            }
        }
    }

    @Override
    public Shape getChildAllocation(int index, Shape alloc) {
        checkChildrenNotNull();
        return children.getChildAllocation(index, alloc);
    }

    /**
     * Returns the child view index representing the given position in
     * the model.
     *
     * @param offset the position >= 0.
     * @param b either forward or backward bias.
     * @return  index of the view representing the given position, or 
     *   -1 if no view represents that position
     */
    @Override
    public int getViewIndex(int offset, Position.Bias b) {
        if (b == Position.Bias.Backward) {
            offset--;
        }
        return getViewIndex(offset);
    }

    /**
     * Returns the child view index representing the given position in
     * the model.
     *
     * @param offset the position >= 0.
     * @return  index of the view representing the given position, or 
     *   -1 if no view represents that position
     */
    public int getViewIndex(int offset) {
        checkChildrenNotNull();
        return children.getViewIndex(this, offset);
    }

    @Override
    public int getViewIndexChecked(double x, double y, Shape alloc) {
        checkChildrenNotNull();
        return children.getViewIndex(this, x, y, alloc);
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Bias bias) {
        checkChildrenNotNull();
        return children.modelToViewChecked(this, offset, alloc, bias);
        
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        checkChildrenNotNull();
        return children.viewToModelChecked(this, x, y, alloc, biasReturn);
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        // The background is already cleared by BasicTextUI.paintBackground() which uses component.getBackground()
        checkChildrenNotNull();
        children.paint(this, g, alloc, clipBounds);
    }

    @Override
    public JComponent getToolTip(double x, double y, Shape allocation) {
        checkChildrenNotNull();
        return children.getToolTip(this, x, y, allocation);
    }

    @Override
    public String getToolTipTextChecked(double x, double y, Shape allocation) {
        checkChildrenNotNull();
        return children.getToolTipTextChecked(this, x, y, allocation);
    }

    @Override
    public HighlightsSequence getPaintHighlights(EditorView view, int shift) {
        return getDocumentView().getPaintHighlights(view, shift);
    }

    @Override
    public void notifyChildWidthChange() {
        View parent = getParent();
        if (parent instanceof EditorView.Parent) {
            ((EditorView.Parent) parent).notifyChildWidthChange(); // Forward to parent
        }
    }

    @Override
    public void notifyChildHeightChange() {
        View parent = getParent();
        if (parent instanceof EditorView.Parent) {
            ((EditorView.Parent) parent).notifyChildHeightChange(); // Forward to parent
        }
    }

    @Override
    public void notifyRepaint(double x0, double y0, double x1, double y1) {
        View parent = getParent();
        if (parent instanceof EditorView.Parent) {
            ((EditorView.Parent) parent).notifyRepaint(x0, y0, x1, y1); // Forward to parent
        }
    }

    @Override
    public int getViewEndOffset(int rawChildEndOffset) {
        return getStartOffset() + children.raw2Offset(rawChildEndOffset);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        EditorView.Parent parent = (EditorView.Parent) getParent();
        return (parent != null) ? parent.getFontRenderContext() : null;
    }

    @Override
    public void insertUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        // Do nothing - parent EditorBoxView is expected to handle this
    }

    @Override
    public void removeUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        // Do nothing - parent EditorBoxView is expected to handle this
    }

    public @Override
    void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        // Do nothing - parent EditorBoxView is expected to handle this
    }

    DocumentView getDocumentView() {
        return (DocumentView) getParent();
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc,
            int direction, Bias[] biasRet)
    {
        int retOffset;
        switch (direction) {
            case SwingConstants.EAST:
            case SwingConstants.WEST:
                retOffset = children.getNextVisualPositionX(this, offset, bias, alloc,
                        direction == SwingConstants.EAST, biasRet);
                break;
            case SwingConstants.NORTH:
            case SwingConstants.SOUTH:
                DocumentView docView = getDocumentView();
                if (docView != null) {
                    retOffset = children.getNextVisualPositionY(this, offset, bias, alloc,
                            direction == SwingConstants.SOUTH, biasRet,
                            HighlightsViewUtils.getMagicX(docView, this, offset, bias, alloc));
                } else {
                    retOffset = offset;
                }
                break;
            default:
                throw new IllegalArgumentException("Bad direction " + direction); // NOI18N
        }
        return retOffset;
    }
    
    void releaseTextLayouts() {
        children.lowerMeasuredEndIndex(0);
    }
    
    private void checkChildrenNotNull() {
        if (children == null) {
            throw new IllegalStateException("Null children in " + getDumpId()); // NOI18N
        }
    }

    @Override
    public String findIntegrityError() {
        String err = super.findIntegrityError();
        if (err == null && children != null) {
            int childrenLength = children.getLength();
            if (getLength() != childrenLength) {
                return "length=" + getLength() + " != childrenLength=" + childrenLength; // NOI18N
            }
        }
        if (err != null) {
            err = getDumpName() + ":" + err; // NOI18N
        }
        return err;
    }

    @Override
    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, String xyInfo, int importantChildIndex) {
        super.appendViewInfo(sb, indent, xyInfo, importantChildIndex);
        sb.append(", WxH:").append(getWidth()).append("x").append(getHeight());
        if (children != null) {
            children.appendViewInfo(this, sb);
            if (importantChildIndex != -1) {
                children.appendChildrenInfo(this, sb, indent + 8, importantChildIndex);
            }
        } else {
            sb.append(", children=null");
        }
        return sb;
    }
    
    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, "", -1).toString();
    }

    public String toStringDetail() { // Dump everything
        return appendViewInfo(new StringBuilder(200), 0, "", -2).toString();
    }

    @Override
    protected String getDumpName() {
        return "PV";
    }

}
