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

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.modules.maven.api.customizer.ModelHandle;

/**
 *
 * @author mkuchtiak
 */
public class MavenModelUtils {
    
    public static Plugin getJaxWSPlugin(ModelHandle handle) {
        @SuppressWarnings("unchecked")
        List<Plugin> plugins = handle.getPOMModel().getBuild().getPlugins();
        for (Plugin plg : plugins) {
            if ("org.codehaus.mojo:jaxws-maven-plugin".equalsIgnoreCase(plg.getKey())) {
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
            if ("org.codehaus.mojo:jaxws-maven-plugin".equalsIgnoreCase(plg.getKey())) {
                //TODO CHECK THE ACTUAL PARAMETER VALUES..
                return plg;
            }
        }

        Plugin plugin = new Plugin();
        plugin.setGroupId("org.codehaus.mojo");
        plugin.setArtifactId("jaxws-maven-plugin");
        plugin.setVersion("1.10");
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
            ex.setId("wsimport-generate");
            ex.setPhase("generate-sources");
            List<String> goals = new ArrayList<String>();
            goals.add("wsimport");
            ex.setGoals(goals);
            plugin.addExecution(ex);
        } else {
            //TODO iterate execution goals and check for wsimport..
        }

        Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();
        if (dom == null) {
            dom = new Xpp3Dom("configuration");
            plugin.setConfiguration(dom);
        }

        Xpp3Dom dom2 = dom.getChild("sourceDestDir");
        if (dom2 == null) {
            dom2 = new Xpp3Dom("sourceDestDir");
            dom2.setValue("${project.build.directory}/generated-sources/jaxws-wsimport");
            dom.addChild(dom2);
        }

        Xpp3Dom dom3 = dom.getChild("xnocompile");
        if (dom3 == null) {
            dom3 = new Xpp3Dom("xnocompile");
            dom3.setValue("true");
            dom.addChild(dom3);
        }
        dom3 = dom.getChild("verbose");
        if (dom3 == null) {
            dom3 = new Xpp3Dom("verbose");
            dom3.setValue("true");
            dom.addChild(dom3);
        }

        handle.markAsModified(handle.getPOMModel());
        return plugin; 
    }
    
    public static void addWsdlFile(ModelHandle handle, String wsdlPath) {
        
        @SuppressWarnings("unchecked")
        List<Plugin> plugins = handle.getPOMModel().getBuild().getPlugins();
        for (Plugin plugin : plugins) {
            if ("org.codehaus.mojo:jaxws-maven-plugin".equalsIgnoreCase(plugin.getKey())) {
                //TODO CHECK THE ACTUAL PARAMETER VALUES..
                Xpp3Dom conf = (Xpp3Dom) plugin.getConfiguration();

                if (conf == null) {
                    conf = new Xpp3Dom("configuration");
                    plugin.setConfiguration(conf);
                }
                
                Xpp3Dom wsdlFiles = conf.getChild("wsdlFiles");
                if (wsdlFiles == null) {
                    wsdlFiles = new Xpp3Dom("wsdlFiles");
                    conf.addChild(wsdlFiles);
                }
                
                Xpp3Dom wsdlFile = new Xpp3Dom("wsdlFile");
                wsdlFile.setValue(wsdlPath);
                wsdlFiles.addChild(wsdlFile);
                
                System.out.println("conf = "+conf);
                handle.markAsModified(handle.getPOMModel());
                
            }
        }

    }

}
