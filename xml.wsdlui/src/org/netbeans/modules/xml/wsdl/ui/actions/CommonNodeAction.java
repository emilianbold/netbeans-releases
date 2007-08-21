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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.actions;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.Node;
import org.openide.util.actions.CookieAction;


/**
 *
 * @author radval
 *
 */
public abstract class CommonNodeAction extends CookieAction {

	

    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
    	if (activatedNodes == null || activatedNodes.length > 1) return false;
        if(activatedNodes.length != 0) {
            Node node = activatedNodes[0];
            WSDLComponent comp = node.getLookup().lookup(WSDLComponent.class);
            if (comp != null) {
            	WSDLModel model = comp.getModel();
            	if (model != null && XAMUtils.isWritable(model)) {
            		return super.enable(activatedNodes);
            	} else {
            		return false;
            	}
            }
        }
        return super.enable(activatedNodes);
    }
}
