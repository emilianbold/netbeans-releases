/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006Sun
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

package org.netbeans.performance.web.menus;

import java.awt.Point;
import java.awt.event.KeyEvent;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of typing in opened source editor.
 *
 * @author  mschovanek@netbeans.org
 */
public class WebRuntimeViewPopupMenuTest extends PerformanceTestCase {
    private Node dataObjectNode;
    private boolean isTomcatRunning = false;
    
    // strings
    private static String SERVERS=null;
    private static String TOMCAT = null;
    private static String WEB_APPLICATIONS=null;
    
    public static final String suiteName="UI Responsiveness Web Menus suite";    

    
    /** Creates a new instance of TypingInEditor */
    public WebRuntimeViewPopupMenuTest(String testName) {
        super(testName);
        init();
    }
    
    /** Creates a new instance of TypingInEditor */
    public WebRuntimeViewPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        init();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(WebSetup.class)
             .addTest(WebRuntimeViewPopupMenuTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }
    
    protected void init() {
        SERVERS = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle",
                "SERVER_REGISTRY_NODE");
        WEB_APPLICATIONS = Bundle.getStringTrimmed("org.netbeans.modules.tomcat5.nodes.Bundle",
        "LBL_WebApps");
        TOMCAT = Bundle.getStringTrimmed("org.netbeans.modules.tomcat5.Bundle",
        "LBL_TomcatFactory60");

        expectedTime = UI_RESPONSE;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
        WAIT_AFTER_PREPARE = 500;
        WAIT_AFTER_OPEN = 1000;

    }
    
    public void testServerRegistryPopupMenuRuntime(){
        testMenu(SERVERS,  false);
    }
    
    public void testTomcatPopupMenuRuntime(){
        testMenu(SERVERS+"|"+TOMCAT,  false);
    }
    
    public void testWebModulesPopupMenuRuntime(){
        testMenu(SERVERS+"|"+TOMCAT+"|"+WEB_APPLICATIONS, true);
    }
/*
    public void testWebModulePopupMenuRuntime(){
        testMenu(SERVERS+"|"+TOMCAT+"|"+WEB_APPLICATIONS+"|/manager", true);
    }
*/
    
    private void testMenu(String path, boolean startTomcat){
        if (startTomcat) {
            CommonUtilities.startTomcatServer();
            isTomcatRunning = true;
        }
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        dataObjectNode = new Node(runtimeTab.getRootNode(), path);
        doMeasurement();
    }
    
    protected void initialize() {
        System.out.println("=== " + this.getClass().getName() + " ===");
    }
    
    public void prepare() {
        dataObjectNode.select();
        waitNoEvent(1000);
    }
    
    public ComponentOperator open(){
        Point point = dataObjectNode.tree().getPointToClick(dataObjectNode
            .getTreePath());
        int button = dataObjectNode.tree().getPopupMouseButton();
        dataObjectNode.tree().clickMouse(point.x, point.y, 1, button);
        return null;
    }
    
    public void close() {
        new ActionNoBlock(null, null, new Shortcut(KeyEvent.VK_ESCAPE)).
            perform();
    }
    
    protected void shutdown() {
        if (isTomcatRunning) {
            CommonUtilities.stopTomcatServer();
        }
        super.shutdown();
    }
}
