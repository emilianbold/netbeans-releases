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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.DropDown;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.Message;
import com.sun.rave.web.ui.component.RadioButton;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.Hyperlink;
import com.sun.rave.web.ui.component.ImageHyperlink;
import org.openide.util.NbBundle;

/**
 * Design state of each column in the TableRowGroup
 * @author Winston Prakash
 */
public class TableColumnDesignState {

    private static final String ID_PROPERTY = "id";
    private static final String TEXT_PROPERTY = "text";
    private static final String LABEL_PROPERTY = "label";
    private static final String HEADER_TEXT_PROPERTY = "headerText";
    private static final String FOOTER_TEXT_PROPERTY = "footerText";
    private static final String HORIZONTAL_ALIGN_PROPERTY = "align";
    private static final String VERTICAL_ALIGN_PROPERTY = "valign";
    private static final String SORT_PROPERTY = "sort";
    private static final String WIDTH_PROPERTY = "width";
    private static final String ITEMS_PROPERTY = "items";
    private static final String URL_PROPERTY = "url";
    private static final String SELECTED_PROPERTY = "selected";

    private String columnName = null;
    private String columnHeader = null;
    private String columnFooter = null;
    private String horizontalAlign = null;
    private String verticalAlign = null;
    private String valueExpression = null;
    private String sourceVariable = TableRowGroupDesignState.sourceVarNameBase;

    private DesignBean tableColumnBean = null;
    private Class childBeanType = StaticText.class;

    private boolean sortable = true;
    private String columnWidth = null;

    private Class columnType = String.class;

    public TableColumnDesignState(String colName, String colHeader, String valExpression){
        columnName =  colName;
        columnHeader = colHeader;
        valueExpression = valExpression;
    }

    public TableColumnDesignState(String colName){
        columnName =  colName;
        if(colName.lastIndexOf(".") > 0){	
             columnHeader = columnName.substring(columnName.lastIndexOf(".")+1);
         }else{	
             columnHeader = columnName;	
         }
        valueExpression = "#{" + sourceVariable  + ".value['" + columnName + "']" + "}";
    }

    /** Creates a new instance of TableColumnDesignState */
    public TableColumnDesignState(DesignBean tblColumnBean) {
        tableColumnBean = tblColumnBean;
    }

    public void loadState(){
        if (tableColumnBean.getChildBeanCount() > 0){
            DesignBean tableColumnChildBean = tableColumnBean.getChildBean(0);
            Object instance = tableColumnChildBean.getInstance();
            childBeanType = instance.getClass();

            // Get the Colum Name from the text value source of the child bean
            DesignProperty valueProperty = getValueDesignProperty(tableColumnChildBean);
            if(valueProperty != null ){
                valueExpression = valueProperty.getValueSource();
                if((valueExpression != null) && valueExpression.startsWith("#{")){
                    // XXX Revisit. For the time being return value enclosed with in [' & ']
                    int startIndex = valueExpression.indexOf('[');
                    int endIndex = valueExpression.indexOf(']');
                    if ((startIndex != -1) && (endIndex != -1)){
                        columnName =  valueExpression.substring(startIndex + 2, endIndex - 1);
                    }
                }
            }
        }
        if(columnName == null){
            columnName = getStringPropertyValue(ID_PROPERTY);
        }
        
        // Get the header text
        columnFooter = getStringPropertyValue(FOOTER_TEXT_PROPERTY);
        columnHeader = getStringPropertyValue(HEADER_TEXT_PROPERTY);
        horizontalAlign = getStringPropertyValue(HORIZONTAL_ALIGN_PROPERTY);
        verticalAlign = getStringPropertyValue(VERTICAL_ALIGN_PROPERTY);
        
        String sort = getStringPropertyValue(SORT_PROPERTY);
        if(sort != null){
            sortable = true;
        }else{
            sortable = false;
        }
        
        columnWidth = getStringPropertyValue(WIDTH_PROPERTY);
    }
    
    /**
     * Clear all the property values set to this state
     */
    public void clearProperties(){
        columnFooter = null;
        columnHeader = null;
        horizontalAlign = null;
        verticalAlign = null;
        valueExpression = null;
        sortable = true;
    }
    
    /**
     * Get int property value
     */
    private int getIntegerPropertyValue(String propertyname){
        Object value = getPropertyValue(propertyname);
        if(value != null){
            return ((Integer)value).intValue();
        }else{
            return -1;
        }
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
        DesignProperty designProperty = tableColumnBean.getProperty(propertyname);
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
        DesignProperty designProperty = tableColumnBean.getProperty(propertyname);
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
            DesignProperty designProperty = tableColumnBean.getProperty(propertyname);
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
        DesignProperty designProperty = tableColumnBean.getProperty(propertyname);
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
        DesignProperty designProperty = tableColumnBean.getProperty(propertyname);
        if(designProperty != null){
            designProperty.unset();
        }
    }
    
    private DesignProperty getValueDesignProperty(DesignBean tblColumnChildBean){
        DesignProperty valueProperty = null;
        if((tblColumnChildBean.getInstance() instanceof Button)){
            valueProperty = tblColumnChildBean.getProperty(TEXT_PROPERTY); //NOI18N
        }else if((tblColumnChildBean.getInstance() instanceof Checkbox)){
            valueProperty = tblColumnChildBean.getProperty(SELECTED_PROPERTY); //NOI18N
        }else if  ((tblColumnChildBean.getInstance() instanceof RadioButton)){
            valueProperty = tblColumnChildBean.getProperty(LABEL_PROPERTY); //NOI18N
        }else if(tblColumnChildBean.getInstance() instanceof DropDown){
            valueProperty = tblColumnChildBean.getProperty(SELECTED_PROPERTY); //NOI18N
        }else if(tblColumnChildBean.getInstance() instanceof ImageComponent){
            valueProperty = tblColumnChildBean.getProperty(URL_PROPERTY); //NOI18N
        }else if  ((tblColumnChildBean.getInstance() instanceof TextField)){
            valueProperty = tblColumnChildBean.getProperty(TEXT_PROPERTY); //NOI18N
        }else if  ((tblColumnChildBean.getInstance() instanceof TextArea)){
            valueProperty = tblColumnChildBean.getProperty(TEXT_PROPERTY); //NOI18N
        }else if ((tblColumnChildBean.getInstance() instanceof StaticText)){
            valueProperty = tblColumnChildBean.getProperty(TEXT_PROPERTY); //NOI18N
        }else if  ((tblColumnChildBean.getInstance() instanceof Label)){
            valueProperty = tblColumnChildBean.getProperty(TEXT_PROPERTY); //NOI18N
        }else if  ((tblColumnChildBean.getInstance() instanceof Message)){
            valueProperty = tblColumnChildBean.getProperty(TEXT_PROPERTY); //NOI18N
        }else if((tblColumnChildBean.getInstance() instanceof Hyperlink)){
            valueProperty = tblColumnChildBean.getProperty(TEXT_PROPERTY); //NOI18N
        }else if((tblColumnChildBean.getInstance() instanceof ImageHyperlink)){
            valueProperty = tblColumnChildBean.getProperty(TEXT_PROPERTY); //NOI18N
        }
        
        return valueProperty;
    }
    
    /*
     * Set the table column bean to this design state
     */
    public void setTableColumnBean(DesignBean tblColumnBean){
        tableColumnBean = tblColumnBean;
    }
    
    /*
     * Get the table column bean of this design state
     */
    public DesignBean getTableColumnBean(){
        return tableColumnBean;
    }
    
    /**
     * Set the value expression
     */
    public void setValueExpression(String expression){
        valueExpression = expression;
    }
    
    /**
     * Set the value expression
     */
    public String getValueExpression(){
        return valueExpression;
    }
    
    /**
     * Set the Source Variable
     */
    public void setSourceVariable(String srcVariable){
        sourceVariable = srcVariable;
    }
    
    /**
     * Get the Source Variable
     */
    public String getSourceVariable(){
        return sourceVariable;
    }
    
    /**
     * Set the type of the Child bean that this container hold
     */
    
    public void setChildType(Class childType){
        childBeanType = childType;
    }
    
    /**
     * Get the type of the child bean
     */
    public Class getChildType(){
        return childBeanType;
    }
    
    /**
     * Get the column type.
     */
    public Class getColumnType() {
        return this.columnType;
    }
    
    /**
     * Set the column type.
     */
    public void setColumnType(Class columnType) {
        this.columnType = columnType;
    }
    
    /**
     * Get the columns name (usually the table data provider filed key)
     */
    public String getName(){
        return columnName;
    }
    
    /**
     * Set the columns name (usually the table data provider filed key)
     */
    public void setName(String name){
        columnName = name;
    }
    
    /**
     * Get the columns header text (Could be same as the data provider filed key)
     */
    public String getHeader(){
        return columnHeader;
    }
    
    /**
     * Set the columns header text (Could be same as the data provider filed key)
     */
    public void setHeader(String header){
        columnHeader = header;
    }
    
    /**
     * Get the column footer  text
     */
    public String getFooter(){
        return columnFooter;
    }
    
    /**
     * Set the column footer  text
     */
    public void setFooter(String footer){
        columnFooter = footer;
    }
    
    /**
     * Set the horizontal align
     */
    public void setHorizontalAlign(String horizAlign){
        horizontalAlign = horizAlign;
    }
    
    /**
     * get the horizontal align
     */
    public String getHorizontalAlign(){
        return horizontalAlign;
    }
    
    /**
     * Set the vertical align
     */
    public void setVerticalAlign(String vertAlign){
        verticalAlign = vertAlign;
    }
    
    /**
     * Get the vertical align
     */
    public String getVerticalAlign(){
        return verticalAlign;
    }
    
    /**
     * Set if sortable
     */
    public void setSortable(boolean sort){
        sortable = sort;
    }
    
    /**
     * Get if sortable
     */
    public boolean isSortable(){
        return sortable;
    }
    
    /**
     * Set column width
     */
    public void setWidth(String width){
        setWidth(width, false);
    }
    /**
     * Set column width
     * immediat - immediatly persist the value (designer refreshed)
     */
    public void setWidth(String width, boolean immediat){
        columnWidth = width;
        if(immediat){
            setPropertyValue(WIDTH_PROPERTY, String.valueOf(columnWidth));
        }
    }
    
    /**
     * Get column width
     */
    public String getWidth(){
        return columnWidth;
    }
    
    public void saveState(){
        // Create the Child bean and set its text property
        FacesDesignContext fcontext = (FacesDesignContext) tableColumnBean.getDesignContext();
        DesignBean tableColumnChildBean = null;
        if(tableColumnBean.getChildBeanCount() > 0){
            tableColumnChildBean = tableColumnBean.getChildBean(0);
            if(!tableColumnChildBean.getInstance().getClass().isAssignableFrom(childBeanType)){
                fcontext.deleteBean(tableColumnChildBean);
                tableColumnChildBean = fcontext.createBean(childBeanType.getName(), tableColumnBean, null);
            }
        }else{
            tableColumnChildBean = fcontext.createBean(childBeanType.getName(), tableColumnBean, null);
        }
        DesignProperty valueProperty = getValueDesignProperty(tableColumnChildBean);
        if(valueProperty != null){
            if(valueExpression == null){
                if((columnName != null) && (sourceVariable != null)){
                    valueExpression = "#{" + sourceVariable  + ".value['" + columnName + "']" + "}";
                }
            }
            valueProperty.setValue(valueExpression);
        }
        
        // If the child is a DropDown and its items property is not set then set it to valueexpression
        if(tableColumnChildBean.getInstance() instanceof DropDown){
            DesignProperty itemsProperty = tableColumnChildBean.getProperty(ITEMS_PROPERTY);
            if((valueExpression != null) && (itemsProperty != null) && (itemsProperty.getValueSource() == null)){
                itemsProperty.setValue(valueExpression);
            }
        }
        
        // Set the properties of the Table Column bean
        setPropertyValue(FOOTER_TEXT_PROPERTY, columnFooter);
        setPropertyValue(HEADER_TEXT_PROPERTY, columnHeader);
        setPropertyValue(HORIZONTAL_ALIGN_PROPERTY, horizontalAlign);
        setPropertyValue(VERTICAL_ALIGN_PROPERTY, verticalAlign);
        
        if(sortable){
            if(isSortAllowed(valueExpression, columnName)){
                setPropertyValue(SORT_PROPERTY, columnName);
            }
        }else{
            unsetPropertyValue(SORT_PROPERTY);
        }
        setPropertyValue(WIDTH_PROPERTY, columnWidth);
    }
    
    public boolean isSortAllowed(){
        return isSortAllowed(valueExpression, columnName);
    }
    
    private boolean isSortAllowed(String valueExpression, String colName){
        if (DropDown.class.isAssignableFrom(childBeanType)){
            return false;
        }
        if((valueExpression != null) && valueExpression.trim().startsWith("#{")){
            int startIndex = valueExpression.indexOf('[');
            int endIndex = valueExpression.indexOf(']');
            if ((startIndex != -1) && (endIndex != -1)){
                String columnName1 =  valueExpression.substring(startIndex + 2, endIndex - 1);
                if(columnName.trim().equals(columnName1.trim())){
                    return true;
                }
            }
        }
        return false;
    }
}
