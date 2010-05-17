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

package org.netbeans.modules.bpel.mapper.tree;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.palette.BpelPalette;
import org.netbeans.modules.soa.mappercore.DefaultMapperContext;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;

/**
 * The default implementation of the MapperContext interface for the BPEL Mapper.
 *
 * @author nk160297
 */
public class BpelMapperContext extends DefaultMapperContext {
    
    @Override
    public String getLeftDysplayText(MapperModel model, Object value) {
        return ((MapperSwingTreeModel) model.getLeftTreeModel())
                .getDisplayName(value);
    }

    @Override
    public Font getLeftFont(MapperModel model, Object value, Font defaultFont) {
        if (model instanceof BpelMapperModel &&
               ((BpelMapperModel) model).isFromInVariable(value)) 
        {
            return defaultFont.deriveFont(Font.BOLD);
        }
        
        return defaultFont.deriveFont(Font.PLAIN);
    }
    
        @Override
    public Font getRightFont(MapperModel model, Object value, Font defaultFont) {
        if (model instanceof BpelMapperModel &&
               ((BpelMapperModel) model).isFromOutVariable(value)) 
        {
            return defaultFont.deriveFont(Font.BOLD);
        }
        
        return defaultFont.deriveFont(Font.PLAIN);
    }
    
    @Override
    public Icon getLeftIcon(MapperModel model, Object value, Icon defaultIcon) {
        Icon icon = ((MapperSwingTreeModel) model.getLeftTreeModel()).getIcon(value);
        return icon != null ? icon : super.getLeftIcon(model, value, defaultIcon);
    }

    @Override
    public JPopupMenu getLeftPopupMenu(MapperModel model, Object value) {
        return ((MapperSwingTreeModel) model.getLeftTreeModel())
                .getPopupMenu(value);
    }

    @Override
    public String getLeftToolTipText(MapperModel mode, Object value) {
        return ((MapperSwingTreeModel) mode.getLeftTreeModel()).
                getToolTipText(value);
    }
    
    

    //==========================================================================
    
    @Override
    public String getRightDysplayText(MapperModel model, Object value) {
        assert model instanceof BpelMapperModel;
        return ((BpelMapperModel)model).getRightTreeModel().getDisplayName(value);
    }

    @Override
    public Icon getRightIcon(MapperModel model, Object value, Icon defaultIcon) {
        assert model instanceof BpelMapperModel;
        Icon icon = ((BpelMapperModel)model).getRightTreeModel().getIcon(value);
        return icon != null ? icon : super.getRightIcon(model, value, defaultIcon);
    }

    @Override
    public JPopupMenu getRightPopupMenu(MapperModel model, Object value) {
        return (model instanceof BpelMapperModel)
                ? ((BpelMapperModel) model).getRightTreeModel().getPopupMenu(value)
                : null;
    }

    @Override
    public String getRightToolTipText(MapperModel model, Object value) {
        return (model instanceof BpelMapperModel)
                ? ((BpelMapperModel) model).getRightTreeModel().getToolTipText(value)
                : null;
    }
    
    //==========================================================================
    @Override
    public JPopupMenu getCanvasPopupMenu(MapperModel mode, GraphItem item, Mapper mapper) {
        JPopupMenu menu = super.getCanvasPopupMenu(mode, item, mapper);
        
        JPopupMenu bpelMenu = ((BpelMapperModel) mode).getRightTreeModel().
                getCanvasPopupMenu(item);
        if (bpelMenu != null && bpelMenu.getComponentCount() > 0) {
            menu.addSeparator();
            for (int i = 0; i < bpelMenu.getComponentCount(); i++) {
                menu.add(bpelMenu.getComponent(i));
            }
        }
        return menu;
    }

    @Override
    protected List<JMenu> getMenuNewEllements(MapperModel mode) {
        List<JMenu> menuList = new ArrayList<JMenu>();
        JMenuBar bar = new BpelPalette(((BpelMapperModel) mode).
                getMapperStaticContext()).getMenuBar();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            menuList.add(bar.getMenu(i));
        }
        return menuList;
    }
    
}
