//GEN-BEGIN:BeanInfo
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
package org.netbeans.modules.visualweb.dataprovider.designtime.impl;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.ResourceBundle;

import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.faces.FacetDescriptor;
import com.sun.rave.designtime.markup.AttributeDescriptor;

import java.beans.SimpleBeanInfo;

/**
 * <p>Auto-generated design time metadata class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

abstract class CompareFilterCriteriaBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataprovider.designtime.impl.Bundle-JSF", Locale.getDefault(), CompareFilterCriteriaBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>CompareFilterCriteriaBeanInfoBase</code>.</p>
     */
    public CompareFilterCriteriaBeanInfoBase() {

        beanClass = com.sun.data.provider.impl.CompareFilterCriteria.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/CompareFilterCriteria_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/CompareFilterCriteria_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/CompareFilterCriteria_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/CompareFilterCriteria_M32";

    }

    /**
     * <p>The bean class that this BeanInfo represents.
     */
    protected Class beanClass;

    /**
     * <p>The cached BeanDescriptor.</p>
     */
    protected BeanDescriptor beanDescriptor;

    /**
     * <p>The index of the default property.</p>
     */
    protected int defaultPropertyIndex = -2;

    /**
     * <p>The name of the default property.</p>
     */
    protected String defaultPropertyName;

    /**
     * <p>The 16x16 color icon.</p>
     */
    protected String iconFileName_C16;

    /**
     * <p>The 32x32 color icon.</p>
     */
    protected String iconFileName_C32;

    /**
     * <p>The 16x16 monochrome icon.</p>
     */
    protected String iconFileName_M16;

    /**
     * <p>The 32x32 monochrome icon.</p>
     */
    protected String iconFileName_M32;

    /**
     * <p>The cached property descriptors.</p>
     */
    protected PropertyDescriptor[] propDescriptors;

    /**
     * <p>Return the <code>BeanDescriptor</code> for this bean.</p>
     */
    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptor(beanClass);
        beanDescriptor.setDisplayName(resources.getString("CompareFilterCriteria_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("CompareFilterCriteria_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"compareFilterCriteria");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());

        return beanDescriptor;

    }

    /**
     * <p>Return the <code>CategoryDescriptor</code> array for the property categories of this component.</p>
     */
    private CategoryDescriptor[] getCategoryDescriptors() {

        return com.sun.rave.designtime.base.CategoryDescriptors.getDefaultCategoryDescriptors();

    }

    /**
     * <p>Return the index of the default property, or
     * -1 if there is no default property.</p>
     */
    public int getDefaultPropertyIndex() {

        if (defaultPropertyIndex > -2) {
            return defaultPropertyIndex;
        } else {
            if (defaultPropertyName == null) {
                defaultPropertyIndex = -1;
            } else {
                PropertyDescriptor pd[] = getPropertyDescriptors();
                for (int i = 0; i < pd.length; i++) {
                    if (defaultPropertyName.equals(pd[i].getName())) {
                        defaultPropertyIndex = i;
                        break;
                    }
                }
            }
        }
        return defaultPropertyIndex;
    }

    /**
     * <p>The cached facet descriptors.</p>
     */
    protected FacetDescriptor[] facetDescriptors;

    /**
     * <p>Return the <code>FacetDescriptor</code>s for this bean.</p>
     */
    public FacetDescriptor[] getFacetDescriptors() {

        if (facetDescriptors != null) {
            return facetDescriptors;
        }
        facetDescriptors = new FacetDescriptor[] {
        };
        return facetDescriptors;

    }

    /**
     * <p>Return the specified image (if any)
     * for this component class.</p>
     */
    public Image getIcon(int kind) {

        String name;
        switch (kind) {
            case ICON_COLOR_16x16:
                name = iconFileName_C16;
                break;
            case ICON_COLOR_32x32:
                name = iconFileName_C32;
                break;
            case ICON_MONO_16x16:
                name = iconFileName_M16;
                break;
            case ICON_MONO_32x32:
                name = iconFileName_M32;
                break;
            default:
                name = null;
                break;
        }
        if (name == null) {
            return null;
        }

        Image image = loadImage(name + ".png");
        if (image == null) {
            image = loadImage(name + ".gif");
        }
        return image;

    }

    /**
     * <p>Return a class loaded by name via the class loader that loaded this class.</p>
     */
    private java.lang.Class loadClass(java.lang.String name) {

        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * <p>Return the <code>PropertyDescriptor</code>s for this bean.</p>
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propDescriptors != null) {
            return propDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_class = new PropertyDescriptor("class",beanClass,"getClass",null);
            prop_class.setDisplayName(resources.getString("CompareFilterCriteria_class_DisplayName"));
            prop_class.setShortDescription(resources.getString("CompareFilterCriteria_class_Description"));
            prop_class.setExpert(false);
            prop_class.setHidden(true);
            prop_class.setPreferred(false);
            prop_class.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_compareLocale = new PropertyDescriptor("compareLocale",beanClass,"getCompareLocale","setCompareLocale");
            prop_compareLocale.setDisplayName(resources.getString("CompareFilterCriteria_compareLocale_DisplayName"));
            prop_compareLocale.setShortDescription(resources.getString("CompareFilterCriteria_compareLocale_Description"));
            prop_compareLocale.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_compareLocale.setExpert(false);
            prop_compareLocale.setHidden(false);
            prop_compareLocale.setPreferred(false);
            prop_compareLocale.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_compareLocale.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", loadClass("com.sun.rave.propertyeditors.domains.InstanceVariableDomain"));

            PropertyDescriptor prop_compareValue = new PropertyDescriptor("compareValue",beanClass,"getCompareValue","setCompareValue");
            prop_compareValue.setDisplayName(resources.getString("CompareFilterCriteria_compareValue_DisplayName"));
            prop_compareValue.setShortDescription(resources.getString("CompareFilterCriteria_compareValue_Description"));
            prop_compareValue.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_compareValue.setExpert(false);
            prop_compareValue.setHidden(false);
            prop_compareValue.setPreferred(false);
            prop_compareValue.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_compareValue.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", loadClass("com.sun.rave.propertyeditors.domains.InstanceVariableDomain"));

            PropertyDescriptor prop_displayName = new PropertyDescriptor("displayName",beanClass,"getDisplayName","setDisplayName");
            prop_displayName.setDisplayName(resources.getString("CompareFilterCriteria_displayName_DisplayName"));
            prop_displayName.setShortDescription(resources.getString("CompareFilterCriteria_displayName_Description"));
            prop_displayName.setExpert(false);
            prop_displayName.setHidden(false);
            prop_displayName.setPreferred(false);
            prop_displayName.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_fieldKey = new PropertyDescriptor("fieldKey",beanClass,"getFieldKey","setFieldKey");
            prop_fieldKey.setDisplayName(resources.getString("CompareFilterCriteria_fieldKey_DisplayName"));
            prop_fieldKey.setShortDescription(resources.getString("CompareFilterCriteria_fieldKey_Description"));
            prop_fieldKey.setExpert(false);
            prop_fieldKey.setHidden(false);
            prop_fieldKey.setPreferred(false);
            prop_fieldKey.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_include = new PropertyDescriptor("include",beanClass,"isInclude","setInclude");
            prop_include.setDisplayName(resources.getString("CompareFilterCriteria_include_DisplayName"));
            prop_include.setShortDescription(resources.getString("CompareFilterCriteria_include_Description"));
            prop_include.setExpert(false);
            prop_include.setHidden(false);
            prop_include.setPreferred(false);
            prop_include.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_matchEqualTo = new PropertyDescriptor("matchEqualTo",beanClass,"isMatchEqualTo","setMatchEqualTo");
            prop_matchEqualTo.setDisplayName(resources.getString("CompareFilterCriteria_matchEqualTo_DisplayName"));
            prop_matchEqualTo.setShortDescription(resources.getString("CompareFilterCriteria_matchEqualTo_Description"));
            prop_matchEqualTo.setExpert(false);
            prop_matchEqualTo.setHidden(false);
            prop_matchEqualTo.setPreferred(false);
            prop_matchEqualTo.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_matchGreaterThan = new PropertyDescriptor("matchGreaterThan",beanClass,"isMatchGreaterThan","setMatchGreaterThan");
            prop_matchGreaterThan.setDisplayName(resources.getString("CompareFilterCriteria_matchGreaterThan_DisplayName"));
            prop_matchGreaterThan.setShortDescription(resources.getString("CompareFilterCriteria_matchGreaterThan_Description"));
            prop_matchGreaterThan.setExpert(false);
            prop_matchGreaterThan.setHidden(false);
            prop_matchGreaterThan.setPreferred(false);
            prop_matchGreaterThan.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_matchLessThan = new PropertyDescriptor("matchLessThan",beanClass,"isMatchLessThan","setMatchLessThan");
            prop_matchLessThan.setDisplayName(resources.getString("CompareFilterCriteria_matchLessThan_DisplayName"));
            prop_matchLessThan.setShortDescription(resources.getString("CompareFilterCriteria_matchLessThan_Description"));
            prop_matchLessThan.setExpert(false);
            prop_matchLessThan.setHidden(false);
            prop_matchLessThan.setPreferred(false);
            prop_matchLessThan.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            propDescriptors = new PropertyDescriptor[] {
                prop_class,
                prop_compareLocale,
                prop_compareValue,
                prop_displayName,
                prop_fieldKey,
                prop_include,
                prop_matchEqualTo,
                prop_matchGreaterThan,
                prop_matchLessThan,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
//GEN-END:BeanInfo
