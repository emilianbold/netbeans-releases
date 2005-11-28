/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;


/**
 * Test of Add Catalog dialog
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class AddXMLandDTDSchemaCatalog extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String BUNDLE, MENU, TITLE;
    private Node thenode;
    
    /**
     * Creates a new instance of AddXMLandDTDSchemaCatalog
     */
    public AddXMLandDTDSchemaCatalog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of AddXMLandDTDSchemaCatalog
     */
    public AddXMLandDTDSchemaCatalog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
    public void initialize() {
        BUNDLE = "org.netbeans.modules.xml.catalog.Bundle";
        MENU = Bundle.getStringTrimmed(BUNDLE,"LBL_mount");
        TITLE = Bundle.getStringTrimmed(BUNDLE,"PROP_Mount_Catalog");
        // show Runtime tab and select XML Entity Catalogs node
        thenode = new Node (RuntimeTabOperator.invoke().getRootNode(), Bundle.getStringTrimmed(BUNDLE,"TEXT_catalog_root"));
        thenode.select();
    }
    
    public void prepare() {
        // do nothing
    }
    
    public ComponentOperator open() {
        // invoke Mount Catalog item from the popup
        thenode.callPopup().pushMenu(MENU);
        return new NbDialogOperator(TITLE);
    }
 
    /** Test could be executed internaly in IDE without XTest
     * @param args arguments from command line
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new AddXMLandDTDSchemaCatalog("measureTime"));
    }
    
    
}
