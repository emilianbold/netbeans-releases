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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.sun.ide.controllers;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.enterprise.deploy.spi.DeploymentManager;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.j2ee.J2EEManagedObject;
import com.sun.appserv.management.j2ee.J2EETypes;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.ModuleConfig;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.ServerConfig;
import com.sun.appserv.management.config.ObjectTypeValues;
import java.io.File;

import java.util.ArrayList;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.bridge.apis.AppserverMgmtController;
import org.netbeans.modules.j2ee.sun.util.AppserverConnectionFactory;
import org.netbeans.modules.j2ee.sun.util.GUIUtils;
import org.netbeans.modules.j2ee.sun.util.NodeTypes;
import org.netbeans.modules.j2ee.sun.util.PluginRequestInterceptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */
public class ControllerUtil {
    
    private static final String DELIMINATOR = " ";
    private static String PROPERTIES_INTERFACE = "PropertiesAccess";
    private static String DESCRIPTION_INTERFACE = "Description";
    private static String ENABLED_INTERFACE = "Enabled";
    private static String RESOURCE_CONFIG_INTERFACE = "ResourceConfig";
    private static String MODULE_CONFIG_INTERFACE = "ModuleConfig";
    private static String J2EE_DEPLOYED_OBJ_INTERFACE = "J2EEDeployedObject"; 
    private static String OBJECT_TYPE_INTERFACE = "ObjectType";
    private static String NAME_ATTRIBUTE = "Name";
    private static String JNDI_NAME_ATTRIBUTE = "JNDIName";    
    
    private static final String CONFIG_OBJ_NAME = "com.sun.appserv:type=applications,category=config";
    private static final String SIP_CONFIG_MBEAN = "com.sun.appserv:type=sip-configs,category=config";
    
    private static final String SIP_MODULE_TYPE = "org.jvnet.glassfish.comms.deployment.backend.SipArchiveDeployer";
        
    private static Logger logger;
    private static Map j2eeTypeToConfigMap;
    
    private static final String DAS_SERVER_NAME = "server";
    
    static {
        logger = Logger.getLogger("org.netbeans.modules.j2ee.sun");
    }
    
    static {
        j2eeTypeToConfigMap = new HashMap();
        j2eeTypeToConfigMap.put(J2EETypes.J2EE_DOMAIN, XTypes.DOMAIN_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.J2EE_CLUSTER, XTypes.CLUSTER_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.J2EE_SERVER, 
                XTypes.STANDALONE_SERVER_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.JVM, XTypes.JAVA_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.J2EE_APPLICATION, 
                XTypes.J2EE_APPLICATION_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.EJB_MODULE, XTypes.EJB_MODULE_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.WEB_MODULE, XTypes.WEB_MODULE_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.APP_CLIENT_MODULE, 
                XTypes.APP_CLIENT_MODULE_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.RESOURCE_ADAPTER_MODULE, 
                XTypes.RAR_MODULE_CONFIG);
        j2eeTypeToConfigMap.put(J2EETypes.RESOURCE_ADAPTER, 
                XTypes.RESOURCE_ADAPTER_CONFIG);
     }

    
    /**
     * Private constructor to avoid instantiation.
     */
    private ControllerUtil() {
    }
    
    
    /**
     *
     *
     */
    public static AppserverMgmtController  getAppserverMgmtControllerFromDeployMgr(final DeploymentManager deployMgr) {
        AppserverConnectionSource con = createAppserverConnFromDeploymentMgr(deployMgr);
        if (con==null){
            return null; //no way to get a valid controller sinc the connection is bad: no password, or bad one
        }
        else {
            return new AppserverMgmtController(deployMgr, con);
        }
    }
    
    
    /**
     *
     *
     */
    public static AppserverConnectionSource  createAppserverConnFromDeploymentMgr(final DeploymentManager deployMgr) {
        AppserverConnectionSource connection = null;
        SunDeploymentManagerInterface sunDpmtMgr = 
                (SunDeploymentManagerInterface) deployMgr;

        try {    
           // connection = AppserverConnectionFactory.getAppserverConnection(
            connection = AppserverConnectionFactory.getHTTPAppserverConnection(
                sunDpmtMgr.getHost(), sunDpmtMgr.getPort(), 
                sunDpmtMgr.getUserName(), sunDpmtMgr.getPassword(), 
                sunDpmtMgr.isSecure());
            
            //next line is important to test if the connection is correct. If it is not, the expcetion is raised
            //so that we can return a null one.
           connection.getDomainRoot();

        } catch (Exception ioex) {
            connection=null;
            //GUIUtils.showError("Bad AppserverConnection:     "+ioex.getMessage());


        
        }
        return connection;
    }
    
    
    /**
     *
     *
     */
    public static MBeanServerConnection getMBeanServerConnWithInterceptor(
            final SunDeploymentManagerInterface sunDplymtIntrface,
            final AppserverConnectionSource connSource) {
        MBeanServerConnection conn = null;
        try {
            conn = connSource.getMBeanServerConnection(false);
        } catch (IOException io) {
            logger.log(Level.FINE, io.getMessage(), io);
        }
        MBeanServerConnection connWithInterceptor =
            new PluginRequestInterceptor(sunDplymtIntrface, conn);
        return connWithInterceptor;
    }
    
    
    /**
     *
     *
     */
    static public AMX getAMXComponentFromMap(Map map, String keyName) {
        for(Iterator itr = map.values().iterator(); itr.hasNext(); ) {            
            AMX component = (AMX)itr.next();
            if(component.getName().equals(keyName)) {
                return component;
            }
        }
        return null;
    }
    
    
    /**
     *
     */
    static public String[] getComponentNamesFromMap(Map map) {
       String [] names = new String[map.size()];
       int pos = 0;
       for(Iterator itr = map.values().iterator(); itr.hasNext(); ) {
           AMX component = (AMX)itr.next();
           names[pos] = component.getName();
           pos++;
       }
       return names;
    }
    
    /**
     *
     */
    static public String[] getComponentNamesFromSet(Set components) {
       String[] names = new String[components.size()];
       ObjectName[] objs = convertToObjNames(components);
       for(int i=0; i<objs.length; i++){
           ObjectName objName = objs[i];
           names[i] = objName.getKeyProperty("name"); //NOI18N
       }
       return names;
    }
    
    /**
     * Return a string representation of an array.
     */
    static public String arrayToString(Object [] array) {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < array.length; i++) {
            buffer.append(array[i]);
            buffer.append(DELIMINATOR);
        }
        return buffer.toString();
    }
    
    
    /**
     *
     *
     */
    public static Map getAllAttributes(final Class clazz, 
            final AMX amx, final List propsToIgnore, 
            final MBeanServerConnection conn, final String nodeType) {
        Map attrs = null;
        Set declaredAttrs =  
            extractAttributeNamesFromMethods(
                getGetterMethods(clazz.getDeclaredMethods()));
        Set inheritedAttrs = getExtraAttributes(clazz);
        declaredAttrs.addAll(inheritedAttrs);
        if(isJNDINameAbsent(declaredAttrs)) {
            declaredAttrs.add(NAME_ATTRIBUTE);
        }
        if(nodeType.equals(NodeTypes.WEB_APPLICATION)){
            //In this filter, the props in the list are displayed and the rest ignored.    
            attrs = getAttributeProperties(
                applyFilterToAttributeNamesForWeb(propsToIgnore,declaredAttrs), amx, conn);
        }else{
            Set names = applyFilterToAttributeNames(propsToIgnore, declaredAttrs);
            if(nodeType.equals(NodeTypes.APP_CLIENT_MODULE)){
                names.add("JavaWebStartEnabled"); //NOI18N
            }
            attrs =  getAttributeProperties(names, amx, conn);
        }    
        attrs = modifyEnabledProperty(attrs, amx);
        return attrs;
    }
    
    /**
     *
     *
     */
    private static Set getExtraAttributes(Class clazz) {
        Set attrSet = new HashSet();
        Class [] classes = clazz.getInterfaces();
        for(int i = 0; i < classes.length; i++) {
            if(isExtraInterface(getSimpleClassName(classes[i]))) {
                attrSet.addAll(extractAttributeNamesFromMethods(
                        getGetterMethods(classes[i].getDeclaredMethods())));
                
            }
            attrSet.addAll(getExtraAttributes(classes[i]));
        }
        return attrSet;
    }
    
    /**
     *
     */
    private static String getSimpleClassName(Class clazz) {
        String fullClassName = clazz.getName();
        int lastIndex = fullClassName.lastIndexOf(".");
        return fullClassName.substring(lastIndex + 1, 
            fullClassName.length()).trim();
    }
    
    /**
     *
     *
     */
    private static boolean isExtraInterface(String intrfaceName) {
        return (DESCRIPTION_INTERFACE.equals(intrfaceName) 
                || ENABLED_INTERFACE.equals(intrfaceName) 
                || PROPERTIES_INTERFACE.equals(intrfaceName) 
                || RESOURCE_CONFIG_INTERFACE.equals(intrfaceName)
                || J2EE_DEPLOYED_OBJ_INTERFACE.equals(intrfaceName)
                || MODULE_CONFIG_INTERFACE.equals(intrfaceName)
                || OBJECT_TYPE_INTERFACE.equals(intrfaceName)); 
    }
    
    
    /**
     *
     */
    private static boolean isJNDINameAbsent(Set attrs) {
        for(Iterator itr = attrs.iterator(); itr.hasNext(); ) {
            if(JNDI_NAME_ATTRIBUTE.equals(itr.next())) {
                return false;
            }
        }
        return true;
    }
    
    
    /**
     *
     */
    private static Set applyFilterToAttributeNames(
            final List propsToIgnore, final Set names) {
       if(propsToIgnore != null && propsInListAreStrings(propsToIgnore)) {
           for(Iterator itr = propsToIgnore.iterator(); itr.hasNext(); ) {
               String name = (String) itr.next();
               if(names.contains(name)) {
                   names.remove(name);
               }
           }
       }
       return names;
    }
    
    private static Set applyFilterToAttributeNamesForWeb(
    final List propsToAdd, final Set names) {
        Set attrNames = new HashSet();
        if(propsToAdd == null){
            return names;
        }
        if(propsToAdd != null && propsInListAreStrings(propsToAdd)) {
            for(Iterator itr = propsToAdd.iterator(); itr.hasNext(); ) {
                String name = (String) itr.next();
                if(names.contains(name)) {
                    attrNames.add(name);
                }
            }
        }
        return attrNames;
    }
    
    
    /**
     *
     */
    public static boolean propsInListAreStrings(List propsToIgnore) {
       for(Iterator itr = propsToIgnore.iterator(); itr.hasNext(); )  {
           if(!(itr.next() instanceof String)) {
                   return false;
           }
       }
       return true;
    }
    
    
    /**
     *
     *
     */
    private static Map getAttributeProperties(Set names, AMX amx, 
            MBeanServerConnection conn) {
        //get attribute info
        Map returnMap = new HashMap();
        final ObjectName on = Util.getObjectName(amx);
        //get the attribute info
        try {
            final MBeanInfo mi = conn.getMBeanInfo(on);
            final MBeanAttributeInfo[] mai = mi.getAttributes();
            for (int i = 0 ; i < mai.length ; i++) {
                if (mai[i] != null && names.contains(mai[i].getName())) {
                    //get the attribute from the mbean
                    try {
                        Object attrValue = conn.getAttribute(on, mai[i].getName());
                        //put it in the return map indexed by attribute
                        returnMap.put(new Attribute(mai[i].getName(), 
                                attrValue), mai[i]);
                    } catch(javax.management.AttributeNotFoundException ex) {
                        continue;
                    }
                }
            }
        } catch(Exception e) {
            logger.log(Level.FINE, e.getMessage(), e);
        }
        if(returnMap.size() == 0 || returnMap == null) {
            logger.log(Level.FINE, "The return Map in getAttrProperties is " +
                    "size 0!");
        }
        return returnMap;
    }
    
     public static Map getFilteredMBeanAttributes(Set names, ObjectName oName, MBeanServerConnection conn) {
        Map returnMap = new HashMap();
        try {
            final MBeanInfo mi = conn.getMBeanInfo(oName);
            final MBeanAttributeInfo[] mai = mi.getAttributes();
            for (int i = 0; i < mai.length; i++) {
                try {
                    if (mai[i] != null && (!names.contains(mai[i].getName()))) {
                        Object attrValue = conn.getAttribute(oName, mai[i].getName());
                        //put it in the return map indexed by attribute
                        returnMap.put(new Attribute(mai[i].getName(),
                                attrValue), mai[i]);
                    }
                } catch (javax.management.AttributeNotFoundException ex) {
                    continue;
                }
            }
        } catch(Exception e) {
            logger.log(Level.FINE, e.getMessage(), e);
        }
        if(returnMap.size() == 0 || returnMap == null) {
            logger.log(Level.FINE, "The return Map in getAttrProperties is " +
                    "size 0!");
        }
        return returnMap;
    }

    /**
     * 
     *
     */
    public static Set extractAttributeNamesFromMethods(final Method[] method) {
        final Set attrNames = new HashSet();
        for (int i = 0 ; i < method.length ; i++) {
            final String methodName = method[i].getName();
            final String name = 
                methodName.substring(methodName.indexOf("get")+3);
            attrNames.add(name);
        }
        return attrNames;
    }
    
    
    /**
     *
     *
     */
    public static Method[] getGetterMethods(Method[] methods) {
        final Vector v = new Vector();
        for (int i = 0 ; i < methods.length ; i++) {
            if (methods[i].getName().startsWith("get")
                    && !methods[i].getName().endsWith("Stats")) {
                v.add(methods[i]);
            }
        }
        final Method[] mm = new Method[v.size()];
        return (Method[]) v.toArray(mm);
    }
    
    
    /**
     *
     *
     */
    public static AMX getConfigPeer(final AMX amx) {
        J2EEManagedObject managedObj = (J2EEManagedObject) amx;
        AMX configPeer = managedObj.getConfigPeer();
        if(configPeer != null) {
            return configPeer;
        } else {
            String configXType = getConfigPeerAMXType(amx.getJ2EEType());
            QueryMgr queryMgr = amx.getDomainRoot().getQueryMgr();
            java.util.Set set = queryMgr.queryJ2EETypeSet(configXType);
            for(Iterator itr = set.iterator(); itr.hasNext(); ) {
                AMX config = (AMX) itr.next();
                if(config.getName().equals(amx.getName())) {
                    return config;
                }
            }
        }
       return null;
    }
    
    
    /**
     *
     *
     */
    public static Attribute setAttributeValue(AMX j2eeMod, AMX configPeer, String attrName, Object value,
            MBeanServerConnection conn) {
        Attribute modAttr = new Attribute(attrName, value);
        try {
            if(attrName.equals(NodeTypes.ENABLED) && (configPeer != null)){
                updateEnabled(configPeer, value);
            }else{
                ObjectName configOn = Util.getObjectName(configPeer);
                conn.setAttribute(configOn, modAttr);
            }
        } catch (RuntimeMBeanException e) {
            if(j2eeMod != null){
                ObjectName j2eeOn = Util.getObjectName(j2eeMod);
                modAttr = setAttrValue(j2eeOn, attrName, modAttr, conn);
            }else
                modAttr = null;
        } catch (Exception ex) {
            Object [] params = new Object[] {attrName, ex.getMessage()};
            GUIUtils.showError(
                getLocalizedString("unexpected_setAttr_except", params));
            modAttr = null;
        }
        return modAttr;
    }
    
    public static Attribute setAttributeValue(AMX amx, String attrName, Object value,
            MBeanServerConnection conn) {
        final ObjectName on = Util.getObjectName(amx);
        Attribute modAttr = new Attribute(attrName, value);
        try {
            if(attrName.equals(NodeTypes.ENABLED) && (amx != null))
                updateEnabled(amx, value);
            else
                conn.setAttribute(on, modAttr);
        } catch (RuntimeMBeanException e) {
            modAttr = setAttributeOnConfigPeer(amx, attrName, value, conn);
        } catch (Exception ex) {
            Object [] params = new Object[] {attrName, ex.getMessage()};
            GUIUtils.showError(
                getLocalizedString("unexpected_setAttr_except", params));
            modAttr = null;
        }
        return modAttr;
    }
    
    public static Attribute setAttributeValue(ObjectName oname, String attrName, Object value,
            MBeanServerConnection conn){
        Attribute modAttr = new Attribute(attrName, value);
        try {
             conn.setAttribute(oname, modAttr);
        } catch (Exception ex) {
            Object [] params = new Object[] {attrName, ex.getMessage()};
            GUIUtils.showError(
                getLocalizedString("unexpected_setAttr_except", params));
            modAttr = null;
        }
        return modAttr;
    
    }
    
    private static void updateEnabled(AMX amx, Object value){
        if(amx instanceof ResourceConfig){
            setResourceEnabled((ResourceConfig)amx, value);
        } else {
            if(amx instanceof ModuleConfig) {
                setModuleEnabled((ModuleConfig)amx, value);
            }
        }
    }
    
    private static Attribute setAttrValue(ObjectName on, String attrName, Attribute modAttr,
            MBeanServerConnection conn) {
        try {
            conn.setAttribute(on, modAttr);
        } catch (Exception ex) {
            Object [] params = new Object[] {attrName, ex.getMessage()};
            GUIUtils.showError(
                getLocalizedString("unexpected_setAttr_except", params));
            modAttr = null;
        }
        return modAttr;
    }
    
    private static final String CONST_SET_PROP_EXCEPT = "unexpected_setProp_except"; // NOI18N
    
    /**
     *
     *
     */
     public static void setPropertyValue(com.sun.appserv.management.config.ResourceConfig resConfig, Object[] props) {
        try {
            for(int i=0; i<props.length; i++){
                Attribute attr = (Attribute)props[i];
                String propValue = null;
                if(attr.getValue() != null){
                    propValue = attr.getValue().toString();
                }
                resConfig.setPropertyValue(attr.getName(), propValue);
            }
        } catch (RuntimeMBeanException e) {
            Object [] params = new Object[] {resConfig.getJNDIName(), e.getMessage()};
            GUIUtils.showError(
                getLocalizedString(CONST_SET_PROP_EXCEPT, params));
        } catch (Exception ex) {
            Object [] params = new Object[] {resConfig.getJNDIName(), ex.getMessage()};
            GUIUtils.showError(
                getLocalizedString(CONST_SET_PROP_EXCEPT, params));
        }
    }
    
     /**
      *
      *
      */
     public static void setPropertyValue(com.sun.appserv.management.config.JDBCConnectionPoolConfig resConfig, Object[] props) {
         try {
             for(int i=0; i<props.length; i++){
                 Attribute attr = (Attribute)props[i];
                 String propValue = null;
                 if(attr.getValue() != null){
                     propValue = attr.getValue().toString();
                 }
                 resConfig.setPropertyValue(attr.getName(), propValue);
             }
         } catch (RuntimeMBeanException e) {
             Object [] params = new Object[] {resConfig.getName(), e.getMessage()};
             GUIUtils.showError(
                     getLocalizedString(CONST_SET_PROP_EXCEPT, params));
         } catch (Exception ex) {
             Object [] params = new Object[] {resConfig.getName(), ex.getMessage()};
             GUIUtils.showError(
                     getLocalizedString(CONST_SET_PROP_EXCEPT, params));
         }
     }
     
     /**
      *
      *
      */
     public static void setPropertyValue(com.sun.appserv.management.config.ConnectorConnectionPoolConfig resConfig, Object[] props) {
         try {
             for(int i=0; i<props.length; i++){
                 Attribute attr = (Attribute)props[i];
                 String propValue = null;
                 if(attr.getValue() != null){
                     propValue = attr.getValue().toString();
                 }
                 resConfig.setPropertyValue(attr.getName(), propValue);
             }
         } catch (RuntimeMBeanException e) {
             Object [] params = new Object[] {resConfig.getName(), e.getMessage()};
             GUIUtils.showError(
                     getLocalizedString(CONST_SET_PROP_EXCEPT, params));
         } catch (Exception ex) {
             Object [] params = new Object[] {resConfig.getName(), ex.getMessage()};
             GUIUtils.showError(
                     getLocalizedString(CONST_SET_PROP_EXCEPT, params));
         }
     }
     
    /**
     *
     *
     */
    private static Attribute setAttributeOnConfigPeer(final AMX amx, 
            final String attrName, final Object value, 
            final MBeanServerConnection conn) {
        AMX configPeer = getConfigPeer(amx);
        Attribute modAttr = new Attribute(attrName, value);
        try {
            final ObjectName on = Util.getObjectName(configPeer);
            conn.setAttribute(on, modAttr);
        } catch (RuntimeMBeanException e) {
            Object [] params = new Object[] {attrName, e.getMessage()};
            GUIUtils.showError(
                getLocalizedString("unexpected_setAttrConfigPeer_except", params));
            modAttr = null;
        } catch (Exception ex) {
            Object [] params = new Object[] {attrName, ex.getMessage()};
            GUIUtils.showError(
                getLocalizedString("unexpected_setAttrConfigPeer_except", params));
            modAttr = null;
        }
        return modAttr;
    }
    
    /**
     * Filters all system modules. Deals with a map containing 
     * com.sun.appserv.management.j2ee.J2EEModule or Subinterfaces,
     * basically JSR 77 objects
     * Use getDeployedObjects for filtering config objects 
     * ie.com.sun.appserv.management.config.ModuleConfig 
     * @param allModules - All modules of type com.sun.appserv.management.j2ee.J2EEModule 
     * or Subinterfaces
     * @return All the non-system related modules.
     */
    protected static Map stripOutSystemApps(final Map allModules) {
        Map deployedObjects = new HashMap();
        for (Iterator it = allModules.values().iterator(); it.hasNext();) {
            J2EEManagedObject j2eeModule = (J2EEManagedObject) it.next();
            ModuleConfig appConfig = (ModuleConfig) j2eeModule.getConfigPeer();
            if ((appConfig != null) && (ObjectTypeValues.USER.equals(appConfig.getObjectType()))) {
                deployedObjects.put(j2eeModule.getName(), j2eeModule);
            }
        }
        return deployedObjects;
    }
    
    
    /**
     *
     */
    public static String getConfigPeerAMXType(final String j2eeType) {
        return (String) j2eeTypeToConfigMap.get(j2eeType);
    }
    
    
    
    /**
      * Checks to see if the appserver is suspended in debug mode. If the 
      * server is suspended, then a popup error window is displayed and a 
      * java.lang.RuntimeException is thrown to avoid IDE hanging.
      *
      * @param dplymtMgr The DeploymentManager used to determining the status
      *        of the appserver.
      */
     public static void checkIfServerInDebugMode(final DeploymentManager dplymtMgr) {
        SunDeploymentManagerInterface sunDpmtMgr =
                (SunDeploymentManagerInterface) dplymtMgr;
        if(sunDpmtMgr != null) {
            if(sunDpmtMgr.isSuspended()) {
                GUIUtils.showInformation(
                    getLocalizedString("server_dbg_mode_notify"));
                throw new RuntimeException(
                    getLocalizedString("server_dbg_mode_notify"));
            } 
        }
    }

     
    /**
     *
     *
     */
     public static Map getLogAttributes(final AMX config, final Map propNames, final MBeanServerConnection conn){
            Map returnMap = new HashMap();
            final ObjectName on = Util.getObjectName(config);
            //get the attribute info
            try {
                final MBeanInfo mi = conn.getMBeanInfo(on);
                final MBeanAttributeInfo[] mai = mi.getAttributes();
                for (int i = 0 ; i < mai.length ; i++) {
                    if (mai[i] != null && propNames.containsKey(mai[i].getName())) {
                        //get the attribute from the mbean
                        try {
                            Object attrValue = conn.getAttribute(on, mai[i].getName());
                            //put it in the return map indexed by attribute
                            returnMap.put(new Attribute(mai[i].getName(),
                                    attrValue), mai[i]);
                        } catch(javax.management.AttributeNotFoundException ex) {
                            continue;
                        }
                    }
                }
            } catch(Exception e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
            if(returnMap.size() == 0 || returnMap == null) {
                logger.log(Level.FINE, "The return Map in getAttrProperties is " +
                        "size 0!");
            }
            return returnMap;
     }
     
    /**
     *
     *
     */
    private static String getLocalizedString(final String bundleStrProp) {
        return NbBundle.getMessage(ControllerUtil.class, 
                bundleStrProp);
    }
    
    
    /**
     *
     *
     */
    private static String getLocalizedString(final String bundleStrProp,
            final Object[] params) {
        return NbBundle.getMessage(ControllerUtil.class, 
                bundleStrProp, params);
    }
    
    public static boolean isGlassFish(DeploymentManager dm){
	//now test for AS 9 (J2EE 5.0) which should work for this plugin
        File candidate = ((SunDeploymentManagerInterface)dm).getPlatformRoot();
        File as9 = new File(candidate.getAbsolutePath()+
                "/lib/dtds/sun-web-app_2_5-0.dtd");                             //NOI18N
        return as9.exists();
    }
    
    public static ObjectName[] getSubComponentsFromConfig(String modName, MBeanServerConnection conn){
        String[] subComponents = new String[] {};
        try{
            ObjectName oName = new ObjectName(CONFIG_OBJ_NAME);
            Object[] params = {modName};
            String[] signature = {"java.lang.String"};
            subComponents = (String[])conn.invoke(oName, "getModuleComponents", params, signature);
        }catch(Exception ex){}           
        return convertToObjNames(subComponents);
    }
    
    public static ObjectName[] getSIPComponents(MBeanServerConnection conn){
        ObjectName[] subComponents = new ObjectName[] {};
        try{
            ObjectName oName = new ObjectName(CONFIG_OBJ_NAME);
            Object[] params = null;
            String[] signature = null;
            subComponents = (ObjectName[])conn.invoke(oName, "getExtensionModule", params, signature);
            
            // This would give all the extension modules. Need to get the sip modules only.
            if ((subComponents != null) && (subComponents.length > 0)) {
                ArrayList list = new ArrayList();
                for (ObjectName child : subComponents) {
                    String moduleType = (String) conn.getAttribute(child, "module-type");
                    if (SIP_MODULE_TYPE.equals(moduleType)) {
                        AttributeList props = (AttributeList) conn.invoke(child, "getProperties", null, null);
                        //boolean converged = Boolean.parseBoolean((String) getPropertyValue(props, "isConverged"));
                        list.add(child);
                    }
                }
                if (list.size() > 0) {
                    subComponents = (ObjectName[])list.toArray();
                }
            }
        }catch(Exception ex){ 
            logger.log(Level.FINE, ex.getMessage(), ex);
        }           
        return subComponents;
    }
    
    protected static String isConvergedSIP(ObjectName objName, MBeanServerConnection conn){
        String sipType = NodeTypes.SIPAPP_CONVERGED;
        boolean converged = Boolean.parseBoolean((String) getPropertyValue(objName, NodeTypes.SIPAPP_CONVERGED_PROP, conn));
        if(! converged){
            sipType = NodeTypes.SIPAPP;
        }
        return sipType;
    }
    
    private static ObjectName[] convertToObjNames(String[] objNames){
        try{
            ObjectName[] compNames = new ObjectName[objNames.length];
            for(int i=0; i<objNames.length; i++){
                ObjectName name = new ObjectName(objNames[i]);
                compNames[i] = name;
            }
            return compNames;
        }catch(Exception ex){
            return new ObjectName[0];
        }            
    }
     
    private static ObjectName[] convertToObjNames(Set objNames){
        try{
            ObjectName[] compNames = new ObjectName[objNames.size()];
            int pos = 0;
            for(Iterator it = objNames.iterator(); it.hasNext();){
                ObjectName name = new ObjectName(it.next().toString());
                compNames[pos] = name;
                pos++;
            }
            return compNames;
        }catch(Exception ex){
            return new ObjectName[0];
        }            
    }
    
    private static void setResourceEnabled(ResourceConfig resConfig, Object value){
        boolean val = Boolean.valueOf(value.toString()).booleanValue();
        ResourceRefConfig config = getResourceRefConfig(resConfig);
        if(config != null) {
            config.setEnabled(val);
        }
    }
    
    private static void setModuleEnabled(ModuleConfig modConfig, Object value){
        boolean val = Boolean.valueOf(value.toString()).booleanValue();
        DeployedItemRefConfig config = getDeployedItemRefConfig(modConfig);
        if(config != null) {
            config.setEnabled(val);
        }
    }
    
    private static ResourceRefConfig getResourceRefConfig(AMX appConfig){
        String appName = appConfig.getName();
        ResourceRefConfig itemName = (ResourceRefConfig)getDASConfig(appConfig).getResourceRefConfigMap().get(appName);
        return itemName;
    }
    
    private static ServerConfig getDASConfig(AMX appConfig){
        Map serverConfigs = getServerInstancesMap(appConfig);
        ServerConfig serverConfig = (ServerConfig)serverConfigs.get(DAS_SERVER_NAME);
        return serverConfig;
    }
    
    public static Map modifyEnabledProperty(Map j2eeProps, AMX configPeer){
        Map modProps = j2eeProps;
        for(Iterator itr = j2eeProps.keySet().iterator(); itr.hasNext(); ) {
            Attribute attr = (Attribute) itr.next();
            if(attr.getName().equals(NodeTypes.ENABLED)){
                MBeanAttributeInfo info = (MBeanAttributeInfo) j2eeProps.get(attr);
                Boolean value = (Boolean)attr.getValue();
                boolean attrVal = calculateIsEnabled(configPeer, value.booleanValue());
                Attribute enabled = new Attribute(NodeTypes.ENABLED, Boolean.valueOf(attrVal));
                modProps.remove(attr);
                modProps.put(enabled, info);
                break;
            }
        }
        return modProps;
    }
    
    protected static boolean calculateIsEnabled(AMX appConfig, boolean configEnabled){
        boolean isEnabled = configEnabled;
        boolean refEnabled = configEnabled;
        if(appConfig instanceof ResourceConfig) {
            ResourceRefConfig itemName = getResourceRefConfig(appConfig);
            if(itemName != null) {
                refEnabled = itemName.getEnabled();
            }
        } else {
            DeployedItemRefConfig itemName = getDeployedItemRefConfig(appConfig);
            if(itemName != null) {
                refEnabled = itemName.getEnabled();
            }
        }
        if(!configEnabled || !refEnabled) {
            isEnabled = false;
        }
        return isEnabled;
    }
    
    protected static DeployedItemRefConfig getDeployedItemRefConfig(AMX appConfig){
        String appName = appConfig.getName();
        DeployedItemRefConfig itemName = (DeployedItemRefConfig)getDASConfig(appConfig).getDeployedItemRefConfigMap().get(appName);
        return itemName;
    }
  
    public static String[] getServerTargets(AMX amx){
        return getComponentNamesFromMap(getServerInstancesMap(amx));
    }
    
    public static Map getServerInstancesMap(AMX amx) {
       Map serverConfigs = amx.getDomainRoot().getDomainConfig().getServerConfigMap();
       return serverConfigs;
    } 
    
    public static Map getStandaloneServerInstancesMap(AMX amx) {
       Map serverConfigs = amx.getDomainRoot().getDomainConfig().getStandaloneServerConfigMap();
       return serverConfigs;
    }
    
    public static List<String> getDeployedTargets(AMX amx, boolean isApp, MBeanServerConnection conn) throws Exception {
        List<String> targetList = new ArrayList();
        Map clusterConfigs = amx.getDomainRoot().getDomainConfig().getClusterConfigMap();
        if (clusterConfigs.size() > 0) {
            String appName = amx.getName();
            String objectName = (isApp) ? "com.sun.appserv:type=applications,category=config" : "com.sun.appserv:type=resources,category=config"; //NOI18N
            String[] params = new String[]{appName};
            String[] types = new String[]{"java.lang.String"}; //NOI18N
            ObjectName[] refs = (ObjectName[]) conn.invoke(new ObjectName(objectName), "listReferencees", params, types); //NOI18N
            for (int i = 0; i < refs.length; i++) {
                targetList.add(refs[i].getKeyProperty("name")); //NOI18N
            }
        }

        return targetList;
    }
    
    /**
     * Filters all system modules. Deals with a map containing 
     * com.sun.appserv.management.config.ModuleConfig or Subinterfaces,
     * basically Config objects
     * Use stripOutSystemApps for filtering config objects 
     * ie.com.sun.appserv.management.j2ee.J2EEModule 
     * @param allModules - All modules of type com.sun.appserv.management.config.ModuleConfig 
     * or Subinterfaces
     * @return All the non-system related modules.
     */
    protected static Map getDeployedObjects(final Map allObjects) {
        Map deployedObjects = new HashMap();
        for (Iterator it = allObjects.values().iterator(); it.hasNext();) {
            Object configObj = it.next();
            if (!((configObj instanceof AMXConfig) || (configObj instanceof ModuleConfig))) {
                continue;
            }
            if (configObj instanceof ModuleConfig) {
                ModuleConfig appConfig = (ModuleConfig) configObj;
                if (ObjectTypeValues.USER.equals(appConfig.getObjectType())) {
                    deployedObjects.put(appConfig.getName(), appConfig);
                }
            }else if(configObj instanceof AMXConfig){
                AMXConfig appConfig = (AMXConfig) configObj;
                deployedObjects.put(appConfig.getName(), appConfig);
            }
        }
        return deployedObjects;
    }
    
    public static boolean isSIPEnabled(MBeanServerConnection conn) {
        boolean enabled = false;
        try {
            ObjectName objectName = new ObjectName(SIP_CONFIG_MBEAN);
            Object mbeanInstance = conn.getMBeanInfo(objectName);
            enabled = true;
        } catch (InstanceNotFoundException ex) {
            enabled = false;
        } catch (Exception ex) {
            logger.log(Level.FINE, ex.getMessage(), ex);
        }
        return enabled;
    }
     
    public static Object getPropertyValue(ObjectName objName, String propName, MBeanServerConnection conn)  {
        try {
            AttributeList props = (AttributeList) conn.invoke(objName, "getProperties", null, null);
            for (Iterator it = props.iterator(); it.hasNext();) {
                Attribute attribute = (Attribute) it.next();
                String name = attribute.getName();
                if (name.equals(propName)) {
                    return attribute.getValue();
                }
            }
            return null;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    /**
     * Returns the value of an attribute as a String
     * @param objName - ObjectName of component
     * @param attrName - Name of attribute
     * @param conn - MBeanServerConnection
     * @return value of attribute (java.lang.String).
     */
    public static String getAttributeValue(ObjectName objName, String attrName, MBeanServerConnection conn) {
        String strValue = ""; //N0I18N
        try {
            Object attrValue = conn.getAttribute(objName, attrName);
            if(attrValue != null) {
                strValue = attrValue.toString();
            }        
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return strValue;
    } 
      
    protected static boolean isModEnabled(ObjectName objName, MBeanServerConnection conn){
        String val = getAttributeValue(objName, "enabled", conn);
        boolean isEnabled = Boolean.valueOf(val.toString()).booleanValue();
        return isEnabled;
    }
        
}


