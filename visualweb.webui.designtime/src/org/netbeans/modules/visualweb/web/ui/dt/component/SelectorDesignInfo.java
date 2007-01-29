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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import com.sun.rave.designtime.faces.ResolveResult;
import com.sun.rave.web.ui.component.Selector;
import com.sun.rave.web.ui.model.DefaultOptionsList;
import com.sun.rave.web.ui.model.OptionsList;
import java.beans.PropertyDescriptor;
import java.util.regex.Pattern;
import javax.faces.el.ValueBinding;

/** DesignInfo class for components that extend the {@link
 * org.netbeans.modules.visualweb.web.ui.dt.component.Selector} component. The following behaviors are
 * implemented:
 * <ul>
 * <li>When a new selector-based component is created, a default list of
 * options is created, and bound to the component's <code>items</code>
 * property.</li>
 * </ul>
 *
 * @author gjmurphy
 */
public class SelectorDesignInfo extends EditableValueHolderDesignInfo {

    static final String ITEMS = "items"; //NOI18N
    static final String MULTIPLE = "multiple"; //NOI18N
    static final String CONVERTER = "converter"; //NOI18N
    static final String SELECTED_VALUE = "selectedValue"; //NOI18N
    static final String ID = "id"; //NOI18N

    public SelectorDesignInfo(Class clazz) {
        super(clazz);
    }

    /** When a new Selector-based component is dropped, create a default
     * list of options and bind if to this component's <code>items</code> and
     * <code>selected</code> properties.
     *
     * @param bean <code>DesignBean</code> for the newly created instance
     */
    public Result beanCreatedSetup(DesignBean bean) {
        FacesDesignContext context = (FacesDesignContext)bean.getDesignContext();
        Class optionsListClass = getOptionsListClass();
        if(context.canCreateBean(optionsListClass.getName(), null, null)) {
            DesignBean options =
                    context.createBean(optionsListClass.getName(), null, null);
            options.setInstanceName(getOptionsListName(bean), true);
            bean.getProperty(ITEMS).setValueSource(context.getBindingExpr(options, ".options")); //NOI18N
        }
        return Result.SUCCESS;
    }

    /**
     * When a Selector-based component is deleted, check for the existence of a
     * default list of options, and delete it if present.
     */
    public Result beanDeletedCleanup(DesignBean bean) {
        super.beanDeletedCleanup(bean);
        // If bound to a default options bean, and no other selector components
        // are bound to it, delete it
        DesignBean options = getOptionsListBean(bean);
        if (options != null) {
            FacesDesignProperty itemsProperty = (FacesDesignProperty) bean.getProperty(ITEMS);
            String oldExpression = itemsProperty.getValueBinding().getExpressionString();
            itemsProperty.unset();
            DesignBean[] beans = bean.getDesignContext().getBeansOfType(Selector.class);
            int referenceCount = 0;
            for (int i = 0; i < beans.length; i++) {
                DesignProperty p = beans[i].getProperty(ITEMS);
                if (p != null && p instanceof FacesDesignProperty) {
                    ValueBinding valueBinding = ((FacesDesignProperty) p).getValueBinding();
                    if (valueBinding != null) {
                        String expression = valueBinding.getExpressionString();
                        if (oldExpression.equals(expression))
                            referenceCount++;
                    }
                }
            }
            if (referenceCount == 0)
                bean.getDesignContext().deleteBean(options);
        }
        deleteConverter(bean);
        return Result.SUCCESS;
    }

    public Result beanPastedSetup(DesignBean bean) {
        FacesDesignContext context = (FacesDesignContext)bean.getDesignContext();
        // If selector cut or copied and then pasted, and it looks like the
        // component was previously bound to a default options bean, create a
        // new bean for the pasted component. If the paste operation follows a
        // copy operation, then copy the previous default option's properties
        DesignProperty itemsProperty = bean.getProperty(ITEMS);
        if (itemsProperty != null && itemsProperty.getValueSource() != null &&
                itemsProperty.getValueSource().indexOf("DefaultOptions") >= 0) {
            DesignBean options = context.createBean(getOptionsListClass().getName(), null, null);
            options.setInstanceName(getOptionsListName(bean), true);
            itemsProperty.setValueSource(context.getBindingExpr(options, ".options")); // NOI18N
        }
        return Result.SUCCESS;
    }
    
    protected DesignProperty getDefaultBindingProperty(DesignBean targetBean) {
        return targetBean.getProperty(ITEMS); //NOI18N
    }
    
    private static Pattern fieldKeysPattern =
            Pattern.compile("options\\s*\\[\\s*'\\s*([\\w.]+)\\s*(,\\s*([\\w.]+)\\s*)?'\\s*\\]"); //NOI18N
    
    /**
     * When the <code>items</code> property is changed, if previous value was a
     * binding to the default <code>OptionsList</code> for this component, then
     * delete the options list if present.
     */
    public void propertyChanged(DesignProperty property, Object oldValue) {
        super.propertyChanged(property, oldValue);
        PropertyDescriptor descriptor = property.getPropertyDescriptor();
        DesignBean selectorBean = property.getDesignBean();
        FacesDesignContext context = (FacesDesignContext) selectorBean.getDesignContext();
        // If instance name is changed: if the component is bound to a default
        // options list, update the name of the options list to reflect the new
        // instance name; if the component is bound to a converter, update the
        // name of the converter to reflect the new instance name
        if (descriptor.getName().equals(ID)) {
            DesignBean optionsBean = getOptionsListBean(selectorBean);
            if (optionsBean != null) {
                optionsBean.setInstanceName(getOptionsListName(selectorBean));
                selectorBean.getProperty(ITEMS).setValueSource(context.getBindingExpr(optionsBean, ".options")); //NOI18N
            }
            DesignBean converterBean = getConverterBean(selectorBean);
            if (converterBean != null) {
                converterBean.setInstanceName(getConverterName(selectorBean));
                selectorBean.getProperty(CONVERTER).setValue(converterBean.getInstance());
            }
        } else if (descriptor.getName().equals(ITEMS)) {
            // If previous value was a value binding to a default options list, and new
            // value is not a value binding to the same options list, then deleat the
            // default options list
            if (oldValue != null && ValueBinding.class.isAssignableFrom(oldValue.getClass())) {
                ValueBinding valueBinding = ((FacesDesignProperty) property).getValueBinding();
                String oldExpression = ((ValueBinding) oldValue).getExpressionString();
                if (((FacesDesignProperty) property).isBound() && !oldExpression.equals(valueBinding.getExpressionString())) {
                    DesignBean optionsBean = getOptionsListBean(context, oldExpression);
                    if (optionsBean != null) {
                        // Make sure no other components are using the default options
                        DesignBean[] beans = context.getBeansOfType(Selector.class);
                        int referenceCount = 0;
                        for (int i = 0; i < beans.length; i++) {
                            DesignProperty p = beans[i].getProperty(ITEMS);
                            if (p != null && p instanceof FacesDesignProperty) {
                                String expression = ((FacesDesignProperty) p).getValueBinding().getExpressionString();
                                if (oldExpression.equals(expression))
                                    referenceCount++;
                            }
                        }
                        if (referenceCount == 0)
                            context.deleteBean(optionsBean);
                    }
                }
            }
            // If new value is a value binding to options supplied by a dataprovider,
            // create or modify any converters needed
            modifyConverter(property);
        }
    }
    
    /**
     * Returns a class to instantiate for the default options bean. Must be
     * <code>OptionsList</code> or a subclass thereof.
     */
    protected Class getOptionsListClass() {
        return DefaultOptionsList.class;
    }
    
    /**
     * Returns the name of the default options bean.
     */
    protected static String getOptionsListName(DesignBean selectorBean) {
        return selectorBean.getInstanceName() + "DefaultOptions"; //NOI18N
    }
    
    /**
     * If the selector component for the bean specified is bound to an options
     * list, returns the design bean for the options list. Otherwise returns
     * null.
     */
    protected static DesignBean getOptionsListBean(DesignBean selectorBean) {
        FacesDesignContext context = (FacesDesignContext) selectorBean.getDesignContext();
        FacesDesignProperty itemsProperty = (FacesDesignProperty) selectorBean.getProperty(ITEMS);
        if (itemsProperty == null || !itemsProperty.isBound())
            return null;
        String expression = itemsProperty.getValueBinding().getExpressionString();
        return getOptionsListBean(context, expression);
    }
    
    protected static DesignBean getOptionsListBean(FacesDesignContext context, String expression) {
        ResolveResult resolveResult = context.resolveBindingExprToBean(expression);
        if (resolveResult == null || resolveResult.getDesignBean() == null)
            return null;
        DesignBean itemsBean = resolveResult.getDesignBean();
        if (OptionsList.class.isAssignableFrom(itemsBean.getInstance().getClass()))
            return itemsBean;
        return null;
    }
    
    
    
}
