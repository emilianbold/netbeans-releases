/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.test.xml.schema.core;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.test.xml.schema.core.lib.SchemaMultiView;
import org.netbeans.test.xml.schema.core.lib.util.Helpers;

/**
 *
 * @author ca@netbeans.org
 */

public class NavigationTestCase extends JellyTestCase {
    
    static final String [] m_aTestMethods = {
        "selectDifferentKindsOfView",
//        "selectColumnsRecursively"
//        "countComponents"
    };
    
    static final String SCHEMA_NAME = "Synt01";
    static final String SCHEMA_EXTENSION = ".xsd";
    
    public NavigationTestCase(String arg0) {
        super(arg0);
    }
    
    public static junit.framework.TestSuite suite() {
        junit.framework.TestSuite testSuite = new junit.framework.TestSuite("XSD Navigator");
        
        for (String strMethodName : m_aTestMethods) {
            testSuite.addTest(new NavigationTestCase(strMethodName));
        }
        
        return testSuite;
    }
    
    public void countComponents() {
        openSchema();
        
        TopComponentOperator opTopComponent = new TopComponentOperator(SCHEMA_NAME + SCHEMA_EXTENSION);
        Helpers.recurseComponent(0, opTopComponent.getSource());
    }
    
    public void selectDifferentKindsOfView() {
        openSchema();
        
        SchemaMultiView opMultiView = new SchemaMultiView(SCHEMA_NAME);
        
        opMultiView.switchToDesign();
        
        opMultiView.switchToSource();
        
        opMultiView.switchToSchema();
        
        opMultiView.switchToSchemaTree();
        opMultiView.switchToSchemaColumns();
        
        opMultiView.switchToDesign();
        
        opMultiView.switchToSource();
        
        opMultiView.switchToSchema();
    }
    
    public void selectColumnsRecursively() {
        openSchema();
        
        SchemaMultiView opMultiView = new SchemaMultiView(SCHEMA_NAME);
        
        opMultiView.switchToSchema();
        
        opMultiView.switchToSchemaColumns();
        
        JListOperator opList = opMultiView.getColumnListOperator(0);
        
        recurseColumns(0, 0, opMultiView, opList);
    }
    
    private void recurseColumns(int column, int row, SchemaMultiView opView, JListOperator opList) {
        
        int listSize = opList.getModel().getSize();
        
        if (row >= listSize) {
            return;
        }
        
        Helpers.writeJemmyLog("col " + column + " row " + row);
        opList.selectItem(row);
        Helpers.waitNoEvent();
        
        String strValue = opList.getSelectedValue().toString();
        
        Helpers.writeJemmyLog("List item value [" + strValue + "]");
        
        if (column > 1 && strValue.indexOf("[Global") >= 0) {
            // Nothing
        } else {
            JListOperator opList1 = opView.getColumnListOperator(column + 1);
            if (opList1 != null) {
                if (opList1.getModel().getSize() > 0) {
                    recurseColumns(column + 1, 0, opView, opList1);
                }
            }
        }
        
        recurseColumns(column, row+1, opView, opList);
    }
    
    private void openSchema() {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        
        ProjectRootNode nodeProjectRoot = pto.getProjectRootNode("XSDTestProject");
        nodeProjectRoot.select();
        Node nodeXSD = new Node(nodeProjectRoot, "Source Packages|qa.xmltools.samples|" + SCHEMA_NAME + SCHEMA_EXTENSION);
        
        new OpenAction().performPopup(nodeXSD);
        
        Helpers.waitNoEvent();
    }
    
    public void tearDown() {
        new SaveAllAction().performAPI();
    }
    
}
