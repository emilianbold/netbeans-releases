/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedResource;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.CurrentProject;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.DataSourceResolver;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.visualweb.dataconnectivity.model.ProjectDataSourceManager;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DatabaseSettingsImporter;
import org.netbeans.modules.visualweb.dataconnectivity.project.datasource.ProjectDataSourceTracker;
import org.netbeans.modules.visualweb.dataconnectivity.utils.ImportDataSource;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.services.DesignTimeDataSourceService;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/**
 * Helper class used by server navigator.  Helps manage
 * datasources in Creator's naming context.
 *
 * @author John Kline, John Baker
 */
public class DesignTimeDataSourceHelper {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle",
        Locale.getDefault());
    private volatile boolean firstTimeShowAlert = false;

    private static final String    DS_SUBCTX       = "java:comp/env/jdbc"; // NOI18N
    private static final String    ROOT_TAG        = "dataSources"; // NOI18N
    private static final String    ATTR_MAJ_VER    = "majVer"; // NOI18N
    private static final String    ATTR_MIN_VER    = "minVer"; // NOI18N
    private static final String    DATASOURCE_TAG  = "dataSource"; // NOI18N
    private static final String    ATTR_NAME       = "name"; // NOI18N
    private static final String    ATTR_DRIVER     = "driverClassName"; // NOI18N
    private static final String    ATTR_URL        = "url"; // NOI18N
    private static final String    ATTR_USERNAME   = "username"; // NOI18N
    private static final String    ATTR_PASSWORD   = "password"; // NOI18N
    private static final String    ATTR_QUERY      = "validationQuery"; // NOI18N
    private static final String    ATTR_REFERENCES = "references" ;
    private DesignTimeDataSource[] dataSources;
    private String[]               dataSourceNames;
    private boolean                isDataSourceAdded;
    private Context                ctx;

    public DesignTimeDataSourceHelper() throws NamingException {
        dataSources     = null;
        dataSourceNames = null;
        isDataSourceAdded = false;    
        ctx = new InitialContext();
    }

    public DataSourceExport[] getDataSourceExports() throws NamingException {
        getDataSources();

        ArrayList exports = new ArrayList() ;
        /* !JK - the following is not good if the datasource list changes
         * but the server navigator won't be adding/changing/deleting datasources
         * while doing this
         */
        for (int i = 0; i < dataSources.length; i++) {
            DesignTimeDataSource ds = dataSources[i];

            if ( !  (ds instanceof DesignTimeDataSourceAlias)  ) {
                DataSourceExport dse = new DataSourceExport(dataSourceNames[i], ds.getDriverClassName(),
                    ds.getUrl(), ds.getUsername(), ds.getPassword(), ds.getValidationQuery())
                    ;
                exports.add(dse) ;
            }
            else {
                DataSourceExport dse = new DataSourceExport(dataSourceNames[i],((DesignTimeDataSourceAlias)ds).getAlias() ) ;
                exports.add(dse) ;
            }
        }
        return (DataSourceExport[])exports.toArray(new DataSourceExport[0]) ;
    }

    static public void writeExportDocument(OutputStream os, DataSourceExport[] exports)
        throws IOException {

        os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8")); // NOI18N
        os.write(("<" + ROOT_TAG + " majVer=\"1\" minVer=\"0\">\n").getBytes("UTF-8")); // NOI18N

        for (int i = 0; i < exports.length; i++) {
            DataSourceExport dse = exports[i];
            if (dse.isExportable()) {
                os.write(("    <" + DATASOURCE_TAG + " name=\"" + DesignTimeDataSource.escapeXML(dse.getName()) // NOI18N
                    + "\"\n").getBytes("UTF-8")); // NOI18N
                
                if ( dse.isAlias() ) {
                    os.write(("        " + ATTR_REFERENCES + "=\"" + DesignTimeDataSource.escapeXML(dse.getAlias()) // NOI18N
                            + "\"\n").getBytes("UTF-8")); // NOI18N
                }
                else {
                    if (dse.getDriverClassName() != null) {
                        os.write(("        driverClassName=\"" + dse.getDriverClassName() // NOI18N
                            + "\"\n").getBytes("UTF-8")); // NOI18N
                    }
                    if (dse.getUrl() != null) {
                        os.write(("        url=\"" + DesignTimeDataSource.escapeXML(dse.getUrl()) // NOI18N
                            + "\"\n").getBytes("UTF-8")); // NOI18N
                    }
                    if (dse.getValidationQuery() != null) {
                        os.write(("        validationQuery=\"" + DesignTimeDataSource.escapeXML(dse.getValidationQuery()) // NOI18N
                            + "\"\n").getBytes("UTF-8")); // NOI18N
                    }
                    if (dse.isUsernameExportable()) {
                        if (dse.getUsername() != null) {
                            os.write(("        username=\"" + dse.getUsername() // NOI18N
                                + "\"\n").getBytes("UTF-8")); // NOI18N
                        }
                        if (dse.isPasswordExportable()) {
                            if (dse.getPassword() != null) {
                                os.write(("        password=\"" // NOI18N
                                    + DesignTimeDataSource.encryptPassword(dse.getPassword())
                                    //!JK + dse.getPassword()
                                    + "\"\n").getBytes("UTF-8")); // NOI18N
                            }
                        }
                    }
                }
                os.write("    />\n".getBytes("UTF-8")); // NOI18N
            }
        }
        os.write(("</" + ROOT_TAG + ">\n").getBytes("UTF-8")); // NOI18N
    }

    static public DataSourceImport[] getDataSourceImports(InputStream is)
        throws IOException, SAXException {

        final ArrayList list = new ArrayList();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            parser.parse(is, new DefaultHandler() {
                public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                    if (qName.equals(ROOT_TAG)) {
                        String majVer = attributes.getValue(ATTR_MAJ_VER);
                        String minVer = attributes.getValue(ATTR_MIN_VER);
                        if (majVer == null || minVer == null ||
                            !(majVer.equals("1") && minVer.equals("0"))) { // NOI18N
                            throw new SAXException(MessageFormat.format(
                                rb.getString("ONLY_VER_MAJ_MIN"),
                                new Object[] { "1", "0" })); // NOI18N
                        }
                    } else if (qName.equals(DATASOURCE_TAG)) {
                        String alias = attributes.getValue(ATTR_REFERENCES) ;
                        String name = attributes.getValue(ATTR_NAME);
                        if ( alias == null ) {
                            String driverClassName = attributes.getValue(ATTR_DRIVER);
                            String url = attributes.getValue(ATTR_URL);
                            String username = attributes.getValue(ATTR_USERNAME);
                            String password = attributes.getValue(ATTR_PASSWORD);
                            if (password != null) {
                                password = DesignTimeDataSource.decryptPassword(password);
                            }
                            String validationQuery = attributes.getValue(ATTR_QUERY);
                            if (name == null) {
                                throw new SAXException(rb.getString("MISSING_NAME_ATTRIBUTE"));
                            }
                            if (driverClassName  == null) {
                                throw new SAXException(rb.getString("MISSING_DRIVER_CLASS_NAME_ATTRIBUTE"));
                            }
                            if (url == null) {
                                throw new SAXException(rb.getString("MISSING_URL_ATTRIBUTE"));
                            } 
                            try {
                                list.add(new DataSourceImport(makeUnique(name), driverClassName, url,
                                    username, password, validationQuery));
                            } catch (NamingException e) {
                                throw new SAXException(e);
                            }                           
                        }
                        else {
                            // add the alias.
                            list.add(new DataSourceImport(name,alias)) ;
                        }
                    } else {
                        throw new SAXNotRecognizedException(qName);
                    }
                }
                public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                    if (qName.equals(ROOT_TAG)) {
                    } else if (qName.equals(DATASOURCE_TAG)) {
                    } else {
                        throw new SAXNotRecognizedException(qName);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (DataSourceImport[])list.toArray(new DataSourceImport[0]);
    }

    static private String makeUnique(String name) throws NamingException {
        String newName = name;
        int ctr        = 0;
        while (true) {
            try {
                new InitialContext().lookup(newName);
                ctr++;
                newName = name + ctr;
            } catch (NameNotFoundException e) {
                return newName;
            }
        }
    }

    public int importDataSources(DataSourceImport[] imports) throws NamingException {
        int importCount = 0;

        for (int i = 0; i < imports.length; i++) {
            DataSourceImport dsi = imports[i];
            if (dsi.isImportable()) {
                addFullNameDataSource(dsi.getName(), dsi.getDriverClassName(), dsi.getUrl(),
                    dsi.getValidationQuery(), dsi.getUsername(), dsi.getPassword());
                importCount++;
            }
        }

        refresh();
        return importCount;
    }

    public String[] getDataSourceNames() throws NamingException {
        if (dataSourceNames == null) {
            getDataSources();
        }
        return dataSourceNames;
    }

    public DesignTimeDataSource[] getDataSources() throws NamingException {
        if (dataSources == null) {

            ArrayList both = getAllDataSourceBindings() ;
            ArrayList dataSourceList = new ArrayList() ;
            ArrayList dataSourceNameList = new ArrayList() ;
            for (int icnt=0 ; icnt < both.size() ; icnt++ ) {
                dataSourceList.add( ((Binding)both.get(icnt)).getObject() ) ;
                dataSourceNameList.add( ((Binding)both.get(icnt)).getName() ) ;
            }
            dataSources = (DesignTimeDataSource[])dataSourceList.toArray(
                new DesignTimeDataSource[0]);
            dataSourceNames = (String[])dataSourceNameList.toArray(
                new String[0]);
            
        }
        return dataSources;
        
    }
    
    /***
     * Compose list of all data sources.
     * @returns an ArrayList of Binding objects
     */
    public ArrayList getAllDataSourceBindings( ) throws NamingException  {
        return getNamesAndDataSources( ctx, DS_SUBCTX, "" ) ;
    } 
    public ArrayList getNamesAndDataSources( Context curCtx, String startName, String dsNamePrefix ) 
        throws NamingException {
            String nodeName = null;
            ArrayList retVal = new ArrayList() ;
            
            NamingEnumeration list = curCtx.listBindings(startName);
            while (list.hasMore()) {
                Binding binding = (Binding)list.next();
                String name = binding.getName();
                if (binding.isRelative()) {
                    // append the name to the startimg name.
                    name = startName + "/" + name; // NOI18N
                }
                nodeName = name.substring(name.lastIndexOf("/") + 1);
                if (binding.getObject() instanceof  DesignTimeDataSource) {

                    String dsNodeName = ("".equals(dsNamePrefix) ? "" : dsNamePrefix + "/") + nodeName ;
                    retVal.add( new Binding( dsNodeName, (DesignTimeDataSource)binding.getObject() ) ) ;
                
                }
                else if ( binding.getObject() instanceof Context ) {     
                    // It's a subcontext, so search that.
                    retVal.addAll(getNamesAndDataSources((Context)binding.getObject(), "", dsNamePrefix + ("".equals(dsNamePrefix) ? "" : "/" ) + nodeName ) );
                }
            }
            list.close();    
            return retVal ;
    }

    public DesignTimeDataSource getDataSource(String name) throws NamingException {
        return getDataSourceFromFullName(DS_SUBCTX + "/" + name); // NOI18N
    }

    public DesignTimeDataSource getDataSourceFromFullName(String fullName)
        throws NamingException {
        Object obj = ctx.lookup(fullName);
        if (obj instanceof DesignTimeDataSource) {
            return (DesignTimeDataSource)obj;
        }
        throw new NamingException(MessageFormat.format(
            rb.getString("NOT_A_DATASOURCE"), new Object[] { fullName }));
    }

    public DesignTimeDataSource addDataSource(String name, String driverClassName,
        String url, String validationQuery, String username, String password)
        throws NamingException {

        isDataSourceAdded = true;
        return addFullNameDataSource(DS_SUBCTX + "/" + name, driverClassName, url, validationQuery, // NOI18N
            username, password);
    }

    public DesignTimeDataSource addFullNameDataSource(String name, String driverClassName,
        String url, String validationQuery, String username, String password)
        throws NamingException {

        DesignTimeDataSource ds = new DesignTimeDataSource(null, false, driverClassName,
            url, validationQuery, username, password);      
        
        return addFullNameDataSource( name, ds ) ;
    }
    public DesignTimeDataSource addFullNameDataSource(String name, DesignTimeDataSource ds )
        throws NamingException {
        
        try {                      
            ctx.bind(name, ds);
        } catch (NameAlreadyBoundException e) {
            throw e;
        } catch (NamingException e) {
            // perhaps the subcontext does not exist
            // we'll attempt to create every name on the path
            // and try to bind one more time
            CompositeName cname = new CompositeName(name);
            String subcontext = "";
            for (int i = 0; i < cname.size()-1; i++) {
                subcontext += cname.get(i);
                try {                    
                    ctx.createSubcontext(subcontext);
                } catch (NameAlreadyBoundException e2) {
                }
                subcontext += "/"; // NOI18N
            }
            ctx.bind(name, ds);
        }
        refresh();
        return ds;
    }

    public void deleteDataSource(String name) throws NamingException {
        String thisName = DS_SUBCTX + "/" + name ;
        ctx.unbind(thisName); // NOI18N
        
        // Now clean up the tree
        // by deleting any empty subcontext nodes above.
        String[] parts = name.split("/") ;
        if ( parts.length > 1 ) {
            for ( int icnt = parts.length-2 ; icnt >= 0 ; icnt-- ) {
                int lastOne = thisName.lastIndexOf("/") ;
                thisName  = thisName.substring(0, lastOne) ;
                ArrayList bindings = getNamesAndDataSources( ctx, thisName , "" ) ;
                if ( bindings.size() > 0 ) {
                    // has children, so do not destroy it.
                    break ;
                }
                // remove sub context.         
                // ctx.destroySubcontext(thisName) ;
                ctx.unbind(thisName) ;
            }
        }
        refresh();
    }
    
    /*** 
     * Check to see if the name or part of the name is in use.
     * @returns false means you can bind to this name.
     *
     * if you have a datasource  with name "a/b" then "a" must be
     * a subcontext or missing, and "b" must not exist.
     */
    public boolean nameInUseInContext( String name ) {
        String thisName = DS_SUBCTX + "/" + name ;
        
        try {            
            Object obj = ctx.lookup(thisName) ;
            // found!
            return true ;
        } catch( NamingException ne ) {
            if ( ! (ne instanceof NameNotFoundException )) {
               return true ;
            }
        }
        
        // Name was not found - now look for subContext values.
        // so if name was x/y/z, make sure both x/y and x are
        // instances of Context or not found.
        String[] parts = name.split("/") ;
        
        for ( int icnt = parts.length-2 ; icnt >= 0 ; icnt-- ) {
            int lastOne = thisName.lastIndexOf("/") ;
            thisName  = thisName.substring(0, lastOne) ;
            try {
                Object obj = ctx.lookup(thisName) ;
                if ( obj instanceof Context ) {
                    continue ;
                }
                return true ;
            }
            catch(NamingException ne) {
                if ( ne instanceof NameNotFoundException) continue ;
                return true ;
            }
        }        
        
        return false ;
    }
    /*
     * save() needs to be called explicity if you change a datasource.  Adds and
     * deletions automatically refresh, but changes to a datasource cannot be detected.
     */
    public void save() throws NamingException {        
        ctx.addToEnvironment("save-context", "true"); // 2nd arg is ignored // NOI18N
    }

    public void refresh() {
        dataSources     = null;
        dataSourceNames = null;
    }
    
    
    public Map updateDataSource(Project currentProj) throws NamingException {       
         // Manage the migration of legacy projects
        if (ImportDataSource.isLegacyProject(currentProj) && JsfProjectUtils.getProjectProperty(currentProj, "migrated").equals("")) {//NOI18N   
            DataSourceResolver.getInstance().updateSettings();
            JsfProjectUtils.createProjectProperty(currentProj, "migrated", "true");
        }
        
        // Get the data sources in the project then bind them to the project's context
        String[] dynamicDataSources = ProjectDataSourceTracker.getDynamicDataSources(currentProj);
        String[] hardCodedDataSources = ProjectDataSourceTracker.getHardcodedDataSources(currentProj);
        List <RequestedResource> jdbcResources = new ArrayList<RequestedResource>();
        RequestedJdbcResource jdbcResource = null;
        List <DesignTimeDataSource> ds = null;
        Map binding = new HashMap();
                 
        ProjectDataSourceManager projectDataSourceManager = new ProjectDataSourceManager(currentProj);
        
        if (dynamicDataSources.length > 0 || hardCodedDataSources.length > 0) {
            for (String name : dynamicDataSources) {
                jdbcResource = (projectDataSourceManager.getDataSourceWithName(name.substring(name.lastIndexOf("/")+1)));
                
                if (jdbcResource != null)
                    jdbcResources.add(jdbcResource);
            }
            
            
            for (String name : hardCodedDataSources) {
                jdbcResource = (projectDataSourceManager.getDataSourceWithName(name.substring(name.lastIndexOf("/")+1)));
                
                if (jdbcResource != null)
                    jdbcResources.add(jdbcResource);
            }
            
            // Add resource reference to web.xml. If already added then resource is not added.
            DatabaseSettingsImporter.getInstance().updateWebXml(currentProj, jdbcResources);
            
            // Support for Creator 2 projects and a hack - serverplugin not detecting datasources in project
            if (jdbcResource == null) {
                RequestedJdbcResource[] resources = null;                
                DataSourceInfo dsInfo = null;
                DesignTimeDataSourceService dataSourceService = null;
                
                for (String name : dynamicDataSources) {
                    List<DataSourceInfo> dataSourcesInfo = DatabaseSettingsImporter.getInstance().getDataSourcesInfo();
                    Iterator it = dataSourcesInfo.iterator();
                    while (it.hasNext()) {
                        dsInfo = (DataSourceInfo)it.next();
                        if (name.equals(DS_SUBCTX + "/VIR") && dsInfo.getUrl().equals("jdbc:derby://localhost:1527/sample")) {
                            binding.put(name, new DesignTimeDataSource(null, false, dsInfo.getDriverClassName(),
                                    "jdbc:derby://localhost:1527/vir", null, dsInfo.getUsername(), dsInfo.getPassword())); // NOI18N                            
                            dataSourceService = (DesignTimeDataSourceService)Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
                            dataSourceService.updateProjectDataSource(currentProj, new RequestedJdbcResource("jdbc/VIR",
                                    dsInfo.getDriverClassName(), "jdbc:derby://localhost:1527/vir", dsInfo.getUsername(),
                                    dsInfo.getPassword()));  // NOI18N
                        } else if (name.equals(DS_SUBCTX + "/" + dsInfo.getName())) { // NOI18N                                                                                    
                            binding.put(name, new DesignTimeDataSource(null, false, dsInfo.getDriverClassName(),
                                    dsInfo.getUrl(), null, dsInfo.getUsername(), dsInfo.getPassword())) ;
                            
                            dataSourceService = (DesignTimeDataSourceService)Lookup.getDefault().lookup(DesignTimeDataSourceService.class);
                            dataSourceService.updateProjectDataSource(currentProj, new RequestedJdbcResource("jdbc/" + //NOI18N
                                    dsInfo.getName(),
                                    dsInfo.getDriverClassName(), dsInfo.getUrl(), dsInfo.getUsername(),
                                    dsInfo.getPassword()));                           
                        }
                    }
                }
            } else {
                // Check if datasource exists in the context.  If it doesn't exist then bind the datasource .
                Iterator it = jdbcResources.iterator();
                boolean found = false;
                
                while (it.hasNext()) {                    
                    jdbcResource = (RequestedJdbcResource) it.next();
                    String name = ((String)jdbcResource.getResourceName());
                    name = name.substring(name.indexOf("/")+1);
                    found = false;
                    if (name.equals(DS_SUBCTX + "/VIR") && jdbcResource.getUrl().equals("jdbc:derby://localhost:1527/sample")) {
                        binding.put(name, new DesignTimeDataSource(null, false, jdbcResource.getDriverClassName(), "jdbc:derby://localhost:1527/vir", null, jdbcResource.getUsername(), jdbcResource.getPassword())); // NOI18N
                    } else if (!found)
                        binding.put(DS_SUBCTX + "/" + name, new DesignTimeDataSource(null, false, jdbcResource.getDriverClassName(), jdbcResource.getUrl(), null, jdbcResource.getUsername(), jdbcResource.getPassword()));
                }
            }
        }
        
        return binding;
    }
     
     public boolean dataSourceAdded() {
         return isDataSourceAdded;
     }
         
     // Update context with existing project's data sources, if any.
    public void updateCtxBindings(Map bindings) {
        Iterator it = bindings.keySet().iterator();
        
        try {
           while (it.hasNext()) {
               String key = (String)it.next();               
               ctx.bind(key, bindings.get(key));
           }
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }
    
    public boolean datasourcesInProject(Project currentProj) {
        String[] dynamicDataSources = ProjectDataSourceTracker.getDynamicDataSources(currentProj);
        String[] hardCodedDataSources = ProjectDataSourceTracker.getHardcodedDataSources(currentProj);        
        
        if (dynamicDataSources.length > 0 || hardCodedDataSources.length > 0) 
            return true;
        else
            return false;
    }
    
    public static DataSourceInfo getDsInfo(String dsName) {
        ProjectDataSourceManager projectDataSourceManager  = new ProjectDataSourceManager(CurrentProject.getInstance().getOpenedProject());
        RequestedJdbcResource jdbcResource = null;
        jdbcResource = projectDataSourceManager.getDataSourceWithName(dsName);
        
        if (jdbcResource == null) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(NbBundle.getMessage(DesignTimeDataSourceHelper.class, "MSG_NameNotFoundMessage"), NotifyDescriptor.INFORMATION_MESSAGE));
            return null;
        }

        return new DataSourceInfo(dsName, jdbcResource.getDriverClassName(), jdbcResource.getUrl(), null, jdbcResource.getUsername(), jdbcResource.getPassword());
    }
    
    /**
     * Make sure the connections needed by the data sources have been registered
     * using DatasourceInfo
     */
     public static boolean isFound(DataSourceInfo ds) {
        boolean found = false;
        
        if (ds != null) {
            String url = ds.getUrl();
            String username = ds.getUsername();
            DatabaseConnection[] dbConns = ConnectionManager.getDefault().getConnections();
            for(int i=0; i<dbConns.length; i++ ){
                DatabaseConnection dbCon = dbConns[i];
                String url1 = dbCon.getDatabaseURL();
                String username1 = dbCon.getUser();
                if (matchURL(url, url1, true) && Utilities.compareObjects(username, username1)) {
                    found = true;
                }
            }
        }
        return found;
    }
    
     /**
     * Make sure the connections needed by the data sources have been registered
      * using DesignTimeDataSource
     */
     public static boolean isFound(DesignTimeDataSource ds) {
        boolean found = false;
        
        if (ds != null) {
            String url = ds.getUrl();
            String username = ds.getUsername();
            DatabaseConnection[] dbConns = ConnectionManager.getDefault().getConnections();
            for(int i=0; i<dbConns.length; i++ ){
                DatabaseConnection dbCon = dbConns[i];
                String url1 = dbCon.getDatabaseURL();
                String username1 = dbCon.getUser();
                if (matchURL(url, url1, true) && Utilities.compareObjects(username, username1)) {
                    found = true;
                }
            }
        }
        return found;
    }
    
    private static boolean matchURL(String jdbcResourceUrl, String dsInfoUrl, boolean ignoreCase) {
        if (ignoreCase){
            jdbcResourceUrl = jdbcResourceUrl.toLowerCase();
            dsInfoUrl = dsInfoUrl.toLowerCase();
        }
        if (jdbcResourceUrl.equals(dsInfoUrl)){
            return true;
        }
        
        if (jdbcResourceUrl.contains("derby")) {
            String newJdbcResourceUrl = jdbcResourceUrl.substring(0, jdbcResourceUrl.lastIndexOf(":")) + jdbcResourceUrl.substring(jdbcResourceUrl.lastIndexOf("/"));
            if (newJdbcResourceUrl.equals(dsInfoUrl)){
                return true;
            }
        }
        
        int nextIndex = 0;
        if ((jdbcResourceUrl != null) && (dsInfoUrl != null)){
            char[] jdbcResourceUrlChars = jdbcResourceUrl.toCharArray();
            char[] dsInfoUrlChars = dsInfoUrl.toCharArray();
            for(int i = 0; i < jdbcResourceUrlChars.length - 1; i++){
                if ((jdbcResourceUrlChars[i] != dsInfoUrlChars[i]) && jdbcResourceUrlChars[i] == ':'){
                    nextIndex = 1;
                } else if (jdbcResourceUrlChars[i + nextIndex] != dsInfoUrlChars[i]){
                    return false;
                }
            }
        }
        return true;
    }              
}
