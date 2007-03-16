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

import org.openide.util.NbBundle;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.border.Border;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

/**
 * Split pane divider with Diff decorations.
 * 
 * @author Maros Sandor
 */
class DiffSplitPaneDivider extends BasicSplitPaneDivider implements MouseMotionListener, MouseListener {
    
    private final Image insertAllImage = org.openide.util.Utilities.loadImage("org/netbeans/modules/diff/builtin/visualizer/editable/move_all.png"); // NOI18N
    private final Image insertAllActiveImage = org.openide.util.Utilities.loadImage("org/netbeans/modules/diff/builtin/visualizer/editable/move_all_active.png"); // NOI18N
    private final int actionIconsHeight;
    private final int actionIconsWidth;
    private final Point POINT_ZERO = new Point(0, 0);
    
    private final EditableDiffView master;

    private Point lastMousePosition = POINT_ZERO;
    private HotSpot lastHotSpot = null;
    private java.util.List<HotSpot> hotspots = new ArrayList<HotSpot>(0);
    
    private DiffSplitDivider mydivider;
    
    DiffSplitPaneDivider(BasicSplitPaneUI splitPaneUI, EditableDiffView master) {
        super(splitPaneUI);
        this.master = master;

        actionIconsHeight = insertAllImage.getHeight(this);
        actionIconsWidth = insertAllImage.getWidth(this);
        
        setBorder(null);
        setLayout(new BorderLayout());
        mydivider = new DiffSplitDivider();
        add(mydivider);
        
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            HotSpot spot = getHotspotAt(e.getPoint());
            if (spot != null) {
                performAction();    // there is only one hotspot
            }
        }
    }

    public void mouseExited(MouseEvent e) {
        lastMousePosition = POINT_ZERO;
        if (lastHotSpot != null) {
            mydivider.repaint(lastHotSpot.getRect());
        }
        lastHotSpot = null;
    }            

    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    public void mousePressed(MouseEvent e) {
        // not interested
    }

    public void mouseReleased(MouseEvent e) {
        // not interested
    }
    
    public void mouseDragged(MouseEvent e) {
        // not interested
    }

    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        lastMousePosition = p;
        HotSpot spot = getHotspotAt(p);
        if (lastHotSpot != spot) {
            mydivider.repaint(lastHotSpot == null ? spot.getRect() : lastHotSpot.getRect());
        }
        lastHotSpot = spot;
        setCursor(spot != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
    }

    private void performAction() {
        master.rollback(null);
    }
    
    public void setBorder(Border border) {
        super.setBorder(BorderFactory.createEmptyBorder());
    }

    DiffSplitDivider getDivider() {
        return mydivider;
    }

    private HotSpot getHotspotAt(Point p) {
        for (HotSpot hotspot : hotspots) {
          if (hotspot.getRect().contains(p)) {
              return hotspot;
          }
        }
        return null;
    }
    
    private class DiffSplitDivider extends JComponent {
    
        private Map renderingHints;

        public DiffSplitDivider() {
            setBackground(UIManager.getColor("SplitPane.background")); // NOI18N
            setOpaque(true);
            renderingHints = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty(
                    "awt.font.desktophints")); // NOI18N
        }

        public String getToolTipText(MouseEvent event) {
            Point p = event.getPoint();
            HotSpot spot = getHotspotAt(p);
            if (spot == null) return null;
            return NbBundle.getMessage(DiffSplitDivider.class, "TT_DiffPanel_MoveAll"); // NOI18N
        }
        
        protected void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr.create();
            Rectangle clip = g.getClipBounds();
        
            Rectangle rightView = master.getEditorPane2().getScrollPane().getViewport().getViewRect();
            Rectangle leftView = master.getEditorPane1().getScrollPane().getViewport().getViewRect();
            
            int editorsOffset = master.getEditorPane2().getLocation().y + master.getEditorPane2().getInsets().top;
            
            int rightOffset = -rightView.y + editorsOffset;
            int leftOffset = -leftView.y + editorsOffset;

            g.setColor(getBackground());
            g.fillRect(clip.x, clip.y, clip.width, clip.height);

            if (renderingHints != null) {
                g.addRenderingHints(renderingHints);
            }
            String diffInfo = (master.getCurrentDifference() + 1) + "/" + master.getDifferenceCount(); // NOI18N
            int width = g.getFontMetrics().stringWidth(diffInfo);
            g.setColor(Color.BLACK);
            g.drawString(diffInfo, (getWidth() - width) / 2, g.getFontMetrics().getHeight());
            
            if (clip.y < editorsOffset) {
                g.setClip(clip.x, editorsOffset, clip.width, clip.height);
            }

            int rightY = getWidth() - 1;

            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(0, clip.y, 0, clip.height);
            g.drawLine(rightY, clip.y, rightY, clip.height);
            
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            DiffViewManager.DecoratedDifference [] decoratedDiffs = master.getManager().getDecorations();
            int [] xCoords = new int[4];
            int [] yCoords = new int[4];
            for (DiffViewManager.DecoratedDifference dd : decoratedDiffs) {
                g.setColor(master.getColor(dd.getDiff()));
                if (dd.getBottomLeft() == -1) {
                    xCoords[0] = 0;             yCoords[0] = dd.getTopLeft() + leftOffset; 
                    xCoords[1] = rightY;        yCoords[1] = dd.getTopRight() + rightOffset;
                    xCoords[2] = rightY;        yCoords[2] = dd.getBottomRight() + rightOffset;
                    g.fillPolygon(xCoords, yCoords, 3);
                    g.setColor(master.getColorLines());
                    g.drawLine(0, dd.getTopLeft() + leftOffset, rightY, dd.getTopRight() + rightOffset);
                    g.drawLine(0, dd.getTopLeft() + leftOffset, rightY, dd.getBottomRight() + rightOffset);
                } else if (dd.getBottomRight() == -1) {
                    xCoords[0] = 0;             yCoords[0] = dd.getTopLeft() + leftOffset; 
                    xCoords[1] = rightY;        yCoords[1] = dd.getTopRight() + rightOffset;
                    xCoords[2] = 0;             yCoords[2] = dd.getBottomLeft() + leftOffset;
                    g.fillPolygon(xCoords, yCoords, 3);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(0, yCoords[0], 0, yCoords[2]);
                    g.setColor(master.getColorLines());
                    g.drawLine(0, dd.getTopLeft() + leftOffset, rightY, dd.getTopRight() + rightOffset);
                    g.drawLine(0, dd.getBottomLeft() + leftOffset, rightY, dd.getTopRight() + rightOffset);
                } else {
                    xCoords[0] = 0;             yCoords[0] = dd.getTopLeft() + leftOffset; 
                    xCoords[1] = rightY;        yCoords[1] = dd.getTopRight() + rightOffset;
                    xCoords[2] = rightY;        yCoords[2] = dd.getBottomRight() + rightOffset;
                    xCoords[3] = 0;             yCoords[3] = dd.getBottomLeft() + leftOffset;
                    g.fillPolygon(xCoords, yCoords, 4);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(0, yCoords[0], 0, yCoords[3]);
                    g.setColor(master.getColorLines());
                    g.drawLine(0, dd.getTopLeft() + leftOffset, rightY, dd.getTopRight() + rightOffset);
                    g.drawLine(0, dd.getBottomLeft() + leftOffset, rightY, dd.getBottomRight() + rightOffset);
                }
            }
            
            if (master.isActionsEnabled()) {
                List<HotSpot> newActionIcons = new ArrayList<HotSpot>();
                Rectangle hotSpot = new Rectangle((getWidth() - actionIconsWidth) /2, editorsOffset, actionIconsWidth, actionIconsHeight);
                if (hotSpot.contains(lastMousePosition)) {
                    g.drawImage(insertAllActiveImage, hotSpot.x, hotSpot.y, this);
                } else {
                    g.drawImage(insertAllImage, hotSpot.x, hotSpot.y, this);
                }
                newActionIcons.add(new HotSpot(hotSpot, null));
                hotspots = newActionIcons;
            }
            g.dispose();
        }
    }    
}
