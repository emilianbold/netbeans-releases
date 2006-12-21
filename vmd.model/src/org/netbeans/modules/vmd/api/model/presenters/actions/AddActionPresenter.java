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



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.vmd.api.model.ComponentDescriptor;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Karol Harezlak
 */
public abstract class AddActionPresenter extends Presenter {

    public static final String ADD_ACTION = NbBundle.getMessage(AddActionPresenter.class, "NAME_AddActionPresenter"); //NOI18N

    // TODO - move this method to a ActionsPresenterFactory class and hide this class
    public static final Presenter create(Integer order, TypeID ... types) {
        return create(null, order, types);
    }

    // TODO - move this method to a ActionsPresenterFactory class and hide this class
    public final static Presenter create(final String name, final int order,final TypeID ... types) {
        
        return new AddActionPresenter() {
            public Integer getOrder() {
                return order;
            }
            
            public String getName(){
                return name;
            }
            
            public AddActionItem[] getAddActionItems() {
                List<Action> actions = new ArrayList<Action>();
                DesignDocument document = getComponent().getDocument();
                for (TypeID type : types) {
                    for (ComponentDescriptor descriptor : document.getDescriptorRegistry().getComponentDescriptors()){
                        if (getComponent().getDocument().getDescriptorRegistry().isInHierarchy(type, descriptor.getTypeDescriptor().getThisType())) {
                            for (ComponentProducer producer : document.getDescriptorRegistry().getComponentProducers()) {
                                if (producer.getComponentTypeID().equals(descriptor.getTypeDescriptor().getThisType()) && AcceptSupport.isAcceptable(getComponent(), producer)){
                                    actions.add(AddActionItem.getInstance(getComponent(), producer));
                                }
                            }
                        }
                    }
                }
                Collections.sort(actions, ACTIONS_COMPARATOR);
                
                return actions.toArray( new AddActionItem[actions.size()]);
            }
            
        };
    }
    
    public abstract Integer getOrder();
    
    public abstract String getName();
    
    public abstract AddActionItem[] getAddActionItems();

    // TODO - move to ActionsPresenterFactory or ActionsSupport class
    protected static final Comparator<Action> ACTIONS_COMPARATOR = new Comparator<Action>() {
        public int compare(Action s1, Action s2) {
            String name1 = s1.getValue(Action.NAME).toString();
            String name2 = s2.getValue(Action.NAME).toString();
            
            return name1.compareTo(name2);
        }
    };
    
}
