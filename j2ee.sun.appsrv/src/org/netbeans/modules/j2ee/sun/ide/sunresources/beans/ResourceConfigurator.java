/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ResourceConfiguration.java
 *
 * Created on August 22, 2005, 12:43 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Field;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;


/**
 *
 * @author Nitya Doraisamy
 */
public class ResourceConfigurator implements ResourceConfiguratorInterface {
    
    public static final String __JMSResource = "jms"; // NOI18N
    public static final String __JMSConnectionFactory = "jms_CF"; // NOI18N
    public static final String __JdbcConnectionPool = "connection-pool"; // NOI18N
    public static final String __JdbcResource = "datasource"; // NOI18N
    
    public static final String QUEUE = "javax.jms.Queue"; // NOI18N
    public static final String QUEUE_CNTN_FACTORY = "javax.jms.QueueConnectionFactory"; // NOI18N
    public static final String TOPIC = "javax.jms.Topic"; // NOI18N
    public static final String TOPIC_CNTN_FACTORY = "javax.jms.TopicConnectionFactory"; // NOI18N
    
    public static final String __SunResourceExt = "sun-resource"; // NOI18N
    
    private final static char BLANK = ' ';
    private final static char DOT   = '.';
    private final static char[]	ILLEGAL_FILENAME_CHARS	= {'/', '\\', ':', '*', '?', '"', '<', '>', '|', ',', '=', ';' };
    // private final static char[]	ILLEGAL_RESOURCE_NAME_CHARS	= {':', '*', '?', '"', '<', '>', '|', ',' };
    private final static char REPLACEMENT_CHAR = '_';
    private final static char DASH = '-';
    
    public static final String __ConnectionPool = "ConnectionPool"; // NOI18N
    private static final String DATAFILE = "org/netbeans/modules/j2ee/sun/sunresources/beans/CPWizard.xml";  // NOI18N

    private boolean showMsg = false;
    private DeploymentManager currentDM = null;
    
    ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.beans.Bundle");// NOI18N
    
    /**
     * Creates a new instance of ResourceConfigurator
     */
    public ResourceConfigurator() {
    }
    
    public void setDeploymentManager(DeploymentManager dm) {
        this.currentDM = dm;
    }
    
    /** Returns whether or not the specific JMS resource alread exists.
     *
     * @param jndiName Jndi name identifying the JMS resource.
     * @param dir Folder where the resources should be stored.  Can be null,
     *   in which case this method will return false.
     *
     * @returns true if the resource exists in the resource, or false for all
     *  other possibilities.
     */
    public boolean isJMSResourceDefined(String jndiName, File dir) {
        return resourceAlreadyDefined(jndiName, dir, __JMSResource);
    }

    /** Creates a new JMS resource with the specified values.
     *
     * @param jndiName jndi-name that identifies this JMS resource.
     * @param msgDstnType type of message destination.
     * @param msgDstnName name of the message destination.
     * @param ejbName name of ejb.
     * @param dir Folder where the resource should be stored.  Should not be null.
     */
    public void createJMSResource(String jndiName, String msgDstnType, String msgDstnName, String ejbName, File dir) {
        try {
            Resources resources = DDProvider.getDefault().getResourcesGraph();
            JmsResource jmsResource = resources.newJmsResource();
            jmsResource.setJndiName(jndiName);
            jmsResource.setResType(msgDstnType);
            jmsResource.setEnabled("true"); // NOI18N
            jmsResource.setDescription(""); // NOI18N
            PropertyElement prop = jmsResource.newPropertyElement();
            prop.setName("Name"); // NOI18N
            prop.setValue(ejbName);
            jmsResource.addPropertyElement(prop);
            resources.addJmsResource(jmsResource);
            createFile(dir, jndiName, __JMSResource, resources);
            
            resources = DDProvider.getDefault().getResourcesGraph();
            JmsResource jmsCntnFactoryResource = resources.newJmsResource();
            String connectionFactoryJndiName= "jms/" + msgDstnName + "Factory"; // NOI18N
            jmsCntnFactoryResource.setJndiName(connectionFactoryJndiName);
            if(msgDstnType.equals(QUEUE)) {
                jmsCntnFactoryResource.setResType(QUEUE_CNTN_FACTORY);
            } else {
                if(msgDstnType.equals(TOPIC)) {
                    jmsCntnFactoryResource.setResType(TOPIC_CNTN_FACTORY);
                } else {
                    assert false; //control should never reach here
                }
            }
            jmsCntnFactoryResource.setEnabled("true"); // NOI18N
            jmsCntnFactoryResource.setDescription(""); // NOI18N
            resources.addJmsResource(jmsCntnFactoryResource);
            createFile(dir, jndiName, __JMSConnectionFactory, resources);
        } catch(IOException ex) {
            // XXX Report I/O Exception to the user.  We should do a nicely formatted
            // message identifying the problem.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }
    
    public void createJDBCDataSourceFromRef(String refName, String databaseInfo, File dir) {
        try {
            String name = refName;
            if(databaseInfo != null) {
                String vendorName = convertToValidName(databaseInfo);
                if(vendorName != null) {
                    name = vendorName;
                }

                // Is connection pool already defined
                String poolName = generatePoolName(name, dir, databaseInfo);
                if(poolName == null) {
                    if(resourceAlreadyDefined(refName, dir, __JdbcResource)) {
                        return;
                    } else {
                        createJDBCResource(name, refName, databaseInfo, dir);
                    }
                } else {
                    name = poolName;
                    createCPPoolResource(name, refName, databaseInfo, dir);
                    createJDBCResource(name, refName, databaseInfo, dir);
                }
            }
        } catch(IOException ex) {
            // XXX Report I/O Exception to the user.  We should do a nicely formatted
            // message identifying the problem.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }
    
    public String createJDBCDataSourceForCmp(String beanName, String databaseInfo, File dir) {
        String name = "jdbc/" + beanName; // NOI18N
        String jndiName = name;
        
        try {
            if(databaseInfo != null) {
                String vendorName = convertToValidName(databaseInfo);
                if(vendorName != null) {
                    name = vendorName;
                }

                // Return if resource already defined
                String poolName = generatePoolName(name, dir, databaseInfo);
                if(poolName == null) {
                    return null;
                } else {
                    name = poolName;
                }

                jndiName = "jdbc/" + name;

                createCPPoolResource(name, jndiName, databaseInfo, dir);
                createJDBCResource(name, jndiName, databaseInfo, dir);

                if(this.showMsg) {
                    String mess = MessageFormat.format(bundle.getString("LBL_UnSupportedDriver"), new Object [] { jndiName }); // NOI18N
                    showInformation(mess);
                    this.showMsg = false;
                }
            }
        } catch(IOException ex) {
            // XXX Report I/O Exception to the user.  We should do a nicely formatted
            // message identifying the problem.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
        
        return jndiName;
    }
    
    // Utility methods needed in case of sun resource creations
    private void createFile(File targetFolder, String beanName, String resourceType, Resources res) throws IOException {
        //jdbc and jdo jndi names might be of format jdbc/ and jdo/
        if(resourceType.indexOf("/") != -1) { // NOI18N
            resourceType = resourceType.substring(0, resourceType.indexOf("/")) + "_" + // NOI18N
                    resourceType.substring(resourceType.indexOf("/")+1, resourceType.length()); // NOI18N
        }
        if(resourceType.indexOf("\\") != -1) { // NOI18N
            resourceType = resourceType.substring(0, resourceType.indexOf("\\")) + "_" +  // NOI18N
                    resourceType.substring(resourceType.indexOf("\\")+1, resourceType.length()); // NOI18N
        }

        ensureFolderExists(targetFolder);
        String filename = getFileName(beanName, resourceType);
        File resourceFile = new File(targetFolder, filename);

        // XXX This will default to UTF-8 encoding (deep inside schema2beans).
        // Is this acceptable or should this be upgraded to allow the encoding to
        // be specified?
        if(!resourceFile.exists()) {
            res.write(new BufferedOutputStream(new FileOutputStream(resourceFile)));
        }
    }
    
    private boolean isLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++) {
            if(filename.indexOf(ILLEGAL_FILENAME_CHARS[i]) >= 0) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isFriendlyFilename(String filename) {
        if(filename.indexOf(BLANK) >= 0 || filename.indexOf(DOT) >= 0) {
            return false;
        }
        
        return isLegalFilename(filename);
    }
    
    private String makeLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++) {
            filename = filename.replace(ILLEGAL_FILENAME_CHARS[i], REPLACEMENT_CHAR);
        }
        
        return filename;
    }
    
    private String makeShorterLegalFilename(String filename) {
        //To clean up the default generation a little
        if(filename.indexOf("://") != -1) { // NOI18N
            filename = filename.substring(0, filename.indexOf("://")) + "_" +  // NOI18N
                    filename.substring(filename.indexOf("://")+3, filename.length()); // NOI18N
        }
        if(filename.indexOf("//") != -1) { // NOI18N
            filename = filename.substring(0, filename.indexOf("//")) + "_" +  // NOI18N
                    filename.substring(filename.indexOf("//")+2, filename.length()); // NOI18N
        }
        filename = makeLegalFilename(filename);
        
        return filename;
    }
    
    private void ensureFolderExists(File folder) throws IOException {
        if(!folder.exists()) {
            folder.mkdir();
        }
    }
    
    private String getFileName(String beanName, String resourceType) {
        
        assert (beanName != null);
        assert (beanName.length() != 0);
        
        assert (resourceType != null);
        assert (resourceType.length() != 0);
        
        String fileName = resourceType;
        
        if(!isFriendlyFilename(beanName)) {
            beanName = makeLegalFilename(beanName);
        }
        
        if(!isFriendlyFilename(fileName)) {
            fileName = makeLegalFilename(fileName);
        }
        
        fileName = fileName + DASH + beanName + DOT + __SunResourceExt;
        return fileName;
    }
    
    private void createCPPoolResource(String name, String jndiName, String databaseUrl, File resourceDir) throws IOException {
        DatabaseConnection databaseConnection = getDatabaseConnection(databaseUrl);
        if((name != null) && (databaseConnection != null)) {
            // Create a JdbcConnectionPool resource
            Resources resources = DDProvider.getDefault().getResourcesGraph();
            JdbcConnectionPool jdbcConnectionPool = resources.newJdbcConnectionPool();
            String connectionPoolName =  name + __ConnectionPool;
            if(!isFriendlyFilename(connectionPoolName)) {
                connectionPoolName = makeLegalFilename(connectionPoolName);
            }
            jdbcConnectionPool.setName(connectionPoolName);
            jdbcConnectionPool.setResType(getResourceType(false));
            
            // XXX Refactor database abstractions into own object.  For example,
            // due to lack of member data, we're parsing CPWizard.xml twice here,
            // once in getDatabaseVendorName() and again in getDatasourceClassName()
            String vendorName = getDatabaseVendorName(databaseUrl);
            String datasourceClassName = ""; // NOI18N
            if(!vendorName.equals("")) { // NOI18N
                datasourceClassName = getDatasourceClassName(vendorName, false);
            }
            
            if(datasourceClassName.equals("")) { // NOI18N
                datasourceClassName = databaseConnection.getDriverClass();
            }
            
            if(datasourceClassName != null) {
                jdbcConnectionPool.setDatasourceClassname(datasourceClassName);
            }
            
            PropertyElement databaseOrUrl = jdbcConnectionPool.newPropertyElement();
            if(vendorName.equals("pointbase")) { // NOI18N
                databaseOrUrl.setName("databaseName"); // NOI18N
            } else {
                databaseOrUrl.setName("URL"); // NOI18N
            }
            
            databaseOrUrl.setValue(databaseConnection.getDatabaseURL());
            jdbcConnectionPool.addPropertyElement(databaseOrUrl);
            
            PropertyElement user = jdbcConnectionPool.newPropertyElement();
            user.setName("User"); // NOI18N
            user.setValue(databaseConnection.getUser());
            jdbcConnectionPool.addPropertyElement(user);
            PropertyElement password = jdbcConnectionPool.newPropertyElement();
            password.setName("Password"); // NOI18N
            password.setValue(databaseConnection.getPassword());
            jdbcConnectionPool.addPropertyElement(password);
            resources.addJdbcConnectionPool(jdbcConnectionPool);
            
            createFile(resourceDir, name, __JdbcConnectionPool, resources);
        }
    }
    
    private void createJDBCResource(String name, String jndiName, String databaseUrl, File resourceDir) throws IOException {
        if(name != null) {
            if(jndiName == null) {
                jndiName = name;
            }
            
            // Create JdbcResource resource
            Resources resources = DDProvider.getDefault().getResourcesGraph();
            JdbcResource jdbcResource = resources.newJdbcResource();
            String connectionPoolName =  name + __ConnectionPool;
            if(!isFriendlyFilename(connectionPoolName)) {
                connectionPoolName = makeLegalFilename(connectionPoolName);
            }
            jdbcResource.setPoolName(connectionPoolName);
            jdbcResource.setJndiName(jndiName);
            resources.addJdbcResource(jdbcResource);
            createFile(resourceDir, jndiName, __JdbcResource, resources);
        }
    }
    
    private DatabaseConnection getDatabaseConnection(String name) {
        if (name != null) {
            return ConnectionManager.getDefault().getConnection(name);
        }
        return null;
    }
    
    
    private String getDatasourceClassName(String vendorName, boolean isXA) {
        Wizard wizard = null;
        if(vendorName == null) {
            return null;
        }
        
        try {
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(DATAFILE);
            wizard = Wizard.createGraph(in);
            
            FieldGroup generalGroup = FieldGroupHelper.getFieldGroup(wizard, WizardConstants.__General);
            
            Field dsField = null;
            if (isXA) {
                dsField = FieldHelper.getField(generalGroup, WizardConstants.__XADatasourceClassname);
            } else {
                dsField = FieldHelper.getField(generalGroup, WizardConstants.__DatasourceClassname);
            }
            in.close();
            return FieldHelper.getConditionalFieldValue(dsField, vendorName);
        } catch(IOException ex) {
            // XXX Report I/O Exception to the user.  We should do a nicely formatted
            // message identifying the problem.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        } catch(Exception ex) {
            // This should really a Schema2BeansException, but for classloader and dependency
            // purposes we're catching Exception instead.
            
            // XXX why do we suppress this?
            // Unable to create Wizard object -- supppress.
        }
        
        return null;
    }
    
    
    private String getDatabaseVendorName(String url) {
        String vendorName = "";
        
        try {
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(DATAFILE);
            Wizard wizard = Wizard.createGraph(in);
            
            FieldGroup propGroup = FieldGroupHelper.getFieldGroup(wizard, WizardConstants.__PropertiesURL);
            Field urlField = FieldHelper.getField(propGroup, "vendorUrls"); // NOI18N
            vendorName = FieldHelper.getOptionNameFromValue(urlField, url);
            in.close();
        } catch(IOException ex) {
            // XXX Report I/O Exception to the user.  We should do a nicely formatted
            // message identifying the problem.
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        } catch(Exception ex) {
            // This should really a Schema2BeansException, but for classloader and dependency
            // purposes we're catching Exception instead.
            
            // XXX why do we suppress this?
            // Unable to create Wizard object -- supppress.
        }
        
        return vendorName;
    }
    
    private String convertToValidName(String database) {
        database = stripExtraDBInfo(database);
        String vendorName = getDatabaseVendorName(database);
        if(vendorName != null) {
            if(!vendorName.equals("")) { // NOI18N
                if(!isFriendlyFilename(vendorName)) {
                    vendorName = makeLegalFilename(vendorName);
                }
                this.showMsg = false;
            } else {
                this.showMsg = true;
                vendorName = makeShorterLegalFilename(database);
            }
        }
        return vendorName;
    }
    
    private String getDatabaseName(String database) {
        String result = null;
        int index = database.lastIndexOf('/') + 1;
        if(index > 0) {
            result = database.substring(index);
        }
        return result;
    }
    
    private String getResourceType(boolean isXA) {
        if(isXA) {
            return "javax.sql.XADataSource";  // NOI18N
        } else {
            return "javax.sql.DataSource";  // NOI18N
        }
    }
    
    private String generatePoolName(String resourceName, File resourceDir, String database) {
        String name = resourceName;
        // Return null if resource already defined
        database = stripExtraDBInfo(database);
        if(resourceAlreadyDefined(resourceName, resourceDir, __JdbcConnectionPool)) {
            boolean sameDBConnection = connectionPoolAlreadyDefined(resourceName, __JdbcConnectionPool, resourceDir, database);
            name = null;
            if(!sameDBConnection) {
                String databaseName =  getDatabaseName(database);
                name = resourceName + '_' + databaseName;
                if(!isFriendlyFilename(name)) {
                    name = makeLegalFilename(name);
                }
            }
        }
        return name;
    }
    
    private boolean resourceAlreadyDefined(String resName, File dir, String resType) {
        boolean result = false;
        
        if(dir != null && dir.exists()) {
            String filename = getFileName(resName, resType);
            File resourceFile = new File(dir, filename);
            if(resourceFile.exists()) {
                result = true;
            }
        }
        
        return result;
    }
    
    private boolean connectionPoolAlreadyDefined(String resName, String resType, File resourceDir, String databaseUrl) {
        boolean returnVal = false;
        String filename = getFileName(resName, resType);
        File resourceFile =  new File(resourceDir, filename);
        if(resourceFile.exists()) {
            returnVal = isSameDatabaseConnection(resourceFile, databaseUrl);
        }
        return returnVal;
    }
    
    private static boolean isSameDatabaseConnection(File resourceFile, String databaseUrl) {
        try {
            FileInputStream in = new FileInputStream(resourceFile);
            Resources resources = DDProvider.getDefault().getResourcesGraph(in);
            
            // identify JDBC Resources xml
            JdbcConnectionPool[] pools = (JdbcConnectionPool[])resources.getJdbcConnectionPool();
            if(pools.length != 0) {
                JdbcConnectionPool connPool = pools[0];
                PropertyElement[] pl = (PropertyElement[])connPool.getPropertyElement();
                for(int i=0; i<pl.length; i++) {
                    String prop = pl[i].getName();
                    if(prop.equals("URL") || prop.equals("databaseName")) { // NOI18N
                        String urlValue = pl[i].getValue();
                        if(urlValue.equals(databaseUrl)) {
                            return true;
                        }
                    }
                }
            }
            in.close();
        } catch(IOException ex) {
            // Could not check local file
        }
        return false;
    }
    
    private String stripExtraDBInfo(String dbConnectionString) {
        if(dbConnectionString.indexOf("[") != -1) {
            dbConnectionString = dbConnectionString.substring(0, dbConnectionString.indexOf("[")).trim(); // NOI18N
        }
        return dbConnectionString;
    }
    
    public static void showInformation(final String msg) {
        // Asynchronous message posting.  Placed on AWT thread automatically by DialogDescriptor.
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }
}
