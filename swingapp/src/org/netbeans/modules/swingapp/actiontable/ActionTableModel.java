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
package org.netbeans.modules.swingapp.actiontable;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.swingapp.*;
import org.openide.util.NbBundle;
// End of variables declaration                   

public class ActionTableModel extends AbstractTableModel {
    
    private static String getLocalizedString(String key) {
        return NbBundle.getMessage(ActionTableModel.class, "ActionTableModel."+key);
    }
    
    final String[] columnNames = new String[]{
        getLocalizedString("header.name"),
        getLocalizedString("header.text"),
        getLocalizedString("header.accelerator"),
        getLocalizedString("header.class"),
        getLocalizedString("header.method"),
        getLocalizedString("header.icon"),
        getLocalizedString("header.task") 
    };
    final Class[] columnClasses = new Class[] {
        String.class, String.class, String.class, 
        String.class, String.class, 
        Icon.class, Boolean.class};
    
    
    private List<ProxyAction> actions;

    public static int ICON_COLUMN = 5;
    public static int TASK_COLUMN = 6;
    public static int METHOD_COLUMN = 4;
    
    public ActionTableModel(List<ProxyAction> actions) {
        super();
        this.actions = actions;
    }
    
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }
    
    public int getColumnCount() {
        return columnClasses.length;
    }
    
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
    
    public int getRowCount() {
        return actions.size();
    }
    
    public ProxyAction getAction(int row) {
        return actions.get(row);
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProxyAction act = actions.get(rowIndex);
        if (columnIndex == 0) {
            return act.getId();
        }
        if (columnIndex == 1) {
            return act.getValue(ProxyAction.NAME);
        }
        if (columnIndex == 2) {
            StringBuffer sb = new StringBuffer();
            KeyStroke key = (KeyStroke) act.getValue(ProxyAction.ACCELERATOR_KEY);
            if(key == null) { return null; }
            if ((key.getModifiers()  & InputEvent.META_DOWN_MASK) > 0) { sb.append("Meta-"); }
            if ((key.getModifiers()  & InputEvent.ALT_DOWN_MASK) > 0) { sb.append("Alt-"); }
            if ((key.getModifiers()  & InputEvent.CTRL_DOWN_MASK) > 0) { sb.append("Ctrl-"); }
            if ((key.getModifiers()  & InputEvent.SHIFT_DOWN_MASK) > 0) { sb.append("Shift-"); }
            //sb.append(key.getKeyChar());
            //sb.append(key.getKeyCode());
            sb.append(KeyEvent.getKeyText(key.getKeyCode()));
            //sb.append(":"+key.toString());
            return sb.toString();
        }
        if (columnIndex == 3) {
            return act.getClassname();
        }
        if (columnIndex == METHOD_COLUMN) {
            return act.getMethodName() + "()";
        }
        if (columnIndex == ICON_COLUMN) {
            int iconCount = 0;
            if (act.getValue(ProxyAction.SMALL_ICON) != null) {
                iconCount++;
            }
            return (Icon) act.getValue(ProxyAction.SMALL_ICON);
        }
        if (columnIndex == TASK_COLUMN) {
            return Boolean.valueOf(act.isTaskEnabled());
        }
        if (columnIndex == 7) {
            return "--";
        }
        return "asdf";
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
    
    
    // ========== action specific methods =========
    public void updateAction(ProxyAction action) {
        for(int i=0; i<actions.size(); i++) {
            ProxyAction a = actions.get(i);
            if( a == action) {
                fireTableRowsUpdated(i,i);
                break;
            }
        }
    }

}