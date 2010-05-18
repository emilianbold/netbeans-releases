/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */

package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.InputEvent;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import javax.swing.KeyStroke;
import org.openide.windows.WindowManager;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.editor.ui.view.TableColumnNode;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.editor.ui.view.TableColumnTreePanel;
import org.openide.util.NbBundle;
/**
 *
 * @author karthikeyan s
 */
public class SelectColumnsAction extends AbstractAction {
    
    private MashupDataObject mObj;
    
    private SQLObject obj;
    private MashupGraphManager manager;
    
    /** Creates a new instance of EditJoinAction */
    public SelectColumnsAction(MashupDataObject dObj, SQLObject obj) {
        super("",new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.RUNTIMEATTR)));
        mObj = dObj;
        this.manager = dObj.getGraphManager();
        this.obj = obj;
    }
    
    public SelectColumnsAction(MashupDataObject dObj, SQLObject obj, String name) {
        super(name,new ImageIcon(
                MashupGraphUtil.getImage(ImageConstants.RUNTIMEATTR)));
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SelectColumnsAction.class, "TOOLTIP_Select/Remove_Table_Columns"));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
        this.manager = dObj.getGraphManager();
        this.obj = obj;
    }
    
    public void actionPerformed(ActionEvent e) {        
        List<SQLDBTable> tableList = new ArrayList<SQLDBTable>();
        SQLDBTable dbTable = (SQLDBTable) obj;            
        tableList.add(dbTable);        

        TableColumnTreePanel columnPanel = new TableColumnTreePanel(tableList, true);
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        String dlgLabel = NbBundle.getMessage(SelectColumnsAction.class, "LBL_Select_columns");
        JLabel lbl = new JLabel(dlgLabel);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.bottom = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        panel.add(new JSeparator(), gbc);
        gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(columnPanel, gbc);

        String dlgTitle = NbBundle.getMessage(SelectColumnsAction.class, "TITLE_Select_Columns");
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), panel, dlgTitle, JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        boolean userClickedOk = (JOptionPane.OK_OPTION == response);
        if (userClickedOk) {
            List columns = dbTable.getColumnList();
            List tableNodes = columnPanel.getTableColumnNodes();

            boolean wantsReload = false;
            
            Iterator iter = columns.iterator();
            while (iter.hasNext()) {
                SQLDBColumn column = (SQLDBColumn) iter.next();
                boolean userWantsVisible = TableColumnNode.isColumnVisible(column, tableNodes);

                if (!userWantsVisible) {
                    column.setVisible(false);
                    wantsReload = true;
                } else if (userWantsVisible) {
                    column.setVisible(true);
                    wantsReload = true;
                }
            }
            if(wantsReload) {
                mObj.getMashupDataEditorSupport().synchDocument();
                manager.updateColumnSelection(dbTable);
            }
        }    
    }
}