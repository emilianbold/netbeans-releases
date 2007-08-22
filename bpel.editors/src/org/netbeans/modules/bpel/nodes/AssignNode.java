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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.AssignChild;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.nodes.dnd.BpelEntityPasteType;
import org.netbeans.modules.bpel.nodes.dnd.CopyEntityPasteType;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class AssignNode extends BpelNode<Assign> {
    
    public AssignNode(Assign reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public AssignNode(Assign reference, Lookup lookup) {
        super(reference, new MyChildren(reference, lookup), lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.ASSIGN;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    protected boolean isDropNodeInstanceSupported(BpelNode childNode) {
        return childNode.getNodeType().equals(NodeType.COPY);
    }
    
    public List<BpelEntityPasteType> createSupportedPasteTypes(BpelNode childNode) {
        if (childNode==null) {
            return Collections.EMPTY_LIST;
        }
        List<BpelEntityPasteType> supportedPTs = new ArrayList<BpelEntityPasteType>();
        if (!(childNode.getNodeType().equals(NodeType.COPY))) {
            return supportedPTs;
        }
        
        Assign assign = getReference();
        BpelEntity childRefObj = (BpelEntity)childNode.getReference();
        supportedPTs.add(new CopyEntityPasteType(assign, (Copy)childRefObj));
        
        return supportedPTs;
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
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        Property prop = PropertyUtils.registerProperty(this, mainPropertySet,
                ASSIGNMENT_COUNT, "sizeOfAssignChildren", null);
        prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        //
        return sheet;
    }
    
    public void reload() {
        MyChildren children = (MyChildren)getChildren();
        children.reloadFrom(getReference());
    }
    
    private static class MyChildren extends Children.Keys {
        
        private Lookup lookup;
        
        public MyChildren(Assign assign, Lookup lookup) {
            super();
            this.lookup = lookup;
            reloadFrom(assign);
        }
        
        protected Node[] createNodes(Object key) {
            assert key instanceof AssignChild;
            //
            if (!(key instanceof Copy)) {
                return null;
            }
            //
            return new Node[] {new CopyNode((Copy)key, lookup)};
        }
        
        public void reloadFrom(Assign assign)  {
            if (assign == null) {
                return;
            }
            
            AssignChild[] assignChildArr = assign.getAssignChildren();
            if (assignChildArr != null) {
                setKeys(assignChildArr);
            } else {
                setKeys(new Copy[0]);
            }
        }
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.WRAP,
            ActionType.SEPARATOR,
            ActionType.MOVE_UP,
            ActionType.MOVE_DOWN,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.SHOW_BPEL_MAPPER,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof EntityInsertEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent.equals(this.getReference())) {
                BpelEntity newEntity = ((EntityInsertEvent)event).getValue();
                if (newEntity instanceof Copy) {
                    updateProperty(ASSIGNMENT_COUNT);
                }
            }
        } else if (event instanceof EntityRemoveEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent.equals(this.getReference())) {
                BpelEntity oldEntity = ((EntityRemoveEvent)event).getOldValue();
                if (oldEntity instanceof Copy) {
                    updateProperty(ASSIGNMENT_COUNT);
                }
            }
        }
    }
}
