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
import org.netbeans.modules.websvc.registry.model.WebServiceData;

import java.util.*;
import org.openide.nodes.*;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.Operation;
import com.sun.xml.rpc.processor.model.java.JavaMethod;

/**
 *
 * @author  David Botterill
 */
public class WebServicesPortNodeChildren extends Children.Keys {
    
    private Port port;
    
    public WebServicesPortNodeChildren(Port inPort) {
        port = inPort;
    }
    
    protected void addNotify() {
        super.addNotify();
        updateKeys();
    }
    
    private void updateKeys() {
        /**
         * FIX bug: 4952054 - Had to make WebServiceData a Proper JavaBean so it would persist correctly.
         * This fix involved changing the List return type to ArrayList.
         */
        List javaMethodList = port.getOperationsList();
        setKeys(javaMethodList);
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        Node node = null;
        if (key instanceof Operation) {
            node = new WebServiceMethodNode(port, (Operation)key);
        }
        return new Node[]{node};
    }
}
