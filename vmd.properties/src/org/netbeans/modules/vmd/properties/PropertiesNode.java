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


package org.netbeans.modules.vmd.properties;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.properties.GroupValue;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;


/**
 *
 * @author Karol Harezlak
 */
public class PropertiesNode extends AbstractNode{
    
    private WeakReference<DesignComponent> component;
    private WeakReference<DataObjectContext> context;
    private String displayName;
    
    public PropertiesNode(DataObjectContext context, DesignComponent component, Lookup lookup) {
        super(Children.LEAF, lookup);
        this.component = new WeakReference<DesignComponent>(component);
        this.context = new WeakReference<DataObjectContext>(context);
    }
    
    public Sheet createSheet() {
        if(component.get() == null)
            super.createSheet();
        return PropertiesNodesManager.getDefault(context.get()).getSheet(component.get());
    }
    
    public String getDisplayName() {
        if (component.get() == null)
            return super.getDisplayName();
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (component.get().getParentComponent() == null && component.get().getDocument().getRootComponent() != component.get())
                    return;
                displayName = InfoPresenter.getDisplayName(component.get());
            }
        });
        return displayName;
    }
    
    public void updateNode() {
        Object value = null;
        for (PropertySet set : getSheet().toArray()) {
            for (Property property : set.getProperties()) {
                value = null;
                try {
                    value = property.getValue();
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (value instanceof GroupValue) {
                    GroupValue oldValue = (GroupValue) value;
                    GroupValue newValue = new GroupValue(java.util.Arrays.asList(oldValue.getPropertyNames()));
                    for (java.lang.String propertyName : newValue.getPropertyNames()) {
                        newValue.putValue(propertyName, oldValue.getValue(propertyName));
                    }
                    firePropertyChange(property.getName(), oldValue, newValue);
                }
                if (value instanceof PropertyValue) {
                    PropertyValue oldValue = (PropertyValue) value;
                    PropertyValue newValue = null;
                    if (oldValue.getKind() == PropertyValue.Kind.ARRAY)
                        newValue = PropertyValue.createArray(oldValue.getType(), oldValue.getArray());
                    else  if (oldValue.getKind() == PropertyValue.Kind.ENUM)
                        newValue = PropertyValue.createValue(component.get().getDocument().getDocumentInterface().getProjectType(), oldValue.getType(), oldValue.getPrimitiveValue());
                    else  if (oldValue.getKind() == PropertyValue.Kind.NULL)
                        newValue = PropertyValue.createNull();
                    else  if (oldValue.getKind() == PropertyValue.Kind.REFERENCE)
                        newValue = PropertyValue.createComponentReference(oldValue.getComponent());
                    else  if (oldValue.getKind() == PropertyValue.Kind.USERCODE)
                        newValue = PropertyValue.createUserCode(oldValue.getUserCode());
                    else  if (oldValue.getKind() == PropertyValue.Kind.VALUE)
                        newValue = PropertyValue.createValue(component.get().getDocument().getDocumentInterface().getProjectType(), oldValue.getType(), oldValue.getPrimitiveValue());
                    if (newValue != null)
                        firePropertyChange(property.getName(), oldValue, newValue);
                }
            }
        }
    }
    
}



