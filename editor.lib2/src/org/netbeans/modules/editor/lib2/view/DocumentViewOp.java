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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * Document View operation management.
 * 
 * @author Miloslav Metelka
 */

@SuppressWarnings("ClassWithMultipleLoggers") //NOI18N
public final class DocumentViewOp
        implements PropertyChangeListener, ChangeListener, MouseWheelListener
{

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentViewOp.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentViewOp.class.getName());

    static final char PRINTING_SPACE = '\u00B7';
    static final char PRINTING_TAB = '\u00BB'; // \u21FE
    static final char PRINTING_NEWLINE = '\u00B6';
    static final char LINE_CONTINUATION = '\u21A9';
    static final char LINE_CONTINUATION_ALTERNATE = '\u2190';
    
    private static final int CHILD_WIDTH_CHANGE                 = 1;
    private static final int CHILD_HEIGHT_CHANGE                = 2;
    private static final int WIDTH_CHANGE                       = 4;
    private static final int HEIGHT_CHANGE                      = 8;
    
    /**
     * Whether the "children" is currently reflecting the document state.
     * <br/>
     * The children may hold a semi-valid view hierarchy which may still be partly used
     * to resolve queries in some cases.
     */
    private static final int CHILDREN_VALID                     = 16;
    
    /**
     * Whether there's a pending document modification so the view hierarchy should not be active
     * until it gets updated by the pending modification.
     */
    private static final int INCOMING_MODIFICATION              = 32;
    
    /**
     * Whether view hierarchy currently fires a change so any queries to view hierarchy are prohibited.
     */
    private static final int FIRING_CHANGE                      = 64;
    
    private static final int ACCURATE_SPAN                      = 128;

    private static final int AVAILABLE_WIDTH_VALID              = 256;
    
    /**
     * Whether the textComponent's font was explicitly set from outside
     * or whether it was only updated internally by settings.
     */
    private static final int CUSTOM_FONT                        = 1024;
    
    private static final int CUSTOM_FOREGROUND                  = 2048;
    
    private static final int CUSTOM_BACKGROUND                  = 4096;
    
    private static final int NON_PRINTABLE_CHARACTERS_VISIBLE   = 8192;

    private final DocumentView docView;

    private int statusBits;
    
    /**
     * Maintenance of view updates.
     * If this is a preview-only view e.g. for a collapsed fold preview
     * when located over collapsed fold's tooltip.
     */
    private ViewUpdates viewUpdates;
    
    private TextLayoutCache textLayoutCache;

    /**
     * Visible rectangle of the viewport or a text component if there is no viewport.
     */
    private Rectangle visibleRect = new Rectangle();

    private float availableWidth;
    
    private float renderWrapWidth;
    
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
    
    private Font defaultFont;

    /**
     * Default line height computed as height of the defaultFont.
     */
    private float defaultLineHeight;

    private float defaultAscent;

    private float defaultDescent;
    
    private float defaultLeading;

    private float defaultCharWidth;

    private Color defaultForeground;

    private Color defaultBackground;

    private Color textLimitLineColor;

    private int textLimitLineX;
    
    private LineWrapType lineWrapType;

    private TextLayout newlineTextLayout;

    private TextLayout tabTextLayout;

    private TextLayout singleCharTabTextLayout;

    private TextLayout lineContinuationTextLayout;

    private LookupListener lookupListener;

    private JViewport listeningOnViewport;

    private Preferences prefs;

    private PreferenceChangeListener prefsListener;

    Map<?, ?> renderingHints;
    
    private int lengthyAtomicEdit; // Long atomic edit being performed

    ViewHierarchyImpl viewHierarchyImpl; // Assigned upon setParent()
    
    private Map<Font,FontInfo> fontInfos = new HashMap<Font, FontInfo>(4);
    
    private Font fallbackFont;
    
    private MouseWheelListener origMouseWheelListener;
    
    public DocumentViewOp(DocumentView docView) {
        this.docView = docView;
    }

    public ViewHierarchyImpl viewHierarchyImpl() {
        return viewHierarchyImpl;
    }

    public boolean isChildrenValid() {
        return isAnyStatusBit(CHILDREN_VALID);
    }

    /**
     * Rebuild views if there are any pending highlight factory changes reported.
     * Method ensures proper locking of document and view hierarchy.
     */
    public void syncViewsRebuild() {
        docView.runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                if (viewUpdates != null) {
                    viewUpdates.syncedViewsRebuild();
                }
            }
        });
    }
    
    public void notifyChildWidthChange() {
        setStatusBits(CHILD_WIDTH_CHANGE);
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINEST)) { // Only when on finest level
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "CHILD-WIDTH changed\n"); // NOI18N
        }
    }
    
    boolean isChildWidthChange() {
        return isAnyStatusBit(CHILD_WIDTH_CHANGE);
    }
    
    void resetChildWidthChange() {
        clearStatusBits(CHILD_WIDTH_CHANGE);
    }

    public void notifyChildHeightChange() {
        setStatusBits(CHILD_HEIGHT_CHANGE);
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINEST)) { // Only when on finest level
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "CHILD-HEIGHT changed\n"); // NOI18N
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
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "DV-WIDTH changed\n"); // NOI18N
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
            return docView.assignChildrenWidth();
        }
        return false;
    }
    
    void notifyHeightChange() {
        setStatusBits(HEIGHT_CHANGE);
        if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
            ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, "DV-HEIGHT changed\n"); // NOI18N
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
            return docView.assignChildrenHeight();
        }
        return false;
    }
    
    boolean isChildWidthHeightChange() {
        return isAnyStatusBit(CHILD_WIDTH_CHANGE | CHILD_HEIGHT_CHANGE );
    }
    
    boolean isAnyWidthHeightChange() {
        return isAnyStatusBit(WIDTH_CHANGE | HEIGHT_CHANGE | CHILD_WIDTH_CHANGE | CHILD_HEIGHT_CHANGE );
    }

    /**
     * Set given status bits to 1.
     */
    private void setStatusBits(int bits) {
        statusBits |= bits;
    }
    
    /**
     * Set given status bits to 0.
     */
    private void clearStatusBits(int bits) {
        statusBits &= ~bits;
    }
    
    /**
     * Set all given status bits to the given value.
     * @param bits status bits to be updated.
     * @param value true to change all status bits to 1 or false to change them to 0.
     */
    private void updateStatusBits(int bits, boolean value) {
        if (value) {
            setStatusBits(bits);
        } else {
            clearStatusBits(bits);
        }
    }
    
    private boolean isAnyStatusBit(int bits) {
        return (statusBits & bits) != 0;
    }

    void unlockCheck() {
        checkRealSpanChange(); // Clears widthChange and heightChange
        checkRepaint();
        if (isAnyWidthHeightChange()) { // There should be no width/height changes
            // Do not throw error (only log) since unlock() call may be in another nested 'finally' section
            // which would lose original stacktrace.
            LOG.log(Level.INFO, "DocumentView invalid state upon unlock: " + docView.toStringUnlocked(), new Exception()); // NOI18N
        }
    }

    void checkRealSpanChange() {
        boolean widthChange = checkRealWidthChange();
        boolean heightChange = checkRealHeightChange();
        if (widthChange || heightChange) {
            if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINE)) {
                String msg = "TC-preferenceChanged(" + // NOI18N
                        (widthChange ? "W" : "-") + "x" + (heightChange ? "H" : "-") + ")\n"; // NOI18N
                ViewUtils.log(ViewHierarchyImpl.SPAN_LOG, msg);
            }
            // RootView.preferenceChanged() calls textComponent.revalidate (reposts to EDT if not called in it already)
            docView.superPreferenceChanged(widthChange, heightChange);
        }
    }
    
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

        if (ViewHierarchyImpl.REPAINT_LOG.isLoggable(Level.FINE)) {
            String msg = "NOTIFY-REPAINT x0,y0,x1,y1: [" + x0 + "," + y0 + "," + x1 + "," + y1 + "] => [" // NOI18N
                     + repaintX0 + "," + repaintY0 + "," + repaintX1 + "," + repaintY1 + "]\n"; // NOI18N
            if (ViewHierarchyImpl.REPAINT_LOG.isLoggable(Level.FINER)) {
                ViewHierarchyImpl.REPAINT_LOG.log(Level.INFO, "Stack of " + msg, new Exception());
            } else {
                ViewHierarchyImpl.REPAINT_LOG.fine(msg);
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
                    JTextComponent textComponent = docView.getTextComponent();
                    if (textComponent != null) {
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.finer("REPAINT x0,y0,x1,y1: [" + x0 + "," + y0 + "," + x1 + "," + y1 + "]\n"); // NOI18N
                        }
                        textComponent.repaint(x0, y0, x1 - x0, y1 - y0);
                    }
                }
            });
        }
    }
    
    private void resetRepaintRegion() {
        repaintX1 = 0d; // Make repaint region empty
    }
    
    Rectangle2D.Double extendToVisibleWidth(Rectangle2D.Double r) {
        r.width = getVisibleRect().getMaxX();
        return r;
    }
    
    void parentViewSet() {
        JTextComponent textComponent = docView.getTextComponent();
        textLayoutCache = new TextLayoutCache();
        updateStatusBits(ACCURATE_SPAN, Boolean.TRUE.equals(textComponent.getClientProperty(DocumentView.ACCURATE_SPAN_PROPERTY)));
        viewUpdates = new ViewUpdates(docView);
        textComponent.addPropertyChangeListener(this);
        viewHierarchyImpl = ViewHierarchyImpl.get(textComponent);
        viewHierarchyImpl.setDocumentView(docView);
        if (ViewHierarchyImpl.REPAINT_LOG.isLoggable(Level.FINE)) {
            DebugRepaintManager.register(textComponent);
        }
    }
    
    void parentCleared() {
        JTextComponent textComponent = docView.getTextComponent(); // not null yet
        viewHierarchyImpl.setDocumentView(null);
        uninstallFromViewport();
        textComponent.removePropertyChangeListener(this);
        textLayoutCache = null;
        viewUpdates = null;
    }
    
    void checkViewsInited() { // Must be called under mutex
        if (!isAnyStatusBit(CHILDREN_VALID) && docView.getTextComponent() != null) {
            updateVisibleDimension();
            checkSettingsInfo();
            if (checkFontRenderContext()) {
                updateCharMetrics();
            }
            ((EditorTabExpander) docView.getTabExpander()).updateTabSize();
            if (isBuildable()) {
                LOG.fine("viewUpdates.reinitViews()\n");
                // Signal early that the views will be valid - otherwise preferenceChange()
                // that calls getPreferredSpan() would attempt to reinit the views again
                // (failing in HighlightsViewFactory on usageCount).
                setStatusBits(CHILDREN_VALID);
                viewUpdates.reinitAllViews();
                
                
            }
        }
    }
    
    boolean ensureChildrenValid(int startIndex, int endIndex, int extraStart, int extraEnd) {
        return viewUpdates.ensureChildrenValid(startIndex, endIndex, extraStart, extraEnd);
    }
    
    private boolean checkFontRenderContext() { // check various things related to rendering
        if (fontRenderContext == null) {
            Graphics graphics = docView.getTextComponent().getGraphics();
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
        clearStatusBits(CHILDREN_VALID);
    }
    
    public void releaseChildren(final boolean updateFonts) { // It acquires document readlock and VH mutex first
        docView.runReadLockTransaction(new Runnable() {
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
        return isAnyStatusBit(ACCURATE_SPAN);
    }

    private void updateVisibleDimension() { // Called only with textComponent != null
        // Must be called under mutex
        JTextComponent textComponent = docView.getTextComponent();
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
            clearStatusBits(AVAILABLE_WIDTH_VALID);
            docView.recomputeChildrenWidths();
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
        docView.runReadLockTransaction(new Runnable() {
            @Override
            public void run() {
                JTextComponent textComponent = docView.getTextComponent();
                if (textComponent != null) {
                    updateVisibleDimension();
                }
            }
        });
    }

    private void checkSettingsInfo() {
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent == null) {
            return;
        }

        if (prefs == null) {
            String mimeType = DocumentUtilities.getMimeType(textComponent);
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
                            docView.runReadLockTransaction(new Runnable() {
                                @Override
                                public void run() {
                                    JTextComponent textComponent = docView.getTextComponent();
                                    if (textComponent != null) {
                                        updateFontColorSettings(result, true);
                                    }
                                }
                            });
                        }
                    });
                }
            };
            String mimeType = DocumentUtilities.getMimeType(textComponent);
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
            Document doc = docView.getDocument();
            updateTextLimitLine(doc);
            clearStatusBits(AVAILABLE_WIDTH_VALID);
            DocumentUtilities.addPropertyChangeListener(doc, WeakListeners.propertyChange(this, doc));
        }
    }
    
    /* private */ void updatePreferencesSettings(boolean updateComponent) {
        boolean nonPrintableCharactersVisibleOrig = isAnyStatusBit(NON_PRINTABLE_CHARACTERS_VISIBLE);
        boolean nonPrintableCharactersVisible = Boolean.TRUE.equals(prefs.getBoolean(
                SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE, false));
        updateStatusBits(NON_PRINTABLE_CHARACTERS_VISIBLE, nonPrintableCharactersVisible);
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
                        JTextComponent textComponent = docView.getTextComponent();
                        if (textComponent != null) {
                            textComponent.repaint();
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
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent != null) {
            lwt = (String) textComponent.getClientProperty(SimpleValueNames.TEXT_LINE_WRAP);
        }
        if (lwt == null) {
            Document doc = docView.getDocument();
            lwt = (String) doc.getProperty(SimpleValueNames.TEXT_LINE_WRAP);
        }
        if (lwt != null) {
            lineWrapType = LineWrapType.fromSettingValue(lwt);
            if (lineWrapType == null) {
                lineWrapType = LineWrapType.NONE;
            }
        }
        clearStatusBits(AVAILABLE_WIDTH_VALID);
    }

    /*private*/ void updateDefaultFontAndColors() {
        // This should be called with mutex acquired
        // Called only with textComponent != null
        final JTextComponent textComponent = docView.getTextComponent();
        Font font = textComponent.getFont();
        Color foreColor = textComponent.getForeground();
        Color backColor = textComponent.getBackground();
        if (defaultColoring != null) {
            Font validFont = (font != null) ? font : fallbackFont();
            font = ViewUtils.getFont(defaultColoring, validFont);
            Integer textZoom = (Integer) textComponent.getClientProperty(DocumentView.TEXT_ZOOM_PROPERTY);
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

        if (!isAnyStatusBit(CUSTOM_FONT) && textComponent != null) {
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    textComponent.setFont(defaultFont);
                }
            });
        }
        if (!isAnyStatusBit(CUSTOM_FOREGROUND) && textComponent != null) {
            textComponent.setForeground(defaultForeground);
        }
        if (!isAnyStatusBit(CUSTOM_BACKGROUND) && textComponent != null) {
            textComponent.setBackground(defaultBackground);
        }
        if (textComponent != null) {
            updateCharMetrics(); // Update metrics with just updated font
            releaseChildrenUnlocked();
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(docView.getDumpId() + ": Updated DEFAULTS: font=" + defaultFont + // NOI18N
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
            FontInfo defaultFontInfo = new FontInfo(defaultFont, docView.getTextComponent(), frc);
            fontInfos.put(defaultFont, defaultFontInfo);
            defaultAscent = defaultFontInfo.ascent;
            defaultDescent = defaultFontInfo.descent;
            defaultLeading = defaultFontInfo.leading;
            updateLineHeight();
            defaultCharWidth = defaultFontInfo.charWidth;
            
            tabTextLayout = null;
            singleCharTabTextLayout = null;
            lineContinuationTextLayout = null;

            updateTextLimitLine(docView.getDocument());
            clearStatusBits(AVAILABLE_WIDTH_VALID);

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
        FontInfo fontInfo = new FontInfo(font, docView.getTextComponent(), getFontRenderContext());
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

    void markIncomingModification() {
        setStatusBits(INCOMING_MODIFICATION);
    }
    
    void clearIncomingModification() {
        clearStatusBits(INCOMING_MODIFICATION);
    }

    boolean isActive() {
        if (isAnyStatusBit(FIRING_CHANGE)) {
            throw new IllegalStateException("View hierarchy must not be queried during change firing"); // NOI18N
        } else if (isAnyStatusBit(INCOMING_MODIFICATION)) {
            if (ViewHierarchyImpl.OP_LOG.isLoggable(Level.FINER)) {
                ViewHierarchyImpl.OP_LOG.log(Level.INFO, "View Hierarchy Query during Incoming Modification\n", // NOI18N
                        new Exception());
            }
            return false;
        } else {
            return isUpdatable();
        }
    }

    boolean isUpdatable() { // Whether the view hierarchy can be updated (by insertUpdate() etc.)
        JTextComponent textComponent = docView.getTextComponent();
        return textComponent != null && isAnyStatusBit(CHILDREN_VALID) && (lengthyAtomicEdit <= 0);
    }
    
    boolean isBuildable() {
        JTextComponent textComponent = docView.getTextComponent();
        return textComponent != null && fontRenderContext != null && 
                (lengthyAtomicEdit <= 0) && !isAnyStatusBit(INCOMING_MODIFICATION);
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
        if (!isAnyStatusBit(AVAILABLE_WIDTH_VALID)) {
            // Mark valid and assign early values to prevent stack overflow in getLineContinuationCharTextLayout()
            setStatusBits(AVAILABLE_WIDTH_VALID);
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

    TextLayoutCache getTextLayoutCache() {
        return textLayoutCache;
    }

    FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    public float getDefaultRowHeight() {
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

    public boolean isNonPrintableCharactersVisible() {
        checkSettingsInfo();
        return isAnyStatusBit(NON_PRINTABLE_CHARACTERS_VISIBLE);
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        JTextComponent textComponent = docView.getTextComponent();
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
                updateTextLimitLine(docView.getDocument());
                releaseChildren = true;
            }
        } else { // an event from JTextComponent
            String propName = evt.getPropertyName();
            if ("ancestor".equals(propName)) { // NOI18N

            } else if ("document".equals(propName)) { // NOI18N
                
            } else if ("font".equals(propName)) { // NOI18N
                boolean customFont = isAnyStatusBit(CUSTOM_FONT);
                if (!customFont && defaultFont != null) {
                    customFont = (textComponent != null) &&
                            !defaultFont.equals(textComponent.getFont());
                    updateStatusBits(CUSTOM_FONT, customFont);
                }
                if (customFont) {
                    updateFonts = true;
                }
                releaseChildren = true;
            } else if ("foreground".equals(propName)) { //NOI18N
                if (!isAnyStatusBit(CUSTOM_FOREGROUND) && defaultForeground != null) {
                    updateStatusBits(CUSTOM_FOREGROUND,
                            (textComponent != null) && !defaultForeground.equals(textComponent.getForeground()));
                }
                // Release children since TextLayoutPart caches foreground and background
                releaseChildren = true;
            } else if ("background".equals(propName)) { //NOI18N
                if (!isAnyStatusBit(CUSTOM_BACKGROUND) && defaultBackground != null) {
                    updateStatusBits(CUSTOM_BACKGROUND,
                            textComponent != null && !defaultBackground.equals(textComponent.getBackground()));
                }
                // Release children since TextLayoutPart caches foreground and background
                releaseChildren = true;
            } else if (SimpleValueNames.TEXT_LINE_WRAP.equals(propName)) {
                updateLineWrapType(); // can run without mutex
                releaseChildren = true;
            } else if (DocumentView.START_POSITION_PROPERTY.equals(propName) ||
                    DocumentView.END_POSITION_PROPERTY.equals(propName))
            {
                docView.runReadLockTransaction(new Runnable() {
                    @Override
                    public void run() {
                        docView.updateStartEndPos();
                        releaseChildrenUnlocked(); // Rebuild view hierarchy
                    }
                });
            } else if (DocumentView.TEXT_ZOOM_PROPERTY.equals(propName)) {
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
            JTextComponent textComponent = docView.getTextComponent();
            if (textComponent != null) {
                Integer textZoom = ((Integer) textComponent.getClientProperty(DocumentView.TEXT_ZOOM_PROPERTY));
                if (textZoom == null) {
                    textZoom = 0;
                }
                textZoom -= evt.getWheelRotation();
                textComponent.putClientProperty(DocumentView.TEXT_ZOOM_PROPERTY, textZoom);
            }
//            evt.consume(); // consuming the event has no effect
        } else {
            origMouseWheelListener.mouseWheelMoved(evt);
        }
    }

    StringBuilder appendInfo(StringBuilder sb) {
        sb.append("incomingMod=").append(isAnyStatusBit(INCOMING_MODIFICATION)); // NOI18N
        sb.append("; lengthyAtomicEdit=").append(lengthyAtomicEdit); // NOI18N
        sb.append("\nChged:");
        if (isWidthChange()) sb.append(" W");
        if (isHeightChange()) sb.append(" H");
        if (isChildWidthChange()) sb.append(" ChW");
        if (isChildHeightChange()) sb.append(" ChH");
        return sb;
    }

    @Override
    public String toString() {
        return appendInfo(new StringBuilder(200)).toString();
    }
    
    private static final class FontInfo {
        
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
        
        FontInfo(Font font, JTextComponent textComponent, FontRenderContext frc) {
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
                FontMetrics fm = textComponent.getFontMetrics(font);
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
