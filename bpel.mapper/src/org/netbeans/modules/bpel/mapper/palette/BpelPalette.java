/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bpel.mapper.palette;


import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.model.customitems.BpelXPathCustomFunction;
import org.netbeans.modules.bpel.mapper.model.customitems.WrapServiceRefHandler;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.soa.xpath.mapper.palette.AbstractMapperPalette;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;

import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.palette.DefaultItemHandler;
import org.netbeans.modules.soa.xpath.mapper.palette.PaletteItem;

/**
 * @author Vladimir Yaroslavskiy
 * @author Nikita Krjukov
 * @version 2009.09.17
 */
public final class BpelPalette extends AbstractMapperPalette {

    public BpelPalette(MapperStaticContext staticContext) {
        super(staticContext);
    }

    @Override
    public MapperTcContext getStaticContext() {
        MapperStaticContext sContext = super.getStaticContext();
        assert sContext instanceof MapperTcContext;
        return (MapperTcContext)sContext;
    }

    protected JMenuBar createMenuBar() {
        JMenuBar newMBar = new JMenuBar();
        newMBar.add(createOperatorMenu());
        newMBar.add(createBooleanMenu());
        newMBar.add(createStringMenu());
        newMBar.add(createNodeMenu());
        newMBar.add(createNumberMenu());
        newMBar.add(createDateTimeMenu());
        newMBar.add(createBPELMenu());
        return newMBar;
    }

    private JMenu createOperatorMenu() {
        JMenu menu = createMenu(BpelPalette.class, "LBL_Operator", "operator"); // NOI18N

        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_GT)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_GE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_LT)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_LE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_SUM)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_MINUS)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_MULT)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_DIV)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_MOD)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_NEGATIVE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_NE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_EQ)));

        return menu;
    }

    private JMenu createBooleanMenu() {
        JMenu menu = createMenu(BpelPalette.class, "LBL_Boolean", "boolean"); // NOI18N

        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_AND)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreOperationType.OP_OR)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_NOT)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_LANG)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_FALSE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_TRUE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_BOOLEAN)));

        return menu;
    }

    private JMenu createStringMenu() {
        JMenu menu = createMenu(BpelPalette.class, "LBL_String", "string"); // NOI18N

        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_CONTAINS)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_NORMALIZE_SPACE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_STRING)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_STARTS_WITH)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_STRING_LENGTH)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_SUBSTRING)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_SUBSTRING_BEFORE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_SUBSTRING_AFTER)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_TRANSLATE)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_CONCAT)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(new String())));

        return menu;
    }

    private JMenu createNodeMenu() {
        JMenu menu = createMenu(BpelPalette.class, "LBL_Node", "node"); // NOI18N

        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_LOCAL_NAME)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_NAME)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_NAMESPACE_URI)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_POSITION)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_LAST)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_COUNT)));

        return menu;
    }

    private JMenu createNumberMenu() {
        JMenu menu = createMenu(BpelPalette.class, "LBL_Number", "number"); // NOI18N

        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_NUMBER)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(new Integer(0))));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_ROUND)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_SUM)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_FLOOR)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(CoreFunctionType.FUNC_CEILING)));

        return menu;
    }

    private JMenu createDateTimeMenu() {
        JMenu menu = createMenu(BpelPalette.class, "LBL_Date_Time", "date_time"); // NOI18N

        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.CURRENT_DATE_METADATA)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.CURRENT_TIME_METADATA)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.CURRENT_DATE_TIME_METADATA)));
        menu.add(new PaletteItem(this, new BpelSpecificItemHandlers.Deadline()));
        menu.add(new PaletteItem(this, new BpelSpecificItemHandlers.Duration()));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.DATE_TIME_LT_METADATA)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.TIME_LT_METADATA)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.DATE_LT_METADATA)));

        return menu;
    }

    private JMenu createBPELMenu() {
        JMenu menu = createMenu(BpelPalette.class, "LBL_BPEL", "bpel"); // NOI18N

        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.DO_XSL_TRANSFORM_METADATA)));
        menu.add(new PaletteItem(this, new WrapServiceRefHandler(BpelXPathCustomFunction.WRAP_WITH_SERVICE_REF_METADATA)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.DO_MARSHAL_METADATA)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.DO_UNMARSHAL_METADATA)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.GET_GUID_METADATA)));
        menu.add(new PaletteItem(this, new DefaultItemHandler(BpelXPathExtFunctionMetadata.GET_BPID_METADATA)));
        menu.add(new PaletteItem(this, new BpelSpecificItemHandlers.XmlLiteral()));
        menu.add(new PaletteItem(this, new BpelSpecificItemHandlers.Java(getStaticContext())));

        return menu;
    }

}
