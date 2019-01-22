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

package org.netbeans.modules.websvc.registry.ui;

/**
 *
 * 
 */

import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTextField;

import org.netbeans.swing.outline.NodeRowModel;
import org.netbeans.swing.outline.OutlineModel;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import java.awt.Dialog;
import java.util.EventObject;

import com.sun.xml.rpc.processor.model.java.JavaType;
import com.sun.xml.rpc.processor.model.java.JavaSimpleType;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationType;


/**
 *
 * 
 */
public class ResultCellEditor extends DefaultCellEditor implements TableCellEditor {
    
    private Dialog dialog;
    private DialogDescriptor dlg;
    private ResultViewerDialog viewerDialog;
    private Object saveValue;
    
    /** Creates a new instance of TypeCellRenderer */
    public ResultCellEditor() {
        super(new JTextField());
        this.setClickCountToStart(1);
    }
    /**
     * return the value of the last component.
     */
    public Object getCellEditorValue() {
        return saveValue;
    }
    
    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
        saveValue = value;
        NodeRowModel rowModel = ((OutlineModel)table.getModel()).getRowNodeModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)rowModel.getNodeForRow(row);
        /**
         * Now depending on the type, create a component to edit/display the type.
         */
        viewerDialog = new ResultViewerDialog();
        if(null == node.getUserObject()) {
            viewerDialog.setText((String)value);
            
        } else {
            ResultNodeData data = (ResultNodeData)node.getUserObject();
            JavaType type = data.getResultType();
            
            if(type instanceof JavaSimpleType) {
                
                viewerDialog.setText(value != null ? value.toString() : "");
            }  else if(type instanceof JavaEnumerationType) {
                viewerDialog.setText(value != null ? value.toString() : "");
            } else {
                return null;
            }
            
            dlg = new DialogDescriptor(viewerDialog, data.getResultType().getRealName(),
            true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN, viewerDialog.getHelpCtx(), null);
            dlg.setOptions(new Object[] { viewerDialog.getOkButton() });
            
            dialog = DialogDisplayer.getDefault().createDialog(dlg);
            dialog.setSize(300,300);
            dialog.show();
        }
        
        
        return null;
    }
    
}
