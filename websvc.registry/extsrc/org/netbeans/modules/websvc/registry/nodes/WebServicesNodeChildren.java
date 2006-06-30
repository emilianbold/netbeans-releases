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


