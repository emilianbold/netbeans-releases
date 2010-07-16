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
import java.awt.Font;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class DefaultMapperContext implements MapperContext {

    public String getLeftDysplayText(MapperModel model, Object value) {
        return value.toString();
    }
    
    public Color getLeftForeground(MapperModel model, Object value, 
            Color defaultColor) 
    {
        return defaultColor;
    }
    
    public Font getLeftFont(MapperModel model, Object value, Font defaultFont) {
        return (defaultFont == null) ? null : defaultFont.deriveFont(Font.PLAIN);
    }

    public Icon getLeftIcon(MapperModel model, Object value, Icon defaultIcon) {
        return defaultIcon;
    }

    public JPopupMenu getLeftPopupMenu(MapperModel model, Object value) {
        return null;
    }


    public String getRightDysplayText(MapperModel model, Object value) {
        return value.toString();
    }
    
    public Color getRightForeground(MapperModel model, Object value) {
        return null;
    }
    
    public Font getRightFont(MapperModel model, Object value, Font defaultFont) {
        return (defaultFont == null) ? null : defaultFont.deriveFont(Font.PLAIN);
    }

    public Icon getRightIcon(MapperModel model, Object value, Icon defaultIcon) {
        return defaultIcon;
    }

    public JPopupMenu getRightPopupMenu(MapperModel model, Object value) {
        return null;
    }

    public String getRightToolTipText(MapperModel mode, Object value) {
        return null;
    }

    public String getLeftToolTipText(MapperModel mode, Object value) {
        return null;
    }

    public JPopupMenu getCanvasPopupMenu(MapperModel mode, GraphItem item, Mapper mapper) {
        JPopupMenu mapperMenu = MapperPopupMenuFactory.
                createMapperPopupMenu(mapper.getCanvas(), item);
       
        List<JMenu> listMenu = getMenuNewEllements(mode);
        if (listMenu != null) {

            JMenu menuItem = new JMenu(NbBundle.getMessage(Canvas.class,
                    "NewMapperElement")); // NOI18N
            
            for (JMenu m : listMenu) {
                menuItem.add(m);
            }
            mapperMenu.add(new JPopupMenu.Separator(), 0);
            mapperMenu.add(menuItem, 0);
        }
       
        return mapperMenu;
    }

    protected List<JMenu> getMenuNewEllements(MapperModel mode) {
        return null;
    }
}
