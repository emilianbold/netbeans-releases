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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.CachedRowSetDataProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.DisplayActionSet;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableColumn;
import com.sun.rave.web.ui.component.TableRowGroup;
import java.awt.Image;
import javax.swing.ImageIcon;
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
    
    
    public DisplayActionSet getContextItemsExt(final DesignBean bean) {
        
        return new DisplayActionSet() {
            
            public DisplayAction[] getDisplayActions() {
                return new DisplayAction[] {
                    new TableCustomizerAction(bean),
                    new TableBindToDataAction(bean)
                };
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
                return new ImageIcon(getClass().getResource(AbstractDesignInfo.DECORATION_ICON)).getImage();
            }
            
            public Image getSmallIcon() {
                return new ImageIcon(getClass().getResource(AbstractDesignInfo.DECORATION_ICON)).getImage();
            }
            
            public String getHelpKey() {
                throw new UnsupportedOperationException("Not supported yet."); //NOI18N
            }
            
            
        };
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
            
            TableDataProvider tdp = (TableDataProvider) sourceBean.getInstance();
            FieldKey[] columns = tdp.getFieldKeys();
            if((columns == null) || (columns.length == 0)){
                return Result.FAILURE;
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
