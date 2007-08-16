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

abstract class ImageHyperlinkBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-JSF", Locale.getDefault(), ImageHyperlinkBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>ImageHyperlinkBeanInfoBase</code>.</p>
     */
    public ImageHyperlinkBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.component.ImageHyperlink.class;
        defaultPropertyName = "text";
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/ImageHyperlink_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/ImageHyperlink_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/ImageHyperlink_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/ImageHyperlink_M32";

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
        beanDescriptor.setDisplayName(resources.getString("ImageHyperlink_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("ImageHyperlink_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_bh_image_hyperlink");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"imageHyperlink");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_palette_bh_propsheets_bh_image_hyperlink_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"imageHyperlink");
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

            PropertyDescriptor prop_align = new PropertyDescriptor("align",beanClass,"getAlign","setAlign");
            prop_align.setDisplayName(resources.getString("ImageHyperlink_align_DisplayName"));
            prop_align.setShortDescription(resources.getString("ImageHyperlink_align_Description"));
            prop_align.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_align.setExpert(false);
            prop_align.setHidden(false);
            prop_align.setPreferred(false);
            attrib = new AttributeDescriptor("align",false,null,true);
            prop_align.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_align.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_align.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlAlignDomain.class);

            PropertyDescriptor prop_alt = new PropertyDescriptor("alt",beanClass,"getAlt","setAlt");
            prop_alt.setDisplayName(resources.getString("ImageHyperlink_alt_DisplayName"));
            prop_alt.setShortDescription(resources.getString("ImageHyperlink_alt_Description"));
            prop_alt.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_alt.setExpert(false);
            prop_alt.setHidden(false);
            prop_alt.setPreferred(false);
            attrib = new AttributeDescriptor("alt",false,null,true);
            prop_alt.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_alt.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ACCESSIBILITY);

            PropertyDescriptor prop_border = new PropertyDescriptor("border",beanClass,"getBorder","setBorder");
            prop_border.setDisplayName(resources.getString("ImageHyperlink_border_DisplayName"));
            prop_border.setShortDescription(resources.getString("ImageHyperlink_border_Description"));
            prop_border.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_border.setExpert(false);
            prop_border.setHidden(false);
            prop_border.setPreferred(false);
            attrib = new AttributeDescriptor("border",false,null,true);
            prop_border.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_border.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_border.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "0");

            PropertyDescriptor prop_height = new PropertyDescriptor("height",beanClass,"getHeight","setHeight");
            prop_height.setDisplayName(resources.getString("ImageHyperlink_height_DisplayName"));
            prop_height.setShortDescription(resources.getString("ImageHyperlink_height_Description"));
            prop_height.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_height.setExpert(false);
            prop_height.setHidden(false);
            prop_height.setPreferred(false);
            attrib = new AttributeDescriptor("height",false,null,true);
            prop_height.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_height.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_height.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_hspace = new PropertyDescriptor("hspace",beanClass,"getHspace","setHspace");
            prop_hspace.setDisplayName(resources.getString("ImageHyperlink_hspace_DisplayName"));
            prop_hspace.setShortDescription(resources.getString("ImageHyperlink_hspace_Description"));
            prop_hspace.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_hspace.setExpert(false);
            prop_hspace.setHidden(false);
            prop_hspace.setPreferred(false);
            attrib = new AttributeDescriptor("hspace",false,null,true);
            prop_hspace.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_hspace.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_hspace.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_icon = new PropertyDescriptor("icon",beanClass,"getIcon","setIcon");
            prop_icon.setDisplayName(resources.getString("ImageHyperlink_icon_DisplayName"));
            prop_icon.setShortDescription(resources.getString("ImageHyperlink_icon_Description"));
            prop_icon.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_icon.setExpert(false);
            prop_icon.setHidden(false);
            prop_icon.setPreferred(false);
            attrib = new AttributeDescriptor("icon",false,null,true);
            prop_icon.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_icon.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_icon.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.ThemeIconsDomain.class);

            PropertyDescriptor prop_imageURL = new PropertyDescriptor("imageURL",beanClass,"getImageURL","setImageURL");
            prop_imageURL.setDisplayName(resources.getString("ImageHyperlink_imageURL_DisplayName"));
            prop_imageURL.setShortDescription(resources.getString("ImageHyperlink_imageURL_Description"));
            prop_imageURL.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.ImageUrlPropertyEditor"));
            prop_imageURL.setExpert(false);
            prop_imageURL.setHidden(false);
            prop_imageURL.setPreferred(false);
            attrib = new AttributeDescriptor("imageURL",false,null,true);
            prop_imageURL.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_imageURL.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_textPosition = new PropertyDescriptor("textPosition",beanClass,"getTextPosition","setTextPosition");
            prop_textPosition.setDisplayName(resources.getString("ImageHyperlink_textPosition_DisplayName"));
            prop_textPosition.setShortDescription(resources.getString("ImageHyperlink_textPosition_Description"));
            prop_textPosition.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_textPosition.setExpert(false);
            prop_textPosition.setHidden(false);
            prop_textPosition.setPreferred(false);
            attrib = new AttributeDescriptor("textPosition",false,"\"right\"",true);
            prop_textPosition.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_textPosition.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_textPosition.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlHorizontalAlignDomain.class);

            PropertyDescriptor prop_value = new PropertyDescriptor("value",beanClass,"getValue","setValue");
            prop_value.setDisplayName(resources.getString("ImageHyperlink_value_DisplayName"));
            prop_value.setShortDescription(resources.getString("ImageHyperlink_value_Description"));
            prop_value.setExpert(false);
            prop_value.setHidden(true);
            prop_value.setPreferred(false);
            attrib = new AttributeDescriptor("value",false,null,true);
            prop_value.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_value.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_visible = new PropertyDescriptor("visible",beanClass,"isVisible","setVisible");
            prop_visible.setDisplayName(resources.getString("ImageHyperlink_visible_DisplayName"));
            prop_visible.setShortDescription(resources.getString("ImageHyperlink_visible_Description"));
            prop_visible.setExpert(false);
            prop_visible.setHidden(false);
            prop_visible.setPreferred(false);
            attrib = new AttributeDescriptor("visible",false,"true",true);
            prop_visible.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_visible.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_vspace = new PropertyDescriptor("vspace",beanClass,"getVspace","setVspace");
            prop_vspace.setDisplayName(resources.getString("ImageHyperlink_vspace_DisplayName"));
            prop_vspace.setShortDescription(resources.getString("ImageHyperlink_vspace_Description"));
            prop_vspace.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_vspace.setExpert(false);
            prop_vspace.setHidden(false);
            prop_vspace.setPreferred(false);
            attrib = new AttributeDescriptor("vspace",false,null,true);
            prop_vspace.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_vspace.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_vspace.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_width = new PropertyDescriptor("width",beanClass,"getWidth","setWidth");
            prop_width.setDisplayName(resources.getString("ImageHyperlink_width_DisplayName"));
            prop_width.setShortDescription(resources.getString("ImageHyperlink_width_Description"));
            prop_width.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_width.setExpert(false);
            prop_width.setHidden(false);
            prop_width.setPreferred(false);
            attrib = new AttributeDescriptor("width",false,null,true);
            prop_width.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_width.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_width.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_action = new PropertyDescriptor("action",beanClass,"getAction","setAction");
            prop_action.setDisplayName(resources.getString("ImageHyperlink_action_DisplayName"));
            prop_action.setShortDescription(resources.getString("ImageHyperlink_action_Description"));
            prop_action.setExpert(false);
            prop_action.setHidden(false);
            prop_action.setPreferred(false);
            attrib = new AttributeDescriptor("action",false,null,true);
            prop_action.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_action.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_actionListener = new PropertyDescriptor("actionListener",beanClass,"getActionListener","setActionListener");
            prop_actionListener.setDisplayName(resources.getString("ImageHyperlink_actionListener_DisplayName"));
            prop_actionListener.setShortDescription(resources.getString("ImageHyperlink_actionListener_Description"));
            prop_actionListener.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.MethodBindingPropertyEditor"));
            prop_actionListener.setExpert(false);
            prop_actionListener.setHidden(false);
            prop_actionListener.setPreferred(false);
            attrib = new AttributeDescriptor("actionListener",false,null,true);
            prop_actionListener.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_actionListener.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_disabled = new PropertyDescriptor("disabled",beanClass,"isDisabled","setDisabled");
            prop_disabled.setDisplayName(resources.getString("ImageHyperlink_disabled_DisplayName"));
            prop_disabled.setShortDescription(resources.getString("ImageHyperlink_disabled_Description"));
            prop_disabled.setExpert(false);
            prop_disabled.setHidden(false);
            prop_disabled.setPreferred(false);
            attrib = new AttributeDescriptor("disabled",false,null,true);
            prop_disabled.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_disabled.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_onBlur = new PropertyDescriptor("onBlur",beanClass,"getOnBlur","setOnBlur");
            prop_onBlur.setDisplayName(resources.getString("ImageHyperlink_onBlur_DisplayName"));
            prop_onBlur.setShortDescription(resources.getString("ImageHyperlink_onBlur_Description"));
            prop_onBlur.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onBlur.setExpert(false);
            prop_onBlur.setHidden(false);
            prop_onBlur.setPreferred(false);
            attrib = new AttributeDescriptor("onBlur",false,null,true);
            prop_onBlur.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onBlur.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onClick = new PropertyDescriptor("onClick",beanClass,"getOnClick","setOnClick");
            prop_onClick.setDisplayName(resources.getString("ImageHyperlink_onClick_DisplayName"));
            prop_onClick.setShortDescription(resources.getString("ImageHyperlink_onClick_Description"));
            prop_onClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onClick.setExpert(false);
            prop_onClick.setHidden(false);
            prop_onClick.setPreferred(false);
            attrib = new AttributeDescriptor("onClick",false,null,true);
            prop_onClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onDblClick = new PropertyDescriptor("onDblClick",beanClass,"getOnDblClick","setOnDblClick");
            prop_onDblClick.setDisplayName(resources.getString("ImageHyperlink_onDblClick_DisplayName"));
            prop_onDblClick.setShortDescription(resources.getString("ImageHyperlink_onDblClick_Description"));
            prop_onDblClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onDblClick.setExpert(false);
            prop_onDblClick.setHidden(true);
            prop_onDblClick.setPreferred(false);
            attrib = new AttributeDescriptor("onDblClick",false,null,true);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onFocus = new PropertyDescriptor("onFocus",beanClass,"getOnFocus","setOnFocus");
            prop_onFocus.setDisplayName(resources.getString("ImageHyperlink_onFocus_DisplayName"));
            prop_onFocus.setShortDescription(resources.getString("ImageHyperlink_onFocus_Description"));
            prop_onFocus.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onFocus.setExpert(false);
            prop_onFocus.setHidden(false);
            prop_onFocus.setPreferred(false);
            attrib = new AttributeDescriptor("onFocus",false,null,true);
            prop_onFocus.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onFocus.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyDown = new PropertyDescriptor("onKeyDown",beanClass,"getOnKeyDown","setOnKeyDown");
            prop_onKeyDown.setDisplayName(resources.getString("ImageHyperlink_onKeyDown_DisplayName"));
            prop_onKeyDown.setShortDescription(resources.getString("ImageHyperlink_onKeyDown_Description"));
            prop_onKeyDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyDown.setExpert(false);
            prop_onKeyDown.setHidden(false);
            prop_onKeyDown.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyDown",false,null,true);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyPress = new PropertyDescriptor("onKeyPress",beanClass,"getOnKeyPress","setOnKeyPress");
            prop_onKeyPress.setDisplayName(resources.getString("ImageHyperlink_onKeyPress_DisplayName"));
            prop_onKeyPress.setShortDescription(resources.getString("ImageHyperlink_onKeyPress_Description"));
            prop_onKeyPress.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyPress.setExpert(false);
            prop_onKeyPress.setHidden(false);
            prop_onKeyPress.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyPress",false,null,true);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyUp = new PropertyDescriptor("onKeyUp",beanClass,"getOnKeyUp","setOnKeyUp");
            prop_onKeyUp.setDisplayName(resources.getString("ImageHyperlink_onKeyUp_DisplayName"));
            prop_onKeyUp.setShortDescription(resources.getString("ImageHyperlink_onKeyUp_Description"));
            prop_onKeyUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyUp.setExpert(false);
            prop_onKeyUp.setHidden(false);
            prop_onKeyUp.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyUp",false,null,true);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseDown = new PropertyDescriptor("onMouseDown",beanClass,"getOnMouseDown","setOnMouseDown");
            prop_onMouseDown.setDisplayName(resources.getString("ImageHyperlink_onMouseDown_DisplayName"));
            prop_onMouseDown.setShortDescription(resources.getString("ImageHyperlink_onMouseDown_Description"));
            prop_onMouseDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseDown.setExpert(false);
            prop_onMouseDown.setHidden(false);
            prop_onMouseDown.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseDown",false,null,true);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseMove = new PropertyDescriptor("onMouseMove",beanClass,"getOnMouseMove","setOnMouseMove");
            prop_onMouseMove.setDisplayName(resources.getString("ImageHyperlink_onMouseMove_DisplayName"));
            prop_onMouseMove.setShortDescription(resources.getString("ImageHyperlink_onMouseMove_Description"));
            prop_onMouseMove.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseMove.setExpert(false);
            prop_onMouseMove.setHidden(false);
            prop_onMouseMove.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseMove",false,null,true);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOut = new PropertyDescriptor("onMouseOut",beanClass,"getOnMouseOut","setOnMouseOut");
            prop_onMouseOut.setDisplayName(resources.getString("ImageHyperlink_onMouseOut_DisplayName"));
            prop_onMouseOut.setShortDescription(resources.getString("ImageHyperlink_onMouseOut_Description"));
            prop_onMouseOut.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOut.setExpert(false);
            prop_onMouseOut.setHidden(false);
            prop_onMouseOut.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOut",false,null,true);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOver = new PropertyDescriptor("onMouseOver",beanClass,"getOnMouseOver","setOnMouseOver");
            prop_onMouseOver.setDisplayName(resources.getString("ImageHyperlink_onMouseOver_DisplayName"));
            prop_onMouseOver.setShortDescription(resources.getString("ImageHyperlink_onMouseOver_Description"));
            prop_onMouseOver.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOver.setExpert(false);
            prop_onMouseOver.setHidden(false);
            prop_onMouseOver.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOver",false,null,true);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseUp = new PropertyDescriptor("onMouseUp",beanClass,"getOnMouseUp","setOnMouseUp");
            prop_onMouseUp.setDisplayName(resources.getString("ImageHyperlink_onMouseUp_DisplayName"));
            prop_onMouseUp.setShortDescription(resources.getString("ImageHyperlink_onMouseUp_Description"));
            prop_onMouseUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseUp.setExpert(false);
            prop_onMouseUp.setHidden(false);
            prop_onMouseUp.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseUp",false,null,true);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_shape = new PropertyDescriptor("shape",beanClass,"getShape","setShape");
            prop_shape.setDisplayName(resources.getString("ImageHyperlink_shape_DisplayName"));
            prop_shape.setShortDescription(resources.getString("ImageHyperlink_shape_Description"));
            prop_shape.setExpert(false);
            prop_shape.setHidden(true);
            prop_shape.setPreferred(false);
            prop_shape.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_style = new PropertyDescriptor("style",beanClass,"getStyle","setStyle");
            prop_style.setDisplayName(resources.getString("ImageHyperlink_style_DisplayName"));
            prop_style.setShortDescription(resources.getString("ImageHyperlink_style_Description"));
            prop_style.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.css.CssStylePropertyEditor"));
            prop_style.setExpert(false);
            prop_style.setHidden(false);
            prop_style.setPreferred(false);
            attrib = new AttributeDescriptor("style",false,null,true);
            prop_style.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_style.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_styleClass = new PropertyDescriptor("styleClass",beanClass,"getStyleClass","setStyleClass");
            prop_styleClass.setDisplayName(resources.getString("ImageHyperlink_styleClass_DisplayName"));
            prop_styleClass.setShortDescription(resources.getString("ImageHyperlink_styleClass_Description"));
            prop_styleClass.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StyleClassPropertyEditor"));
            prop_styleClass.setExpert(false);
            prop_styleClass.setHidden(false);
            prop_styleClass.setPreferred(false);
            attrib = new AttributeDescriptor("styleClass",false,null,true);
            prop_styleClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_styleClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_tabIndex = new PropertyDescriptor("tabIndex",beanClass,"getTabIndex","setTabIndex");
            prop_tabIndex.setDisplayName(resources.getString("ImageHyperlink_tabIndex_DisplayName"));
            prop_tabIndex.setShortDescription(resources.getString("ImageHyperlink_tabIndex_Description"));
            prop_tabIndex.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_tabIndex.setExpert(false);
            prop_tabIndex.setHidden(false);
            prop_tabIndex.setPreferred(false);
            attrib = new AttributeDescriptor("tabIndex",false,null,true);
            prop_tabIndex.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tabIndex.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ACCESSIBILITY);
            prop_tabIndex.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_target = new PropertyDescriptor("target",beanClass,"getTarget","setTarget");
            prop_target.setDisplayName(resources.getString("ImageHyperlink_target_DisplayName"));
            prop_target.setShortDescription(resources.getString("ImageHyperlink_target_Description"));
            prop_target.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_target.setExpert(false);
            prop_target.setHidden(false);
            prop_target.setPreferred(false);
            attrib = new AttributeDescriptor("target",false,null,true);
            prop_target.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_target.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);
            prop_target.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlFrameTargetsDomain.class);

            PropertyDescriptor prop_text = new PropertyDescriptor("text",beanClass,"getText","setText");
            prop_text.setDisplayName(resources.getString("ImageHyperlink_text_DisplayName"));
            prop_text.setShortDescription(resources.getString("ImageHyperlink_text_Description"));
            prop_text.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_text.setExpert(false);
            prop_text.setHidden(false);
            prop_text.setPreferred(false);
            attrib = new AttributeDescriptor("text",false,null,true);
            prop_text.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_text.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_toolTip = new PropertyDescriptor("toolTip",beanClass,"getToolTip","setToolTip");
            prop_toolTip.setDisplayName(resources.getString("ImageHyperlink_toolTip_DisplayName"));
            prop_toolTip.setShortDescription(resources.getString("ImageHyperlink_toolTip_Description"));
            prop_toolTip.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_toolTip.setExpert(false);
            prop_toolTip.setHidden(false);
            prop_toolTip.setPreferred(false);
            attrib = new AttributeDescriptor("toolTip",false,null,true);
            prop_toolTip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_toolTip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_type = new PropertyDescriptor("type",beanClass,"getType","setType");
            prop_type.setDisplayName(resources.getString("ImageHyperlink_type_DisplayName"));
            prop_type.setShortDescription(resources.getString("ImageHyperlink_type_Description"));
            prop_type.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_type.setExpert(false);
            prop_type.setHidden(false);
            prop_type.setPreferred(false);
            attrib = new AttributeDescriptor("type",false,null,true);
            prop_type.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_type.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_type.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.MimeTypesDomain.class);

            PropertyDescriptor prop_url = new PropertyDescriptor("url",beanClass,"getUrl","setUrl");
            prop_url.setDisplayName(resources.getString("ImageHyperlink_url_DisplayName"));
            prop_url.setShortDescription(resources.getString("ImageHyperlink_url_Description"));
            prop_url.setPropertyEditorClass(loadClass("org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.SunWebUrlPropertyEditor"));
            prop_url.setExpert(false);
            prop_url.setHidden(false);
            prop_url.setPreferred(false);
            attrib = new AttributeDescriptor("url",false,null,true);
            prop_url.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_url.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_urlLang = new PropertyDescriptor("urlLang",beanClass,"getUrlLang","setUrlLang");
            prop_urlLang.setDisplayName(resources.getString("ImageHyperlink_urlLang_DisplayName"));
            prop_urlLang.setShortDescription(resources.getString("ImageHyperlink_urlLang_Description"));
            prop_urlLang.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_urlLang.setExpert(false);
            prop_urlLang.setHidden(false);
            prop_urlLang.setPreferred(false);
            attrib = new AttributeDescriptor("urlLang",false,null,true);
            prop_urlLang.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_urlLang.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);
            prop_urlLang.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.LanguagesDomain.class);

            PropertyDescriptor prop_attributes = new PropertyDescriptor("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("ImageHyperlink_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("ImageHyperlink_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptor("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("ImageHyperlink_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("ImageHyperlink_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptor("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("ImageHyperlink_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("ImageHyperlink_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_facets = new PropertyDescriptor("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("ImageHyperlink_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("ImageHyperlink_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptor("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("ImageHyperlink_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("ImageHyperlink_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptor("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("ImageHyperlink_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("ImageHyperlink_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,true);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_immediate = new PropertyDescriptor("immediate",beanClass,"isImmediate","setImmediate");
            prop_immediate.setDisplayName(resources.getString("ImageHyperlink_immediate_DisplayName"));
            prop_immediate.setShortDescription(resources.getString("ImageHyperlink_immediate_Description"));
            prop_immediate.setExpert(false);
            prop_immediate.setHidden(false);
            prop_immediate.setPreferred(false);
            attrib = new AttributeDescriptor("immediate",false,null,true);
            prop_immediate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_immediate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_parent = new PropertyDescriptor("parent",beanClass,"getParent",null);
            prop_parent.setDisplayName(resources.getString("ImageHyperlink_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("ImageHyperlink_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("ImageHyperlink_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("ImageHyperlink_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptor("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("ImageHyperlink_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("ImageHyperlink_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptor("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("ImageHyperlink_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("ImageHyperlink_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            propDescriptors = new PropertyDescriptor[] {
                prop_action,
                prop_actionListener,
                prop_align,
                prop_alt,
                prop_attributes,
                prop_border,
                prop_childCount,
                prop_children,
                prop_disabled,
                prop_facets,
                prop_family,
                prop_height,
                prop_hspace,
                prop_icon,
                prop_id,
                prop_imageURL,
                prop_immediate,
                prop_onBlur,
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
                prop_parent,
                prop_rendered,
                prop_rendererType,
                prop_rendersChildren,
                prop_shape,
                prop_style,
                prop_styleClass,
                prop_tabIndex,
                prop_target,
                prop_text,
                prop_textPosition,
                prop_toolTip,
                prop_type,
                prop_url,
                prop_urlLang,
                prop_value,
                prop_visible,
                prop_vspace,
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
