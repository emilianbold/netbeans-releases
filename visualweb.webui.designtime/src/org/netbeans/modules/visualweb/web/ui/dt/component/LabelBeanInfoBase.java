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

abstract class LabelBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-JSF", Locale.getDefault(), LabelBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>LabelBeanInfoBase</code>.</p>
     */
    public LabelBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.component.Label.class;
        defaultPropertyName = "text";
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Label_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Label_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Label_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Label_M32";

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
        beanDescriptor.setDisplayName(resources.getString("Label_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("Label_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_bh_label");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"label");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_palette_bh_propsheets_bh_label_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"label");
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

            PropertyDescriptor prop_for = new PropertyDescriptor("for",beanClass,"getFor","setFor");
            prop_for.setDisplayName(resources.getString("Label_for_DisplayName"));
            prop_for.setShortDescription(resources.getString("Label_for_Description"));
            prop_for.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_for.setExpert(false);
            prop_for.setHidden(false);
            prop_for.setPreferred(false);
            attrib = new AttributeDescriptor("for",false,null,true);
            prop_for.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_for.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_for.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.InputComponentIdsDomain.class);

            PropertyDescriptor prop_hideIndicators = new PropertyDescriptor("hideIndicators",beanClass,"isHideIndicators","setHideIndicators");
            prop_hideIndicators.setDisplayName(resources.getString("Label_hideIndicators_DisplayName"));
            prop_hideIndicators.setShortDescription(resources.getString("Label_hideIndicators_Description"));
            prop_hideIndicators.setExpert(false);
            prop_hideIndicators.setHidden(false);
            prop_hideIndicators.setPreferred(false);
            attrib = new AttributeDescriptor("hideIndicators",false,null,true);
            prop_hideIndicators.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_hideIndicators.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_labelLevel = new PropertyDescriptor("labelLevel",beanClass,"getLabelLevel","setLabelLevel");
            prop_labelLevel.setDisplayName(resources.getString("Label_labelLevel_DisplayName"));
            prop_labelLevel.setShortDescription(resources.getString("Label_labelLevel_Description"));
            prop_labelLevel.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_labelLevel.setExpert(false);
            prop_labelLevel.setHidden(false);
            prop_labelLevel.setPreferred(false);
            attrib = new AttributeDescriptor("labelLevel",false,"2",true);
            prop_labelLevel.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_labelLevel.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_labelLevel.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.LabelLevelsDomain.class);

            PropertyDescriptor prop_onClick = new PropertyDescriptor("onClick",beanClass,"getOnClick","setOnClick");
            prop_onClick.setDisplayName(resources.getString("Label_onClick_DisplayName"));
            prop_onClick.setShortDescription(resources.getString("Label_onClick_Description"));
            prop_onClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onClick.setExpert(false);
            prop_onClick.setHidden(false);
            prop_onClick.setPreferred(false);
            attrib = new AttributeDescriptor("onClick",false,null,true);
            prop_onClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseDown = new PropertyDescriptor("onMouseDown",beanClass,"getOnMouseDown","setOnMouseDown");
            prop_onMouseDown.setDisplayName(resources.getString("Label_onMouseDown_DisplayName"));
            prop_onMouseDown.setShortDescription(resources.getString("Label_onMouseDown_Description"));
            prop_onMouseDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseDown.setExpert(false);
            prop_onMouseDown.setHidden(false);
            prop_onMouseDown.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseDown",false,null,true);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseMove = new PropertyDescriptor("onMouseMove",beanClass,"getOnMouseMove","setOnMouseMove");
            prop_onMouseMove.setDisplayName(resources.getString("Label_onMouseMove_DisplayName"));
            prop_onMouseMove.setShortDescription(resources.getString("Label_onMouseMove_Description"));
            prop_onMouseMove.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseMove.setExpert(false);
            prop_onMouseMove.setHidden(false);
            prop_onMouseMove.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseMove",false,null,true);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOut = new PropertyDescriptor("onMouseOut",beanClass,"getOnMouseOut","setOnMouseOut");
            prop_onMouseOut.setDisplayName(resources.getString("Label_onMouseOut_DisplayName"));
            prop_onMouseOut.setShortDescription(resources.getString("Label_onMouseOut_Description"));
            prop_onMouseOut.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOut.setExpert(false);
            prop_onMouseOut.setHidden(false);
            prop_onMouseOut.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOut",false,null,true);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOver = new PropertyDescriptor("onMouseOver",beanClass,"getOnMouseOver","setOnMouseOver");
            prop_onMouseOver.setDisplayName(resources.getString("Label_onMouseOver_DisplayName"));
            prop_onMouseOver.setShortDescription(resources.getString("Label_onMouseOver_Description"));
            prop_onMouseOver.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOver.setExpert(false);
            prop_onMouseOver.setHidden(false);
            prop_onMouseOver.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOver",false,null,true);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseUp = new PropertyDescriptor("onMouseUp",beanClass,"getOnMouseUp","setOnMouseUp");
            prop_onMouseUp.setDisplayName(resources.getString("Label_onMouseUp_DisplayName"));
            prop_onMouseUp.setShortDescription(resources.getString("Label_onMouseUp_Description"));
            prop_onMouseUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseUp.setExpert(false);
            prop_onMouseUp.setHidden(false);
            prop_onMouseUp.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseUp",false,null,true);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_requiredIndicator = new PropertyDescriptor("requiredIndicator",beanClass,"isRequiredIndicator","setRequiredIndicator");
            prop_requiredIndicator.setDisplayName(resources.getString("Label_requiredIndicator_DisplayName"));
            prop_requiredIndicator.setShortDescription(resources.getString("Label_requiredIndicator_Description"));
            prop_requiredIndicator.setExpert(false);
            prop_requiredIndicator.setHidden(false);
            prop_requiredIndicator.setPreferred(false);
            attrib = new AttributeDescriptor("requiredIndicator",false,null,true);
            prop_requiredIndicator.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_requiredIndicator.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_style = new PropertyDescriptor("style",beanClass,"getStyle","setStyle");
            prop_style.setDisplayName(resources.getString("Label_style_DisplayName"));
            prop_style.setShortDescription(resources.getString("Label_style_Description"));
            prop_style.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.css.CssStylePropertyEditor"));
            prop_style.setExpert(false);
            prop_style.setHidden(false);
            prop_style.setPreferred(false);
            attrib = new AttributeDescriptor("style",false,null,true);
            prop_style.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_style.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_styleClass = new PropertyDescriptor("styleClass",beanClass,"getStyleClass","setStyleClass");
            prop_styleClass.setDisplayName(resources.getString("Label_styleClass_DisplayName"));
            prop_styleClass.setShortDescription(resources.getString("Label_styleClass_Description"));
            prop_styleClass.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StyleClassPropertyEditor"));
            prop_styleClass.setExpert(false);
            prop_styleClass.setHidden(false);
            prop_styleClass.setPreferred(false);
            attrib = new AttributeDescriptor("styleClass",false,null,true);
            prop_styleClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_styleClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_text = new PropertyDescriptor("text",beanClass,"getText","setText");
            prop_text.setDisplayName(resources.getString("Label_text_DisplayName"));
            prop_text.setShortDescription(resources.getString("Label_text_Description"));
            prop_text.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_text.setExpert(false);
            prop_text.setHidden(false);
            prop_text.setPreferred(false);
            attrib = new AttributeDescriptor("text",false,null,true);
            prop_text.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_text.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_toolTip = new PropertyDescriptor("toolTip",beanClass,"getToolTip","setToolTip");
            prop_toolTip.setDisplayName(resources.getString("Label_toolTip_DisplayName"));
            prop_toolTip.setShortDescription(resources.getString("Label_toolTip_Description"));
            prop_toolTip.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_toolTip.setExpert(false);
            prop_toolTip.setHidden(false);
            prop_toolTip.setPreferred(false);
            attrib = new AttributeDescriptor("toolTip",false,null,true);
            prop_toolTip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_toolTip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_visible = new PropertyDescriptor("visible",beanClass,"isVisible","setVisible");
            prop_visible.setDisplayName(resources.getString("Label_visible_DisplayName"));
            prop_visible.setShortDescription(resources.getString("Label_visible_Description"));
            prop_visible.setExpert(false);
            prop_visible.setHidden(false);
            prop_visible.setPreferred(false);
            attrib = new AttributeDescriptor("visible",false,"true",true);
            prop_visible.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_visible.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_attributes = new PropertyDescriptor("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("Label_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("Label_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptor("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("Label_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("Label_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptor("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("Label_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("Label_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_converter = new PropertyDescriptor("converter",beanClass,"getConverter","setConverter");
            prop_converter.setDisplayName(resources.getString("Label_converter_DisplayName"));
            prop_converter.setShortDescription(resources.getString("Label_converter_Description"));
            prop_converter.setPropertyEditorClass(loadClass("org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.JSF1_1ConverterPropertyEditor"));
            prop_converter.setExpert(false);
            prop_converter.setHidden(false);
            prop_converter.setPreferred(false);
            attrib = new AttributeDescriptor("converter",false,null,true);
            prop_converter.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_converter.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_facets = new PropertyDescriptor("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("Label_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("Label_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptor("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("Label_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("Label_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptor("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("Label_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("Label_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,true);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_localValue = new PropertyDescriptor("localValue",beanClass,"getLocalValue",null);
            prop_localValue.setDisplayName(resources.getString("Label_localValue_DisplayName"));
            prop_localValue.setShortDescription(resources.getString("Label_localValue_Description"));
            prop_localValue.setExpert(false);
            prop_localValue.setHidden(true);
            prop_localValue.setPreferred(false);
            prop_localValue.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_parent = new PropertyDescriptor("parent",beanClass,"getParent",null);
            prop_parent.setDisplayName(resources.getString("Label_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("Label_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("Label_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("Label_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptor("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("Label_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("Label_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptor("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("Label_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("Label_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_value = new PropertyDescriptor("value",beanClass,"getValue","setValue");
            prop_value.setDisplayName(resources.getString("Label_value_DisplayName"));
            prop_value.setShortDescription(resources.getString("Label_value_Description"));
            prop_value.setExpert(false);
            prop_value.setHidden(true);
            prop_value.setPreferred(false);
            prop_value.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            propDescriptors = new PropertyDescriptor[] {
                prop_attributes,
                prop_childCount,
                prop_children,
                prop_converter,
                prop_facets,
                prop_family,
                prop_for,
                prop_hideIndicators,
                prop_id,
                prop_labelLevel,
                prop_localValue,
                prop_onClick,
                prop_onMouseDown,
                prop_onMouseMove,
                prop_onMouseOut,
                prop_onMouseOver,
                prop_onMouseUp,
                prop_parent,
                prop_rendered,
                prop_rendererType,
                prop_rendersChildren,
                prop_requiredIndicator,
                prop_style,
                prop_styleClass,
                prop_text,
                prop_toolTip,
                prop_value,
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
