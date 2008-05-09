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

import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.xml.schema.model.Schema;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * This node represents a schema is embedded to a WSDL.
 *
 * @author nk160297
 */
public class EmbeddedSchemaNode extends BpelNode<Schema> {
    
    public EmbeddedSchemaNode(Schema schema, Children children, Lookup lookup) {
        super(schema, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.EMBEDDED_SCHEMA;
    }
    
    protected String getNameImpl() {
        Schema schema = getReference();
        if (schema != null) {
            String targetNamespace = schema.getTargetNamespace();
            if (targetNamespace != null && targetNamespace.length() != 0) {
                return targetNamespace;
            }
        }
        //
        return super.getNameImpl();
    }
    
    
    
}
