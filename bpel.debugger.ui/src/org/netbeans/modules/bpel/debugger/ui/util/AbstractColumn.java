/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.debugger.ui.util;

import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class AbstractColumn extends ColumnModel {

    protected String myId;
    protected String myName;
    protected String myTooltip;
    protected Class myType;
    protected Properties myProperties;
    protected boolean mySortable;

    public AbstractColumn() {
        myProperties = Properties.getDefault().
                getProperties("debugger").getProperties("views"); // NOI18N
        
        mySortable = true;
    }
    
    public String getID() {
        return myId;
    }
    
    public String getDisplayName() {
        return getMessage(myName);
    }
    
    public Class getType() {
        return myType;
    }
    
    @Override
    public String getShortDescription() {
        return getMessage(myTooltip);
    }
    
    @Override
    public int getCurrentOrderNumber() {
        return myProperties.getInt(getFieldId(ORDER_NUMBER), -1);
    }
    
    @Override
    public void setCurrentOrderNumber(
            final int orderNumber) {
        myProperties.setInt(getFieldId(ORDER_NUMBER), orderNumber);
    }

    @Override
    public int getColumnWidth() {
        return myProperties.getInt(getFieldId(COLUMN_WIDTH), WIDTH);
    }

    @Override
    public void setColumnWidth(
            final int columnWidth) {
        myProperties.setInt(getFieldId(COLUMN_WIDTH), columnWidth);
    }

    @Override
    public void setVisible(
            final boolean visible) {
        myProperties.setBoolean(getFieldId(VISIBLE), visible);
    }

    @Override
    public boolean isVisible() {
        return myProperties.getBoolean(getFieldId(VISIBLE), true);
    }

    @Override
    public void setSorted(
            final boolean sorted) {
        myProperties.setBoolean(getFieldId(SORTED), sorted);
    }

    @Override
    public boolean isSorted() {
        return myProperties.getBoolean(getFieldId(SORTED), false);
    }

    @Override
    public void setSortedDescending(
            final boolean sortedDescending) {
        myProperties.setBoolean(getFieldId(DESCENDING), sortedDescending);
    }

    @Override
    public boolean isSortedDescending() {
        return myProperties.getBoolean(getFieldId(DESCENDING), false);
    }

    @Override
    public boolean isSortable() {
        return mySortable;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Private
    private String getFieldId(
            final String value) {
        return getID() + "." + value; // NOI18N
    }

    private String getMessage(
            final String value) {
        if (value == null) {
            return null;
        }

        return NbBundle.getMessage(getClass(), value);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final int WIDTH =
            150;
    
    private static final String SORTED =
            "sorted"; // NOI18N
    
    private static final String DESCENDING =
            "sortedDescending"; // NOI18N
    
    private static final String VISIBLE =
            "visible"; // NOI18N
    
    private static final String COLUMN_WIDTH =
            "columnWidth"; // NOI18N
    
    private static final String ORDER_NUMBER =
            "currentOrderNumber"; // NOI18N
}
