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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * GenericTablePanelTest.java
 * JUnit based test
 *
 * Created on April 20, 2004, 12:24 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import junit.framework.*;
import org.netbeans.modules.schema2beans.BaseBean;

import org.netbeans.modules.j2ee.sun.common.dd.webapp.WebProperty;
import org.netbeans.modules.j2ee.sun.common.dd.webapp.JspConfig;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.*;

/**
 *
 * @author vkraemer
 */
public class GenericTablePanelTest extends TestCase {
    
    private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
    "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
    
    public void testSomething() {
        ArrayList tableColumns = new ArrayList(3);
        tableColumns.add(new GenericTableModel.AttributeEntry(
        WebProperty.NAME, "LBL_Name", true));	// NOI18N
        tableColumns.add(new GenericTableModel.AttributeEntry(
        WebProperty.VALUE, "LBL_Value", true));	// NOI18N
        tableColumns.add(new GenericTableModel.ValueEntry(
        WebProperty.DESCRIPTION, "LBL_Description"));	// NOI18N
        
        // add JspConfig table
        GenericTableModel jspConfigModel = new GenericTableModel(JspConfig.PROPERTY, WebProperty.class, tableColumns);
        jspConfigModel.setData(new JspConfig());
        Object objectArray[] = new Object[3];
        objectArray[0] = "Name1";
        objectArray[1] = "Value1";
        objectArray[2] = "Description1";
        jspConfigModel.addRow(objectArray);
        jspConfigModel.alreadyExists(objectArray);
        objectArray = new Object[3];
        objectArray[0] = "Name2";
        objectArray[1] = "Value2";
        objectArray[2] = "Description2";
        jspConfigModel.editRow(0, objectArray);
        jspConfigModel.getColumnNames();
        jspConfigModel.getData();
        jspConfigModel.getDataBaseBean();
        jspConfigModel.getPropertyDefinitions();
        jspConfigModel.getRowCount();
        jspConfigModel.getValueAt(0,1);
        jspConfigModel.getValues(0);
        GenericTablePanel j, jspConfigPanel = new GenericTablePanel(jspConfigModel,
            webappBundle, "JspConfigProperties",	// NOI18N - property name
            DynamicPropertyPanel.class, HelpContext.HELP_WEBAPP_JSPCONFIG_POPUP,
            PropertyListMapping.getPropertyList(PropertyListMapping.WEBAPP_JSPCONFIG_PROPERTIES));
        j = jspConfigPanel;
        j.setHeadingMnemonic('a');
        
        javax.swing.JFrame foo = new javax.swing.JFrame();
        foo.getContentPane().add(jspConfigPanel);
        
        jspConfigPanel.getInputDialog();
        j.getInputDialog(objectArray);
        jspConfigModel.removeRow(0);
        
    }
    
    public GenericTablePanelTest(java.lang.String testName) {
        super(testName);
    }
    
}
