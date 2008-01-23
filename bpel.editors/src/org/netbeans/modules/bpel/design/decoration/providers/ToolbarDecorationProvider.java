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

package org.netbeans.modules.bpel.design.decoration.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.decoration.components.ContextToolBar;
import org.netbeans.modules.bpel.design.decoration.components.ContextToolBarButton;
import org.netbeans.modules.bpel.design.selection.DiagramSelectionListener;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.actions.AddCatchAction;
import org.netbeans.modules.bpel.nodes.actions.AddCatchAllAction;
import org.netbeans.modules.bpel.nodes.actions.AddCompensationHandlerAction;
import org.netbeans.modules.bpel.nodes.actions.AddCorrelationSetAction;
import org.netbeans.modules.bpel.nodes.actions.AddElseIfAction;
import org.netbeans.modules.bpel.nodes.actions.AddEventHandlersAction;
import org.netbeans.modules.bpel.nodes.actions.AddFaultHandlersAction;
import org.netbeans.modules.bpel.nodes.actions.AddOnAlarmAction;
import org.netbeans.modules.bpel.nodes.actions.AddOnEventAction;
import org.netbeans.modules.bpel.nodes.actions.AddOnMessageAction;
import org.netbeans.modules.bpel.nodes.actions.AddTerminationHandlerAction;
import org.netbeans.modules.bpel.nodes.actions.AddVariableAction;
import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.nodes.actions.OpenPartnerLinkInEditor;
import org.netbeans.modules.bpel.nodes.actions.ShowPropertyEditorAction;
import org.openide.actions.NewAction;
import org.openide.awt.Actions;
import org.openide.nodes.Node;

/**
 *
 * @author aa160298
 */
public class ToolbarDecorationProvider extends DecorationProvider implements
        DiagramSelectionListener {
    

    
    
    
    public ToolbarDecorationProvider(DesignView designView) {
        super(designView);
        getDesignView().getSelectionModel().addSelectionListener(this);
    }
    
    
    public Decoration getDecoration(BpelEntity entity) {
        
        UniqueId entityID = entity.getUID();
        UniqueId selectedEntityID = getDesignView().getSelectionModel().getSelectedID();
        
        if (entityID!= null && entityID.equals(selectedEntityID)) {
            List<Descriptor> descriptors = new ArrayList<Descriptor>();
            
            List<Action> actions = getContextActions(entity);
            
            ContextToolBar toolBar = new ContextToolBar();
            
            Action collapseExpandAction = getDesignView()
                    .getCollapseExpandDecorationProvider()
                    .createCollapseExpandAction(entity);
            
            if (collapseExpandAction != null) {
                toolBar.add(new ContextToolBarButton(collapseExpandAction));
            }
            
            for (int i = 0; i < actions.size(); i++) {
                Action a = actions.get(i);
                JButton button = new ContextToolBarButton(CONTEXT_ICONS.get(a.getClass()));
                Actions.connect(button, a);
                toolBar.add(button);
            }
            
            if (!actions.isEmpty() || (collapseExpandAction != null)) {
                ComponentsDescriptor components = new ComponentsDescriptor();
                components.add(toolBar);
                descriptors.add(components);
            }
            
            return new Decoration(descriptors.toArray(new Descriptor[descriptors
                    .size()]));
        }
        return null;
    }
    
    
    public void selectionChanged(BpelEntity oldSelection, BpelEntity newSelection) {
            fireDecorationChanged();
    }
    
    
    
    private Map<Class, Action> extractActions(BpelEntity entity) {
        Map<Class, Action> result = new HashMap<Class, Action>();
        
        Node node = getDesignView().getNodeForPattern(
                getDesignView().getModel().getPattern(entity)
        );
        
        if ((node == null) || !(node instanceof BpelNode)) {
            return result;
        }
        
        BpelNode bpelNode = (BpelNode) node;
        
        Action[] actions = node.getActions(true);
        
        if (actions == null){
            return result;
        }
        
        for (Action action : actions) {
            if (action == null) {
                continue;
            }
            
            if (action instanceof NewAction) {
                List<BpelNodeAction> addActions = bpelNode.getAddActions();
                if (addActions != null) {
                    for (Action addAction : addActions) {
                        if (addAction == null) {
                            continue;
                        }
                        result.put(addAction.getClass(), addAction);
                    }
                }
            } else {
                result.put(action.getClass(), action);
            }
        }
        
        return result;
    }
    
    
    private List<Action> getContextActions(BpelEntity entity) {
        List<Action> result = new ArrayList<Action>();
        
        Map<Class, Action> classToActionMap = extractActions(entity);
        
        List<Class> contextActionsClasses = null;
        
        for (Class bpelEntityClass : CONTEXT_ACTIONS.keySet()) {
            if (bpelEntityClass.isAssignableFrom(entity.getClass())) {
                contextActionsClasses = CONTEXT_ACTIONS.get(bpelEntityClass);
                break;
            }
        }
        
        if (contextActionsClasses == null) {
            return result;
        }
        
        for (Class contextActionClass : contextActionsClasses) {
            Action action = classToActionMap.get(contextActionClass);
            if (action != null) {
                result.add(action);
            }
        }
        
        return result;
    }
    
   
    private static final Map<Class, List<Class>> CONTEXT_ACTIONS;
    private static final Map<Class, Icon> CONTEXT_ICONS;
    
    
    private static Icon loadPNGIcon(String name) {
        return new ImageIcon(Decoration.class
                .getResource("resources/" + name + ".png")); // NOI18N
    }
    
    
    static {
        CONTEXT_ACTIONS = new HashMap<Class, List<Class>>();
        
        CONTEXT_ACTIONS.put(Invoke.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(Invoke.class).add(ShowPropertyEditorAction.class);
        CONTEXT_ACTIONS.get(Invoke.class).add(AddCatchAllAction.class);
        CONTEXT_ACTIONS.get(Invoke.class).add(AddCatchAction.class);
        CONTEXT_ACTIONS.get(Invoke.class).add(AddCompensationHandlerAction.class);

        CONTEXT_ACTIONS.put(Receive.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(Receive.class).add(ShowPropertyEditorAction.class);
        
        CONTEXT_ACTIONS.put(Reply.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(Reply.class).add(ShowPropertyEditorAction.class);
        
        CONTEXT_ACTIONS.put(PartnerLink.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(PartnerLink.class).add(ShowPropertyEditorAction.class);
        CONTEXT_ACTIONS.get(PartnerLink.class).add(OpenPartnerLinkInEditor.class);
        
        CONTEXT_ACTIONS.put(If.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(If.class).add(AddElseIfAction.class);
        
        CONTEXT_ACTIONS.put(Process.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(Process.class).add(AddVariableAction.class);
        CONTEXT_ACTIONS.get(Process.class).add(AddCorrelationSetAction.class);
        CONTEXT_ACTIONS.get(Process.class).add(AddEventHandlersAction.class);
        CONTEXT_ACTIONS.get(Process.class).add(AddFaultHandlersAction.class);
        
        CONTEXT_ACTIONS.put(Scope.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(Scope.class).add(AddVariableAction.class);
        CONTEXT_ACTIONS.get(Scope.class).add(AddCorrelationSetAction.class);
        CONTEXT_ACTIONS.get(Scope.class).add(AddEventHandlersAction.class);
        CONTEXT_ACTIONS.get(Scope.class).add(AddCompensationHandlerAction.class);
        CONTEXT_ACTIONS.get(Scope.class).add(AddTerminationHandlerAction.class);
        CONTEXT_ACTIONS.get(Scope.class).add(AddFaultHandlersAction.class);
        
        CONTEXT_ACTIONS.put(Pick.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(Pick.class).add(AddOnMessageAction.class);
        CONTEXT_ACTIONS.get(Pick.class).add(AddOnAlarmAction.class);
        
        CONTEXT_ACTIONS.put(EventHandlers.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(EventHandlers.class).add(AddOnEventAction.class);
        CONTEXT_ACTIONS.get(EventHandlers.class).add(AddOnAlarmAction.class);
        
        CONTEXT_ACTIONS.put(FaultHandlers.class, new ArrayList<Class>());
        CONTEXT_ACTIONS.get(FaultHandlers.class).add(AddCatchAllAction.class);
        CONTEXT_ACTIONS.get(FaultHandlers.class).add(AddCatchAction.class);
        
        CONTEXT_ICONS = new HashMap<Class, Icon>();
        CONTEXT_ICONS.put(AddCatchAction.class, loadPNGIcon("add_catch")); // NOI18N
        CONTEXT_ICONS.put(AddCatchAllAction.class, loadPNGIcon("add_catch_all")); // NOI18N
        CONTEXT_ICONS.put(AddElseIfAction.class, loadPNGIcon("add_else_if")); // NOI18N
        CONTEXT_ICONS.put(AddCompensationHandlerAction.class, loadPNGIcon("add_compensation_handler")); // NOI18N
        CONTEXT_ICONS.put(AddVariableAction.class, loadPNGIcon("add_variable")); // NOI18N
        CONTEXT_ICONS.put(AddCorrelationSetAction.class, loadPNGIcon("add_correlation_set")); // NOI18N
        CONTEXT_ICONS.put(AddEventHandlersAction.class, loadPNGIcon("add_event_handlers")); // NOI18N
        CONTEXT_ICONS.put(AddTerminationHandlerAction.class, loadPNGIcon("add_termination_handler")); // NOI18N
        CONTEXT_ICONS.put(AddFaultHandlersAction.class, loadPNGIcon("add_fault_handlers")); // NOI18N
        CONTEXT_ICONS.put(AddOnMessageAction.class, loadPNGIcon("add_on_message")); // NOI18N
        CONTEXT_ICONS.put(AddOnAlarmAction.class, loadPNGIcon("add_on_alarm")); // NOI18N
        CONTEXT_ICONS.put(AddOnEventAction.class, loadPNGIcon("add_on_event")); // NOI18N
        CONTEXT_ICONS.put(ShowPropertyEditorAction.class, loadPNGIcon("edit_properties")); // NOI18N
        CONTEXT_ICONS.put(OpenPartnerLinkInEditor.class, loadPNGIcon("open_wsdl")); // NOI18N
    }
}
