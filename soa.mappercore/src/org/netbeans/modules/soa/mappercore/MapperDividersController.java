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

package org.netbeans.modules.soa.mappercore;


import java.awt.Insets;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

/**
 *
 * @author anjeleevich
 */
public class MapperDividersController extends MouseInputAdapter {
    
    private Mapper mapper;
    private JPanel leftDivider;
    private JPanel rightDivider;

    
    private int mouseX;

    
    public MapperDividersController(Mapper mapper,
            JPanel leftDivider, JPanel rightDivider) 
    {
        this.mapper = mapper;
        this.leftDivider = leftDivider;
        this.rightDivider = rightDivider;
        
        leftDivider.addMouseListener(this);
        leftDivider.addMouseMotionListener(this);
        
        rightDivider.addMouseListener(this);
        rightDivider.addMouseMotionListener(this);
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        Insets insets = mapper.getInsets();
        int w = mapper.getWidth() - insets.right - insets.left;

        int minDividerPosition = MapperLayout.MIN_WIDTH;
        int maxDividerPosition = w - 2 * (MapperLayout.MIN_WIDTH 
                + MapperLayout.DIVIDER_WIDTH);
        
        int leftDividerPosition = mapper.getLeftDividerPosition();
        int rightDividerPosition = mapper.getRightDividerPosition();
        
        if (e.getSource() == leftDivider) {
            leftDividerPosition += (e.getX() - mouseX);
            
            leftDividerPosition = Math.max(minDividerPosition, 
                    Math.min(leftDividerPosition, maxDividerPosition));
            
            if (w - leftDividerPosition - rightDividerPosition 
                    - MapperLayout.MIN_DELTA < 0) 
            {
                rightDividerPosition = w - leftDividerPosition 
                        - MapperLayout.MIN_DELTA;
                rightDividerPosition = Math.max(rightDividerPosition, 0); 
            }
            
            mapper.revalidate();
            mapper.repaint();
        } else if (e.getSource() == rightDivider) {
            rightDividerPosition -= (e.getX() - mouseX);

            rightDividerPosition = Math.max(minDividerPosition, 
                    Math.min(rightDividerPosition, maxDividerPosition));
            
            if (w - leftDividerPosition - rightDividerPosition 
                    - MapperLayout.MIN_DELTA < 0) 
            {
                leftDividerPosition = w - rightDividerPosition 
                        - MapperLayout.MIN_DELTA;
                leftDividerPosition = Math.max(leftDividerPosition, 0);
            }
            
            mapper.revalidate();
            mapper.repaint();
        }
        
        if ((rightDividerPosition != mapper.getRightDividerPosition())
                || (leftDividerPosition != mapper.getLeftDividerPosition())) 
        {
            mapper.setDividerPositions(leftDividerPosition, 
                    rightDividerPosition);
            mapper.revalidate();
            mapper.repaint();
        }
    }
    
}    

