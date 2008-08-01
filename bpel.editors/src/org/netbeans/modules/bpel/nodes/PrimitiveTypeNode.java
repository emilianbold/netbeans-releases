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
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.openide.nodes.Children;

/**
 *
 * @author nk160297
 */
public class PrimitiveTypeNode extends BpelNode<GlobalSimpleType> {
    
    public PrimitiveTypeNode(GlobalSimpleType type, Children children, Lookup lookup) {
        super(type, children, lookup);
    }
    
    public PrimitiveTypeNode(GlobalSimpleType type, Lookup lookup) {
        super(type, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.PRIMITIVE_TYPE;
    }
    
    protected String getImplHtmlDisplayName() {
        String result = super.getImplHtmlDisplayName();
        NodesTreeParams treeParams = (NodesTreeParams)getLookup().
                lookup(NodesTreeParams.class);
        if (treeParams != null) {
            //
            if (treeParams.isHighlightTargetNodes()) {
                boolean isTargetNodeClass =
                        treeParams.isTargetNodeClass(this.getClass());
                if (isTargetNodeClass) {
                    result = EditorUtil.getAccentedString(result);
                }
            }
        }
        //
        return result;
    }
    
    public VariableStereotype getStereotype() {
        return VariableStereotype.PRIMITIVE_TYPE;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        //
        Sheet.Set mainPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                NAME, "getName", null); // NOI18N
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                VARIABLE_STEREOTYPE, "getStereotype", null); // NOI18N
        return sheet;
    }
    
}
