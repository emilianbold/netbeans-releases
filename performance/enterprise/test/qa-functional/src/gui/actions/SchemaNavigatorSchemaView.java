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

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;


/**
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class SchemaNavigatorSchemaView  extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String testSchemaName = "fields";
    
    private Node processNode, schemaNode;
    
    /** Creates a new instance of SchemaNavigatorDesignView */
    public SchemaNavigatorSchemaView(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
    }
    
    /** Creates a new instance of SchemaNavigatorDesignView */
    public SchemaNavigatorSchemaView(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
    }
    
    protected void initialize() {
        log(":: initialize");
        processNode = EPUtilities.getProcessFilesNode("SOATestProject");
        processNode.select();
        
        schemaNode = new Node(processNode, testSchemaName+".xsd");
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log(":: open");
        schemaNode.select();
        JComboBoxOperator combo = new JComboBoxOperator(new TopComponentOperator("Navigator")); // NOI18N
        combo.selectItem("Schema View"); // NOI18N
        return null;
    }
    
    @Override
    public void close() {
        processNode.select();
        new EventTool().waitNoEvent(1000);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new SchemaNavigatorSchemaView("measureTime"));
    }
}