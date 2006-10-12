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

package org.netbeans.modules.j2ee.sun.dd.api;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean;
import org.netbeans.modules.schema2beans.Common;
import org.xml.sax.*;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.impl.ejb.SunEjbJarProxy;

import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.impl.web.SunWebAppProxy;

import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.impl.app.SunApplicationProxy;

import org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources;
import org.netbeans.modules.j2ee.sun.dd.impl.serverresources.ResourcesProxy;
        
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.impl.client.SunApplicationClientProxy;

import org.netbeans.modules.j2ee.sun.dd.impl.DTDRegistry;

/**
 * Provides access to Deployment Descriptor root objects.
 *
 * @author  Milan Kuchtiak
 */

public final class DDProvider {
    // !PW FIXME refer to DTDRegistry file directly, or at least map to it, rather than redeclaring.
    private static final String EJB_30_90_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 9.0 EJB 3.0//EN"; //NOI18N     "sun-ejb-jar_3_0-0.dtd"
    private static final String EJB_21_81_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 8.1 EJB 2.1//EN"; //NOI18N
    private static final String EJB_21_80_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 8.0 EJB 2.1//EN"; //NOI18N
    private static final String EJB_20_70_DOCTYPE_SUNONE = "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 EJB 2.0//EN"; //NOI18N   "sun-ejb-jar_2_0-0.dtd" ,
    private static final String EJB_21_80_DOCTYPE_SUNONE = "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 EJB 2.1//EN"; //NOI18N    "sun-ejb-jar_2_1-0.dtd" , ///[THIS IS DEPRECATED]

    private static final String WEB_25_90_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Servlet 2.5//EN"; //NOI18N  "sun-web-app_2_5-0"
    private static final String WEB_21_81_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Servlet 2.4//EN"; //NOI18N
    private static final String WEB_21_80_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Servlet 2.4//EN"; //NOI18N
    private static final String WEB_20_70_DOCTYPE_SUNONE =  "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Servlet 2.3//EN" ; //NOI18N   "sun-web-app_2_3-0.dtd" ,
    private static final String WEB_21_80_DOCTYPE_SUNONE =  "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Servlet 2.4//EN"; //NOI18N    "sun-web-app_2_4-0.dtd" , ///[THIS IS DEPRECATED]
    
    private static final String APP_50_90_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Java EE Application 5.0//EN"; //NOI18N     "sun-application_5_0-0.dtd" 
    private static final String APP_14_81_DOCTYPE =  "-//Sun Microsystems, Inc.//DTD Application Server 8.1 J2EE Application 1.4//EN"; //NOI18N     "sun-application_1_4-0.dtd" 
    private static final String APP_14_80_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 8.0 J2EE Application 1.4//EN"; //NOI18N      "sun-application_1_4-0.dtd" 
    private static final String APP_13_70_DOCTYPE_SUNONE   =  "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 J2EE Application 1.3//EN"; //NOI18N  "sun-application_1_3-0.dtd" 
    private static final String APP_14_80_DOCTYPE_SUNONE = "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 J2EE Application 1.4//EN"; //NOI18N    "sun-application_1_4-0.dtd"  ///[THIS IS DEPRECATED]
    
    private static final String APPCLIENT_50_90_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Application Client 5.0//EN"; //NOI18N  "sun-application-client_5_0-0.dtd"
    private static final String APPCLIENT_14_81_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client 1.4//EN"; //NOI18N  "sun-application-client_1_4-1.dtd" 
    private static final String APPCLIENT_14_80_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Application Client 1.4//EN"; //NOI18N  "sun-application-client_1_4-0.dtd" 
    private static final String APPCLIENT_14_80_DOCTYPE_SUNONE = "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Application Client 1.4//EN"; //NOI18N "sun-application-client_1_4-0.dtd"  [THIS IS DEPRECATED]
    private static final String APPCLIENT_13_70_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client 1.3//EN"; //NOI18N  "sun-application-client_1_3-0.dtd" 
   
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Connector 1.0//EN"               , "sun-connector_1_0-0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client Container 1.0//EN" 	, "sun-application-client-container_1_0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 OR Mapping //EN"                 , "sun-cmp-mapping_1_0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.0 OR Mapping//EN"                          , "sun-cmp-mapping_1_1.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.1 OR Mapping//EN"                          , "sun-cmp-mapping_1_2.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Application Client Container //EN" 	, "sun-application-client-container_1_0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client Container //EN" 	, "sun-application-client-container_1_1.dtd" ,
    
    
    
    
    
    
    
    private static final DDProvider ddProvider = new DDProvider();
    private Map ddMap;
    
    /** Creates a new instance of DDProvider */
    private DDProvider() {
        //ddMap=new java.util.WeakHashMap(5);
        ddMap = new java.util.HashMap(5);
    }
    
    /**
    * Accessor method for DDProvider singleton
    * @return DDProvider object
    */
    public static DDProvider getDefault() {
        return ddProvider;
    }
    
     /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the sun-ejb-jar.xml file
     * @return Ejb object - root of the deployment descriptor bean graph
     */    
    public SunEjbJar getEjbDDRoot(InputSource is) throws IOException, SAXException {
        DDParse parse = parseDD(is);
        SunEjbJar ejbRoot = createEjbJar(parse);
        SunEjbJarProxy proxy = new SunEjbJarProxy(ejbRoot, ejbRoot.getVersion().toString());
        setEjbProxyErrorStatus(proxy, parse);
        return proxy;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the sun-web.xml file
     * @return Web object - root of the deployment descriptor bean graph
     */    
    public SunWebApp getWebDDRoot(InputSource is) throws IOException, SAXException, DDException {
        DDParse parse = parseDD(is);
        return processWebAppParseTree(parse);
    }

    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is stream representing the sun-web.xml file
     * @return Web object - root of the deployment descriptor bean graph
     */    
    public SunWebApp getWebDDRoot(InputStream is) throws IOException, SAXException, DDException {
        DDParse parse = parseDD(is);
        return processWebAppParseTree(parse);
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param doc XML document representing the sun-web.xml file
     * @return Web object - root of the deployment descriptor bean graph
     */    
    public SunWebApp getWebDDRoot(org.w3c.dom.Document doc) throws DDException {
        DDParse parse = new DDParse(doc, null);
        return processWebAppParseTree(parse);
    }
    
    private SunWebApp processWebAppParseTree(DDParse parse) throws DDException {
        SunWebApp webRoot = createWebApp(parse);
        SunWebAppProxy proxy = new SunWebAppProxy(webRoot, webRoot.getVersion().toString());
        setWebProxyErrorStatus(proxy, parse);
        return proxy;
    }    
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the sun-application.xml file
     * @return Application object - root of the deployment descriptor bean graph
     */    
    public SunApplication getAppDDRoot(InputSource is) throws IOException, SAXException {
        DDParse parse = parseDD(is);
        SunApplication appRoot = createApplication(parse);
        SunApplicationProxy proxy = new SunApplicationProxy(appRoot, appRoot.getVersion().toString());
        setAppProxyErrorStatus(proxy, parse);
        return proxy;
    }
    
    /**
     * Returns the root of deployment descriptor bean graph for java.io.File object.
     *
     * @param is source representing the sun-application-client.xml file
     * @return Application object - root of the deployment descriptor bean graph
     */    
    public SunApplicationClient getAppClientDDRoot(InputSource is) throws IOException, SAXException {
        DDParse parse = parseDD(is);
        SunApplicationClient appClientRoot = createApplicationClient(parse);
        SunApplicationClientProxy proxy = new SunApplicationClientProxy(appClientRoot, appClientRoot.getVersion().toString());
        setAppClientProxyErrorStatus(proxy, parse);
        return proxy;
    }
        
    // PENDING j2eeserver needs BaseBean - this is a temporary workaround to avoid dependency of web project on DD impl
    /**  Convenient method for getting the BaseBean object from CommonDDBean object
     *
     */
    public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean bean) {
        if (bean instanceof org.netbeans.modules.schema2beans.BaseBean) 
            return (org.netbeans.modules.schema2beans.BaseBean)bean;
        else if (bean instanceof SunEjbJarProxy) 
            return (org.netbeans.modules.schema2beans.BaseBean) ((SunEjbJarProxy)bean).getOriginal();
        else if (bean instanceof SunWebAppProxy) 
            return (org.netbeans.modules.schema2beans.BaseBean) ((SunWebAppProxy)bean).getOriginal();
        else if (bean instanceof SunApplicationProxy) 
            return (org.netbeans.modules.schema2beans.BaseBean) ((SunApplicationProxy)bean).getOriginal();
        else if (bean instanceof SunApplicationClientProxy) 
            return (org.netbeans.modules.schema2beans.BaseBean) ((SunApplicationClientProxy)bean).getOriginal();
        return null;
    }

    private static void setEjbProxyErrorStatus(SunEjbJarProxy ejbJarProxy, DDParse parse) {
        SAXParseException error = parse.getWarning();
        ejbJarProxy.setError(error);
        /*if (error!=null) {
            ejbJarProxy.setStatus(SunEjbJar.STATE_INVALID_PARSABLE);
        } else {
            ejbJarProxy.setStatus(SunEjbJar.STATE_VALID);
        }*/
    }
    
    private static void setAppProxyErrorStatus(SunApplicationProxy appProxy, DDParse parse) {
        SAXParseException error = parse.getWarning();
        appProxy.setError(error);
        /*if (error!=null) {
            appProxy.setStatus(SunApplication.STATE_INVALID_PARSABLE);
        } else {
            appProxy.setStatus(SunApplication.STATE_VALID);
        }*/
    }
    
    private static void setWebProxyErrorStatus(SunWebAppProxy webProxy, DDParse parse) {
        SAXParseException error = parse.getWarning();
        webProxy.setError(error);
        /*if (error!=null) {
            appProxy.setStatus(SunApplication.STATE_INVALID_PARSABLE);
        } else {
            appProxy.setStatus(SunApplication.STATE_VALID);
        }*/
    }

    private static class VersionInfo {
        private Class implClass;
        private Class proxyClass;
        private String publicId;
        private String systemId;
        
        public VersionInfo(Class implClass, Class proxyClass, String publicId, String systemId) {
            this.implClass = implClass;
            this.proxyClass = proxyClass;
            this.publicId = publicId;
            this.systemId = systemId;
        }

        public Class getImplClass() {
            return implClass;
        }

        public Class getProxyClass() {
            return proxyClass;
        }

        public String getPublicId() {
            return publicId;
        }

        public String getSystemId() {
            return systemId;
        }
    }
    
    private static HashMap apiToVersionMap = new HashMap(11);
    private static HashMap sunWebAppVersionMap = new HashMap(11);
    private static HashMap sunEjbJarVersionMap = new HashMap(11);
    private static HashMap sunApplicationVersionMap = new HashMap(11);
    private static HashMap sunAppClientVersionMap = new HashMap(11);
    
    static {
        sunWebAppVersionMap.put(SunWebApp.VERSION_2_3_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_230_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_230_DTD_SYSTEM_ID
            ));
        sunWebAppVersionMap.put(SunWebApp.VERSION_2_4_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_240_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_240_DTD_SYSTEM_ID
            ));
        sunWebAppVersionMap.put(SunWebApp.VERSION_2_4_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_241_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_241_DTD_SYSTEM_ID
            ));
        sunWebAppVersionMap.put(SunWebApp.VERSION_2_5_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp.class, SunWebAppProxy.class,
                DTDRegistry.SUN_WEBAPP_250_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_250_DTD_SYSTEM_ID
            ));

        sunEjbJarVersionMap.put(SunEjbJar.VERSION_2_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_200_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_200_DTD_SYSTEM_ID
            ));
        sunEjbJarVersionMap.put(SunEjbJar.VERSION_2_1_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_210_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_210_DTD_SYSTEM_ID
            ));
        sunEjbJarVersionMap.put(SunEjbJar.VERSION_2_1_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_211_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_211_DTD_SYSTEM_ID
            ));
        sunEjbJarVersionMap.put(SunEjbJar.VERSION_3_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar.class, SunEjbJarProxy.class,
                DTDRegistry.SUN_EJBJAR_300_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_300_DTD_SYSTEM_ID
            ));

        sunApplicationVersionMap.put(SunApplication.VERSION_1_3_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication.class, SunApplicationProxy.class,
                DTDRegistry.SUN_APPLICATION_130_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_130_DTD_SYSTEM_ID
            ));
        sunApplicationVersionMap.put(SunApplication.VERSION_1_4_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication.class, SunApplicationProxy.class,
                DTDRegistry.SUN_APPLICATION_140_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_140_DTD_SYSTEM_ID
            ));
        sunApplicationVersionMap.put(SunApplication.VERSION_5_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication.class, SunApplicationProxy.class,
                DTDRegistry.SUN_APPLICATION_50_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_50_DTD_SYSTEM_ID
            ));

        sunAppClientVersionMap.put(SunApplicationClient.VERSION_1_3_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_130_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_130_DTD_SYSTEM_ID
            ));
        sunAppClientVersionMap.put(SunApplicationClient.VERSION_1_4_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_140_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_140_DTD_SYSTEM_ID
            ));
        sunAppClientVersionMap.put(SunApplicationClient.VERSION_1_4_1, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_141_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_141_DTD_SYSTEM_ID
            ));
        sunAppClientVersionMap.put(SunApplicationClient.VERSION_5_0_0, new VersionInfo(
                org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient.class, SunApplicationClientProxy.class,
                DTDRegistry.SUN_APPCLIENT_50_DTD_PUBLIC_ID, DTDRegistry.SUN_APPCLIENT_50_DTD_SYSTEM_ID
            ));
        
        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp.class, sunWebAppVersionMap);
        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar.class, sunEjbJarVersionMap);
        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication.class, sunApplicationVersionMap);
        apiToVersionMap.put(org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient.class, sunAppClientVersionMap);
    }
    
    public RootInterface newGraph(Class rootType, String version) {
        RootInterface result = null;
        SunBaseBean graphRoot = null;
        Class graphRootClass = null;
        
        Map versionMap = (Map) apiToVersionMap.get(rootType);
        if(versionMap != null) {
            VersionInfo vInfo = (VersionInfo) versionMap.get(version);
            if(vInfo != null) {
                try {
                    // Formerly invoked static 'createGraph()' method, but that is merely a wrapper 
                    // for the default constructor so we'll call it directly.
                    graphRoot = (SunBaseBean) vInfo.getImplClass().newInstance();
                    graphRoot.graphManager().setDoctype(vInfo.getPublicId(), vInfo.getSystemId());
                    
                    Class proxyClass = vInfo.getProxyClass();
                    Constructor proxyConstructor = proxyClass.getConstructor(new Class [] { rootType, String.class });
                    result = (RootInterface) proxyConstructor.newInstance(new Object [] { graphRoot, version });
                } catch(IllegalArgumentException ex) {
                    // These five exceptions will be caught and logged either in StorageBeanFactory static
                    // initializer or in Base.addToGraphs().  They all represent some type of coding error
                    // on our part and should not occur under normal conditions (unless there is a bug).
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch(InvocationTargetException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch(InstantiationException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch(IllegalAccessException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                } catch(NoSuchMethodException ex) {
                    throw new RuntimeException(ex.getMessage(), ex); // Programmer error
                }
            } else {
                throw new IllegalStateException("No version information for " + version + " of type " + rootType.getName());
            }
        } else {
            throw new IllegalStateException("No version map for " + rootType.getName());
        }
        
        return result;
    }
    
    private static void setAppClientProxyErrorStatus(SunApplicationClientProxy appClientProxy, DDParse parse) {
        SAXParseException error = parse.getWarning();
        appClientProxy.setError(error);
        /*if (error!=null) {
            appProxy.setStatus(SunApplication.STATE_INVALID_PARSABLE);
        } else {
            appProxy.setStatus(SunApplication.STATE_VALID);
        }*/
    }
    
    /** @deprecated use the version that specifies the graph version you want.
     */
    public RootInterface newGraph(Class rootType) {
        if(org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp.class.equals(rootType)) {
            return newGraph(rootType, SunWebApp.VERSION_2_4_1);
        } else if(org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar.class.equals(rootType)) {
            return newGraph(rootType, SunEjbJar.VERSION_2_1_1);
        } else if(org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication.class.equals(rootType)) {
            return newGraph(rootType, SunApplication.VERSION_1_4_0);
        } else if(org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient.class.equals(rootType)) {
            return newGraph(rootType, SunApplicationClient.VERSION_1_4_1);
        }
        
        return null;
    }
    
    private static SunEjbJar createEjbJar(DDParse parse) {        
          SunEjbJar jar = null;
          String version = parse.getVersion();
          if (SunEjbJar.VERSION_3_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar(parse.getDocument(),  Common.NO_DEFAULT_VALUES); 
          } else if (SunEjbJar.VERSION_2_1_1.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar(parse.getDocument(),  Common.NO_DEFAULT_VALUES); 
          } else if (SunEjbJar.VERSION_2_1_0.equals(version)) {//ludo fix that!!!2.1.0 below
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunEjbJar.VERSION_2_0_0.equals(version)) {//ludo fix that!!!2.1.0 below
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } //LUDO CHANGE LATER!!!
          else{
              //What should we do there? ludo throws somethig or try with 3.0.0? FIXTIT
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          }
          
        //  return jar;
    }
    
    private static SunWebApp createWebApp(DDParse parse) throws DDException {
        SunWebApp webRoot = null;
        String version = parse.getVersion();
        if (SunWebApp.VERSION_2_5_0.equals(version)) {
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp(parse.getDocument(),  Common.NO_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_4_1.equals(version)) {
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp(parse.getDocument(),  Common.NO_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_4_0.equals(version)){ //ludo fix that!!!2_4_0 below
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp(parse.getDocument(),  Common.NO_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_3_0.equals(version)){ 
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp(parse.getDocument(),  Common.NO_DEFAULT_VALUES); 
        }else
            throw new DDException(
                    MessageFormat.format(ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/dd/api/Bundle").getString("MSG_UnknownWebXml"),new Object[]  {version} ));
        
    }
  
    private static SunApplication createApplication(DDParse parse) {        
          SunApplication jar = null;
          String version = parse.getVersion();
          if (SunApplication.VERSION_5_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplication.VERSION_1_4_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if(SunApplication.VERSION_1_3_0.equals(version)){
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          }
          
          return jar;
    }
    
    private static SunApplicationClient createApplicationClient(DDParse parse) {        
          SunApplicationClient jar = null;
          String version = parse.getVersion();
          if (SunApplicationClient.VERSION_5_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_5_0_0.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplicationClient.VERSION_1_4_1.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_1.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplicationClient.VERSION_1_4_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_4_0.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          } else if (SunApplicationClient.VERSION_1_3_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.client.model_1_3_0.SunApplicationClient(parse.getDocument(),  Common.NO_DEFAULT_VALUES);
          }
          
          return jar;
    }
    
    /**
     * Returns the root of Resources bean graph for java.io.File object.
     *
     * @param doc XML document representing the .sun-resource file    
     */
    public Resources getResourcesGraph() {
        Resources resourcesRoot = org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources.createGraph();
        ResourcesProxy proxy = new ResourcesProxy(resourcesRoot);
        return proxy;
    }
    
    /**
     * Returns the root of Resources bean graph for java.io.File object.
     *
     * @param doc XML document representing the .sun-resource file    
     */
    public Resources getResourcesGraph(InputStream in) {
        Resources resourcesRoot = org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources.createGraph(in);
        ResourcesProxy proxy = new ResourcesProxy(resourcesRoot);
        return proxy;
    }
    
    
    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }
        public InputSource resolveEntity(String publicId, String systemId) {
            String resource = null;
            if (EJB_30_90_DOCTYPE.equals(publicId)) {
                //return ejb30
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_3_0-0.dtd"); //NOI18N
            }else if (EJB_21_81_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-1.dtd"); //NOI18N
            } else if (EJB_21_80_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-0.dtd"); //NOI18N
            } else if (EJB_21_80_DOCTYPE_SUNONE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-0.dtd"); //NOI18N
            } else if (EJB_20_70_DOCTYPE_SUNONE.equals(publicId)) {////LUDO this 2.0.0 is missing FIXIT
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_0-0.dtd"); //NOI18N
            } else if (WEB_25_90_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_5-0.dtd"); //NOI18N
            }else if (WEB_21_81_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-1.dtd"); //NOI18N
            } else if (WEB_21_80_DOCTYPE.equals(publicId)) {
                resource ="/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-0.dtd"; //NOI18N
            } else if (WEB_21_80_DOCTYPE_SUNONE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-0.dtd"); //NOI18N
            } else if (WEB_20_70_DOCTYPE_SUNONE.equals(publicId)) {//LUDO this 2.3.0 is missing FIXIT
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_3-0.dtd"); //NOI18N
            } else if (APP_50_90_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_5_0-0.dtd"); //NOI18N
            }else if (APP_14_80_DOCTYPE.equals(publicId) || APP_14_81_DOCTYPE.equals(publicId) || APP_14_80_DOCTYPE_SUNONE.equals(publicId) ) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_1_4-0.dtd"); //NOI18N
            } else if(APP_13_70_DOCTYPE_SUNONE.equals(publicId)){
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_1_3-0.dtd"); //NOI18N
            } else if(APPCLIENT_50_90_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_5_0-0.dtd"); //NOI18N
            } else if (APPCLIENT_14_81_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_1_4-1.dtd"); //NOI18N
            } else if (APPCLIENT_14_80_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_1_4-0.dtd"); //NOI18N
            } else if (APPCLIENT_14_80_DOCTYPE_SUNONE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_1_4-0.dtd"); //NOI18N
            } else if (APPCLIENT_13_70_DOCTYPE.equals(publicId)) {
                resource = ("/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application-client_1_3-0.dtd"); //NOI18N
            }
            java.net.URL url = this.getClass().getResource(resource);
            return new InputSource(url.toString());
        }
    }
    
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        private int errorType=-1;
        SAXParseException error;

        public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<0) {
                errorType=0;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<1) {
                errorType=1;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }        
        public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            errorType=2;
            throw sAXParseException;
        }
        
        public int getErrorType() {
            return errorType;
        }
        public SAXParseException getError() {
            return error;
        }        
    }
   
    private DDParse parseDD (InputStream is) 
    throws SAXException, java.io.IOException {
        return parseDD(new InputSource(is));
    }
    
    private DDParse parseDD(InputSource is)  throws SAXException, java.io.IOException {
        
        DDProvider.ErrorHandler errorHandler = new DDProvider.ErrorHandler();
        DocumentBuilder parser = createParser(errorHandler);
        parser.setEntityResolver(DDResolver.getInstance());
        Document document = parser.parse(is);
        SAXParseException error = errorHandler.getError();
        return new DDParse(document, error);
    }

    private static DocumentBuilder createParser(ErrorHandler errorHandler) throws SAXException {
        DocumentBuilder parser=null;
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            parser = fact.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new SAXException(ex.getMessage());
        }
        parser.setErrorHandler(errorHandler);
        return parser;
    }

  
    /**
     * This class represents one parse of the deployment descriptor
     */
    private static class DDParse {
        private Document document;
        private SAXParseException saxException;
        private String version;
        public DDParse(Document d, SAXParseException saxEx) {
            document = d;
            saxException = saxEx;
            extractVersion();
        }
        
        /**
         * @return document from last parse
         */
        public Document getDocument() {
            return document;
        }
        
        /**
         * @return version of deployment descriptor. 
         */
        private void extractVersion () {
            // first check the doc type to see if there is one
            DocumentType dt = document.getDoctype();
            // This is the default version
            version = SunEjbJar.VERSION_3_0_0;
            if (dt != null) {
                if (EJB_21_81_DOCTYPE.equals(dt.getPublicId())) {
                    version = SunEjbJar.VERSION_2_1_1;
                }else if (EJB_21_80_DOCTYPE.equals(dt.getPublicId())) {
                    version = SunEjbJar.VERSION_2_1_0;
                }else if (EJB_30_90_DOCTYPE.equals(dt.getPublicId())) {
                    version = SunEjbJar.VERSION_3_0_0;
                }else if(EJB_21_80_DOCTYPE_SUNONE.equals(dt.getPublicId())) {
                    version = SunEjbJar.VERSION_2_1_0;
                }else if(EJB_20_70_DOCTYPE_SUNONE.equals(dt.getPublicId())) {
                    version = SunEjbJar.VERSION_2_0_0;
                }else if(WEB_25_90_DOCTYPE.equals(dt.getPublicId())){
                    version = SunWebApp.VERSION_2_5_0;
                }else if(WEB_21_81_DOCTYPE.equals(dt.getPublicId())){
                    version = SunWebApp.VERSION_2_4_1;
                }else if(WEB_21_80_DOCTYPE.equals(dt.getPublicId())){
                    version = SunWebApp.VERSION_2_4_0;
                }else if(WEB_21_80_DOCTYPE_SUNONE.equals(dt.getPublicId())){
                    version = SunWebApp.VERSION_2_4_0;
                }else if(WEB_20_70_DOCTYPE_SUNONE.equals(dt.getPublicId())){
                    version = SunWebApp.VERSION_2_3_0;
                }else if(APP_50_90_DOCTYPE.equals(dt.getPublicId())){
                    version = SunApplication.VERSION_5_0_0;
                }else if(APP_14_80_DOCTYPE.equals(dt.getPublicId()) || APP_14_81_DOCTYPE.equals(dt.getPublicId()) || APP_14_80_DOCTYPE_SUNONE.equals(dt.getPublicId())){
                    version = SunApplication.VERSION_1_4_0;
                }else if(APP_13_70_DOCTYPE_SUNONE.equals(dt.getPublicId())) {
                    version = SunApplication.VERSION_1_3_0;
                }else if (APPCLIENT_50_90_DOCTYPE.equals(dt.getPublicId())){
                    version = SunApplicationClient.VERSION_5_0_0;
                }else if (APPCLIENT_14_81_DOCTYPE.equals(dt.getPublicId())){
                    version = SunApplicationClient.VERSION_1_4_1;
                }else if(APPCLIENT_14_80_DOCTYPE.equals(dt.getPublicId()) || APPCLIENT_14_80_DOCTYPE_SUNONE.equals(dt.getPublicId())){
                    version = SunApplicationClient.VERSION_1_4_0;
                }else if(APPCLIENT_13_70_DOCTYPE.equals(dt.getPublicId())){
                    version = SunApplicationClient.VERSION_1_3_0;  
                }      
            }
        }
        
        public String getVersion() {
            return version;
        }
        
        /** 
         * @return validation error encountered during the parse
         */
        public SAXParseException getWarning() {
            return saxException;
        }
    }
    
}
