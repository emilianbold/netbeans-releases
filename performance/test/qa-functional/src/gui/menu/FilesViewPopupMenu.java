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
    
    public void testProjectNodePopupMenuFiles() {
        testNode(getProjectNode());
    }
    
    public void testPackagePopupMenuFiles(){
        testNode(new Node(getProjectNode(), "src|org|netbeans|test|performance"));
    }
    
    public void testbuildXmlFilePopupMenuFiles(){
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
