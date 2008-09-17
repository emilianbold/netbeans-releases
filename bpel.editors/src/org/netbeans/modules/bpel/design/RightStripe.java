/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
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

package org.netbeans.modules.bpel.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.StripeDescriptor;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class RightStripe extends JPanel implements ComponentListener, 
        MouseListener, MouseMotionListener 
{
    
    private DesignView designView;
    private StatusPanel statusPanel = new StatusPanel();
    private DataPanel dataPanel = new DataPanel();

    private StripeDescriptor statusStripe = new StripeDescriptor();
    private Map<Integer, Cell> cells = new HashMap<Integer, Cell>();
    
    private boolean needsToBePrepeared = true;

    
    public RightStripe(DesignView designView) {
        this.designView = designView;

        setFocusable(false);
        setLayout(new BorderLayout(0, 0));
        
        add(statusPanel, BorderLayout.NORTH);
        add(dataPanel, BorderLayout.CENTER);
        
        dataPanel.addComponentListener(this);
        dataPanel.addMouseListener(this);
        dataPanel.addMouseMotionListener(this);
        
        setToolTipText(null);
    }

    
    
    private boolean isModelBroken() {
        return (getDesignView().getBPELModel().getState() 
                != BpelModel.State.VALID);
    }
    
    
    public void repaint() {
        needsToBePrepeared = true;
        super.repaint();
    }
    
    
    public DesignView getDesignView() {
        return designView;
    };

    
    private void prepareToRepaint() {
        if (needsToBePrepeared) {
            statusStripe = new StripeDescriptor();
            cells.clear();

            if (!isModelBroken()) {
                prepareToRepaint(designView.getRootPattern());
            }
            
            needsToBePrepeared = false;
        
            updateCursor(null);
        }
    }
    
    
    private void prepareToRepaint(Pattern pattern) {
        if (pattern == null) return;
        
        addCellItem(pattern);
        
        if (pattern instanceof CompositePattern) {
            for (Pattern child : ((CompositePattern) pattern)
                .getNestedPatterns())
            {
                prepareToRepaint(child);
            }
        }
    }
    
    
    private Rectangle getPatternBounds(Pattern pattern) {
        FBounds bounds = null;
        
        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            if (border != null) {
                bounds = border.getBounds();
            }
        }
        
        if (bounds == null) {
            bounds = pattern.getBounds();
        }

        DiagramView view = pattern.getView();
        
        Point p1 = view.convertDiagramToScreen(bounds.getTopLeft());
        Point p2 = view.convertDiagramToScreen(bounds.getBottomRight());
        
        return new Rectangle(p1.x - 16, p1.y - 32, p2.x - p1.x + 32, 
                p2.y - p1.y + 64);
    }
    
    
    private void addCellItem(Pattern pattern) {
        if (pattern == null) return;
        
        Decoration decoration = designView.getDecoration(pattern);
        
        if (decoration == null || !decoration.hasStripe()) return;
        
        int cellNumber = getCellNumber(pattern);
        
        Cell cell = cells.get(cellNumber);
        
        if (cell == null) {
            cell = new Cell();
            cells.put(cellNumber, cell);
        }
        
        StripeDescriptor stripeDescriptor = decoration.getStripe();
        
        statusStripe = StripeDescriptor.merge(statusStripe, stripeDescriptor);
        
        cell.addItem(pattern, stripeDescriptor);
    }
    
    
    private int getCellNumber(Pattern pattern) {
        FPoint point = pattern.getBounds().getTopLeft();
        FBounds bounds = getDesignView().getRootPattern().getBounds();
        
        int i = (int) ((point.y - bounds.y) / bounds.height 
                * dataPanel.getHeight() / 4.0);
        
        if (i >= getCellCount()) {
            i = getCellCount() - 1;
        } 
        
        if (i < 0) {
            i = 0;
        }
        
        return i;
                
        
//        double y = designView.convertDiagramToScreen(pattern.getBounds()
//                .getTopLeft()).y;
//        
//        y /= designView.getHeight();
//        
//        return (int) (y * dataPanel.getHeight() / 4.0);
    }
    
    
    private int getCellCount() {
        return (dataPanel.getHeight() + 1) / 4;
    }

    
    private Cell getCell(int y) {
        int n = y / 4;

        Cell result = cells.get(n); 
        
        if (result != null) {
            return result;
        }
       
        if (y < n * 4 + 2) {
            result = cells.get(n - 1);
        } else {
            result = cells.get(n + 1);
        }
        
        return result;
    }
    
    
    private void updateCursor(Point mousePoint) {
        if (mousePoint == null) {
            mousePoint = dataPanel.getMousePosition();
        }
        
        if (mousePoint != null) {
            dataPanel.setCursor((getCell(mousePoint.y) != null) 
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    : Cursor.getDefaultCursor());
        }
    }
    

    public void componentResized(ComponentEvent e) {
        repaint();
    }


    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 1) return;
        
        Cell cell = getCell(e.getY());
        
        if (cell == null) return;
        
        Pattern nextPattern = cell.getNextPattern(designView.getSelectionModel()
                .getSelectedPattern());

        if (nextPattern != null) {
            if (nextPattern != designView.getSelectionModel().getSelectedPattern()) {
                designView.getSelectionModel().setSelectedPattern(nextPattern);
            }

            DiagramView diagramView = nextPattern.getView();
            
            Rectangle rect = getPatternBounds(nextPattern);
            Rectangle visibleRect = designView.getVisibleRect();

            int dx1 = 0;
            int dx2 = 0;

            if (diagramView instanceof ProcessView) {
                DiagramView providersView = designView.getProvidersView();
                DiagramView consumersView = designView.getConsumersView();

                if (consumersView.isVisible()) {
                    dx1 = consumersView.getWidth();
                }

                if (providersView.isVisible()) {
                    dx2 = providersView.getWidth();
                }
            }

            visibleRect.x += dx1;
            visibleRect.width -= dx1 + dx2;
            
            if (rect.height <= visibleRect.height) {
                visibleRect.y = Math.max(0, rect.y + (rect.height 
                        - visibleRect.height) / 2);
            } else {
                visibleRect.y = Math.max(0, rect.y);
            }
            
            visibleRect.x = Math.max(0, rect.x + (rect.width
                        - visibleRect.width) / 2);

            visibleRect.x -= dx1;
            visibleRect.width += dx1 + dx2;
            
            diagramView.scrollRectToVisible(visibleRect);
        }
    }


    public void mouseEntered(MouseEvent e) {
        updateCursor(e.getPoint());
    }
    
    
    public void mouseMoved(MouseEvent e) {
        updateCursor(e.getPoint());
    }    
    
    
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}


    private class StatusPanel extends JPanel {
        public StatusPanel() {
            setFocusable(false);
            setPreferredSize(new Dimension(13, 24));
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth();
            int h = getHeight();
            
            int x0 = 2;
            int y0 = 7;

            int size = w - 2 * x0;

            if (isModelBroken()) {
                g.setColor(StripeDescriptor.ERROR_COLOR);
            } else {
                g.setColor(statusStripe.getStatusColor());
            } 
            
            g.fillRect(x0 + 1, y0 + 1, size - 2, size - 2);
            g.setColor(BORDER_BRIGHT);
            g.drawRect(x0, y0, size - 1, size - 1);
            g.setColor(BORDER_DARK);
            g.drawLine(x0, y0, x0 + size - 2, y0);
            g.drawLine(x0, y0, x0, y0 + size - 2);
        }

        public String getToolTipText(MouseEvent event) {
            if (isModelBroken()) {
                return NbBundle.getMessage(RightStripe.class, 
                        "LBL_InvalidSources"); // NOI18N
            } 
            
            return statusStripe.getStatusText();
        }
    }
    
    
    private class DataPanel extends JPanel {
        public DataPanel() {
            setFocusable(false);
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        
        
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            prepareToRepaint();
            
            int w = getWidth();
            
            for (Map.Entry<Integer, Cell> entity : cells.entrySet()) {
                int y0 = (entity.getKey() * 4);
                
                StripeDescriptor stripeDescriptor = entity.getValue()
                        .getStripeDescriptor();
                
                g.setColor(stripeDescriptor.getColor());
                
                if (stripeDescriptor.isFilled()) {
                    g.fillRect(1, y0, w - 2, 3);
                } else {
                    g.drawRect(1, y0, w - 3, 2);
                }
            }
        }

        
        public String getToolTipText(MouseEvent event) {
            Cell cell = getCell(event.getY());
            return (cell == null) ? null : cell.getToolTipText();
        }
    }
    
    
    private static class Cell {
        private StripeDescriptor stripeDescriptor = null;
        private List<Pattern> patterns = new ArrayList<Pattern>();
        
        
        public void addItem(Pattern pattern, StripeDescriptor stripeDescriptor) {
            this.stripeDescriptor = StripeDescriptor.merge(
                    this.stripeDescriptor, stripeDescriptor);
            patterns.add(pattern);
        }
        
        
        public String getToolTipText() {
            return (stripeDescriptor == null) ? null 
                    : stripeDescriptor.getText();
        }
        
        
        public StripeDescriptor getStripeDescriptor() {
            return stripeDescriptor;
        }
        
        
        public Pattern getNextPattern(Pattern pattern) {
            int patternIndex = (pattern == null) ? -1 
                    : patterns.indexOf(pattern);
            Pattern result = patterns.get((patternIndex + 1) % patterns.size());
            return result;
        }
    }
    
    
    private static final Color BORDER_DARK = new Color(0xCDCABB);
    private static final Color BORDER_BRIGHT = Color.WHITE;
}
