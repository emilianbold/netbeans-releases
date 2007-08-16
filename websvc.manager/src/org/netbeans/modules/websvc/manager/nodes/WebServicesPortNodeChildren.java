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

package org.netbeans.modules.websvc.manager.nodes;
import org.netbeans.modules.websvc.manager.model.WebServiceData;

import java.util.*;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.nodes.*;

import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.java.JavaMethod;

/**
 *
 * @author  David Botterill
 */
public class WebServicesPortNodeChildren extends Children.Keys {
    
    private WebServiceData wsData;
    private WsdlPort port;
    
    public WebServicesPortNodeChildren(WebServiceData inWsData, WsdlPort inPort) {
        wsData = inWsData;
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
        List javaMethodList = port.getOperations();
        setKeys(javaMethodList);
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        Node node = null;
        if (key instanceof WsdlOperation) {
            node = new WebServiceMethodNode( wsData, port, (WsdlOperation)key);
        }
        return new Node[]{node};
    }
}
