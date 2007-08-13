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

package gui.action;

import gui.window.WebFormDesignerOperator;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */

public class OpenProjectFirstPage extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private Node openNode;
    private String targetProject;
    private ProjectsTabOperator pto;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    
    /** Creates a new instance of OpenProjectFirstPage */
    public OpenProjectFirstPage(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /** Creates a new instance of OpenProjectFirstPage */
    public OpenProjectFirstPage(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void testOpenSmallProjectFirstPage() {
        targetProject = "VisualWebProject";
        doMeasurement();
    }
    
    public void testOpenLargeProjectFirstPage() {
        targetProject = "HugeApp";
        doMeasurement();
    }
    
    
    public void initialize(){
        log("::initialize::");
        EditorOperator.closeDiscardAll();
        pto = ProjectsTabOperator.invoke();        
        //Workaround for "Update data sources" dialog
        try {
            new JDialogOperator(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.dataconnectivity.utils.Bundle", "MSG_Update_Datasources_Title")).close();
        } catch (TimeoutExpiredException tex) {
            // Do nothing
            log(tex.toString());
        }        
    }
    
    public void prepare(){
        log("::prepare");
        long nodeTimeout = pto.getTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        try {
            openNode = new Node(pto.getProjectRootNode(targetProject), gui.VWPUtilities.WEB_PAGES + "|" + "Page1.jsp");
        } catch(TimeoutExpiredException tex) {
            pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",nodeTimeout);
            throw new Error("Cannot find expected node because of Timeout");
        }
        pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",nodeTimeout);
        
        if (this.openNode == null) {
            throw new Error("Cannot find expected node");
        }
        openNode.select();
    }
    
    public ComponentOperator open(){
        log("::open");
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        log("------------------------- after popup invocation ------------");
        popup.getTimeouts().setTimeout("JMenuOperator.PushMenuTimeout", 90000);
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        }
        
        return WebFormDesignerOperator.findWebFormDesignerOperator("Page1");
    }
    
    public void close(){
        log("::close");
        if(testedComponentOperator != null) {
            ((WebFormDesignerOperator)testedComponentOperator).close();
            testedComponentOperator = null;
        }
    }
    
    protected void shutdown() {
        log("::shutdown");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenProjectFirstPage("testOpenLargeProjectFirstPage"));
    }
}
