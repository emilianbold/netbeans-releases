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
/*
 * ImportDataSource.java
 *
 * Created on June 17, 2005, 2:49 PM
 *
 */

package org.netbeans.modules.visualweb.dataconnectivity.utils;

import org.netbeans.modules.visualweb.dataconnectivity.DataconnectivityModuleInstaller;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfoManager;
import org.netbeans.modules.visualweb.dataconnectivity.ui.ImportDataSourcesDialog;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DesignTimeInitialContextFactory;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DataSourceImport;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSourceHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 * This imports a (previously) exported datasource.xml file.
 *
 * @author jfbrown
 */
public class ImportDataSource {

    public static final String BUNDLED_DB_URL_SUBSTRING="jdbc:derby://localhost:1527/" ;
    /**
     * Creates a new instance of ImportDataSource
     */
    public ImportDataSource() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        // check args
        if ( args.length <  2 ) {
            printArgs() ;
            RunSqlFromFile.bomb() ;
        }
        String filename = args[0] ;
        String userDir = args[1] ;
        String propFile = null ;
        if ( args.length >2 ) {
            propFile = args[2] ;
        }

        System.setProperty("netbeans.user", userDir ) ;

        try {
            importDataSource( filename, userDir, propFile ) ;
        } catch (Exception eee) {
            System.err.println("Import Failed: " + eee.getLocalizedMessage() ) ;
            eee.printStackTrace() ;
            RunSqlFromFile.bomb() ;
        }
    }

    public static void importDataSource( String filename, String userDir, String propFile)
        throws IOException, SAXException, NamingException {
        // make sure input file exists.
        File inputFile = new File(filename) ;
        if ( ! inputFile.exists() ) {
            throw new IOException("Input file '"+filename+"' does not exist") ;

        }

        // make sure userdir has a context.xml.
        if ( userDir.endsWith(File.separator) ) {
            userDir = userDir.substring(0,  userDir.length()-File.separator.length()) ;
        }
        String ctxPathName = userDir + File.separator + "context.xml" ; // NOI18N

        File ctxFile = new File(ctxPathName) ;
        if ( ! ctxFile.exists() ) {
            throw new IOException("User Directory is not valid.  The file '"+ctxPathName+"' does not exist") ;
        }

        // find the database port.
        String port = null ;
        String derbyPropertyPort = DbPortUtilities.getDerbyPortFromDerbyProperties() ;
        if ( derbyPropertyPort != null ) {
            port = derbyPropertyPort ;
        } else {
            if (propFile != null ) {
                // read the port from the config file
                String fPort = DbPortUtilities.getPropFromFile( new File(propFile), "derbyPort" ) ;
                if ( fPort != null) port = fPort ;
            }
        }
        if ( port == null) port = "1527" ; // default

        // Now load the Context File
        DesignTimeInitialContextFactory.setInitialContextFactoryBuilder();
        Context ctx = null ;
        DesignTimeDataSourceHelper dsHelper = null ;

        // Can throw NamingException...
            ctx = new InitialContext() ;
            dsHelper = new DesignTimeDataSourceHelper();

        FileInputStream is = null ;
        try{
            is = new FileInputStream(inputFile);
            DataSourceImport[] dsImports = dsHelper.getDataSourceImports(is);
            is.close();
            if(dsImports != null){
                // clean up name and patch up port.
                for (int i=0; i< dsImports.length; i++) {
                    // Fixup name.
                    String dsName = dsImports[i].getName();
                    if(!DataSourceInfoManager.getInstance().isDataSourceNameUnique(dsName,true) ){
                        dsName = DataSourceInfoManager.getInstance().getUniqueDataSourceName(dsName);
                        dsImports[i].setName(dsName);
                    }
                    // Fixup port number
                    String url = dsImports[i].getUrl() ;
                    if ( url.indexOf(BUNDLED_DB_URL_SUBSTRING) >= 0 ) {
                        url.replaceAll("1527",  port ) ;
                        dsImports[i].setUrl(url) ;
                    }
                }
                for (int i = 0; i < dsImports.length; i++) {
                    DataSourceImport dsi = dsImports[i];
                    if (dsi.isImportable()) {
                        DataSourceInfo dsInfo ;
                        if ( ! dsi.isAlias() ) {
                            dsInfo = new DataSourceInfo(dsi.getDisplayName(), dsi.getDriverClassName(), dsi.getUrl(),
                                    dsi.getValidationQuery(), dsi.getUsername(), dsi.getPassword());
                        } else {
                            dsInfo = new DataSourceInfo(dsi.getDisplayName(), dsi.getAlias());
                        }
                        DataSourceInfoManager.getInstance().addDataSourceInfo(dsInfo);
                        System.out.println("Imported data source "+dsi.getDisplayName() );
                    }
                }
            }
        }catch (IOException ioExc){
            throw ioExc ;
            /*
            String emsg = ioExc.getLocalizedMessage() ;
            if ( emsg == null ) emsg  = ioExc.getMessage() ;
            String msg = NbBundle.getMessage(ImportDataSourcesDialog.class, "READ_ERROR", filename, emsg);
            System.err.println(msg) ;
            RunSqlFromFile.bomb() ;
             */
        }catch (SAXException saxExc){
            throw saxExc ;
            /*
            String msg = NbBundle.getMessage(ImportDataSourcesDialog.class, "SAX_ERROR");
            System.err.println(msg) ;
            RunSqlFromFile.bomb() ;
             **/
        } 
        finally {
            if ( is != null ) {
                try {
                    is.close() ;
                } catch(Exception eee) {
                    /// don't care.
                }
            }
        }
        
    }
    public static void printInfo(String errMsg) {
        System.out.println(errMsg);
    }
    
    public static void printError(String errMsg) {
        System.out.println(errMsg);
        System.out.println("");
    }
    
    public static void printArgs() {
        System.out.println("Syntax:  ImportDataSources <file> <userdir>");
    }
    
    public static void bonb() {
        RunSqlFromFile.bomb() ;
    }
}
