/*
 * DDXmiConstants.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.websphere6.dd.beans;

/**
 *
 * @author dlm198383
 */
public interface DDXmiConstants {
    
    public  static final String XMI_VERSION="XmiVersion";
    public  static final String XMI_VERSION_ID="xmi:version";
    public  static final String XMI_VERSION_STRING="2.0";
    
    public  static final String NS_XMI="NamespaceXmi";
    public  static final String NS_XMI_ID="xmlns:xmi";
    public  static final String NS_XMI_STRING="http://www.omg.org/XMI";
    
    public  static final String NS_EJB="NamespaceEjb";
    public  static final String NS_EJB_STRING="ejb.xmi";
    public  static final String NS_EJB_ID="xmlns:ejb";
    
    public  static final String NS_EJB_BND="NamespaceEjbBnd";
    public  static final String NS_EJB_BND_STRING="ejbbnd.xmi";
    public  static final String NS_EJB_BND_ID="xmlns:ejbbnd";
    
    public  static final String NS_EJB_EXT="NamespaceEjbExt";
    public  static final String NS_EJB_EXT_STRING="ejbext.xmi";
    public  static final String NS_EJB_EXT_ID="xmlns:ejbext";
    
    public  static final String NS_APP="NamespaceApplication";
    public  static final String NS_APP_STRING="application.xmi";
    public  static final String NS_APP_ID="xmlns:application";
    
    
    public  static final String NS_APP_BND="NamespaceApplicationBnd";
    public  static final String NS_APP_BND_STRING="applicationbnd.xmi";
    public  static final String NS_APP_BND_ID="xmlns:applicationbnd";
    
    public  static final String NS_APP_EXT="NamespaceApplicationExtention";
    public  static final String NS_APP_EXT_ID="xmlns:applicationext";
    public  static final String NS_APP_EXT_STRING="applicationext.xmi";
    
    public  static final String NS_COMMON="NamespaceCommon";
    public  static final String NS_COMMON_STRING="common.xmi";
    public  static final String NS_COMMON_ID="xmlns:common";
    
    public  static final String NS_COMMON_BND="NamespaceCommonBnd";
    public  static final String NS_COMMON_BND_STRING="commonbnd.xmi";
    public  static final String NS_COMMON_BND_ID="xmlns:commonbnd";
    
    
    public static final String  NS_WEB_APP="NameSpaceWebApplication";
    public static final String  NS_WEB_APP_ID="xmlns:webapplication";
    public static final String  NS_WEB_APP_STRING="webapplication.xmi";
    
    public static final String  NS_WEB_APP_EXT="NameSpaceWebAppExtension";
    public static final String  NS_WEB_APP_EXT_ID="xmlns:webappext";
    public static final String  NS_WEB_APP_EXT_STRING="webappext.xmi";
    
    public static final String  NS_WEB_APP_BND="NameSpaceWebAppBinding";
    public static final String  NS_WEB_APP_BND_ID="xmlns:webappbnd";
    public static final String  NS_WEB_APP_BND_STRING="webappbnd.xmi";
    
    public static final String NS_XSI="NamespaceXsi";
    public static final String NS_XSI_ID="xmlns:xsi";
    public static final String NS_XSI_STRING="http://www.w3.org/2001/XMLSchema-instance";
    
    public static final String VIRTUAL_HOST_NAME="VirtualHostName";
    public static final String VIRTUAL_HOST_NAME_ID="virtualHostName";
    
    
    public  static final String XMI_ID="XmiId";
    public  static final String XMI_ID_ID="xmi:id";
    
    public  static final String JNDI_NAME_ID="jndiName";
    public  static final String HREF_ID="href";
    public  static final String XMI_TYPE_ID="xmi:type";
    public  static final String NAME_ID="name";
    static public final String URI_ID="uri";
    static public final String MIME_TYPE_ID="mimeType";
    static public final String ERROR_PAGE_ID="errorPage";
    static public final String DEFAULT_PAGE_ID="defaultPage";
    static public final String RES_AUTH_ID="resAuth";
    
    public  static final String APPLICATION="Application";
    public static final  String APPLICATION_ID="application";
    public  static final String APPLICATION_HREF="ApplicationHref";
    
    public  static final String WEB_APPLICATION="WebApplication";
    public static final  String WEB_APPLICATION_ID="webApp";
    public static final  String WEB_aPPLICATION_ID="webapp";
    public  static final String WEB_APPLICATION_HREF="WebApplicationHref";
    
    
    public  static final String EJB_JAR="EjbJar";
    public static final  String EJB_JAR_ID="ejbJar";
    public  static final String EJB_JAR_HREF="EjbJarHref";
    
    
    
    public static final  String APPROOT_ID="appRoot";
    
    
    static public final  String MODULE_XMI_TYPE_EJB_ID="application:EjbModule";
    static public final  String MODULE_XMI_TYPE_WEB_ID="application:WebModule";
    
    static public final  String EJB_EXTENSIONS_TYPE_SESSION="ejbext:SessionExtension";
    static public final  String EJB_EXTENSIONS_TYPE_ENTITY="ejbext:EntityExtension";
    static public final  String EJB_EXTENSIONS_TYPE_MESSAGEDRIVEN="ejbext:MessageDrivenExtension";
    
    static public final  String EJB_ENTERPRISE_BEAN_TYPE_SESSION="ejb:Session";
    static public final  String EJB_ENTERPRISE_BEAN_TYPE_ENTITY="ejb:Entity";
    static public final  String EJB_ENTERPRISE_BEAN_TYPE_MESSAGEDRIVEN="ejb:MessageDriven";
    static public final  String EJB_ENTERPRISE_BEAN_TYPE_CONTAINER_MANAGED_ENTITY="ejb:ContainerManagedEntity";
    
    
    static public final  String TYPE_APP_EXT_EJB_ID="applicationext:EjbModuleExtension";
    static public final  String TYPE_APP_EXT_WEB_ID="applicationext:WebModuleExtension";
    static public final  String TYPE_APP_EXT_APP_ID="applicationext:ApplicationExtension";
    
    static public final  String TYPE_WEB_APP_EXT_ID="webappext:WebAppExtension";
    static public final  String TYPE_WEB_APP_BND_ID="webappbnd:WebAppBinding";
    
    static public final  String TYPE_EJB_BND_ID="ejbbnd:EJBJarBinding";
    static public final  String TYPE_EJB_EXT_ID="ejbext:EJBJarExtension";
    
    
    static public final  String RELOAD_INTERVAL_ID="reloadInterval";
    static public final  String RELOAD_INTERVAL="ReloadInterval";
    
    static public final  String RELOAD_ENABLED_ID="reloadingEnabled";
    static public final  String RELOAD_ENABLED="ReloadEnabled";
    
    static public final  String ADDITIONAL_CLASSPATH_ID="additionalClassPath";
    static public final  String ADDITIONAL_CLASSPATH="AdditionalClassPath";
    
    static public final  String FILE_SERVING_ENABLED_ID="fileServingEnabled";
    static public final  String FILE_SERVING_ENABLED="FileServingEnabled";
    
    static public final  String DIRECTORY_BROWSING_ENABLED_ID="directoryBrowsingEnabled";
    static public final  String DIRECTORY_BROWSING_ENABLED="DirectoryBrowsingEnabled";
    
    static public final  String SERVE_SERVLETS_ID="serveServletsByClassnameEnabled";
    static public final  String SERVE_SERVLETS="ServeServletsByClassnameEnabled";
    
    static public final String RES_REF_BINDINGS_XMI_ID="ResRefBindingsXmiId";
    static public final String RES_REF_BINDINGS_JNDI_NAME="ResRefBindingsJndiName";
    static public final String RES_REF_BINDINGS_ROOT="ResRefBindingsRoot";
    static public final String RES_REF_BINDINGS="ResRefBindings";
    
    static public final String RES_ENV_REF_BINDINGS_XMI_ID="ResEnvRefBindingsXmiId";
    static public final String RES_ENV_REF_BINDINGS_JNDI_NAME="ResEnvRefBindingsJndiName";
    static public final String RES_ENV_REF_BINDINGS_ROOT="ResEnvRefBindingsRoot";
    static public final String RES_ENV_REF_BINDINGS="ResEnvRefBindings";
    
    
    static public final String EJB_REF_BINDINGS_XMI_ID="EjbRefBindingsXmiId";
    static public final String EJB_REF_BINDINGS_JNDI_NAME="EjbRefBindingsJndiName";
    static public final String EJB_REF_BINDINGS_ROOT="EjbRefBindingsRoot";
    static public final String EJB_REF_BINDINGS="EjbRefBindings";
    static public final String EJB_BINDINGS_ID="ejbBindings";
    static public final String EXTENDED_SERVLETS="ExtendedServlets";
    static public final String EXTENDED_SERVLETS_XMI_ID="ExtendedServletsXmiId";
    
    
    static public final String BINDING_EJB_REF_HREF="BindingEjbHref";
    static public final String BINDING_EJB_REF="BindingEjbRef";
    static public final String BINDING_EJB_REF_ID="bindingEjbRef";
    
    static public final String BINDING_RESOURCE_REF_HREF="BindingResourceRefHref";
    static public final String BINDING_RESOURCE_REF="BindingResourceRef";
    static public final String BINDING_RESOURCE_REF_ID="bindingResourceRef";
    static public final String BINDING_RESOURCE_ENV_REF_HREF="BindingResourceEnvRefHref";
    static public final String BINDING_RESOURCE_ENV_REF="BindingResourceEnvRef";
    static public final String BINDING_RESOURCE_ENV_REF_ID="bindingResourceEnvRef";
    
    
    static public final String MARKUP_LANGUAGES_XMI_ID="MarkupLanguagesXmiId";
    static public final String MARKUP_LANGUAGES_NAME="MarkupLanguagesName";
    static public final String MARKUP_LANGUAGES_MIME_TYPE="MarkupLanguagesMimeType";
    static public final String MARKUP_LANGUAGES_ERROR_PAGE="MarkupLanguagesErrorPage";
    static public final String MARKUP_LANGUAGES_DEFAULT_PAGE="MarkupLanguagesDefaultPage";
    static public final String PRECOMPILE_JSPS="PreCompileJPSs";
    static public final String PRECOMPILE_JSPS_ID="preCompileJSPs";
    static public final String AUTO_RESPONSE_ENCODING="AutoResponseEncoding";
    static public final String AUTO_RESPONSE_ENCODING_ID="autoResponseEncoding";
    static public final String AUTO_REQUEST_ENCODING="AutoRequestEncoding";
    static public final String AUTO_REQUEST_ENCODING_ID="autoRequestEncoding";
    
    static public final String DEFAULT_ERROR_PAGE="DefaultErrorPage";
    static public final String DEFAULT_ERROR_PAGE_ID="defaultErrorPage";
    static public final String PAGES_XMI_ID="PagesXmiId";
    static public final String PAGES_NAME="PagesName";
    static public final String PAGES_URI="PagesUri";
    
    static public final String NS_COMMONEXT_LOCALTRAN_ID="xmlns:commonext.localtran";
    static public final String NS_COMMONEXT_LOCALTRAN="NamespaceCommonextLocaltran";
    static public final String NS_COMMONEXT_LOCALTRAN_STRING="commonext.localtran.xmi";
    
    
    static public final String EJB_BINDINGS="EjbBindings";
    static public final String EJB_BINDINGS_XMI_ID="EjbBindingsXmiId";
    static public final String EJB_BINDINGS_XMI_TYPE="EjbBindingsXmiType";
    static public final String EJB_BINDINGS_JNDI_NAME="EjbBindingsJNDIName";
    
    
    static public final String AUTO_LOAD_FILTERS_ID="autoLoadFilters";
    static public final String AUTO_LOAD_FILTERS="AutoLoadFilters";
    
    
    
    static public final String APP_HREF_PREFIX    = "META-INF/application.xml#";
    static public final String EJBJAR_HREF_PREFIX = "META-INF/ejb-jar.xml#";
    static public final String WEB_HREF_PREFIX    = "WEB-INF/web.xml#";
    
    static public final String EJB_EXTENSIONS="EjbExtensions";
    static public final String EJB_EXTENSIONS_XMI_ID="EjbExtensionsXmiId";
    static public final String EJB_EXTENSIONS_XMI_TYPE="EjbExtensionsXmiType";
    static public final String EJB_EXTENSIONS_XMI_NAME="EjbExtensionsXmiName";
    
    
    static public final String ENTERPRISE_BEAN_HREF="EnterpriseBeanHref";
    static public final String ENTERPRISE_BEAN_TYPE="EnterpriseBeanType";
    static public final String ENTERPRISE_BEAN="EnterpriseBean";
    static public final String ENTERPRISE_BEAN_ID="enterpriseBean";
    
    
    static public final String [] LOCAL_TRANSACTION_BOUNDARY_TYPES={"BeanMethod", "ActivitySession"};
    static public final String [] LOCAL_TRANSACTION_RESOLVER_TYPES={"Application","ContainerAtBoundary"};
    static public final String [] LOCAL_TRANSACTION_UNRESOLVED_ACTION_TYPES={"Commi","Rollback"};
    
    static public final String LOCAL_TRANSACTION="LocalTransaction";
    static public final String LOCAL_TRANSACTION_XMI_ID="LocalTransactionXmiId";
    static public final String LOCAL_TRANSACTION_UNRESOLVED_ACTION="LocalTransactionUnresolverAction";
    static public final String LOCAL_TRANSACTION_BOUNDARY="LocalTransactionBoundary";
    static public final String LOCAL_TRANSACTION_RESOLVER="LocalTransactionResolver";
    
    static public final String CURRENT_BACKEND_ID_ID="currentBackendId";
    static public final String CURRENT_BACKEND_ID="CurrentBackendId";
    
    
    static public final String CMP_CONNECTION_FACTORY="CMPConnectionFactory";
    static public final String CMP_CONNECTION_FACTORY_ID="cmpConnectionFactory";
    static public final String CMP_CONNECTION_FACTORY_XMI_ID="CMPConnectionFactoryXmiId";
    static public final String CMP_CONNECTION_FACTORY_JNDI_NAME="CMPConnectionFactoryJndiName";
    static public final String CMP_CONNECTION_FACTORY_RES_AUTH="CMPConnectionFactoryResAuth";
    static public final String CMP_RES_AUTH_TYPE_PER_CONNECTION_FACTORY="Per_Connection_Factory";
    static public final String CMP_RES_AUTH_TYPE_CONTAINER="Container";
    static public final String []CMP_RES_AUTH_TYPES={CMP_RES_AUTH_TYPE_PER_CONNECTION_FACTORY,CMP_RES_AUTH_TYPE_CONTAINER};
    static public final String DEFAULT_CMP_CONNECTION_FACTORY_ID="defaultCMPConnectionFactory";
    static public final String DEFAULT_CMP_CONNECTION_FACTORY="DefaultCMPConnectionFactory";
    
    
    static public final String RES_REF_BINDINGS_ID="resRefBindings";
    static public final String EJB_REF_BINDINGS_ID="ejbRefBindings";
    static public final String RES_ENV_REF_BINDINGS_ID="resourceEnvRefBindings";
    
    static public final String BINDING_REFERENCE_TYPE_RESOURCE="Resource Reference";
    static public final String BINDING_REFERENCE_TYPE_EJB="Ejb Reference";
    static public final String BINDING_REFERENCE_TYPE_RESOURCE_ENV="Resource Environment Reference";
    static public final String [] BINDING_REFERENCE_TYPES={
        BINDING_REFERENCE_TYPE_RESOURCE,
        BINDING_REFERENCE_TYPE_EJB,
        BINDING_REFERENCE_TYPE_RESOURCE_ENV};
    
}