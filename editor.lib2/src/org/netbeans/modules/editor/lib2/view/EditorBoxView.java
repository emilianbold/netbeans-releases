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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.TabExpander;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

/**
 * Box view implementation that allows for lazy computation of individual members.
 * <br>
 * This implementation assumes that a major axis (the axis the child views are tiled along)
 * is orthogonal to the major axis of the possible parent. For example
 * a document view has y as a major axis while lines have x as major axis.
 * <br/>
 * Also a parent view is expected to implement {@link EditorView.Parent}.
 * <br/>
 * All the view operation is expected to be single-threaded.
 * <br>
 * The view can only work with document instances
 * extending {@link javax.swing.text.AbstractDocument}
 * <p>
 * The view can be constructed and work with element parameter being null.
 * <br>
 * The following methods need to be overriden when using the view in such setup:
 * <ul>
 *   <li> <code>getDocument()</code>,
 *        <code>getStartOffset()</code>,
 *        <code>getEndOffset()</code>,
 *        <code>getAttributes()</code>
 *           must be overriden to not delegate to element.
 *
 *   <li> <code>insertUpdate()</code> and <code>removeUpdate()</code>
 *       methods will not find any element changes.
 * </ul>
 *
 * @author Miloslav Metelka
 */

public abstract class EditorBoxView<V extends EditorView> extends EditorView implements EditorView.Parent {

    // -J-Dorg.netbeans.modules.editor.lib2.view.EditorBoxView.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorBoxView.class.getName());
    
    /**
     * Total preferred span along the major axis. It's a sum of all the children' spans
     * along the major axis. If the children don't exist this value presents a cache.
     */
    private double majorAxisSpan; // 24 + 8 = 32 bytes

    private float minorAxisSpan; // 32 + 4 = 36 bytes

    /**
     * Maintainer of children view infos. They may be computed lazily and dropped
     * once they are unnecessary or if they become obsolete.
     */
    EditorBoxViewChildren<V> children; // 36 + 4 = 40 bytes

    /**
     * Construct a composite box view over the given element.
     *
     * @param elem the element of the model to represent.
     * @param majorAxis the axis to tile along.  This can be
     *  either X_AXIS or Y_AXIS.
     */
    public EditorBoxView(Element elem) {
        super(elem);
    }

    /**
     * Get the axis the children are tiled along.
     * <br/>
     * For example document view has y as major axis while line view has x as major axis.
     * 
     * @return either {@link View#X_AXIS} or {@link View#Y_AXIS).
     */
    public abstract int getMajorAxis();
    
    /**
     * Set textual length of this view.
     *
     * @param length
     */
    public abstract void setLength(int length);

    @Override
    public float getPreferredSpan(int axis) {
        if (axis == getMajorAxis()) {
            return (float) getMajorAxisSpan();
        } else {
            return getMinorAxisSpan();
        }
    }

    /**
     * Get current preferred span of this view along the major axis.
     */
    public double getMajorAxisSpan() {
        return majorAxisSpan;
    }

    /**
     * Assign a new preferred span for major axis. It may be necessary to notify
     * preferenceChange to parent of this view.
     * @param majorAxisSpan
     */
    protected void setMajorAxisSpan(double majorAxisSpan) {
        this.majorAxisSpan = majorAxisSpan;
    }

    public float getMinorAxisSpan() {
        return minorAxisSpan;
    }

    /**
     * Assign a new preferred span for minor axis. It may be necessary to notify
     * preferenceChange to parent of this view.
     * @param minorAxisSpan
     */
    protected void setMinorAxisSpan(float minorAxisSpan) {
        this.minorAxisSpan = minorAxisSpan;
    }

    protected TabExpander getTabExpander() {
        View parent = getParent();
        return (parent instanceof EditorBoxView)
            ? ((EditorBoxView)parent).getTabExpander()
            : null;
    }

    /**
     * Returns the number of child views of this view.
     *
     * @return the number of views &gt;= 0
     * @see #getView(int)
     */
    @Override
    public  final int getViewCount() {
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
    
    public final V getEditorView(int index) {
        return children.get(index);
    }

    public final V getEditorViewChildrenValid(int index) {
        return children.getEditorViewChildrenValid(this, index);
    }

    final double getViewVisualOffset(int index) {
        checkChildrenNotNull();
        return children.getViewVisualOffset(this, index);
    }

    final double getViewVisualOffset(V view) {
        checkChildrenNotNull();
        return children.getViewVisualOffset(view.getRawVisualOffset());
    }

    final double getViewMajorAxisSpan(int index) {
        checkChildrenNotNull();
        return children.getViewMajorAxisSpan(this, index);
    }

    protected EditorBoxViewChildren<V> createChildren(int capacity) {
        return new EditorBoxViewChildren<V>(capacity);
    }

    protected void releaseChildren() {
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

    /**
     * Initialize null <code>EditorBoxView.children</code> of child views
     * at given range of indices.
     * <br/>
     * The view may increase the interval to be initialized.
     * <br/>
     * Implementation should re-check how large portion of the given index range
     * is really necessary to initialize if any.
     *
     * @param startIndex index >= 0.
     * @param endIndex index >= startIndex.
     */
    protected void initChildren(int startIndex, int endIndex) {
        throw new IllegalStateException("Not expected to have child views with null children");
    }

    /**
     * Replace child views.
     * <br/>
     * Replacing may lead to change in minor axis span which should be communicated
     * to parent view by caller.
     * <br/>
     * Given bounds for repainting will be updated by the method to their proper value.
     * Caller must call actual repaint on the associated component.
     *
     * @param index the starting index into the child views >= 0
     * @param length the number of existing views to replace >= 0
     * @param views the child views to insert
     * @param offsetDelta offset delta to be applied to the views that follow the last removed view (and the added ones).
     * @return visual update suitable for call to 
     */
    public VisualUpdate<V> replace(int index, int length, View[] views, int offsetDelta) {
        if (children == null) {
            assert (length == 0) : "Attempt to remove from null children length=" + length; // NOI18N
            children = createChildren(views.length);
            // Reset any previously (possibly estimated) spans (replace does just relative increase/decrease
            // so it would be fooled by it).
            setMajorAxisSpan(0d);
            setMinorAxisSpan(0f);
        }
        return children.replace(this, index, length, views, offsetDelta);
    }

    /**
     * Update spans and layout after call to replace().
     * This method should not be called directly - instead visualUpdate.updateSpansAndLayout()
     * should be called.
     * 
     * @param visualUpdate visual update obtained from replace().
     * @param alloc allocation for this view.
     */
    void updateSpansAndLayout(VisualUpdate<V> visualUpdate, Shape alloc) {
        children.updateSpansAndLayout(this, visualUpdate, alloc);
    }

    /**
     * Fix upper offsets above the index+count views and also
     * visual spans of the requested views and possibly the ones that follow (for tabbed views).
     * 
     * @param index index of first view that was modified.
     * @param count number of modified views.
     * @param alloc allocation for this view.
     */
    VisualUpdate<V> updateViews(int index, int count, Shape alloc) {
        checkChildrenNotNull();
        return children.updateViews(this, index, count, alloc);
    }

    /**
     * Child views can call this on the parent to indicate that
     * the preference has changed and should be reconsidered
     * for layout.
     *
     * @param childView the child view of this view or null to signal
     *  change in this view.
     * @param width true if the width preference has changed
     * @param height true if the height preference has changed
     * @see javax.swing.JComponent#revalidate
     */
    @Override
    public void preferenceChanged(View childView, boolean width, boolean height) {
        if (childView == null) { // notify parent about this view change
            View parent = getParent();
            if (parent != null) {
                parent.preferenceChanged(this, width, height);
            }
        } else { // Child of this view has changed
            preferenceChanged(getViewIndex(childView), width, height, true);
        }
    }

    public void preferenceChanged(int childViewIndex, boolean width, boolean height, boolean forwardToParent) {
        if (!width && !height) {
            return;
        }
        View childView = getView(childViewIndex);
        // First find the index of the child view
        boolean majorSpanChange;
        boolean minorSpanChange;
        int majorAxis = getMajorAxis();
        if (majorAxis == View.X_AXIS) {
            majorSpanChange = width;
            minorSpanChange = height;
        } else { // major axis is Y
            majorSpanChange = height;
            minorSpanChange = width;
        }
        if (majorSpanChange) {
            updateViews(childViewIndex, 1, null);
        }
        if (minorSpanChange) {
            int minorAxis = ViewUtils.getOtherAxis(majorAxis);
            float newSpan = childView.getPreferredSpan(minorAxis);
            if (newSpan > minorAxisSpan) {
                setMinorAxisSpan(newSpan);
            } else {
                minorSpanChange = false; // No change for this view
            }
        }
        if (forwardToParent && (majorSpanChange || minorSpanChange)) {
            View parent = getParent();
            if (parent != null) {
                if (majorAxis == View.X_AXIS) {
                    parent.preferenceChanged(this, majorSpanChange, minorSpanChange);
                } else {
                    parent.preferenceChanged(this, minorSpanChange, majorSpanChange);
                }
            }
        }
    }

    @Override
    public Shape getChildAllocation(int index, Shape alloc) {
        checkChildrenNotNull();
        return children.getChildAllocation(this, index, index + 1, alloc); // alloc overwritten
    }

    public Shape getChildAllocation(int startIndex, int endIndex, Shape alloc) {
        checkChildrenNotNull();
        return children.getChildAllocation(this, startIndex, endIndex, alloc);
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
        return (children != null) ? children.getViewIndex(offset) : -1;
    }

    public int getViewIndex(View childView) {
        checkChildrenNotNull();
        int index = children.getViewIndex(childView.getStartOffset());
        assert (getEditorView(index) == childView);
        return index;
    }

    public int getViewIndexFirst(int offset) {
        checkChildrenNotNull();
        return children.getViewIndexFirst(offset);
    }

    @Override
    public int getViewIndexChecked(double x, double y, Shape alloc) {
        checkChildrenNotNull();
        return children.getViewIndexAtPoint(this, x, y, alloc);
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
    public int getViewOffset(int rawOffset) {
        return getStartOffset() + children.raw2RelOffset(rawOffset);
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

    public @Override void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        // Do nothing - parent EditorBoxView is expected to handle this
    }

    private void checkChildrenNotNull() {
        if (children == null) {
            throw new IllegalStateException("Null children in " + getDumpId()); // NOI18N
        }
    }

    @Override
    protected String getDumpName() {
        return "EBV";
    }

    @Override
    public String findIntegrityError() {
        String err = super.findIntegrityError();
        if (err != null) {
            return err;
        }
        if (children != null) {
            if (children.size() == 0) {
                return "children.size()==0";
            }
            // Check children (visual offsets)
            if (children.rawOffsetUpdate()) {
                int viewCount = getViewCount();
                double lastVisualOffset = 0d;
                for (int i = 0; i < viewCount; i++) {
                    V child = getEditorView(i);
                    double childVisualOffset = getViewVisualOffset(child);
                    err = null;
                    // Do not examine child.getPreferredSpan() since it may be expensive (for HighlightsView calls getTextLayout())
                    if (childVisualOffset < 0) {
                        err = "childVisualOffset=" + childVisualOffset + " < 0"; // NOI18N
                    } else if (childVisualOffset < lastVisualOffset) {
                        err = "childVisualOffset=" + childVisualOffset + " < lastVisualOffset=" + lastVisualOffset; // NOI18N
                    }
                    if (err != null) {
                        return getDumpId() + "[" + i + "]=" + child.getDumpId() + ": " + err + '\n';
                    }
                    lastVisualOffset = childVisualOffset;
                }
            }
        } // Children == null permitted
        return err;
    }

    @Override
    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, int importantChildIndex) {
        appendViewInfoCore(sb, indent, importantChildIndex);
        if (children != null) {
            children.appendChildrenInfo(this, sb, indent + 4, importantChildIndex);
        } else {
            sb.append(", children=null");
        }
        return sb;
    }

    protected StringBuilder appendViewInfoCore(StringBuilder sb, int indent, int importantChildIndex) {
        super.appendViewInfo(sb, indent, importantChildIndex);
        sb.append(", WxH:");
        if (getMajorAxis() == View.X_AXIS) {
            sb.append(getMajorAxisSpan()).append('x').append(getMinorAxisSpan());
        } else {
            sb.append(getMinorAxisSpan()).append('x').append(getMajorAxisSpan());
        }
        return sb;
    }

    @Override
    public String toString() {
        return appendViewInfo(new StringBuilder(200), 0, -1).toString();
    }

    public String toStringDetail() { // Dump everything
        return appendViewInfo(new StringBuilder(200), 0, -2).toString();
    }

}
