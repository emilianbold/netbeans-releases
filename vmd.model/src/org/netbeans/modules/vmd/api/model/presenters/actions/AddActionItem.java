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
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSupport;
import org.openide.util.Utilities;

/**
 *
 * @author Karol Harezlak
 */

public abstract class AddActionItem extends AbstractAction {
    
    public static final String TYPEID_KEY = "typeID"; //NOI18N
    
    private static Map<String, AddActionItem> instances = new WeakHashMap<String, AddActionItem>();
    
    public static final AddActionItem getInstance(final DesignComponent component, final ComponentProducer producer) {
        AddActionItem action = instances.get(producer.getProducerID());
        if (action != null) {
            action.resolveAction(component);
            return action;
        }
        action = create(component, producer);
        instances.put(producer.getProducerID(), action);
        
        return action;
    }
    
    private ImageIcon icon;
    
    protected AddActionItem(TypeID typeID) {
        putValue(TYPEID_KEY, typeID);
    }
    
    private AddActionItem(TypeID typeID, DesignComponent component, ComponentProducer producer) {
        icon = new ImageIcon(Utilities.loadImage(producer.getPaletteDescriptor().getSmallIcon()));
        putValue(TYPEID_KEY, typeID);
        putValue(Action.NAME, producer.getPaletteDescriptor().getDisplayName());
        putValue(Action.SMALL_ICON, icon);
        resolveAction(component);
    }
    
    public abstract void resolveAction(DesignComponent component);
    
    private static AddActionItem create(final DesignComponent component, final ComponentProducer producer) {
        return new AddActionItem(producer.getComponentTypeID(), component, producer) {
            private DesignComponent component;
            
            public void actionPerformed(ActionEvent e) {
                if (producer == null)
                    throw new IllegalArgumentException("Argument typeID cant be null"); //NOI18N
                this.component.getDocument().getTransactionManager().writeAccess(new Runnable() {
                    public void run() {
                        AcceptSupport.accept(component, producer);
                    }
                });
                
            }
            
            public void resolveAction(DesignComponent component) {
                this.component = component;
            }
        };
        
    }
    
}
