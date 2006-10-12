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
 * PrincipalTableModel.java
 *
 * Created on April 14, 2006, 10:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.sun.share.PrincipalNameMapping;

/**
 *
 * @author Peter Williams
 */
public class PrincipalTableModel extends SecurityMappingTableModel {
    
    /** Principal table can have 1 column (AS 7.0, 8.1) or 2 columns (AS 9.0).
     */
    public PrincipalTableModel(List p, int columns) {
        super(p, columns);
        assert (columns >= 1 && columns <= 2);
    }
    
    /** Model manipulation
     */
    public int addElement(PrincipalNameMapping entry) {
        return super.addElement(entry);
    }
    
    public int replaceElement(PrincipalNameMapping oldEntry, PrincipalNameMapping newEntry) {
        return super.replaceElement(oldEntry, newEntry);
    }
    
    public int removeElement(PrincipalNameMapping entry) {
        return super.removeElement(entry);
    }
    
    public boolean contains(PrincipalNameMapping entry) {
        return super.contains(entry);
    }
    
    public PrincipalNameMapping getElementAt(int rowIndex) {
        return (PrincipalNameMapping) super.getRowElement(rowIndex);
    }
    
    /** TableModel interface methods
     */
    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return SecurityRoleMappingCustomizer.customizerBundle.getString("LBL_PrincipalName"); // NOI18N
            case 1:
                return SecurityRoleMappingCustomizer.customizerBundle.getString("LBL_ClassName"); // NOI18N
        }
        return null;
    }

    /** SecurityMappingTableModel methods
     */
    protected Object getColumnValueFromRow(Object rowEntry, int columnIndex) {
        Object result = null;
        PrincipalNameMapping entry = (PrincipalNameMapping) rowEntry;
        if(columnIndex == 0) {
            result = entry.getPrincipalName();
        } else {
            result = entry.getClassName();
        }
        return result;
    }
}
