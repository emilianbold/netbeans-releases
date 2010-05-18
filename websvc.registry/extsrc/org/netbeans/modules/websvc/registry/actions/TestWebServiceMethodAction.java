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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.registry.actions;

import org.netbeans.modules.websvc.api.registry.WebServiceMethod;
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
			activatedNodes[0].getCookie(WebServiceMethod.class) != null) {
			return true;
		} else {
			return false;
		}
    }
    
    public boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(TestWebServiceMethodAction.class, "TEST_METHOD");
    }
    
    protected void performAction(Node[] activatedNodes) {
        if(null != activatedNodes && activatedNodes.length > 0) {
			WebServiceMethod methodCookie = (WebServiceMethod) activatedNodes[0].getCookie(WebServiceMethod.class);
			
			// !PW The node logic below relies on the fact that the tree of filternodes
			// is a continuous wrapper of the webservice node structure from the service
			// node, downward.
			
			/** First get the method name
			 */
			String methodName = activatedNodes[0].getName();
			final JavaMethod currentMethod = (JavaMethod)methodCookie.getJavaMethod();
			
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
