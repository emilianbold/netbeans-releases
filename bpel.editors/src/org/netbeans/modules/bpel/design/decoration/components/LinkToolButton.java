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


package org.netbeans.modules.bpel.design.decoration.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.FlowlinkTool;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 *
 * @author aa160298
 */
public class LinkToolButton extends JLabel
        implements DecorationComponent, MouseListener {
    

    private UniqueId omReference;
    private DesignView designView;
    
    //private Pattern pattern;
 
    public LinkToolButton(Pattern pattern) {
        super(ICON_GRAY);

        omReference = pattern.getOMReference().getUID();
        designView = pattern.getModel().getView();
        
        setOpaque(false);
        setBorder(null);
        setBackground(null);
        setFocusable(false);
        
        setPreferredSize(new Dimension(16, 16));
        
        addMouseListener(this);
    }

    
    public Pattern getPattern(){
        BpelEntity be = omReference.getModel().getEntity(omReference);
        return (be == null) ? null : designView.getModel().getPattern(be);
    }

    
    public void setPosition(Point p) {
        Dimension size = getPreferredSize();
        setBounds(p.x - size.width / 2, p.y - size.height / 2, size.width, size.height);
    }

    
    protected void paintComponent(Graphics g) {
        Point point = getMousePosition();
        
        Pattern pattern = getPattern();
        
        if (pattern == null){
            return;
        }
        
        FlowlinkTool flowLinkTool = pattern.getModel().getView()
                .getFlowLinkTool();
        
        
        if (flowLinkTool.isActive()) {
            if (flowLinkTool.isValidLocation()) {
                ICON.paintIcon(this, g, 0, 0);
            } else {
                ICON_RED.paintIcon(this, g, 0, 0);
            }
        } else {
            boolean rollover = (point != null) && (0 <= point.x) 
                    && (point.x < getWidth()) && (0 <= point.y) 
                    && (point.y < getHeight());

            if (rollover) {
                ICON.paintIcon(this, g, 0, 0);
            } else {
                ICON_GRAY.paintIcon(this, g, 0, 0);
            }
        }
    }
    

    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    
    
    public void mouseEntered(MouseEvent e) { 
        repaint(); 
    }
    
    
    public void mouseExited(MouseEvent e) { 
        repaint(); 
    }    
    
    
    private static final String ICON_PATH = "resources/envelope_small.png"; // NOI18N
    private static final String ICON_GRAY_PATH = "resources/envelope_small_gray.png"; // NOI18N
    private static final String ICON_RED_PATH = "resources/envelope_small_red.png"; // NOI18N
    
    private static final Icon ICON;
    private static final Icon ICON_GRAY;
    private static final Icon ICON_RED;
    
    static {
        ICON = new ImageIcon(Decoration.class.getResource(ICON_PATH));
        ICON_GRAY = new ImageIcon(Decoration.class.getResource(ICON_GRAY_PATH));
        ICON_RED = new ImageIcon(Decoration.class.getResource(ICON_RED_PATH));
    }
}
