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

import java.util.List;
import javax.xml.namespace.QName;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;

/**
 *
 * @author mkuchtiak
 */
public class MavenModelUtils {

    /**
     * returns a RESOLVED project instance of the
     * plugin if defined in the pom.xml or any of the parents.
     * @param handle
     * @return
     */
    public static org.apache.maven.model.Plugin getJaxWSPlugin(MavenProject project) {
        @SuppressWarnings("unchecked")
        List<org.apache.maven.model.Plugin> plugins = project.getBuildPlugins();
        for (org.apache.maven.model.Plugin plg : plugins) {
            if ("org.codehaus.mojo:jaxws-maven-plugin".equalsIgnoreCase(plg.getKey())) {
                //TODO CHECK THE ACTUAL PARAMETER VALUES..
                return plg;
            }
        }
        return null;
    }
    /**
     * adds jaxws plugin, requires the model to have a transaction started,
     * eg. by calling as part of Utilities.performPOMModelOperations(ModelOperation<POMModel>)
     * @param model
     * @return
     */
    public static Plugin addJaxWSPlugin(POMModel model) {
        assert model.isIntransaction() : "need to call model modifications under transaction."; //NOI18N
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            model.getProject().setBuild(bld);
        }
        Plugin plugin = bld.findPluginById("org.codehaus.mojo", "jaxws-maven-plugin"); //NOI18N
        if (plugin != null) {
            //TODO CHECK THE ACTUAL PARAMETER VALUES..
            return plugin;
        }
        plugin = model.getFactory().createPlugin();
        plugin.setGroupId("org.codehaus.mojo"); //NOI18N
        plugin.setArtifactId("jaxws-maven-plugin"); //NOI18N
        plugin.setVersion("1.10"); //NOI18N
        bld.addPlugin(plugin);
        PluginExecution exec = plugin.findExecutionById("wsimport-generate"); //NOI18N
        if (exec == null) {
            exec = model.getFactory().createExecution();
            exec.setId("wsimport-generate"); //NOI18N
            exec.setPhase("generate-sources"); //NOI18N
            exec.addGoal("wsimport"); //NOI18N
            plugin.addExecution(exec);
        } else {
            //shall we do something here?
        }
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            plugin.setConfiguration(config);
        }
        config.setSimpleParameter("sourceDestDir", "${project.build.directory}/generated-sources/jaxws-wsimport"); //NOI18N
        config.setSimpleParameter("xnocompile", "true"); //NOI18N
        config.setSimpleParameter("verbose", "true"); //NOI18N
        return plugin; 
    }
    
    public static void addWsdlFile(Plugin plugin, String wsdlPath) {
        POMModel model = plugin.getModel();
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            plugin.setConfiguration(config);
            // a bit suspicious but still.. the addJaxWSPlugin method shall have added it..
        }
        List<POMExtensibilityElement> elems = config.getConfigurationElements();
        POMExtensibilityElement wsdlFiles = null;
        QName qname = POMQName.createQName("wsdlFiles", model.getPOMQNames().isNSAware()); //NOI18N
        for (POMExtensibilityElement el : elems) {
            if (qname.equals(el.getQName())) {
                wsdlFiles = el;
                break;
            }
        }
        if (wsdlFiles == null) {
            wsdlFiles = model.getFactory().createPOMExtensibilityElement(qname);
            config.addExtensibilityElement(wsdlFiles);
        }
        qname = POMQName.createQName("wsdlFile", model.getPOMQNames().isNSAware()); //NOI18N
        elems = wsdlFiles.getExtensibilityElements();
        for (POMExtensibilityElement el : elems) {
            if (qname.equals(el.getQName())) {
                if (wsdlPath.equals(el.getElementText())) {
                    //already there..
                    return;
                }
            }
        }
        POMExtensibilityElement el = model.getFactory().createPOMExtensibilityElement(qname);
        el.setElementText(wsdlPath);
        wsdlFiles.addExtensibilityElement(el);
    }

}
