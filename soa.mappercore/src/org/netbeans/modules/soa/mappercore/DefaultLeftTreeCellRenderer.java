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

import java.awt.Component;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.modules.soa.mappercore.model.MapperModel;

/**
 *
 * @author anjeleevich
 */
public class DefaultLeftTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private Mapper mapper;
    
    public DefaultLeftTreeCellRenderer(Mapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) 
    {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, 
                hasFocus);
        
        MapperContext context = mapper.getContext();
        MapperModel model = mapper.getModel();
        
        setText(context.getLeftDysplayText(model, value));
        
        Icon oldIcon = getIcon();
        Icon newIcon = context.getLeftIcon(model, value, oldIcon);
        if (oldIcon != newIcon) setIcon(newIcon);
        
        Font oldFont = getFont();
        Font newFont = context.getLeftFont(model, value, oldFont);
        if (newFont != oldFont) setFont(newFont);
        
        setForeground(context.getLeftForeground(model, value, getForeground()));
        
        return this;
    }
}
