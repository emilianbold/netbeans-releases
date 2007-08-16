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

abstract class CalendarBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-JSF", Locale.getDefault(), CalendarBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>CalendarBeanInfoBase</code>.</p>
     */
    public CalendarBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.component.Calendar.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Calendar_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Calendar_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Calendar_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Calendar_M32";

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
        beanDescriptor.setDisplayName(resources.getString("Calendar_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("Calendar_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_bh_calendar");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"calendar");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_palette_bh_propsheets_bh_calendar_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"calendar");
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
        try {

            FacetDescriptor facet_label = new FacetDescriptor("label");
            facet_label.setDisplayName(resources.getString("Calendar_label_DisplayName"));
            facet_label.setShortDescription(resources.getString("Calendar_label_Description"));

            FacetDescriptor facet_readOnly = new FacetDescriptor("readOnly");
            facet_readOnly.setDisplayName(resources.getString("Calendar_readOnly_DisplayName"));
            facet_readOnly.setShortDescription(resources.getString("Calendar_readOnly_Description"));

            facetDescriptors = new FacetDescriptor[] {
                facet_label,
                facet_readOnly,
            };
            return facetDescriptors;

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            throw e;
        }

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

            PropertyDescriptor prop_converter = new PropertyDescriptor("converter",beanClass,"getConverter","setConverter");
            prop_converter.setDisplayName(resources.getString("Calendar_converter_DisplayName"));
            prop_converter.setShortDescription(resources.getString("Calendar_converter_Description"));
            prop_converter.setExpert(false);
            prop_converter.setHidden(true);
            prop_converter.setPreferred(false);
            attrib = new AttributeDescriptor("converter",false,null,true);
            prop_converter.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_converter.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_dateFormatPattern = new PropertyDescriptor("dateFormatPattern",beanClass,"getDateFormatPattern","setDateFormatPattern");
            prop_dateFormatPattern.setDisplayName(resources.getString("Calendar_dateFormatPattern_DisplayName"));
            prop_dateFormatPattern.setShortDescription(resources.getString("Calendar_dateFormatPattern_Description"));
            prop_dateFormatPattern.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_dateFormatPattern.setExpert(false);
            prop_dateFormatPattern.setHidden(false);
            prop_dateFormatPattern.setPreferred(false);
            attrib = new AttributeDescriptor("dateFormatPattern",false,null,true);
            prop_dateFormatPattern.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dateFormatPattern.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_dateFormatPattern.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.DateFormatPatternsDomain.class);

            PropertyDescriptor prop_dateFormatPatternHelp = new PropertyDescriptor("dateFormatPatternHelp",beanClass,"getDateFormatPatternHelp","setDateFormatPatternHelp");
            prop_dateFormatPatternHelp.setDisplayName(resources.getString("Calendar_dateFormatPatternHelp_DisplayName"));
            prop_dateFormatPatternHelp.setShortDescription(resources.getString("Calendar_dateFormatPatternHelp_Description"));
            prop_dateFormatPatternHelp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_dateFormatPatternHelp.setExpert(false);
            prop_dateFormatPatternHelp.setHidden(false);
            prop_dateFormatPatternHelp.setPreferred(false);
            attrib = new AttributeDescriptor("dateFormatPatternHelp",false,null,true);
            prop_dateFormatPatternHelp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_dateFormatPatternHelp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_maxDate = new PropertyDescriptor("maxDate",beanClass,"getMaxDate","setMaxDate");
            prop_maxDate.setDisplayName(resources.getString("Calendar_maxDate_DisplayName"));
            prop_maxDate.setShortDescription(resources.getString("Calendar_maxDate_Description"));
            prop_maxDate.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor"));
            prop_maxDate.setExpert(false);
            prop_maxDate.setHidden(false);
            prop_maxDate.setPreferred(false);
            attrib = new AttributeDescriptor("maxDate",false,null,true);
            prop_maxDate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_maxDate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_maxLength = new PropertyDescriptor("maxLength",beanClass,"getMaxLength","setMaxLength");
            prop_maxLength.setDisplayName(resources.getString("Calendar_maxLength_DisplayName"));
            prop_maxLength.setShortDescription(resources.getString("Calendar_maxLength_Description"));
            prop_maxLength.setExpert(false);
            prop_maxLength.setHidden(true);
            prop_maxLength.setPreferred(false);
            prop_maxLength.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_minDate = new PropertyDescriptor("minDate",beanClass,"getMinDate","setMinDate");
            prop_minDate.setDisplayName(resources.getString("Calendar_minDate_DisplayName"));
            prop_minDate.setShortDescription(resources.getString("Calendar_minDate_Description"));
            prop_minDate.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor"));
            prop_minDate.setExpert(false);
            prop_minDate.setHidden(false);
            prop_minDate.setPreferred(false);
            attrib = new AttributeDescriptor("minDate",false,null,true);
            prop_minDate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_minDate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_required = new PropertyDescriptor("required",beanClass,"isRequired","setRequired");
            prop_required.setDisplayName(resources.getString("Calendar_required_DisplayName"));
            prop_required.setShortDescription(resources.getString("Calendar_required_Description"));
            prop_required.setExpert(false);
            prop_required.setHidden(false);
            prop_required.setPreferred(false);
            attrib = new AttributeDescriptor("required",false,null,true);
            prop_required.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_required.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_selectedDate = new PropertyDescriptor("selectedDate",beanClass,"getSelectedDate","setSelectedDate");
            prop_selectedDate.setDisplayName(resources.getString("Calendar_selectedDate_DisplayName"));
            prop_selectedDate.setShortDescription(resources.getString("Calendar_selectedDate_Description"));
            prop_selectedDate.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor"));
            prop_selectedDate.setExpert(false);
            prop_selectedDate.setHidden(false);
            prop_selectedDate.setPreferred(false);
            attrib = new AttributeDescriptor("selectedDate",false,null,true);
            prop_selectedDate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_selectedDate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_timeZone = new PropertyDescriptor("timeZone",beanClass,"getTimeZone","setTimeZone");
            prop_timeZone.setDisplayName(resources.getString("Calendar_timeZone_DisplayName"));
            prop_timeZone.setShortDescription(resources.getString("Calendar_timeZone_Description"));
            prop_timeZone.setExpert(false);
            prop_timeZone.setHidden(true);
            prop_timeZone.setPreferred(false);
            prop_timeZone.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_trim = new PropertyDescriptor("trim",beanClass,"isTrim","setTrim");
            prop_trim.setDisplayName(resources.getString("Calendar_trim_DisplayName"));
            prop_trim.setShortDescription(resources.getString("Calendar_trim_Description"));
            prop_trim.setExpert(false);
            prop_trim.setHidden(true);
            prop_trim.setPreferred(false);
            prop_trim.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_columns = new PropertyDescriptor("columns",beanClass,"getColumns","setColumns");
            prop_columns.setDisplayName(resources.getString("Calendar_columns_DisplayName"));
            prop_columns.setShortDescription(resources.getString("Calendar_columns_Description"));
            prop_columns.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_columns.setExpert(false);
            prop_columns.setHidden(false);
            prop_columns.setPreferred(false);
            attrib = new AttributeDescriptor("columns",false,"20",true);
            prop_columns.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_columns.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_columns.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_disabled = new PropertyDescriptor("disabled",beanClass,"isDisabled","setDisabled");
            prop_disabled.setDisplayName(resources.getString("Calendar_disabled_DisplayName"));
            prop_disabled.setShortDescription(resources.getString("Calendar_disabled_Description"));
            prop_disabled.setExpert(false);
            prop_disabled.setHidden(false);
            prop_disabled.setPreferred(false);
            attrib = new AttributeDescriptor("disabled",false,null,true);
            prop_disabled.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_disabled.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_label = new PropertyDescriptor("label",beanClass,"getLabel","setLabel");
            prop_label.setDisplayName(resources.getString("Calendar_label_DisplayName"));
            prop_label.setShortDescription(resources.getString("Calendar_label_Description"));
            prop_label.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_label.setExpert(false);
            prop_label.setHidden(false);
            prop_label.setPreferred(false);
            attrib = new AttributeDescriptor("label",false,null,true);
            prop_label.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_label.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_labelLevel = new PropertyDescriptor("labelLevel",beanClass,"getLabelLevel","setLabelLevel");
            prop_labelLevel.setDisplayName(resources.getString("Calendar_labelLevel_DisplayName"));
            prop_labelLevel.setShortDescription(resources.getString("Calendar_labelLevel_Description"));
            prop_labelLevel.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_labelLevel.setExpert(false);
            prop_labelLevel.setHidden(false);
            prop_labelLevel.setPreferred(false);
            attrib = new AttributeDescriptor("labelLevel",false,"2",true);
            prop_labelLevel.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_labelLevel.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_labelLevel.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.LabelLevelsDomain.class);

            PropertyDescriptor prop_onBlur = new PropertyDescriptor("onBlur",beanClass,"getOnBlur","setOnBlur");
            prop_onBlur.setDisplayName(resources.getString("Calendar_onBlur_DisplayName"));
            prop_onBlur.setShortDescription(resources.getString("Calendar_onBlur_Description"));
            prop_onBlur.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onBlur.setExpert(false);
            prop_onBlur.setHidden(false);
            prop_onBlur.setPreferred(false);
            attrib = new AttributeDescriptor("onBlur",false,null,true);
            prop_onBlur.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onBlur.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onChange = new PropertyDescriptor("onChange",beanClass,"getOnChange","setOnChange");
            prop_onChange.setDisplayName(resources.getString("Calendar_onChange_DisplayName"));
            prop_onChange.setShortDescription(resources.getString("Calendar_onChange_Description"));
            prop_onChange.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onChange.setExpert(false);
            prop_onChange.setHidden(false);
            prop_onChange.setPreferred(false);
            attrib = new AttributeDescriptor("onChange",false,null,true);
            prop_onChange.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onChange.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onClick = new PropertyDescriptor("onClick",beanClass,"getOnClick","setOnClick");
            prop_onClick.setDisplayName(resources.getString("Calendar_onClick_DisplayName"));
            prop_onClick.setShortDescription(resources.getString("Calendar_onClick_Description"));
            prop_onClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onClick.setExpert(false);
            prop_onClick.setHidden(false);
            prop_onClick.setPreferred(false);
            attrib = new AttributeDescriptor("onClick",false,null,true);
            prop_onClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onDblClick = new PropertyDescriptor("onDblClick",beanClass,"getOnDblClick","setOnDblClick");
            prop_onDblClick.setDisplayName(resources.getString("Calendar_onDblClick_DisplayName"));
            prop_onDblClick.setShortDescription(resources.getString("Calendar_onDblClick_Description"));
            prop_onDblClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onDblClick.setExpert(false);
            prop_onDblClick.setHidden(false);
            prop_onDblClick.setPreferred(false);
            attrib = new AttributeDescriptor("onDblClick",false,null,true);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onFocus = new PropertyDescriptor("onFocus",beanClass,"getOnFocus","setOnFocus");
            prop_onFocus.setDisplayName(resources.getString("Calendar_onFocus_DisplayName"));
            prop_onFocus.setShortDescription(resources.getString("Calendar_onFocus_Description"));
            prop_onFocus.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onFocus.setExpert(false);
            prop_onFocus.setHidden(false);
            prop_onFocus.setPreferred(false);
            attrib = new AttributeDescriptor("onFocus",false,null,true);
            prop_onFocus.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onFocus.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyDown = new PropertyDescriptor("onKeyDown",beanClass,"getOnKeyDown","setOnKeyDown");
            prop_onKeyDown.setDisplayName(resources.getString("Calendar_onKeyDown_DisplayName"));
            prop_onKeyDown.setShortDescription(resources.getString("Calendar_onKeyDown_Description"));
            prop_onKeyDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyDown.setExpert(false);
            prop_onKeyDown.setHidden(false);
            prop_onKeyDown.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyDown",false,null,true);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyPress = new PropertyDescriptor("onKeyPress",beanClass,"getOnKeyPress","setOnKeyPress");
            prop_onKeyPress.setDisplayName(resources.getString("Calendar_onKeyPress_DisplayName"));
            prop_onKeyPress.setShortDescription(resources.getString("Calendar_onKeyPress_Description"));
            prop_onKeyPress.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyPress.setExpert(false);
            prop_onKeyPress.setHidden(false);
            prop_onKeyPress.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyPress",false,null,true);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyUp = new PropertyDescriptor("onKeyUp",beanClass,"getOnKeyUp","setOnKeyUp");
            prop_onKeyUp.setDisplayName(resources.getString("Calendar_onKeyUp_DisplayName"));
            prop_onKeyUp.setShortDescription(resources.getString("Calendar_onKeyUp_Description"));
            prop_onKeyUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyUp.setExpert(false);
            prop_onKeyUp.setHidden(false);
            prop_onKeyUp.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyUp",false,null,true);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseDown = new PropertyDescriptor("onMouseDown",beanClass,"getOnMouseDown","setOnMouseDown");
            prop_onMouseDown.setDisplayName(resources.getString("Calendar_onMouseDown_DisplayName"));
            prop_onMouseDown.setShortDescription(resources.getString("Calendar_onMouseDown_Description"));
            prop_onMouseDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseDown.setExpert(false);
            prop_onMouseDown.setHidden(false);
            prop_onMouseDown.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseDown",false,null,true);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseMove = new PropertyDescriptor("onMouseMove",beanClass,"getOnMouseMove","setOnMouseMove");
            prop_onMouseMove.setDisplayName(resources.getString("Calendar_onMouseMove_DisplayName"));
            prop_onMouseMove.setShortDescription(resources.getString("Calendar_onMouseMove_Description"));
            prop_onMouseMove.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseMove.setExpert(false);
            prop_onMouseMove.setHidden(false);
            prop_onMouseMove.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseMove",false,null,true);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOut = new PropertyDescriptor("onMouseOut",beanClass,"getOnMouseOut","setOnMouseOut");
            prop_onMouseOut.setDisplayName(resources.getString("Calendar_onMouseOut_DisplayName"));
            prop_onMouseOut.setShortDescription(resources.getString("Calendar_onMouseOut_Description"));
            prop_onMouseOut.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOut.setExpert(false);
            prop_onMouseOut.setHidden(false);
            prop_onMouseOut.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOut",false,null,true);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOver = new PropertyDescriptor("onMouseOver",beanClass,"getOnMouseOver","setOnMouseOver");
            prop_onMouseOver.setDisplayName(resources.getString("Calendar_onMouseOver_DisplayName"));
            prop_onMouseOver.setShortDescription(resources.getString("Calendar_onMouseOver_Description"));
            prop_onMouseOver.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOver.setExpert(false);
            prop_onMouseOver.setHidden(false);
            prop_onMouseOver.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOver",false,null,true);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseUp = new PropertyDescriptor("onMouseUp",beanClass,"getOnMouseUp","setOnMouseUp");
            prop_onMouseUp.setDisplayName(resources.getString("Calendar_onMouseUp_DisplayName"));
            prop_onMouseUp.setShortDescription(resources.getString("Calendar_onMouseUp_Description"));
            prop_onMouseUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseUp.setExpert(false);
            prop_onMouseUp.setHidden(false);
            prop_onMouseUp.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseUp",false,null,true);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onSelect = new PropertyDescriptor("onSelect",beanClass,"getOnSelect","setOnSelect");
            prop_onSelect.setDisplayName(resources.getString("Calendar_onSelect_DisplayName"));
            prop_onSelect.setShortDescription(resources.getString("Calendar_onSelect_Description"));
            prop_onSelect.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onSelect.setExpert(false);
            prop_onSelect.setHidden(false);
            prop_onSelect.setPreferred(false);
            attrib = new AttributeDescriptor("onSelect",false,null,true);
            prop_onSelect.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onSelect.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_readOnly = new PropertyDescriptor("readOnly",beanClass,"isReadOnly","setReadOnly");
            prop_readOnly.setDisplayName(resources.getString("Calendar_readOnly_DisplayName"));
            prop_readOnly.setShortDescription(resources.getString("Calendar_readOnly_Description"));
            prop_readOnly.setExpert(false);
            prop_readOnly.setHidden(false);
            prop_readOnly.setPreferred(false);
            attrib = new AttributeDescriptor("readOnly",false,null,true);
            prop_readOnly.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_readOnly.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_style = new PropertyDescriptor("style",beanClass,"getStyle","setStyle");
            prop_style.setDisplayName(resources.getString("Calendar_style_DisplayName"));
            prop_style.setShortDescription(resources.getString("Calendar_style_Description"));
            prop_style.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.css.CssStylePropertyEditor"));
            prop_style.setExpert(false);
            prop_style.setHidden(false);
            prop_style.setPreferred(false);
            attrib = new AttributeDescriptor("style",false,null,true);
            prop_style.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_style.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_styleClass = new PropertyDescriptor("styleClass",beanClass,"getStyleClass","setStyleClass");
            prop_styleClass.setDisplayName(resources.getString("Calendar_styleClass_DisplayName"));
            prop_styleClass.setShortDescription(resources.getString("Calendar_styleClass_Description"));
            prop_styleClass.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StyleClassPropertyEditor"));
            prop_styleClass.setExpert(false);
            prop_styleClass.setHidden(false);
            prop_styleClass.setPreferred(false);
            attrib = new AttributeDescriptor("styleClass",false,null,true);
            prop_styleClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_styleClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_tabIndex = new PropertyDescriptor("tabIndex",beanClass,"getTabIndex","setTabIndex");
            prop_tabIndex.setDisplayName(resources.getString("Calendar_tabIndex_DisplayName"));
            prop_tabIndex.setShortDescription(resources.getString("Calendar_tabIndex_Description"));
            prop_tabIndex.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_tabIndex.setExpert(false);
            prop_tabIndex.setHidden(false);
            prop_tabIndex.setPreferred(false);
            attrib = new AttributeDescriptor("tabIndex",false,null,true);
            prop_tabIndex.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tabIndex.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ACCESSIBILITY);
            prop_tabIndex.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_text = new PropertyDescriptor("text",beanClass,"getText","setText");
            prop_text.setDisplayName(resources.getString("Calendar_text_DisplayName"));
            prop_text.setShortDescription(resources.getString("Calendar_text_Description"));
            prop_text.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_text.setExpert(false);
            prop_text.setHidden(true);
            prop_text.setPreferred(false);
            prop_text.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_toolTip = new PropertyDescriptor("toolTip",beanClass,"getToolTip","setToolTip");
            prop_toolTip.setDisplayName(resources.getString("Calendar_toolTip_DisplayName"));
            prop_toolTip.setShortDescription(resources.getString("Calendar_toolTip_Description"));
            prop_toolTip.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_toolTip.setExpert(false);
            prop_toolTip.setHidden(false);
            prop_toolTip.setPreferred(false);
            attrib = new AttributeDescriptor("toolTip",false,null,true);
            prop_toolTip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_toolTip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_visible = new PropertyDescriptor("visible",beanClass,"isVisible","setVisible");
            prop_visible.setDisplayName(resources.getString("Calendar_visible_DisplayName"));
            prop_visible.setShortDescription(resources.getString("Calendar_visible_Description"));
            prop_visible.setExpert(false);
            prop_visible.setHidden(false);
            prop_visible.setPreferred(false);
            attrib = new AttributeDescriptor("visible",false,"true",true);
            prop_visible.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_visible.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_attributes = new PropertyDescriptor("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("Calendar_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("Calendar_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptor("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("Calendar_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("Calendar_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptor("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("Calendar_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("Calendar_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_facets = new PropertyDescriptor("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("Calendar_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("Calendar_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptor("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("Calendar_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("Calendar_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptor("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("Calendar_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("Calendar_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,true);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_immediate = new PropertyDescriptor("immediate",beanClass,"isImmediate","setImmediate");
            prop_immediate.setDisplayName(resources.getString("Calendar_immediate_DisplayName"));
            prop_immediate.setShortDescription(resources.getString("Calendar_immediate_Description"));
            prop_immediate.setExpert(false);
            prop_immediate.setHidden(false);
            prop_immediate.setPreferred(false);
            attrib = new AttributeDescriptor("immediate",false,null,true);
            prop_immediate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_immediate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_localValue = new PropertyDescriptor("localValue",beanClass,"getLocalValue",null);
            prop_localValue.setDisplayName(resources.getString("Calendar_localValue_DisplayName"));
            prop_localValue.setShortDescription(resources.getString("Calendar_localValue_Description"));
            prop_localValue.setExpert(false);
            prop_localValue.setHidden(true);
            prop_localValue.setPreferred(false);
            prop_localValue.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_localValueSet = new PropertyDescriptor("localValueSet",beanClass,"isLocalValueSet","setLocalValueSet");
            prop_localValueSet.setDisplayName(resources.getString("Calendar_localValueSet_DisplayName"));
            prop_localValueSet.setShortDescription(resources.getString("Calendar_localValueSet_Description"));
            prop_localValueSet.setExpert(false);
            prop_localValueSet.setHidden(true);
            prop_localValueSet.setPreferred(false);
            prop_localValueSet.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_parent = new PropertyDescriptor("parent",beanClass,"getParent",null);
            prop_parent.setDisplayName(resources.getString("Calendar_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("Calendar_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("Calendar_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("Calendar_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptor("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("Calendar_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("Calendar_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptor("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("Calendar_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("Calendar_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_submittedValue = new PropertyDescriptor("submittedValue",beanClass,"getSubmittedValue","setSubmittedValue");
            prop_submittedValue.setDisplayName(resources.getString("Calendar_submittedValue_DisplayName"));
            prop_submittedValue.setShortDescription(resources.getString("Calendar_submittedValue_Description"));
            prop_submittedValue.setExpert(false);
            prop_submittedValue.setHidden(true);
            prop_submittedValue.setPreferred(false);
            prop_submittedValue.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_validator = new PropertyDescriptor("validator",beanClass,"getValidator","setValidator");
            prop_validator.setDisplayName(resources.getString("Calendar_validator_DisplayName"));
            prop_validator.setShortDescription(resources.getString("Calendar_validator_Description"));
            prop_validator.setPropertyEditorClass(loadClass("org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.JSF1_1ValidatorPropertyEditor"));
            prop_validator.setExpert(false);
            prop_validator.setHidden(false);
            prop_validator.setPreferred(false);
            attrib = new AttributeDescriptor("validator",false,null,true);
            prop_validator.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_validator.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_value = new PropertyDescriptor("value",beanClass,"getValue","setValue");
            prop_value.setDisplayName(resources.getString("Calendar_value_DisplayName"));
            prop_value.setShortDescription(resources.getString("Calendar_value_Description"));
            prop_value.setExpert(false);
            prop_value.setHidden(true);
            prop_value.setPreferred(false);
            prop_value.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_valueChangeListener = new PropertyDescriptor("valueChangeListener",beanClass,"getValueChangeListener","setValueChangeListener");
            prop_valueChangeListener.setDisplayName(resources.getString("Calendar_valueChangeListener_DisplayName"));
            prop_valueChangeListener.setShortDescription(resources.getString("Calendar_valueChangeListener_Description"));
            prop_valueChangeListener.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.MethodBindingPropertyEditor"));
            prop_valueChangeListener.setExpert(false);
            prop_valueChangeListener.setHidden(false);
            prop_valueChangeListener.setPreferred(false);
            attrib = new AttributeDescriptor("valueChangeListener",false,null,true);
            prop_valueChangeListener.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_valueChangeListener.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            propDescriptors = new PropertyDescriptor[] {
                prop_attributes,
                prop_childCount,
                prop_children,
                prop_columns,
                prop_converter,
                prop_dateFormatPattern,
                prop_dateFormatPatternHelp,
                prop_disabled,
                prop_facets,
                prop_family,
                prop_id,
                prop_immediate,
                prop_label,
                prop_labelLevel,
                prop_localValue,
                prop_localValueSet,
                prop_maxDate,
                prop_maxLength,
                prop_minDate,
                prop_onBlur,
                prop_onChange,
                prop_onClick,
                prop_onDblClick,
                prop_onFocus,
                prop_onKeyDown,
                prop_onKeyPress,
                prop_onKeyUp,
                prop_onMouseDown,
                prop_onMouseMove,
                prop_onMouseOut,
                prop_onMouseOver,
                prop_onMouseUp,
                prop_onSelect,
                prop_parent,
                prop_readOnly,
                prop_rendered,
                prop_rendererType,
                prop_rendersChildren,
                prop_required,
                prop_selectedDate,
                prop_style,
                prop_styleClass,
                prop_submittedValue,
                prop_tabIndex,
                prop_text,
                prop_timeZone,
                prop_toolTip,
                prop_trim,
                prop_validator,
                prop_value,
                prop_valueChangeListener,
                prop_visible,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
//GEN-END:BeanInfo
