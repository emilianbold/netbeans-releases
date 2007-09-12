/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.javaee.sunresources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.ArchiveConstants;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 *
 * @author echou
 */
public class SunResourcesUtil {
    
    public static final String RES_DIR_PROPNAME = "resource.dir"; // NOI18N
    
    public static void addJavaEEResourceMetaData(final Project jbiProject, final AntArtifact artifact) {
        final AntProjectHelper aph = 
                (AntProjectHelper) jbiProject.getLookup().lookup(AntProjectHelper.class);
        if (aph == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(SunResourcesUtil.class, "EXC_no_anthelper"), 
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        
        try {
            ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction() {
                    public Object run() throws IOException {
                        EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        
                        try {
                            Project p = artifact.getProject();
                            String resourceDirRelative = getResourceDirRelative(p);
                            String projName = ProjectUtils.getInformation(p).getName();
                            String propName = "resource." + projName; // NOI18N
                            String propValue = "${project." + projName + "}/" // NOI18N
                                + resourceDirRelative;
                            ep.setProperty(propName, propValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                        ProjectManager.getDefault().saveProject(jbiProject);
                    
                        return null;
                    }
                }
            );
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }
    
    public static void removeJavaEEResourceMetaData(final Project jbiProject, final String projName) {
        final AntProjectHelper aph = 
                (AntProjectHelper) jbiProject.getLookup().lookup(AntProjectHelper.class);
        if (aph == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(SunResourcesUtil.class, "EXC_no_anthelper"),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }
        
        try {
            ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction() {
                    public Object run() throws IOException {
                        EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String propName = "resource." + projName; // NOI18N
                        ep.remove(propName);
                        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                        ProjectManager.getDefault().saveProject(jbiProject);
                        
                        return null;
                    }
                }
            );
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }
    
    public static String scanForSunResources(Project p) throws Exception {
        // aggregate *.sun-resource files into memory here
        ResourceAggregator resAggregator = new ResourceAggregator();
        List<Project> projList = getSubProjects(p);
        projList.add(p);
        for (int n = 0; n < projList.size(); n++) {
            Project currentP = projList.get(n);
            FileObject resourceDirFO = getResourceDir(currentP);
            FileObject[] children = resourceDirFO.getChildren();
            for (int i = 0; i < children.length; i++) {
                FileObject fo = children[i];
                if (!fo.isFolder() && fo.getExt().equalsIgnoreCase("sun-resource")) { // NOI18N
                    resAggregator.addResource(fo);
                }
            }
        }
        
        return resAggregator.toSunResourcesXML();
    }

    public static String scanForSunResources(String resourceFolder) throws Exception {
        // aggregate *.sun-resource files into memory here     

        // Backward compatibility
        if ((resourceFolder == null) || "".equals(resourceFolder)){
            Logger logger = Logger.getLogger(SunResourcesUtil.class.getName());
            logger.warning(NbBundle.getMessage(SunResourcesUtil.class, "EXC_resource_config_missisng"));
            return "";
        }
                
        ResourceAggregator resAggregator = new ResourceAggregator();
        
        File f = new File(resourceFolder);
        if (f.exists() && f.isDirectory()) {
            File[] children = f.listFiles();
            for (int i = 0; i < children.length; i++) {
                File child = children[i];
                if (child.isFile() && 
                        child.getName().toLowerCase().endsWith("sun-resource")) { // NOI18N
                    resAggregator.addResource(child);
                }
            }
        }
        return resAggregator.toSunResourcesXML();
    }
    
    public static String getResourceDirRelative (Project p) throws Exception {
        // default location of where *.sun-resource files are stored under project
        String resourceDir = "setup"; // NOI18N
        
        // find out where *.sun-resource files are stored
        AntProjectHelper aph = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
        if (aph == null) {
            // try again to use reflection to get AntProjectHelper
            Method m = p.getClass().getDeclaredMethod("getAntProjectHelper"); // NOI18N
            if (m != null) {
                aph = (AntProjectHelper) m.invoke(p);
            }
        }
        
        if (aph != null) {
            EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String propVal = ep.getProperty(RES_DIR_PROPNAME);
            if (propVal != null) {
                resourceDir = propVal;
            }
        }
        
        return resourceDir;
    }
    
    public static FileObject getResourceDir(Project p) throws Exception {
        // default location of where *.sun-resource files are stored under project
        String resourceDir = "setup"; // NOI18N
        
        // find out where *.sun-resource files are stored
        AntProjectHelper aph = (AntProjectHelper) p.getLookup().lookup(AntProjectHelper.class);
        if (aph == null) {
            // try again to use reflection to get AntProjectHelper
            Method m = p.getClass().getDeclaredMethod("getAntProjectHelper"); // NOI18N
            if (m != null) {
                aph = (AntProjectHelper) m.invoke(p);
            }
        }
        
        if (aph != null) {
            EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            PropertyEvaluator pe = aph.getStandardPropertyEvaluator();
            String propVal = ep.getProperty(RES_DIR_PROPNAME);
            if (propVal != null) {
                propVal = pe.evaluate(propVal);
                if (propVal != null) {
                    resourceDir = propVal;
                }
            }
        }
        FileObject resourceDirFO = p.getProjectDirectory().getFileObject(resourceDir);
        if (resourceDirFO == null) {
            resourceDirFO = p.getProjectDirectory().createFolder(resourceDir);
        }
        return resourceDirFO;
    }
    
    public static List<Project> getSubProjects(Project p) {
        List<Project> list = new ArrayList<Project> ();
        SubprojectProvider spp = 
                (SubprojectProvider) p.getLookup().lookup(SubprojectProvider.class);
        if (spp != null) {
            list.addAll(spp.getSubprojects());
        }
        return list;
    }
    
    public static ArchiveConstants.ArchiveType getJavaEEProjectType(Project proj) {
        String projClassName = proj.getClass().getName();
        if (projClassName.toLowerCase().indexOf("earproject") > -1){ // NOI18N
            return ArchiveConstants.ArchiveType.EAR;
        } else if (projClassName.toLowerCase().indexOf("ejbjarproject") > -1) { // NOI18N
            return ArchiveConstants.ArchiveType.EJB;
        } else if (projClassName.toLowerCase().indexOf("web.project") > -1) { // NOI18N
            return ArchiveConstants.ArchiveType.WAR;
        } else {
            return ArchiveConstants.ArchiveType.UNKNOWN;
        }
    }

    public static FileObject getProjectDistJar(Project p, String distPropName) throws Exception {
        AntProjectHelper aph = null;
        Method m = p.getClass().getDeclaredMethod("getAntProjectHelper"); // NOI18N
        if (m != null) {
           aph = (AntProjectHelper) m.invoke(p);
        }
        if (aph == null) {
            throw new Exception(
                    NbBundle.getMessage(SunResourcesUtil.class, "EXC_no_anthelper2", p.toString()));
        }
        
        EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        PropertyEvaluator pe = aph.getStandardPropertyEvaluator();
        String propVal = ep.getProperty(distPropName);
        if (propVal != null) {
            propVal = pe.evaluate(propVal);
            if (propVal != null) {
                return p.getProjectDirectory().getFileObject(propVal);
            }
        }
        
        return null;
    }
    
    
    // find the java source name under the project
    // and open it.
    public static void openSourceFile(final Project p, String javaName) {
        assert p != null;
        
        // look in selected project node first
        FileObject sourceFile = findSourceInProject(p, javaName);
        
        // look in dependent projects
        if (sourceFile == null) {
            SubprojectProvider spp = 
                    (SubprojectProvider) p.getLookup().lookup(SubprojectProvider.class);
            if (spp != null) {
                Set<? extends org.netbeans.api.project.Project> subs = spp.getSubprojects();
                for (Iterator<? extends Project> iter = subs.iterator(); iter.hasNext(); ) {
                    Project subProj = iter.next();
                    sourceFile = findSourceInProject(subProj, javaName);
                    if (sourceFile != null) {
                        break;
                    }
                }
            }
        }
        
        if (sourceFile == null) {
            String msg = 
                    NbBundle.getMessage(SunResourcesUtil.class, "EXC_srcfile_notfound", javaName);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
        } else {
            // open the fileobject in editor
            try {
                DataObject dataObj = DataObject.find(sourceFile);
                EditorCookie.Observable ec = 
                        (EditorCookie.Observable) dataObj.getCookie(EditorCookie.Observable.class);
                ec.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static FileObject findSourceInProject(final Project p, String javaName) {
        Sources sources = ProjectUtils.getSources(p);
        if (sources == null) {
            return null;
        }
        
        SourceGroup[] sgs = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0; i < sgs.length; i++) {
            SourceGroup sg = sgs[i];
            FileObject rootFolder = sg.getRootFolder();
            FileObject javaFile = rootFolder.getFileObject(javaName);
            if (javaFile != null) {
               return javaFile;
            }
        }
        
        return null;
    }
}
