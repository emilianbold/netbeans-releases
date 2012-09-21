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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cordova.android.AndroidPlatform;
import org.netbeans.modules.cordova.ios.Device;
import org.netbeans.modules.cordova.ios.IOSPlatform;
import org.netbeans.modules.cordova.project.ClientProjectConfigurationImpl;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.*;

/**
 *
 * @author Jan Becicka
 */
public class CordovaPerformer extends AbstractAction implements ProjectActionPerformer, ActionListener, ContextAwareAction {
    
    public static final String BUILD_IOS = "build-ios";
    public static final String BUILD_ANDROID = "build-android";
    public static final String RUN_IOS = "sim-ios";
    public static final String RUN_ANDROID = "sim-android";
    public static final String CLEAN_IOS = "clean-ios";
    public static final String CLEAN_ANDROID = "clean-android";
    
    private String target;
    private Project p;
 
    
//    @ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.buildIOS", category = "Project")
//    @ActionRegistration(displayName = "#build-ios", lazy=false)
//    @ActionReference(position = 1000, path = "Projects/org.netbeans.modules.web.clientproject/Actions")
    public static CordovaPerformer buildIOS() {
        return new CordovaPerformer(BUILD_IOS);
    }

//    @ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.buildAndroid", category = "Project")
//    @ActionRegistration(displayName = "#build-android", lazy=false)
//    @ActionReference(position = 1100, path = "Projects/org.netbeans.modules.web.clientproject/Actions")
    public static CordovaPerformer buildAndroid() {
        return new CordovaPerformer(BUILD_ANDROID);
    }

//    @ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.runIOS", category = "Project")
//    @ActionRegistration(displayName = "#sim-ios", lazy=false)
//    @ActionReference(position = 1200, path = "Projects/org.netbeans.modules.web.clientproject/Actions")
    public static CordovaPerformer runIOS() {
        return new CordovaPerformer(RUN_IOS);
    }

//    @ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.runAndroid", category = "Project")
//    @ActionRegistration(displayName = "#sim-android", lazy=false)
//    @ActionReference(position = 1300, path = "Projects/org.netbeans.modules.web.clientproject/Actions")
    public static CordovaPerformer runAndroid() {
        return new CordovaPerformer(RUN_ANDROID);
    }

//    @ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.cleanIOS", category = "Project")
//    @ActionRegistration(displayName = "#clean-ios", lazy=false)
//    @ActionReference(position = 1400, path = "Projects/org.netbeans.modules.web.clientproject/Actions")
    public static CordovaPerformer cleanIOS() {
        return new CordovaPerformer(CLEAN_IOS);
    }

//    @ActionID(id = "org.netbeans.modules.web.clientproject.ui.action.cleanAndroid", category = "Project")
//    @ActionRegistration(displayName = "#clean-android", lazy=false)
//    @ActionReference(position = 1500, path = "Projects/org.netbeans.modules.web.clientproject/Actions")
    public static CordovaPerformer cleanAndroid() {
        return new CordovaPerformer(CLEAN_ANDROID);
    }

    public CordovaPerformer(String target) {
        super(NbBundle.getMessage(CordovaPerformer.class, target));
        this.target = target;
    }

    private CordovaPerformer(Project p, String target) {
        this(target);
        this.p = p;
    }

    @Override
    public boolean enable(Project project) {
        return true;
    }

    @Override
    public void perform(Project project) {
        FileObject buildFo = project.getProjectDirectory().getFileObject("nbproject/build.xml");
        if (buildFo == null) {
            generateBuildScripts(project);
            //updateProjectProperties(project);
            buildFo = project.getProjectDirectory().getFileObject("nbproject/build.xml");
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
        props.put("cordova.home", CordovaPlatform.getDefault().getSdkLocation());
        props.put("cordova.version", CordovaPlatform.getDefault().getVersion());
        String debug = activeConfiguration.getProperty("debug.enable");
        if (debug == null) {
            debug = "true";
        }
        props.put("debug.enable", debug);
        //workaround for some strange behavior of ant execution in netbeans
        props.put("env.DISPLAY", ":0.0");
        if (activeConfiguration.getType().equals(AndroidPlatform.TYPE)) {
            props.put("android.build.target", AndroidPlatform.getDefault().getPrefferedTarget());
            props.put("android.sdk.home", AndroidPlatform.getDefault().getSdkLocation());
            props.put("android.target.device.arg", "device".equals(activeConfiguration.getProperty("device"))?"-d":"-e");
        } else if (activeConfiguration.getType().equals(IOSPlatform.TYPE)) {
            props.put("ios.sim.exec", InstalledFileLocator.getDefault().locate("bin/ios-sim", "org.netbeans.modules.cordova", false).getPath());
            String virtualDevice = activeConfiguration.getProperty("vd");
            if (virtualDevice!=null) {
                Device dev = Device.valueOf(virtualDevice);
                props.put("ios.device.args", dev.getArgs());
            } else {
                //default
                props.put("ios.device.args", Device.IPHONE.getArgs());
            }
        }
        return props;
    }

    private void generateBuildScripts(Project project) {
        try {
            createScript(project, "build.xml", "nbproject/build.xml");
            createProperties(project, "build.properties", "nbproject/build.properties");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

//    private void updateProjectProperties(Project project) {
//        AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
//        helper.getProperties("nbproject/project.properties");
//        EditableProperties ep = new EditableProperties(true);
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    private void createScript(Project project, String source, String target) throws IOException {
        //URL buildUrl = CordovaPerformer.class.getResource("build.xml");
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

    private void createProperties(Project project, String buildproperties, String nbprojectbuildproperties) {
        Properties props = new Properties();
        try {
            InputStream is = CordovaPerformer.class.getResourceAsStream("build.properties");
            try {
                props.load(is);
            } finally {
                is.close();
            }
            props.put("project.name", ProjectUtils.getInformation(project).getDisplayName().replaceAll(" ", ""));
            props.put("android.project.activity", ProjectUtils.getInformation(project).getDisplayName().replaceAll(" ", ""));
            props.put("android.project.package","org.netbeans");
            props.put("android.project.package.folder","org/netbeans");
            FileObject p= FileUtil.createData(project.getProjectDirectory(), nbprojectbuildproperties);
            OutputStream outputStream = p.getOutputStream();
            try {
                props.store(outputStream, null);
            } finally {
                outputStream.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        perform(p);
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Project p = actionContext.lookup(Project.class);
        if (p == null) {
            return this;
        }
        if (p instanceof Project) {
            return new CordovaPerformer(p, target);
        }
        return this;
    }
}
