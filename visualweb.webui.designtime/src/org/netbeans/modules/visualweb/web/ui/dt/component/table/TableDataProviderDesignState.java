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
package org.netbeans.modules.visualweb.web.ui.dt.component.table;

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.ObjectArrayDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.ext.DesignBeanExt;
import com.sun.rave.web.ui.component.Checkbox;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.DefaultListModel;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;

/**
 * Data structure to hold design state of a data provider
 * @author Winston Prakash
 */
public class TableDataProviderDesignState {
    
    boolean dataProviderBroken = false;
    private static int newColumnNameCount = 0;
    
    private DesignBean dataProviderBean;
    
    private DefaultListModel selectedColumnListModel = new DefaultListModel();
    private DefaultListModel availableColumnListModel = new DefaultListModel();
    
    Map columnsDesignStates = null;
    
    private TableDataProvider tableDataProvider;
    private ResourceBundle bundle =
            ResourceBundle.getBundle(TableDataProviderDesignState.class.getPackage().getName() + ".Bundle");
    
    /** Creates a new instance of TableDataProviderDesignState */
    public TableDataProviderDesignState(DesignBean modelBean) {
        
        if(modelBean.getInstance()  instanceof TableDataProvider){
            tableDataProvider =  (TableDataProvider) modelBean.getInstance();
        }else if(List.class.isAssignableFrom(modelBean.getBeanInfo().getBeanDescriptor().getBeanClass())){
            List listObject = (List)modelBean.getInstance();
            if(listObject == null){
                listObject = new ArrayList();
            }
            tableDataProvider =  new ObjectListDataProvider(listObject);
            if(modelBean instanceof DesignBeanExt){
                try {
                    java.lang.reflect.Type[] parameterTypes = ((com.sun.rave.designtime.ext.DesignBeanExt) modelBean).getTypeParameters();
                    if (parameterTypes != null && (parameterTypes.length > 0)) {
                        ((com.sun.data.provider.impl.ObjectListDataProvider) tableDataProvider).setObjectType((java.lang.Class) parameterTypes[0]);
                    }
                } catch (ClassNotFoundException exc) {
                    ErrorManager.getDefault().notify(exc);
                }
            }
        }else if(modelBean.getInstance()  instanceof Object[]){
            tableDataProvider = new ObjectArrayDataProvider((Object[])modelBean.getInstance());
        }else{
            throw new IllegalArgumentException(modelBean.getInstanceName() + " " + bundle.getString("NOT_DATA_PROVIDER")); //NOI18N
        }
        
        dataProviderBean = modelBean;
        // Check if this is a broken DP
        try{
            tableDataProvider.getFieldKeys();
        }catch (Exception exc){
            ErrorManager.getDefault().notify(exc);
            dataProviderBroken = true;
        }
    }
    
    public boolean isBroken(){
        return dataProviderBroken;
    }
    
    /**
     * Get the Data Model Bean
     */
    public DesignBean getDataProviderBean(){
        return dataProviderBean;
    }
    
    public String getUniqueColumnName(String baseName){
        int colNameCount = selectedColumnListModel.size() + 1;
        boolean found = false;
        String  newName =  baseName + colNameCount; //NOI18N
        do  {
            if(selectedColumnListModel.contains(newName)){
                found = true;
            }else if(availableColumnListModel.contains(newName)){
                found = true;
            }else{
                found = false;
            }
            if(found){
                newName =  baseName + colNameCount++; //NOI18N
            }
        }while(found);
        return newName;
    }
    
    /**
     * Set the available column vector
     */
    public void setSelectedColumnListModel(DefaultListModel listModel){
        selectedColumnListModel =  listModel;
    }
    
    /**
     * Get the Selected table column information
     */
    public DefaultListModel getSelectedColumnListModel(){
        return selectedColumnListModel;
    }
    
    /**
     * Set the available column vector
     */
    public void setAvailableColumnListModel(DefaultListModel listModel){
        availableColumnListModel =  listModel;
    }
    
    /**
     * Get the Selected table column information
     */
    public DefaultListModel getAvailableColumnListModel(){
        return availableColumnListModel;
    }
    
    /**
     * Add the TableColumnDesignState to the selectedColumnsDesignStates design state
     * The name is added to the selected column list model.
     */
    public void addColumnDesignStates(TableColumnDesignState colDesignState){
        if(columnsDesignStates == null) columnsDesignStates = new HashMap();
        columnsDesignStates.put(colDesignState.getName(), colDesignState);
        selectedColumnListModel.addElement(colDesignState.getName());
    }
    
    /*
     * Set the Table Column Design states
     */
    public void setColumnDesignStates(Map colDesignStates){
        columnsDesignStates = colDesignStates;
    }
    
   /*
    * Get the Table Column Design states
    */
    public Map getColumnDesignStates(){
        return columnsDesignStates;
    }
    
    public TableColumnDesignState getTableColumnDesignState(String columnName){
        if(columnsDesignStates != null){
            TableColumnDesignState tableColumnDesignState =  (TableColumnDesignState) columnsDesignStates.get(columnName);
            return tableColumnDesignState;
        }else{
            return null;
        }
    }
    
    /**
     * Remove or add items from Selected Column List and Available Column List
     */
    public void setSelectedColumnNames(Vector selectedColumnNames){
        for(int i=0; i < selectedColumnNames.size(); i++){
            selectedColumnListModel.addElement(selectedColumnNames.get(i));
        }
    }
    
    /**
     * Get the Selected Column
     */
    public Vector getSelectedColumnNames(){
        Vector columnNames = new Vector();
        for (int i=0; i< selectedColumnListModel.size(); i++){
            //String columnName = selectedColumnListModel.getElementAt(i).toString();
            columnNames.add(selectedColumnListModel.getElementAt(i));
        }
        return columnNames;
    }
    
    /**
     * Get the Selected Column
     */
    public Vector getAvailableColumnNames(){
        Vector columnNames = new Vector();
        for (int i=0; i< availableColumnListModel.size(); i++){
            //String columnName = selectedColumnListModel.getElementAt(i).toString();
            columnNames.add(availableColumnListModel.getElementAt(i));
        }
        return columnNames;
    }
    
    /**
     * Populate the Selected Column. All the columns of the data model are
     * selected by defaullt
     */
    public void initialize(){
        if (dataProviderBroken) return;
        
        
        
        FieldKey[] columns = tableDataProvider.getFieldKeys();
        
        if((columns != null) && (columns.length > 0)){
            if(columnsDesignStates == null){
                columnsDesignStates = new HashMap();
                // Populate the selected column list and create corresponding TableColumnDesignState
                for (int i=0; i< columns.length; i++){
                    //Skip FieldKey of type "Class" - 6309491
                    if((tableDataProvider.getType(columns[i]) != null) && tableDataProvider.getType(columns[i]).toString().indexOf("java.lang.Class") == -1){
                        String columnName = columns[i].getDisplayName();
                        selectedColumnListModel.addElement(columnName);
                        TableColumnDesignState tableColumnDesignState = new TableColumnDesignState(columnName);
                        tableColumnDesignState.setColumnType(tableDataProvider.getType(columns[i]));
                        if(tableColumnDesignState.getColumnType().isAssignableFrom(Boolean.class)){
                            tableColumnDesignState.setChildType(Checkbox.class);
                        }
                        columnsDesignStates.put(columnName, tableColumnDesignState);
                    }
                }
            }else{
                // Populate the available column list and create the corresponding TableColumnDesignState
                for (int i=0; i< columns.length; i++){
                    //Skip FieldKey of type "Class" - 6309491
                    if(tableDataProvider.getType(columns[i]).toString().indexOf("java.lang.Class") == -1){
                        String columnName = columns[i].getDisplayName();
                        if(!selectedColumnListModel.contains(columnName)){
                            availableColumnListModel.addElement(columnName);
                            TableColumnDesignState tableColumnDesignState = new TableColumnDesignState(columnName);
                            tableColumnDesignState.setColumnType(tableDataProvider.getType(columns[i]));
                            if(tableColumnDesignState.getColumnType().isAssignableFrom(Boolean.class)){
                                tableColumnDesignState.setChildType(Checkbox.class);
                            }
                            columnsDesignStates.put(columnName, tableColumnDesignState);
                        }
                    }
                }
                
            }
        }
        
    }
}
