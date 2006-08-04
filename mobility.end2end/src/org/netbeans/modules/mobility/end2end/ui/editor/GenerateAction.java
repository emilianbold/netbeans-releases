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

/*
 * GenerateAction.java
 *
 * Created on July 13, 2005, 3:22 PM
 *
 */
package org.netbeans.modules.mobility.end2end.ui.editor;

import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * An Action for execution of Javon run method. This action on node
 * performs generation of stubs given by node data object configuration
 *
 * @author Michal Skvor
 */
public class GenerateAction extends CookieAction {
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public String getName() {
        return NbBundle.getMessage( GenerateAction.class, "GENERATE_ACTION_LABEL" );
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] { E2EDataObject.class };
    }
    
    protected boolean enable( final Node[] activatedNodes ) {
        if( super.enable( activatedNodes )) {
            return !((E2EDataObject)activatedNodes[0].getCookie( E2EDataObject.class )).isGenerating();
        }
        
        return false;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected void performAction( final Node[] activatedNodes ) {
        final E2EDataObject dataObject = (E2EDataObject)activatedNodes[0].getCookie( E2EDataObject.class );
        dataObject.generate(false);
    }
    
}
