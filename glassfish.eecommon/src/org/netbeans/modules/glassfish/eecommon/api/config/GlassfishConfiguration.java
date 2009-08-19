/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.eecommon.api.config;

import org.netbeans.modules.glassfish.eecommon.api.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Basic j2eeserver configuration api support for V2 and V3 plugins
 *
 * @author Peter Williams
 */
public abstract class GlassfishConfiguration implements
        ContextRootConfiguration,
        EjbResourceConfiguration,
        MessageDestinationConfiguration,
        DatasourceConfiguration
    {

    protected final J2eeModule module;
    protected final J2eeModuleHelper moduleHelper;
    protected final File primarySunDD;
    protected final File secondarySunDD;
    protected DescriptorListener descriptorListener;

    private ASDDVersion appServerVersion;
    private ASDDVersion minASVersion;
    private ASDDVersion maxASVersion;
    private boolean deferredAppServerChange;


    protected GlassfishConfiguration(J2eeModule module) throws ConfigurationException {
        this(module, J2eeModuleHelper.getJ2eeModuleHelper(module.getType()));
    }

    protected GlassfishConfiguration(J2eeModule module, J2eeModuleHelper moduleHelper) throws ConfigurationException {
        this.module = module;
        this.moduleHelper = moduleHelper;
        if(moduleHelper != null) {
            this.primarySunDD = moduleHelper.getPrimarySunDDFile(module);
            this.secondarySunDD = moduleHelper.getSecondarySunDDFile(module);
        } else {
            throw new ConfigurationException("Unsupported module type: " + module.getType());
        }

        addConfiguration(primarySunDD, this);

        // Default to 8.1 in new beans.  This is set by the bean parser
        // in the appropriate root type, if reading from existing file(s).
        this.appServerVersion = ASDDVersion.SUN_APPSERVER_8_1;
        this.deferredAppServerChange = false;

        try {
            Object mt = module.getType();
            ModuleType moduleType = mt instanceof ModuleType ? (ModuleType) mt : null;
            String moduleVersion = module.getModuleVersion();

            minASVersion = computeMinASVersion(moduleVersion);
            maxASVersion = computeMaxASVersion();
            appServerVersion = maxASVersion;

            J2EEBaseVersion j2eeVersion = J2EEBaseVersion.getVersion(moduleType, moduleVersion);
            boolean isPreJavaEE5 = (j2eeVersion != null) ?
                    (J2EEVersion.J2EE_1_4.compareSpecification(j2eeVersion) >= 0) : false;
            if (!primarySunDD.exists()) {
                // If module is J2EE 1.4 (or 1.3), or this is a web app (where we have
                // a default property even for JavaEE5), then copy the default template.
                if (J2eeModule.WAR.equals(moduleType) || isPreJavaEE5) {
                    try {
                        createDefaultSunDD(primarySunDD);
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex);
                        String defaultMessage = " trying to create " + primarySunDD.getPath(); // Requires I18N
                        displayError(ex, defaultMessage);
                    }
                }
            }

            if(isPreJavaEE5) {
                // Create standard descriptor listener holder
                descriptorListener = new DescriptorListener(this);

                // Attach folder listener to config folder (primarily to monitor for webservices.xml
                // if it does not exist yet.)
                File configDir = primarySunDD.getParentFile();
                FileObject configFolder = FileUtil.toFileObject(configDir);
                if(configFolder != null) {
                    FolderListener.createListener(primarySunDD, configFolder, moduleType);
                }

                // Attach listeners to the standard descriptors to handle automatic
                // jndi-name and endpoint assignment.
                addDescriptorListener(getStandardRootDD());
                addDescriptorListener(getWebServicesRootDD());
            }
        } catch (RuntimeException ex) {
            Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex);
        }

    }

    @Deprecated
    public GlassfishConfiguration() {
        throw new UnsupportedOperationException("JSR-88 configuration not supported.");
    }

    public void dispose() {
        if(descriptorListener != null) {
            descriptorListener.removeListeners();
            descriptorListener = null;
        }

        GlassfishConfiguration storedCfg = getConfiguration(primarySunDD);
        if (storedCfg != this) {
            Logger.getLogger("glassfish-eecommon").log(Level.INFO, "Stored DeploymentConfiguration ("
                    + storedCfg + ") instance not the one being disposed of (" + this + ").");
        }

        if (storedCfg != null) {
            removeConfiguration(primarySunDD);
        }
    }

    // ------------------------------------------------------------------------
    // Appserver version support
    // ------------------------------------------------------------------------
    private ASDDVersion computeMinASVersion(String j2eeModuleVersion) {
        return moduleHelper.getMinASVersion(j2eeModuleVersion, ASDDVersion.SUN_APPSERVER_7_0);
    }

    private ASDDVersion computeMaxASVersion() {
        // This is min of (current server target, 9.0) so if we can figure out the
        // target server, use that, otherwise, use 9.0.
        ASDDVersion result = getTargetAppServerVersion();
        if (result == null) {
            result = ASDDVersion.SUN_APPSERVER_9_0;
            Logger.getLogger("glassfish-eecommon").log(Level.WARNING, NbBundle.getMessage(
                    GlassfishConfiguration.class, "ERR_UnidentifiedTargetServer", result.toString())); // NOI18N
        }
        return result;
    }

    public ASDDVersion getMinASVersion() {
        return minASVersion;
    }

    public ASDDVersion getMaxASVersion() {
        return maxASVersion;
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
            throw new IllegalArgumentException(asVersion.toString() +
                    " is lower than required minimum version " + getMinASVersion().toString());
        }

        if (asVersion.compareTo(getMaxASVersion()) > 0) {
            throw new IllegalArgumentException(asVersion.toString() +
                    " is higher than required maximum version " + getMaxASVersion().toString());
        }

        if (!asVersion.equals(appServerVersion) || deferredAppServerChange) {
            appServerVersion = asVersion;
//            ConfigurationStorage localStorage = getStorage();
//            if (localStorage != null) {
                deferredAppServerChange = false;
//                localStorage.setChanged();
//            }
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

    // !PW FIXME replace these with more stable version of equivalent functionality
    // once Vince or j2eeserver crew can implement a good api for this.
    // this code will NOT work for remote servers.
    private static String [] sunServerIds = {
        "APPSERVERSJS",
        "GlassFishV1",
        "J2EE",
        "JavaEEPlusSIP",
        "gfv3",
        "gfv3ee6"
    };

    protected ASDDVersion getTargetAppServerVersion() {
        ASDDVersion result = null;
        J2eeModuleProvider provider = getProvider(primarySunDD.getParentFile());
        String serverType = provider.getServerID();
// [/tools/as81ur2]deployer:Sun:AppServer::localhost:4848, serverType: J2EE
// [/tools/as82]deployer:Sun:AppServer::localhost:4848, serverType: J2EE
// [/tools/glassfish_b35]deployer:Sun:AppServer::localhost:4948, serverType: J2EE
        if (Arrays.asList(sunServerIds).contains(serverType)) {
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
                    Logger.getLogger("glassfish-eecommon").log(Level.WARNING, NbBundle.getMessage(
                            GlassfishConfiguration.class, "ERR_NoServerInstallLocation", instance)); // NOI18N
                } catch (NullPointerException ex) {
                    Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        } else if ("SUNWebserver7".equals(serverType)) {
            // NOI18N
            result = ASDDVersion.SUN_APPSERVER_8_1;
        }

        return result;
    }

    protected ASDDVersion getInstalledAppServerVersion(File asInstallFolder) {
        File dtdFolder = new File(asInstallFolder, "lib/dtds/"); // NOI18N
        if (dtdFolder.exists()) {
            if (new File(dtdFolder, "sun-domain_1_3.dtd").exists()) {
                // !PW FIXME need to add SUN_APPSERVER_9_1 for V3 (& maybe V2.1)
                return ASDDVersion.SUN_APPSERVER_9_0;
            }
            if (new File(dtdFolder, "sun-domain_1_2.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_9_0;
            }
            if (new File(dtdFolder, "sun-domain_1_1.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_8_1;
            }
            if (new File(dtdFolder, "sun-domain_1_0.dtd").exists()) {
                return ASDDVersion.SUN_APPSERVER_7_0;
            }
        }

        return null;
    }

    // ------------------------------------------------------------------------
    // Access to V2/V3 specific information.  Allows for graceful deprecation
    // of unsupported features (e.g. CMP, etc.)
    // ------------------------------------------------------------------------

    protected void createDefaultSunDD(File sunDDFile) throws IOException {
        boolean isPreAS90 = false; // FIXME (ASDDVersion.SUN_APPSERVER_9_0.compareTo(appServerVersion) > 0);
        String resource = "org-netbeans-modules-j2ee-sun-ddui" + // NOI18N
                (isPreAS90 ? "-version-8_2/" : "/") + sunDDFile.getName(); // NOI18N
        FileObject sunDDTemplate = FileUtil.getConfigFile(resource);
        if (sunDDTemplate != null) {
            FileObject configFolder = FileUtil.createFolder(sunDDFile.getParentFile());
            FileSystem fs = configFolder.getFileSystem();
            XmlFileCreator creator = new XmlFileCreator(sunDDTemplate, configFolder, sunDDTemplate.getName(), sunDDTemplate.getExt());
            fs.runAtomicAction(creator);
        }
    }

    public J2eeModule getJ2eeModule() {
        return module;
    }

    public J2EEBaseVersion getJ2eeVersion() {
        return J2EEBaseVersion.getVersion(module.getType(), module.getModuleVersion());
    }

    public org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = null;
        J2eeModuleHelper j2eeModuleHelper = J2eeModuleHelper.getJ2eeModuleHelper(module.getType());
        if(j2eeModuleHelper != null) {
            stdRootDD = j2eeModuleHelper.getStandardRootDD(module);
        }
        return stdRootDD;
    }

    public org.netbeans.modules.j2ee.dd.api.webservices.Webservices getWebServicesRootDD() {
        org.netbeans.modules.j2ee.dd.api.webservices.Webservices wsRootDD = null;
        J2eeModuleHelper j2eeModuleHelper = J2eeModuleHelper.getJ2eeModuleHelper(module.getType());
        if(j2eeModuleHelper != null) {
            wsRootDD = j2eeModuleHelper.getWebServicesRootDD(module);
        }
        return wsRootDD;
    }

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        return module.getMetadataModel(type);
    }

    /** !PW FIXME web freeform project does not implement J2eeModulePrvoider so
     *  this method will fail for that project type.  This method is used for:
     *
     *  * Getting the server instance id => install location for determining
     *    server version.
     *  * Getting the deployment manager => accessing the ResourceConfigurator
     *    and CMP Mapper (V2 only).
     *
     */
    protected J2eeModuleProvider getProvider(File file) {
        J2eeModuleProvider provider = null;
        if (file != null) {
            file = FileUtil.normalizeFile(file);
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

    // ------------------------------------------------------------------------
    // J2EE 1.4 Automatic Descriptor updating support.
    //
    // Exposed as api outside this package only because CMP related change
    // listeners need to be injected from j2ee.sun.ddui module and handled
    // by SunONEDeploymentConfiguration instances only.
    // ------------------------------------------------------------------------
    public enum ChangeOperation { CREATE, DELETE };

    void updateDefaultEjbJndiName(final String ejbName, final String prefix, final ChangeOperation op) {
        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, op == ChangeOperation.CREATE);

            if(primarySunDDFO != null) {
                boolean changed = false;
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                if (sunDDRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if(eb == null && op == ChangeOperation.CREATE) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    if(eb != null) {
                        Ejb ejb = findNamedBean(eb, ejbName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                        if(ejb == null && op == ChangeOperation.CREATE) {
                            ejb = eb.newEjb();
                            ejb.setEjbName(ejbName);
                            eb.addEjb(ejb);
                        }

                        if(ejb != null) {
                            assert ejbName.equals(ejb.getEjbName());

                            String defaultJndiName = ejbName.startsWith(prefix) ? ejbName : (prefix + ejbName);
                            if(op == ChangeOperation.CREATE && Utils.strEmpty(ejb.getJndiName())) {
                                ejb.setJndiName(defaultJndiName);
                                changed = true;
                            } else if(op == ChangeOperation.DELETE && Utils.strEquals(defaultJndiName, ejb.getJndiName())) {
                                ejb.setJndiName(null);
                                if(ejb.isTrivial(Ejb.EJB_NAME)) {
                                    eb.removeEjb(ejb);
                                    if(eb.isTrivial(null)) {
                                        sunEjbJar.setEnterpriseBeans(null);
                                    }
                                }
                                changed = true;
                            }
                        }
                    }
                }

                if(changed) {
                    sunDDRoot.write(primarySunDDFO);
                }
            }
        } catch(IOException ex) {
            handleEventRelatedIOException(ex);
        } catch(Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    /**
     * Set a default Endpoint Address URI for the specified ejb hosted endpoint.
     */
    void updateDefaultEjbEndpointUri(final String linkName, final String portName, final ChangeOperation op) {
        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);

            if(primarySunDDFO != null) {
                boolean changed = false;
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                if (sunDDRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if(eb == null && op == ChangeOperation.CREATE) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    if(eb != null) {
                        Ejb ejb = findNamedBean(eb, linkName, EnterpriseBeans.EJB, Ejb.EJB_NAME);
                        if(ejb == null && op == ChangeOperation.CREATE) {
                            ejb = eb.newEjb();
                            ejb.setEjbName(linkName);
                            eb.addEjb(ejb);
                        }

                        if(ejb != null) {
                            assert linkName.equals(ejb.getEjbName());

                            WebserviceEndpoint endpoint = findNamedBean(ejb, portName, Ejb.WEBSERVICE_ENDPOINT,
                                    WebserviceEndpoint.PORT_COMPONENT_NAME);
                            if(endpoint == null && op == ChangeOperation.CREATE) {
                                endpoint = ejb.newWebserviceEndpoint();
                                endpoint.setPortComponentName(portName);
                                ejb.addWebserviceEndpoint(endpoint);
                            }

                            if(endpoint != null) {
                                assert portName.equals(endpoint.getPortComponentName());

                                if(op == ChangeOperation.CREATE && Utils.strEmpty(endpoint.getEndpointAddressUri())) {
                                    String defaultUri = portName;
                                    endpoint.setEndpointAddressUri(defaultUri);
                                    changed = true;
                                } else if(op == ChangeOperation.DELETE) {
                                    endpoint.setEndpointAddressUri(null);
                                    if(endpoint.isTrivial(WebserviceEndpoint.PORT_COMPONENT_NAME)) {
                                        ejb.removeWebserviceEndpoint(endpoint);
                                        if(ejb.isTrivial(Ejb.EJB_NAME)) {
                                            eb.removeEjb(ejb);
                                            if(eb.isTrivial(null)) {
                                                sunEjbJar.setEnterpriseBeans(null);
                                            }
                                        }
                                    }
                                    changed = true;
                                }
                            }
                        }
                    }
                }

                if(changed) {
                    sunDDRoot.write(primarySunDDFO);
                }
            }
        } catch(IOException ex) {
            handleEventRelatedIOException(ex);
        } catch(Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    // ------------------------------------------------------------------------
    // J2EE Server API implementations
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Implementation of ContextRootConfiguration
    // ------------------------------------------------------------------------
    public String getContextRoot() throws ConfigurationException {
        String contextRoot = null;
        if (J2eeModule.Type.WAR.equals(module.getType())) {
            try {
                RootInterface rootDD = getSunDDRoot(false);
                if (rootDD instanceof SunWebApp) {
                    contextRoot = ((SunWebApp) rootDD).getContextRoot();
                }
            } catch (IOException ex) {
                Logger.getLogger("glassfish-eecommon").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                String defaultMessage = " retrieving context-root from sun-web.xml";
                displayError(ex, defaultMessage);
            }
        } else {
            Logger.getLogger("glassfish-eecommon").log(Level.WARNING,
                    "GlassfishConfiguration.getContextRoot() invoked on incorrect module type: " + module.getType());
        }
        return contextRoot;
    }

    public void setContextRoot(final String contextRoot) throws ConfigurationException {
        if (J2eeModule.Type.WAR.equals(module.getType())) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        FileObject primarySunDDFO = getSunDD(primarySunDD, true);
                        if (primarySunDDFO != null) {
                            RootInterface rootDD = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                            if (rootDD instanceof SunWebApp) {
                                SunWebApp swa = (SunWebApp) rootDD;
                                swa.setContextRoot(contextRoot);
                                swa.write(primarySunDDFO);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish-eecommon").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        String defaultMessage = " trying set context-root in sun-web.xml";
                        displayError(ex, defaultMessage);
                    } catch (Exception ex) {
                        Logger.getLogger("glassfish-eecommon").log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        String defaultMessage = " trying set context-root in sun-web.xml";
                        displayError(ex, defaultMessage);
                    }
                }
            });
        } else {
            Logger.getLogger("glassfish-eecommon").log(Level.WARNING,
                    "GlassfishConfiguration.setContextRoot() invoked on incorrect module type: " + module.getType());
        }
    }


    // ------------------------------------------------------------------------
    // Implementation of DatasourceConfiguration
    // ------------------------------------------------------------------------
    public abstract Set<Datasource> getDatasources() throws ConfigurationException;

    public abstract boolean supportsCreateDatasource();

    public abstract Datasource createDatasource(String jndiName, String url, 
            String username, String password, String driver)
            throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException;

    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                ResourceRef ref = findNamedBean(sunDDRoot, referenceName, SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
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
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(ejbType) ||
                Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
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

                    ResourceRef ref = findNamedBean(ejb, referenceName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
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
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName)) {
            return null;
        }

        String jndiName = null;
        try {
            RootInterface sunDDRoot = getSunDDRoot(false);
            ResourceRef ref = findNamedBean(sunDDRoot, referenceName, SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
            if (ref != null) {
                // get jndi name of existing reference.
                assert referenceName.equals(ref.getResRefName());
                jndiName = ref.getJndiName();
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }

        return jndiName;    }

    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
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
                        ResourceRef ref = findNamedBean(ejb, referenceName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
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
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionReadingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }

        return jndiName;
    }


    // ------------------------------------------------------------------------
    // Implementation of EjbResourceConfiguration
    // ------------------------------------------------------------------------
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName)) {
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
                        // get jndi name of existing reference.
                        assert ejbName.equals(ejb.getEjbName());
                        jndiName = ejb.getJndiName();
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionReadingEjb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionReadingEjb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }

        return jndiName;
    }

    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
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
            Logger.getLogger("glassfish-eecommon").log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }

        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                EjbRef ref = findNamedBean(sunDDRoot, referenceName, SunWebApp.EJB_REF, EjbRef.EJB_REF_NAME);
                if (ref != null) {
                    // set jndi name of existing reference.
                    assert referenceName.equals(ref.getEjbRefName());
                    ref.setJndiName(jndiName);
                } else {
                    // add new ejb-ref
                    if (sunDDRoot instanceof SunWebApp) {
                        ref = ((SunWebApp) sunDDRoot).newEjbRef();
                    } else if (sunDDRoot instanceof SunApplicationClient) {
                        ref = ((SunApplicationClient) sunDDRoot).newEjbRef();
                    }
                    ref.setEjbRefName(referenceName);
                    ref.setJndiName(jndiName);
                    sunDDRoot.addValue(SunWebApp.EJB_REF, ref);
                }

                // if changes, save file.
                sunDDRoot.write(primarySunDDFO);
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    public void bindEjbReferenceForEjb(String ejbName, String ejbType, String referenceName,
            String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(ejbType) ||
                Utils.strEmpty(referenceName) || Utils.strEmpty(jndiName)) {
            return;
        }

        // Version > 2.1, then return, but we can't compare directly against 2.1
        // because FP formats are not exact.
        try {
            if (Double.parseDouble(module.getModuleVersion()) > 2.15) {
                return;
            }
        } catch(NumberFormatException ex) {
            Logger.getLogger("glassfish-eecommon").log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }

        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
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

                    EjbRef ref = findNamedBean(ejb, referenceName, Ejb.EJB_REF, EjbRef.EJB_REF_NAME);
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
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingEjbRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }


    // ------------------------------------------------------------------------
    // Implementation of MessageDestinationConfiguration
    // ------------------------------------------------------------------------
    public abstract Set<MessageDestination> getMessageDestinations() throws ConfigurationException;

    public abstract boolean supportsCreateMessageDestination();

    public abstract MessageDestination createMessageDestination(String name, Type type)
            throws UnsupportedOperationException, ConfigurationException;

    public void bindMdbToMessageDestination(String mdbName, String name, Type type) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(mdbName) || Utils.strEmpty(name)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
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
                    String factory = name + "Factory"; //NOI18N
                    MdbConnectionFactory connFactory = ejb.newMdbConnectionFactory();
                    connFactory.setJndiName(factory);
                    ejb.setMdbConnectionFactory(connFactory);
//                    /* I think the following is not needed. These entries are being created through
//                     * some other path - Peter
//                     */
//                    org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination destination = 
//                            findNamedBean(eb, mdbName, EnterpriseBeans.MESSAGE_DESTINATION,
//                            org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination.JNDI_NAME);
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
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
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
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
        return destinationName;
    }

    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName,
            String destName, Type type) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(referenceName) || Utils.strEmpty(connectionFactoryName) || Utils.strEmpty(destName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                MessageDestinationRef destRef = findNamedBean(sunDDRoot, referenceName,
                        SunWebApp.MESSAGE_DESTINATION_REF, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME);
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

                ResourceRef factoryRef = findNamedBean(sunDDRoot, connectionFactoryName,
                        SunWebApp.RESOURCE_REF, ResourceRef.RES_REF_NAME);
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
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType, String referenceName,
            String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
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
                if ((org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION.equals(ejbType)) ||
                        (org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY.equals(ejbType))) {
                    org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef ref =
                            findNamedBean(ejb, connectionFactoryName, Ejb.RESOURCE_REF, ResourceRef.RES_REF_NAME);
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

                    MessageDestinationRef destRef = findNamedBean(ejb, referenceName,
                            Ejb.MESSAGE_DESTINATION_REF, MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME);
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
                } else if(org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN.equals(ejbType)){
                    ejb.setJndiName(referenceName);
                    MdbConnectionFactory connFactory = ejb.newMdbConnectionFactory();
                    connFactory.setJndiName(connectionFactoryName);
                    ejb.setMdbConnectionFactory(connFactory);
                    org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination destination =
                            findNamedBean(eb, referenceName, EnterpriseBeans.MESSAGE_DESTINATION,
                            org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination.JNDI_NAME);
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
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(GlassfishConfiguration.class,
                    "ERR_ExceptionBindingMdb", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of DeploymentPlanConfiguration.
    //
    // save(OutputStream) is renamed due to conflict with JSR-88 method
    // DeploymentConfiguration.save(OutputStream).  Differentiation between
    // these two methods is performed by ModuleConfigurationImpl class.
    // ------------------------------------------------------------------------
    public void saveConfiguration(OutputStream outputStream) throws ConfigurationException {
        try {
            if (this.module.getType().equals(J2eeModule.Type.WAR)) {
                // copy sun-web.xml to stream directly.
                FileObject configFO = FileUtil.toFileObject(primarySunDD);
                if(configFO != null) {
                    RootInterface rootDD = DDProvider.getDefault().getDDRoot(configFO);
                    rootDD.write(outputStream);
                }
            } else {
                Logger.getLogger("glassfish-eecommon").log(Level.WARNING,
                        "Deployment plan not supported in GlassfishConfiguration.save()");
            }
        } catch(Exception ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }


    // ------------------------------------------------------------------------
    // Internal implementation methods.
    // ------------------------------------------------------------------------

    /* ------------------------------------------------------------------------
     * Default descriptor file creation, root interface retrieval
     */
    // This method is only useful for reading the model.  If the model is to
    // be modified and rewritten to disk, you'll need the FileObject it was
    // retrieved from as well.
    protected RootInterface getSunDDRoot(boolean create) throws IOException {
        RootInterface sunDDRoot = null;
        FileObject primarySunDDFO = getSunDD(primarySunDD, create);
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

    protected FileObject getSunDD(File sunDDFile, boolean create) throws IOException {
        if (!sunDDFile.exists()) {
            if (create) {
                createDefaultSunDD(sunDDFile);
            } else {
                return null;
            }
        }
        return FileUtil.toFileObject(sunDDFile);
    }

    protected void displayError(Exception ex, String defaultMessage) {
        String message = ex.getLocalizedMessage();
        if(message == null || message.length() == 0) {
            message = ex.getClass().getSimpleName() + defaultMessage;
        }
        final NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                message, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(msg);
    }

    // Could have beanProp & nameProp in db indexed by Class<T>
    protected <T extends CommonDDBean> T findNamedBean(
            CommonDDBean parentDD, String referenceName, /*Class<T> c,*/ String beanProp, String nameProp) {
        T result = null;
        @SuppressWarnings("unchecked")
        T[] beans = (T[]) parentDD.getValues(beanProp);
        if (beans != null) {
            for (int i = 0; i < beans.length; i++) {
                String name = (String) beans[i].getValue(nameProp);
                if (referenceName.equals(name)) {
                    result = beans[i];
                    break;
                }
            }
        }
        return result;
    }

    void addDescriptorListener(FileObject target) {
        // Note: We don't use target to locate the genuine descriptor file.  We
        // lookup the descriptor file through proper channels and add a listener
        // to that result (which 99% of the time ought to be the same as what was
        // passed in here.  But there's that pesky 1% case.... bleah.)
        addDescriptorListener("webservices.xml".equals(target.getNameExt()) ?
            getWebServicesRootDD() : getStandardRootDD());
    }

    protected void addDescriptorListener(org.netbeans.modules.j2ee.dd.api.common.RootInterface rootDD) {
        if(rootDD != null) {
            descriptorListener.addListener(rootDD);
        }
    }

    protected void handleEventRelatedIOException(IOException ex) {
        // This is a legitimate exception that could occur, such as a problem
        // writing the changed descriptor to disk.
        // !PW FIXME notify user
        // RR = could do handleEventRelatedException(ex) instead
        Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex);
    }

    protected void handleEventRelatedException(Exception ex) {
        // This would probably be a runtime exception due to a bug, but we
        // must trap it here so it doesn't cause trouble upstream.
        // We handle it the same as above for now.
        // !PW FIXME should we notify here, or just log?
        Logger.getLogger("glassfish-eecommon").log(Level.INFO, ex.getLocalizedMessage(), ex);
    }

    // ------------------------------------------------------------------------
    // !PW FIXME workaround for linking sun descriptor file DataObjects w/ the
    // correct Deployment Configuration object.  Key is primary File for configuration.
    // ------------------------------------------------------------------------
    private static final Object configurationMonitor = new Object();
    private static final WeakHashMap<File, WeakReference<GlassfishConfiguration>> configurationMap =
            new WeakHashMap<File, WeakReference<GlassfishConfiguration>>();

    public static void addConfiguration(File key, GlassfishConfiguration config) {
        synchronized(configurationMonitor) {
            configurationMap.put(key, new WeakReference<GlassfishConfiguration>(config));
        }
    }

    public static void removeConfiguration(File key) {
        synchronized(configurationMonitor) {
            configurationMap.remove(key);
        }
    }

    public static GlassfishConfiguration getConfiguration(File key) {
        GlassfishConfiguration config = null;
        WeakReference<GlassfishConfiguration> ref = null;
        synchronized(configurationMonitor) {
            ref = configurationMap.get(key);
        }
        if (ref != null) {
            config = ref.get();
        }
        return config;
    }

}
