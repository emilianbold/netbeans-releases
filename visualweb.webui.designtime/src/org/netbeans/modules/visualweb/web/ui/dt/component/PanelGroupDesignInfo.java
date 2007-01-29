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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import com.sun.rave.designtime.*;
import com.sun.rave.designtime.markup.*;
import com.sun.rave.web.ui.component.PanelGroup;
import com.sun.rave.web.ui.component.Tab;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * DesignInfo for the {@link com.sun.web.ui.dt.component.PanelGroup} component.
 *
 * @author gjmurphy
 */
public class PanelGroupDesignInfo extends AbstractDesignInfo implements MarkupDesignInfo {

    public PanelGroupDesignInfo() {
        super(PanelGroup.class);
    }

    public Result beanCreatedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if(childClass.equals(PanelGroup.class))
            return true;
        if (childClass.getName().equals("org.netbeans.modules.visualweb.xhtml.Jsp_Directive_Include"))
            return false;
        return super.acceptChild(parentBean, childBean, childClass);
    }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if(Tab.class.isAssignableFrom(parentBean.getInstance().getClass()))
            return true;
        if(PanelGroup.class.isAssignableFrom(parentBean.getInstance().getClass()))
            return true;
        return super.acceptParent(parentBean, childBean, childClass);
    }

    public void customizeRender(MarkupDesignBean markupDesignBean, MarkupRenderContext renderContext) {
        DocumentFragment documentFragment = renderContext.getDocumentFragment();
        MarkupPosition begin = renderContext.getBeginPosition();
        MarkupPosition end = renderContext.getEndPosition();

        if (begin == end || markupDesignBean.getChildBeanCount() > 0) {
            return;
        }

        assert begin.getUnderParent() == end.getUnderParent();
        Node child = begin.getBeforeSibling();
        Node stop = end.getBeforeSibling();

        // Draw a rave design border around the panel
        for (child = begin.getBeforeSibling(); child != stop; child = child.getNextSibling()) {
            if (child instanceof Element) {
                Element e = (Element)child;
                String styleClass = e.getAttribute("class"); //NOI18N
                styleClass = styleClass == null ? "" : styleClass;
                e.setAttribute("class", styleClass + " rave-design-border"); // NOI18N
                break;
            }
        }
    }

}
