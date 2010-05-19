/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.tree;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import org.netbeans.modules.soa.mappercore.DefaultMapperContext;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.GraphItem;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.worklist.editor.mapper.model.WlmMapperModel;
import org.netbeans.modules.worklist.editor.mapper.palette.Palette;

/**
 * The default implementation of the MapperContext interface for the WLM Mapper.
 *
 * @author nk160297
 */
public class WlmMapperContext extends DefaultMapperContext {
    
    @Override
    public String getLeftDysplayText(MapperModel model, Object value) {
        return ((MapperSwingTreeModel) model.getLeftTreeModel())
                .getDisplayName(value);
    }

//    @Override
//    public Font getLeftFont(MapperModel model, Object value, Font defaultFont) {
//        if (model instanceof WlmMapperModel &&
//               ((WlmMapperModel) model).isFromInVariable(value))
//        {
//            return defaultFont.deriveFont(Font.BOLD);
//        }
//
//        return defaultFont.deriveFont(Font.PLAIN);
//    }
//
//        @Override
//    public Font getRightFont(MapperModel model, Object value, Font defaultFont) {
//        if (model instanceof WlmMapperModel &&
//               ((WlmMapperModel) model).isFromOutVariable(value))
//        {
//            return defaultFont.deriveFont(Font.BOLD);
//        }
//
//        return defaultFont.deriveFont(Font.PLAIN);
//    }
    
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
        assert model instanceof WlmMapperModel;
        return ((WlmMapperModel)model).getRightTreeModel().getDisplayName(value);
    }

    @Override
    public Icon getRightIcon(MapperModel model, Object value, Icon defaultIcon) {
        assert model instanceof WlmMapperModel;
        Icon icon = ((WlmMapperModel)model).getRightTreeModel().getIcon(value);
        return icon != null ? icon : super.getRightIcon(model, value, defaultIcon);
    }

    @Override
    public JPopupMenu getRightPopupMenu(MapperModel model, Object value) {
        return ((WlmMapperModel) model).getRightTreeModel().getPopupMenu(value);
    }

    @Override
    public String getRightToolTipText(MapperModel mode, Object value) {
        return ((WlmMapperModel) mode).getRightTreeModel().getToolTipText(value);
    }
    
    //==========================================================================
    @Override
    public JPopupMenu getCanvasPopupMenu(MapperModel mode, GraphItem item, Mapper mapper) {
        JPopupMenu menu = super.getCanvasPopupMenu(mode, item, mapper);
        
        JPopupMenu wlmMenu = ((WlmMapperModel) mode).getRightTreeModel().
                getCanvasPopupMenu(item);
        if (wlmMenu != null && wlmMenu.getComponentCount() > 0) {
            menu.addSeparator();
            for (int i = 0; i < wlmMenu.getComponentCount(); i++) {
                menu.add(wlmMenu.getComponent(i));
            }
        }
        return menu;
    }

    @Override
    protected List<JMenu> getMenuNewEllements(MapperModel mode) {
        List<JMenu> menuList = new ArrayList<JMenu>();
        JMenuBar bar = new Palette(((WlmMapperModel) mode).getMapperTcContext().
                getMapper()).createMenuBar();
        for (int i = 0; i < bar.getMenuCount(); i++) {
            menuList.add(bar.getMenu(i));
        }
        return menuList;
    }
    
}
