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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ActivationCfgPropertyModel.java        October 27, 2003, 6:12 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanTableModel;
import org.netbeans.modules.j2ee.sun.share.configbean.MDEjb;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ActivationCfgPropertyModel extends BeanTableModel {

    /* A class implementation comment can go here. */

    /** Creates a new instance of ActivationCfgPropertyModel */
    public ActivationCfgPropertyModel(){
        super();
    }


    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); //NOI18N

    private static final String[] columnNames = {
        bundle.getString("LBL_Activation_Config_Property_Name"),        //NOI18N
        bundle.getString("LBL_Activation_Config_Property_Value")        //NOI18N
    };


    protected String[] getColumnNames() {
        return columnNames;
    }


    public Object getValueAt(int row, int column){
        Object retValue = null;
        ActivationConfigProperty param = 
            (ActivationConfigProperty)getChildren().get(row);
        if(param != null){
            if (column == 0){ 
                retValue = param.getActivationConfigPropertyName();
            } else {
                retValue =  param.getActivationConfigPropertyValue(); 
            }
        }
        return retValue;
    }


    //BeanTableModel Methods
    public Object addRow(Object[] values){
        StorageBeanFactory storageFactory = ((MDEjb) getParent()).getConfig().getStorageFactory();
        ActivationConfigProperty param = storageFactory.createActivationConfigProperty();
        param.setActivationConfigPropertyName((String)values[0]);
        param.setActivationConfigPropertyValue((String)values[1]);
        ((MDEjb)getParent()).addActivationConfigProperty(param);
        getChildren().add(param);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        return param;
    }


    public void editRow(int row, Object[] values){
        ActivationConfigProperty param = 
            (ActivationConfigProperty)getChildren().get(row);
        if(param != null){
            param.setActivationConfigPropertyName((String)values[0]);
            param.setActivationConfigPropertyValue((String)values[1]);
        }
    }


    public void removeRow(int row){
        ((MDEjb)getParent()).removeActivationConfigProperty(
            (ActivationConfigProperty)getChildren().get(row));
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
    }


    public Object[] getValues(int row){
        Object[] values = new Object[2];
        ActivationConfigProperty property =
            (ActivationConfigProperty)getChildren().get(row);
        if(property != null){
            values[0] = (Object)property.getActivationConfigPropertyName();
            values[1] = (Object)property.getActivationConfigPropertyValue();
        }
        return values; 
    }


    //check whether the given object already exists.
    public boolean alreadyExists(Object[] values){
        boolean exists = false;

        if(values != null){
            String name = (String)values[0];
            if(name != null){
                int count = getRowCount();
                ActivationConfigProperty property;
                for(int i=0; i<count; i++){
                    property = (ActivationConfigProperty)getChildren().get(i);
                    if(property != null){
                        if(name.equals(
                                property.getActivationConfigPropertyName())){
                            exists = true;
                            break;
                        }
                    }
                }
            }
        }
        return exists;
    }
}
