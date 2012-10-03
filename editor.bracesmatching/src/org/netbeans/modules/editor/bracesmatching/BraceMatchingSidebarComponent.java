/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bracesmatching;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.view.ViewHierarchy;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyEvent;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyListener;
import org.openide.text.NbDocument;
import org.openide.text.NbDocument.CustomEditor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;

/**
 *
 * @author sdedic
 */
public class BraceMatchingSidebarComponent extends JComponent implements 
        MatchListener, FocusListener, ViewHierarchyListener, ChangeListener {
    
    private static final int TOOLTIP_CHECK_DELAY = 700;
    
    public static final String BRACES_COLORING = "nbeditor-bracesMatching-sidebar"; // NOI18N

    /**
     * Client property of the JEditorPane, which orders this Component to show certain brace matches.
     * The value is 2-item array of int[]; first of which contains "origins" and the second item contains
     * "matches" as returned from the brace matching algorithm.
     */
    private static final String MATCHED_BRACES = "showMatchedBrace"; // NOI18N
            
    /**
     * The editor component
     */
    private final JTextComponent editor;
    
    /**
     * The editor pane encapsulating the text component
     */
    private final JEditorPane editorPane;
    
    /**
     * MIME type of the edited file.
     */
    private final String mimeType;
    
    /**
     * TextUI of the editor
     */
    private final BaseTextUI baseUI;

    /**
     * Line width for the outline, default 2.
     */
    private int lineWidth = 2;
    
    /**
     * Blank margin left of the outline
     */
    private int leftMargin = 1;
    
    /**
     * Width of the sidebar. Computed in {@link #updatePreferredSize() 
     */
    private int barWidth;

    /**
     * Origin + matches from the last highlight event.
     */
    private int[]   origin;
    private int[]   matches;
    
    private int     lineHeight;
    
    private boolean showOutline;
    
    private boolean showToolTip;
    
    private Preferences prefs;

    /**
     * Prevent listeners from being reclaimed before this Component
     */
    private LookupListener  lookupListenerGC;
    private PreferenceChangeListener prefListenerGC;
    private JViewport viewport;

    /**
     * Coloring from user settings. Updated in {@link #updateColors}.
     */
    private Coloring coloring;
    
    public BraceMatchingSidebarComponent(JTextComponent editor) {
        this.editor = editor;
        this.mimeType = DocumentUtilities.getMimeType(editor);
        this.prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        
        final Lookup.Result r = MimeLookup.getLookup(org.netbeans.lib.editor.util.swing.DocumentUtilities.getMimeType(editor)).lookupResult(
                FontColorSettings.class);
        lookupListenerGC = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                Iterator<FontColorSettings> fcsIt = r.allInstances().iterator();
                if (fcsIt.hasNext()) {
                  updateColors(r);
                }
            }
        };
        prefListenerGC = new PrefListener();

        r.addLookupListener(WeakListeners.create(LookupListener.class, lookupListenerGC , r));
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListenerGC, prefs));
        loadPreferences();
        
        editorPane = findEditorPane(editor);
        Component parent = editor.getParent();
        if (parent instanceof JViewport) {
            this.viewport = (JViewport)parent;
            // see #219015; need to listen on viewport change to show/hide the tooltip
            viewport.addChangeListener(this);
        }
        TextUI ui = editor.getUI();
        if (ui instanceof BaseTextUI) {
            baseUI = (BaseTextUI)ui;
            MasterMatcher.get(editor).addMatchListener(this);
            updateColors(r);
        } else {
            baseUI = null;
        }
        updatePreferredSize();
    }
    
    private void loadPreferences() {
        showOutline = prefs.getBoolean(SimpleValueNames.BRACE_SHOW_OUTLINE, true);
        showToolTip = prefs.getBoolean(SimpleValueNames.BRACE_FIRST_TOOLTIP, true);
    }
    
    /**
     * Updates the tooltip as a response to viewport scroll event. The actual
     * update is yet another runnable replanned to AWT, the Updater only 
     * provides coalescing of the scroll events.
     */
    private Task scrollUpdater = RequestProcessor.getDefault().create(new Runnable() {
        @Override
        // delayed runnable
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
               // runnable that can access visual hierearchy
               public void run() {
                    Rectangle visible = getVisibleRect();
                    if (visible.y < tooltipYAnchor) {
                        hideToolTip(true);
                    } else if (autoHidden) {
                        showTooltip();
                    }
               } 
            });
        }
    });

    @Override
    public void stateChanged(ChangeEvent e) {
        scrollUpdater.schedule(TOOLTIP_CHECK_DELAY);
    }
    
    private class PrefListener implements PreferenceChangeListener {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String prefName = evt == null ? null : evt.getKey();
            
            if (SimpleValueNames.BRACE_SHOW_OUTLINE.equals(prefName)) {
                showOutline = prefs.getBoolean(SimpleValueNames.BRACE_SHOW_OUTLINE, true);
                updatePreferredSize();
                BraceMatchingSidebarComponent.this.repaint();
            } else if (SimpleValueNames.BRACE_FIRST_TOOLTIP.equals(prefName)) {
                showToolTip = prefs.getBoolean(SimpleValueNames.BRACE_FIRST_TOOLTIP, true);
            }
        }
        
    }
    
    private static JEditorPane findEditorPane(JTextComponent editor) {
        Container c = editor.getUI().getRootView(editor).getContainer();
        return c instanceof JEditorPane ? (JEditorPane)c : null;
    }

    @Override
    public void focusGained(FocusEvent e) {}

    @Override
    public void focusLost(FocusEvent e) {
        hideToolTip(false);
    }

    @Override
    public void viewHierarchyChanged(ViewHierarchyEvent evt) {
        checkRepaint(evt);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ViewHierarchy.get(editor).addViewHierarchyListener(this);
        editor.addFocusListener(this);
    }

    @Override
    public void removeNotify() {
        ViewHierarchy.get(editor).removeViewHierarchyListener(this);
        editor.removeFocusListener(this);
        super.removeNotify();
    }
    
    
    
    private void updateColors(Lookup.Result r) {
        Iterator<FontColorSettings> fcsIt = r.allInstances().iterator();
        if (!fcsIt.hasNext()) {
          return;
        }
        FontColorSettings fcs = fcsIt.next();
        
        AttributeSet as = fcs.getFontColors(BRACES_COLORING);
        if (as == null) {
            as = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        } else {
            as = AttributesUtilities.createComposite(as, fcs.getFontColors(FontColorNames.DEFAULT_COLORING));
        }
        this.coloring = Coloring.fromAttributeSet(as);
        int w = 0;
        
        if (coloring.getFont() != null) {
            w = coloring.getFont().getSize();
        } else if (baseUI != null) {
            w = baseUI.getEditorUI().getLineNumberDigitWidth();
        }
        this.barWidth = Math.max(4, w / 2);
        updatePreferredSize();
    }
    
    private void updatePreferredSize() {
        if (showOutline) {
            setPreferredSize(new Dimension(barWidth, editor.getHeight()));
        } else {
            setPreferredSize(new Dimension(0,0));
        }
        lineHeight = baseUI.getEditorUI().getLineHeight();
    }
    
    public void checkRepaint(ViewHierarchyEvent evt) {
        if (!evt.isChangeY()) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updatePreferredSize();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!showOutline) {
            return;
        }
        int[] points;
        
        Rectangle clip = getVisibleRect();//g.getClipBounds();
        g.setColor(coloring.getBackColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(coloring.getForeColor());

        try {
            points = findLinePoints(origin, matches);
            if (points == null) {
                return;
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        
        // brace outline starts/ends in the middle of the line, just in the middle of
        // the fold mark (if it is present)
        int dist = lineHeight / 2; //(getFontMetrics(coloring.getFont()).getDescent() + 1) / 2;
        
        Graphics2D g2d = (Graphics2D)g;
        
        Stroke s = new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
        if (coloring.getForeColor() != null) {
            g.setColor(coloring.getForeColor());
        }
        int start = points[0] + dist;
        int end = points[points.length - 1] - dist;
        int x = leftMargin + (lineWidth + 1) / 2;
        
        g2d.setStroke(s);
        g2d.drawLine(barWidth, start, x, start);
        
        g2d.drawLine(x, start, x, end);
        
        g2d.drawLine(x, end, barWidth, end);
    }

    @Override
    public void matchHighlighted(final MatchEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
            if (!isEditorValid()) {
                return;
            }
            origin = evt.getOrigin();
            matches = evt.getMatches();
            repaint();
            showTooltip();
           } 
        });
    }

    @Override
    public void matchCleared(MatchEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
           public void run() {
            origin = null;
            matches = null;
            repaint();
           } 
        });
    }
    
    /**
     * Computes points where the outline starts, ends and possibly when the
     * outline marks a match, but continues. The matcher can return several "matches"
     * for an origin, so the outline COULD include marks for all of them.
     * <p/>
     * The return value is interpreted as Y-coordinates.
     * 
     * @param origin values from Matcher
     * @param matches values from Matcher
     * @return array of Y coordinates of outline start/end/branching.
     */
    private int[] findLinePoints(int[] origin, int[] matches) throws BadLocationException {
        boolean lineDown = false;
        
        if (editorPane != null) {
            Object o = editorPane.getClientProperty(MATCHED_BRACES);
            if (o instanceof int[]) {
                origin = (int[])o;
                matches = null;
                lineDown = true;
            }
        }
        if (baseUI == null || origin == null || (matches == null && !lineDown)) {
            return null;
        }
        int minOffset = origin[0];
        int maxOffset = origin[1];
        
        int maxY;
        
        if (lineDown) {
            maxY = getSize().height;
        } else {
            for (int i = 0; i < matches.length; i += 2) {
                minOffset = Math.min(minOffset, matches[i]);
                maxOffset = Math.max(maxOffset, matches[i + 1]);
            }
            maxY = baseUI.getYFromPos(maxOffset);
        }

        int minY = baseUI.getYFromPos(minOffset);
        
        // do not paint ranges for a single-line brace
        if (minY == maxY) {
            return null;
        }
        
        int height = baseUI.getEditorUI().getLineHeight();
        
        minY += lineWidth;
        maxY += (height - lineWidth);
        
        return new int[] { minY, maxY };
    }
    
    private void hideToolTip(boolean autoHidden) {
        if (isMatcherTooltipVisible()) {
            ToolTipSupport tts = baseUI.getEditorUI().getToolTipSupport();
            tts.setToolTipVisible(false);
            this.autoHidden = autoHidden;
        }
    }
    
    private int[] findTooltipRange() {
        if (origin == null || matches == null) {
            return null;
        }
        int start = Integer.MAX_VALUE;
        int end = -1;
        
        for (int i = 0; i < matches.length; i += 2) {
            int s = matches[i];
            int e = matches[i+1];
            
            if (s < start) {
                start = s;
            }
            if (e > end) {
                end = e;
            }
        }
        
        if (start > origin[0]) {
            return null;
        } else {
            return new int[] { start, end };
        }
    }
    
    private boolean isEditorValid() {
        if (editor.isVisible() && Utilities.getEditorUI(editor) != null) {
            // do not operate on editors, which do not have focus
            return editor.hasFocus();
        } else {
            return false;
        }
    }
    
    public JComponent createToolTipView(int start, int end) {
        JEditorPane tooltipPane = new JEditorPane();
        EditorKit kit = editorPane.getEditorKit();
        Document doc = editor.getDocument();
        if (kit != null && doc instanceof NbDocument.CustomEditor) {
            CustomEditor ed = (NbDocument.CustomEditor)doc;
            Element lineRootElement = doc.getDefaultRootElement();
            try {
                // Start-offset of the fold => line start => position
                int lineIndex = lineRootElement.getElementIndex(start);
                Position pos = doc.createPosition(
                        lineRootElement.getElement(lineIndex).getStartOffset());
                // DocumentView.START_POSITION_PROPERTY
                tooltipPane.putClientProperty("document-view-start-position", pos);
                // End-offset of the fold => line end => position
                lineIndex = lineRootElement.getElementIndex(end);
                pos = doc.createPosition(lineRootElement.getElement(lineIndex).getEndOffset());
                // DocumentView.END_POSITION_PROPERTY
                tooltipPane.putClientProperty("document-view-end-position", pos);
                tooltipPane.putClientProperty("document-view-accurate-span", true);
                // Set the same kit and document
                tooltipPane.setEditorKit(kit);
                tooltipPane.setDocument(doc);
                tooltipPane.setEditable(false);
                tooltipPane.setFocusable(false);
                tooltipPane.putClientProperty("nbeditorui.vScrollPolicy", JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                tooltipPane.putClientProperty("nbeditorui.hScrollPolicy", JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                tooltipPane.putClientProperty("nbeditorui.selectSidebarLocations", "West");
                
                if (matches != null && origin != null) {
                    tooltipPane.putClientProperty(MATCHED_BRACES, origin);
                }
                
                tooltipPane.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hideToolTip(false);
                    }
                    
                });

            JComponent c = (JComponent)ed.createEditor(tooltipPane);
                /*
            c.putClientProperty("tooltip-type", "fold-preview"); // Checked in NbToolTip
            */
            Color foreColor = tooltipPane.getForeground();
            c.setBorder(new LineBorder(foreColor));
            c.setOpaque(true);
            
            //JComponent c2 = new BraceToolTip(editorPane, tooltipPane);
                return new BraceToolTip(c, tooltipPane);
            } catch (BadLocationException e) {
                // => return null
            }
        }
        return null;
    }
    
    private boolean isMatcherTooltipVisible() {
        ToolTipSupport tts = baseUI.getEditorUI().getToolTipSupport();
        return tts.getToolTip() instanceof BraceToolTip;
    }
    
    /**
     * If automatically hidden because of visibility in of the anchor point in
     * the viewport. Allows to determine whether the tooltip should be shown
     * again if the anchor scrolls outside viewport
     */
    private boolean autoHidden;
    
    /**
     * Y view coordinate of the highlight range start; updated on tooltip show,
     * Integer.MAX when the range is empty.
     */
    private int tooltipYAnchor;
    
    private void showTooltip() {
        if (!showToolTip) {
            return;
        }
        int[] range = findTooltipRange();
        if (range == null) {
            autoHidden = false;
            tooltipYAnchor = Integer.MAX_VALUE;
            return;
        }
        
        // show only iff the 1st line is out of the screen view:
        int contentHeight;
        Rectangle visible = getVisibleRect();
        
        try {
            int yPos = baseUI.getYFromPos(range[0]);
            tooltipYAnchor = yPos;
            if (yPos >= visible.y) {
                autoHidden = true;
                return;
            }
            int yPos2 = baseUI.getYFromPos(range[1]);
            contentHeight = yPos2 - yPos + lineHeight;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        
        JComponent tooltip = createToolTipView(range[0], range[1]);
        if (tooltip == null) {
            return;
        }
        
        int x = 1;
        int y = contentHeight + 5;
        
        if (tooltip.getBorder() != null) {
            Insets in = tooltip.getBorder().getBorderInsets(tooltip);
            x += in.left;
            y += in.bottom;
        }
        //y += visible.y;
        
        ToolTipSupport tts = baseUI.getEditorUI().getToolTipSupport();
        tts.setToolTipVisible(true, false);
        tts.setToolTip(tooltip, 
                PopupManager.ScrollBarBounds, 
                new Point(-x, -y),
                0, 0,
                ToolTipSupport.FLAG_PERMANENT);
    }
}
