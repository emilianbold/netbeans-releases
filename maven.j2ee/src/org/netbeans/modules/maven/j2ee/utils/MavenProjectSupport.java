/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.utils;

import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.web.WebModuleImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/**
 * Provides a various methods to help with typical Maven Projects requirements
 * 
 * @author Martin Janicek
 */
public class MavenProjectSupport {

    private MavenProjectSupport() {
    }
    
    
    public static void storeSettingsToPom(Project project, final String name, final String value) {
        storeSettingsToPom(project.getProjectDirectory(), name, value);
    }
    
    /**
     * Store given property pair <name, value> to pom.xml file of the given project
     * 
     * @param projectFile project to which pom.xml should be updated
     * @param name property name
     * @param value property value
     */
    public static void storeSettingsToPom(FileObject projectFile, final String name, final String value) {
        final ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {

            @Override
            public void performOperation(POMModel model) {
                Properties props = model.getProject().getProperties();
                if (props == null) {
                    props = model.getFactory().createProperties();
                    model.getProject().setProperties(props);
                }
                props.setProperty(name, value);
            }
        };
        final FileObject pom = projectFile.getFileObject("pom.xml"); //NOI18N
        try {
            pom.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public static void createDDIfRequired(Project project) {
        createDDIfRequired(project, null);
    }
    
    /**
     * Creates web.xml deployment descriptor if it's required for given project (this method was created as a
     * workaround for issue #204572 and probably won't be needed when WebLogic issue will be fixed)
     * 
     * @param project project for which should DD be generated
     * @param serverID server ID of given project
     */
    public static void createDDIfRequired(Project project, String serverID) {
        if (serverID == null) {
            serverID = readServerID(project);
        }
        // TODO change condition to use ConfigSupportImpl.isDescriptorRequired
        if (serverID != null && serverID.contains("WebLogic")) {
            createDD(project);
        }
    }
    
    private static void createDD(Project project) {
        WebModuleProviderImpl webModule = project.getLookup().lookup(WebModuleProviderImpl.class);
        
        if (webModule != null) {
            WebModuleImpl webModuleImpl = webModule.getModuleImpl();
            try {
                FileObject webInf = webModuleImpl.getWebInf();
                if (webInf == null) {
                    webInf = webModuleImpl.createWebInf();
                }
                assert webInf != null;
                
                FileObject webXml = webModuleImpl.getDeploymentDescriptor();
                if (webXml == null) {
                    AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
                    String j2eeVersion = props.get(MavenJavaEEConstants.HINT_J2EE_VERSION, false);
                    webXml = DDHelper.createWebXml(Profile.fromPropertiesString(j2eeVersion), webInf);
                }

                assert webXml != null; // this should never happend if there a valid j2eeVersion was parsed
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Read server ID for the given project
     * 
     * @param project project for which we want to get server ID
     * @return server ID
     */
    public static String readServerID(Project project) {
        return readSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, false);
    }

    /**
     * Read J2EE version for the given project
     * 
     * @param projectfor which we want to get J2EE version
     * @return J2EE version
     */
    public static String readJ2eeVersion(Project project)  {
        return readSettings(project, MavenJavaEEConstants.HINT_J2EE_VERSION, false);
    }
    
    /**
     * Read compileOnSave property for the given project
     * Possible values are:
     * <ul>
     * <li>all - both tests and application gets run by netbeans quick run infrastructure</li>
     * <li>test - only tests are run by netbeans quick run infrastructure, not application - default value</li>
     * <li>app - only application is run by netbeans quick run infrastructure, not tests</li>
     * <li>none - no compile on save
     * </ul>
     * 
     * @param project project for which we want to get CoS value
     * @return one of possible CoS values
     */
    public static String isCompileOnSave(Project project) {
        return readSettings(project, Constants.HINT_COMPILE_ON_SAVE, true);
    }
    
    private static String readSettings(Project project, String propertyName, boolean shared) {
        return project.getLookup().lookup(AuxiliaryProperties.class).get(propertyName, shared);
    }
    
    
    
    public static void setJ2eeVersion(Project project, String value) {
        setSettings(project, MavenJavaEEConstants.HINT_J2EE_VERSION, value, false);
    }
    
    public static void setServerID(Project project, String value) {
        setSettings(project, MavenJavaEEConstants.HINT_DEPLOY_J2EE_SERVER, value, false);
    }
    
    public static void setCompileOnSave(Project project, String value) {
        setSettings(project, Constants.HINT_COMPILE_ON_SAVE, value, true);
    }
    
    private static void setSettings(Project project, String key, String value, boolean shared) {
        AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
        props.put(key, value, shared);
    }
}
