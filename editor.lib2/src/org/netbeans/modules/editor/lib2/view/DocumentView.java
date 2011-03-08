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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
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
import javax.swing.text.Caret;
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
public final class DocumentView extends EditorBoxView<ParagraphView>
        implements PropertyChangeListener, ChangeListener
{

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentView.class.getName());

    // -J-Dorg.netbeans.modules.editor.lib2.view.DebugRepaintManager.level=FINE
    private static final Logger REPAINT_LOG = Logger.getLogger(DebugRepaintManager.class.getName());
    
    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView-QueryInModification.level=FINE
    private static final Logger LOG_QUERY_IN_MODIFICATION = Logger.getLogger(DocumentView.class.getName() +
            "-QueryInModification"); // NOI18N

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView-ModelView.level=FINE
    /**
     * Log modelToView(), modelToY() and viewToModel() translations stacks.
     */
    private static final Logger LOG_MODEL_VIEW = Logger.getLogger(DocumentView.class.getName() +
            "-ModelView"); // NOI18N

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView-Paint.level=FINE
    /**
     * Log paint() stacks.
     */
    private static final Logger LOG_PAINT = Logger.getLogger(DocumentView.class.getName() +
            "-Paint"); // NOI18N

    // True to log real source chars
    static final boolean LOG_SOURCE_TEXT = Boolean.getBoolean("org.netbeans.editor.log.source.text");

    static final char PRINTING_SPACE = '\u00B7';
    static final char PRINTING_TAB = '\u00BB'; // \u21FE
    static final char PRINTING_NEWLINE = '\u00B6';
    static final char LINE_CONTINUATION = '\u21A9';
    static final char LINE_CONTINUATION_ALTERNATE = '\u2190';

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
    private static final String START_POSITION_PROPERTY = "document-view-start-position";

    /**
     * Component's client property that contains swing position - end of document's area
     * to be displayed by the view.
     * Value of the property is only examined at time of view.setParent().
     */
    private static final String END_POSITION_PROPERTY = "document-view-end-position";

    /**
     * Component's client property that defines whether accurate width and height should be computed
     * by the view or whether the view can estimate its width and improve the estimated
     * upon rendering of the concrete region.
     * Value of the property is only examined at time of view.setParent().
     */
    private static final String ACCURATE_SPAN_PROPERTY = "document-view-accurate-span";


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

    private PriorityMutex pMutex;

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
     * Whether the "children" is currently reflecting the document state.
     * <br/>
     * The children may hold a semi-valid view hierarchy which may still be partly used
     * to resolve queries in some cases.
     */
    private boolean childrenValid;

    private Position startPos;

    private Position endPos;

    private float width;

    private float height;

    private boolean accurateSpan;

    /**
     * Visible width of the viewport or a text component if there is no viewport.
     */
    private int visibleWidth;

    private int visibleHeight;

    /**
     * Cached font render context (in order not to call getContainer().getGraphics() etc. each time).
     * It appears the FontRenderContext class is not extended (inspected SunGraphics2D)
     * so it should be safe and work fine.
     */
    private FontRenderContext fontRenderContext;

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

    private Color defaultLimitLine;

    private int defaultLimitLineWidth;
    
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
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                r.run();
            } finally {
                mutex.unlock();
            }
        } else { // If no mutex present run without mutex (not running at all would be more serious)
            r.run();
        }
    }
    
    /**
     * Rebuild views if there are any pending highlight factory changes reported.
     * The document must be read-locked prior calling this method.
     */
    public void syncViewsRebuild() {
        checkDocumentLockedIfLogging();
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (viewUpdates != null) {
                    viewUpdates.syncViewsRebuild();
                }
            } finally {
                mutex.unlock();
            }
        }
    }
    
    @Override
    public float getPreferredSpan(int axis) {
        // Since this may be called e.g. from BasicTextUI.getPreferredSize()
        // this method needs to acquire mutex
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                checkDocumentLockedIfLogging(); // Should only be called with read-locked document
                checkViewsInited();
                if (!childrenValid) {
                    return 0f; // Return zero until parent and etc. gets initialized
                }
                float span = super.getPreferredSpan(axis);
                if (axis == View.Y_AXIS) {
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
                mutex.unlock();
            }
        } else {
            return 1f;
        }
    }

    @Override
    protected void setMajorAxisSpan(double majorAxisSpan) {
        super.setMajorAxisSpan(majorAxisSpan);
    }

    @Override
    protected void setMinorAxisSpan(float minorAxisSpan) {
        super.setMinorAxisSpan(minorAxisSpan);
    }

    public PriorityMutex getMutex() {
        return pMutex;
    }

    @Override
    public Document getDocument() {
        return getElement().getDocument();
    }

    @Override
    public int getStartOffset() {
        return (startPos != null) ? startPos.getOffset() : super.getStartOffset();
    }
    
    @Override
    public int getEndOffset() {
        return (endPos != null) ? endPos.getOffset() : super.getEndOffset();
    }
    
    Position getEndPos() {
        return endPos;
    }

    @Override
    public void setLength(int length) {
        // Do nothing (no raw-offset maintenance)
    }

    @Override
    public int getMajorAxis() {
        return View.Y_AXIS;
    }

    @Override
    protected TabExpander getTabExpander() {
        return tabExpander;
    }

    @Override
    public int getRawOffset() {
        return 0;
    }

    @Override
    public void setRawOffset(int rawOffset) {
        throw new IllegalStateException("Unexpected"); // NOI18N
    }

    @Override
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Get current allocation of the document view by using size from last call
     * to {@link #setSize(float, float)}.
     *
     * @return current allocation of document view.
     */
    public Rectangle2D.Double getAllocation() {
        return new Rectangle2D.Double(0, 0, width, height);
    }

    public float getWidth() {
        return width;
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
            PriorityMutex mutex = getMutex();
            if (mutex != null) {
                mutex.lock();
                try {
                    super.setParent(parent);
                    textLayoutCache = new TextLayoutCache();
                    textComponent = tc;
                    viewHierarchy = ViewHierarchy.get(textComponent);
                    startPos = (Position) textComponent.getClientProperty(START_POSITION_PROPERTY);
                    endPos = (Position) textComponent.getClientProperty(END_POSITION_PROPERTY);
                    // If one position is non-null then make the other one non-null too
                    if (startPos != null) { 
                        if (endPos == null) {
                            endPos = getDocument().getEndPosition();
                            assert (endPos != null) : "Null position from doc.getEndPosition()"; // NOI18N
                        }
                    } else {
                        if (endPos != null) {
                            startPos = getDocument().getStartPosition();
                            assert (startPos != null) : "Null position from doc.getStartPosition()"; // NOI18N
                        }
                    }

                    accurateSpan = Boolean.TRUE.equals(textComponent.getClientProperty(ACCURATE_SPAN_PROPERTY));
                    viewUpdates = new ViewUpdates(this);
                    textComponent.addPropertyChangeListener(this);
                    if (REPAINT_LOG.isLoggable(Level.FINE)) {
                        DebugRepaintManager.register(textComponent);
                    }
                } finally {
                    mutex.unlock();
                }
            }

        } else { // Setting null parent
            // Set the textComponent to null under mutex
            // so that children suddenly don't see a null textComponent
            getDocument().render(new Runnable() {
                @Override
                public void run() {
                    PriorityMutex mutex = getMutex();
                    if (mutex != null) {
                        mutex.lock();
                        try {
                            if (textComponent != null) {
                                if (listeningOnViewport != null) {
                                    listeningOnViewport.removeChangeListener(DocumentView.this);
                                }
                                textComponent.removePropertyChangeListener(DocumentView.this);
                                textLayoutCache = null;
                                viewUpdates = null;
                                textComponent = null; // View services stop working and propagating to children
                            }
                            DocumentView.super.setParent(null);
                        } finally {
                            mutex.unlock();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void preferenceChanged(View childView, boolean width, boolean height) {
        super.preferenceChanged(childView, width, height);
        if (childView == null) { // Track component resizes
            if (LOG.isLoggable(Level.FINER)) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.INFO, "Cause of DocumentView.preferenceChanged()", new Exception()); // NOI18N
                }
                float prefWidth = getPreferredSpan(View.X_AXIS);
                float prefHeight = getPreferredSpan(View.Y_AXIS);
                String changed = (width ? "T" : "F") + "x" + (height ? "T" : "F"); // NOI18N
                LOG.finer("DocumentView-preferenceChanged: WxH[" + changed + "]:" + // NOI18N
                        prefWidth + "x" + prefHeight + '\n'); // NOI18N
            }
        }
    }

    void checkViewsInited() { // Must be called under mutex
        if (!childrenValid && textComponent != null) {
            updateVisibleDimension();
            checkSettingsInfo();
            checkFontRenderContext();
            ((EditorTabExpander) tabExpander).updateTabSize();
            if (isBuildable()) {
                LOG.fine("viewUpdates.reinitViews()\n");
                // Signal early that the views will be valid - otherwise preferenceChange()
                // that calls getPreferredSpan() would attempt to reinit the views again
                // (failing in HighlightsViewFactory on usageCount).
                childrenValid = true;
                viewUpdates.reinitViews();
                // Re-check since addFont(font) caused by views creation might make childrenValid = false.
                if (!childrenValid) {
                    childrenValid = true;
                    viewUpdates.reinitViews();
                }
            }
        }
    }
    
    private void checkFontRenderContext() { // check various things related to rendering
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
                    updateCharMetrics(); // Explicitly update char metrics since fontRenderContext affects them
                }
            }
        }
    }
    
    @Override
    protected void releaseChildren() { // It should be called with acquired mutex
        // Do not set children == null like in super.releaseChildren()
        // Instead mark them as invalid but allow to use them in certain limited cases
        childrenValid = false;
    }
    
    public void releaseChildrenLocked(final boolean updateFonts) { // It acquires document readlock and VH mutex first
        getDocument().render(new Runnable() {
            @Override
            public void run() {
                PriorityMutex mutex = getMutex();
                if (mutex != null) {
                    mutex.lock();
                    // checkDocumentLocked() - unnecessary - doc.render() called
                    try {
                        if (updateFonts) {
                            updateDefaultFontAndColors(null); // Includes releaseChildren()
                        } else {
                            releaseChildren();
                        }
                    } finally {
                        mutex.unlock();
                    }
                }
            }
        });
    }

    @Override
    protected void initChildren(int startIndex, int endIndex) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("DocumentView.initChildren(): <" + startIndex + "," + endIndex + ">\n");
        }
        viewUpdates.initChildren(startIndex, endIndex);
    }

    void recomputeLayout() {
        checkDocumentLockedIfLogging();
        int viewCount = getViewCount();
        boolean heightChange = false;
        float origWidth = getMinorAxisSpan();
        float newWidth = 0f;
        for (int i = 0; i < viewCount; i++) {
            ParagraphView paragraphView = getEditorView(i);
            double origChildWidth = paragraphView.getMajorAxisSpan();
            float origChildHeight = paragraphView.getMinorAxisSpan();
            paragraphView.recomputeLayout();
            double childWidth = paragraphView.getMajorAxisSpan();
            boolean childWidthChange = (origChildWidth != childWidth);
            if (childWidth > newWidth) {
                newWidth = (float) childWidth;
            }
            boolean childHeightChange = (origChildHeight != paragraphView.getMinorAxisSpan());
            heightChange |= childHeightChange;
            // Call preference change so that child's major axis span gets updated
            preferenceChanged(i, childWidthChange, childHeightChange, false);
        }
        boolean widthChange = (origWidth != newWidth);
        if (widthChange) {
            setMinorAxisSpan(newWidth);
        }
        if (widthChange || heightChange) {
            preferenceChanged(null, widthChange, heightChange);
        }
        viewHierarchy.fireViewHierarchyEvent(new ViewHierarchyEvent(viewHierarchy, getStartOffset()));
    }

    /**
     * Whether the view should compute accurate spans (no lazy children views computation).
     * This is handy e.g. for fold preview computation.
     *
     * @return whether accurate span measurements should be performed.
     */
    boolean isAccurateSpan() {
        return accurateSpan;
    }

    private void updateVisibleDimension() { // Called only with textComponent != null
        // Must be called under mutex
        Component parent = textComponent.getParent();
        Dimension newSize;
        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport) parent;
            if (listeningOnViewport != viewport) {
                if (listeningOnViewport != null) {
                    listeningOnViewport.removeChangeListener(this);
                }
                viewport.addChangeListener(this);
                listeningOnViewport = viewport;
            }
            newSize = viewport.getExtentSize();

        } else {
            newSize = textComponent.getSize();
        }

        boolean widthDiffers = (newSize.width != visibleWidth);
        boolean heightDiffers = (newSize.height != visibleHeight);
        if (widthDiffers) {
            visibleWidth = newSize.width;
        }
        if (heightDiffers) {
            visibleHeight = newSize.height;
        }
        if (widthDiffers) {
            recomputeLayout();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // First lock document and then monitor
        Document doc = getDocument();
        doc.render(new Runnable() {
            @Override
            public void run() {
                PriorityMutex mutex = getMutex();
                if (mutex != null) {
                    mutex.lock();
                    // checkDocumentLocked() - unnecessary - doc.render() called
                    try {
                        if (textComponent != null) {
                            updateVisibleDimension();
                        }
                    } finally {
                        mutex.unlock();
                    }
                }
            }
        });
    }

    private void checkSettingsInfo() {
        JTextComponent tc = textComponent;
        if (tc == null) {
            return;
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
                            getDocument().render(new Runnable() {
                                @Override
                                public void run() {
                                    PriorityMutex mutex = getMutex();
                                    if (mutex != null) {
                                        mutex.lock();
                                        // checkDocumentLocked() - unnecessary - doc.render() called
                                        try {
                                            if (textComponent != null) {
                                                updateDefaultFontAndColors(result);
                                            }
                                        } finally {
                                            mutex.unlock();
                                        }
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
            updateDefaultFontAndColors(result);

            result.addLookupListener(WeakListeners.create(LookupListener.class,
                    lookupListener, result));
        }

        if (prefs == null) {
            String mimeType = DocumentUtilities.getMimeType(tc);
            prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            prefsListener = new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    String key = evt.getKey();
                    if (key ==  null || key.equals(SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE)) {
                        releaseChildrenLocked(false);
                    }
                }
            };
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs));
        }

        if (lineWrapType == null) {
            updateLineWrapType();

            Document doc = getDocument();
            // #183797 - most likely seeing a non-nb document during the editor pane creation
            Integer dllw = (Integer) doc.getProperty(SimpleValueNames.TEXT_LIMIT_WIDTH);
            defaultLimitLineWidth = dllw != null ? dllw.intValue() : EditorPreferencesDefaults.defaultTextLimitWidth;

            DocumentUtilities.addPropertyChangeListener(doc, WeakListeners.propertyChange(this, doc));
        }
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
    }

    /*private*/ void updateDefaultFontAndColors(Lookup.Result<FontColorSettings> result) {
        // This should be called with mutex acquired
        // Called only with textComponent != null
        Font font = textComponent.getFont();
        Color foreColor = textComponent.getForeground();
        Color backColor = textComponent.getBackground();
        Color limitLineColor = Color.PINK;
        if (result != null) {
            FontColorSettings fcs = result.allInstances().iterator().next();
            AttributeSet attributes = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            if (attributes != null) {
                font = ViewUtils.getFont(attributes, new Font(font.getFamily(), 0, font.getSize()));
                Color c = (Color) attributes.getAttribute(StyleConstants.Foreground);
                if (c != null) {
                    foreColor = c;
                }
                c = (Color) attributes.getAttribute(StyleConstants.Background);
                if (c != null) {
                    backColor = c;
                }
                renderingHints = (Map<?, ?>) attributes.getAttribute(EditorStyleConstants.RenderingHints);
            }
            attributes = fcs.getFontColors(FontColorNames.TEXT_LIMIT_LINE_COLORING);
            if (attributes != null) {
                Color c = (Color) attributes.getAttribute(StyleConstants.Foreground);
                if (c != null) {
                    limitLineColor = c;
                }
            }
        }

        defaultFont = font;
        defaultForeground = foreColor;
        defaultBackground = backColor;
        defaultLimitLine = limitLineColor;

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

            releaseChildren();
        }
            
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(getDumpId() + ": Updated DEFAULTS: font=" + defaultFont + // NOI18N
                    ", fg=" + ViewUtils.toString(defaultForeground) + // NOI18N
                    ", bg=" + ViewUtils.toString(defaultBackground) + '\n'); // NOI18N
        }
    }

    private void updateCharMetrics() { // Update default line height and other params
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
            releaseChildren();
        }
    }

    @Override
    public String getToolTipTextChecked(double x, double y, Shape allocation) {
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    return children.getToolTipTextChecked(this, x, y, allocation);
                }
            } finally {
                mutex.unlock();
            }
        }
        return null;
    }

    @Override
    public JComponent getToolTip(double x, double y, Shape allocation) {
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    return children.getToolTip(this, x, y, allocation);
                }
            } finally {
                mutex.unlock();
            }
        }
        return null;
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (LOG_PAINT.isLoggable(Level.FINE)) {
                    LOG_PAINT.log(Level.FINE, "ViewHierarchy paint: clip=" + clipBounds + // NOI18N
                            "\n", new Exception()); // NOI18N
                }
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    // Use rendering hints (antialiasing etc.)
                    if (g != null && renderingHints != null) {
                        g.setRenderingHints(renderingHints);
                    }
                    super.paint(g, alloc, clipBounds);
                }
            } finally {
                mutex.unlock();
            }
        }
    }
    
    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Bias bias) {
        Rectangle2D.Double rect = ViewUtils.shape2Bounds(alloc);
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (LOG_MODEL_VIEW.isLoggable(Level.FINE)) {
                    LOG_MODEL_VIEW.log(Level.FINE, "ViewHierarchy modelToView: offset=" + offset + // NOI18N
                            "\n", new Exception()); // NOI18N
                }
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (LOG.isLoggable(Level.FINER)) {
                    String msg = "DocumentView.modelToViewChecked(): offset=" + offset + "\n"; // NOI18N
                    LOG.finer(msg); // NOI18N
//                    LOG.log(Level.INFO, "Cause of " + msg, new Exception()); // NOI18N
                }
                if (isActive()) {
                    return super.modelToViewChecked(offset, alloc, bias);
                } else if (children != null) {
                    // Not active but attempt to find at least a reasonable y
                    // The existing line views may not be updated for a longer time
                    // but the binary search should find something and end in finite time.
                    int index = getViewIndexFirst(offset); // Must work without children inited
                    if (index >= 0) {
                        rect.y = getViewVisualOffset(index); // Must work without children inited
                        // Let the height to possibly be set to default line height later
                    }
                }
            } finally {
                mutex.unlock();
            }
        }
        // Attempt to just return height of line since otherwise e.g. caret
        // would have height of the whole doc which is undesirable.
        if (defaultLineHeight > 0f) {
            rect.height = defaultLineHeight;
        }
        return rect;
    }

    public double modelToY(int offset, Shape alloc) {
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (LOG_MODEL_VIEW.isLoggable(Level.FINE)) {
                    LOG_MODEL_VIEW.log(Level.FINE, "ViewHierarchy modelToY: offset=" + offset + // NOI18N
                            "\n", new Exception()); // NOI18N
                }
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    int index = getViewIndexFirst(offset);
                    if (index >= 0) {
                        return getViewVisualOffset(index);
                    }
                }
            } finally {
                mutex.unlock();
            }
        }
        return 0.0d;
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Bias[] biasReturn) {
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (LOG_MODEL_VIEW.isLoggable(Level.FINE)) {
                    LOG_MODEL_VIEW.log(Level.FINE, "ViewHierarchy viewToModel: [x,y]=" + x + "," + y + // NOI18N
                            "\n", new Exception()); // NOI18N
                }
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (LOG.isLoggable(Level.FINER)) {
                    String msg = "DocumentView.viewToModelChecked(): x=" + x + ", y=" + y + "\n"; // NOI18N
                    LOG.finer(msg);
//                    LOG.log(Level.INFO, "Cause of " + msg, new Exception()); // NOI18N
                }
                if (isActive()) {
                    return super.viewToModelChecked(x, y, alloc, biasReturn);
                }
            } finally {
                mutex.unlock();
            }
        }
        return 0;
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Bias bias, Shape alloc,
            int direction, Bias[] biasRet)
    {
        PriorityMutex mutex = getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (LOG_MODEL_VIEW.isLoggable(Level.FINE)) {
                    LOG_MODEL_VIEW.log(Level.FINE, "ViewHierarchy nextVisualPosition: offset=" + offset + // NOI18N
                            "\n", new Exception()); // NOI18N
                }
                checkDocumentLockedIfLogging();
                checkViewsInited();
                if (isActive()) {
                    int retOffset;
                    switch (direction) {
                        case SwingConstants.EAST:
                        case SwingConstants.WEST:
                            retOffset = getNextVisualPositionX(offset, bias, alloc, direction, biasRet);
                            break;
                        case SwingConstants.NORTH:
                        case SwingConstants.SOUTH:
                            retOffset = getNextVisualPositionY(offset, bias, alloc, direction, biasRet);
                            break;
                        default:
                            throw new IllegalArgumentException("Bad direction " + direction); // NOI18N
                    }
                    // Since BaseCaret does not handle bias translate backward bias to prev-pos and forward bias
                    if (retOffset > 0 && biasRet[0] == Bias.Backward) {
                        retOffset--;
                        biasRet[0] = Bias.Forward;
                    }
                    if (retOffset == -1) { // Return original offset
                        retOffset = offset;
                    }
                    offset = retOffset;
                }
            } finally {
                mutex.unlock();
            }
        }
        return offset;
    }
    
    private int getNextVisualPositionY(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        // NORTH or SOUTH
        assert (direction == SwingConstants.NORTH || direction == SwingConstants.SOUTH) : "Invalid direction " + direction;
        if (textComponent == null) {
            return -1;
        }
        Caret caret = textComponent.getCaret();
        Point magicCaretPoint = (caret != null) ? caret.getMagicCaretPosition() : null;
        double x;
        if (magicCaretPoint == null) {
            Shape offsetBounds = modelToViewChecked(offset, alloc, bias);
            if (offsetBounds == null) {
                x = 0d;
            } else {
                x = offsetBounds.getBounds2D().getX();
            }
        } else {
            x = magicCaretPoint.x;
        }
        int pIndex = getViewIndex(offset, bias);
        int viewCount = getViewCount(); // Should always be >0
        int increment = (direction == SwingConstants.SOUTH) ? 1 : -1;
        int retOffset = -1;
        for (; retOffset == -1 && pIndex >= 0 && pIndex < viewCount; pIndex += increment) {
            ParagraphView pView = getEditorViewChildrenValid(pIndex); // Ensure valid children
            Shape pAlloc = getChildAllocation(pIndex, alloc);
            retOffset = pView.getNextVisualPositionY(offset, bias, pAlloc, direction, biasRet, x);
            if (retOffset == -1) {
                offset = -1; // Continue by entering the paragraph from outside
            }
        }
        return retOffset;
    }

    private int getNextVisualPositionX(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        // EAST or WEST
        assert (direction == SwingConstants.EAST || direction == SwingConstants.WEST) : "Invalid direction " + direction;
        int pIndex = getViewIndex(offset);
        int viewCount = getViewCount(); // Should always be >0
        int increment = (direction == SwingConstants.EAST) ? 1 : -1;
        int retOffset = -1;
        for (; retOffset == -1 && pIndex >= 0 && pIndex < viewCount; pIndex += increment) {
            ParagraphView pView = getEditorViewChildrenValid(pIndex); // Ensure valid children
            Shape pAlloc = getChildAllocation(pIndex, alloc);
            retOffset = pView.getNextVisualPositionFromChecked(offset, bias, pAlloc, direction, biasRet);
            if (retOffset == -1) {
                offset = -1; // Continue by entering the paragraph from outside
            }
        }
        return retOffset;
    }

    @Override
    public View getView(int index) {
        if (LOG.isLoggable(Level.FINE)) {
            checkDocumentLockedIfLogging();
            checkMutexAcquiredIfLogging();
        }
        return super.getView(index);
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
        LOG.log(Level.FINE, "updateLengthyAtomicEdit: delta={0} lengthyAtomicEdit={1}\n", // NOI18N
                new Object[] { delta, lengthyAtomicEdit} );
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.INFO, "updateLengthyAtomicEdit() stack", new Exception("updateLengthyAtomicEdit()")); // NOI18N
        }
        if (lengthyAtomicEdit == 0) {
            releaseChildrenLocked(false);
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

    float getVisibleWidth() {
        return visibleWidth;
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

    public boolean isShowNonprintingCharacters() {
        checkSettingsInfo();
        return prefs.getBoolean(SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE, false);
    }

    LineWrapType getLineWrapType() {
        checkSettingsInfo();
        return lineWrapType;
    }

    Color getTextLimitLineColor() {
        checkSettingsInfo();
        return defaultLimitLine;
    }

    boolean isTextLimitLineDrawn() {
        checkSettingsInfo();
        return prefs.getBoolean(SimpleValueNames.TEXT_LIMIT_LINE_VISIBLE, true);
    }

    int getTextLimitWidth() {
        checkSettingsInfo();
        if (defaultLimitLineWidth > 0) {
            return defaultLimitLineWidth;
        }
        return prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80);
    }

    TextLayout getNewlineCharTextLayout() {
        if (newlineTextLayout == null) {
            newlineTextLayout = createTextLayout(String.valueOf(PRINTING_NEWLINE), defaultFont);
        }
        return newlineTextLayout;
    }

    TextLayout getTabCharTextLayout(double availableWidth) {
        if (tabTextLayout == null) {
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
        if (lineContinuationTextLayout == null) {
            char lineContinuationChar = LINE_CONTINUATION;
            if (!defaultFont.canDisplay(lineContinuationChar)) {
                lineContinuationChar = LINE_CONTINUATION_ALTERNATE;
            }
            lineContinuationTextLayout = createTextLayout(String.valueOf(lineContinuationChar), defaultFont);
        }
        return lineContinuationTextLayout;
    }

    TextLayout createTextLayout(String text, Font font) {
        checkSettingsInfo();
        if (fontRenderContext != null && font != null) {
//            LOG.log(Level.INFO, "New TextLayout for text=" + text, new Exception());
            ViewStats.incrementTextLayoutCreated(text.length());
            return new TextLayout(text, font, fontRenderContext);
        }
        return null;
    }
    
    TextLayout createTextLayout(String text, AttributeSet attrs) {
        Font font = ViewUtils.getFont(attrs, defaultFont);
        return createTextLayout(text, font);
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
            checkMutexAcquired();
        }
    }

    void checkMutexAcquired() {
        PriorityMutex mutex = getMutex();
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

    boolean isMutexAcquired() {
        PriorityMutex mutex = getMutex();
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
                int llw = (Integer) getDocument().getProperty(SimpleValueNames.TEXT_LIMIT_WIDTH);
                if (llw != defaultLimitLineWidth) {
                    LOG.log(Level.FINE, "Changing defaultLimitLineWidth from {0} to {1}", new Object [] { defaultLimitLineWidth, llw }); //NOI18N
                    defaultLimitLineWidth = llw;
                    releaseChildren = true;
                }
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
            }
        }
        if (releaseChildren) {
            releaseChildrenLocked(updateFonts);
        }
    }
    
//    private Position createPos(int offset) {
//        try {
//            return getDocument().createPosition(offset);
//        } catch (BadLocationException ex) {
//            throw new IllegalStateException("Invalid offset=" + offset + " in doc: " + getDocument(), ex);
//        }
//    }

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
            EditorView firstView = getEditorView(0);
            if (firstView.getStartOffset() != startOffset) {
                return "firstView.getStartOffset()=" + firstView.getStartOffset() + // NOI18N
                        " != startOffset=" + startOffset; // NOI18N
            }
            EditorView lastView = getEditorView(viewCount - 1);
            if (lastView.getEndOffset() != endOffset) {
                return "lastView.endOffset=" + lastView.getEndOffset() + " != endOffset=" + endOffset; // NOI18N
            }
        }
        return null;
    }

    @Override
    public String findTreeIntegrityError() {
        final String[] ret = new String[1];
        getDocument().render(new Runnable() {
            @Override
            public void run() {
                PriorityMutex mutex = getMutex();
                if (mutex != null) {
                    mutex.lock();
                    // checkDocumentLocked() - unnecessary - doc.render() called
                    try {
                        ret[0] = DocumentView.super.findTreeIntegrityError();
                    } finally {
                        mutex.unlock();
                    }
                }
            }
        });
        return ret[0];
    }

    @Override
    protected StringBuilder appendViewInfoCore(StringBuilder sb, int indent, int importantChildIndex) {
        super.appendViewInfoCore(sb, indent, importantChildIndex);
        sb.append("; incomingMod=").append(incomingModification);
        sb.append("; lengthyAtomicEdit=").append(lengthyAtomicEdit);
        if (LOG_SOURCE_TEXT) {
            Document doc = getDocument();
            sb.append("\nDoc: ").append(ViewUtils.toString(doc));
        }
        return sb;
    }

    @Override
    protected StringBuilder appendViewInfo(final StringBuilder sb, final int indent, final int importantChildIndex) {
        getDocument().render(new Runnable() {
            @Override
            public void run() {
                PriorityMutex mutex = getMutex();
                if (mutex != null) {
                    mutex.lock();
                    // checkDocumentLocked() - unnecessary - doc.render() called
                    try {
                        DocumentView.super.appendViewInfo(sb, indent, importantChildIndex);
                    } finally {
                        mutex.unlock();
                    }
                }
            }
        });
        return sb;
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
            underlineAndStrike[0] = ViewUtils.floorFractions(lineMetrics.getUnderlineOffset());
            underlineAndStrike[1] = lineMetrics.getUnderlineThickness();
            underlineAndStrike[2] = ViewUtils.floorFractions(lineMetrics.getStrikethroughOffset());
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
