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
import com.sun.xml.rpc.processor.model.java.JavaMethod;
import com.sun.xml.rpc.processor.model.Port;

/** Node Children for the Webservice Node
 * @author  octav, Winston Prakash
 */
public class WebServicesNodeChildren extends Children.Keys {
    
    private WebServiceData wsData;
    
    public WebServicesNodeChildren(WebServiceData wsData) {
        this.wsData = wsData;
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
        Port [] ports = wsData.getPorts();
        List portList = Arrays.asList(ports);
        setKeys(portList);
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        Node node = null;
        if (key instanceof Port) {
            node = new WebServicesPortNode((Port)key);
        }
        return new Node[]{node};
    }
}


