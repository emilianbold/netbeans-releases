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

/** Port children (Operation elements)
 *
 * @author mkuchtiak
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class PortChildren extends Children.Keys {
    WsdlPort wsdlPort;
    
    public PortChildren(WsdlPort wsdlPort) {
        this.wsdlPort=wsdlPort;
    }
    
    protected void addNotify() {
        super.addNotify();
        updateKeys();
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
       
    private void updateKeys() {
        List keys =  wsdlPort.getOperations();
        setKeys(keys==null?new ArrayList():keys);
    }

    protected Node[] createNodes(Object key) {
        if(key instanceof WsdlOperation) {
            return new Node[] {new OperationNode((WsdlOperation)key)};
        }
        return new Node[0];
    }

}
