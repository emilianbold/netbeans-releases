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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cordova.platforms.spi.BuildPerformer;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.project.MobileConfigurationImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.modules.cordova.PropertyNames.*;
import org.netbeans.modules.cordova.platforms.api.ClientProjectUtilities;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.wizard.CordovaProjectExtender;
import org.netbeans.modules.cordova.platforms.spi.SDK;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.cordova.project.CordovaCustomizerPanel;
import org.netbeans.modules.cordova.project.PhoneGapBrowserFactory;
import org.netbeans.modules.cordova.updatetask.SourceConfig;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service = BuildPerformer.class)
public class CordovaPerformer implements BuildPerformer {
    public static final String NAME_BUILD_XML = "build.xml"; // NOI18N
    public static final String NAME_CONFIG_XML = "config.xml"; // NOI18N
    public static final String PATH_BUILD_XML = "nbproject/" + NAME_BUILD_XML; // NOI18N
    public static final String PATH_EXTRA_ANT_JAR = "ant/extra/org-netbeans-modules-cordova-projectupdate.jar"; // NOI18N
    public static final String DEFAULT_ID_PREFIX = "com.coolappz"; // NOI18N
    public static final String DEFAULT_EMAIL = "info@com.coolappz"; // NOI18N
    public static final String DEFAULT_WWW = "http://www.coolappz.com";
    public static final String DEFAULT_VERSION = "1.0.0"; // NOI18N
    public static final String DEFAULT_DESCRIPTION = Bundle.DSC_PhoneGap();
    public static final String PROP_BUILD_SCRIPT_VERSION = "cordova_build_script_version"; // NOI18N
    public static final String PROP_PROVISIONING_PROFILE = "ios.provisioning.profile"; // NOI18N
    public static final String PROP_CERTIFICATE_NAME = "ios.certificate.name"; // NOI18N
    
    private final RequestProcessor RP = new RequestProcessor(CordovaPerformer.class.getName(), 10);

    private final int BUILD_SCRIPT_VERSION = 9;
    
    public static CordovaPerformer getDefault() {
        return Lookup.getDefault().lookup(CordovaPerformer.class);
    }
    
    
    public Task createPlatforms(Project project) {
        Task task1 = null;
        Task task2 = null;
        if (PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).isReady()) {
            task1 = perform("create-android", project); // NOI18N
        }
        if (PlatformManager.getPlatform(PlatformManager.IOS_TYPE).isReady()) {
            task2 = perform("create-ios", project); // NOI18N
        }
        
        Task t = new CompoundTask(task1, task2);
        return t;
    }
    
    @NbBundle.Messages({
        "LBL_InstallThroughItunes=Install application using iTunes and tap on it",
        "CTL_InstallAndRun=Install and Run",
        "DSC_PhoneGap=PhoneGap Application"    
    })
    @Override
    public ExecutorTask perform(final String target, final Project project) {
        if (((target.startsWith("build") || target.startsWith("sim")) // NOI18N
                && ClientProjectUtilities.getSiteRoot(project).getFileObject("res") == null)) { // NOI18N
            String message = NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaCustomizerPanel.createConfigsLabel.text") + "\n"
                    + NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaPanel.createConfigs.text") + "?";
            NotifyDescriptor desc = new NotifyDescriptor(
                    message,
                    NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaPanel.createConfigs.text"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null,
                    NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(desc);
            if (desc.getValue() != NotifyDescriptor.OK_OPTION) {
                return null;
            }
        }

        if (!CordovaPlatform.getDefault().isReady()) {
            throw new IllegalStateException();
        }
        final ExecutorTask runTarget[] = new ExecutorTask[1];
        Runnable run = new Runnable() {
            @Override
            public void run() {
                generateBuildScripts(project);
                FileObject buildFo = project.getProjectDirectory().getFileObject(PATH_BUILD_XML);//NOI18N
                try {
                    runTarget[0] = ActionUtils.runTarget(buildFo, new String[]{target}, properties(project));
                    if (target.equals(BuildPerformer.RUN_IOS)) {
                        if (runTarget[0].result() == 0) {
                            ProjectBrowserProvider provider = project.getLookup().lookup(ProjectBrowserProvider.class);
                            if (provider != null) {
                                WebBrowser activeConfiguration = provider.getActiveBrowser();
                                MobileConfigurationImpl mobileConfig = MobileConfigurationImpl.create(project, activeConfiguration.getId());
                                Device device = mobileConfig.getDevice();
                                final FileObject startFile = ClientProjectUtilities.getStartFile(project);
                                
                                //#231037
                                URL u = ServerURLMapping.toServer(project, startFile);
                                activeConfiguration.toBrowserURL(project, startFile, u);
                                
                                BrowserURLMapperImplementation.BrowserURLMapper mapper = ((PhoneGapBrowserFactory) activeConfiguration.getHtmlBrowserFactory()).getMapper();
                                if (!device.isEmulator()) {
                                    DialogDescriptor dd = new DialogDescriptor(Bundle.LBL_InstallThroughItunes(), Bundle.CTL_InstallAndRun());
                                    if (DialogDisplayer.getDefault().notify(dd) != DialogDescriptor.OK_OPTION) {
                                        return;
                                    }
                                }
                                WebKitDebuggingSupport.getDefault().startDebugging(device, project, Lookups.fixed(mapper, BrowserFamilyId.PHONEGAP, getConfig(project).getId()), false);
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
            RP.post(run);
        } else {
            run.run();
        }
        return runTarget[0];
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
           "org.netbeans.modules.cordova" , true); // NOI18N
        props.put(PROP_UPDATE_TASK_JAR, antTaskJar.getAbsolutePath());
        final String id = getConfig(p).getId();
        String activity = id.substring(id.lastIndexOf(".")+1, id.length()); // NOI18N
        props.put(PROP_ANDROID_PROJECT_ACTIVITY, activity);//NOI18N
        
        MobilePlatform iosPlatform = PlatformManager.getPlatform(PlatformManager.IOS_TYPE);

        final String provisioningProfilePath = iosPlatform.getProvisioningProfilePath();
        if (provisioningProfilePath != null) {
            props.put(PROP_PROVISIONING_PROFILE, provisioningProfilePath);
        }
        final String codeSignIdentity = iosPlatform.getCodeSignIdentity();

        if (codeSignIdentity != null) {
            props.put(PROP_CERTIFICATE_NAME, codeSignIdentity);
        }

        //workaround for some strange behavior of ant execution in netbeans
        props.put(PROP_ENV_DISPLAY, ":0.0");//NOI18N
        props.put(PROP_ANDROID_SDK_HOME, PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).getSdkLocation());

        ProjectBrowserProvider provider = p.getLookup().lookup(ProjectBrowserProvider.class);
        if (provider != null && provider.getActiveBrowser().getBrowserFamily()==BrowserFamilyId.PHONEGAP) {
            WebBrowser activeConfiguration = provider.getActiveBrowser();
            MobileConfigurationImpl mobileConfig = MobileConfigurationImpl.create(p, activeConfiguration.getId());

            props.put(PROP_CONFIG, mobileConfig.getId());
            mobileConfig.getDevice().addProperties(props);
            if (mobileConfig.getId().equals("ios")) { // NOI18N
                boolean sdkVerified = false;
                try {
                    for (SDK sdk:iosPlatform.getSDKs()) {
                        if (sdk.getIdentifier().equals(mobileConfig.getProperty("ios.build.sdk"))) {
                            sdkVerified = true;
                            break;
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (!sdkVerified) {
                    mobileConfig.putProperty("ios.build.sdk", iosPlatform.getPrefferedTarget().getIdentifier()); // NOI18N
                    mobileConfig.save();
                }
            }
        }

        return props;
    }

    private void generateBuildScripts(Project project) {
        try {
            CordovaProjectExtender.createMobileConfigs(project.getProjectDirectory());
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
        return ClientProjectUtilities.getSiteRoot(project).getNameExt() + "/" + NAME_CONFIG_XML; // NOI18N
    }
    
    public static SourceConfig getConfig(Project project)  {
        try {
            String configPath = getConfigPath(project);
            boolean fresh = createScript(project, NAME_CONFIG_XML, configPath, false);//NOI18N

            FileObject config = project.getProjectDirectory().getFileObject(configPath);
            SourceConfig conf = new SourceConfig(FileUtil.toFile(config));
            if (fresh) {
                final String appName = ProjectUtils.getInformation(project).getDisplayName().replaceAll(" ", "_").replaceAll("-", "_").replaceAll("\\.","_"); // NOI18N
                conf.setId(DEFAULT_ID_PREFIX + "." + appName); // NOI18N
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
    
    private class CompoundTask extends Task {

        private final Task task1;
        private final Task task2;

        public CompoundTask(Task task1, Task task2) {
            this.task1 = task1;
            this.task2 = task2;
        }
        @Override
        public void waitFinished() {
            if (task1!=null) {
                task1.waitFinished();
            }
            if (task2!=null) {
                task2.waitFinished();
            }
        }
    }

}
