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

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author anjeleevich
 */
public class DefaultRightTreeCellRenderer extends JLabel 
        implements RightTreeCellRenderer 
{
    /** Creates a new instance of DefaultRightTreeCellRenderer */
    
    private Color defaultColor = null;
    
    
    public DefaultRightTreeCellRenderer() {
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        setOpaque(false);
    }

    
    public Component getTreeCellRendererComponent(Mapper mapper, Object value, 
            boolean selected, boolean expanded, boolean leaf, 
            int row, boolean hasFocus) 
    {
        if (defaultColor == null) {
            defaultColor = getForeground();
        }
        
        Icon icon = null;
        if (leaf) {
            icon = mapper.getLeafIcon();
        } else if (expanded) {
            icon = mapper.getOpenIcon();
        } else {
            icon = mapper.getClosedIcon();
        }
        
        MapperContext context = mapper.getContext();
        MapperModel model = mapper.getModel();
        
        setText(context.getRightDysplayText(model, value));
        setIcon(context.getRightIcon(model, value, icon));
        setForeground(context.getRightForeground(model, value));
        setFont(context.getRightFont(model, value, getFont()));
        
        return this;
    }
}
