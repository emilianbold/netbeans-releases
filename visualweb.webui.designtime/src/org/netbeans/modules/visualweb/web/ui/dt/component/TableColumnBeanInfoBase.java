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

abstract class TableColumnBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-JSF", Locale.getDefault(), TableColumnBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>TableColumnBeanInfoBase</code>.</p>
     */
    public TableColumnBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.component.TableColumn.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TableColumn_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TableColumn_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TableColumn_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TableColumn_M32";

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
        beanDescriptor.setDisplayName(resources.getString("TableColumn_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("TableColumn_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_bh_column");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"tableColumn");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_palette_bh_propsheets_bh_column_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"tableColumn");
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

            PropertyDescriptor prop_abbr = new PropertyDescriptor("abbr",beanClass,"getAbbr","setAbbr");
            prop_abbr.setDisplayName(resources.getString("TableColumn_abbr_DisplayName"));
            prop_abbr.setShortDescription(resources.getString("TableColumn_abbr_Description"));
            prop_abbr.setExpert(false);
            prop_abbr.setHidden(true);
            prop_abbr.setPreferred(false);
            prop_abbr.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_align = new PropertyDescriptor("align",beanClass,"getAlign","setAlign");
            prop_align.setDisplayName(resources.getString("TableColumn_align_DisplayName"));
            prop_align.setShortDescription(resources.getString("TableColumn_align_Description"));
            prop_align.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_align.setExpert(false);
            prop_align.setHidden(false);
            prop_align.setPreferred(false);
            attrib = new AttributeDescriptor("align",false,null,true);
            prop_align.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_align.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_align.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.StringTokensDomain.class);
            prop_align.setValue("com.sun.rave.propertyeditors.domains.TOKEN_STRING", "left,center,right,justify");

            PropertyDescriptor prop_alignKey = new PropertyDescriptor("alignKey",beanClass,"getAlignKey","setAlignKey");
            prop_alignKey.setDisplayName(resources.getString("TableColumn_alignKey_DisplayName"));
            prop_alignKey.setShortDescription(resources.getString("TableColumn_alignKey_Description"));
            prop_alignKey.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_alignKey.setExpert(false);
            prop_alignKey.setHidden(false);
            prop_alignKey.setPreferred(false);
            attrib = new AttributeDescriptor("alignKey",false,null,true);
            prop_alignKey.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_alignKey.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_axis = new PropertyDescriptor("axis",beanClass,"getAxis","setAxis");
            prop_axis.setDisplayName(resources.getString("TableColumn_axis_DisplayName"));
            prop_axis.setShortDescription(resources.getString("TableColumn_axis_Description"));
            prop_axis.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_axis.setExpert(false);
            prop_axis.setHidden(true);
            prop_axis.setPreferred(false);
            prop_axis.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_bgColor = new PropertyDescriptor("bgColor",beanClass,"getBgColor","setBgColor");
            prop_bgColor.setDisplayName(resources.getString("TableColumn_bgColor_DisplayName"));
            prop_bgColor.setShortDescription(resources.getString("TableColumn_bgColor_Description"));
            prop_bgColor.setExpert(false);
            prop_bgColor.setHidden(true);
            prop_bgColor.setPreferred(false);
            prop_bgColor.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_char = new PropertyDescriptor("char",beanClass,"getChar","setChar");
            prop_char.setDisplayName(resources.getString("TableColumn_char_DisplayName"));
            prop_char.setShortDescription(resources.getString("TableColumn_char_Description"));
            prop_char.setExpert(false);
            prop_char.setHidden(true);
            prop_char.setPreferred(false);
            prop_char.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_charOff = new PropertyDescriptor("charOff",beanClass,"getCharOff","setCharOff");
            prop_charOff.setDisplayName(resources.getString("TableColumn_charOff_DisplayName"));
            prop_charOff.setShortDescription(resources.getString("TableColumn_charOff_Description"));
            prop_charOff.setExpert(false);
            prop_charOff.setHidden(true);
            prop_charOff.setPreferred(false);
            prop_charOff.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_colSpan = new PropertyDescriptor("colSpan",beanClass,"getColSpan","setColSpan");
            prop_colSpan.setDisplayName(resources.getString("TableColumn_colSpan_DisplayName"));
            prop_colSpan.setShortDescription(resources.getString("TableColumn_colSpan_Description"));
            prop_colSpan.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_colSpan.setExpert(false);
            prop_colSpan.setHidden(false);
            prop_colSpan.setPreferred(false);
            prop_colSpan.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.LAYOUT);
            prop_colSpan.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");

            PropertyDescriptor prop_descending = new PropertyDescriptor("descending",beanClass,"isDescending","setDescending");
            prop_descending.setDisplayName(resources.getString("TableColumn_descending_DisplayName"));
            prop_descending.setShortDescription(resources.getString("TableColumn_descending_Description"));
            prop_descending.setExpert(false);
            prop_descending.setHidden(false);
            prop_descending.setPreferred(false);
            attrib = new AttributeDescriptor("descending",false,null,true);
            prop_descending.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_descending.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_embeddedActions = new PropertyDescriptor("embeddedActions",beanClass,"isEmbeddedActions","setEmbeddedActions");
            prop_embeddedActions.setDisplayName(resources.getString("TableColumn_embeddedActions_DisplayName"));
            prop_embeddedActions.setShortDescription(resources.getString("TableColumn_embeddedActions_Description"));
            prop_embeddedActions.setExpert(false);
            prop_embeddedActions.setHidden(false);
            prop_embeddedActions.setPreferred(false);
            attrib = new AttributeDescriptor("embeddedActions",false,null,true);
            prop_embeddedActions.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_embeddedActions.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_emptyCell = new PropertyDescriptor("emptyCell",beanClass,"isEmptyCell","setEmptyCell");
            prop_emptyCell.setDisplayName(resources.getString("TableColumn_emptyCell_DisplayName"));
            prop_emptyCell.setShortDescription(resources.getString("TableColumn_emptyCell_Description"));
            prop_emptyCell.setExpert(false);
            prop_emptyCell.setHidden(false);
            prop_emptyCell.setPreferred(false);
            attrib = new AttributeDescriptor("emptyCell",false,null,true);
            prop_emptyCell.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_emptyCell.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_extraFooterHtml = new PropertyDescriptor("extraFooterHtml",beanClass,"getExtraFooterHtml","setExtraFooterHtml");
            prop_extraFooterHtml.setDisplayName(resources.getString("TableColumn_extraFooterHtml_DisplayName"));
            prop_extraFooterHtml.setShortDescription(resources.getString("TableColumn_extraFooterHtml_Description"));
            prop_extraFooterHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraFooterHtml.setExpert(false);
            prop_extraFooterHtml.setHidden(false);
            prop_extraFooterHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraFooterHtml",false,null,true);
            prop_extraFooterHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraFooterHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_extraHeaderHtml = new PropertyDescriptor("extraHeaderHtml",beanClass,"getExtraHeaderHtml","setExtraHeaderHtml");
            prop_extraHeaderHtml.setDisplayName(resources.getString("TableColumn_extraHeaderHtml_DisplayName"));
            prop_extraHeaderHtml.setShortDescription(resources.getString("TableColumn_extraHeaderHtml_Description"));
            prop_extraHeaderHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraHeaderHtml.setExpert(false);
            prop_extraHeaderHtml.setHidden(false);
            prop_extraHeaderHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraHeaderHtml",false,null,true);
            prop_extraHeaderHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraHeaderHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_extraTableFooterHtml = new PropertyDescriptor("extraTableFooterHtml",beanClass,"getExtraTableFooterHtml","setExtraTableFooterHtml");
            prop_extraTableFooterHtml.setDisplayName(resources.getString("TableColumn_extraTableFooterHtml_DisplayName"));
            prop_extraTableFooterHtml.setShortDescription(resources.getString("TableColumn_extraTableFooterHtml_Description"));
            prop_extraTableFooterHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraTableFooterHtml.setExpert(false);
            prop_extraTableFooterHtml.setHidden(false);
            prop_extraTableFooterHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraTableFooterHtml",false,null,true);
            prop_extraTableFooterHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraTableFooterHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_footerText = new PropertyDescriptor("footerText",beanClass,"getFooterText","setFooterText");
            prop_footerText.setDisplayName(resources.getString("TableColumn_footerText_DisplayName"));
            prop_footerText.setShortDescription(resources.getString("TableColumn_footerText_Description"));
            prop_footerText.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_footerText.setExpert(false);
            prop_footerText.setHidden(false);
            prop_footerText.setPreferred(false);
            attrib = new AttributeDescriptor("footerText",false,null,true);
            prop_footerText.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_footerText.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_headerText = new PropertyDescriptor("headerText",beanClass,"getHeaderText","setHeaderText");
            prop_headerText.setDisplayName(resources.getString("TableColumn_headerText_DisplayName"));
            prop_headerText.setShortDescription(resources.getString("TableColumn_headerText_Description"));
            prop_headerText.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_headerText.setExpert(false);
            prop_headerText.setHidden(false);
            prop_headerText.setPreferred(false);
            attrib = new AttributeDescriptor("headerText",false,null,true);
            prop_headerText.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_headerText.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_headers = new PropertyDescriptor("headers",beanClass,"getHeaders","setHeaders");
            prop_headers.setDisplayName(resources.getString("TableColumn_headers_DisplayName"));
            prop_headers.setShortDescription(resources.getString("TableColumn_headers_Description"));
            prop_headers.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_headers.setExpert(false);
            prop_headers.setHidden(true);
            prop_headers.setPreferred(false);
            prop_headers.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_height = new PropertyDescriptor("height",beanClass,"getHeight","setHeight");
            prop_height.setDisplayName(resources.getString("TableColumn_height_DisplayName"));
            prop_height.setShortDescription(resources.getString("TableColumn_height_Description"));
            prop_height.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_height.setExpert(false);
            prop_height.setHidden(false);
            prop_height.setPreferred(false);
            attrib = new AttributeDescriptor("height",false,null,true);
            prop_height.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_height.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.LAYOUT);
            prop_height.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_noWrap = new PropertyDescriptor("noWrap",beanClass,"isNoWrap","setNoWrap");
            prop_noWrap.setDisplayName(resources.getString("TableColumn_noWrap_DisplayName"));
            prop_noWrap.setShortDescription(resources.getString("TableColumn_noWrap_Description"));
            prop_noWrap.setExpert(false);
            prop_noWrap.setHidden(false);
            prop_noWrap.setPreferred(false);
            attrib = new AttributeDescriptor("noWrap",false,null,true);
            prop_noWrap.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_noWrap.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_onClick = new PropertyDescriptor("onClick",beanClass,"getOnClick","setOnClick");
            prop_onClick.setDisplayName(resources.getString("TableColumn_onClick_DisplayName"));
            prop_onClick.setShortDescription(resources.getString("TableColumn_onClick_Description"));
            prop_onClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onClick.setExpert(false);
            prop_onClick.setHidden(false);
            prop_onClick.setPreferred(false);
            attrib = new AttributeDescriptor("onClick",false,null,true);
            prop_onClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onDblClick = new PropertyDescriptor("onDblClick",beanClass,"getOnDblClick","setOnDblClick");
            prop_onDblClick.setDisplayName(resources.getString("TableColumn_onDblClick_DisplayName"));
            prop_onDblClick.setShortDescription(resources.getString("TableColumn_onDblClick_Description"));
            prop_onDblClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onDblClick.setExpert(false);
            prop_onDblClick.setHidden(false);
            prop_onDblClick.setPreferred(false);
            attrib = new AttributeDescriptor("onDblClick",false,null,true);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyDown = new PropertyDescriptor("onKeyDown",beanClass,"getOnKeyDown","setOnKeyDown");
            prop_onKeyDown.setDisplayName(resources.getString("TableColumn_onKeyDown_DisplayName"));
            prop_onKeyDown.setShortDescription(resources.getString("TableColumn_onKeyDown_Description"));
            prop_onKeyDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyDown.setExpert(false);
            prop_onKeyDown.setHidden(false);
            prop_onKeyDown.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyDown",false,null,true);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyPress = new PropertyDescriptor("onKeyPress",beanClass,"getOnKeyPress","setOnKeyPress");
            prop_onKeyPress.setDisplayName(resources.getString("TableColumn_onKeyPress_DisplayName"));
            prop_onKeyPress.setShortDescription(resources.getString("TableColumn_onKeyPress_Description"));
            prop_onKeyPress.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyPress.setExpert(false);
            prop_onKeyPress.setHidden(false);
            prop_onKeyPress.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyPress",false,null,true);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyUp = new PropertyDescriptor("onKeyUp",beanClass,"getOnKeyUp","setOnKeyUp");
            prop_onKeyUp.setDisplayName(resources.getString("TableColumn_onKeyUp_DisplayName"));
            prop_onKeyUp.setShortDescription(resources.getString("TableColumn_onKeyUp_Description"));
            prop_onKeyUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyUp.setExpert(false);
            prop_onKeyUp.setHidden(false);
            prop_onKeyUp.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyUp",false,null,true);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseDown = new PropertyDescriptor("onMouseDown",beanClass,"getOnMouseDown","setOnMouseDown");
            prop_onMouseDown.setDisplayName(resources.getString("TableColumn_onMouseDown_DisplayName"));
            prop_onMouseDown.setShortDescription(resources.getString("TableColumn_onMouseDown_Description"));
            prop_onMouseDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseDown.setExpert(false);
            prop_onMouseDown.setHidden(false);
            prop_onMouseDown.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseDown",false,null,true);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseMove = new PropertyDescriptor("onMouseMove",beanClass,"getOnMouseMove","setOnMouseMove");
            prop_onMouseMove.setDisplayName(resources.getString("TableColumn_onMouseMove_DisplayName"));
            prop_onMouseMove.setShortDescription(resources.getString("TableColumn_onMouseMove_Description"));
            prop_onMouseMove.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseMove.setExpert(false);
            prop_onMouseMove.setHidden(false);
            prop_onMouseMove.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseMove",false,null,true);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOut = new PropertyDescriptor("onMouseOut",beanClass,"getOnMouseOut","setOnMouseOut");
            prop_onMouseOut.setDisplayName(resources.getString("TableColumn_onMouseOut_DisplayName"));
            prop_onMouseOut.setShortDescription(resources.getString("TableColumn_onMouseOut_Description"));
            prop_onMouseOut.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOut.setExpert(false);
            prop_onMouseOut.setHidden(false);
            prop_onMouseOut.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOut",false,null,true);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOver = new PropertyDescriptor("onMouseOver",beanClass,"getOnMouseOver","setOnMouseOver");
            prop_onMouseOver.setDisplayName(resources.getString("TableColumn_onMouseOver_DisplayName"));
            prop_onMouseOver.setShortDescription(resources.getString("TableColumn_onMouseOver_Description"));
            prop_onMouseOver.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOver.setExpert(false);
            prop_onMouseOver.setHidden(false);
            prop_onMouseOver.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOver",false,null,true);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseUp = new PropertyDescriptor("onMouseUp",beanClass,"getOnMouseUp","setOnMouseUp");
            prop_onMouseUp.setDisplayName(resources.getString("TableColumn_onMouseUp_DisplayName"));
            prop_onMouseUp.setShortDescription(resources.getString("TableColumn_onMouseUp_Description"));
            prop_onMouseUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseUp.setExpert(false);
            prop_onMouseUp.setHidden(false);
            prop_onMouseUp.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseUp",false,null,true);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_rowHeader = new PropertyDescriptor("rowHeader",beanClass,"isRowHeader","setRowHeader");
            prop_rowHeader.setDisplayName(resources.getString("TableColumn_rowHeader_DisplayName"));
            prop_rowHeader.setShortDescription(resources.getString("TableColumn_rowHeader_Description"));
            prop_rowHeader.setExpert(false);
            prop_rowHeader.setHidden(false);
            prop_rowHeader.setPreferred(false);
            attrib = new AttributeDescriptor("rowHeader",false,null,true);
            prop_rowHeader.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rowHeader.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rowSpan = new PropertyDescriptor("rowSpan",beanClass,"getRowSpan","setRowSpan");
            prop_rowSpan.setDisplayName(resources.getString("TableColumn_rowSpan_DisplayName"));
            prop_rowSpan.setShortDescription(resources.getString("TableColumn_rowSpan_Description"));
            prop_rowSpan.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_rowSpan.setExpert(false);
            prop_rowSpan.setHidden(false);
            prop_rowSpan.setPreferred(false);
            prop_rowSpan.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.LAYOUT);
            prop_rowSpan.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");

            PropertyDescriptor prop_scope = new PropertyDescriptor("scope",beanClass,"getScope","setScope");
            prop_scope.setDisplayName(resources.getString("TableColumn_scope_DisplayName"));
            prop_scope.setShortDescription(resources.getString("TableColumn_scope_Description"));
            prop_scope.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_scope.setExpert(false);
            prop_scope.setHidden(false);
            prop_scope.setPreferred(false);
            attrib = new AttributeDescriptor("scope",false,null,true);
            prop_scope.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_scope.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_selectId = new PropertyDescriptor("selectId",beanClass,"getSelectId","setSelectId");
            prop_selectId.setDisplayName(resources.getString("TableColumn_selectId_DisplayName"));
            prop_selectId.setShortDescription(resources.getString("TableColumn_selectId_Description"));
            prop_selectId.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_selectId.setExpert(false);
            prop_selectId.setHidden(false);
            prop_selectId.setPreferred(false);
            attrib = new AttributeDescriptor("selectId",false,null,true);
            prop_selectId.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_selectId.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_severity = new PropertyDescriptor("severity",beanClass,"getSeverity","setSeverity");
            prop_severity.setDisplayName(resources.getString("TableColumn_severity_DisplayName"));
            prop_severity.setShortDescription(resources.getString("TableColumn_severity_Description"));
            prop_severity.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_severity.setExpert(false);
            prop_severity.setHidden(false);
            prop_severity.setPreferred(false);
            attrib = new AttributeDescriptor("severity",false,null,true);
            prop_severity.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_severity.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_severity.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.StringTokensDomain.class);
            prop_severity.setValue("com.sun.rave.propertyeditors.domains.TOKEN_STRING", "critical,major,minor,down,ok");

            PropertyDescriptor prop_sort = new PropertyDescriptor("sort",beanClass,"getSort","setSort");
            prop_sort.setDisplayName(resources.getString("TableColumn_sort_DisplayName"));
            prop_sort.setShortDescription(resources.getString("TableColumn_sort_Description"));
            prop_sort.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_sort.setExpert(false);
            prop_sort.setHidden(false);
            prop_sort.setPreferred(false);
            attrib = new AttributeDescriptor("sort",false,null,true);
            prop_sort.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_sort.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_sortIcon = new PropertyDescriptor("sortIcon",beanClass,"getSortIcon","setSortIcon");
            prop_sortIcon.setDisplayName(resources.getString("TableColumn_sortIcon_DisplayName"));
            prop_sortIcon.setShortDescription(resources.getString("TableColumn_sortIcon_Description"));
            prop_sortIcon.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_sortIcon.setExpert(false);
            prop_sortIcon.setHidden(false);
            prop_sortIcon.setPreferred(false);
            attrib = new AttributeDescriptor("sortIcon",false,null,true);
            prop_sortIcon.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_sortIcon.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_sortIcon.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.ThemeIconsDomain.class);

            PropertyDescriptor prop_sortImageURL = new PropertyDescriptor("sortImageURL",beanClass,"getSortImageURL","setSortImageURL");
            prop_sortImageURL.setDisplayName(resources.getString("TableColumn_sortImageURL_DisplayName"));
            prop_sortImageURL.setShortDescription(resources.getString("TableColumn_sortImageURL_Description"));
            prop_sortImageURL.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.ImageUrlPropertyEditor"));
            prop_sortImageURL.setExpert(false);
            prop_sortImageURL.setHidden(false);
            prop_sortImageURL.setPreferred(false);
            attrib = new AttributeDescriptor("sortImageURL",false,null,true);
            prop_sortImageURL.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_sortImageURL.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_spacerColumn = new PropertyDescriptor("spacerColumn",beanClass,"isSpacerColumn","setSpacerColumn");
            prop_spacerColumn.setDisplayName(resources.getString("TableColumn_spacerColumn_DisplayName"));
            prop_spacerColumn.setShortDescription(resources.getString("TableColumn_spacerColumn_Description"));
            prop_spacerColumn.setExpert(false);
            prop_spacerColumn.setHidden(false);
            prop_spacerColumn.setPreferred(false);
            attrib = new AttributeDescriptor("spacerColumn",false,null,true);
            prop_spacerColumn.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_spacerColumn.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.LAYOUT);

            PropertyDescriptor prop_style = new PropertyDescriptor("style",beanClass,"getStyle","setStyle");
            prop_style.setDisplayName(resources.getString("TableColumn_style_DisplayName"));
            prop_style.setShortDescription(resources.getString("TableColumn_style_Description"));
            prop_style.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.css.CssStylePropertyEditor"));
            prop_style.setExpert(false);
            prop_style.setHidden(false);
            prop_style.setPreferred(false);
            attrib = new AttributeDescriptor("style",false,null,true);
            prop_style.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_style.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_styleClass = new PropertyDescriptor("styleClass",beanClass,"getStyleClass","setStyleClass");
            prop_styleClass.setDisplayName(resources.getString("TableColumn_styleClass_DisplayName"));
            prop_styleClass.setShortDescription(resources.getString("TableColumn_styleClass_Description"));
            prop_styleClass.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StyleClassPropertyEditor"));
            prop_styleClass.setExpert(false);
            prop_styleClass.setHidden(false);
            prop_styleClass.setPreferred(false);
            attrib = new AttributeDescriptor("styleClass",false,null,true);
            prop_styleClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_styleClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_tableFooterText = new PropertyDescriptor("tableFooterText",beanClass,"getTableFooterText","setTableFooterText");
            prop_tableFooterText.setDisplayName(resources.getString("TableColumn_tableFooterText_DisplayName"));
            prop_tableFooterText.setShortDescription(resources.getString("TableColumn_tableFooterText_Description"));
            prop_tableFooterText.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_tableFooterText.setExpert(false);
            prop_tableFooterText.setHidden(false);
            prop_tableFooterText.setPreferred(false);
            attrib = new AttributeDescriptor("tableFooterText",false,null,true);
            prop_tableFooterText.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tableFooterText.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_toolTip = new PropertyDescriptor("toolTip",beanClass,"getToolTip","setToolTip");
            prop_toolTip.setDisplayName(resources.getString("TableColumn_toolTip_DisplayName"));
            prop_toolTip.setShortDescription(resources.getString("TableColumn_toolTip_Description"));
            prop_toolTip.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_toolTip.setExpert(false);
            prop_toolTip.setHidden(false);
            prop_toolTip.setPreferred(false);
            attrib = new AttributeDescriptor("toolTip",false,null,true);
            prop_toolTip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_toolTip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_valign = new PropertyDescriptor("valign",beanClass,"getValign","setValign");
            prop_valign.setDisplayName(resources.getString("TableColumn_valign_DisplayName"));
            prop_valign.setShortDescription(resources.getString("TableColumn_valign_Description"));
            prop_valign.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_valign.setExpert(false);
            prop_valign.setHidden(false);
            prop_valign.setPreferred(false);
            attrib = new AttributeDescriptor("valign",false,null,true);
            prop_valign.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_valign.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_valign.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlVerticalAlignDomain.class);

            PropertyDescriptor prop_visible = new PropertyDescriptor("visible",beanClass,"isVisible","setVisible");
            prop_visible.setDisplayName(resources.getString("TableColumn_visible_DisplayName"));
            prop_visible.setShortDescription(resources.getString("TableColumn_visible_Description"));
            prop_visible.setExpert(false);
            prop_visible.setHidden(false);
            prop_visible.setPreferred(false);
            attrib = new AttributeDescriptor("visible",false,"true",true);
            prop_visible.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_visible.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_width = new PropertyDescriptor("width",beanClass,"getWidth","setWidth");
            prop_width.setDisplayName(resources.getString("TableColumn_width_DisplayName"));
            prop_width.setShortDescription(resources.getString("TableColumn_width_Description"));
            prop_width.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_width.setExpert(false);
            prop_width.setHidden(false);
            prop_width.setPreferred(false);
            attrib = new AttributeDescriptor("width",false,null,true);
            prop_width.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_width.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.LAYOUT);

            PropertyDescriptor prop_attributes = new PropertyDescriptor("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("TableColumn_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("TableColumn_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptor("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("TableColumn_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("TableColumn_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptor("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("TableColumn_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("TableColumn_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_facets = new PropertyDescriptor("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("TableColumn_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("TableColumn_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptor("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("TableColumn_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("TableColumn_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptor("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("TableColumn_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("TableColumn_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,false);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_parent = new PropertyDescriptor("parent",beanClass,"getParent","setParent");
            prop_parent.setDisplayName(resources.getString("TableColumn_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("TableColumn_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("TableColumn_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("TableColumn_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptor("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("TableColumn_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("TableColumn_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptor("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("TableColumn_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("TableColumn_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            propDescriptors = new PropertyDescriptor[] {
                prop_abbr,
                prop_align,
                prop_alignKey,
                prop_attributes,
                prop_axis,
                prop_bgColor,
                prop_char,
                prop_charOff,
                prop_childCount,
                prop_children,
                prop_colSpan,
                prop_descending,
                prop_embeddedActions,
                prop_emptyCell,
                prop_extraFooterHtml,
                prop_extraHeaderHtml,
                prop_extraTableFooterHtml,
                prop_facets,
                prop_family,
                prop_footerText,
                prop_headerText,
                prop_headers,
                prop_height,
                prop_id,
                prop_noWrap,
                prop_onClick,
                prop_onDblClick,
                prop_onKeyDown,
                prop_onKeyPress,
                prop_onKeyUp,
                prop_onMouseDown,
                prop_onMouseMove,
                prop_onMouseOut,
                prop_onMouseOver,
                prop_onMouseUp,
                prop_parent,
                prop_rendered,
                prop_rendererType,
                prop_rendersChildren,
                prop_rowHeader,
                prop_rowSpan,
                prop_scope,
                prop_selectId,
                prop_severity,
                prop_sort,
                prop_sortIcon,
                prop_sortImageURL,
                prop_spacerColumn,
                prop_style,
                prop_styleClass,
                prop_tableFooterText,
                prop_toolTip,
                prop_valign,
                prop_visible,
                prop_width,
            };
            return propDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}
//GEN-END:BeanInfo
