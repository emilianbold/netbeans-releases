/*
 * Utils.java
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import gui.ProgressSupport;

/**
 *
 * @author lm97939
 */
public class Utils {
    
    private static final String SERVER_REGISTRY = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE");
    
    public static String getTimeIndex() {
        return new SimpleDateFormat("HHmmssS",Locale.US).format(new Date());
    }
    
    public static void startStopServer(boolean start) {
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node serverNode = new Node(runtimeTab.getRootNode(), SERVER_REGISTRY+"|Glassfish V2");
        try {
	    new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
            if (start)
	        serverNode.performPopupAction("Start");
	    else
	        serverNode.performPopupAction("Stop");
	    new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
	    ProgressSupport.waitFinished((start?"Starting":"Stopping") + "Glassfish V2", 300000);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
        }
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
    }
    
}
