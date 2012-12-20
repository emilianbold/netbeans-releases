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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.prefs.Preferences;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cordova.platforms.BuildPerformer;
import org.netbeans.modules.cordova.platforms.Device;
import org.netbeans.modules.cordova.platforms.MobileDebugTransport;
import org.netbeans.modules.cordova.project.ClientProjectConfigurationImpl;
import org.netbeans.modules.cordova.project.ClientProjectUtilities;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.modules.web.common.api.WebServer;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.netbeans.modules.web.webkit.debugging.spi.netbeansdebugger.NetBeansJavaScriptDebuggerFactory;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service=BuildPerformer.class)
public class CordovaPerformer implements BuildPerformer {

    public static EditableProperties getBuildProperties(Project project) {
        return createProperties(project, "build.properties", "nbproject/build.properties");//NOI18N
    }

    public static void storeBuildProperties(Project proj, EditableProperties props) {
        try {
            FileObject p = FileUtil.createData(proj.getProjectDirectory(), "nbproject/build.properties");
            OutputStream outputStream = p.getOutputStream();
            try {
                props.store(outputStream);
            } finally {
                outputStream.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    private NetBeansJavaScriptDebuggerFactory javascriptDebuggerFactory;
    private Session debuggerSession;
    private WebKitDebugging webKitDebugging;
    private MobileDebugTransport transport;

    
    @Override
    public void perform(String target, Project project) {
        FileObject buildFo = project.getProjectDirectory().getFileObject("nbproject/build.xml"); //NOI18N
        if (buildFo == null) {
            generateBuildScripts(project);
            buildFo = project.getProjectDirectory().getFileObject("nbproject/build.xml");//NOI18N
        }
        try { 
            ActionUtils.runTarget(buildFo, new String[]{target}, properties(project));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Properties properties(Project p) {
        ProjectConfigurationProvider provider = p.getLookup().lookup(ProjectConfigurationProvider.class);
        ClientProjectConfigurationImpl activeConfiguration = (ClientProjectConfigurationImpl) provider.getActiveConfiguration();
        Properties props = new Properties();
        final CordovaPlatform phoneGap = CordovaPlatform.getDefault();
        props.put("cordova.home", phoneGap.getSdkLocation());//NOI18N
        props.put("cordova.version", phoneGap.getVersion());//NOI18N
        props.put("site.root", org.netbeans.modules.cordova.project.ClientProjectUtilities.getSiteRoot(p).getPath());
        props.put("start.file", org.netbeans.modules.cordova.project.ClientProjectUtilities.getStartFile(p).getPath());

        String debug = ClientProjectUtilities.getProperty(p, "debug.enable");//NOI18N
        if (debug == null) {
            debug = "true";//NOI18N
        }
        props.put("debug.enable", debug);//NOI18N
        //workaround for some strange behavior of ant execution in netbeans
        props.put("env.DISPLAY", ":0.0");//NOI18N
        props.put("config", activeConfiguration.getId());
        activeConfiguration.getDevice().addProperties(props);
        return props;
    }

    private void generateBuildScripts(Project project) {
        try {
            createScript(project, "build.xml", "nbproject/build.xml");//NOI18N
            createProperties(project, "build.properties", "nbproject/build.properties");//NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void createScript(Project project, String source, String target) throws IOException {
        FileObject build= FileUtil.createData(project.getProjectDirectory(), target);
        InputStream resourceAsStream = CordovaPerformer.class.getResourceAsStream(source);
        OutputStream outputStream = build.getOutputStream();
        try {
            FileUtil.copy(resourceAsStream, outputStream);
        } finally {
            outputStream.close();
            resourceAsStream.close();
        }
    }

    private static EditableProperties createProperties(Project project, String buildproperties, String nbprojectbuildproperties) {
        EditableProperties props = new EditableProperties(true);
        try {
            FileObject fileObject = project.getProjectDirectory().getFileObject(nbprojectbuildproperties);
            if (fileObject !=null) {
                final InputStream inputStream = fileObject.getInputStream();
                try {
                    props.load(inputStream);
                    return props;
                } finally {
                    inputStream.close();
                }
            }
            
            InputStream is = CordovaPerformer.class.getResourceAsStream("build.properties");//NOI18N
            try {
                props.load(is);
            } finally {
                is.close();
            }
            props.put("project.name", ProjectUtils.getInformation(project).getDisplayName().replaceAll(" ", ""));//NOI18N
            props.put("android.project.activity", ProjectUtils.getInformation(project).getDisplayName().replaceAll(" ", ""));//NOI18N
            FileObject p = FileUtil.createData(project.getProjectDirectory(), nbprojectbuildproperties);
            OutputStream outputStream = p.getOutputStream();
            try {
                props.store(outputStream);
            } finally {
                outputStream.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return props;
    }

    @Override
    public String getUrl(Project p) {
        if (org.netbeans.modules.cordova.project.ClientProjectUtilities.isUsingEmbeddedServer(p)) {
            WebServer.getWebserver().start(p, org.netbeans.modules.cordova.project.ClientProjectUtilities.getSiteRoot(p), org.netbeans.modules.cordova.project.ClientProjectUtilities.getWebContextRoot(p));
        } else {
            WebServer.getWebserver().stop(p);
        }
        
        FileObject fileObject = org.netbeans.modules.cordova.project.ClientProjectUtilities.getStartFile(p);
        try {
            //TODO: hack to workaround #221791
            return ServerURLMapping.toServer(p, fileObject).toExternalForm().replace("localhost", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public boolean isPhoneGapBuild(Project p) {
        Preferences preferences = ProjectUtils.getPreferences(p, CordovaPlatform.class, true);
        return Boolean.parseBoolean(preferences.get("phonegap", "false"));
    }
    
    @Override
    public void startDebugging(Device device, Project p) {
        transport = device.getPlatform().getDebugTransport();
        transport.setBaseUrl(getUrl(p));
        transport.attach();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        webKitDebugging = Factory.createWebKitDebugging(transport);
        webKitDebugging.getDebugger().enable();
        javascriptDebuggerFactory = Lookup.getDefault().lookup(NetBeansJavaScriptDebuggerFactory.class);
        debuggerSession = javascriptDebuggerFactory.createDebuggingSession(webKitDebugging, Lookups.singleton(p));
        PageInspector.getDefault().inspectPage(Lookups.fixed(webKitDebugging, p));
    }

    @Override
    public void stopDebugging() {
            if (webKitDebugging == null || webKitDebugging == null) {
                return;
            }
            if (debuggerSession != null) {
                javascriptDebuggerFactory.stopDebuggingSession(debuggerSession);
            }
            debuggerSession = null;
            if (webKitDebugging.getDebugger().isEnabled()) {
                webKitDebugging.getDebugger().disable();
            }
            webKitDebugging.reset();
            transport.detach();
            transport = null;
            webKitDebugging = null;
            javascriptDebuggerFactory = null;
            PageInspector.getDefault().inspectPage(Lookup.EMPTY);
    }
    
}
