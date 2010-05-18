/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.actions.dialog;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.FireEvent;
import org.netbeans.modules.jmx.mbeanwizard.renderer.TextFieldRenderer;
import org.netbeans.modules.jmx.mbeanwizard.table.AttributeTable;

/**
 * Class responsible for the attribute table shown when you use Add Attributes...
 * popup action in the contextual management menu.
 * @author tl156378
 */
public class AddAttributeTable extends AttributeTable {
    
    /**
     * Constructor
     * @param model the table model of this table
     * @param wiz the panel to notify for events
     */
    public AddAttributeTable(AbstractTableModel model, FireEvent wiz) {
        super(model,wiz);
    }
        
   /**
     * Returns the cell renderer for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellRenderer the cell renderer
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        AddMBeanAttributeTableModel addAttrModel = 
                (AddMBeanAttributeTableModel) this.getModel();
        int firstEditable = addAttrModel.getFirstEditable();
        
        if(row < firstEditable) {
            switch (column) {
                case AddMBeanAttributeTableModel.IDX_ATTR_NAME :
                    return new TextFieldRenderer(new JTextField(), true, false);
                case AddMBeanAttributeTableModel.IDX_ATTR_TYPE :
                    return new TextFieldRenderer(new JTextField(),true,false);
                case AddMBeanAttributeTableModel.IDX_ATTR_ACCESS :
                    return new TextFieldRenderer(new JTextField(),true,false);
                case AddMBeanAttributeTableModel.IDX_ATTR_DESCRIPTION :
                    return new TextFieldRenderer(new JTextField(), true, false);
                default : 
                    return super.getCellRenderer(row,column);
            }
        }
            
        return super.getCellRenderer(row,column);
    }
}
