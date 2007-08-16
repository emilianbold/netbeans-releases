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

abstract class TreeNodeBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-JSF", Locale.getDefault(), TreeNodeBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>TreeNodeBeanInfoBase</code>.</p>
     */
    public TreeNodeBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.component.TreeNode.class;
        defaultPropertyName = "text";
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TreeNode_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TreeNode_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TreeNode_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/TreeNode_M32";

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
        beanDescriptor.setDisplayName(resources.getString("TreeNode_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("TreeNode_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_bh_tree_node");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"treeNode");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_palette_bh_propsheets_bh_tree_node_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"treeNode");
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

            FacetDescriptor facet_content = new FacetDescriptor("content");
            facet_content.setDisplayName(resources.getString("TreeNode_content_DisplayName"));
            facet_content.setShortDescription(resources.getString("TreeNode_content_Description"));

            FacetDescriptor facet_image = new FacetDescriptor("image");
            facet_image.setDisplayName(resources.getString("TreeNode_image_DisplayName"));
            facet_image.setShortDescription(resources.getString("TreeNode_image_Description"));

            facetDescriptors = new FacetDescriptor[] {
                facet_content,
                facet_image,
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

            PropertyDescriptor prop_action = new PropertyDescriptor("action",beanClass,"getAction","setAction");
            prop_action.setDisplayName(resources.getString("TreeNode_action_DisplayName"));
            prop_action.setShortDescription(resources.getString("TreeNode_action_Description"));
            prop_action.setExpert(false);
            prop_action.setHidden(false);
            prop_action.setPreferred(false);
            attrib = new AttributeDescriptor("action",false,null,true);
            prop_action.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_action.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_actionListener = new PropertyDescriptor("actionListener",beanClass,"getActionListener","setActionListener");
            prop_actionListener.setDisplayName(resources.getString("TreeNode_actionListener_DisplayName"));
            prop_actionListener.setShortDescription(resources.getString("TreeNode_actionListener_Description"));
            prop_actionListener.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.MethodBindingPropertyEditor"));
            prop_actionListener.setExpert(false);
            prop_actionListener.setHidden(false);
            prop_actionListener.setPreferred(false);
            attrib = new AttributeDescriptor("actionListener",false,null,true);
            prop_actionListener.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_actionListener.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_expanded = new PropertyDescriptor("expanded",beanClass,"isExpanded","setExpanded");
            prop_expanded.setDisplayName(resources.getString("TreeNode_expanded_DisplayName"));
            prop_expanded.setShortDescription(resources.getString("TreeNode_expanded_Description"));
            prop_expanded.setExpert(false);
            prop_expanded.setHidden(false);
            prop_expanded.setPreferred(false);
            attrib = new AttributeDescriptor("expanded",false,"false",true);
            prop_expanded.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_expanded.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_imageURL = new PropertyDescriptor("imageURL",beanClass,"getImageURL","setImageURL");
            prop_imageURL.setDisplayName(resources.getString("TreeNode_imageURL_DisplayName"));
            prop_imageURL.setShortDescription(resources.getString("TreeNode_imageURL_Description"));
            prop_imageURL.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.ImageUrlPropertyEditor"));
            prop_imageURL.setExpert(false);
            prop_imageURL.setHidden(true);
            prop_imageURL.setPreferred(false);
            attrib = new AttributeDescriptor("imageURL",false,null,true);
            prop_imageURL.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_imageURL.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_style = new PropertyDescriptor("style",beanClass,"getStyle","setStyle");
            prop_style.setDisplayName(resources.getString("TreeNode_style_DisplayName"));
            prop_style.setShortDescription(resources.getString("TreeNode_style_Description"));
            prop_style.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.css.CssStylePropertyEditor"));
            prop_style.setExpert(false);
            prop_style.setHidden(false);
            prop_style.setPreferred(false);
            attrib = new AttributeDescriptor("style",false,null,true);
            prop_style.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_style.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_styleClass = new PropertyDescriptor("styleClass",beanClass,"getStyleClass","setStyleClass");
            prop_styleClass.setDisplayName(resources.getString("TreeNode_styleClass_DisplayName"));
            prop_styleClass.setShortDescription(resources.getString("TreeNode_styleClass_Description"));
            prop_styleClass.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StyleClassPropertyEditor"));
            prop_styleClass.setExpert(false);
            prop_styleClass.setHidden(false);
            prop_styleClass.setPreferred(false);
            attrib = new AttributeDescriptor("styleClass",false,null,true);
            prop_styleClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_styleClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_target = new PropertyDescriptor("target",beanClass,"getTarget","setTarget");
            prop_target.setDisplayName(resources.getString("TreeNode_target_DisplayName"));
            prop_target.setShortDescription(resources.getString("TreeNode_target_Description"));
            prop_target.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_target.setExpert(false);
            prop_target.setHidden(false);
            prop_target.setPreferred(false);
            attrib = new AttributeDescriptor("target",false,null,true);
            prop_target.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_target.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);
            prop_target.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.HtmlFrameTargetsDomain.class);

            PropertyDescriptor prop_text = new PropertyDescriptor("text",beanClass,"getText","setText");
            prop_text.setDisplayName(resources.getString("TreeNode_text_DisplayName"));
            prop_text.setShortDescription(resources.getString("TreeNode_text_Description"));
            prop_text.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_text.setExpert(false);
            prop_text.setHidden(false);
            prop_text.setPreferred(false);
            attrib = new AttributeDescriptor("text",false,null,true);
            prop_text.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_text.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_toolTip = new PropertyDescriptor("toolTip",beanClass,"getToolTip","setToolTip");
            prop_toolTip.setDisplayName(resources.getString("TreeNode_toolTip_DisplayName"));
            prop_toolTip.setShortDescription(resources.getString("TreeNode_toolTip_Description"));
            prop_toolTip.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_toolTip.setExpert(false);
            prop_toolTip.setHidden(false);
            prop_toolTip.setPreferred(false);
            attrib = new AttributeDescriptor("toolTip",false,null,true);
            prop_toolTip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_toolTip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_url = new PropertyDescriptor("url",beanClass,"getUrl","setUrl");
            prop_url.setDisplayName(resources.getString("TreeNode_url_DisplayName"));
            prop_url.setShortDescription(resources.getString("TreeNode_url_Description"));
            prop_url.setPropertyEditorClass(loadClass("org.netbeans.modules.visualweb.web.ui.dt.component.propertyeditors.SunWebUrlPropertyEditor"));
            prop_url.setExpert(false);
            prop_url.setHidden(false);
            prop_url.setPreferred(false);
            attrib = new AttributeDescriptor("url",false,null,true);
            prop_url.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_url.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_visible = new PropertyDescriptor("visible",beanClass,"isVisible","setVisible");
            prop_visible.setDisplayName(resources.getString("TreeNode_visible_DisplayName"));
            prop_visible.setShortDescription(resources.getString("TreeNode_visible_Description"));
            prop_visible.setExpert(false);
            prop_visible.setHidden(false);
            prop_visible.setPreferred(false);
            attrib = new AttributeDescriptor("visible",false,"true",true);
            prop_visible.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_visible.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_attributes = new PropertyDescriptor("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("TreeNode_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("TreeNode_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptor("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("TreeNode_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("TreeNode_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptor("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("TreeNode_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("TreeNode_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_facets = new PropertyDescriptor("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("TreeNode_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("TreeNode_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptor("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("TreeNode_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("TreeNode_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptor("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("TreeNode_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("TreeNode_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,false);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_parent = new PropertyDescriptor("parent",beanClass,"getParent","setParent");
            prop_parent.setDisplayName(resources.getString("TreeNode_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("TreeNode_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("TreeNode_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("TreeNode_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptor("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("TreeNode_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("TreeNode_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptor("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("TreeNode_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("TreeNode_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            propDescriptors = new PropertyDescriptor[] {
                prop_action,
                prop_actionListener,
                prop_attributes,
                prop_childCount,
                prop_children,
                prop_expanded,
                prop_facets,
                prop_family,
                prop_id,
                prop_imageURL,
                prop_parent,
                prop_rendered,
                prop_rendererType,
                prop_rendersChildren,
                prop_style,
                prop_styleClass,
                prop_target,
                prop_text,
                prop_toolTip,
                prop_url,
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
