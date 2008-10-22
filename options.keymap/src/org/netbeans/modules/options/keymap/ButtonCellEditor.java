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
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
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

    /** Constructor argument type (SC text, ShortcutAction) */
    private static final Class[] argTypes = new Class[]{String.class, Object.class};
    private java.lang.reflect.Constructor constructor;
    private Object value;
    private Object action;
    private KeymapViewModel model;
    private String orig;

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
        String s = (String) super.getCellEditorValue();

        try {
            value = constructor.newInstance(s, action);
            ShortcutAction sca = (ShortcutAction) action;
            ShortcutAction conflictingAction = model.findActionForShortcut(s);
            if (conflictingAction != null && !conflictingAction.equals(sca)) {//there is a conflicting action, show err dialog
                if(!overrride(conflictingAction.getDisplayName()))
                    return false;
            }
            model.removeShortcut(sca, orig);
            model.addShortcut(sca ,s);
            model.update();
        } catch (Exception e) {
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
            return false;
        }
        return super.stopCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected,
            int row, int column) {
        this.orig = ((ShortcutCell) value).toString();
        this.value = null;
        
        ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
        try {
            constructor = ShortcutCell.class.getConstructor(argTypes);
            this.action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        } catch (Exception e) {
            return null;
        }
        Component comp = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        return comp;
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }

    /**
     * Shows dialog where useer chooses whether SC of confl. action should be overriden
     * @param displayName name of conflicting action
     * @return true if shortcut of confliting action should be overriden
     */
    private boolean overrride(String displayName) {
        JPanel innerPane = new JPanel();
        innerPane.add(new JLabel(NbBundle.getMessage(ButtonCellEditor.class, "Override_Shortcut", displayName))); //NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                innerPane,
                NbBundle.getMessage(ButtonCellEditor.class, "Conflicting_Shortcut_Dialog"),
                true,
                DialogDescriptor.YES_NO_OPTION,
                null,
                null); //NOI18N
        
        DialogDisplayer.getDefault().notify(descriptor);

        if (descriptor.getValue().equals(DialogDescriptor.YES_OPTION))
            return true;
        else {
            JComponent comp = (JComponent) getComponent();
            comp.setBorder(new LineBorder(Color.red));
            comp.requestFocus();
            return false;
        }
    }

}
