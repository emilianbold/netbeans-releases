/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this wsdlFile are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this wsdlFile except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each wsdlFile and include the License wsdlFile at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular wsdlFile as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License wsdlFile that
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
 * If you wish your version of this wsdlFile to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this wsdlFile under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.wag.manager.wizards;

import com.zembly.oauth.api.Parameter;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.wag.manager.model.WagService;
import org.netbeans.modules.wag.manager.model.WagServiceParameter;
import org.netbeans.modules.wag.manager.zembly.TestDriver;
import org.netbeans.modules.wag.manager.zembly.ZemblySession;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Dialog that test drive zembly services.
 *
 * @author  peterliu
 */
public class TestDriveDlg extends JPanel implements ActionListener {
    
    private static final String KEYSET_TYPE = "KEYSET";     //NOI18N

    private Dialog dialog;
    private DialogDescriptor dlg = null;
    private String okString = NbBundle.getMessage(this.getClass(), "LBL_Close");
    private WagService service;
    private ParamTableModel tableModel;
    private List<Parameter> inputParams;

    /** Creates new form TestWebServiceMethodDlg */
    public TestDriveDlg(WagService service) {
        this.service = service;
     
        initComponents();
        myInitComponents();
    }

    public void displayDialog() {

        dlg = new DialogDescriptor(this, NbBundle.getMessage(this.getClass(), "LBL_TestDrive"),
                false, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, this.getHelpCtx(), this);
        dlg.setOptions(new Object[]{okButton});
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        /**
         * After the window is opened, set the focus to the Get information button.
         */
        final JPanel thisPanel = this;
        dialog.addWindowListener(new WindowAdapter() {

            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(
                        new Runnable() {

                            public void run() {
                                testButton.requestFocus();
                                thisPanel.getRootPane().setDefaultButton(testButton);
                            }
                        });
            }
        });

        dialog.setVisible(true);
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        paramLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        paramTable = new javax.swing.JTable();
        testButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        resultTA = new javax.swing.JTextArea();
        jSeparator1 = new javax.swing.JSeparator();
        resultLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "jLabel2");

        setToolTipText("null");
        setPreferredSize(new java.awt.Dimension(700, 800));

        org.openide.awt.Mnemonics.setLocalizedText(paramLabel, org.openide.util.NbBundle.getMessage(TestDriveDlg.class, "LBL_Parameters")); // NOI18N

        paramTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(paramTable);

        org.openide.awt.Mnemonics.setLocalizedText(testButton, org.openide.util.NbBundle.getMessage(TestDriveDlg.class, "LBL_Test")); // NOI18N
        testButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });

        resultTA.setColumns(20);
        resultTA.setRows(5);
        jScrollPane2.setViewportView(resultTA);

        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(TestDriveDlg.class, "LBL_Result")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, paramLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .add(testButton)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, resultLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(paramLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 178, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(testButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(resultLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName("null");
    }// </editor-fold>//GEN-END:initComponents

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
        // TODO add your handling code here:
        testDrive();
    }//GEN-LAST:event_testButtonActionPerformed

    private void myInitComponents() {
        okButton.setText(okString);
        
        inputParams = new ArrayList<Parameter>();
        for (WagServiceParameter p : service.getParameters()) {
            if (!p.getType().equals(KEYSET_TYPE)) {
                inputParams.add(Parameter.create(p.getName(), null));
            }
        }

        tableModel = new ParamTableModel();
        paramTable.setModel(tableModel);
        paramTable.addKeyListener(new TableKeyListener());
    }

    public void actionPerformed(ActionEvent evt) {
        String actionCommand = evt.getActionCommand();
        if (actionCommand.equalsIgnoreCase(okString)) {
            okButtonAction(evt);
        }
    }

    private void okButtonAction(ActionEvent evt) {
        /**
         * If the MethodTask is not null, the MethodTask
         * thread may still be running so we need to tell
         * it we've cancelled.
         */
        dialog.setCursor(normalCursor);
        dialog.dispose();
    }

    private boolean getRequiredStatus(Parameter param) {
        for (WagServiceParameter p : service.getParameters()) {
            if (param.getName().equals(p.getName())) {
                return p.isRequired();
            }
        }

        return false;
    }

    private void testDrive() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                TestDriver testDriver = ZemblySession.getInstance().getTestDriver();

                final String result = testDriver.test(service, inputParams);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        resultTA.setText(result);
                    }
                });
            }
        });
    }


    private class TableKeyListener implements KeyListener {

        public TableKeyListener() {
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 10) //Carriage return
            {
                dialog.dispose();
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }

    private class ParamTable extends JTable {

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (column != 0) {
                return new ParamCellRenderer();
            }
            return super.getCellRenderer(row, column);
        }
    }

    private class ParamCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component ret = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
       
            if (value == null) {
                return new JLabel(NbBundle.getMessage(TestDriveDlg.class, "LBL_NotSet"));
            } else if (value instanceof Class) {
                return new JLabel(((Class) value).getName());
            } else if (value instanceof Boolean) {
                JCheckBox cb = new JCheckBox();
                cb.setHorizontalAlignment(JLabel.CENTER);
                cb.setBorderPainted(true);
                cb.setSelected((Boolean) value);
                return cb;
            }
            return ret;
        }
    }

    private class ParamTableModel extends AbstractTableModel {

        public ParamTableModel() {
            columnNames = new String[]{NbBundle.getMessage(TestDriveDlg.class, "LBL_Name"),
                        NbBundle.getMessage(TestDriveDlg.class, "LBL_Value"),
                        NbBundle.getMessage(TestDriveDlg.class, "LBL_Required")};
            types = new Class[]{String.class, Object.class, Boolean.class};
            canEdit = new boolean[]{false, true, false};
        }
        String[] columnNames;
        Class[] types;
        boolean[] canEdit;

        @Override
        public String getColumnName(int index) {
            return columnNames[index];
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return inputParams.size();
        }

        public Object getValueAt(int row, int column) {
            Parameter param = inputParams.get(row);
            switch (column) {
                case 0:
                    return param.getName();
                case 1:
                    return param.getValue();
                case 2:
                    return getRequiredStatus(param);
            }

            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            Parameter info = inputParams.get(row);

            if (column == 1) {
                info.setValue(value.toString());
            }
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit[columnIndex];
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel paramLabel;
    private javax.swing.JTable paramTable;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JTextArea resultTA;
    private javax.swing.JButton testButton;
    // End of variables declaration//GEN-END:variables
    private JButton okButton = new JButton();
    private Cursor normalCursor;
}
