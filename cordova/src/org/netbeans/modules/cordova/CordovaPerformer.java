/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cordova.platforms.BuildPerformer;
import org.netbeans.modules.cordova.platforms.Device;
import org.netbeans.modules.cordova.platforms.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.PlatformManager;
import org.netbeans.modules.cordova.project.ClientProjectUtilities;
import org.netbeans.modules.cordova.project.MobileConfigurationImpl;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebServer;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.modules.cordova.PropertyNames.*;
import org.netbeans.modules.cordova.platforms.CordovaMapping;
import org.netbeans.modules.cordova.platforms.MobilePlatform;
import org.netbeans.modules.cordova.updatetask.SourceConfig;
import org.netbeans.modules.web.common.spi.ServerURLMappingImplementation;
import org.netbeans.modules.web.webkit.debugging.api.WebKitUIManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service = BuildPerformer.class)
public class CordovaPerformer implements BuildPerformer {
    public static final String NAME_BUILD_XML = "build.xml";
    public static final String NAME_CONFIG_XML = "config.xml";
    public static final String PATH_BUILD_XML = "nbproject/" + NAME_BUILD_XML;
    public static final String PATH_EXTRA_ANT_JAR = "ant/extra/org-netbeans-modules-cordova-projectupdate.jar";
    public static final String DEFAULT_ID_PREFIX = "com.coolappz";
    public static final String DEFAULT_EMAIL = "info@com.coolappz";
    public static final String DEFAULT_WWW = "http://www.coolappz.com";
    public static final String DEFAULT_VERSION = "1.0.0";
    public static final String DEFAULT_DESCRIPTION = "PhoneGap Application";
    public static final String PROP_BUILD_SCRIPT_VERSION = "cordova_build_script_version";
    public static final String PROP_PROVISIONING_PROFILE = "ios.provisioning.profile";
    public static final String PROP_CERTIFICATE_NAME = "ios.certificate.name";

    private Session debuggerSession;
    private Lookup consoleLogger;
    private Lookup networkMonitor;
    private WebKitDebugging webKitDebugging;
    private MobileDebugTransport transport;
    private final int BUILD_SCRIPT_VERSION = 5;
    
    public static CordovaPerformer getDefault() {
        return Lookup.getDefault().lookup(CordovaPerformer.class);
    }
    
    public void createPlatforms(Project project) {
        if (PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).isReady()) {
            perform("create-android", project);
        }
        if (PlatformManager.getPlatform(PlatformManager.IOS_TYPE).isReady()) {
            perform("create-ios", project);
        }
    }
    
    @Override
    public void perform(final String target, final Project project) {
        if (!CordovaPlatform.getDefault().isReady()) {
            throw new IllegalStateException();
        }
        Runnable run = new Runnable() {
            @Override
            public void run() {
                generateBuildScripts(project);
                FileObject buildFo = project.getProjectDirectory().getFileObject(PATH_BUILD_XML);//NOI18N
                try {
                    ExecutorTask runTarget = ActionUtils.runTarget(buildFo, new String[]{target}, properties(project));
                    if (target.equals(BuildPerformer.RUN_IOS)) {
                        if (runTarget.result() == 0) {
                            ProjectConfigurationProvider provider = project.getLookup().lookup(ProjectConfigurationProvider.class);
                            if (provider != null) {
                                ProjectConfiguration activeConfiguration = provider.getActiveConfiguration();
                                if (activeConfiguration instanceof MobileConfigurationImpl) {
                                    Device device = ((MobileConfigurationImpl) activeConfiguration).getDevice();
                                    CordovaMapping map = (CordovaMapping) Lookup.getDefault().lookup(ServerURLMappingImplementation.class);
                                    map.setProject(project);
                                    if (device.isEmulator()) {
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                    } else {
                                        DialogDescriptor dd = new DialogDescriptor("Install application using iTunes and tap on it", "Install and Run");
                                        if (DialogDisplayer.getDefault().notify(dd) != DialogDescriptor.OK_OPTION) {
                                            return;
                                        }
                                    }
                                    startDebugging(device, project, null, false);
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(run);
        } else {
            run.run();
        }
    }

    private Properties properties(Project p) {
        Properties props = new Properties();
        final CordovaPlatform phoneGap = CordovaPlatform.getDefault();
        props.put(PROP_CORDOVA_HOME, phoneGap.getSdkLocation());//NOI18N
        props.put(PROP_CORDOVA_VERSION, phoneGap.getVersion().toString());//NOI18N
        final FileObject siteRoot = ClientProjectUtilities.getSiteRoot(p);
        final String siteRootRelative = FileUtil.getRelativePath(p.getProjectDirectory(), siteRoot);
        props.put(PROP_SITE_ROOT, siteRootRelative);
        final String startFileRelative = FileUtil.getRelativePath(siteRoot, ClientProjectUtilities.getStartFile(p));
        props.put(PROP_START_FILE, startFileRelative);
        final File antTaskJar = InstalledFileLocator.getDefault().locate(
           PATH_EXTRA_ANT_JAR, 
           "org.netbeans.modules.cordova" , true);
        props.put(PROP_UPDATE_TASK_JAR, antTaskJar.getAbsolutePath());
        final String id = getConfig(p).getId();
        String activity = id.substring(id.lastIndexOf(".")+1, id.length());
        props.put(PROP_ANDROID_PROJECT_ACTIVITY, activity);//NOI18N
        
        MobilePlatform iosPlatform = PlatformManager.getPlatform(PlatformManager.IOS_TYPE);
        props.put(PROP_PROVISIONING_PROFILE, iosPlatform.getProvisioningProfilePath());
        props.put(PROP_CERTIFICATE_NAME, iosPlatform.getCodeSignIdentity());

        String debug = ClientProjectUtilities.getProperty(p, PROP_DEBUG_ENABLE);//NOI18N
        if (debug == null) {
            debug = Boolean.TRUE.toString();
        }
        props.put(PROP_DEBUG_ENABLE, debug);//NOI18N
        //workaround for some strange behavior of ant execution in netbeans
        props.put(PROP_ENV_DISPLAY, ":0.0");//NOI18N
        props.put(PROP_ANDROID_SDK_HOME, PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).getSdkLocation());

        ProjectConfigurationProvider provider = p.getLookup().lookup(ProjectConfigurationProvider.class);
        if (provider != null) {
            ProjectConfiguration activeConfiguration = provider.getActiveConfiguration();
            if (activeConfiguration instanceof MobileConfigurationImpl) {
                props.put(PROP_CONFIG, ((MobileConfigurationImpl) activeConfiguration).getId());
                ((MobileConfigurationImpl) activeConfiguration).getDevice().addProperties(props);
            }
        }
        
        return props;
    }

    private void generateBuildScripts(Project project) {
        try {
            Preferences preferences = ProjectUtils.getPreferences(project, CordovaPlatform.class, true);
            int version = preferences.getInt(PROP_BUILD_SCRIPT_VERSION, 0);

            boolean fresh;
            if (version < BUILD_SCRIPT_VERSION) {
                fresh = createScript(project, NAME_BUILD_XML, PATH_BUILD_XML, true);//NOI18N
            } else {
                fresh = createScript(project, NAME_BUILD_XML, PATH_BUILD_XML, false);//NOI18N
            }
            if (fresh) {
                preferences.putInt(PROP_BUILD_SCRIPT_VERSION, BUILD_SCRIPT_VERSION);
            }

            getConfig(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static String getConfigPath(Project project) {
        return ClientProjectUtilities.getSiteRoot(project).getNameExt() + "/" + NAME_CONFIG_XML;
    }
    
    public static SourceConfig getConfig(Project project)  {
        try {
            String configPath = getConfigPath(project);
            boolean fresh = createScript(project, NAME_CONFIG_XML, configPath, false);//NOI18N

            FileObject config = project.getProjectDirectory().getFileObject(configPath);
            SourceConfig conf = new SourceConfig(FileUtil.toFile(config));
            if (fresh) {
                final String appName = ProjectUtils.getInformation(project).getDisplayName().replaceAll(" ", "");
                conf.setId(DEFAULT_ID_PREFIX + "." + appName);
                conf.setName(appName);
                conf.setDescription(DEFAULT_DESCRIPTION);
                conf.setAuthor(System.getProperty("user.name"));
                conf.setAuthorEmail(DEFAULT_EMAIL);
                conf.setAuthorHref(DEFAULT_WWW);
                conf.setVersion(DEFAULT_VERSION);
                conf.save();
            }
            return conf;
        } catch (IOException iOException) {
            throw new IllegalStateException(iOException);
        }
    }

    private static boolean createScript(Project project, String source, String target, boolean overwrite) throws IOException {
        FileObject build = null;
        if (!overwrite) {
            build = project.getProjectDirectory().getFileObject(target);
        }
        if (build == null) {
            build = FileUtil.createData(project.getProjectDirectory(), target);
            InputStream resourceAsStream = CordovaPerformer.class.getResourceAsStream(source);
            OutputStream outputStream = build.getOutputStream();
            try {
                FileUtil.copy(resourceAsStream, outputStream);
            } finally {
                outputStream.close();
                resourceAsStream.close();
            }
            return true;
        }
        return false;
    }

    @Override
    public String getUrl(Project p, Lookup context) {
        if (context == null) {
            return null;
        }
        URL url = context.lookup(URL.class);
        if (url!=null) {
            //TODO: hack to workaround #221791
            return url.toExternalForm().replace("localhost", WebUtils.getLocalhostInetAddress().getHostAddress());
        }
        if (org.netbeans.modules.cordova.project.ClientProjectUtilities.isUsingEmbeddedServer(p)) {
            WebServer.getWebserver().start(p, ClientProjectUtilities.getSiteRoot(p), ClientProjectUtilities.getWebContextRoot(p));
        } else {
            WebServer.getWebserver().stop(p);
        }

        DataObject dObject = context.lookup(DataObject.class);
        FileObject fileObject = dObject==null?ClientProjectUtilities.getStartFile(p):dObject.getPrimaryFile();
        //TODO: hack to workaround #221791
        return ServerURLMapping.toServer(p, fileObject).toExternalForm().replace("localhost", WebUtils.getLocalhostInetAddress().getHostAddress());
    }
    
    private String getUrl(Project p) {
        return getUrl(p, Lookup.EMPTY);
    }


    @Override
    public boolean isPhoneGapBuild(Project p) {
        Preferences preferences = ProjectUtils.getPreferences(p, CordovaPlatform.class, true);
        return Boolean.parseBoolean(preferences.get(PROP_PHONEGAP, Boolean.FALSE.toString()));
    }

    @Override
    public void startDebugging(Device device, Project p, Lookup context, boolean navigateToUrl) {
        if (transport != null || webKitDebugging != null) {
            //stop old session
            stopDebugging();
        }
        transport = device.getDebugTransport();
        final String url = getUrl(p, context);
        transport.setBaseUrl(url);
        if (url==null) {
            String id = getConfig(p).getId();
            transport.setBundleIdentifier(id);
        }
        transport.attach();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        webKitDebugging = Factory.createWebKitDebugging(transport);
        if (navigateToUrl) {
            webKitDebugging.getPage().navigate(url);
        }
        webKitDebugging.getDebugger().enable();
        Lookup projectContext = Lookups.singleton(p);
        debuggerSession = WebKitUIManager.getDefault().createDebuggingSession(webKitDebugging, projectContext);
        consoleLogger = WebKitUIManager.getDefault().createBrowserConsoleLogger(webKitDebugging, projectContext);
        networkMonitor = WebKitUIManager.getDefault().createNetworkMonitor(webKitDebugging, projectContext);
        PageInspector.getDefault().inspectPage(Lookups.fixed(webKitDebugging, p));
    }

    @Override
    public void stopDebugging() {
        try {
            if (webKitDebugging == null || webKitDebugging == null) {
                return;
            }
            if (debuggerSession != null) {
                WebKitUIManager.getDefault().stopDebuggingSession(debuggerSession);
            }
            debuggerSession = null;
            if (consoleLogger != null) {
                WebKitUIManager.getDefault().stopBrowserConsoleLogger(consoleLogger);
            }
            consoleLogger = null;
            if (networkMonitor != null) {
                WebKitUIManager.getDefault().stopNetworkMonitor(networkMonitor);
            }
            networkMonitor = null;
            if (webKitDebugging.getDebugger().isEnabled()) {
                webKitDebugging.getDebugger().disable();
            }
            webKitDebugging.reset();
            transport.detach();
            transport = null;
            webKitDebugging = null;
            PageInspector.getDefault().inspectPage(Lookup.EMPTY);
        } finally {
            CordovaMapping map = Lookup.getDefault().lookup(CordovaMapping.class);
            map.setBaseUrl(null);
            map.setProject(null);
        }
    }

}
