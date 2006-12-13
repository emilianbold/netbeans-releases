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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.jaxrpc.actions;


import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;


import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;

/**
 *
 * @author Peter Williams
 */
public class RefreshClientsAction extends NodeAction {
	
	protected boolean enable(Node[] activatedNodes) {
		return true;
	}
	
	public HelpCtx getHelpCtx() {
		// !PW FIXME use correct help context when known.
		return HelpCtx.DEFAULT_HELP;
	}
	
	public String getName() {
		return "Refresh View";
	}
	
	protected void performAction(Node[] activatedNodes) {
		
		assert (activatedNodes != null && activatedNodes.length == 1);
		
		// Invoked on ClientRootNode to refresh the list of webservice clients
		// in this project.
		WebServicesClientSupport clientSupport = null;
		
		// Find WebServicesClientSupport from activated node.
		DataObject dobj = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
		if(dobj != null) {
			clientSupport = WebServicesClientSupport.getWebServicesClientSupport(dobj.getPrimaryFile());
		}
		
		if(clientSupport == null) {
			String mes = "Can't locate web services client support for Node: " + activatedNodes[0];
			NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
			DialogDisplayer.getDefault().notify(desc);
			return;
		}
		
		String mes = "Not Implemented Yet";
		NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
		DialogDisplayer.getDefault().notify(desc);
	}
	
	protected boolean asynchronous() {
		return false;
	}
}
