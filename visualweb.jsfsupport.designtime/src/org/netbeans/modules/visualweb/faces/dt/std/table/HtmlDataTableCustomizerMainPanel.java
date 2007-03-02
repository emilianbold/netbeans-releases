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
package org.netbeans.modules.visualweb.faces.dt.std.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;

public class HtmlDataTableCustomizerMainPanel extends JPanel {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(
        HtmlDataTableCustomizerMainPanel.class);

    public boolean isModified() {
        return true;
    }

    public Result applyChanges() {
        Result result = new Result(true);
        columnsPanel.saveState();
        pagingPanel.saveState();
        columnsPanel.validate(result);
        if (result.isSuccess()) {
            tableState.saveState();
        }
        return result;
    }

    public void revertChanges() {
        tableState.loadState();
        columnsPanel.initState();
        pagingPanel.initState();
    }

    private DesignBean bean;
    private HtmlDataTableState tableState;
    private JTabbedPane tabPane = new JTabbedPane();
    private HtmlDataTableCustomizerColumnsPanel columnsPanel;
    private HtmlDataTableCustomizerPagingPanel pagingPanel;

    public void setDesignBean(DesignBean bean) {
        this.bean = bean;
        tableState = new HtmlDataTableState(bean);
    }

    public DesignBean getDesignBean() {
        return bean;
    }

    public HtmlDataTableCustomizerMainPanel(DesignBean bean) {
        setDesignBean(bean);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        columnsPanel = new HtmlDataTableCustomizerColumnsPanel();
        columnsPanel.setTable(tableState);
        columnsPanel.setContext(getDesignBean().getDesignContext());

        pagingPanel = new HtmlDataTableCustomizerPagingPanel();
        pagingPanel.setTable(tableState);

        tabPane.add(columnsPanel, bundle.getMessage("cols")); //NOI18N
        tabPane.add(pagingPanel, bundle.getMessage("paging")); //NOI18N

        this.setLayout(new BorderLayout());
        this.add(tabPane);

        this.setPreferredSize(new Dimension(500, 400));
    }
}
