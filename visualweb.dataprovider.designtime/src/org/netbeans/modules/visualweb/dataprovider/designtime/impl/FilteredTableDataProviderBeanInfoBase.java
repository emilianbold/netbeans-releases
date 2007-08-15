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

abstract class FilteredTableDataProviderBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataprovider.designtime.impl.Bundle-JSF", Locale.getDefault(), FilteredTableDataProviderBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>FilteredTableDataProviderBeanInfoBase</code>.</p>
     */
    public FilteredTableDataProviderBeanInfoBase() {

        beanClass = com.sun.data.provider.impl.FilteredTableDataProvider.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/FilteredTableDataProvider_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/FilteredTableDataProvider_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/FilteredTableDataProvider_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/dataprovider/designtime/impl/FilteredTableDataProvider_M32";

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
        beanDescriptor.setDisplayName(resources.getString("FilteredTableDataProvider_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("FilteredTableDataProvider_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"filteredTableDataProvider");
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
            prop_class.setDisplayName(resources.getString("FilteredTableDataProvider_class_DisplayName"));
            prop_class.setShortDescription(resources.getString("FilteredTableDataProvider_class_Description"));
            prop_class.setExpert(false);
            prop_class.setHidden(true);
            prop_class.setPreferred(false);
            prop_class.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_cursorRow = new PropertyDescriptor("cursorRow",beanClass,"getCursorRow","setCursorRow");
            prop_cursorRow.setDisplayName(resources.getString("FilteredTableDataProvider_cursorRow_DisplayName"));
            prop_cursorRow.setShortDescription(resources.getString("FilteredTableDataProvider_cursorRow_Description"));
            prop_cursorRow.setExpert(false);
            prop_cursorRow.setHidden(true);
            prop_cursorRow.setPreferred(false);
            prop_cursorRow.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_dataListeners = new PropertyDescriptor("dataListeners",beanClass,"getDataListeners",null);
            prop_dataListeners.setDisplayName(resources.getString("FilteredTableDataProvider_dataListeners_DisplayName"));
            prop_dataListeners.setShortDescription(resources.getString("FilteredTableDataProvider_dataListeners_Description"));
            prop_dataListeners.setExpert(false);
            prop_dataListeners.setHidden(true);
            prop_dataListeners.setPreferred(false);
            prop_dataListeners.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_fieldKeys = new PropertyDescriptor("fieldKeys",beanClass,"getFieldKeys",null);
            prop_fieldKeys.setDisplayName(resources.getString("FilteredTableDataProvider_fieldKeys_DisplayName"));
            prop_fieldKeys.setShortDescription(resources.getString("FilteredTableDataProvider_fieldKeys_Description"));
            prop_fieldKeys.setExpert(false);
            prop_fieldKeys.setHidden(true);
            prop_fieldKeys.setPreferred(false);
            prop_fieldKeys.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_rowCount = new PropertyDescriptor("rowCount",beanClass,"getRowCount",null);
            prop_rowCount.setDisplayName(resources.getString("FilteredTableDataProvider_rowCount_DisplayName"));
            prop_rowCount.setShortDescription(resources.getString("FilteredTableDataProvider_rowCount_Description"));
            prop_rowCount.setExpert(false);
            prop_rowCount.setHidden(true);
            prop_rowCount.setPreferred(false);
            prop_rowCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_tableCursorListeners = new PropertyDescriptor("tableCursorListeners",beanClass,"getTableCursorListeners",null);
            prop_tableCursorListeners.setDisplayName(resources.getString("FilteredTableDataProvider_tableCursorListeners_DisplayName"));
            prop_tableCursorListeners.setShortDescription(resources.getString("FilteredTableDataProvider_tableCursorListeners_Description"));
            prop_tableCursorListeners.setExpert(false);
            prop_tableCursorListeners.setHidden(true);
            prop_tableCursorListeners.setPreferred(false);
            prop_tableCursorListeners.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_tableDataFilter = new PropertyDescriptor("tableDataFilter",beanClass,"getTableDataFilter","setTableDataFilter");
            prop_tableDataFilter.setDisplayName(resources.getString("FilteredTableDataProvider_tableDataFilter_DisplayName"));
            prop_tableDataFilter.setShortDescription(resources.getString("FilteredTableDataProvider_tableDataFilter_Description"));
            prop_tableDataFilter.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_tableDataFilter.setExpert(false);
            prop_tableDataFilter.setHidden(false);
            prop_tableDataFilter.setPreferred(false);
            prop_tableDataFilter.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_tableDataFilter.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", loadClass("com.sun.rave.propertyeditors.domains.InstanceVariableDomain"));

            PropertyDescriptor prop_tableDataListeners = new PropertyDescriptor("tableDataListeners",beanClass,"getTableDataListeners",null);
            prop_tableDataListeners.setDisplayName(resources.getString("FilteredTableDataProvider_tableDataListeners_DisplayName"));
            prop_tableDataListeners.setShortDescription(resources.getString("FilteredTableDataProvider_tableDataListeners_Description"));
            prop_tableDataListeners.setExpert(false);
            prop_tableDataListeners.setHidden(true);
            prop_tableDataListeners.setPreferred(false);
            prop_tableDataListeners.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_tableDataProvider = new PropertyDescriptor("tableDataProvider",beanClass,"getTableDataProvider","setTableDataProvider");
            prop_tableDataProvider.setDisplayName(resources.getString("FilteredTableDataProvider_tableDataProvider_DisplayName"));
            prop_tableDataProvider.setShortDescription(resources.getString("FilteredTableDataProvider_tableDataProvider_Description"));
            prop_tableDataProvider.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_tableDataProvider.setExpert(false);
            prop_tableDataProvider.setHidden(false);
            prop_tableDataProvider.setPreferred(false);
            prop_tableDataProvider.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_tableDataProvider.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", loadClass("com.sun.rave.propertyeditors.domains.InstanceVariableDomain"));

            propDescriptors = new PropertyDescriptor[] {
                prop_class,
                prop_cursorRow,
                prop_dataListeners,
                prop_fieldKeys,
                prop_rowCount,
                prop_tableCursorListeners,
                prop_tableDataFilter,
                prop_tableDataListeners,
                prop_tableDataProvider,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
//GEN-END:BeanInfo
