/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.menu;

import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.junit.NbTestSuite;


/**
 * Test of popup menu on nodes in Files View.
 * @author  mmirilovic@netbeans.org
 */
public class FilesViewPopupMenu extends ValidatePopupMenuOnNodes {
    
    private static FilesTabOperator filesTab = null;
    
    /** Creates a new instance of FilesViewPopupMenu */
    public FilesViewPopupMenu(String testName) {
        super(testName);
    }
    
    /** Creates a new instance of FilesViewPopupMenu */
    public FilesViewPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new FilesViewPopupMenu("testProjectNodePopupMenu", "Project node popup in Files View"));
        suite.addTest(new FilesViewPopupMenu("testPackagePopupMenu", "Package node popup in Files View"));
        suite.addTest(new FilesViewPopupMenu("testbuildXmlFilePopupMenu", "build.xml file node popup in Files View"));
        return suite;
    }
    
    public void testProjectNodePopupMenu() {
        testNode(getProjectNode());
    }
    
    public void testPackagePopupMenu(){
        testNode(new Node(getProjectNode(), "src|org|netbeans|test|performance"));
    }
    
    public void testbuildXmlFilePopupMenu(){
        testNode(new Node(getProjectNode(), "build.xml"));
    }
    
    public void testNode(Node node){
        dataObjectNode = node;
        doMeasurement();
    }
   
    private Node getProjectNode() {
        if(filesTab==null)
            filesTab = new FilesTabOperator();
        
        return filesTab.getProjectNode("PerformanceTestData");
    }

    private Node getWebProjectNode() {
        if(filesTab==null)
            filesTab = new FilesTabOperator();
        
        return filesTab.getProjectNode("PerformanceTestWebApplication");
    }
    
    
}
