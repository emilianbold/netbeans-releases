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

abstract class TableRowGroupBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-JSF", Locale.getDefault(), TableRowGroupBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>TableRowGroupBeanInfoBase</code>.</p>
     */
    public TableRowGroupBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.component.TableRowGroup.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TableRowGroup_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TableRowGroup_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TableRowGroup_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TableRowGroup_M32";

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
        beanDescriptor.setDisplayName(resources.getString("TableRowGroup_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("TableRowGroup_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_bh_row_group");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"tableRowGroup");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_palette_bh_propsheets_bh_row_group_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"tableRowGroup");
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

            PropertyDescriptor prop_aboveColumnFooter = new PropertyDescriptor("aboveColumnFooter",beanClass,"isAboveColumnFooter","setAboveColumnFooter");
            prop_aboveColumnFooter.setDisplayName(resources.getString("TableRowGroup_aboveColumnFooter_DisplayName"));
            prop_aboveColumnFooter.setShortDescription(resources.getString("TableRowGroup_aboveColumnFooter_Description"));
            prop_aboveColumnFooter.setExpert(false);
            prop_aboveColumnFooter.setHidden(false);
            prop_aboveColumnFooter.setPreferred(false);
            attrib = new AttributeDescriptor("aboveColumnFooter",false,null,true);
            prop_aboveColumnFooter.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_aboveColumnFooter.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_aboveColumnHeader = new PropertyDescriptor("aboveColumnHeader",beanClass,"isAboveColumnHeader","setAboveColumnHeader");
            prop_aboveColumnHeader.setDisplayName(resources.getString("TableRowGroup_aboveColumnHeader_DisplayName"));
            prop_aboveColumnHeader.setShortDescription(resources.getString("TableRowGroup_aboveColumnHeader_Description"));
            prop_aboveColumnHeader.setExpert(false);
            prop_aboveColumnHeader.setHidden(false);
            prop_aboveColumnHeader.setPreferred(false);
            attrib = new AttributeDescriptor("aboveColumnHeader",false,null,true);
            prop_aboveColumnHeader.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_aboveColumnHeader.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_align = new PropertyDescriptor("align",beanClass,"getAlign","setAlign");
            prop_align.setDisplayName(resources.getString("TableRowGroup_align_DisplayName"));
            prop_align.setShortDescription(resources.getString("TableRowGroup_align_Description"));
            prop_align.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_align.setExpert(false);
            prop_align.setHidden(false);
            prop_align.setPreferred(false);
            attrib = new AttributeDescriptor("align",false,null,true);
            prop_align.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_align.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_align.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.StringTokensDomain.class);
            prop_align.setValue("com.sun.rave.propertyeditors.domains.TOKEN_STRING", "left,center,right,justify,char");

            PropertyDescriptor prop_bgColor = new PropertyDescriptor("bgColor",beanClass,"getBgColor","setBgColor");
            prop_bgColor.setDisplayName(resources.getString("TableRowGroup_bgColor_DisplayName"));
            prop_bgColor.setShortDescription(resources.getString("TableRowGroup_bgColor_Description"));
            prop_bgColor.setExpert(false);
            prop_bgColor.setHidden(true);
            prop_bgColor.setPreferred(false);
            prop_bgColor.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_char = new PropertyDescriptor("char",beanClass,"getChar","setChar");
            prop_char.setDisplayName(resources.getString("TableRowGroup_char_DisplayName"));
            prop_char.setShortDescription(resources.getString("TableRowGroup_char_Description"));
            prop_char.setExpert(false);
            prop_char.setHidden(true);
            prop_char.setPreferred(false);
            prop_char.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_charOff = new PropertyDescriptor("charOff",beanClass,"getCharOff","setCharOff");
            prop_charOff.setDisplayName(resources.getString("TableRowGroup_charOff_DisplayName"));
            prop_charOff.setShortDescription(resources.getString("TableRowGroup_charOff_Description"));
            prop_charOff.setExpert(false);
            prop_charOff.setHidden(true);
            prop_charOff.setPreferred(false);
            prop_charOff.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_collapsed = new PropertyDescriptor("collapsed",beanClass,"isCollapsed","setCollapsed");
            prop_collapsed.setDisplayName(resources.getString("TableRowGroup_collapsed_DisplayName"));
            prop_collapsed.setShortDescription(resources.getString("TableRowGroup_collapsed_Description"));
            prop_collapsed.setExpert(false);
            prop_collapsed.setHidden(false);
            prop_collapsed.setPreferred(false);
            attrib = new AttributeDescriptor("collapsed",false,null,true);
            prop_collapsed.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_collapsed.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_emptyDataMsg = new PropertyDescriptor("emptyDataMsg",beanClass,"getEmptyDataMsg","setEmptyDataMsg");
            prop_emptyDataMsg.setDisplayName(resources.getString("TableRowGroup_emptyDataMsg_DisplayName"));
            prop_emptyDataMsg.setShortDescription(resources.getString("TableRowGroup_emptyDataMsg_Description"));
            prop_emptyDataMsg.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_emptyDataMsg.setExpert(false);
            prop_emptyDataMsg.setHidden(false);
            prop_emptyDataMsg.setPreferred(false);
            attrib = new AttributeDescriptor("emptyDataMsg",false,null,true);
            prop_emptyDataMsg.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_emptyDataMsg.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_extraFooterHtml = new PropertyDescriptor("extraFooterHtml",beanClass,"getExtraFooterHtml","setExtraFooterHtml");
            prop_extraFooterHtml.setDisplayName(resources.getString("TableRowGroup_extraFooterHtml_DisplayName"));
            prop_extraFooterHtml.setShortDescription(resources.getString("TableRowGroup_extraFooterHtml_Description"));
            prop_extraFooterHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraFooterHtml.setExpert(false);
            prop_extraFooterHtml.setHidden(false);
            prop_extraFooterHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraFooterHtml",false,null,true);
            prop_extraFooterHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraFooterHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_extraHeaderHtml = new PropertyDescriptor("extraHeaderHtml",beanClass,"getExtraHeaderHtml","setExtraHeaderHtml");
            prop_extraHeaderHtml.setDisplayName(resources.getString("TableRowGroup_extraHeaderHtml_DisplayName"));
            prop_extraHeaderHtml.setShortDescription(resources.getString("TableRowGroup_extraHeaderHtml_Description"));
            prop_extraHeaderHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraHeaderHtml.setExpert(false);
            prop_extraHeaderHtml.setHidden(false);
            prop_extraHeaderHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraHeaderHtml",false,null,true);
            prop_extraHeaderHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraHeaderHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_first = new PropertyDescriptor("first",beanClass,"getFirst","setFirst");
            prop_first.setDisplayName(resources.getString("TableRowGroup_first_DisplayName"));
            prop_first.setShortDescription(resources.getString("TableRowGroup_first_Description"));
            prop_first.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_first.setExpert(false);
            prop_first.setHidden(false);
            prop_first.setPreferred(false);
            attrib = new AttributeDescriptor("first",false,"0",true);
            prop_first.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_first.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);
            prop_first.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");

            PropertyDescriptor prop_footerText = new PropertyDescriptor("footerText",beanClass,"getFooterText","setFooterText");
            prop_footerText.setDisplayName(resources.getString("TableRowGroup_footerText_DisplayName"));
            prop_footerText.setShortDescription(resources.getString("TableRowGroup_footerText_Description"));
            prop_footerText.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_footerText.setExpert(false);
            prop_footerText.setHidden(false);
            prop_footerText.setPreferred(false);
            attrib = new AttributeDescriptor("footerText",false,null,true);
            prop_footerText.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_footerText.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_groupToggleButton = new PropertyDescriptor("groupToggleButton",beanClass,"isGroupToggleButton","setGroupToggleButton");
            prop_groupToggleButton.setDisplayName(resources.getString("TableRowGroup_groupToggleButton_DisplayName"));
            prop_groupToggleButton.setShortDescription(resources.getString("TableRowGroup_groupToggleButton_Description"));
            prop_groupToggleButton.setExpert(false);
            prop_groupToggleButton.setHidden(false);
            prop_groupToggleButton.setPreferred(false);
            attrib = new AttributeDescriptor("groupToggleButton",false,null,true);
            prop_groupToggleButton.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_groupToggleButton.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_headerText = new PropertyDescriptor("headerText",beanClass,"getHeaderText","setHeaderText");
            prop_headerText.setDisplayName(resources.getString("TableRowGroup_headerText_DisplayName"));
            prop_headerText.setShortDescription(resources.getString("TableRowGroup_headerText_Description"));
            prop_headerText.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_headerText.setExpert(false);
            prop_headerText.setHidden(false);
            prop_headerText.setPreferred(false);
            attrib = new AttributeDescriptor("headerText",false,null,true);
            prop_headerText.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_headerText.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_multipleColumnFooters = new PropertyDescriptor("multipleColumnFooters",beanClass,"isMultipleColumnFooters","setMultipleColumnFooters");
            prop_multipleColumnFooters.setDisplayName(resources.getString("TableRowGroup_multipleColumnFooters_DisplayName"));
            prop_multipleColumnFooters.setShortDescription(resources.getString("TableRowGroup_multipleColumnFooters_Description"));
            prop_multipleColumnFooters.setExpert(false);
            prop_multipleColumnFooters.setHidden(false);
            prop_multipleColumnFooters.setPreferred(false);
            attrib = new AttributeDescriptor("multipleColumnFooters",false,null,true);
            prop_multipleColumnFooters.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_multipleColumnFooters.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.LAYOUT);

            PropertyDescriptor prop_multipleTableColumnFooters = new PropertyDescriptor("multipleTableColumnFooters",beanClass,"isMultipleTableColumnFooters","setMultipleTableColumnFooters");
            prop_multipleTableColumnFooters.setDisplayName(resources.getString("TableRowGroup_multipleTableColumnFooters_DisplayName"));
            prop_multipleTableColumnFooters.setShortDescription(resources.getString("TableRowGroup_multipleTableColumnFooters_Description"));
            prop_multipleTableColumnFooters.setExpert(false);
            prop_multipleTableColumnFooters.setHidden(false);
            prop_multipleTableColumnFooters.setPreferred(false);
            attrib = new AttributeDescriptor("multipleTableColumnFooters",false,null,true);
            prop_multipleTableColumnFooters.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_multipleTableColumnFooters.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.LAYOUT);

            PropertyDescriptor prop_onClick = new PropertyDescriptor("onClick",beanClass,"getOnClick","setOnClick");
            prop_onClick.setDisplayName(resources.getString("TableRowGroup_onClick_DisplayName"));
            prop_onClick.setShortDescription(resources.getString("TableRowGroup_onClick_Description"));
            prop_onClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onClick.setExpert(false);
            prop_onClick.setHidden(false);
            prop_onClick.setPreferred(false);
            attrib = new AttributeDescriptor("onClick",false,null,true);
            prop_onClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onDblClick = new PropertyDescriptor("onDblClick",beanClass,"getOnDblClick","setOnDblClick");
            prop_onDblClick.setDisplayName(resources.getString("TableRowGroup_onDblClick_DisplayName"));
            prop_onDblClick.setShortDescription(resources.getString("TableRowGroup_onDblClick_Description"));
            prop_onDblClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onDblClick.setExpert(false);
            prop_onDblClick.setHidden(false);
            prop_onDblClick.setPreferred(false);
            attrib = new AttributeDescriptor("onDblClick",false,null,true);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyDown = new PropertyDescriptor("onKeyDown",beanClass,"getOnKeyDown","setOnKeyDown");
            prop_onKeyDown.setDisplayName(resources.getString("TableRowGroup_onKeyDown_DisplayName"));
            prop_onKeyDown.setShortDescription(resources.getString("TableRowGroup_onKeyDown_Description"));
            prop_onKeyDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyDown.setExpert(false);
            prop_onKeyDown.setHidden(false);
            prop_onKeyDown.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyDown",false,null,true);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyPress = new PropertyDescriptor("onKeyPress",beanClass,"getOnKeyPress","setOnKeyPress");
            prop_onKeyPress.setDisplayName(resources.getString("TableRowGroup_onKeyPress_DisplayName"));
            prop_onKeyPress.setShortDescription(resources.getString("TableRowGroup_onKeyPress_Description"));
            prop_onKeyPress.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyPress.setExpert(false);
            prop_onKeyPress.setHidden(false);
            prop_onKeyPress.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyPress",false,null,true);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyUp = new PropertyDescriptor("onKeyUp",beanClass,"getOnKeyUp","setOnKeyUp");
            prop_onKeyUp.setDisplayName(resources.getString("TableRowGroup_onKeyUp_DisplayName"));
            prop_onKeyUp.setShortDescription(resources.getString("TableRowGroup_onKeyUp_Description"));
            prop_onKeyUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyUp.setExpert(false);
            prop_onKeyUp.setHidden(false);
            prop_onKeyUp.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyUp",false,null,true);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseDown = new PropertyDescriptor("onMouseDown",beanClass,"getOnMouseDown","setOnMouseDown");
            prop_onMouseDown.setDisplayName(resources.getString("TableRowGroup_onMouseDown_DisplayName"));
            prop_onMouseDown.setShortDescription(resources.getString("TableRowGroup_onMouseDown_Description"));
            prop_onMouseDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseDown.setExpert(false);
            prop_onMouseDown.setHidden(false);
            prop_onMouseDown.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseDown",false,null,true);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseMove = new PropertyDescriptor("onMouseMove",beanClass,"getOnMouseMove","setOnMouseMove");
            prop_onMouseMove.setDisplayName(resources.getString("TableRowGroup_onMouseMove_DisplayName"));
            prop_onMouseMove.setShortDescription(resources.getString("TableRowGroup_onMouseMove_Description"));
            prop_onMouseMove.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseMove.setExpert(false);
            prop_onMouseMove.setHidden(false);
            prop_onMouseMove.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseMove",false,null,true);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOut = new PropertyDescriptor("onMouseOut",beanClass,"getOnMouseOut","setOnMouseOut");
            prop_onMouseOut.setDisplayName(resources.getString("TableRowGroup_onMouseOut_DisplayName"));
            prop_onMouseOut.setShortDescription(resources.getString("TableRowGroup_onMouseOut_Description"));
            prop_onMouseOut.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOut.setExpert(false);
            prop_onMouseOut.setHidden(false);
            prop_onMouseOut.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOut",false,null,true);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOver = new PropertyDescriptor("onMouseOver",beanClass,"getOnMouseOver","setOnMouseOver");
            prop_onMouseOver.setDisplayName(resources.getString("TableRowGroup_onMouseOver_DisplayName"));
            prop_onMouseOver.setShortDescription(resources.getString("TableRowGroup_onMouseOver_Description"));
            prop_onMouseOver.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOver.setExpert(false);
            prop_onMouseOver.setHidden(false);
            prop_onMouseOver.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOver",false,null,true);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseUp = new PropertyDescriptor("onMouseUp",beanClass,"getOnMouseUp","setOnMouseUp");
            prop_onMouseUp.setDisplayName(resources.getString("TableRowGroup_onMouseUp_DisplayName"));
            prop_onMouseUp.setShortDescription(resources.getString("TableRowGroup_onMouseUp_Description"));
            prop_onMouseUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseUp.setExpert(false);
            prop_onMouseUp.setHidden(false);
            prop_onMouseUp.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseUp",false,null,true);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_rows = new PropertyDescriptor("rows",beanClass,"getRows","setRows");
            prop_rows.setDisplayName(resources.getString("TableRowGroup_rows_DisplayName"));
            prop_rows.setShortDescription(resources.getString("TableRowGroup_rows_Description"));
            prop_rows.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_rows.setExpert(false);
            prop_rows.setHidden(false);
            prop_rows.setPreferred(false);
            attrib = new AttributeDescriptor("rows",false,"25",true);
            prop_rows.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rows.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_rows.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");

            PropertyDescriptor prop_selectMultipleToggleButton = new PropertyDescriptor("selectMultipleToggleButton",beanClass,"isSelectMultipleToggleButton","setSelectMultipleToggleButton");
            prop_selectMultipleToggleButton.setDisplayName(resources.getString("TableRowGroup_selectMultipleToggleButton_DisplayName"));
            prop_selectMultipleToggleButton.setShortDescription(resources.getString("TableRowGroup_selectMultipleToggleButton_Description"));
            prop_selectMultipleToggleButton.setExpert(false);
            prop_selectMultipleToggleButton.setHidden(false);
            prop_selectMultipleToggleButton.setPreferred(false);
            attrib = new AttributeDescriptor("selectMultipleToggleButton",false,null,true);
            prop_selectMultipleToggleButton.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_selectMultipleToggleButton.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_selected = new PropertyDescriptor("selected",beanClass,"isSelected","setSelected");
            prop_selected.setDisplayName(resources.getString("TableRowGroup_selected_DisplayName"));
            prop_selected.setShortDescription(resources.getString("TableRowGroup_selected_Description"));
            prop_selected.setExpert(false);
            prop_selected.setHidden(false);
            prop_selected.setPreferred(false);
            attrib = new AttributeDescriptor("selected",false,null,true);
            prop_selected.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_selected.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_sourceData = new PropertyDescriptor("sourceData",beanClass,"getSourceData","setSourceData");
            prop_sourceData.setDisplayName(resources.getString("TableRowGroup_sourceData_DisplayName"));
            prop_sourceData.setShortDescription(resources.getString("TableRowGroup_sourceData_Description"));
            prop_sourceData.setExpert(false);
            prop_sourceData.setHidden(false);
            prop_sourceData.setPreferred(false);
            attrib = new AttributeDescriptor("sourceData",false,null,true);
            prop_sourceData.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_sourceData.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_sourceVar = new PropertyDescriptor("sourceVar",beanClass,"getSourceVar","setSourceVar");
            prop_sourceVar.setDisplayName(resources.getString("TableRowGroup_sourceVar_DisplayName"));
            prop_sourceVar.setShortDescription(resources.getString("TableRowGroup_sourceVar_Description"));
            prop_sourceVar.setExpert(false);
            prop_sourceVar.setHidden(false);
            prop_sourceVar.setPreferred(false);
            attrib = new AttributeDescriptor("sourceVar",false,null,true);
            prop_sourceVar.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_sourceVar.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_styleClasses = new PropertyDescriptor("styleClasses",beanClass,"getStyleClasses","setStyleClasses");
            prop_styleClasses.setDisplayName(resources.getString("TableRowGroup_styleClasses_DisplayName"));
            prop_styleClasses.setShortDescription(resources.getString("TableRowGroup_styleClasses_Description"));
            prop_styleClasses.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StyleClassPropertyEditor"));
            prop_styleClasses.setExpert(false);
            prop_styleClasses.setHidden(false);
            prop_styleClasses.setPreferred(false);
            attrib = new AttributeDescriptor("styleClasses",false,null,true);
            prop_styleClasses.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_styleClasses.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_tableDataFilter = new PropertyDescriptor("tableDataFilter",beanClass,"getTableDataFilter","setTableDataFilter");
            prop_tableDataFilter.setDisplayName(resources.getString("TableRowGroup_tableDataFilter_DisplayName"));
            prop_tableDataFilter.setShortDescription(resources.getString("TableRowGroup_tableDataFilter_Description"));
            prop_tableDataFilter.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_tableDataFilter.setExpert(false);
            prop_tableDataFilter.setHidden(true);
            prop_tableDataFilter.setPreferred(false);
            attrib = new AttributeDescriptor("tableDataFilter",false,null,true);
            prop_tableDataFilter.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tableDataFilter.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_tableDataFilter.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.FieldKeyDomain.class);

            PropertyDescriptor prop_tableDataSorter = new PropertyDescriptor("tableDataSorter",beanClass,"getTableDataSorter","setTableDataSorter");
            prop_tableDataSorter.setDisplayName(resources.getString("TableRowGroup_tableDataSorter_DisplayName"));
            prop_tableDataSorter.setShortDescription(resources.getString("TableRowGroup_tableDataSorter_Description"));
            prop_tableDataSorter.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_tableDataSorter.setExpert(false);
            prop_tableDataSorter.setHidden(true);
            prop_tableDataSorter.setPreferred(false);
            attrib = new AttributeDescriptor("tableDataSorter",false,null,true);
            prop_tableDataSorter.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tableDataSorter.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_tableDataSorter.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.FieldKeyDomain.class);

            PropertyDescriptor prop_toolTip = new PropertyDescriptor("toolTip",beanClass,"getToolTip","setToolTip");
            prop_toolTip.setDisplayName(resources.getString("TableRowGroup_toolTip_DisplayName"));
            prop_toolTip.setShortDescription(resources.getString("TableRowGroup_toolTip_Description"));
            prop_toolTip.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_toolTip.setExpert(false);
            prop_toolTip.setHidden(false);
            prop_toolTip.setPreferred(false);
            attrib = new AttributeDescriptor("toolTip",false,null,true);
            prop_toolTip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_toolTip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_valign = new PropertyDescriptor("valign",beanClass,"getValign","setValign");
            prop_valign.setDisplayName(resources.getString("TableRowGroup_valign_DisplayName"));
            prop_valign.setShortDescription(resources.getString("TableRowGroup_valign_Description"));
            prop_valign.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_valign.setExpert(false);
            prop_valign.setHidden(false);
            prop_valign.setPreferred(false);
            attrib = new AttributeDescriptor("valign",false,null,true);
            prop_valign.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_valign.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_valign.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlVerticalAlignDomain.class);

            PropertyDescriptor prop_visible = new PropertyDescriptor("visible",beanClass,"isVisible","setVisible");
            prop_visible.setDisplayName(resources.getString("TableRowGroup_visible_DisplayName"));
            prop_visible.setShortDescription(resources.getString("TableRowGroup_visible_Description"));
            prop_visible.setExpert(false);
            prop_visible.setHidden(false);
            prop_visible.setPreferred(false);
            attrib = new AttributeDescriptor("visible",false,"true",true);
            prop_visible.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_visible.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_attributes = new PropertyDescriptor("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("TableRowGroup_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("TableRowGroup_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptor("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("TableRowGroup_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("TableRowGroup_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptor("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("TableRowGroup_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("TableRowGroup_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_facets = new PropertyDescriptor("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("TableRowGroup_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("TableRowGroup_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptor("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("TableRowGroup_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("TableRowGroup_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptor("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("TableRowGroup_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("TableRowGroup_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,false);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_parent = new PropertyDescriptor("parent",beanClass,"getParent","setParent");
            prop_parent.setDisplayName(resources.getString("TableRowGroup_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("TableRowGroup_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("TableRowGroup_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("TableRowGroup_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptor("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("TableRowGroup_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("TableRowGroup_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptor("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("TableRowGroup_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("TableRowGroup_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            propDescriptors = new PropertyDescriptor[] {
                prop_aboveColumnFooter,
                prop_aboveColumnHeader,
                prop_align,
                prop_attributes,
                prop_bgColor,
                prop_char,
                prop_charOff,
                prop_childCount,
                prop_children,
                prop_collapsed,
                prop_emptyDataMsg,
                prop_extraFooterHtml,
                prop_extraHeaderHtml,
                prop_facets,
                prop_family,
                prop_first,
                prop_footerText,
                prop_groupToggleButton,
                prop_headerText,
                prop_id,
                prop_multipleColumnFooters,
                prop_multipleTableColumnFooters,
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
                prop_rows,
                prop_selectMultipleToggleButton,
                prop_selected,
                prop_sourceData,
                prop_sourceVar,
                prop_styleClasses,
                prop_tableDataFilter,
                prop_tableDataSorter,
                prop_toolTip,
                prop_valign,
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
