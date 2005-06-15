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
 * Utils.java
 *
 * Created on December 5, 2004, 6:33 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.*;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Rajeshwar Patil
 */
public class Utils {

    static final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.j2ee.Bundle");// NOI18N
    static final File[] EMPTY_FILE_LIST = new File[0];

    /** Creates a new instance of Utils */
    public Utils() {
    }


    public static Object getResource(java.io.File primaryFile) {
       try {
            if((! primaryFile.isDirectory())/* && primaryFile.isValid()*/){
                FileInputStream in = new FileInputStream(primaryFile);
                Resources resources = (Resources)org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources.createGraph(in);
                
                // identify JDBC Connection Pool xml
                JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
                if(pools.length != 0){
                    return pools[0];
                }  
                // identify JDBC Resources xml
                JdbcResource[] dataSources = resources.getJdbcResource();
                if(dataSources.length != 0){
                    return dataSources[0];
                }
                
                // import Persistence Manager Factory Resources
                PersistenceManagerFactoryResource[] pmfResources = resources.getPersistenceManagerFactoryResource();
                if(pmfResources.length != 0){
                    return pmfResources[0];
                }
                // import Mail Resources
                MailResource[] mailResources = resources.getMailResource();
                if(mailResources.length != 0){
                    return mailResources[0];
                }
                // import Java Message Service Resources
                JmsResource[] jmsResources = resources.getJmsResource();
                if(jmsResources.length != 0){
                    return jmsResources[0];
                }
            }
        }catch(Exception exception){
            System.out.println("Error while resource creation  ");
            return null;
        }
         return null;
    }


    public static AttributeList getAttributes(JdbcConnectionPool resource){
        AttributeList attrs = new AttributeList();
        try{
            String value = ""; //NOI18N
            value = resource.getName();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Name, value));
            value = resource.getDatasourceClassname();
            if(value != null) attrs.add(new Attribute(WizardConstants.__DatasourceClassname, value));
            value = resource.getResType();
            if(value != null) attrs.add(new Attribute(WizardConstants.__ResType, value));
            value = resource.getSteadyPoolSize();
            if(value != null) attrs.add(new Attribute(WizardConstants.__SteadyPoolSize, value));
            value = resource.getMaxPoolSize();
            if(value != null) attrs.add(new Attribute(WizardConstants.__MaxPoolSize, value));
            value = resource.getMaxWaitTimeInMillis();
            if(value != null) attrs.add(new Attribute(WizardConstants.__MaxWaitTimeInMillis, value));
            value = resource.getPoolResizeQuantity();
            if(value != null) attrs.add(new Attribute(WizardConstants.__PoolResizeQuantity, value));
            ///value = resource.getIdleIimeoutInSeconds();   //FIXME
            if(value != null) attrs.add(new Attribute(WizardConstants.__IdleTimeoutInSeconds, value));
            value = resource.getTransactionIsolationLevel();
            String isolation = value;
            if (value != null && (value.length() == 0 || value.equals(bundle.getString("LBL_driver_default"))) ){  //NOI18N
                isolation = null;
            }
            if(value != null) attrs.add(new Attribute(WizardConstants.__TransactionIsolationLevel, isolation));
            value = resource.getIsIsolationLevelGuaranteed();
            if(value != null) attrs.add(new Attribute(WizardConstants.__IsIsolationLevelGuaranteed, value));
            value = resource.getIsConnectionValidationRequired();
            if(value != null) attrs.add(new Attribute(WizardConstants.__IsConnectionValidationRequired, value));
            value = resource.getConnectionValidationMethod();
            if(value != null) attrs.add(new Attribute(WizardConstants.__ConnectionValidationMethod, value));
            value = resource.getValidationTableName();
            if(value != null) attrs.add(new Attribute(WizardConstants.__ValidationTableName, value));
            value = resource.getFailAllConnections();
            if(value != null) attrs.add(new Attribute(WizardConstants.__FailAllConnections, value));
            value = resource.getDescription();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Description, value));
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getConnectionPoolAttributes ");
        }
        return attrs;
    } 


    public static Properties getProperties(JdbcConnectionPool resource){
        Properties propList = new Properties();
        try{
            PropertyElement[] extraValue = resource.getPropertyElement();
            for(int j=0; j<extraValue.length; j++){
                propList.put(extraValue[j].getName(), extraValue[j].getValue());
            }
        }catch(Exception ex){
            System.out.println("Unable to construct properties list for: getConnectionPoolProperties " );
        }
        return propList;
    }


    public static AttributeList getAttributes(JdbcResource resource){
        AttributeList attrs = new AttributeList();
        try{
            String value = ""; //NOI18N
            value = resource.getJndiName();
            if(value != null) attrs.add(new Attribute(WizardConstants.__JndiName, value));
            value = resource.getPoolName();
            if(value != null) attrs.add(new Attribute(WizardConstants.__PoolName, value));
            value = resource.getObjectType();
            if(value != null) attrs.add(new Attribute(WizardConstants.__JdbcObjectType, value));
            value = resource.getEnabled();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Enabled, value));
            value = resource.getDescription();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Description, value));
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getDataSourceAttributes ");
        }
        return attrs;
    } 


    public static Properties getProperties(JdbcResource resource){
        Properties propList = new Properties();
        try{
            PropertyElement[] extraValue = resource.getPropertyElement();
            for(int j=0; j<extraValue.length; j++){
                propList.put(extraValue[j].getName(), extraValue[j].getValue());
            }
        }catch(Exception ex){
            System.out.println("Unable to construct properties list for: getDataSourceProperties " );
        }
        return propList;
    }

    
    public static AttributeList getAttributes(PersistenceManagerFactoryResource resource){
        AttributeList attrs = new AttributeList();
        try{
            String value = ""; //NOI18N
            value = resource.getJndiName();
            if(value != null) attrs.add(new Attribute(WizardConstants.__JndiName, value));
            value = resource.getFactoryClass();
            if(value != null) attrs.add(new Attribute(WizardConstants.__FactoryClass, value));
            value = resource.getJdbcResourceJndiName();
            if(value != null) attrs.add(new Attribute(WizardConstants.__JdbcResourceJndiName, value));
            value = resource.getEnabled();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Enabled, value));
            value = resource.getDescription();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Description, value));
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getPersistenceManagerAttributes ");
        }
        return attrs;
    } 


    public static Properties getProperties(PersistenceManagerFactoryResource resource){
        Properties propList = new Properties();
        try{
            PropertyElement[] extraValue = resource.getPropertyElement();
            for(int j=0; j<extraValue.length; j++){
                propList.put(extraValue[j].getName(), extraValue[j].getValue());
            }
        }catch(Exception ex){
            System.out.println("Unable to construct properties list for: getPersistenceManagerProperties " );
        }
        return propList;
    }


    public static AttributeList getAttributes(MailResource resource){
        
        AttributeList attrs = new AttributeList();
        try{
            String value = ""; //NOI18N

            value = resource.getJndiName();
            if(value != null) attrs.add(new Attribute(WizardConstants.__JndiName, value));
            value = resource.getStoreProtocol();
            if(value != null) attrs.add(new Attribute(WizardConstants.__StoreProtocol, value));
            value = resource.getStoreProtocolClass();
            if(value != null) attrs.add(new Attribute(WizardConstants.__StoreProtocolClass, value));
            value = resource.getTransportProtocol();
            if(value != null) attrs.add(new Attribute(WizardConstants.__TransportProtocol, value));
            value = resource.getTransportProtocolClass();
            if(value != null) attrs.add(new Attribute(WizardConstants.__TransportProtocolClass, value));
            value = resource.getHost();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Host, value));
            value = resource.getUser();
            if(value != null) attrs.add(new Attribute(WizardConstants.__MailUser, value));
            value = resource.getFrom();
            if(value != null) attrs.add(new Attribute(WizardConstants.__From, value));
            value = resource.getDebug();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Debug, value));
            value = resource.getEnabled();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Enabled, value));
            value = resource.getDescription();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Description, value));
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getMailSessionAttributes ");
        }
        return attrs;
    } 


    public static Properties getProperties(MailResource resource){
        Properties propList = new Properties();
        try{
            PropertyElement[] extraValue = resource.getPropertyElement();
            for(int j=0; j<extraValue.length; j++){
                propList.put(extraValue[j].getName(), extraValue[j].getValue());
            }
        }catch(Exception ex){
            System.out.println("Unable to construct properties list for: getMailSessionProperties " );
        }
        return propList;
    }


    public static AttributeList getAttributes(JmsResource resource){
        AttributeList attrs = new AttributeList();
        try{
            String value = ""; //NOI18N

            value = resource.getJndiName();
            if(value != null) attrs.add(new Attribute(WizardConstants.__JavaMessageJndiName, value)); 
            value = resource.getResType();
            if(value != null) attrs.add(new Attribute(WizardConstants.__JavaMessageResType, value));
            value = resource.getEnabled();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Enabled, value)); 
            value = resource.getDescription();
            if(value != null) attrs.add(new Attribute(WizardConstants.__Description, value)); 
        }catch(Exception ex){
            System.out.println("Unable to construct attribute list: getJMSAttributes ");
        }
        return attrs;
    }


    public static Properties getProperties(JmsResource resource){
        Properties propList = new Properties();
        try{
            PropertyElement[] extraValue = resource.getPropertyElement();
            for(int j=0; j<extraValue.length; j++){
                propList.put(extraValue[j].getName(), extraValue[j].getValue());
            }
        }catch(Exception ex){
            System.out.println("Unable to construct properties list for: getJMSProperties " );
        }
        return propList;
    }    


    public static void setTopManagerStatus(String msg){
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);
    }


    public static File[] getResourceDirs(javax.enterprise.deploy.model.DeployableObject deployableObject){
        try{
            SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(deployableObject);
            if (sourceFileMap != null)
                return sourceFileMap.getEnterpriseResourceDirs();
            }catch(Exception exception){
                System.out.println(exception.getMessage());
        }
        return EMPTY_FILE_LIST;
    }


    public static File[] getResourceDirs(File file){
      try{
             FileObject fo = FileUtil.toFileObject(file);
             SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(fo);
             if (sourceFileMap != null)
                 return sourceFileMap.getEnterpriseResourceDirs();
             }catch(Exception exception){
                 System.out.println(exception.getMessage());
         }
         return EMPTY_FILE_LIST;
    }     


    public static void registerResources(java.io.File[] resourceDirs, ServerInterface mejb){
        System.out.println(bundle.getString("Msg_ProjResRegisterStart")); //NOI18N
        for (int j=0; j<resourceDirs.length; j++){
            File resourceDir = resourceDirs[j];
            File[] resources = null;

            if(resourceDir != null){
                resources = resourceDir.listFiles();
            }

            File resource = null;
            Object sunResource = null;
            if(resources != null){
                //Register All Connection Pools First
                for(int i=0; i<resources.length; i++ ){
                    resource = resources[i];
                    if((resource != null) && (!resource.isDirectory())){
                        sunResource = Utils.getResource(resource);
                        if(sunResource != null && sunResource instanceof JdbcConnectionPool){
                                JdbcConnectionPool connectionPoolBean =(JdbcConnectionPool)sunResource;
                                register(connectionPoolBean, mejb);
                        }
                    }
                }//Connection Pools
                
                //Register All DataSources
                for(int i=0; i<resources.length; i++ ){
                    resource = resources[i];
                    if((resource != null) && (!resource.isDirectory())){
                        sunResource = Utils.getResource(resource);
                        if(sunResource != null && sunResource instanceof JdbcResource){
                                JdbcResource datasourceBean = (JdbcResource)sunResource;
                                register(datasourceBean, mejb);
                        }
                    }
                }//DataSources
                
                //Register All Remaining Resources
                for(int i=0; i<resources.length; i++ ){
                    resource = resources[i];
                    if((resource != null) && (!resource.isDirectory())){
                        sunResource = Utils.getResource(resource);
                        if(sunResource != null){
                            if(sunResource instanceof PersistenceManagerFactoryResource){
                                PersistenceManagerFactoryResource pmBean = (PersistenceManagerFactoryResource)sunResource;
                                register(pmBean, mejb);
                            } else if(sunResource instanceof MailResource){
                                MailResource jmBean = (MailResource)sunResource;
                                register(jmBean, mejb);
                            } else if(sunResource instanceof JmsResource){
                                JmsResource jmsBean = (JmsResource)sunResource;
                                register(jmsBean, mejb);
                            }
                        }
                    }
                }//Remaining Resources
                
            }
        }
        System.out.println(bundle.getString("Msg_ProjResRegisterFinish")); //NOI18N
    }


    private static void register(JdbcConnectionPool resource, ServerInterface mejb){
        try{
            String resourceName = resource.getName();
            AttributeList attrList = Utils.getAttributes(resource);
            Properties props = Utils.getProperties(resource);
            
            if(!isResourceUpdated(resourceName, mejb, attrList, props, WizardConstants.__GetJdbcConnectionPool)){
                Object[] params = new Object[]{attrList, props, null};
                createResource(WizardConstants.__CreateCP, params, mejb);
            }
        }catch(Exception ex){
            String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
            System.out.println(errorMsg);
        }
    }


     private static void register(JdbcResource resource, ServerInterface mejb){
        try{
            String resourceName = resource.getJndiName();
            AttributeList attrList = Utils.getAttributes(resource);
            Properties props = Utils.getProperties(resource);
            
            if(!isResourceUpdated(resourceName, mejb, attrList, props, WizardConstants.__GetJdbcResource)){
                Object[] params = new Object[]{attrList, props, null};
                createResource(WizardConstants.__CreateDS, params, mejb);
            }
        }catch(Exception ex){
            String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
            System.out.println(errorMsg);
        }
    }


     private static void register(PersistenceManagerFactoryResource resource, ServerInterface mejb){
         try{
             String resourceName = resource.getJndiName();
             AttributeList attrList = Utils.getAttributes(resource);
             Properties props = Utils.getProperties(resource);
             
             if(!isResourceUpdated(resourceName, mejb, attrList, props, WizardConstants.__GetPMFResource)){
                 Object[] params = new Object[]{attrList, props, null};
                 createResource(WizardConstants.__CreatePMF, params, mejb);
             }
         }catch(Exception ex){
             String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
             System.out.println(errorMsg);
         }
     }


     private static void register(MailResource resource, ServerInterface mejb){
         try{
             String resourceName = resource.getJndiName();
             AttributeList attrList = Utils.getAttributes(resource);
             Properties props = Utils.getProperties(resource);
             
             if(!isResourceUpdated(resourceName, mejb, attrList, props, WizardConstants.__GetMailResource)){
                 Object[] params = new Object[]{attrList, props, null};
                 createResource(WizardConstants.__CreateMail, params, mejb);
             }
         }catch(Exception ex){
             String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
             System.out.println(errorMsg);
         }
     }


     private static void register(JmsResource resource, ServerInterface mejb){
         try{
             //FIXME: last param needs to be target ?? servername??
             String resourceName = resource.getJndiName();
             AttributeList attrList = Utils.getAttributes(resource);
             Properties props = Utils.getProperties(resource);
             if(!isResourceUpdated(resourceName, mejb, attrList, props, WizardConstants.__GetJmsResource)){
                 Object[] params = new Object[]{attrList, props, null};
                 createResource(WizardConstants.__CreateJMS, params, mejb);
             }
         }catch(Exception ex){
             String errorMsg = MessageFormat.format(bundle.getString( "Msg_RegFailure"), new Object[]{ex.getLocalizedMessage()}); //NOI18N
             System.out.println(errorMsg);
         }
     }


     private static void createResource(String operName, Object[] params, ServerInterface mejb) throws Exception{
        ///ServerInterface mejb = (ServerInterface)getManagement();
        if(mejb != null){
            String[] signature = new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};  //NOI18N
            try{
                ObjectName objName = new ObjectName(WizardConstants.MAP_RESOURCES);
                mejb.invoke(objName, operName, params, signature);
            }catch(Exception ex){
                throw new Exception(ex.getLocalizedMessage());
            }
        }
    }
    
    private static boolean isResourceUpdated(String resourceName, ServerInterface mejb, AttributeList attrList, Properties props, String operName ){  
        boolean isResUpdated = false;
        try{
            ObjectName objName = new ObjectName(WizardConstants.MAP_RESOURCES);
            ObjectName[] resourceObjects = null;
            if(operName.equals(WizardConstants.__GetPMFResource) || operName.equals(WizardConstants.__GetJmsResource)){
                String[] signature = new String[]{"java.lang.String"};  //NOI18N
                Object[] params = new Object[]{null};
                resourceObjects = (ObjectName[])  mejb.invoke(objName, operName, params, signature);
            }else{
                resourceObjects = (ObjectName[])  mejb.invoke(objName, operName, null, null);
            }
            if(resourceObjects != null){
                ObjectName resOnServer = null;
                if(operName.equals(WizardConstants.__GetJdbcConnectionPool))
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
                jndiName = resObj.getKeyProperty(WizardConstants.__JndiName);
            else
                jndiName = resObj.getKeyProperty(WizardConstants.__Name);
            
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
     
     private static Map getResourceAttributeNames(ObjectName objName, ServerInterface mejb) throws Exception {
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
     
}
