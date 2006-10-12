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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.openide.util.NbBundle;

/** 
 * SecurityRoleTableModel.java
 *
 * Table model SecurityRoleTablePanel.
 *
 * @author ptliu
 */
public class SecurityRoleTableModel extends DDBeanTableModel {
    
    private static final String[] columnNames = {
        NbBundle.getMessage(SecurityRoleTableModel.class, "TTL_SecurityRoleName"),
        NbBundle.getMessage(SecurityRoleTableModel.class, "TTL_SecurityRoleDescription")
    };
    
    protected String[] getColumnNames() {
        return columnNames;
    }
    
    public void setValueAt(Object value, int row, int column) {
        SecurityRole role = getSecurityRole(row);
        
        if (column == 0) {
            role.setRoleName((String) value);
        } else if (column == 1) {
            role.setDescription((String) value);
        }
    }
    
    
    public Object getValueAt(int row, int column) {
        SecurityRole role = getSecurityRole(row);
        
        if (column == 0) {
            return role.getRoleName();
        } else if (column == 1) {
            return role.getDefaultDescription();
        }
        
        return null;
    }
    
    public CommonDDBean addRow(Object[] values) {
        try {
            WebApp webApp = (WebApp)getParent();
            SecurityRole role = (SecurityRole) webApp.createBean("SecurityRole");  //NOI18N
            role.setRoleName((String) values[0]);
            role.setDescription((String) values[1]);
            
            int row = webApp.sizeSecurityRole();
            webApp.addSecurityRole(role);         
            getChildren().add(row, role);
            fireTableRowsInserted(row, row);
            
            return role;
        } catch (ClassNotFoundException ex) {
        }
        
        return null;
    }
    
    public void editRow(int row, Object[] values) {
        //try {
        SecurityRole role = getSecurityRole(row);
        role.setRoleName((String) values[0]);
        role.setDescription((String) values[1]);
        
        fireTableRowsUpdated(row,row);
    }
    
    public void removeRow(int row) {
        WebApp webApp = (WebApp)getParent();
        SecurityRole role = getSecurityRole(row);
        webApp.removeSecurityRole(role);
        
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    SecurityRole getSecurityRole(int row) {
        return (SecurityRole) getChildren().get(row);
    }
}
