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

package org.netbeans.modules.worklist.editor.mapper.palette;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;

import java.awt.event.MouseMotionAdapter;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.wlm.model.xpath.WlmXPathExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.10.29
 */
public final class Palette {

    public Palette(Mapper mapper) {
        myMapper = mapper;
    }

    public JPanel getPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 0.0;

        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        panel.add(createMenuBar(), c);

        panel.setBorder(new Border());
        panel.setOpaque(true);
        panel.addMouseMotionListener(new MouseMotionAdapter() {
        });

        return panel;
    }

    public JMenuBar createMenuBar() {
        myBar = new JMenuBar();
        myBar.setBorder(BorderFactory.createEmptyBorder());

        myBar.add(createOperatorMenu());
        myBar.add(createBooleanMenu());
        myBar.add(createStringMenu());
        myBar.add(createNodeMenu());
        myBar.add(createNumberMenu());
        myBar.add(createDateTimeMenu());
        myBar.add(createWlmFuncMenu());
//        myBar.add(createBPELMenu());
        myBar.setBorder(new Border());

        return myBar;
    }

    public void hideMenu() {
        for (int i = 0; i < myBar.getMenuCount(); i++) {
            JMenu menu = myBar.getMenu(i);

            if (menu != null && menu.isSelected()) {
                menu.setSelected(false);
                menu.getPopupMenu().setVisible(false);
                return;
            }
        }
    }

    public Mapper getMapper() {
        return myMapper;
    }

    private JMenu createOperatorMenu() {
        JMenu menu = createMenu("LBL_Operator", "operator"); // NOI18N

        menu.add(new Item(this, new Handler(CoreOperationType.OP_GT)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_GE)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_LT)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_LE)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_SUM)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_MINUS)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_MULT)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_DIV)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_MOD)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_NEGATIVE)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_NE)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_EQ)));

        return menu;
    }

    private JMenu createBooleanMenu() {
        JMenu menu = createMenu("LBL_Boolean", "boolean"); // NOI18N

        menu.add(new Item(this, new Handler(CoreOperationType.OP_AND)));
        menu.add(new Item(this, new Handler(CoreOperationType.OP_OR)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NOT)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_LANG)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_FALSE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_TRUE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_BOOLEAN)));

        return menu;
    }

    private JMenu createStringMenu() {
        JMenu menu = createMenu("LBL_String", "string"); // NOI18N

        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_CONTAINS)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NORMALIZE_SPACE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_STRING)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_STARTS_WITH)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_STRING_LENGTH)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_SUBSTRING)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_SUBSTRING_BEFORE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_SUBSTRING_AFTER)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_TRANSLATE)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_CONCAT)));
        menu.add(new Item(this, new Handler(new String())));

        return menu;
    }

    private JMenu createNodeMenu() {
        JMenu menu = createMenu("LBL_Node", "node"); // NOI18N

        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_LOCAL_NAME)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NAME)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NAMESPACE_URI)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_POSITION)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_LAST)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_COUNT)));

        return menu;
    }

    private JMenu createNumberMenu() {
        JMenu menu = createMenu("LBL_Number", "number"); // NOI18N

        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_NUMBER)));
        menu.add(new Item(this, new Handler(new Integer(0))));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_ROUND)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_SUM)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_FLOOR)));
        menu.add(new Item(this, new Handler(CoreFunctionType.FUNC_CEILING)));

        return menu;
    }

    private JMenu createDateTimeMenu() {
        JMenu menu = createMenu("LBL_Date_Time", "date_time"); // NOI18N

        // TODO: Nikita. Temporary commented. Maybe it worth extract all such funciton
        // to separate module to be able using them from different places. 
        //
        menu.add(new Item(this, new Handler(WlmXPathExtFunctionMetadata.CURRENT_DATE_METADATA)));
        menu.add(new Item(this, new Handler(WlmXPathExtFunctionMetadata.CURRENT_TIME_METADATA)));
        menu.add(new Item(this, new Handler(WlmXPathExtFunctionMetadata.CURRENT_DATE_TIME_METADATA)));
        menu.add(new Item(this, new Handler.Deadline()));
        menu.add(new Item(this, new Handler.Duration()));
//        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.DATE_TIME_LT_METADATA)));
//        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.TIME_LT_METADATA)));
//        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.DATE_LT_METADATA)));

        return menu;
    }

    private JMenu createWlmFuncMenu() {
        JMenu menu = createMenu("LBL_WLM_Functions", "wlm_functions"); // NOI18N
        //
        menu.add(new Item(this, new Handler(WlmXPathExtFunctionMetadata.GET_TASK_ID_METADATA)));
        menu.add(new Item(this, new Handler(WlmXPathExtFunctionMetadata.GET_EMAIL_METADATA)));
        menu.add(new Item(this, new Handler(WlmXPathExtFunctionMetadata.GET_TASK_OWNER_METADATA)));
        menu.add(new Item(this, new Handler(WlmXPathExtFunctionMetadata.GET_MANAGER_EMAIL_METADATA)));
        menu.add(new Item(this, new Handler(WlmXPathExtFunctionMetadata.GET_MANAGER_UID_METADATA)));
        //
        return menu;
    }

//    private JMenu createBPELMenu() {
//        JMenu menu = createMenu("LBL_BPEL", "bpel"); // NOI18N
//
//        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.DO_XSL_TRANSFORM_METADATA)));
//        //menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.GET_VARIABLE_PROPERTY_METADATA)));
//        menu.add(new Item(this, new WrapServiceRefHandler(BpelXPathCustomFunction.WRAP_WITH_SERVICE_REF_METADATA)));
//        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.DO_MARSHAL_METADATA)));
//        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.DO_UNMARSHAL_METADATA)));
//        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.GET_GUID_METADATA)));
//        menu.add(new Item(this, new Handler(BpelXPathExtFunctionMetadata.GET_BPID_METADATA)));
//        // menu.add(new Item(this, new Handler.XmlLiteral()));
//
//        return menu;
//    }

    private JMenu createMenu(String key, String icon) {
        String name = i18n(Palette.class, key);
        JMenu menu = new MyMenu(name);
        menu.setToolTipText(name);
        menu.setIcon(icon(Palette.class, icon));
        return menu;
    }

    // ---------------------------------------------------------------
    private static class Border implements javax.swing.border.Border {

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color color = g.getColor();
            g.setColor(c.getBackground().darker());
            y += height - 1;
            g.drawLine(x, y, x + width - 1, y);
            g.setColor(color);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 1, 0);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }

    // ----------------------------------------
    private static class MyMenu extends JMenu {

        MyMenu(String name) {
            super(name);
        }

        @Override
        public String getToolTipText() {
            Dimension size = getPreferredSize();

            if (getWidth() < size.width) {
                return super.getToolTipText();
            }
            return null;
        }
    }
    private JMenuBar myBar;
    private Mapper myMapper;
}
