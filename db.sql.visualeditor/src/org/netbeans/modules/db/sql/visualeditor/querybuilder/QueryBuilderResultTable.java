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

import java.util.Vector;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Mode;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.modules.db.sql.visualeditor.Log;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;

import java.text.DateFormat ;

/**
 * A table for displaying query results in the query editor
 * @author  Sanjay Dhamankar, Jim Davidson
 */
public class QueryBuilderResultTable extends JTable
                        implements KeyListener {

    private DefaultTableModel resultTableModel = null;
    private QueryBuilder                _queryBuilder;

    public QueryBuilderResultTable() {
        this(null);
    }

    /** Constructor which takes the parent as parameter */
    public QueryBuilderResultTable(QueryBuilder queryBuilder) {

        super();

        _queryBuilder = queryBuilder;
        resultTableModel = new DefaultTableModel() {
            public boolean isCellEditable ( int row, int column ) {
                return false;
            }
        };
        this.setModel(resultTableModel);

        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        this.setMinimumSize(new Dimension(200, 30) );
//        this.setPreferredSize(new Dimension(200, 30) );

        // change foreground color to Dark Gray ...
        this.setForeground(Color.DARK_GRAY);
        addKeyListener(this);
    }


    /** ignore */
    public void keyTyped(KeyEvent e) {
    }

    /** ignore */
    public void keyReleased(KeyEvent e) {
    }

    /** Handle the key pressed event and change the focus if a particular
     * key combination is pressed. */
    public void keyPressed(KeyEvent e) {
        if ( _queryBuilder != null ) _queryBuilder.handleKeyPress(e);
    }

    /**
     * Clear the model
     */
    void clearModel() {
        ((DefaultTableModel)this.getModel()).setRowCount(0);
    }

    /**
     * Update the table model from the ResultSet
     * @param the result set associated with the table
     */
    public void displayResultSet(ResultSet rs) {
        displayResultSet( rs, 40, true ) ;
    }
    public boolean displayResultSet(ResultSet rs, int maxEntries, boolean infoMsgIfTruncated ) {
        // Check validity of ResultSet
        ResultSetMetaData rsmd;
        boolean resultsTruncated = false ;
        try {
            if ((rs == null) ||
            ((rsmd=rs.getMetaData())==null)) {
                //Thread.dumpStack();
                Log.err.log(ErrorManager.EXCEPTION,
                "Exception - unable to get query result ! "); // NOI18N
                return resultsTruncated;
            }

            // Get Column Names
            int numberOfColumns = rsmd.getColumnCount();
            Log.err.log(ErrorManager.INFORMATIONAL,
            " rsmd.getColumnCount(): " + numberOfColumns); // NOI18N

            // Create a vector of column names, for headers
            String[] dbColumnNames = new String[numberOfColumns];
            for (int i = 1; i <= numberOfColumns; i++) {
                dbColumnNames[i - 1] = rsmd.getColumnName(i);
            }

            // Set Column Headers; this only works with DefaultTableModel
            resultTableModel.setColumnIdentifiers(dbColumnNames);

            // Discard all rows in the current model
            resultTableModel.setRowCount(0);
            
            // Process the result set, producing a vector
            // For each row, produce a 1-d array that gets added to the Model
            
            // Add a check to diaplay ONLY first 40 entries
            // Add buttons "PREVIOUS", "NEXT" under the result table and
            // show the proper entires. This is to avoid the
            // OutOfMemoryException
            
            // int maxEntries = 40;
            int count = 0;
            while (rs.next() && count < maxEntries + 1 ) {
                if ( count >= maxEntries ) {
                    resultsTruncated = true ;
                    break ;
                }
                Object[] row = new Object[numberOfColumns];
                for (int i = 1; i <= numberOfColumns; i++) {
                    // since this is an array, we start at 0
                    
                    // Do not show the values of Blob & Clob
                    if(rsmd.getColumnType(i) == java.sql.Types.BLOB){
                        Blob blobData = rs.getBlob(i);
                        // Added check to fix
                        // 5064319 : ServerNav> View Data on a Db2 Table with 
                        // BLOB column throws NPE
                        if ( blobData != null )
                            row[i - 1] = "[BLOB of size " + blobData.length() + "]"; //NOI18N
                    }else if(rsmd.getColumnType(i) == java.sql.Types.CLOB){
                        Clob clobData = rs.getClob(i);
                        // Added check to fix
                        // 5064319 : ServerNav> View Data on a Db2 Table with 
                        // BLOB column throws NPE
                        if ( clobData != null )
                            row[i - 1] = "[CLOB of size " + clobData.length() + "]"; //NOI18N
                    }
                    // convert timestamp to the current locale
                    else if ( rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP){
                        Timestamp timeStampData = rs.getTimestamp(i);

                        // Added check to fix 
                        // 5062947 : Null Date Breaks Viewing Table Data
                        if ( timeStampData != null ) {
                            row[i - 1] = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM).format( timeStampData ) ;
                        }
                    }
                    // convert date to the current locale
                    else if ( rsmd.getColumnType(i) == java.sql.Types.DATE){
                        Date dateData = rs.getDate(i);
                        // Added check to fix 
                        // 5062947 : Null Date Breaks Viewing Table Data
                        if ( dateData != null ) {
                            row[i - 1] = DateFormat.getDateInstance(DateFormat.SHORT).format( dateData ) ;
                        }
                    }
                    // convert time to the current locale
                    else if ( rsmd.getColumnType(i) == java.sql.Types.TIME){
                        Time timeData = rs.getTime(i);
                        // Added check to fix 
                        // 5062947 : Null Date Breaks Viewing Table Data
                        if ( timeData != null ) {
                            row[i - 1] = java.text.DateFormat.getTimeInstance(DateFormat.MEDIUM).format(timeData );
                        }
                    }
                    else {
                          row[i - 1] = rs.getObject(i);
                    }
                }
                resultTableModel.addRow(row);
                count++;
            }
            if ( resultsTruncated && infoMsgIfTruncated ) {
                String msg = NbBundle.getMessage(QueryBuilderResultTable.class, "MAX_ENTRIES_DISPLAYED", Integer.toString(maxEntries));       // NOI18N
                NotifyDescriptor d =
                new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.INFORMATION_MESSAGE); // NOI18N
                DialogDisplayer.getDefault().notify(d);
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
            Log.err.log(ErrorManager.EXCEPTION, "Exception - unable to build table"); // NOI18N
        }finally{
            if (rs != null){
                try{
                    rs.close();
                }catch (Exception exc){
                    
                }
            }
        }
        return resultsTruncated ;
        
    }
}
