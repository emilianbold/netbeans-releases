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
package org.netbeans.modules.visualweb.web.ui.dt.component;

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

abstract class CalendarMonthBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-JSF", Locale.getDefault(), CalendarMonthBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>CalendarMonthBeanInfoBase</code>.</p>
     */
    public CalendarMonthBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.component.CalendarMonth.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/CalendarMonth_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/CalendarMonth_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/CalendarMonth_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/CalendarMonth_M32";

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
        beanDescriptor.setDisplayName(resources.getString("CalendarMonth_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("CalendarMonth_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(true);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"calendarMonth");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"calendarMonth");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_PREFIX,"ui");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI,"http://www.sun.com/web/ui");

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

            PropertyDescriptor prop_dateFormat = new PropertyDescriptor("dateFormat",beanClass,"getDateFormat","setDateFormat");
            prop_dateFormat.setDisplayName(resources.getString("CalendarMonth_dateFormat_DisplayName"));
            prop_dateFormat.setShortDescription(resources.getString("CalendarMonth_dateFormat_Description"));
            prop_dateFormat.setExpert(false);
            prop_dateFormat.setHidden(false);
            prop_dateFormat.setPreferred(false);
            attrib = new AttributeDescriptor("dateFormat",false,null,true);
            prop_dateFormat.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dateFormat.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_dateFormatPattern = new PropertyDescriptor("dateFormatPattern",beanClass,"getDateFormatPattern","setDateFormatPattern");
            prop_dateFormatPattern.setDisplayName(resources.getString("CalendarMonth_dateFormatPattern_DisplayName"));
            prop_dateFormatPattern.setShortDescription(resources.getString("CalendarMonth_dateFormatPattern_Description"));
            prop_dateFormatPattern.setExpert(false);
            prop_dateFormatPattern.setHidden(false);
            prop_dateFormatPattern.setPreferred(false);
            attrib = new AttributeDescriptor("dateFormatPattern",false,null,true);
            prop_dateFormatPattern.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dateFormatPattern.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_popup = new PropertyDescriptor("popup",beanClass,"isPopup","setPopup");
            prop_popup.setDisplayName(resources.getString("CalendarMonth_popup_DisplayName"));
            prop_popup.setShortDescription(resources.getString("CalendarMonth_popup_Description"));
            prop_popup.setExpert(false);
            prop_popup.setHidden(false);
            prop_popup.setPreferred(false);
            attrib = new AttributeDescriptor("popup",false,null,true);
            prop_popup.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_popup.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_selectedDate = new PropertyDescriptor("selectedDate",beanClass,"getSelectedDate","setSelectedDate");
            prop_selectedDate.setDisplayName(resources.getString("CalendarMonth_selectedDate_DisplayName"));
            prop_selectedDate.setShortDescription(resources.getString("CalendarMonth_selectedDate_Description"));
            prop_selectedDate.setExpert(false);
            prop_selectedDate.setHidden(false);
            prop_selectedDate.setPreferred(false);
            attrib = new AttributeDescriptor("selectedDate",false,null,true);
            prop_selectedDate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_selectedDate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_style = new PropertyDescriptor("style",beanClass,"getStyle","setStyle");
            prop_style.setDisplayName(resources.getString("CalendarMonth_style_DisplayName"));
            prop_style.setShortDescription(resources.getString("CalendarMonth_style_Description"));
            prop_style.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.css.CssStylePropertyEditor"));
            prop_style.setExpert(false);
            prop_style.setHidden(false);
            prop_style.setPreferred(false);
            attrib = new AttributeDescriptor("style",false,null,true);
            prop_style.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_style.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_styleClass = new PropertyDescriptor("styleClass",beanClass,"getStyleClass","setStyleClass");
            prop_styleClass.setDisplayName(resources.getString("CalendarMonth_styleClass_DisplayName"));
            prop_styleClass.setShortDescription(resources.getString("CalendarMonth_styleClass_Description"));
            prop_styleClass.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StyleClassPropertyEditor"));
            prop_styleClass.setExpert(false);
            prop_styleClass.setHidden(false);
            prop_styleClass.setPreferred(false);
            attrib = new AttributeDescriptor("styleClass",false,null,true);
            prop_styleClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_styleClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_tabIndex = new PropertyDescriptor("tabIndex",beanClass,"getTabIndex","setTabIndex");
            prop_tabIndex.setDisplayName(resources.getString("CalendarMonth_tabIndex_DisplayName"));
            prop_tabIndex.setShortDescription(resources.getString("CalendarMonth_tabIndex_Description"));
            prop_tabIndex.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_tabIndex.setExpert(false);
            prop_tabIndex.setHidden(false);
            prop_tabIndex.setPreferred(false);
            attrib = new AttributeDescriptor("tabIndex",false,null,true);
            prop_tabIndex.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tabIndex.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ACCESSIBILITY);
            prop_tabIndex.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_timeZone = new PropertyDescriptor("timeZone",beanClass,"getTimeZone","setTimeZone");
            prop_timeZone.setDisplayName(resources.getString("CalendarMonth_timeZone_DisplayName"));
            prop_timeZone.setShortDescription(resources.getString("CalendarMonth_timeZone_Description"));
            prop_timeZone.setExpert(false);
            prop_timeZone.setHidden(false);
            prop_timeZone.setPreferred(false);
            attrib = new AttributeDescriptor("timeZone",false,null,true);
            prop_timeZone.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_timeZone.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_toolTip = new PropertyDescriptor("toolTip",beanClass,"getToolTip","setToolTip");
            prop_toolTip.setDisplayName(resources.getString("CalendarMonth_toolTip_DisplayName"));
            prop_toolTip.setShortDescription(resources.getString("CalendarMonth_toolTip_Description"));
            prop_toolTip.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_toolTip.setExpert(false);
            prop_toolTip.setHidden(false);
            prop_toolTip.setPreferred(false);
            attrib = new AttributeDescriptor("toolTip",false,null,true);
            prop_toolTip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_toolTip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_visible = new PropertyDescriptor("visible",beanClass,"getVisible","setVisible");
            prop_visible.setDisplayName(resources.getString("CalendarMonth_visible_DisplayName"));
            prop_visible.setShortDescription(resources.getString("CalendarMonth_visible_Description"));
            prop_visible.setExpert(false);
            prop_visible.setHidden(false);
            prop_visible.setPreferred(false);
            attrib = new AttributeDescriptor("visible",false,null,true);
            prop_visible.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_visible.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_yearsToShow = new PropertyDescriptor("yearsToShow",beanClass,"getYearsToShow","setYearsToShow");
            prop_yearsToShow.setDisplayName(resources.getString("CalendarMonth_yearsToShow_DisplayName"));
            prop_yearsToShow.setShortDescription(resources.getString("CalendarMonth_yearsToShow_Description"));
            prop_yearsToShow.setExpert(false);
            prop_yearsToShow.setHidden(false);
            prop_yearsToShow.setPreferred(false);
            attrib = new AttributeDescriptor("yearsToShow",false,null,true);
            prop_yearsToShow.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_yearsToShow.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_attributes = new PropertyDescriptor("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("CalendarMonth_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("CalendarMonth_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptor("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("CalendarMonth_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("CalendarMonth_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptor("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("CalendarMonth_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("CalendarMonth_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_converter = new PropertyDescriptor("converter",beanClass,"getConverter","setConverter");
            prop_converter.setDisplayName(resources.getString("CalendarMonth_converter_DisplayName"));
            prop_converter.setShortDescription(resources.getString("CalendarMonth_converter_Description"));
            prop_converter.setPropertyEditorClass(loadClass("org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.JSF1_1ConverterPropertyEditor"));
            prop_converter.setExpert(false);
            prop_converter.setHidden(false);
            prop_converter.setPreferred(false);
            attrib = new AttributeDescriptor("converter",false,null,true);
            prop_converter.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_converter.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_facets = new PropertyDescriptor("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("CalendarMonth_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("CalendarMonth_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptor("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("CalendarMonth_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("CalendarMonth_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptor("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("CalendarMonth_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("CalendarMonth_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,true);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_localValue = new PropertyDescriptor("localValue",beanClass,"getLocalValue",null);
            prop_localValue.setDisplayName(resources.getString("CalendarMonth_localValue_DisplayName"));
            prop_localValue.setShortDescription(resources.getString("CalendarMonth_localValue_Description"));
            prop_localValue.setExpert(false);
            prop_localValue.setHidden(true);
            prop_localValue.setPreferred(false);
            prop_localValue.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_parent = new PropertyDescriptor("parent",beanClass,"getParent",null);
            prop_parent.setDisplayName(resources.getString("CalendarMonth_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("CalendarMonth_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("CalendarMonth_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("CalendarMonth_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptor("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("CalendarMonth_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("CalendarMonth_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptor("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("CalendarMonth_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("CalendarMonth_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_value = new PropertyDescriptor("value",beanClass,"getValue","setValue");
            prop_value.setDisplayName(resources.getString("CalendarMonth_value_DisplayName"));
            prop_value.setShortDescription(resources.getString("CalendarMonth_value_Description"));
            prop_value.setExpert(false);
            prop_value.setHidden(true);
            prop_value.setPreferred(false);
            prop_value.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            propDescriptors = new PropertyDescriptor[] {
                prop_attributes,
                prop_childCount,
                prop_children,
                prop_converter,
                prop_dateFormat,
                prop_dateFormatPattern,
                prop_facets,
                prop_family,
                prop_id,
                prop_localValue,
                prop_parent,
                prop_popup,
                prop_rendered,
                prop_rendererType,
                prop_rendersChildren,
                prop_selectedDate,
                prop_style,
                prop_styleClass,
                prop_tabIndex,
                prop_timeZone,
                prop_toolTip,
                prop_value,
                prop_visible,
                prop_yearsToShow,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
//GEN-END:BeanInfo
