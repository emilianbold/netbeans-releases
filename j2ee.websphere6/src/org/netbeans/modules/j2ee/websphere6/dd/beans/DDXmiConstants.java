/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
    
    public  static final String TYPE="Type";
    public  static final String NAME="Name";
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
    static public final String GROUPS_ID="groups";
    public  static final String USERS_ID="users";
    public static final String ROLE_ID="role";
    static public final String SPECIAL_SUBJECTS_ID="specialSubjects";
    
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
    
    
    public static final  String ALT_ROOT="AltRoot";
    public static final  String ALT_BINDINGS_ID="altBindings";
    public static final  String ALT_EXTENSIONS_ID="altExtensions";
    public static final  String ALT_ROOT_ID="altRoot";
    //public static final  String APPROOT_ID="appRoot";
    
    public static final String MODULE="Module";
    public static final String MODULE_ID="module";
    static public final String MODULE_XMI_TYPE="ModuleXmiType";
    static public final String MODULE_HREF="ModuleHref";
    static public final String MODULE_EXTENSIONS_ID="moduleExtensions";
    static public final String MODULE_EXTENSIONS="ModuleExtensions";
    static public final String MODULE_EXTENSIONS_XMI_ID="ModuleExtensionsXmiId";
    static public final String MODULE_EXTENSIONS_ALT_ROOT="ModuleExtensionsAltRoot";
    static public final String MODULE_EXTENSIONS_ALT_EXTENSIONS="ModuleExtensionsAltExtensions";
    static public final String MODULE_EXTENSIONS_ALT_BINDINGS="ModuleExtensionsAltBindings";
    static public final String MODULE_EXTENSIONS_XMI_TYPE="ModuleExtensionsXmiType";
    
    
    static public final  String MODULE_TYPE_EJB_STRING="application:EjbModule";
    static public final  String MODULE_TYPE_WEB_STRING="application:WebModule";
    static public final  String MODULE_TYPE_JAVA_CLIENT_MODULE_STRING="application:JavaClientModule";
    static public final  String MODULE_TYPE_CONNECTOR_MODULE_STRING="application:ConnectorModule";
    static public final  String [] MODULE_TYPE_STRINGS= {
        MODULE_TYPE_EJB_STRING,
        MODULE_TYPE_WEB_STRING,
        MODULE_TYPE_JAVA_CLIENT_MODULE_STRING,
        MODULE_TYPE_CONNECTOR_MODULE_STRING};
    
    
    static public final  String MODULE_EXTENSIONS_TYPE_EJB_STRING="applicationext:EjbModuleExtension";
    static public final  String MODULE_EXTENSIONS_TYPE_WEB_STRING="applicationext:WebModuleExtension";
    static public final  String MODULE_EXTENSIONS_TYPE_JAVA_CLIENT_MODULE_STRING="applicationext:JavaClientModuleExtension";
    static public final  String MODULE_EXTENSIONS_TYPE_CONNECTOR_MODULE_STRING="applicationext:ConnectorModuleExtension";
    static public final  String [] MODULE_EXTENSIONS_TYPE_STRINGS= {
        MODULE_EXTENSIONS_TYPE_EJB_STRING,
        MODULE_EXTENSIONS_TYPE_WEB_STRING,
        MODULE_EXTENSIONS_TYPE_JAVA_CLIENT_MODULE_STRING,
        MODULE_EXTENSIONS_TYPE_CONNECTOR_MODULE_STRING};
    
    
    
    static public final  String MODULE_TYPE_EJB="Ejb";
    static public final  String MODULE_TYPE_WEB="Web";
    static public final  String MODULE_TYPE_JAVA_CLIENT="JavaClient";
    static public final  String MODULE_TYPE_CONNECTOR="Connector";
    static public final  String [] MODULE_TYPES={
        MODULE_TYPE_EJB,
        MODULE_TYPE_WEB,
        MODULE_TYPE_JAVA_CLIENT,
        MODULE_TYPE_CONNECTOR
    };
    
    
    
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
    
    static public final  String TYPE_APP_BND_ID="applicationbnd:ApplicationBinding";
    
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
    static public final String RES_REF_BINDINGS_XMI_TYPE="ResRefBindingsXmiType";
    static public final String RES_REF_BINDINGS_JNDI_NAME="ResRefBindingsJndiName";
    static public final String RES_REF_BINDINGS_ROOT="ResRefBindingsRoot";
    static public final String RES_REF_BINDINGS="ResRefBindings";
    
    static public final String RES_ENV_REF_BINDINGS_XMI_ID="ResEnvRefBindingsXmiId";
    static public final String RES_ENV_REF_BINDINGS_XMI_TYPE="ResEnvRefBindingsXmiType";
    static public final String RES_ENV_REF_BINDINGS_JNDI_NAME="ResEnvRefBindingsJndiName";
    static public final String RES_ENV_REF_BINDINGS_ROOT="ResEnvRefBindingsRoot";
    static public final String RES_ENV_REF_BINDINGS="ResEnvRefBindings";
    
    
    static public final String EJB_REF_BINDINGS_XMI_ID="EjbRefBindingsXmiId";
    static public final String EJB_REF_BINDINGS_XMI_TYPE="EjbRefBindingsXmiType";
    static public final String EJB_REF_BINDINGS_JNDI_NAME="EjbRefBindingsJndiName";
    static public final String EJB_REF_BINDINGS_ROOT="EjbRefBindingsRoot";
    static public final String EJB_REF_BINDINGS="EjbRefBindings";
    static public final String EJB_BINDINGS_ID="ejbBindings";
    
    static public final String BINDING_EJB_REF_TYPE_LOCAL_STRING = "common:EJBLocalRef";
    static public final String BINDING_EJB_REF_TYPE_REMOTE_STRING = "common:EJBRemoteRef";
    static public final String [] BINDING_EJB_REF_TYPES = new String [] {
        BINDING_EJB_REF_TYPE_LOCAL_STRING,
        BINDING_EJB_REF_TYPE_REMOTE_STRING
    };
    
    static public final String EXTENDED_SERVLETS="ExtendedServlets";
    static public final String EXTENDED_SERVLETS_ID="extendedServlets";
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
    
    static public final String PAGES_ID="pages";
    static public final String PAGES   = "Pages";	// NOI18N
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
    static public final String EJB_EXTENSIONS_ID="ejbExtensions";
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
    static public final String LOCAL_TRANSACTION_ID="localTransaction";
    static public final String LOCAL_TRANSACTION="LocalTransaction";
    static public final String LOCAL_TRANSACTION_XMI_ID="LocalTransactionXmiId";
    static public final String LOCAL_TRANSACTION_UNRESOLVED_ACTION="LocalTransactionUnresolverAction";
    static public final String LOCAL_TRANSACTION_BOUNDARY="LocalTransactionBoundary";
    static public final String LOCAL_TRANSACTION_RESOLVER="LocalTransactionResolver";
    static public final String UNRESOLVED_ACTION_ID="unresolvedAction";
    static public final String BOUNDARY_ID="boundary";
    static public final String RESOLVER_ID="resolver";
    
    
    static public final String EXTENDED_SERVLET="ExtendedServlet";
    static public final String MARKUP_LANGUAGES="MarkupLanguages";
    static public final String EXTENDED_SERVLET_ID="extendedServlet";
    static public final String MARKUP_LANGUAGES_ID="markupLanguages";
    
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
    static public final String RES_ENV_REF_BINDINGS_ID="resEnvRefBindings";
    
    static public final String BINDING_REFERENCE_TYPE_RESOURCE="Resource Reference";
    static public final String BINDING_REFERENCE_TYPE_EJB="Ejb Reference";
    static public final String BINDING_REFERENCE_TYPE_RESOURCE_ENV="Resource Environment Reference";
    static public final String [] BINDING_REFERENCE_TYPES={
        BINDING_REFERENCE_TYPE_RESOURCE,
        BINDING_REFERENCE_TYPE_EJB,
        BINDING_REFERENCE_TYPE_RESOURCE_ENV};
    
    
    public  static final String AUTH_TABLE_ID="authorizationTable";
    public  static final String AUTH_TABLE="AuthorizationTable";
    public  static final String AUTH_TABLE_XMI_ID=AUTH_TABLE+XMI_ID;
    public  static final String AUTHORIZATIONS="authorizations";
    public  static final String AUTHORIZATION = "Authorization";
    public  static final String AUTH_ID=AUTHORIZATION+XMI_ID;
    
    
    public  static final String RUN_AS_MAP="RunAsMap";
    public  static final String RUN_AS_MAP_ID="runAsMap";
    public  static final String RUN_AS_MAP_XMI_ID=RUN_AS_MAP+XMI_ID;
    
    public static final String ROLE   = "Role";	// NOI18N
    public static final String GROUPS = "Groups";	// NOI18N
    public static final String USERS  = "Users";
    public static final String GROUP = "Group";	// NOI18N
    public static final String USER  = "User";
    
    public static final String ROLE_HREF   = "RoleHref";
    public static final String GROUPS_XMI_ID = GROUPS+XMI_ID;
    public static final String GROUPS_NAME   = GROUPS+NAME;
    public static final String USERS_XMI_ID = USERS+XMI_ID;
    public static final String USERS_NAME   = USERS+NAME;
    
    public static final String USERS_DEFAULT_NAME="samples";
    public static final String GROUPS_DEFAULT_NAME="sampadmn";
    
    
    static public final String SPECIAL_SUBJECTS="SpecialSubjects";
    static public final String SPECIAL_SUBJECTS_TYPE=SPECIAL_SUBJECTS+TYPE;
    static public final String SPECIAL_SUBJECTS_XMI_ID=SPECIAL_SUBJECTS+XMI_ID;
    static public final String SPECIAL_SUBJECTS_NAME=SPECIAL_SUBJECTS+NAME;
    
    static public final String SPECIAL_SUBJECTS_TYPE_EVERYONE_STRING="applicationbnd:Everyone";
    static public final String SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS_STRING="applicationbnd:AllAuthenticatedUsers";
    static public final String SPECIAL_SUBJECTS_TYPE_SERVER_STRING="applicationbnd:Server";
    
    static public final String SPECIAL_SUBJECTS_TYPE_EVERYONE="Everyone";
    static public final String SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS="AllAuthenticatedUsers";
    
    
    static public final String []SPECIAL_SUBJECTS_TYPES= {
        SPECIAL_SUBJECTS_TYPE_EVERYONE,
        SPECIAL_SUBJECTS_TYPE_ALL_AUTHENTICATED_USERS
    };
    
    
    static public final String SHARED_SESSION_CONTEXT_ID="sharedSessionContext";
    static public final String SHARED_SESSION_CONTEXT="SharedSessionContext";
    
    
}
