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

package org.netbeans.modules.worklist.wizard2;

import org.netbeans.modules.worklist.wizard.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author radval
 */
public class UsersTableModel extends AbstractTableModel {

        private List<User> users = new ArrayList<User>();
        
        public int getRowCount() {
            return users.size();
        }

        public int getColumnCount() {
            return 1;
        }
        
        public List<User> getUsers() {
            return this.users;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            User user = users.get(rowIndex);
            if(user != null) {
                return user.getUserName();
            }
            
            return "";
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            User user = users.get(rowIndex);
            if(user != null) {
                user.setUserName((String)aValue);
            }
        }
         
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
         
        public void addNewUser(User user) {
            if(user != null) {
                users.add(user);
                fireTableDataChanged();
            }
        }
        
        public void addNewRow() {
            User user = new User("");
            users.add(user);
            fireTableDataChanged();
        }
        
        public void removeRow(int row) {
            if(users.size() > row)  {
                users.remove(row);
            }
            fireTableDataChanged();
        }
        
        
        class User {
            
            private String user;
            
            public User(String user) {
                this.user = user;
            }
            
            public void setUserName(String user) {
                this.user = user;
            }
            
            public String getUserName() {
                return this.user;
            }
            
            public String toString() {
                return this.user;
            }
        }
        
        class Group {
            
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
