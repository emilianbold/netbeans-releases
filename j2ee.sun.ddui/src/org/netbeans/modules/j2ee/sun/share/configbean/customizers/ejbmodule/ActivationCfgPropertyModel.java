/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
