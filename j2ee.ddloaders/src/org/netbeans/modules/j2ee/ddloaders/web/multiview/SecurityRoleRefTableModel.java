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
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.openide.util.NbBundle;

/**
 * SecurityRoleRefTableModel.java
 *
 * Table model for the SecurityRoleRefTablePanel.
 *
 * @author ptliu
 */
public class SecurityRoleRefTableModel extends DDBeanTableModel{
     
    private WebApp webApp;
    
    private static final String[] columnNames = {
        NbBundle.getMessage(SecurityRoleRefTableModel.class, "TTL_SecurityRoleRefName"),
        NbBundle.getMessage(SecurityRoleRefTableModel.class, "TTL_SecurityRoleRefLink"),
        NbBundle.getMessage(SecurityRoleRefTableModel.class, "TTL_SecurityRoleRefDescription")
    };
    
    protected String[] getColumnNames() {
        return columnNames;
    }
    
    public void setWebApp(WebApp webApp) {
        this.webApp = webApp;
    }
    
    public void setValueAt(Object value, int row, int column) {
        SecurityRoleRef roleRef = getSecurityRoleRef(row);
        
        if (column == 0) {
            roleRef.setRoleName((String) value);
        } else if (column == 1) {
            roleRef.setRoleLink((String) value);
        } else if (column == 2) {
            roleRef.setDescription((String) value);
        }
    }
    
    
    public Object getValueAt(int row, int column) {
        SecurityRoleRef roleRef = getSecurityRoleRef(row);
        
        if (column == 0) {
            return roleRef.getRoleName();
        } else if (column == 1) {
            return roleRef.getRoleLink();
        } else if (column == 2) {
            return roleRef.getDefaultDescription();
        }
        
        return null;
    }
    
    public CommonDDBean addRow(Object[] values) {
        try {
            SecurityRoleRef roleRef = (SecurityRoleRef) webApp.createBean("SecurityRoleRef");  //NOI18N
            roleRef.setRoleName((String) values[0]);
            roleRef.setRoleLink((String) values[1]);
            roleRef.setDescription((String) values[2]);
            
            Servlet servlet = (Servlet) getParent();
            int row = servlet.sizeSecurityRoleRef();
            servlet.addSecurityRoleRef(roleRef);         
            getChildren().add(row, roleRef);
            fireTableRowsInserted(row, row);
            
            return roleRef;
        } catch (ClassNotFoundException ex) {
        }
        
        return null;
    }
    
    public void editRow(int row, Object[] values) {
        //try {
        SecurityRoleRef roleRef = getSecurityRoleRef(row);
        roleRef.setRoleName((String) values[0]);
        roleRef.setRoleLink((String) values[1]);
        roleRef.setDescription((String) values[2]);
        
        fireTableRowsUpdated(row,row);
    }
    
    public void removeRow(int row) {
        Servlet servlet = (Servlet) getParent();
        SecurityRoleRef role = getSecurityRoleRef(row);
        servlet.removeSecurityRoleRef(role);
        
        getChildren().remove(row);
        fireTableRowsDeleted(row, row);
    }
    
    SecurityRoleRef getSecurityRoleRef(int row) {
        return (SecurityRoleRef) getChildren().get(row);
    }
}
