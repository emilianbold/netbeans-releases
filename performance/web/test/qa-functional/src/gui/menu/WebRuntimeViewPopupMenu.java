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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.menu;
import java.awt.Point;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.performance.test.guitracker.ActionTracker;
import org.netbeans.test.web.performance.Macros;
import org.netbeans.test.web.performance.WebPerformanceTestCase;
import org.netbeans.jemmy.Timeouts;

/**
 * Test of typing in opened source editor.
 *
 * @author  mschovanek@netbeans.org
 */
public class WebRuntimeViewPopupMenu extends WebPerformanceTestCase {
    private Node dataObjectNode;
    private RuntimeTabOperator runtimeTab = null;
    private boolean isTomcatRunning = false;
    
    // strings
    private static String SERVERS = Bundle.getStringTrimmed(
        "org.netbeans.modules.j2ee.deployment.impl.ui.Bundle",
        "SERVER_REGISTRY_NODE");
    private static String BUNDLED_TOMCAT = "Tomcat"; // not in a bundle
    private static String WEB_APPLICATIONS = Bundle.getStringTrimmed(
        "org.netbeans.modules.tomcat5.nodes.Bundle",
        "LBL_WebApps");
    
    /** Creates a new instance of TypingInEditor */
    public WebRuntimeViewPopupMenu(String testName) {
        super(testName);
        init();
    }
    
    /** Creates a new instance of TypingInEditor */
    public WebRuntimeViewPopupMenu(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        init();
    }
    
    protected void init() {
        super.init();
        expectedTime = UI_RESPONSE;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
        WAIT_AFTER_PREPARE = 500;
        WAIT_AFTER_OPEN = 1000;
    }
    
    public void testServerRegistryPopupMenuRuntime(){
        testMenu(SERVERS,  false);
    }
    
    public void testTomcatPopupMenuRuntime(){
        testMenu(SERVERS+"|"+BUNDLED_TOMCAT,  false);
    }
    
    public void testWebModulesPopupMenuRuntime(){
        testMenu(SERVERS+"|"+BUNDLED_TOMCAT+"|"+WEB_APPLICATIONS, true);
    }
    
    public void testWebModulePopupMenuRuntime(){
        testMenu(SERVERS+"|"+BUNDLED_TOMCAT+"|"+WEB_APPLICATIONS+"|/manager", true);
    }
    
    
    private void testMenu(String path, boolean startTomcat){
        if (startTomcat) {
            Macros.startBundledTomact();
            isTomcatRunning = true;
        }
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        dataObjectNode = new Node(runtimeTab.getRootNode(), path);
        log("testMenu(): runtimeTab.isValid = "+runtimeTab.isValid());
        doMeasurement();
    }
    
    protected void initialize() {
        System.out.println("=== " + this.getClass().getName() + " ===");
    }
    
    public void prepare() {
        dataObjectNode.select();
        eventTool().waitNoEvent(1000);
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
            Macros.stopBundledTomact();
        }
        super.shutdown();
    }
}
