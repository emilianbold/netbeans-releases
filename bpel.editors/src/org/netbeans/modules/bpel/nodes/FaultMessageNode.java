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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.Lookup;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.04.09
 */
public class FaultMessageNode extends WsdlFileNode {

    public FaultMessageNode(WSDLModel wsdlModel, Lookup lookup) {
        super(wsdlModel, new MessageTypeChildren(wsdlModel, lookup), lookup);
    }

    public NodeType getNodeType() {
        return NodeType.FAULT_MESSAGE;
    }
}
