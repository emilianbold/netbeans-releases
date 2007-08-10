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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.client.nodes;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class AdvancedConfigurationClientNode extends org.openide.nodes.AbstractNode {
    
    public AdvancedConfigurationClientNode() {
        super(org.openide.nodes.Children.LEAF);
        setDisplayName(NbBundle.getMessage(AdvancedConfigurationClientNode.class, "LBL_Section_AdvancedConfiguration_Client"));//NOI18N
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AdvancedConfigurationClientNode.class); //NOI18N
    }

    public String getPanelId() {
        return "advancedconfig"; //NOI18N
    }

}
