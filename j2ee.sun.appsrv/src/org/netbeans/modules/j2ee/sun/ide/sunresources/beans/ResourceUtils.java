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
 * ResourceUtils.java
 *
 * Created on September 17, 2003, 11:54 AM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileInputStream;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.AttributeList;

import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.Node.Property;
import org.openide.cookies.SaveCookie;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileAlreadyLockedException;

import javax.enterprise.deploy.spi.DeploymentManager;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.*;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.ide.editors.IsolationLevelEditor;
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;

import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;

/*
 *
 * @author  nityad
 */
public class ResourceUtils implements WizardConstants{
    
    /** Creates a new instance of ResourceUtils */
    public ResourceUtils() {
    }
    
    public static void saveNodeToXml(FileObject resFile, Resources res){
        try {             
            res.write(FileUtil.toFile(resFile));
        }catch(Exception ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); 
        }
    } 
    
    public static AttributeList getResourceAttributes(JdbcConnectionPool connPool){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__Name, connPool.getName()));
        attrs.add(new Attribute(__DatasourceClassname, connPool.getDatasourceClassname()));
        attrs.add(new Attribute(__ResType, connPool.getResType()));
        attrs.add(new Attribute(__SteadyPoolSize, connPool.getSteadyPoolSize()));
        attrs.add(new Attribute(__MaxPoolSize, connPool.getMaxPoolSize()));
        attrs.add(new Attribute(__MaxWaitTimeInMillis, connPool.getMaxWaitTimeInMillis()));
        attrs.add(new Attribute(__PoolResizeQuantity, connPool.getPoolResizeQuantity()));
        attrs.add(new Attribute(__IdleTimeoutInSeconds, connPool.getIdleTimeoutInSeconds()));
        String isolation = connPool.getTransactionIsolationLevel();
        if (isolation != null && (isolation.length() == 0 || isolation.equals(NbBundle.getMessage(IsolationLevelEditor.class, "LBL_driver_default"))) ){  //NOI18N
            isolation = null;
        }
        attrs.add(new Attribute(__TransactionIsolationLevel, isolation));
        attrs.add(new Attribute(__IsIsolationLevelGuaranteed, connPool.getIsIsolationLevelGuaranteed()));
        attrs.add(new Attribute(__IsConnectionValidationRequired, connPool.getIsConnectionValidationRequired()));
        attrs.add(new Attribute(__ConnectionValidationMethod, connPool.getConnectionValidationMethod()));
        attrs.add(new Attribute(__ValidationTableName, connPool.getValidationTableName()));
        attrs.add(new Attribute(__FailAllConnections, connPool.getFailAllConnections()));
        attrs.add(new Attribute(__Description, connPool.getDescription()));
        return attrs;
    }
    
    public static AttributeList getResourceAttributes(JdbcResource jdbcResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, jdbcResource.getJndiName()));
        attrs.add(new Attribute(__PoolName, jdbcResource.getPoolName()));
        attrs.add(new Attribute(__JdbcObjectType, jdbcResource.getObjectType()));
        attrs.add(new Attribute(__Enabled, jdbcResource.getEnabled()));
        attrs.add(new Attribute(__Description, jdbcResource.getDescription()));
        return attrs;
    }
    
    public static AttributeList getResourceAttributes(PersistenceManagerFactoryResource pmResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, pmResource.getJndiName()));
        attrs.add(new Attribute(__FactoryClass, pmResource.getFactoryClass()));
        attrs.add(new Attribute(__JdbcResourceJndiName, pmResource.getJdbcResourceJndiName()));
        attrs.add(new Attribute(__Enabled, pmResource.getEnabled()));
        attrs.add(new Attribute(__Description, pmResource.getDescription()));
        return attrs;
    }
    
    public static AttributeList getResourceAttributes(MailResource mailResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, mailResource.getJndiName()));
        attrs.add(new Attribute(__StoreProtocol, mailResource.getStoreProtocol()));
        attrs.add(new Attribute(__StoreProtocolClass, mailResource.getStoreProtocolClass()));
        attrs.add(new Attribute(__TransportProtocol, mailResource.getTransportProtocol()));
        attrs.add(new Attribute(__TransportProtocolClass, mailResource.getTransportProtocolClass()));
        attrs.add(new Attribute(__Host, mailResource.getHost()));
        attrs.add(new Attribute(__MailUser, mailResource.getUser()));
        attrs.add(new Attribute(__From, mailResource.getFrom()));
        attrs.add(new Attribute(__Debug, mailResource.getDebug()));
        attrs.add(new Attribute(__Enabled, mailResource.getEnabled()));
        attrs.add(new Attribute(__Description, mailResource.getDescription()));
        return attrs;
    }
    
    public static AttributeList getResourceAttributes(JmsResource jmsResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JavaMessageJndiName, jmsResource.getJndiName()));
        attrs.add(new Attribute(__JavaMessageResType, jmsResource.getResType()));
        attrs.add(new Attribute(__Enabled, jmsResource.getEnabled()));
        attrs.add(new Attribute(__Description, jmsResource.getDescription()));
        return attrs;
    }
    
    public static Properties getProperties(PropertyElement[] props){
        Properties propList = new Properties();
        for(int i=0; i<props.length; i++){
            propList.put(props[i].getName(), props[i].getValue());
        }
        return propList;
    }
    
    public List getTargetServers(){
        String instances [] = InstanceProperties.getInstanceList();
        List targets = new ArrayList();
        for (int i=0; i < instances.length; i++) {
            if (instances[i].startsWith("deployer:Sun:AppServer")) {
                targets.add(InstanceProperties.getInstanceProperties(instances[i]).getDeploymentManager());
            }
        }
        //This returns the deploymanager uri. Can we go from this to getting deployment manager??
        //deployer:Sun:AppServer::localhost:4848
        //    String[] targetArray = instanceProperties.getInstanceList();
        //}
        return targets;
    }
    
    public static void saveConnPoolDatatoXml(ResourceConfigData data) {
        try{
            Vector vec = data.getProperties();
            Resources res = getResourceGraph();
            JdbcConnectionPool connPool = res.newJdbcConnectionPool();
            
            String[] keys = data.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)data.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = connPool.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        connPool.addPropertyElement(prop);
                    }
                }else{
                    String value = data.getString(key);
                    if (key.equals(__Name))
                        connPool.setName(value);
                    else if (key.equals(__DatasourceClassname))
                        connPool.setDatasourceClassname(value);
                    else if (key.equals(__ResType))
                        connPool.setResType(value);
                    else if (key.equals(__SteadyPoolSize))
                        connPool.setSteadyPoolSize(value);
                    else if (key.equals(__MaxPoolSize))
                        connPool.setMaxPoolSize(value);
                    else if (key.equals(__MaxWaitTimeInMillis))
                        connPool.setMaxWaitTimeInMillis(value);
                    else if (key.equals(__PoolResizeQuantity))
                        connPool.setPoolResizeQuantity(value);
                    else if (key.equals(__IdleTimeoutInSeconds))
                        connPool.setIdleTimeoutInSeconds(value);
                    else if (key.equals(__TransactionIsolationLevel))
                        connPool.setTransactionIsolationLevel(value);
                    else if (key.equals(__IsIsolationLevelGuaranteed))
                        connPool.setIsIsolationLevelGuaranteed(value);
                    else if (key.equals(__IsConnectionValidationRequired))
                        connPool.setIsConnectionValidationRequired(value);
                    else if (key.equals(__ConnectionValidationMethod))
                        connPool.setConnectionValidationMethod(value);
                    else if (key.equals(__ValidationTableName))
                        connPool.setValidationTableName(value);
                    else if (key.equals(__FailAllConnections))
                        connPool.setFailAllConnections(value);
                    else if (key.equals(__Description)){
                        connPool.setDescription(value); 
                    }    
                }
                
            } //for
            res.addJdbcConnectionPool(connPool);
            createFile(data.getTargetFileObject(), data.getTargetFile(), res);
        }catch(Exception ex){
            System.out.println("Unable to saveConnPoolDatatoXml ");
        }
    }
    
    public static void saveJDBCResourceDatatoXml(ResourceConfigData dsData, ResourceConfigData cpData) {
        try{
            Resources res = getResourceGraph();
            JdbcResource datasource = res.newJdbcResource();
           
            String[] keys = dsData.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)dsData.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = datasource.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        datasource.addPropertyElement(prop);
                    }
                }else{
                    String value = dsData.getString(key);
                    if (key.equals(__JndiName))
                        datasource.setJndiName(value);
                    else if (key.equals(__PoolName))
                        datasource.setPoolName(value);
                    else if (key.equals(__JdbcObjectType))
                        datasource.setObjectType(value);
                    else if (key.equals(__Enabled))
                        datasource.setEnabled(value);
                    else if (key.equals(__Description))
                        datasource.setDescription(value); 
                }
                
            } //for
            res.addJdbcResource(datasource);
            if(cpData != null){
                saveConnPoolDatatoXml(cpData);
            }
            createFile(dsData.getTargetFileObject(), dsData.getTargetFile(), res);
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("Unable to saveJDBCResourceDatatoXml ");
        }
    }
    
    public static void savePMFResourceDatatoXml(ResourceConfigData pmfData, ResourceConfigData dsData, ResourceConfigData cpData) {
        try{
            Resources res = getResourceGraph();
            PersistenceManagerFactoryResource pmfresource = res.newPersistenceManagerFactoryResource();
           
            String[] keys = pmfData.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)pmfData.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = pmfresource.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        pmfresource.addPropertyElement(prop);
                    }
                }else{
                    String value = pmfData.getString(key);
                    if (key.equals(__JndiName))
                        pmfresource.setJndiName(value);
                    else if (key.equals(__FactoryClass))
                        pmfresource.setFactoryClass(value);
                    else if (key.equals(__JdbcResourceJndiName))
                        pmfresource.setJdbcResourceJndiName(value);
                    else if (key.equals(__Enabled))
                        pmfresource.setEnabled(value);
                    else if (key.equals(__Description))
                        pmfresource.setDescription(value); 
                }

            } //for
            res.addPersistenceManagerFactoryResource(pmfresource);
            createFile(pmfData.getTargetFileObject(), pmfData.getTargetFile(), res);
            
            if(dsData != null){
                saveJDBCResourceDatatoXml(dsData, cpData);
            }
        }catch(Exception ex){
            System.out.println("Unable to savePMFResourceDatatoXml ");
        }
    }
    
    public static void saveJMSResourceDatatoXml(ResourceConfigData jmsData) {
        try{
            Resources res = getResourceGraph();
            JmsResource jmsResource = res.newJmsResource();
            
            String[] keys = jmsData.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)) {
                    Vector props = (Vector)jmsData.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = jmsResource.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        jmsResource.addPropertyElement(prop);
                    }
                }else{
                    String value = jmsData.getString(key);
                    if (key.equals(__JndiName))
                        jmsResource.setJndiName(value);
                    else if (key.equals(__ResType))
                        jmsResource.setResType(value);
                    else if (key.equals(__Enabled))
                        jmsResource.setEnabled(value);
                    else if (key.equals(__Description))
                        jmsResource.setDescription(value); 
                }
            }
	    res.addJmsResource(jmsResource);
	    createFile(jmsData.getTargetFileObject(), jmsData.getTargetFile(), res);
        }catch(Exception ex){
            System.out.println("Unable to saveJMSResourceDatatoXml ");
        }
    }
    
    public static void saveMailResourceDatatoXml(ResourceConfigData data) {
        try{
            Vector vec = data.getProperties();
            Resources res = getResourceGraph();
            MailResource mlresource = res.newMailResource();
                        
            String[] keys = data.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)) {
                    Vector props = (Vector)data.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        PropertyElement prop = mlresource.newPropertyElement();
                        prop = populatePropertyElement(prop, pair);
                        mlresource.addPropertyElement(prop);
                    }
                }else{
                    String value = data.getString(key);
                    if (key.equals(__JndiName))
                        mlresource.setJndiName(value);
                    else if (key.equals(__StoreProtocol))
                        mlresource.setStoreProtocol(value);
                    else if (key.equals(__StoreProtocolClass))
                        mlresource.setStoreProtocolClass(value);
                    else if (key.equals(__TransportProtocol))
                        mlresource.setTransportProtocol(value);
                    else if (key.equals(__TransportProtocolClass))
                        mlresource.setTransportProtocolClass(value);
                    else if (key.equals(__Host))
                        mlresource.setHost(value);
                    else if (key.equals(__MailUser))
                        mlresource.setUser(value);
                    else if (key.equals(__From))
                        mlresource.setFrom(value);
                    else if (key.equals(__Debug))
                        mlresource.setDebug(value);
                    else if (key.equals(__Description))
                        mlresource.setDescription(value); 
                }    
            } //for
            
            res.addMailResource(mlresource);
            createFile(data.getTargetFileObject(), data.getTargetFile(), res);
        }catch(Exception ex){
            System.out.println("Unable to saveMailResourceDatatoXml ");
        }
    }
    
    public static void createFile(FileObject targetFolder, String filename, final Resources res){
        try{
            //jdbc and jdo jndi names might be of format jdbc/ and jdo/
            if(filename.indexOf("/") != -1){ //NOI18N
                filename = filename.substring(0, filename.indexOf("/")) + "_" + filename.substring(filename.indexOf("/")+1, filename.length()); //NOI18N
            }
            if(filename.indexOf("\\") != -1){ //NOI18N
                filename = filename.substring(0, filename.indexOf("\\")) + "_" + filename.substring(filename.indexOf("\\")+1, filename.length()); //NOI18N
            }
            String oldName = filename;
            targetFolder = setUpExists(targetFolder);
            filename =  createUniqueFileName(filename, targetFolder, null);        
	    if(!filename.equals(oldName)){
                String msg = java.text.MessageFormat.format(NbBundle.getMessage(ResourceUtils.class, "LBL_UniqueResourceName"), new Object[]{oldName, filename}); //NOI18N
                org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
            }
            
            final String resFileName = filename;
            final FileObject resTargetFolder  = targetFolder;
            
            FileSystem fs = targetFolder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws java.io.IOException {
                    FileObject newfile = resTargetFolder.createData(resFileName, "sun-resource"); //NOI18N
                    
                    FileLock lock = newfile.lock();
                    try {
                        PrintWriter to = new PrintWriter(newfile.getOutputStream(lock));
                        try {
                            res.write(to);
                            to.flush();
                        } catch(Exception ex){
                            //Unable to create file
                        } finally {
                            to.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            });
        }catch(Exception ex){
            //Unable to create file
            System.out.println("Error while creating file");
        }
    }
    
    public static String createUniqueFileName(String in_targetName, FileObject fo, String defName){
        String targetName = in_targetName;
        
        if (targetName == null || targetName.length() == 0) {
            targetName = FileUtil.findFreeFileName(fo, defName, __SunResourceExt);
        }else{
            //Fix for bug# 5025573 - Check for invalid file names
            if(! isFriendlyFilename(targetName)){
                if(defName != null)
                    targetName = defName;
                else
                    targetName = makeLegalFilename(targetName);
            }
            targetName = FileUtil.findFreeFileName(fo, targetName, __SunResourceExt);
        }
        return targetName;
    }
    
    public static List getRegisteredConnectionPools(ResourceConfigData data, String resourceType){
        List connPools = new ArrayList();
        try {
            String OPER_OBJ_ConnPoolResource = "getJdbcConnectionPool"; //NOI18N
            String keyProp = "name"; //NOI18N
            InstanceProperties instanceProperties = InstanceProperties.getDefaultInstance();
            connPools = getResourceNames(instanceProperties, OPER_OBJ_ConnPoolResource, keyProp);
            List projectCP = getProjectResources(data, resourceType);
            for(int i=0; i<projectCP.size(); i++){
                String localCP = projectCP.get(i).toString();
                if(! connPools.contains(localCP))
                    connPools.add(localCP);
            }
        } catch (java.lang.NoClassDefFoundError ncdfe) {
            // this happens durring  unit tests for the DataSourceWizard
        }
        return connPools;
    }
    
    public static List getRegisteredJdbcResources(ResourceConfigData data, String resourceType){
        List dataSources = new ArrayList();
        try {
            String OPER_OBJ_JDBCResource = "getJdbcResource"; //NOI18N
            String keyProp = "jndi-name"; //NOI18N
            InstanceProperties instanceProperties = InstanceProperties.getDefaultInstance();
            dataSources = getResourceNames(instanceProperties, OPER_OBJ_JDBCResource, keyProp);
            List projectDS = getProjectResources(data, resourceType);
            for(int i=0; i<projectDS.size(); i++){
                String localDS = projectDS.get(i).toString();
                if(! dataSources.contains(localDS))
                    dataSources.add(localDS);
            }
        } catch (java.lang.NoClassDefFoundError ncdfe) {
            // this happens durring  unit tests for the PMFWizard
        }
        return dataSources;
    }
    
    private static List getResourceNames(InstanceProperties instProps, String query, String keyProperty){
        List resList = new ArrayList();
        String MAP_RESOURCES = "ias:type=resources,category=config";//NOI18N
        try{
            SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)instProps.getDeploymentManager();
            ServerInterface mejb = (ServerInterface)eightDM.getManagement();
            ObjectName objName = new ObjectName(MAP_RESOURCES);
            ObjectName[] beans = (ObjectName[])mejb.invoke(objName, query, null, null);
            for(int i=0; i<beans.length; i++){
                String resName = ((ObjectName)beans[i]).getKeyProperty(keyProperty);
                resList.add(resName);
            }
        }catch(Exception ex){
            //Suppress exception when unable to get resource names
            //Possibe errors: deafult server is not Sun Application Server (classcast exception)
            //Application server is not running.
        }
        return resList;
    }
    
    private static List getProjectResources(ResourceConfigData data, String resourceType){
        List projectResources = new ArrayList();
        FileObject targetFolder = data.getTargetFileObject();
        if(targetFolder != null){
            FileObject setUpFolder = setUpExists(targetFolder);
            java.util.Enumeration en = setUpFolder.getData(false);
            while(en.hasMoreElements()){
                FileObject resourceFile = (FileObject)en.nextElement();
                File resource = FileUtil.toFile(resourceFile);
                if(resourceType.equals(__ConnectionPoolResource))
                    projectResources = filterConnectionPools(resource, projectResources);
                else
                    projectResources = filterDataSources(resource, projectResources);
            }
        }
        return projectResources;
    }
    
    private static List filterConnectionPools(File primaryFile, List projectCP){
        try{
            if(! primaryFile.isDirectory()){
                FileInputStream in = new FileInputStream(primaryFile);
                Resources resources = DDProvider.getDefault().getResourcesGraph(in);
                
                // identify JDBC Connection Pool xml
                JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
                for(int i=0; i<pools.length; i++){
                    projectCP.add(pools[i].getName());
                }
            }
        }catch(Exception exception){
            //Could not get list of local Connection pools
        }
        return projectCP;
    }
    
    private static List filterDataSources(File primaryFile, List projectDS){
        try{
            if(! primaryFile.isDirectory()){
                FileInputStream in = new FileInputStream(primaryFile);
                Resources resources = DDProvider.getDefault().getResourcesGraph(in);
                
                // identify JDBC Resources xml
                JdbcResource[] dataSources = resources.getJdbcResource();
                for(int i=0; i<dataSources.length; i++){
                    projectDS.add(dataSources[i].getJndiName());
                }
            }
        }catch(Exception exception){
            //Could not get list of local Connection pools
        }
        return projectDS;
    }
    
    public static FileObject setUpExists(FileObject targetFolder){
        java.util.Enumeration enume = targetFolder.getFolders(false);
        boolean setupExists = false;
        while(enume.hasMoreElements()){
            FileObject fold = (FileObject)enume.nextElement();
            if(fold.getName().equals(__SunResourceFolder)){
                setupExists = true;
                targetFolder = targetFolder.getFileObject(__SunResourceFolder, null);
                break;
            }    
        }
        try{
            if(!setupExists){
                targetFolder = targetFolder.createFolder(__SunResourceFolder);
            }
        }catch(Exception ex){
            //Unable to create setup folder
            //resource will be created under existing structure 
        }    
        return targetFolder;
    }
    
    private static Resources getResourceGraph(){
        return DDProvider.getDefault().getResourcesGraph();
    }
    
    private static PropertyElement populatePropertyElement(PropertyElement prop, NameValuePair pair){
        prop.setName(pair.getParamName()); 
        prop.setValue(pair.getParamValue()); 
        return prop;
    }
    
    //Obtained from com.iplanet.ias.util.io.FileUtils - Byron's
    public static boolean isLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++)
            if(filename.indexOf(ILLEGAL_FILENAME_CHARS[i]) >= 0)
                return false;
        
        return true;
    }
    
    public static boolean isFriendlyFilename(String filename) {
        if(filename.indexOf(BLANK) >= 0 || filename.indexOf(DOT) >= 0)
            return false;
        
        return isLegalFilename(filename);
    }
    
    public static String makeLegalFilename(String filename) {
        for(int i = 0; i < ILLEGAL_FILENAME_CHARS.length; i++)
            filename = filename.replace(ILLEGAL_FILENAME_CHARS[i], REPLACEMENT_CHAR);
        
        return filename;
    }
    
    public static boolean isLegalResourceName(String filename) {
        for(int i = 0; i < ILLEGAL_RESOURCE_NAME_CHARS.length; i++)
            if(filename.indexOf(ILLEGAL_RESOURCE_NAME_CHARS[i]) >= 0)
                return false;
        
        return true;
    }
    
    private final static char BLANK = ' ';
    private final static char DOT   = '.';
    private final static char REPLACEMENT_CHAR = '_';
    private final static char[]	ILLEGAL_FILENAME_CHARS	= {'/', '\\', ':', '*', '?', '"', '<', '>', '|', ',' };
    private final static char[]	ILLEGAL_RESOURCE_NAME_CHARS	= {':', '*', '?', '"', '<', '>', '|', ',' };
}
