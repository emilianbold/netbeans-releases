/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.archive.project;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.w3c.dom.Element;

public class ArchiveProjectProperties {
    
    private Project project;
    private AntProjectHelper helper;
    private EditableProperties projectProps, privateProps;
    
    final public static String J2EE_SERVER_INSTANCE = "j2ee.server.instance";  //NOI18N
    
    final public static String J2EE_SERVER_TYPE = "j2ee.server.type";  //NOI18N
    
    final public static String SOURCE_ARCHIVE = "source.archive";  //NOI18N
    
    final public static String DIST_DIR = "dist.dir";  //NOI18N
    
    // TODO - better name (also fix in build-impl.xsl)
    final public static String WAR_NAME = "war.name";  //NOI18N
    
    final public static String PROXY_PROJECT_DIR = "proxy.project.dir";                   //NOI18N
    
    final public static String TMP_PROJ_DIR_VALUE = "tmpproj";  //NOI18N
    
    final public static String DIST_DIR_VALUE = "dist";  //NOI18N
    
    final public static String DIST_ARCHIVE ="dist.archive";  //NOI18N
    
    final public static String CONTENT_DIR = "content.dir";  //NOI18N
    
    final public static String CONF_DIR = "conf.dir";  //NOI18N
    
    public static final String SETUP_DIR_VALUE = "setup";  //NOI18N
    
    public static final String ARCHIVE_TYPE = "javaee.archive.type";  //NOI18N
    
    public static final String  PROJECT_TYPE_VALUE_UNKNOWN = "unknown";                //NOI18N
    
    public static final String PROJECT_TYPE_VALUE_WAR = "war";                         //NOI18N
    
    public static final String PROJECT_TYPE_VALUE_EAR = "ear";                         //NOI18N
    
    public static final String PROJECT_TYPE_VALUE_CAR = "car";                         //NOI18N
    
    public static final String PROJECT_TYPE_VALUE_RAR = "rar";                         //NOI18N
    
    public static final String PROJECT_TYPE_VALUE_JAR = "jar";                         //NOI18N
    
    public static final String WAR_ARCHIVES = "javaee.archive.war.archives";
    public static final String JAR_ARCHIVES = "javaee.archive.jar.archives";
    public static final String RAR_ARCHIVES = "javaee.archive.rar.archives";
    
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; //NOI18N
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N
    public static final String CAR_ARCHIVES = "javaee.archive.car.archives";
    
    public ArchiveProjectProperties(Project project, AntProjectHelper helper
            /*PropertyEvaluator evaluator, ReferenceHelper refHelper*/) {
        this.project = project;
        this.helper = helper;
        
        privateProps = helper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
//        privateGroup = new StoreGroup();
//        projectGroup = new StoreGroup();
    }
    
    public void save() {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                @Override
                public Object run() throws IOException {
                    // and save the project
                    try {
////
////                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
////                    setNewServerInstanceValue(serverInstanceID, projectProps, privateProps);
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                        Element data = helper.getPrimaryConfigurationData(true);
                        helper.putPrimaryConfigurationData(data, true);
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify(e.getException());
        }
    }
    
    public Object get(String propertyName) {
//        EditableProperties projectProperties = helper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
//        EditableProperties privateProperties = helper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        Object retVal;
        if (J2EE_SERVER_INSTANCE.equals(propertyName))
            retVal = privateProps.getProperty(J2EE_SERVER_INSTANCE);
        else
            retVal = projectProps.getProperty(propertyName);
        return retVal;
    }
    
    public void put( String propertyName, String value ) {
//        EditableProperties projectProperties = helper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        if (J2EE_SERVER_INSTANCE.equals(propertyName)) {
            try {
                projectProps.put(J2EE_SERVER_TYPE, Deployment.getDefault().getServerInstance(value).getServerID()); //            EditableProperties privateProperties = helper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
                //            EditableProperties privateProperties = helper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
            } catch (InstanceRemovedException ex) {
                Logger.getLogger("global").log(Level.INFO, value, ex);
            }
            privateProps.put(J2EE_SERVER_INSTANCE,value);
            
        } else {
            projectProps.put(propertyName, value);
        }
    }
    
    public static void setServerInstance(final Project project, final AntProjectHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    setNewServerInstanceValue(serverInstanceID, projectProps, privateProps);
                    // ant deployment support
                    File projectFolder = FileUtil.toFile(project.getProjectDirectory());
                    try {
                        AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, ANT_DEPLOY_BUILD_SCRIPT),
                                mapType(projectProps.getProperty(ARCHIVE_TYPE)), serverInstanceID); // NOI18N
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    }
                    File antDeployPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
                    if (antDeployPropsFile == null) {
                        privateProps.remove(DEPLOY_ANT_PROPS_FILE);
                    } else {
                        privateProps.setProperty(DEPLOY_ANT_PROPS_FILE, antDeployPropsFile.getAbsolutePath());
                    }
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        });
    }
    
    private static void setNewServerInstanceValue(String newServInstID, /* Project project,*/ EditableProperties projectProps, EditableProperties privateProps) {
        // update j2ee.server.type
        try {
            projectProps.put(J2EE_SERVER_TYPE, Deployment.getDefault().getServerInstance(newServInstID).getServerID()); //            EditableProperties privateProperties = helper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
            //            EditableProperties privateProperties = helper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        } catch (InstanceRemovedException ex) {
            Logger.getLogger("global").log(Level.INFO, newServInstID, ex);
        }
        
        // update j2ee.server.instance
        privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
    }
    public static  Object mapType(String string) {
        Object retVal = J2eeModule.Type.EJB;
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_CAR)) {
            retVal = J2eeModule.Type.CAR;
        }
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_WAR)) {
            retVal = J2eeModule.Type.WAR;
        }
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_JAR)) {
            retVal = J2eeModule.Type.EJB;
        }
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_EAR)) {
            retVal = J2eeModule.Type.EAR;
        }
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_RAR)) {
            retVal = J2eeModule.Type.RAR;
        }
        return retVal;
    }
    
}
