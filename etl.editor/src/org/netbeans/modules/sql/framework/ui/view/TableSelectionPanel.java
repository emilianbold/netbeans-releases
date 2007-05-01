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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TableSelectionPanel extends JPanel {

    private JList tableList;

    /** Creates a new instance of TableSelectionPanel */
    public TableSelectionPanel(List tables) {
        initGUI(tables);
    }

    public List getSelectedTables() {
        ArrayList selectedTables = new ArrayList();
        Object[] selected = this.tableList.getSelectedValues();

        for (int i = 0; i < selected.length; i++) {
            selectedTables.add(selected[i]);
        }
        return selectedTables;
    }

    public void setSelectionMode(int selectionMode) {
        tableList.setSelectionMode(selectionMode);
    }

    private void initGUI(List tables) {
        this.setLayout(new BorderLayout());

        tableList = new JList(tables.toArray());
        if (tables.size() > 0) {
            tableList.setSelectedIndex(0);
        }
        this.add(tableList, BorderLayout.CENTER);
    }
}

