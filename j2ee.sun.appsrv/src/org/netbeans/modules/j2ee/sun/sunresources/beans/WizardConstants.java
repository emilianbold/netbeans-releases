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
 * WizardConstants.java
*
 * Created on October 15, 2002, 12:24 PM
 */

package org.netbeans.modules.j2ee.sun.sunresources.beans;

import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 *
 * @author  shirleyc
 */
public interface WizardConstants {
    public static final ResourceBundle openideBundle = NbBundle.getBundle ("org.openide.Bundle"); // NOI18N
        
    public static final String __CreateNewCP = "createNewCP";
    public static final String __CreateNewDS = "createNewDS";
    
    //common 
    public static final String __General = "general";    
    public static final String __Properties = "properties";
    public static final String __PropertiesURL = "propertiesUrl";
    public static final String __RegisterResource = "register-resource";
    
    //jdbc-connection-pool
    public static final String __Name = "name";
    public static final String __DatasourceClassname = "datasource-classname";
    public static final String __XADatasourceClassname = "datasource-classname-xa";
    public static final String __ResType = "res-type";
    public static final String __SteadyPoolSize = "steady-pool-size";
    public static final String __MaxPoolSize = "max-pool-size";
    public static final String __MaxWaitTimeInMillis = "max-wait-time-in-millis";
    public static final String __PoolResizeQuantity = "pool-resize-quantity";
    public static final String __IdleTimeoutInSeconds = "idle-timeout-in-seconds";
    public static final String __TransactionIsolationLevel = "transaction-isolation-level";
    public static final String __IsIsolationLevelGuaranteed = "is-isolation-level-guaranteed";
    public static final String __IsConnectionValidationRequired = "is-connection-validation-required";
    public static final String __ConnectionValidationMethod = "connection-validation-method";
    public static final String __ValidationTableName = "validation-table-name";
    public static final String __FailAllConnections = "fail-all-connections";
    public static final String __Description = "description";
    public static final String __JdbcConnectionPool = "jdbc-connection-pool"; 
        
    public static final String __DatabaseVendor = "database-vendor";
    public static final String __DatabaseName = "databaseName";
    public static final String __Url = "URL";
    public static final String __User = "User";
    public static final String __Password = "Password";
    public static final String __NotApplicable = "NA";
    public static final String __IsXA = "isXA";  
    public static final String __IsCPExisting = "is-cp-existing";
    
    //jdbc-connection-pool Bean
    //Also contains __Name & __Description
    public static final String __CPdsClass = "dsClass";
    public static final String __CPresType = "resType";
    public static final String __CPsteadyPoolSize = "steadyPoolSize";
    public static final String __CPmaxPoolSize = "maxPoolSize";
    public static final String __CPmaxWaitTimeMilli = "maxWaitTimeMilli";
    public static final String __CPpoolResizeQty = "poolResizeQty";
    public static final String __CPidleIimeoutSecond = "idleIimeoutSecond";
    public static final String __CPtranxIsoLevel = "tranxIsoLevel";
    public static final String __CPisIsoLevGuaranteed = "isIsoLevGuaranteed";
    public static final String __CPisConnValidReq = "isConnValidReq";
    public static final String __CPconnValidMethod = "connValidMethod";
    public static final String __CPvalidationTableName = "validationTableName";
    public static final String __CPfailAllConns = "failAllConns";
    public static final String __ExtraParams = "extraParams";
    
    //CommonAttributesPanel
    static final String[] COMMON_ATTR_INTEGER = {"Steady Pool Size:", "Max Pool Size:", "Max Wait time:", "Pool Resize Quantity:", "Idle Timeout (secs):" }; //NOI18N
    
    //jdbc-resource
    //Contains __Description
    public static final String __JndiName = "jndi-name";
    public static final String __PoolName = "pool-name";
    public static final String __JdbcObjectType = "object-type";
    public static final String __Enabled = "enabled";
    public static final String __JdbcResource = "jdbc-resource";
    
    //For Beans 
    public static final String __BeanjndiName = "jndiName";
    public static final String __BeanisEnabled = "isEnabled";
    
    //jdbc-resource Bean
    //Also contains __BeanjndiName & __BeanisEnabled and __Description 
    public static final String __DSconnPoolName = "connPoolName";
    public static final String __DSresType = "resType";
        
    //persistence-manager-factory
    //Contains __BeanjndiName and __BeanisEnabled and __Description
    public static final String __FactoryClass = "factory-class";
    public static final String __JdbcResourceJndiName = "jdbc-resource-jndi-name";
    public static final String __PersistenceManagerFactoryResource = "persistence-manager-factory-resource";
    
    //persistence-manager-factory Bean
    //Also contains  __BeanjndiName and __BeanisEnabled and __Description 
    public static final String __PMFfactoryClass = "factoryClass";
    public static final String __PMFdatasourceJndiName = "datasourceJndiName";
    
    
    //mail-resource
    //Contains __JndiName and __Enabled and __Description   
    public static final String __StoreProtocol = "store-protocol";
    public static final String __StoreProtocolClass = "store-protocol-class";
    public static final String __TransportProtocol = "transport-protocol";
    public static final String __TransportProtocolClass = "transport-protocol-class";
    public static final String __Host = "host";
    public static final String __MailUser = "user";
    public static final String __From = "from";
    public static final String __Debug = "debug";
    public static final String __MailResource = "mail-resource";
    
    //mail-resource Bean
    //Contains __BeanjndiName and __BeanisEnabled and __Description   
    public static final String __MAILstoreProt = "storeProt";
    public static final String __MAILstoreProtClass = "storeProtClass";
    public static final String __MAILtransProt = "transProt";
    public static final String __MAILtransProtClass = "transProtClass";
    public static final String __MAILhostName = "hostName";
    public static final String __MAILuserName = "userName";
    public static final String __MAILfromAddr = "fromAddr";
    public static final String __MAILisDebug = "isDebug";
    
    
    //jms-resource
    //Contains __JndiName, __ResType, __Enabled, __Properties, __Description
    public static final String __JmsResource = "jms-resource";
    public static final String __Properties2 = "properties2";
    
    //jms-resource Bean
    //Contains __BeanjndiName and __BeanisEnabled and __Description   
    public static final String __JMSResType = "resType";
    
    public static final String __JavaMessageJndiName = "jndi_name";
    public static final String __JavaMessageResType = "res_type";
    
    //Default Names for the resources
    public static final String __ConnectionPoolResource = "connectionPool";
    public static final String __JDBCResource = "datasource";
    public static final String __JMSResource = "jms";
    public static final String __MAILResource = "mail";
    public static final String __PersistenceResource = "persistence";
    public static final String __DynamicWizPanel = "dynamicPanel"; //to identify ds & cp created dynamically
    public static final String __SunResourceExt = "sun-resource";
    
    public static final String __ResourceType = "ResourceType";
    
    //First Step - temporary workaround
    public static final String __FirstStepChoose = "Choose ...";
    //Resource Folder
    public static final String __SunResourceFolder = "setup";
}
