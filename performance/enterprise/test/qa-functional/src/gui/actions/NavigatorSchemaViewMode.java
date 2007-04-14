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

package gui.actions;

import gui.EPUtilities;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

/**
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class NavigatorSchemaViewMode  extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node processNode; 
    
    /** Creates a new instance of SchemaDesignView */
    public NavigatorSchemaViewMode(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
    }
    
    public NavigatorSchemaViewMode(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
    }
    
    protected void initialize() {
        log(":: initialize");
        new CloseAllDocumentsAction().performAPI();
        
        processNode = EPUtilities.getProcessFilesNode("SOATestProject");
        Node doc1 = new Node(processNode,"batch.xsd");
        doc1.select();
        
        JComboBoxOperator combo = new JComboBoxOperator(new TopComponentOperator("Navigator")); // NOI18N
        combo.selectItem("Schema View"); // NOI18N
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log(":: open");
        Node doc = new Node(processNode,"fields.xsd");
        doc.select();
        
        return new TopComponentOperator("fields.xsd");
    }
    
    public void close() {
        log("::close");
        Node doc1 = new Node(processNode,"batch.xsd");
        doc1.select();
    }
    
    protected void shutdown() {
        log(":: shutdown");
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new NavigatorSchemaViewMode("measureTime"));
    }
    
}