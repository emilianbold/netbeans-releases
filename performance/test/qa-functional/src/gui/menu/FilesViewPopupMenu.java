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

}
