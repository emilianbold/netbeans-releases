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
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.openide.nodes.Children;

/**
 *
 * @author nk160297
 */
public class GlobalElementNode extends SchemaComponentNode<GlobalElement> {
    
    public GlobalElementNode(GlobalElement element, Children children, Lookup lookup) {
        super(element, children, lookup);
    }
    
    public GlobalElementNode(GlobalElement element, Lookup lookup) {
        super(element, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.GLOBAL_ELEMENT;
    }
    
    public VariableStereotype getStereotype() {
        return VariableStereotype.GLOBAL_ELEMENT;
    }
    
//    protected Sheet createSheet() {
//        Sheet sheet = super.createSheet();
//        //
//        Sheet.Set mainPropertySet = 
//                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
//        //
//        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
//                PropertyType.NAME, "getName", null); // NOI18N
//        return sheet;
//    }
    
}
