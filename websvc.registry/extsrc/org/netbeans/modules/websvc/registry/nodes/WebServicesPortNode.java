/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.nodes;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;

import com.sun.xml.rpc.processor.model.Port;

import java.awt.Image;
import javax.swing.Action;

import java.util.Iterator;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
/**
 *
 * @author  david
 */
public class WebServicesPortNode  extends AbstractNode {
    
    private Port port;
    
    
    /** Creates a new instance of WebServicesPortNode */
    public WebServicesPortNode() {
        this(null);
    }
    
    
    
    public WebServicesPortNode(Port  inPort) {
        super(new WebServicesPortNodeChildren(inPort));
        
        
        if(null == inPort) {
            return;
        }
        port = inPort;
        /**
         * We have to save off the Port name in a property because a QNAME is not serializable
         * using XMLEncoder.
         */
        
        String portName = (String)port.getProperty(WebServiceData.PORT_PROPERTY_NAME);
        setIconBaseWithExtension("org/netbeans/modules/websvs/registry/resources/wsport.png");
        setName(portName);
    }

    public Action[] getActions(boolean context) {
            return new Action[0]; // No actions on this node.
    }    
	
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(WebServiceMethodNode.class);
    }
    
    public Port getPort() {
        return port;
    }
    
}