/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.identity.profile.ui;

import org.netbeans.modules.identity.profile.ui.editor.*;
import org.netbeans.modules.identity.profile.ui.support.J2eeProjectHelper;
import org.openide.util.HelpCtx;

/**
 * Represents the WSC security view in the web service attribute editor.
 * 
 * Created on April 18, 2006, 1:38 PM
 *
 * @author ptliu
 */
public class WSCSecurityView extends SecurityView implements HelpCtx.Provider {

    public WSCSecurityView(J2eeProjectHelper helper) {
        super();
        WSCSectionNode rootNode = new WSCSectionNode(this, helper);
        setRootNode(rootNode);
        focusSection(rootNode.getSectionNodePanel());
    }
    
    public HelpCtx getHelpCtx() {
        //System.out.println("calling identity client node help");
        return new HelpCtx("idmtools_csh_securingwebserviceclient");
    }
}
