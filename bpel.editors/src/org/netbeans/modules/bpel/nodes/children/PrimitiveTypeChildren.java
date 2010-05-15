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
package org.netbeans.modules.bpel.nodes.children;

import org.netbeans.modules.bpel.nodes.PrimitiveTypeNode;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Shows the list of Primitive Type nodes
 *
 * @author nk160297
 */
public class PrimitiveTypeChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public PrimitiveTypeChildren(Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(SchemaModelFactory.getDefault().getPrimitiveTypesModel().
                getSchema().getSimpleTypes());
    }
    
    protected Node[] createNodes(Object key) {
        if(key instanceof GlobalSimpleType) {
            return new Node[] {
                new PrimitiveTypeNode((GlobalSimpleType)key, myLookup)
            };
        }
        assert false;
        return new Node[]{};
    }
    
}
