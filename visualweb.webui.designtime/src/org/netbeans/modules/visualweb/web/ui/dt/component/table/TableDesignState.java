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

import com.sun.data.provider.TableDataProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignBean;
import com.sun.rave.designtime.faces.FacesDesignContext;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;
import com.sun.rave.web.ui.component.*;

/**
 * This class defines the design time state of the Table Component
 *
 * @author Winston Prakash
 */
public class TableDesignState {

    private static final String PAGINATION_CONTROLS_PROPERTY = "paginationControls";
    private static final String PAGINATION_BUTTON_PROPERTY = "paginateButton";
    private static final String TITLE_PROPERTY = "title";
    private static final String SUMMARY_PROPERTY = "summary";
    private static final String FOOTER_TEXT_PROPERTY = "footerText";
    private static final String DESELECT_MULTIPLE_BUTTON_PROPERTY = "deselectMultipleButton";
    private static final String SELECT_MULTIPLE_BUTTON_PROPERTY = "selectMultipleButton";
    private static final String CLEAR_TABLE_SORT_BUTTON_PROPERTY = "clearSortButton";
    private static final String SORT_PANEL_TOGGLE_BUTTON_PROPERTY = "sortPanelToggleButton";

    private DesignBean tableBean;
    private DesignBean tableRowGroupBean = null;

    private FacesDesignContext fcontext = null;

    private TableRowGroupDesignState tableRowGroupDesignState = null;

    private boolean showPaginationControls = false;
    private boolean showPaginationButton = false;
    private boolean deselectMultipleButtonShown = false;
    private boolean selectMultipleButtonShown = false;
    private boolean clearTableSortButtonShown = false;
    private boolean sortPanelToggleButtonShown = false;

    private String tableTitle = null;
    private String tableSummary = null;
    private String footerText = null;

    /** Creates a new instance of TableDesignState */
    public TableDesignState(DesignBean tblBean) {
        tableBean = tblBean;
        fcontext = (FacesDesignContext) tableBean.getDesignContext();
    }

    /** Creates a new instance of TableDesignState */
    public TableDesignState(DesignBean tblBean, DesignBean tblRowGroupBean) {
        this(tblBean);
        tableRowGroupBean = tblRowGroupBean;
    }

    /**
     * Load the current design time state of the Table component
     */
    public void loadState(){
        if(tableRowGroupBean == null) tableRowGroupBean = getTableRowGroupBean();
        tableRowGroupDesignState = new TableRowGroupDesignState(tableRowGroupBean);
        tableRowGroupDesignState.loadSourceVariable();

        tableTitle = getStringPropertyValue(TITLE_PROPERTY);
        tableSummary = getStringPropertyValue(SUMMARY_PROPERTY);
        footerText = getStringPropertyValue(FOOTER_TEXT_PROPERTY);

        showPaginationControls = getBooleanPropertyValue(PAGINATION_CONTROLS_PROPERTY);
        showPaginationButton = getBooleanPropertyValue(PAGINATION_BUTTON_PROPERTY);
        deselectMultipleButtonShown = getBooleanPropertyValue(DESELECT_MULTIPLE_BUTTON_PROPERTY);
        selectMultipleButtonShown = getBooleanPropertyValue(SELECT_MULTIPLE_BUTTON_PROPERTY);
        clearTableSortButtonShown = getBooleanPropertyValue(CLEAR_TABLE_SORT_BUTTON_PROPERTY);
        sortPanelToggleButtonShown = getBooleanPropertyValue(SORT_PANEL_TOGGLE_BUTTON_PROPERTY);
    }

    /**
     * Find the first TableRowGroup bean. If not found create one
     */
    public DesignBean getTableRowGroupBean(){
        DesignBean tableRowGroupBean = null;
        int childCount = tableBean.getChildBeanCount();
        for(int i=0; i< childCount; i++){
            tableRowGroupBean = tableBean.getChildBean(i);
            if (tableRowGroupBean.getInstance() instanceof TableRowGroup){
                break;
            }
        }
        if(tableRowGroupBean == null){
            tableRowGroupBean = fcontext.createBean(TableRowGroup.class.getName(), tableBean, null);
        }
        return tableRowGroupBean;
    }
    
    /**
     * Set the TableRowGroup design State
     */
    public TableRowGroupDesignState getTableRowGroupDesignState(){
        if(tableRowGroupDesignState == null){
            tableRowGroupDesignState = new TableRowGroupDesignState(getTableRowGroupBean());
            tableRowGroupDesignState.loadSourceVariable();
        }
        return tableRowGroupDesignState;
    }
    
    /**
     * Clear all the property values set to this state
     */
    public void clearProperties(){
        showPaginationControls = false;
        showPaginationButton = false;
        deselectMultipleButtonShown = false;
        selectMultipleButtonShown = false;
        clearTableSortButtonShown = false;
        sortPanelToggleButtonShown = false;
        tableTitle = null;
        tableSummary = null;
        footerText = null;
    }
    
    /**
     * Get the boolean value of the property
     */
    public boolean getBooleanPropertyValue(String propertyname){
        boolean value = false;
        Object propValue = getPropertyValue(propertyname);
        if(propValue != null){
            value = ((Boolean) propValue).booleanValue();
        }
        return value;
    }
    
    /**
     * Get String property value
     */
    private String getStringPropertyValue(String propertyname){
        Object value = getPropertyValue(propertyname);
        if(value != null){
            return value.toString();
        }else{
            return null;
        }
    }
    
    /**
     * Load the property value from the bean to this state
     */
    private Object getPropertyValue(String propertyname){
        Object propertyValue = null;
        DesignProperty designProperty = tableBean.getProperty(propertyname);
        if(designProperty != null){
            if(designProperty.getValue() != null){
                propertyValue = designProperty.getValue();
            }
        }
        return propertyValue;
    }
    
    /**
     * Get the property value source from the bean to this state
     */
    private String getPropertyValueSource(String propertyname){
        String propertyValue = null;
        DesignProperty designProperty = tableBean.getProperty(propertyname);
        if(designProperty != null){
            propertyValue = designProperty.getValueSource();
        }
        return propertyValue;
    }
    
    /**
     * Set the value to the bean property as stored in this state
     */
    private void setPropertyValue(String propertyname, Object value){
        if(value != null){
            DesignProperty designProperty = tableBean.getProperty(propertyname);
            if(designProperty != null){
                Object origValue = getPropertyValue(propertyname);
                if(value != origValue){
                    if((value instanceof String) && value.toString().equals("")){
                        designProperty.unset();
                    }else{
                        designProperty.setValue(value);
                    }
                }
            }
        }
    }
    
    /**
     * Set a boolean value to the property
     */
    private void setBooleanPropertyValue(String propertyname, boolean value){
        DesignProperty designProperty = tableBean.getProperty(propertyname);
        if(designProperty != null){
            boolean origValue = getBooleanPropertyValue(propertyname);
            if(origValue != value){
                if(value){
                    designProperty.setValue(new Boolean(true));
                }else{
                    designProperty.unset();
                }
            }
        }
    }
    
    /**
     * Unset the value to the bean property
     */
    private void unsetPropertyValue(String propertyname){
        DesignProperty designProperty = tableBean.getProperty(propertyname);
        if(designProperty != null){
            designProperty.unset();
        }
    }
    
    /**
     * Set the TableRowGroup design State
     */
    public void setTableRowGroupDesignState(TableRowGroupDesignState designState){
        tableRowGroupDesignState = designState;
    }
    
    
    
    /**
     * Set the Data model DesignBeean to this table design state
     * In turn the DataProvider of the TableRowGroup is set
     */
    public void setDataProviderBean(DesignBean dataProviderBean){
        if(!(dataProviderBean.getInstance()  instanceof TableDataProvider)){
            throw new IllegalArgumentException(dataProviderBean.getInstanceName() + " not a table data provider.");
        }
        getTableRowGroupDesignState().setDataProviderBean(dataProviderBean,true);
    }
    
    /**
     * Set the footer text
     */
    public void setFooterText(String footer){
        footerText = footer;
    }
    
    /**
     * get the footer text
     */
    public String getFooterText(){
        return footerText;
    }
    
    /**
     * Set the table title
     */
    public void setTitle(String title){
        tableTitle = title;
    }
    
    /**
     * get the table title
     */
    public String getTitle(){
        return tableTitle;
    }
    
    /**
     * Set the table title
     */
    public void setSummary(String summary){
        tableSummary = summary;
    }
    
    /**
     * get the table title
     */
    public String getSummary(){
        return tableSummary;
    }
    
    /**
     * Set if pagination is enabled
     */
    public void setPaginationEnabled(boolean enabled){
        if(enabled){
            showPaginationControls = true;
            showPaginationButton = true;
            getTableRowGroupDesignState().setPaginated(true);
        }else{
            getTableRowGroupDesignState().setPaginated(false);
            showPaginationControls = false;
            showPaginationButton = false;
        }
    }
    
    /**
     * Get if pagination is enabled
     */
    public boolean isPaginationEnabled(){
        return showPaginationControls;
    }
    
    /**
     * Set if deselect multiple button should be shown
     */
    public void setDeselectMultipleButtonShown(boolean shown){
        deselectMultipleButtonShown = shown;
    }
    
    /**
     * Get if deselect multiple button should be shown
     */
    public boolean isDeselectMultipleButtonShown(){
        return deselectMultipleButtonShown;
    }
    
    /**
     * Set if deselect multiple button should be shown
     */
    public void setSelectMultipleButtonShown(boolean shown){
        selectMultipleButtonShown = shown;
    }
    
    /**
     * Get if deselect multiple button should be shown
     */
    public boolean isSelectMultipleButtonShown(){
        return selectMultipleButtonShown;
    }
    
    /**
     * Set if deselect multiple button should be shown
     */
    public void setClearTableSortButtonShown(boolean shown){
        clearTableSortButtonShown = shown;
    }
    
    /**
     * Get if deselect multiple button should be shown
     */
    public boolean isClearTableSortButtonShown(){
        return clearTableSortButtonShown;
    }
    
    /**
     * Set if deselect multiple button should be shown
     */
    public void setSortPanelToggleButtonShown(boolean shown){
        sortPanelToggleButtonShown = shown;
    }
    
    /**
     * Get if deselect multiple button should be shown
     */
    public boolean isSortPanelToggleButtonShown(){
        return sortPanelToggleButtonShown;
    }
    
    /**
     * Save the table design time state to the design bean
     */
    public void saveState() {
        FacesDesignBean ftableBean = (FacesDesignBean)tableBean;
        setPropertyValue(TITLE_PROPERTY, tableTitle);
        
        setPropertyValue(SUMMARY_PROPERTY, tableSummary);
        setPropertyValue(FOOTER_TEXT_PROPERTY, footerText);
        
        setBooleanPropertyValue(CLEAR_TABLE_SORT_BUTTON_PROPERTY,  clearTableSortButtonShown);
        setBooleanPropertyValue(SELECT_MULTIPLE_BUTTON_PROPERTY, selectMultipleButtonShown);
        setBooleanPropertyValue(DESELECT_MULTIPLE_BUTTON_PROPERTY, deselectMultipleButtonShown);
        setBooleanPropertyValue(SORT_PANEL_TOGGLE_BUTTON_PROPERTY, sortPanelToggleButtonShown);
        
        tableRowGroupDesignState.saveState();
        
        setBooleanPropertyValue(PAGINATION_CONTROLS_PROPERTY, showPaginationControls);
        setBooleanPropertyValue(PAGINATION_BUTTON_PROPERTY, showPaginationButton);
        
    }
}
