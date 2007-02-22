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

/*
 * FindUnusedAction.java
 *
 * Created on July 12, 2006, 3:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.actions;

import java.io.IOException;
import org.netbeans.modules.xml.schema.refactoring.query.QueryUnusedGlobals;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaModelCookie;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Jeri Lockhart
 */
public class FindUnusedAction extends CookieAction{
    
    /** Creates a new instance of FindUnusedAction */
    public FindUnusedAction() {
    }

    protected void performAction(Node[] node) {
        try {
            assert node.length==1:
                "Length of nodes array should be 1";
            DataObject dobj = (DataObject)node[0].getCookie(DataObject.class);
            SchemaModelCookie modelCookie = (SchemaModelCookie)dobj.getCookie(SchemaModelCookie.class);
            QueryUnusedGlobals query = new QueryUnusedGlobals(modelCookie.getModel());
            new QueryPanel(query);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify( ex);
        }
        
    }

    public String getName() {
        return NbBundle.getMessage(FindUnusedAction.class,"LBL_Find_Unused");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }
    
    protected int mode() {
	return CookieAction.MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
	return new Class[] {SchemaModelCookie.class};
    }
}
