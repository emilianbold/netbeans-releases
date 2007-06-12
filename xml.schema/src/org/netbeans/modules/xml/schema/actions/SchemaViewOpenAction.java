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

package org.netbeans.modules.xml.schema.actions;

import java.io.IOException;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.SchemaEditorSupport;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * An action on the SchemaDataObject node (SchemaNode)
 * to "Open" the schema multiview to the SchemaView tab
 *
 * @author Jeri Lockhart
 */
public class SchemaViewOpenAction extends NodeAction{
    private static final long serialVersionUID = 1L;
    
    /** Creates a new instance of SchemaViewOpenAction */
    public SchemaViewOpenAction() {
    }
    
    protected void performAction(Node[] node) {
	if (node == null || node[0] == null){
	    return;
	}
	SchemaDataObject sdo = node[0].getLookup().lookup(SchemaDataObject.class);
	if (sdo != null){
	    SchemaEditorSupport ses = sdo.getSchemaEditorSupport();
	    ViewComponentCookie svc = sdo.getCookie(
		ViewComponentCookie.class);
	    if(svc!=null) {
		try {
		    if ( ses.getOpenedPanes()==null ||
			 ses.getOpenedPanes().length==0 ) {
			
			svc.view(ViewComponentCookie.View.STRUCTURE,
			    sdo.getSchemaEditorSupport().getModel().getSchema());
		    } else {
			ses.open();
		    }
		    return;
		} catch (IOException ex) {
		    //ErrorManager.getDefault().notify(ex);
		}
	    }
	}
	// default to open cookie
	OpenCookie oc = node[0].getCookie(OpenCookie.class);
	if (oc != null){
	    oc.open();
	}
    }
    
    protected boolean enable(Node[] node) {
	return true;
    }
    
    public String getName() {
	return NbBundle.getMessage(SchemaViewOpenAction.class, "Open");
    }
    
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
	return false;
    }
    
}
