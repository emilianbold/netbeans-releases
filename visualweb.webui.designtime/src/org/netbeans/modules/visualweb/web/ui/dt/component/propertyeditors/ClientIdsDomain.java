/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
