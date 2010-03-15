/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.TextUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabExpander;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
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
public final class DocumentView extends EditorBoxView
        implements PropertyChangeListener, ChangeListener
{

    // -J-Dorg.netbeans.modules.editor.lib2.view.DocumentView.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentView.class.getName());

    private static final Logger REPAINT_LOG = Logger.getLogger(DebugRepaintManager.class.getName());

    static final char PRINTING_SPACE = '\u00B7';
    static final char PRINTING_TAB = '\u00BB'; // \u21FE
    static final char PRINTING_NEWLINE = '\u00B6';
    static final char LINE_CONTINUATION = '\u21A9';

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

    private final Object monitor = new String("DocumentView-Monitor"); //NOI18N

    private JTextComponent textComponent;

    /**
     * Maintenance of view updates.
     * If this is a preview-only view e.g. for a collapsed fold preview
     * when located over collapsed fold's tooltip.
     */
    private ViewUpdates viewUpdates;

    private TextLayoutCache textLayoutCache;

    private float width;

    private float height;

    /**
     * Visible width of the viewport.
     */
    private float visibleWidth;

    /**
     * Cached font render context (in order not to call getContainer().getGraphics() etc. each time).
     * It appears the FontRenderContext class is not extended (inspected SunGraphics2D)
     * so it should be safe and work fine.
     */
    private FontRenderContext fontRenderContext;

    private Font defaultFont;

    private boolean customFont;

    /**
     * Default line height computed as height of the defaultFont.
     */
    private float defaultLineHeight;

    private float defaultBaselineOffset;

    private float defaultUnderlineOffset;

    private float defaultCharWidth;

    private Color defaultForeground;

    private boolean customForeground;

    private Color defaultBackground;

    private boolean customBackground;

    private LineWrapType lineWrapType;

    private TextLayout newlineTextLayout;

    private TextLayout tabTextLayout;

    private TextLayout singleCharTabTextLayout;

    private TextLayout lineContinuationTextLayout;

    private TabExpander tabExpander;

    private LookupListener lookupListener;

    private JViewport listeningOnViewport;

    private boolean previewOnly;

    private Preferences prefs;

    private PreferenceChangeListener prefsListener;

    public DocumentView(Element elem, boolean previewOnly) {
        super(elem);
        assert (elem != null) : "Expecting non-null element"; // NOI18N
        this.previewOnly = previewOnly;
        this.tabExpander = new EditorTabExpander(this);
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (lineWrapType == null) {
            return 0f; // Return zero until parent and etc. gets initialized
        }
        return super.getPreferredSpan(axis);
    }

    @Override
    protected void setMajorAxisSpan(double majorAxisSpan) {
        super.setMajorAxisSpan(majorAxisSpan);
    }

    @Override
    protected void setMinorAxisSpan(float minorAxisSpan) {
        super.setMinorAxisSpan(minorAxisSpan);
    }

    public Object getMonitor() {
        return monitor;
    }

    @Override
    public Document getDocument() {
        return getElement().getDocument();
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
        super.setParent(parent);
        if (parent != null) {
            Container container = getContainer();
            assert (container != null) : "Container is null"; // NOI18N
            assert (container instanceof JTextComponent) : "Container not JTextComponent"; // NOI18N
            textComponent = (JTextComponent) container;
            viewUpdates = previewOnly ? null : new ViewUpdates(this);
            textLayoutCache = new TextLayoutCache();

            checkViewsInited();
            textComponent.addPropertyChangeListener(this);
            if (REPAINT_LOG.isLoggable(Level.FINE)) {
                DebugRepaintManager.register(textComponent);
            }

        } else { // Setting null parent
            textComponent.removePropertyChangeListener(this);
        }
    }

    void checkViewsInited() {
        if (children == null) {
            // Check whether Graphics can be constructed (only if component is part of hierarchy)
            assert (textComponent != null);
            Graphics graphics = textComponent.getGraphics();
            if (graphics != null) {
                assert (graphics instanceof Graphics2D) : "Not Graphics2D";
                fontRenderContext = ((Graphics2D) graphics).getFontRenderContext();
                updateVisibleWidth();
                checkSettingsInfo();
                updateCharMetrics(); // Explicitly update char metrics since fontRenderContext affects them
                Document doc = getDocument();
                reinitViews();
            }
        }
    }

    public void reinitViews() {
        synchronized (getMonitor()) {
            viewUpdates.reinitViews();
        }
    }

    void recomputeSpans() {
        int viewCount = getViewCount();
        boolean heightChange = false;
        float origWidth = getMinorAxisSpan();
        float newWidth = 0f;
        for (int i = 0; i < viewCount; i++) {
            ParagraphView paragraphView = (ParagraphView) getEditorView(i);
            double origChildWidth = paragraphView.getMajorAxisSpan();
            float origChildHeight = paragraphView.getMinorAxisSpan();
            paragraphView.recomputeSpans();
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
    }

    private void updateVisibleWidth() {
        Component parent = textComponent.getParent();
        float newWidth;
        if (parent instanceof JViewport) {
            JViewport viewport = (JViewport) parent;
            if (listeningOnViewport != viewport) {
                if (listeningOnViewport != null) {
                    listeningOnViewport.removeChangeListener(this);
                }
                viewport.addChangeListener(this);
                listeningOnViewport = viewport;
            }
            newWidth = viewport.getExtentSize().width;
        } else {
            newWidth = textComponent.getWidth();
        }

        if (newWidth != visibleWidth) {
            visibleWidth = newWidth;
            recomputeSpans();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        synchronized (getMonitor()) {
            updateVisibleWidth();
        }
    }

    private void checkSettingsInfo() {
        if (lookupListener == null) {
            lookupListener = new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    @SuppressWarnings("unchecked")
                    Lookup.Result<FontColorSettings> result = (Lookup.Result<FontColorSettings>) ev.getSource();
                    updateDefaultFontAndColors(result);
                }
            };
            String mimeType = DocumentUtilities.getMimeType(textComponent);
            Lookup lookup = MimeLookup.getLookup(mimeType);
            Lookup.Result<FontColorSettings> result = lookup.lookupResult(FontColorSettings.class);
            updateDefaultFontAndColors(result);
            result.addLookupListener(lookupListener);
        }

        if (prefs == null) {
            String mimeType = DocumentUtilities.getMimeType(textComponent);
            prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            prefsListener = new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    if (evt.getKey().equals(SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE)) {
                        synchronized (getMonitor()) {
                            reinitViews();
                        }
                    }
                }
            };
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsListener, prefs));
        }

        if (lineWrapType == null) {
            Document doc = getDocument();
            lineWrapType = LineWrapType.fromSettingValue((String) doc.getProperty(SimpleValueNames.TEXT_LINE_WRAP));
            if (lineWrapType == null) {
                lineWrapType = LineWrapType.NONE;
            }
            DocumentUtilities.addPropertyChangeListener(doc, WeakListeners.propertyChange(this, doc));
        }
    }

    /*private*/ void updateDefaultFontAndColors(Lookup.Result<FontColorSettings> result) {
        Font font = textComponent.getFont();
        Color foreColor = textComponent.getForeground();
        Color backColor = textComponent.getBackground();
        if (result != null) {
            FontColorSettings fcs = result.allInstances().iterator().next();
            AttributeSet attributes = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
            if (attributes != null) {
                font = ViewUtils.getFont(attributes, font);
                Color c = (Color) attributes.getAttribute(StyleConstants.Foreground);
                if (c != null) {
                    foreColor = c;
                }
                c = (Color) attributes.getAttribute(StyleConstants.Background);
                if (c != null) {
                    backColor = c;
                }
            }
        }

        defaultFont = font;
        defaultForeground = foreColor;
        defaultBackground = backColor;

        if (!customFont) {
            textComponent.setFont(defaultFont);
        }
        if (!customForeground) {
            textComponent.setForeground(defaultForeground);
        }
        if (!customBackground) {
            textComponent.setBackground(defaultBackground);
        }

        updateCharMetrics(); // Update metrics with just updated font

        reinitViews();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(getDumpId() + ": Updated DEFAULTS: font=" + defaultFont + // NOI18N
                    ", fg=" + ViewUtils.toString(defaultForeground) + // NOI18N
                    ", bg=" + ViewUtils.toString(defaultBackground)); // NOI18N
        }
    }

    private void updateCharMetrics() {
        FontRenderContext frc = getFontRenderContext();
        assert (defaultFont != null) : "Null defaultFont"; // NOI18N
        if (frc != null) {
            char defaultChar = 'A';
            String defaultText = String.valueOf(defaultChar);
            TextLayout textLayout = new TextLayout(defaultText, defaultFont, frc); // NOI18N
            defaultLineHeight = textLayout.getAscent() + textLayout.getDescent() +
                    textLayout.getLeading(); // Leading: end of descent till next line's start distance
            defaultBaselineOffset = textLayout.getAscent(); // textLayout.getBaselineOffsets()[textLayout.getBaseline()];
            LineMetrics lineMetrics = defaultFont.getLineMetrics(defaultText, frc);
            defaultUnderlineOffset = lineMetrics.getUnderlineOffset();
            defaultCharWidth = (float) textLayout.getBounds().getWidth();
            tabTextLayout = null;
            singleCharTabTextLayout = null;
            lineContinuationTextLayout = null;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Font: " + defaultFont + "\nline-height=" + defaultLineHeight + // NOI18N
                        ", ascent=" + textLayout.getAscent() + ", descent=" + textLayout.getDescent() + // NOI18N
                        ", leading=" + textLayout.getLeading() + "\nchar-width=" + defaultCharWidth + // NOI18N
                        ", underlineOffset=" + defaultUnderlineOffset + // NOI18N
                        ", font-metrics-height=" + textComponent.getFontMetrics(defaultFont).getHeight()); // NOI18N
            }
        }
    }

    @Override
    public void paint(Graphics2D g, Shape alloc, Rectangle clipBounds) {
        synchronized (getMonitor()) {
            super.paint(g, alloc, clipBounds);
        }
    }

    @Override
    public int getNextVisualPositionFromChecked(int offset, Position.Bias bias, Shape alloc,
            int direction, Position.Bias[] biasRet)
    {
        synchronized (getMonitor()) {
            return super.getNextVisualPositionFromChecked(offset, bias, alloc, direction, biasRet);
        }
    }

    @Override
    public Shape modelToViewChecked(int offset, Shape alloc, Position.Bias bias) {
        synchronized (getMonitor()) {
            return super.modelToViewChecked(offset, alloc, bias);
        }
    }

    @Override
    public int viewToModelChecked(double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        synchronized (getMonitor()) {
            return super.viewToModelChecked(x, y, alloc, biasReturn);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        synchronized (getMonitor()) {
            if (children != null && viewUpdates != null) {
                viewUpdates.insertUpdate(evt, alloc, viewFactory);
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        synchronized (getMonitor()) {
            if (children != null && viewUpdates != null) {
                viewUpdates.removeUpdate(evt, alloc, viewFactory);
            }
        }
    }

    @Override
    public void changedUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        synchronized (getMonitor()) {
            if (children != null && viewUpdates != null) {
                viewUpdates.changedUpdate(evt, alloc, viewFactory);
            }
        }
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

    FontRenderContext getFontRenderContext() { // sync necessary??
        return fontRenderContext;
    }

    public float getDefaultLineHeight() {
        checkSettingsInfo();
        return defaultLineHeight;
    }

    public float getDefaultBaselineOffset() {
        checkSettingsInfo();
        return defaultBaselineOffset;
    }

    public float getDefaultUnderlineOffset() {
        checkSettingsInfo();
        return defaultUnderlineOffset;
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
            lineContinuationTextLayout = createTextLayout(String.valueOf(LINE_CONTINUATION), defaultFont);
        }
        return lineContinuationTextLayout;
    }

    private TextLayout createTextLayout(String text, Font font) {
        checkSettingsInfo();
        if (fontRenderContext != null && font != null) {
            return new TextLayout(text, font, fontRenderContext);
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Document) {
            String propName = evt.getPropertyName();
            if (propName == null || SimpleValueNames.TEXT_LINE_WRAP.equals(propName)) {
                LineWrapType lwt = LineWrapType.fromSettingValue((String)getDocument().getProperty(SimpleValueNames.TEXT_LINE_WRAP));
                if (lwt == null) {
                    lwt = LineWrapType.NONE;
                }
                if (lwt != lineWrapType) {
                    LOG.log(Level.FINE, "Changing lineWrapType from {0} to {1}", new Object [] { lineWrapType, lwt }); //NOI18N
                    lineWrapType = lwt;
                    synchronized (getMonitor()) {
                        reinitViews();
                    }
                }
            }
        } else { // an event from JTextComponent
            String propName = evt.getPropertyName();
            if ("ancestor".equals(propName)) { // NOI18N
                checkViewsInited();
            } else if ("font".equals(propName)) {
                if (!customFont && defaultFont != null) {
                    customFont = !defaultFont.equals(textComponent.getFont());
                }
            } else if ("foreground".equals(propName)) { //NOI18N
                if (!customForeground && defaultForeground != null) {
                    customForeground = !defaultForeground.equals(textComponent.getForeground());
                }
            } else if ("background".equals(propName)) { //NOI18N
                if (!customBackground && defaultBackground != null) {
                    customBackground = !defaultBackground.equals(textComponent.getBackground());
                }
            }
        }
    }

    @Override
    protected String getDumpName() {
        return "DV";
    }

    @Override
    public String findIntegrityError() {
        int startOffset = getStartOffset();
        if (startOffset != 0) {
            return "Invalid startOffset=" + startOffset; // NOI18N
        }
        int endOffset = getEndOffset();
        Document doc = getDocument();
        if (endOffset != doc.getLength() + 1) {
            return "Invalid endOffset=" + endOffset + ", docLen=" + doc.getLength(); // NOI18N
        }
        return null;
    }

}
