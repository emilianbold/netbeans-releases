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
 * StorageBeanFactory.java
 *
 * Created on February 15, 2005, 9:51 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;

import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.app.Web;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.AsContext;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.IorSecurityConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SasContext;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.TransportConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmInuse;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptors;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement;

import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo;
import org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap;
import org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty;
import org.netbeans.modules.j2ee.sun.dd.api.web.Cache;
import org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping;
import org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper;
import org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField;
import org.netbeans.modules.j2ee.sun.dd.api.web.DefaultHelper;
import org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig;
import org.netbeans.modules.j2ee.sun.dd.api.web.SessionManager;
import org.netbeans.modules.j2ee.sun.dd.api.web.ManagerProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.StoreProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.SessionProperties;
import org.netbeans.modules.j2ee.sun.dd.api.web.CookieProperties;

import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.LoginConfig;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort;
import org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty;
import org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;

import org.netbeans.modules.j2ee.sun.dd.api.common.JavaMethod;
import org.netbeans.modules.j2ee.sun.dd.api.common.Message;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;
import org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointAtEndOfMethod;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Method;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PrefetchDisabled;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod;

/** Factory to create beans via the DDAPI, outside any existing graph.  These
 *  are primarily used for internal data storage by the DConfigBeans and used
 *  to be constructed directly when we used the BaseBeans directly (NB 3.6, 4.0 plugins.)
 *  With the DDAPI, this is no longer possible and this is a temporary solution
 *  until we switch over fully to have the DDAPI manage the graph as well as
 *  the construction of the beans.
 *
 * @author Peter Williams
 */
public class StorageBeanFactory {

    // default factory to use.
//    private static StorageBeanFactory defaultFactory = new StorageBeanFactory();
    private static StorageBeanFactory defaultFactory;
    
    static {
        try {
            defaultFactory = new StorageBeanFactory();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    // DDAPI provider
    private DDProvider provider;
    
    // -- Cache of known parent beans --
    //
    // sun-application graph
    private SunApplication sunApplication;
    
    // sun-ejb-jar graph
    private SunEjbJar sunEjbJar;
    private EnterpriseBeans enterpriseBeans;
    private Ejb ejb;
    private MdbResourceAdapter mdbResourceAdapter;
    private ActivationConfig activationConfig;
    private IorSecurityConfig iorSecurityConfig;
    private PmDescriptors pmDescriptors;
    private Cmp cmp;
    private OneOneFinders oneOneFinders;
    private CmpResource cmpResource;
    
    // sun-web-app graph
    private SunWebApp sunWebApp;
    private Cache webAppCache;
    private CacheMapping cacheMapping;
    private SessionConfig sessionConfig;
    private SessionManager sessionManager;
    private LocaleCharsetInfo localeCharsetInfo;
    
    // common graph
    private ResourceRef resourceRef;
    private ServiceRef serviceRef;
    private PortInfo portInfo;
    private WebserviceEndpoint webserviceEndpoint;
    
    private MessageSecurityBinding messageSecurityBinding;
    private MessageSecurity messageSecurity;
    private Message message;
    private FlushAtEndOfMethod flushAtEndOfMethod;
    private Method method;
    private PrefetchDisabled prefetchDisabled;

    public StorageBeanFactory() {
        provider = DDProvider.getDefault();
        
        sunApplication = (SunApplication) provider.newGraph(SunApplication.class);
        
        sunEjbJar = (SunEjbJar) provider.newGraph(SunEjbJar.class);
        enterpriseBeans = sunEjbJar.newEnterpriseBeans();
        ejb = enterpriseBeans.newEjb();
        mdbResourceAdapter = ejb.newMdbResourceAdapter();
        activationConfig = mdbResourceAdapter.newActivationConfig();
        iorSecurityConfig = ejb.newIorSecurityConfig();
        pmDescriptors = enterpriseBeans.newPmDescriptors();
        cmpResource = enterpriseBeans.newCmpResource();
        cmp = ejb.newCmp();
        oneOneFinders = cmp.newOneOneFinders();
    
        try{
            prefetchDisabled = cmp.newPrefetchDisabled();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
            //System.out.println("Not Supported Version");      //NOI18N
        }

        sunWebApp = (SunWebApp) provider.newGraph(SunWebApp.class);
        webAppCache = sunWebApp.newCache();
        cacheMapping = webAppCache.newCacheMapping();
        sessionConfig = sunWebApp.newSessionConfig();
        sessionManager = sessionConfig.newSessionManager();
        localeCharsetInfo = sunWebApp.newLocaleCharsetInfo();

        resourceRef = sunWebApp.newResourceRef();
        serviceRef = sunWebApp.newServiceRef();
        portInfo = serviceRef.newPortInfo();
        webserviceEndpoint = ejb.newWebserviceEndpoint();
 
        try{
            messageSecurityBinding = webserviceEndpoint.newMessageSecurityBinding();
            messageSecurity = messageSecurityBinding.newMessageSecurity();
            message = messageSecurity.newMessage();
            flushAtEndOfMethod = ejb.newFlushAtEndOfMethod();
            method = flushAtEndOfMethod.newMethod();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
            //System.out.println("Not Supported Version");      //NOI18N
        }
    }
    
    public static StorageBeanFactory getDefault() {
        return defaultFactory;
    }
    
    // Sun Application beans
    public Web createWeb() {
        return sunApplication.newWeb();
    }
    
    // Sun Ejb Jar beans
    public Ejb createEjb() {
        return enterpriseBeans.newEjb();
    }
    
    public Cmp createCmp() {
        return ejb.newCmp();
    }
    
    public CmpResource createCmpResource() {
        return enterpriseBeans.newCmpResource();
    }
    
    public BeanPool createBeanPool() {
        return ejb.newBeanPool();
    }
    
    public BeanCache createBeanCache() {
        return ejb.newBeanCache();
    }
    
    public ActivationConfig createActivationConfig() {
        return mdbResourceAdapter.newActivationConfig();
    }

    public ActivationConfigProperty createActivationConfigProperty() {
        return activationConfig.newActivationConfigProperty();
    }
    
    public MdbConnectionFactory createMdbConnectionFactory() {
        return ejb.newMdbConnectionFactory();
    }

    public MdbResourceAdapter createMdbResourceAdapter() {
        return ejb.newMdbResourceAdapter();
    }
    
    public IorSecurityConfig createIorSecurityConfig() {
        return ejb.newIorSecurityConfig();
    }
    
    public AsContext createAsContext() {
        return iorSecurityConfig.newAsContext();
    }
    
    public TransportConfig createTransportConfig() {
        return iorSecurityConfig.newTransportConfig();
    }
    
    public SasContext createSasContext() {
        return iorSecurityConfig.newSasContext();
    }
    
    public DefaultResourcePrincipal createDefaultResourcePrincipal() {
        return resourceRef.newDefaultResourcePrincipal();
    }
    
    public PmInuse createPmInuse() {
        return pmDescriptors.newPmInuse();
    }
    
    public PmDescriptor createPmDescriptor() {
        return pmDescriptors.newPmDescriptor();
    }
    
    public PmDescriptors createPmDescriptors() {
        return enterpriseBeans.newPmDescriptors();
    }
    
    public PropertyElement createPropertyElement() {
        return cmpResource.newPropertyElement();
    }
    
    public Finder createFinder() {
        return oneOneFinders.newFinder();
    }
    
    // Sun Web App beans
    public Servlet createServlet() {
        return sunWebApp.newServlet();
    }
    
    public JspConfig createJspConfig() {
        return sunWebApp.newJspConfig();
    }
    
    public LocaleCharsetInfo createLocaleCharsetInfo() {
        return sunWebApp.newLocaleCharsetInfo();
    }
    
    public LocaleCharsetMap createLocaleCharsetMap() {
        return localeCharsetInfo.newLocaleCharsetMap();
    }
    
    public WebProperty createWebProperty() {
        return sunWebApp.newWebProperty();
    }

    public Cache createCache() {
        return sunWebApp.newCache();
    }
    
    public Cache createCache_NoDefaults() {
        Cache cache = createCache();

        // clear default values that we can't prevent from being set anymore.
        cache.setMaxEntries(null);
        cache.setEnabled(null);
        cache.setTimeoutInSeconds(null);
        
        return cache;
    }
    
    public CacheMapping createCacheMapping() {
        return webAppCache.newCacheMapping();
    }
    
    public CacheMapping createCacheMapping_NoDefaults() {
        CacheMapping cacheMapping = createCacheMapping();
        
        // clear default values that we can't prevent from being set anymore.
        cacheMapping.setTimeoutScope(null);
        cacheMapping.setRefreshFieldScope(null);
        
        return cacheMapping;
    }
    
    public ConstraintField createConstraintField() {
        return cacheMapping.newConstraintField();
    }
    
    public DefaultHelper createDefaultHelper() {
        return webAppCache.newDefaultHelper();
    }

    public CacheHelper createCacheHelper() {
        return webAppCache.newCacheHelper();
    }

    public SessionConfig createSessionConfig() {
        return sunWebApp.newSessionConfig();
    }
    
    public SessionManager createSessionManager() {
        return sessionConfig.newSessionManager();
    }
    
    public ManagerProperties createManagerProperties() {
        return sessionManager.newManagerProperties();
    }
    
    public StoreProperties createStoreProperties() {
        return sessionManager.newStoreProperties();
    }
    
    public SessionProperties createSessionProperties() {
        return sessionConfig.newSessionProperties();
    }
    
    public CookieProperties createCookieProperties() {
        return sessionConfig.newCookieProperties();
    }
    
    // Common beans
    
	// !PW Very important to note that these are all common beans.  Even though
    // sun-web-app is used to create them, some or all of them can be requested 
    // by sun-ejb-jar sun-application, sun-application-client, or sun-connector.
    // They must remain in the common area of the DDAPI or this code will have
    // to be updated accordingly.
    public EjbRef createEjbRef() {
        return sunWebApp.newEjbRef();
    }
    
    public MessageDestination createMessageDestination() {
        return sunWebApp.newMessageDestination();
    }
    
    public PortInfo createPortInfo() {
        return serviceRef.newPortInfo();
    }

    public CallProperty createCallProperty() {
        return portInfo.newCallProperty();
    }
    
    public StubProperty createStubProperty() {
        return portInfo.newStubProperty();
    }

    public ResourceEnvRef createResourceEnvRef() {
        return sunWebApp.newResourceEnvRef();
    }
    
    public ResourceRef createResourceRef() {
        return sunWebApp.newResourceRef();
    }
    
    public SecurityRoleMapping createSecurityRoleMapping() {
        return sunWebApp.newSecurityRoleMapping();
    }
    
    public ServiceRef createServiceRef() {
        return sunWebApp.newServiceRef();
    }
    
    public WebserviceDescription createWebserviceDescription() {
        return sunWebApp.newWebserviceDescription();
    }
    
    public WebserviceEndpoint createWebserviceEndpoint() {
        return ejb.newWebserviceEndpoint();
    }
    
    public LoginConfig createLoginConfig() {
        return webserviceEndpoint.newLoginConfig();
    }
    
    public WsdlPort createWsdlPort() {
        return portInfo.newWsdlPort();
    }

    public JavaMethod createJavaMethod() {
        return message.newJavaMethod();
    }

    public Message createMessage() {
        return messageSecurity.newMessage();
    }


    public MessageSecurityBinding createMessageSecurityBinding() {
        try{
            return webserviceEndpoint.newMessageSecurityBinding();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
        }
        return null;
    }

    public MethodParams createMethodParams() {
        return method.newMethodParams();
    }

    public MessageSecurity createMessageSecurity() {
        return messageSecurityBinding.newMessageSecurity();
    }


    public FlushAtEndOfMethod createFlushAtEndOfMethod() {
        try{
            return ejb.newFlushAtEndOfMethod();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
            //System.out.println("Not Supported Version");      //NOI18N
        }
        return null;
    }


    public CheckpointAtEndOfMethod createCheckpointAtEndOfMethod() {
        try{
            return ejb.newCheckpointAtEndOfMethod();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException ex){
            //System.out.println("Not Supported Version");      //NOI18N
        }
        return null;
    }


    public Method createMethod() {
        return flushAtEndOfMethod.newMethod();
    }


    public PrefetchDisabled createPrefetchDisabled() {
        try{
            return cmp.newPrefetchDisabled();
        }catch(org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException e){
            //System.out.println("Not Supported Version");      //NOI18N
        }
        return null;
    }


    public QueryMethod createQueryMethod() {
        return prefetchDisabled.newQueryMethod();
    }
}
