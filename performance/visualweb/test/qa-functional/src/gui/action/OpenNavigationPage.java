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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.performance.test.guitracker.LoggingRepaintManager.RegionFilter;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class OpenNavigationPage extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    /** Node to be opened/edited */
    private static Node openNode ;
    
    private static String openNodeName;
    
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    
    /** Creates a new instance of OpenNavigationPage */
    public OpenNavigationPage(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=20000;
    }
    
    public OpenNavigationPage(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=20000;
    }
    
    protected void initialize() {
        log("::initialize::");
        EditorOperator.closeDiscardAll();
        repaintManager().setRegionFilter(NAVIGATION_FILTER);
    }
    
    public void prepare() {
        log("::prepare::");
        //openNodeName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualwebproject.jsfproject.ui.Bundle", "NODENAME_Navigation_xml"); // Navigation Page
        
        openNodeName = "Page Navigation"; // NOI18N
        openNode = new Node(new ProjectsTabOperator().getProjectRootNode("VisualWebProject"), openNodeName);
        openNode.select();
        
        if (this.openNode == null) {
            throw new Error("Cannot find node "+openNodeName);
        }
        log("========== Open file path ="+this.openNode.getPath());
    }
    
    public ComponentOperator open() {
        JPopupMenuOperator popup =  this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node " + openNodeName);
        }
        log("------------------------- after popup invocation ------------");
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item Open on node " + openNodeName);
        }
        log("------------------------- after open ------------");
        return new TopComponentOperator(openNodeName,0);
    }
    
    public void close() {
        ((TopComponentOperator)this.testedComponentOperator).close();
    }
    
    protected void shutdown() {
        log("::shutdwown");
        repaintManager().setRegionFilter(null);
        super.shutdown();
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new OpenNavigationPage("measureTime"));
    }
    
    private static final RegionFilter NAVIGATION_FILTER =
            new RegionFilter() {
        
        public boolean accept(javax.swing.JComponent c) {
            return c.getClass().getName().equals("org.netbeans.api.visual.widget.SceneComponent");
        }
        
        public String getFilterName() {
            return "Accept paints from org.netbeans.api.visual.widget.SceneComponent";
        }
    };
    
}
