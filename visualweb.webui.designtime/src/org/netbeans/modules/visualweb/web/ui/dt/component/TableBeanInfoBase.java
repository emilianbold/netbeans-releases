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

abstract class TableBeanInfoBase extends SimpleBeanInfo {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.web.ui.dt.component.Bundle-JSF", Locale.getDefault(), TableBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>TableBeanInfoBase</code>.</p>
     */
    public TableBeanInfoBase() {

        beanClass = com.sun.rave.web.ui.component.Table.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Table_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Table_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Table_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/web/ui/dt/component/Table_M32";

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
        beanDescriptor.setDisplayName(resources.getString("Table_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("Table_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_bh_table");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"table");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_palette_bh_propsheets_bh_table_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"table");
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
            prop_align.setDisplayName(resources.getString("Table_align_DisplayName"));
            prop_align.setShortDescription(resources.getString("Table_align_Description"));
            prop_align.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.SelectOneDomainEditor"));
            prop_align.setExpert(false);
            prop_align.setHidden(true);
            prop_align.setPreferred(false);
            prop_align.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);
            prop_align.setValue("com.sun.rave.propertyeditors.DOMAIN_CLASS", com.sun.rave.propertyeditors.domains.StringTokensDomain.class);
            prop_align.setValue("com.sun.rave.propertyeditors.domains.TOKEN_STRING", "text-bottom,top,baseline,sub,middle,bottom,text-top,super");

            PropertyDescriptor prop_augmentTitle = new PropertyDescriptor("augmentTitle",beanClass,"isAugmentTitle","setAugmentTitle");
            prop_augmentTitle.setDisplayName(resources.getString("Table_augmentTitle_DisplayName"));
            prop_augmentTitle.setShortDescription(resources.getString("Table_augmentTitle_Description"));
            prop_augmentTitle.setExpert(false);
            prop_augmentTitle.setHidden(false);
            prop_augmentTitle.setPreferred(false);
            attrib = new AttributeDescriptor("augmentTitle",false,"true",true);
            prop_augmentTitle.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_augmentTitle.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_bgColor = new PropertyDescriptor("bgColor",beanClass,"getBgColor","setBgColor");
            prop_bgColor.setDisplayName(resources.getString("Table_bgColor_DisplayName"));
            prop_bgColor.setShortDescription(resources.getString("Table_bgColor_Description"));
            prop_bgColor.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_bgColor.setExpert(false);
            prop_bgColor.setHidden(true);
            prop_bgColor.setPreferred(false);
            prop_bgColor.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_border = new PropertyDescriptor("border",beanClass,"getBorder","setBorder");
            prop_border.setDisplayName(resources.getString("Table_border_DisplayName"));
            prop_border.setShortDescription(resources.getString("Table_border_Description"));
            prop_border.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.LengthPropertyEditor"));
            prop_border.setExpert(false);
            prop_border.setHidden(true);
            prop_border.setPreferred(false);
            prop_border.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_cellPadding = new PropertyDescriptor("cellPadding",beanClass,"getCellPadding","setCellPadding");
            prop_cellPadding.setDisplayName(resources.getString("Table_cellPadding_DisplayName"));
            prop_cellPadding.setShortDescription(resources.getString("Table_cellPadding_Description"));
            prop_cellPadding.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_cellPadding.setExpert(false);
            prop_cellPadding.setHidden(false);
            prop_cellPadding.setPreferred(false);
            attrib = new AttributeDescriptor("cellPadding",false,null,true);
            prop_cellPadding.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_cellPadding.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_cellSpacing = new PropertyDescriptor("cellSpacing",beanClass,"getCellSpacing","setCellSpacing");
            prop_cellSpacing.setDisplayName(resources.getString("Table_cellSpacing_DisplayName"));
            prop_cellSpacing.setShortDescription(resources.getString("Table_cellSpacing_Description"));
            prop_cellSpacing.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_cellSpacing.setExpert(false);
            prop_cellSpacing.setHidden(false);
            prop_cellSpacing.setPreferred(false);
            attrib = new AttributeDescriptor("cellSpacing",false,null,true);
            prop_cellSpacing.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_cellSpacing.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_clearSortButton = new PropertyDescriptor("clearSortButton",beanClass,"isClearSortButton","setClearSortButton");
            prop_clearSortButton.setDisplayName(resources.getString("Table_clearSortButton_DisplayName"));
            prop_clearSortButton.setShortDescription(resources.getString("Table_clearSortButton_Description"));
            prop_clearSortButton.setExpert(false);
            prop_clearSortButton.setHidden(false);
            prop_clearSortButton.setPreferred(false);
            attrib = new AttributeDescriptor("clearSortButton",false,null,true);
            prop_clearSortButton.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_clearSortButton.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_deselectMultipleButton = new PropertyDescriptor("deselectMultipleButton",beanClass,"isDeselectMultipleButton","setDeselectMultipleButton");
            prop_deselectMultipleButton.setDisplayName(resources.getString("Table_deselectMultipleButton_DisplayName"));
            prop_deselectMultipleButton.setShortDescription(resources.getString("Table_deselectMultipleButton_Description"));
            prop_deselectMultipleButton.setExpert(false);
            prop_deselectMultipleButton.setHidden(false);
            prop_deselectMultipleButton.setPreferred(false);
            attrib = new AttributeDescriptor("deselectMultipleButton",false,null,true);
            prop_deselectMultipleButton.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_deselectMultipleButton.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_deselectMultipleButtonOnClick = new PropertyDescriptor("deselectMultipleButtonOnClick",beanClass,"getDeselectMultipleButtonOnClick","setDeselectMultipleButtonOnClick");
            prop_deselectMultipleButtonOnClick.setDisplayName(resources.getString("Table_deselectMultipleButtonOnClick_DisplayName"));
            prop_deselectMultipleButtonOnClick.setShortDescription(resources.getString("Table_deselectMultipleButtonOnClick_Description"));
            prop_deselectMultipleButtonOnClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_deselectMultipleButtonOnClick.setExpert(false);
            prop_deselectMultipleButtonOnClick.setHidden(false);
            prop_deselectMultipleButtonOnClick.setPreferred(false);
            attrib = new AttributeDescriptor("deselectMultipleButtonOnClick",false,null,true);
            prop_deselectMultipleButtonOnClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_deselectMultipleButtonOnClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_deselectSingleButton = new PropertyDescriptor("deselectSingleButton",beanClass,"isDeselectSingleButton","setDeselectSingleButton");
            prop_deselectSingleButton.setDisplayName(resources.getString("Table_deselectSingleButton_DisplayName"));
            prop_deselectSingleButton.setShortDescription(resources.getString("Table_deselectSingleButton_Description"));
            prop_deselectSingleButton.setExpert(false);
            prop_deselectSingleButton.setHidden(false);
            prop_deselectSingleButton.setPreferred(false);
            attrib = new AttributeDescriptor("deselectSingleButton",false,null,true);
            prop_deselectSingleButton.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_deselectSingleButton.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_deselectSingleButtonOnClick = new PropertyDescriptor("deselectSingleButtonOnClick",beanClass,"getDeselectSingleButtonOnClick","setDeselectSingleButtonOnClick");
            prop_deselectSingleButtonOnClick.setDisplayName(resources.getString("Table_deselectSingleButtonOnClick_DisplayName"));
            prop_deselectSingleButtonOnClick.setShortDescription(resources.getString("Table_deselectSingleButtonOnClick_Description"));
            prop_deselectSingleButtonOnClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_deselectSingleButtonOnClick.setExpert(false);
            prop_deselectSingleButtonOnClick.setHidden(false);
            prop_deselectSingleButtonOnClick.setPreferred(false);
            attrib = new AttributeDescriptor("deselectSingleButtonOnClick",false,null,true);
            prop_deselectSingleButtonOnClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_deselectSingleButtonOnClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_extraActionBottomHtml = new PropertyDescriptor("extraActionBottomHtml",beanClass,"getExtraActionBottomHtml","setExtraActionBottomHtml");
            prop_extraActionBottomHtml.setDisplayName(resources.getString("Table_extraActionBottomHtml_DisplayName"));
            prop_extraActionBottomHtml.setShortDescription(resources.getString("Table_extraActionBottomHtml_Description"));
            prop_extraActionBottomHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraActionBottomHtml.setExpert(false);
            prop_extraActionBottomHtml.setHidden(false);
            prop_extraActionBottomHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraActionBottomHtml",false,null,true);
            prop_extraActionBottomHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraActionBottomHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_extraActionTopHtml = new PropertyDescriptor("extraActionTopHtml",beanClass,"getExtraActionTopHtml","setExtraActionTopHtml");
            prop_extraActionTopHtml.setDisplayName(resources.getString("Table_extraActionTopHtml_DisplayName"));
            prop_extraActionTopHtml.setShortDescription(resources.getString("Table_extraActionTopHtml_Description"));
            prop_extraActionTopHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraActionTopHtml.setExpert(false);
            prop_extraActionTopHtml.setHidden(false);
            prop_extraActionTopHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraActionTopHtml",false,null,true);
            prop_extraActionTopHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraActionTopHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_extraFooterHtml = new PropertyDescriptor("extraFooterHtml",beanClass,"getExtraFooterHtml","setExtraFooterHtml");
            prop_extraFooterHtml.setDisplayName(resources.getString("Table_extraFooterHtml_DisplayName"));
            prop_extraFooterHtml.setShortDescription(resources.getString("Table_extraFooterHtml_Description"));
            prop_extraFooterHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraFooterHtml.setExpert(false);
            prop_extraFooterHtml.setHidden(false);
            prop_extraFooterHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraFooterHtml",false,null,true);
            prop_extraFooterHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraFooterHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_extraPanelHtml = new PropertyDescriptor("extraPanelHtml",beanClass,"getExtraPanelHtml","setExtraPanelHtml");
            prop_extraPanelHtml.setDisplayName(resources.getString("Table_extraPanelHtml_DisplayName"));
            prop_extraPanelHtml.setShortDescription(resources.getString("Table_extraPanelHtml_Description"));
            prop_extraPanelHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraPanelHtml.setExpert(false);
            prop_extraPanelHtml.setHidden(false);
            prop_extraPanelHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraPanelHtml",false,null,true);
            prop_extraPanelHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraPanelHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_extraTitleHtml = new PropertyDescriptor("extraTitleHtml",beanClass,"getExtraTitleHtml","setExtraTitleHtml");
            prop_extraTitleHtml.setDisplayName(resources.getString("Table_extraTitleHtml_DisplayName"));
            prop_extraTitleHtml.setShortDescription(resources.getString("Table_extraTitleHtml_Description"));
            prop_extraTitleHtml.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_extraTitleHtml.setExpert(false);
            prop_extraTitleHtml.setHidden(false);
            prop_extraTitleHtml.setPreferred(false);
            attrib = new AttributeDescriptor("extraTitleHtml",false,null,true);
            prop_extraTitleHtml.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_extraTitleHtml.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_filterId = new PropertyDescriptor("filterId",beanClass,"getFilterId","setFilterId");
            prop_filterId.setDisplayName(resources.getString("Table_filterId_DisplayName"));
            prop_filterId.setShortDescription(resources.getString("Table_filterId_Description"));
            prop_filterId.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_filterId.setExpert(false);
            prop_filterId.setHidden(true);
            prop_filterId.setPreferred(false);
            attrib = new AttributeDescriptor("filterId",false,null,true);
            prop_filterId.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_filterId.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_filterPanelFocusId = new PropertyDescriptor("filterPanelFocusId",beanClass,"getFilterPanelFocusId","setFilterPanelFocusId");
            prop_filterPanelFocusId.setDisplayName(resources.getString("Table_filterPanelFocusId_DisplayName"));
            prop_filterPanelFocusId.setShortDescription(resources.getString("Table_filterPanelFocusId_Description"));
            prop_filterPanelFocusId.setExpert(false);
            prop_filterPanelFocusId.setHidden(true);
            prop_filterPanelFocusId.setPreferred(false);
            attrib = new AttributeDescriptor("filterPanelFocusId",false,null,true);
            prop_filterPanelFocusId.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_filterPanelFocusId.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_filterText = new PropertyDescriptor("filterText",beanClass,"getFilterText","setFilterText");
            prop_filterText.setDisplayName(resources.getString("Table_filterText_DisplayName"));
            prop_filterText.setShortDescription(resources.getString("Table_filterText_Description"));
            prop_filterText.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_filterText.setExpert(false);
            prop_filterText.setHidden(true);
            prop_filterText.setPreferred(false);
            attrib = new AttributeDescriptor("filterText",false,null,true);
            prop_filterText.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_filterText.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_footerText = new PropertyDescriptor("footerText",beanClass,"getFooterText","setFooterText");
            prop_footerText.setDisplayName(resources.getString("Table_footerText_DisplayName"));
            prop_footerText.setShortDescription(resources.getString("Table_footerText_Description"));
            prop_footerText.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_footerText.setExpert(false);
            prop_footerText.setHidden(false);
            prop_footerText.setPreferred(false);
            attrib = new AttributeDescriptor("footerText",false,null,true);
            prop_footerText.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_footerText.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_frame = new PropertyDescriptor("frame",beanClass,"getFrame","setFrame");
            prop_frame.setDisplayName(resources.getString("Table_frame_DisplayName"));
            prop_frame.setShortDescription(resources.getString("Table_frame_Description"));
            prop_frame.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_frame.setExpert(false);
            prop_frame.setHidden(true);
            prop_frame.setPreferred(false);
            prop_frame.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_hiddenSelectedRows = new PropertyDescriptor("hiddenSelectedRows",beanClass,"isHiddenSelectedRows","setHiddenSelectedRows");
            prop_hiddenSelectedRows.setDisplayName(resources.getString("Table_hiddenSelectedRows_DisplayName"));
            prop_hiddenSelectedRows.setShortDescription(resources.getString("Table_hiddenSelectedRows_Description"));
            prop_hiddenSelectedRows.setExpert(false);
            prop_hiddenSelectedRows.setHidden(false);
            prop_hiddenSelectedRows.setPreferred(false);
            attrib = new AttributeDescriptor("hiddenSelectedRows",false,null,true);
            prop_hiddenSelectedRows.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_hiddenSelectedRows.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_internalVirtualForm = new PropertyDescriptor("internalVirtualForm",beanClass,"isInternalVirtualForm","setInternalVirtualForm");
            prop_internalVirtualForm.setDisplayName(resources.getString("Table_internalVirtualForm_DisplayName"));
            prop_internalVirtualForm.setShortDescription(resources.getString("Table_internalVirtualForm_Description"));
            prop_internalVirtualForm.setExpert(false);
            prop_internalVirtualForm.setHidden(false);
            prop_internalVirtualForm.setPreferred(false);
            prop_internalVirtualForm.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_itemsText = new PropertyDescriptor("itemsText",beanClass,"getItemsText","setItemsText");
            prop_itemsText.setDisplayName(resources.getString("Table_itemsText_DisplayName"));
            prop_itemsText.setShortDescription(resources.getString("Table_itemsText_Description"));
            prop_itemsText.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_itemsText.setExpert(false);
            prop_itemsText.setHidden(false);
            prop_itemsText.setPreferred(false);
            attrib = new AttributeDescriptor("itemsText",false,null,true);
            prop_itemsText.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_itemsText.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_lite = new PropertyDescriptor("lite",beanClass,"isLite","setLite");
            prop_lite.setDisplayName(resources.getString("Table_lite_DisplayName"));
            prop_lite.setShortDescription(resources.getString("Table_lite_Description"));
            prop_lite.setExpert(false);
            prop_lite.setHidden(false);
            prop_lite.setPreferred(false);
            attrib = new AttributeDescriptor("lite",false,null,true);
            prop_lite.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_lite.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_onClick = new PropertyDescriptor("onClick",beanClass,"getOnClick","setOnClick");
            prop_onClick.setDisplayName(resources.getString("Table_onClick_DisplayName"));
            prop_onClick.setShortDescription(resources.getString("Table_onClick_Description"));
            prop_onClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onClick.setExpert(false);
            prop_onClick.setHidden(false);
            prop_onClick.setPreferred(false);
            attrib = new AttributeDescriptor("onClick",false,null,true);
            prop_onClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onDblClick = new PropertyDescriptor("onDblClick",beanClass,"getOnDblClick","setOnDblClick");
            prop_onDblClick.setDisplayName(resources.getString("Table_onDblClick_DisplayName"));
            prop_onDblClick.setShortDescription(resources.getString("Table_onDblClick_Description"));
            prop_onDblClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onDblClick.setExpert(false);
            prop_onDblClick.setHidden(false);
            prop_onDblClick.setPreferred(false);
            attrib = new AttributeDescriptor("onDblClick",false,null,true);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onDblClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyDown = new PropertyDescriptor("onKeyDown",beanClass,"getOnKeyDown","setOnKeyDown");
            prop_onKeyDown.setDisplayName(resources.getString("Table_onKeyDown_DisplayName"));
            prop_onKeyDown.setShortDescription(resources.getString("Table_onKeyDown_Description"));
            prop_onKeyDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyDown.setExpert(false);
            prop_onKeyDown.setHidden(false);
            prop_onKeyDown.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyDown",false,null,true);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyPress = new PropertyDescriptor("onKeyPress",beanClass,"getOnKeyPress","setOnKeyPress");
            prop_onKeyPress.setDisplayName(resources.getString("Table_onKeyPress_DisplayName"));
            prop_onKeyPress.setShortDescription(resources.getString("Table_onKeyPress_Description"));
            prop_onKeyPress.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyPress.setExpert(false);
            prop_onKeyPress.setHidden(false);
            prop_onKeyPress.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyPress",false,null,true);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyPress.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onKeyUp = new PropertyDescriptor("onKeyUp",beanClass,"getOnKeyUp","setOnKeyUp");
            prop_onKeyUp.setDisplayName(resources.getString("Table_onKeyUp_DisplayName"));
            prop_onKeyUp.setShortDescription(resources.getString("Table_onKeyUp_Description"));
            prop_onKeyUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onKeyUp.setExpert(false);
            prop_onKeyUp.setHidden(false);
            prop_onKeyUp.setPreferred(false);
            attrib = new AttributeDescriptor("onKeyUp",false,null,true);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onKeyUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseDown = new PropertyDescriptor("onMouseDown",beanClass,"getOnMouseDown","setOnMouseDown");
            prop_onMouseDown.setDisplayName(resources.getString("Table_onMouseDown_DisplayName"));
            prop_onMouseDown.setShortDescription(resources.getString("Table_onMouseDown_Description"));
            prop_onMouseDown.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseDown.setExpert(false);
            prop_onMouseDown.setHidden(false);
            prop_onMouseDown.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseDown",false,null,true);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseDown.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseMove = new PropertyDescriptor("onMouseMove",beanClass,"getOnMouseMove","setOnMouseMove");
            prop_onMouseMove.setDisplayName(resources.getString("Table_onMouseMove_DisplayName"));
            prop_onMouseMove.setShortDescription(resources.getString("Table_onMouseMove_Description"));
            prop_onMouseMove.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseMove.setExpert(false);
            prop_onMouseMove.setHidden(false);
            prop_onMouseMove.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseMove",false,null,true);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseMove.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOut = new PropertyDescriptor("onMouseOut",beanClass,"getOnMouseOut","setOnMouseOut");
            prop_onMouseOut.setDisplayName(resources.getString("Table_onMouseOut_DisplayName"));
            prop_onMouseOut.setShortDescription(resources.getString("Table_onMouseOut_Description"));
            prop_onMouseOut.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOut.setExpert(false);
            prop_onMouseOut.setHidden(false);
            prop_onMouseOut.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOut",false,null,true);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOut.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseOver = new PropertyDescriptor("onMouseOver",beanClass,"getOnMouseOver","setOnMouseOver");
            prop_onMouseOver.setDisplayName(resources.getString("Table_onMouseOver_DisplayName"));
            prop_onMouseOver.setShortDescription(resources.getString("Table_onMouseOver_Description"));
            prop_onMouseOver.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseOver.setExpert(false);
            prop_onMouseOver.setHidden(false);
            prop_onMouseOver.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseOver",false,null,true);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseOver.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_onMouseUp = new PropertyDescriptor("onMouseUp",beanClass,"getOnMouseUp","setOnMouseUp");
            prop_onMouseUp.setDisplayName(resources.getString("Table_onMouseUp_DisplayName"));
            prop_onMouseUp.setShortDescription(resources.getString("Table_onMouseUp_Description"));
            prop_onMouseUp.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_onMouseUp.setExpert(false);
            prop_onMouseUp.setHidden(false);
            prop_onMouseUp.setPreferred(false);
            attrib = new AttributeDescriptor("onMouseUp",false,null,true);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_onMouseUp.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_paginateButton = new PropertyDescriptor("paginateButton",beanClass,"isPaginateButton","setPaginateButton");
            prop_paginateButton.setDisplayName(resources.getString("Table_paginateButton_DisplayName"));
            prop_paginateButton.setShortDescription(resources.getString("Table_paginateButton_Description"));
            prop_paginateButton.setExpert(false);
            prop_paginateButton.setHidden(false);
            prop_paginateButton.setPreferred(false);
            attrib = new AttributeDescriptor("paginateButton",false,null,true);
            prop_paginateButton.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_paginateButton.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_paginationControls = new PropertyDescriptor("paginationControls",beanClass,"isPaginationControls","setPaginationControls");
            prop_paginationControls.setDisplayName(resources.getString("Table_paginationControls_DisplayName"));
            prop_paginationControls.setShortDescription(resources.getString("Table_paginationControls_Description"));
            prop_paginationControls.setExpert(false);
            prop_paginationControls.setHidden(false);
            prop_paginationControls.setPreferred(false);
            attrib = new AttributeDescriptor("paginationControls",false,null,true);
            prop_paginationControls.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_paginationControls.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_preferencesPanelFocusId = new PropertyDescriptor("preferencesPanelFocusId",beanClass,"getPreferencesPanelFocusId","setPreferencesPanelFocusId");
            prop_preferencesPanelFocusId.setDisplayName(resources.getString("Table_preferencesPanelFocusId_DisplayName"));
            prop_preferencesPanelFocusId.setShortDescription(resources.getString("Table_preferencesPanelFocusId_Description"));
            prop_preferencesPanelFocusId.setExpert(false);
            prop_preferencesPanelFocusId.setHidden(true);
            prop_preferencesPanelFocusId.setPreferred(false);
            attrib = new AttributeDescriptor("preferencesPanelFocusId",false,null,true);
            prop_preferencesPanelFocusId.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_preferencesPanelFocusId.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rules = new PropertyDescriptor("rules",beanClass,"getRules","setRules");
            prop_rules.setDisplayName(resources.getString("Table_rules_DisplayName"));
            prop_rules.setShortDescription(resources.getString("Table_rules_Description"));
            prop_rules.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_rules.setExpert(false);
            prop_rules.setHidden(true);
            prop_rules.setPreferred(false);
            prop_rules.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_selectMultipleButton = new PropertyDescriptor("selectMultipleButton",beanClass,"isSelectMultipleButton","setSelectMultipleButton");
            prop_selectMultipleButton.setDisplayName(resources.getString("Table_selectMultipleButton_DisplayName"));
            prop_selectMultipleButton.setShortDescription(resources.getString("Table_selectMultipleButton_Description"));
            prop_selectMultipleButton.setExpert(false);
            prop_selectMultipleButton.setHidden(false);
            prop_selectMultipleButton.setPreferred(false);
            attrib = new AttributeDescriptor("selectMultipleButton",false,null,true);
            prop_selectMultipleButton.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_selectMultipleButton.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_selectMultipleButtonOnClick = new PropertyDescriptor("selectMultipleButtonOnClick",beanClass,"getSelectMultipleButtonOnClick","setSelectMultipleButtonOnClick");
            prop_selectMultipleButtonOnClick.setDisplayName(resources.getString("Table_selectMultipleButtonOnClick_DisplayName"));
            prop_selectMultipleButtonOnClick.setShortDescription(resources.getString("Table_selectMultipleButtonOnClick_Description"));
            prop_selectMultipleButtonOnClick.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.JavaScriptPropertyEditor"));
            prop_selectMultipleButtonOnClick.setExpert(false);
            prop_selectMultipleButtonOnClick.setHidden(false);
            prop_selectMultipleButtonOnClick.setPreferred(false);
            attrib = new AttributeDescriptor("selectMultipleButtonOnClick",false,null,true);
            prop_selectMultipleButtonOnClick.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_selectMultipleButtonOnClick.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.JAVASCRIPT);

            PropertyDescriptor prop_sortPanelFocusId = new PropertyDescriptor("sortPanelFocusId",beanClass,"getSortPanelFocusId","setSortPanelFocusId");
            prop_sortPanelFocusId.setDisplayName(resources.getString("Table_sortPanelFocusId_DisplayName"));
            prop_sortPanelFocusId.setShortDescription(resources.getString("Table_sortPanelFocusId_Description"));
            prop_sortPanelFocusId.setExpert(false);
            prop_sortPanelFocusId.setHidden(true);
            prop_sortPanelFocusId.setPreferred(false);
            attrib = new AttributeDescriptor("sortPanelFocusId",false,null,true);
            prop_sortPanelFocusId.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_sortPanelFocusId.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_sortPanelToggleButton = new PropertyDescriptor("sortPanelToggleButton",beanClass,"isSortPanelToggleButton","setSortPanelToggleButton");
            prop_sortPanelToggleButton.setDisplayName(resources.getString("Table_sortPanelToggleButton_DisplayName"));
            prop_sortPanelToggleButton.setShortDescription(resources.getString("Table_sortPanelToggleButton_Description"));
            prop_sortPanelToggleButton.setExpert(false);
            prop_sortPanelToggleButton.setHidden(false);
            prop_sortPanelToggleButton.setPreferred(false);
            attrib = new AttributeDescriptor("sortPanelToggleButton",false,null,true);
            prop_sortPanelToggleButton.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_sortPanelToggleButton.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_style = new PropertyDescriptor("style",beanClass,"getStyle","setStyle");
            prop_style.setDisplayName(resources.getString("Table_style_DisplayName"));
            prop_style.setShortDescription(resources.getString("Table_style_Description"));
            prop_style.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.css.CssStylePropertyEditor"));
            prop_style.setExpert(false);
            prop_style.setHidden(false);
            prop_style.setPreferred(false);
            attrib = new AttributeDescriptor("style",false,null,true);
            prop_style.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_style.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_styleClass = new PropertyDescriptor("styleClass",beanClass,"getStyleClass","setStyleClass");
            prop_styleClass.setDisplayName(resources.getString("Table_styleClass_DisplayName"));
            prop_styleClass.setShortDescription(resources.getString("Table_styleClass_Description"));
            prop_styleClass.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StyleClassPropertyEditor"));
            prop_styleClass.setExpert(false);
            prop_styleClass.setHidden(false);
            prop_styleClass.setPreferred(false);
            attrib = new AttributeDescriptor("styleClass",false,null,true);
            prop_styleClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_styleClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_summary = new PropertyDescriptor("summary",beanClass,"getSummary","setSummary");
            prop_summary.setDisplayName(resources.getString("Table_summary_DisplayName"));
            prop_summary.setShortDescription(resources.getString("Table_summary_Description"));
            prop_summary.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_summary.setExpert(false);
            prop_summary.setHidden(false);
            prop_summary.setPreferred(false);
            attrib = new AttributeDescriptor("summary",false,null,true);
            prop_summary.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_summary.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_tabIndex = new PropertyDescriptor("tabIndex",beanClass,"getTabIndex","setTabIndex");
            prop_tabIndex.setDisplayName(resources.getString("Table_tabIndex_DisplayName"));
            prop_tabIndex.setShortDescription(resources.getString("Table_tabIndex_Description"));
            prop_tabIndex.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.IntegerPropertyEditor"));
            prop_tabIndex.setExpert(false);
            prop_tabIndex.setHidden(false);
            prop_tabIndex.setPreferred(false);
            attrib = new AttributeDescriptor("tabIndex",false,null,true);
            prop_tabIndex.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_tabIndex.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ACCESSIBILITY);
            prop_tabIndex.setValue("com.sun.rave.propertyeditors.MIN_VALUE", "1");

            PropertyDescriptor prop_title = new PropertyDescriptor("title",beanClass,"getTitle","setTitle");
            prop_title.setDisplayName(resources.getString("Table_title_DisplayName"));
            prop_title.setShortDescription(resources.getString("Table_title_Description"));
            prop_title.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_title.setExpert(false);
            prop_title.setHidden(false);
            prop_title.setPreferred(false);
            attrib = new AttributeDescriptor("title",false,null,true);
            prop_title.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_title.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_toolTip = new PropertyDescriptor("toolTip",beanClass,"getToolTip","setToolTip");
            prop_toolTip.setDisplayName(resources.getString("Table_toolTip_DisplayName"));
            prop_toolTip.setShortDescription(resources.getString("Table_toolTip_Description"));
            prop_toolTip.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_toolTip.setExpert(false);
            prop_toolTip.setHidden(false);
            prop_toolTip.setPreferred(false);
            attrib = new AttributeDescriptor("toolTip",false,null,true);
            prop_toolTip.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_toolTip.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_visible = new PropertyDescriptor("visible",beanClass,"isVisible","setVisible");
            prop_visible.setDisplayName(resources.getString("Table_visible_DisplayName"));
            prop_visible.setShortDescription(resources.getString("Table_visible_Description"));
            prop_visible.setExpert(false);
            prop_visible.setHidden(false);
            prop_visible.setPreferred(false);
            attrib = new AttributeDescriptor("visible",false,"true",true);
            prop_visible.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_visible.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.BEHAVIOR);

            PropertyDescriptor prop_width = new PropertyDescriptor("width",beanClass,"getWidth","setWidth");
            prop_width.setDisplayName(resources.getString("Table_width_DisplayName"));
            prop_width.setShortDescription(resources.getString("Table_width_Description"));
            prop_width.setPropertyEditorClass(loadClass("com.sun.rave.propertyeditors.StringPropertyEditor"));
            prop_width.setExpert(false);
            prop_width.setHidden(false);
            prop_width.setPreferred(false);
            attrib = new AttributeDescriptor("width",false,null,true);
            prop_width.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_width.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_attributes = new PropertyDescriptor("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("Table_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("Table_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptor("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("Table_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("Table_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptor("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("Table_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("Table_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_facets = new PropertyDescriptor("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("Table_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("Table_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptor("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("Table_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("Table_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptor("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("Table_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("Table_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,false);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_parent = new PropertyDescriptor("parent",beanClass,"getParent","setParent");
            prop_parent.setDisplayName(resources.getString("Table_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("Table_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("Table_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("Table_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptor("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("Table_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("Table_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptor("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("Table_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("Table_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            propDescriptors = new PropertyDescriptor[] {
                prop_align,
                prop_attributes,
                prop_augmentTitle,
                prop_bgColor,
                prop_border,
                prop_cellPadding,
                prop_cellSpacing,
                prop_childCount,
                prop_children,
                prop_clearSortButton,
                prop_deselectMultipleButton,
                prop_deselectMultipleButtonOnClick,
                prop_deselectSingleButton,
                prop_deselectSingleButtonOnClick,
                prop_extraActionBottomHtml,
                prop_extraActionTopHtml,
                prop_extraFooterHtml,
                prop_extraPanelHtml,
                prop_extraTitleHtml,
                prop_facets,
                prop_family,
                prop_filterId,
                prop_filterPanelFocusId,
                prop_filterText,
                prop_footerText,
                prop_frame,
                prop_hiddenSelectedRows,
                prop_id,
                prop_internalVirtualForm,
                prop_itemsText,
                prop_lite,
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
                prop_paginateButton,
                prop_paginationControls,
                prop_parent,
                prop_preferencesPanelFocusId,
                prop_rendered,
                prop_rendererType,
                prop_rendersChildren,
                prop_rules,
                prop_selectMultipleButton,
                prop_selectMultipleButtonOnClick,
                prop_sortPanelFocusId,
                prop_sortPanelToggleButton,
                prop_style,
                prop_styleClass,
                prop_summary,
                prop_tabIndex,
                prop_title,
                prop_toolTip,
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
