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
package org.netbeans.modules.xslt.tmap.nodes;

import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.ParamType;
import org.netbeans.modules.xslt.tmap.nodes.actions.ActionType;
import org.netbeans.modules.xslt.tmap.nodes.properties.Constants;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyType;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ParamNode extends TMapComponentNode<DecoratedParam> {

    public ParamNode(Param ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }

    public ParamNode(Param ref, Children children, Lookup lookup) {
        super(new DecoratedParam(ref), children, lookup);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.PARAM;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.getInstance().registerAttributeProperty(this.getReference(), mainPropertySet,
                Named.NAME_PROPERTY, PropertyType.NAME, "getName", "setName", null); // NOI18N
        //
// operation property covers portType too        
//        PropertyUtils.getInstance().registerAttributeProperty(this.getReference(), mainPropertySet,
//                Invoke.PORT_TYPE, PropertyType.PORT_TYPE,
//                "getPortType", "setPortType", null); // NOI18N
        //
//        PropertyUtils.getInstance().registerCalculatedProperty(
//                this, mainPropertySet,
//                PropertyType.PARAM_TYPE, 
//                "getType", "setType"); // NOI18N
        PropertyUtils.getInstance().registerAttributeProperty(
                this.getReference(), mainPropertySet,
                Param.TYPE, PropertyType.PARAM_TYPE,
                "getType", "setType", null); // NOI18N
        //
        PropertyUtils.getInstance().registerAttributeProperty(this.getReference(), mainPropertySet,
                Param.VALUE, PropertyType.PARAM_VALUE,
                "getValue", "setValue", "removeValue"); // NOI18N
        //
        PropertyUtils.getInstance().registerAttributeProperty(this.getReference(), mainPropertySet,
                Param.CONTENT, PropertyType.PARAM_CONTENT,
                "getContent", "setContent", "removeContent"); // NOI18N
        //
        return sheet;
    }
    
//    public String getType() {
//        Param param = getReference().getReference();
//        if (param == null) {
//            return null;
//        }
//        ParamType pType = param.getType();
//        return pType == null ? null : pType.getStringValue();
//    }
//    
//    public void setType(String pTypeValue) {
//        Param param = getReference().getReference();
//        if (param == null) {
//            return;
//        }
//        
//        param.setType(ParamType.parseParamType(pTypeValue));
//    }
//    
//    public void setType(ParamType pTypeValue) {
//        Param param = getReference().getReference();
//        if (param == null) {
//            return;
//        }
//        
//        param.setType(pTypeValue);
//    }
    @Override
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES,
            
        };
    }
}

