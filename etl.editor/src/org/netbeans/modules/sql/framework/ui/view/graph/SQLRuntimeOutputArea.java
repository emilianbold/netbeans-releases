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

package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.impl.GradientBrush;
import org.openide.util.NbBundle;

import com.nwoods.jgo.JGoBrush;

/**
 * @author Ritesh Adval
 * @author Jonathan Giron
 */
public class SQLRuntimeOutputArea extends SQLBasicTableArea {

    private static URL runtimeOutputImgUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/RuntimeOutput.png");

    private static URL propertiesUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/properties.png");
    
    private static final Color DEFAULT_BG_COLOR = new Color(204, 213, 241);
    
    private static final Color DEFAULT_BG_COLOR_DARK = new Color(165, 193, 249);
    
    private static final JGoBrush DEFAULT_TITLE_BRUSH = new GradientBrush(DEFAULT_BG_COLOR, DEFAULT_BG_COLOR_DARK);    
    
    private JMenuItem editRuntimeItem;

    /**
     * Creates a new instance of SQLRuntimeOutputArea
     */
    public SQLRuntimeOutputArea() {
        super();
    }

    /**
     * Creates a new instance of SQLRuntimeOutputArea
     * 
     * @param table the table to render
     */
    public SQLRuntimeOutputArea(SQLDBTable table) {
        super(table);
    }

    protected void initializePopUpMenu() {
        ActionListener aListener = new TableActionListener();
        //      edit runtime
        String lbl = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_edit");
        editRuntimeItem = new JMenuItem(lbl, new ImageIcon(propertiesUrl));
        editRuntimeItem.addActionListener(aListener);
        popUpMenu.add(editRuntimeItem);

        addSelectVisibleColumnsPopUpMenu(aListener);
    }

    Icon createIcon() {
        return new ImageIcon(runtimeOutputImgUrl);
    }

    private class TableActionListener implements ActionListener {
        /**
         * Invoked when an action occurs.
         * 
         * @param e ActionEvent to handle
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source == editRuntimeItem) {
                EditRuntime_ActionPerformed(e);
            } else {
                handleCommonActions(e);
            }
        }
    }

    private void EditRuntime_ActionPerformed(ActionEvent e) {
        Object[] args = new Object[] { new Integer(table.getObjectType())};
        this.getGraphView().execute(ICommand.ADD_RUNTIME_CMD, args);
        DataObjectProvider.getProvider().getActiveDataObject().getETLEditorSupport().synchDocument();
    }

    public void setConditionIcons() {
        //do nothing
    }
    
    /**
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#getDefaultTitleBrush()
     */
    protected JGoBrush getDefaultTitleBrush() {
        return DEFAULT_TITLE_BRUSH;
    }
    
    /**
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#getDefaultBackgroundColor()
     */
    protected Color getDefaultBackgroundColor() {
        return DEFAULT_BG_COLOR;
    }
}

