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
 * GroupTableModel.java
 *
 * Created on April 14, 2006, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;


/**
 *
 * @author Peter Williams
 */
public class SRMGroupTableModel extends SRMBaseTableModel implements GroupTableModel {
    
    public SRMGroupTableModel(XmlMultiViewDataSynchronizer s, SecurityRoleMapping m) {
        super(s, m);
    }

    /** Model manipulation
     */
    public int addElement(String entry) {
        int index = mapping.addGroupName(entry);
        fireTableRowsInserted(index, index);
        modelUpdatedFromUI();
        return index;
    }
    
    public int replaceElement(String oldEntry, String newEntry) {
        int index = indexOf(oldEntry);
        if(index != -1) {
            mapping.setGroupName(index, newEntry);
            fireTableRowsUpdated(index, index);
            modelUpdatedFromUI();
        }
        return index;
    }
    
    public int removeElement(String entry) {
        int index = indexOf(entry);
        if(index != -1) {
            mapping.removeGroupName(entry);
//            fireTableRowsDeleted(index, index);
            fireTableDataChanged();
            modelUpdatedFromUI();
        }
        return index;
    }

	public void removeElementAt(int index) {
		if(index >= 0 || index < mapping.sizeGroupName())  {
            mapping.removeValue(SecurityRoleMapping.GROUP_NAME, index);
//            fireTableRowsDeleted(index, index);
            fireTableDataChanged();
            modelUpdatedFromUI();
		}
	}
	
	public void removeElements(int[] indices) {
        // !PW FIXME this method has an unwritten requirement that the
        // list of indices passed in is ordered in ascending numerical order.
		if(indices.length > 0) {
            boolean dataChanged = false;
			for(int i = indices.length-1; i >= 0; i--) {
				if(indices[i] >= 0 || indices[i] < mapping.sizeGroupName())  {
                    mapping.removeValue(SecurityRoleMapping.GROUP_NAME, indices[i]);
                    dataChanged = true;
				}
			}
            
            if(dataChanged) {
//                fireTableRowsUpdated(indices[0], indices[indices.length-1]);
                fireTableDataChanged();
                modelUpdatedFromUI();
            }
		}
	}
    
    public boolean contains(String entry) {
        return indexOf(entry) != -1;
    }
    
    public String getElementAt(int rowIndex) {
        String result = null;
        if(rowIndex >= 0 && rowIndex < mapping.sizeGroupName()) {
            result = mapping.getGroupName(rowIndex);
        }
        return result;
    }

    private int indexOf(String entry) {
        String [] names = mapping.getGroupName();
        if(names != null) {
            for(int index = 0; index < names.length; index++) {
                if(Utils.strEquivalent(names[index], entry)) {
                    return index;
                }
            }
        }
        return -1;
    }
    
    /** TableModel interface methods
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        String result = null;
        if(rowIndex >= 0 && rowIndex < mapping.sizeGroupName() && columnIndex == 0) {
            result = mapping.getGroupName(rowIndex);
        } 
        return result;
    }
    
    public int getRowCount() {
        return mapping.sizeGroupName();
    }

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int columnIndex) {
        if(columnIndex == 0) {
            return SecurityRoleMappingPanel.customizerBundle.getString("LBL_GroupName"); // NOI18N
        }
        return null;
    }

}
