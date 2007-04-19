package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;

public class QBNodeComponent extends JPanel
{
    private QueryBuilder                _queryBuilder;
    private QueryBuilderTable           _qbTable;
    private QueryBuilderTableModel      _queryBuilderTableModel = null;
    private String 			_nodeName;

    // Constructor

    public QBNodeComponent(String nodeName, QueryBuilderTableModel queryBuilderTableModel)
    {
        // Set some private variables
        _queryBuilderTableModel = queryBuilderTableModel;
	_nodeName = nodeName;

        // Create a JTable component, with the specified TableModel behind it
        _qbTable = new QueryBuilderTable(_queryBuilderTableModel);
        _qbTable.setBackground(Color.white); 
        
        // Wrap the JTable in a JScrollPane
        JScrollPane sp = new JScrollPane(_qbTable);
        sp.getViewport().setBackground(Color.white); 
        
        // Wrap the JScrollPane in a JPanel
        this.add(sp,BorderLayout.CENTER);
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	this.setPreferredSize(new Dimension(175,80));

	// Only applies to JInternalFrame
        // setResizable(true);
        
        // setVisible(true);
    }


    // Convenience methods -- return tablename/tablespec from associated model

    String getNodeName() {
	return _nodeName;
    }

    String getFullTableName() {
        return _queryBuilderTableModel.getFullTableName();
    }

    String getTableSpec() {
        return _queryBuilderTableModel.getTableSpec();
    }

    QueryBuilderTableModel getQueryBuilderTableModel() {
	return _queryBuilderTableModel;
    }
}
