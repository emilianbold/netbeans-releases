package org.netbeans.modules.uml.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * We want a label that can be selected.  The key is that when the label
 * is selected, that we only want the text to be selected, not the
 * icon selection of the label.
 */
public class SelectableLabel extends JLabel
{
    private boolean isSelected;
    private boolean hasFocus;
    private Color selectedBackground = null;
    
    
    public SelectableLabel()
    {
        
    }
    
    public void setSelectedBackground(Color c)
    {
        selectedBackground = c;
    }
    
    public Color getSelectedBackground()
    {
        return selectedBackground;
    }
    
    public void paint(Graphics g)
    {
        
        int imageOffset = 0;
        Icon currentI = getIcon();
        if (currentI != null)
        {
            imageOffset = currentI.getIconWidth() +
                    Math.max(0, getIconTextGap() - 1);
        }
        
        if (isSelected)
        {
            if(isSelected == true)
            {
                Dimension d = getPreferredSize();
                
                g.setColor(getSelectedBackground());
                g.fillRect(imageOffset,
                        0,
                        d.width - 1 - imageOffset,
                        d.height);
            }
        }
        
        super.paint(g);
    }
    
    public Dimension getPreferredSize()
    {
        Dimension retDimension = super.getPreferredSize();
        if (retDimension != null)
        {
            retDimension =
                    new Dimension(retDimension.width + 3, retDimension.height);
        }
        return retDimension;
    }
    
    public void setSelected(boolean isSelected)
    {
        this.isSelected = isSelected;
    }
    
    public void setFocus(boolean hasFocus)
    {
        this.hasFocus = hasFocus;
    }
}