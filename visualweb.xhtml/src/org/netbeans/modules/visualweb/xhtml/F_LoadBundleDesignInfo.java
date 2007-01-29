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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.event.DesignProjectListener;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupDesignContext;
import com.sun.rave.designtime.markup.MarkupPosition;
import com.sun.rave.propertyeditors.domains.ResourceBundlesDomain;

import java.util.BitSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DesignInfo for the F_LoadBundle component
 *
 * @author Craig McClanahan
 * @author gjmurphy
 */
public class F_LoadBundleDesignInfo extends XhtmlDesignInfo implements DesignProjectListener {

    /**
     * <p>Set the default value of the <code>var</code> property, based on the
     * number of <code>&lt;f:loadBundle&gt;</code> tags that exist in the page.
     * Also, set the <code>basename</code> property to a default resource
     * bundle name that should also be added to the project template.</p>
     *
     * @param bean {@link DesignBean} for the new component
     */
    public Result beanCreatedSetup(DesignBean bean) {
        bean.getProperty("var").setValue(var(bean));
        bean.getProperty("basename").setValue(basename(bean));
        // Attempt to make the load bundle component the first child of the
        // HTML "html" element, so that element attributes within the head can
        // reference the resource bundle
        if (bean.getDesignContext() instanceof MarkupDesignContext) {
            // Look for parent bean that corresponds to "html" markup element
            DesignBean parentBean = bean.getBeanParent();
            while (parentBean != null && parentBean instanceof MarkupDesignBean &&
                    !"html".equals(((MarkupDesignBean) parentBean).getElement().getLocalName())) {
                parentBean = parentBean.getBeanParent();
            }
            // If none found, and top of tree reached, check the siblings of the
            // original parent
            if (parentBean == null || !(parentBean instanceof MarkupDesignBean)) {
                DesignBean[] siblingBeans = bean.getBeanParent().getChildBeans();
                for (int i = 0; i < siblingBeans.length; i++) {
                    if (siblingBeans[i] instanceof MarkupDesignBean &&
                            "html".equals(((MarkupDesignBean) siblingBeans[i]).getElement().getLocalName()))
                        parentBean = siblingBeans[i];
                }
            }
            if (parentBean != null && parentBean instanceof MarkupDesignBean &&
                    "html".equals(((MarkupDesignBean) parentBean).getElement().getLocalName())) {
                final MarkupDesignContext designContext = (MarkupDesignContext) bean.getDesignContext();
                final Element finalParentElem = ((MarkupDesignBean) parentBean).getElement();
                final DesignBean finalParentBean = parentBean;
                final DesignBean thisBean = bean;
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        NodeList children = finalParentElem.getChildNodes();
                        Element firstChildElem = null;
                        for (int i = 0; i < children.getLength() && firstChildElem == null; i++) {
                            if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
                                firstChildElem = (Element) children.item(i);
                        }
                        if (firstChildElem != null) {
                            MarkupPosition position = new MarkupPosition(finalParentElem, firstChildElem);
                            if (designContext.canMoveBean(thisBean, finalParentBean, position))
                                designContext.moveBean(thisBean, finalParentBean, position);
                        }
                    }
                });
            }
        }
        return Result.SUCCESS;
    }

    public Class getBeanClass() {
        return F_LoadBundle.class;
    }

    public void contextActivated(com.sun.rave.designtime.DesignContext context) {
    }

    public void contextClosed(com.sun.rave.designtime.DesignContext context) {
    }

    public void contextDeactivated(com.sun.rave.designtime.DesignContext context) {
    }

    public void contextOpened(com.sun.rave.designtime.DesignContext context) {
    }
    
    /**
     * Return an appropriate default resource bundle name to be used for
     * initializing the <code>basename</code> property.
     *
     * @param bean {@link DesignBean} for the new component
     */
    private String basename(DesignBean bean) {
        ResourceBundlesDomain domain = new ResourceBundlesDomain();
        domain.setDesignProperty(bean.getProperty("basename"));
        com.sun.rave.propertyeditors.domains.Element[] elements = domain.getElements();
        String value = null;
        for (int i = 0; i < elements.length && value == null; i++)
            value = (String) elements[i].getValue();
        return value;
    }

    // Base name for generated value for "var" property
    private static final String BASE_NAME = "messages";
    
    /**
     * Return a unique value to be used to initialize the <code>var</code>
     * property of a new component.
     *
     * @param bean {@link DesignBean} for the new component
     */
    private String var(DesignBean bean) {
        // Identify all the suffixes that have already been used
        BitSet used = new BitSet();
        DesignBean beans[] =
          bean.getDesignContext().getBeansOfType(getBeanClass());
        for (int i = 0; i < beans.length; i++) {
            String var = (String) beans[i].getProperty("var").getValue(); //NOI18N
            if ((var == null) || !var.startsWith(BASE_NAME)) {
                continue;
            }
            String suffix = var.substring(BASE_NAME.length());
            try {
                int j = Integer.valueOf(suffix).intValue();
                if (j >= 0) {
                    used.set(j);
                }
            } catch (NumberFormatException e) {
                ;
            }
        }
        // Use the first positive suffix not yet used
        int n = 1;
        while (true) {
            if (used.get(n)) {
                n++;
                continue;
            }
            return BASE_NAME + n;
        }
    }

}
