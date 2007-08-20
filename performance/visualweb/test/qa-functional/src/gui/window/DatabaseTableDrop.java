/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.PaletteOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jemmy.JemmyProperties;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class DatabaseTableDrop extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private RuntimeTabOperator rto;
    protected WebFormDesignerOperator surface;
    protected PaletteComponentOperator palette;
    
    protected String categoryName;
    protected String componentName;
    protected java.awt.Point addPoint;
    
    private static final String DBRootName = "jdbc:derby://localhost:1527/travel [travel on TRAVEL]";
    private static final String DBTableName = "Tables"+"|"+"TRIP";
    
    /** Creates a new instance of DatabaseTableDrop
     * @param testName
     */
    public DatabaseTableDrop(String testName) {
        super(testName);
        expectedTime = 10000; // 20 seconds ?
        WAIT_AFTER_OPEN=5000;
        categoryName = "Basic";  // NOI18N
        componentName = "Table"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    /** Creates a new instance of DatabaseTableDrop
     * @param testName
     * @param performanceDataName
     */
    public DatabaseTableDrop(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 10000; // 20 seconds ?
        WAIT_AFTER_OPEN=5000;
        categoryName = "Basic";  // NOI18N
        componentName = "Table"; // NOI18N
        addPoint = new java.awt.Point(50,50);
    }
    
    protected void initialize() {
        log(":: initialize");
        new ActionNoBlock("Window|Navigating|Navigator",null).perform(); //NOI18N
        
        rto = RuntimeTabOperator.invoke();
        Node travelBaseNode = new Node(rto.getRootNode(),"Databases"+"|"+DBRootName); // NOI18N
        travelBaseNode.performPopupActionNoBlock("Connect"); // NOI18N
        processDBConnectDialog();
        ProjectsTabOperator.invoke();
        
        PaletteOperator.invoke();
        openPage();
    }
    
    private void openPage() {
        surface = gui.VWPUtilities.openedWebDesignerForJspFile("VisualWebProject", "Page1");
        palette = new PaletteComponentOperator();
    }
    
    private void addComponent() throws Error {
        //Select component in palette
        palette.getCategoryListOperator(categoryName).selectItem(componentName);
        
        //Click on design surface to add selected component on page
        surface.clickOnSurface(new Double(addPoint.getX()).intValue(),new Double(addPoint.getY()).intValue());
        
        long click1 = System.currentTimeMillis();
        log(":: click on surface");
        //Click some other surface point to make added component deselected
        
        new QueueTool().waitEmpty();
        long click2 = System.currentTimeMillis();
        surface.clickOnSurface(10,10);
        log(":: click on surface");
        log(":: Delta = " +(click2-click1));
        waitNoEvent(5000);
    }
    
    public void prepare() {
        log(":: prepare");
        addComponent();
        selectDBTableNode();
    }
    
    public ComponentOperator open() {
        log(":: open");
        surface.clickOnSurface(70,70);
        return null;
    }
    
    public void close() {
        log(":: close");
        clearBindingArtefacts();
    }
    
    private void processDBConnectDialog() {
        NbDialogOperator connectDlg = new NbDialogOperator("Connect"); // NOI18N
        JTextComponentOperator password = new JTextComponentOperator(connectDlg,0);
        password.setText("travel");
        
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",60000);        
        
        connectDlg.ok();
        try {
            connectDlg.waitClosed();
        } catch (TimeoutExpiredException tex) 
        {
            fail("Unable to start Java DB server");
        }
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",oldTimeout);
    }
    
    private void selectDBTableNode() {
        rto.invoke();
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
        
        table = new Node(tree,"Page1|tripDataProvider");
        new DeleteAction().perform(table);
        new NbDialogOperator(title).yes();
        surface.clickOnSurface(10,10);
        
        table  = new Node(tree,"SessionBean1|tripRowSet:");
        new DeleteAction().perform(table);
        new NbDialogOperator(title).yes();
    }
    
    protected void shutdown() {
        super.shutdown();
        rto = RuntimeTabOperator.invoke();
        Node travelBaseNode = new Node(rto.getRootNode(),"Databases"+"|"+DBRootName); // NOI18N
        travelBaseNode.performPopupActionNoBlock("Disconnect"); // NOI18N
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new DatabaseTableDrop("measureTime", "Time to Drop Database table on table"));
    }
}
