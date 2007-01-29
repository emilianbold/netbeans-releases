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
package org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors;

import com.sun.rave.designtime.DesignBean;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.faces.component.UIComponent;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.propertyeditors.domains.AttachedDomain;
import com.sun.rave.propertyeditors.domains.Element;
import javax.faces.context.FacesContext;

/**
 * Domain of identifiers of components in scope that meet the criteria of the
 * abstract method <code>isDomainComponent(UIComponent)</code>.
 */
public abstract class ClientIdsDomain extends AttachedDomain {

    /**
     * Returns a list of all the non-<code>null</code> component client identifiers.
     */
    public Element[] getElements() {

        // If we have not been attached yet, there is nothing we can do
        // except return an empty list
        if (getDesignProperty() == null) {
            return Element.EMPTY_ARRAY;
        }

        // Scan all the beans on this page and accumulate identifiers of all
        // of them that implement ActionSource or EditableValueHolder
        Set set = new TreeSet();
        DesignBean designBeans[] =
          getDesignProperty().getDesignBean().getDesignContext().
          getBeansOfType(UIComponent.class);
        if (designBeans == null) {
            return Element.EMPTY_ARRAY;
        }
        for (int i = 0; i < designBeans.length; i++) {
            Object instance = designBeans[i].getInstance();
            if (instance instanceof UIComponent && isDomainComponent((UIComponent) instance)){
                String id = getId(designBeans[i]);
                if (id != null)
                    set.add(id);
            }
        }

        // Construct a list of elements of the retained identifiers
        Element elements[] = new Element[set.size()];
        Iterator ids = set.iterator();
        int n = 0;
        while (ids.hasNext()) {
            elements[n++] = new Element((String) ids.next());
        }
        return elements;

    }

    protected abstract boolean isDomainComponent(UIComponent component);

    protected String getId(DesignBean bean) {
        if (hasTableParent(bean)) {
            return null;
        }
        Object instance = bean.getInstance();
        if (!(instance instanceof UIComponent)) {
            return null;
        }
        UIComponent uic = (UIComponent)instance;
        DesignContext dcontext = getDesignProperty().getDesignBean().getDesignContext();
        FacesContext fcontext = ((FacesDesignContext)dcontext).getFacesContext();
        return uic.getClientId(fcontext);
    }

    private boolean hasTableParent(DesignBean bean) {
        DesignBean parent = bean.getBeanParent();
        if (parent == null) {
            return false;
        }
        Object instance = parent.getInstance();
        if (instance instanceof javax.faces.component.UIData || instance instanceof com.sun.rave.web.ui.component.TableRowGroup) {
            return true;
        }
        return hasTableParent(parent);
    }

}
