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

/*
 * Util.java
 *
 * Created on August 22, 2005, 8:44 PM
 *
 */
package org.netbeans.modules.mobility.end2end.util;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.mobility.e2e.mapping.ServerJavonTemplate;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.output.OutputLogger;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.mobility.javon.JavonMapping;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author suchys
 */
public final class Util {

    private Util() {
    }

    public static SourceGroup getPreselectedGroup(final SourceGroup[] groups, final String preselectedFolder) {
        if (preselectedFolder != null) {
            for (int i = 0; i < groups.length; i++) {
                if (groups[i].getName().equals(preselectedFolder)) {
                    return groups[i];
                }
            }
        }
        return groups.length >= 0 ? groups[0] : null;
    }

    public static Project openProject() {
        return openProject(null);
    }

    public static Project openProject(final String projectFolder) {
        Project project = null;
        if (projectFolder != null) {
            final File folder = new File(projectFolder);
            if (folder.exists() && folder.isDirectory()) {
                ProjectChooser.setProjectsFolder(folder);
            }
        }
        final JFileChooser chooser = ProjectChooser.projectChooser();
        final NotifyDescriptor.Message message1 = new NotifyDescriptor.Message(
                NbBundle.getMessage(Util.class, "MSG_notProjectDir"), // NOI18N
                NotifyDescriptor.WARNING_MESSAGE);
        final NotifyDescriptor.Message message2 = new NotifyDescriptor.Message(
                NbBundle.getMessage(Util.class, "ERR_NoWebProject"), // NOI18N
                NotifyDescriptor.WARNING_MESSAGE);

        while (true) {  // Cycle while users does some reasonable action e.g.
            // select project dir or cancel the chooser
            final int option = chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()); // Show the chooser

            if (option == JFileChooser.APPROVE_OPTION) {
                final File projectDir = FileUtil.normalizeFile(chooser.getSelectedFile());

                project = fileToProject(projectDir);
                if (project == null) {
                    DialogDisplayer.getDefault().notify(message1);
                } else {
                    if (!isWebProject(project)) {
                        DialogDisplayer.getDefault().notify(message2);
                    } else {
                        OpenProjects.getDefault().open(new Project[]{project}, true);
                        break; // and exit the loop
                    }
                }
            } else {
                return null; // OK user changed his mind and won't open anything
                // Don't remeber the last selected dir
            }
        }

        return project;
    }

    public static boolean isWebProject(final Project p) {
        if (p == null) {
            return false;
        }
        final WebModuleProvider provider = p.getLookup().lookup(WebModuleProvider.class);
        if (provider != null) {
            return true;
        }
        return false;
    }

    public static Project fileToProject(final File projectDir) {
        try {
            final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projectDir));
            if (fo != null) {
                return ProjectManager.getDefault().findProject(fo);
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static Project getServerProject(final Configuration configuration) {
        Project result = null;
        final OpenProjects openProject = OpenProjects.getDefault();
        final Project[] openedProjects = openProject.getOpenProjects();
        final String serverProjectName = configuration.getServerConfigutation().getProjectName();
        for (final Project p : openedProjects) {
            final ProjectInformation pi = p.getLookup().lookup(ProjectInformation.class);
            final String webProjectName = pi.getName();
            if (serverProjectName.equals(webProjectName)) {
                result = p;
                break;
            }
        }
        return result;
    }

    public static void addServletToWebProject(final Project project, JavonMapping mapping) {
        /* mark Servlet */
        try {
            boolean servlet = false;

            String servletName = mapping.getServerMapping().getClassName();
            String packageName = mapping.getServerMapping().getPackageName();
            String servletClassFQN = packageName.length() != 0 ? packageName + '.' + servletName : servletName;
            final WebModuleProvider provider = project.getLookup().lookup(WebModuleProvider.class);
            final WebModule wm = provider.findWebModule(project.getProjectDirectory());

            FileObject ddFO = wm.getDeploymentDescriptor();
            if (ddFO == null) { // no web.xml exists
                // TODO for EE6 register servlet through annotations
                ddFO = createWebXml(wm);
            }
            if (ddFO == null) {
                throw new IllegalStateException(NbBundle.getMessage(Util.class, "ERR_NoDeploymentDescriptor")); // NOI18N
            }
            
            final WebApp webApp = DDProvider.getDefault().getDDRootCopy(ddFO);
            final Servlet[] servlets = webApp.getServlet();
            for (int i = 0; i < servlets.length; i++) {
                if (servlets[i].getServletClass().equals(servletClassFQN)) {
                    servlet = true; //already contains
                }
            }

            if (!servlet) {
                servletName = findFreeName(servletName, webApp);
                OutputLogger.getInstance().log(MessageFormat.format(
                        NbBundle.getMessage(ServerJavonTemplate.class,
                        "MSG_AddServlet"), servletName, // NOI18N
                        FileUtil.toFile(project.getProjectDirectory())));
                final Servlet newServlet = (Servlet) webApp.createBean("Servlet"); // NOI18N
                newServlet.setServletName(servletName);
                newServlet.setServletClass(servletClassFQN);
                newServlet.setDescription(NbBundle.getMessage(Util.class, "TXT_servletElementDescription")); // NOI18N
                newServlet.setDisplayName("Javon service for  " + servletClassFQN); // NOI18N
                webApp.addServlet(newServlet);

                final ServletMapping25 newServletMapping = (ServletMapping25) webApp.createBean("ServletMapping"); // NOI18N
                newServletMapping.setServletName(servletName);
                newServletMapping.addUrlPattern("/servlet/" + servletClassFQN); // NOI18N
                webApp.addServletMapping(newServletMapping);

                webApp.write(ddFO);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    //#198444
    private static FileObject createWebXml(WebModule wm) throws IOException {
        FileObject ddFO = null;
        FileObject webInf = wm.getWebInf();
        if (webInf == null) {
            FileObject docBase = wm.getDocumentBase();
            if (docBase != null) {
                webInf = docBase.createFolder("WEB-INF"); //NOI18N
            }
        }
        if (webInf != null) {
            ddFO = DDHelper.createWebXml(wm.getJ2eeProfile(), webInf);
        }
        return ddFO;
    }

    private static String findFreeName(String servletName, WebApp webApp) {
        int nameIndex = 0;
        final Servlet[] servlets = webApp.getServlet();
        for (int i = 0; i < servlets.length; i++) {
            if (!servlets[i].getServletName().equals(servletName)) {
                break;
            }
            servletName = servletName + (++nameIndex);
        }
        final ServletMapping[] servletsMapping = webApp.getServletMapping();
        for (int i = 0; i < servletsMapping.length; i++) {
            if (!servletsMapping[i].getServletName().equals(servletName)) {
                break;
            }
            servletName = servletName + (++nameIndex);
        }
        return servletName;
    }

//    public static JavaClass resolveWebServiceClass( final FileObject projectFolder, final String fqn ) {
//        assert projectFolder != null;
//        assert fqn != null;
//        final FileObject resObj = projectFolder.getFileObject( "build/generated/wsimport/client/" + fqn.replace('.','/') + ".java" ); //NOI18N
//        if( resObj == null ) return null;
//        resObj.refresh( false );
//        final Resource res = JavaModel.getResource(resObj);
//        return (JavaClass)res.getClassifiers().get(0);
//    }
    
    public static String getServerURL(final Project p, final Configuration configuration) {
        String port = "8080"; //NOI18N
        final J2eeModuleProvider provider = p.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            final InstanceProperties ip = provider.getInstanceProperties();
            if (ip != null) {
                port = ip.getProperty(InstanceProperties.HTTP_PORT_NUMBER) != null ? ip.getProperty(InstanceProperties.HTTP_PORT_NUMBER) : "8080";//NOI18N
            }
        }
        return "http://localhost:" + port + "/" + configuration.getServerConfigutation().getProjectName() + "/servlet/" + //NOI18N
                configuration.getServerConfigutation().getClassDescriptor().getType();

    }

    public static boolean isSuitableProjectConfiguration(final Project project) {
        if (!(project instanceof J2MEProject)) {
            return false;
        }
        final String profile = evaluateProjectProperty((J2MEProject) project, DefaultPropertiesDescriptor.PLATFORM_PROFILE);

        return "MIDP-2.0".equals(profile); // NOI18N
    }

    /**
     * Gets currently used device screen size from J2ME project
     * @param project
     * @return
     */
    private static String evaluateProjectProperty(final J2MEProject project, final String property) {
        final AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
        final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final ProjectConfigurationsHelper confs = project.getConfigurationHelper();
        final String activeConfiguration = confs.getActiveConfiguration() != confs.getDefaultConfiguration() ? confs.getActiveConfiguration().getDisplayName() : null;

        return evaluateProperty(ep, property, activeConfiguration);
    }

    private static String evaluateProperty(final EditableProperties ep, final String propertyName, final String configuration) {
        if (configuration == null) {
            return ep.getProperty(propertyName);
        }
        final String value = ep.getProperty("configs." + configuration + "." + propertyName); // NOI18N
        return value != null ? value : evaluateProperty(ep, propertyName, null);
    }

    /**
     * 
     * @param project 
     * @return 
     */
    public static String getServerLocation(final Project project) {
        return "localhost"; // NOI18N
    }

    public static String getServerPort(final Project project) {
        String port = "8080"; // NOI18N
        final J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            final InstanceProperties ip = provider.getInstanceProperties();
            if (ip != null) {
                port = ip.getProperty(InstanceProperties.HTTP_PORT_NUMBER) != null ? ip.getProperty(InstanceProperties.HTTP_PORT_NUMBER) : "8080";//NOI18N
            }
        }
        return port;
    }

    /**
     * Appends DataBinding library to the libraries
     * 
     */
    public static boolean registerDataBindingLibrary(Project p) {
        ProjectClassPathExtender pcpe = p.getLookup().lookup(ProjectClassPathExtender.class);
        Library[] libraries = LibraryManager.getDefault().getLibraries();
        for (Library library : libraries) {
            if (library.getName().equals("DataBindingME")) { // NOI18N
                try {
                    pcpe.addLibrary(library);
                    return true;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                }
            }
        }
        return false;
    }
}
