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

/* $Id$ */

package org.netbeans.modules.form;

import java.util.EventObject;
import org.netbeans.modules.form.layoutsupport.LayoutSupport;

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
    private LayoutSupport oldLayoutSupport;
    private LayoutSupport newLayoutSupport;
    
    FormModelEvent(FormModel source) {
        super(source);
    }

    FormModelEvent(FormModel source,
                   RADComponent metacomp,
                   String propName, Object propOldVal, Object propNewVal) {
        this(source);
        component = metacomp;
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
    }

    FormModelEvent(FormModel source,
                   RADVisualContainer metacont,
                   LayoutSupport oldLayoutSupp, LayoutSupport newLayoutSupp) {
        this(source);
        container = metacont;
        oldLayoutSupport = oldLayoutSupp;
        newLayoutSupport = newLayoutSupp;
    }

    ComponentContainer getContainer() {
        return container;
    }

    RADComponent getComponent() {
        return component;
    }

    String getPropertyName() {
        return propertyName;
    }

    Object getPropertyOldValue() {
        return propertyOldValue;
    }

    Object getPropertyNewValue() {
        return propertyNewValue;
    }

    LayoutSupport getOldLayoutSupport() {
        return oldLayoutSupport;
    }

    LayoutSupport getNewLayoutSupport() {
        return newLayoutSupport;
    }
}
