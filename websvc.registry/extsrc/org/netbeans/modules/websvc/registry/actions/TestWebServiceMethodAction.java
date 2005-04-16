/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

import org.netbeans.modules.websvc.registry.nodes.*;
import org.netbeans.modules.websvc.registry.ui.TestWebServiceMethodDlg;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.nodes.WebServicesCookie;

import com.sun.xml.rpc.processor.model.java.JavaMethod;

import javax.swing.SwingUtilities;


/**
 *
 * @author  david
 */
public class TestWebServiceMethodAction extends NodeAction { 
    
    protected boolean enable(Node[] activatedNodes) {
        // !PW this does not handle multiple selection (or rather, it does, but
		// only acts on the first node, ignoring the remainder.  See also performAction()
		// which is written the same way.
        if(activatedNodes != null && activatedNodes.length > 0 && 
			activatedNodes[0].getCookie(WebServiceMethodCookieImpl.class) != null) {
			return true;
		} else {
			return false;
		}
    }
    
    public boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_add_websvcdb");
    }
    
    public String getName() {
        return NbBundle.getMessage(TestWebServiceMethodAction.class, "TEST_METHOD");
    }
    
    protected void performAction(Node[] activatedNodes) {
        if(null != activatedNodes && activatedNodes.length > 0) {
			WebServiceMethodCookieImpl methodCookie = (WebServiceMethodCookieImpl) activatedNodes[0].getCookie(WebServiceMethodCookieImpl.class);
			
			// !PW The node logic below relies on the fact that the tree of filternodes
			// is a continuous wrapper of the webservice node structure from the service
			// node, downward.
			
			/** First get the method name
			 */
			String methodName = activatedNodes[0].getName();
			final JavaMethod currentMethod = methodCookie.getJavaMethod();
			
			/** Now the parent node is a port node with the port information.
			 */
			Node portNodeWrapper = activatedNodes[0].getParentNode();
			final String portName = (String) portNodeWrapper.getName();
			
			/** The port node's parent is the web service node with the WebServiceData.
			 */
			Node webServiceNodeWrapper = portNodeWrapper.getParentNode();
			WebServicesCookie serviceCookie = (WebServicesCookie) webServiceNodeWrapper.getCookie(WebServicesCookie.class);
			final WebServiceData wsData = serviceCookie.getWebServiceData();
			
			if(null != wsData) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						TestWebServiceMethodDlg dlg = new TestWebServiceMethodDlg(wsData, currentMethod, portName);
						dlg.displayDialog();
					}
				});
			}
		}
    }
}
