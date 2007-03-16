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

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class SchemaNavigatorSchemaView  extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String testProjectName = "SOATestProject";
    private static String testSchemaName = "fields";
    private static String testSchemaName1 = "batch";
    
    private Node projectNode, schemaNode, schema1Node;
    
    /** Creates a new instance of SchemaNavigatorSchemaView */
    public SchemaNavigatorSchemaView(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        //   WAIT_AFTER_OPEN=4000;
    }
    
    /** Creates a new instance of SchemaNavigatorSchemaView */
    public SchemaNavigatorSchemaView(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        //   WAIT_AFTER_OPEN=4000;
    }
    
    protected void initialize() {
        log(":: initialize");
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+java.io.File.separator+testProjectName);
        new CloseAllDocumentsAction().performAPI();
        
        projectNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        
        schema1Node = new Node(projectNode,"Process Files"+"|"+testSchemaName1+".xsd"); // NOI18N
        schema1Node.select();
        
        SchemaOperator tco = new SchemaOperator(testSchemaName1+".xsd");
        JComboBoxOperator NavCombo = new JComboBoxOperator(tco,0);
        NavCombo.selectItem("Schema View"); // NOI18N
        
        schemaNode = new Node(projectNode,"Process Files"+"|"+testSchemaName+".xsd"); //NOI18N
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log(":: open");
        schemaNode.select();
        return new SchemaOperator(testSchemaName+".xsd");
    }
    
    public void close() {
        log("::close");
        schema1Node.select();
    }
    
    protected void shutdown() {
        ProjectSupport.closeProject(testProjectName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new SchemaNavigatorSchemaView("measureTime"));
    }
}