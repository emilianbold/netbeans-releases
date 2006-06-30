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

package org.netbeans.modules.websvc.registry.nodes;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.websvc.registry.actions.*;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.Action;

/**
 * The top level node representing Web Services in the Server Navigator
 * @author octav, Winston Prakash
 */
public class WebServicesRootNode extends AbstractNode implements WebServiceGroupCookie , org.netbeans.modules.websvc.registry.netbeans.WebServicesRootNodeInterface{
    
    public WebServicesRootNode() {
        super(Children.LEAF);// will calculate the children later.
        setName("default");
        setDisplayName(NbBundle.getMessage(WebServicesRootNode.class, "Web_Services"));
        setShortDescription(NbBundle.getMessage(WebServicesRootNode.class, "Web_Services"));
        setIconBaseWithExtension("org/netbeans/modules/websvc/registry/resources/webservicegroup.png");
        getCookieSet().add(this);
    }

    public WebServiceGroup getWebServiceGroup(){
        return WebServiceListModel.getInstance().getWebServiceGroup("default");
    }
    

    
	public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(AddWebServiceAction.class),
            SystemAction.get(AddWebServiceGroupAction.class),
        };
	}
	
	public Action getPreferredAction() {
		return SystemAction.get(AddWebServiceAction.class);
	}

	public HelpCtx getHelpCtx() {
        return new HelpCtx("server_nav_web_svcs_node");
    }
    

}
