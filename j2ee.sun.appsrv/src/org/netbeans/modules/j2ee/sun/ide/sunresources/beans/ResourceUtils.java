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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ResourceUtils.java
 *
 * Created on September 17, 2003, 11:54 AM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.io.File;
import java.io.FileFilter;
import java.io.Writer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;

import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Properties;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.AttributeList;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;

import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;

import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;
import org.netbeans.modules.j2ee.sun.share.serverresources.SunDatasource;
import org.netbeans.modules.j2ee.sun.share.serverresources.SunMessageDestination;
import org.netbeans.modules.j2ee.sun.sunresources.beans.DatabaseUtils;

/*
 *
 * @author  nityad
 */

public class ResourceUtils implements WizardConstants{
    
    static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.beans.Bundle");// NOI18N
    static final String[] sysDatasources = {"jdbc/__TimerPool", "jdbc/__CallFlowPool", "jdbc/__default"}; //NOI18N
    static final String[] sysConnpools = {"__CallFlowPool", "__TimerPool", "DerbyPool"}; //NOI18N
    static final String SAMPLE_DATASOURCE = "jdbc/sample";
    static final String SAMPLE_CONNPOOL = "SamplePool";
    static final String SUN_RESOURCE_FILENAME = "sun-resources.xml"; //NOI18N
            
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
    
    public static void register(Resources resource, SunDeploymentManagerInterface sunDm, boolean update, String resType) throws Exception {
        if(sunDm.isRunning()){
            ServerInterface mejb = sunDm.getManagement();
            if(resType.equals(__JdbcConnectionPool)){
                register(resource.getJdbcConnectionPool(0), mejb, update);
            }else if(resType.equals(__JdbcResource)){
                register(resource.getJdbcResource(0), mejb, update);
            }else if(resType.equals(__PersistenceManagerFactoryResource)){
                register(resource.getPersistenceManagerFactoryResource(0), mejb, update);
            }else if(resType.equals(__MailResource)){
                register(resource.getMailResource(0), mejb, update);
            }else if(resType.equals(__JmsResource)){
                if(resource.getAdminObjectResource().length != 0){
                    register(resource.getAdminObjectResource(0), mejb, update);
                }else{
                    if(resource.getConnectorResource().length != 0 && resource.getConnectorConnectionPool().length != 0) {
                        register(resource.getConnectorConnectionPool(0), mejb, update);
                        register(resource.getConnectorResource(0), mejb, update);
                    }
                }
            }
        }else{
            throw new Exception(bundle.getString("Err_RegResServerStopped")); //NOI18N
        }
    }
    
    public static void register(JdbcConnectionPool resource, ServerInterface mejb, boolean update) throws Exception{
        AttributeList attrList = ResourceUtils.getResourceAttributes(resource, mejb);
        PropertyElement[] props = resource.getPropertyElement();
        Properties propsList = getProperties(props);
        Object[] params = new Object[]{attrList, propsList, null};
        String resourceName = resource.getName();
        if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetJdbcConnectionPool)){
            createResource(__CreateCP, params, mejb);
        }
    }
    
    public static void register(JdbcResource resource, ServerInterface mejb, boolean update) throws Exception{
        AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
        PropertyElement[] props = resource.getPropertyElement();
        Properties propsList = getProperties(props);
        Object[] params = new Object[]{attrList, propsList, null};
        String resourceName = resource.getJndiName();
        if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetJdbcResource)){
            createResource(__CreateDS, params, mejb);
        }
    }
       
     public static void register(PersistenceManagerFactoryResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetPMFResource)){
             createResource(__CreatePMF, params, mejb);
         }
     }
     
     public static void register(AdminObjectResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetAdmObjResource)){
             createResource(__CreateAdmObj, params, mejb);
         }
     }
    
     public static void register(ConnectorResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         Properties propsList = new Properties();
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetConnectorResource)){
             createResource(__CreateConnector, params, mejb);
         }
     }
     
     public static void register(ConnectorConnectionPool resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetConnPoolResource)){
             createResource(__CreateConnPool, params, mejb);
         }
     }
     
     public static void register(MailResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, __GetMailResource)){
             createResource(__CreateMail, params, mejb);
         }
     }
     
     public static void register(JmsResource resource, ServerInterface mejb, boolean update) throws Exception{
         AttributeList attrList = ResourceUtils.getResourceAttributes(resource);
         PropertyElement[] props = resource.getPropertyElement();
         Properties propsList = getProperties(props);
         Object[] params = new Object[]{attrList, propsList, null};
         String operName = NbBundle.getMessage(ResourceUtils.class, "CreateJMS"); //NOI18N
         String resourceName = resource.getJndiName();
         if(!isResourceUpdated(resourceName, mejb, attrList, propsList, WizardConstants.__GetJmsResource)){
             createResource(operName, params, mejb);
         }
     }
    
     private static boolean isResourceUpdated(String resourceName, ServerInterface mejb, AttributeList attrList, Properties props, String operName ){  
        boolean isResUpdated = false;
        try{
            ObjectName objName = new ObjectName(MAP_RESOURCES);
            ObjectName[] resourceObjects = null;
            if(operName.equals(__GetPMFResource) || operName.equals(__GetJmsResource)){
                String[] signature = new String[]{"java.lang.String"};  //NOI18N
                Object[] params = new Object[]{null};
                resourceObjects = (ObjectName[])  mejb.invoke(objName, operName, params, signature);
            }else{
                resourceObjects = (ObjectName[])  mejb.invoke(objName, operName, null, null);
            }
            if(resourceObjects != null){
                ObjectName resOnServer = null;
                if(operName.equals(__GetJdbcConnectionPool) || operName.equals(__GetConnPoolResource))
                    resOnServer = getResourceDeployed(resourceObjects, resourceName, false);
                else
                    resOnServer = getResourceDeployed(resourceObjects, resourceName, true);
                if(resOnServer != null){
                    isResUpdated = true;
                    updateResourceAttributes(resOnServer, attrList, mejb);
                    updateResourceProperties(resOnServer, props, mejb);
                }
            }//Returned value is null for JMS.
        }catch(Exception ex){
            String errorMsg = MessageFormat.format(bundle.getString("Err_ResourceUpdate"), new Object[]{resourceName}); //NOI18N
            System.out.println(errorMsg);
        }
        return isResUpdated;
    }    
    
    private static ObjectName getResourceDeployed(ObjectName[] resourceObjects, String resourceName, boolean useJndi){
        for(int i=0; i<resourceObjects.length; i++){
            ObjectName resObj = resourceObjects[i];
            String jndiName = null;
            if(useJndi)
                jndiName = resObj.getKeyProperty(__JndiName);
            else
                jndiName = resObj.getKeyProperty(__Name);
            
            if(jndiName.equals(resourceName)){
                return resObj;
            }
        }
        return null;
    }
    
    public static void updateResourceAttributes(ObjectName objName, AttributeList attrList, ServerInterface mejb) throws Exception {
         try{
             Map attributeInfos = getResourceAttributeNames(objName, mejb);
             String[] attrNames = (String[]) attributeInfos.keySet().toArray(new String[attributeInfos.size()]);
             
             //Attributes from server
             AttributeList existAttrList = mejb.getAttributes(objName, attrNames);
             for(int i=0; i<existAttrList.size(); i++){
                Attribute existAttr = (Attribute)existAttrList.get(i);
                String existAttrName = existAttr.getName();
                for(int j=0; j<attrList.size(); j++){
                    Attribute resAttr = (Attribute)attrList.get(j);
                    String resAttrName = resAttr.getName();
                    if(existAttrName.equals(resAttrName)){
                        if(resAttr.getValue() == null && existAttr.getValue() != null) { 
                            mejb.setAttribute(objName, resAttr);
                        }else if(existAttr.getValue() == null) { //NOI18N
                            if((resAttr.getValue() != null) && (! resAttr.getValue().toString().equals("")))
                                mejb.setAttribute(objName, resAttr);
                        }else{    
                            if(! resAttr.getValue().toString().equals(existAttr.getValue().toString())){
                                mejb.setAttribute(objName, resAttr);
                            }
                        }
                    }//if
                }//loop through project's resource Attributes
             }
         }catch(Exception ex){
             throw new Exception(ex.getLocalizedMessage());
         }
     }
    public static void updateResourceProperties(ObjectName objName, Properties props, ServerInterface mejb) throws Exception {
         try{
             String[] signature = new String[]{"javax.management.Attribute"};  //NOI18N
             Object[] params = null;
             //Get Extra Properties From Server
             AttributeList attrList = (AttributeList)mejb.invoke(objName, WizardConstants.__GetProperties, null, null);             
             for(int i=0; i<attrList.size(); i++){
                 Attribute oldAttr = (Attribute)attrList.get(i);
                 String oldAttrName = oldAttr.getName();
                 if(props.containsKey(oldAttrName)){
                     if(oldAttr.getValue() != null){
                         String oldAttrValue = oldAttr.getValue().toString();
                         if(! props.getProperty(oldAttrName).equals(oldAttrValue)){
                             Attribute attr = new Attribute(oldAttrName, props.getProperty(oldAttrName));
                             params = new Object[]{attr};
                             mejb.invoke(objName, WizardConstants.__SetProperty, params, signature);
                         }
                     }else{//Server extra property value not null
                         if(props.getProperty(oldAttrName) != null){
                             Attribute attr = new Attribute(oldAttrName, props.getProperty(oldAttrName));
                             params = new Object[]{attr};
                             mejb.invoke(objName, WizardConstants.__SetProperty, params, signature);
                         }
                     }
                 }else{
                     //Modifies extra properties does not contain this property
                     //Remove from server resource
                     Attribute removeAttr = new Attribute(oldAttrName, null);
                     params = new Object[]{removeAttr};
                     mejb.invoke(objName, WizardConstants.__SetProperty, params, signature);
                 }
             }//loop through server extra properties
             addNewExtraProperties(objName, props, attrList, mejb);
         }catch(Exception ex){
             throw new Exception(ex.getLocalizedMessage());
         }
     }
     
     public static Map getResourceAttributeNames(ObjectName objName, ServerInterface mejb) throws Exception {
         try{
             Map attributeInfos = new java.util.HashMap();
             javax.management.MBeanInfo info = mejb.getMBeanInfo(objName);
             javax.management.MBeanAttributeInfo[] attrs = info.getAttributes();
             for (int i=0; i<attrs.length; i++) {
                 if(attrs[i] != null){
                     attributeInfos.put(attrs[i].getName(), attrs[i]);
                 }
             }
             return attributeInfos;
         }catch(Exception ex){
             throw new Exception(ex.getLocalizedMessage());
         }
     }
     
     private static void addNewExtraProperties(ObjectName objName, Properties props, AttributeList attrList, ServerInterface mejb) throws Exception {
         try{
             String[] signature = new String[]{"javax.management.Attribute"};  //NOI18N
             Object[] params = null;
             if(props.size() > attrList.size()){
                 java.util.Enumeration listProps = props.propertyNames();
                 while(listProps.hasMoreElements()){
                     String propName = listProps.nextElement().toString();
                     if(! attrList.contains(propName)){
                         Attribute attr = new Attribute(propName, props.getProperty(propName));
                         params = new Object[]{attr};
                         mejb.invoke(objName, WizardConstants.__SetProperty, params, signature);
                     }
                 }//while
             }
         }catch(Exception ex){
             throw new Exception(ex.getLocalizedMessage());
         }
     }
     
    
    
    static final String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config";//NOI18N
    public static void createResource(String operName, Object[] params, ServerInterface mejb) throws Exception{
        try{
            ObjectName objName = new ObjectName(MAP_RESOURCES);
            String[] signature = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};  //NOI18N
            mejb.invoke(objName, operName, params, signature);
        }catch(Exception ex){
            throw new Exception(ex.getLocalizedMessage());
        }
    }
    
    public static AttributeList getResourceAttributes(JdbcConnectionPool connPool, ServerInterface mejb){
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
        if (isolation != null && (isolation.length() == 0 || isolation.equals(WizardConstants.__IsolationLevelDefault)) ){  
            isolation = null;
        }
        attrs.add(new Attribute(__TransactionIsolationLevel, isolation));
        attrs.add(new Attribute(__IsIsolationLevelGuaranteed, connPool.getIsIsolationLevelGuaranteed()));
        attrs.add(new Attribute(__IsConnectionValidationRequired, connPool.getIsConnectionValidationRequired()));
        attrs.add(new Attribute(__ConnectionValidationMethod, connPool.getConnectionValidationMethod()));
        attrs.add(new Attribute(__ValidationTableName, connPool.getValidationTableName()));
        attrs.add(new Attribute(__FailAllConnections, connPool.getFailAllConnections()));
        attrs.add(new Attribute(__Description, connPool.getDescription()));
        
        if(is90Server(mejb)){
            attrs.add(new Attribute(__NonTransactionalConnections, connPool.getNonTransactionalConnections()));
            attrs.add(new Attribute(__AllowNonComponentCallers, connPool.getAllowNonComponentCallers()));
        }
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
    
    public static AttributeList getResourceAttributes(AdminObjectResource aoResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, aoResource.getJndiName()));
        attrs.add(new Attribute(__Description, aoResource.getDescription()));
        attrs.add(new Attribute(__Enabled, aoResource.getEnabled()));
        attrs.add(new Attribute(__JavaMessageResType, aoResource.getResType()));
        attrs.add(new Attribute(__AdminObjResAdapterName, aoResource.getResAdapter()));
        return attrs;
    }
    
    public static AttributeList getResourceAttributes(ConnectorResource connResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__JndiName, connResource.getJndiName()));
        attrs.add(new Attribute(__PoolName, connResource.getPoolName()));
        attrs.add(new Attribute(__Description, connResource.getDescription()));
        attrs.add(new Attribute(__Enabled, connResource.getEnabled()));
        return attrs;
    }
    
    public static AttributeList getResourceAttributes(ConnectorConnectionPool connPoolResource){
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute(__Name, connPoolResource.getName()));
        attrs.add(new Attribute(__ConnectorPoolResAdName, connPoolResource.getResourceAdapterName()));
        attrs.add(new Attribute(__ConnectorPoolConnDefName, connPoolResource.getConnectionDefinitionName()));
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
    
    private static Properties getProperties(PropertyElement[] props) throws Exception {
        Properties propList = new Properties();
        for(int i=0; i<props.length; i++){
            String name = props[i].getName();
            String value = props[i].getValue();
            if(value != null && value.trim().length() != 0){
                propList.put(name, value);
            }
        }
        return propList;
    }
    
    public List getTargetServers(){
        String instances [] = InstanceProperties.getInstanceList();
        List targets = new ArrayList();
        for (int i=0; i < instances.length; i++) {
            if (instances[i].startsWith(SunURIManager.SUNSERVERSURI)) {
                targets.add(InstanceProperties.getInstanceProperties(instances[i]).getDeploymentManager());
            }
            else  if (instances[i].startsWith("[")) {
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
        Resources res = getServerResourcesGraph(data.getTargetFileObject());
        saveConnPoolDatatoXml(data, res);
    }
    
    public static void saveConnPoolDatatoXml(ResourceConfigData data, Resources res) {
        try{
            Vector vec = data.getProperties();
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
                    if (key.equals(__Name)){
                        connPool.setName(value);
                        data.setTargetFile(value);
                    }else if (key.equals(__DatasourceClassname))
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
                    else if (key.equals(__TransactionIsolationLevel)){
                        if (value.equals(WizardConstants.__IsolationLevelDefault)){  
                            value = null;
                        }
                        connPool.setTransactionIsolationLevel(value);
                    }else if (key.equals(__IsIsolationLevelGuaranteed))
                        connPool.setIsIsolationLevelGuaranteed(value);
                    else if (key.equals(__IsConnectionValidationRequired))
                        connPool.setIsConnectionValidationRequired(value);
                    else if (key.equals(__ConnectionValidationMethod))
                        connPool.setConnectionValidationMethod(value);
                    else if (key.equals(__ValidationTableName))
                        connPool.setValidationTableName(value);
                    else if (key.equals(__FailAllConnections))
                        connPool.setFailAllConnections(value);
                    else if (key.equals(__Description))
                        connPool.setDescription(value); 
                    else if (key.equals(__NonTransactionalConnections))
                        connPool.setNonTransactionalConnections(value);
                    else if (key.equals(__AllowNonComponentCallers))
                        connPool.setAllowNonComponentCallers(value);    
                }
                
            } //for
            res.addJdbcConnectionPool(connPool);
            createFile(data.getTargetFileObject(), res);
        }catch(Exception ex){
            System.out.println("Unable to saveConnPoolDatatoXml ");
        }
    }
    
    public static void saveJDBCResourceDatatoXml(ResourceConfigData dsData, ResourceConfigData cpData) {
        try{
            Resources res = getServerResourcesGraph(dsData.getTargetFileObject());
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
                    if (key.equals(__JndiName)){
                        datasource.setJndiName(value);
                        dsData.setTargetFile(value);
                    }else if (key.equals(__PoolName))
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
                saveConnPoolDatatoXml(cpData, res);
            }
            createFile(dsData.getTargetFileObject(), res);
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("Unable to saveJDBCResourceDatatoXml ");
        }
    }
    
    public static void savePMFResourceDatatoXml(ResourceConfigData pmfData, ResourceConfigData dsData, ResourceConfigData cpData) {
        try{
            Resources res = getServerResourcesGraph(pmfData.getTargetFileObject());
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
                    if (key.equals(__JndiName)){
                        pmfresource.setJndiName(value);
                        pmfData.setTargetFile(value);
                    }else if (key.equals(__FactoryClass))
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
            createFile(pmfData.getTargetFileObject(), res);
            
            if(dsData != null){
                saveJDBCResourceDatatoXml(dsData, cpData);
            }
        }catch(Exception ex){
            System.out.println("Unable to savePMFResourceDatatoXml ");
        }
    }
    
    public static void saveJMSResourceDatatoXml(ResourceConfigData jmsData) {
        try{
            Resources res = getServerResourcesGraph(jmsData.getTargetFileObject());
            String type = jmsData.getString(__ResType);
            if(type.equals(__QUEUE) || type.equals(__TOPIC)){
                AdminObjectResource aoresource = res.newAdminObjectResource();
                aoresource.setDescription(jmsData.getString(__Description));
                aoresource.setEnabled(jmsData.getString(__Enabled));
                aoresource.setJndiName(jmsData.getString(__JndiName));
                aoresource.setResType(jmsData.getString(__ResType));
                aoresource.setResAdapter(__JmsResAdapter);
                Vector props = (Vector)jmsData.getProperties();
                for (int j = 0; j < props.size(); j++) {
                    NameValuePair pair = (NameValuePair)props.elementAt(j);
                    PropertyElement prop = aoresource.newPropertyElement();
                    prop = populatePropertyElement(prop, pair);
                    aoresource.addPropertyElement(prop);
                }
                
                res.addAdminObjectResource(aoresource);
            }else{
                ConnectorResource connresource = res.newConnectorResource();
                connresource.setDescription(jmsData.getString(__Description));
                connresource.setEnabled(jmsData.getString(__Enabled));
                connresource.setJndiName(jmsData.getString(__JndiName));
                connresource.setPoolName(jmsData.getString(__JndiName));
                
                ConnectorConnectionPool connpoolresource = res.newConnectorConnectionPool();
                connpoolresource.setName(jmsData.getString(__JndiName));
                connpoolresource.setConnectionDefinitionName(jmsData.getString(__ResType));
                connpoolresource.setResourceAdapterName(__JmsResAdapter);
                
                Vector props = (Vector)jmsData.getProperties();
                for (int j = 0; j < props.size(); j++) {
                    NameValuePair pair = (NameValuePair)props.elementAt(j);
                    PropertyElement prop = connpoolresource.newPropertyElement();
                    prop = populatePropertyElement(prop, pair);
                    connpoolresource.addPropertyElement(prop);
                }
                
                res.addConnectorResource(connresource);
                res.addConnectorConnectionPool(connpoolresource);
            }
            
            createFile(jmsData.getTargetFileObject(), res);
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("Unable to saveJMSResourceDatatoXml ");
        }
    }
    
    public static void saveMailResourceDatatoXml(ResourceConfigData data) {
        try{
            Vector vec = data.getProperties();
            Resources res = getServerResourcesGraph(data.getTargetFileObject());
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
                    if (key.equals(__JndiName)){
                        mlresource.setJndiName(value);
                        data.setTargetFile(value);
                    }else if (key.equals(__StoreProtocol))
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
            createFile(data.getTargetFileObject(), res);
        }catch(Exception ex){
            System.out.println("Unable to saveMailResourceDatatoXml ");
        }
    }
    
    public static String createUniqueFileName(String in_targetName, FileObject fo, String defName){
        String targetName = in_targetName;
        if (targetName == null || targetName.length() == 0) 
            targetName = defName;
        
        targetName = makeLegalFilename(targetName);
        targetName = FileUtil.findFreeFileName(fo, targetName, __SunResourceExt);
        targetName = revertToResName(targetName);
        return targetName;
    }
    
    public static List getRegisteredConnectionPools(ResourceConfigData data){
        List connPools = new ArrayList();
        try {
            String OPER_OBJ_ConnPoolResource = "getJdbcConnectionPool"; //NOI18N
            String keyProp = "name"; //NOI18N
            InstanceProperties instanceProperties = getTargetServer(data.getTargetFileObject());
            if(instanceProperties != null)
                connPools = getResourceNames(instanceProperties, OPER_OBJ_ConnPoolResource, keyProp);
            connPools.removeAll(Arrays.asList(sysConnpools));  
            List projectCP = getProjectResources(data, __ConnectionPoolResource);
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
    
    public static List getRegisteredJdbcResources(ResourceConfigData data){
        List dataSources = new ArrayList();
        try {
            String keyProp = "jndi-name"; //NOI18N
            InstanceProperties instanceProperties = getTargetServer(data.getTargetFileObject());
            if(instanceProperties != null)
                dataSources = getResourceNames(instanceProperties, WizardConstants.__GetJdbcResource, keyProp);
            dataSources.removeAll(Arrays.asList(sysDatasources));    
            List projectDS = getProjectResources(data, __JDBCResource);
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
        Object tmp = instProps.getDeploymentManager();
        List retVal;
        if (tmp instanceof SunDeploymentManagerInterface)  {
            SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)tmp;
            retVal = getResourceNames(eightDM, query, keyProperty);
        } else {
            retVal = Collections.EMPTY_LIST;
        }
        return retVal;
    }
    
    private static List getResourceNames(SunDeploymentManagerInterface eightDM, String query, String keyProperty){
        List resList = new ArrayList();
        String MAP_RESOURCES = "com.sun.appserv:type=resources,category=config";//NOI18N
        try{
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
        FileObject pkgLocation = getResourceDirectory(targetFolder);
        if(pkgLocation == null){
            //resource will be created under existing structure
            return targetFolder;
        }else{
            return pkgLocation;
        }
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
    
    public static FileObject getResourceDirectory(FileObject fo){
        Project holdingProj = FileOwnerQuery.getOwner(fo);
        FileObject resourceDir = null;
        if (holdingProj != null){
            J2eeModuleProvider provider = (J2eeModuleProvider) holdingProj.getLookup().lookup(J2eeModuleProvider.class);
            if(provider != null){
                File resourceLoc = provider.getJ2eeModule().getResourceDirectory();
                if(resourceLoc != null){
                    if(resourceLoc.exists ()){
                        resourceDir = FileUtil.toFileObject (resourceLoc);
                    }else{
                        resourceLoc.mkdirs ();
                        resourceDir = FileUtil.toFileObject (resourceLoc);
                    }
                }
            }
        }
        return resourceDir;
    }
    
    /***************************************** DS Management API *****************************************************************************/
    
    public static HashSet getServerDataSources(DeploymentManager dm){
        HashSet datasources = new HashSet();
        try {
            ObjectName configObjName = new ObjectName(WizardConstants.MAP_RESOURCES);
            SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)dm;
            ServerInterface mejb = (ServerInterface)eightDM.getManagement();
            List systemDS = Arrays.asList(sysDatasources);
            if(eightDM.isRunning()){
                updateSampleDatasource(eightDM, configObjName);
                ObjectName[] resourceObjects = (ObjectName[])  mejb.invoke(configObjName, WizardConstants.__GetJdbcResource, null, null);
                for(int i=0; i<resourceObjects.length; i++){
                    ObjectName objName = resourceObjects[i];
                    //Get Required values from JDBC Resource
                    String dsJndiName = (String)mejb.getAttribute(objName, "jndi-name"); //NOI18N
                    if(! systemDS.contains(dsJndiName)){
                        String poolName = (String)mejb.getAttribute(objName, "pool-name"); //NOI18N
                        HashMap poolValues = fillInPoolValues(eightDM, configObjName, poolName);
                        if(! poolValues.isEmpty()){
                            String username = (String)poolValues.get(WizardConstants.__User);
                            String password = (String)poolValues.get(WizardConstants.__Password);
                            String url = (String)poolValues.get(WizardConstants.__Url);
                            String driverClassName = (String)poolValues.get(WizardConstants.__DriverClassName);
                            
                            SunDatasource ds = new SunDatasource(dsJndiName, url, username, password, driverClassName);
                            datasources.add(ds);
                        }
                    }
                } // for - each JDBC Resource
            } else{
                if(eightDM.isLocal()) {
                    datasources = formatXmlSunDatasources(eightDM.getSunDatasourcesFromXml());
                }    
            }// Server Running
        } catch (Exception ex) {
            //Unable to get server datasources
        }
        return datasources;
    }
    
    private static void updateSampleDatasource(SunDeploymentManagerInterface eightDM, ObjectName configObjName){
        try{
            if(! eightDM.isLocal())
                return;
            List datasources = getResourceNames(eightDM, __GetJdbcResource, "jndi-name"); //NOI18N
            if(! datasources.contains(SAMPLE_DATASOURCE)){
                ServerInterface mejb = (ServerInterface)eightDM.getManagement();
                
                if(getConnectionPoolObjByName(mejb, configObjName, SAMPLE_CONNPOOL) == null){
                    AttributeList poolAttrs = new AttributeList();
                    Attribute attr = new Attribute("name", SAMPLE_CONNPOOL); //NOI18N
                    poolAttrs.add(attr);
                    attr = new Attribute("datasource-classname", "org.apache.derby.jdbc.ClientDataSource"); //NOI18N
                    poolAttrs.add(attr);
                    attr = new Attribute("res-type", "javax.sql.DataSource"); //NOI18N
                    poolAttrs.add(attr);
                    
                    Properties propsList = new Properties();
                    propsList.put(__User, "app"); //NOI18N
                    propsList.put(__Password, "app"); //NOI18N
                    propsList.put(__ServerName, "localhost"); //NOI18N
                    propsList.put(__DerbyPortNumber, "1527");
                    propsList.put(__DerbyDatabaseName, "sample"); //NOI18N
                    Object[] poolParams = new Object[]{poolAttrs, propsList, null};
                    createResource(__CreateCP, poolParams, mejb);
                }
                
                AttributeList attrs = new AttributeList();
                attrs.add(new Attribute(__JndiName, SAMPLE_DATASOURCE));
                attrs.add(new Attribute(__PoolName, SAMPLE_CONNPOOL));
                attrs.add(new Attribute(__JdbcObjectType, "user")); //NOI18N
                attrs.add(new Attribute(__Enabled, "true")); //NOI18N
                Object[] params = new Object[]{attrs, new Properties(), null};
                createResource(__CreateDS, params, mejb);
            }
        }catch(Exception ex){}
    }
    
    public static HashMap fillInPoolValues(SunDeploymentManagerInterface eightDM, ObjectName configObjName, String poolName) throws Exception {
        HashMap connPoolAttrs = new HashMap();
        ServerInterface mejb = (ServerInterface)eightDM.getManagement();
        //Get Values from JDBC Connection Pool : driver
        ObjectName connPoolObj = getConnectionPoolByName(mejb, configObjName, poolName);
        String driverClassName = (String)mejb.getAttribute(connPoolObj, "datasource-classname"); //NOI18N
        String url = ""; //NOI18N
        String username = ""; //NOI18N
        String password = ""; //NOI18N
        String serverName = ""; //NOI18N
        String portNo = ""; //NOI18N
        String dbName = ""; //NOI18N
        String sid = ""; //NOI18N
        
        AttributeList attrList = (AttributeList)mejb.invoke(connPoolObj, WizardConstants.__GetProperties, null, null);
        HashMap attrs = getObjMap(attrList);
        Object[] keys = attrs.keySet().toArray();
        for(int i=0; i<keys.length; i++){
            String keyName = (String)keys[i];
            if(keyName.equalsIgnoreCase(WizardConstants.__DatabaseName)){
                if(driverClassName.indexOf("pointbase") != -1){ //NOI18N
                    url = getStringVal(attrs.get(keyName));
                }else{
                    dbName = getStringVal(attrs.get(keyName));
                }
            }else if(keyName.equalsIgnoreCase(WizardConstants.__User)) {
                username = getStringVal(attrs.get(keyName));
            }else if(keyName.equalsIgnoreCase(WizardConstants.__Password)) {
                password = getStringVal(attrs.get(keyName));
            }else if(keyName.equalsIgnoreCase(WizardConstants.__Url)) {
                url = getStringVal(attrs.get(keyName));
            }else if(keyName.equalsIgnoreCase(WizardConstants.__ServerName)) {
                serverName = getStringVal(attrs.get(keyName));
            }else if(keyName.equalsIgnoreCase(WizardConstants.__DerbyPortNumber)) {
                portNo = getStringVal(attrs.get(keyName));
            }else if(keyName.equalsIgnoreCase(WizardConstants.__SID)) {
                sid = getStringVal(attrs.get(keyName));
            }
        }
        
        if(driverClassName.indexOf("derby") != -1){ //NOI18N
            url = "jdbc:derby://"; //NOI18N
            if(serverName != null){
                url = url + serverName;
                if(portNo != null && portNo.length() > 0) {
                    url = url + ":" + portNo; //NOI18N
                }    
                url = url + "/" + dbName ; //NOI8N
            }
        }else if(url.equals("")) { //NOI18N
            String urlPrefix = DatabaseUtils.getUrlPrefix(driverClassName);
            String vName = ResourceConfigurator.getDatabaseVendorName(urlPrefix, null); 
            if(serverName != null){
                if(vName.equals("sybase2")){ //NOI18N
                    url = urlPrefix + serverName; 
                } else{
                    url = urlPrefix + "//" + serverName; //NOI18N
                }
                if(portNo != null && portNo.length() > 0) {
                    url = url + ":" + portNo; //NOI18N
                }    
            }
            if(vName.equals("sun_oracle") || vName.equals("datadirect_oracle")) { //NOI18N
                url = url + ";SID=" + sid; //NOI18N
            }else if(Arrays.asList(WizardConstants.Reqd_DBName).contains(vName)) {
                url = url + ";databaseName=" + dbName; //NOI18N
            }else if(Arrays.asList(WizardConstants.VendorsDBNameProp).contains(vName)) {
                url = url + "/" + dbName ; //NOI8N
            }    
        }
        
        if((! eightDM.isLocal()) && (url.indexOf("localhost") != -1)){ //NOI18N
            String hostName = eightDM.getHost();
            url = url.replaceFirst("localhost", hostName); //NOI18N
        }
        DatabaseConnection databaseConnection = getDatabaseConnection(url);
        if(databaseConnection != null){
            driverClassName = databaseConnection.getDriverClass();
        }else{
            //Fix Issue 78212 - NB required driver classname
            String drivername = DatabaseUtils.getDriverName(url);
            if(drivername != null) {
                driverClassName = drivername;
            }    
        }    
        
        connPoolAttrs.put(__User, username);
        connPoolAttrs.put(__Password, password);
        connPoolAttrs.put(__Url, url);
        connPoolAttrs.put(__DriverClassName, driverClassName);
        return connPoolAttrs;
    }
    
    private static ObjectName getConnectionPoolByName(ServerInterface mejb, ObjectName configObjName, String poolName) throws Exception {
        String[] signature = new String[]{"java.lang.String"};  //NOI18N
        Object[] params = new Object[]{poolName};
        ObjectName connPoolObj = (ObjectName) mejb.invoke(configObjName, WizardConstants.__GetJdbcConnectionPoolByName, params, signature);
        return connPoolObj;
    }
    
    private static ObjectName getConnectionPoolObjByName(ServerInterface mejb, ObjectName configObjName, String poolName) {
        ObjectName connPoolObj = null;
        try{
            connPoolObj = getConnectionPoolByName(mejb, configObjName, poolName);
        }catch(Exception ex){}
        return connPoolObj;
    }
        
    
    private static String getStringVal(Object val){
        String value = null;
        if (val != null)
            value = val.toString();
        return value; 
    }
    
    private static HashMap getObjMap(AttributeList attrList){
        HashMap attrs = new HashMap();
        for(int k=0; k<attrList.size(); k++){
            Attribute currAttr = (Attribute)attrList.get(k);
            String pname = currAttr.getName();
            Object pObjvalue = currAttr.getValue();
            attrs.put(pname, pObjvalue);
        }
        return attrs;
    }
    
    public static String revertToResName(String filename) {
        if(filename.indexOf("jdbc_") != -1)
            filename = filename.replaceFirst("jdbc_", "jdbc/");
        if(filename.indexOf("mail_") != -1)
            filename = filename.replaceFirst("mail_", "mail/");
        if(filename.indexOf("jms_") != -1)
            filename = filename.replaceFirst("jms_", "jms/");
        return filename;
    }
    
    public static boolean isUniqueFileName(String in_targetName, FileObject fo, String defName){
        boolean isUniq = true;
        String targetName = in_targetName;
        if (targetName != null && targetName.length() != 0) {
              targetName = makeLegalFilename(targetName);
              targetName = targetName + "." + __SunResourceExt; //NOI18N
              File targFile = new File(fo.getPath(), targetName);
              if(targFile.exists())
                  isUniq = false;
        }
        return isUniq;
    }
    
    public static DatabaseConnection getDatabaseConnection(String url) {
        DatabaseConnection[] dbConns = ConnectionManager.getDefault().getConnections();
        for(int i=0; i<dbConns.length; i++){
            String dbConnUrl = ((DatabaseConnection)dbConns[i]).getDatabaseURL();
            if(dbConnUrl.startsWith(url))
                return ((DatabaseConnection)dbConns[i]);
        }
        return null;
    }
    
    public static InstanceProperties getTargetServer(FileObject fo){
        InstanceProperties serverName = null;
        Project holdingProj = FileOwnerQuery.getOwner(fo);
        if (holdingProj != null){
            J2eeModuleProvider modProvider = (J2eeModuleProvider) holdingProj.getLookup().lookup(J2eeModuleProvider.class);
            if(modProvider != null)
                serverName = modProvider.getInstanceProperties();
        }
        return serverName;
    }
    
    public static HashMap getConnPoolValues(File resourceDir, String poolName){
        HashMap poolValues = new HashMap();
        try{
            ObjectName configObjName = new ObjectName(WizardConstants.MAP_RESOURCES);
            InstanceProperties instanceProperties = getTargetServer(FileUtil.toFileObject(resourceDir));
            if(instanceProperties != null){
                SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)instanceProperties.getDeploymentManager();
                if(eightDM.isRunning()){
                    ServerInterface mejb = (ServerInterface)eightDM.getManagement();
                    poolValues = fillInPoolValues(eightDM, configObjName, poolName);
                }else{
                    if(eightDM.isLocal()){
                        HashMap poolMap = eightDM.getConnPoolsFromXml();
                        poolValues = formatPoolMap((HashMap)poolMap.get(poolName));
                    }
                }    
            }
        }catch(Exception ex){ }
        return poolValues;
    }
    
    public static HashSet formatXmlSunDatasources(HashMap dsMap){
        HashSet datasources = new HashSet();
        String[] keys = (String[])dsMap.keySet().toArray(new String[dsMap.size()]);
        for(int i=0; i<keys.length; i++){
            String jndiName = keys[i];
            HashMap poolValues = (HashMap)dsMap.get(jndiName);
            poolValues = formatPoolMap(poolValues);
            
            String url = getStringVal(poolValues.get(__Url));
            String username = getStringVal(poolValues.get(__User));
            String password = getStringVal(poolValues.get(__Password));
            String driverClassName = getStringVal(poolValues.get(__DriverClassName)); //NOI18N
            if((url != null) && (! url.equals (""))) { //NOI18N
                SunDatasource ds = new SunDatasource (jndiName, url, username, password, driverClassName);
                datasources.add (ds);
            }
        }

        return datasources;
    }
    
    private static HashMap formatPoolMap(HashMap poolValues){
        String driverClassName = getStringVal(poolValues.get("dsClassName")); //NOI18N
        
        String url = ""; //NOI18N
        String serverName = getStringVal(poolValues.get(__ServerName));
        String portNo     = getStringVal(poolValues.get(__DerbyPortNumber));
        String dbName     = getStringVal(poolValues.get(__DerbyDatabaseName));
        String dbVal     = getStringVal(poolValues.get(__DatabaseName));
        String portVal     = getStringVal(poolValues.get(__PortNumber));
        String sid     = getStringVal(poolValues.get(__SID));
        if(driverClassName.indexOf("pointbase") != -1){
            url = getStringVal(poolValues.get(__DatabaseName));
        }else if(driverClassName.indexOf("derby") != -1){
            if(serverName != null){
                url = "jdbc:derby://" + serverName;
                if(portNo != null && portNo.length() > 0) {
                    url = url + ":" + portNo; //NOI18N
                }
                url = url + "/" + dbName ; //NOI8N
            }
        }else{
            String in_url = getStringVal(poolValues.get(__Url));
            if(in_url != null) {
                url = in_url;
            }    
            if(url.equals("")) { //NOI18N
                String urlPrefix = DatabaseUtils.getUrlPrefix(driverClassName);
                String vName = ResourceConfigurator.getDatabaseVendorName(urlPrefix, null); 
                if(serverName != null){
                    if(vName.equals("sybase2")){ //NOI18N
                        url = urlPrefix + serverName; 
                    }else{
                         url = urlPrefix + "//" + serverName; //NOI18N
                    } 
                    if(portVal != null && portVal.length() > 0) {
                        url = url + ":" + portVal; //NOI18N
                    }    
                }
                if(vName.equals("sun_oracle") || vName.equals("datadirect_oracle")) { //NOI18N
                    url = url + ";SID=" + sid; //NOI18N
                }else if(Arrays.asList(WizardConstants.Reqd_DBName).contains(vName)) {
                    url = url + ";databaseName=" + dbVal; //NOI18N
                }else if(Arrays.asList(WizardConstants.VendorsDBNameProp).contains(vName)) {
                    url = url + "/" + dbVal ; //NOI8N
                }    
            }   
        }    
        
        DatabaseConnection databaseConnection = getDatabaseConnection(url);
        if(databaseConnection != null) {
            driverClassName = databaseConnection.getDriverClass();
        }else{
            //Fix Issue 78212 - NB required driver classname
            String drivername = DatabaseUtils.getDriverName(url);
            if(drivername != null) {
                driverClassName = drivername;
            }    
        }
        
        poolValues.put(__Url, url);
        poolValues.put(__DriverClassName, driverClassName);
        
        return poolValues;
    }
    
    public static HashSet getServerDestinations(DeploymentManager dm){
        HashSet destinations = new HashSet();
        try {
            ObjectName configObjName = new ObjectName(WizardConstants.MAP_RESOURCES);
            SunDeploymentManagerInterface eightDM = (SunDeploymentManagerInterface)dm;
            ServerInterface mejb = (ServerInterface)eightDM.getManagement();
            if(eightDM.isRunning()){
                ObjectName[] resourceObjects = (ObjectName[])  mejb.invoke(configObjName, WizardConstants.__GetAdmObjResource, null, null);
                for(int i=0; i<resourceObjects.length; i++){
                    ObjectName objName = resourceObjects[i];
                    String jndiName = (String)mejb.getAttribute(objName, "jndi-name"); //NOI18N
                    String type = (String)mejb.getAttribute(objName, "res-type"); //NOI18N
                    SunMessageDestination sunMessage = null;
                    if(type.equals(__QUEUE)){
                        sunMessage = new SunMessageDestination(jndiName, MessageDestination.Type.QUEUE);
                    } else {
                        sunMessage = new SunMessageDestination(jndiName, MessageDestination.Type.TOPIC);
                    }
                    destinations.add(sunMessage);
                } // 
            } else{
                if(eightDM.isLocal()) {
                    HashMap aoMap =  eightDM.getAdminObjectResourcesFromXml();
                    String[] keys = (String[])aoMap.keySet().toArray(new String[aoMap.size()]);
                    for(int i=0; i<keys.length; i++){
                        String jndiName = keys[i];
                        String type = (String)aoMap.get(jndiName);
                        SunMessageDestination sunMessage = null;
                        if(type.equals(__QUEUE)){
                            sunMessage = new SunMessageDestination(jndiName, MessageDestination.Type.QUEUE);
                        } else {
                            sunMessage = new SunMessageDestination(jndiName, MessageDestination.Type.TOPIC);
                        }
                        destinations.add(sunMessage);
                    }
                }   
            }// Server Running
        } catch (Exception ex) {
            //Unable to get server datasources
        }
        return destinations;
    }
    
    public static boolean is90Server(ServerInterface mejb){
        boolean is90Server = true;
        SunDeploymentManagerInterface sunDm = (SunDeploymentManagerInterface)mejb.getDeploymentManager();
        if(sunDm.isLocal()){   
            is90Server = is90ServerLocal(sunDm); 
        }else{
            try{
                ObjectName serverObj = new ObjectName("com.sun.appserv:j2eeType=J2EEServer,name=server,category=runtime"); //NOI18N
                String serverName = (String)mejb.getAttribute(serverObj, "serverVersion"); //NOI18N
                if((serverName != null) && (serverName.indexOf("8.") != -1)) //NOI18N
                    is90Server = false;
            }catch(Exception ex){ }
        }
        return is90Server;
    }
     
    private static boolean is90ServerLocal(SunDeploymentManagerInterface sunDm){
        boolean isGlassfish = true;
        try{
            isGlassfish = ServerLocationManager.isGlassFish(sunDm.getPlatformRoot());
        }catch(Exception ex){ }
        return isGlassfish;

    }
     
    /*
     * Create a new sun-resources graph if none exists or obtain the existing 
     * graph to add new resource.
     */     
    public static Resources getServerResourcesGraph(File targetFolder){
        FileObject location = FileUtil.toFileObject(targetFolder.getParentFile());
        try{
            location = FileUtil.createFolder(targetFolder);
        }catch(Exception ex){
        
        }    
        return getServerResourcesGraph(location);
            
    }
    
    /*
     * Create a new sun-resources graph if none exists or obtain the existing 
     * graph to add new resource.
     */     
    public static Resources getServerResourcesGraph(FileObject targetFolder){
        Resources res = getResourceGraph();
        targetFolder = setUpExists(targetFolder);               
        File sunResource = getServerResourcesFile(targetFolder);
        if(sunResource != null){
            res = getResourcesGraph(sunResource);
        }
        return res;
    }

    /*
     * Get the resources-graph for a sun-resource.xml
     *
     */     
    public static Resources getResourcesGraph(File sunResource){
        Resources res = null;
        if(sunResource != null){
            java.io.FileInputStream in = null;
            try {
                in = new java.io.FileInputStream(sunResource);
                res = DDProvider.getDefault().getResourcesGraph(in);
            } catch (FileNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                }
            }
        }
        return res;
    }
    
    public static void createFile(File targetFolder, final Resources res){
        createFile(FileUtil.toFileObject(targetFolder), res);
    }
    
    public static void createFile(FileObject targetFolder, final Resources res){
        targetFolder = setUpExists(targetFolder);
        File sunResource = getServerResourcesFile(targetFolder);
        if((sunResource != null) && sunResource.exists()){
            try {
                res.write(sunResource);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }else{
            writeServerResource(targetFolder, res);
        }
    }
        
    private static void writeServerResource(FileObject targetFolder, final Resources res){
        try {
            final FileObject resTargetFolder  = targetFolder;
            FileSystem fs = targetFolder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws java.io.IOException {
                    FileObject newfile = resTargetFolder.createData("sun-resources", "xml"); //NOI18N
                    FileLock lock = newfile.lock();
                    Writer w = null;
                    try {
                        Writer out = new OutputStreamWriter(newfile.getOutputStream(lock), "UTF8");
                        res.write(out);
                        out.flush();
                        out.close();
                    } catch(Exception ex){
                        //Error writing file
                    } finally {
                        lock.releaseLock();
                    }
                }
            });
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }
    
    /*
     *  Get sun-resources.xml file if it exists in a given folder.
     *  Returns null if no file exists.
     */
    public static File getServerResourcesFile(FileObject targetFolder){
        File serverResource = null;
        if(targetFolder != null){
            FileObject setUpFolder = setUpExists(targetFolder);
            
            java.util.Enumeration en = setUpFolder.getData(false);
            while(en.hasMoreElements()){
                FileObject resourceFile = (FileObject)en.nextElement();
                File resource = FileUtil.toFile(resourceFile);
                if(resource.getName().equals(SUN_RESOURCE_FILENAME)){
                    serverResource = resource;
                }
            }
        }
        return serverResource;
    }
    
    /*
     * Consolidates *.sun-resource into sun-resources.xml 
     * Called by SunResourceDataObject by the .sun-resource
     * loader. sun-resources.xml is created once.
     */
    public static void migrateResources(FileObject targetFolder){
        targetFolder = setUpExists(targetFolder);
        File sunResource = getServerResourcesFile(targetFolder);
        boolean exists = false;
        if((sunResource == null) || (! sunResource.exists())){
            File resourceDir = FileUtil.toFile(targetFolder);
            File[] resources = resourceDir.listFiles(new ResourceFileFilter());
            Resources newGraph = DDProvider.getDefault().getResourcesGraph();
            try {
                for(int i=0; i<resources.length; i++){
                    FileInputStream in = new java.io.FileInputStream(resources[i]);
                    Resources existResource = DDProvider.getDefault().getResourcesGraph(in);
                    newGraph = getResourceGraphs(newGraph, existResource);
                }
                createFile(targetFolder, newGraph);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
            }
        }
    }
    
    private static Resources getResourceGraphs(Resources consolidatedGraph, Resources existResource){
        JdbcConnectionPool[] pools = existResource.getJdbcConnectionPool();
        if(pools.length != 0){
            ConnPoolBean currCPBean = ConnPoolBean.createBean(pools[0]);
            currCPBean.getBeanInGraph(consolidatedGraph);
        }
        
        JdbcResource[] dataSources = existResource.getJdbcResource();
        if(dataSources.length != 0){
            DataSourceBean currDSBean = DataSourceBean.createBean(dataSources[0]);
            currDSBean.getBeanInGraph(consolidatedGraph);
        }

        MailResource[] mailResources = existResource.getMailResource();
        if(mailResources.length != 0){
            JavaMailSessionBean currMailBean = JavaMailSessionBean.createBean(mailResources[0]);
            currMailBean.getBeanInGraph(consolidatedGraph);
        }
        
        AdminObjectResource[] aoResources = existResource.getAdminObjectResource();
        if(aoResources.length != 0){
            JMSBean jmsBean = JMSBean.createBean(aoResources[0]);
            jmsBean.getAdminObjectBeanInGraph(consolidatedGraph);
        }
        
        ConnectorResource[] connResources = existResource.getConnectorResource();
        ConnectorConnectionPool[] connPoolResources = existResource.getConnectorConnectionPool();
        if(connResources.length != 0 && connPoolResources.length != 0){
            JMSBean jmsBean = JMSBean.createBean(existResource);
            jmsBean.getConnectorBeanInGraph(consolidatedGraph);
        }
        
        PersistenceManagerFactoryResource[] pmfResources = existResource.getPersistenceManagerFactoryResource();
        if(pmfResources.length != 0){
            PersistenceManagerBean currPMFBean = PersistenceManagerBean.createBean(pmfResources[0]);
            currPMFBean.getBeanInGraph(consolidatedGraph);
        }

        return consolidatedGraph;
    }    
    
    private static class ResourceFileFilter implements FileFilter {
        public boolean accept(File f) {
            return f.isDirectory() ||
                    f.getName().toLowerCase(Locale.getDefault()).endsWith(".sun-resource"); //NOI18N
        }
    }
    
    /****************************************Utilities *********************************************/
    /**
     * 
     * @param name Resource Name
     * @param resources Map of objects to check Resource Name for duplicate
     * @return Returns unique resource name
     *    
     */
    public static String getUniqueResourceName(String name, HashMap resources){
        for (int i = 1;; i++) {
            String resourceName = name + "_" + i; // NOI18N
            if (! resources.containsKey(resourceName)) {
                return resourceName;
            }
        }
    }
    
    private final static char BLANK = ' ';
    private final static char DOT   = '.';
    private final static char REPLACEMENT_CHAR = '_';
    private final static char[]	ILLEGAL_FILENAME_CHARS	= {'/', '\\', ':', '*', '?', '"', '<', '>', '|', ',' };
    private final static char[]	ILLEGAL_RESOURCE_NAME_CHARS	= {':', '*', '?', '"', '<', '>', '|', ',' };
}
