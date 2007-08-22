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

package org.netbeans.modules.visualweb.dataconnectivity.naming;

import org.netbeans.modules.visualweb.dataconnectivity.utils.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarFile;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedResource;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.DataSourceResolver;
import org.netbeans.modules.visualweb.dataconnectivity.model.DataSourceInfo;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * DatabaseSettingsImporter imports JDBC drivers and connections from previous Creator/visualweb releases then registers them in DB Explorer
 * @author John Baker
 */
public class DatabaseSettingsImporter {
    
    private static DatabaseSettingsImporter databaseSettingsImporter;
    public static final String  ROOT_CTX_TAG = "rootContext"; // NOI18N
    public static final String  CTX_TAG      = "context"; // NOI18N
    public static final String  OBJ_TAG      = "object"; // NOI18N
    public static final String  ARG_TAG      = "arg"; // NOI18N
    public static final String  NAME_ATTR    = "name"; // NOI18N
    public static final String  CLASS_ATTR   = "class"; // NOI18N
    public static final String  VALUE_ATTR   = "value"; // NOI18N
    public static final String DRIVER_CLASS_NET = "org.apache.derby.jdbc.ClientDriver"; // NOI18N
    private File         userCtxFile;
    private List <String[]>    dataSources;            
    List <DataSourceInfo> dataSourcesInfo;   
    
    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.naming.Bundle", // NOI18N
            Locale.getDefault());
    private static String[] destPaths = new String [] {"migrated"  + File.separator +  "context.xml", "migrated"  +
            File.separator +  "2_0"  + File.separator +  "context.xml", "migrated"  + File.separator +  "2_1"  + File.separator +  "context.xml", 
            "migrated"  + File.separator +  "5_5"  + File.separator +  "context.xml", "migrated"  + File.separator +  "5_5_1"  + File.separator +  "context.xml"};
    
    
    private static final String HACK_WELCOME_FILE = "JSCreator_index.jsp"; // NOI18N
    
    /** Creates a new instance of DatabaseImporter */
    private DatabaseSettingsImporter() {
        dataSources = Collections.synchronizedList(new ArrayList<String[]>());
        dataSourcesInfo = new ArrayList<DataSourceInfo>();
    }
    
    /**
     * this class is a Singleton
     * @return
     */
    public static DatabaseSettingsImporter getInstance() {
        if (databaseSettingsImporter == null){
            databaseSettingsImporter = new DatabaseSettingsImporter();
        }
        return databaseSettingsImporter;
    }
    
    
    /**
     * Obtain JDBC driver jars from previous release then register the drivers
     * @param isStartup 
     * @return
     */
    public boolean locateAndRegisterDrivers() {
        String driversPath = locateDrivers();
        if (driversPath.equals("")) // NOI18N
            return false;
        
        registerDrivers(driversToRegister(driversPath));
        return true;
    }
    
    private String locateDrivers() {
        String driverLocation;
        
        driverLocation = System.getProperty("netbeans.user") + File.separator +  "jdbc-drivers"; // NOI18N
        File driverDir = new File(driverLocation);
        if (driverDir == null)
            return ""; // NOI18N
        if (driverDir.exists())
            return driverLocation;
        else
            return ""; // NOI18N
    }
    
    private File[] driversToRegister(String driversPath) {
        File driverDir = new File(driversPath);
        
        if (driverDir != null) {
            File[] drivers = driverDir.listFiles(
                    new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar") || name.endsWith(".zip"); // NOI18N
                }
            });
            
            return drivers;
        } else {
            return null;
        }

    }
    
    private void registerDriver(File driverJar) {
        String[] drivers = (String[]) DriverListUtil.getDrivers().toArray(new String[DriverListUtil.getDrivers().size()]);
        
        try {
            JarFile jf = new JarFile(driverJar);
            
            String drv;
            Set drvs = DriverListUtil.getDrivers();
            Iterator it = drvs.iterator();
            while (it.hasNext()) {
                drv = (String) it.next();
                if (jf.getEntry(drv.replace('.', '/') + ".class") != null) {//NOI18N
                    String driverName = DriverListUtil.getName(drv);
                    if (DataSourceResolver.getInstance().findMatchingDriver(DriverListUtil.getDriver(driverName)) != null)
                        break;
                    JDBCDriver driver = JDBCDriver.create(driverName, driverName, drv, new URL[] {driverJar.toURI().toURL()});
                    try {
                        JDBCDriverManager.getDefault().addDriver(driver);
                    } catch (DatabaseException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
            jf.close();
            
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    
    private void registerDrivers(File[] driverFiles) {
        for (File drv : driverFiles) {
            registerDriver(drv);            
        }
    }
    
    
    /**
     * Obtain connection info from previous release's context.xml then register connections
     * @param isStartup flag indicates that settings were migrated at startup or not
     * @return
     */
    public boolean locateAndRegisterConnections(boolean isStartup) {
        File contextFile;
        Set <File> contextFiles = new HashSet<File>();
        
        if (isStartup) {
            destPaths = new String [] {"2_0"  + File.separator +  "context.xml", "2_1"  + File.separator +  "context.xml", 
             "5_5"  + File.separator +  "context.xml", "5_5_1"  + File.separator +  "context.xml"};
            contextFile = retrieveMigratedSettingsAtStartup();
            if (contextFile != null) {
                registerConnections(contextFile);
            }
        } else {
            for (int i = 0; i < destPaths.length; i++) {
                File ctxtFile = new File(System.getProperty("netbeans.user") + File.separator + "config" + File.separator + destPaths[i]);
                if (ctxtFile.exists()) {
                    contextFiles.add(ctxtFile);
                }
            }

            registerConnections(contextFiles);
        }
           
        
        return true;
    }
    
    public File retrieveMigratedSettingsAtStartup() {
        File contextReleaseRoot  = new File(System.getProperty("netbeans.user") + File.separator + "config" + File.separator); // NOI18N        
        File[] configDirs = null;                
        
        configDirs = contextReleaseRoot.listFiles();
        File contextFile = null;
        for (int i = 0; i < configDirs.length; i++) {
            File[] contextReleaseDirFiles = configDirs[i].listFiles();

            if (contextReleaseDirFiles == null) {
                return null;
            }
            for (File releaseDir : contextReleaseDirFiles) {
                String fileName = releaseDir.getName();
                if (fileName.equals("context.xml")) {
                    contextFile = contextReleaseDirFiles[i];
                    break;
                }
            }
        }
                        
        return contextFile;                
    }
    
    public Set <File> locateMigratedSettings() {
        File contextReleaseRoot  = new File(System.getProperty("netbeans.user")); // NOI18N
        File[] contextFileDirs = null;
        Set <File> contextReleaseDirFiles = new HashSet<File>();                 
        File migratedDir = new File (contextReleaseRoot.getAbsolutePath() + File.separator + "migrated");
        
        if (migratedDir == null || !migratedDir.exists() || !migratedDir.isDirectory()) {
           return null;
        }
        
        contextFileDirs = migratedDir.listFiles();
        for (File releaseDir : contextFileDirs) {
            contextReleaseDirFiles.add(releaseDir);          
        }
        
        return contextReleaseDirFiles;        
    }
    
    private void registerConnections(File contextFile) {
        dataSourcesInfo = createDataSourceInfoFromCtx(contextFile);
        
        if (dataSourcesInfo != null) {
            try {
                Iterator it = dataSourcesInfo.iterator();
                DataSourceInfo dsInfo = null;
                boolean isDriverJavaDB = false;
                DatabaseConnection dbconn = null;
                JDBCDriver drvs = null;
                JDBCDriver[] drvsArray = null;
                
                // From each Data Source, add a connection to DB Explorer
                while (it.hasNext()) {
                    dsInfo = ((DataSourceInfo)it.next());
                    String username = dsInfo.getUsername();
                    String password = dsInfo.getPassword();
                    isDriverJavaDB = dsInfo.getDriverClassName().equals(DRIVER_CLASS_NET);
                    
                    // To register a Derby connection, no need to check to see if Java DB driver had been registered
                    if (dsInfo.getDriverClassName().equals(DRIVER_CLASS_NET)) {
                        if (!dsInfo.getName().equals("Travel") && !dsInfo.getName().equals("JumpStartCycles")) { 
                            drvsArray = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
                            dbconn = DatabaseConnection.create(drvsArray[0], dsInfo.getUrl(), username,  username.toUpperCase(), password,  true);
                            ConnectionManager.getDefault().addConnection(dbconn);
                        }
                    } else {
                        drvs = DataSourceResolver.getInstance().findMatchingDriver(dsInfo.getDriverClassName());
                        if (drvs != null) {
                            dbconn = DatabaseConnection.create(drvs, dsInfo.getUrl(), username,  username.toUpperCase(), password,  true);
                            ConnectionManager.getDefault().addConnection(dbconn);
                        }
                    }
                }
            } catch (DatabaseException de) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, de);
            }
        }
    }
    
    
    private void registerConnections(Set<File> contextFiles) {
        if (contextFiles == null) {
            return;
        }
            
        Iterator it = contextFiles.iterator();                
        while (it.hasNext()) {
            dataSourcesInfo = createDataSourceInfoFromCtx((File)it.next());
            
            try {
                Iterator itDataSource = dataSourcesInfo.iterator();
                DataSourceInfo dsInfo = null;
                boolean isDriverJavaDB = false;
                DatabaseConnection dbconn = null;
                JDBCDriver drvs = null;
                JDBCDriver[] drvsArray = null;
                
                // From each Data Source, add a connection to DB Explorer
                while (itDataSource.hasNext()) {
                    dsInfo = ((DataSourceInfo)itDataSource.next());
                    String username = dsInfo.getUsername();
                    String password = dsInfo.getPassword();
                    isDriverJavaDB = dsInfo.getDriverClassName().equals(DRIVER_CLASS_NET);
                    
                    // To register a Derby connection, no need to check to see if Java DB driver had been registered
                    if (dsInfo.getDriverClassName().equals(DRIVER_CLASS_NET)) {
                        if (!dsInfo.getName().equals("Travel")) {
                            drvsArray = JDBCDriverManager.getDefault().getDrivers(DRIVER_CLASS_NET);
                            dbconn = DatabaseConnection.create(drvsArray[0], dsInfo.getUrl(), username,  username.toUpperCase(), password,  true);
                            ConnectionManager.getDefault().addConnection(dbconn);
                        }
                    } else {
                        drvs = DataSourceResolver.getInstance().findMatchingDriver(dsInfo.getDriverClassName());
                        if (drvs != null) {
                            dbconn = DatabaseConnection.create(drvs, dsInfo.getUrl(), username,  username.toUpperCase(), password,  true);
                            ConnectionManager.getDefault().addConnection(dbconn);
                        }
                    }
                }
            } catch (DatabaseException de) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, de);
            }
        }
    }
    
    static private SecretKey secretKey = null;
    
    static final private char[] secretKeyHex =
    {'D','6','0','7','5','E','2','9','8','A','4','9','6','2','5','1'};
    
    private static SecretKey getSecretKey() {
        if (secretKey == null) {
            byte[] encodedKey = new byte[secretKeyHex.length/2];
            for (int i = 0; i < encodedKey.length; i++) {
                encodedKey[i] = hexToByte(secretKeyHex[i*2], secretKeyHex[i*2+1]);
            }
            secretKey = new SecretKeySpec(encodedKey, "DES"); // NOI18N
        }
        
        return secretKey;
    }
    
    private static final String hexString = "0123456789ABCDEF"; // NOI18N
    
    private static byte hexToByte(char char1, char char2) {
        return (byte)((hexString.indexOf(char1) << 4) + hexString.indexOf(char2));
    }
    
    private String decryptPassword(String password) {
        
        if (password == null) {
            return null;
        }
        
        try {
            char[] hexChars = password.toCharArray();
            
            byte[] encryptedBytes = new byte[hexChars.length/2];
            for (int i = 0; i < encryptedBytes.length; i++) {
                encryptedBytes[i] = hexToByte(hexChars[i*2], hexChars[i*2+1]);
            }
            Cipher cipher = Cipher.getInstance("DES"); // NOI18N
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] passwordBytes = cipher.doFinal(encryptedBytes);
            return new String(passwordBytes);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return "";
        }
    }
    
    private DataSourceInfo createDataSourceInfo(String[] dataSource) {
        String dsName = dataSource[0];
        String driverClassName = dataSource[1];
        String driverUrl = dataSource[2];
        String username = dataSource[3];
        String password = dataSource[4];
        
        // create data source info
        password = decryptPassword(password);
        
        if (driverClassName.equals(DRIVER_CLASS_NET)) { // NOI18N
            int oldPortLoc = driverUrl.indexOf(":21527"); // NOI18N
            if (oldPortLoc != -1) { // NOI18N
                String beginURL = driverUrl.substring(0, oldPortLoc);
                String endURL = driverUrl.substring(driverUrl.lastIndexOf(":21527")+6, driverUrl.length()); // NOI18N
                driverUrl = beginURL + ":1527" + endURL; // NOI18N
            }
        }
        return new DataSourceInfo(dsName, driverClassName, driverUrl, "", username, password);
    }
    
    private ArrayList<DataSourceInfo> createDataSourceInfoFromCtx(File contextFile) {
        ArrayList <DataSourceInfo> dsInfo = new ArrayList<DataSourceInfo>();
        
        try {
            userCtxFile = contextFile;
            parseContextFile();
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // extract data source info from each datasource ArrayList

        String[] dataSource = new String[5];
        
        synchronized (dataSources) {
            Iterator itDss = dataSources.iterator();
            while (itDss.hasNext()) {
                dataSource = (String[]) itDss.next();
                dsInfo.add(createDataSourceInfo(dataSource));
            }
        }
        
        return dsInfo;
    }
    
    private void storeArgs(ArrayList args) {
        String[] dsItems = new String[5];
        
        Iterator it = args.iterator();
        int i = 0;
        int cnt = 0;
        while (it.hasNext()) {
            // exclude unneeded items of data source
            if ((cnt == 1) || (cnt == 2) || (cnt == 3) || (cnt == 6))
                it.next();
            else
                dsItems[i++] = (String)it.next();
            
            cnt++;
        }
        
        dataSources.add(dsItems);
    }
    
    private void parseContextFile() throws ParserConfigurationException, SAXException, IllegalArgumentException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();
        final ArrayList storArgs = new ArrayList();
        
        parser.parse(userCtxFile, new DefaultHandler() {
            private String objectName;
            private String className;
            private int tagCount = 0;
            ArrayList<String> args = new ArrayList<String>();
            public void startElement(String uri, String localName, String qName, Attributes attributes)  throws SAXException {
                
                if (qName.equals(OBJ_TAG)) {
                    objectName = attributes.getValue(NAME_ATTR);
                    
                    if (objectName != null) {
                        if (!args.isEmpty()) {
                            storeArgs(args);
                        }
                        
                        args.clear();
                        args.add(objectName);
                    }
                    
                    
                } else  if (qName.equals("arg")) { // NOI18N
                    String valueValue = attributes.getValue(VALUE_ATTR);
                    args.add(valueValue);
                }
            }
            
            public void endDocument() {
                if (!args.isEmpty()) {
                    storeArgs(args);
                }
            }
        });
    }      
    
    public List  <DataSourceInfo> getDataSourcesInfo() {
        return dataSourcesInfo;
    }
    
    private WebApp getWebApp(FileObject f) {
        WebApp webApp = null;
        
        if (f != null) {
            // Make sure, we see any user/editor changes.
            try {
                DataObject deployDescDO = DataObject.find(f);
                SaveCookie saveCookie = (SaveCookie)deployDescDO.getCookie(SaveCookie.class);
                
                if (saveCookie != null) {
                    try {
                        saveCookie.save();
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            } catch (DataObjectNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            try {
                webApp = DDProvider.getDefault().getDDRoot(f);
            } catch (Exception e) {
                // Ok do nothing for now.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        return webApp;
    }
    
    /**
     * updateWebXml updates the resource reference with the datasources used in the project
     * @param project
     * @param ress
     *
     */
    public void updateWebXml(Project project, List <RequestedResource> ress) {
        WebModule wmod = WebModule.getWebModule(project.getProjectDirectory());
        FileObject deployDescFO = wmod.getDeploymentDescriptor();
        WebApp webApp = getWebApp(deployDescFO);
        boolean needWrite = false;
        
        ResourceRef[] rscRefs = webApp.getResourceRef();
        List<RequestedResource> reqList = new LinkedList<RequestedResource>();
        Iterator itRess = ress.iterator();
        while (itRess.hasNext()) {
            reqList.add((RequestedResource)itRess.next());
        }
        
        // Resource Refs
        if (rscRefs == null) {
            rscRefs = new ResourceRef[0];
        } // end of if (refs == null)
        
        for (int i = 0; i < rscRefs.length; i++) {
            boolean found = false;
            Iterator it = reqList.iterator();
            
            while (it.hasNext()) {
                RequestedResource r = (RequestedResource)it.next();
                
                if (rscRefs[i].getResRefName().equals(r.getResourceName())) {
                    found = true;
                    it.remove();
                    
                    break;
                } // end of if (refs[i].getResRefName().
                //    equals(r.getResourceName()))
            } // end of while (it.hasNext())
        } // end of for (int i = 0; i < refs.length; i++)
        
        // Add the refs to the deployment descriptor
        Iterator it = reqList.iterator();
        while (it.hasNext()) {
            RequestedResource r = (RequestedResource)it.next();
            
            if (r instanceof RequestedJdbcResource) {
                RequestedJdbcResource jr = (RequestedJdbcResource)r;
                
                try {
                    webApp.addBean("ResourceRef", // NOI18N
                            new String[] {
                        "ResRefName", // NOI18N
                        "Description", // NOI18N
                        "ResType", // NOI18N
                        "ResAuth" }, // NOI18N
                        new Object[] {
                        jr.getResourceName(),
                        "Visual Web generated DataSource Reference", // NOI18N
                        "javax.sql.DataSource", // NOI18N
                        "Container" }, // NOI18N
                        "ResRefName"); // NOI18N
                } catch (ClassNotFoundException e) {
                    // This should really not happen
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                } // end of try-catch
                catch (NameAlreadyUsedException ne) {
                    // Why did the code above not find it?
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ne);
                } // end of catch
                
                needWrite = true;
            } // end of if (r instanceof RequestedJdbcResource)
        } // end of while (it.hasNext())
        
        if (needWrite) {
            try {
                webApp.write(deployDescFO);
            } catch (IOException e) {
                // Do nothing for now.
                e.printStackTrace();
            }
        }
    }           
}
