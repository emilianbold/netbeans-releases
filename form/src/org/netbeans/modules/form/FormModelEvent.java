/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.EventObject;
import org.netbeans.modules.form.layoutsupport.LayoutSupportDelegate;

/**
 *
 * @author Tran Duc Trung
 */

public class FormModelEvent extends EventObject
{
    private RADComponent component;
    private ComponentContainer container;
    private String propertyName;
    private Object propertyOldValue;
    private Object propertyNewValue;
    private LayoutSupportDelegate oldLayoutSupport;
    private LayoutSupportDelegate newLayoutSupport;

    FormModelEvent(FormModel source) {
        super(source);
    }

    FormModelEvent(FormModel source,
                   RADComponent metacomp,
                   String propName, Object propOldVal, Object propNewVal) {
        this(source);
        if (component != null) {
            component = metacomp;
            deriveContainer(metacomp);
        }
        propertyName = propName;
        propertyOldValue = propOldVal;
        propertyNewValue = propNewVal;
    }

    FormModelEvent(FormModel source,
                   RADComponent metacomp,
                   ComponentContainer metacont) {
        this(source);
        component = metacomp;
        container = metacont;
    }

    FormModelEvent(FormModel source,
                   RADComponent metacomp) {
        this(source);
        component = metacomp;
        deriveContainer(metacomp);
    }

    FormModelEvent(FormModel source,
                   ComponentContainer metacont) {
        this(source);
        container = metacont;
    }

    FormModelEvent(FormModel source,
                   RADVisualContainer metacont,
                   LayoutSupportDelegate oldLayoutSupp,
                   LayoutSupportDelegate newLayoutSupp) {
        this(source);
        component = metacont;
        container = metacont;
        oldLayoutSupport = oldLayoutSupp;
        newLayoutSupport = newLayoutSupp;
    }

    private void deriveContainer(RADComponent comp) {
        if (comp.getParentComponent() instanceof ComponentContainer)
            container = (ComponentContainer) comp.getParentComponent();
        else if (comp.getParentComponent() == null)
            container = comp.getFormModel().getModelContainer();
    }

    // -------

    public final ComponentContainer getContainer() {
        return container;
    }

    public final RADComponent getComponent() {
        return component;
    }

    public final String getPropertyName() {
        return propertyName;
    }

    public final RADProperty getComponentProperty() {
        return component != null && propertyName != null ?
               component.getPropertyByName(propertyName) : null;
    }

    public final Object getPropertyOldValue() {
        return propertyOldValue;
    }

    public final Object getPropertyNewValue() {
        return propertyNewValue;
    }

    public final LayoutSupportDelegate getOldLayoutSupport() {
        return oldLayoutSupport;
    }

    public final LayoutSupportDelegate getNewLayoutSupport() {
        return newLayoutSupport;
    }
}
