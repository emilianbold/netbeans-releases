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
import org.netbeans.modules.db.sql.visualeditor.ui.ConnectionStatusPanel;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Cursor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.RepaintManager;

import javax.swing.text.DefaultEditorKit;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx ;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.db.sql.visualeditor.Log;

import org.netbeans.modules.db.sql.visualeditor.querymodel.ColumnProvider;
import org.netbeans.modules.db.sql.visualeditor.querymodel.Column;
import org.netbeans.modules.db.sql.visualeditor.querymodel.JoinTable;
import org.netbeans.modules.db.sql.visualeditor.querymodel.OrderBy;

import org.netbeans.modules.db.sql.visualeditor.parser.ParseException;
import org.netbeans.modules.db.sql.visualeditor.parser.TokenMgrError;
// import com.sun.rave.designtime.DesignBean;

// import org.netbeans.modules.visualweb.sql.DatabaseMetaDataHelper;
// import org.netbeans.modules.visualweb.sql.TableMetaData;

import java.sql.PreparedStatement;
import java.sql.ParameterMetaData;

/**
 * The top-level class for the QueryBuilder.
 *
 * This is the class that gets invoked from outside, to create QueryBuilder
 * instances.  It also handles communication between the QueryBuilder panes and the
 * data source (RowSet, Connection), including retrieving metadata, executing
 * queries, and retrieving result sets.
 *
 * @author  Sanjay Dhamankar, Jim Davidson, then hacked apart, chewed up, by jfbrown, but still needs more!
 */
public class QueryBuilder extends TopComponent
        implements
        ClipboardOwner,
        KeyListener,
        ColumnProvider {

    // this stores the cache of query build objects
    private static Map                  qbMap = new HashMap();

    private String lastQuery;

    // DO NOT CHANGE the next line.  Caching should not be implemented in this class.
    private static final boolean CACHE_QueryBulderInstances = false ;

    // the boolean below is used to determine if we need to store
    // the query in the backing file. First time when 'generateText' is
    // called we avoid saving the query in the backing file as
    // this unnecessarily sets the save buttons ON.
    private boolean        firstTimeGenerateText = true;

    // Provide package access on these, for use by other classes in the Query Builder

    QueryBuilderPane                    _queryBuilderPane;
    QueryModel                          _queryModel;
    boolean                             _updateModel = true;
    boolean                             _updateText = true;
    boolean                             _graphicsEnabled = true;

    // Private variables

    private boolean                     DEBUG = false;
    // true if database is connected, false otherwise.

    // private DatabaseMetaData            _databaseMetaData;
    // private DatabaseMetaDataHelper      _dbmdh;
    
    private SqlStatementMetaDataCache   metaDataCache = null ;

    // all the schema names in the datasource.
    // private List                        _schemaNames    = null;
    // all the table/view names in the datasource.
    // private List                        _tableNames     = null;
    // all the column names for each table/view name in the datasource.
    // private List                        _columnNames    = null;

    // used to make sure that the _tableNames is initialized only once.
    // private List                        _tableColumns ;

    private String                      _parseErrorMessage = null ;

    // used for syntax highlighting
    public boolean isSchemaName( String schemaName ) {
        String[] schemas = metaDataCache.getSchemas() ;
        for (int i = 0 ; i < schemas.length ; i++ ) {
            if ( schemas[i].equals(schemaName)) {
                Log.log(" found schema name "+schemaName) ;
                return true ;
            }
        }
        return false ;
    }

    public boolean isTableName( String tableName ) {
        try {
            String x = checkTableName( tableName ) ;
            if ( x != null )
                return true ;
        } catch( SQLException se) {
            // exception handled elsewhere.
        }
        return false ;
    }

    public boolean isColumnName( String columnName ) {
        
        try {
            return metaDataCache.getAllColumnNames().containsKey(columnName) ;
        }catch( SQLException se) {
            // exception handled elsewhere.
        }
        return false ;
    }

    /////////////////////////////////////////////////////////////////////////
    // Delete support
    /////////////////////////////////////////////////////////////////////////

    /** delete action performer */
    private final transient DeleteActionPerformer deleteActionPerformer = new DeleteActionPerformer();

    /** copy action performer */
    protected final transient CopyCutActionPerformer copyActionPerformer =
        new CopyCutActionPerformer(true);

    /** cut action performer */
    protected final transient CopyCutActionPerformer cutActionPerformer =
        new CopyCutActionPerformer(false);

    
    // Implements ClipboardOwner
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    boolean isSelectionEmpty() {
        Node[] nodes = getActivatedNodes();
        if ( (nodes != null) && (nodes.length != 0) )
            return false;
        else
            return true;
    }

    private boolean isActivated() {
        return this == TopComponent.getRegistry().getActivated();
    }

    /*** do not save across IDE session ***/
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER ;
    }

    public String preferredID() {
        return "QueryEditor" ; //NOI18N
    }



    /** Called when this window is activated: make delete
     * sensitive based on whether or not anything is selected and whether
     * the clipboard contains something we can absorb. */
    public void activateActions() {
        if (isSelectionEmpty()) {
            disableDelete();
        } else {
            enableDelete();
        }
        // for now cut / copy is always disabled.
        disableCutCopy();
    }

    /** Called when the when the component is deactivated. We no longer
     * allow our paste types to be invoked so clear it - get rid of
     * the action performers as well. */
    public void deactivateActions() {
        if (deleteActionPerformer != null) {
//            delete.setActionPerformer(null);
        }
    }

    /** Called when the selection is non zero and the component is active:
     * enable cut, copy and delete */
    void enableDelete() {
        if (!isActivated()) {
            return;
        }
        deleteActionPerformer.setEnabled(true);
    }

    /** Called when the selection is removed: disable delete */
    void disableDelete() {
        if (!isActivated()) {
            return;
        }
        deleteActionPerformer.setEnabled(false);
        DeleteAction da = (DeleteAction)SystemAction.get(DeleteAction.class);
        da.setEnabled(false);
    }

    /** Called when the selection is removed: disable cut/copy */
    void disableCutCopy() {
        if ( DEBUG )
            System.out.println(" disableCutCopy called. " + "\n" ); // NOI18N
        // Fix 6265915 Copy/Cut menu items are always enabled
        cutActionPerformer.setEnabled(false);
        copyActionPerformer.setEnabled(false);
    }

    /** Class which performs delete action */
    class DeleteActionPerformer extends AbstractAction implements ActionPerformer {

        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        // Perform delete action.
        public void performAction(SystemAction action) {
            // We run into deadlocks without this; !#$!@#!@ ModuleActions thread
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    deleteSelection();
                }
            }
            );
        }
    }

    /** Class which performs copy and cut actions */
    class CopyCutActionPerformer extends AbstractAction implements ActionPerformer {
        /** determine if adapter is used for copy or cut action. */
        boolean isCopy;

        /** Create new adapter */
        public CopyCutActionPerformer(boolean b) {
            isCopy = b;
        }

        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        /** Perform copy or cut action. */
        public void performAction(SystemAction action) {
            // for now do nothing
        }
    }


    /** Remove the currently selected components */
    void deleteSelection() {
        if ( DEBUG )
            System.out.println(" deleteSelection called. " + "\n" ); // NOI18N
        java.awt.KeyboardFocusManager kbfm = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager();
        java.awt.Component c = kbfm.getFocusOwner();
        if ( c != null ) {
            java.awt.Container p = c.getParent(); 
            while ( p != null ) {
                if ( p instanceof QueryBuilderGraphFrame ) {

                    Node[] nodes = getActivatedNodes();
                    if (nodes == null || nodes.length == 0) {
                        return;
                    }
                    for (int i = 0; i < nodes.length; i++) {
                        if ( ( nodes[i] instanceof CondNode ) ) {
                            _queryBuilderPane.getQueryBuilderGraphFrame().removeNode((CondNode)nodes[i]);
                        } else if ( ( nodes[i] instanceof JoinNode ) ) {
                            _queryBuilderPane.getQueryBuilderGraphFrame().removeNode((JoinNode)nodes[i]);
                        } else if ( ( nodes[i] instanceof TableNode ) ) {
                            _queryBuilderPane.getQueryBuilderGraphFrame().removeNode((TableNode)nodes[i]);
                        }
                    }
                }
            p = p.getParent();
            }
        }
    }

    protected void installActions(ActionMap map, InputMap keys) {
       /*
        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer);
        // Paste still done the old way...
        //map.put(DefaultEditorKit.pasteAction, pasteActionPerformer);
        */
        map.put("delete", deleteActionPerformer); // or false
        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer); 

        /*
        // Popup menu from the keyboard
        map.put ("org.openide.actions.PopupAction",
                new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        showKeyboardPopup();
                    }
                });
            }
        });

        keys.put(KeyStroke.getKeyStroke("control C"), DefaultEditorKit.copyAction);
        keys.put(KeyStroke.getKeyStroke("control X"), DefaultEditorKit.cutAction);
        keys.put(KeyStroke.getKeyStroke("control V"), DefaultEditorKit.pasteAction);
         */
        keys.put(KeyStroke.getKeyStroke("DELETE"), "delete");
    }


    /**
     * Static factory method, added for access from RowSet
     * @Return a QueryBuilder instance, either new or retrieved from the Map
     */
    public static Component openCustomizerPanel( SqlStatement sqlStatement) {

        Log.err.log(ErrorManager.INFORMATIONAL,
                "Entering QueryBuilder.openCustomizerPanel"); // NOI18N

	showBusyCursor( true );

        QueryBuilder qb ;
        try {
            qb = new QueryBuilder(sqlStatement);
        } catch (SQLException sqle ) {
            qb = null ;
            // TODO:  popup an error dialog.
            ConnectionStatusPanel csp = new ConnectionStatusPanel() ;
            csp.configureDisplay(sqlStatement.getConnectionInfo(), false,sqle.getLocalizedMessage(),  "", 0, false ) ;
            // csp.setGeneralInfo("") ;
            csp.displayDialog( sqlStatement.getConnectionInfo() ) ;
        }
        final QueryBuilder queryBuilder = qb ;
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if ( queryBuilder != null) {
                    queryBuilder.open();
                    queryBuilder.requestActive();
                }
                showBusyCursor( false );
            }
        }) ;

        queryBuilder.getTextAreaFocusInvokeLater();

        return queryBuilder;
    }

    //  --- changes for designtime/insync avoidance.
    private SqlStatement sqlStatement = null ;
    // private Connection   connection = null ;

    private QueryBuilder(SqlStatement sqlStatement) throws SQLException {

        Log.err.log(ErrorManager.INFORMATIONAL, "Entering QueryBuilder ctor"); // NOI18N

        // Record the bean that created us, and the unique name
        this.sqlStatement = sqlStatement ;
       
        this.metaDataCache = sqlStatement.getMetaDataCache() ;
        
        String title = sqlStatement.getTitle() ;
        
        // Set the name to display
        setName(title);
        setDisplayName(title);

        setLayout(new java.awt.BorderLayout());

        ImageIcon imgIcon =
                new ImageIcon(getClass().getResource("/org/netbeans/modules/db/sql/visualeditor/resources/query-editor-tab.png")); // NOI18N
        if (imgIcon != null)
            setIcon(imgIcon.getImage());

        _queryBuilderPane = new QueryBuilderPane(this);

        // Add the pane to the end of the QueryBuilder container
        add(_queryBuilderPane);

        addKeyListener(this);
    }

    void getGraphFrameCanvasFocus() {
        _queryBuilderPane.getQueryBuilderGraphFrame().getCanvasFocus ();
    }

    void getTextAreaFocusInvokeLater() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus( true );
                _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocusInWindow ();
            }
        }) ;
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
        handleKeyPress(e);
    }

    public void handleKeyPress(KeyEvent e) {
        if( e.isAltDown() ) {
            int code = e.getKeyCode();
            switch(code) {
                // diagram pane
                case KeyEvent.VK_1:
                    if ( DEBUG )
                        System.out.println(" Alt + 1 pressed. "); // NOI18N
//                    _queryBuilderPane.getQueryBuilderGraphFrame().getFocus ();
                    getGraphFrameCanvasFocus();
                    break;
                    // grid pane
                case KeyEvent.VK_2:
                    if ( DEBUG )
                        System.out.println(" Alt + 2 pressed. "); // NOI18N
                    if ( _queryBuilderPane.getQueryBuilderInputTable().getRowCount() > 0 ) {
                        _queryBuilderPane.getQueryBuilderInputTable().setRowSelectionInterval(0, 0);
                        _queryBuilderPane.getQueryBuilderInputTable().requestFocus( true );
                    }
                    break;
                    // SQL text pane
                case KeyEvent.VK_3:
                    if ( DEBUG )
                        System.out.println(" Alt + 3 pressed. "); // NOI18N
                    _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus( true );
                    break;
                    // Result Pane
                case KeyEvent.VK_4:
                    if ( DEBUG )
                        System.out.println(" Alt + 4 pressed. "); // NOI18N
                    _queryBuilderPane.getQueryBuilderResultTable().requestFocus( true );
                    break;
            }
        }
    }

    public SqlStatement getSqlStatement() {
        return this.sqlStatement ;
    }
    
    List getCachedAllTablesInDataSource() throws SQLException {
        return metaDataCache.getTables() ;
        /*
        if (_tableNames == null ) {
            _tableColumns = createTableColumns() ;
        }
        return _tableNames;
         **/
    }

    /**
     *   Check if the table name exists
     *   Case is ignored while searching
     *   If table does not exist then return null
     *   else return the same name if it exactly matches
     *   else return the name from the database
     *
     *   tableName can be schema.table or just table - look for both.
     */

    String checkTableName( String tableName ) throws SQLException {

        if ( tableName == null || tableName.length() < 1) {
            return tableName ;
        }
        Log.log("checkTableName called. tableName = " + tableName); // NOI18N
        
        String[] descrip = parseTableName(tableName) ;
        String paramSchemaName = descrip[0] ;
        String paramTableName = descrip[1] ;
        
        if ( paramSchemaName != null ) {
            return checkFullTableName( tableName ) ;
        }

        String returnTable = null ;
        
        List tables = metaDataCache.getTables() ;
        // now search for the tablename in the list.
        for ( Iterator i = tables.iterator(); i.hasNext() ;  ) {
            // first check if the table name exists as is
            String fullNameDb = (String)i.next() ;
            String tableNameDb = parseTableName(fullNameDb)[1] ;
            if ( tableNameDb.equalsIgnoreCase( paramTableName ) ) {
                returnTable = fullNameDb ;
                break ;
            }   
        }
        
        if ( returnTable == null ) {
            String fullAliasTableName = _queryModel.getFullTableName(paramTableName) ;
            if ( fullAliasTableName != null && tableName.equals(fullAliasTableName)) {
                return null;

            } else if ( fullAliasTableName != null ) {
                 return checkTableName( fullAliasTableName ) ;
            }
        }

        if ( returnTable != null )
            metaDataCache.getColumnNames(returnTable ) ;

        // table name was not found
        return returnTable ;
    }

    /**
     *   Check if the full table name exists in the _tableColumns
     *   Case is ignored while searching
     *   If table does not exist then return null
     *   
     *   return the same name if it exactly matches
     *   if case differs, return the full name from the database
     *   if it's an alias and there's a cases-insenstive match, return the full name from the database
     *
     *   This function is called only from checkFrom.
     *   Cases to be considered :
     *   case 1 : <schema_name>.<table_name>
     *   case 2 : <table_name> SHOULD NOT EXIST.
     *   case 3 : <alias_table_name>
     *
     *   return the
     *
     */

    String checkFullTableName( String fullTableName ) throws SQLException {

        Log.log("checkFullTableName called. tableName = " + fullTableName ); // NOI18N
 
        String returnTable = null ;

        if ( parseTableName(fullTableName)[0] == null ) {
            // no schema name, so fullTableName is really just a tableName.
            return checkTableName(fullTableName) ;
        }

        List tables = metaDataCache.getTables() ;
        for ( Iterator i = tables.iterator(); i.hasNext() ;  ) {
            // first check if the table name exists as is
            String fullNameDb = (String)i.next() ;
            if ( fullNameDb.equalsIgnoreCase( fullTableName ) ) {
                returnTable = fullNameDb ;
                break ;
            }   
        }
        
        // Load the column cache for this table.
        if ( returnTable != null ) metaDataCache.getColumnNames(returnTable ) ;
        
        return returnTable ;
    }


    /**
     *   Check if the column name exists in the _tableColumns
     *   case is ignored while searching
     *   if column does not exist then return null
     *   else return the same name if it exactly matches
     *   else return the name from the database
     */
    String checkColumnName( String tableName, String columnName ) throws SQLException {
        Log.log("checkColumnName() " + tableName  + ", " + columnName  ); // NOI18N
        
        String tabName = checkTableName( tableName ) ;
        
        List columns = metaDataCache.getColumnNames(tabName) ;
        
        if ( columns == null ) return null ;
        
        for ( int k = 0; k < columns.size(); k++ ) {
            String  columnDB = (String) columns.get(k);
            // first check if the column name exists "as is"
            if ( columnName.equals( columnDB ) ) {
                return columnName;
            }
            // otherwise compare ignoring case, if matched return the
            // name from database
            else if ( columnName.equalsIgnoreCase( columnDB ) ) {
                return  columnDB ;
            }
        }
        
        // column name was not found
        return null;
    }

    /**
     *  Given a column and table name checks if the table name stored in the
     *  column matches with the one in the database.
     *  Updates the column name with the one in the database and returns true
     *  false otherwise.
     */

    boolean checkColumnNameForTable( Column col, String tableName ) {
        String columnName = col.getColumnName();
        
        Log.log("checkColumnNameForTable for " + tableName + " . " + columnName ); // NOI18N
        
        String fullTableNameFromAlias = _queryModel.getFullTableName( tableName );
        if ( fullTableNameFromAlias != null ) {
            tableName = fullTableNameFromAlias ;
        }
        boolean retVal = false ;
        
        // TODO JFB should not catch this.
        List cols ;
        String checkedTable ;
        try {
            checkedTable = checkTableName( tableName ) ;
            if (checkedTable == null) return false ;
            cols = metaDataCache.getColumnNames(checkedTable) ;
        } catch (SQLException sqle) {
            Log.log("  ** problems getting metadata " + sqle.getMessage()) ;
            return false ;

        }
        if ( "*".equals(columnName)) { // NOI18N
            retVal = true ;                    
            if ( fullTableNameFromAlias == null && ! ( checkedTable.equals(col.getTableSpec()) ) ) {
                col.setTableSpec(col.getTableSpec(), checkedTable ) ;
                Log.log( " adjust table to " + checkedTable) ;
            }
        } else {
            for ( int icnt = 0 ; icnt < cols.size() ; icnt++ ) {
                if ( columnName.equalsIgnoreCase( (String)cols.get(icnt))) {
                    col.setColumnName(col.getColumnName(), (String)cols.get(icnt) );
                    Log.log( " adjust colname to " + (String)cols.get(icnt) ) ;
                    if ( col.getTableSpec() == null ) {
                        col.setTableSpec(col.getTableSpec(), checkedTable ) ;
                        Log.log( " adjust table to " + checkedTable) ;
                    }
                    retVal = true ;
                    break ;
                }
            }
        }
        
        Log.log("checkColumnNameForTable found="+retVal ); // NOI18N
        
        return retVal ;
        
        /***
        for ( int i = 0; i < _tableColumns.size(); i++ ) {
            TableColumns tableColumn = (TableColumns) _tableColumns.get(i);
            String _tableName = tableColumn.getTableName();
            // first check if the table name exists "as is"
            // table name must already be valid using checkTableName
            // reset the column's table spec.
            if ( _tableName.equals( tableName ) ) {

                List columns = tableColumn.getColumns();
                for ( int k = 0; k < columns.size(); k++ ) {
                    String _columnName = (String) columns.get(k);
                    if ( ( _columnName.equals( columnName ) ) ||
                            ( _columnName.equalsIgnoreCase( columnName ) ) ) {
                        // change the column's table name
                        col.setTableSpec(col.getTableSpec(), tableName);
                        // change the column's name to the correct one.
                        col.setColumnName(col.getColumnName(), _columnName);
                        return true;
                    }
                }
            } else {
                // check if the tableName is actually an alias
                String fullTableNameFromAlias =
                        _queryModel.getFullTableName( tableName );
                if ( fullTableNameFromAlias != null ) {
                    // tableName is an alias
                    if ( _tableName.equals( fullTableNameFromAlias ) ) {
                        if ( ! tableColumn.columnsLoaded() ) {
                            loadColumns( tableColumn );
                        }
                        List columns = tableColumn.getColumns();
                        for ( int k = 0; k < columns.size(); k++ ) {
                            String _columnName = (String) columns.get(k);
                            if ( ( _columnName.equals( columnName ) ) ||
                                    ( _columnName.equalsIgnoreCase( columnName ) ) ) {
                                // change the column's table name
                                col.setColumnTableName(fullTableNameFromAlias);
                                // change the column's corr name
                                col.setColumnCorrName(tableName);
                                // change the column's name to the correct one.
                                col.setColumnName(col.getColumnName(), _columnName);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
         ***/
    }

    // checks the table name and column name given a col.
    // if possible corrects the column name and table name
    // otherwise returns false, the caller is supposed to give an error message
    boolean checkTableColumnName( Column col ) throws SQLException {

        String tableSpec = col.getTableSpec() ;
        String tableName = col.getFullTableName() ;
        String colName = col.getColumnName() ;
        
        Log.log("checkTableColumnName called. " + " tableSpec = " + tableSpec  +
                    " tableName = " + tableName + " . " + colName  ); // NOI18N        
        
        if ( "*".equals(col.getColumnName() ) && tableSpec == null  ) {
            // Column name was "*" with no tableSpec - assume it's OK.
            Log.log("checkTableColunName col=*, notable ") ;
            return true ; //NOI18N
        }
        String checkedTableName = checkTableName( tableSpec ) ;

        String fullTableNameFromAlias = null ;
//        if ( checkedTableName == null && tableSpec != null ) {
            // why the above check ? This will not set fullTableNameFromAlias
            // regression
            // http://daning.sfbay/cvsweb/queryeditor/src/com/sun/rave/queryeditor/querybuilder/QueryBuilder.java.diff?r1=1.133&r2=1.134&cvsroot=/cvs/rave
            fullTableNameFromAlias = _queryModel.getFullTableName( tableSpec );
//        }
        Log.log("checkTableColumnName called. " +
                    " checkedTableName = " + checkedTableName  +
                    " fullTableNameFromAlias = " + fullTableNameFromAlias ); // NOI18N
        if ( checkedTableName == null ) {
            // table not found
            return false; // let the caller display the error
        }

        // table has alias set corr name and set table name.
        // Added the following check to fix :
        //     5061214  Cannot parse a query with alias.
        else if ( ( fullTableNameFromAlias != null ) &&
                ( ! fullTableNameFromAlias.equalsIgnoreCase( tableSpec ) ) ) {
            if (DEBUG)
                System.out.println("setColumnTableName called. " +
                        " checkedTableName = " + checkedTableName  + 
                        " tableSpec = " + tableSpec  + 
                        " fullTableNameFromAlias = " + fullTableNameFromAlias + "\n" ); // NOI18N
            col.setColumnTableName( checkedTableName );
            col.setColumnCorrName( tableSpec );
        } else if ( ! checkedTableName.equals( tableName ) ) {
            // table found but maybe in a wrong case, replace
            // it in the querymodel
            if (DEBUG)
                System.out.println("setTableSpec called. " +
                        " checkedTableName = " + checkedTableName  + "\n" ); // NOI18N
            col.setTableSpec( tableName, checkedTableName );
        }

        String columnName = col.getColumnName();

        if (columnName.equals("*")) return true ;
        
        String checkedColumnName = checkColumnName( checkedTableName,
                columnName ) ;
        if (DEBUG)
            System.out.println("column Name = " + columnName  + "\n" // NOI18N
                    + "checked column Name = " + checkedColumnName  + "\n" ); // NOI18N
        if ( checkedColumnName == null ) {
            // column not found
            return false; // let the caller display the error
        } else if ( ! checkedColumnName.equals( columnName ) )  {
            if (DEBUG)
                System.out.println("set column name called. oldColumnName = " + columnName + //NOI18N
                        " newColumnName = " + checkedColumnName  + "\n" ); // NOI18N
            // column found but maybe in a wrong case, replace
            // it in the querymodel
            col.setColumnName( columnName, checkedColumnName );
        }

        return true;
    }

    /****
     * Check the database connection.
     * If no connection, ask to retry.
     * if user doesn't want to retry,
     * then disable the query editor
     */
    public boolean checkDatabaseAndDisable(String query) {
        if ( query == null ) query = _queryBuilderPane.getQueryBuilderSqlTextArea().getText() ;
        if ( checkDatabaseConnection() == false ) {
            Log.log("checkDatabaseConnection returns false ... \n " ); // NOI18N
            // If we don't have a valid connection, disable all visual editing.
            disableVisualEditing(query);
            return false;
        }
        return true ;
    }
    /**
     * Parse the query and regenerate all the panes
     * If parsing fails, raise an notification and do nothing else
     * If parsing succeeds, return true, false otherwise.
     * @param text query to parse
     */
    boolean populate(String query) {
        return populate( query, false) ;
    }

    boolean populate(String query, boolean forceParse ) {

        Log.log("Entering populate, forceParse: " + forceParse); // NOI18N

        if ( ! forceParse ) {
            if ( query.trim().equals( _queryBuilderPane.getQueryBuilderSqlTextArea().getText().trim())) {
                // no change, just return.
                Log.log("  skipping populate(), no change") ; //NOI18N
                return true ;
            }
        }

        // Fix CR 6275870 Error when parsing invalid SQL
        if ( query.trim().equals( lastQuery ) ) {
            // no change, just return.
            Log.log("  skipping populate(), no change") ; //NOI18N
            return true ;
        }
        else {
            lastQuery = new String (query.trim());
        }

        // if ( ! checkDatabaseAndDisable( query )) return false ;

        // First parse the query, and report any exception
        try {
            parseQuery(query);

            // if the parsing is successful and if the user modified query
            // by hand, then check for all the table names and column names.
            // if there is an error, give a message to the user and return false
            // else if there is only error in case and/or ommission of
            // tablename etc fix the query model with the correct values.

            if (!checkQuery()) 
                return false;

            _queryBuilderPane.getQueryBuilderGraphFrame().setQBGFEnabled( true ) ;
            _queryBuilderPane.setQueryBuilderInputTableEnabled( true ) ;
            _queryBuilderPane.getQueryBuilderGraphFrame().setTableColumnValidity(false) ;
            _queryBuilderPane.getQueryBuilderGraphFrame().setGroupBy(_queryModel.hasGroupBy() );
            _graphicsEnabled=true;
            _queryBuilderPane.getQueryBuilderSqlTextArea().setQueryText(query);
        } catch (ParseException pe) {

            Log.err.log(ErrorManager.ERROR, "Parse error: " + pe.getLocalizedMessage());  // NOI18N
            promptForContinuation(pe.getMessage(), query);
            return false;
        } catch (TokenMgrError tme) {

            Log.err.log(ErrorManager.ERROR, "Parse error: " + tme.getLocalizedMessage());  // NOI18N
            promptForContinuation(tme.getMessage(), query);
            return false;

        } catch (SQLException sqe) {
            lastException = sqe ;
            Log.err.log(ErrorManager.ERROR, "Parse error: " + sqe.getLocalizedMessage());  // NOI18N
            promptForContinuation(sqe.getMessage(), query);
            return false;
        }

        _parseErrorMessage = null ;
        // If parsing was successful...

        // ...generate the editor panes
        this.generate();

        // ...save the sql command.
        saveSqlCommand(query);

        _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus();

        return true;
    }


    // Ask the user whether to Retry&Continue or Cancel&Continue

    private boolean promptForContinuation(String msg, String query) {

        // There could be an error or the typed SQL may not be SQL-92
        // compliant. Give the user an option to keep the query and test by
        // running it. If the user is satisfied then this could reflect in the
        // backing file. In that case the tables may not be displayed in the
        // graph properly. If the user thinks that there is a genuine error,
        // then the previous good query will be restored.

        Object[] options = {
            NbBundle.getMessage(QueryBuilder.class, "CONTINUE"), // "Continue"
            NbBundle.getMessage(QueryBuilder.class, "CANCEL")    // "Cancel"
        };
        if ( _queryBuilderPane.getQueryBuilderSqlTextArea().queryChanged() ) {
            int val = JOptionPane.showOptionDialog(
                    this,
                    (msg + "\n\n" +
                    NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR_MESSAGE")
                    +
                    NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR_MESSAGE_PROMPT")),
                    NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (val==JOptionPane.NO_OPTION) {    // Cancel - Revert to previous
                Log.err.log(ErrorManager.WARNING, "Query execution canceled"); // NOI18N
                _queryBuilderPane.getQueryBuilderSqlTextArea().restoreLastGoodQuery();
                _queryBuilderPane.getQueryBuilderGraphFrame().setQBGFEnabled( true ) ;
                _queryBuilderPane.setQueryBuilderInputTableEnabled( true ) ;
                _graphicsEnabled=true;
                _parseErrorMessage = null ;
            } else {    // Continue - Disable visual editing
                _parseErrorMessage = NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR_MESSAGE"); // NOI18N
                disableVisualEditing(query);
            }
        } else {
            // display the message in the graph area
            _parseErrorMessage = NbBundle.getMessage(QueryBuilder.class, "PARSE_ERROR_MESSAGE"); // NOI18N
            disableVisualEditing(query);
        }
        return false;
    }


    // Disable the graph and grid panes, leaving only the text pane.
    // Used when either a parse fails, or the database is down
    void disableVisualEditing(String query)    {
        _graphicsEnabled=false;
        _queryBuilderPane.clear();
        _queryBuilderPane.getQueryBuilderGraphFrame().setQBGFEnabled( false ) ;
        _queryBuilderPane.setQueryBuilderInputTableEnabled( false ) ;

        String command = sqlStatement.getCommand();
        if ( query != null && query.trim().length() != 0) {
            _queryBuilderPane.getQueryBuilderSqlTextArea().setQueryText(query);
            setSqlCommand(query) ;
        }
        else {
            _queryBuilderPane.getQueryBuilderSqlTextArea().setQueryText(command);
        }
    }

    void showTableColumnNameError( String error ) {
        String msg = NbBundle.getMessage(QueryBuilder.class, "TABLE_COLUMN_NAME_ERROR");
        NotifyDescriptor d =
            new NotifyDescriptor.Message( error + " : " + msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        _parseErrorMessage = error + " : " + msg + "\n\n" ;
        String query = getUnformattedSqlString();
        disableVisualEditing(query);
    }


    boolean checkQuery()  throws SQLException {

        if ( ( getUnformattedSqlString()  != null ) ||
             ( _queryBuilderPane.getQueryBuilderGraphFrame().checkTableColumnValidity() ) ) {

            // from
            if ( ! checkFrom() ) return false;

            // We  were calling a function to replace the "*" with the
            // column names of the tables immediately after parsing the
            // query. With the introduction of routines to check the
            // table and column names and to resolve them properly,
            // if the from clause contains the table names not as they
            // appear in database, we change them to match with those
            // in the database. e.g. trip becomes "TRAVEL.TRIP". This
            // is done in function checkFrom which gets called after
            // parsing is done. In the case of replaceStar being called
            // immediately after parsing, the column names and table
            // names were resolved without checking for their validity
            // in the database. I have changed this sequence as follows:
            // 1. First parse the query.
            // 2. Check for table names in the from clause. If the names
            //    only differ in case or if they are missing schema name
            //    then they are corrected in the datamodel.
            // 3. These corrected table names are used to resolve the
            // column names in SQLs like "select * from trip" when we
            // call replaceStar() function.  This should fix the problem
            // where "select * from TRIP" was parsed properly but
            // "select * from trip" used to give errors.
            // Bug Id : 4962093


            // we need to replace star after we validate all the table names
            // in the from list.
            _queryModel.replaceStar(this);

            // select
            if ( ! checkSelect() ) return false;

            // where
            if ( ! checkWhere() ) return false;
            
            // groupby
            if ( ! checkGroupBy() ) return false;

            // having
            if ( ! checkHaving() ) return false;

            // orderby
            if ( ! checkOrderBy() ) return false;

        } else {
            // we are not validating the fromList as the query has not been
            // changed by the user.
            _queryModel.replaceStar(this);
        }
        return true;
    }


    // Check the tables specified in the FROM clause (plus any columns
    // specified in join conditions), against the DB Schema

    boolean checkFrom() throws SQLException {

        if (DEBUG)
            System.out.println("checkFrom called... \n " ); // NOI18N
        
        // we could reuse this to find the tablename if the user
        // only specifies "select 'column_name' from 'table_name'"
        List fromTables;
        // from
        if ( _queryModel.getFrom() != null ) {

            fromTables = _queryModel.getFrom().getTableList();
            for ( int i = 0; i < fromTables.size(); i++ ) {

                String fromTableName = (String)
                ( (JoinTable) fromTables.get(i) ).getFullTableName();
                String fromTableSpec = (String)
                ( (JoinTable) fromTables.get(i) ).getTableSpec();
                String checkedFullTableName = checkFullTableName( fromTableName ) ;

                if (DEBUG)
                    System.out.println("checkFullTableName called fromTableName = " + fromTableName + " returns " +
                                       checkedFullTableName + " \n " ); // NOI18N
                if ( checkedFullTableName == null ) {
                    // table not found, give an error
                    showTableColumnNameError( fromTableName ) ;
                    return false;
                } else if ( ! checkedFullTableName.equals( fromTableName ) ) {
                    // table found but maybe in a wrong case, replace
                    // it in the querymodel's from
                    if (DEBUG)
                        System.out.println(
                                " fromTableName = " + fromTableName  +  // NOI18N
                                " fromTableSpec = " + fromTableSpec  +  // NOI18N
                                " \n" ); // NOI18N
                    _queryModel.getFrom().setTableSpec(
                            fromTableSpec, checkedFullTableName );
                }

                // now check the columns in the condition if any.
                List fromColumns = new ArrayList();
                ( (JoinTable) fromTables.get(i) ).getReferencedColumns(fromColumns);
                for ( int j = 0; j < fromColumns.size(); j++ ) {
                    Column fromColumn = (Column) fromColumns.get(j);
                    if (! checkTableColumnName( fromColumn)) {
                        showTableColumnNameError(  fromColumn.getColumnName() );
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Check the tables specified in the SELECT clause, against the DB Schema

    boolean checkSelect()  throws SQLException  {
        if (DEBUG)
            System.out.println("checkSelect called. _queryModel.getSelect() = " + _queryModel.getSelect() + "\n " ); // NOI18N
        if ( _queryModel.getSelect() != null ) {
            ArrayList selectColumns = new ArrayList();
            _queryModel.getSelect().getReferencedColumns(selectColumns);
            if ( ! checkColumns( selectColumns ) )
                return false;
        }
        return true;
    }

    boolean checkWhere()  throws SQLException {
        if (DEBUG)
            System.out.println("checkWhere called... \n " ); // NOI18N
        if ( _queryModel.getWhere() != null ) {
            ArrayList whereColumns = new ArrayList();
            _queryModel.getWhere().getReferencedColumns(whereColumns);
            if ( ! checkColumns( whereColumns ) ) return false;
        }
        return true;
    }

    boolean checkGroupBy()  throws SQLException {
        if (DEBUG)
            System.out.println("checkGroupBy called... \n " ); // NOI18N
        if ( _queryModel.getGroupBy() != null ) {
            ArrayList groupByColumns = new ArrayList();
            _queryModel.getGroupBy().getReferencedColumns(groupByColumns);
            if ( ! checkColumns( groupByColumns ) ) return false;
        }
        return true;
    }


    boolean checkHaving()  throws SQLException {
        if (DEBUG)
            System.out.println("checkHaving called... \n " ); // NOI18N
        if ( _queryModel.getHaving() != null ) {
            ArrayList havingColumns = new ArrayList();
            _queryModel.getHaving().getReferencedColumns(havingColumns);
            if ( ! checkColumns( havingColumns ) ) return false;
        }
        return true;
    }


    boolean checkOrderBy()  throws SQLException {
        if (DEBUG)
            System.out.println("checkOrderBy called... \n " ); // NOI18N
        OrderBy orderBy = _queryModel.getOrderBy();
        if ( orderBy != null ) {
            ArrayList orderByColumns = new ArrayList();
            for ( int i = 0; i < orderBy.getSortSpecificationCount(); i++ ) {
                Column sortColumn = orderBy.getSortSpecification(i).getColumn();
                orderByColumns.add( sortColumn);
            }
            if ( ! checkColumns( orderByColumns ) ) return false;
        }
        return true;
    }

    // Check and correct any columns that may have wrong or missing table
    // specifications. If the column is not found in the database it displays
    // an error message and returns false. If there is just case mismatch
    // this function corrects the column name.
    // The column name could be :
    //    case 1 : <schema_name>.<table_name>.<column_name>
    //    case 2 : <table_name>.<column_name>
    //    case 3 : <alias_table_name>.<column_name>
    //    case 4 : <column_name>
    //

    boolean checkColumns( ArrayList columns )  throws SQLException {
        Log.log ( " checkColumns called. columns.size = " + columns.size() );  // NOI18N
        for ( int i = 0; i < columns.size(); i++ ) {
            Column column = (Column) columns.get(i);
            String columnTableSpec = column.getTableSpec();
            String columnFullTableName = column.getFullTableName();

            // If the user has specified a column without tablename resolve it
            // from the from_table_list.
            // Reversed the first test, switched || to &&.
            if ( ( columnFullTableName == null ) 	&&
                 ( _queryModel.getFrom() != null ) )
            {
                // Check every table in the From list, to see if any have
                // this column
                List fromTables = _queryModel.getFrom().getTableList();
                boolean found=false;
                for ( int j = 0; j < fromTables.size(); j++ ) {
                    String fromTableName = (String) ( (JoinTable) fromTables.get(j) ).getFullTableName();
                    // this could be an alias
                    String fromTableSpec = (String)
                    ( (JoinTable) fromTables.get(j) ).getTableSpec();

                    if (DEBUG)
                        System.out.println(
                                " checkColumns called " +  // NOI18N
                                " fromTableName = " + fromTableName  +  // NOI18N
                                " fromTableSpec = " + fromTableSpec  +  // NOI18N
                                " \n" ); // NOI18N
                    // use the following function to check if fromTableSpec
                    // is in the database. If it is found update the column.
                    if ( checkColumnNameForTable( column, fromTableSpec )) {
                        found=true;
                        break;
                    }
                }
                // Give an error only if all the columns have been checked in all tables
                if (!found) {
                    // table not found, give an error
                    showTableColumnNameError( column.getColumnName() ) ;
                    return false;
                }
            }

            if (! checkTableColumnName(column)
                // Not clear what this test was for; it meant that we only reported an error if
                // the column that failed was the last one
                // && ( i == ( columns.size() - 1 ) )
                )
            {
                showTableColumnNameError( column.getColumnName() ) ;
                return false;
            }
            // the table has an alias, do not check the table name, just
            // check column name
        }
        return true;
    }

    /**
     * Parse the current query (obtained from the RowSet).
     * @param the current query
     */
    void parseQuery(String query) throws ParseException {

        Log.err.log(ErrorManager.INFORMATIONAL, "Entering parseQuery, query: " + query); // NOI18N

        // Initialize the QueryModel object if necessary
        if (_queryModel==null)
            _queryModel = new QueryModel();

        _queryModel.parse(query);
    }


    // Update the command property of the calling thingy.

    public void saveSqlCommand(String query) {

        _queryBuilderPane.getQueryBuilderSqlTextArea().saveLastGoodQuery (_queryBuilderPane.getSqlTextAreaText());

        // Comapare the new query to the existing property value; don't do the
        // setValue operation if there has been no change

        String newQuery = getUnformattedSqlString();
        if (!newQuery.equals( sqlStatement.getCommand())) {
            Log.log("QB:  setting sql command to: " + newQuery) ; //NOI18N
            setSqlCommand( newQuery ) ;
        }
    }

    void setSqlCommand(String query) {
        sqlStatement.setCommand(query) ;
    }
/***
    private void refreshDataBaseMetaData() {
        if (DEBUG) {
            System.out.println(" refreshDataBaseMetaData() called " + "\n" ); // NOI18N
        }
        try {
            _databaseMetaData = null;
            checkMetaData();
        } catch (SQLException e) {
            reportDatabaseError("DATABASE_ERROR", e); // NOI18N
        }
    }
***/
    /**
     * Executes the specified query against the database
     *
     */

    public void executeQuery(String query) {

        Log.err.log(ErrorManager.INFORMATIONAL,
                    "Entering QueryBuilder.executeQuery, query:\n" + query);  // NOI18N

        String sqlCommand = _queryBuilderPane.getSqlTextAreaText() ; // why not "query"?
        ResultSet result = null ;  // value to be returned.

        Connection connection = null ;
        PreparedStatement myStatement = null ;
        
        showBusyCursor ( true );
        
        boolean canExecute = true ;

        ParameterMetaData pmd = null;
        int paramCount =  0;

        try {
            connection = sqlStatement.getConnection() ;
            myStatement = connection.prepareStatement(sqlCommand) ;
            pmd = myStatement.getParameterMetaData();
            paramCount =  pmd.getParameterCount();
            if (DEBUG) {
                System.out.println(" Parameter Count  = " + paramCount + "\n");
                for (int i = 1; i <= paramCount; i++) {
                    System.out.println(" Parameter Type  = " + pmd.getParameterType(i) + "\n");
                    System.out.println(" Parameter Type Name = " + pmd.getParameterTypeName(i) + "\n");
                }
            }
        } catch ( SQLException e) {
            reportDatabaseError(e); // NOI18N
            canExecute = false ;
        } catch ( AbstractMethodError e) {
            // Certain drivers (e.g., Sybase 5.5) can throw Errors because of incompatibility.  Catch and report.
            Log.log(ErrorManager.ERROR,
                    "Error occurred when trying to retrieve table information: " + e); // NOI18N
            String title = NbBundle.getMessage(QueryBuilder.class, "PROCESSING_ERROR");
            JOptionPane.showMessageDialog( this, e.toString() + "\n\n", title, JOptionPane.ERROR_MESSAGE );
            canExecute = false ;
        }

        // Deal with any query parameters if we know about them.
        if ( canExecute && ( _queryModel != null ) )
        {
            if ( getParseErrorMessage() == null && _queryModel.isParameterized()) {

                ArrayList  list = new ArrayList();
                _queryModel.getParameterizedPredicates(list);
                String[] parameters = new String[list.size()];
                String[] values = new String[list.size()];

                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = new String((String) list.get(i));
                }
                ParameterizedQueryDialog pqDlg = new ParameterizedQueryDialog(
                    parameters, true);
                // System.out.println(pqDlg.getReturnStatus());
                if (pqDlg.getReturnStatus() == ParameterizedQueryDialog.RETURNED_OK) {
                    values = pqDlg.getParameterValues();
    
                    try {
                        for (int i = 0; i < values.length; i++) {
                            if (DEBUG) {
                                System.out.println(" command  = " + sqlCommand + "\n");
                                System.out.println("PreparedStatement i = " + i + " values = " + values[i] + "\n");
                            }
                            myStatement.setObject(i+1, ((String)values[i]),
                                                  pmd.getParameterType(i+1) );
                        }
    
                    } catch ( SQLException e) {
                        reportDatabaseError(e); // NOI18N
                        canExecute = false ;

                    }
                } else {
                    // cancelled the dialog.
                    canExecute = false ;
                }
            }
            else if (paramCount != 0) {
                // we have a query which can not be parsed.
                ArrayList  list = new ArrayList(paramCount);
                if (DEBUG) {
                    System.out.println(" param count = " + paramCount + "\n");
                    System.out.println(" list size  = " + list.size()+ "\n");
                }
                String[] parameters = new String[paramCount];
                String[] values = new String[paramCount];
                if (DEBUG) {
                    System.out.println(" parameters size  = " + parameters.length+ "\n");
                    System.out.println(" values size  = " + values.length+ "\n");
                }
    
                for (int i = 0; i < paramCount; i++) {
                    parameters[i] = new String( "Parameter " + new Integer(i).toString());
                }
                if (DEBUG) {
                    for (int i = 0; i < parameters.length; i++) {
                        System.out.println(" parameter  = " + parameters[i] + "\n");
                    }
                }
                ParameterizedQueryDialog pqDlg = new ParameterizedQueryDialog(
                    parameters, true);
                // System.out.println(pqDlg.getReturnStatus());
                if (pqDlg.getReturnStatus() == ParameterizedQueryDialog.RETURNED_OK) {
                    values = pqDlg.getParameterValues();
    
                    try {
                        for (int i = 0; i < values.length; i++) {
                            if (DEBUG) {
                                System.out.println(" command  = " + sqlCommand + "\n");
                                System.out.println("PreparedStatement i = " + i + " values = " + values[i] + "\n");
                            }
                            myStatement.setObject(i+1, ((String)values[i]),
                                                  pmd.getParameterType(i+1) );
                        }
    
                    } catch ( SQLException e) {
                        reportDatabaseError(e); // NOI18N
                        canExecute = false ;
                    }
                } else {
                    // cancelled the dialog.
                    canExecute = false ;
                }
            }
        }

        // Now execute the query
        if ( canExecute ) {

            try {
                boolean hasResults = myStatement.execute() ;
                if ( hasResults ) {
                    result = myStatement.getResultSet() ;
                    _queryBuilderPane.getQueryBuilderResultTable().displayResultSet(result);
                    result.close() ;
                }
                
            }  catch (SQLException e) {
                reportDatabaseError(e); // NOI18N
            }
        }
        
        // clean up stuff.
        try {
            if ( myStatement != null ) {
                myStatement.close() ;
            }
        } catch( SQLException se) {
            Log.err.log(ErrorManager.INFORMATIONAL, "Error Closing statement: " + se.getLocalizedMessage()); // NOI18N
        }

        try {
            if ( connection != null ) {
                connection.close() ;
            }
        } catch( SQLException se) {
            Log.err.log(ErrorManager.INFORMATIONAL, "Error Closing connection: " + se.getLocalizedMessage()); // NOI18N
        }
        
        showBusyCursor ( false );
        Log.err.log(ErrorManager.INFORMATIONAL, "Returning from QueryBuilder.executeQuery"); // NOI18N

    }


    /**
     * Initialize all the panes based on a new query passed in from the RowSet
     */
    void generate() {

        Log.err.log(ErrorManager.INFORMATIONAL, "Entering QueryBuilder.generate"); // NOI18N

        // Suppress updating of the text pane until we're ready
        _updateText = false;
        try {

            // Clear the current state.  It might be easier to recreate each of
            // the panes, as we did at startup.
            _queryBuilderPane.clear();

            // Generate the Diagram Pane and Grid Pane
            this.generateGraph();

        } finally {
            _updateText = true;
        }

        // Generate the Text Pane
        // It's not clear whether we should use the passed-in command,
        // or generate it from the model
        // _queryBuilderSqlTextArea.setText(command);
        this.generateText();
    }


    /**
     * Generate the query graph and tables that correspond to the current query model
     */
    void generateGraph() {
        // If this is false, graphics and model are disabled.  Don't build graph/table.
        if (_graphicsEnabled)
            _queryBuilderPane.getQueryBuilderGraphFrame().generateGraph(_queryModel);
    }

    /**
     * Build the SQL query from the current model, and update the text pane
     */
    void generateText() {
        // If this is false, graphics and model are disabled.  Don't generate text.
        if (_graphicsEnabled) {
            _queryBuilderPane.getQueryBuilderSqlTextArea().setQueryText(_queryModel.genText());
            if ( ! firstTimeGenerateText ) {
                saveSqlCommand(_queryBuilderPane.getSqlTextAreaText());
            } else  {
                firstTimeGenerateText = false;
            }
        }
    }


    void displayResultSet(ResultSet rs) {
        _queryBuilderPane.getQueryBuilderResultTable().displayResultSet(rs);
    }


//     /**
//      * Disable everything except the SQL Text Pane
//      */
//     void disable() {
//     }

//     /**
//      * Re-enable the panes that were disabled
//      */
//     void enable() {
//     }


    // Accessors/Mutators

    /**
     * Return the current query model
     */
    QueryModel getQueryModel() {
        return _queryModel;
    }

    /**
     *  Restore the last good query.
     */

    QueryBuilderPane getQueryBuilderPane ()
    {
        return ( _queryBuilderPane );        
    }

    /**
     * Returns true if the current query is parameterized
     */
    boolean isParameterized() {
        return _queryModel.isParameterized();
    }


    /**
     * Returns the list of tables and views
     */
    List getAllTables() throws SQLException {
        /*
        List tables = getTablesInternal("TABLE");
        tables.addAll(getTablesInternal("VIEW"));
        return tables;
        */
        return metaDataCache.getTables() ;
    }

    /* ===== JFB
    private List getTablesInternal(String type) {
        List tableNames = new ArrayList();
        if ( checkDatabaseConnection() == false ) {
            return tableNames;
        }
        boolean firstTime = true;
        while ( true ) {
            try {
                checkMetaData();

                TableMetaData[] tmd;
                if ( Log.isLoggable()) Log.log("start get"+type+"MetaData") ;
                tmd = (type.equals("TABLE")) ? _dbmdh.getTableMetaData() : _dbmdh.getViewMetaData();
                if ( Log.isLoggable()) Log.log("end get"+type+"MetaData") ;
                for (int i=0; i<tmd.length; i++)
                    tableNames.add(getFullTableName(tmd[i]));
                break;
            } catch (SQLException sqle) {
                if ( firstTime ) {
                    refreshDataBaseMetaData();
                    firstTime = false;
                } else {
                    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
                    break;
                }
            }
        }
        return tableNames;
    }
     *****/


//     private String getFullTableName(TableMetaData tmd) throws SQLException {
//         if (DEBUG) {
//             System.out.println(" getFullTableName() called " + "\n" ); // NOI18N
//         }
//         String schema = tmd.getMetaInfo(TableMetaData.TABLE_SCHEM);
//         /*
//          * !JK always show schema
//          * if (schema == null || schema.trim().equals("") || isSchemaInPath(schema)) {
//          }
//          */
//         if (schema == null || schema.trim().equals("")) {
//             schema = "";
//         } else {
//             schema += ".";
//         }
//         String tableName = tmd.getMetaInfo(TableMetaData.TABLE_NAME);
// 
//         // if table name does not contain spaces
//         if (tableName.indexOf(' ') == -1 ) {
//             return schema + tableName;
//         } else {
//             return schema + "\"" + tableName + "\"";
//         }
//     }

    /**
     * Returns the list of tables and views in all schemas that are accessible
     * through the DataSource associated with this QE
     */
    List getAllTablesInDataSource() throws SQLException {

        // Log.log(" getAllTablesInDataSource() called " + "\n" ); // NOI18N

        return metaDataCache.getTables() ;
        
        /*
        try {
            checkMetaData();
        } catch (SQLException sqle) {
            reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
        }

        // Get list of schemas in the datasource
        String[] schemaNames =  sqlStatement.getSchemas();
        if (schemaNames == null || schemaNames.length == 0)
            return getAllTables();
        else {
            _schemaNames = new ArrayList();
            List tables = new ArrayList();
            for (int i=0; i<schemaNames.length; i++) {
                tables.addAll(getTablesInternal("TABLE", schemaNames[i]));
                tables.addAll(getTablesInternal("VIEW", schemaNames[i]));
                _schemaNames.add(schemaNames[i]);
            }
            return tables;
        }
        */
    }


    /**
     * Returns the list of table names in the specified schema
     */
    /**** JFB private List getTablesInternal(String type, String schemaName) {
        if (Log.isLoggable() ) Log.log("enter tablesInternal "+type+","+schemaName) ;
        List tableNames = new ArrayList();
        if ( checkDatabaseConnection() == false )
            return tableNames;
        boolean firstTime = true;
        while ( true ) {
            try {
                checkMetaData();
                String[] tables =
                        (type.equals("TABLE")) ?_dbmdh.getTables(schemaName) : _dbmdh.getViews(schemaName);     // NOI18N

                // Convert to ArrayList, because caller expects it
                for (int i=0; i<tables.length; i++) {
                    tableNames.add(tables[i]);
                    if (DEBUG)
                        System.out.println(" getAllTablesInternal() tables [ " + i + " ]  = " + tables[i]  + "\n" ); // NOI18N
                }
                break;
            } catch (SQLException sqle) {
                if ( firstTime ) {
                    refreshDataBaseMetaData();
                    firstTime = false;
                } else {
                    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
                    break;
                }
            }
        }

        if (Log.isLoggable() ) Log.log("exit tablesInternal, cnt= " + tableNames.size() ) ;
        return tableNames;
    }
    ***/

    /**
     * Returns the set of columns in the specified table.
     * This is obtained from the DbMetaData.
     */
    // SCH: Modified to use schema if available
    /*  JFB
    public List getColumnNames(String tableName) throws SQLException {

        Log.err.log(ErrorManager.INFORMATIONAL,
                "Entering QueryBuilder.getColumnNames, tableName: " + tableName); // NOI18N

        return metaDataCache.getColumnNames(tableName)) ;
      
        boolean firstTime = true;
        while ( true ) {
            try {
                checkMetaData();

                ResultSet rs = _dbmdh.getMetaData().getColumns(null, null, tableName, "%"); // NOI18N
                if (rs != null) {
                    while (rs.next()) {
                        columnNames.add(rs.getString("COLUMN_NAME")); // NOI18N
                    }
                    rs.close();
                }
                break;
            } catch (SQLException sqle) {
                if ( firstTime ) {
                    refreshDataBaseMetaData();
                    firstTime = false;
                } else {
                    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
                    break;
                }
            }
        }
        if (DEBUG)
            for (int j=0; j<columnNames.size(); j++)
                System.out.println("Column ["+j+"] : " + (String) columnNames.get(j) + "\n" ); // NOI18N
        
    }
    */
    // Get the list of column names associated with the specified table name

    public void getColumnNamesFull(String fullTableName, List columnNames)  {
        try {
            columnNames.addAll( getColumnNames(fullTableName)) ;
        } catch(SQLException sqle) {
            // can't do anything.
        }
    }
    public List getColumnNames(String fullTableName) throws SQLException {

        Log.err.log(ErrorManager.INFORMATIONAL,
                "Entering QueryBuilder.getColumnNames, fullTableName: " + fullTableName); // NOI18N
    
        return metaDataCache.getColumnNames( fullTableName ) ;
        /*
        String[] table = fullTableName.split("\\.");
        if (table.length==1) // no schema -- use the old method
            getColumnNames(fullTableName, columnNames);
        else {
            String[] colNames=null;
            boolean firstTime = true;
            try {
                checkMetaData();
                // hack, getColumns throws an exception if table name has
                // spaces.
                colNames = _dbmdh.getColumns(fullTableName.replaceAll("\"", "") );
            } catch (SQLException sqle) {
                // First time we catch an error, try resetting the RowSet
                refreshDataBaseMetaData();
                try {
                    checkMetaData();
                    colNames = _dbmdh.getColumns(fullTableName);
                } catch (SQLException sqle2) {
                    // We must have a real error.  Report it.
                    reportDatabaseError("DATABASE_ERROR", sqle2); // NOI18N
                }
            }

            // Convert to ArrayList because caller expects it
            if (colNames!=null)
                for (int i=0; i<colNames.length; i++)
                    columnNames.add(colNames[i]);
        }
        */
    }


    /**
     * Returns the primary key columns of the specified table
     */
    List getPrimaryKeys(String fullTableName) throws SQLException {

        Log.log(ErrorManager.INFORMATIONAL,
                "Entering QueryBuilder.getPrimaryKeys, fullTableName: " + fullTableName ); // NOI18N

        return metaDataCache.getPrimaryKeys(fullTableName) ;
        /*
        List primaryKeys = new ArrayList();

        String tableName, schemaName=null;
        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length>1) {
            schemaName=table[0];
            tableName = table[1];
        } else
            tableName=table[0];

        boolean firstTime = true;
        while ( true ) {
            try {
                checkMetaData();

                ResultSet rs = _databaseMetaData.getPrimaryKeys(null, schemaName, tableName);
                if (rs != null) {
                    String name;
                    while (rs.next()) {
                        name = rs.getString("COLUMN_NAME"); // NOI18N
                        primaryKeys.add(name);
                    }
                    rs.close();
                }
                break;

            } catch (SQLException sqle) {
                if ( firstTime ) {
                    refreshDataBaseMetaData();
                    firstTime = false;
                } else {
                    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
                    break;
                }
            }
        }

        return primaryKeys;
         **/
    }


    /**
     * Returns the Foreign Key Constraints that apply to the specified table
     *
     * Result is an a-list of <foreignTable, foreignCol, primTable, primCol>.
     */
    List getForeignKeys(String fullTableName) throws SQLException {

        Log.err.log(ErrorManager.INFORMATIONAL,
                "Entering QB.getForeignKeys, fullTableName: " + fullTableName); // NOI18N

        
        // keys.add(new String[] {"travel.trip", "personid", "travel.person", "personid"});

        // We get the exported keys (foreign tables that reference this one), then
        // imported keys (foreign tables that this one references).
        /*
        List keys = getForeignKeys1(fullTableName, true);
        keys.addAll(getForeignKeys1(fullTableName, false));
        */
        List keys = metaDataCache.getForeignKeys(fullTableName, true);
        keys.addAll( metaDataCache.getForeignKeys(fullTableName, false));

        return keys;
        
    }

    /**
     * Returns either the exported or imported keys for this table, depending on the flag
     */
    /*
    List getForeignKeys1(String fullTableName, boolean exported) {

        String tableName, schemaName=null;
        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length>1) {
            schemaName=table[0];
            tableName = table[1];
        } else
            tableName=table[0];

        Log.log(" getForeignKeys1 schemaName = " + schemaName + " tableName = " + tableName + "\n" ); // NOI18N
        List keys = new ArrayList();
        boolean firstTime = true;
        while ( true ) {
            try {
                checkMetaData();

                ResultSet rs =
                        exported ?
                            _databaseMetaData.getExportedKeys(null, schemaName, tableName) :
                            _databaseMetaData.getImportedKeys(null, schemaName, tableName);
                if (rs != null) {
                    while (rs.next()) {
                        String fschem = rs.getString("FKTABLE_SCHEM"); // NOI18N
                        String pschem = rs.getString("PKTABLE_SCHEM"); // NOI18N
                        String[] key = new String[] {
                            ((fschem!=null) ? fschem+"." : "") + rs.getString("FKTABLE_NAME"), // NOI18N
                                    rs.getString("FKCOLUMN_NAME"), // NOI18N
                                    ((pschem!=null) ? pschem+"." : "") + rs.getString("PKTABLE_NAME"), // NOI18N
                                    rs.getString("PKCOLUMN_NAME") }; // NOI18N
                                    keys.add(key);
                    }
                    rs.close();
                }
                break;
            } catch (SQLException sqle) {
                if ( firstTime ) {
                    refreshDataBaseMetaData();
                    firstTime = false;
                } else {
                    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
                    break;
                }
            }
        }
        return keys;
    }

    */
    /**
     * Returns the imported key columns for this table -- i.e., the columns
     * whose value is a foreign key for another table.  These columns are
     * displayed with a special icon in the Query Builder.
     */
    List getImportedKeyColumns(String fullTableName) throws SQLException {

        return metaDataCache.getImportedKeyColumns(fullTableName) ;
        /*
        List keys = new ArrayList();
        String tableName, schemaName=null;
        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length>1) {
            schemaName=table[0];
            tableName = table[1];
        } else
            tableName=table[0];

        boolean firstTime = true;
        while ( true ) {
            try {
                checkMetaData();

                ResultSet rs = _databaseMetaData.getImportedKeys(null, schemaName, tableName);
                if (rs != null) {
                    while (rs.next()) {
                        keys.add(rs.getString("FKCOLUMN_NAME")); // NOI18N
                    }
                    rs.close();
                }
                break;
            } catch (SQLException sqle) {
                if ( firstTime ) {
                    refreshDataBaseMetaData();
                    firstTime = false;
                } else {
                    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
                    break;
                }
            }
        }

        Log.err.log(ErrorManager.INFORMATIONAL, "Imported key columns for table " + fullTableName); // NOI18N
        if (keys!= null)
            for (int i=0; i<keys.size(); i++)
                Log.err.log(ErrorManager.INFORMATIONAL, "Keys("+i+"): " + keys.get(i)); // NOI18N

        return keys;
        */
    }

    /**
     * Returns a FK between this pair of tables if there is one, else null
     * Note that the set of FKs is passed in from the caller, to avoid having to make multiple
     * fetches from the dbmetedata when we're adding a new table
     */
    String[] findForeignKey(String oldFullTableName, String newFullTableName, List foreignKeys) {

        Log.err.log(ErrorManager.INFORMATIONAL,
                "Entering QB.findForeignKey, t1: "+oldFullTableName + " t2: "+newFullTableName); // NOI18N

        if (foreignKeys!=null) {
            for (int i=0; i<foreignKeys.size(); i++) {
                String[] key = (String[])foreignKeys.get(i);
                if ((key[0].equalsIgnoreCase(newFullTableName)&&key[2].equalsIgnoreCase(oldFullTableName)) ||
                        (key[0].equalsIgnoreCase(oldFullTableName)&&key[2].equalsIgnoreCase(newFullTableName)))
                    return (String[]) foreignKeys.get(i);
            }
        }
        Log.err.log(ErrorManager.INFORMATIONAL, "No key found"); // NOI18N
        return null;
    }


    /**
     * Returns a FK between this pair of tables and columnsif there is one, else null
     */
    String[] findForeignKey(String fullTableName1, String colName1,
            String fullTableName2, String colName2) throws SQLException {

        Log.err.log(ErrorManager.INFORMATIONAL,
                "Entering findForeignKey, t1: " + fullTableName1 + " c1: " + colName1 // NOI18N
                + "   t2: " + fullTableName2 + "  c2: " + colName2); // NOI18N

        // Get the complete list of keys for one of the tables; we use table1
        List foreignKeys = getForeignKeys(fullTableName1);
        if (foreignKeys!=null) {
            for (int i=0; i<foreignKeys.size(); i++) {
                String[] key = (String[])foreignKeys.get(i);
                if ((key[0].equalsIgnoreCase(fullTableName1) && key[1].equalsIgnoreCase(colName1)
                && key[2].equalsIgnoreCase(fullTableName2) && key[3].equalsIgnoreCase(colName2)) ||
                        (key[0].equalsIgnoreCase(fullTableName2) && key[1].equalsIgnoreCase(colName2)
                        && key[2].equalsIgnoreCase(fullTableName1) && key[3].equalsIgnoreCase(colName1)))
                    return (String[]) foreignKeys.get(i);
            }
        }
        Log.err.log(ErrorManager.INFORMATIONAL, "No key found"); // NOI18N
        return null;
    }

    public String getConnectionInfo() {
        return sqlStatement.getConnectionInfo() ;
    }

    // Methods inherited from org.openide.windows.TopComponent

    /**
     * About to shown.  Could have been previously hidden ( e.g.,
     * when the user tabs back to the QueryEditor) or on first showing.
     *
     * We need to flush all the current information (model, graph, metadata),
     * and read the command, as if we were starting for the first time.
     */
    protected void componentShowing() {

        Log.err.log(ErrorManager.INFORMATIONAL,
                "Entering QueryBuilder.componentShowing()"); // NOI18N

        String command = sqlStatement.getCommand();

        if (_queryModel==null)
            _queryModel = new QueryModel();

        Log.log("  * command=" + command) ;

        // Parse the current query, in case it has changed

        // Special case for handling null queries -- prompt for an initial table
        // We should probably allow this, since the user can delete the last table in the
        // editor anyway, so we need to be able to deal with empty queries as a special case.
        if ((command==null) || (command.trim().length()==0)) {
            Log.log("QBShowing command is null") ;
            setVisible(true);
            this.repaint();
            String msg = NbBundle.getMessage(QueryBuilder.class, "EMPTY_QUERY_ADD_TABLE");
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE); // NOI18N
            DialogDisplayer.getDefault().notify(d);

            _queryBuilderPane.getQueryBuilderGraphFrame().addTable() ;

        } else {

            /* TODO JOEL:  why get column names here?  why now in the
             * else for populate?

            if (_tableNames == null ) {
                _tableColumns = createTableColumns() ;
            }
             **/
            String queryText = getUnformattedSqlString();
            // parse and populate only if the query has changed.
            if ( queryText == null || (! command.trim().equalsIgnoreCase( queryText.trim() ) ) ) {
                this.populate(command);
                setVisible(true);
                this.repaint();
            }
        }
        activateActions();

        _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus();

        if ( DEBUG )
            System.out.println(" _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus () called. " + "\n" ); // NOI18N

    }

    /**
     * Component is about to be shown.
     * Called when the user moves to another tab.
     * If we have an associated rowset, update it with current text query.
     */
    protected void componentHidden() {
        Log.err.log(ErrorManager.INFORMATIONAL,
                "QB.componentHidden, updating command to: " + _queryBuilderPane.getSqlTextAreaText()); // NOI18N
        String command = sqlStatement.getCommand();
        if ((command!=null) && (command.trim().length()!=0)) {
            String queryText = getUnformattedSqlString();

            // parse and populate only if the query has changed.
            if ( queryText == null || (! command.trim().equalsIgnoreCase( queryText.trim() ) ) ) {
                if ( _graphicsEnabled ) {
                    boolean good = this.populate(queryText, true);
		    if ( !good) {
                        setSqlCommand(queryText); //HACK, temporary (jfb)
                    }
                } else {
                    setSqlCommand(queryText) ;
                }
            }
        }
        deactivateActions();
    }

    private String getUnformattedSqlString() {
        return _queryBuilderPane.getQueryBuilderSqlTextArea().getText() ;
        // return (_queryBuilderPane.getQueryBuilderSqlTextArea().getText().replace('\n', ' ').replaceAll("  "," ") ); // NOI18N
    }

    /** Opened for the first time */
    protected void componentOpened() {
        Log.err.log(ErrorManager.INFORMATIONAL,
                "Entering QueryBuilder.componentOpened()"); // NOI18N

        activateActions();
        ActionMap map = getActionMap();
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        installActions(map, keys);
        sqlStatement.addPropertyChangeListener(sqlStatementListener) ;

        // do NOT force a parse here.  It's done componentShowing().
        // populate( sqlStatement.getCommand()) ;

    }

    /* closed - not visible anywhere)
     */
    protected void componentClosed() {
        Log.err.log(ErrorManager.INFORMATIONAL,
                "QueryBuilder.componentClosed()"); // NOI18N

        deactivateActions();

        sqlStatement.close() ;
        lastQuery = null;
    }

    /*****
     * listener for changes in the sqlStatement - either the
     * command changed or the connection changed (e.g., datasource changed).
     */
    private PropertyChangeListener sqlStatementListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            // what property?
            String propName = evt.getPropertyName() ;
            Log.log("QB sqlStatement property change: " + propName ) ;
            if ( propName.equals(SqlStatement.COMMAND)) {
                Log.err.log(" newValue=" + sqlStatement.getCommand()) ;
                populate( sqlStatement.getCommand() ) ;
                _queryBuilderPane.getQueryBuilderSqlTextArea().requestFocus();

            } else if ( propName.equals(SqlStatement.CONNECTION_INFO)) {
                Log.err.log(" resetting connections") ;
                // _dbmdh.refresh() ;

            } else if ( propName.equals(SqlStatement.TITLE) ) { 
                Log.err.log(" title to " + sqlStatement.getTitle()) ; // NOI18N
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setDisplayName(sqlStatement.getTitle()) ;
                    }
                }) ;

            } else if ( propName.equals(SqlStatement.CLOSING)) {
                Log.err.log(" closing...") ; // NOI18N
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        close() ;
                    }
                }) ;
            }
        }
    } ;

    // Return true if there is a connection to the database, false otherwise
    private SQLException lastException = null ;


    // Return true if we have a database connection, false otherwise.
    // If connection is down, allow user to Retry (loop) or Cancel&Continue
    protected boolean checkDatabaseConnection() {

        Log.err.log( "checkDatabaseConnection()" ); // NOI18N
        boolean keepChecking = true ;
        boolean connected = false ;
        while ( keepChecking ) {

            try {
                metaDataCache.checkDataBaseConnection() ;
                connected = true ;
            } catch (SQLException sqle ) {
                lastException = sqle ;
                connected = false ;
            }
            
            if ( ! connected ) {
                // either the verify failed or the connect failed.
                boolean retry = showRetryDialog() ;
                if (! retry) {
                    keepChecking = false ;
                }
            } else keepChecking = false ;
        }
        return connected ;
    }
    
    /***
    private final JButton retryButton = new JButton(NbBundle.getMessage(QueryBuilder.class, "RETRY_AND_CONTINUE")) ;
    private final JButton cancelButton = new JButton(NbBundle.getMessage(QueryBuilder.class, "CANCEL_AND_CONTINUE")) ;
    **/
    /******
     * show a dialog with the a message saying the database connection is hosed.
     * It has a Retry and Cancel button.
     * returns true if the Retry is the closing action.
     */
    int ii = 0 ;
    public boolean showRetryDialog() {
        Log.log("enter showRetryDialog() " + ii++ ) ;
        ConnectionStatusPanel csp = new ConnectionStatusPanel() ;
        csp.configureDisplay(sqlStatement.getConnectionInfo(), false, lastException.getLocalizedMessage(),  "", 0, false ) ;
        csp.setGeneralInfo(NbBundle.getMessage(QueryBuilder.class, "DATABASE_CONNECTION_ERROR") ) ;    // NOI18N
        csp.setFooterInfo(NbBundle.getMessage(QueryBuilder.class, "NO_DATABASE_CONNECTION") ) ;      // NOI18N

        final JButton retryButton = new JButton(NbBundle.getMessage(QueryBuilder.class, "RETRY_AND_CONTINUE")) ;
        final JButton cancelButton = new JButton(NbBundle.getMessage(QueryBuilder.class, "CANCEL_AND_CONTINUE")) ;

        // this listener is for the dialog.
        final Object[] retVal = new Object[1] ;
        ActionListener listener = new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Log.log("  retry dialog event: " + evt) ;
                retVal[0] = evt.getSource() ;
            }

        };

        DialogDescriptor dlg = new DialogDescriptor(csp,
                NbBundle.getMessage(ConnectionStatusPanel.class, "ConStat_title", sqlStatement.getConnectionInfo()), // NOI18N
                true/*modal*/,
                new Object[] {retryButton, cancelButton}, cancelButton,
                        DialogDescriptor.DEFAULT_ALIGN, null, listener);

        dlg.setClosingOptions( null );

        Dialog dialog = (Dialog) DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setResizable(true);
        dialog.pack() ;

        // present dialog, waits for it to be disposed.
        dialog.show();

        boolean val = ( retVal[0] == retryButton ) ;
        Log.err.log( "  * dlg says:  Retry=" + val ) ;
        return val ;
    }


    // Utility methods
    private void checkMetaData() throws SQLException {
/**
        if (_databaseMetaData == null) {
            try {
                if ( checkDatabaseConnection() ) {
                    // _dbmdh = new DatabaseMetaDataHelper(connection);
                    // _databaseMetaData = _dbmdh.getMetaData();
                }

            } catch (SQLException sqle) {
                reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
            }
        }
 ***/
    }

    public void reportDatabaseError(SQLException e) {

        Log.log(ErrorManager.ERROR,
                "Error occurred when trying to retrieve table information: " + e); // NOI18N

        String msg = 
            (e.getErrorCode() == 17023) ?
            // Oracle "Unsupported feature" error
            NbBundle.getMessage(QueryBuilder.class, "UNSUPPORTED_FEATURE") :
            e.getLocalizedMessage();

        // NbBundle.getMessage(QueryBuilder.class, key);
        String title = NbBundle.getMessage(QueryBuilder.class, "PROCESSING_ERROR");

        JOptionPane.showMessageDialog( this, msg + "\n\n", title, JOptionPane.ERROR_MESSAGE );
        /*
        ConnectionStatusPanel csp = new ConnectionStatusPanel() ;
        csp.configureDisplay(sqlStatement.getConnectionInfo(), false, e.getLocalizedMessage(),  "", 0, false ) ;
        csp.setGeneralInfo(msg) ;
        csp.displayDialog( sqlStatement.getConnectionInfo() ) ;
        */

    }

    String getParseErrorMessage() {
        return _parseErrorMessage;
    }
    /* ================================================================ */
    /*****
     * parse a full table name, e.g. Schema.Table or Table
     * and returns an array where 
     * [0] = schema (or null if none found)
     * [1] = table name.
     */
    private static String[]  parseTableName(String fullTableName) {
        
        String[] retVal = new String[2] ;
        
        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length>1) {
            retVal[0] = table[0];
            retVal[1] = table[1];
        } else {
            retVal[0] = null ;
            retVal[1] = table[0];
        }
        return retVal ;
    }
    
    /**
     * Showing/hiding busy cursor, before this funcionality was in Rave winsys,
     * the code is copied from that module.
     * It needs to be called from event-dispatching thread to work synch,
     * otherwise it is scheduled into that thread. */
    static void showBusyCursor(final boolean busy) {
        if(SwingUtilities.isEventDispatchThread()) {
            doShowBusyCursor(busy);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    doShowBusyCursor(busy);
                }
            });
        }
    }

    private static void doShowBusyCursor(boolean busy) {
        JFrame mainWindow = (JFrame)WindowManager.getDefault().getMainWindow();
        if(busy){
            RepaintManager.currentManager(mainWindow).paintDirtyRegions();
            mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            mainWindow.getGlassPane().setVisible(true);
            mainWindow.repaint();
        } else {
            mainWindow.getGlassPane().setVisible(false);
            mainWindow.getGlassPane().setCursor(null);
            mainWindow.repaint();
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( "projrave_ui_elements_editors_about_query_editor" );        // NOI18N
    }
    
}
