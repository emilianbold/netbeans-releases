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

import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.nodes.actions.ActionType;
import org.netbeans.modules.xslt.tmap.nodes.properties.Constants;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyType;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class OperationNode extends TMapComponentNode<DecoratedOperation> {

    public OperationNode(Operation ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }

    public OperationNode(Operation ref, Children children, Lookup lookup) {
        super(new DecoratedOperation(ref), children, lookup);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.OPERATION;
    }

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
        Node.Property prop;
        prop = PropertyUtils.getInstance().registerAttributeProperty(this.getReference(), mainPropertySet,
                Operation.OPERATION_NAME, PropertyType.OPERATION,
                "getOperation", "setOperation", null); // NOI18N
//        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        return sheet;
    }
    
    @Override
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_NEWTYPES,
            ActionType.SEPARATOR,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES,
            
        };
    }
    
    @Override
    public ActionType[] getAddActionArray() {
        return new ActionType[] {
            ActionType.ADD_TRANSFORM,
            ActionType.ADD_INVOKE
        };
    }

}

