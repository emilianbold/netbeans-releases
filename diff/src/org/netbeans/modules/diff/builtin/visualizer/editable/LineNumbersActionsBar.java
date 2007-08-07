/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.api.diff.Difference;
import org.netbeans.editor.*;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Draws both line numbers and diff actions for a decorated editor pane.
 * 
 * @author Maros Sandor
 */
class LineNumbersActionsBar extends JComponent implements Scrollable, MouseMotionListener, MouseListener, PropertyChangeListener {

    private static final int ACTIONS_BAR_WIDTH = 16;
    private static final int LINES_BORDER_WIDTH = 4;
    private static final Point POINT_ZERO = new Point(0, 0);
    
    private final Image insertImage = org.openide.util.Utilities.loadImage("org/netbeans/modules/diff/builtin/visualizer/editable/insert.png"); // NOI18N
    private final Image removeImage = org.openide.util.Utilities.loadImage("org/netbeans/modules/diff/builtin/visualizer/editable/remove.png"); // NOI18N

    private final Image insertActiveImage = org.openide.util.Utilities.loadImage("org/netbeans/modules/diff/builtin/visualizer/editable/insert_active.png"); // NOI18N
    private final Image removeActiveImage = org.openide.util.Utilities.loadImage("org/netbeans/modules/diff/builtin/visualizer/editable/remove_active.png"); // NOI18N
    
    private final DiffContentPanel master;
    private final boolean actionsEnabled;
    private final int actionIconsHeight;
    private final int actionIconsWidth;

    private final String  lineNumberPadding = "        "; // NOI18N

    /**
     * Rendering hints for annotations sidebar inherited from editor settings.
     */
    private Map renderingHints;

    private int     linesWidth;
    private int     actionsWidth;
    
    private Color   linesColor;
    private int     linesCount;
    private int     maxNumberCount;

    private Point   lastMousePosition = POINT_ZERO;
    private HotSpot lastHotSpot = null;
    
    private List<HotSpot> hotspots = new ArrayList<HotSpot>(0);

    public LineNumbersActionsBar(DiffContentPanel master, boolean actionsEnabled) {
        this.master = master;
        this.actionsEnabled = actionsEnabled;
        actionsWidth = actionsEnabled ? ACTIONS_BAR_WIDTH : 0;
        actionIconsHeight = insertImage.getHeight(this);
        actionIconsWidth = insertImage.getWidth(this);
        setOpaque(true);
        setToolTipText(""); // NOI18N
        master.getMaster().addPropertyChangeListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    public void addNotify() {
        super.addNotify();
        initUI();
    }

    public void removeNotify() {
        super.removeNotify();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        repaint();
    }
    
    private Font getLinesFont() {
        Map coloringMap = EditorUIHelper.getSharedColoringMapFor(master.getEditorPane().getEditorKit().getClass());
        
        Object colValue = coloringMap.get(SettingsNames.LINE_NUMBER_COLORING);
        Coloring col = null;
        if (colValue != null && colValue instanceof Coloring) {
            col = (Coloring)colValue;
        } else {
            col = SettingsDefaults.defaultLineNumberColoring;
        }
        
        Font font = col.getFont();
        if (font == null) {
            font = ((Coloring) coloringMap.get(SettingsNames.DEFAULT_COLORING)).getFont();
        }
        return font;
    }
    
    private void initUI() {
        Map coloringMap = EditorUIHelper.getSharedColoringMapFor(master.getEditorPane().getEditorKit().getClass());
        
        Object colValue = coloringMap.get(SettingsNames.LINE_NUMBER_COLORING);
        Coloring col = null;
        if (colValue != null && colValue instanceof Coloring) {
            col = (Coloring)colValue;
        } else {
            col = SettingsDefaults.defaultLineNumberColoring;
        }
        
        linesColor = col.getForeColor();
        if (linesColor == null) {
            linesColor = ((Coloring) coloringMap.get(SettingsNames.DEFAULT_COLORING)).getForeColor();
        }
        Color bg = col.getBackColor();
        if (bg == null) {
            bg = ((Coloring) coloringMap.get(SettingsNames.DEFAULT_COLORING)).getBackColor();
        }
        setBackground(bg);
        
        updateStateOnDocumentChange();
    }

    private HotSpot getHotspotAt(Point p) {
        for (HotSpot hotspot : hotspots) {
          if (hotspot.getRect().contains(p)) {
              return hotspot;
          }
        }
        return null;
    }
    
    public String getToolTipText(MouseEvent event) {
        Point p = event.getPoint();
        HotSpot spot = getHotspotAt(p);
        if (spot == null) return null;
        Difference diff = spot.getDiff();
        if (diff.getType() == Difference.ADD) {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Remove"); // NOI18N
        } else if (diff.getType() == Difference.CHANGE) {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Replace"); // NOI18N
        } else {
            return NbBundle.getMessage(LineNumbersActionsBar.class, "TT_DiffPanel_Insert"); // NOI18N
        }
    }

    private void performAction(HotSpot spot) {
        master.getMaster().rollback(spot.getDiff());
    }
    
    public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            HotSpot spot = getHotspotAt(e.getPoint());
            if (spot != null) {
                performAction(spot);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        // not interested
    }

    public void mouseReleased(MouseEvent e) {
        // not interested
    }

    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    public void mouseExited(MouseEvent e) {
        lastMousePosition = POINT_ZERO;
        if (lastHotSpot != null) {
            repaint(lastHotSpot.getRect());
        }
        lastHotSpot = null;
    }
    
    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        lastMousePosition = p;
        HotSpot spot = getHotspotAt(p);
        if (lastHotSpot != spot) {
            repaint(lastHotSpot == null ? spot.getRect() : lastHotSpot.getRect());
        }
        lastHotSpot = spot;
        setCursor(spot != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }
    
    public void mouseDragged(MouseEvent e) {
        // not interested
    }

    void onUISettingsChanged() {
        initUI();        
        updateStateOnDocumentChange();
        repaint();
    }
    
    private void updateStateOnDocumentChange() {
        assert SwingUtilities.isEventDispatchThread();
        StyledDocument doc = (StyledDocument) master.getEditorPane().getDocument();
        int lastOffset = doc.getEndPosition().getOffset();
        linesCount = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);            

        Graphics g = getGraphics(); 
        if (g != null) checkLinesWidth(g);
        maxNumberCount = getNumberCount(linesCount);
        revalidate();
    }
    
    private int oldLinesWidth;
    
    private boolean checkLinesWidth(Graphics g) {
        FontMetrics fm = g.getFontMetrics(getLinesFont());
        Rectangle2D rect = fm.getStringBounds(Integer.toString(linesCount), g);
        linesWidth = (int) rect.getWidth() + LINES_BORDER_WIDTH * 2;
        if (linesWidth != oldLinesWidth) {
            oldLinesWidth = linesWidth;
            revalidate();
            repaint();
            return true;
        }
        return false;
    }
    
    private int getNumberCount(int n) {
        int nc = 0;
        for (; n > 0; n /= 10, nc++);
        return nc;
    }

    public Dimension getPreferredScrollableViewportSize() {
        Dimension dim = master.getEditorPane().getPreferredScrollableViewportSize();
        return new Dimension(getBarWidth(), dim.height);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return master.getEditorPane().getScrollableUnitIncrement(visibleRect, orientation, direction);//123
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return master.getEditorPane().getScrollableBlockIncrement(visibleRect, orientation, direction);
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(getBarWidth(), Integer.MAX_VALUE >> 2);
    }

    private int getBarWidth() {
        return actionsWidth + linesWidth;
    }

    public void onDiffSetChanged() {
        updateStateOnDocumentChange();
        repaint();
    }

    protected void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        Rectangle clip = g.getClipBounds();
        Stroke cs = g.getStroke();

        if (checkLinesWidth(gr)) return;
        
        if (renderingHints == null) {
            DecoratedEditorPane jep = master.getEditorPane();
            Class kitClass = jep.getEditorKit().getClass();
            Object userSetHints = Settings.getValue(kitClass, SettingsNames.RENDERING_HINTS);
            renderingHints = (userSetHints instanceof Map && ((Map)userSetHints).size() > 0) ? (Map)userSetHints : null;
        }
        if (renderingHints != null) {
            g.addRenderingHints(renderingHints);
        }
        
        EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(master.getEditorPane());
        int lineHeight = editorUI.getLineHeight();
        
        g.setColor(getBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        g.setColor(Color.LIGHT_GRAY);
        int x = master.isFirst() ? 0 : getBarWidth() - 1;
        g.drawLine(x, clip.y, x, clip.y + clip.height - 1);

        DiffViewManager.DecoratedDifference [] diffs = master.getMaster().getManager().getDecorations();

        int actionsYOffset = (lineHeight - actionIconsHeight) / 2;
        int offset = linesWidth;

        int currentDifference = master.getMaster().getCurrentDifference();
        List<HotSpot> newActionIcons = new ArrayList<HotSpot>();
        if (master.isFirst()) {
            int idx = 0;
            for (DiffViewManager.DecoratedDifference dd : diffs) {
                g.setColor(master.getMaster().getColorLines());
                g.setStroke(currentDifference == idx ? master.getMaster().getBoldStroke() : cs);                            
                g.drawLine(0, dd.getTopLeft(), clip.width, dd.getTopLeft());
                if (dd.getBottomLeft() != -1) {
                    g.drawLine(0, dd.getBottomLeft(), clip.width, dd.getBottomLeft());
                }
                if (actionsEnabled && dd.canRollback()) {
                    if (dd.getDiff().getType() != Difference.ADD) {
                        Rectangle hotSpot = new Rectangle(1, dd.getTopLeft() + actionsYOffset, actionIconsWidth, actionIconsHeight);
                        if (hotSpot.contains(lastMousePosition) || idx == currentDifference) {
                            g.drawImage(insertActiveImage, hotSpot.x, hotSpot.y, this);
                        } else {
                            g.drawImage(insertImage, hotSpot.x, hotSpot.y, this);
                        }
                        newActionIcons.add(new HotSpot(hotSpot, dd.getDiff()));
                    }
                }
                idx++;
            }
        } else {
            int idx = 0;
            for (DiffViewManager.DecoratedDifference dd : diffs) {
                g.setColor(master.getMaster().getColorLines());
                g.setStroke(currentDifference == idx ? master.getMaster().getBoldStroke() : cs);                            
                g.drawLine(clip.x, dd.getTopRight(), clip.x + clip.width, dd.getTopRight());
                if (dd.getBottomRight() != -1) {
                    g.drawLine(clip.x, dd.getBottomRight(), clip.x + clip.width, dd.getBottomRight());
                }
                if (actionsEnabled && dd.canRollback()) {
                    if (dd.getDiff().getType() == Difference.ADD) {
                        Rectangle hotSpot = new Rectangle(offset + 1, dd.getTopRight() + actionsYOffset, actionIconsWidth, actionIconsHeight);
                        if (hotSpot.contains(lastMousePosition) || idx == currentDifference) {
                            g.drawImage(removeActiveImage, hotSpot.x, hotSpot.y, this);
                        } else {
                            g.drawImage(removeImage, hotSpot.x, hotSpot.y, this);
                        }
                        newActionIcons.add(new HotSpot(hotSpot, dd.getDiff()));
                    }
                }
                idx++;
            }
        }

        hotspots = newActionIcons;
        
        int linesXOffset = master.isFirst() ? actionsWidth : 0;
        linesXOffset += LINES_BORDER_WIDTH;
        
        g.setFont(getLinesFont()); 
        g.setColor(linesColor);
        int lineNumber = clip.y / lineHeight;
        int yOffset = lineNumber * lineHeight;
        yOffset -= lineHeight / 4; // baseline correction
        int linesDrawn = clip.height / lineHeight + 3;  // draw past clipping rectangle to avoid partially drawn numbers
        int docLines = Utilities.getRowCount((BaseDocument) master.getEditorPane().getDocument());
        if (lineNumber + linesDrawn - 1 > docLines) {
            linesDrawn = docLines - lineNumber + 1;
        }
        for (int i = 0; i < linesDrawn; i++) {
            g.drawString(formatLineNumber(lineNumber), linesXOffset, yOffset);
            lineNumber++;
            yOffset += lineHeight;
        }
    }

    private String formatLineNumber(int lineNumber) {
        String strNumber = Integer.toString(lineNumber);
        int nc = getNumberCount(lineNumber);
        if (nc < maxNumberCount) {
            StringBuilder sb = new StringBuilder(10);
            sb.append(lineNumberPadding, 0, maxNumberCount - nc);
            sb.append(strNumber);
            return sb.toString();
        } else {
            return strNumber;
        }
    }

    private static class EditorUIHelper extends EditorUI {
        /** 
         * Gets the coloring map that can be shared by the components
         * with the same kit. Only the component coloring map is provided.
         */
        public static Map getSharedColoringMapFor(Class kitClass) {
            return EditorUIHelper.getSharedColoringMap(kitClass);
        }
    }
}
