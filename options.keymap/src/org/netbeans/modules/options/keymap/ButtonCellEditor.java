/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.options.keymap;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Cell Editor for shortcuts column
 * @author Max Sauer
 */
public class ButtonCellEditor extends DefaultCellEditor {

    private Object action;
    private KeymapViewModel model;
    private String orig;

    private KeyAdapter escapeAdapter = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                JTable table = (JTable) cell.getParent();
                table.requestFocus();
                table.getCellEditor().cancelCellEditing();
                table.requestFocus();
            }
        }
    };

    private ShortcutCell cell = new ShortcutCell();

    public ButtonCellEditor(KeymapViewModel model) {
        super(new ShortcutTextField());
        this.model = model;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        String s = cell.toString();

        ShortcutAction sca = (ShortcutAction) action;
        ShortcutAction conflictingAction = model.findActionForShortcut(s);
        if (conflictingAction != null && !conflictingAction.equals(sca)) {//there is a conflicting action, show err dialog
            //there is a conflicting action, show err dialog
            Object overrride = overrride(conflictingAction.getDisplayName());
            if (overrride.equals(DialogDescriptor.YES_OPTION)) {
                //proceed with override
                } else if (overrride.equals(DialogDescriptor.NO_OPTION)) {
                JComponent comp = (JComponent) getComponent();
                comp.setBorder(new LineBorder(Color.red));
                comp.requestFocus();
                return false;
            } else {
                cell.getTextField().setText(orig);
                fireEditingCanceled();
                setBorderEmpty();
                return true;
            }
        }
        model.removeShortcut((ShortcutAction) action, orig);
        if (!(s.length() == 0)) // do not add empty shortcuts
            model.addShortcut((ShortcutAction) action, s);
        fireEditingStopped();
        setBorderEmpty();
        model.update();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        cell.getTextField().setText(orig);
        fireEditingCanceled();
        setBorderEmpty();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected,
            int row, int column) {
        cell = (ShortcutCell) value;
        this.orig = cell.toString();
        this.action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        JTextField textField = cell.getTextField();
        textField.addActionListener(delegate);
        textField.setBorder(new LineBorder(Color.BLACK));
        if(!Arrays.asList(textField.getKeyListeners()).contains(escapeAdapter)) {
            textField.addKeyListener(escapeAdapter);
        }
        return cell;
    }

    @Override
    public Object getCellEditorValue() {
        return cell;
    }

    @Override
    public Component getComponent() {
        return cell.getTextField();
    }

    /**
     * Shows dialog where user chooses whether SC of confl. action should be overriden
     * @param displayName name of conflicting action
     * @return dialog result
     */
    private Object overrride(String displayName) {
        JPanel innerPane = new JPanel();
        innerPane.add(new JLabel(NbBundle.getMessage(ButtonCellEditor.class, "Override_Shortcut", displayName))); //NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                innerPane,
                NbBundle.getMessage(ButtonCellEditor.class, "Conflicting_Shortcut_Dialog"),
                true,
                DialogDescriptor.YES_NO_CANCEL_OPTION,
                null,
                null); //NOI18N
        
        DialogDisplayer.getDefault().notify(descriptor);
        return descriptor.getValue();
    }

    private void setBorderEmpty() {
        ((JComponent) getComponent()).setBorder(new EmptyBorder(0, 0, 0, 0));
    }

}
