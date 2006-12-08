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

package org.netbeans.modules.websvc.core.dev.wizard.nodes;

import java.awt.Image;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/** Node representing WS Port
 *
 * @author mkuchtiak
 */
public class PortNode extends AbstractNode {
    WsdlPort port;
    
    public PortNode(WsdlPort port) {
        super(Children.LEAF);
        this.port=port;
        setName(port.getName());
        setDisplayName(port.getName());
    }
    
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/core/webservices/ui/resources/wsport.png"); //NOI18N
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // Handle deleting:
    public boolean canDestroy() {
        return false;
    }
    
}