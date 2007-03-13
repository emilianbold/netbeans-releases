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
package org.netbeans.modules.visualweb.xhtml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.markup.MarkupDesignBean;

import javax.faces.component.UIComponent;


/** DesignInfo ancestor for the various xhtml components
 *
 * @author Tor Norbye
 */
public abstract class XhtmlDesignInfo implements DesignInfo {
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        // f: beans (such as F_Verbatim) can be dropped anywhere
        Class clz = getBeanClass();
        if (clz.getName().startsWith("F_")) {
            return true;
        }

        // Refuse drops into JSF components that renders their own children, such as
        // grid panels -- you can't drop HTML components into these
        if (parentBean != null) {
            java.lang.Object o = parentBean.getInstance();

            if (o instanceof UIComponent) {
                UIComponent uic = (UIComponent)o;
                if (uic.getRendersChildren()) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        // Many tags are simply style tags that when inserted should
        // have a sample string inside - for example <a> or <h1>.
        // Children of this DesignInfo simply need to return a
        // String instead of null and not override beanCreated
        // to get this behavior.
        String s = getText();

        if (s != null) {
            try {
                addTextChild(bean, getText());
            } catch (Exception x) {
                x.printStackTrace();
            }
        }

        return Result.SUCCESS;
    }

    public Result beanPastedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        return Result.SUCCESS;
    }

    /**
     * Override this method to return a string to have beanCreated create a single text node
     * child of the element when inserted.
     */
    protected String getText() {
        return null;
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return DisplayAction.EMPTY_ARRAY;
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return false;
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        return Result.SUCCESS;
    }

    public void beanContextActivated(DesignBean bean) {
    }

    public void beanContextDeactivated(DesignBean bean) {
    }

    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {
    }

    public void beanChanged(DesignBean bean) {
    }

    public void propertyChanged(com.sun.rave.designtime.DesignProperty prop,
        java.lang.Object oldValue) {
    }

    public void eventChanged(DesignEvent event) {
    }

    // Utility methods

    /** Look up the element for a given bean */
    protected Element getElement(DesignBean bean) {
        if (bean instanceof MarkupDesignBean) {
            MarkupDesignBean mlb = (MarkupDesignBean)bean;

            return mlb.getElement();
        }

        return null;
    }

    /** Add a text node child below the given tag's element */
    protected void addTextChild(DesignBean bean, String text) {
        // Don't look, Joe!
        // I need to access the actual Elements created by the
        // <h1>, so I can insert text inside of it
        Element element = getElement(bean);

        if (element != null) {
            addTextChild(element, text);
        }
    }

    protected void addTextChild(Element element, String text) {
        Document doc = element.getOwnerDocument();
        Node node;
        

        // XXX Commented out, seems to be a dummy code (see the next line).
//        if (doc instanceof RaveDocument) {
//            node = ((RaveDocument)doc).createJspxTextNode(text);
//        } else {
//            node = doc.createTextNode(text);
//        }
        node = doc.createTextNode(text);
        
        element.appendChild(node);
    }
}
