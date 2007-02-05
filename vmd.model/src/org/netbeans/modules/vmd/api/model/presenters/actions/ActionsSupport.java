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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.api.model.presenters.actions;


import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Karol Harezlak
 */
public final class ActionsSupport {
    
    static final String SEPERATOR_KEY = "#SEPARATOR#"; //NOI18N
    static final Set<Action> actionsToDelete = new HashSet<Action>();
    
    private static final TypeID[] EMPTY_TYPEID_ARRAY = new TypeID[0];
    
    public static Action[] createAddActionArray(final DesignComponent component) {
        return createAddActionArray(component, EMPTY_TYPEID_ARRAY);
    }
    
    public static Action[] createAddActionArray(final DesignComponent component,final TypeID... filtersTypeID) {
        final List<Action> actions = new ArrayList<Action>();
        
        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (! component.getPresenters(AddActionPresenter.class).isEmpty()) {
                    Map <Integer, Map<String, List<Action>>> sortedMaps = new TreeMap<Integer, Map<String, List<Action>>>();
                    DescriptorRegistry registry = component.getDocument().getDescriptorRegistry();
                    
                    //Sorting presenters by the order and name of the AddActionPresenter
                    for (AddActionPresenter presenter : component.getPresenters(AddActionPresenter.class)) {
                        AddActionItem[] addActionItems = presenter.getAddActionItems();
                        if (addActionItems == null || addActionItems.length < 1)
                            continue;
                        
                        Integer order = presenter.getOrder() == null ? Integer.MAX_VALUE : presenter.getOrder();
                        
                        if (sortedMaps.get(order) == null) {
                            sortedMaps.put(order, new TreeMap<String, List<Action>>());
                            sortedMaps.get(order).put(presenter.getName(), new ArrayList<Action>(Arrays.asList(addActionItems)));
                        } else if (sortedMaps.get(order) != null && sortedMaps.get(order).get(presenter.getName()) == null ) {
                            sortedMaps.get(order).put(presenter.getName(), new ArrayList<Action>(Arrays.asList(addActionItems)));
                        } else
                            sortedMaps.get(order).get(presenter.getName()).addAll(Arrays.asList(addActionItems));
                    }
                    // Creating of list of AddActionPresenter action list with separators and filtring it is necessary
                    for (Map<String, List<Action>> sortedMap : sortedMaps.values()) {
                        for (String name : sortedMap.keySet()) {
                            if (filtersTypeID.length == 0) {
                                if (name != null)
                                    actions.add(new SeperatorAction(name));
                                actions.addAll(sortedMap.get(name));
                            } else {
                                boolean removeCategory = true;
                                if (name != null)
                                    actions.add(new SeperatorAction(name));
                                for (Action  action : sortedMap.get(name)) {
                                    for (TypeID typeID : filtersTypeID) {
                                        TypeID actionTypeID = (TypeID) action.getValue(AddActionItem.TYPEID_KEY);
                                        if (registry.isInHierarchy(typeID, actionTypeID)) {
                                            actions.add(action);
                                            removeCategory = false;
                                        }
                                    }
                                }
                                if (name != null && removeCategory)
                                    actions.remove(actions.size() - 1);
                            }
                        }
                    }
                }
            }
        });
        
        return actions.toArray(new Action[actions.size()]);
    }
    
    public static Action[] createActionsArray(final DesignComponent component) {
        final List<Action> actions = new ArrayList<Action>();
        
        component.getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                Map <Integer, List<Action>> sortedLists = new TreeMap<Integer, List<Action>>();
                
                DesignDocument document = null;
                
                for (ActionsPresenter presenter : component.getPresenters(ActionsPresenter.class)) {
                    List<Action> actions = presenter.getActions();
                    if (actions == null || actions.isEmpty())
                        continue;
                    
                    DesignComponent relatedComponent = presenter.getRelatedComponent();
                    assert relatedComponent != null : "No related component for ActionsPresenter "+ presenter ;//NOI18N
                    for (Action action : actions) {
                        if (action instanceof ActionContext)
                            ((ActionContext) action).setComponent(relatedComponent);
                    }
                    
                    if (document != null)
                        assert document == relatedComponent.getDocument();
                    else
                        document = relatedComponent.getDocument();
                    
                    Integer order = presenter.getOrder();
                    if (order == null)
                        order = Integer.MAX_VALUE;
                    
                    List<Action> list = sortedLists.get(order);
                    if (list == null)
                        sortedLists.put(order, new ArrayList<Action>(actions));
                    else
                        list.addAll(actions);
                }
                
                for (List<Action> list : sortedLists.values()) {
                    actions.addAll(list);
                    actions.add(null);
                }
            }
        });
        
        return actions.toArray(new Action[actions.size()]);
    }
  
    private static class SeperatorAction extends  AbstractAction {
        
        public SeperatorAction(String name){
            putValue(Action.NAME, name);
            putValue(SEPERATOR_KEY, SEPERATOR_KEY);
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
        public boolean isEnabled() {
            return false;
        }
        
    }
    
}
