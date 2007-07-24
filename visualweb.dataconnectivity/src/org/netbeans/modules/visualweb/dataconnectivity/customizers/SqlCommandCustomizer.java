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
package org.netbeans.modules.visualweb.dataconnectivity.customizers;

import javax.naming.NamingException;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.visualweb.dataconnectivity.Log;
import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditor;
import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorFactory;
import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSource;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.impl.BasicCustomizer2;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 *  steps:  this customizer is instatiated with the name of the class
 *    it'll handle:  JdbcRowsetX or cacheRowSetX
 *    the instance is then registered with insync
 *  When a customizer is needed, getCustomizerPanel() is called.
 *  Which calls
 */
public class SqlCommandCustomizer extends BasicCustomizer2 {
    
    public String customerizerClassName = "" ; // NOI18N
    
//    private static boolean useViewData = false ;
    
    private DesignBean 				bean;
    private VisualSQLEditor			vse;
    
    private static HashMap<DesignBean, TopComponent> queryEditors =
            new HashMap<DesignBean, TopComponent>();
    
    // Constructor
    
    public SqlCommandCustomizer(String customerizerClassName )  {
        super(null, NbBundle.getMessage(SqlCommandCustomizer.class, "EDIT_QUERY"));        // NOI18N
        this.customerizerClassName = customerizerClassName ; 
        Log.err.log("Customizer for "+customerizerClassName) ;
    }
    
    public Component getCustomizerPanel(DesignBean srcBean ) {
        
        Log.err.log("Customizer panel requested for " + srcBean.getInstanceName() ) ;
        
        bean = srcBean;
        
        TopComponent qe = null;
        
        if ((qe = queryEditors.get(srcBean)) != null) {
            if (!qe.isOpened())
                qe.open();
            qe.requestActive();
            return qe;
        }

        /****
         * get the dataSourceName
         */
        // Object o = designBean.getInstance();
        String dsName = null ;
        dsName = (String)srcBean.getProperty("dataSourceName").getValue() ;
        VisualSQLEditorMetaData metadata = null;
        try {
            metadata = VisualSQLEditorMetaDataImpl.getDataSourceCache(dsName);
        } catch (java.sql.SQLException e) {
            
            // JDTODO
        }
        
//        SqlStatement sqlStatement = null ;
//        try {
//            sqlStatement = new SqlStatementImpl( dsName, srcBean ) ;
//        } catch (javax.naming.NamingException ne) {
//            org.openide.ErrorManager.getDefault().notify(ne);
//            sqlStatement = null ;
//        }
//        if ( sqlStatement == null ) return null ;
        
        // Get the DatabaseConnection, to be passed to the Visual SQL Editor
        DatabaseConnection dbconn = null;
        try {
            // First, get the DesignTimeDataSource
            DesignTimeDataSource dtds = lookupDataSource(dsName);
            
            // Get the list of DatabaseConnections
            DatabaseConnection[] dbconns = ConnectionManager.getDefault().getConnections();
            // Find the one we want
            for (int i=0; i<dbconns.length; i++) {
                if ((dbconns[i]).getDatabaseURL().equals(dtds.getUrl())) {
                    dbconn = dbconns[i];
                    break;
                }
            }
        } catch (NamingException ex) {
        }
        
//        if ( ! useViewData) {
        
        String command = (String)srcBean.getProperty("command").getValue();
        vse = VisualSQLEditorFactory.createVisualSQLEditor(dbconn, command, metadata);
        
        vse.addPropertyChangeListener(vseListener);
        Component retComp = vse.open();
        if (retComp instanceof TopComponent) {
            queryEditors.put(srcBean, (TopComponent)retComp);
        }
        
        return retComp ;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( "projrave_ui_elements_editors_about_query_editor" );        // NOI18N
    }
//     public static void setUseViewData(boolean use) {
//         useViewData = use ;
//     }
//     public static boolean getUseViewData() {
//         return useViewData ;
//     }
    
//     static {
//         if ( System.getProperty("useViewData")!=null ) {
//             useViewData = true ;
//         } else {
//             useViewData = false ;
//         }
//     }
      
    /**
     * Attempt to locate an existing QB for the given designBean.
     */
//     public static QueryBuilder findCurrent(DesignBean dBean ) {
        
//         QueryBuilder qbForm = null;
        
//         // Search through workspaces, then modes, then topcomponents
//         Set modes = WindowManager.getDefault().getModes();
//         Iterator it2 = modes.iterator();
        
//         // JDTODO - figure out a way of doing this in the new version
//         // The Customizer will have to retain the mapping, since the QueryEditor no longer
//         // knows about the designbean
//        while (it2.hasNext()) {
//            Mode m = (Mode)it2.next();
//            TopComponent[] tcs = m.getTopComponents();
//
//            if (tcs != null) {
//                for (int j = 0; j < tcs.length; j++) { // for each topcomponents
//                    TopComponent tc = (TopComponent)tcs[j] ;
//
//                    if ( tcs[j] instanceof QueryBuilder) {
//                        SqlStatement sss = ((QueryBuilder)tcs[j]).getSqlStatement() ;
//                        if ( sss instanceof SqlStatementImpl) {
//                            if ( dBean == ((SqlStatementImpl)sss).designBean ) {
//                                qbForm = (QueryBuilder)tcs[j] ;
//                                break ;
//                            }
//                        }
//                    }
//                } // for each topcomponents
//            }
//
//            if (qbForm != null ) {
//                break ;
//            }
//        }
//
//        return qbForm;
//         return null;
//     }    
    
    // Listen for changes to statement property, and notify the bean
    
    private PropertyChangeListener vseListener =
            
            new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            // what property?
            String propName = evt.getPropertyName() ;
            Log.log("VSE property change: " + propName ) ;
            if ( propName.equals(VisualSQLEditor.PROP_STATEMENT)) {
                Log.err.log(" newValue=" + vse.getStatement()) ;
                bean.getProperty("command").setValue(vse.getStatement()) ;
            }
        }
    } ;

    
    /****
     * convenience method for looking up the datasource in the current
     * context.  Copied from SqlStatementImpl
     */
    private DesignTimeDataSource lookupDataSource( String dataSourceName )
        throws NamingException 
    {
        String dsName ;
        if ( dataSourceName == null ) {
            // we should never be here, but just in case ...
            NamingException ne = new NamingException("Data Source Name is required:  none provided." ) ; // NOI18N
            throw ne ;
        }
        
        javax.naming.Context ctx = new javax.naming.InitialContext();
        if ( ! dataSourceName.startsWith("java:comp/env/jdbc/") ) {
            dsName =  "java:comp/env/jdbc/" + dataSourceName ;
        } else {
            dsName = dataSourceName ;
        }
        
        DesignTimeDataSource ds = (DesignTimeDataSource) ctx.lookup( dsName );
        return ds ;
    }
    
    
    
}
