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

package org.netbeans.modules.j2ee.sun.dd.api;
import java.io.IOException;
import java.io.InputStream;
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
    
    private static final String APP_50_90_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 9.0 J2EE Application 5.0//EN"; //NOI18N     "sun-application_5_0-0.dtd" 
    private static final String APP_14_81_DOCTYPE =  "-//Sun Microsystems, Inc.//DTD Application Server 8.1 J2EE Application 1.4//EN"; //NOI18N     "sun-application_1_4-0.dtd" 
    private static final String APP_14_80_DOCTYPE = "-//Sun Microsystems, Inc.//DTD Application Server 8.0 J2EE Application 1.4//EN"; //NOI18N      "sun-application_1_4-0.dtd" 
    private static final String APP_13_70_DOCTYPE_SUNONE   =  "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 J2EE Application 1.3//EN"; //NOI18N  "sun-application_1_3-0.dtd" 
    private static final String APP_14_80_DOCTYPE_SUNONE = "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 J2EE Application 1.4//EN"; //NOI18N    "sun-application_1_4-0.dtd"  ///[THIS IS DEPRECATED]
    
   
//    private static final String  APP_14_70_DOCTYPE     =      "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client 1.3//EN" 	, "sun-application-client_1_3-0.dtd" ,
//    private static final String    =      "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Application Client 1.4//EN" 	, "sun-application-client_1_4-0.dtd" , ///[THIS IS DEPRECATED]
//   private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Application Client 1.4//EN"              , "sun-application-client_1_4-0.dtd" ,
//   private static final String    =      "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client 1.4//EN"              , "sun-application-client_1_4-1.dtd" ,
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
    
    public RootInterface newGraph(Class rootType) {
        if(org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp.class.equals(rootType)) {
            org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp webAppGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp.createGraph();
            // !PW FIXME need to find a way to do match version in webapp - e.g.
            // use Servlet 2.3 tag when that is the version we have.
            webAppGraph.graphManager().setDoctype(DTDRegistry.SUN_WEBAPP_241_DTD_PUBLIC_ID, DTDRegistry.SUN_WEBAPP_241_DTD_SYSTEM_ID);
            return new SunWebAppProxy(webAppGraph, webAppGraph.getVersion().toString());
        } else if(org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar.class.equals(rootType)) {
            org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar ejbJarGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar.createGraph();
            ejbJarGraph.graphManager().setDoctype(DTDRegistry.SUN_EJBJAR_211_DTD_PUBLIC_ID, DTDRegistry.SUN_EJBJAR_211_DTD_SYSTEM_ID);
            return new SunEjbJarProxy(ejbJarGraph, ejbJarGraph.getVersion().toString());
        } else if(org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication.class.equals(rootType)) {
            org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication appGraph =
                org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication.createGraph();
            appGraph.graphManager().setDoctype(DTDRegistry.SUN_APPLICATION_140_DTD_PUBLIC_ID, DTDRegistry.SUN_APPLICATION_140_DTD_SYSTEM_ID);
            return new SunApplicationProxy(appGraph, appGraph.getVersion().toString());
        }
        
        return null;
    }
    
    private static SunEjbJar createEjbJar(DDParse parse) {        
          SunEjbJar jar = null;
          String version = parse.getVersion();
          if (SunEjbJar.VERSION_3_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar(parse.getDocument(),  Common.USE_DEFAULT_VALUES); 
          } else if (SunEjbJar.VERSION_2_1_1.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_1.SunEjbJar(parse.getDocument(),  Common.USE_DEFAULT_VALUES); 
          } else if (SunEjbJar.VERSION_2_1_0.equals(version)) {//ludo fix that!!!2.1.0 below
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_1_0.SunEjbJar(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          } else if (SunEjbJar.VERSION_2_0_0.equals(version)) {//ludo fix that!!!2.1.0 below
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_2_0_0.SunEjbJar(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          } //LUDO CHANGE LATER!!!
          else{
              //What should we do there? ludo throws somethig or try with 3.0.0? FIXTIT
              return new org.netbeans.modules.j2ee.sun.dd.impl.ejb.model_3_0_0.SunEjbJar(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          }
          
        //  return jar;
    }
    
    private static SunWebApp createWebApp(DDParse parse) throws DDException {
        SunWebApp webRoot = null;
        String version = parse.getVersion();
        if (SunWebApp.VERSION_2_5_0.equals(version)) {
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_5_0.SunWebApp(parse.getDocument(),  Common.USE_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_4_1.equals(version)) {
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_1.SunWebApp(parse.getDocument(),  Common.USE_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_4_0.equals(version)){ //ludo fix that!!!2_4_0 below
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_4_0.SunWebApp(parse.getDocument(),  Common.USE_DEFAULT_VALUES); 
        } else if (SunWebApp.VERSION_2_3_0.equals(version)){ 
            return new org.netbeans.modules.j2ee.sun.dd.impl.web.model_2_3_0.SunWebApp(parse.getDocument(),  Common.USE_DEFAULT_VALUES); 
        }else
            throw new DDException(ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/dd/api/Bundle").getString("SG_UnknownWebXml") );
        
    }
  
    private static SunApplication createApplication(DDParse parse) {        
          SunApplication jar = null;
          String version = parse.getVersion();
          if (SunApplication.VERSION_5_0_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_5_0_0.SunApplication(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          } else if (SunApplication.VERSION_1_4_0.equals(version)) {
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_4_0.SunApplication(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          } else if(SunApplication.VERSION_1_3_0.equals(version)){
              return new org.netbeans.modules.j2ee.sun.dd.impl.app.model_1_3_0.SunApplication(parse.getDocument(),  Common.USE_DEFAULT_VALUES);
          }
          
          return jar;
    }
    
    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }       
        public InputSource resolveEntity (String publicId, String systemId) {
            if (EJB_30_90_DOCTYPE.equals(publicId)) { 
                //return ejb30 input source
                return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_3_0-0.dtd"); //NOI18N
            }else if (EJB_21_81_DOCTYPE.equals(publicId)) { 
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-1.dtd"); //NOI18N
            } else if (EJB_21_80_DOCTYPE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-0.dtd"); //NOI18N
            } else if (EJB_21_80_DOCTYPE_SUNONE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_1-0.dtd"); //NOI18N
            } else if (EJB_20_70_DOCTYPE_SUNONE.equals(publicId)) {////LUDO this 2.0.0 is missing FIXIT
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-ejb-jar_2_0-0.dtd"); //NOI18N
            } else if (WEB_25_90_DOCTYPE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_5-0.dtd"); //NOI18N
            }else if (WEB_21_81_DOCTYPE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-1.dtd"); //NOI18N
            } else if (WEB_21_80_DOCTYPE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-0.dtd"); //NOI18N
            } else if (WEB_21_80_DOCTYPE_SUNONE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_4-0.dtd"); //NOI18N
            } else if (WEB_20_70_DOCTYPE_SUNONE.equals(publicId)) {//LUDO this 2.3.0 is missing FIXIT
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-web-app_2_3-0.dtd"); //NOI18N
            } else if (APP_50_90_DOCTYPE.equals(publicId)) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_5_0-0.dtd"); //NOI18N
            }else if (APP_14_80_DOCTYPE.equals(publicId) || APP_14_81_DOCTYPE.equals(publicId) || APP_14_80_DOCTYPE_SUNONE.equals(publicId) ) {
                  // return a special input source
             return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_1_4-0.dtd"); //NOI18N
            } else if(APP_13_70_DOCTYPE_SUNONE.equals(publicId)){
                return new InputSource("nbres:/org/netbeans/modules/j2ee/sun/dd/impl/resources/sun-application_1_3-0.dtd"); //NOI18N
                
            }else {
                // use the default behaviour
                return null;
            }
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
    
    private DDParse parseDD (InputSource is) 
    throws SAXException, java.io.IOException {
        DDProvider.ErrorHandler errorHandler = new DDProvider.ErrorHandler();
        org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
        parser.setErrorHandler(errorHandler);
        parser.setEntityResolver(DDProvider.DDResolver.getInstance());
        // XXX do we need validation here, if no one is using this then
        // the dependency on xerces can be removed and JAXP can be used
        parser.setFeature("http://xml.org/sax/features/validation", true); //NOI18N
        parser.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true); //NOI18N
        parser.parse(is);
        Document d = parser.getDocument();
        SAXParseException error = errorHandler.getError();
        return new DDParse(d, error);
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
                }else if(WEB_21_80_DOCTYPE_SUNONE.equals(dt.getPublicId())){
                    version = SunWebApp.VERSION_2_4_0;
                }else if(APP_50_90_DOCTYPE.equals(dt.getPublicId())){
                    version = SunApplication.VERSION_5_0_0;
                }else if(APP_14_80_DOCTYPE.equals(dt.getPublicId()) || APP_14_81_DOCTYPE.equals(dt.getPublicId()) || APP_14_80_DOCTYPE_SUNONE.equals(dt.getPublicId())){
                    version = SunApplication.VERSION_1_4_0;
                }else if(APP_13_70_DOCTYPE_SUNONE.equals(dt.getPublicId())) {
                    version = SunApplication.VERSION_1_3_0;
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
