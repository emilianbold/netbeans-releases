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
package org.netbeans.modules.bpel.properties.editors.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.bpel.properties.editors.StandardButtonBar;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.openide.util.NbBundle;

/**
 * This panel is the default container for different Node related tables.
 * It contains standard buttons: Add, Remove, Edit, Up and Down.
 * Panel provides buttons' layout, enabling policy
 *  and a set of buttons' handlers stub methods .
 *
 * @author  nk160297
 */
public class BaseTablePanel extends EditorLifeCycleAdapter {
    
    static final long serialVersionUID = 1L;
    
    private ListSelectionModel mySelectionModel;
    private ListSelectionListener mySelectionListener;
    private MouseListener myMouseListener;
    private StandardButtonBar buttonBar;
    private JTable myTableView;
    
    public BaseTablePanel() {
    }
    
    public void createContent() {
        setLayout(new java.awt.BorderLayout());
        //
        buttonBar = new StandardButtonBar();
        buttonBar.createContent();
        add(buttonBar, java.awt.BorderLayout.NORTH);
        //
        addListeners();
        //
        buttonBar.btnUp.setVisible(isRowMoveSupported());
        buttonBar.btnDown.setVisible(isRowMoveSupported());
    }
    
    public boolean initControls() {
        updateButtonState();
        return true;
    }
    
    private void addListeners() {
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
/*
                if (source == buttonBar.btnRef resh) {
                    doRef resh();
                } else */
                if (source == buttonBar.btnAdd) {
                    addRow(e);
                } else if (source == buttonBar.btnEdit) {
                    editRow(e);
                } else if (source == buttonBar.btnDelete) {
                    deleteRow(e);
                    updateButtonState();
                } else if (source == buttonBar.btnUp) {
                    moveUp();
                } else if (source == buttonBar.btnDown) {
                    moveDown();
                }
            }
        };
        //
//        buttonBar.btnRef resh.addActionListener(listener);
        buttonBar.btnAdd.addActionListener(listener);
        buttonBar.btnEdit.addActionListener(listener);
        buttonBar.btnDelete.addActionListener(listener);
        buttonBar.btnUp.addActionListener(listener);
        buttonBar.btnDown.addActionListener(listener);
    }
    
    protected void doRefresh() {
    }
    
    protected void addRow(ActionEvent event) {
    }
    
    protected void editRow(ActionEvent event) {
    }
    
    protected void deleteRow(ActionEvent event) {
        //
        // Remember selection
        int selectedRow = getTableView().getSelectedRow();
        //    
        deleteRowImpl(event);
        //
        // Restor selection
        if (selectedRow != -1) {
            int rowCount = getTableView().getModel().getRowCount();
            selectedRow = Math.min(rowCount - 1, selectedRow);
            getTableView().getSelectionModel().
                    setSelectionInterval(selectedRow, selectedRow);
        }
    }
    
    protected void deleteRowImpl(ActionEvent event) {
    }
    
    /**
     * Indicate if the table supports the moving rows up and down.
     * The corresponding buttons are hidden if the method return false.
     */
    protected boolean isRowMoveSupported() {
        return false;
    }
    
    protected void moveUp() {
    }
    
    protected void moveDown() {
    }
    
    protected void processDoubleClick(int rowIndex) {
        editRow(new ActionEvent(this, 0, "edit")); // NOI18N
    }
    
    protected void attach2SelectionModel(ListSelectionModel newSelectionModel) {
        if (mySelectionModel == null || !mySelectionModel.equals(newSelectionModel)) {
            if (mySelectionModel != null) {
                mySelectionModel.removeListSelectionListener(getSelectionListener());
            }
            mySelectionModel = newSelectionModel;
            if (mySelectionModel != null) {
                mySelectionModel.addListSelectionListener(getSelectionListener());
            }
        }
    }
    
    protected ListSelectionListener getSelectionListener() {
        if (mySelectionListener == null) {
            mySelectionListener = new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent event) {
                    if (!event.getValueIsAdjusting()) {
                        updateButtonState();
                    }
                }
            };
        }
        return mySelectionListener;
    }
    
    protected void updateButtonState() {
        if (mySelectionModel != null && myTableView != null) {
            int minSelection = mySelectionModel.getMinSelectionIndex();
            int maxSelection = mySelectionModel.getMaxSelectionIndex();
            boolean hasSeleciton = minSelection != -1;
            //
            buttonBar.btnEdit.setEnabled(hasSeleciton);
            buttonBar.btnDelete.setEnabled(hasSeleciton);
            //
            buttonBar.btnUp.setEnabled(hasSeleciton && (minSelection > 0));
            int rowCount = BaseTablePanel.this.myTableView.getModel().getRowCount();
            buttonBar.btnDown.
                    setEnabled(hasSeleciton && (maxSelection < rowCount - 1));
        }
    }
    
    protected MouseListener getMouseListener() {
        if (myMouseListener == null) {
            myMouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        int rowIndex = BaseTablePanel.this.
                                getTableView().getSelectedRow();
                        if (rowIndex != -1) {
                            BaseTablePanel.this.processDoubleClick(rowIndex);
                        }
                    }
                }
            };
        }
        return myMouseListener;
    }
    
    public JTable getTableView() {
        return myTableView;
    }
    
    public void setTableView(JTable tableView) {
        if (myTableView == null || !myTableView.equals(tableView)) {
            if(myTableView != null) {
                myTableView.removeMouseListener(getMouseListener());
                attach2SelectionModel(null);
            }
            myTableView = tableView;
            if(myTableView != null) {
                myTableView.addMouseListener(getMouseListener());
                attach2SelectionModel(tableView.getSelectionModel());
                //
                updateButtonState();
            }
        }
    }
    
    public StandardButtonBar getButtonBar() {
        return buttonBar;
    }
    
    protected void restoreSelectedRows(int[] rows) {
        if (myTableView == null) {
            return;
        }
        //
        ListSelectionModel selectionModel = myTableView.getSelectionModel();
        int rowCount = myTableView.getRowCount();
        for (int rowIndex : rows) {
            if (rowIndex < rowCount) {
                selectionModel.addSelectionInterval(rowIndex, rowIndex);
            }
        }
    }
    
    protected void disableButtons(){
        buttonBar.btnAdd.setEnabled( false );
        buttonBar.btnDelete.setEnabled( false );
        buttonBar.btnDown.setEnabled( false );
        buttonBar.btnEdit.setEnabled( false );
        buttonBar.btnUp.setEnabled( false );
    }
    
    protected JLabel createLabel( String txt ) {
        return new JLabel( txt , SwingConstants.CENTER );
    }
    
    /*
     * Fix for #6377490
     * TODO :
     * Here may be will need JPanel that will agregate Error text 
     * with Error icon.... 
     */
    protected JLabel createWSDLErrorMessage() {
        String label = NbBundle.getMessage(FormBundle.class, "LBL_BadWSDL"); // NOI18N
        return createLabel( "<HTML><b><font color='red'>" + label 
                +"</font></b></HTML>" ); // NOI18N
    }
}
