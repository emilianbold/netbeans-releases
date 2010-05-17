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
