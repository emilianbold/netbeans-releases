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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import org.netbeans.modules.xml.catalog.spi.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.netbeans.modules.xml.api.model.DTDUtil;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
/** Catalog for App Server 8PE DTDs that enables completion support in editor.
 *
 * @author Ludo
 */
public class RunTimeDDCatalog extends GrammarQueryManager implements CatalogReader, CatalogDescriptor,org.xml.sax.EntityResolver  {
    
    private static final String XML_XSD="http://www.w3.org/2001/xml.xsd"; // NOI18N
    private static final String XML_XSD_DEF="<?xml version='1.0'?><xs:schema targetNamespace=\"http://www.w3.org/XML/1998/namespace\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xml:lang=\"en\"><xs:attribute name=\"lang\" type=\"xs:language\"><xs:annotation><xs:documentation>In due course, we should install the relevant ISO 2- and 3-letter codes as the enumerated possible values . . .</xs:documentation></xs:annotation></xs:attribute></xs:schema>"; // NOI18N
    public  static final String TypeToURLMap[] = {
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 J2EE Application 1.3//EN" 	, "sun-application_1_3-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 J2EE Application 1.4//EN" 	, "sun-application_1_4-0.dtd" , ///[THIS IS DEPRECATED]
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 J2EE Application 1.4//EN"                , "sun-application_1_4-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 J2EE Application 1.4//EN"                , "sun-application_1_4-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 9.0 J2EE Application 5.0//EN"                             , "sun-application_5_0-0.dtd",
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 EJB 2.0//EN"                     , "sun-ejb-jar_2_0-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 EJB 2.1//EN"                     , "sun-ejb-jar_2_1-0.dtd" , ///[THIS IS DEPRECATED]
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 EJB 2.1//EN"                             , "sun-ejb-jar_2_1-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 EJB 2.1//EN"                             , "sun-ejb-jar_2_1-1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 EJB 3.0//EN"                             , "sun-ejb-jar_3_0-0.dtd",
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client 1.3//EN" 	, "sun-application-client_1_3-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Application Client 1.4//EN" 	, "sun-application-client_1_4-0.dtd" , ///[THIS IS DEPRECATED]
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Application Client 1.4//EN"              , "sun-application-client_1_4-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client 1.4//EN"              , "sun-application-client_1_4-1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Connector 1.0//EN"               , "sun-connector_1_0-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Servlet 2.3//EN"                 , "sun-web-app_2_3-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 8.0 Servlet 2.4//EN"                 , "sun-web-app_2_4-0.dtd" , ///[THIS IS DEPRECATED]
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Servlet 2.4//EN"                         , "sun-web-app_2_4-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Web Server 6.1 Servlet 2.3//EN"                         , "sun-web-app_2_3-1.dtd" ,                
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Servlet 2.4//EN"                         , "sun-web-app_2_4-1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 9.0 Servlet 2.5//EN"                         , "sun-web-app_2_5-0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 Application Client Container 1.0//EN" 	, "sun-application-client-container_1_0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Sun ONE Application Server 7.0 OR Mapping //EN"                 , "sun-cmp-mapping_1_0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 OR Mapping//EN"                          , "sun-cmp-mapping_1_1.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 OR Mapping//EN"                          , "sun-cmp-mapping_1_2.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Domain//EN"                              , "sun-domain_1_0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.0 Application Client Container //EN" 	, "sun-application-client-container_1_0.dtd" ,
        "-//Sun Microsystems, Inc.//DTD Application Server 8.1 Application Client Container //EN" 	, "sun-application-client-container_1_1.dtd" ,
        "-//Sun Microsystems Inc.//DTD Application Server 8.0 Domain//EN"                              ,"sun-domain_1_1.dtd",
        "-//Sun Microsystems Inc.//DTD Application Server 8.1 Domain//EN"                              ,"sun-domain_1_1.dtd",
        
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN"                                       , "application_1_3.dtd",
        "-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN"                                       , "application_1_2.dtd",
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN"                                   , "ejb-jar_2_0.dtd",
        "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN"                                   , "ejb-jar_1_1.dtd",
        "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN"                                , "application-client_1_3.dtd",
        "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.2//EN"                                , "application-client_1_2.dtd",
        "-//Sun Microsystems, Inc.//DTD Connector 1.0//EN"                                              , "connector_1_0.dtd",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"                                        , "web-app_2_3.dtd",
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN"                                        , "web-app_2_2.dtd",
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN"                                        , "web-jsptaglibrary_1_2.dtd",
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN"                                        , "web-jsptaglibrary_1_1.dtd",
    };
    
    /*******NetBeans 3.6 is NOT ready yet to support schemas for code completion... What a pity!:        */
    public  static final String SchemaToURLMap[] = {
        
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd"                    , "ejb-jar_2_1",
        
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/application-client_1_4.xsd"         , "application-client_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/application_1_4.xsd"                , "application_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/jax-rpc-ri-config.xsd"              , "jax-rpc-ri-config",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/connector_1_5.xsd"                  , "connector_1_5",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/jsp_2_0.xsd"                        , "jsp_2_0.xsd",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/datatypes.dtd"                      , "datatypes",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"                    , "web-app_2_4",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/web-jsptaglibrary_2_0.xsd"          , "web-jsptaglibrary_2_0",
        ///"SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_1_4.xsd"                       , "j2ee_1_4",
        "SCHEMA:http://java.sun.com/xml/ns/j2ee/j2ee_jaxrpc_mapping_1_1.xsd"        , "j2ee_jaxrpc_mapping_1_1",
        "SCHEMA:http://www.ibm.com/webservices/xsd/j2ee_web_services_1_1.xsd"             ,"j2ee_web_services_1_1",
       // "SCHEMA:http://www.ibm.com/webservices/xsd/j2ee_web_services_client_1_1.xsd"          ,"j2ee_web_services_client_1_1",
    };
    
    
    /** Creates a new instance of RunTimeDDCatalog */
    public RunTimeDDCatalog() {
    }
    private static RunTimeDDCatalog ddCatalog;
    
    /** Factory method providing catalog for XML completion of DD */
    public static synchronized RunTimeDDCatalog getRunTimeDDCatalog(){
        if (ddCatalog==null) {
            ddCatalog = new RunTimeDDCatalog();
        }
        return ddCatalog;
    }
    /**
     * Get String iterator representing all public IDs registered in catalog.
     * @return null if cannot proceed, try later.
     */
    public java.util.Iterator getPublicIDs() {
        String  installRoot = PluginProperties.getDefault().getInstallRoot().getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
        if (installRoot == null) {
            return null;
        }
        
        java.util.List list = new java.util.ArrayList();
        for (int i=0;i<TypeToURLMap.length;i = i+2){
            list.add(TypeToURLMap[i]);
        }
        for (int i=0;i<SchemaToURLMap.length;i = i+2){
            list.add(SchemaToURLMap[i]);
        }
        list.add("-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN");//JSF code completion
        list.add("-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN");//JSF code completion
        
        return list.listIterator();
    }

    /**
     * Get registered systemid for given public Id or null if not registered.
     * @return null if not registered
     */
    public String getSystemID(String publicId) {
        String  installRoot = PluginProperties.getDefault().getInstallRoot().getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
        if (installRoot == null) {
            return null;
        }
        String loc="dtds";
        for (int i=0;i<TypeToURLMap.length;i = i+2){
            if (TypeToURLMap[i].equals(publicId)){
                File file = new File(installRoot+"/lib/"+loc+"/"+TypeToURLMap[i+1]);
                try{
                    return file.toURI().toURL().toExternalForm();  
                }catch(Exception e){
                    return "";
                }
            }
        }
        loc="schemas";
        for (int i=0;i<SchemaToURLMap.length;i = i+2){
            if (SchemaToURLMap[i].equals(publicId)){
                return "nbres:/org/netbeans/modules/j2ee/sun/ide/resources/"+SchemaToURLMap[i+1]+".dtd";
                
                
            }
        }
        if (publicId.equals("-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.0//EN")){
            File file = new File(installRoot+"/lib/jsf-impl.jar");
            String s = "";
            try{
                s= file.toURI().toURL().toExternalForm();
            }catch(Exception e){
                
            }
            return "jar:"+s+"!/com/sun/faces/web-facesconfig_1_0.dtd";
        }
        if (publicId.equals("-//Sun Microsystems, Inc.//DTD JavaServer Faces Config 1.1//EN")){
            File file = new File(installRoot+"/lib/jsf-impl.jar");
            String s = "";
            try{
                s= file.toURI().toURL().toExternalForm();
            }catch(Exception e){
                
            }
            return "jar:"+s+"!/com/sun/faces/web-facesconfig_1_1.dtd";
        }
        return null;
    }
    
    /**
     * Refresh content according to content of mounted catalog.
     */
    public void refresh() {
    }
    
    /**
     * Optional operation allowing to listen at catalog for changes.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void addCatalogListener(CatalogListener l) {
    }
    
    /**
     * Optional operation couled with addCatalogListener.
     * @throws UnsupportedOpertaionException if not supported by the implementation.
     */
    public void removeCatalogListener(CatalogListener l) {
    }
    
    /** Registers new listener.  */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    /**
     * @return I18N display name
     */
    public String getDisplayName() {
        return NbBundle.getMessage(RunTimeDDCatalog.class, "LBL_RunTimeDDCatalog");
    }
    
    /**
     * Return visuaized state of given catalog.
     * @param type of icon defined by JavaBeans specs
     * @return icon representing current state or null
     */
    public java.awt.Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/web/dd/resources/DDCatalog.gif"); // NOI18N
    }
    
    /**
     * @return I18N short description
     */
    public String getShortDescription() {
        return NbBundle.getMessage(RunTimeDDCatalog.class, "DESC_RunTimeDDCatalog");
    }
    
    /** Unregister the listener.  */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }
    
    public static final String J2EE_NS = "http://java.sun.com/xml/ns/j2ee"; // NOI18N
    public static final String RI_CONFIG_NS = "http://java.sun.com/xml/ns/jax-rpc/ri/config"; // NOI18N

    public static final String IBM_J2EE_NS = "http://www.ibm.com/webservices/xsd"; // NOI18N
    private static final String XMLNS_ATTR="xmlns"; //NOI18N
    //  public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
    //      return null;
    //  }
    private static final String EJB_JAR_TAG="ejb-jar"; //NOI18N
    private static final String EJBJAR_2_1_XSD="ejb-jar_2_1.xsd"; // NOI18N
    private static final String EJBJAR_2_1 = J2EE_NS+"/"+EJBJAR_2_1_XSD; // NOI18N
    public static final String EJBJAR_2_1_ID = "SCHEMA:"+EJBJAR_2_1; // NOI18N
    
    
    private static final String APP_TAG="application"; //NOI18N
    private static final String APP_1_4_XSD="application_1_4.xsd"; // NOI18N
    private static final String APP_1_4= J2EE_NS+"/"+APP_1_4_XSD; // NOI18N
    public static final String APP_1_4_ID = "SCHEMA:"+APP_1_4; // NOI18N
 

    private static final String WEBSERVICES_TAG="webservices"; //NOI18N
    private static final String WEBSERVICES_1_1_XSD="j2ee_web_services_1_1.xsd"; // NOI18N
    private static final String WEBSERVICES_1_1= IBM_J2EE_NS+"/"+WEBSERVICES_1_1_XSD; // NOI18N
    public static final String WEBSERVICES_1_1_ID = "SCHEMA:"+WEBSERVICES_1_1; // NOI18N

    
    public String getFullURLFromSystemId(String systemId){
        return null;
        
    }
    
    private static String SCHEMASLOCATION=null;
    /**
     * Resolves schema definition file for deployment descriptor (spec.2_4)
     * @param publicId publicId for resolved entity (null in our case)
     * @param systemId systemId for resolved entity
     * @return InputSource for
     */
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
        
        if (SCHEMASLOCATION == null) {
            String  installRoot = PluginProperties.getDefault().getInstallRoot().getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
            if (installRoot==null)
                return null;
            File f = new File(installRoot);
            if (f.exists()==false)
                return null;
            File file = new File(installRoot+"/lib/schemas/");
            SCHEMASLOCATION = "";
            try{
                SCHEMASLOCATION= file.toURI().toURL().toExternalForm();
            }catch(Exception e){
                
            }

            
        }
        if (systemId!=null && systemId.endsWith(EJBJAR_2_1_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+EJBJAR_2_1_XSD);
        } else if (systemId!=null && systemId.endsWith(APP_1_4_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+APP_1_4_XSD);
        }  else if (systemId!=null && systemId.endsWith(WEBSERVICES_1_1_XSD)) {
            return new org.xml.sax.InputSource(SCHEMASLOCATION+WEBSERVICES_1_1_XSD);
        }
        else if (XML_XSD.equals(systemId)) {
            return new org.xml.sax.InputSource(new java.io.StringReader(XML_XSD_DEF));
        } else {
            return null;
        }
    }
    
    
    
    public java.util.Enumeration enabled(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        if (ctx.getFileObject() == null) return null;
        InputSource is= ctx.getInputSource();
        java.util.Enumeration en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = (Node) en.nextElement();
            if (next.getNodeType() == next.DOCUMENT_TYPE_NODE) {
                return null; // null for web.xml specified by DTD
            } else if (next.getNodeType() == next.ELEMENT_NODE) {
                Element element = (Element) next;
                String tag = element.getTagName();
                if (EJB_JAR_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && J2EE_NS.equals(xmlns)) {
                        java.util.Vector v = new java.util.Vector();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                }
                
                if (APP_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && J2EE_NS.equals(xmlns)) {
                        java.util.Vector v = new java.util.Vector();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                }
                 if (WEBSERVICES_TAG.equals(tag)) {  // NOI18N
                    String xmlns = element.getAttribute(XMLNS_ATTR);
                    if (xmlns!=null && J2EE_NS.equals(xmlns)) {
                        java.util.Vector v = new java.util.Vector();
                        v.add(next);
                        return v.elements();
                        //   return org.openide.util.Enumerations.singleton(next);
                    }
                }           }
        }
        
        return null;
    }
    
    public java.beans.FeatureDescriptor getDescriptor() {
        return new java.beans.FeatureDescriptor();
    }
    
    /** Returns pseudo DTD for code completion
     */
    public org.netbeans.modules.xml.api.model.GrammarQuery getGrammar(org.netbeans.modules.xml.api.model.GrammarEnvironment ctx) {
        UserCatalog catalog = UserCatalog.getDefault();
        ///System.out.println("bbb");
        InputSource is= ctx.getInputSource();
       /// System.out.println(is.getPublicId());
       /// System.out.println(is.getSystemId());
       /// System.out.println(is);
        if (catalog != null) {
            
            EntityResolver resolver = catalog.getEntityResolver();
            if (resolver != null) {
                try {
                    
                    
                    if (ctx.getFileObject() == null) return null;
                    
                    if (is.getSystemId().endsWith("ejb-jar.xml") ) {  // NOI18N
                       // System.out.println("ejbjar  tag");
                        InputSource inputSource = resolver.resolveEntity(EJBJAR_2_1_ID, null);
                        if (inputSource!=null) {
                            return DTDUtil.parseDTD(true, inputSource);
                        }
                    }
                    
                    if (is.getSystemId().endsWith("application.xml") ) {  // NOI18N
                        //System.out.println("app tag");
                        InputSource inputSource = resolver.resolveEntity(APP_1_4_ID, null);
                        if (inputSource!=null) {
                            return DTDUtil.parseDTD(true, inputSource);
                        }
                    }
                    if (is.getSystemId().endsWith("webservices.xml") ) {  // NOI18N
                       // System.out.println("webservices tag");
                        InputSource inputSource = resolver.resolveEntity(WEBSERVICES_1_1_ID, null);
                        if (inputSource!=null) {
                            return DTDUtil.parseDTD(true, inputSource);
                        }
                    }
                    
                    
                    
                } catch(SAXException e) {
                } catch(java.io.IOException e) {
                    //System.out.println("eeee");
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Get registered URI for the given name or null if not registered.
     * @return null if not registered
     */
    public String resolveURI(String name) {
       // System.out.println("resolveURI(String name)="+name);
        String  installRoot = PluginProperties.getDefault().getInstallRoot().getAbsolutePath(); //System.getProperty("com.sun.aas.installRoot");
        String prefix ="";
                    File file = new File(installRoot+"/lib/schemas/");
             try{
                prefix= file.toURI().toURL().toExternalForm();
            }catch(Exception e){
                
            }

        if (name.equals("http://java.sun.com/xml/ns/jax-rpc/ri/config")){
            return prefix +"jax-rpc-ri-config.xsd";            
        }
        if (name.equals("http://java.sun.com/xml/ns/j2eeppppppp")){
            return prefix +"j2ee_web_services_1_1.xsd";            
        }
        
        return null;
    }
    /**
     * Get registered URI for the given publicId or null if not registered.
     * @return null if not registered
     */ 
    public String resolvePublic(String publicId) {
        return null;
    }    
}
