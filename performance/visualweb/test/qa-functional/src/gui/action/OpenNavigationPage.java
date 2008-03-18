/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    private TopComponentOperator navPage;
    
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
    
    @Override
    protected void initialize() {
        log("::initialize::");
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(NAVIGATION_FILTER);
    }
    
    public void prepare() {
        log("::prepare::");
        
        openNodeName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_Config")+"|"+"faces-config.xml"; // NOI18N
        Node projectRoot = null;
        try {
            projectRoot = new ProjectsTabOperator().getProjectRootNode("VisualWebProject");
            projectRoot.select();
            openNode = new Node(projectRoot, openNodeName);
            openNode.select();            
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project node");
        }
                
        if (openNode == null) {
            throw new Error("Cannot find node "+openNodeName);
        }
    }
    
    public ComponentOperator open() {
        System.out.println("opening");
        JPopupMenuOperator popup =  openNode.callPopup();
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
        navPage = new TopComponentOperator("faces-config.xml",0); // NOI18N
        System.out.println("opened");
        return navPage;
    }
    
    @Override
    public void close() {
        navPage.closeDiscard();

    }
    
    @Override
    protected void shutdown() {
        log("::shutdwown");
        repaintManager().resetRegionFilters();
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
