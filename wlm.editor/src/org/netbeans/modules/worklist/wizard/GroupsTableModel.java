/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.wizard;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author radval
 */
public class GroupsTableModel extends AbstractTableModel {

        private List<Group> groups = new ArrayList<Group>();
        
        public int getRowCount() {
            return groups.size();
        }

        public int getColumnCount() {
            return 1;
        }

        public List<Group> getGroups() {
            return this.groups;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            Group group = groups.get(rowIndex);
            if(group != null) {
                return group.getGroupName();
            }
            
            return "";
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Group group = groups.get(rowIndex);
            if(group != null) {
                group.setGroupName((String)aValue);
            }
        }
         
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
         
        public void addNewGroup(Group group) {
            if(group != null) {
                groups.add(group);
                fireTableDataChanged();
            }
        }
        
        public void addNewRow() {
            Group group = new Group("");
            groups.add(group);
            fireTableDataChanged();
        }
        
        public void removeRow(int row) {
            if(groups.size() > row)  {
                groups.remove(row);
            }
            fireTableDataChanged();
        }
        
        
        public class Group {
            
            private String group;
            
            public Group(String group) {
                this.group = group;
            }
            
            public void setGroupName(String group) {
                this.group = group;
            }
            
            public String getGroupName() {
                return this.group;
            }
            
            public String toString() {
                return this.group;
            }
        }
}
