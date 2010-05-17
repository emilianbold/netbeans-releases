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

package org.netbeans.performance.visualweb.dialogs;

import org.netbeans.performance.visualweb.PaletteComponentOperator;
import org.netbeans.performance.visualweb.JSFComponentOptionsDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

import org.netbeans.performance.visualweb.setup.VisualWebSetup;
import org.netbeans.performance.visualweb.VWPUtilities;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class DatabaseTableDropTest extends JSFComponentOptionsDialog {

    private RuntimeTabOperator rto;
//    protected WebFormDesignerOperator surface;
//    protected PaletteComponentOperator palette;
    
//    protected String categoryName;
//    protected String componentName;
//    protected java.awt.Point addPoint;
    private StringComparator previousComparator;
    
    private static final String DBRootName = "jdbc:derby://localhost:1527/travel [travel on TRAVEL]";
    private static final String DBTableName = "Tables"+"|"+"TRIP";

    /** Creates a new instance of DatabaseTableDropTest
     * @param testName
     */
    public DatabaseTableDropTest(String testName) {
        super(testName);
        expectedTime = 10000; // 20 seconds ?
        WAIT_AFTER_OPEN=5000;
        categoryName = "Woodstock Basic";  // NOI18N
        componentName = "Table"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    /** Creates a new instance of DatabaseTableDropTest
     * @param testName
     * @param performanceDataName
     */
    public DatabaseTableDropTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 10000; // 20 seconds ?
        WAIT_AFTER_OPEN=5000;
        categoryName = "Woodstock Basic";  // NOI18N
        componentName = "Table"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(VisualWebSetup.class)
             .addTest(DatabaseTableDropTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
    public void testDatabaseTableDrop() {
        doMeasurement();
    }
    

    public void initialize() {
 /*      new ActionNoBlock("Window|Navigating|Navigator",null).perform(); //NOI18N
        
        rto = RuntimeTabOperator.invoke();
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(false, false);
        previousComparator = rto.tree().getComparator();
        //rto.setComparator(comparator);
        rto.tree().setComparator(comparator);
*/
        super.initialize();

        rto = RuntimeTabOperator.invoke();
        Node travelBaseNode = new Node(rto.getRootNode(),"Databases"+"|"+DBRootName); // NOI18N
        travelBaseNode.performPopupActionNoBlock("Connect"); // NOI18N
        processDBConnectDialogNoPass();
        //VWPUtilities.invokePTO();

  //      PaletteComponentOperator.invoke();
  //      openPage();
 
    
    }
    
    private void openPage() {
        surface = org.netbeans.performance.visualweb.VWPUtilities.openedWebDesignerForJspFile("UltraLargeWA", "TestPage");
        palette = new PaletteComponentOperator();
    }
    
    private void addComponent() throws Error {
        //Select component in palette
        palette.getCategoryListOperator(categoryName).selectItem(componentName);
        
        //Click on design surface to add selected component on page
        surface.clickOnSurface(new Double(addPoint.getX()).intValue(),new Double(addPoint.getY()).intValue());
        
   //     long click1 = System.currentTimeMillis();
   //     log(":: click on surface");
        //Click some other surface point to make added component deselected
        
        new QueueTool().waitEmpty();
   //     long click2 = System.currentTimeMillis();
        //surface.clickOnSurface(10,10);
   //     log(":: click on surface");
   //     log(":: Delta = " +(click2-click1));
        
    }
    
    @Override
    public void prepare() {
       // super.prepare();
        addComponent();
        selectDBTableNode();
        surface.clickOnSurface(60,60);
    }
    
    public ComponentOperator open() {
        new NbDialogOperator("Add").ok();
        return null;
    }
    
    @Override
    public void close() {
        surface.clickOnSurface(70,70);
        surface.deleteSelection();
     //        clearBindingArtefacts();
    }
    private void processDBConnectDialogNoPass() {
        NbDialogOperator connectDlg = new NbDialogOperator("Connecting to Database"); // NOI18N
        connectDlg.waitClosed();
    }

    
    private void selectDBTableNode() {
        rto = RuntimeTabOperator.invoke();
        
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(false, false);
        previousComparator = rto.tree().getComparator();
        //rto.setComparator(comparator);
        rto.tree().setComparator(comparator);
        
        Node TableNode = new Node(rto.getRootNode(),"Databases"+"|"+DBRootName+"|"+DBTableName); // NOI18N
        TableNode.select();
    }
    
    private void clearBindingArtefacts() {
        String title = Bundle.getStringTrimmed("org.openide.explorer.Bundle","MSG_ConfirmDeleteObjectTitle"); //Confirm Object Deletion
        
        TopComponentOperator navigator = new TopComponentOperator("Navigator"); // NOI18N
        
        JTreeOperator tree =  new JTreeOperator(navigator);
                
        Node table = new Node(tree,"Page1|page1|html1|body1|form1|table1");
        new DeleteAction().perform(table);
        new NbDialogOperator(title).yes();
        surface.clickOnSurface(10,10);
        waitNoEvent(5000);
        
        navigator.makeComponentVisible();
        tree =  new JTreeOperator(navigator);
        table  = new Node(tree,"SessionBean1|trip:");
        new DeleteAction().perform(table);
        new NbDialogOperator(title).yes();
        surface.clickOnSurface(10,10);
        waitNoEvent(5000);
        
        navigator.makeComponentVisible();
        tree =  new JTreeOperator(navigator);
        table = new Node(tree,"Page1|trip");
        new DeleteAction().perform(table);
        new NbDialogOperator(title).yes();
    }
    
    @Override
    protected void shutdown() {
        super.shutdown();
        rto = RuntimeTabOperator.invoke();
        Node travelBaseNode = new Node(rto.getRootNode(),"Databases"+"|"+DBRootName); // NOI18N
        travelBaseNode.performPopupActionNoBlock("Disconnect"); // NOI18N
        rto.tree().setComparator(previousComparator);
    }
    

}
