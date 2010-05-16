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


package org.netbeans.modules.bpel.design.selection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FStroke;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;


public class GhostSelection implements ActionListener {
    
    private DesignView designView;
    
    private Area ghostArea;
    private JLabel label;
    
    private float currentX;
    private float currentY;
    
    private boolean enabled;
    
    private Timer timer;
    
    private int iteration;
    
    
    public GhostSelection(DesignView designView) {
        this.designView = designView;
        this.timer = new Timer(RESIZE_TIME / ITERATION_COUNT, this);
    }
    
    
    public void init(String text, Point pt) {
        
        FPoint p = designView.getOverlayView().convertScreenToDiagram(pt);
        
        currentX = p.x;
        currentY = p.y;

        label = new JLabel(text);
        label.setIcon(ERROR_BADGE);
        label.setBorder(new EmptyBorder(2, 2, 2, 2));
        label.setBackground(new Color(0xFFFFDD));
        label.setOpaque(true);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(Color.RED);
        
        ghostArea = null;
    }

    
    public void initCentered(Pattern pattern, Point pt) {
        
        FPoint p = designView.getOverlayView().convertScreenToDiagram(pt);
        
        label = null;
        
        currentX = p.x;
        currentY = p.y;
        enabled = false;
        iteration = 0;
    
        if (pattern != null ){
            ghostArea = pattern.createSelection();
        } else {
            ghostArea = new Area(ContentElement.TASK_SHAPE);
        }
        
        Rectangle2D ghostBounds = ghostArea.getBounds2D();
        
        double size = Math.max(ghostBounds.getWidth(), ghostBounds.getHeight());
        
        double k = Math.min(MAXIMUM_GHOST_SIZE / size, 1);

        double cx = ghostBounds.getCenterX();
        double cy = ghostBounds.getCenterY();

        AffineTransform t = AffineTransform
                .getTranslateInstance(currentX, currentY);
        t.scale(k, k);
        t.translate(-cx, -cy);
        
        ghostArea.transform(t);
        
        getDesignView().repaint();
    }
    

    public void init(Pattern pattern, Point pt) {
        
        FPoint p = designView.getOverlayView().convertScreenToDiagram(pt);
        
        label = null;
        
        currentX = p.x;
        currentY = p.y;
        iteration = 0;
        
        if (pattern != null ){
            ghostArea = pattern.createSelection();
        } else {
            ghostArea = new Area(ContentElement.TASK_SHAPE);
        }
        
        Rectangle2D ghostBounds = ghostArea.getBounds2D();

        timer.restart();
        getDesignView().repaint();
    }
    
    
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    
    public void move(FPoint p) {
        move(p.x, p.y);
    }
    
    
    public void move(float newX, float newY) {
        if (moveImpl(newX, newY)) {
            getDesignView().repaint();
        }
    }
    
    
    private boolean moveImpl(float newX, float newY) {
        if (isEmpty()) return false;
        
        double dx = newX - currentX;
        double dy = newY - currentY;
        
        if (dx == 0 && dy == 0) return false;
        
        if (ghostArea != null) {
            ghostArea.transform(AffineTransform.getTranslateInstance(dx, dy));
        }
        
        currentX = newX;
        currentY = newY;
        
        return true;
    }
    
    
    public void clear() {
        if (isEmpty()) return;
        
        ghostArea = null;
        label = null;
        
        iteration = 0;
        getDesignView().repaint();
    }
    
    
    public boolean isEmpty() {
        return (ghostArea == null) && (label == null);
    }
    
    
    public DesignView getDesignView() {
        return designView;
    }
    
    
    public EntitySelectionModel getSelectionModel() {
        return getDesignView().getSelectionModel();
    }
    
    
    public void paint(Graphics2D g2) {
//        if (isEmpty()) return;
//
//        Point p = getDesignView().getMousePosition();
//        if (p != null) {
//            FPoint mp = getDesignView().convertScreenToDiagram(p);
//            moveImpl(mp.x, mp.y);
//        }
//        
//        if (ghostArea != null) {
//            g2.setStroke(new FStroke(2, 4, 3).createStroke(g2));
//            g2.setPaint(enabled ? ENABLED_COLOR : DISABLED_COLOR);
//
//            g2.setRenderingHint(
//                    RenderingHints.KEY_STROKE_CONTROL,
//                    RenderingHints.VALUE_STROKE_NORMALIZE);
//            
//            g2.draw(ghostArea);
//        }
//        
//        if (label != null) {
//            Dimension labelSize = label.getPreferredSize();
//            label.setBounds(0, 0, labelSize.width, labelSize.height);
//            
//            double zoom = designView.getCorrectedZoom();
//            
//            double tx = currentX;
//            double ty = currentY;
//
//            Graphics2D lg2 = (Graphics2D) g2.create();
//            lg2.setRenderingHint(
//                    RenderingHints.KEY_STROKE_CONTROL,
//                    RenderingHints.VALUE_STROKE_NORMALIZE);
//            lg2.translate(tx, ty);
//            lg2.scale(1.0 / zoom, 1.0 / zoom);
//            
//            if (lg2.getTransform() != null) {
//                double ctx = lg2.getTransform().getTranslateX();
//                double cty = lg2.getTransform().getTranslateY();
//                lg2.translate(-(ctx - (int) ctx), -(cty - (int) cty));
//            } 
//            
//            lg2.translate(-labelSize.width / 2, -labelSize.height);
//            lg2.setStroke(new BasicStroke(1));
//            
//            label.paint(lg2);
//            
//            lg2.dispose();
//        }
    }
    
    
    public void actionPerformed(ActionEvent e) {
        if (ghostArea == null) {
            timer.stop();
            return;
        }
        
        int steps = Math.max(1, ITERATION_COUNT - iteration++);
        
        Rectangle2D ghostBounds = ghostArea.getBounds2D();
        
        double size = Math.max(ghostBounds.getWidth(), ghostBounds.getHeight());

        if (size > MAXIMUM_GHOST_SIZE) {
            double k = Math.pow(MAXIMUM_GHOST_SIZE / size, 1.0 / steps);
            
            AffineTransform t = AffineTransform
                    .getTranslateInstance(currentX, currentY);
            t.scale(k, k);
            t.translate(-currentX, -currentY);
            
            ghostArea.transform(t);
        } else {
            timer.stop();
        }
        
        if (steps == 1) {
            timer.stop();
        }
        
        getDesignView().repaint();
    }
    
    
    private static int RESIZE_TIME = 300;
    private static int ITERATION_COUNT = 10;
    private static final Color ENABLED_COLOR = new Color(0x5D985C);
    private static final Color DISABLED_COLOR = new Color(0xCC0000);
    private static double MAXIMUM_GHOST_SIZE = 160;

    private static final Icon ERROR_BADGE = new ImageIcon(DesignView.class
            .getResource("resources/error_badge.png")); // NOI18N
}
