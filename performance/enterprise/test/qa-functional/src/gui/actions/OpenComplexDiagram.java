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
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org, mmirilovic@netbeans.org
 *
 */
public class OpenComplexDiagram extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Creates a new instance of OpenComplexDiagram */
    
    public OpenComplexDiagram(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    public OpenComplexDiagram(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public void initialize(){
        log(":: initialize");
        new CloseAllDocumentsAction().performAPI();
    }
    
    public void prepare() {
        log(":: prepare");
    }

    public ComponentOperator open() {
        log("::open");
        Node processFilesNode = EPUtilities.getProcessFilesNode("TravelReservationService");
        Node doc = new Node(processFilesNode,"TravelReservationService.bpel");
        doc.select();
        
        new OpenAction().performPopup(doc);
        return new TopComponentOperator("TravelReservationService.bpel");
    }
    
    protected void shutdown() {
        log("::shutdown");
    }
    
    public void close(){
        log("::close");
        new CloseAllDocumentsAction().performAPI();
    }

}