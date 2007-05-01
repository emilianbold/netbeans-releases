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
package org.netbeans.modules.sql.framework.ui.view.validation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.netbeans.modules.sql.framework.ui.graph.IGraphView;


/**
 * @author Ritesh Adval
 */
public class SQLValidationView extends JPanel {

    private static final String TABLE_VIEW = "table_view";

    private static final String TEXT_VIEW = "text_view";

    private ValidationTableView vTableView;

    private JTextArea textArea;

    private JPanel cardPanel;

    private IGraphView graphView;

    public SQLValidationView(IGraphView gView) {
        this.graphView = gView;
        initGui();
    }

    private void initGui() {
        this.setLayout(new BorderLayout());

        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout());
        this.add(cardPanel, BorderLayout.CENTER);

        //add table panel
        vTableView = new ValidationTableView(this.graphView);
        cardPanel.add(vTableView, TABLE_VIEW);

        //add text panel
        textArea = new JTextArea();
        cardPanel.add(textArea, TEXT_VIEW);

    }

    public void setValidationInfos(List vInfos) {
        this.graphView.clearSelection();
        this.graphView.resetSelectionColors();
        vTableView.setValidationInfos(vInfos);
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, TABLE_VIEW);
    }

    public void clearView() {
        vTableView.clearView();
        textArea.setText("");
    }

    public void appendToView(String msg) {
        textArea.append(msg);
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, TEXT_VIEW);
    }
}

