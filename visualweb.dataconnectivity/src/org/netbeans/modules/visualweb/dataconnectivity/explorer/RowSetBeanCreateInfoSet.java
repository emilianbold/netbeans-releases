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
package org.netbeans.modules.visualweb.dataconnectivity.explorer;

import org.netbeans.modules.visualweb.dataconnectivity.DataconnectivitySettings;
import org.netbeans.modules.visualweb.dataconnectivity.Log;
import org.netbeans.modules.visualweb.dataconnectivity.customizers.RowSetSelection;
import java.awt.Image;
import java.sql.SQLException;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSource;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import com.sun.rave.designtime.BeanCreateInfoSet;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.Result;


// XXX Originally RowSetPaletteItem, but extracted the uneeded palette stuff.
/**
 * Pallette iteam representation for Table (or View)  - used for dragging from
 * Server Nav to pages or app outline.
 *
 * This creates a data provider (unless canceled).  It will like this
 * dataprovider to a rowset.  The rowset may be an existing one or it
 * might be created (depends on options and user feedback).
 *
 * jfbrown - 25-may-2005 separted from SingleTableNode and revised to create
 * the rowset in the appropriate bean.
 */

public class RowSetBeanCreateInfoSet implements BeanCreateInfoSet {

    DataSourceInfo dsInfo;
    String tableName;
    String bareTableName;
    String rowSetInstanceName;

    protected static String rowSetNameSuffix = DataconnectivitySettings.getRsSuffix() ;
    public static Class rowSetClass = com.sun.sql.rowset.CachedRowSetXImpl.class;
    protected static String dataProviderProperty = "cachedRowSet" ; // NOI18N ,
    protected static String dataProviderClassName = "com.sun.data.provider.impl.CachedRowSetDataProvider" ; // NOI18N

    public RowSetBeanCreateInfoSet(DataSourceInfo dsi, String tableName ) {
        this(tableName);
        setDataSourceInfo(dsi);
    }

    /*
     * Lazily set the datasource info
     */
    public RowSetBeanCreateInfoSet(String tableName){
        this.tableName = tableName;
        int bt = tableName.lastIndexOf('.') ;
        if ( bt >= 0 ) {
            this.bareTableName = tableName.substring(bt+1) ;
        } else {
            this.bareTableName = tableName ;
        }
        Log.log("RowSetPaletteItem ="+tableName+","+bareTableName);
    }

    public String getDisplayName() { return null; }
    public String getDescription() { return null; }
    public Image  getLargeIcon()   { return null; }
    public Image  getSmallIcon()   { return null; }
    public String getHelpKey()     { return null; }
    public String getTableName()   { return tableName; }

    public void setDataSourceInfo(DataSourceInfo dsi){
        dsInfo = dsi;
        try {
            rowSetInstanceName =
                    dsInfo.getDatabaseMetaDataHelper().getNoSchemaName(tableName).toLowerCase()
                    + rowSetNameSuffix ;  // NOI18N
        } catch (SQLException e) {
            rowSetInstanceName = bareTableName.toLowerCase() + rowSetNameSuffix ; // NOI18N
        }
        // remove blanks
        rowSetInstanceName = rowSetInstanceName.replaceAll(" ", "") ; // NOI18N
    }

    public DataSourceInfo getDataSourceInfo(DataSourceInfo dsi){
        return dsInfo;
    }

    String dsName = null ;
    private String getDataSourceName() {
        if (dsName == null) dsName = "java:comp/env/jdbc/" + dsInfo.getName(); // NOI18N
        return dsName ;
    }

    String command = null ;
    private String getCommand() {
        if (command == null) command = DesignTimeDataSource.composeSelect(tableName);
        return command ;
    }

    public String[] getBeanClassNames(){
        return new String[] {
            /* rowSetClass.getName(), */
            dataProviderClassName
        };
    }

    DesignBean rsBean = null ;
    public Result beansCreatedSetup(DesignBean[] designBeans) {
        
        // DesignBean rsBean = null;
        rsBean = null ;
        DesignBean dpBean = null;
        
        // only one bean expected - see getBeanClassNames().
        if ( designBeans.length > 0 ) {
            dpBean = designBeans[0] ;
        }
        if ( dpBean == null || ! dataProviderClassName.equals(dpBean.getInstance().getClass().getName()) ) {
            // should never be here.
            String cname = ( dpBean == null ? "none provided" : dpBean.getInstance().getClass().getName() ) ; // NOI18N
            String msg = "Unknown dataProvider bean given to SingleTableNode: " + cname ; // NOI18N
            RuntimeException ree = new RuntimeException(msg) ;
            Log.err.notify(ErrorManager.ERROR, ree ) ;
            throw ree ;
        }
        
        DesignContext ctx = dpBean.getDesignContext() ;
        String dpBeanScope = (String)ctx.getContextData( Constants.ContextData.SCOPE ) ;
        if ( Log.isLoggable()) {
            Log.log("RSPI: dpBeanScope='"+dpBeanScope+"' for "+ ctx.getDisplayName() ); // NOI18N
            Log.log("RSPI: canDropAnywhere="+DataconnectivitySettings.canDropAnywhere() ) ;
        }
        
        // locate or create the correct CachedRowSet bean.
        // -------------------------
        
        final RowSetSelection rss = new RowSetSelection(ctx, this.tableName, getDataSourceName(),  "", "", getCommand() ) ;
        
        DesignContext rsContext = null ;
        if ( ! rss.hasMatchingRowSets() ) {
            
            // no existing rowset - so find the context where we should create it.
            String rsScope = getScopeForNewRowSet(dpBeanScope, ctx ) ;
            if ( ! rsScope.equals(dpBeanScope) ) {
                // rsContext = findPreferredBean( ctx, rsScope ) ;
                for ( ; rsScope != null ; rsScope = getNextScope( rsScope ) ) {
                    Object[] contexts = rss.getCreateBeans(rsScope) ;
                    if ( contexts.length < 1 ) {
                        continue ;
                    }
                    String beanName = rsScope.substring(0,1).toUpperCase() + rsScope.substring(1) + "Bean1" ;
                    for ( int i = 0 ; i < contexts.length ; i++ ) {
                        if  ( ((DesignContext)contexts[i]).getDisplayName().equals(beanName) ) {
                            rsContext = (DesignContext)contexts[i] ;
                            break ;
                        }
                    }
                    if ( rsContext  == null ) {
                        // just use the first on.
                        rsContext = (DesignContext)contexts[0] ;
                    }
                    break ;
                }
            }
            if ( rsContext == null ) {
                rsContext = ctx ; // create the thing in the same context.
            }
            
            
        } else {
            // have rowset matches, so prompt for the rowset.  User always has to pick one.
            boolean makeStuff = rss.showDialog() ;
            final DesignBean dp = dpBean ;
            if ( ! makeStuff ) {
                // delete the already-created
                final DesignBean deleteMeBean = dpBean ;
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        DesignContext dc = deleteMeBean.getDesignContext() ;
                        dc.deleteBean( deleteMeBean ) ;
                    }
                }) ;
                return Result.FAILURE ;
            }
            
            rsContext = rss.getSelectedDesignContext() ;
            rsBean = rss.getSelectedRowSetBean() ;
            if ( rsBean == null ) {
                // if here, we're going to create one later.
                rowSetInstanceName = rss.getSelectedRowSetName() ;
            }
        }
        if ( rsBean == null ) {
            // create the rowset design bean here.
            rsBean = rsContext.createBean(rowSetClass.getName(), null, null);
            setRowSetProperties( rsBean, rowSetInstanceName) ;
            DesignInfo rsBeanDesignInfo = rsBean.getDesignInfo();
            if (rsBeanDesignInfo != null) {
                rsBeanDesignInfo.beanCreatedSetup(rsBean);
            }
            if ( Log.isLoggable() ) {
                Log.log("RSPI:  created rowset " + rowSetInstanceName + " in " + rsContext.getDisplayName() ) ;
            }
        }
        
        String dpInstanceName = rowSetInstanceName.replaceAll(rowSetNameSuffix, DataconnectivitySettings.getDpSuffix()); // NOI18N
        if ( dpInstanceName.indexOf(DataconnectivitySettings.getDpSuffix()) < 0 ) { // NOI18N
            // if existing rowset was renamed, the replaceall might not have changed anything.
            dpInstanceName = dpInstanceName + DataconnectivitySettings.getDpSuffix() ; // NOI18N
        }
        dpBean.setInstanceName(dpInstanceName, true);
        Log.log("RSPI:  setting prop " + dataProviderProperty ) ;
        dpBean.getProperty(dataProviderProperty).setValue(rsBean.getInstance());
        
        return Result.SUCCESS;
    }
    
    /****
     * for a rowset bean, set it's properties.
     */
    private DesignBean setRowSetProperties(DesignBean rsBean, String nameForInstance) {
        
        if ( nameForInstance != null && nameForInstance.length()> 0) {
            rsBean.setInstanceName(nameForInstance, true);  // NOI18N
        }
        String curValue ;
        curValue = (String)rsBean.getProperty("dataSourceName").getValue() ;
        if ( curValue == null || curValue.length() < 1) {
            rsBean.getProperty("dataSourceName").setValue(getDataSourceName());
        }
        
        curValue = (String)rsBean.getProperty("command").getValue() ;
        if ( curValue == null || curValue.length() < 1) {
            rsBean.getProperty("command").setValue(getCommand()); // NOI18N
        }
        
        if (rowSetClass == com.sun.sql.rowset.CachedRowSetXImpl.class) {
            curValue = (String)rsBean.getProperty("tableName").getValue() ;
            if ( curValue == null || curValue.length() < 1) {
                rsBean.getProperty("tableName").setValue(bareTableName); // NOI18N
            }
        }
        return rsBean ;
    }
    
    /***
     * Determine if it's a request bean.
     * TODO EA HACK:  just look for "RequestBean" in the name
     */
    private static boolean isRequestBean(DesignContext context) {
        if ( SCOPE_REQUEST.equals(context.getContextData(Constants.ContextData.SCOPE))) {
            if ( context.getDisplayName().indexOf("RequestBean") >= 0) {
                return true ;
            }
        }
        return false ;
    }
    
    private static final String SCOPE_PAGE = "page" ; // ??unused??
    private static final String SCOPE_REQUEST = "request" ;
    private static final String SCOPE_SESSION = "session" ;
    private static final String SCOPE_APPLICATION = "application" ;
    
    // navigate up the scope hierarchy
    private static String getNextScope( String curScope ) {
        if ( curScope.equals(SCOPE_PAGE)) return SCOPE_REQUEST ;
        if ( curScope.equals(SCOPE_REQUEST)) return SCOPE_SESSION ;
        if ( curScope.equals(SCOPE_SESSION)) return SCOPE_APPLICATION ;
        return null ;
    }
    
    /**
     * for a new rowset bean, determine the scope where it should be created.
     * goal:  if dragging to a page, return session. (a page has request scope).
     *        if dragging to a "RequestBean" in the app outline, return request.
     */
    private String getScopeForNewRowSet( String curScope, DesignContext context ) {
        // if page or request, return session
        // if session or application, return the same.
        if ( DataconnectivitySettings.canDropAnywhere() ) {
            return curScope ;
        }
        if ( curScope.equals(SCOPE_PAGE)) return SCOPE_SESSION ;
        if ( curScope.equals(SCOPE_REQUEST)) {
            if ( isRequestBean( context) ) {
                return SCOPE_REQUEST ;
            }
            return SCOPE_SESSION ;
        }
        if ( curScope.equals(SCOPE_SESSION)) return SCOPE_SESSION ;
        if ( curScope.equals(SCOPE_APPLICATION)) return SCOPE_APPLICATION ;
        return SCOPE_SESSION ; // should never be here.
    }
}

