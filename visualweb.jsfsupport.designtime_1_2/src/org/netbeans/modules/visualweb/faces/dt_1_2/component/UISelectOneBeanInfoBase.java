/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.visualweb.faces.dt_1_2.component;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.faces.FacetDescriptor;
import com.sun.rave.designtime.markup.AttributeDescriptor;

import org.netbeans.modules.visualweb.faces.dt.HtmlBeanInfoBase;
import org.netbeans.modules.visualweb.faces.dt.BeanDescriptorBase;
import org.netbeans.modules.visualweb.faces.dt.PropertyDescriptorBase;

/**
 * <p>Auto-generated design time metadata class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

abstract class UISelectOneBeanInfoBase extends HtmlBeanInfoBase {

    protected static ResourceBundle resources = ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt_1_2.component.Bundle-JSF", Locale.getDefault(), UISelectOneBeanInfoBase.class.getClassLoader());

    /**
     * <p>Construct a new <code>UISelectOneBeanInfoBase</code>.</p>
     */
    public UISelectOneBeanInfoBase() {

        beanClass = javax.faces.component.UISelectOne.class;
        iconFileName_C16 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/UISelectOne_C16";
        iconFileName_C32 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/UISelectOne_C32";
        iconFileName_M16 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/UISelectOne_M16";
        iconFileName_M32 = "/org/netbeans/modules/visualweb/faces/dt_1_2/component/UISelectOne_M32";

    }

    private BeanDescriptor beanDescriptor;

    /**
     * <p>Return the <code>BeanDescriptor</code> for this bean.</p>
     */
    public BeanDescriptor getBeanDescriptor() {

        if (beanDescriptor != null) {
            return beanDescriptor;
        }

        beanDescriptor = new BeanDescriptorBase(beanClass);
        beanDescriptor.setDisplayName(resources.getString("UISelectOne_DisplayName"));
        beanDescriptor.setShortDescription(resources.getString("UISelectOne_Description"));
        beanDescriptor.setExpert(false);
        beanDescriptor.setHidden(false);
        beanDescriptor.setPreferred(false);
        beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS,getFacetDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY,"projrave_ui_elements_palette_jsfstd_dropdown_list");
        beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME,"dropdown");
        beanDescriptor.setValue(Constants.BeanDescriptor.IS_CONTAINER,Boolean.TRUE);
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY,"projrave_ui_elements_propsheets_jsfstd_dropdown_list_props");
        beanDescriptor.setValue(Constants.BeanDescriptor.PROPERTY_CATEGORIES,getCategoryDescriptors());
        beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME,"selectOneMenu");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_PREFIX,"h");
        beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI,"http://java.sun.com/jsf/html");

        return beanDescriptor;

    }

    /**
     * <p>Return the <code>CategoryDescriptor</code> array for the property categories of this component.</p>
     */
    private CategoryDescriptor[] getCategoryDescriptors() {

        return com.sun.rave.designtime.base.CategoryDescriptors.getDefaultCategoryDescriptors();

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
     * <p>Return a class loaded by name via the class loader that loaded this class.</p>
     */

    private PropertyDescriptor[] propertyDescriptors;

    /**
     * <p>Return the <code>PropertyDescriptor</code>s for this bean.</p>
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors != null) {
            return propertyDescriptors;
        }
        AttributeDescriptor attrib = null;

        try {

            PropertyDescriptor prop_attributes = new PropertyDescriptorBase("attributes",beanClass,"getAttributes",null);
            prop_attributes.setDisplayName(resources.getString("UISelectOne_attributes_DisplayName"));
            prop_attributes.setShortDescription(resources.getString("UISelectOne_attributes_Description"));
            prop_attributes.setExpert(false);
            prop_attributes.setHidden(true);
            prop_attributes.setPreferred(false);
            prop_attributes.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_childCount = new PropertyDescriptorBase("childCount",beanClass,"getChildCount",null);
            prop_childCount.setDisplayName(resources.getString("UISelectOne_childCount_DisplayName"));
            prop_childCount.setShortDescription(resources.getString("UISelectOne_childCount_Description"));
            prop_childCount.setExpert(false);
            prop_childCount.setHidden(true);
            prop_childCount.setPreferred(false);
            prop_childCount.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_children = new PropertyDescriptorBase("children",beanClass,"getChildren",null);
            prop_children.setDisplayName(resources.getString("UISelectOne_children_DisplayName"));
            prop_children.setShortDescription(resources.getString("UISelectOne_children_Description"));
            prop_children.setExpert(false);
            prop_children.setHidden(true);
            prop_children.setPreferred(false);
            prop_children.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_converter = new PropertyDescriptorBase("converter",beanClass,"getConverter","setConverter");
            prop_converter.setDisplayName(resources.getString("UISelectOne_converter_DisplayName"));
            prop_converter.setShortDescription(resources.getString("UISelectOne_converter_Description"));
            prop_converter.setPropertyEditorClass(com.sun.rave.propertyeditors.ConverterPropertyEditor.class);
            prop_converter.setExpert(false);
            prop_converter.setHidden(false);
            prop_converter.setPreferred(false);
            attrib = new AttributeDescriptor("converter",false,null,true);
            prop_converter.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_converter.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_disabledClass = new PropertyDescriptorBase("disabledClass",beanClass,"getDisabledClass","setDisabledClass");
            prop_disabledClass.setDisplayName(resources.getString("UISelectOne_disabledClass_DisplayName"));
            prop_disabledClass.setShortDescription(resources.getString("UISelectOne_disabledClass_Description"));
            prop_disabledClass.setExpert(false);
            prop_disabledClass.setHidden(false);
            prop_disabledClass.setPreferred(false);
            attrib = new AttributeDescriptor("disabledClass",false,null,true);
            prop_disabledClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_disabledClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_enabledClass = new PropertyDescriptorBase("enabledClass",beanClass,"getEnabledClass","setEnabledClass");
            prop_enabledClass.setDisplayName(resources.getString("UISelectOne_enabledClass_DisplayName"));
            prop_enabledClass.setShortDescription(resources.getString("UISelectOne_enabledClass_Description"));
            prop_enabledClass.setExpert(false);
            prop_enabledClass.setHidden(false);
            prop_enabledClass.setPreferred(false);
            attrib = new AttributeDescriptor("enabledClass",false,null,true);
            prop_enabledClass.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_enabledClass.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.APPEARANCE);

            PropertyDescriptor prop_facets = new PropertyDescriptorBase("facets",beanClass,"getFacets",null);
            prop_facets.setDisplayName(resources.getString("UISelectOne_facets_DisplayName"));
            prop_facets.setShortDescription(resources.getString("UISelectOne_facets_Description"));
            prop_facets.setExpert(false);
            prop_facets.setHidden(true);
            prop_facets.setPreferred(false);
            prop_facets.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_family = new PropertyDescriptorBase("family",beanClass,"getFamily",null);
            prop_family.setDisplayName(resources.getString("UISelectOne_family_DisplayName"));
            prop_family.setShortDescription(resources.getString("UISelectOne_family_Description"));
            prop_family.setExpert(false);
            prop_family.setHidden(true);
            prop_family.setPreferred(false);
            prop_family.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_id = new PropertyDescriptorBase("id",beanClass,"getId","setId");
            prop_id.setDisplayName(resources.getString("UISelectOne_id_DisplayName"));
            prop_id.setShortDescription(resources.getString("UISelectOne_id_Description"));
            prop_id.setExpert(false);
            prop_id.setHidden(true);
            prop_id.setPreferred(false);
            attrib = new AttributeDescriptor("id",false,null,true);
            prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_id.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.GENERAL);

            PropertyDescriptor prop_immediate = new PropertyDescriptorBase("immediate",beanClass,"isImmediate","setImmediate");
            prop_immediate.setDisplayName(resources.getString("UISelectOne_immediate_DisplayName"));
            prop_immediate.setShortDescription(resources.getString("UISelectOne_immediate_Description"));
            prop_immediate.setExpert(false);
            prop_immediate.setHidden(false);
            prop_immediate.setPreferred(false);
            attrib = new AttributeDescriptor("immediate",false,null,true);
            prop_immediate.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_immediate.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_localValue = new PropertyDescriptorBase("localValue",beanClass,"getLocalValue",null);
            prop_localValue.setDisplayName(resources.getString("UISelectOne_localValue_DisplayName"));
            prop_localValue.setShortDescription(resources.getString("UISelectOne_localValue_Description"));
            prop_localValue.setExpert(false);
            prop_localValue.setHidden(true);
            prop_localValue.setPreferred(false);
            prop_localValue.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_localValueSet = new PropertyDescriptorBase("localValueSet",beanClass,"isLocalValueSet","setLocalValueSet");
            prop_localValueSet.setDisplayName(resources.getString("UISelectOne_localValueSet_DisplayName"));
            prop_localValueSet.setShortDescription(resources.getString("UISelectOne_localValueSet_Description"));
            prop_localValueSet.setExpert(false);
            prop_localValueSet.setHidden(true);
            prop_localValueSet.setPreferred(false);
            prop_localValueSet.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_parent = new PropertyDescriptorBase("parent",beanClass,"getParent",null);
            prop_parent.setDisplayName(resources.getString("UISelectOne_parent_DisplayName"));
            prop_parent.setShortDescription(resources.getString("UISelectOne_parent_Description"));
            prop_parent.setExpert(false);
            prop_parent.setHidden(true);
            prop_parent.setPreferred(false);
            prop_parent.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendered = new PropertyDescriptorBase("rendered",beanClass,"isRendered","setRendered");
            prop_rendered.setDisplayName(resources.getString("UISelectOne_rendered_DisplayName"));
            prop_rendered.setShortDescription(resources.getString("UISelectOne_rendered_Description"));
            prop_rendered.setExpert(false);
            prop_rendered.setHidden(false);
            prop_rendered.setPreferred(false);
            attrib = new AttributeDescriptor("rendered",false,null,true);
            prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_rendered.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            PropertyDescriptor prop_rendererType = new PropertyDescriptorBase("rendererType",beanClass,"getRendererType","setRendererType");
            prop_rendererType.setDisplayName(resources.getString("UISelectOne_rendererType_DisplayName"));
            prop_rendererType.setShortDescription(resources.getString("UISelectOne_rendererType_Description"));
            prop_rendererType.setExpert(false);
            prop_rendererType.setHidden(true);
            prop_rendererType.setPreferred(false);
            prop_rendererType.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_rendersChildren = new PropertyDescriptorBase("rendersChildren",beanClass,"getRendersChildren",null);
            prop_rendersChildren.setDisplayName(resources.getString("UISelectOne_rendersChildren_DisplayName"));
            prop_rendersChildren.setShortDescription(resources.getString("UISelectOne_rendersChildren_Description"));
            prop_rendersChildren.setExpert(false);
            prop_rendersChildren.setHidden(true);
            prop_rendersChildren.setPreferred(false);
            prop_rendersChildren.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_required = new PropertyDescriptorBase("required",beanClass,"isRequired","setRequired");
            prop_required.setDisplayName(resources.getString("UISelectOne_required_DisplayName"));
            prop_required.setShortDescription(resources.getString("UISelectOne_required_Description"));
            prop_required.setExpert(false);
            prop_required.setHidden(false);
            prop_required.setPreferred(false);
            attrib = new AttributeDescriptor("required",false,null,true);
            prop_required.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_required.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_submittedValue = new PropertyDescriptorBase("submittedValue",beanClass,"getSubmittedValue","setSubmittedValue");
            prop_submittedValue.setDisplayName(resources.getString("UISelectOne_submittedValue_DisplayName"));
            prop_submittedValue.setShortDescription(resources.getString("UISelectOne_submittedValue_Description"));
            prop_submittedValue.setExpert(false);
            prop_submittedValue.setHidden(true);
            prop_submittedValue.setPreferred(false);
            prop_submittedValue.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.INTERNAL);

            PropertyDescriptor prop_validator = new PropertyDescriptorBase("validator",beanClass,"getValidator","setValidator");
            prop_validator.setDisplayName(resources.getString("UISelectOne_validator_DisplayName"));
            prop_validator.setShortDescription(resources.getString("UISelectOne_validator_Description"));
            prop_validator.setPropertyEditorClass(com.sun.rave.propertyeditors.ValidatorPropertyEditor.class);
            prop_validator.setExpert(false);
            prop_validator.setHidden(false);
            prop_validator.setPreferred(false);
            attrib = new AttributeDescriptor("validator",false,null,true);
            prop_validator.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_validator.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);

            PropertyDescriptor prop_value = new PropertyDescriptorBase("value",beanClass,"getValue","setValue");
            prop_value.setDisplayName(resources.getString("UISelectOne_value_DisplayName"));
            prop_value.setShortDescription(resources.getString("UISelectOne_value_Description"));
            prop_value.setPropertyEditorClass(org.netbeans.modules.visualweb.faces.dt.std.ValueBindingPropertyEditor.class);
            prop_value.setExpert(false);
            prop_value.setHidden(false);
            prop_value.setPreferred(false);
            attrib = new AttributeDescriptor("value",false,null,true);
            prop_value.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_value.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);
            prop_value.setValue("ignoreIsBound", "true");

            PropertyDescriptor prop_valueChangeListener = new PropertyDescriptorBase("valueChangeListener",beanClass,"getValueChangeListener","setValueChangeListener");
            prop_valueChangeListener.setDisplayName(resources.getString("UISelectOne_valueChangeListener_DisplayName"));
            prop_valueChangeListener.setShortDescription(resources.getString("UISelectOne_valueChangeListener_Description"));
            prop_valueChangeListener.setPropertyEditorClass(com.sun.rave.propertyeditors.MethodBindingPropertyEditor.class);
            prop_valueChangeListener.setExpert(false);
            prop_valueChangeListener.setHidden(false);
            prop_valueChangeListener.setPreferred(false);
            attrib = new AttributeDescriptor("valueChangeListener",false,null,true);
            prop_valueChangeListener.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR,attrib);
            prop_valueChangeListener.setValue(Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.ADVANCED);

            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            propertyDescriptorList.add(prop_attributes);
            propertyDescriptorList.add(prop_childCount);
            propertyDescriptorList.add(prop_children);
            propertyDescriptorList.add(prop_converter);
            propertyDescriptorList.add(prop_disabledClass);
            propertyDescriptorList.add(prop_enabledClass);
            propertyDescriptorList.add(prop_facets);
            propertyDescriptorList.add(prop_family);
            propertyDescriptorList.add(prop_id);
            propertyDescriptorList.add(prop_immediate);
            propertyDescriptorList.add(prop_localValue);
            propertyDescriptorList.add(prop_localValueSet);
            propertyDescriptorList.add(prop_parent);
            propertyDescriptorList.add(prop_rendered);
            propertyDescriptorList.add(prop_rendererType);
            propertyDescriptorList.add(prop_rendersChildren);
            propertyDescriptorList.add(prop_required);
            propertyDescriptorList.add(prop_submittedValue);
            propertyDescriptorList.add(prop_validator);
            propertyDescriptorList.add(prop_value);
            propertyDescriptorList.add(prop_valueChangeListener);

            propertyDescriptorList.addAll(Arrays.asList(super.getPropertyDescriptors()));
            propertyDescriptors = propertyDescriptorList.toArray(new PropertyDescriptor[propertyDescriptorList.size()]);
            return propertyDescriptors;

        } catch (IntrospectionException e) {
            e.printStackTrace();
            return null;
        }

    }

}

