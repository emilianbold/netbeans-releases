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
package com.sun.rave.propertyeditors.domains;

import com.sun.rave.designtime.DesignBean;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.faces.component.UIComponent;

/**
 * Specialized domain, representing all JSF components in the current context
 * (ie. the current page), for which <code>isDomainComponent(UIComponent)</code>
 * returns true. The value for each element in the domain is a component
 * identifier.
 */
public abstract class ComponentIdsDomain extends AttachedDomain {

    public Element[] getElements() {
        if (getDesignProperty() == null) {
            return Element.EMPTY_ARRAY;
        }
        Set set = new TreeSet();
        DesignBean designBeans[] =
                getDesignProperty().getDesignBean().getDesignContext().getBeansOfType(UIComponent.class);
        if (designBeans == null) {
            return Element.EMPTY_ARRAY;
        }
        for (int i = 0; i < designBeans.length; i++) {
            Object instance = designBeans[i].getInstance();
            if (instance instanceof UIComponent && isDomainComponent((UIComponent) instance)){
                String id = ((UIComponent) instance).getId();
                if (id != null)
                    set.add(new Element(id));
            }
        }
        return (Element[]) set.toArray(new Element[set.size()]);
    }

    /**
     * This method is called for each JSF component found in the current context.
     * If <code>true</code> is returned, the component's identifier will be added to
     * the domain.
     */
    protected abstract boolean isDomainComponent(UIComponent component);

}
