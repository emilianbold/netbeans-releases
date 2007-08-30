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
package org.netbeans.modules.websvc.core.jaxws.nodes;

/** Service children (Port elements)
 *
 * @author mkuchtiak
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class ServiceChildren extends Children.Keys {
    WsdlService wsdlService;
    
    public ServiceChildren(WsdlService wsdlService) {
        this.wsdlService=wsdlService;
    }
    
    protected void addNotify() {
        updateKeys();
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
    }
    
    private void updateKeys() {
        List keys =  wsdlService.getPorts();
        setKeys(keys==null?new ArrayList():keys);
    }
    
    protected Node[] createNodes(Object key) {
        if(key instanceof WsdlPort) {
            WsdlPort wsdlPort = (WsdlPort)key;
            if(wsdlPort.getAddress() != null){  //Determine if it is a SOAP port
                return new Node[] {new PortNode((WsdlPort)key)};
            }
        }
        return new Node[0];
    }
    
}
