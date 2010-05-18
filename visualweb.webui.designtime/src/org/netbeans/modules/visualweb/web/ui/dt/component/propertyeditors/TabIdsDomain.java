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
