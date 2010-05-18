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
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupTableDesignInfo;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.component.Table;
import com.sun.rave.web.ui.component.TableRowGroup;
import java.awt.Image;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import org.netbeans.modules.visualweb.web.ui.dt.component.customizers.TableBindToDataAction;
import org.netbeans.modules.visualweb.web.ui.dt.component.customizers.TableCustomizerAction;
import org.netbeans.modules.visualweb.web.ui.dt.component.table.TableDesignHelper;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import org.netbeans.modules.visualweb.web.ui.dt.component.table.TableDesignState;
import com.sun.rave.web.ui.model.DefaultTableDataProvider;
import java.io.StringWriter;
import javax.swing.ImageIcon;
import org.w3c.dom.Element;

/**
 * DesignInfo for the <code>Table</code> component. The following behavior is
 * implemented:
 * <ul>
 * <li>Upon component creation, pre-populate with one Table Row group.</li>
 * </ul>
 *
 * @author Winston Prakash
 */
public class TableDesignInfo extends AbstractDesignInfo implements MarkupTableDesignInfo {
    
    //private static final String TITLE_FACET = "title"; //NOI18N
    //private static final String TITLE_FACET_TEXT_PROPERTY = "text"; //NOI18N
    private static final String SOURCE_DATA_PROPERTY = "sourceData"; //NOI18N
    private static final String TITLE_PROPERTY = "title"; //NOI18N
    private static final String AUGMENT_TITLE_PROPERTY = "augmentTitle"; //NOI18N
    private static final String WIDTH_PROPERTY = "width"; //NOI18N
    
    public TableDesignInfo() {
        super(Table.class);
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
     * {@inheritDoc}
     * Accept only TableRowGroup as Child
     */
    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return childClass.isAssignableFrom(TableRowGroup.class) || childClass.isAssignableFrom(StaticText.class);
    }
    
    /**
     * Create TableDesignState with the table bean and a TableRowGroup and save it as source bean and
     * then save the state of TableDesignState which in turn would fill the TableRowRroup with default
     * data.
     *
     * {@inheritDoc}
     */
    public Result beanCreatedSetup(DesignBean tableBean) {
        FacesDesignContext fcontext = (FacesDesignContext) tableBean.getDesignContext();
        String tableTitle = DesignMessageUtil.getMessage(TableDesignInfo.class,"table.title");
        DesignProperty titleProperty = tableBean.getProperty(TITLE_PROPERTY); //NOI18N
        titleProperty.setValue(tableTitle);
        // By default do not augment the title with row information
        DesignProperty augmentTitleProperty = tableBean.getProperty(AUGMENT_TITLE_PROPERTY); //NOI18N
        augmentTitleProperty.setValue(new Boolean(false));
        
        DesignBean tableGroupBean = fcontext.createBean(TableRowGroup.class.getName(), tableBean, null);
        TableDesignState ts = new TableDesignState(tableBean);
        // Create the Default Table Model. It will be used by TableRowGroup to populate itself
        ts.setDataProviderBean(TableDesignHelper.createDefaultDataProvider(tableBean));
        ts.saveState();
        return Result.SUCCESS;
    }
    
    /**
     * Find if the row group of the pasted table is bound to default model.
     * If the model doesn't exist recreate it.
     * @param bean The bean that has been pasted
     */
    public Result beanPastedSetup(DesignBean tableBean) {
        for(int i=0; i< tableBean.getChildBeanCount(); i++){
            DesignBean tableRowGroupBean = tableBean.getChildBean(i);
            if (tableRowGroupBean.getInstance() instanceof TableRowGroup){
                DesignProperty designProperty = tableRowGroupBean.getProperty(SOURCE_DATA_PROPERTY);
                if(designProperty != null){
                    String sourceDataStr = designProperty.getValueSource();
                    if(sourceDataStr != null) {
                        if(sourceDataStr.indexOf(TableDesignHelper.DEFAULT_TABLE_DATA_PROVIDER) != -1){
                            DesignBean dpBean = TableDesignHelper.createDefaultDataProvider(tableBean);
                            FacesDesignContext fcontext = (FacesDesignContext) tableBean.getDesignContext();
                            String modelBindingExpr = fcontext.getBindingExpr(dpBean);
                            designProperty.setValueSource(modelBindingExpr);
                        }
                    }
                }
            }
        }
        return Result.SUCCESS;
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
     * If the child bean is a TableDataProvider create TableDesignState with the target bean
     * and set TableDataProvider as source bean and then save its state which in turn would create
     * corresponding TableRowRroup and fill it with data from the TableDataProvider.
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
            
            TableDesignState ts = new TableDesignState(targetBean);
            ts.loadState();
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
    
    // ------ Implements MarkupTableDesignInfo ------------------------
    
    public int testResizeRow(MarkupDesignBean mdBean, int row, int column, int height) {
        return -1;
    }
    
    public Result resizeRow(MarkupDesignBean mdBean, int row, int height) {
        return Result.SUCCESS;
    }
    
    public Result clearRowSize(MarkupDesignBean mdBean, int row) {
        return Result.SUCCESS;
    }
    
    public int testResizeColumn(MarkupDesignBean mdBean, int row, int column, int width) {
        return width;
    }
    
    public Result resizeColumn(MarkupDesignBean tableBean, int colNo, int columnWidth) {
        //System.out.println("\n\nAdjusting Width of column  -  " + colNo);
        if (tableBean.getInstance() instanceof Table){
            DesignBean tableColumnBean = TableDesignHelper.getTableColumnBean(TableDesignHelper.getTableRowGroupBean(tableBean), colNo);
            DesignProperty designProperty = tableColumnBean.getProperty("width");
            int oldColumnWidth = -1;
            try{
                oldColumnWidth = Integer.parseInt((String)designProperty.getValue());
            }catch(Exception exc){
            }
            designProperty.setValue(String.valueOf(columnWidth));
        }
        return Result.SUCCESS;
    }
    
    
    
    public Result clearColumnSize(MarkupDesignBean mdBean, int column) {
        return Result.SUCCESS;
    }
    
    
}
