/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.archive.project;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
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
            projectProps.put(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID((String) value));
//            EditableProperties privateProperties = helper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
            privateProps.put(J2EE_SERVER_INSTANCE,value);
            
        } else {
            projectProps.put(propertyName, value);
        }
    }
    
    public static void setServerInstance(final Project project, final AntProjectHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().writeAccess(new Runnable() {
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
        projectProps.setProperty(J2EE_SERVER_TYPE, Deployment.getDefault().getServerID(newServInstID));
        
        // update j2ee.server.instance
        privateProps.setProperty(J2EE_SERVER_INSTANCE, newServInstID);
    }
    public static  Object mapType(String string) {
        Object retVal = J2eeModule.EJB;
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_CAR)) {
            retVal = J2eeModule.CLIENT;
        }
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_WAR)) {
            retVal = J2eeModule.WAR;
        }
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_JAR)) {
            retVal = J2eeModule.EJB;
        }
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_EAR)) {
            retVal = J2eeModule.EAR;
        }
        if (string.equals(ArchiveProjectProperties.PROJECT_TYPE_VALUE_RAR)) {
            retVal = J2eeModule.CONN;
        }
        return retVal;
    }
    
}
