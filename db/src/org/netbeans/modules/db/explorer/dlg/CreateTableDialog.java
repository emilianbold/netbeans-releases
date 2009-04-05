/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import org.netbeans.lib.ddl.DDLException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.util.CommandBuffer;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.util.TextFieldValidator;
import org.netbeans.modules.db.util.ValidableTextField;
import org.openide.awt.Mnemonics;
import org.openide.util.Utilities;

public class CreateTableDialog {
    boolean result = false;
    Dialog dialog = null;
    JTextField dbnamefield, dbownerfield;
    JTable table;
    JButton addbtn, delbtn;
    Specification spec;
    private Vector<TypeElement> ttab;

    private static Map dlgtab = null;
    private static final String filename = "org/netbeans/modules/db/resources/CreateTableDialog.plist"; // NOI18N
    private static final int SIZE_COL_INDEX = 6;
    private static Logger LOGGER = Logger.getLogger(
            CreateTableDialog.class.getName());

    public static final Map getProperties() {
        if (dlgtab == null) try {
            ClassLoader cl = CreateTableDialog.class.getClassLoader();
            InputStream stream = cl.getResourceAsStream(filename);
            if (stream == null) {
                String message = NbBundle.getMessage (CreateTableDialog.class, "EXC_UnableToOpenStream", filename); // NOI18N
                throw new Exception(message);
            }
            PListReader reader = new PListReader(stream);
            dlgtab = reader.getData();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            dlgtab = null;
        }

        return dlgtab;
    }

    public CreateTableDialog(final Specification spe, final String schema) {
        spec = spe;
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constr = new GridBagConstraints();
            pane.setLayout(layout);
            pane.setMinimumSize(new Dimension(200,100));
     
            // Table name field

            label = new JLabel();
            Mnemonics.setLocalizedText(label, NbBundle.getMessage (CreateTableDialog.class, "CreateTableName")); // NOI18N
            label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableNameA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 0;
            layout.setConstraints(label, constr);
            pane.add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.weighty = 0.0;
            constr.gridx = 1;
            constr.gridy = 0;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            dbnamefield = new JTextField(NbBundle.getMessage (CreateTableDialog.class, "CreateTableUntitledName"), 10); // NOI18N
            dbnamefield.setToolTipText(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableNameTextFieldA11yDesc"));
            dbnamefield.getAccessibleContext().setAccessibleName(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableNameTextFieldA11yName"));
            label.setLabelFor(dbnamefield);
            layout.setConstraints(dbnamefield, constr);
            pane.add(dbnamefield);

            // Table columns in scrollpane

            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;
            constr.gridx = 0;
            constr.gridy = 1;
            constr.gridwidth = 4;
            constr.gridheight = 3;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            table = new DataTable(new ColumnDataModel(getTypes()));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setToolTipText(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableColumnTableA11yDesc"));
            table.getAccessibleContext().setAccessibleName(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableColumnTableA11yName"));
            table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableColumnTableA11yDesc"));
            JScrollPane scrollpane = new JScrollPane(table);
            scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
            layout.setConstraints(scrollpane, constr);
            pane.add(scrollpane);

            // Setup cell editors for table

            Map tmap = spec.getTypeMap();
            ttab = new Vector<TypeElement> (tmap.size());
            Iterator iter = tmap.keySet().iterator();
            while (iter.hasNext()) {
                String iterkey = (String)iter.next();
                String iterval = (String)tmap.get(iterkey);
                ttab.add(new TypeElement(iterkey, iterval));
            }

            final JComboBox combo = new JComboBox(ttab);
            combo.setSelectedIndex(0);
            table.setDefaultEditor(String.class, new DataCellEditor(new JTextField()));
            table.getColumn("type").setCellEditor(new ComboBoxEditor(combo)); // NOI18N
            table.getColumn("size").setCellEditor(new DataCellEditor(new ValidableTextField(new TextFieldValidator.integer()))); // NOI18N
            table.getColumn("scale").setCellEditor(new DataCellEditor(new ValidableTextField(new TextFieldValidator.integer()))); // NOI18N
            table.setRowHeight(combo.getPreferredSize().height);

            // Button pane

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.anchor = GridBagConstraints.NORTH;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 4;
            constr.gridy = 1;
            constr.insets = new java.awt.Insets (2, 8, 2, 2);
            JPanel btnpane = new JPanel();
            GridLayout btnlay = new GridLayout(2,1,0,5);
            btnpane.setLayout(btnlay);
            layout.setConstraints(btnpane, constr);
            pane.add(btnpane);

            // Button add column

            addbtn = new JButton();
            Mnemonics.setLocalizedText(addbtn, NbBundle.getMessage (CreateTableDialog.class, "CreateTableAddButtonTitle")); // NOI18N
            addbtn.setToolTipText(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableAddButtonTitleA11yDesc"));
            btnpane.add(addbtn);
            addbtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (table.getCellEditor() != null) {
                        table.getCellEditor().stopCellEditing();
                    }
                    DataModel model = (DataModel)table.getModel();
                    ColumnItem item = new ColumnItem();
                    item.setProperty(ColumnItem.TYPE, ttab.elementAt(0));
                    model.addRow(item);
                }
            });

            // Button delete column

            delbtn = new JButton();
            Mnemonics.setLocalizedText(delbtn, NbBundle.getMessage (CreateTableDialog.class, "CreateTableRemoveButtonTitle")); // NOI18N
            delbtn.setToolTipText(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableRemoveButtonTitleA11yDesc")); // NOI18N
            btnpane.add(delbtn);
            delbtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (table.getCellEditor() != null) {
                        table.getCellEditor().stopCellEditing();
                    }
                    int idx = table.getSelectedRow();
                    if (idx != -1)
                        ((DataModel)table.getModel()).removeRow(idx);
                }
            });

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    final ActionEvent evt = event;
                    if (table.getCellEditor() != null) {
                        table.getCellEditor().stopCellEditing();
                    }
                    if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                        result = CreateTableDialog.this.validate();

                        CommandBuffer cbuff = new CommandBuffer();
                        Vector idxCommands = new Vector();

                        if (! result) {
                            String msg = NbBundle.getMessage (CreateTableDialog.class, "EXC_InsufficientCreateTableInfo");
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                            return;
                        }
                        try {
                            final String tablename = getTableName();
                            final DataModel dataModel = (DataModel)table.getModel();
                            final Vector data = dataModel.getData();

                            boolean wasException = DbUtilities.doWithProgress(null, new Callable<Boolean>() {
                                public Boolean call() throws Exception {
                                    CreateTableDDL ddl = new CreateTableDDL(
                                            spec, schema, tablename);

                                    return ddl.execute(data, dataModel.getTablePrimaryKeys());
                                }
                            });

                            //bugfix for #31064
                            combo.setSelectedItem(combo.getSelectedItem());

                            // was execution of commands with or without exception?
                            if(!wasException) {
                                // dialog is closed after successfully create table
                                dialog.setVisible(false);
                                dialog.dispose();
                            }
                            //dialog is not closed after unsuccessfully create table

                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getCause();
                            if (cause instanceof DDLException) {
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                            } else {
                                LOGGER.log(Level.INFO, null, cause);
                                DbUtilities.reportError(NbBundle.getMessage (CreateTableDialog.class, "ERR_UnableToCreateTable"), e.getMessage());
                            }
                        }
                    }
                }
            };

            pane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (CreateTableDialog.class, "ACS_CreateTableDialogA11yDesc")); // NOI18N

            addbtn.doClick();
            DialogDescriptor descriptor = new DialogDescriptor(pane, NbBundle.getMessage (CreateTableDialog.class, "CreateTableDialogTitle"), true, listener); // NOI18N
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public String getTableName() {
        return dbnamefield.getText();
    }

    private boolean validate() {
        String tname = getTableName();
        if (tname == null || tname.length()<1)
            return false;

        Vector cols = ((DataModel)table.getModel()).getData();
        Enumeration colse = cols.elements();
        while(colse.hasMoreElements())
            if (!((ColumnItem)colse.nextElement()).validate())
                return false;

        return true;
    }

    class DataTable extends JTable {
        static final long serialVersionUID =1222037401669064863L;
        public DataTable(TableModel model) {
            super(model);
            setSurrendersFocusOnKeystroke(true);
            TableColumnModel cmodel = getColumnModel();
            int i;
            int ccount = model.getColumnCount();
            int columnWidth;
            int preferredWidth;
            String columnName;
            int width = 0;
            for (i = 0; i < ccount; i++) {
                TableColumn col = cmodel.getColumn(i);
                Map cmap = ColumnItem.getColumnProperty(i);
                col.setIdentifier(cmap.get("name")); //NOI18N
                columnName = NbBundle.getMessage (CreateTableDialog.class, "CreateTable_" + i); //NOI18N
                columnWidth = (new Double(getFontMetrics(getFont()).getStringBounds(columnName, getGraphics()).getWidth())).intValue() + 20;
                if (cmap.containsKey("width")) { // NOI18N
                    if (((Integer)cmap.get("width")).intValue() < columnWidth)
                        col.setPreferredWidth(columnWidth);
                    else
                        col.setPreferredWidth(((Integer)cmap.get("width")).intValue()); // NOI18N
                    preferredWidth = col.getPreferredWidth();
                }
                if (cmap.containsKey("minwidth")) // NOI18N
                    if (((Integer)cmap.get("minwidth")).intValue() < columnWidth)
                        col.setMinWidth(columnWidth);
                    else
                        col.setMinWidth(((Integer)cmap.get("minwidth")).intValue()); // NOI18N
                //				if (cmap.containsKey("alignment")) {}
                //				if (cmap.containsKey("tip")) ((JComponent)col.getCellRenderer()).setToolTipText((String)cmap.get("tip"));
                if (i < 7) { // the first 7 columns should be visible
                    width += col.getPreferredWidth();
                }
            }
            width = Math.min(Math.max(width, 380), Toolkit.getDefaultToolkit().getScreenSize().width - 100);
            setPreferredScrollableViewportSize(new Dimension(width, 150));
        }
    }

    class FocusInvoker implements Runnable {
        private JTextField xxx;
        public FocusInvoker(JTextField fld) {
            xxx=fld;
        }

        public void run() {
            xxx.selectAll();
        }
    }

    class DataCellEditor extends DefaultCellEditor {
        static final long serialVersionUID =3855371868128838794L;

        public DataCellEditor(final JTextField x) {
            super(x);
            setClickCountToStart(1);
        }
    }
    
    class PopupInvoker implements Runnable {
        private JComboBox jComboBox;

        public PopupInvoker(JComboBox aJComboBox) {
            jComboBox = aJComboBox;
        }

        public void run() {
            try {
                jComboBox.showPopup();
            } catch (IllegalComponentStateException icse) {
                // This is a valid exception that occurs
                // if the jComboBox is somehow hide. Do nothing.
            }
        }

    }

    private List<String> getTypes() {
        // TODO: replace with static metadata API to return a List of the fixed SQL types
        final String[] varTypes = {"java.sql.Types.VARCHAR", "java.sql.Types.BLOB", "java.sql.Types.BINARY"}; // NOI18N
        return Arrays.asList(varTypes);
    }

    private class ColumnDataModel extends DataModel {
        List<String> varTypeList;

        ColumnDataModel(List<String> varTypes) {
            varTypeList = varTypes;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            boolean isFixed = false;
            if (column == SIZE_COL_INDEX) {
                String selectedSQLType = ((TypeElement) table.getValueAt(row, column - 1)).getType();
                if (!varTypeList.contains(selectedSQLType)) {
                    isFixed = true;
                }
            }
            if (column == SIZE_COL_INDEX && isFixed) {
                return false;
            }
            return true;
        }
    }


    class ComboBoxEditor extends DefaultCellEditor {
        public ComboBoxEditor(final JComboBox jComboBox) {
            super(jComboBox);
            jComboBox.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    SwingUtilities.invokeLater(new PopupInvoker(jComboBox));
                }

                public void focusLost(FocusEvent e) {}
            });
        }
    }

    private static final class ListCellRendererImpl extends DefaultListCellRenderer {
        
        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            // hack to fix issue 65759
            if (Utilities.isWindows()) {
                size.width += 4;
            }
            return size;
        }
    }
}
