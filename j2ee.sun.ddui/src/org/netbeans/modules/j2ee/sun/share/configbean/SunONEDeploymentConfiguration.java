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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.configbean;

import com.sun.jdo.api.persistence.mapping.ejb.EJBInfoHelper;
import java.beans.PropertyVetoException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentConfigurationInterface;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.DDException;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.share.Constants;
import org.netbeans.modules.j2ee.sun.share.config.ConfigurationStorage;
import org.netbeans.modules.j2ee.sun.share.config.DDRoot;
import org.netbeans.modules.j2ee.sun.share.config.DDFilesListener;
import org.netbeans.modules.j2ee.sun.share.config.StandardDDImpl;
import org.netbeans.modules.j2ee.sun.share.configbean.templates.SunDDWizardIterator.XmlFileCreator;
import org.netbeans.modules.j2ee.sun.share.plan.DeploymentPlan;
import org.netbeans.modules.j2ee.sun.share.plan.FileEntry;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NotImplementedException;
import org.openide.util.RequestProcessor;
import org.xml.sax.SAXException;


/** Manages the deployment plan I/O and access for initializing DConfigBeans
 *
 * @author Vince Kraemer
 * @author Peter Williams
 */ 
public class SunONEDeploymentConfiguration implements Constants, SunDeploymentConfigurationInterface {

    // !PW FIXME workaround for linking sun descriptor file DataObjects w/ the 
    // correct Deployment Configuration object.  Key is primary File for configuration.
    private static WeakHashMap<File, WeakReference<SunONEDeploymentConfiguration>> configurationMap = 
            new WeakHashMap<File, WeakReference<SunONEDeploymentConfiguration>>();

    public static void addConfiguration(File key, SunONEDeploymentConfiguration config) {
        configurationMap.put(key, new WeakReference<SunONEDeploymentConfiguration>(config));
    }

    public static void removeConfiguration(File key) {
        configurationMap.remove(key);
    }

    public static SunONEDeploymentConfiguration getConfiguration(File key) {
        SunONEDeploymentConfiguration config = null;
        WeakReference<SunONEDeploymentConfiguration> ref = configurationMap.get(key);
        if (ref != null) {
            config = ref.get();
        }
        return config;
    }

    //private DeployableObject dObj;
    private Map contentMap = new HashMap();
    private Map beanMap = new HashMap();
    private Map priorBeanMap = new HashMap();

    private final Object storageMonitor = new Object();
    private ConfigurationStorage storage = null;

    /*
     * value to hold the module name used by the IDE to define the deployable object
     * this is a jsr88 extension for directory deployment: we need to find a good
     * dir name to put the bits that will be deployed.
     * */
    private String deploymentModuleName = "_default_"; // NOI18N
    /** Configuration files and the directory they belong in, as specified by init.
     */
    private File[] configFiles;
    private File resourceDir;
    private boolean keepUpdated;

    private DDFilesListener ddFilesListener;

    private static final RequestProcessor resourceProcessor = new RequestProcessor("sun-resource-ref"); // NOI18N
    
    /** Available server targets:
     */
    private ASDDVersion appServerVersion;
    private ASDDVersion minASVersion;
    private ASDDVersion maxASVersion;
    private boolean deferredAppServerChange;
    private J2eeModule module;


    /** Creates a new instance of SunONEDeploymentConfiguration
     * @param dObj The deployable object this object configures
     * @param dm The DeploymentManager that created the DeploymentConfiguration
     */
    public SunONEDeploymentConfiguration(javax.enterprise.deploy.model.DeployableObject dObj) {
        //this.dObj = dObj;
        // Default to 8.1 in new beans.  This is set by the bean parser
        // in the appropriate root type, if reading from existing file(s).
        this.appServerVersion = ASDDVersion.SUN_APPSERVER_8_1;
        this.deferredAppServerChange = false;
    }

    /** Creates a new instance of SunONEDeploymentConfiguration
     * @param dObj The deployable object this object configures
     * @param dm The DeploymentManager that created the DeploymentConfiguration
     */
    public SunONEDeploymentConfiguration(J2eeModule module) {
        this.module = module;

        // Default to 8.1 in new beans.  This is set by the bean parser
        // in the appropriate root type, if reading from existing file(s).
        this.appServerVersion = ASDDVersion.SUN_APPSERVER_8_1;
        this.deferredAppServerChange = false;
    }

    public J2eeModule getJ2eeModule() {
        return module;
    }


    /**
     * SunONEDeploymentConfiguration initialization. This method should be called before
     * this class is being used.
     *
     * @param configFiles Sun specific DD files referenced by this J2EE module,
     *   e.g. sun-web.xml, sun-ejb-jar.xml, sun-cmp-mappings.xml, etc.
     * @param resourceDir Directory that the sun resource files will be created in.
     */
    public void init(File[] cfgFiles, File resourceDir, boolean keepUpdated) throws ConfigurationException {
        // cfgFiles array checked for validity by caller in appsrv module (ConfigurationSupportImpl.java)
        configFiles = new File[cfgFiles.length];
        for (int i = 0; i < cfgFiles.length; i++) {
            // Array is too short to justify arraycopy.
            configFiles[i] = cfgFiles[i];
        }

        // configFiles array is just one file except for EJB modules, where it's two.
        //
        // IZ 76455 - A regression is now causing sun-cmp-mappings.xml to be the first
        // entry now.  Rather than isolate and fix the regression, I'm just going to
        // normalize the array here to ensure that sun-ejb-jar.xml is always first
        // since other code depends on the main configuration file being the first entry
        // in this list.
        //
        if (configFiles.length == 2 && configFiles[1] != null && "sun-ejb-jar.xml".equals(configFiles[1].getName())) { // NOI18N
            File tmp = configFiles[0];
            configFiles[0] = configFiles[1];
            configFiles[1] = tmp;
        }

        this.resourceDir = resourceDir;
        this.keepUpdated = keepUpdated;

        addConfiguration(configFiles[0], this);

        // !PW FIXME web freeform project does not implement J2eeModulePrvoider.  This section
        // and any related code could be reworked to use WebModule api directly and thereby enable
        // sun-web.xml gui descriptor editing for web freeform.
        J2eeModuleProvider provider = getProvider(configFiles[0].getParentFile());
        if (provider == null) {
            throw new ConfigurationException("No Project and/or J2eeModuleProvider located for " + configFiles[0].getPath()); // NOI18N
        }

        // -------- prototype ------- checking server version
//        provider.addInstanceListener(this);
//        String instance = provider.getServerInstanceID();
//        String serverType = provider.getServerID();
//        System.out.println("SunONEDeploymentConfiguration::init: instance: " + instance + ", serverType: " + serverType);
// -------- end prototype ------- checking server version
        try {
            // Determine what the available server types can be (WS 6.0, AS 7.0, AS 8.1, AS 9.0)
            // based on j2ee spec version.
            Object mt = module.getModuleType();
            ModuleType moduleType = mt instanceof ModuleType ? (ModuleType) mt : null;
            String moduleVersion = module.getModuleVersion();
            minASVersion = computeMinASVersion(moduleType, moduleVersion);
            maxASVersion = computeMaxASVersion();

            appServerVersion = maxASVersion;
//            // Connectors are not supported by the configuration editor since sun-ra.xml is deprecated.
//            // To avoid failing initialization here when encountering that file, we will ignore it explicitly.
//            if("sun-ra.xml".equals(configFiles[0].getName())) {
//                return;
//            }
            if (!cfgFiles[0].exists()) {
                // If module is J2EE 1.4 (or 1.3), or this is a web app (where we have
                // a default property even for JavaEE5), then copy the default template.
                J2EEBaseVersion j2eeVersion = J2EEBaseVersion.getVersion(moduleType, moduleVersion);
                boolean isJ2ee14 = (j2eeVersion != null) ? (J2EEVersion.J2EE_1_4.compareSpecification(j2eeVersion) >= 0) : false;
                if (J2eeModule.WAR.equals(moduleType) || isJ2ee14) {
                    try {
                        createDefaultSunDD(configFiles[0]);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String defaultMessage = " trying to create " + configFiles[0].getPath(); // Requires I18N
                        displayError(ex, defaultMessage);
                    }
                }
            }
        } catch (RuntimeException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }

    public void dispose() {
//        J2eeModuleProvider provider = getProvider(configFiles[0].getParentFile());
//        if(provider != null) {
//            provider.removeInstanceListener(this);
//            String instance = provider.getServerInstanceID();
//            String serverType = provider.getServerID();
//            System.out.println("SunONEDeploymentConfiguration::dispose: instance: " + instance + ", serverType: " + serverType);
//        }
        SunONEDeploymentConfiguration storedCfg = getConfiguration(configFiles[0]);
        if (storedCfg != this) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Stored DeploymentConfiguration (" + storedCfg + ") instance not the one being disposed of (" + this + ").");
        }

        if (storedCfg != null) {
            removeConfiguration(configFiles[0]);
        }
    }

    // !PW This is a total waste of space for something so small.  Fixed array lookup maybe?
    private static Map<Object, String> standardDDNameMap = new HashMap<Object, String>(11);
    private static Map<Object, String> webserviceDDNameMap = new HashMap<Object, String>(7);
    static {
        standardDDNameMap.put(J2eeModule.WAR, J2eeModule.WEB_XML);
        standardDDNameMap.put(J2eeModule.EJB, "ejb-jar.xml");
        standardDDNameMap.put(J2eeModule.EAR, "application.xml");
        standardDDNameMap.put(J2eeModule.CLIENT, "application-client.xml");
        webserviceDDNameMap.put(J2eeModule.WAR, J2eeModule.WEBSERVICES_XML);
        webserviceDDNameMap.put(J2eeModule.EJB, "webservices.xml");
    }

    public org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = null;
        String ddName = standardDDNameMap.get(module.getModuleType());
        if (ddName != null) {
            File ddFile = module.getDeploymentConfigurationFile(ddName);
            if (ddFile.exists()) {
                FileObject ddFO = FileUtil.toFileObject(ddFile);
                try {
                    if (J2eeModule.WAR == module.getModuleType()) {
                        stdRootDD = org.netbeans.modules.j2ee.dd.api.web.DDProvider.getDefault().getDDRoot(ddFO);
                    } else if (J2eeModule.EJB == module.getModuleType()) {
                        stdRootDD = org.netbeans.modules.j2ee.dd.api.ejb.DDProvider.getDefault().getDDRoot(ddFO);
                    } else if (J2eeModule.CLIENT == module.getModuleType()) {
                        stdRootDD = org.netbeans.modules.j2ee.dd.api.client.DDProvider.getDefault().getDDRoot(ddFO);
                    } else if (J2eeModule.EAR == module.getModuleType()) {
                        stdRootDD = org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(ddFO);
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        return stdRootDD;
    }

    public org.netbeans.modules.j2ee.dd.api.webservices.Webservices getWebServicesRootDD() {
        org.netbeans.modules.j2ee.dd.api.webservices.Webservices wsRootDD = null;
        String ddName = webserviceDDNameMap.get(module.getModuleType());
        if (ddName != null) {
            File ddFile = module.getDeploymentConfigurationFile(ddName);
            if (ddFile.exists()) {
                FileObject ddFO = FileUtil.toFileObject(ddFile);
                try {
                    wsRootDD = org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault().getDDRoot(ddFO);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        return wsRootDD;
    }

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        return module.getMetadataModel(type);
    }

    public void updateResourceDir(File resourceDir) {
        this.resourceDir = resourceDir;
    }

    private void postResourceError(String resourceMsg) {
        // Unable to create JDBC data source for CMP.
        // JNDI name of CMP resource field not set.
        String folderMsg;
        String projectName = getProjectName(configFiles[0]);
        if (projectName != null) {
            folderMsg = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoResourceFolderForProject", projectName); // NOI18N
        } else {
            folderMsg = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoResourceFolderUnknown"); // NOI18N
        }

        final String text = folderMsg + " " + resourceMsg;
        resourceProcessor.post(new Runnable() {

            public void run() {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(text, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        });
    }

//    public void ensureResourceDefined(StandardDDImpl ddBean) {
//        // Determine type of ddbean we have so we know what resource to create.
//        String xpath = ddBean.getXpath();
//        int finalSlashIndex = xpath.lastIndexOf('/') + 1;
//        String type = (finalSlashIndex < xpath.length()) ? xpath.substring(finalSlashIndex) : ""; //NOI18N
//        if ("message-driven".equals(type)) { //NOI18N
//            // Find the DConfigBean for this ddBean.  This is actually quite complicated since
//            // the DDBean passed in is from j2eeserver, not from the DDBean tree used and managed
//            // by the plugin.
//            BaseEjb theEjbDCB = getEjbDConfigBean(ddBean);
//
//            if (theEjbDCB == null) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("EJB DConfigBean cannot be found for DDBean: " + ddBean));
//                return;
//            }
//
//            ResourceConfiguratorInterface rci = getResourceConfigurator();
//            String jndiName = theEjbDCB.getJndiName(); // correct for 2.1.  For 3.0, if null, have to ask DD for default.
//            if (isEJB3()) {
//                String ejbName = getField(ddBean, "ejb-name");
//
//                if (!Utils.notEmpty(jndiName)) {
//                    // If the user has not explicitly set a jndi name in the server specific descriptor
//                    // then the jndi name is specified by the mapped-name field if set, otherwise, the ejb-name.
//                    jndiName = getField(ddBean, "mapped-name", ejbName); //NOI18N
//                }
//
//                if (!rci.isJMSResourceDefined(jndiName, resourceDir)) {
//                    // attempt to get activation-config property "destinationType" -- if present, must be one of
//                    // javax.jms.Queue or javax.jms.Topic, otherwise, default to Queue.
//                    String destinationType = "javax.jms.Queue"; // NOI18N
//                    try {
//                        DDBean[] activationNameFields = ddBean.getChildBean("activation-config/activation-config-property/activation-config-property-name");
//                        for (int i = 0; i < activationNameFields.length; i++) {
//                            if ("destinationType".equals(activationNameFields[i].getText())) {
//                                DDBean[] activationValueFields = activationNameFields[i].getChildBean("../activation-config-property-value");
//                                if (activationValueFields.length > 0) {
//                                    String value = activationValueFields[0].getText();
//                                    if (Utils.notEmpty(value)) {
//                                        destinationType = value;
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    } catch (Exception ex) {
//                        // It's possible that an exception here could normal.  Log for now and suppress
//                        // later if I confirm that it is normal.
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                    }
//
//                    // Check message destination but default to the above data if not specified.
//                    String messageDestinationName = getField(ddBean, "message-destination-link", ejbName); // NOI18N
//                    String messageDestinationType = getField(ddBean, "message-destination-type", destinationType); // NOI18N
//                    if (resourceDir == null) {
//                        // Unable to create JMS resource for message driven bean.
//                        postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoJMSResource", theEjbDCB.getEjbName())); // NOI18N
//                        // fall through and continue creating the remaining configuration elements though.
//                    } else {
//                        rci.createJMSResource(jndiName, messageDestinationType, messageDestinationName, ejbName, resourceDir);
//                    }
//                }
//            } else {
//                if (!rci.isJMSResourceDefined(jndiName, resourceDir)) {
//                    String ejbName = getField(ddBean, "ejb-name"); //NOI18N
//                    String messageDestinationName = getField(ddBean, "message-destination-link"); // NOI18N
//                    String messageDestinationType = getField(ddBean, "message-destination-type"); // NOI18N
//                    if (resourceDir == null) {
//                        // Unable to create JMS resource for message driven bean.
//                        postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoJMSResource", theEjbDCB.getEjbName())); // NOI18N
//                        // fall through and continue creating the remaining configuration elements though.
//                    } else {
//                        rci.createJMSResource(jndiName, messageDestinationType, messageDestinationName, ejbName, resourceDir);
//                    }
//
//                    MdbConnectionFactory mcf = getStorageFactory().createMdbConnectionFactory();
//                    String connectionFactoryJndiName = "jms/" + messageDestinationName + "Factory"; //NOI18N
//                    mcf.setJndiName(connectionFactoryJndiName);
//                    try {
//                        ((MDEjb) theEjbDCB).setMdbConnectionFactory(mcf);
//                    } catch (PropertyVetoException ex) {
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                    }
//
//                    org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination md = getStorageFactory().createMessageDestination();
//                    md.setMessageDestinationName(messageDestinationName);
//                    md.setJndiName(theEjbDCB.getJndiName());
//                    EjbJarRoot root = (EjbJarRoot) theEjbDCB.getParent();
//                    try {
//                        root.addMessageDestination(md);
//                    } catch (PropertyVetoException ex) {
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                    }
//                }
//            }
//        } else if ("resource-ref".equals(type)) { //NOI18N
//            if (ddBean instanceof StandardDDImpl) {
//                Object o = getDCBCache().get(ddBean);
//                if (o instanceof ResourceRef) {
//                    ResourceRef theResRefDCB = (ResourceRef) o;
//                    final String refName = getField(ddBean, "res-ref-name"); //NOI18N
//                    final String description = getField(ddBean, "description"); //NOI18N
//                    final File targetDir = resourceDir;
//
//                    // Only execute resource autocreation code if the description field has contents
//                    // (Note the contents might still fail to parse, but the parser is not accessible
//                    // from here in the current code base.)
//                    if (Utils.notEmpty(description)) {
//                        if (resourceDir == null) {
//                            // Unable to create JDBC data source for resource ref.
//                            postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoRefJdbcDataSource", theResRefDCB.getResRefName())); // NOI18N
//                            return;
//                        }
//
//                        /** !PW This mechanism is from the original incarnation of this code from
//                         *  appsrv plugin module in NB 4.1.  There should be a more stable
//                         *  way to solve any such timing issue.  This method is likelky
//                         *  unstable.
//                         */
//                        /* Creating a RequestProcessor to create resources seperately to
//                         * prevent NPE while initial loading of IDE because of call to
//                         * access DatabaseRuntimeManager.getConnection(). This NPE
//                         * causes failure while loading WebServices Registry in Runtime Tab
//                         */
//                        resourceProcessor.post(new Runnable() {
//
//                            public void run() {
//                                ResourceConfiguratorInterface rci = getResourceConfigurator();
//                                if (rci != null) {
//                                    rci.createJDBCDataSourceFromRef(refName, description, targetDir);
//                                }
//                            }
//                        }, 500);
//                    }
//                } else {
//                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "No ResourceRef DConfigBean found bound to resource-ref DDBean: " + ddBean); // NOI18N
//                }
//            } else {
//                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DDBean from wrong tree in ensureResourceDefined: " + ddBean); // NOI18N
//            }
//        } else if ("entity".equals(type)) { //NOI18N
//            ensureResourceDefinedForEjb(ddBean, null);
//        }
//    }
//
//    public void ensureResourceDefinedForEjb(StandardDDImpl ddBean, String jndiName) {
//        // Find the DConfigBean for this ddBean.  This is actually quite complicated since
//        // the DDBean passed in is from j2eeserver, not from the DDBean tree used and managed
//        // by the plugin.
//        BaseEjb theEjbDCB = getEjbDConfigBean(ddBean);
//
//        if (theEjbDCB == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("EJB DConfigBean cannot be found for DDBean: " + ddBean)); // NOI18N
//            return;
//        }
//
//        if (theEjbDCB instanceof CmpEntityEjb) {
//            ResourceConfiguratorInterface rci = getResourceConfigurator();
//            CmpEntityEjb cmpEjbDCB = (CmpEntityEjb) theEjbDCB;
//
//            if (resourceDir == null) {
//                // Unable to create JDBC data source for CMP.
//                // JNDI name of CMP resource field not set.
//                postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoCmpOrJdbcDataSource", cmpEjbDCB.getEjbName())); // NOI18N
//                return;
//            }
//
//            if (jndiName == null) {
//                String description = getField(ddBean, "description"); //NOI18N
//                jndiName = rci.createJDBCDataSourceForCmp(cmpEjbDCB.getEjbName(), description, resourceDir);
//            }
//
//            // Set the CmpResource jndi-name if not already defined.
//            if (jndiName != null) {
//                Base parentDCB = cmpEjbDCB.getParent();
//                if (parentDCB instanceof EjbJarRoot) {
//                    EjbJarRoot ejbJarRoot = (EjbJarRoot) parentDCB;
//                    CmpResource cmpResource = null;
//                    if (ejbJarRoot.getCmpResource() == null) {
//                        cmpResource = getStorageFactory().createCmpResource();
//                    } else {
//                        cmpResource = (CmpResource) ejbJarRoot.getCmpResource().clone();
//                    }
//                    cmpResource.setJndiName(jndiName);
//                    try {
//                        ejbJarRoot.setCmpResource(cmpResource);
//                    } catch (PropertyVetoException ex) {
//                        // Should never happen
//                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                    }
//                } else {
//                    // Should never happen
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("CmpEntityBean DConfigBean parent is of wrong type: " + parentDCB)); // NOI18N
//                }
//            }
//        }
//    }
//
//    private boolean isEJB3() {
//        boolean result = false;
//
//        String j2eeModuleVersion = module.getModuleVersion(); // dObj.getModuleDTDVersion();
//        EjbJarVersion ejbJarVersion = EjbJarVersion.getEjbJarVersion(j2eeModuleVersion);
//        if (EjbJarVersion.EJBJAR_3_0.compareTo(ejbJarVersion) <= 0) {
//            result = true;
//        }
//
//        return result;
//    }

    private ASDDVersion computeMinASVersion(ModuleType moduleType, String j2eeModuleVersion) {
        ASDDVersion result = ASDDVersion.SUN_APPSERVER_7_0;

        if (ModuleType.WAR.equals(moduleType)) {
            ServletVersion servletVersion = ServletVersion.getServletVersion(j2eeModuleVersion);
            if (ServletVersion.SERVLET_2_4.equals(servletVersion)) {
                result = ASDDVersion.SUN_APPSERVER_8_1;
            } else if (ServletVersion.SERVLET_2_5.equals(servletVersion)) {
                result = ASDDVersion.SUN_APPSERVER_9_0;
            }
        } else if (ModuleType.EJB.equals(moduleType)) {
            EjbJarVersion ejbJarVersion = EjbJarVersion.getEjbJarVersion(j2eeModuleVersion);
            if (EjbJarVersion.EJBJAR_2_1.equals(ejbJarVersion)) {
                result = ASDDVersion.SUN_APPSERVER_8_1;
            } else if (EjbJarVersion.EJBJAR_3_0.equals(ejbJarVersion)) {
                result = ASDDVersion.SUN_APPSERVER_9_0;
            }
        } else if (ModuleType.EAR.equals(moduleType)) {
            ApplicationVersion applicationVersion = ApplicationVersion.getApplicationVersion(j2eeModuleVersion);
            if (ApplicationVersion.APPLICATION_1_4.equals(applicationVersion)) {
                result = ASDDVersion.SUN_APPSERVER_8_1;
            } else if (ApplicationVersion.APPLICATION_5_0.equals(applicationVersion)) {
                result = ASDDVersion.SUN_APPSERVER_9_0;
            }
        } else if (ModuleType.CAR.equals(moduleType)) {
            AppClientVersion appClientVersion = AppClientVersion.getAppClientVersion(j2eeModuleVersion);
            if (AppClientVersion.APP_CLIENT_1_4.equals(appClientVersion)) {
                result = ASDDVersion.SUN_APPSERVER_8_1;
            } else if (AppClientVersion.APP_CLIENT_5_0.equals(appClientVersion)) {
                result = ASDDVersion.SUN_APPSERVER_9_0;
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Unsupported J2EE module type: " + moduleType); // NOI18N
            result = ASDDVersion.SUN_APPSERVER_8_1;
        }

        return result;
    }

    private ASDDVersion computeMaxASVersion() {
        // This is min of (current server target, 9.0) so if we can figure out the
        // target server, use that, otherwise, use 9.0.
        ASDDVersion result = getTargetAppServerVersion();
        if (result == null) {
            result = ASDDVersion.SUN_APPSERVER_9_0;
            ErrorManager.getDefault().log(ErrorManager.WARNING, NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_UnidentifiedTargetServer", result.toString())); // NOI18N
        }
        return result;
    }

    public ASDDVersion getMinASVersion() {
        return minASVersion;
    }

    public ASDDVersion getMaxASVersion() {
        return maxASVersion;
    }

    public StorageBeanFactory getStorageFactory() {
        return StorageBeanFactory.getStorageBeanFactory(appServerVersion);
    }

    /** Get the AppServer version to be used for saving deployment descriptors.
     *  Note that this is different than the version of the connected target
     *  application server (which can be queried by the appropriate method on
     *  SunONEDeploymentConfiguration.)
     *
     * @return ASDDVersion enum for the appserver version
     */
    public ASDDVersion getAppServerVersion() {
        return appServerVersion;
    }

    /** Set the AppServer version to be used for saving deployment descriptors.
     *  This version should be greater or equal to "minASVersion" and lesser or
     *  equal to "maxASVersion", as specified by the configuration, otherwise an
     *  IllegalArgumentException will be thrown.
     *
     * @param asVersion enum for the appserver version (cannot be null)
     */
    public void setAppServerVersion(ASDDVersion asVersion) {
        if (asVersion.compareTo(getMinASVersion()) < 0) {
            throw new IllegalArgumentException(asVersion.toString() + " is lower than required minimum version " + getMinASVersion().toString());
        }

        if (asVersion.compareTo(getMaxASVersion()) > 0) {
            throw new IllegalArgumentException(asVersion.toString() + " is higher than required maximum version " + getMaxASVersion().toString());
        }

        if (!asVersion.equals(appServerVersion) || deferredAppServerChange) {
            appServerVersion = asVersion;
            ConfigurationStorage localStorage = getStorage();
            if (localStorage != null) {
                deferredAppServerChange = false;
                localStorage.setChanged();
            }
        }
    }

    /** Set the AppServer version to be used for saving deployment descriptors.
     *
     *  This method is only for use by the DConfigBean tree, used to set the version
     *  while the configuration is being loaded (and thus should not and cannot be
     *  saved, which the public version would do.)  Instead, this changes the version
     *  and marks the change unsaved.  The version passed in here is the version
     *  actually found in the descriptor file as specified by the DOCTYPE, hence
     *  no range validation.  What recourse to take if the version found is actually
     *  outside the "valid range" is as yet an unsupported scenario.
     *
     * @param asVersion enum for the appserver version.
     */
    void internalSetAppServerVersion(ASDDVersion asVersion) {
        if (!asVersion.equals(appServerVersion)) {
            appServerVersion = asVersion;
            deferredAppServerChange = true;
        }
    }

    /** Returns the configuration files list for this deployment configuration instance.
     *
     * @return File array of the files managed by this deployment configuration.
     */
    public File[] getConfigFiles() {
        return configFiles;
    }

//    private BaseEjb getEjbDConfigBean(DDBean ejbDDBean) {
//        BaseEjb theEjbDCB = null;
//
//        try {
//            DDBean realEjbDDBean = getStorage().normalizeEjbDDBean(ejbDDBean);
//            DDBeanRoot ddBeanRoot = realEjbDDBean.getRoot();
//            DConfigBeanRoot dcbRoot = getDConfigBeanRoot(ddBeanRoot);
//            DConfigBean dcb = dcbRoot.getDConfigBean(realEjbDDBean);
//            if (dcb instanceof BaseEjb) {
//                theEjbDCB = (BaseEjb) dcb;
//            }
//        } catch (ConfigurationException ex) {
//            // I don't expect this exception to be thrown, but it might be.  If it is,
//            // it's probably a programmer error somewhere.
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        } catch (NullPointerException ex) {
//            // If one of above values ends up being null, we definitely want to
//            // log that it happened.  But for reporting purposes, we'll return
//            // null and let the caller's IllegalStateException take precedence.
//            // This would probably also be caused only by programmer error.
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//
//        return theEjbDCB;
//    }
//
//    /** Extract value of singular child field from a DDBean
//     */
//    private String getField(DDBean bean, String fieldId) {
//        String result = null;
//
//        try {
//            DDBean[] childFields = bean.getChildBean(fieldId);
//            if (childFields.length > 0) {
//                result = childFields[0].getText();
//            }
//        } catch (Exception ex) {
//            // If our code is written correctly, exceptions here should not happen.
//            // However, if the fieldId does not exist for this DD, or a few other
//            // types of programming errors, we could get a runtime exception here.
//            // This is here for runtime safety and to trap and log bugs.
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//
//        return result;
//    }
//
//    private String getField(DDBean bean, String fieldId, String defaultValue) {
//        String result = getField(bean, fieldId);
//
//        if (!Utils.notEmpty(result)) {
//            result = defaultValue;
//        }
//
//        return result;
//    }

    private ResourceConfiguratorInterface getResourceConfigurator() {
        ResourceConfiguratorInterface rci = null;
        DeploymentManager dm = getDeploymentManager();
        if (dm instanceof SunDeploymentManagerInterface) {
            SunDeploymentManagerInterface sdmi = (SunDeploymentManagerInterface) dm;
            rci = sdmi.getResourceConfigurator();
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Invalid DeploymentManager: " + dm));
        }
        return rci;
    }

    private DeploymentManager getDeploymentManager() {
        DeploymentManager dm = null;
        J2eeModuleProvider provider = getProvider(configFiles[0]);
        if (provider != null) {
            InstanceProperties ip = provider.getInstanceProperties();
            if (ip != null) {
                dm = ip.getDeploymentManager();
            } else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("Null Server InstanceProperties"));
            }
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new NullPointerException("Null J2eeModuleProvider"));
        }
        return dm;
    }

    private J2eeModuleProvider getProvider(File file) {
        J2eeModuleProvider provider = null;
        if (file != null) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                Project project = FileOwnerQuery.getOwner(fo);
                if (project != null) {
                    org.openide.util.Lookup lookup = project.getLookup();
                    provider = lookup.lookup(J2eeModuleProvider.class);
                }
            } else {
                File parent = file.getParentFile();
                if (parent != null) {
                    provider = getProvider(parent);
                }
            }
        }
        return provider;
    }

    private String getProjectName(File file) {
        String result = null;
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                ProjectInformation info = ProjectUtils.getInformation(project);
                if (info != null) {
                    result = info.getName();
                }
            }
        }
        return result;
    }

    public void saveConfiguration(OutputStream outputStream) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        try {
            if (this.module.getModuleType().equals(ModuleType.WAR)) {
                // copy sun-web.xml to stream directly.
                FileObject configFO = FileUtil.toFileObject(configFiles[0]);
                if(configFO != null) {
                    RootInterface rootDD = DDProvider.getDefault().getDDRoot(configFO);
                    rootDD.write(outputStream);
                }
            } else {
                DeploymentPlan dp = new DeploymentPlan();
                // write all existing config files to stream

                for(int i = 0; i < configFiles.length; i++) {
                    FileObject configFO = FileUtil.toFileObject(configFiles[i]);
                    if(configFO != null) {
                        try {
                            FileEntry fe = new FileEntry();
                            fe.setName(configFO.getNameExt());
                            long estimate = configFO.getSize() * 2;
                            RootInterface rootDD = DDProvider.getDefault().getDDRoot(configFO);
                            StringWriter stringWriter = new StringWriter((int) estimate);
                            rootDD.write(stringWriter);
                            fe.setContent(stringWriter.toString());
                            dp.addFileEntry(fe);
                        } catch(PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                dp.write(outputStream);
            }
//        } catch(Schema2BeansException ex) {
//            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(ex.getMessage(), ex);
//        } catch(IOException ex) {
//            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(ex.getMessage(), ex);
        } catch(Exception ex) {
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(ex.getMessage(), ex);
        }    
    }
    
    /** Retrieves DConfigBeanRoot associated with the specified DDBean root.  If
     *  this DCB has already created, retrieves it from cache, otherwise creates
     *  a new DCB via factory mechanism.
     *
     * @param dDBeanRoot
     * @throws ConfigurationException
     * @return
     */
    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot dDBeanRoot) throws ConfigurationException {
        /*
         *		dcbroot = rootcache.get(ddroot)
         *		if dcbroot == null
         *			factory = lookup(moduletype)
         *			dcbroot = factory.create(ddroot)
         *			cache.add(dcbroot)
         *		return dcbroot
         */ 
        if (null == dDBeanRoot) {
            throw Utils.makeCE("ERR_DDBeanIsNull", null, null);
        }

        if (null == dDBeanRoot.getXpath()) {
            throw Utils.makeCE("ERR_DDBeanHasNullXpath", null, null);
        }

        // If DDBean is not from our internal tree, normalize it to one that is.
        if (!(dDBeanRoot instanceof DDRoot)) {
            // If the root cache is empty, then it is likely that
            if (getDCBRootCache().entrySet().size() == 0) {
                throw Utils.makeCE("ERR_CannotNormalizeDDBean", new Object[]{dDBeanRoot.getClass().getName()}, null);
            }
            dDBeanRoot = getStorage().normalizeDDBeanRoot(dDBeanRoot);
        }

        BaseRoot rootDCBean = (BaseRoot) getDCBRootCache().get(dDBeanRoot);

        if (null == rootDCBean) {
            DCBFactory factory = (DCBFactory) getDCBFactoryMap().get(dDBeanRoot.getXpath());
            if (factory != null) {
                rootDCBean = (BaseRoot) factory.createDCB(dDBeanRoot, null);
                System.out.println("New DCB Root for " + dDBeanRoot.getXpath());
                if (rootDCBean != null) {
                    getDCBCache().put(dDBeanRoot, rootDCBean);
                    getDCBRootCache().put(dDBeanRoot, rootDCBean);
                }
            }
        }

        return rootDCBean;
    }

    /**
     * @return
     */
    public javax.enterprise.deploy.model.DeployableObject getDeployableObject() {
        // TODO : remove before merge to trunk
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new UnsupportedOperationException());
        return null; //dObj;
    }

    /** JSR88: Removes the DConfigBeanRoot and all its children.
     *
     * @param dConfigBeanRoot The DConfigBeanRoot to remove.
     * @throws BeanNotFoundException
     */
    public void removeDConfigBean(DConfigBeanRoot dConfigBeanRoot) throws BeanNotFoundException {
        jsr88Logger.entering(this.getClass().toString(), "removeDConfigBean", dConfigBeanRoot);

        if (null != dConfigBeanRoot) {
            DDBeanRoot key = (DDBeanRoot) dConfigBeanRoot.getDDBean();

            // Find DConfigBean directly in cache.
            BaseRoot deadBean = (BaseRoot) getDCBCache().get(key);
            if (deadBean != null) {
                // Remove the children first.  Each child also removes it's own
                // children so this will remove the entire tree when it's done.
                // Can't use iterator here or we will possibly get a ConcurrentModificationException
                // as the children clean themselves up.
                Object[] children = deadBean.getChildren().toArray();
                for (int i = 0; i < children.length; i++) {
                    try {
                        deadBean.removeDConfigBean((Base) children[i]);
                    } catch (BeanNotFoundException ex) {
                        // This would suggest a corrupt tree or bad code somewhere if it happens.
                        // Catch & log it and continue cleaning the tree.
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }

                System.out.println("DCB Root removed for " + key.getXpath());

                // Remove this bean from both general and root caches.
                getDCBCache().remove(key);
                getDCBRootCache().remove(key);

                // cleanup bean before throwing away
                deadBean.cleanup();
                deadBean = null;
            } else {
                // If not found, throw appropriate exception per spec.
                throw new BeanNotFoundException(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_DConfigBeanRootNotFoundOnRemove", dConfigBeanRoot.toString()));
            }
        }

        jsr88Logger.exiting(this.getClass().toString(), "removeDConfigBean", dConfigBeanRoot);
    }

    /** Restore the configuration object from a deployment plan .
     * This method reads the plan from the InputStream.  The plan is
     * not completely parsed, though. When a config bean needs to use data
     * is in the content, it will be converted into a bean graph.
     * @param inputStream
     * @throws ConfigurationException
     */
    public void restore(InputStream inputStream) throws ConfigurationException {
        jsr88Logger.entering(this.getClass().toString(), "restore", inputStream);

        restoreDConfigBean(inputStream, null);

        jsr88Logger.exiting(this.getClass().toString(), "restore", inputStream);
    }

    /**
     * @param inputStream
     * @param dDBeanRoot
     * @throws ConfigurationException
     * @return
     */
    public DConfigBeanRoot restoreDConfigBean(InputStream inputStream, DDBeanRoot dDBeanRoot) throws ConfigurationException {
        //Find the section of the deployment plan that implements the bean.
        jsr88Logger.finest("S1DepConfig:restoreDConfigBean(jiIS,DDBeanRoot)");
        // this stream has deployment plan format.
        //
        //jsr88Logger.finest("S1DepConfig:restore(jiIS)");
        DeploymentPlan dp = null;

        // Flush bean cache.  This forces reparsing of the new tree we load here.
        beanMap.clear();

        try {
            if (null != inputStream) {
                try {
                    if (this.module.getModuleType().equals(ModuleType.WAR)) {
                        // read the sun-web.xml file in and conjure a
                        // deployment plan file object for it.
                        try {
                            SunWebApp sunW = DDProvider.getDefault().getWebDDRoot(inputStream);
                            dp = DeploymentPlan.createGraph();
                            FileEntry fe = new FileEntry();
                            fe.setName("sun-web.xml");
                            java.io.StringWriter strWriter = new java.io.StringWriter();
                            sunW.write(strWriter);
                            fe.setContent(strWriter.toString());
                            dp.addFileEntry(fe);
                        } catch (DDException ex) {
                            // bad sun-web.xml file in stream.
                            jsr88Logger.finest(ex.getClass().getName() + " while processing sun-web.xml into a deployment plan: " + ex.getLocalizedMessage());
                            dp = DeploymentPlan.createGraph();
                        } catch (SAXException ex) {
                            // bad sun-web.xml file in stream.
                            jsr88Logger.finest(ex.getClass().getName() + " while processing sun-web.xml into a deployment plan: " + ex.getLocalizedMessage());
                            dp = DeploymentPlan.createGraph();
                        } catch (IOException ex) {
                            // error reading sun-web.xml file from stream.
                            jsr88Logger.finest(ex.getClass().getName() + " while processing sun-web.xml into a deployment plan: " + ex.getLocalizedMessage());
                            dp = DeploymentPlan.createGraph();
                        }
                    } else {
                        dp = DeploymentPlan.createGraph(inputStream);
                    }
                } catch (Schema2BeansRuntimeException s2bre) {
                    // vbk--todo : do more investigation before creating an
                    // empty plan object.
                    jsr88Logger.finest("the stream did not have a deployment plan");
                    dp = DeploymentPlan.createGraph();
                }
            } else {
                jsr88Logger.finest("the stream was null");
            }
            FileEntry[] entries = new FileEntry[0];
            if (null != dp) {
                FileEntry[] tentries = dp.getFileEntry();
                if (null != tentries) {
                    entries = tentries;
                }
            }
            for (int i = 0; i < entries.length; i++) {
                String key = Utils.getFQNKey(entries[i].getUri(), entries[i].getName()); // !PW This is FileEntry.getUri(), not Base.getUri()
                contentMap.put(key, entries[i].getContent().getBytes());
            }
        } catch (Schema2BeansRuntimeException ex) {
            jsr88Logger.finest("Schema2Beans threw a Runtime Exception");
        } catch (Exception ex) {
            jsr88Logger.finest("foo");
            ConfigurationException ce = new ConfigurationException("bad plan stream");
            ce.initCause(ex);
            //throw ce;
        }

        // find the DConfigBean that corresponds to the
        // DDBeanRoot
        //
        List pending = new ArrayList();
        BaseRoot rootToRestore = null;

        if (null == dDBeanRoot) {
            // no rootbean, add roots from this deployment configuration
            Iterator rootIter = getDCBRootCache().entrySet().iterator();
            while (rootIter.hasNext()) {
                pending.add(((Map.Entry) rootIter.next()).getValue());
            }

            rootToRestore = getMasterDCBRoot();
        } else {
            rootToRestore = (BaseRoot) getDCBRootCache().get(dDBeanRoot);
            if (null != rootToRestore) {
                pending.add(rootToRestore);
            }
        }

        int index = 0;
        while (index < pending.size()) {
            Base current = (Base) pending.get(index);
            try {
                current.loadFromPlanFile(this);
            } catch (java.lang.IllegalStateException ise) {
                jsr88Logger.throwing(current.getClass().toString(), "loadFromPlanFile", ise);
                assert ise == null;
            }
            pending.addAll(current.getChildren());
            index++;
        }

        return rootToRestore;
    }

    /**
     * @param outputStream
     * @throws ConfigurationException
     */
    public void save(OutputStream outputStream) throws ConfigurationException {
        jsr88Logger.entering(this.getClass().toString(), "save", outputStream);

        saveDConfigBean(outputStream, null);

        jsr88Logger.exiting(this.getClass().toString(), "save", outputStream);
    }

    /**
     * @param outputStream
     * @param rootBean
     * @throws ConfigurationException
     */
    public void saveDConfigBean(OutputStream outputStream, DConfigBeanRoot rootBean) throws ConfigurationException {
        Object[] params = new Object[]{outputStream, rootBean};
        jsr88Logger.entering(this.getClass().toString(), "save", params);

        boolean useUriDataAtSave = false;
//        if(null == rootBean || rootBean instanceof AppRoot) {
//            useUriDataAtSave = true;
//        }
        Map outputGraphs = new LinkedHashMap();
        Map cmpGraphs = new LinkedHashMap(); // !PW FIXME added for beta -- remove after switching to DD API
        try {
            // Build the map of output bean graphs by calling addToGraphs() on
            // each root bean we have (or the one passed in, if any).
            //
            if (rootBean == null) {
                Iterator rootIter = getDCBRootCache().entrySet().iterator();
                while (rootIter.hasNext()) {
                    Base dcb = (Base) ((Map.Entry) rootIter.next()).getValue();
                    dcb.addToGraphs(outputGraphs, null, "");

                    // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
                    if (dcb instanceof EjbJarRoot) {
                        EjbJarRoot ejbJar = (EjbJarRoot) dcb;
                        Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                        if (cmpSnippet.hasDDSnippet()) {
                            String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                            cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                        }
                    }
                }
            } else {
                ((Base) rootBean).addToGraphs(outputGraphs, null, "");

                // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
                if (rootBean instanceof EjbJarRoot) {
                    EjbJarRoot ejbJar = (EjbJarRoot) rootBean;
                    Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                    if (cmpSnippet.hasDDSnippet()) {
                        String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                        cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                    }
                }
            }

            // combine the sun-XXX deployment descriptor bean graphs
            // in a deployment plan file graph.
            Set entrySet = outputGraphs.entrySet();
            Iterator iter = entrySet.iterator();
            DeploymentPlan dp = new DeploymentPlan();
            CommonDDBean bean = null;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                bean = (CommonDDBean) entry.getValue();
                if (null != bean) {
                    String keyString = (String) entry.getKey();
                    String uri = Utils.getUriFromKey(keyString);
                    String fname = Utils.getFilenameFromKey(keyString);
                    FileEntry fe = new FileEntry();
                    fe.setName(fname);
                    if (useUriDataAtSave && uri.length() > 0) {
                        fe.setUri(uri);
                    }
                    java.io.StringWriter strWriter = new java.io.StringWriter();
                    bean.write(strWriter);
                    fe.setContent(strWriter.toString());
                    dp.addFileEntry(fe);
                } else {
                    jsr88Logger.warning("no bean for key: " + entry.getKey());
                }
            }

            // !PW FIXME also add cmp graphs
            entrySet = cmpGraphs.entrySet();
            iter = entrySet.iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                org.netbeans.modules.schema2beans.BaseBean cmpRoot = (org.netbeans.modules.schema2beans.BaseBean) entry.getValue();
                if (null != cmpRoot) {
                    String keyString = (String) entry.getKey();
                    String uri = Utils.getUriFromKey(keyString);
                    String fname = Utils.getFilenameFromKey(keyString);
                    FileEntry fe = new FileEntry();
                    fe.setName(fname);
                    if (useUriDataAtSave && uri.length() > 0) {
                        fe.setUri(uri);
                    }
                    java.io.StringWriter strWriter = new java.io.StringWriter();
                    cmpRoot.write(strWriter);
                    fe.setContent(strWriter.toString());
                    dp.addFileEntry(fe);
                } else {
                    jsr88Logger.warning("no bean for key: " + entry.getKey());
                }
            }

            if (this.module.getModuleType().equals(ModuleType.WAR)) {
                if (null != bean) {
                    bean.write(outputStream);
                }
            } else {
                dp.write(outputStream);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            ConfigurationException ce = new ConfigurationException("Sun Deployment Descriptor Error"); // NOI18N
            ce.initCause(ex);
        }
        jsr88Logger.exiting(this.getClass().toString(), "Save", params); // NOI18N
    }

    /** Get the schema2beans object graph that provides data for a DConfigBean
     * @param uri The uri for the descriptor source
     * @param fileName the name of the descriptor file
     * @param parser the ConfigParser that converts a stream into a bean graph
     * @param finder The ConfigFinder that accepts the parser's return value
     * and finds the subgraph for a DConfigBean
     * @return An Object to initialize the values in the DConfigBean
     */
    Object getBeans(String uri, String fileName, ConfigParser parser, ConfigFinder finder) {
        String key = Utils.getFQNKey(uri, fileName);
        Object root = beanMap.get(key);

        if (root == null) {
            // parse the content
            byte[] content = (byte[]) contentMap.get(key);
            if (content == null) {
                return null;
            }

            if (parser == null) {
                jsr88Logger.severe("Missing parser");
                return null;
            }

            try {
                root = parser.parse(new ByteArrayInputStream(content));
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                root = priorBeanMap.remove(key);
                if (root == null) {
                    // No prior map, just return null (otherwise, put into main map
                    // same as for newly parsed root.
                    return null;
                }
            }

            beanMap.put(key, root);
        }

        Object result = finder.find(root);
        return result;
    }

    /* ------------------------- Utility Functions ------------------------
     */
    /** Loads and initializes the DConfigBean tree for this DeploymentConfiguration
     *  instance if it has not already been done yet.
     */
    public boolean ensureConfigurationLoaded() {
        // Retrieve storage, forcing it to be created if necessary, and initialized.
        boolean result = getStorage() != null;
        return result;
    }

    /** Retrieves the ConfigurationStorage instance from the primary DataObject
     *  representing the saved version of this configuration.  The storage object
     *  should be created if necessary.  This will initialize the entire DConfigBean
     *  tree.
     */
    public ConfigurationStorage getStorage() {
        ConfigurationStorage theStorage = null;

        // !PW Do we need to do this?  What about the secondary file?  Need to convert to file based as well.
//        getPrimaryFile().refresh(); //check for external changes
        synchronized (storageMonitor) {
            theStorage = storage;
        }

        if (theStorage == null) {
            // 0nly lookup the provider when we need to, but additionally, do it
            // outside of any locks on configuration initialization.
            J2eeModuleProvider provider = getProvider(configFiles[0].getParentFile());
            if (provider == null) {
                // We get this if someone calls getStorage() on a project for which this is true
                // _and_ the project was never initialized (or failed initialization and the user continued).
                throw new IllegalStateException("No Project and/or J2eeModuleProvider located for " + configFiles[0].getPath());
            }

            synchronized (storageMonitor) {
                if (storage == null) {
                    try {
                        storage = new ConfigurationStorage(provider, this);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    } catch (InvalidModuleException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    } catch (SAXException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    } catch (ConfigurationException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
                theStorage = storage;
            }
        }

        return theStorage;
    }

    /** If this module is a web application, retrieve the WebAppRoot bean for it.
     *  You do not need to call ensureConfigurationLoaded() before calling this
     *  method, as this method will do so itself when it normalizes the DDRoot.
     */
    public WebAppRoot getWebAppRoot() {
        WebAppRoot war = null;

        throw new NotImplementedException();
        // TODO: the code below needs to be rewritten to use the metadata model
//        org.netbeans.modules.j2ee.dd.api.common.RootInterface root =
//                module.getDeploymentDescriptor(J2eeModule.WEB_XML);
//        if(null != root) {
//            try {
//                ConfigurationStorage storage = getStorage();
//                if(storage != null) {
//                    DConfigBeanRoot dcbRoot = getDConfigBeanRoot(storage.getDDBeanRoot(module));
//                    if(dcbRoot instanceof WebAppRoot) {
//                        war = (WebAppRoot) dcbRoot;
//                    }
//                }
//            } catch (ConfigurationException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
//            }
//        } else {
//            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DeployableObject with null DDBeanRoot!!!");
//        }
//
//        return war;
    }

    /** If this module is a ejbjar, retrieve the EjbJarRoot bean for it.
     *  You do not need to call ensureConfigurationLoaded() before calling this
     *  method, as this method will do so itself when it normalizes the DDRoot.
     */
    public EjbJarRoot getEjbJarRoot() {
        EjbJarRoot ejbJar = null;
//        TODO fixme
//        DDBeanRoot root = dObj.getDDBeanRoot();
//        if(null != root) {
//            try {
//                ConfigurationStorage storage = getStorage();
//                if(storage != null) {
//                    DConfigBeanRoot dcbRoot = getDConfigBeanRoot(storage.normalizeDDBeanRoot(root));
//                    if(dcbRoot instanceof EjbJarRoot) {
//                        ejbJar = (EjbJarRoot) dcbRoot;
//                    }
//                }
//            } catch (ConfigurationException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
//            }
//        } else {
//            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "DeployableObject with null DDBeanRoot!!!");
//        }
//
        return ejbJar;
    }

    /**
     * Set the context root of the module this DeploymentConfiguration represents,
     * if the deployable object is a WAR file.
     *
     * @param contextRoot new value for context-root field for this web app.
     */
    public void setContextRoot(String contextRoot) {
        if (ModuleType.WAR.equals(module.getModuleType())) {
            try {
                FileObject primarySunDDFO = getSunDD(configFiles[0], true);
                if (primarySunDDFO != null) {
                    RootInterface rootDD = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                    if (rootDD instanceof SunWebApp) {
                        SunWebApp swa = (SunWebApp) rootDD;
                        swa.setContextRoot(contextRoot);
                        swa.write(primarySunDDFO);
                    }
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String defaultMessage = " trying set context-root in sun-web.xml";
                displayError(ex, defaultMessage);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String defaultMessage = " trying set context-root in sun-web.xml";
                displayError(ex, defaultMessage);
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "SunONEDeploymentConfiguration.setContextRoot() invoked on incorrect module type: " + module.getModuleType());
        }
    }

    /**
     * Get the context root of the module this DeploymentConfiguration represents,
     * if the deployable object is a WAR file.
     *
     * @return value of context-root field for this web app, if any.
     */
    public String getContextRoot() {
        String contextRoot = null;
        if (ModuleType.WAR.equals(module.getModuleType())) {
            try {
                RootInterface rootDD = getSunDDRoot(false);
                if (rootDD instanceof SunWebApp) {
                    contextRoot = ((SunWebApp) rootDD).getContextRoot();
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String defaultMessage = " retrieving context-root from sun-web.xml";
                displayError(ex, defaultMessage);
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "SunONEDeploymentConfiguration.setContextRoot() invoked on incorrect module type: " + module.getModuleType());
        }
        return contextRoot;
    }

    /* Get the deploymentModuleName value which is usually passed in by an IDE
     * to define a good value for a directory name used for dir deploy actions.
     **/
    public String getDeploymentModuleName() {
        return deploymentModuleName;
    }

    /* Set the deploymentModuleName value which is usually passed in by an IDE
     * to define a good value for a directory name used for dir deploy actions.
     **/
    public void setDeploymentModuleName(String s) {
        deploymentModuleName = s;
    }

    /** If this module has any webservices, retrieve the root DConfigBean of that
     *  portion of the tree.
     */
    public WebServices getWebServicesRoot() {
        WebServices wsr = null;

        Map rootMap = getDCBRootCache();
        if (rootMap.size() > 1) {
            // Grab second entry in map (backing map is LinkedHashMap, so second
            // entry is the one we want.)
            Iterator rootIterator = rootMap.entrySet().iterator();
            rootIterator.next();
            Object root = ((Map.Entry) rootIterator.next()).getValue();
            if (root instanceof WebServices) {
                wsr = (WebServices) root;
            }
        }

        return wsr;
    }

    /* ------------------------------------------------------------------------
     * Default descriptor file creation, root interface retrieval
     */
    // This method is only useful for reading the model.  If the model is to
    // be modified and rewritten to disk, you'll need the FileObject it was
    // retrieved from as well.
    private RootInterface getSunDDRoot(boolean create) throws IOException {
        RootInterface sunDDRoot = null;
        FileObject primarySunDDFO = getSunDD(configFiles[0], create);
        if (primarySunDDFO != null) {
            sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
        }
        return sunDDRoot;
    }

    public RootInterface getSunDDRoot(File sunDD, boolean create) throws IOException {
        RootInterface sunDDRoot = null;
        FileObject primarySunDDFO = getSunDD(sunDD, create);
        if (primarySunDDFO != null) {
            sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
        }
        return sunDDRoot;
    }

    private FileObject getSunDD(File sunDDFile, boolean create) throws IOException {
        if (!sunDDFile.exists()) {
            if (create) {
                createDefaultSunDD(sunDDFile);
            } else {
                return null;
            }
        }
        return FileUtil.toFileObject(sunDDFile);
    }

    private void createDefaultSunDD(File sunDDFile) throws IOException {
        String resource = "org-netbeans-modules-j2ee-sun-ddui/" + sunDDFile.getName(); // NOI18N
        FileObject sunDDTemplate = Repository.getDefault().getDefaultFileSystem().findResource(resource);
        if (sunDDTemplate != null) {
            File configDir = sunDDFile.getParentFile();
            if (!configDir.exists()) {
                if (!configDir.mkdirs()) {
                    throw new IOException("Unable to create folder " + configDir.getPath());
                }
            }
            FileObject configFolder = FileUtil.toFileObject(configDir);
            FileSystem fs = configFolder.getFileSystem();
            XmlFileCreator creator = new XmlFileCreator(sunDDTemplate, configFolder, sunDDTemplate.getName(), sunDDTemplate.getExt());
            fs.runAtomicAction(creator);
        }
    }

    private void displayError(Exception ex, String defaultMessage) {
        String message = ex.getLocalizedMessage();
        if (Utils.strEmpty(message)) {
            message = ex.getClass().getSimpleName() + defaultMessage;
        }
        final NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(msg);
    }

    /* ------------------------------------------------------------------------
     * DDBeanRoot -> DConfigBeanRoot cache support
     *
     * moduleDCBCache is a map containing all root beans this configuration
     *   is responsible for.
     * completeDCBCache is the DCB cache for all DCB's by this configuration,
     *   it's root DCB's and their children.  Essentially all DCB's involved
     *   in a particular invocation of JSR-88.
     * patchCache is a list of any unpatched reference DCB's created as children
     *   of AppRoot (for now, could be anywhere we use a reference).  The key
     *   for such references is the ddBeanRoot they expect to be a reference to.
     */
    private Map moduleDCBCache = new LinkedHashMap(13);
    private Map completeDCBCache = new LinkedHashMap(63);
    private Map patchCache = new LinkedHashMap(13);

    /** Retrieve the cache containing all DConfigBeans owned by this configuration.
     *
     * @return Map containing all DConfigBeans owned by this configuration.
     */
    Map getDCBCache() {
        return completeDCBCache;
    }

    /** Retrieve the cache containing all root DConfigBeans owned by this configuration.
     *
     * @return Map containing all root DConfigBeans owned by this configuration.
     */
    Map getDCBRootCache() {
        return moduleDCBCache;
    }

    /** Retrieve the cache containing all reference DConfigBeans that are currently
     *  unpatched (no matching regular bean has been created for them yet.) owned
     *  by this configuration.
     *
     * @return Map containing all unpatched reference DConfigBeans owned by this
     *         configuration.
     */
    Map getPatchList() {
        return patchCache;
    }

    /** Retrieves the "master" DConfigBean root for this configuration (as opposed
     *  to just any root bean -- EAR files have multiple DConfigBeanRoots, the
     *  one representing sun-application.xml is the master in that case.)  For right
     *  now the logic just picks the first root in the cache on the assumption
     *  that the master was the first one created.  However there is no guarantee
     *  that will work so we should come up with something better.
     *
     * @return The DConfigBeanRoot owned by this configuration that is deemed to
     *   be the "master" root bean.  For example, in an EAR file, the master root
     *   bean is the bean representing sun-application.xml.
     */
    BaseRoot getMasterDCBRoot() {
        BaseRoot masterRoot = null;
        Iterator rootIterator = moduleDCBCache.entrySet().iterator();
        while (rootIterator.hasNext()) {
            BaseRoot tmpRoot = (BaseRoot) ((Map.Entry) rootIterator.next()).getValue();
            // Only accept this root if is _not_ the root of webservices.xml or equivalent annotation
            // WebServices is occasionally now showing up as the first element in this list though I
            // am not sure how.  It is never the masterroot, so we should never return it.
            if (!(tmpRoot instanceof WebServices)) {
                masterRoot = tmpRoot;
                break;
            }
        }
        return masterRoot;
    }

    /* ------------------------------------------------------------------------
     * XPath to factory mapping support
     */
    private Map dcbFactoryMap = null;

    /** Retrieve the factory manager for this DConfigBean.  If one has not been
     *  constructed yet, create it.
     * @return
     */
    private Map getDCBFactoryMap() {
        if (dcbFactoryMap == null) {
            dcbFactoryMap = new HashMap(17);

            // Only factories that create a BaseRoot bean with no parent are
            // allowed here, e.g. DCBTopRootFactory.
            dcbFactoryMap.put("/application", new DCBTopRootFactory(AppRoot.class)); // EAR	// NOI18N
            dcbFactoryMap.put("/ejb-jar", new DCBTopRootFactory(EjbJarRoot.class)); // EJB	// NOI18N
            dcbFactoryMap.put("/web-app", new DCBTopRootFactory(WebAppRoot.class)); // WAR	// NOI18N
            dcbFactoryMap.put("/application-client", new DCBTopRootFactory(AppClientRoot.class)); // CAR	// NOI18N
//            dcbFactoryMap.put("/connector", new DCBTopRootFactory(ConnectorRoot.class));			// RAR	// NOI18N
        }

        return dcbFactoryMap;
    }

    /** Factory that knows how to create and initialize root DConfigBeans from a
     *  DDBeanRoot passed in by the tool side of JSR-88.
     */
    private class DCBTopRootFactory implements DCBFactory {

        private Class dcbRootClass;

        DCBTopRootFactory(Class c) {
            dcbRootClass = c;
        }

        public Base createDCB(DDBean ddBean, Base dcbParent) throws ConfigurationException {
            if (ddBean == null) {
                throw Utils.makeCE("ERR_RootDDBeanIsNull", null, null); // NOI18N
            }

            if (!(ddBean instanceof DDBeanRoot)) {
                Object[] args = new Object[1];
                args[0] = dcbRootClass.getName();
                throw Utils.makeCE("ERR_RootDDBeanWrongType", args, null); // NOI18N
            }

            DDBeanRoot ddbRoot = (DDBeanRoot) ddBean;
            BaseRoot newDCB = null;

            try {
                newDCB = (BaseRoot) dcbRootClass.newInstance();
                newDCB.init(ddbRoot, SunONEDeploymentConfiguration.this, ddbRoot);
            } catch (InstantiationException ex) {
                Object[] args = new Object[1];
                args[0] = dcbRootClass.getName();
                throw Utils.makeCE("ERR_UnexpectedInstantiateException", args, ex); // NOI18N
            } catch (IllegalAccessException ex) {
                Object[] args = new Object[1];
                args[0] = dcbRootClass.getName();
                throw Utils.makeCE("ERR_UnexpectedIllegalAccessException", args, ex); // NOI18N
            } catch (RuntimeException ex) {
                throw Utils.makeCE("ERR_UnexpectedRuntimeException", null, ex); // NOI18N
            }

            return newDCB;
        }

        public Base createDCB(J2eeModule ddBean, Base dcbParent) throws ConfigurationException {
            if (ddBean == null) {
                throw Utils.makeCE("ERR_RootDDBeanIsNull", null, null); // NOI18N
            }
//            if(!(ddBean instanceof DDBeanRoot)) {
//                Object [] args = new Object [1];
//                args[0] = dcbRootClass.getName();
//                throw Utils.makeCE("ERR_RootDDBeanWrongType", args, null); // NOI18N
//            }
            J2eeModule ddbRoot = ddBean;
            BaseRoot newDCB = null;

            try {
                newDCB = (BaseRoot) dcbRootClass.newInstance();
                newDCB.init(ddbRoot, SunONEDeploymentConfiguration.this, ddbRoot);
            } catch (InstantiationException ex) {
                Object[] args = new Object[1];
                args[0] = dcbRootClass.getName();
                throw Utils.makeCE("ERR_UnexpectedInstantiateException", args, ex); // NOI18N
            } catch (IllegalAccessException ex) {
                Object[] args = new Object[1];
                args[0] = dcbRootClass.getName();
                throw Utils.makeCE("ERR_UnexpectedIllegalAccessException", args, ex); // NOI18N
            } catch (RuntimeException ex) {
                throw Utils.makeCE("ERR_UnexpectedRuntimeException", null, ex); // NOI18N
            }

            return newDCB;
        }
    }

    // New methods to support the studio
    private static int BUF_LEN = 1024;

    // MS5 -- deal with the schema files...
    public void addFileToPlanForModule(File f, J2eeModule mod, ConfigurationStorage storage) throws ConfigurationException {
        // find the uri
        String uri = getUriForModule(mod, storage);
        // create the key
        String fname = f.getName();
        String key = Utils.getFQNKey(uri, fname);
        // read in the file's content
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = null;
        int totalRead = 0;
        try {
            fis = new FileInputStream(f);
            byte[] buf = new byte[BUF_LEN];
            int lastRead = 0;
            do {
                lastRead = fis.read(buf);
                if (lastRead > -1) {
                    baos.write(buf, 0, lastRead);
                    totalRead += lastRead;
                }
            } while (lastRead > -1);
        } catch (java.io.FileNotFoundException fnfe) {
            // somebody was being mean to us --
            //  log it. forget it. move on.
        } catch (java.io.IOException ioe) {
            // this is more drastic, Throw an exception here
            ConfigurationException ce = new ConfigurationException("Failed while reading"); // NOI18N
            ce.initCause(ioe);
            throw ce;
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (java.io.IOException ioe) {
                    // log this and move on
                }
            }
        }
        //Document doc = GraphManager.createXmlDocument(fis, false);
        // convert it to the right form of xml
        //BaseBean bean = convertDocToBean(doc);
        // put it in the map
        if (totalRead > 0) {
            contentMap.put(key, baos.toByteArray());
        } else {
            // remove the old content
            contentMap.remove(key);
        }

        // Remove this entry from the beanMap, forcing it to be reparsed.
        // Flush bean cache.  This forces reparsing of the new tree we load here.
        Object oldMap = beanMap.remove(key);

        // Save any prior bean map to fall back to in case parsing new content fails.
        if (oldMap != null) {
            priorBeanMap.put(key, oldMap);
        }
    }

    public void extractFileFromPlanForModule(File f, J2eeModule mod, ConfigurationStorage storage) throws IOException {
        // find the uri
        String uri = getUriForModule(mod, storage);
        final String fname = f.getName();

        // create the key
        String key = Utils.getFQNKey(uri, fname);

        // get the bean
        final byte[] content = (byte[]) contentMap.get(key);

        // save it into the file
        if (null != content) {
            File parentFile = f.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            final FileObject folder = FileUtil.toFileObject(parentFile);
            if (folder == null) {
                // cannot create folder!
                String errorMsg = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_SaveError", fname) + "  " + NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_FailedToCreateConfigFolder", parentFile.getAbsolutePath()); // NOI18N
                throw new IOException(errorMsg);
            } else if (!folder.isFolder()) {
                // config "folder" is not a folder!!!
                String errorMsg = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_SaveError", fname) + "  " + NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ConfigFolderNotDirectory", parentFile.getAbsolutePath()); // NOI18N
                throw new IOException(errorMsg);
            }

            System.out.println("Writing descriptor file " + fname);

            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {

                public void run() throws IOException {
                    FileLock lock = null;
                    OutputStream out = null;
                    try {
                        FileObject configFO = folder.getFileObject(fname);
                        if (configFO == null) {
                            configFO = folder.createData(fname);
                        }
                        lock = configFO.lock();
                        out = new BufferedOutputStream(configFO.getOutputStream(lock), 4096);
                        out.write(content);
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ioe) {
                            }
                        }
                        if (lock != null) {
                            lock.releaseLock();
                        }
                    }
                }
            });
        }
    }

    private String getUriForModule(J2eeModule mod, ConfigurationStorage storage) {
        // Logic here duplicates logic from the BaseRoot derived classes that implement
        // getUriText().  The reason is that the DConfigBean's do not necessarily exist
        // at times when this routine is needed and we don't want to be creating them at
        // that point for performance and other reasons.
        String rootUri = ""; // NOI18N
        if (ModuleType.EAR.equals(mod.getModuleType())) {
            rootUri = "EAR"; // NOI18N
        }
        return rootUri;
    }

    // this routine flushes the bean data into the content map
    //
    void updateContentMap(DConfigBeanRoot rootBean) throws ConfigurationException {
        jsr88Logger.entering(this.getClass().toString(), "save", rootBean); // NOI18N
//        boolean useUriDataAtSave = false;
//        if(null == rootBean || rootBean instanceof AppRoot) {
//            useUriDataAtSave = true;
//        }
        Map outputGraphs = new LinkedHashMap();
        Map cmpGraphs = new LinkedHashMap(); // !PW FIXME added for beta -- remove after switching to DD API
        try {
            // Build the map of output bean graphs by calling addToGraphs() on
            // each root bean we have (or the one passed in, if any).
            //
            if (rootBean == null) {
                Iterator rootIter = getDCBRootCache().entrySet().iterator();
                while (rootIter.hasNext()) {
                    Base dcb = (Base) ((Map.Entry) rootIter.next()).getValue();
                    dcb.addToGraphs(outputGraphs, null, ""); // NOI18N
                    // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
                    if (dcb instanceof EjbJarRoot) {
                        EjbJarRoot ejbJar = (EjbJarRoot) dcb;
                        Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                        if (cmpSnippet.hasDDSnippet()) {
                            String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                            cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                        }
                    }
                }
            } else {
                ((Base) rootBean).addToGraphs(outputGraphs, null, "");
                // !PW FIXME code to retrieve the CMP snippet for beta since it's still base bean type.
                if (rootBean instanceof EjbJarRoot) {
                    EjbJarRoot ejbJar = (EjbJarRoot) rootBean;
                    Snippet cmpSnippet = ejbJar.getCmpMappingSnippet();
                    if (cmpSnippet.hasDDSnippet()) {
                        String snippetKey = Utils.getFQNKey(ejbJar.getUriText(), cmpSnippet.getFileName());
                        cmpGraphs.put(snippetKey, cmpSnippet.getCmpDDSnippet());
                    }
                }
            }

            // combine the sun-XXX deployment descriptor bean graphs
            // in a deployment plan file graph.
            Set entrySet = outputGraphs.entrySet();
            Iterator iter = entrySet.iterator();
            DeploymentPlan dp = new DeploymentPlan();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                CommonDDBean bean = (CommonDDBean) entry.getValue();
                if (null != bean) {
                    /*String keyString = (String) k;
                    String uri = Utils.getUriFromKey(keyString);
                    String fname = Utils.getFilenameFromKey(keyString);
                    FileEntry fe = new FileEntry();
                    fe.setName(fname);
                    if (useUriDataAtSave && uri.length() > 0) {
                    fe.setUri(uri);
                    }
                    java.io.StringWriter strWriter = new java.io.StringWriter();*/
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bean.write(baos);
                    contentMap.put(entry.getKey(), baos.toByteArray());
                    //fe.setContent(strWriter.toString());
                    //dp.addFileEntry(fe);
                } else {
                    jsr88Logger.warning("no bean for key: " + entry.getKey()); // NOI18N
                }
            }

            // !PW FIXME write out cmp graphs too.
            entrySet = cmpGraphs.entrySet();
            iter = entrySet.iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                org.netbeans.modules.schema2beans.BaseBean cmpRoot = (org.netbeans.modules.schema2beans.BaseBean) entry.getValue();
                if (null != cmpRoot) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    cmpRoot.write(baos);
                    contentMap.put(entry.getKey(), baos.toByteArray());
                } else {
                    jsr88Logger.warning("no bean for key: " + entry.getKey()); // NOI18N
                }
            }
            /*if (!this.dObj.getType().equals(ModuleType.EAR)) {
            if (null != bean) {
            bean.write(outputStream);
            }
            } else {
            dp.write(outputStream);
            }*/
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            ConfigurationException ce = new ConfigurationException("Sun Deployment Descriptor Error"); // NOI18N
            ce.initCause(ex);
        }
        jsr88Logger.exiting(this.getClass().toString(), "save", rootBean); // NOI18N
    }

    private void refreshGraphFromContentMap(BaseRoot br) {
        List pending = new ArrayList();
        BaseRoot rootToRestore = br;
/*        if (null == dDBeanRoot) {
        // no rootbean, add roots from this deployment configuration
        Iterator rootIter = getDCBRootCache().entrySet().iterator();
        while(rootIter.hasNext()) {
        pending.add(((Map.Entry) rootIter.next()).getValue());
        }
        rootToRestore = getMasterDCBRoot();
        } else {
        rootToRestore = (BaseRoot) getDCBRootCache().get(dDBeanRoot);
        if(null != rootToRestore) {*/ pending.add(rootToRestore);


        int index = 0;
        while (index < pending.size()) {
            Base current = (Base) pending.get(index);
            try {
                current.loadFromPlanFile(this);
            } catch (java.lang.IllegalStateException ise) {
                jsr88Logger.throwing(current.getClass().toString(), "loadFromPlanFile", ise);
                assert ise == null;
            }
            pending.addAll(current.getChildren());
            index++;
        }
    }

    // Formerly DeploymentPlanSplitter read/write config files.
    public void readDeploymentPlanFiles(ConfigurationStorage storage, DDBeanRoot masterRootDDBean) throws ConfigurationException {
        boolean loadGraph = false;

        contentMap.clear();
        BaseRoot masterRoot = (BaseRoot) getDConfigBeanRoot(masterRootDDBean);

        int len = getValidatedNumberOfFiles(configFiles);
        for (int i = 0; i < len; i++) {
            if (configFiles[i].exists()) {
                System.out.println("Reading descriptor file " + configFiles[i].getName());
                addFileToPlanForModule(configFiles[i], module, storage);
                loadGraph = true;
            }
        }

        // refresh the configuration...
        if (loadGraph) {
            if (masterRoot != null) {
                refreshGraphFromContentMap(masterRoot);
            } else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Loaded configuration data from disk but master DConfigBeanRoot is null.")); // NOI18N
            }
        }

        for (int j = 0; j < configFiles.length; j++) {
            FileObject fo = FileUtil.toFileObject(configFiles[j]);
            if (fo != null) {
                fo.refresh(true);
            }
        }
    }

    // Formerly DeploymentPlanSplitter read/write config files.
    public void readDeploymentPlanFiles(ConfigurationStorage storage, J2eeModule mod) throws ConfigurationException {
        boolean loadGraph = false;

        contentMap.clear();
        BaseRoot masterRoot = (BaseRoot) getDConfigBeanRoot(mod);

        int len = getValidatedNumberOfFiles(configFiles);
        for (int i = 0; i < len; i++) {
            if (configFiles[i].exists()) {
                addFileToPlanForModule(configFiles[i], module, storage);
                loadGraph = true;
            }
        }

        // refresh the configuration...
        if (loadGraph) {
            if (masterRoot != null) {
                refreshGraphFromContentMap(masterRoot);
//            } else {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new IllegalStateException("Loaded configuration data from disk but master DConfigBeanRoot is null.")); // NOI18N
            }
        }

        for (int j = 0; j < configFiles.length; j++) {
            FileObject fo = FileUtil.toFileObject(configFiles[j]);
            if (fo != null) {
                fo.refresh(true);
            }
        }
    }

    public void writeDeploymentPlanFiles(ConfigurationStorage storage) throws IOException, ConfigurationException {
        // Update content map, then write all files.
        updateContentMap(null);

        int len = getValidatedNumberOfFiles(configFiles);
        for (int i = 0; i < len; i++) {
            extractFileFromPlanForModule(configFiles[i], module, storage);
        }

        for (int j = 0; j < configFiles.length; j++) {
            FileObject fo = FileUtil.toFileObject(configFiles[j]);
            if (fo != null) {
                fo.refresh(true);
            }
        }
    }

    int getValidatedNumberOfFiles(File[] files) throws ConfigurationException {
        int len = 0;
        if (null != files) {
            len = files.length;
        }

        // ?? This should check by module type
        if (len < 1) {
            throw new ConfigurationException("file list is too short"); // !PW should this be I18N????
        }

        return len;
    }
//    public void instanceAdded(String serverInstanceID) {
//        System.out.println("SunONEDeploymentConfiguration::instanceAdded: " + serverInstanceID);
//
//        J2eeModuleProvider provider = getProvider(configFiles[0].getParentFile());
//        if(provider != null) {
//            String instance = provider.getServerInstanceID();
//            String serverType = provider.getServerID();
//            System.out.println("Current instance: " + instance + ", serverType: " + serverType);
//        }
//    }
//
//    public void instanceRemoved(String serverInstanceID) {
//        System.out.println("SunONEDeploymentConfiguration::instanceRemoved: " + serverInstanceID);
//
//        J2eeModuleProvider provider = getProvider(configFiles[0].getParentFile());
//        if(provider != null) {
//            String instance = provider.getServerInstanceID();
//            String serverType = provider.getServerID();
//            System.out.println("Current instance: " + instance + ", serverType: " + serverType);
//        }
//    }
//
//    public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
//        System.out.println("SunONEDeploymentConfiguration::changeDefaultInstance: old instance" + oldServerInstanceID + ", new instance: " + newServerInstanceID);
//
//        J2eeModuleProvider provider = getProvider(configFiles[0].getParentFile());
//        if(provider != null) {
//            String instance = provider.getServerInstanceID();
//            String serverType = provider.getServerID();
//            System.out.println("Current instance: " + instance + ", serverType: " + serverType);
//        }
//    }
// !PW FIXME replace these with more stable version of equivalent functionality
    // once Vince or j2eeserver crew can implement a good api for this.
    // this code will NOT work for remote servers.
    private ASDDVersion getTargetAppServerVersion() {
        ASDDVersion result = null;
        J2eeModuleProvider provider = getProvider(configFiles[0].getParentFile());
        String serverType = provider.getServerID();
// [/tools/as81ur2]deployer:Sun:AppServer::localhost:4848, serverType: J2EE
// [/tools/as82]deployer:Sun:AppServer::localhost:4848, serverType: J2EE
// [/tools/glassfish_b35]deployer:Sun:AppServer::localhost:4948, serverType: J2EE
        if ("J2EE".equals(serverType)) {
            // NOI18N
            String instance = provider.getServerInstanceID();
            if (Utils.notEmpty(instance)) {
                try {
                    String asInstallPath = instance.substring(1, instance.indexOf("deployer") - 1);
                    File asInstallFolder = new File(asInstallPath);
                    if (asInstallFolder.exists()) {
                        result = getInstalledAppServerVersion(asInstallFolder);
                    }
                } catch (IndexOutOfBoundsException ex) {
                    // Can't identify server install folder.
                    ErrorManager.getDefault().log(ErrorManager.WARNING, NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoServerInstallLocation", instance)); // NOI18N
                } catch (NullPointerException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        } else if ("SUNWebserver7".equals(serverType)) {
            // NOI18N
            result = ASDDVersion.SUN_APPSERVER_8_1;
        }

        return result;
    }

    private ASDDVersion getInstalledAppServerVersion(File asInstallFolder) {
        File dtdFolder = new File(asInstallFolder, "lib/dtds/"); // NOI18N
        if (dtdFolder.exists()) {
            File domain12dtd = new File(dtdFolder, "sun-domain_1_2.dtd"); // NOI18N
            if (domain12dtd.exists()) {
                return ASDDVersion.SUN_APPSERVER_9_0;
            }
            File domain11dtd = new File(dtdFolder, "sun-domain_1_1.dtd"); // NOI18N
            if (domain11dtd.exists()) {
                return ASDDVersion.SUN_APPSERVER_8_1;
            }
            File domain10dtd = new File(dtdFolder, "sun-domain_1_0.dtd"); // NOI18N
            if (domain10dtd.exists()) {
                return ASDDVersion.SUN_APPSERVER_7_0;
            }
        }

        return null;
    }

    /**
     * Implementation of DS Management API in ConfigurationSupport
     * @return Returns true of plugin implements DS Management API's
     */
    public boolean isDatasourceCreationSupported() {
        return true;
    }

    /**
     * Implementation of DS Management API in ConfigurationSupport
     * @return Returns Set of SunDataSource's(JDBC Resources) present in this J2EE project
     * SunDataSource is a combination of JDBC & JDBC Connection Pool Resources.
     */
    public Set<Datasource> getDatasources() {
        Set<Datasource> datasources = null;
        ResourceConfiguratorInterface rci = getResourceConfigurator();
        if ((rci != null) && (resourceDir != null) && resourceDir.exists()) {
            datasources = rci.getResources(resourceDir);
        }
        if(datasources == null) {
            datasources = new HashSet<Datasource>();
        }
        return datasources;
    }

    /**
     * Implementation of DS Management API in ConfigurationSupport
     * Creates DataSource objects for this J2EE Project
     * @param jndiName JNDI Name of JDBC Resource
     * @param url Url for database referred to by this JDBC Resource's Connection Pool
     * @param username UserName for database referred to by this JDBC Resource's Connection Pool
     * @param password Password for database referred to by this JDBC Resource's Connection Pool
     * @param driver Driver ClassName for database referred to by this JDBC Resource's Connection Pool
     * @return Set containing SunDataSource
     */
    public Datasource createDatasource(final String jndiName, final String url, final String username, final String password, final String driver) throws UnsupportedOperationException, org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException, DatasourceAlreadyExistsException {
        Datasource ds = null;
        if (resourceDir == null) {
            // Unable to create JDBC data source for resource ref.
            postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoRefJdbcDataSource", jndiName)); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoRefJdbcDataSource", jndiName)); // NOI18N
        }

        ResourceConfiguratorInterface rci = getResourceConfigurator();
        if (rci != null) {
            ds = rci.createDataSource(jndiName, url, username, password, driver, resourceDir);
        }
        return ds;
    }

    public void bindDatasourceReference(String referenceName, String jndiName) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(configFiles[0], true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef ref = findNamedBean(sunDDRoot, referenceName, SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                if (ref != null) {
                    // set jndi name of existing reference.
                    assert referenceName.equals(ref.getResRefName());
                    ref.setJndiName(jndiName);
                } else {
                    // add new resource-ref
                    if (sunDDRoot instanceof SunWebApp) {
                        ref = ((SunWebApp) sunDDRoot).newResourceRef();
                    } else if (sunDDRoot instanceof SunApplicationClient) {
                        ref = ((SunApplicationClient) sunDDRoot).newResourceRef();
                    }
                    ref.setResRefName(referenceName);
                    ref.setJndiName(jndiName);
                    sunDDRoot.addValue(SunWebApp.RESOURCE_REF, ref);
                }

                // if changes, save file.
                sunDDRoot.write(primarySunDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }

    public String findDatasourceJndiName(String referenceName) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName)) {
            return null;
        }

        String jndiName = null;
        try {
            RootInterface sunDDRoot = getSunDDRoot(false);
            org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef ref = findNamedBean(sunDDRoot, referenceName, SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
            if (ref != null) {
                // get jndi name of existing reference.
                assert referenceName.equals(ref.getResRefName());
                jndiName = ref.getJndiName();
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }

        return jndiName;
    }

    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(ejbType) || Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(configFiles[0], true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                if (sunDDRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb == null) {
                        ejb = eb.newEjb();
                        ejb.setEjbName(ejbName);
                        eb.addEjb(ejb);
                    }

                    org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef ref = findNamedBean(ejb, referenceName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                    if (ref != null) {
                        // set jndi name of existing reference.
                        assert referenceName.equals(ref.getResRefName());
                        ref.setJndiName(jndiName);
                    } else {
                        // add new resource-ref
                        ref = ejb.newResourceRef();
                        ref.setResRefName(referenceName);
                        ref.setJndiName(jndiName);
                        ejb.addValue(Ejb.RESOURCE_REF, ref);
                    }

                    // if changes, save file.
                    sunEjbJar.write(primarySunDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }

    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(referenceName)) {
            return null;
        }

        String jndiName = null;
        try {
            RootInterface sunDDRoot = getSunDDRoot(false);
            if (sunDDRoot instanceof SunEjbJar) {
                SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if (eb != null) {
                    Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb != null) {
                        org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef ref = findNamedBean(ejb, referenceName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                        if (ref != null) {
                            // get jndi name of existing reference.
                            assert referenceName.equals(ref.getResRefName());
                            jndiName = ref.getJndiName();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }

        return jndiName;
    }

    public void setCMPResource(String ejbName, String jndiName) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(configFiles[0], true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                if (sunDDRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    CmpResource cmpResource = eb.getCmpResource();
                    if(cmpResource == null) {
                        cmpResource = eb.newCmpResource();
                        eb.setCmpResource(cmpResource);
                    }

                    String oldJndiName = cmpResource.getJndiName();
                    if(!Utils.strEquivalent(oldJndiName, jndiName)) {
                        if(Utils.notEmpty(oldJndiName)) {
                            // !PW FIXME changing existing jndi name, should we notify user?
                        }

                        cmpResource.setJndiName(jndiName);

                        // if changes, save file.
                        sunEjbJar.write(primarySunDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }
    
    public void mapCmpBeans(OriginalCMPMapping [] mapping) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        if(!J2eeModule.EJB.equals(module.getModuleType())) {
            return; // wrong module type.
        }
        
        try {
            FileObject sunCmpDDFO = getSunDD(configFiles[1], true);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;

                    // EjbJarRoot.mapCmpBeans() body moves to here, integrating with the graph above.

                    // if changes, save file.
                    sunCmpMappings.write(sunCmpDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionMapCmpBeans", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionMapCmpBeans", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }

    // Could have beanProp & nameProp in db indexed by Class<T>
    private <T extends CommonDDBean> T findNamedBean(CommonDDBean parentDD, String referenceName, /*Class<T> c,*/ String beanProp, String nameProp) {
        T result = null;
        T[] beans = (T[]) parentDD.getValues(beanProp);
        if (beans != null) {
            for (int i = 0; i < beans.length; i++) {
                String name = (String) beans[0].getValue(nameProp);
                if (referenceName.equals(name)) {
                    result = beans[i];
                    break;
                }
            }
        }
        return result;
    }

    /*****************************  MessageDestinationConfiguration **************************************/
    public Set<MessageDestination> getMessageDestinations() throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        Set<MessageDestination> destinations = null;
        ResourceConfiguratorInterface rci = getResourceConfigurator();
        if (resourceDir != null && resourceDir.exists()) {
            destinations = rci.getMessageDestinations(resourceDir);
        }
        if(destinations == null) {
            destinations = new HashSet<MessageDestination>();
        }
        return destinations;
    }

    public boolean supportsCreateMessageDestination() {
        return true;
    }

    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) throws UnsupportedOperationException, org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        MessageDestination jmsResource = null;
        if (resourceDir == null) {
            // Unable to create reqested JMS Resource
            postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoJMSResource", name)); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoJMSResource", name)); // NOI18N
        }
        ResourceConfiguratorInterface rci = getResourceConfigurator();
        if (rci != null) {
            if (!rci.isJMSResourceDefined(name, resourceDir)) {
                jmsResource = rci.createJMSResource(name, type, name, resourceDir);
            }
        }
        return jmsResource;
    }

    public void bindMdbToMessageDestination(String mdbName, String name, MessageDestination.Type type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(mdbName) || Utils.strEmpty(name)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(configFiles[0], true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                if (sunDDRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }
                    Ejb ejb = findNamedBean(eb, mdbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb == null) {
                        ejb = eb.newEjb();
                        ejb.setEjbName(mdbName);
                        eb.addEjb(ejb);
                    }
                    ejb.setJndiName(name);
                    String factory = "jms/" + name + "Factory"; //NOI18N
                    MdbConnectionFactory connFactory = ejb.newMdbConnectionFactory();
                    connFactory.setJndiName(factory);
                    ejb.setMdbConnectionFactory(connFactory);
//                    /* I think the following is not needed. These entries are being created through
//                     * some other path - Peter
//                     */
//                    org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination destination = findNamedBean(eb, mdbName, EnterpriseBeans.MESSAGE_DESTINATION, org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination.JNDI_NAME);
//                    if (destination == null) {
//                        destination = eb.newMessageDestination();
//                        destination.setJndiName(name);
//                        eb.addMessageDestination(destination);
//                    }
                    // if changes, save file.
                    sunDDRoot.write(primarySunDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }

    public String findMessageDestinationName(String mdbName) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(mdbName)) {
            return null;
        }

        String destinationName = null;
        try {
            RootInterface sunDDRoot = getSunDDRoot(false);
            if(sunDDRoot instanceof SunEjbJar) {
                SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if (eb != null) {
                    Ejb ejb = findNamedBean(eb, mdbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb != null) {
                        assert mdbName.equals(ejb.getEjbName());
                        destinationName = ejb.getJndiName();
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
        return destinationName;
    }

    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, String destName, MessageDestination.Type type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {

        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(connectionFactoryName) || Utils.strEmpty(destName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(configFiles[0], true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef destRef = findNamedBean(sunDDRoot, referenceName, SunWebApp.MESSAGE_DESTINATION_REF, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME);
                if (destRef != null) {
                    // set jndi name of existing reference.
                    assert referenceName.equals(destRef.getMessageDestinationRefName());
                    destRef.setJndiName(referenceName);
                } else {
                    // add new resource-ref
                    if (sunDDRoot instanceof SunWebApp) {
                        destRef = ((SunWebApp) sunDDRoot).newMessageDestinationRef();
                    } else if (sunDDRoot instanceof SunApplicationClient) {
                        destRef = ((SunApplicationClient) sunDDRoot).newMessageDestinationRef();
                    }
                    destRef.setJndiName(referenceName);
                    destRef.setMessageDestinationRefName(referenceName);
                    sunDDRoot.addValue(SunWebApp.MESSAGE_DESTINATION_REF, destRef);
                }

                org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef factoryRef = findNamedBean(sunDDRoot, connectionFactoryName, SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                if (factoryRef != null) {
                    // set jndi name of existing reference.
                    assert connectionFactoryName.equals(factoryRef.getResRefName());
                    factoryRef.setJndiName(connectionFactoryName);
                } else {
                    // add new resource-ref
                    if (sunDDRoot instanceof SunWebApp) {
                        factoryRef = ((SunWebApp) sunDDRoot).newResourceRef();
                    } else if (sunDDRoot instanceof SunApplicationClient) {
                        factoryRef = ((SunApplicationClient) sunDDRoot).newResourceRef();
                    }
                    factoryRef.setResRefName(connectionFactoryName);
                    factoryRef.setJndiName(connectionFactoryName);
                    sunDDRoot.addValue(SunWebApp.RESOURCE_REF, factoryRef);
                }

                // if changes, save file.
                sunDDRoot.write(primarySunDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }

    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType, String referenceName, String connectionFactoryName, String destName, MessageDestination.Type type) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        try {
            FileObject primarySunDDFO = getSunDD(configFiles[0], true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if (eb == null) {
                    eb = sunEjbJar.newEnterpriseBeans();
                    sunEjbJar.setEnterpriseBeans(eb);
                }
                Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                if (ejb == null) {
                    ejb = eb.newEjb();
                    ejb.setEjbName(ejbName);
                    eb.addEjb(ejb);
                }
                if ((org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION.equals(ejbType)) || (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY.equals(ejbType))) {
                    org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef ref = findNamedBean(ejb, connectionFactoryName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
                    if (ref != null) {
                        // set jndi name of existing reference.
                        assert referenceName.equals(ref.getResRefName());
                        ref.setJndiName(connectionFactoryName);
                    } else {
                        // add new resource-ref
                        ref = ejb.newResourceRef();
                        ref.setResRefName(connectionFactoryName);
                        ref.setJndiName(connectionFactoryName);
                        ejb.addResourceRef(ref);
                    }

                    org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef destRef = findNamedBean(ejb, referenceName, Ejb.MESSAGE_DESTINATION_REF, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME);
                    if (destRef != null) {
                        // set jndi name of existing reference.
                        assert referenceName.equals(destRef.getMessageDestinationRefName());
                        destRef.setJndiName(referenceName);
                    } else {
                        destRef = ejb.newMessageDestinationRef();
                        destRef.setJndiName(referenceName);
                        destRef.setMessageDestinationRefName(referenceName);
                        ejb.addMessageDestinationRef(destRef);
                    }
                }else if(org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)){
                    ejb.setJndiName(referenceName);
                    MdbConnectionFactory connFactory = ejb.newMdbConnectionFactory();
                    connFactory.setJndiName(connectionFactoryName);
                    ejb.setMdbConnectionFactory(connFactory);
                    org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination destination = findNamedBean(eb, referenceName, EnterpriseBeans.MESSAGE_DESTINATION, org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination.JNDI_NAME);
                    if (destination == null) {
                        destination = eb.newMessageDestination();
                        destination.setJndiName(referenceName);
                        eb.addMessageDestination(destination);
                    }
                }
                // if changes, save file.
                sunDDRoot.write(primarySunDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }

    private DConfigBeanRoot getDConfigBeanRoot(J2eeModule module) throws ConfigurationException {
        if (null == module) {
            throw Utils.makeCE("ERR_DDBeanIsNull", null, null);
        }
//        if (null == dDBeanRoot.getXpath()) {
//            throw Utils.makeCE("ERR_DDBeanHasNullXpath", null, null);
//        }
//        // If DDBean is not from our internal tree, normalize it to one that is.
//        if(!(dDBeanRoot instanceof DDRoot)) {
//            // If the root cache is empty, then it is likely that
//            if(getDCBRootCache().entrySet().size() == 0) {
//                throw Utils.makeCE("ERR_CannotNormalizeDDBean",
//                        new Object [] { dDBeanRoot.getClass().getName() }, null);
//            }
//            dDBeanRoot = getStorage().normalizeDDBeanRoot(dDBeanRoot);
//        }
        BaseRoot rootDCBean = (BaseRoot) getDCBRootCache().get(module);

        if (null == rootDCBean) {
            DCBFactory factory = (DCBFactory) getDCBFactoryMap().get(module);
            if (factory != null) {
                rootDCBean = (BaseRoot) factory.createDCB(module, null);
                if (rootDCBean != null) {
                    getDCBCache().put(module, rootDCBean);
                    getDCBRootCache().put(module, rootDCBean);
                }
            }
        }

        return rootDCBean;
    }

    public void ensureResourceDefined(org.netbeans.modules.j2ee.dd.api.common.ComponentInterface ci) {
        // TODO : remove before merge to trunk
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new UnsupportedOperationException());
    }

    public void ensureResourceDefinedForEjb(org.netbeans.modules.j2ee.dd.api.common.ComponentInterface ci, String jndi) {
        // TODO : remove before merge to trunk
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new UnsupportedOperationException());
    }

    /** extend the vendor specific descriptor
     *
     * @param referenceName
     * @param referencedEjbName
     * @throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
     */
    public void bindEjbReference(String referenceName, String referencedEjbName) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(referencedEjbName)) {
            return;
        }

        // Version > 2.4, then return, but we can't compare directly against 2.4
        // because FP formats are not exact.
        //
        // !PW this appears to be overloaded logic in that it's differentiating 
        // servlet 2.4 from servlet 2.5 and also appclient 1.4 from appclient 5.0,
        // hence the odd usage of "2.45" in the comparison.
        //
        try {
            if (Double.parseDouble(module.getModuleVersion()) > 2.45) {
                return;
            }
        } catch(NumberFormatException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        try {
            FileObject primarySunDDFO = getSunDD(configFiles[0], true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ref = findNamedBean(sunDDRoot, referenceName, SunWebApp.EJB_REF, EjbRef.EJB_REF_NAME);
                if (ref != null) {
                    // set jndi name of existing reference.
                    assert referenceName.equals(ref.getEjbRefName());
                    ref.setJndiName(referencedEjbName);
                } else {
                    // add new ejb-ref
                    if (sunDDRoot instanceof SunWebApp) {
                        ref = ((SunWebApp) sunDDRoot).newEjbRef();
                    } else if (sunDDRoot instanceof SunApplicationClient) {
                        ref = ((SunApplicationClient) sunDDRoot).newEjbRef();
                    }
                    ref.setEjbRefName(referenceName);
                    ref.setJndiName(referencedEjbName);
                    sunDDRoot.addValue(SunWebApp.EJB_REF, ref);
                }

                // if changes, save file.
                sunDDRoot.write(primarySunDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }

    /** extend the vendor specific descriptor
     *
     * @param ejbName
     * @param ejbType
     * @param referenceName
     * @param jndiName
     * @throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
     */
    public void bindEjbReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(ejbType) || Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        // Version > 2.1, then return, but we can't compare directly against 2.1 
        // because FP formats are not exact.
        try {
            if (Double.parseDouble(module.getModuleVersion()) > 2.15) {
                return;
            }
        } catch(NumberFormatException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        try {
            FileObject primarySunDDFO = getSunDD(configFiles[0], true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                if (sunDDRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                    if (ejb == null) {
                        ejb = eb.newEjb();
                        ejb.setEjbName(ejbName);
                        eb.addEjb(ejb);
                    }

                    org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ref = findNamedBean(ejb, referenceName, Ejb.EJB_REF, EjbRef.EJB_REF_NAME);
                    if (ref != null) {
                        // set jndi name of existing reference.
                        assert referenceName.equals(ref.getEjbRefName());
                        ref.setJndiName(jndiName);
                    } else {
                        // add new ejb-ref
                        ref = ejb.newEjbRef();
                        ref.setEjbRefName(referenceName);
                        ref.setJndiName(jndiName);
                        ejb.addValue(Ejb.EJB_REF, ref);
                    }

                    // if changes, save file.
                    sunEjbJar.write(primarySunDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException(message, ex);
        }
    }
}
