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

import java.awt.Component;
import java.awt.Image;
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
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;

/**
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

    @Override
    protected Image getRawIcon(int type) {
        Image rawIcon = getNodeType().getImage();
        Assign assign = getReference();
        if (assign != null && assign.isJavaScript()) {
            rawIcon = NodeType.JAVA_SCRIPT.getImage();
        }
        return rawIcon;
    }

    @Override
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    @Override
    protected boolean isDropNodeInstanceSupported(BpelNode childNode) {
        return childNode.getNodeType().equals(NodeType.COPY);
    }
    
    @Override
    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        Assign assign = getReference();

        if (assign != null && assign.isJavaScript()) {
            return new javax.swing.JPanel();
        }
        else {
            return super.getCustomizer(editingMode);
        }
    }

    @Override
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
        if (childRefObj instanceof Copy) {
            supportedPTs.add(new CopyEntityPasteType(assign, (Copy)childRefObj));
        }
        
        return supportedPTs;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        if (getReference() == null) {
            return sheet;
        }
        Sheet.Set mainPropertySet = getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        PropertyUtils propUtil = PropertyUtils.getInstance();
        propUtil.registerAttributeProperty(this, mainPropertySet, NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N

        if ( !(getReference()).isJavaScript()) {
            Property prop = propUtil.registerProperty(this, mainPropertySet, ASSIGNMENT_COUNT, "sizeOfAssignChildren", null, null);
            prop.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        }
        propUtil.registerProperty(this, mainPropertySet, VALIDATE, "getValidate", "setValidate", "removeValidate"); // NOI18N
        propUtil.registerProperty(this, mainPropertySet, DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N

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

            if (!(key instanceof Copy)) {
                return null;
            }
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

    @Override
    protected ActionType[] getActionsArray() {
        if ((getReference()).isJavaScript()) {
            return new ActionType[] {
                ActionType.GO_TO,
                ActionType.SEPARATOR,
                ActionType.WRAP,

                ActionType.SEPARATOR,
                ActionType.SHOW_POPERTY_EDITOR,
                ActionType.SEPARATOR,

                ActionType.MOVE_UP,
                ActionType.MOVE_DOWN,
                ActionType.SEPARATOR,
                ActionType.TOGGLE_BREAKPOINT,
                ActionType.SEPARATOR,
                ActionType.REMOVE,
                ActionType.SEPARATOR,
                ActionType.PROPERTIES
            };
        }
        else {
            return new ActionType[] {
                ActionType.GO_TO,
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
                ActionType.PROPERTIES
            };
        }
    }
    
    @Override
    protected void updateComplexProperties(ChangeEvent event) {
        Assign curAssign = getReference();
        if (curAssign == null || curAssign.isJavaScript()) {
            return;
        }
        if (event instanceof EntityInsertEvent) {
            BpelEntity parentEntity = event.getParent();
            if (curAssign.equals(parentEntity)) {
                BpelEntity newEntity = ((EntityInsertEvent)event).getValue();
                if (newEntity instanceof Copy) {
                    updateProperty(ASSIGNMENT_COUNT);
                }
            }
        } else if (event instanceof EntityRemoveEvent) {
            BpelEntity parentEntity = event.getParent();
            if (curAssign.equals(parentEntity)) {
                BpelEntity oldEntity = ((EntityRemoveEvent)event).getOldValue();
                if (oldEntity instanceof Copy) {
                    updateProperty(ASSIGNMENT_COUNT);
                }
            }
        }
    }

    @Override
    protected void updateComplexIcons(ChangeEvent event) {
        boolean isUpdate = false;
        if (event instanceof EntityInsertEvent || event instanceof EntityRemoveEvent) {
            BpelEntity parentEntity = event.getParent();
            isUpdate = parentEntity != null && parentEntity.equals(getReference());
            if (!isUpdate) {
                parentEntity = parentEntity != null ? parentEntity.getParent() : null;
                isUpdate = parentEntity != null && parentEntity.equals(this.getReference());
            }
        }

        if (isUpdate) {
            updateIcon();
        }
    }

    @Override
    protected boolean isRequireNameUpdate(ChangeEvent event) {
        if (super.isRequireNameUpdate(event)) {
            return true;
        }
        boolean isUpdate = false;
        if (event instanceof EntityInsertEvent || event instanceof EntityRemoveEvent) {
            BpelEntity parentEntity = event.getParent();
            isUpdate = parentEntity != null && parentEntity.equals(getReference());
            if (!isUpdate) {
                parentEntity = parentEntity != null ? parentEntity.getParent() : null;
                isUpdate = parentEntity != null && parentEntity.equals(this.getReference());
            }
        }
        return isUpdate;
    }
}
