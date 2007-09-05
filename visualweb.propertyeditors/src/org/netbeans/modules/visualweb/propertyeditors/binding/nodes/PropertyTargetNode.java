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
package org.netbeans.modules.visualweb.propertyeditors.binding.nodes;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNode;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetPanel;
import org.netbeans.modules.visualweb.propertyeditors.binding.PropertyBindingHelper;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;

public class PropertyTargetNode extends BindingTargetNode {
    private static Bundle bundle = Bundle.getBundle(PropertyTargetNode.class);

    public PropertyTargetNode(BindingTargetNode parent, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
        super(parent);
        this.bean = bean;
        this.propPath = propPath;
        this.propInstance = propInstance;
        this.displayTextEnabled = getDisplayText(true);
        this.displayTextDisabled = getDisplayText(false);
    }

    protected DesignBean bean;
    public DesignBean getBean() {
        return bean;
    }
    protected PropertyDescriptor[] propPath;
    public PropertyDescriptor[] getPropPath() {
        return propPath;
    }
    protected Object propInstance;
    public Object getPropInstance() {
        return propInstance;
    }

    public boolean lazyLoad() {
        lazyLoadCustomTargetNodes();
        if (propPath == null) {
            lazyLoadBeanTargetNodes();
        }
        if (isValidBindingTarget()) {
            lazyLoadPropertyTargetNodes();
        }
        return true;
    }
    public void lazyLoadCustomTargetNodes() {
        // subclasses can put their stuff here
    }
    public void lazyLoadBeanTargetNodes() {
        if (bean.isContainer()) {
            DesignBean[] kids = bean.getChildBeans();
            for (int i = 0; kids != null && i < kids.length; i++) {
                super.add(_createTargetNode(this, kids[i], null, kids[i].getInstance()));
            }
        }
    }
    public void lazyLoadPropertyTargetNodes() {
        try {
            BeanInfo bi = Introspector.getBeanInfo(getTargetTypeClass());
            PropertyDescriptor[] pds = bi.getPropertyDescriptors();
            for (int i = 0; pds != null && i < pds.length; i++) {
                if ((pds[i].getReadMethod() != null) && !pds[i].getReadMethod().getName().equals("getClass")) {
                    ArrayList pdList = new ArrayList();
                    for (int j = 0; propPath != null && j < propPath.length; j++) {
                        pdList.add(propPath[j]);
                    }
                    pdList.add(pds[i]);
                    PropertyDescriptor[] pda = (PropertyDescriptor[])pdList.toArray(new PropertyDescriptor[pdList.size()]);
                    BindingTargetNode btn = _createTargetNode(this, bean, pda, null);
                    super.add(btn);
                }
            }
        }
        catch (Exception x) {
            x.printStackTrace();
        }
    }
    protected String displayTextEnabled;
    protected String displayTextDisabled;
    public String getDisplayText(boolean enableNode) {
        if (enableNode && displayTextEnabled != null) {
            return displayTextEnabled;
        }
        else if (!enableNode && displayTextDisabled != null) {
            return displayTextDisabled;
        }
        PropertyDescriptor pd = (propPath != null && propPath.length > 0) ?
            propPath[propPath.length - 1] : null;
        StringBuffer sb = new StringBuffer();
        sb.append("<html>"); //NOI18N
        if (!enableNode) {
            sb.append("<font color=\"gray\">"); //NOI18N
        }
        if (pd != null) {
            sb.append(bundle.getMessage("property")); //NOI18N
            sb.append(" ");  //NOI18N
        }
        if (enableNode) {
            sb.append("<b>");  //NOI18N
        }
        if (pd != null) {
            sb.append(pd.getName());
        }
        else {
            sb.append(bean.getInstanceName());
        }
        if (enableNode) {
            sb.append("</b>");  //NOI18N
        }        
        sb.append(" &nbsp; <font><i>");  //NOI18N
        sb.append(getTargetTypeDisplayName());
        sb.append("</i></font>");  //NOI18N
        if (!enableNode) {
            sb.append("</font>");  //NOI18N
        }
        sb.append("</html>");  //NOI18N
        return sb.toString();
    }
    public boolean isValidBindingTarget() {
        if (propPath == null && bean.getDesignContext() instanceof FacesDesignContext) {
            return ((FacesDesignContext)bean.getDesignContext()).isValidBindingTarget(bean);
        }
        return true;
    }
    public String getBindingExpressionPart() {
        if (propPath != null && propPath.length > 0) {
            return propPath[propPath.length - 1].getName();
        }
        return bean.getInstanceName();
    }
    public Class getTargetTypeClass() {
        if (propInstance == null) {
            propInstance = PropertyBindingHelper.getPropInstance(bean, propPath);
        }
        if (propInstance != null) {
            if (!propInstance.getClass().isPrimitive()) {
                return propInstance.getClass();
            }
        }
        return propPath != null && propPath.length > 0
            ? propPath[propPath.length - 1].getPropertyType()
            : bean.getInstance() != null
                ? bean.getInstance().getClass()
                : bean.getBeanInfo() != null
                    ? bean.getBeanInfo().getBeanDescriptor().getBeanClass()
                    : null;
    }
    boolean iconChecked = false;
    public boolean hasDisplayIcon() {
        if (!iconChecked) {
            displayIcon = getDisplayIcon(true);
            iconChecked = true;
        }
        return displayIcon != null;
    }
    Icon displayIcon = null;
    public Icon getDisplayIcon(boolean enableNode) {
        if (displayIcon == null) {
            if (propInstance == null) {
                propInstance = PropertyBindingHelper.getPropInstance(bean, propPath);
            }
            if (propInstance != null) {
                try {
                    BeanInfo bi = Introspector.getBeanInfo(propInstance.getClass());
                    Image img = bi.getIcon(BeanInfo.ICON_COLOR_16x16);
                    if (img != null) {
                        displayIcon = new ImageIcon(img);
                    }
                } catch (Exception x) {}
                if (displayIcon == null && (propPath == null || propPath.length == 0)) {
                    if (bean instanceof MarkupDesignBean && ((MarkupDesignBean)bean).getElement() != null) {
                        displayIcon = BindingTargetPanel.TAG_ICON;
                    }
                }
            }
        }
        if (displayIcon == null/* && propPath == null*/) {
            displayIcon = BindingTargetPanel.BEAN_ICON;
        }
        return displayIcon;
    }
}
