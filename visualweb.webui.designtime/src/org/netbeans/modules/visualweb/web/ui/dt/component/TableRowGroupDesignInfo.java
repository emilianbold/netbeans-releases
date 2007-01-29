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

import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableColumn;
import com.sun.rave.web.ui.component.TableRowGroup;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import org.netbeans.modules.visualweb.web.ui.dt.component.customizers.TableBindToDataAction;
import org.netbeans.modules.visualweb.web.ui.dt.component.customizers.TableCustomizerAction;
import org.netbeans.modules.visualweb.web.ui.dt.component.table.TableDesignHelper;
import org.netbeans.modules.visualweb.web.ui.dt.component.table.TableRowGroupDesignState;

/**
 * DesignInfo for the <code>TableRowGroup</code> component. The following behavior is
 * implemented:
 * <ul>
 * <li>Upon component creation, pre-populate with one table coulum.</li>
 * </ul>
 *
 * @author Winston Prakash
 */
public class TableRowGroupDesignInfo extends AbstractDesignInfo {

    private static final String SOURCE_DATA_PROPERTY = "sourceData";

    public TableRowGroupDesignInfo() {
        super(TableRowGroup.class);
    }

    /** {@inheritDoc} */
    public DisplayAction[] getContextItems(DesignBean bean) {
        return new DisplayAction[] {
            new TableCustomizerAction(bean),
            new TableBindToDataAction(bean)
        };
    }

    /**
     * Create Table Row Group Design State with the design bean and save its state
     * which in turn would create default no of table columns with few row of data.
     * {@inheritDoc}
     */
    public Result beanCreatedSetup(DesignBean tableRowGroupBean) {
        DesignProperty rowCountProperty = tableRowGroupBean.getProperty("rows"); //NOI18N
        rowCountProperty.setValue(new Integer(5));
        // Now table automatically created the column header. So may not be required.
        /*if (tableRowGroupBean.getBeanParent().getChildBeanCount() > 0){
            DesignProperty headerProperty = tableRowGroupBean.getProperty("headerText"); //NOI18N
            headerProperty.setValue(DesignMessageUtil.getMessage(TableRowGroupDesignInfo.class,"tableRowGroupHeader.headerText"));
        }*/
        FacesDesignContext fcontext = (FacesDesignContext) tableRowGroupBean.getDesignContext();
        TableRowGroupDesignState tblRowGroupDesignState = new TableRowGroupDesignState(tableRowGroupBean);
        tblRowGroupDesignState.setDataProviderBean(TableDesignHelper.createDefaultDataProvider(tableRowGroupBean.getBeanParent()));
        tblRowGroupDesignState.saveState();
        return Result.SUCCESS;
    }


    /**
     * {@inheritDoc}
     * Accept only TableColumn as Child
     */
    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return childClass.isAssignableFrom(TableColumn.class);
    }

    /**
     * {@inheritDoc}
     * Accept only Table as Parent
     */
    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class parentClass) {
        return parentBean.getInstance().getClass().isAssignableFrom(Table.class);
    }

    /**
     * Accept only Reult Set (may be not required in future) or  TableDataProvider as links
     *
     * {@inheritDoc}
     */
    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        if (TableDataProvider.class.isAssignableFrom(sourceClass)){
            return true;
        }
        return false;
    }

    /**
     * If child bean is a TableDataProvider create TableRowGroupDesignState with the target bean
     * and set TableDataProvider as source bean and then save its state which in turn would create
     * corresponding tables and columns with the data in the TableDataProvider.
     *
     * TBD - if the child bean is TableColumn
     *
     * {@inheritDoc}
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        if (sourceBean.getInstance() instanceof TableDataProvider) {
            // Bug 6333281 - After cancelling rowset previous table binding lost
            // After dropping a CachedRowsetDataProvider, a dialog pops up if
            // already a CachedRowset exists in the session bean. If the user
            // Cancels this dialog, then the rowset is not set and the data provider
            // is deleted. 
            if (sourceBean.getInstance() instanceof CachedRowSetDataProvider){
                CachedRowSetDataProvider cachedRowSetDataProvider = (CachedRowSetDataProvider)sourceBean.getInstance();
                if(cachedRowSetDataProvider.getCachedRowSet() == null){
                    return Result.FAILURE;
                }
            }
            TableRowGroupDesignState ts = new TableRowGroupDesignState(targetBean);
            ts.setDataProviderBean(sourceBean);
            ts.saveState();
            TableDesignHelper.deleteDefaultDataProvider(targetBean);
        }
        return Result.SUCCESS;
    }
    
    /** {@inheritDoc} */
    public Result beanDeletedCleanup(DesignBean bean) {
        TableDesignHelper.deleteDefaultDataProvider(bean);
        return Result.SUCCESS;
    }
    
    
    /**
     * Reset the table row group to use default table if the source data is set to null
     * This could happen if the user deleted the data provider. 
     * @param property The <code>DesignProperty</code> that has changed.
     * @param oldValue Optional oldValue, or <code>null</code> if the
     *  previous value is not known
     */
    public void propertyChanged(DesignProperty property, Object oldValue) {
        String propertyName = property.getPropertyDescriptor().getName();
        if(propertyName.equals(SOURCE_DATA_PROPERTY)){
            if((oldValue != null) && (!property.isModified())) {
                DesignBean tableRowGroupBean = property.getDesignBean();
                TableRowGroupDesignState tblRowGroupDesignState = new TableRowGroupDesignState(tableRowGroupBean);
                tblRowGroupDesignState.setDataProviderBean(TableDesignHelper.createDefaultDataProvider(tableRowGroupBean.getBeanParent()),true);
                tblRowGroupDesignState.saveState();
            }
        }
    }
    
}
