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

package org.netbeans.modules.maven.jaxws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class MavenModelUtils {
    
    public static Plugin getJaxWSPlugin(ModelHandle handle) {
        @SuppressWarnings("unchecked")
        List<Plugin> plugins = handle.getPOMModel().getBuild().getPlugins();
        for (Plugin plg : plugins) {
            if ("org.codehaus.mojo:jaxws-maven-plugin".equalsIgnoreCase(plg.getKey())) { //NOI18N
                //TODO CHECK THE ACTUAL PARAMETER VALUES..
                return plg;
            }
        }
        return null;
    }
        
    public static Plugin addJaxWSPlugin(ModelHandle handle) {
        @SuppressWarnings("unchecked")
        List<Plugin> plugins = handle.getPOMModel().getBuild().getPlugins();
        for (Plugin plg : plugins) {
            if ("org.codehaus.mojo:jaxws-maven-plugin".equalsIgnoreCase(plg.getKey())) { //NOI18N
                //TODO CHECK THE ACTUAL PARAMETER VALUES..
                return plg;
            }
        }

        Plugin plugin = new Plugin();
        plugin.setGroupId("org.codehaus.mojo"); //NOI18N
        plugin.setArtifactId("jaxws-maven-plugin"); //NOI18N
        plugin.setVersion("1.10"); //NOI18N
        Plugin old = null;

        Build bld = handle.getPOMModel().getBuild();
        if (bld != null) {
            old = (Plugin) bld.getPluginsAsMap().get(plugin.getKey());
        } else {
            handle.getPOMModel().setBuild(new Build());
        }
        if (old != null) {
            plugin = old;
        } else {
            handle.getPOMModel().getBuild().addPlugin(plugin);
        }

        @SuppressWarnings("unchecked")
        List<PluginExecution> execs = plugin.getExecutions();
        if (execs == null || execs.size() == 0) {
            PluginExecution ex = new PluginExecution();
            ex.setId("wsimport-generate"); //NOI18N
            ex.setPhase("generate-sources"); //NOI18N
            List<String> goals = new ArrayList<String>();
            goals.add("wsimport"); //NOI18N
            ex.setGoals(goals);
            plugin.addExecution(ex);
        } else {
            //TODO iterate execution goals and check for wsimport..
        }

        Xpp3Dom conf = (Xpp3Dom) plugin.getConfiguration();
        if (conf == null) {
            conf = new Xpp3Dom("configuration"); //NOI18N
            plugin.setConfiguration(conf);
        }

        Xpp3Dom destDir = conf.getChild("sourceDestDir"); //NOI18N
        if (destDir  == null) {
            destDir  = new Xpp3Dom("sourceDestDir"); //NOI18N
            destDir .setValue("${project.build.directory}/generated-sources/jaxws-wsimport"); //NOI18N
            conf.addChild(destDir );
        }

        Xpp3Dom xnocompile = conf.getChild("xnocompile"); //NOI18N
        if (xnocompile== null) {
            xnocompile = new Xpp3Dom("xnocompile"); //NOI18N
            xnocompile.setValue("true");
            conf.addChild(xnocompile);
        }
        Xpp3Dom verbose = conf.getChild("verbose"); //NOI18N
        if (verbose == null) {
            verbose = new Xpp3Dom("verbose"); //NOI18N
            verbose.setValue("true"); //NOI18N
            conf.addChild(verbose);
        }

        handle.markAsModified(handle.getPOMModel());
        return plugin; 
    }
    
    public static void addWsdlFile(ModelHandle handle, String wsdlPath) {
        
        @SuppressWarnings("unchecked")
        List<Plugin> plugins = handle.getPOMModel().getBuild().getPlugins();
        for (Plugin plugin : plugins) {
            if ("org.codehaus.mojo:jaxws-maven-plugin".equalsIgnoreCase(plugin.getKey())) { //NOI18N
                //TODO CHECK THE ACTUAL PARAMETER VALUES..
                Xpp3Dom conf = (Xpp3Dom) plugin.getConfiguration();

                if (conf == null) {
                    conf = new Xpp3Dom("configuration"); //NOI18N
                    plugin.setConfiguration(conf);
                    
                    Xpp3Dom destDir = new Xpp3Dom("sourceDestDir"); //NOI18N
                    destDir.setValue("${project.build.directory}/generated-sources/jaxws-wsimport"); //NOI18N
                    conf.addChild(destDir);
                    
                    Xpp3Dom xnocompile = new Xpp3Dom("xnocompile"); //NOI18N
                    xnocompile.setValue("true"); //NOI18N
                    conf.addChild(xnocompile);
                    
                    Xpp3Dom verbose = new Xpp3Dom("verbose"); //NOI18N
                    verbose.setValue("true"); //NOI18N
                    conf.addChild(verbose);
                }
                
                Xpp3Dom wsdlFiles = conf.getChild("wsdlFiles"); //NOI18N
                if (wsdlFiles == null) {
                    wsdlFiles = new Xpp3Dom("wsdlFiles"); //NOI18N
                    conf.addChild(wsdlFiles);
                }
                
                Xpp3Dom wsdlFile = new Xpp3Dom("wsdlFile"); //NOI18N
                wsdlFile.setValue(wsdlPath);
                wsdlFiles.addChild(wsdlFile);

                handle.markAsModified(handle.getPOMModel());
                
            }
        }

    }
    
    public static void addJaxws21Library(Project project) throws IOException {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.COMPILE);
            FileObject wsimportFO = classPath.findResource("javax/xml/ws/WebServiceFeature.class"); // NOI18N
            if (wsimportFO == null) {
                //add the JAXWS 2.1 library
                Library jaxws21_ext = LibraryManager.getDefault().getLibrary("jaxws21"); //NOI18N
                if (jaxws21_ext == null) {
                    throw new IOException("Unable to find JAXWS 21 Library."); //NOI18N
                }
                try {
                    ProjectClassPathModifier.addLibraries(new Library[] {jaxws21_ext}, srcGroups[0].getRootFolder(), ClassPath.COMPILE);
                } catch (IOException e) {
                    throw new IOException("Unable to add JAXWS 21 Library. " + e.getMessage()); //NOI18N
                }
            }
        }

    }

}
