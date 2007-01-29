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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.web.ui.component.Form;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;

/**
 * DesignInfo for the {@link org.netbeans.modules.visualweb.web.ui.dt.component.Form} component.
 *
 * @author Matt
 * @author gjmurphy
 */
public class FormDesignInfo extends AbstractDesignInfo {

    private static final String ID_SEP = String.valueOf(NamingContainer.SEPARATOR_CHAR);

    /** Creates a new instance of FormDesignInfo */
    public FormDesignInfo() {
        super(Form.class);
    }

    /**
     * Allow form anywhere, so long as parent is not a form and the parent has
     * no form ancestor.
     */
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        DesignBean thisBean = parentBean;
        while (thisBean.getBeanParent() != null) {
            if (thisBean.getInstance() instanceof Form)
                return false;
            thisBean = thisBean.getBeanParent();
        }
        return super.isSunWebUIContext(parentBean);
    }

    /**
     * <p>Designtime version of
     * <code>Form.getFullyQualifiedId(UIComponent)</code> for webui.
     */
    /*
     * Be sure to keep this method in sync with the versions in
     * <code>org.netbeans.modules.visualweb.web.ui.dt.component.Form</code> (in webui) and
     * <code>javax.faces.component.html.HtmlFormDesignInfo</code>
     * (in jsfcl).</p>
     */
    public static String getFullyQualifiedId(DesignBean bean) {
        if (bean == null) {
            return null;
        }
        Object beanInstance = bean.getInstance();
        if (! (beanInstance instanceof UIComponent)) {
            return null;
        }
        if (beanInstance instanceof Form) {
            return ID_SEP;
        }
        String compId = bean.getInstanceName();
        if (compId == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(compId);
        DesignBean currentBean = bean.getBeanParent();
        boolean formEncountered = false;
        while (currentBean != null) {
            sb.insert(0, ID_SEP);
            if (currentBean.getInstance() instanceof Form) {
                formEncountered = true;
                break;
            }
            else {
                String currentCompId = currentBean.getInstanceName();
                if (currentCompId == null) {
                    return null;
                }
                sb.insert(0, currentCompId);
            }
            currentBean = currentBean.getBeanParent();
        }
        if (formEncountered) {
            return sb.toString();
        }
        else {
            return null;
        }
    }
}
