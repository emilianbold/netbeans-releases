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
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.impl.BasicCustomizer2;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * This customizer is instantiated once with the name of each class that it will customize
 * (currently only CachedRowSetX); the instance is then registered with insync
 *  When a customizer is needed, getCustomizerPanel() is called.
 */
public class SqlCommandCustomizer extends BasicCustomizer2 {
    
    public String customerizerClassName = "" ; // NOI18N
    
    private OpenProjectsListener openProjectsListener = new OpenProjectsListener();
    
    // Maps for tracking open Query Editors

    private static HashMap<DesignBean, QBPair> queryEditors =
            new HashMap<DesignBean, QBPair>();

    private static HashMap<Project, ArrayList<DesignBean>> projectBeans = 
            new HashMap<Project, ArrayList<DesignBean>>();
    

    // Constructor
    
    public SqlCommandCustomizer(String customerizerClassName )  {
        super(null, NbBundle.getMessage(SqlCommandCustomizer.class, "EDIT_QUERY"));        // NOI18N
        this.customerizerClassName = customerizerClassName ; 
        OpenProjects.getDefault().addPropertyChangeListener(openProjectsListener);
        Log.err.log("Customizer for "+customerizerClassName) ;
    }
    

    // Factory method.  Returns QueryBuilder, which is a TopComponent
    
    public Component getCustomizerPanel(DesignBean srcBean ) {
        
        Log.err.log("Customizer panel requested for " + srcBean.getInstanceName() ) ;
        
	// Check the map for an existing query editor for this bean, and reuse it if so
        QBPair qbp = queryEditors.get(srcBean);
        if (qbp != null) {
            TopComponent qb = qbp.qb;
            if (!qb.isOpened()) {
                qb.open();
	    }
            qb.requestActive();
            return qb;
        }
        
        Project project = null;
        DesignContext dc = srcBean.getDesignContext();
        if (dc instanceof LiveUnit) {
            FacesModel fm = ((LiveUnit)dc).getModel();
            FacesModelSet fms = fm.getFacesModelSet();
            project = fms.getProject();
        }
        if (project != null) {
            
            ArrayList<DesignBean> beans = projectBeans.get(project);
            if (beans == null) {
                beans=new ArrayList<DesignBean>();
                projectBeans.put(project, beans);
            }
            beans.add(srcBean);
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
        
        String command = (String)srcBean.getProperty("command").getValue();
        VisualSQLEditor vse = VisualSQLEditorFactory.createVisualSQLEditor(dbconn, command, metadata);
        
        vse.addPropertyChangeListener(vseListener);
        Component retComp = vse.open();
        if (retComp instanceof TopComponent) {
            queryEditors.put(srcBean, new QBPair((TopComponent)retComp, vse));
        }
        
        return retComp ;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( "projrave_ui_elements_editors_about_query_editor" );        // NOI18N
    }
      
        
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
    

    // Listen for changes to statement property; update design bean
    
    private PropertyChangeListener vseListener =
            
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // what property?
                String propName = evt.getPropertyName() ;
                Log.log("VSE property change: " + propName ) ;
                if ( propName.equals(VisualSQLEditor.PROP_STATEMENT)) {
                    
		    // Get the VSE that raised the event
                    VisualSQLEditor vse = (VisualSQLEditor)evt.getSource();

                    // Find the bean that is associated with this VSE in the map
                    for (DesignBean bean : queryEditors.keySet()) {
                        QBPair qbp = queryEditors.get(bean);
                        if ((qbp != null) && (qbp.vse == vse)) { 
                            Log.err.log(" newValue=" + vse.getStatement()) ;
			    // Found it.  Update the bean property.
                            bean.getProperty("command").setValue(vse.getStatement()) ;
			    break;
                        }
                    }    
                }
            }
        } ;

    
    // Listen for project close events; update Maps
    
    private class OpenProjectsListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent event) {
            
            // The list of open projects has changed; clean up any old projects we may be holding on to.
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(event.getPropertyName())) {

                List<Project> oldOpenProjectsList = Arrays.asList((Project[]) event.getOldValue());
                List<Project> newOpenProjectsList = Arrays.asList((Project[]) event.getNewValue());
                Set<Project> closedProjectsSet = new LinkedHashSet<Project>(oldOpenProjectsList);
                closedProjectsSet.removeAll(newOpenProjectsList);
                for (Project project : closedProjectsSet) {

                    // Project has been closed; close any open QueryEditors, then remove it from map
                    ArrayList<DesignBean> beans = projectBeans.get(project);
		    if (beans!=null) {
			for (DesignBean bean : beans ) {
			    QBPair qbp = queryEditors.get(bean);
			    if (qbp != null) { 
				qbp.qb.close();
			    }
			    queryEditors.remove(bean);
			}
		    }
                    projectBeans.remove(project);
                }
            }
        }
    }
    
    // Data class representing a <QueryBuilder, VisualSQLEditor> pair.  Used as the value in the HashMap
    private class QBPair {
        TopComponent qb;
        VisualSQLEditor vse;
        QBPair(TopComponent qb, VisualSQLEditor vse) {
            this.qb=qb;
            this.vse=vse;
        }
    }
}
