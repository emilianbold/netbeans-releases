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
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.nodes.actions.DeleteCorrelationSetAction;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.editors.CorrelationSetMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author nk160297
 */
public class CorrelationSetNode extends BpelNode<CorrelationSet> {
    
    public CorrelationSetNode(CorrelationSet set, Children children, Lookup lookup) {
        super(set, children, lookup);
    }
    
    public CorrelationSetNode(final CorrelationSet set, final Lookup lookup) {
        super(set, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CORRELATION_SET;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        PropertyUtils.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_PROPERTY,
            ActionType.SEPARATOR,
            ActionType.GO_TO_SOURCE,
            ActionType.FIND_USAGES,
            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
    public Action createAction(ActionType actionType) {
        switch (actionType) {
            case REMOVE:
                return SystemAction.get(DeleteCorrelationSetAction.class);
            default:
                return super.createAction(actionType);
        }
    }

    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        SimpleCustomEditor customEditor = new SimpleCustomEditor<CorrelationSet>(
                this, CorrelationSetMainPanel.class, editingMode);
        return customEditor;
    }
    
    public static class MyChildren extends Children.Keys
            implements ReloadableChildren {
        
        private Lookup myLookup;
        private CorrelationSet mySet;
        
        public MyChildren(CorrelationSet set, Lookup lookup) {
            super();
            myLookup = lookup;
            mySet = set;
            //
            setKeys(createKeys());
        }
        
        protected Node[] createNodes(Object key) {
            Node[] result = null;
            //
            if (key instanceof Node) {
                result = new Node[]{(Node)key};
            }
            //
            return result;
        }
        
        protected Node[] createKeys() {
            //
            List<WSDLReference<CorrelationProperty>> propertiesList =
                    mySet.getProperties();
            //
            if (propertiesList == null) {
                return new Node[] {};
            }
            //
            ArrayList<CorrelationPropertyNode> nodesList =
                    new ArrayList<CorrelationPropertyNode>();
            for (WSDLReference<CorrelationProperty> propRef : propertiesList) {
                CorrelationProperty property = propRef.get();
                if (property != null) {
                    CorrelationPropertyNode cpNode =
                            new CorrelationPropertyNode(property, myLookup);
                    nodesList.add(cpNode);
                }
            }
            //
            Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
            return nodesArr;
        }
        
        public void reload() {
            setKeys(createKeys());
        }
    }
}
