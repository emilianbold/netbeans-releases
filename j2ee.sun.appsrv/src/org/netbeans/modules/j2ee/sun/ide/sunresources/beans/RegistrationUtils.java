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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.sun.ide.sunresources.beans;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource;
import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;

/**
 *
 * @author Nitya Doraisamy
 */
public class RegistrationUtils {

    private static String POOL_EXTENSION = "_Base";
    private static String DELETE_POOL = "deleteJdbcConnectionPool";
    private static String DELETE_JDBC = "deleteJdbcResource";
    private static String DAS_SERVER_NAME = "server";
    
    public RegistrationUtils() {
    }
    
    public static void checkUpdateServerResources(ServerInterface mejb, java.io.File primaryFile) throws Exception {
        Resources resources = ResourceUtils.getResourcesGraph(primaryFile);
        HashMap serverPools = getServerConnectionPools(mejb);
        HashMap serverDatasources = getServerJdbcResources(mejb);
          
        JdbcConnectionPool[] pools = resources.getJdbcConnectionPool();
        JdbcResource[] dataSources = resources.getJdbcResource();
        
        //Delete datasources that are in this project
        HashMap dupJdbcResources = getProjectDatasources(serverDatasources, dataSources);
        deleteServerJdbcResources(dupJdbcResources, mejb);
        
        for(int i=0; i<pools.length; i++){           
            JdbcConnectionPool connectionPoolBean = pools[i];
            String newPoolName = connectionPoolBean.getName();
            
            //Is this pool registered on the server.
            if(serverPools.containsKey(newPoolName)){
                HashMap serverJdbcResources = getReferringDatasources(newPoolName, serverDatasources, mejb);               
                if(serverJdbcResources.size() > 0){
                    //Change this connectionPoolName
                    copyServerPool(serverPools, newPoolName, mejb);
                    updateExternalJdbcResource(serverJdbcResources, newPoolName, mejb);
                }
                deleteOldServerPool(newPoolName, mejb);
            }else{
                //delete pool.
                deleteOldServerPool(newPoolName, mejb);
            }
        }
    }

    public static HashMap getServerConnectionPools(ServerInterface mejb){
        HashMap pools = new HashMap();
        try {
            ObjectName configObjName = new ObjectName(WizardConstants.MAP_RESOURCES);
            ObjectName[] resourceObjects = (ObjectName[])  mejb.invoke(configObjName, WizardConstants.__GetJdbcConnectionPool, null, null);
            for(int i=0; i<resourceObjects.length; i++){
                ObjectName objName = resourceObjects[i];
                String poolName = (String)mejb.getAttribute(objName, "name"); //NOI18N
                pools.put(poolName, objName);
            } // for - each connection pool
        } catch (Exception ex) {
            //Unable to get server connection pools
        }
        return pools;
    }
    
    public static HashMap getServerJdbcResources(ServerInterface mejb){
        HashMap<String, ObjectName> datasources = new HashMap<String, ObjectName>();
        try {
            ObjectName configObjName = new ObjectName(WizardConstants.MAP_RESOURCES);
            ObjectName[] resourceObjects = (ObjectName[]) mejb.invoke(configObjName, WizardConstants.__GetJdbcResource, null, null);
            for(int i=0; i<resourceObjects.length; i++){
                ObjectName objName = resourceObjects[i];
                String jndiName = (String)mejb.getAttribute(objName, "jndi-name"); //NOI18N
                datasources.put(jndiName, objName);
            } // for - each datasource
        } catch (Exception ex) {
            //Unable to get server datasourcess
        }
        return datasources;
    }

    public static HashMap getReferringDatasources(String poolName, HashMap serverDatasources, ServerInterface mejb) {
        HashMap<String, ObjectName> datasources = new HashMap<String, ObjectName>();
        try{
            for(Iterator itr=serverDatasources.keySet().iterator(); itr.hasNext();){
                String jdbcName = (String)itr.next();
                ObjectName objName = (ObjectName)serverDatasources.get(jdbcName);
                
                String connpoolName = (String)mejb.getAttribute(objName, "pool-name"); //NOI18N
                if(connpoolName.equals(poolName)){
                    datasources.put(jdbcName, objName);
                }
            }
        }catch(Exception ex){}
        return datasources;
    }

    public static HashMap getProjectDatasources(HashMap serverJdbcResources, JdbcResource[] dataSources){
        HashMap<String, ObjectName> datasources = new HashMap<String, ObjectName>();
        for(int i=0; i<dataSources.length; i++){
            JdbcResource dsResource = (JdbcResource)dataSources[i];
            String dsName = dsResource.getJndiName();
            if(serverJdbcResources.containsKey(dsName)){
                datasources.put(dsName, (ObjectName)serverJdbcResources.get(dsName));
            }
        }
        return datasources;
    }
    
    public static void updateExternalJdbcResource(HashMap serverJdbcResources, String newPoolName, ServerInterface mejb){
        try{
            String updatedPoolName = newPoolName + POOL_EXTENSION;
            for(Iterator itr=serverJdbcResources.values().iterator(); itr.hasNext();){
                ObjectName dsObjName = (ObjectName)itr.next();
                Attribute poolNameAttr = new Attribute("pool-name", updatedPoolName);
                mejb.setAttribute(dsObjName, poolNameAttr);
            }
        }catch(Exception ex){
            //Could not update resource. 
        }
    }
    
    
    
    public static void copyServerPool(HashMap serverPools, String newPoolName, ServerInterface mejb){
        try{
            String updatedPoolName = newPoolName + POOL_EXTENSION;
            if(! serverPools.containsKey(updatedPoolName)){
                ObjectName serverPoolObj = (ObjectName)serverPools.get(newPoolName);
                Map attributeInfos = ResourceUtils.getResourceAttributeNames(serverPoolObj, mejb);
                attributeInfos.remove("name");
                String[] attrNames = (String[]) attributeInfos.keySet().toArray(new String[attributeInfos.size()]);
                AttributeList attrList = mejb.getAttributes(serverPoolObj, attrNames);
                Attribute nameAttr = new Attribute("name", updatedPoolName);
                attrList.add(nameAttr);
                          
                Properties props = new Properties();
                AttributeList propsList = (AttributeList)mejb.invoke(serverPoolObj, WizardConstants.__GetProperties, null, null);             
                for(int i=0; i<attrList.size(); i++){
                    Attribute propAttr = (Attribute)attrList.get(i);
                    String propName = propAttr.getName();
                    Object propValue = propAttr.getValue();
                    if(propValue != null){
                        props.put(propName, propValue);
                    }    
                }
                
                Object[] params = new Object[]{attrList, props, null};
                ResourceUtils.createResource(WizardConstants.__CreateCP, params, mejb);
            }
        }catch(Exception ex){  
            //Unable to copy pool
        }
    }
    
    public static void deleteOldServerPool(String newPoolName, ServerInterface mejb){
        try{
            ObjectName objName = new ObjectName(WizardConstants.MAP_RESOURCES);
            mejb.invoke(objName, DELETE_POOL, new Object[]{newPoolName, DAS_SERVER_NAME},
                    new String[]{"java.lang.String", "java.lang.String"} );
        }catch(Exception ex){
            //Unable to clean up existing duplicate pools
        }
    }
    
    public static void deleteServerJdbcResources(HashMap serverJdbcResources, ServerInterface mejb){
        try{
            ObjectName objName = new ObjectName(WizardConstants.MAP_RESOURCES);
            for(Iterator itr = serverJdbcResources.keySet().iterator(); itr.hasNext();){
                String jdbcName = (String)itr.next();
                mejb.invoke(objName, DELETE_JDBC, new Object[]{jdbcName, DAS_SERVER_NAME},
                        new String[]{"java.lang.String", "java.lang.String"} );
            }
        }catch(Exception ex){
            //Unable to clean up existing duplicate datasources
        }
    }
}

