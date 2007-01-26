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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Component;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class DBSchemaUISupport {

    private DBSchemaUISupport() {
    }

    /**
     * Connects a combo box with the list of dbschemas in a project, making
     * the combo box display these dbschemas.
     */
    public static void connect(JComboBox comboBox, DBSchemaFileList dbschemaFileList) {
        comboBox.setModel(new DBSchemaModel(dbschemaFileList));
        comboBox.setRenderer(new DBSchemaRenderer(comboBox));
    }

    /**
     * Model for database schemas. Contains either a list of schemas (FileObject's)
     * or a single "no schemas" item.
     */
    private static final class DBSchemaModel extends AbstractListModel implements ComboBoxModel {

        private final DBSchemaFileList dbschemaFileList;
        private Object selectedItem;

        public DBSchemaModel(DBSchemaFileList dbschemaFileList) {
            this.dbschemaFileList = dbschemaFileList;
        }

        public void setSelectedItem(Object anItem) {
            if (dbschemaFileList.getFileList().size() > 0) {
                selectedItem = anItem;
            }
        }

        public Object getElementAt(int index) {
            List<FileObject> dbschemaFiles = dbschemaFileList.getFileList();
            if (dbschemaFiles.size() > 0) {
                return dbschemaFiles.get(index);
            } else {
                return NbBundle.getMessage(DBSchemaUISupport.class, "LBL_NoSchemas");
            }
        }

        public int getSize() {
            int dbschemaCount = dbschemaFileList.getFileList().size();
            return dbschemaCount > 0 ? dbschemaCount : 1;
        }

        public Object getSelectedItem() {
            return dbschemaFileList.getFileList().size() > 0 ? selectedItem : NbBundle.getMessage(DBSchemaUISupport.class, "LBL_NoSchemas");
        }
    }

    private static final class DBSchemaRenderer extends DefaultListCellRenderer {

        private JComboBox comboBox;

        public DBSchemaRenderer(JComboBox comboBox) {
            this.comboBox = comboBox;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object displayName = null;
            ComboBoxModel model = comboBox.getModel();

            if (model instanceof DBSchemaModel && value instanceof FileObject) {
                displayName = ((DBSchemaModel)model).dbschemaFileList.getDisplayName((FileObject)value);
            } else {
                displayName = value;
            }

            return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
        }
    }
}
