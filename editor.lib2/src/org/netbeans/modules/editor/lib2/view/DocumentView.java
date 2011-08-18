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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.TextUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabExpander;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.PriorityMutex;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * View representing the whole document.
 * <br/>
 * It consists of individual paragraph views that typically map to line elements
 * but e.g. code folding may cause multiple line elements correspond to one line view.
 * 
 * @author Miloslav Metelka
 */

@SuppressWarnings("ClassWithMultipleLoggers") //NOI18N
public final class DocumentView extends EditorView
        implements EditorView.Parent, PropertyChangeListener, ChangeListener, MouseWheelListener
{

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentView.class.getName());

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView-QueryInModification.level=FINE
    private static final Logger LOG_QUERY_IN_MODIFICATION = Logger.getLogger(DocumentView.class.getName() +
            "-QueryInModification"); // NOI18N

    // True to log real source chars
    static final boolean LOG_SOURCE_TEXT = Boolean.getBoolean("org.netbeans.editor.log.source.text");

    static final char PRINTING_SPACE = '\u00B7';
    static final char PRINTING_TAB = '\u00BB'; // \u21FE
    static final char PRINTING_NEWLINE = '\u00B6';
    static final char LINE_CONTINUATION = '\u21A9';
    static final char LINE_CONTINUATION_ALTERNATE = '\u2190';
    
    private static final int CHILD_WIDTH_CHANGE     = 1;
    private static final int CHILD_HEIGHT_CHANGE    = 2;
    private static final int WIDTH_CHANGE           = 4;
    private static final int HEIGHT_CHANGE          = 8;

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
    private static final String ACCURATE_SPAN_PROPERTY = "document-view-accurate-span";
    
    /**
     * Component's client property (containing Integer) that defines by how many points
     * the default font size should be increased/decreased.
     */
    private static final String TEXT_ZOOM_PROPERTY = "text-zoom";

    static enum LineWrapType {
        NONE("none"), //NOI18N
        CHARACTER_BOUND("chars"), //NOI18N
        WORD_BOUND("words"); //NOI18N

        private final String settingValue;
        private LineWrapType(String settingValue) {
            this.settingValue = settingValue;
        }

        public static LineWrapType fromSettingValue(String settingValue) {
            if (settingValue != null) {
                for(LineWrapType lwt : LineWrapType.values()) {
                    if (lwt.settingValue.equals(settingValue)) {
                        return lwt;
                    }
                }
            }
            return null;
        }
    };

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

    private PriorityMutex pMutex;
    
    DocumentViewChildren children;

    /**
     * Whether the "children" is currently reflecting the document state.
     * <br/>
     * The children may hold a semi-valid view hierarchy which may still be partly used
     * to resolve queries in some cases.
     */
    private boolean childrenValid;

    private JTextComponent textComponent;

    /**
     * Maintenance of view updates.
     * If this is a preview-only view e.g. for a collapsed fold preview
     * when located over collapsed fold's tooltip.
     */
    private ViewUpdates viewUpdates;
    
    private TextLayoutCache textLayoutCache;

    private boolean incomingModification;
    
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
     * Preferred width of document view.
     */
    private float width;

    /**
     * Preferred height of document view.
     */
    private float height;

    private boolean accurateSpan;

    /**
     * Visible rectangle of the viewport or a text component if there is no viewport.
     */
    private Rectangle visibleRect = new Rectangle();

    private float availableWidth;
    
    private boolean availableWidthValid;
    
    private float renderWrapWidth;
    
    private int statusBits;
    
    private double repaintX0;
    
    private double repaintY0;
    
    private double repaintX1;
    
    private double repaintY1;
    
    /**
     * Cached font render context (in order not to call getContainer().getGraphics() etc. each time).
     * It appears the FontRenderContext class is not extended (inspected SunGraphics2D)
     * so it should be safe and work fine.
     */
    private FontRenderContext fontRenderContext;
    
    private AttributeSet defaultColoring;
    
    private int textLimitLine;
    
    private Font defaultFont;

    /**
     * Whether the textComponent's font was explicitly set from outside
     * or whether it was only updated internally by settings.
     */
    private boolean customFont;

    /**
     * Default line height computed as height of the defaultFont.
     */
    private float defaultLineHeight;

    private float defaultAscent;

    private float defaultDescent;
    
    private float defaultLeading;

    private float defaultCharWidth;

    private Color defaultForeground;

    private boolean customForeground;

    private Color defaultBackground;

    private boolean customBackground;

    private Color textLimitLineColor;

    private int textLimitLineX;
    
    private boolean nonPrintableCharactersVisible;
    
    private LineWrapType lineWrapType;

    private TextLayout newlineTextLayout;

    private TextLayout tabTextLayout;

    private TextLayout singleCharTabTextLayout;

    private TextLayout lineContinuationTextLayout;

    private TabExpander tabExpander;

    private LookupListener lookupListener;

    private JViewport listeningOnViewport;

    private Preferences prefs;

    private PreferenceChangeListener prefsListener;

    private Map<?, ?> renderingHints;
    
    private int lengthyAtomicEdit; // Long atomic edit being performed

    ViewHierarchy viewHierarchy; // Assigned upon setParent()
    
    private Map<Font,FontInfo> fontInfos = new HashMap<Font, FontInfo>(4);
    
    private Font fallbackFont;
    
    private MouseWheelListener origMouseWheelListener;
    
    public DocumentView(Element elem) {
        super(elem);
        assert (elem != null) : "Expecting non-null element"; // NOI18N
        this.tabExpander = new EditorTabExpander(this);
    }

    public ViewHierarchy viewHierarchy() { // not synced (multiple instances should not harm)
        return viewHierarchy;
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
    
    /**
     * Rebuild views if there are any pending highlight factory changes reported.
     * Method ensures proper locking of document and view hierarchy.
     */
    public void syncViewsRebuild() {
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                if (viewUpdates != null) {
                    viewUpdates.syncedViewsRebuild();
                }
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
                checkViewsInited();
                // Ensure the width and height are updated before unlock() gets called (which is too late)
                checkRealSpanChange();
                if (!childrenValid) {
                    return 1f; // Return 1f until parent and etc. gets initialized
                }
                float span;
                if (axis == View.X_AXIS) {
                    span = width;
                } else { // Y_AXIS
                    span = height;
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
            checkRealSpanChange(); // Clears widthChange and heightChange
            checkRepaint();
            if (isAnyWidthHeightChange()) { // There should be no width/height changes
                // Do not throw error (only log) since unlock() call may be in another nested 'finally' section
                // which would lose original stacktrace.
                LOG.log(Level.INFO, "DocumentView invalid state upon unlock: " + toStringUnlocked(), new Exception()); // NOI18N
            }
//            assert (!isAnyWidthHeightChange()) : toStringUnlocked();
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
        if (LOG.isLoggable(Level.FINE)) {
            checkDocumentLockedIfLogging();
            checkMutexAcquiredIfLogging();
        }
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
        return children.viewIndexFirstByStartOffset(offset);
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
    
    double replaceViews(int index, int length, View[] views) {
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

    public TabExpander getTabExpander() {
        return tabExpander;
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
     * Returned instance may be mutated.
     *
     * @return current allocation of document view.
     */
    public Rectangle2D.Double getAllocation() {
        return new Rectangle2D.Double(0, 0, width, height);
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
        setStatusBits(CHILD_WIDTH_CHANGE);
        if (ViewHierarchy.SPAN_LOG.isLoggable(Level.FINEST)) { // Only when on finest level
            ViewUtils.log(ViewHierarchy.SPAN_LOG, "CHILD-WIDTH changed\n"); // NOI18N
        }
    }
    
    boolean isChildWidthChange() {
        return isAnyStatusBit(CHILD_WIDTH_CHANGE);
    }
    
    void resetChildWidthChange() {
        clearStatusBits(CHILD_WIDTH_CHANGE);
    }

    @Override
    public void notifyChildHeightChange() {
        setStatusBits(CHILD_HEIGHT_CHANGE);
        if (ViewHierarchy.SPAN_LOG.isLoggable(Level.FINEST)) { // Only when on finest level
            ViewUtils.log(ViewHierarchy.SPAN_LOG, "CHILD-HEIGHT changed\n"); // NOI18N
        }
    }

    boolean isChildHeightChange() {
        return isAnyStatusBit(CHILD_HEIGHT_CHANGE);
    }
    
    void resetChildHeightChange() {
        clearStatusBits(CHILD_HEIGHT_CHANGE);
    }
    
    void notifyWidthChange() {
        setStatusBits(WIDTH_CHANGE);
        if (ViewHierarchy.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchy.SPAN_LOG, "DV-WIDTH changed\n"); // NOI18N
        }
    }
    
    boolean isWidthChange() {
        return isAnyStatusBit(WIDTH_CHANGE);
    }
    
    private void resetWidthChange() {
        clearStatusBits(WIDTH_CHANGE);
    }
    
    /**
     * If width-change was notified then check if real change occurred comparing
     * current width and width of children.
     * @return true if real change occurred.
     */
    private boolean checkRealWidthChange() {
        if (isWidthChange()) {
            resetWidthChange();
            float newWidth = children.width();
            if (newWidth != this.width) {
                this.width = newWidth;
                return true;
            }
        }
        return false;
    }
    
    void notifyHeightChange() {
        setStatusBits(HEIGHT_CHANGE);
        if (ViewHierarchy.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchy.SPAN_LOG, "DV-HEIGHT changed\n"); // NOI18N
        }
    }
    
    boolean isHeightChange() {
        return isAnyStatusBit(HEIGHT_CHANGE);
    }
    
    private void resetHeightChange() {
        clearStatusBits(HEIGHT_CHANGE);
    }
    
    /**
     * If height-change was notified then check if real change occurred comparing
     * current height and height of children.
     * @return true if real change occurred.
     */
    private boolean checkRealHeightChange() {
        if (isHeightChange()) {
            resetHeightChange();
            float newHeight = children.height();
            if (newHeight != this.height) {
                this.height = newHeight;
                return true;
            }
        }
        return false;
    }
    
    boolean isChildWidthHeightChange() {
        return isAnyStatusBit(CHILD_WIDTH_CHANGE | CHILD_HEIGHT_CHANGE );
    }
    
    boolean isAnyWidthHeightChange() {
        return isAnyStatusBit(WIDTH_CHANGE | HEIGHT_CHANGE | CHILD_WIDTH_CHANGE | CHILD_HEIGHT_CHANGE );
    }
    
    private void setStatusBits(int bits) {
        statusBits |= bits;
    }
    
    private void clearStatusBits(int bits) {
        statusBits &= ~bits;
    }
    
    private boolean isAnyStatusBit(int bits) {
        return (statusBits & bits) != 0;
    }
    
    private void checkRealSpanChange() {
        boolean widthChange = checkRealWidthChange();
        boolean heightChange = checkRealHeightChange();
        if (widthChange || heightChange) {
            if (ViewHierarchy.SPAN_LOG.isLoggable(Level.FINE)) {
                String msg = "TC-preferenceChanged(" + // NOI18N
                        (widthChange ? "W" : "-") + "x" + (heightChange ? "H" : "-") + ")\n"; // NOI18N
                ViewUtils.log(ViewHierarchy.SPAN_LOG, msg);
            }

            // RootView.preferenceChanged() calls textComponent.revalidate (reposts to EDT if not called in it already)
            super.preferenceChanged(this, widthChange, heightChange);
        }
    }

    @Override
    public void preferenceChanged(View childView, boolean widthChange, boolean heightChange) {
        if (childView == null) { // This docView
            if (widthChange) {
                notifyWidthChange();
            }
            if (heightChange) {
                notifyHeightChange();
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
                notifyRepaint(extendToVisibleWidth(ViewUtils.shape2Bounds(childAlloc)));
            }
        }
    }

    @Override
    public void notifyRepaint(double x0, double y0, double x1, double y1) {
        if (repaintX1 == 0d) {
            repaintX0 = x0;
            repaintY0 = y0;
            repaintX1 = x1;
            repaintY1 = y1;
        } else { // Merge regions
            repaintX0 = Math.min(repaintX0, x0);
            repaintX1 = Math.max(repaintX1, x1);
            repaintY0 = Math.min(repaintY0, y0);
            repaintY1 = Math.max(repaintY1, y1);
        }

        if (ViewHierarchy.REPAINT_LOG.isLoggable(Level.FINE)) {
            String msg = "NOTIFY-REPAINT x0,y0,x1,y1: [" + x0 + "," + y0 + "," + x1 + "," + y1 + "] => [" // NOI18N
                     + repaintX0 + "," + repaintY0 + "," + repaintX1 + "," + repaintY1 + "]\n"; // NOI18N
            if (ViewHierarchy.REPAINT_LOG.isLoggable(Level.FINER)) {
                ViewHierarchy.REPAINT_LOG.log(Level.INFO, "Stack of " + msg, new Exception());
            } else {
                ViewHierarchy.REPAINT_LOG.fine(msg);
            }
        }
    }
    
    void notifyRepaint(Rectangle2D repaintRect) {
        notifyRepaint(repaintRect.getX(), repaintRect.getY(), repaintRect.getMaxX(), repaintRect.getMaxY());
    }

    final void checkRepaint() {
        if (repaintX1 != 0d) {
            final int x0 = (int) repaintX0;
            final int x1 = (int) Math.ceil(repaintX1);
            final int y0 = (int) repaintY0;
            final int y1 = (int) Math.ceil(repaintY1);
            resetRepaintRegion();
            // Possibly post repaint into EDT since there was a deadlock in JDK related to this.
            ViewUtils.runInEQ(new Runnable() {
                @Override
                public void run() {
                    JTextComponent tc = textComponent;
                    if (tc != null) {
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.finer("REPAINT x0,y0,x1,y1: [" + x0 + "," + y0 + "," + x1 + "," + y1 + "]\n"); // NOI18N
                        }
                        tc.repaint(x0, y0, x1 - x0, y1 - y0);
                    }
                }
            });
        }
    }
    
    private void resetRepaintRegion() {
        repaintX1 = 0d; // Make repaint region empty
    }
    
    void offsetRepaint(int startOffset, int endOffset) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging(); // Should only be called with read-locked document
                if (ViewHierarchy.REPAINT_LOG.isLoggable(Level.FINE)) {
                    ViewHierarchy.REPAINT_LOG.fine("OFFSET-REPAINT: <" + startOffset + "," + endOffset + ">\n");
                }
                if (isActive() && startOffset < endOffset && getViewCount() > 0) {
                    Rectangle2D repaintRect;
                    Rectangle2D.Double docViewRect = getAllocation();
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
                            repaintRect = extendToVisibleWidth(ViewUtils.shape2Bounds(pViewAlloc));
                        }
                    } else { // Spans paragraphs
                        docViewRect.y = getY(pViewIndex);
                        int endIndex = getViewIndex(endOffset) + 1;
                        docViewRect.height = getY(endIndex) - docViewRect.y;
                        extendToVisibleWidth(docViewRect);
                        repaintRect = docViewRect;
                    }
                    if (repaintRect != null) {
                        notifyRepaint(repaintRect);
                    }
                }
            } finally {
                unlock();
            }
        }
        
    }
    
    Rectangle2D.Double extendToVisibleWidth(Rectangle2D.Double r) {
        r.width = getVisibleRect().getMaxX();
        return r;
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
                    textLayoutCache = new TextLayoutCache();
                    textComponent = tc;
                    viewHierarchy = ViewHierarchy.get(textComponent);
                    updateStartEndPos();
                    accurateSpan = Boolean.TRUE.equals(textComponent.getClientProperty(ACCURATE_SPAN_PROPERTY));
                    viewUpdates = new ViewUpdates(this);
                    textComponent.addPropertyChangeListener(this);
                    if (ViewHierarchy.REPAINT_LOG.isLoggable(Level.FINE)) {
                        DebugRepaintManager.register(textComponent);
                    }
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
                        uninstallFromViewport();
                        textComponent.removePropertyChangeListener(DocumentView.this);
                        textLayoutCache = null;
                        viewUpdates = null;
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
    
    void checkViewsInited() { // Must be called under mutex
        if (!childrenValid && textComponent != null) {
            updateVisibleDimension();
            checkSettingsInfo();
            if (checkFontRenderContext()) {
                updateCharMetrics();
            }
            ((EditorTabExpander) tabExpander).updateTabSize();
            if (isBuildable()) {
                LOG.fine("viewUpdates.reinitViews()\n");
                // Signal early that the views will be valid - otherwise preferenceChange()
                // that calls getPreferredSpan() would attempt to reinit the views again
                // (failing in HighlightsViewFactory on usageCount).
                childrenValid = true;
                viewUpdates.reinitAllViews();
            }
        }
    }
    
    boolean ensureChildrenValid(int startIndex, int endIndex, int extraStart, int extraEnd) {
        return viewUpdates.ensureChildrenValid(startIndex, endIndex, extraStart, extraEnd);
    }
    
    private boolean checkFontRenderContext() { // check various things related to rendering
        if (fontRenderContext == null) {
            Graphics graphics = textComponent.getGraphics();
            if (graphics != null) {
                assert (graphics instanceof Graphics2D) : "Not Graphics2D";
                // Use rendering hints (antialiasing etc.)
                if (renderingHints != null) {
                    ((Graphics2D) graphics).setRenderingHints(renderingHints);
                }
                fontRenderContext = ((Graphics2D) graphics).getFontRenderContext();
                if (fontRenderContext != null) {
                    return true; // Updated
                }
            }
        }
        return false;
    }
    
    protected void releaseChildrenUnlocked() { // It should be called with acquired mutex
        // Do not set children == null like in super.releaseChildren()
        // Instead mark them as invalid but allow to use them in certain limited cases
        childrenValid = false;
    }
    
    public void releaseChildren(final boolean updateFonts) { // It acquires document readlock and VH mutex first
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                if (updateFonts) {
                    updateDefaultFontAndColors(); // Includes releaseChildren()
                } else {
                    releaseChildrenUnlocked();
                }
            }
        });
    }

    /**
     * Whether the view should compute accurate spans (no lazy children views computation).
     * This is handy e.g. for fold preview computation since the fold preview
     * pane must be properly measured.
     *
     * @return whether accurate span measurements should be performed.
     */
    boolean isAccurateSpan() {
        return accurateSpan;
    }

    private void updateVisibleDimension() { // Called only with textComponent != null
        // Must be called under mutex
        Component parent = textComponent.getParent();
        Rectangle newRect;
        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport) parent;
            if (listeningOnViewport != viewport) {
                uninstallFromViewport();
                listeningOnViewport = viewport;
                if (listeningOnViewport != null) {
                    listeningOnViewport.addChangeListener(this);
                    // Assume JViewport's parent JScrollPane won't change without viewport change as well
                    Component scrollPane = listeningOnViewport.getParent();
                    MouseWheelListener[] mwls = scrollPane.getListeners(MouseWheelListener.class);
                    // Only function in regular setup when there's BasicScrollPaneUI.Handler listening and nothing else
                    if (mwls.length == 1) {
                        origMouseWheelListener = mwls[0]; // Component.addMouseWheelListener() checks listener's non-nullity
                        scrollPane.removeMouseWheelListener(origMouseWheelListener);
                    }
                    // Listener in "this" will delegate to origMouseWheelListener when desired (Ctrl not pressed).
                    listeningOnViewport.getParent().addMouseWheelListener(this);
                }
            }
            newRect = viewport.getViewRect();

        } else {
            Dimension size = textComponent.getSize();
            newRect = new Rectangle(0, 0, size.width, size.height);
        }

        boolean widthDiffers = (newRect.width != visibleRect.width);
        visibleRect = newRect;
        if (widthDiffers) {
            availableWidthValid = false;
            if (children != null) {
                if (ViewHierarchy.SPAN_LOG.isLoggable(Level.FINE)) {
                    ViewHierarchy.SPAN_LOG.fine("Component width differs => children.recomputeChildrenWidths()\n"); // NOI18N
                }
                children.recomputeChildrenWidths();
            }
        }
    }
    
    private void uninstallFromViewport() {
        if (listeningOnViewport != null) {
            // Assume JViewport's parent JScrollPane won't change without viewport change as well
            listeningOnViewport.getParent().removeMouseWheelListener(this);
            if (origMouseWheelListener != null) {
                listeningOnViewport.getParent().addMouseWheelListener(origMouseWheelListener);
            }
            listeningOnViewport.removeChangeListener(this);
            listeningOnViewport = null;
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // First lock document and then monitor
        runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                if (textComponent != null) {
                    updateVisibleDimension();
                }
            }
        });
    }

    private void checkSettingsInfo() {
        JTextComponent tc = textComponent;
        if (tc == null) {
            return;
        }

        if (prefs == null) {
            String mimeType = DocumentUtilities.getMimeType(tc);
            prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            prefsListener = new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    updatePreferencesSettings(true);
                }
            };
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs));
            updatePreferencesSettings(false);
        }

        if (lookupListener == null) {
            lookupListener = new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    @SuppressWarnings("unchecked")
                    final Lookup.Result<FontColorSettings> result = (Lookup.Result<FontColorSettings>) ev.getSource();
                    SwingUtilities.invokeLater(new Runnable() { // Must run in AWT to apply fonts/colors to comp.
                        @Override
                        public void run() {
                            runReadLockTransaction(new Runnable() {
                                @Override
                                public void run() {
                                    if (textComponent != null) {
                                        updateFontColorSettings(result, true);
                                    }
                                }
                            });
                        }
                    });
                }
            };
            String mimeType = DocumentUtilities.getMimeType(tc);
            Lookup lookup = MimeLookup.getLookup(mimeType);
            Lookup.Result<FontColorSettings> result = lookup.lookupResult(FontColorSettings.class);
            // Called without explicitly acquiring mutex but it's called only when lookup listener is null
            // so it should be acquired.
            updateFontColorSettings(result, false);
            updateDefaultFontAndColors();

            result.addLookupListener(WeakListeners.create(LookupListener.class,
                    lookupListener, result));
        }

        if (lineWrapType == null) {
            updateLineWrapType();
            Document doc = getDocument();
            updateTextLimitLine(doc);
            availableWidthValid = false;
            DocumentUtilities.addPropertyChangeListener(doc, WeakListeners.propertyChange(this, doc));
        }
    }
    
    /* private */ void updatePreferencesSettings(boolean updateComponent) {
        boolean nonPrintableCharactersVisibleOrig = nonPrintableCharactersVisible;
        nonPrintableCharactersVisible = Boolean.TRUE.equals(prefs.getBoolean(
                SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE, false));
        boolean releaseChildren = updateComponent && 
                (nonPrintableCharactersVisible != nonPrintableCharactersVisibleOrig);
        if (releaseChildren) {
            releaseChildren(false);
        }
    }

    /* private */ void updateFontColorSettings(Lookup.Result<FontColorSettings> result, boolean updateComponent) {
        AttributeSet defaultColoringOrig = defaultColoring;
        FontColorSettings fcs = result.allInstances().iterator().next();
        AttributeSet newDefaultColoring = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        // Attempt to always hold non-null content of "defaultColoring" variable once it became non-null
        if (newDefaultColoring != null) {
            defaultColoring = newDefaultColoring;
        }
        Color textLimitLineColorOrig = textLimitLineColor;
        AttributeSet textLimitLineColoring = fcs.getFontColors(FontColorNames.TEXT_LIMIT_LINE_COLORING);
        textLimitLineColor = (textLimitLineColoring != null) 
                ? (Color) textLimitLineColoring.getAttribute(StyleConstants.Foreground)
                : null;
        if (textLimitLineColor == null) {
            textLimitLineColor = Color.PINK;
        }
        boolean releaseChildren = updateComponent &&
                (!defaultColoring.equals(defaultColoringOrig));
        if (releaseChildren) {
            releaseChildren(true); // update fonts and colors
        } else {
            boolean repaint = updateComponent &&
                    (textLimitLineColor == null || !textLimitLineColor.equals(textLimitLineColorOrig));
            if (repaint) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JTextComponent tc = textComponent;
                        if (tc != null) {
                            tc.repaint();
                        }
                    }
                });
                
            }
        }
    }
    
    private void updateTextLimitLine(Document doc) {
        // #183797 - most likely seeing a non-nb document during the editor pane creation
        Integer dllw = (Integer) doc.getProperty(SimpleValueNames.TEXT_LIMIT_WIDTH);
        int textLimitLineColumn = (dllw != null) ? dllw.intValue() : EditorPreferencesDefaults.defaultTextLimitWidth;
        boolean drawTextLimitLine = prefs.getBoolean(SimpleValueNames.TEXT_LIMIT_LINE_VISIBLE, true);
        textLimitLineX = drawTextLimitLine ? (int) (textLimitLineColumn * defaultCharWidth) : -1;
    }
    
    private void updateLineWrapType() {
        // Should be able to run without mutex
        String lwt = null;
        if (textComponent != null) {
            lwt = (String) textComponent.getClientProperty(SimpleValueNames.TEXT_LINE_WRAP);
        }
        if (lwt == null) {
            Document doc = getDocument();
            lwt = (String) doc.getProperty(SimpleValueNames.TEXT_LINE_WRAP);
        }
        if (lwt != null) {
            lineWrapType = LineWrapType.fromSettingValue(lwt);
            if (lineWrapType == null) {
                lineWrapType = LineWrapType.NONE;
            }
        }
        availableWidthValid = false;
    }

    /*private*/ void updateDefaultFontAndColors() {
        // This should be called with mutex acquired
        // Called only with textComponent != null
        Font font = textComponent.getFont();
        Color foreColor = textComponent.getForeground();
        Color backColor = textComponent.getBackground();
        if (defaultColoring != null) {
            Font validFont = (font != null) ? font : fallbackFont();
            font = ViewUtils.getFont(defaultColoring, validFont);
            Integer textZoom = (Integer) textComponent.getClientProperty(TEXT_ZOOM_PROPERTY);
            if (textZoom != null && textZoom != 0) {
                int newSize = Math.max(font.getSize() + textZoom, 2);
                font = new Font(font.getFamily(), font.getStyle(), newSize);
            }
            Color c = (Color) defaultColoring.getAttribute(StyleConstants.Foreground);
            if (c != null) {
                foreColor = c;
            }
            c = (Color) defaultColoring.getAttribute(StyleConstants.Background);
            if (c != null) {
                backColor = c;
            }
            renderingHints = (Map<?, ?>) defaultColoring.getAttribute(EditorStyleConstants.RenderingHints);
        }

        defaultFont = font;
        defaultForeground = foreColor;
        defaultBackground = backColor;

        if (!customFont && textComponent != null) {
            textComponent.setFont(defaultFont);
        }
        if (!customForeground && textComponent != null) {
            textComponent.setForeground(defaultForeground);
        }
        if (!customBackground && textComponent != null) {
            textComponent.setBackground(defaultBackground);
        }
        if (textComponent != null) {
            updateCharMetrics(); // Update metrics with just updated font
            releaseChildrenUnlocked();
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(getDumpId() + ": Updated DEFAULTS: font=" + defaultFont + // NOI18N
                    ", fg=" + ViewUtils.toString(defaultForeground) + // NOI18N
                    ", bg=" + ViewUtils.toString(defaultBackground) + '\n'); // NOI18N
        }
    }

    private void updateCharMetrics() { // Update default line height and other params
        checkFontRenderContext(); // Possibly get FRC created; ignore ret value since now actually updating the metrics
        FontRenderContext frc = getFontRenderContext();
        assert (defaultFont != null) : "Null defaultFont"; // NOI18N
        if (frc != null) {
            // Reset all the measurements to adhere just to default font.
            // Possible other fonts in fontInfos get eliminated.
            fontInfos.clear();
            FontInfo defaultFontInfo = new FontInfo(defaultFont, this, frc);
            fontInfos.put(defaultFont, defaultFontInfo);
            defaultAscent = defaultFontInfo.ascent;
            defaultDescent = defaultFontInfo.descent;
            defaultLeading = defaultFontInfo.leading;
            updateLineHeight();
            defaultCharWidth = defaultFontInfo.charWidth;
            
            tabTextLayout = null;
            singleCharTabTextLayout = null;
            lineContinuationTextLayout = null;

            updateTextLimitLine(getDocument());
            availableWidthValid = false;

            LOG.fine("updateCharMetrics(): FontRenderContext: AA=" + frc.isAntiAliased() + // NOI18N
                    ", AATransformed=" + frc.isTransformed() + // NOI18N
                    ", AAFractMetrics=" + frc.usesFractionalMetrics() + // NOI18N
                    ", AAHint=" + frc.getAntiAliasingHint() + "\n"); // NOI18N
        }
    }
    
    private void updateLineHeight() {
        defaultLineHeight = (float) Math.ceil(defaultAscent + defaultDescent + defaultLeading);
    }
    
    void notifyFontUse(Font font) {
        if (font == defaultFont || fontInfos.containsKey(font)) { // quick check for ==
            return;
        }
        FontInfo fontInfo = new FontInfo(font, this, getFontRenderContext());
        fontInfos.put(font, fontInfo);
        boolean change = false;
        if (fontInfo.ascent > defaultAscent) {
            defaultAscent = fontInfo.ascent;
            change = true;
        }
        if (fontInfo.descent > defaultDescent) {
            defaultDescent = fontInfo.descent;
            change = true;
        }
        if (fontInfo.leading > defaultLeading) {
            defaultLeading = fontInfo.leading;
            change = true;
        }
        if (change) {
            updateLineHeight();
            releaseChildrenUnlocked();
        }
    }

    @Override
    public String getToolTipTextChecked(double x, double y, Shape allocation) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    return children.getToolTipTextChecked(this, x, y, allocation);
                }
            } finally {
                unlock();
            }
        }
        return null;
    }

    @Override
    public JComponent getToolTip(double x, double y, Shape allocation) {
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    return children.getToolTip(this, x, y, allocation);
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
                checkViewsInited();
                if (isActive()) {
                    // Use rendering hints (antialiasing etc.)
                    if (g != null && renderingHints != null) {
                        g.setRenderingHints(renderingHints);
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
        Rectangle2D.Double rect = ViewUtils.shape2Bounds(alloc);
        Shape retShape = null;
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
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
            } finally {
                unlock();
            }
        }
        if (retShape == null) {
            // Attempt to just return height of line since otherwise e.g. caret
            // would have height of the whole doc which is undesirable.
            if (defaultLineHeight > 0f) {
                rect.height = defaultLineHeight;
            }
            retShape = rect;
        }
        if (ViewHierarchy.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchy.OP_LOG, "modelToView(" + offset + // NOI18N
                    ")=" + retShape + "\n"); // NOI18N
        }
        return retShape;
    }

    public double modelToY(int offset, Shape alloc) {
        double retY = 0d;
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    int index = getViewIndex(offset);
                    if (index >= 0) {
                        retY = getY(index);
                    }
                }
            } finally {
                unlock();
            }
        }
        if (ViewHierarchy.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchy.OP_LOG, "modelToY(" + offset + ")=" + retY + "\n"); // NOI18N
        }
        return retY;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        int retOffset = 0;
        if (lock()) {
            try {
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    retOffset = children.viewToModelChecked(this, x, y, alloc, biasReturn);
                }
            } finally {
                unlock();
            }
        }
        if (ViewHierarchy.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchy.OP_LOG, "viewToModel: [x,y]=" + x + "," + y + // NOI18N
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
                checkViewsInited();
                if (isActive()) {
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
        if (ViewHierarchy.OP_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchy.OP_LOG, "nextVisualPosition(" + offset + "," + // NOI18N
                    ViewUtils.toStringDirection(direction) + ")=" + retOffset + "\n"); // NOI18N
        }
        return retOffset;
    }
    
    void setIncomingModification(boolean incomingModification) {
        this.incomingModification = incomingModification;
    }

    boolean isActive() {
        if (incomingModification) {
            if (LOG_QUERY_IN_MODIFICATION.isLoggable(Level.FINE)) {
                LOG_QUERY_IN_MODIFICATION.log(Level.INFO, "View Hierarchy Query during Incoming Modification\n", // NOI18N
                        new Exception());
            }
            return false;
        } else {
            return isUpdatable();
        }
    }

    boolean isUpdatable() { // Whether the view hierarchy can be updated (by insertUpdate() etc.)
        return textComponent != null && childrenValid && (lengthyAtomicEdit <= 0);
    }
    
    boolean isBuildable() {
        return textComponent != null && fontRenderContext != null && 
                (lengthyAtomicEdit <= 0) && !incomingModification;
    }

    /**
     * It should be called with +1 once it's detected that there's a lengthy atomic edit
     * in progress and with -1 when such edit gets finished.
     * @param delta +1 or -1 when entering/leaving lengthy atomic edit.
     */
    public void updateLengthyAtomicEdit(int delta) {
        lengthyAtomicEdit += delta;
        if (LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(LOG, "updateLengthyAtomicEdit: delta=" + delta + // NOI18N
                    " lengthyAtomicEdit=" + lengthyAtomicEdit + "\n"); // NOI18N
        }
        if (lengthyAtomicEdit == 0) {
            releaseChildren(false);
        }
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

    /**
     * Get displayed portion of the component (either viewport.getViewRect())
     * or (if viewport is missing) size of the component.
     * @return 
     */
    Rectangle getVisibleRect() {
        return visibleRect;
    }
    
    /**
     * Get width available for display of child views. For non-wrap case it's Integer.MAX_VALUE
     * and for wrapping it's a display width or (if display width would become too narrow)
     * a width of four chars to not overflow the word wrapping algorithm.
     * @return 
     */
    float getAvailableWidth() {
        if (!availableWidthValid) {
            // Mark valid and assign early values to prevent stack overflow in getLineContinuationCharTextLayout()
            availableWidthValid = true;
            availableWidth = Integer.MAX_VALUE;
            renderWrapWidth = availableWidth;
            TextLayout lineContTextLayout = getLineContinuationCharTextLayout();
            if (lineContTextLayout != null && (getLineWrapType() != LineWrapType.NONE)) {
                availableWidth = Math.max(getVisibleRect().width, 4 * getDefaultCharWidth() + lineContTextLayout.getAdvance());
                renderWrapWidth = availableWidth - lineContTextLayout.getAdvance();
            }
        }
        return availableWidth;
    }
    
    /**
     * Get width available for rendering of real text on a wrapped line.
     * It's {@link #getAvailableWidth()} minus width of wrap designating character.
     * @return 
     */
    float getRenderWrapWidth() {
        return renderWrapWidth;
    }

    JTextComponent getTextComponent() {
        return textComponent;
    }

    TextLayoutCache getTextLayoutCache() {
        return textLayoutCache;
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    public float getDefaultLineHeight() {
        checkSettingsInfo();
        return defaultLineHeight;
    }

    public float getDefaultAscent() {
        checkSettingsInfo();
        return defaultAscent;
    }

    /**
     * Return array of default:
     * <ol>
     *     <li>Underline offset.</li>
     *     <li>Underline thickness.</li>
     *     <li>Strike-through offset.</li>
     *     <li>Strike-through thickness.</li>
     * </ol>
     */
    public float[] getUnderlineAndStrike(Font font) {
        checkSettingsInfo();
        FontInfo fontInfo = fontInfos.get(font);
        if (fontInfo == null) { // Should not normally happen
            fontInfo = fontInfos.get(defaultFont);
        }
        return fontInfo.underlineAndStrike;
    }

    public float getDefaultCharWidth() {
        checkSettingsInfo();
        return defaultCharWidth;
    }

    public boolean isShowNonPrintingCharacters() {
        checkSettingsInfo();
        return nonPrintableCharactersVisible;
    }

    LineWrapType getLineWrapType() {
        checkSettingsInfo();
        return lineWrapType;
    }

    Color getTextLimitLineColor() {
        checkSettingsInfo();
        return textLimitLineColor;
    }

    int getTextLimitLineX() {
        return textLimitLineX;
    }

    TextLayout getNewlineCharTextLayout() {
        if (newlineTextLayout == null && defaultFont != null) {
            newlineTextLayout = createTextLayout(String.valueOf(PRINTING_NEWLINE), defaultFont);
        }
        return newlineTextLayout;
    }

    TextLayout getTabCharTextLayout(double availableWidth) {
        if (tabTextLayout == null && defaultFont != null) {
            tabTextLayout = createTextLayout(String.valueOf(PRINTING_TAB), defaultFont);
        }
        TextLayout ret = tabTextLayout;
        if (tabTextLayout != null && availableWidth > 0 && tabTextLayout.getAdvance() > availableWidth) {
            if (singleCharTabTextLayout == null) {
                for (int i = defaultFont.getSize() - 1; i >= 0; i--) {
                    Font font = new Font(defaultFont.getName(), defaultFont.getStyle(), i);
                    singleCharTabTextLayout = createTextLayout(String.valueOf(PRINTING_TAB), font);
                    if (singleCharTabTextLayout != null) {
                        if (singleCharTabTextLayout.getAdvance() <= getDefaultCharWidth()) {
                            LOG.log(Level.FINE, "singleChar font size={0}\n", i);
                            break;
                        }
                    } else { // layout creation failed
                        break;
                    }
                }
            }
            ret = singleCharTabTextLayout;
        }
        return ret;
    }

    TextLayout getLineContinuationCharTextLayout() {
        if (lineContinuationTextLayout == null && defaultFont != null) {
            char lineContinuationChar = LINE_CONTINUATION;
            if (!defaultFont.canDisplay(lineContinuationChar)) {
                lineContinuationChar = LINE_CONTINUATION_ALTERNATE;
            }
            lineContinuationTextLayout = createTextLayout(String.valueOf(lineContinuationChar), defaultFont);
        }
        return lineContinuationTextLayout;
    }

    private TextLayout createTextLayout(String text, Font font) {
        checkSettingsInfo();
        if (fontRenderContext != null && font != null) {
            ViewStats.incrementTextLayoutCreated(text.length());
            return new TextLayout(text, font, fontRenderContext);
        }
        return null;
    }
    
    TextLayout createTextLayout(String text, AttributeSet attrs) {
        Font font = (defaultFont != null) ? defaultFont : fallbackFont();
        font = ViewUtils.getFont(attrs, font);
        return createTextLayout(text, font);
    }
    
    Font fallbackFont() {
        if (fallbackFont == null) {
            fallbackFont = new Font("Monospaced", Font.PLAIN, 12);
        }
        return fallbackFont;
    }

    void checkDocumentLockedIfLogging() {
        if (LOG.isLoggable(Level.FINE)) {
            checkDocumentLocked();
        }
    }
    
    void checkDocumentLocked() {
        if (!DocumentUtilities.isReadLocked(getDocument())) {
            LOG.log(Level.INFO, "Document not locked", new Exception("Document not locked")); // NOI18N
        }
    }
    
    void checkMutexAcquiredIfLogging() {
        if (LOG.isLoggable(Level.FINE)) {
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
                LOG.log(Level.INFO, msg + " for textComponent=" + textComponent, new Exception()); // NOI18N
            }
        }
    }

    boolean isLocked() {
        PriorityMutex mutex = pMutex;
        return (mutex != null && mutex.getLockThread() == Thread.currentThread());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean releaseChildren = false;
        boolean updateFonts = false;
        if (evt.getSource() instanceof Document) {
            String propName = evt.getPropertyName();
            if (propName == null || SimpleValueNames.TEXT_LINE_WRAP.equals(propName)) {
                LineWrapType origLineWrapType = lineWrapType;
                updateLineWrapType(); // can run without mutex
                if (origLineWrapType != lineWrapType) {
                    LOG.log(Level.FINE, "Changing lineWrapType from {0} to {1}", new Object [] { origLineWrapType, lineWrapType }); //NOI18N
                    releaseChildren = true;
                }
            }
            if (propName == null || SimpleValueNames.TAB_SIZE.equals(propName)) {
                releaseChildren = true;
            }
            if (propName == null || SimpleValueNames.TEXT_LIMIT_WIDTH.equals(propName)) {
                updateTextLimitLine(getDocument());
                releaseChildren = true;
            }
        } else { // an event from JTextComponent
            String propName = evt.getPropertyName();
            if ("ancestor".equals(propName)) { // NOI18N

            } else if ("document".equals(propName)) { // NOI18N
                
            } else if ("font".equals(propName)) { // NOI18N
                if (!customFont && defaultFont != null) {
                    customFont = (textComponent != null) &&
                            !defaultFont.equals(textComponent.getFont());
                }
                if (customFont) {
                    updateFonts = true;
                }
                releaseChildren = true;
            } else if ("foreground".equals(propName)) { //NOI18N
                if (!customForeground && defaultForeground != null) {
                    customForeground = (textComponent != null) &&
                            !defaultForeground.equals(textComponent.getForeground());
                }
                // Release children since TextLayoutPart caches foreground and background
                releaseChildren = true;
            } else if ("background".equals(propName)) { //NOI18N
                if (!customBackground && defaultBackground != null) {
                    customBackground = (textComponent != null) &&
                            !defaultBackground.equals(textComponent.getBackground());
                }
                // Release children since TextLayoutPart caches foreground and background
                releaseChildren = true;
            } else if (SimpleValueNames.TEXT_LINE_WRAP.equals(propName)) {
                updateLineWrapType(); // can run without mutex
                releaseChildren = true;
            } else if (START_POSITION_PROPERTY.equals(propName) || END_POSITION_PROPERTY.equals(propName)) {
                runReadLockTransaction(new Runnable() {
                    @Override
                    public void run() {
                        updateStartEndPos();
                        releaseChildrenUnlocked(); // Rebuild view hierarchy
                    }
                });
            } else if (TEXT_ZOOM_PROPERTY.equals(propName)) {
                releaseChildren = true;
                updateFonts = true;
            }
        }
        if (releaseChildren) {
            releaseChildren(updateFonts);
        }
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {
        // Since consume() idoes not prevent BasicScrollPaneUI.Handler from operation
        // the code in DocumentView.setParent() removes BasicScrollPaneUI.Handler and stores it
        // in origMouseWheelListener and installs "this" as MouseWheelListener instead.
        // This method only calls origMouseWheelListener if Ctrl is not pressed (zooming in/out).
        if (evt.isControlDown() && evt.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            // Subtract rotation (instead of adding) in order to increase the font by rotating wheel up.
            JTextComponent tc = textComponent;
            if (tc != null) {
                Integer textZoom = ((Integer) tc.getClientProperty(TEXT_ZOOM_PROPERTY));
                if (textZoom == null) {
                    textZoom = 0;
                }
                textZoom -= evt.getWheelRotation();
                tc.putClientProperty(TEXT_ZOOM_PROPERTY, textZoom);
            }
//            evt.consume(); // consuming the event has no effect
        } else {
            origMouseWheelListener.mouseWheelMoved(evt);
        }
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
        sb.append("; incomingMod=").append(incomingModification); // NOI18N
        sb.append("; lengthyAtomicEdit=").append(lengthyAtomicEdit); // NOI18N
        sb.append("; Bounds:<");
        sb.append(hasExtraStartBound() ? startPos.getOffset() : "DOC-START");
        sb.append(","); // NOI18N
        sb.append(hasExtraEndBound() ? endPos.getOffset() : "DOC-END");
        sb.append('>');
        sb.append(", Chged:");
        if (isWidthChange()) sb.append(" W");
        if (isHeightChange()) sb.append(" H");
        if (isChildWidthChange()) sb.append(" ChW");
        if (isChildHeightChange()) sb.append(" ChH");
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

    private static class FontInfo {
        
        final float ascent;
        
        final float descent;
        
        final float leading;
        
        final float charWidth;

        /**
         * Array of
         * <ol>
         *     <li>Underline offset.</li>
         *     <li>Underline thickness.</li>
         *     <li>Strike-through offset.</li>
         *     <li>Strike-through thickness.</li>
         * </ol>
         */
        final float[] underlineAndStrike = new float[4];
        
        FontInfo(Font font, DocumentView docView, FontRenderContext frc) {
            char defaultChar = 'A';
            String defaultCharText = String.valueOf(defaultChar);
            TextLayout defaultCharTextLayout = new TextLayout(defaultCharText, font, frc); // NOI18N
            TextLayout lineHeightTextLayout = new TextLayout("A_|B", font, frc);
            // Round the ascent to eliminate long mantissa without any visible effect on rendering.
            ascent = lineHeightTextLayout.getAscent();
            descent = lineHeightTextLayout.getDescent();
            leading = lineHeightTextLayout.getLeading();
            // Ceil fractions to whole numbers since this measure may be used for background rendering
            charWidth = (float) Math.ceil(defaultCharTextLayout.getAdvance());

            LineMetrics lineMetrics = font.getLineMetrics(defaultCharText, frc);
            underlineAndStrike[0] = lineMetrics.getUnderlineOffset();
            underlineAndStrike[1] = lineMetrics.getUnderlineThickness();
            underlineAndStrike[2] = lineMetrics.getStrikethroughOffset();
            underlineAndStrike[3] = lineMetrics.getStrikethroughThickness();

            if (LOG.isLoggable(Level.FINE)) {
                FontMetrics fm = docView.getTextComponent().getFontMetrics(font);
                LOG.fine("Font: " + font + "\nSize2D: " + font.getSize2D() + // NOI18N
                        ", ascent=" + ascent + ", descent=" + descent + // NOI18N
                        ", leading=" + leading + "\nChar-width=" + charWidth + // NOI18N
                        ", underlineO/T=" + underlineAndStrike[0] + "/" + underlineAndStrike[1] + // NOI18N
                        ", strikethroughO/T=" + underlineAndStrike[2] + "/" + underlineAndStrike[3] + // NOI18N
                        "\nFontMetrics (for comparison): fm-line-height=" + // NOI18N
                        fm.getHeight() + ", fm-ascent=" + fm.getAscent() + // NOI18N
                        ", fm-descent=" + fm.getDescent() + "\n");
            }
        }
        
    }
}
