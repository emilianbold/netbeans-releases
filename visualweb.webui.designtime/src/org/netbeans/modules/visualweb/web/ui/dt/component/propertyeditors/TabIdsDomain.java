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
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.propertyeditors.domains.AttachedDomain;
import com.sun.rave.propertyeditors.domains.Element;
import com.sun.rave.web.ui.component.Tab;
import com.sun.rave.web.ui.component.TabSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import javax.faces.component.UIComponent;

/**
 * Domain of identifiers of all {@link com.sun.rave.web.ui.dt.component.Tab} components
 * within the currently selected {@link com.sun.rave.web.ui.dt.component.TabSet}
 * component. Used to provide an enumeration of values for the <code>selected</code>
 * property. Iterates over all tabs in the tabSet in document order.
 *
 * @author gjmurphy
 */
public class TabIdsDomain extends AttachedDomain {

    public Element[] getElements() {
        // If we have not been attached yet, there is nothing we can do
        // except return an empty list
        if (this.getDesignProperty() == null)
            return Element.EMPTY_ARRAY;
        // Construct a list of all tab descendants
        Stack beanStack = new Stack();
        beanStack.push(this.getDesignProperty().getDesignBean());
        List beanList = new ArrayList();
        while (!beanStack.isEmpty()) {
            DesignBean bean = (DesignBean) beanStack.pop();
            if (bean.getInstance() instanceof Tab)
                beanList.add(bean);
            DesignBean[] childBeans = bean.getChildBeans();
            for (int i = bean.getChildBeanCount() - 1; i >= 0; i--)
                beanStack.push(childBeans[i]);
        }
        if (beanList.size() == 0)
            return Element.EMPTY_ARRAY;
        // Construct an array of elements from the labels and identifiers of
        // the retained tab components
        Element elements[] = new Element[beanList.size()];
        for (int i = 0; i < elements.length; i++) {
            DesignBean bean = (DesignBean) beanList.get(i);
            Tab tab = (Tab) bean.getInstance();
            String id = tab.getId();
            String text = tab.getText().toString();
            String gutter = "";
            if (bean.getBeanParent().getInstance() instanceof Tab) {
                gutter = "  ";
                if (bean.getBeanParent().getBeanParent().getInstance() instanceof Tab) {
                    gutter = "    ";
                }
            }
            if (text == null)
                elements[i] = new Element(id, gutter + id);
            else
                elements[i] = new Element(id, gutter + text + " (" + id + ")");
        }
        return elements;

    }

}
