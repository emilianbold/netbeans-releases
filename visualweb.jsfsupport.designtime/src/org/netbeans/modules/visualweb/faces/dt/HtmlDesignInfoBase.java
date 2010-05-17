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
package org.netbeans.modules.visualweb.faces.dt;
import com.sun.data.provider.DataProvider;
import com.sun.data.provider.FieldKey;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.NumberConverter;
import javax.faces.el.MethodBinding;
import javax.faces.validator.Validator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.RowSet;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.CustomizerResult;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignContext;
import java.util.List;
import javax.faces.component.NamingContainer;
import javax.faces.component.html.HtmlCommandLink;

import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.faces.FacesDesignProject;
import com.sun.rave.faces.data.DefaultSelectItemsArray;
import com.sun.rave.faces.data.RowSetDataModel;
import org.netbeans.modules.visualweb.faces.dt.component.html.HtmlFormDesignInfo;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import org.netbeans.modules.visualweb.faces.dt.converter.NumberConverterCustomizer;
import javax.faces.el.ValueBinding;
import org.netbeans.modules.visualweb.propertyeditors.binding.data.DataBindingHelper;

public abstract class HtmlDesignInfoBase implements DesignInfo {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(HtmlDesignInfoBase.class);
    protected static final String BEAN_DESCRIPTOR_PROPERTY_DESCRIPTOR_MAP_CACHE_KEY =
        HtmlDesignInfoBase.class.getName() + "-PropertyDescriptorMapCache"; // NOI18N

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public Result beanPastedSetup(DesignBean bean) {
        return Result.SUCCESS;
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        modifyVirtualFormsOnBeanDeletedCleanup(bean);
        return Result.SUCCESS;
    }

    /** Find the containing form, if it exists. */
    /*
     * Be sure to keep this method in sync with the version in
     * <code>com.sun.rave.web.ui.design.AbstractDesignInfo</code> (in webui).</p>
     */
    private DesignBean findFormBean(DesignBean bean) {
        DesignBean formBean = null;
        DesignBean testBean = null;
        if (bean != null) {
            testBean = bean.getBeanParent();
        }
        while (testBean != null) {
            Object testInstance = testBean.getInstance();
            if (testInstance != null &&
                    "com.sun.rave.web.ui.component.Form".equals(testInstance.getClass().getName())) {
                formBean = testBean;
                break;
            }
            testBean = testBean.getBeanParent();
        }
        return formBean;
    }
    
    /*
     * Be sure to keep this method in sync with the version in 
     * <code>com.sun.rave.web.ui.design.AbstractDesignInfo</code> (in webui).</p>
     */
    protected void modifyVirtualFormsOnBeanDeletedCleanup(DesignBean bean) {
        //find the containing form, if it exists
        DesignBean formBean = findFormBean(bean);
        //make sure the id for the bean being deleted is removed from the virtualFormsConfig property
        if (formBean != null) {
            DesignProperty vformsConfigProp = formBean.getProperty("virtualFormsConfig");   //NOI18N
            if (vformsConfigProp != null) {
                Object vformsConfigValueObj = vformsConfigProp.getValue();
                if (vformsConfigValueObj instanceof String) {
                    String vfc = (String)vformsConfigValueObj;
                    HtmlFormDesignInfo.VirtualFormDescriptor[] descriptors = HtmlFormDesignInfo.generateVirtualForms(vfc);
                    String beanId = HtmlFormDesignInfo.getFullyQualifiedId(bean);
                    boolean modified = false;
                    if (beanId != null) {
                        modified = removeIdFromVirtualFormDescriptors(descriptors, beanId); //potentially modifies descriptors object
                    }
                    if (modified) {
                        String newVfc = HtmlFormDesignInfo.generateVirtualFormsConfig(descriptors);
                        vformsConfigProp.setValue(newVfc);
                    }
                }
            }
        }
    }

    /*
     * Be sure to keep this method in sync with the version in 
     * <code>com.sun.rave.web.ui.design.AbstractDesignInfo</code> (in webui).</p>
     */
    private boolean removeIdFromVirtualFormDescriptors(HtmlFormDesignInfo.VirtualFormDescriptor[] descriptors, String idToRemove) {
        boolean modified = false;
        for (int d = 0; descriptors != null && d < descriptors.length; d++) {
            HtmlFormDesignInfo.VirtualFormDescriptor vfd = descriptors[d];

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
     * <code>com.sun.rave.web.ui.design.AbstractDesignInfo</code> (in webui).</p>
     */
    private String[] removeIdFromArray(String[] ids, String idToRemove) {
        if (ids == null || ids.length == 0) {return ids;}
        List list = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i]; //might be namespaced
            if (id != null && 
                    id.length() > 0 && 
                    !HtmlFormDesignInfo.fullyQualifiedIdMatchesPattern(idToRemove, id)) {  //id in array does not represent fqId being removed
                list.add(id);
            }
        }
        return (String[])list.toArray(new String[list.size()]);
    }

    /**
     * Return the BeanInfo class of my getBeanClass().
     * May return nil if an error is encountered on retrieving BeanInfo.
     *
     * @return
     * @throws IntrospectionException
     */
    protected BeanInfo getBeanInfo() throws IntrospectionException {

        return Introspector.getBeanInfo(getBeanClass());
    }

    protected BeanDescriptor getBeanDescriptor() {

        try {
            BeanInfo beanInfo = getBeanInfo();
            return beanInfo.getBeanDescriptor();
        } catch (IntrospectionException e) {
            return null;
        }
    }

    protected PropertyDescriptor getPropertyDescriptorNamed(String propertyName) {

        if (propertyName == null) {
            return null;
        }
        BeanInfo beanInfo;
        try {
            beanInfo = getBeanInfo();
        } catch (IntrospectionException e) {
            beanInfo = null;
        }
        if (beanInfo == null) {
            return null;
        }
        BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
        // if I have no way of caching a hashmap then do it iteratively
        if (beanDescriptor == null) {
            PropertyDescriptor propertyDescriptors[] = beanInfo.getPropertyDescriptors();
            if (propertyDescriptors == null) {
                return null;
            }
            for (int i = 0, max = propertyDescriptors.length; i < max; i++) {
                PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
                if (propertyName.equals(propertyDescriptor.getName())) {
                    return propertyDescriptor;
                }
            }
            return null;
        }
        // since BeanDescriptors allow me to store named values, I can create a hash map and use it to do the lookup
        // instead of doing it iterativelly
        HashMap propertyDescriptorsByName = (HashMap)beanDescriptor.getValue(
            BEAN_DESCRIPTOR_PROPERTY_DESCRIPTOR_MAP_CACHE_KEY);
        if (propertyDescriptorsByName != null) {
            return (PropertyDescriptor)propertyDescriptorsByName.get(propertyName);
        }
        PropertyDescriptor propertyDescriptors[] = beanInfo.getPropertyDescriptors();
        propertyDescriptorsByName = new HashMap(propertyDescriptors.length);
        for (int i = 0, max = propertyDescriptors.length; i < max; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            propertyDescriptorsByName.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        beanDescriptor.setValue(BEAN_DESCRIPTOR_PROPERTY_DESCRIPTOR_MAP_CACHE_KEY,
            propertyDescriptorsByName);
        return (PropertyDescriptor)propertyDescriptorsByName.get(propertyName);
    }

    /**
     * Returns the list (or hierarchy) of items to be included in a right-click context menu for
     * this bean at design-time.
     *
     * @param bean The DesignBean that a user has right-clicked on
     * @return An array of DisplayAction objects representing a context menu to display to the user
     */
    public DisplayAction[] getContextItems(DesignBean bean) {
        // Use the new data binding dialogs that support Data providers
        ArrayList actions = new ArrayList();
        DesignProperty property = bean.getProperty("value");
        if(property != null){
            Class bindingPanelClass = DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER;
            DisplayAction bindToDataAction = DataBindingHelper.getDataBindingAction(bean,
                    property.getPropertyDescriptor().getName(),
                    new Class[] {bindingPanelClass, DataBindingHelper.BIND_VALUE_TO_OBJECT});

            if (!(bean.getInstance() instanceof HtmlCommandLink)) {
                actions.add(bindToDataAction);
            }
            if (bean.getInstance() instanceof UIInput) {
                actions.add(new AutoSubmitOnChangeCheckedAction(bean));
            }
        }
        return (DisplayAction[])actions.toArray(new DisplayAction[actions.size()]);
    }

    /**
     * This method is called when an object from a design surface or palette is being dragged 'over'
     * a JavaBean type handled by this DesignInfo.  If the 'sourceClass' is of interest to the
     * 'targetBean' instance (they can be "linked"), this method should return <b>true</b>.  The
     * user will then be presented with visual cues that this is an appropriate place to 'drop' the
     * item and establish a link.  If the user decides to drop the item on this targetBean, the
     * 'linkBeans' method will be called.
     *
     * @param targetBean The DesignBean instance that the user is 'hovering' the mouse over
     * @param sourceBean The DesignBean instance that the user may potentially 'drop' to link (may be null)
     * @param sourceClass The class type of the object that the user may potentially 'drop' to link
     * @return <b>true</b> if the 'targetBean' cares to have an instance of type 'sourceClass'
     *         linked to it, <b>false</b> if not
     * @see linkBeans
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {

        boolean retVal = false ;
        if (canLinkConverterOrValidatorBeans(targetBean, sourceClass)) {
            retVal = true;
        }
        if ( ! retVal ) {
            boolean isDatabaseClass = isResultSetClass( sourceClass) ;
            DesignProperty valueProp = targetBean.getProperty("value"); // NOI18N
            retVal = isDatabaseClass && valueProp != null;
        }
        if ( ! retVal ) {
            if (DataProvider.class.isAssignableFrom(sourceClass)) {
                DesignProperty valueProp = targetBean.getProperty("value"); // NOI18N
                retVal = valueProp != null;
            }
        }
        return retVal ;
    }

    /**
     * <P>This method is called when an object from a design surface or palette has been dropped
     * 'on' a JavaBean type handled by this DesignInfo (to establish a link).  This method will
     * not be called unless the corresponding 'acceptLink' method call returned <b>true</b>.
     * Typically, this results in property settings on potentially both of the DesignBean objects.</P>
     *
     * @param targetBean The target DesignBean instance that the user has 'dropped' an object onto to
     *        establish a link
     * @param sourceBean The DesignBean instance that has been 'dropped'
     * @return A Result object, indicating success or failure and including messages for the user
     * @see acceptLink
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        
        // if cachedRowSetDataProvider, switch the sourceBean to
        // it's rowset.
        if ( isCachedRowSetDP( sourceBean.getInstance() ) ) {
            DesignBean db = findCachedRowSetBean( sourceBean ) ;
            if ( db != null ) {
                sourceBean = db ;
            }
        }
        if (sourceBean.getInstance() instanceof RowSetDataModel) {
            try {
                RowSetDataModel rsdm = (RowSetDataModel)sourceBean.getInstance();
                ResultSet rs = (ResultSet)rsdm.getWrappedData();
                if (rs != null) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    if (rsmd.getColumnCount() >= 1) {
                        String table = rsmd.getTableName(1);
                        String column = rsmd.getColumnName(1);
                        DesignBean rootBean = sourceBean.getDesignContext().getRootContainer();
                        String outerName = rootBean.getInstanceName();
                        String valueRef = "#{" + outerName + "." + sourceBean.getInstanceName() +
                            ".rowData." + column + "}"; //NOI18N
                        targetBean.getProperty("value").setValueSource(valueRef); //NOI18N
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        } else if (sourceBean.getInstance() instanceof ResultSet) {
            try {
                ResultSet rs = (ResultSet)sourceBean.getInstance();
                ResultSetMetaData rsmd = rs.getMetaData();
                if (rsmd.getColumnCount() >= 1) {
                    String table = rsmd.getTableName(1);
                    String column = rsmd.getColumnName(1);
                    DesignBean rootBean = sourceBean.getDesignContext().getRootContainer();
                    String outerName = rootBean.getInstanceName();
                    String valueRef = "#{" + outerName + "." + sourceBean.getInstanceName() +
                        ".currentRow['" + column + "']}"; //NOI18N
                    targetBean.getProperty("value").setValueSource(valueRef); //NOI18N
//                    String dtvalue = "[" + table + "." + column + "]";
//                    targetBean.getDesignContext().setContextData(targetBean.getInstanceName() + ".databind", dtvalue);
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        ;

        try {
            if (canLinkConverterOrValidatorBeans(targetBean, sourceBean)) {
                linkConverterOrValidatorBeans(targetBean, sourceBean);
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    public void beanContextActivated(DesignBean bean) {}

    public void beanContextDeactivated(DesignBean bean) {}

    public void instanceNameChanged(DesignBean bean, String oldInstanceName) {}

    public void beanChanged(DesignBean bean) {}

    public void propertyChanged(DesignProperty prop, Object oldValue) {
        modifyVirtualFormsOnPropertyChanged(prop, oldValue);
        modifyAutoSubmitOnPropertyChanged(prop);
    }
    
    /*
     * Be sure to keep this method in sync with the version in 
     * <code>com.sun.rave.web.ui.design.AbstractDesignInfo</code> (in webui).</p>
     */
    private void modifyAutoSubmitOnPropertyChanged(DesignProperty property) {
        PropertyDescriptor pd = property.getPropertyDescriptor();
        String propertyName = pd.getName();
        if ("id".equals(propertyName))  {   //NOI18N
            DesignBean bean = property.getDesignBean();
            //technically, should probably be checking for EditableValueHolder rather than UIInput, but
            //getContextItems checks for UIInput, and this must match
            if (bean != null && bean.getInstance() instanceof UIInput) {
                AutoSubmitOnChangeCheckedAction autoSubmitAction = new AutoSubmitOnChangeCheckedAction(bean);
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
     * <code>com.sun.rave.web.ui.design.AbstractDesignInfo</code> (in webui).</p>
     */
    private void modifyVirtualFormsOnPropertyChanged(DesignProperty property, Object oldValue) {
        PropertyDescriptor pd = property.getPropertyDescriptor();
        String propertyName = pd.getName();
        if ("id".equals(propertyName) && oldValue instanceof String)  {   //NOI18N
            //get virtual form descriptors
            DesignBean bean = property.getDesignBean();
            if (bean != null) {
                String fqId = HtmlFormDesignInfo.getFullyQualifiedId(bean);
                String replacementId = fqId;
                if (replacementId == null) {
                    //try using just straight id
                    Object replacementIdObj = property.getValue();
                    if (replacementIdObj instanceof String) {
                        replacementId = (String)replacementIdObj;
                    }
                }
                else if (replacementId.startsWith(String.valueOf(NamingContainer.SEPARATOR_CHAR)) && replacementId.length() > 1) {
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
                                HtmlFormDesignInfo.VirtualFormDescriptor[] vfds = HtmlFormDesignInfo.generateVirtualForms(vfc);
                                if (vfds != null && vfds.length > 0) {
                                    //get old fully qualified id
                                    DesignBean parentBean = bean.getBeanParent();
                                    if (parentBean != null) {
                                        String parentBeanFqId = HtmlFormDesignInfo.getFullyQualifiedId(parentBean);
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
                                                String newVfc = HtmlFormDesignInfo.generateVirtualFormsConfig(vfds);
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
     * <code>com.sun.rave.web.ui.design.AbstractDesignInfo</code> (in webui).</p>
     */
    private boolean modifyIdArray(String[] ids, String oldFqId, String replacementId) {
        if (ids == null || ids.length < 1) return false;
        boolean modified = false;;
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i]; //could be qualified
            boolean idRepresentsOldFqId = HtmlFormDesignInfo.fullyQualifiedIdMatchesPattern(oldFqId, id);
            if (idRepresentsOldFqId) {
                ids[i] = replacementId;
                modified = true;
            }
        }
        return modified;
    }

    public void eventChanged(DesignEvent event) {}

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean, int[] types) {
        int[][] _types = new int[1][];
        _types[0] = types;
        return linkBeans(targetBean, sourceBean, _types);
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean, int[][] types) {
        if (sourceBean.getInstance() instanceof RowSetDataModel) {
            try {
                RowSetDataModel rsdm = (RowSetDataModel)sourceBean.getInstance();
                ResultSet rs = (ResultSet)rsdm.getWrappedData();
                if (rs != null) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int col = findAppropriateColumn(rsmd, types);
                    if (col != -1) {
                        String table = rsmd.getTableName(col);
                        String column = rsmd.getColumnName(col);
                        String outerName = sourceBean.getDesignContext().getRootContainer().
                            getInstanceName();
                        String valueRef = "#{" + outerName + "." + sourceBean.getInstanceName() //NOI18N
                            + ".rowData." + column + "}"; //NOI18N
                        String dtvalue = "[" + table + "." + column + "]"; //NOI18N
                        targetBean.getProperty("value").setValue(valueRef); //NOI18N
                        targetBean.getDesignContext().setContextData(targetBean.getInstanceName()
                            + ".databind", dtvalue); //NOI18N
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Result.FAILURE;
            }
        }
        return Result.SUCCESS;
    }

    /*
     * the following method is called by getContextItems for components that contain SelectItems
     */
    public DisplayAction[] selectOneGetContextItems(DesignBean bean) {
        
        // Use the new data binding dialogs that support Data providers
        
        DesignProperty selectionProperty = bean.getProperty("value");
        Class selectionBindingPanelClass = DataBindingHelper.BIND_VALUE_TO_DATAPROVIDER;
        DisplayAction bindSelectionToDataAction = DataBindingHelper.getDataBindingAction(bean,
            selectionProperty.getPropertyDescriptor().getName(),
            new Class[] {selectionBindingPanelClass, DataBindingHelper.BIND_VALUE_TO_OBJECT},
            true, bundle.getMessage("bindSelectionToDataEllipse"), bundle.getMessage("bindSelectionToData")
         );
        
        DesignBean selectItemsBean = null;
     
        int childCount = bean.getChildBeanCount();
        for(int i=0; i< childCount; i++){
            selectItemsBean = bean.getChildBean(i);
            if (bean.getChildBean(i).getInstance() instanceof UISelectItems){
                break;
            }
        }
        if(selectItemsBean != null){
            DesignProperty property = selectItemsBean.getProperty("value");
            if(property != null){
                Class bindingPanelClass = DataBindingHelper.BIND_OPTIONS_TO_DATAPROVIDER;
                DisplayAction bindToDataAction = DataBindingHelper.getDataBindingAction(selectItemsBean,
                        property.getPropertyDescriptor().getName(),
                        new Class[] {bindingPanelClass, DataBindingHelper.BIND_VALUE_TO_OBJECT});
                return new DisplayAction[] {
                    new AutoSubmitOnChangeCheckedAction(bean),
                    bindToDataAction
                    // See bug 6324729 - as per Jeffs comment removing it        
                    //bindSelectionToDataAction
                };
            }
        }
        return new DisplayAction[0];
    }

    public DisplayAction[] selectManyGetContextItems(DesignBean bean) {
       // Use the new data binding dialogs that support Data providers
        
        DesignBean selectItemsBean = null;
        int childCount = bean.getChildBeanCount();
        for(int i=0; i< childCount; i++){
            selectItemsBean = bean.getChildBean(i);
            if (bean.getChildBean(i).getInstance() instanceof UISelectItems){
                break;
            }
        }
        if(selectItemsBean != null){
            DesignProperty property = selectItemsBean.getProperty("value");
            if(property != null){
                Class bindingPanelClass = DataBindingHelper.BIND_OPTIONS_TO_DATAPROVIDER;
                DisplayAction bindToDataAction = DataBindingHelper.getDataBindingAction(selectItemsBean,
                        property.getPropertyDescriptor().getName(),
                        new Class[] {bindingPanelClass, DataBindingHelper.BIND_VALUE_TO_OBJECT});
                return new DisplayAction[] {
                    new AutoSubmitOnChangeCheckedAction(bean),
                    bindToDataAction
                };
            }
        }
        return new DisplayAction[0];
    }

    /*
     * the following method is called by beanCreated for components that contain SelectItems
     */
    public Result selectOneBeanCreated(DesignBean bean) {
        try {
            DesignContext context = bean.getDesignContext();
            // create and setup a default items array
            DesignBean items = context.createBean(DefaultSelectItemsArray.class.getName(), null, null);
            items.setInstanceName(bean.getInstanceName() + "DefaultItems", true); //NOI18N

            // create a selectitems child
            if (context.canCreateBean(UISelectItems.class.getName(), bean, null)) {
                DesignBean si = context.createBean(UISelectItems.class.getName(), bean, null);
                if (si != null) {
                    si.setInstanceName(bean.getInstanceName() + "SelectItems", true); //NOI18N
                    String outer = ((FacesDesignContext) bean.getDesignContext()).getReferenceName();
                    si.getProperty("value").setValueSource("#{" + outer + "." + //NOI18N
                        items.getInstanceName() + "}"); //NOI18N
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.SUCCESS;
    }

    public Result selectManyBeanCreated(DesignBean bean) {
        return selectOneBeanCreated(bean);
    }

    public Result selectManyBeanPasted(DesignBean bean) {
        return selectOneBeanPasted(bean);
    }

    /*
     * the following method is called by beanCreated for components that contain SelectItems
     */
    public Result selectOneBeanPasted(DesignBean bean) {
        try {
            DesignContext context = bean.getDesignContext();
            final String itemsName = bean.getInstanceName() + "DefaultItems"; //NOI18N
            final String selectItemsName = bean.getInstanceName() + "SelectItems"; //NOI18N
            // See if the component has selectItems assigned
            DesignBean si = context.getBeanByName(selectItemsName);
            if (si != null) {
                String source = si.getProperty("value").getValueSource(); //NOI18N
                // If value refers to the defaultItems we would create automatically, then go ahead and create them
                if (source != null && source.indexOf(itemsName) >= 0) {
                    DesignBean items = context.createBean(DefaultSelectItemsArray.class.getName(), null, null);
                    items.setInstanceName(itemsName, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.SUCCESS;
    }

    public Result selectOneBeanDeleted(DesignBean bean) {
        try {
            DesignContext context = bean.getDesignContext();
            // find and kill the default items array
            DesignBean defaultItemsBean = context.getBeanByName(bean.getInstanceName() +
                "DefaultItems"); //NOI18N
            if (defaultItemsBean != null &&
                defaultItemsBean.getInstance() instanceof DefaultSelectItemsArray) {
                context.deleteBean(defaultItemsBean);
            }
            // find and kill the default converter
            DesignBean converterBean = context.getBeanByName(bean.getInstanceName() + "Converter"); //NOI18N
            if (converterBean != null && converterBean.getInstance() instanceof Converter) {
                context.deleteBean(converterBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.SUCCESS;
    }

    public Result selectManyBeanDeleted(DesignBean bean) {
        return selectOneBeanDeleted(bean);
    }

    protected ArrayList getExportedKeys(ResultSet rowSet, String catalog, String schema,
        String table) {

        if (!(rowSet instanceof RowSet)) {
            return null;
        }
        Connection c = null;
        try {
            c = getConnection((RowSet)rowSet);
            DatabaseMetaData dmd = c.getMetaData();
            ResultSet rs = dmd.getExportedKeys(catalog, schema, table);
            ArrayList list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString("PKCOLUMN_NAME")); //NOI18N
            }
            return list;
        } catch (Exception ex) {
            return null;
        } finally {
            // attempt to close, if fails, not an exceptional condition
            try {
                c.close();
            } catch (SQLException e) {
            }
        }
    }

    protected ArrayList getPrimaryKeys(ResultSet rowSet, String catalog, String schema,
        String table) {

        if (!(rowSet instanceof RowSet)) {
            return null;
        }
        Connection c = null;
        try {
            c = getConnection((RowSet)rowSet);
            DatabaseMetaData dmd = c.getMetaData();
            ResultSet rs = dmd.getPrimaryKeys(catalog, schema, table);
            ArrayList list = new ArrayList();
            while (rs.next()) {
                list.add(rs.getString("COLUMN_NAME")); //NOI18N
            }
            return list;
        } catch (Exception ex) {
            return null;
        } finally {
            // attempt to close, if fails, not an exceptional condition
            try {
                c.close();
            } catch (SQLException e) {
            }
        }
    }

    static protected Connection getConnection(RowSet rs) throws SQLException {
        try {
            if (rs.getDataSourceName() == null) {
                return DriverManager.getConnection(rs.getUrl(), rs.getUsername(), rs.getPassword());
            } else {
                Context ctx = new InitialContext();
                DataSource ds = (DataSource)ctx.lookup(rs.getDataSourceName());
                return ds.getConnection();
            }
        } catch (NamingException e) {
            SQLException sqle = new SQLException();
            sqle.initCause(e);
            throw sqle;
        }
    }

    /*
     * the following method is called by linkBeans for components that contain SelectItems
     */
    public Result selectOneLinkBeans(DesignBean targetBean, DesignBean sourceBean) {
        //DesignProperty selectionProperty = targetBean.getProperty("value");
        
        DesignBean selectItemsBean = null;
        int childCount = targetBean.getChildBeanCount();
        for(int i=0; i< childCount; i++){
            selectItemsBean = targetBean.getChildBean(i);
            if (targetBean.getChildBean(i).getInstance() instanceof UISelectItems){
                break;
            }
        }
        FacesDesignProperty selectItemsValueproperty = (FacesDesignProperty) selectItemsBean.getProperty("value");
        Class sourceClass = sourceBean.getInstance().getClass();
        if (DataProvider.class.isAssignableFrom(sourceClass)) {
            FacesDesignContext fdc = (FacesDesignContext)targetBean.getDesignContext();
            DataProvider provider = (DataProvider)sourceBean.getInstance();
            FieldKey[] fieldKeys = provider.getFieldKeys();

            // Find a field of type Integer for value and String for display
            if (fieldKeys != null && fieldKeys.length > 0) {
                String valueField = null;
                String displayField = null;
                Class valueFieldType = null;
                for(int i=0; i< fieldKeys.length; i++){
                   if ((valueField == null) && provider.getType(fieldKeys[i]).isAssignableFrom(Integer.class)){
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
                expr.append(sourceBean.getInstanceName());
                expr.append(".options['");
                expr.append(valueField);
                expr.append(",");
                expr.append(displayField);
                expr.append("']}");
                ValueBinding vb = fdc.getFacesContext().getApplication().createValueBinding(expr.toString());
                selectItemsValueproperty.setValueBinding(vb);
                return Result.SUCCESS;
            }
            return Result.FAILURE;
        }
        /*if ( isResultSetClass( sourceBean.getInstance().getClass() ) )  {
            try {
                // if cachedRowSetDataProvider, switch the sourceBean to
                // it's rowset.
                if ( isCachedRowSetDP( sourceBean.getInstance() ) ) {
                    DesignBean db = findCachedRowSetBean( sourceBean) ;
                    if ( db != null ) {
                        sourceBean = db ;
                    }
                }
                
                ResultMessage[] messages = new ResultMessage[1];
                DisplayAction[] actions = new DisplayAction[2];
                messages[0] = new ResultMessage(ResultMessage.TYPE_INFORMATION,
                    bundle.getMessage("selItemLinkBeanPrompt", sourceBean.getInstanceName()), //NOI18N
                    bundle.getMessage("dbBindSelItemClarify")); //NOI18N
                final DesignBean tb = targetBean;
                final DesignBean sb = sourceBean;
                actions[0] = new BasicDisplayAction(bundle.getMessage("fillList")) { //NOI18N
                    public Result invoke() {
                        try {
                            ResultSet rs = (ResultSet)sb.getInstance();
                            if (rs != null) {
                                ResultSetMetaData rsmd = rs.getMetaData();
                                if (rsmd.getColumnCount() >= 1) {
                                    //default to first column for value and no label or desc
                                    //but maybe we can do better
                                    int valueColumnNum = -1;
                                    int labelColumnNum = -1;
                                    // use the table from the first column
                                    String catalog = rsmd.getCatalogName(1);
                                    String schema = rsmd.getSchemaName(1);
                                    String table = rsmd.getTableName(1);
                                    ArrayList list = getExportedKeys(rs, catalog, schema, table);
                                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                                        if (list.contains(rsmd.getColumnName(i))) {
                                            valueColumnNum = i;
                                            break;
                                        }
                                    }
                                    if (valueColumnNum == -1) {
                                        list = getPrimaryKeys(rs, catalog, schema, table);
                                        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                                            if (list.contains(rsmd.getColumnName(i))) {
                                                valueColumnNum = i;
                                                break;
                                            }
                                        }
                                    }
                                    if (valueColumnNum == -1) {
                                        valueColumnNum = 1;
                                    }
                                    table = rsmd.getTableName(valueColumnNum);
                                    String column = rsmd.getColumnName(valueColumnNum);

                                    //if value is not a character type, look for first char type
                                    //and use that column as a label
                                  
                                    String labelColumn = null;
                                    int valueType = rsmd.getColumnType(valueColumnNum);
                                    if (valueType != Types.CHAR && valueType != Types.LONGVARCHAR
                                        && valueType != Types.VARCHAR) {
                                        labelColumnNum = findAppropriateColumn(rsmd,
                                            new int[][] {
                                            {
                                            Types.CHAR, Types.VARCHAR}
                                            , {
                                            Types.LONGVARCHAR}
                                        });
                                    }
                                    if (labelColumnNum != -1) {
                                        labelColumn = rsmd.getColumnName(labelColumnNum);
                                    }
                                    // build the value
                                    String outerName =
                                        ((FacesDesignContext)sb.getDesignContext()).getReferenceName();
                                    String value = "#{" + outerName + "." + //NOI18N
                                        sb.getInstanceName() + ".selectItems['" + column; //NOI18N
                                    if (labelColumnNum != -1) {
                                        value += "," + labelColumn; //NOI18N
                                    }
                                    value += "']}"; //NOI18N
//                                    String dtvalue = "[" + table + "." + column + "]";
                                    DesignBean selectItems = null;
                                    DesignBean[] children = tb.getChildBeans();
                                    if (children != null && children.length > 0) {
                                        for (int i = 0; i < children.length; i++) {
                                            if (children[i].getInstance() instanceof UISelectItems) {
                                                selectItems = children[i];
                                                break;
                                            }
                                        }
                                    }
                                    if (selectItems == null) {
                                        selectItems = tb.getDesignContext().createBean(
                                            UISelectItems.class.getName(), tb, null);
                                        selectItems.setInstanceName(tb.getInstanceName()
                                            + "SelectItems", true); //NOI18N
                                    }
                                    ///MBOHM for 6194849, no harm in calling this line here
                                    value = maybeSetupDefaultSelectItems(selectItems, value);
                                    selectItems.getProperty("value").setValue(value); //NOI18N
                                    //MBOHM 6194849 //maybeSetupConverter(selectItems, valueType);
                                    return Result.SUCCESS;
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return Result.FAILURE;
                    }
                };
                actions[1] = new BasicDisplayAction(bundle.getMessage("bindToSel")) { //NOI18N
                    public Result invoke() {
                        try {
                            ResultSet rs = (ResultSet)sb.getInstance();
                            if (rs != null) {
                                ResultSetMetaData rsmd = rs.getMetaData();
                                if (rsmd.getColumnCount() >= 1) {
                                    String table = rsmd.getTableName(1);
                                    String column = rsmd.getColumnName(1);
                                    // build the value
                                    String outerName =
                                        ((FacesDesignContext)sb.getDesignContext()).getReferenceName();
                                    String value = "#{" + outerName + "." + //NOI18N
                                        sb.getInstanceName() + ".currentRow['" + column + "']}"; //NOI18N
                                    tb.getProperty("value").setValue(value); //NOI18N
                                }
                                // MBOHM 6194849
                                //DesignBean[] siKids = tb.getChildBeans();
                                //for (int i = 0; i < siKids.length; i++) {
                                //    maybeSetupConverter(siKids[i], 0);
                                //}
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return Result.SUCCESS;
                    }
                };
                return new Result(true, messages, actions);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.FAILURE;
            }
        }*/
        return Result.SUCCESS;
    }

    public Result selectManyLinkBeans(DesignBean targetBean, DesignBean sourceBean) {
        
        // if cachedRowSetDataProvider, switch the sourceBean to
        // it's rowset.
        if ( isCachedRowSetDP( sourceBean.getInstance() ) ) {
            DesignBean db = findCachedRowSetBean( sourceBean) ;
            if ( db != null ) {
                sourceBean = db ;
            }
        }        
        
        if (sourceBean.getInstance() instanceof ResultSet) {
            DesignBean tb = targetBean;
            DesignBean sb = sourceBean;
            try {
                ResultSet rs = (ResultSet)sb.getInstance();
                if (rs != null) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    if (rsmd.getColumnCount() >= 1) {
                        /*
                         * default to first column for value and no label or desc
                         * but maybe we can do better
                         */
                        int valueColumnNum = -1;
                        int labelColumnNum = -1;
                        // use the table from the first column
                        String catalog = rsmd.getCatalogName(1);
                        String schema = rsmd.getSchemaName(1);
                        String table = rsmd.getTableName(1);
                        ArrayList list = getExportedKeys(rs, catalog, schema, table);
                        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                            if (list.contains(rsmd.getColumnName(i))) {
                                valueColumnNum = i;
                                break;
                            }
                        }
                        if (valueColumnNum == -1) {
                            list = getPrimaryKeys(rs, catalog, schema, table);
                            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                                if (list.contains(rsmd.getColumnName(i))) {
                                    valueColumnNum = i;
                                    break;
                                }
                            }
                        }
                        if (valueColumnNum == -1) {
                            valueColumnNum = 1;
                        }
                        table = rsmd.getTableName(valueColumnNum);
                        String column = rsmd.getColumnName(valueColumnNum);

                        /* if value is not a character type, look for first char type
                         * and use that column as a label
                         */
                        String labelColumn = null;
                        int valueType = rsmd.getColumnType(valueColumnNum);
                        if (valueType != Types.CHAR && valueType != Types.LONGVARCHAR
                            && valueType != Types.VARCHAR) {
                            labelColumnNum = findAppropriateColumn(rsmd,
                                new int[][] {
                                {
                                Types.CHAR, Types.VARCHAR}
                                , {
                                Types.LONGVARCHAR}
                            });
                        }
                        if (labelColumnNum != -1) {
                            labelColumn = rsmd.getColumnName(labelColumnNum);
                        }
                        // build the value
                        String outerName =
                            ((FacesDesignContext)sb.getDesignContext()).getReferenceName();
                        String value = "#{" + outerName + "." + //NOI18N
                            sb.getInstanceName() + ".selectItems['" + column; //NOI18N
                        if (labelColumnNum != -1) {
                            value += "," + labelColumn; //NOI18N
                        }
                        value += "']}"; //NOI18N
//                                    String dtvalue = "[" + table + "." + column + "]";
                        DesignBean selectItems = null;
                        DesignBean[] children = tb.getChildBeans();
                        if (children != null && children.length > 0) {
                            for (int i = 0; i < children.length; i++) {
                                if (children[i].getInstance() instanceof UISelectItems) {
                                    selectItems = children[i];
                                    break;
                                }
                            }
                        }
                        if (selectItems == null) {
                            selectItems = tb.getDesignContext().createBean(
                                UISelectItems.class.getName(), tb, null);
                            selectItems.setInstanceName(tb.getInstanceName()
                                + "SelectItems", true); //NOI18N
                        }
                        //MBOHM for 6194849, no harm in calling this line here
                        value = maybeSetupDefaultSelectItems(selectItems, value);
                        selectItems.getProperty("value").setValue(value); //NOI18N
                        //MBOHM 6194849 //maybeSetupConverter(selectItems, valueType);
                        return Result.SUCCESS;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    public static String maybeSetupDefaultSelectItems(DesignBean selectItemsBean, String valueExpr) {
        try {
            if (valueExpr != null && !"".equals(valueExpr)) {
                if (selectItemsBean.getInstance() instanceof UISelectItems) {
                    // find the parent UISelectOne or UISelectMany bean
                    DesignBean selectCompBean = selectItemsBean.getBeanParent();
                    if (selectCompBean != null &&
                        (selectCompBean.getInstance() instanceof UISelectOne ||
                        selectCompBean.getInstance() instanceof UISelectMany)) {

                        DesignContext context = selectCompBean.getDesignContext();
                        // check for existance of default items array - and nuke it
                        String itemsName = selectCompBean.getInstanceName() + "DefaultItems"; //NOI18N
                        DesignBean itemsBean = context.getBeanByName(itemsName);
                        if (itemsBean != null) {
                            // check if this value expression points at the default items already
                            // if not, then delete them...
//JOE: this exposes an insync bug in 'resolveBindingExpr'
//JOE: so the below fix works without hitting the bug
//                            if (context instanceof FacesDesignContext) {
//                                Object o = ((FacesDesignContext)context).resolveBindingExpr(valueExpr);
//                                if (o != itemsBean.getInstance()) {
//                                    context.deleteBean(itemsBean);
//                                }
//                            }
                            String[] vx = valueExpr.split("\\.");
                            if (vx.length > 0) {
                                String name = vx[vx.length - 1];
                                name = name.substring(0, name.length() - 1);
                                if (!name.equals(itemsBean.getInstanceName())) {
                                    context.deleteBean(itemsBean);
                                }
                            }
                        }
                    }
                }
                return valueExpr;
            } else if (selectItemsBean.getInstance() instanceof UISelectItems) {
                // find the parent UISelectOne or UISelectMany bean
                DesignBean selectCompBean = selectItemsBean.getBeanParent();
                if (selectCompBean != null &&
                    (selectCompBean.getInstance() instanceof UISelectOne ||
                    selectCompBean.getInstance() instanceof UISelectMany)) {

                    DesignContext context = selectCompBean.getDesignContext();
                    // check for existance / create and setup a default items array
                    String itemsName = selectCompBean.getInstanceName() + "DefaultItems"; //NOI18N
                    DesignBean itemsBean = context.getBeanByName(itemsName);
                    if (itemsBean == null ||
                        !(itemsBean.getInstance() instanceof DefaultSelectItemsArray)) {
                        itemsBean = context.createBean(DefaultSelectItemsArray.class.getName(), null, null);
                        itemsBean.setInstanceName(itemsName, true); //NOI18N
                    }
                    if (context instanceof FacesDesignContext) {
                        return "#{" + ((FacesDesignContext)context).getReferenceName() + "." +
                            itemsBean.getInstanceName() + "}"; //NOI18N
                    }
                    return "#{" + context.getDisplayName() + "." + itemsBean.getInstanceName() +
                        "}"; //NOI18N
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valueExpr;
    }

    public static void maybeSetupConverter(DesignBean selectItemsBean, int sqlDataType) {
        Class convClass = getConverterClass(sqlDataType);
        try {
            if (selectItemsBean.getInstance() instanceof UISelectItems) {
                // find the parent UISelectOne or UISelectMany bean
                DesignBean selectCompBean = selectItemsBean.getBeanParent();
                if (selectCompBean != null &&
                    (selectCompBean.getInstance() instanceof UISelectOne ||
                    selectCompBean.getInstance() instanceof UISelectMany)) {

                    DesignContext context = selectItemsBean.getDesignContext();
                    String convName = selectCompBean.getInstanceName() + "Converter"; //NOI18N
                    DesignBean oldConvBeanByName = context.getBeanByName(convName);
                    if (oldConvBeanByName != null) {
                        context.deleteBean(oldConvBeanByName);
                    }

                    // if the value is not set...
                    DesignProperty scValueProp = selectCompBean.getProperty("value"); //NOI18N
                    if (scValueProp != null &&
                        (scValueProp.getValue() == null ||
                        scValueProp.getValueSource() == null ||
                        "".equals(scValueProp.getValueSource()))) { //NOI18N

                        // and no converter (or wrong type) is set...
                        DesignProperty convProp = selectCompBean.getProperty("converter"); //NOI18N
                        if (convProp != null) {
                            Object convObj = convProp.getValue();
                            if (convClass == null && convObj != null) {
                                convProp.setValue(null);
                            } else if (convClass != null && (convObj == null ||
                                !convClass.isAssignableFrom(convObj.getClass()))) {
                                // we need to setup a new converter
                                DesignBean convBean = context.createBean(convClass.getName(), null, null);
                                convBean.setInstanceName(convName);
                                if (convBean != null) {
                                    convProp.setValue(convBean.getInstance());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Pass in the java.sql.Types type returned by ResultSet.getColumnType()
     * True is returned if a converter is available or if a converter is not
     * needed.
     *
     * Note:  The information is returned is a best guess.  This will be further refined
     *        post-RR.  Also, more converters will be written post-RR.
     */
    public static boolean isConvertible(int sqlType) {

        switch (sqlType) {

            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.BIT:
            case Types.BOOLEAN:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.DOUBLE:
            case Types.REAL:
            case Types.FLOAT:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
                return true;

            case Types.BINARY:
            case Types.BLOB:
            case Types.CLOB:
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
            case Types.LONGVARBINARY:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.STRUCT:
            case Types.VARBINARY:
                return false;
        }

        return false;
    }

    /**
     * Pass in the java.sql.Types type returned by ResultSet.getColumnType()
     * The appropriate faces converter class is returned.  If null is returned,
     * it may be because no converter exists or because a converter is not
     * needed.  If you want to know which case it is, call isConvertible(int sqlType)
     * this will return true if a converter is available or a converter is not needed.
     *
     * The converter choice is based on the JDBC 3.0 Specification, Appendix B,
     * "Data Type Conversion Tables", Table B-1.
     */
    public static Class getConverterClass(int sqlType) {

        switch (sqlType) {

            case Types.SMALLINT:
                return javax.faces.convert.ShortConverter.class;

            case Types.INTEGER:
                return javax.faces.convert.IntegerConverter.class;

            case Types.TINYINT:
                return javax.faces.convert.ByteConverter.class;

            case Types.BIGINT:
                return javax.faces.convert.LongConverter.class;

            case Types.BIT:
            case Types.BOOLEAN:
                return javax.faces.convert.BooleanConverter.class;

            case Types.DATE:
                return com.sun.rave.faces.converter.SqlDateConverter.class;

            case Types.TIME:
                return com.sun.rave.faces.converter.SqlTimeConverter.class;

            case Types.TIMESTAMP:
                return com.sun.rave.faces.converter.SqlTimestampConverter.class;

            case Types.DECIMAL:
            case Types.NUMERIC:
                return javax.faces.convert.BigDecimalConverter.class;

            case Types.DOUBLE:
            case Types.FLOAT:
                return javax.faces.convert.DoubleConverter.class;

            case Types.REAL:
                return javax.faces.convert.FloatConverter.class;

            case Types.BINARY:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.BLOB:
            case Types.CLOB:
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
            case Types.LONGVARBINARY:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.STRUCT:
            case Types.VARBINARY:
                return null;
        }

        return null;
    }

    /**
     * findAppropriateColumn finds an appropriate column to bind to in a resultset.
     * Pass an array of SQL types (java.sql.Types) and this method will return
     * the first column in the resultset that matches any of the types in the
     * array.  If some types have a higher priority, use the  version of
     * findAppropriateColumn that takes an array of array types. If a match is found,
     * the column position (1 based) in the resultset will be returned.  If no match
     * is found, -1 is returned.
     */
    protected int findAppropriateColumn(ResultSetMetaData rsmd, int[] types) throws SQLException {
        int[][] _types = new int[1][];
        _types[0] = types;
        return findAppropriateColumn(rsmd, _types);
    }

    /**
     * findAppropriateColumn finds an appropriate column to bind to in a resultset.
     * Pass an array of arrays of SQL types (java.sql.Types).  This method will first
     * attempt to match any of the types in the first array.  If no columns match, it
     * will proceed to the next array.  If a match is found, the column position (1 based)
     * in the resultset will be returned.  If no match is found, -1 is returned.
     *
     * Example:
     *
     * private static final int[][] TYPES = new int[][] {
     *     {Types.INTEGER, Types.SMALLINT}, // First try to match on these
     *     {Types.BIT, Types.TINYINT}       // If no match above, try to match on these
     * };
     *
     * public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
     *     return linkBeans(targetBean, sourceBean, TYPES);
     * }
     */
    protected int findAppropriateColumn(ResultSetMetaData rsmd, int[][] types) throws SQLException {
        for (int i = 0; i < types.length; i++) {
            for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                for (int k = 0; k < types[i].length; k++) {
                    if (rsmd.getColumnType(j) == types[i][k]) {
                        return j;
                    }
                }
            }
        }
        return -1;
    }

    /*
     * I know this SUCKS and is a HACK, but with the way all of the subclasses implement linkBeans()
     * and the fact that there is no consumed ability to the event, I'm doing it this way to be safe.
     * If I dont, it may end up that something is linked twice and can't afford that at T-1.5 of RR :(
     * I most likely could have just made sure that each class did or did not have the properties
     * in question, but brain not working that well at moment, and this is what I can deal with at
     * moment.
     *
     * HACKS
     *  - doConverterOrValidatorLinkBeans
     *  - isConverterOrValidatorLinkBeans
     *  - and all there callers
     *
     * @param targetBean
     * @param sourceBean
     * @return
     */
    public Result linkConverterOrValidatorBeans(DesignBean targetBean, DesignBean sourceBean) {

        try {
            DesignBean rootBean = sourceBean.getDesignContext().getRootContainer();
            DesignProperty property;
            if (sourceBean.getInstance() instanceof Converter) {
                property = targetBean.getProperty("converter"); // NOI18N
                if (property != null && property.getPropertyDescriptor() != null &&
                    Converter.class.isAssignableFrom(property.getPropertyDescriptor().
                    getPropertyType())) {
                    property.setValue(sourceBean.getInstance());
                    if (sourceBean.getInstance() instanceof NumberConverter) {
                        return new CustomizerResult(sourceBean,
                            new NumberConverterCustomizer());
                    }
                    return Result.SUCCESS;
                }
            }
            if (sourceBean.getInstance() instanceof Validator) {
                property = targetBean.getProperty("validator"); // NOI18N
                if (property != null && property.getPropertyDescriptor() != null &&
                    MethodBinding.class.isAssignableFrom(property.getPropertyDescriptor().
                    getPropertyType())) {
                    FacesDesignContext fctx = (FacesDesignContext)targetBean.getDesignContext();
                    String validateBinding = fctx.getBindingExpr(sourceBean, ".validate"); // NOI18N
                    Application app = fctx.getFacesContext().getApplication();
                    MethodBinding mb = app.createMethodBinding(validateBinding,
                        new Class[] {
                        FacesContext.class, UIComponent.class, Object.class});
                    property.setValue(mb);
                    return Result.SUCCESS;
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    public boolean canLinkConverterOrValidatorBeans(DesignBean targetBean, Class sourceClass) {

        PropertyDescriptor propertyDescriptor;
        if (Converter.class.isAssignableFrom(sourceClass)) {
            propertyDescriptor = getPropertyDescriptorNamed("converter"); // NOI18N
            if (propertyDescriptor != null &&
                Converter.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                return true;
            }
        }
        if (Validator.class.isAssignableFrom(sourceClass)) {
            propertyDescriptor = getPropertyDescriptorNamed("validator"); // NOI18N
            if (propertyDescriptor != null &&
                MethodBinding.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                return true;
            }
        }
        return false;
    }

    public boolean canLinkConverterOrValidatorBeans(DesignBean targetBean, DesignBean sourceBean) {

        if (sourceBean == null || sourceBean.getInstance() == null) {
            return false;
        }
        return canLinkConverterOrValidatorBeans(targetBean, sourceBean.getInstance().getClass());
    }

    /***
     * methods for using cachedRowSets and CachedRowSetDataProviders
     */
    public static boolean isCachedRowSetDP( Object db ) {
        return ( db instanceof CachedRowSetDataProvider ) ;
    }
    public static boolean isResultSetClass(Class sourceClass) {
        return ResultSet.class.isAssignableFrom(sourceClass) 
        || CachedRowSetDataProvider.class.isAssignableFrom(sourceClass) ;
    }
    
    // For performance improvement. No need to get all the contexts in the project
    private static DesignContext[] getDesignContexts(DesignBean designBean){
        DesignProject designProject = designBean.getDesignContext().getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = designBean.getDesignContext();
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }
             
    /**
     * given a srcBean of type CachedRowSetDataProvider, find it's
     * CachedRowSet designBean.
     * return null if not found.
     */
    public static DesignBean findCachedRowSetBean( DesignBean dpBean) {
        if ( !(dpBean.getInstance() instanceof CachedRowSetDataProvider) ) return null ;
        
        // change sourceBean to the DesignBean for the dataprovider's cachedRowSet property.
        DesignProperty prop = dpBean.getProperty("cachedRowSet") ;
        if ( prop == null ) return null ;
        
        Object rowSetInstance = prop.getValue() ;
        if ( ! (rowSetInstance instanceof RowSet)) return null ;
        
        // now loop through all contexts to find the DesignBean that wraps this
        // CachedRowSet.
        //DesignContext[] contexts = dpBean.getDesignContext().getProject().getDesignContexts();
        DesignContext[] contexts = getDesignContexts(dpBean);
        
        DesignBean rowSetBean ;
        for (int i = 0, n = contexts.length; i < n; i++) {
            rowSetBean = contexts[i].getBeanForInstance(rowSetInstance) ;
            if ( rowSetBean != null ) {
                return rowSetBean ;
            }
        }
        return null ;
    }   
}
