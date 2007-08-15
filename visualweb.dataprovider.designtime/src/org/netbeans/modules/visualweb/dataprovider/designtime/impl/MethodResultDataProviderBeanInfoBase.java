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

abstract class MethodResultDataProviderBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataprovider.designtime.impl.Bundle-JSF", Locale.getDefault(), MethodResultDataProviderBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>MethodResultDataProviderBeanInfoBase</code>.</p>
     */
    public MethodResultDataProviderBeanInfoBase() {

        beanClass = com.sun.data.provider.impl.MethodResultDataProvider.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/MethodResultDataProvider_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/MethodResultDataProvider_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/MethodResultDataProvider_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/MethodResultDataProvider_M32";

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
        beanDescriptor.setDisplayName(resources.getString("MethodResultDataProvider_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("MethodResultDataProvider_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"methodResultDataProvider");
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
            prop_class.setDisplayName(resources.getString("MethodResultDataProvider_class_DisplayName"));
            prop_class.setShortDescription(resources.getString("MethodResultDataProvider_class_Description"));
            prop_class.setExpert(false);
            prop_class.setHidden(true);
            prop_class.setPreferred(false);
            prop_class.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_dataClassInstance = new PropertyDescriptor("dataClassInstance",beanClass,"getDataClassInstance","setDataClassInstance");
            prop_dataClassInstance.setDisplayName(resources.getString("MethodResultDataProvider_dataClassInstance_DisplayName"));
            prop_dataClassInstance.setShortDescription(resources.getString("MethodResultDataProvider_dataClassInstance_Description"));
            prop_dataClassInstance.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_dataClassInstance.setExpert(false);
            prop_dataClassInstance.setHidden(false);
            prop_dataClassInstance.setPreferred(false);
            prop_dataClassInstance.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_dataClassInstance.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", loadClass("com.sun.rave.propertyeditors.domains.InstanceVariableDomain"));

            PropertyDescriptor prop_dataListeners = new PropertyDescriptor("dataListeners",beanClass,"getDataListeners",null);
            prop_dataListeners.setDisplayName(resources.getString("MethodResultDataProvider_dataListeners_DisplayName"));
            prop_dataListeners.setShortDescription(resources.getString("MethodResultDataProvider_dataListeners_Description"));
            prop_dataListeners.setExpert(false);
            prop_dataListeners.setHidden(true);
            prop_dataListeners.setPreferred(false);
            prop_dataListeners.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_dataMethod = new PropertyDescriptor("dataMethod",beanClass,"getDataMethod","setDataMethod");
            prop_dataMethod.setDisplayName(resources.getString("MethodResultDataProvider_dataMethod_DisplayName"));
            prop_dataMethod.setShortDescription(resources.getString("MethodResultDataProvider_dataMethod_Description"));
            prop_dataMethod.setExpert(false);
            prop_dataMethod.setHidden(false);
            prop_dataMethod.setPreferred(false);
            prop_dataMethod.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_dataMethodArguments = new PropertyDescriptor("dataMethodArguments",beanClass,"getDataMethodArguments","setDataMethodArguments");
            prop_dataMethodArguments.setDisplayName(resources.getString("MethodResultDataProvider_dataMethodArguments_DisplayName"));
            prop_dataMethodArguments.setShortDescription(resources.getString("MethodResultDataProvider_dataMethodArguments_Description"));
            prop_dataMethodArguments.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_dataMethodArguments.setExpert(false);
            prop_dataMethodArguments.setHidden(false);
            prop_dataMethodArguments.setPreferred(false);
            prop_dataMethodArguments.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_dataMethodArguments.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", loadClass("com.sun.rave.propertyeditors.domains.InstanceVariableDomain"));

            PropertyDescriptor prop_fieldKeys = new PropertyDescriptor("fieldKeys",beanClass,"getFieldKeys",null);
            prop_fieldKeys.setDisplayName(resources.getString("MethodResultDataProvider_fieldKeys_DisplayName"));
            prop_fieldKeys.setShortDescription(resources.getString("MethodResultDataProvider_fieldKeys_Description"));
            prop_fieldKeys.setExpert(false);
            prop_fieldKeys.setHidden(true);
            prop_fieldKeys.setPreferred(false);
            prop_fieldKeys.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_refreshableDataListeners = new PropertyDescriptor("refreshableDataListeners",beanClass,"getRefreshableDataListeners",null);
            prop_refreshableDataListeners.setDisplayName(resources.getString("MethodResultDataProvider_refreshableDataListeners_DisplayName"));
            prop_refreshableDataListeners.setShortDescription(resources.getString("MethodResultDataProvider_refreshableDataListeners_Description"));
            prop_refreshableDataListeners.setExpert(false);
            prop_refreshableDataListeners.setHidden(true);
            prop_refreshableDataListeners.setPreferred(false);
            prop_refreshableDataListeners.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_resultObject = new PropertyDescriptor("resultObject",beanClass,"getResultObject",null);
            prop_resultObject.setDisplayName(resources.getString("MethodResultDataProvider_resultObject_DisplayName"));
            prop_resultObject.setShortDescription(resources.getString("MethodResultDataProvider_resultObject_Description"));
            prop_resultObject.setExpert(false);
            prop_resultObject.setHidden(false);
            prop_resultObject.setPreferred(false);
            prop_resultObject.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            propDescriptors = new PropertyDescriptor[] {
                prop_class,
                prop_dataClassInstance,
                prop_dataListeners,
                prop_dataMethod,
                prop_dataMethodArguments,
                prop_fieldKeys,
                prop_refreshableDataListeners,
                prop_resultObject,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
//GEN-END:BeanInfo
