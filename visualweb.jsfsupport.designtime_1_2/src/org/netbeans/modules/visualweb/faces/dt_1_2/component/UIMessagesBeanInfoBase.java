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

package org.netbeans.modules.visualweb.faces.dt_1_2.component;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.faces.FacetDescriptor;
import com.sun.rave.designtime.markup.AttributeDescriptor;

import org.netbeans.modules.visualweb.faces.dt.HtmlBeanInfoBase;
import org.netbeans.modules.visualweb.faces.dt.BeanDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;


public abstract class UIMessagesBeanInfoBase extends UIComponentBeanInfoBase {

    protected static ResourceBundle resources =
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_2.component.Bundle", Locale.getDefault(), UIMessagesBeanInfoBase.class.getClassLoader());


    public UIMessagesBeanInfoBase() {
        beanClass = javax.faces.component.UIMessages.class;
    }


    private PropertyDescriptor[] propertyDescriptors;

    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_globalOnly = new PropertyDescriptorBase("globalOnly",beanClass,"isGlobalOnly","setGlobalOnly");
            prop_globalOnly.setDisplayName(resources.getString("UIMessages_globalOnly_DisplayName"));
            prop_globalOnly.setShortDescription(resources.getString("UIMessages_globalOnly_Description"));
            prop_globalOnly.setExpert(false);
            prop_globalOnly.setHidden(false);
            prop_globalOnly.setPreferred(false);
            attrib = new AttributeDescriptor("globalOnly",false,"false",true);
            prop_globalOnly.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_globalOnly.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_showDetail = new PropertyDescriptorBase("showDetail",beanClass,"isShowDetail","setShowDetail");
            prop_showDetail.setDisplayName(resources.getString("UIMessages_showDetail_DisplayName"));
            prop_showDetail.setShortDescription(resources.getString("UIMessages_showDetail_Description"));
            prop_showDetail.setExpert(false);
            prop_showDetail.setHidden(false);
            prop_showDetail.setPreferred(false);
            attrib = new AttributeDescriptor("showDetail",false,"false",true);
            prop_showDetail.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_showDetail.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_showSummary = new PropertyDescriptorBase("showSummary",beanClass,"isShowSummary","setShowSummary");
            prop_showSummary.setDisplayName(resources.getString("UIMessages_showSummary_DisplayName"));
            prop_showSummary.setShortDescription(resources.getString("UIMessages_showSummary_Description"));
            prop_showSummary.setExpert(false);
            prop_showSummary.setHidden(false);
            prop_showSummary.setPreferred(false);
            attrib = new AttributeDescriptor("showSummary",false,"true",true);
            prop_showSummary.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_showSummary.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_globalOnly);
            propertyDescriptorList.add(prop_showDetail);
            propertyDescriptorList.add(prop_showSummary);

            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}

