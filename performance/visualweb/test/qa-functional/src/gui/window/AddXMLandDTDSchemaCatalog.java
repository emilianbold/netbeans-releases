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
