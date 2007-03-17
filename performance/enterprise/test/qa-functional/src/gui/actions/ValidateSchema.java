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

import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class ValidateSchema extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node schemaNode;
    
    /** Creates a new instance of ValidateSchema */
    public ValidateSchema(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public ValidateSchema(String testName, String  performanceDataName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public void initialize(){
        log(":: initialize");
        schemaNode = new Node(EPUtilities.getProcessFilesNode("SOATestProject"),"fields.xsd");
        schemaNode.select();
        new OpenAction().perform(schemaNode);
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log("::open");
        
        schemaNode.performPopupAction("Validate XML"); // NOI18N
        
        OutputOperator oot = new OutputOperator();
        OutputTabOperator asot = oot.getOutputTab("XML check"); // NOI18N
        asot.waitText("XML validation finished"); // NOI18N
        return oot;
    }
    
    protected void shutdown() {
        log("::shutdown");
        new CloseAllDocumentsAction().perform();
    }
    
}