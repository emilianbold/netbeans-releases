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

import org.netbeans.modules.j2ee.sun.share.dd.resources.*;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader.*;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.netbeans.modules.j2ee.sun.ide.editors.IsolationLevelEditor;
import org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.ResourceConfigData;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;


/*
 *
 * @author  nityad
 */
public class ResourceUtils implements WizardConstants{
    
    /** Creates a new instance of ResourceUtils */
    public ResourceUtils() {
    }
    
    public static void saveNodeToXml(ConnPoolBeanDataNode currentNode, ConnPoolBean bean){
        try {
            FileObject resFile = currentNode.getDataObject().getPrimaryFile();
                        
            Resources res = new Resources();
            JdbcConnectionPool connPool = new JdbcConnectionPool();
            connPool.setDescription(bean.getDescription());
            connPool.setAttributeValue(__Name, bean.getName());
            connPool.setAttributeValue(__DatasourceClassname, bean.getDsClass());
            connPool.setAttributeValue(__ResType, bean.getResType());
            connPool.setAttributeValue(__SteadyPoolSize, bean.getSteadyPoolSize());
            connPool.setAttributeValue(__MaxPoolSize, bean.getMaxPoolSize());
            connPool.setAttributeValue(__MaxWaitTimeInMillis, bean.getMaxWaitTimeMilli());
            connPool.setAttributeValue(__PoolResizeQuantity, bean.getPoolResizeQty());
            connPool.setAttributeValue(__IdleTimeoutInSeconds, bean.getIdleIimeoutSecond());
            String isolation = bean.getTranxIsoLevel();
            if (isolation != null && (isolation.length() == 0 || isolation.equals(NbBundle.getMessage(IsolationLevelEditor.class, "LBL_driver_default")))) {  //NOI18N
                isolation = null;
            }
            connPool.setAttributeValue(__TransactionIsolationLevel, isolation);
            connPool.setAttributeValue(__IsIsolationLevelGuaranteed, bean.getIsIsoLevGuaranteed());
            connPool.setAttributeValue(__IsConnectionValidationRequired, bean.getIsConnValidReq());
            connPool.setAttributeValue(__ConnectionValidationMethod, bean.getConnValidMethod());
            connPool.setAttributeValue(__ValidationTableName, bean.getValidationTableName());
            connPool.setAttributeValue(__FailAllConnections, bean.getFailAllConns());
            
            NameValuePair[] params = bean.getExtraParams();
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    ExtraProperty prop = new ExtraProperty();
                    prop.setAttributeValue("name", params[i].getParamName()); //NOI18N
                    prop.setAttributeValue("value", params[i].getParamValue()); //NOI18N
                    //prop.setDescription(params[i].getParamDescription());
                    connPool.addExtraProperty(prop);
                }
            }
            res.addJdbcConnectionPool(connPool);
            res.write(FileUtil.toFile(resFile));
            
        }catch(Exception ex){
            System.out.println("Unable to save file  nodeToXml for JDBC Connection Pool" + ex.getLocalizedMessage());
        }
    } // JDBC Connection Pool
    
    public static void saveNodeToXml(DataSourceBeanDataNode currentNode, DataSourceBean bean){
        try {
            FileObject resFile = currentNode.getDataObject().getPrimaryFile();
                        
            Resources res = new Resources();
            JdbcResource datasource = new JdbcResource();
            datasource.setDescription(bean.getDescription());
            datasource.setAttributeValue(__JndiName, bean.getJndiName());
            datasource.setAttributeValue(__PoolName, bean.getConnPoolName());
            datasource.setAttributeValue(__JdbcObjectType, bean.getResType());
            datasource.setAttributeValue(__Enabled, bean.getIsEnabled());
            
            // set properties
            NameValuePair[] params = bean.getExtraParams();
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    ExtraProperty prop = new ExtraProperty();
                    prop.setAttributeValue("name", params[i].getParamName()); //NOI18N
                    prop.setAttributeValue("value", params[i].getParamValue()); //NOI18N
                    //prop.setDescription(params[i].getParamDescription());
                    datasource.addExtraProperty(prop);
                }
            }
            
            res.addJdbcResource(datasource);
            res.write(FileUtil.toFile(resFile));
            
        }catch(Exception ex){
            System.out.println("Unable to save file  nodeToXml for DataSource" + ex.getLocalizedMessage());
        }
    }// DataSource
    
    public static void saveNodeToXml(PersistenceManagerBeanDataNode currentNode, PersistenceManagerBean bean){
        try {
            FileObject resFile = currentNode.getDataObject().getPrimaryFile();
                        
            Resources res = new Resources();
            PersistenceManagerFactoryResource pmfresource = new PersistenceManagerFactoryResource();
            pmfresource.setDescription(bean.getDescription());
            pmfresource.setAttributeValue(__JndiName, bean.getJndiName());
            pmfresource.setAttributeValue(__FactoryClass, bean.getFactoryClass());
            pmfresource.setAttributeValue(__JdbcResourceJndiName, bean.getDatasourceJndiName());
            pmfresource.setAttributeValue(__Enabled, bean.getIsEnabled());
            
            // set properties
            NameValuePair[] params = bean.getExtraParams();
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    ExtraProperty prop = new ExtraProperty();
                    prop.setAttributeValue("name", params[i].getParamName()); //NOI18N
                    prop.setAttributeValue("value", params[i].getParamValue()); //NOI18N
                    //prop.setDescription(params[i].getParamDescription());
                    pmfresource.addExtraProperty(prop);
                }
            }
            
            res.addPersistenceManagerFactoryResource(pmfresource);
            res.write(FileUtil.toFile(resFile));
            
        }catch(Exception ex){
            System.out.println("Unable to save file  nodeToXml for Persistence Manager" + ex.getLocalizedMessage());
        }
    }// Persistence Manager
    
    public static void saveNodeToXml(JavaMailSessionBeanDataNode currentNode, JavaMailSessionBean bean){
        try {
            FileObject resFile = currentNode.getDataObject().getPrimaryFile();
                        
            Resources res = new Resources();
            MailResource mlresource = new MailResource();
            mlresource.setDescription(bean.getDescription());
            mlresource.setAttributeValue(__JndiName, bean.getJndiName());
            mlresource.setAttributeValue(__StoreProtocol, bean.getStoreProt());
            mlresource.setAttributeValue(__StoreProtocolClass, bean.getStoreProtClass());
            mlresource.setAttributeValue(__TransportProtocol, bean.getTransProt());
            mlresource.setAttributeValue(__TransportProtocolClass, bean.getTransProtClass());
            mlresource.setAttributeValue(__Host, bean.getHostName());
            mlresource.setAttributeValue(__MailUser, bean.getUserName());
            mlresource.setAttributeValue(__From, bean.getFromAddr());
            mlresource.setAttributeValue(__Debug, bean.getIsDebug());
            mlresource.setAttributeValue(__Enabled, bean.getIsEnabled());
            
            // set properties
            NameValuePair[] params = bean.getExtraParams();
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    ExtraProperty prop = new ExtraProperty();
                    prop.setAttributeValue("name", params[i].getParamName()); //NOI18N
                    prop.setAttributeValue("value", params[i].getParamValue()); //NOI18N
                    //prop.setDescription(params[i].getParamDescription());
                    mlresource.addExtraProperty(prop);
                }
            }
            
            res.addMailResource(mlresource);
            res.write(FileUtil.toFile(resFile));
            
        }catch(Exception ex){
            System.out.println("Unable to save file  nodeToXml for Java Mail Session" + ex.getLocalizedMessage());
        }
    }// Java Mail Session
    
    public static void saveNodeToXml(JMSBeanDataNode currentNode, JMSBean bean){
        try {
            FileObject resFile = currentNode.getDataObject().getPrimaryFile();
                        
            Resources res = new Resources();
            JmsResource jmsresource = new JmsResource();
            jmsresource.setDescription(bean.getDescription());
            jmsresource.setAttributeValue(__JndiName, bean.getJndiName());
            jmsresource.setAttributeValue(__ResType, bean.getResType());
            jmsresource.setAttributeValue(__Enabled, bean.getIsEnabled());
            
            // set properties
            NameValuePair[] params = bean.getExtraParams();
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    ExtraProperty prop = new ExtraProperty();
                    prop.setAttributeValue("name", params[i].getParamName()); //NOI18N
                    prop.setAttributeValue("value", params[i].getParamValue()); //NOI18N
                    //prop.setDescription(params[i].getParamDescription());
                    jmsresource.addExtraProperty(prop);
                }
            }
            
            res.addJmsResource(jmsresource);
            res.write(FileUtil.toFile(resFile));
            
        }catch(Exception ex){
            System.out.println("Unable to save file  nodeToXml for Java Message Service" + ex.getLocalizedMessage());
        }
    }// Java Message Service
    
    public static AttributeList getAttributes(Property[] props, String type){
        AttributeList attrList = null;
        if(type.equals(__JdbcConnectionPool)){
            attrList = getConnPoolAttributesFromNode(props);
        }else if(type.equals(__JdbcResource)){
            attrList = getDataSourceAttributesFromNode(props);
        }else if(type.equals(__PersistenceManagerFactoryResource)){
            attrList = getPersistenceManagerAttributesFromNode(props);
        }else if(type.equals(__MailResource)){
            attrList = getMailSessionAttributesFromNode(props);
        }else if(type.equals(__JmsResource)){
            attrList = getJMSAttributesFromNode(props);
        }
        return attrList;
    }
    
    public static AttributeList getConnPoolAttributesFromNode(Property[] props){
        AttributeList attrs = new AttributeList();
        try{
            for(int i=0; i<props.length; i++){
                String propName = props[i].getName();
                Object propValue = props[i].getValue();
                String value = ""; //NOI18N 
                if(propValue != null){
                    value = propValue.toString();
                }
                if(propName.equals(__Name)){
                    attrs.add(new Attribute(__Name, value));
                }else if(propName.equals(__CPdsClass)){
                    attrs.add(new Attribute(__DatasourceClassname, value));
                }else if(propName.equals(__CPresType)){
                    attrs.add(new Attribute(__ResType, value));
                }else if(propName.equals(__CPsteadyPoolSize)){
                    attrs.add(new Attribute(__SteadyPoolSize, value));
                }else if(propName.equals(__CPmaxPoolSize)){
                    attrs.add(new Attribute(__MaxPoolSize, value));
                }else if(propName.equals(__CPmaxWaitTimeMilli)){
                    attrs.add(new Attribute(__MaxWaitTimeInMillis, value));
                }else if(propName.equals(__CPpoolResizeQty)){
                    attrs.add(new Attribute(__PoolResizeQuantity, value));
                }else if(propName.equals(__CPidleIimeoutSecond)){
                    attrs.add(new Attribute(__IdleTimeoutInSeconds, value));
                }else if(propName.equals(__CPtranxIsoLevel)){
                    String isolation = value;
                    if (value != null && (value.length() == 0 || value.equals(NbBundle.getMessage(IsolationLevelEditor.class, "LBL_driver_default"))) ){  //NOI18N
                        isolation = null;
                    }
                    attrs.add(new Attribute(__TransactionIsolationLevel, isolation));
                }else if(propName.equals(__CPisIsoLevGuaranteed)){
                    attrs.add(new Attribute(__IsIsolationLevelGuaranteed, value));
                }else if(propName.equals(__CPisConnValidReq)){
                    attrs.add(new Attribute(__IsConnectionValidationRequired, value));
                }else if(propName.equals(__CPconnValidMethod)){
                    attrs.add(new Attribute(__ConnectionValidationMethod, value));
                }else if(propName.equals(__CPvalidationTableName)){
                    attrs.add(new Attribute(__ValidationTableName, value));
                }else if(propName.equals(__CPfailAllConns)){
                    attrs.add(new Attribute(__FailAllConnections, value));
                }else if(propName.equals(__Description)){
                    attrs.add(new Attribute(__Description, value));
                }
            }
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list for: getConnPoolAttributesFromNode " );
            ex.printStackTrace();
        }
        return attrs;
    }
    
    public static AttributeList getDataSourceAttributesFromNode(Property[] props){
        AttributeList attrs = new AttributeList();
        try{
            for(int i=0; i<props.length; i++){
                String propName = props[i].getName();
                Object propValue = props[i].getValue();
                String value = ""; //NOI18N
                if(propValue != null){
                    value = propValue.toString();
                }
                
                if(propName.equals(__BeanjndiName)){
                    attrs.add(new Attribute(__JndiName, value));
                }else if(propName.equals(__DSconnPoolName)){
                    attrs.add(new Attribute(__PoolName, value));
                }else if(propName.equals(__DSresType)){
                    attrs.add(new Attribute(__JdbcObjectType, value));
                }else if(propName.equals(__BeanisEnabled)){
                    attrs.add(new Attribute(__Enabled, value));
                }else if(propName.equals(__Description)){
                    attrs.add(new Attribute(__Description, value));
                }
            }
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getDataSourceAttributesFromNode ");
        }
        return attrs;
    }
    
    public static AttributeList getPersistenceManagerAttributesFromNode(Property[] props){
        AttributeList attrs = new AttributeList();
        try{
            for(int i=0; i<props.length; i++){
                String propName = props[i].getName();
                Object propValue = props[i].getValue();
                String value = ""; //NOI18N
                if(propValue != null){
                    value = propValue.toString();
                }
                
                if(propName.equals(__BeanjndiName)){
                    attrs.add(new Attribute(__JndiName, value));
                }else if(propName.equals(__PMFfactoryClass)){
                    attrs.add(new Attribute(__FactoryClass, value));
                }else if(propName.equals(__PMFdatasourceJndiName)){
                    attrs.add(new Attribute(__JdbcResourceJndiName, value));
                }else if(propName.equals(__BeanisEnabled)){
                    attrs.add(new Attribute(__Enabled, value));
                }else if(propName.equals(__Description)){
                    attrs.add(new Attribute(__Description, value));
                }
            }
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getPersistenceManagerAttributesFromNode ");
        }
        return attrs;
    }
    
    public static AttributeList getMailSessionAttributesFromNode(Property[] props){
        AttributeList attrs = new AttributeList();
        try{
            for(int i=0; i<props.length; i++){
                String propName = props[i].getName();
                Object propValue = props[i].getValue();
                String value = ""; //NOI18N
                if(propValue != null){
                    value = propValue.toString();
                }
                
                if(propName.equals(__BeanjndiName)){
                    attrs.add(new Attribute(__JndiName, value));
                }else if(propName.equals(__MAILstoreProt)){
                    attrs.add(new Attribute(__StoreProtocol, value));
                }else if(propName.equals(__MAILstoreProtClass)){
                    attrs.add(new Attribute(__StoreProtocolClass, value));
                }else if(propName.equals(__MAILtransProt)){
                    attrs.add(new Attribute(__TransportProtocol, value));
                }else if(propName.equals(__MAILtransProtClass)){
                    attrs.add(new Attribute(__TransportProtocolClass, value));
                }else if(propName.equals(__MAILhostName)){
                    attrs.add(new Attribute(__Host, value));
                }else if(propName.equals(__MAILuserName)){
                    attrs.add(new Attribute(__MailUser, value));
                }else if(propName.equals(__MAILfromAddr)){
                    attrs.add(new Attribute(__From, value));
                }else if(propName.equals(__MAILisDebug)){
                    attrs.add(new Attribute(__Debug, value));
                }else if(propName.equals(__BeanisEnabled)){
                    attrs.add(new Attribute(__Enabled, value));
                }else if(propName.equals(__Description)){
                    attrs.add(new Attribute(__Description, value));
                }
            }
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getMailSessionAttributesFromNode ");
        }
        return attrs;
    }
    
    public static AttributeList getJMSAttributesFromNode(Property[] props){
        AttributeList attrs = new AttributeList();
        try{
            for(int i=0; i<props.length; i++){
                String propName = props[i].getName();
                Object propValue = props[i].getValue();
                String value = ""; //NOI18N
                if(propValue != null){
                    value = propValue.toString();
                }
                
                if(propName.equals(__BeanjndiName)){
                    attrs.add(new Attribute(__JavaMessageJndiName, value)); 
                }else if(propName.equals(__JMSResType)){
                    attrs.add(new Attribute(__JavaMessageResType, value));
                }else if(propName.equals(__BeanisEnabled)){
                    attrs.add(new Attribute(__Enabled, value));
                }else if(propName.equals(__Description)){
                    attrs.add(new Attribute(__Description, value));
                }
            }
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getJMSAttributesFromNode ");
        }
        return attrs;
    }
    
    public static Properties getProperties(Property[] props){
        Properties propList = new Properties();
        try{
            for(int i=0; i<props.length; i++){
                String propName = props[i].getName();
                Object propValue = props[i].getValue();
                if(propValue != null && propName.equals(__ExtraParams)){
                    NameValuePair[] extraValue = (NameValuePair[])props[i].getValue();
                    for(int j=0; j<extraValue.length; j++){
                        propList.put(extraValue[j].getParamName(), extraValue[j].getParamValue());
                    }
                }    
            }
        }catch(Exception ex){
            System.out.println("Unable to construct properties list for: getProperties " );
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
            Resources res = new Resources();
            JdbcConnectionPool connPool = new JdbcConnectionPool();
            
            String[] keys = data.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)data.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        ExtraProperty prop = new ExtraProperty();
                        prop.setAttributeValue("name",pair.getParamName()); //NOI18N
                        prop.setAttributeValue("value", pair.getParamValue()); //NOI18N
                        //prop.setDescription(pair.getParamDescription());
                        connPool.addExtraProperty(prop);
                    }
                }else{
                    String value = data.getString(key);
                    if (key.equals(__Name))
                        connPool.setAttributeValue(__Name, value);
                    else if (key.equals(__DatasourceClassname))
                        connPool.setAttributeValue(__DatasourceClassname, value);
                    else if (key.equals(__ResType))
                        connPool.setAttributeValue(__ResType, value);
                    else if (key.equals(__SteadyPoolSize))
                        connPool.setAttributeValue(__SteadyPoolSize, value);
                    else if (key.equals(__MaxPoolSize))
                        connPool.setAttributeValue(__MaxPoolSize, value);
                    else if (key.equals(__MaxWaitTimeInMillis))
                        connPool.setAttributeValue(__MaxWaitTimeInMillis, value);
                    else if (key.equals(__PoolResizeQuantity))
                        connPool.setAttributeValue(__PoolResizeQuantity, value);
                    else if (key.equals(__IdleTimeoutInSeconds))
                        connPool.setAttributeValue(__IdleTimeoutInSeconds, value);
                    else if (key.equals(__TransactionIsolationLevel))
                        connPool.setAttributeValue(__TransactionIsolationLevel, value);
                    else if (key.equals(__IsIsolationLevelGuaranteed))
                        connPool.setAttributeValue(__IsIsolationLevelGuaranteed, value);
                    else if (key.equals(__IsConnectionValidationRequired))
                        connPool.setAttributeValue(__IsConnectionValidationRequired, value);
                    else if (key.equals(__ConnectionValidationMethod))
                        connPool.setAttributeValue(__ConnectionValidationMethod, value);
                    else if (key.equals(__ValidationTableName))
                        connPool.setAttributeValue(__ValidationTableName, value);
                    else if (key.equals(__FailAllConnections))
                        connPool.setAttributeValue(__FailAllConnections, value);
                    else if (key.equals(__Description)){
                        connPool.setDescription(value); 
                    }    
                    //else if(key.equals(__IsCPExisting))
                    //bean.setIsExistingConnection(value);
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
            Resources res = new Resources();
            JdbcResource datasource = new JdbcResource();
           
            String[] keys = dsData.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)dsData.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        ExtraProperty prop = new ExtraProperty();
                        prop.setAttributeValue("name",pair.getParamName()); //NOI18N
                        prop.setAttributeValue("value", pair.getParamValue()); //NOI18N
                        //prop.setDescription(pair.getParamDescription());
                        datasource.addExtraProperty(prop);
                    }
                }else{
                    String value = dsData.getString(key);
                    if (key.equals(__JndiName))
                        datasource.setAttributeValue(__JndiName, value);
                    else if (key.equals(__PoolName))
                        datasource.setAttributeValue(__PoolName, value);
                    else if (key.equals(__JdbcObjectType))
                        datasource.setAttributeValue(__JdbcObjectType, value);
                    else if (key.equals(__Enabled))
                        datasource.setAttributeValue(__Enabled, value);
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
            Resources res = new Resources();
            PersistenceManagerFactoryResource pmfresource = new PersistenceManagerFactoryResource();
           
            String[] keys = pmfData.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)){
                    Vector props = (Vector)pmfData.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        ExtraProperty prop = new ExtraProperty();
                        prop.setAttributeValue("name",pair.getParamName()); //NOI18N
                        prop.setAttributeValue("value", pair.getParamValue()); //NOI18N
                        //prop.setDescription(pair.getParamDescription());
                        pmfresource.addExtraProperty(prop);
                    }
                }else{
                    String value = pmfData.getString(key);
                    if (key.equals(__JndiName))
                        pmfresource.setAttributeValue(__JndiName, value);
                    else if (key.equals(__FactoryClass))
                        pmfresource.setAttributeValue(__FactoryClass, value);
                    else if (key.equals(__JdbcResourceJndiName))
                        pmfresource.setAttributeValue(__JdbcResourceJndiName, value);
                    else if (key.equals(__Enabled))
                        pmfresource.setAttributeValue(__Enabled, value);
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
            Resources res = new Resources();
            JmsResource jmsResource = new JmsResource();
            String[] keys = jmsData.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)) {
                    Vector props = (Vector)jmsData.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        ExtraProperty prop = new ExtraProperty();
                        prop.setAttributeValue("name",pair.getParamName()); //NOI18N
                        prop.setAttributeValue("value", pair.getParamValue()); //NOI18N
                        //prop.setDescription(pair.getParamDescription());
                        jmsResource.addExtraProperty(prop);
                    }
                }else{
                    String value = jmsData.getString(key);
                    if (key.equals(__JndiName))
                        jmsResource.setAttributeValue(__JndiName, value);
                    else if (key.equals(__ResType))
                        jmsResource.setAttributeValue(__ResType, value);
                    else if (key.equals(__Enabled))

                        jmsResource.setAttributeValue(__Enabled, value);
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
            Resources res = new Resources();
            MailResource mlresource = new MailResource();
                        
            String[] keys = data.getFieldNames();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key.equals(__Properties)) {
                    Vector props = (Vector)data.getProperties();
                    for (int j = 0; j < props.size(); j++) {
                        NameValuePair pair = (NameValuePair)props.elementAt(j);
                        ExtraProperty prop = new ExtraProperty();
                        prop.setAttributeValue("name",pair.getParamName()); //NOI18N
                        prop.setAttributeValue("value", pair.getParamValue()); //NOI18N
                        //prop.setDescription(pair.getParamDescription());
                        mlresource.addExtraProperty(prop);
                    }
                }else{
                    String value = data.getString(key);
                    if (key.equals(__JndiName))
                        mlresource.setAttributeValue(__JndiName, value);
                    else if (key.equals(__StoreProtocol))
                        mlresource.setAttributeValue(__StoreProtocol, value);
                    else if (key.equals(__StoreProtocolClass))
                        mlresource.setAttributeValue(__StoreProtocolClass, value);
                    else if (key.equals(__TransportProtocol))
                        mlresource.setAttributeValue(__TransportProtocol, value);
                    else if (key.equals(__TransportProtocolClass))
                        mlresource.setAttributeValue(__TransportProtocolClass, value);
                    else if (key.equals(__Host))
                        mlresource.setAttributeValue(__Host, value);
                    else if (key.equals(__MailUser))
                        mlresource.setAttributeValue(__MailUser, value);
                    else if (key.equals(__From))
                        mlresource.setAttributeValue(__From, value);
                    else if (key.equals(__Debug))
                        mlresource.setAttributeValue(__Debug, value);
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
                Resources resources = Resources.createGraph(in);
                
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
                Resources resources = Resources.createGraph(in);
                
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
