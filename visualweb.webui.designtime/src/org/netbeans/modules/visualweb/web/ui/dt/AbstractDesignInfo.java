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
package org.netbeans.modules.visualweb.web.ui.dt;

import com.sun.data.provider.DataProvider;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.ext.DesignInfoExt;
import com.sun.rave.designtime.faces.ResolveResult;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.data.provider.FieldKey;
import com.sun.rave.designtime.DisplayActionSet;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import java.awt.Image;
import org.netbeans.modules.visualweb.web.ui.dt.component.FormDesignInfo;

import com.sun.rave.web.ui.component.Alert;
import com.sun.rave.web.ui.component.Form;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.ListManager;
import com.sun.rave.web.ui.component.Page;
import com.sun.rave.web.ui.component.ListSelector;
import com.sun.rave.web.ui.component.Selector;
import com.sun.rave.web.ui.component.RbCbSelector;
import com.sun.rave.web.ui.component.StaticText;
import org.netbeans.modules.visualweb.web.ui.dt.component.customizers.AutoSubmitOnChangeAction;
import org.netbeans.modules.visualweb.web.ui.dt.component.customizers.OptionsListCustomizerAction;
import com.sun.rave.web.ui.model.DefaultOptionsList;
import com.sun.rave.web.ui.component.BreadcrumbsBase;
import com.sun.rave.web.ui.component.ButtonBase;
import com.sun.rave.web.ui.component.HyperlinkBase;
import com.sun.rave.web.ui.component.SkipHyperlinkBase;
import com.sun.rave.web.ui.component.DropDown;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlCommandButton;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.validator.Validator;
import javax.faces.el.ValueBinding;
import org.w3c.dom.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import org.netbeans.modules.visualweb.propertyeditors.binding.data.DataBindingHelper;

 

/**
 * <p>Convenience base class for <code>DesignInfo</code> implementations
 * that provide design time behavior for JSF components inside Creator.
 * Any <code>DesignInfo</code> implementation that extends this class
 * will receive the default behavior described for each method, unless that
 * method is overridden.</p>
 */
public abstract class AbstractDesignInfo implements DesignInfo, DesignInfoExt {
    public static final String DECORATION_ICON =
            "/org/netbeans/modules/visualweb/web/ui/dt/resources/Decoration.png";
    
    // ------------------------------------------------------------- Constructor
    
    
    /**
     * <p>Construct a <code>DesignInfo</code> instance for the specified
     * JavaBean class.</p>
     *
     * @param clazz Class of the JavaBean for which this instance is created
     */
    public AbstractDesignInfo(Class clazz) {
        this.beanClass = clazz;
    }
    
    
    // ------------------------------------------------------ Instance Variables
    
    
    /**
     * <p>The JavaBean class this <code>DesignInfo</code> instance is
     * designed to wrap.</p>
     */
    private Class beanClass = null;
    
    
    // ------------------------------------------------------ DesignInfo Methods
    
    
    /**
     * Returns the class type of the JavaBean that was passed to our constructor.
     */
    public Class getBeanClass() {
        return this.beanClass;
    }
    
    /**
     * By default, components are allowed to nest, one with another, unless any
     * of the following conditions apply:
     * <ul>
     * <li>The parent and child component are the same component type.
     * <li>The parent component implements {@link javax.faces.component.EditableValueHolder}
     * <li>
     * </ul>
     */
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if(parentBean == null || parentBean.getInstance() == null)
            return false;
        
        Class parentClass = parentBean.getInstance().getClass();
        return acceptFiliation(parentBean, parentClass, childBean, childClass);
    }
    
    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if(parentBean == null || parentBean.getInstance() == null)
            return false;
        
        Class parentClass = parentBean.getInstance().getClass();
        return acceptFiliation(parentBean, parentClass, childBean, childClass);
    }
    
    /**
     * Returns true is the design bean specified is on a Braveheart page or a
     * page fragment.
     */
    protected static boolean isSunWebUIContext(DesignBean bean) {
        DesignBean thisBean = bean;
        while (thisBean.getBeanParent() != null) {
            if (thisBean.getInstance() instanceof Page)
                return true;
            else if (thisBean instanceof MarkupDesignBean) {
                Element element = ((MarkupDesignBean)thisBean).getElement();
                if (element.getTagName().compareTo("div") == 0 &&
                        thisBean.getBeanParent().getInstance() instanceof UIViewRoot)
                    return true;
            }
            thisBean = thisBean.getBeanParent();
        }
        return false;
    }
    
    private static boolean acceptFiliation(DesignBean parentBean, Class parentClass,
            DesignBean childBean, Class childClass) {
        if (parentClass.equals(childClass))
            return false;
        if (!isSunWebUIContext(parentBean))
            return false;
        if (EditableValueHolder.class.isAssignableFrom(parentClass))
            return false;
        if (ValueHolder.class.isAssignableFrom(parentClass)) {
            if (Label.class.equals(parentClass) && EditableValueHolder.class.isAssignableFrom(childClass))
                return false;
            if (ValueHolder.class.isAssignableFrom(childClass))
                return false;
        }
        if (BreadcrumbsBase.class.isAssignableFrom(parentClass)) {
            return false;   //StaticText and ImageComponent cannot be children of Breadcrumbs
        }
        if (ButtonBase.class.isAssignableFrom(parentClass) ||               //just defensive, not strictly necessary
                HyperlinkBase.class.isAssignableFrom(parentClass) ||
                SkipHyperlinkBase.class.isAssignableFrom(parentClass) ||    //just defensive, not strictly necessary
                HtmlCommandLink.class.isAssignableFrom(parentClass) ||
                DropDown.class.isAssignableFrom(parentClass) ||             //just defensive, not strictly necessary
                HtmlCommandButton.class.isAssignableFrom(parentClass)) {    //just defensive, not strictly necessary
            if (!childClass.equals(StaticText.class) && !childClass.equals(ImageComponent.class))
                return false;
        }
        return true;
    }
    
    /**
     * <p>Take no action by default.  Return <code>Result.SUCCESS</code>.</p>
     *
     * @param bean The bean that was just created
     */
    public Result beanCreatedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }
    
    /**
     * Find the containing Form component and remove the id of the bean that is
     * about to be deleted from the virtual form configuration. Also, if this
     * component implements {@link javax.faces.component.ValueHolder}, search
     * for any components that reference it (ie. that have a <code>for</code>
     * property whose value is this component's instance name), and clear the
     * reference.
     *
     * @param bean The <code>DesignBean</code> that has been renamed.
     * @param oldInstanceName The prior instance name of the bean.
     */
    public Result beanDeletedCleanup(DesignBean bean) {
        modifyVirtualFormsOnBeanDeletedCleanup(bean);
        if (ValueHolder.class.isAssignableFrom(bean.getInstance().getClass())) {
            DesignContext context = bean.getDesignContext();
            DesignBean[] designBeans = context.getBeans();
            String instanceName = bean.getInstanceName();
            for (int i = 0; i < designBeans.length; i++) {
                DesignProperty property = designBeans[i].getProperty("for"); //NOI18N
                if (property != null && instanceName.equals(property.getValue())) {
                    property.setValue(null);
                }
            }
        }
        return Result.SUCCESS;
    }
    
    /** Find the containing form, if it exists. */
    /*
     * Be sure to keep this method in sync with the version in
     * <code>javax.faces.component.html.HtmlDesignInfoBase</code>
     * (in jsfcl).</p>
     */
    private DesignBean findFormBean(DesignBean bean) {
        DesignBean formBean = null;
        DesignBean testBean = null;
        if (bean != null) {
            testBean = bean.getBeanParent();
        }
        while (testBean != null) {
            Object testInstance = testBean.getInstance();
            if (testInstance instanceof Form) {
                formBean = testBean;
                break;
            }
            testBean = testBean.getBeanParent();
        }
        return formBean;
    }
    
    /*
     * Be sure to keep this method in sync with the version in
     * <code>javax.faces.component.html.HtmlDesignInfoBase</code>
     * (in jsfcl).</p>
     */
    private void modifyVirtualFormsOnBeanDeletedCleanup(DesignBean bean) {
        //find the containing form, if it exists
        DesignBean formBean = findFormBean(bean);
        //make sure the id for the bean being deleted is removed from the virtualFormsConfig property
        if (formBean != null) {
            DesignProperty vformsConfigProp = formBean.getProperty("virtualFormsConfig");   //NOI18N
            if (vformsConfigProp != null) {
                Object vformsConfigValueObj = vformsConfigProp.getValue();
                if (vformsConfigValueObj instanceof String) {
                    String vfc = (String)vformsConfigValueObj;
                    Form.VirtualFormDescriptor[] descriptors = Form.generateVirtualForms(vfc);
                    String beanId = FormDesignInfo.getFullyQualifiedId(bean);
                    boolean modified = false;
                    if (beanId != null) {
                        modified = removeIdFromVirtualFormDescriptors(descriptors, beanId); //potentially modifies descriptors object
                    }
                    if (modified) {
                        String newVfc = Form.generateVirtualFormsConfig(descriptors);
                        vformsConfigProp.setValue(newVfc);
                    }
                }
            }
        }
    }
    
    /*
     * Be sure to keep this method in sync with the version in
     * <code>javax.faces.component.html.HtmlDesignInfoBase</code>
     * (in jsfcl).</p>
     */
    private boolean removeIdFromVirtualFormDescriptors(Form.VirtualFormDescriptor[] descriptors, String idToRemove) {
        boolean modified = false;
        for (int d = 0; descriptors != null && d < descriptors.length; d++) {
            Form.VirtualFormDescriptor vfd = descriptors[d];
            
            String[] pids = vfd.getParticipatingIds();
            String[] newPids = removeIdFromArray(pids, idToRemove);
            if (pids != null && newPids != null && pids.length != newPids.length) {
                modified = true;
            }
            vfd.setParticipatingIds(newPids);
            
            String[] sids = vfd.getSubmittingIds();
            String[] newSids = removeIdFromArray(sids, idToRemove);
            if (sids != null && newSids != null && sids.length != newSids.length) {
                modified = true;
            }
            vfd.setSubmittingIds(newSids);
        }
        return modified;
    }
    
    /*
     * Be sure to keep this method in sync with the version in
     * <code>javax.faces.component.html.HtmlDesignInfoBase</code>
     * (in jsfcl).</p>
     */
    private String[] removeIdFromArray(String[] ids, String idToRemove) {
        if (ids == null || ids.length == 0) {return ids;}
        List list = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i]; //might be namespaced
            if (id != null &&
                    id.length() > 0 &&
                    !Form.fullyQualifiedIdMatchesPattern(idToRemove, id)) {  //id in array does not represent fqId being removed
                list.add(id);
            }
        }
        return (String[])list.toArray(new String[list.size()]);
    }
    
    /**
     * <p>Take no action by default.  Return <code>Result.SUCCESS</code>.</p>
     *
     * @param bean The bean that has been pasted
     */
    public Result beanPastedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }
    
    public DisplayActionSet getContextItemsExt(DesignBean bean) {
        DesignProperty property = getDefaultBindingProperty(bean);
        if (property == null) {
            return null;
        }
        final List displayActions = new ArrayList();
        if (EditableValueHolder.class.isAssignableFrom(beanClass))
            displayActions.add(new AutoSubmitOnChangeAction(bean));
        Class beanClass = bean.getInstance().getClass();
        Class bindingPanelClass = null;
        if (Selector.class.isAssignableFrom(beanClass)) {
            if (RbCbSelector.class.isAssignableFrom(beanClass))
                bindingPanelClass = DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER;
            else
                bindingPanelClass = DataBindingHelper.BIND_OPTIONS_TO_DATAPROVIDER;
        } else {
            bindingPanelClass = DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER;
        }
        displayActions.add(
                DataBindingHelper.getDataBindingAction(bean,
                property.getPropertyDescriptor().getName(),
                new Class[] {bindingPanelClass, DataBindingHelper.BIND_VALUE_TO_OBJECT}));
        return new DisplayActionSet() {
            
            public DisplayAction[] getDisplayActions() {
                return (DisplayAction[]) displayActions.toArray(new DisplayAction[displayActions.size()]);
            }
            
            public boolean isPopup() {
                return true;
            }
            
            public boolean isEnabled() {
                return true;
            }
            
            public Result invoke() {
                throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }
            
            public String getDisplayName() {
                return "";
            }
            
            public String getDescription() {
                return "";
            }
            
            public Image getLargeIcon() {
                return new ImageIcon(getClass().getResource(DECORATION_ICON)).getImage();
            }
            
            public Image getSmallIcon() {
                return new ImageIcon(getClass().getResource(DECORATION_ICON)).getImage();
            }
            
            public String getHelpKey() {
                throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }
        }; 
    }
    
    /**
     * <p>Return <code>null</code>, indicating that no context menu items
     * will be provided.</p>
     *
     * @param bean The DesignBean that a user has right-clicked on
     */
    public DisplayAction[] getContextItems(DesignBean bean) {
        DesignProperty property = getDefaultBindingProperty(bean);
        if (property == null) {
            return new DisplayAction[0];
        }
        ArrayList displayActions = new ArrayList();
        if (EditableValueHolder.class.isAssignableFrom(beanClass))
            displayActions.add(new AutoSubmitOnChangeAction(bean));
        Class beanClass = bean.getInstance().getClass();
        Class bindingPanelClass = null;
        if (Selector.class.isAssignableFrom(beanClass)) {
            if (RbCbSelector.class.isAssignableFrom(beanClass))
                bindingPanelClass = DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER;
            else
                bindingPanelClass = DataBindingHelper.BIND_OPTIONS_TO_DATAPROVIDER;
        } else {
            bindingPanelClass = DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER;
        }
        displayActions.add(
                DataBindingHelper.getDataBindingAction(bean,
                property.getPropertyDescriptor().getName(),
                new Class[] {bindingPanelClass, DataBindingHelper.BIND_VALUE_TO_OBJECT}));
        
        if (Selector.class.isAssignableFrom(beanClass)) {
            DesignProperty itemsProperty = bean.getProperty("items");
            if (itemsProperty instanceof FacesDesignProperty && ((FacesDesignProperty) itemsProperty).isBound()) {
                String expression = ((FacesDesignProperty) itemsProperty).getValueBinding().getExpressionString();
                ResolveResult resolveResult =
                        ((FacesDesignContext)bean.getDesignContext()).resolveBindingExprToBean(expression);
                if (resolveResult != null && resolveResult.getDesignBean() != null &&
                        resolveResult.getDesignBean().getInstance() instanceof DefaultOptionsList)
                    displayActions.add(new OptionsListCustomizerAction(bean));
            }
        }
        
        return (DisplayAction[]) displayActions.toArray(new DisplayAction[displayActions.size()]);
    }
    
    // FIXME - HtmlDesignInfoBase returns true if target is-a ValueHolder
    // and source is-a ResultSet
    /**
     * <p>Return <code>true</code> for the specific cases listed below,
     * else return <code>false</code>.</p>
     *
     * <table border="1">
     *  <tr>
     *    <th>Target Bean Class Is-A</th>
     *    <th>Source Bean Class Is-A</th>
     *  </tr>
     *  <tr>
     *    <td><code>javax.faces.component.EditableValueHolder</code></td>
     *    <td><code>javax.faces.validator.Validator</code></td>
     *  </tr>
     *  <tr>
     *    <td><code>javax.faces.component.ValueHolder</code></td>
     *    <td><code>javax.faces.convert.Converter</code></td>
     *  </tr>
     * </table>
     *
     * @param targetBean The <code>DesignBean</code> instance that the user
     *   is 'hovering' the mouse over
     * @param sourceBean Optional <code>DesignBean</code> instance describing
     *   a preexisting source bean that is being dragged or linked, or
     *   <code>null</code> if no such bean exists
     * @param sourceClass The class type of the object that the user may
     *   potentially 'drop' to link
     *
     * @see #linkBeans
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean,
            Class sourceClass) {
        
        Class targetClass = targetBean.getInstance().getClass();
        if (Converter.class.isAssignableFrom(sourceClass) &&
                ValueHolder.class.isAssignableFrom(targetClass)) {
            return true;
        } else if (Validator.class.isAssignableFrom(sourceClass) &&
                EditableValueHolder.class.isAssignableFrom(targetClass)) {
            return true;
        } else if (DataProvider.class.isAssignableFrom(sourceClass)) {
            if (this.getDefaultBindingProperty(targetBean) != null)
                return true;
        }
        return false;
    }
    
    
    // FIXME - HtmlDesignInfoBase does ResultSet and RowSetDataModel too
    /**
     * <p>For the cases where the default <code>acceptLink()</code>
     * returns true, attempt to set the appropriate property and return
     * a <code>Result</code> reflecting the outcome.  Otherwise, just
     * return <code>Result.SUCCESS</code>.</p>
     *
     * @param targetBean The target <code>DesignBean</code> instance that the
     *  user has 'dropped' an object onto to establish a link
     * @param sourceBean The <code>DesignBean</code> instance that has
     *  been 'dropped'
     *
     * @see #acceptLink
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        
        Class sourceClass = sourceBean.getInstance().getClass();
        Class targetClass = targetBean.getInstance().getClass();
        if (!acceptLink(targetBean, sourceBean, sourceClass))
            return Result.FAILURE;
        
        if (Converter.class.isAssignableFrom(sourceClass)) {
            DesignProperty property = targetBean.getProperty("converter"); //NOI18N
            property.setValue(sourceBean.getInstance());
        } else if (Validator.class.isAssignableFrom(sourceClass)) {
            DesignProperty property = targetBean.getProperty("validator"); //NOI18N
            FacesDesignContext fdc = (FacesDesignContext) targetBean.getDesignContext();
            String validateBinding = fdc.getBindingExpr(sourceBean, ".validate"); //NOI18N
            Application app = fdc.getFacesContext().getApplication();
            MethodBinding mb = app.createMethodBinding(validateBinding, VALIDATE_PARAMS);
            property.setValue(mb);
        } else if (DataProvider.class.isAssignableFrom(sourceClass)) {
            DesignProperty property = getDefaultBindingProperty(targetBean);
            if (property != null && property instanceof FacesDesignProperty) {
                if (Selector.class.isAssignableFrom(targetClass)) {
                    // Listbox, DropDown, CheckboxGroup, RadioButtonGroup, AddRemove
                    if (RbCbSelector.class.isAssignableFrom(targetClass))
                        linkDataProviderToSingleton((FacesDesignProperty) property, sourceBean);
                    else
                        linkDataProviderToListSelector((FacesDesignProperty) property, sourceBean);
                } else if (ListManager.class.isAssignableFrom(targetClass)) {
                    // OrderableList, EditableList
                    linkDataProviderToStringListSelector((FacesDesignProperty) property, sourceBean);
                } else {
                    linkDataProviderToSingleton((FacesDesignProperty) property, sourceBean);
                }
            }
        }
        return Result.SUCCESS;
    }
    
    /**
     * Returns a property descriptor for the property which should be bound to
     * a data source by default (e.g. whan a data source is linked to the component).
     * If data binding is not appropriate for this component, returns null.
     */
    protected DesignProperty getDefaultBindingProperty(DesignBean targetBean) {
        Class targetClass = targetBean.getInstance().getClass();
        DesignProperty property = null;
        if (Alert.class.isAssignableFrom(targetClass)){
            property = targetBean.getProperty("summary"); //NOI18N
        }else if (ListSelector.class.isAssignableFrom(targetClass)) {
            property = targetBean.getProperty("items"); //NOI18N
        } else if (ValueHolder.class.isAssignableFrom(targetClass)) {
            property = targetBean.getProperty("text"); //NOI18N
        } else if (ActionSource.class.isAssignableFrom(targetClass)) {
            property = targetBean.getProperty("text"); //NOI18N
        }
        if (property == null){
            property = targetBean.getProperty("value"); //NOI18N
        }
        return property;
    }
    
    public Result linkDataProviderToSingleton(FacesDesignProperty property, DesignBean dataBean) {
        FacesDesignContext fdc = (FacesDesignContext)property.getDesignBean().getDesignContext();
        DataProvider provider = (DataProvider)dataBean.getInstance();
        FieldKey[] fieldKeys = provider.getFieldKeys();
        if (fieldKeys != null && fieldKeys.length > 0) {
            FieldKey fieldKey = null;
            Class propertyClass = property.getPropertyDescriptor().getPropertyType();
            for(int i=0; i< fieldKeys.length && fieldKey == null; i++){
                if (provider.getType(fieldKeys[i]).isAssignableFrom(propertyClass))
                    fieldKey = fieldKeys[i];
            }
            if (fieldKey == null)
                fieldKey = fieldKeys[0];
            StringBuffer expr = new StringBuffer();
            expr.append("#{");
            expr.append(fdc.getReferenceName());
            expr.append(".");
            expr.append(dataBean.getInstanceName());
            expr.append(".value['");
            expr.append(fieldKey.getFieldId());
            expr.append("']}");
            ValueBinding vb = fdc.getFacesContext().getApplication().createValueBinding(expr.toString());
            property.setValueBinding(vb);
            return Result.SUCCESS;
        }
        return Result.FAILURE;
    }
    
    public Result linkDataProviderToListSelector(FacesDesignProperty property, DesignBean dataBean) {
        FacesDesignContext fdc = (FacesDesignContext)property.getDesignBean().getDesignContext();
        DataProvider provider = (DataProvider)dataBean.getInstance();
        FieldKey[] fieldKeys = provider.getFieldKeys();
        
        if (fieldKeys != null && fieldKeys.length > 0) {
            String valueField = null;
            String displayField = null;
            Class valueFieldType = null;
            for(int i=0; i< fieldKeys.length; i++){
                if ((valueField == null) && isTypeOf(Integer.class, "int", provider.getType(fieldKeys[i]))){
                    valueField = fieldKeys[i].getFieldId();
                    valueFieldType = provider.getType(fieldKeys[i]);
                }
                if ((displayField == null) && provider.getType(fieldKeys[i]).isAssignableFrom(String.class)){
                    displayField = fieldKeys[i].getFieldId();
                }
            }
            if(valueField == null){
                valueField = fieldKeys[0].getFieldId();
                valueFieldType = provider.getType(fieldKeys[0]);
            }
            if(displayField == null){
                displayField = fieldKeys[0].getFieldId();
            }
            StringBuffer expr = new StringBuffer();
            expr.append("#{");
            expr.append(fdc.getReferenceName());
            expr.append(".");
            expr.append(dataBean.getInstanceName());
            expr.append(".options['");
            expr.append(valueField);
            expr.append(",");
            expr.append(displayField);
            expr.append("']}");
            ValueBinding vb = fdc.getFacesContext().getApplication().createValueBinding(expr.toString());
            property.setValueBinding(vb);
            return Result.SUCCESS;
        }
        return Result.FAILURE;
    }
    
    public Result linkDataProviderToStringListSelector(FacesDesignProperty property, DesignBean dataBean) {
        FacesDesignContext fdc = (FacesDesignContext)property.getDesignBean().getDesignContext();
        DataProvider provider = (DataProvider)dataBean.getInstance();
        FieldKey[] fieldKeys = provider.getFieldKeys();
        if (fieldKeys != null && fieldKeys.length > 0) {
            FieldKey fieldKey = null;
            for(int i=0; i< fieldKeys.length && fieldKey == null; i++){
                if (provider.getType(fieldKeys[i]).isAssignableFrom(String.class))
                    fieldKey = fieldKeys[i];
            }
            if (fieldKey == null)
                fieldKey = fieldKeys[0];
            StringBuffer expr = new StringBuffer();
            expr.append("#{");
            expr.append(fdc.getReferenceName());
            expr.append(".");
            expr.append(dataBean.getInstanceName());
            expr.append(".stringList['");
            expr.append(fieldKey.getFieldId());
            expr.append("']}");
            ValueBinding vb = fdc.getFacesContext().getApplication().createValueBinding(expr.toString());
            property.setValueBinding(vb);
            return Result.SUCCESS;
        }
        return Result.FAILURE;
    }
    
    public static Class getConverterClass(Class type) {
        
        if(isTypeOf(Integer.class, "int", type)){
            return javax.faces.convert.IntegerConverter.class;
        }else if(isTypeOf(Byte.class, "byte", type)){
            return javax.faces.convert.ByteConverter.class;
        }else if(isTypeOf(Long.class, "long", type)){
            return javax.faces.convert.LongConverter.class;
        }else if(isTypeOf(Boolean.class, "boolean", type)){
            return javax.faces.convert.BooleanConverter.class;
        }else if(isTypeOf(Short.class, "short", type)){
            return javax.faces.convert.ShortConverter.class;
        }else if(type.isAssignableFrom(Date.class)){
            return com.sun.rave.faces.converter.SqlDateConverter.class;
        }else if(type.isAssignableFrom(Time.class)){
            return com.sun.rave.faces.converter.SqlTimeConverter.class;
        }else if(type.isAssignableFrom(BigDecimal.class)){
            return javax.faces.convert.BigDecimalConverter.class;
        }else if(isTypeOf(Double.class, "double", type)){
            return javax.faces.convert.DoubleConverter.class;
        }else if(isTypeOf(Float.class, "float", type)){
            return javax.faces.convert.FloatConverter.class;
        }else if(type.isAssignableFrom(Calendar.class)) {
            return com.sun.rave.faces.converter.CalendarConverter.class;
        }
        
        return null;
    }
    
    // ---------------------------------------------- DesignBeanListener Methods
    
    
    /**
     * <p>Take no action by default.</p>
     *
     * @param bean The <code>DesignBean</code> whose context has been activated
     */
    public void beanContextActivated(DesignBean bean) {
        ;
    }
    
    
    /**
     * <p>Take no action by default.</p>
     *
     * @param bean The <code>DesignBean</code> whose context has been deactivated
     */
    public void beanContextDeactivated(DesignBean bean) {
        ;
    }
    
    
    /**
     * If this component implements {@link javax.faces.component.ValueHolder},
     * search for any components that reference it (ie. that have a <code>for</code>
     * property whose value is this component's instance name), and update the
     * reference.
     *
     * @param bean The <code>DesignBean</code> that has been renamed.
     * @param oldInstanceName The prior instance name of the bean.
     */
    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {
        if (ValueHolder.class.isAssignableFrom(bean.getInstance().getClass())) {
            DesignContext context = bean.getDesignContext();
            DesignBean[] designBeans = context.getBeans();
            for (int i = 0; i < designBeans.length; i++) {
                DesignProperty property = designBeans[i].getProperty("for"); //NOI18N
                if (property != null && oldInstanceName.equals(property.getValue())) {
                    property.setValue(bean.getInstanceName());
                }
            }
        }
    }
    
    /**
     * <p>Take no action by default.</p>
     *
     * @param bean The <code>DesignBean</code> that has changed.
     */
    public void beanChanged(DesignBean bean) {
        ;
    }
    
    
    /**
     * <p>Take no action by default.</p>
     *
     * @param event The <code>DesignEvent</code> that has changed.
     */
    public void eventChanged(DesignEvent event) {
        ;
    }
    
    
    /**
     * <p>By default, if the id property changed, modify the virtual forms
     * configuration and any autosubmit scripting to reflect the change.</p>
     *
     * @param property The <code>DesignProperty</code> that has changed.
     * @param oldValue Optional oldValue, or <code>null</code> if the
     *  previous value is not known
     */
    public void propertyChanged(DesignProperty property, Object oldValue) {
        modifyVirtualFormsOnPropertyChanged(property, oldValue);
        modifyAutoSubmitOnPropertyChanged(property);
    }
    
    private static Pattern fieldKeysPattern =
            Pattern.compile("options\\s*\\[\\s*'\\s*([\\w.]+)\\s*(,\\s*([\\w.]+)\\s*)?'\\s*\\]"); //NOI18N
    static final String CONVERTER = "converter"; //NOI18N
    
    /**
     * Create, modify or delete converters as needed
     * based on the type of the Field Key bound to the
     * property.
     */
    public void modifyConverter(DesignProperty property){
        // If new value is a value binding to options supplied by a dataprovider,
        // create or modify any converters needed
        DesignBean designBean = property.getDesignBean();
        DesignProperty converterProp = designBean.getProperty(CONVERTER);
        if (converterProp != null){
            FacesDesignContext context = (FacesDesignContext) designBean.getDesignContext();
            if (((FacesDesignProperty) property).isBound()) {
                String expression = ((FacesDesignProperty) property).getValueBinding().getExpressionString();
                ResolveResult resolveResult = context.resolveBindingExprToBean(expression);
                if (resolveResult == null || resolveResult.getDesignBean() == null ||
                        !DataProvider.class.isAssignableFrom(resolveResult.getDesignBean().getInstance().getClass()))
                    return;
                DataProvider dataProvider = (DataProvider) resolveResult.getDesignBean().getInstance();
                Matcher matcher = fieldKeysPattern.matcher(resolveResult.getRemainder());
                if (!matcher.matches())
                    return;
                FieldKey valueKey = dataProvider.getFieldKey(matcher.group(1));
                Class converterClass = getConverterClass(dataProvider.getType(valueKey));
                
                if(converterProp.isModified()){
                    DesignBean oldConverterBean = getConverterBean(designBean);
                    if (oldConverterBean != null) {
                        context.deleteBean(oldConverterBean);
                    }
                }
                if (converterClass != null) {
                    DesignBean converterBean = context.createBean(converterClass.getName(), null, null);
                    if (converterBean != null) {
                        converterBean.setInstanceName(getConverterName(designBean));
                        converterProp.setValue(converterBean.getInstance());
                    }
                }
            }
        }
    }
    
    public void deleteConverter(DesignBean designBean){
        // If bound to a converter, and no other components are bound to it, delete it
        DesignBean converter = getConverterBean(designBean);
        if (converter != null) {
            FacesDesignProperty converterProperty = (FacesDesignProperty) designBean.getProperty(CONVERTER);
            String oldExpression = converterProperty.getValueBinding().getExpressionString();
            converterProperty.unset();
            DesignBean[] beans = designBean.getDesignContext().getBeansOfType(EditableValueHolder.class);
            int referenceCount = 0;
            for (int i = 0; i < beans.length; i++) {
                DesignProperty p = beans[i].getProperty(CONVERTER);
                if (p != null && p instanceof FacesDesignProperty) {
                    String expression = ((FacesDesignProperty) p).getValueBinding().getExpressionString();
                    if (oldExpression.equals(expression))
                        referenceCount++;
                }
            }
            if (referenceCount == 0) designBean.getDesignContext().deleteBean(converter);
        }
    }
    
    /**
     * Returns the name of the default options bean.
     */
    protected static String getConverterName(DesignBean designBean) {
        return designBean.getInstanceName() + "Converter"; //NOI18N
    }
    
    /**
     * If the selector component for the bean specified is bound to a converter,
     * returns the design bean for the converter. Otherwise returns null.
     */
    protected static DesignBean getConverterBean(DesignBean designBean) {
        FacesDesignContext context = (FacesDesignContext) designBean.getDesignContext();
        FacesDesignProperty converterProperty = (FacesDesignProperty) designBean.getProperty(CONVERTER);
        if (converterProperty == null || !converterProperty.isBound())
            return null;
        String expression = converterProperty.getValueBinding().getExpressionString();
        return getConverterBean(context, expression);
    }
    
    protected static DesignBean getConverterBean(FacesDesignContext context, String expression) {
        ResolveResult resolveResult = context.resolveBindingExprToBean(expression);
        if (resolveResult == null || resolveResult.getDesignBean() == null)
            return null;
        DesignBean converterBean = resolveResult.getDesignBean();
        if (Converter.class.isAssignableFrom(converterBean.getInstance().getClass()))
            return converterBean;
        return null;
    }
    
    private static boolean isTypeOf( Class ofType, String primitiveType, Class tobecheckType ) {
        if( tobecheckType.isAssignableFrom( ofType ) ||
                (tobecheckType.isPrimitive() && tobecheckType.getName().equals( primitiveType ) ) )
            return true;
        else
            return false;
    }
    
    /*
     * Be sure to keep this method in sync with the version in
     * <code>javax.faces.component.html.HtmlDesignInfoBase</code>
     * (in jsfcl).</p>
     */
    private void modifyAutoSubmitOnPropertyChanged(DesignProperty property) {
        PropertyDescriptor pd = property.getPropertyDescriptor();
        String propertyName = pd.getName();
        if ("id".equals(propertyName))  {   //NOI18N
            DesignBean bean = property.getDesignBean();
            if (bean != null && bean.getInstance() instanceof EditableValueHolder) {
                AutoSubmitOnChangeAction autoSubmitAction = new AutoSubmitOnChangeAction(bean);
                if (autoSubmitAction.isAutoSubmit()) {
                    //toggle twice
                    autoSubmitAction.toggleAutoSubmit();
                    autoSubmitAction.toggleAutoSubmit();
                }
            }
        }
    }
    
    /*
     * Be sure to keep this method in sync with the version in
     * <code>javax.faces.component.html.HtmlDesignInfoBase</code>
     * (in jsfcl).</p>
     */
    private void modifyVirtualFormsOnPropertyChanged(DesignProperty property, Object oldValue) {
        PropertyDescriptor pd = property.getPropertyDescriptor();
        String propertyName = pd.getName();
        if ("id".equals(propertyName) && oldValue instanceof String)  {   //NOI18N
            //get virtual form descriptors
            DesignBean bean = property.getDesignBean();
            if (bean != null) {
                String fqId = FormDesignInfo.getFullyQualifiedId(bean);
                String replacementId = fqId;
                if (replacementId == null) {
                    //try using just straight id
                    Object replacementIdObj = property.getValue();
                    if (replacementIdObj instanceof String) {
                        replacementId = (String)replacementIdObj;
                    }
                } else if (replacementId.startsWith(String.valueOf(NamingContainer.SEPARATOR_CHAR)) && replacementId.length() > 1) {
                    //fully qualified replacementId (starting with ":") could look intimidating to users. so just chop off leading ":"
                    replacementId = replacementId.substring(1, replacementId.length());
                }
                if (replacementId != null) {
                    DesignBean formBean = findFormBean(bean);
                    if (formBean != null) {
                        DesignProperty vfcProp = formBean.getProperty("virtualFormsConfig"); //NOI18N
                        if (vfcProp != null) {
                            Object vfcObj = vfcProp.getValue();
                            if (vfcObj instanceof String) {
                                String vfc = (String)vfcObj;
                                Form.VirtualFormDescriptor[] vfds = Form.generateVirtualForms(vfc);
                                if (vfds != null && vfds.length > 0) {
                                    //get old fully qualified id
                                    DesignBean parentBean = bean.getBeanParent();
                                    if (parentBean != null) {
                                        String parentBeanFqId = FormDesignInfo.getFullyQualifiedId(parentBean);
                                        if (parentBeanFqId != null && parentBeanFqId.length() > 0) {
                                            String oldFqId = parentBeanFqId;
                                            String sep = String.valueOf(NamingContainer.SEPARATOR_CHAR);
                                            if (!sep.equals(oldFqId)) oldFqId += sep;
                                            oldFqId += oldValue;
                                            boolean vfdsModified = false;
                                            for (int v = 0; v < vfds.length; v++) {
                                                String[] participants = vfds[v].getParticipatingIds();
                                                String[] submitters = vfds[v].getSubmittingIds();
                                                boolean pMod = modifyIdArray(participants, oldFqId, replacementId);
                                                boolean sMod = modifyIdArray(submitters, oldFqId, replacementId);
                                                if (pMod || sMod) vfdsModified = true;
                                            }
                                            if (vfdsModified) {
                                                String newVfc = Form.generateVirtualFormsConfig(vfds);
                                                vfcProp.setValue(newVfc);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /*
     * Be sure to keep this method in sync with the version in
     * <code>javax.faces.component.html.HtmlDesignInfoBase</code>
     * (in jsfcl).</p>
     */
    private boolean modifyIdArray(String[] ids, String oldFqId, String replacementId) {
        if (ids == null || ids.length < 1) return false;
        boolean modified = false;;
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i]; //could be qualified
            boolean idRepresentsOldFqId = Form.fullyQualifiedIdMatchesPattern(oldFqId, id);
            if (idRepresentsOldFqId) {
                ids[i] = replacementId;
                modified = true;
            }
        }
        return modified;
    }
    
    
    // ------------------------------------------------------- Protected Methods
    
    
    // FIXME - deal with component bundle and associated utility class
    
    
    /**
     * <p>Return the <code>BeanDescriptor</code> for the class this
     * <code>DesignInfo</code> is designed to wrap, if possible;
     * otherwise, return <code>null</code>.</p>
     */
    protected BeanDescriptor getBeanDescriptor() {
        try {
            return getBeanInfo().getBeanDescriptor();
        } catch (IntrospectionException e) {
            return null;
        }
    }
    
    
    /**
     * <p>Return the <code>BeanInfo</code> for the class this
     * <code>DesignInfo</code> is designed to wrap.</p>
     *
     * @exception IntrospectionException if an error occurs during introspection
     */
    protected BeanInfo getBeanInfo() throws IntrospectionException {
        return Introspector.getBeanInfo(getBeanClass());
    }
    
    
    /**
     * <p>Return the <code>PropertyDescriptor</code> for the specified
     * property of the class this <code>DesignInfo</code> is designed
     * to wrap, if possible and if it exists; otherwise, return
     * <code>null</code>.</p>
     */
    protected PropertyDescriptor getPropertyDescriptor(String name) {
        Map map = getPropertyDescriptorMap();
        if (map != null) {
            return (PropertyDescriptor) map.get(name);
        } else {
            
            return null;
        }
    }
    
    
    /**
     * <p>Return an array of <code>PropertyDescriptor</code>s for the class
     * this <code>DesignInfo</code> is designed to wrap, if possible;
     * otherwise, return <code>null</code>.</p>
     */
    protected PropertyDescriptor[] getPropertyDescriptors() {
        try {
            return getBeanInfo().getPropertyDescriptors();
        } catch (IntrospectionException e) {
            return null;
        }
    }
    
    
    // --------------------------------------------------------- Private Methods
    
    
    /**
     * <p>Cache key for the property descriptor map, cached in the
     * <code>BeanDescriptor</code> on first access.</p>
     */
    private static final String PROPERTY_DESCRIPTOR_MAP =
            "com.sun.rave.designtime.PROPERTY_DESCRIPTOR_MAP"; //NOI18N
    
    
    /**
     * <p>Method signature for a <code>Validator.validate()</code> method.</p>
     */
    private static final Class[] VALIDATE_PARAMS = {
        FacesContext.class, UIComponent.class, Object.class
    };
    
    
    /**
     * <p>Return the <code>Map</code> of <code>PropertyDescriptor</code>s for
     * the class this <code>DesignInfo</code> is designed to wrap, if
     * possible; otherwise, return <code>null</code>.</p>
     */
    private Map getPropertyDescriptorMap() {
        BeanDescriptor bd = getBeanDescriptor();
        if (bd == null) {
            return null;
        }
        Map map = (Map) bd.getValue(PROPERTY_DESCRIPTOR_MAP);
        if (map == null) {
            PropertyDescriptor pd[] = getPropertyDescriptors();
            if (pd == null) {
                return null;
            }
            map = new HashMap(pd.length);
            for (int i = 0; i < pd.length; i++) {
                map.put(pd[i].getName(), pd[i]);
            }
            bd.setValue(PROPERTY_DESCRIPTOR_MAP, map);
        }
        return map;
    }
    
    
}
