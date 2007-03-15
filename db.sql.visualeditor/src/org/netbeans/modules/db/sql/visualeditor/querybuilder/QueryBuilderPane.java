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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import javax.swing.JViewport;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BorderFactory;

import javax.swing.table.DefaultTableModel;

import javax.swing.event.*;

import java.util.Hashtable;

import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.event.*;

import org.openide.util.NbBundle;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.modules.db.sql.visualeditor.Log;

/**
 *
 * @author  Sanjay Dhamankar
 */
public class QueryBuilderPane extends JSplitPane {

    // Variables

    private boolean                     DEBUG = false;

    private int                         DIVIDER_SIZE = 7;

    // Make these package access, so components can access each other
    // through here

    private QueryBuilderResultTable             _queryBuilderResultTable;
    private QueryBuilderGraphFrame              _queryBuilderGraphFrame;
    private QueryBuilderInputTable              _queryBuilderInputTable;
    private QueryBuilderSqlTextArea             _queryBuilderSqlTextArea;

    private QueryBuilder                _queryBuilder;

    private JLabel			qbitLabel;
    private JLabel			qbstaLabel;
    private JLabel			qbgfLabel;

    // Use these to store the current state and to avoid the expensive
    // recursive calls
    private boolean                     graphScrollPaneEnabled;
    private boolean                     qbInputTableSPEnabled;

    private JScrollPane                 graphScrollPane;
    private JScrollPane                 qbInputTableSP;

    private JPanel                      bottomPanel;
    private JPanel                      buttonPanel;
    private boolean                     _keyTyped = false;


    // Constructor

    public QueryBuilderPane(QueryBuilder queryBuilder)
    {

/*
        ---------------------------------------------------------
        |         graphScrollPane                               |
        ---------------------------------------------------------
        ---------------------------------------------------------
        |         qbInputTableSP                                |
        ---------------------------------------------------------
        ---------------------------------------------------------
        |         qbSqlTextAreaSP                               |
        ---------------------------------------------------------
        ---------------------------------------------------------
        |         qbResultTableSP                               |
        ---------------------------------------------------------

     firstSplitPane ==> qbSqlTextAreaSP and qbResultTableSP
     secondSplitPane ==> qbInputTableSP and firstSplitPane
     this ==> graphScrollPane and secondSplitPane
*/

        super(JSplitPane.VERTICAL_SPLIT);
	Log.getLogger().entering("QueryBuilderPane", "constructor", queryBuilder); // NOTIFYDESCRIPTOR

        _queryBuilder = queryBuilder;

        graphScrollPaneEnabled = true;
        qbInputTableSPEnabled = true;

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Create the four panes that provide the main functionality

        _queryBuilderInputTable = new QueryBuilderInputTable(_queryBuilder);
        _queryBuilderInputTable.setFocusable ( true );
        _queryBuilderInputTable.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBIT_a11yName"));
        _queryBuilderInputTable.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBIT_a11yDescription"));
        qbitLabel = new JLabel();
        qbitLabel.setText(NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBIT_label"));
        qbitLabel.setLabelFor(_queryBuilderInputTable);

        _queryBuilderSqlTextArea = new QueryBuilderSqlTextArea(_queryBuilder);
        _queryBuilderSqlTextArea.setFocusable ( true );
        _queryBuilderSqlTextArea.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBSTA_a11yName"));
        _queryBuilderSqlTextArea.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBSTA_a11yDescription"));
        qbstaLabel = new JLabel();
        qbstaLabel.setText(NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBSTA_label"));
        qbstaLabel.setLabelFor(_queryBuilderSqlTextArea);

        _queryBuilderResultTable = new QueryBuilderResultTable(_queryBuilder);
        _queryBuilderResultTable.setFocusable ( true );

        // When constructing the diagram pane, pass in the other three panes
        // (or their models)
        // ToDo: revisit this decision.  
        _queryBuilderGraphFrame = new QueryBuilderGraphFrame(
            _queryBuilder,
            _queryBuilderInputTable, 
            _queryBuilderSqlTextArea,
            (DefaultTableModel) _queryBuilderResultTable.getModel()
            );
        _queryBuilderGraphFrame.setFocusable ( true );
        _queryBuilderGraphFrame.getAccessibleContext().setAccessibleName(
                            NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBGF_a11yName"));
        _queryBuilderGraphFrame.getAccessibleContext().setAccessibleDescription(
                            NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBGF_a11yDescription"));
        qbgfLabel = new JLabel();
        qbgfLabel.setText(NbBundle.getMessage(QueryBuilderPane.class, "QBP_QBGF_label"));
        qbgfLabel.setLabelFor(_queryBuilderGraphFrame);

        // Wrap the diagram into a scroll pane, and make it the
        // top component in the split pane
        graphScrollPane = new JScrollPane(_queryBuilderGraphFrame);
        graphScrollPane.setMinimumSize ( new java.awt.Dimension ( this.getWidth(),  40 ) );
        this.setTopComponent(graphScrollPane);

        JSplitPane firstSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane secondSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Create a JPanel for the bottom pane, and add the other three panes
        // to it (input table, text query, result table)
        bottomPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        bottomPanel.setLayout(gridbag);

        // Wrap the input table in a scroll pane, and add to bottom panel
        qbInputTableSP = new JScrollPane(_queryBuilderInputTable,
                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        qbInputTableSP.getViewport().setBackground(Color.white); 
        qbInputTableSP.setMinimumSize ( new java.awt.Dimension ( this.getWidth(),  40 ) );
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(qbInputTableSP, c);

//        bottomPanel.add(qbInputTableSP);


        // Wrap the SQL text area in a scroll pane, and add to bottom panel
        JScrollPane qbSqlTextAreaSP = new JScrollPane(_queryBuilderSqlTextArea,
                                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(qbSqlTextAreaSP, c);
        qbSqlTextAreaSP.setMinimumSize ( new java.awt.Dimension ( this.getWidth(),  40 ) );
        // ToDo: Should we do: qbSqlTextAreaSP.setBackground(Color.white); 

        // Wrap the result table in a scroll pane, and add to bottom panel
        JScrollPane qbResultTableSP = new JScrollPane(_queryBuilderResultTable,
                                                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        qbResultTableSP.getViewport().setBackground(Color.white); 
        qbResultTableSP.setMinimumSize ( new java.awt.Dimension ( this.getWidth(),  40 ) );
        c.gridx = 0;
        c.gridy = 3;
        gridbag.setConstraints(qbResultTableSP, c);
        
        firstSplitPane.setTopComponent ( qbSqlTextAreaSP );
        firstSplitPane.setBottomComponent ( qbResultTableSP );
        firstSplitPane.setDividerSize(DIVIDER_SIZE);
        firstSplitPane.setOneTouchExpandable(true);
        firstSplitPane.setResizeWeight(0.5);

        secondSplitPane.setTopComponent ( qbInputTableSP );
        secondSplitPane.setBottomComponent ( firstSplitPane );
        secondSplitPane.setDividerSize(DIVIDER_SIZE);
        secondSplitPane.setOneTouchExpandable(true);
        secondSplitPane.setResizeWeight(0.5);

        this.setBottomComponent(secondSplitPane);
        this.setDividerSize(DIVIDER_SIZE);
        this.setResizeWeight(0.5);
        this.setOneTouchExpandable(true);
        this.setVisible(true);
    }


    /**
     * Return the current query text
     */
    String getSqlTextAreaText()
    {
        return _queryBuilderSqlTextArea.getText();
    }

    /**
     * Return the query builder result table
     */
    QueryBuilderResultTable getQueryBuilderResultTable()
    {
        return _queryBuilderResultTable;
    }

    /**
     * Return the query builder graph frame
     */
    QueryBuilderGraphFrame getQueryBuilderGraphFrame()
    {
        return _queryBuilderGraphFrame;
    }

    /**
     * Return the query builder input table 
     */
    QueryBuilderInputTable getQueryBuilderInputTable()
    {
        return _queryBuilderInputTable;
    }
    /**
     * Return the query builder sql text area
     */
    QueryBuilderSqlTextArea getQueryBuilderSqlTextArea()
    {
        return _queryBuilderSqlTextArea;
    }

    /**
     * Clear each of the panes or their models
     */
    void clear()
    {
        _queryBuilderInputTable.clearModel();
        _queryBuilderSqlTextArea.clear();
        _queryBuilderResultTable.clearModel();
        _queryBuilderGraphFrame.clearGraph();
    }

    /**
     * Enable / Disable the container and its children.
     */
    public void setEnableContainer ( java.awt.Container c , boolean value ) {
        java.awt.Component[] components = c.getComponents();
        for(int i=0; i<components.length; i++) {
            components[i].setEnabled(value);
            if(components[i] instanceof java.awt.Container) {
                setEnableContainer((java.awt.Container)components[i], value);
            }
        }
    }

    /**
     * Enable / Disable the graph scrolled pane and its children.
     * We store the state to avoid the expensive recursive call to
     * setEnableContainer.
     */
    /*
    public void setQueryBuilderGraphFrameEnabled ( boolean value )
    {
        if ( graphScrollPaneEnabled == value ) return;
        if ( graphScrollPane != null ) {
            setEnableContainer ( graphScrollPane , value );
            graphScrollPaneEnabled = value;
        }
    }
    */

    /**
     * Enable / Disable the input table scrolled pane and its children.
     * We store the state to avoid the expensive recursive call to
     * setEnableContainer.
     */
    public void setQueryBuilderInputTableEnabled ( boolean value ) {
        if ( qbInputTableSPEnabled == value ) return;
        if ( qbInputTableSP != null ) {
            setEnableContainer ( qbInputTableSP , value );
            qbInputTableSPEnabled = value ;
        }
    }
}
