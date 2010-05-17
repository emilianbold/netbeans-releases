/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author jqian
 */
public abstract class AbstractActionTableCellEditorDecorator
        implements TableCellEditor, ActionListener {

    private TableCellEditor realEditor;
    private JButton editButton = new JButton("..."); // NOI18N
    protected JTable table;
    protected int row;
    protected int column;

    public AbstractActionTableCellEditorDecorator(TableCellEditor realEditor) {
        this.realEditor = realEditor;
        editButton.addActionListener(this);

        editButton.setFocusable(false);
        editButton.setFocusPainted(false);
        editButton.setMargin(new Insets(0, 0, 0, 0));
    }
    
    public TableCellEditor getRealEditor() {
        return realEditor;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        JPanel panel = new JPanel(new BorderLayout());

        Component editorComponent = realEditor.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        panel.add(editorComponent);
        panel.add(editButton, BorderLayout.EAST);
        this.table = table;
        this.row = row;
        this.column = column;

        return panel;
    }

    public Object getCellEditorValue() {
        return realEditor.getCellEditorValue();
    }

    public boolean isCellEditable(EventObject anEvent) {
        return realEditor.isCellEditable(anEvent);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return realEditor.shouldSelectCell(anEvent);
    }

    public boolean stopCellEditing() {
        return realEditor.stopCellEditing();
    }

    public void cancelCellEditing() {
        realEditor.cancelCellEditing();
    }

    public void addCellEditorListener(CellEditorListener l) {
        realEditor.addCellEditorListener(l);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        realEditor.removeCellEditorListener(l);
    }

    public final void actionPerformed(ActionEvent e) {
        realEditor.cancelCellEditing();
        editCell(table, row, column);
    }

    protected abstract void editCell(JTable table, int row, int column);
}
