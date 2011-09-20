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

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.TextUI;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.TabExpander;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.lib.editor.util.PriorityMutex;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * View representing the whole document.
 * <br/>
 * It consists of individual paragraph views that typically map to line elements
 * but e.g. code folding may cause multiple line elements correspond to one line view.
 * 
 * @author Miloslav Metelka
 */

public final class DocumentView extends EditorView implements EditorView.Parent {

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentView.class.getName());

    // True to log real source chars
    static final boolean LOG_SOURCE_TEXT = Boolean.getBoolean("org.netbeans.editor.log.source.text");

    /**
     * Text component's client property for the mutex doing synchronization
     * for view's operation. The mutex is physically the same like the one
     * for the fold hierarchy otherwise deadlocks could occur.
     */
    private static final String MUTEX_CLIENT_PROPERTY = "foldHierarchyMutex"; //NOI18N

    /**
     * Component's client property that contains swing position - start of document's area
     * to be displayed by the view.
     * Value of the property is only examined at time of view.setParent().
     */
    static final String START_POSITION_PROPERTY = "document-view-start-position";

    /**
     * Component's client property that contains swing position - end of document's area
     * to be displayed by the view.
     * Value of the property is only examined at time of view.setParent().
     */
    static final String END_POSITION_PROPERTY = "document-view-end-position";

    /**
     * Component's client property that defines whether accurate width and height should be computed
     * by the view or whether the view can estimate its width and improve the estimated
     * upon rendering of the concrete region.
     * Value of the property is only examined at time of view.setParent().
     */
    static final String ACCURATE_SPAN_PROPERTY = "document-view-accurate-span";
    
    /**
     * Component's client property (containing Integer) that defines by how many points
     * the default font size should be increased/decreased.
     */
    static final String TEXT_ZOOM_PROPERTY = "text-zoom";

    public static DocumentView get(JTextComponent component) {
        TextUI textUI = component.getUI();
        if (textUI != null) {
            View rootView = textUI.getRootView(component);
            if (rootView != null && rootView.getViewCount() > 0) {
                View view = rootView.getView(0);
                if (view instanceof DocumentView) {
                    return (DocumentView)view;
                }
            }
        }
        return null;
    }

    static DocumentView get(View view) {
        while (view != null && !(view instanceof DocumentView)) {
            view = view.getParent();
        }
        return (DocumentView) view;
    }

    static {
        EditorViewFactory.registerFactory(new HighlightsViewFactory.HighlightsFactory());
    }
    
    DocumentViewOp op;

    private PriorityMutex pMutex;
    
    DocumentViewChildren children;

    private JTextComponent textComponent;

    /**
     * Non-null position in case the document view does not cover the whole document
     * but starts at certain position instead.
     * It can be influenced by textComponent.putClientProperty(START_POSITION_PROPERTY).
     * The position may be corrected (moved back) explicitly when inserting upon it.
     */
    private Position startPos;

    /**
     * Non-null end bound in case the document view does not cover the whole document
     * but ends at certain position instead.
     * It can be influenced by textComponent.putClientProperty(END_POSITION_PROPERTY).
     * The view factories can however refuse to end the last created view right at this boundary
     * so the real ending offset of the currently present views (docView.getEndOffset()) may be higher.
     */
    private Position endPos;
    
    /**
     * Current allocation of document view.
     */
    private Rectangle2D.Float allocation = new Rectangle2D.Float();

    private final TabExpander tabExpander;
    
    /**
     * Change that occurred in the view hierarchy.
     */
    private ViewHierarchyChange change;


    public DocumentView(Element elem) {
        super(elem);
        assert (elem != null) : "Expecting non-null element"; // NOI18N
        this.op = new DocumentViewOp(this);
        this.tabExpander = new EditorTabExpander(this);
    }

    /**
     * Run transaction over locked view hierarchy.
     * The document must be read-locked prior calling this method.
     * 
     * @param r non-null runnable to be executed over locked view hierarchy.
     */
    public void runTransaction(Runnable r) {
        if (lock()) {
            try {
                r.run();
            } finally {
                unlock();
            }
        } else { // If no mutex present run without mutex (not running at all would be more serious)
            r.run();
        }
    }
    
    public void runReadLockTransaction(final Runnable r) {
        getDocument().render(new Runnable() {
            @Override
            public void run() {
                runTransaction(r);
            }
        });
    }
    
    @Override
    public float getPreferredSpan(int axis) {
        // Since this may be called e.g. from BasicTextUI.getPreferredSize()
        // this method needs to acquire mutex
        if (lock()) {
            try {
                checkDocumentLockedIfLogging(); // Should only be called with read-locked document
                op.checkViewsInited();
                // Ensure the width and height are updated before unlock() gets called (which is too late)
                op.checkRealSpanChange();
                if (!op.isChildrenValid()) {
                    return 1f; // Return 1f until parent and etc. gets initialized
                }
                float span;
                if (axis == View.X_AXIS) {
                    span = allocation.width;
                } else { // Y_AXIS
                    span = allocation.height;
                    // Add extra span when component in viewport
                    Component parent;
                    if (textComponent != null && ((parent = textComponent.getParent()) instanceof JViewport)) {
                        JViewport viewport = (JViewport) parent;
                        int viewportHeight = viewport.getExtentSize().height;
                        span += viewportHeight / 3;
                    }
                }
                return span;
            } finally {
                unlock();
            }
        } else {
            return 1f;
        }
    }

    boolean lock() {
        if (pMutex != null) {
            pMutex.lock();
            return true;
        }
        return false;
    }
    
    void unlock() {
        try {
            op.unlockCheck();
            checkFireEvent();
        } finally {
            pMutex.unlock();
        }
    }

    @Override
    public Document getDocument() {
        return getElement().getDocument();
    }

    @Override
    public int getStartOffset() {
        return (startPos == null) ? super.getStartOffset() : startPos.getOffset();
    }
    
    @Override
    public int getEndOffset() {
        if (endPos == null) {
            return super.getEndOffset();
        } else { // Custom ending position
            // Get end offset of the last view since that may differ from endPos
            int viewCount = getViewCount();
            return (viewCount > 0)
                    ? getView(viewCount - 1).getEndOffset()
                    : getStartOffset();
        }
    }

    @Override
    public int getViewCount() {
        return (children != null) ? children.size() : 0;
    }

    @Override
    public View getView(int index) {
        checkDocumentLockedIfLogging();
        checkMutexAcquiredIfLogging();
        return (children != null) ? children.get(index) : null;
    }

    public ParagraphView getParagraphView(int index) {
        return children.get(index);
    }

    @Override
    public Shape getChildAllocation(int index, Shape alloc) {
        return children.getChildAllocation(this, index, alloc);
    }

    @Override
    public int getViewIndex(int offset, Position.Bias b) {
        if (b == Position.Bias.Backward) {
            offset--;
        }
        return getViewIndex(offset);
    }

    public int getViewIndex(int offset) {
        return children.viewIndexFirstByStartOffset(offset, 0);
    }
    
    public double getY(int pViewIndex) {
        return children.startVisualOffset(pViewIndex);
    }

    @Override
    public int getViewEndOffset(int rawChildEndOffset) {
        throw new IllegalStateException("Raw end offsets storage not maintained for DocumentView."); // NOI18N
    }

    @Override
    public void replace(int index, int length, View[] views) {
        replaceViews(index, length, views);
    }

    ViewHierarchyChange validChange() {
        if (change == null) {
            change = new ViewHierarchyChange();
        }
        return change;
    }

    void checkFireEvent() {
        if (change != null) {
            op.viewHierarchyImpl().fireChange(change);
            change = null;
        }
    }

    public TabExpander getTabExpander() {
        return tabExpander;
    }
    
    double[] replaceViews(int index, int length, View[] views) {
        if (children == null) {
            children = new DocumentViewChildren(views.length);
        }
        return children.replace(this, index, length, views);
    }
    
    boolean hasExtraStartBound() {
        return (startPos != null);
    }
    
    Position getStartPosition() {
        return startPos;
    }
    
    void setStartPosition(Position startPosition) {
        this.startPos = startPosition;
    }

    boolean hasExtraEndBound() {
        return (endPos != null);
    }
    
    public int getEndBoundOffset() {
        return (endPos != null) ? endPos.getOffset() : getDocument().getLength() + 1;
    }

    boolean hasExtraBounds() {
        return hasExtraStartBound() || hasExtraEndBound();
    }

    @Override
    public int getRawEndOffset() {
        return -1;
    }

    @Override
    public void setRawEndOffset(int rawOffset) {
        throw new IllegalStateException("Unexpected"); // NOI18N
    }

    @Override
    public void setSize(float width, float height) {
        // Currently the view is not designed to possibly shrink/extend its size according to the given size.
        // Do not update this.width and this.height here (they are updated in preferenceChanged()
    }

    /**
     * Get current allocation of the document view by using size from last call
     * to {@link #setSize(float, float)}.
     * <br/>
     * Returned instance may not be mutated (use getAllocationMutable()).
     *
     * @return current allocation of document view.
     */
    public Rectangle2D getAllocation() {
        return allocation;
    }
    
    public Rectangle2D.Double getAllocationMutable() {
        return new Rectangle2D.Double(0d, 0d, allocation.getWidth(), allocation.getHeight());
    }
    
    /**
     * Get paint highlights for the given view.
     *
     * @param view view for which the highlights are obtained.
     * @param shift shift inside the view where the returned highlights should start.
     * @return highlights sequence containing the merged highlights of the view and painting highlights.
     */
    @Override
    public HighlightsSequence getPaintHighlights(EditorView view, int shift) {
        return children.getPaintHighlights(view, shift);
    }

    @Override
    public void notifyChildWidthChange() {
        op.notifyChildWidthChange();
    }
    
    @Override
    public void notifyChildHeightChange() {
        op.notifyChildHeightChange();
    }

    @Override
    public void preferenceChanged(View childView, boolean widthChange, boolean heightChange) {
        if (childView == null) { // This docView
            if (widthChange) {
                op.notifyWidthChange();
            }
            if (heightChange) {
                op.notifyHeightChange();
            }
        } else { // Individual view
            if (children != null) {
                int index = getViewIndex(childView.getStartOffset());
                if (widthChange) {
                    notifyChildWidthChange();
                }
                if (heightChange) {
                    notifyChildHeightChange();
                }
                children.checkChildrenSpanChange(this, index);
                Shape childAlloc = getChildAllocation(index, getAllocation());
                op.notifyRepaint(op.extendToVisibleWidth(ViewUtils.shape2Bounds(childAlloc)));
            }
        }
    }
    
    void superPreferenceChanged(boolean widthChange, boolean heightChange) {
        super.preferenceChanged(this, widthChange, heightChange);
    }

    boolean assignChildrenWidth() {
        float newWidth = children.width();
        if (newWidth != allocation.width) {
            allocation.width = newWidth;
            return true;
        }
        return false;
    }

    boolean assignChildrenHeight() {
        float newHeight = children.height();
        if (newHeight != allocation.height) {
            allocation.height = newHeight;
            return true;
        }
        return false;
    }

    void recomputeChildrenWidths() {
        if (children != null) {
            if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
                ViewHierarchyImpl.SPAN_LOG.fine("Component width differs => children.recomputeChildrenWidths()\n"); // NOI18N
            }
            children.recomputeChildrenWidths();
        }
    }

    @Override
    public void notifyRepaint(double x0, double y0, double x1, double y1) {
        op.notifyRepaint(x0, y0, x1, y1);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return op.getFontRenderContext();
    }

    void offsetRepaint(int startOffset, int endOffset) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging(); // Should only be called with read-locked document
                if (ViewHierarchyImpl.REPAINT_LOG.isLoggable(Level.FINE)) {
                    ViewHierarchyImpl.REPAINT_LOG.fine("OFFSET-REPAINT: <" + startOffset + "," + endOffset + ">\n");
                }
                if (op.isActive() && startOffset < endOffset && getViewCount() > 0) {
                    Rectangle2D repaintRect;
                    Rectangle2D.Double docViewRect = getAllocationMutable();
                    int pViewIndex = getViewIndex(startOffset);
                    ParagraphView pView = getParagraphView(pViewIndex);
                    if (endOffset <= pView.getEndOffset()) {
                        Shape pViewAlloc = getChildAllocation(pViewIndex, docViewRect);
                        if (pView.children != null) { // Do local repaint
                            Shape s = pView.modelToViewChecked(startOffset, Bias.Forward,
                                    endOffset, Bias.Forward, pViewAlloc);
                            repaintRect = ViewUtils.shapeAsRect(s);
                            children.checkChildrenSpanChange(this, pViewIndex);
                            
                        } else { // Repaint single paragraph
                            repaintRect = op.extendToVisibleWidth(ViewUtils.shape2Bounds(pViewAlloc));
                        }
                    } else { // Spans paragraphs
                        docViewRect.y = getY(pViewIndex);
                        int endIndex = getViewIndex(endOffset) + 1;
                        docViewRect.height = getY(endIndex) - docViewRect.y;
                        op.extendToVisibleWidth(docViewRect);
                        repaintRect = docViewRect;
                    }
                    if (repaintRect != null) {
                        op.notifyRepaint(repaintRect);
                    }
                }
            } finally {
                unlock();
            }
        }
        
    }
    
    @Override
    public void setParent(View parent) {
        if (parent != null) {
            Container container = parent.getContainer();
            assert (container != null) : "Container is null"; // NOI18N
            assert (container instanceof JTextComponent) : "Container not JTextComponent"; // NOI18N
            JTextComponent tc = (JTextComponent) container;
            pMutex = (PriorityMutex) tc.getClientProperty(MUTEX_CLIENT_PROPERTY);
            if (pMutex == null) {
                pMutex = new PriorityMutex();
                tc.putClientProperty(MUTEX_CLIENT_PROPERTY, pMutex);
            }
            if (lock()) {
                try {
                    super.setParent(parent);
                    textComponent = tc;
                    op.parentViewSet();
                    updateStartEndPos();
                } finally {
                    unlock();
                }
            }

        } else { // Setting null parent
            // Set the textComponent to null under mutex
            // so that children suddenly don't see a null textComponent
            runReadLockTransaction(new Runnable() {
                @Override
                public void run() {
                    if (textComponent != null) {
                        op.parentCleared();
                        textComponent = null; // View services stop working and propagating to children
                    }
                    DocumentView.super.setParent(null);
                }
            });
        }
    }
    
    void updateStartEndPos() {
        startPos = (Position) textComponent.getClientProperty(START_POSITION_PROPERTY);
        endPos = (Position) textComponent.getClientProperty(END_POSITION_PROPERTY);
        int startOffset = 0;
        if (startPos != null && (startOffset = startPos.getOffset()) == 0) {
            startPos = null;
        }
        if (endPos != null && (endPos.getOffset() == getDocument().getEndPosition().getOffset() ||
                endPos.getOffset() < startOffset)) // For invalid endPos value make endPos=null
        {
            endPos = null;
        }
    }
    
    @Override
    public String getToolTipTextChecked(double x, double y, Shape alloc) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    return children.getToolTipTextChecked(this, x, y, alloc);
                }
            } finally {
                unlock();
            }
        }
        return null;
    }

    @Override
    public JComponent getToolTip(double x, double y, Shape alloc) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    return children.getToolTip(this, x, y, alloc);
                }
            } finally {
                unlock();
            }
        }
        return null;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    // Use rendering hints (antialiasing etc.)
                    if (g != null && op.renderingHints != null) {
                        g.setRenderingHints(op.renderingHints);
                    }
                    children.paint(this, g, alloc, clipBounds);
                }
            } finally {
                unlock();
            }
        }
    }
    
    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Bias bias) {
        if (lock()) {
            try {
                return modelToViewUnlocked(offset, alloc, bias);
            } finally {
                unlock();
            }
        }
        return null;
    }

    public Shape modelToViewUnlocked(int offset, Shape alloc, Bias bias) {
        Rectangle2D.Double rect = ViewUtils.shape2Bounds(alloc);
        Shape retShape = null;
        checkDocumentLockedIfLogging();
        op.checkViewsInited();
        if (op.isActive()) {
            retShape = children.modelToViewChecked(this, offset, alloc, bias);
        } else if (children != null) {
            // Not active but attempt to find at least a reasonable y
            // The existing line views may not be updated for a longer time
            // but the binary search should find something and end in finite time.
            int index = getViewIndex(offset); // Must work without children inited
            if (index >= 0) {
                rect.y = getY(index); // Must work without children inited
                // Let the height to possibly be set to default line height later
            }
        }
        if (retShape == null) {
            // Attempt to just return height of line since otherwise e.g. caret
            // would have height of the whole doc which is undesirable.
            float defaultRowHeight = op.getDefaultRowHeight();
            if (defaultRowHeight > 0f) {
                rect.height = defaultRowHeight;
            }
            retShape = rect;
        }
        if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.OP_LOG, "modelToView(" + offset + // NOI18N
                    ")=" + retShape + "\n"); // NOI18N
        }
        return retShape;
    }

    public double modelToY(int offset) {
        double retY = 0d;
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    retY = modelToYUnlocked(offset);
                }
            } finally {
                unlock();
            }
        }
        if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.OP_LOG, "modelToY(" + offset + ")=" + retY + "\n"); // NOI18N
        }
        return retY;
    }
    
    public double modelToYUnlocked(int offset) {
        int index = getViewIndex(offset);
        return getY(index);
    }
    
    public double[] modelToYUnlocked(int[] offsets) {
        double[] retYs = new double[offsets.length];
        if (offsets.length > 0) {
            // Can in fact assume lastOffset == 0 corresponds to retY == 0d even if the view hierarchy
            // covers only portion of document since offset == 0 should be covered and it falls into first pView.
            int lastOffset = 0;
            int lastIndex = 0;
            double lastY = 0d;
            for (int i = 0; i < offsets.length; i++) {
                int offset = offsets[i];
                double y;
                if (offset == lastOffset) {
                    y = lastY;
                } else {
                    int startIndex = (offset > lastOffset) ? lastIndex : 0;
                    int index = children.viewIndexFirstByStartOffset(offset, startIndex);
                    y = getY(index);
                }
                retYs[i] = y;
            }
        }
        return retYs;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        if (lock()) {
            try {
                return viewToModelUnlocked(x, y, alloc, biasReturn);
            } finally {
                unlock();
            }
        }
        return 0;
    }
    
    public int viewToModelUnlocked(double x, double y, Shape alloc, Bias[] biasReturn) {
        int retOffset = 0;
        checkDocumentLockedIfLogging();
        op.checkViewsInited();
        if (op.isActive()) {
            retOffset = children.viewToModelChecked(this, x, y, alloc, biasReturn);
        }
        if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.OP_LOG, "viewToModel: [x,y]=" + x + "," + y + // NOI18N
                    " => " + retOffset + "\n"); // NOI18N
        }
        return retOffset;
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc,
            int direction, Bias[] biasRet)
    {
        int retOffset = offset;
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                op.checkViewsInited();
                if (op.isActive()) {
                    switch (direction) {
                        case SwingConstants.EAST:
                            if (offset == -1) {
                                retOffset = getEndOffset() - 1;
                                break;
                            }
                            // Pass to SwingConstants.WEST
                        case SwingConstants.WEST:
                            if (offset == -1) {
                                retOffset = getStartOffset();
                            } else {
                                retOffset = children.getNextVisualPositionX(this, offset, bias, alloc,
                                        direction == SwingConstants.EAST, biasRet);
                            }
                            break;
                        case SwingConstants.NORTH:
                            if (offset == -1) {
                                retOffset = getEndOffset() - 1;
                                break;
                            }
                            // Pass to SwingConstants.SOUTH
                        case SwingConstants.SOUTH:
                            if (offset == -1) {
                                retOffset = getStartOffset();
                            } else {
                                retOffset = children.getNextVisualPositionY(this, offset, bias, alloc,
                                        direction == SwingConstants.SOUTH, biasRet);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Bad direction " + direction); // NOI18N
                    }
                }
            } finally {
                unlock();
            }
        }
        if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.OP_LOG, "nextVisualPosition(" + offset + "," + // NOI18N
                    ViewUtils.toStringDirection(direction) + ")=" + retOffset + "\n"); // NOI18N
        }
        return retOffset;
    }
    
    /**
     * It should be called with +1 once it's detected that there's a lengthy atomic edit
     * in progress and with -1 when such edit gets finished.
     * @param delta +1 or -1 when entering/leaving lengthy atomic edit.
     */
    public void updateLengthyAtomicEdit(int delta) {
        op.updateLengthyAtomicEdit(delta);
    }

    @Override
    public void insertUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        // Do nothing here - see ViewUpdates constructor
    }

    @Override
    public void removeUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        // Do nothing here - see ViewUpdates constructor
    }

    @Override
    public void changedUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        // Do nothing here - see ViewUpdates constructor
    }

    JTextComponent getTextComponent() {
        return textComponent;
    }

    void checkDocumentLockedIfLogging() {
        if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINE)) {
            checkDocumentLocked();
        }
    }
    
    void checkDocumentLocked() {
        if (!DocumentUtilities.isReadLocked(getDocument())) {
            ViewHierarchyImpl.CHECK_LOG.log(Level.INFO, "Document not locked", new Exception("Document not locked")); // NOI18N
        }
    }
    
    void checkMutexAcquiredIfLogging() {
        if (ViewHierarchyImpl.CHECK_LOG.isLoggable(Level.FINE)) {
            checkLocked();
        }
    }

    void checkLocked() {
        PriorityMutex mutex = pMutex;
        if (mutex != null) {
            Thread mutexThread = mutex.getLockThread();
            if (mutexThread != Thread.currentThread()) {
                String msg = (mutexThread == null)
                        ? "Mutex not acquired" // NOI18N
                        : "Mutex already acquired for different thread: " + mutexThread; // NOI18N
                ViewHierarchyImpl.CHECK_LOG.log(Level.INFO, msg + " for textComponent=" + textComponent, new Exception()); // NOI18N
            }
        }
    }

    boolean isLocked() {
        PriorityMutex mutex = pMutex;
        return (mutex != null && mutex.getLockThread() == Thread.currentThread());
    }

    @Override
    protected String getDumpName() {
        return "DV";
    }

    @Override
    public String findIntegrityError() {
        String err = super.findIntegrityError();
        if (err != null) {
            return err;
        }
        int startOffset = getStartOffset();
        int endOffset = getEndOffset();
        int viewCount = getViewCount();
        if (viewCount > 0) {
            ParagraphView firstView = getParagraphView(0);
            if (firstView.getStartOffset() != startOffset) {
                return "firstView.getStartOffset()=" + firstView.getStartOffset() + // NOI18N
                        " != startOffset=" + startOffset; // NOI18N
            }
            ParagraphView lastView = getParagraphView(viewCount - 1);
            if (lastView.getEndOffset() != endOffset) {
                return "lastView.endOffset=" + lastView.getEndOffset() + " != endOffset=" + endOffset; // NOI18N
            }
            if (endOffset < getEndBoundOffset()) {
                return "endOffset=" + endOffset + " < endBoundOffset=" + getEndBoundOffset(); // NOI18N
            }
        }
        return null;
    }

    @Override
    public String findTreeIntegrityError() {
        final String[] ret = new String[1];
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                ret[0] = DocumentView.super.findTreeIntegrityError();
            }
        });
        return ret[0];
    }

    @Override
    protected StringBuilder appendViewInfo(StringBuilder sb, int indent, int importantChildIndex) {
        DocumentView.super.appendViewInfo(sb, indent, importantChildIndex);
        sb.append("; Bounds:<");
        sb.append(hasExtraStartBound() ? startPos.getOffset() : "DOC-START");
        sb.append(","); // NOI18N
        sb.append(hasExtraEndBound() ? endPos.getOffset() : "DOC-END");
        sb.append(">, ");
        op.appendInfo(sb);
        if (LOG_SOURCE_TEXT) {
            Document doc = getDocument();
            sb.append("\nDoc: ").append(ViewUtils.toString(doc)); // NOI18N
        }
        if (children != null) {
            if (importantChildIndex != -1) {
                children.appendChildrenInfo(DocumentView.this, sb, indent, importantChildIndex);
            }
        }
        return sb;
    }

    @Override
    public String toString() {
        final String[] s = new String[1];
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                s[0] = toStringUnlocked();
            }
        });
        return s[0];
    }
    
    public String toStringUnlocked() {
        return appendViewInfo(new StringBuilder(200), 0, -1).toString();
    }

    public String toStringDetail() {
        final String[] s = new String[1];
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                s[0] = toStringDetailUnlocked();
            }
        });
        return s[0];
    }
    
    public String toStringDetailUnlocked() { // Dump everything
        return appendViewInfo(new StringBuilder(200), 0, -2).toString();
    }

}
