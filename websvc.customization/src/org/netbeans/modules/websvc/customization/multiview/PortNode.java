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
 * DefinitionsNode.java
 *
 * Created on February 26, 2006, 6:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.multiview;

import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;

/**
 *
 * @author Roderico Cruz
 */
public class PortNode extends SectionContainerNode{
    private Port port;
    /** Creates a new instance of DefinitionsNode */
    public PortNode(SectionView view,
            Port port) {
        super(Children.LEAF);
        this.port = port;
        setDisplayName(this.port.getName());
    }
    
    public String getPanelId() {
        return "port"; //NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(WSCustomizationView.ID_PORT);
    }
    
}
