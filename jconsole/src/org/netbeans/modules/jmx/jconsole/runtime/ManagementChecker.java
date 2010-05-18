/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jmx.jconsole.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

import org.netbeans.modules.jmx.common.runtime.J2SEProjectType;
import org.netbeans.modules.jmx.common.runtime.ManagementDialogs;
import org.netbeans.modules.jmx.common.runtime.ProjectUtilities;


/**
 *
 */
public class ManagementChecker {
    
    private static final String STANDARD_IMPORT_STRING = "<import file=\"nbproject/build-impl.xml\"/>";// NOI18N
    private static final String MANAGEMENT_IMPORT_STRING = "<import file=\"nbproject/management-build-impl.xml\"/>";// NOI18N
    private static final String MANAGEMENT_NAME_SPACE = "http://www.netbeans.org/ns/jmx/1";// NOI18N
    
    public static boolean checkProjectCanBeManaged(Project project) {
        Properties pp = J2SEProjectType.getProjectProperties(project);
        String mainClass = pp.getProperty("main.class");// NOI18N
        boolean res = false;
        if (mainClass != null && !"".equals(mainClass)) {// NOI18N
            FileObject fo = findFileForClass(mainClass, true);
            if (fo != null) res = true;
        }
        if (!res) {
            ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(ManagementChecker.class, "ERR_MainClassNotSet"), NotifyDescriptor.WARNING_MESSAGE));// NOI18N
            
            return false;
        }
        return true;
    }
    
    public static FileObject findFileForClass(String className, boolean tryInnerclasses) {
        FileObject fo = null;
        try {
            String resourceName = className.replaceAll("\\.", "/") + ".java"; //NOI18N
            GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
            Set paths = gpr.getPaths("classpath/source"); //NOI18N
            for (Iterator iterator = paths.iterator(); iterator.hasNext();) {
                ClassPath cp = (ClassPath) iterator.next();
                fo = cp.findResource(resourceName);
                if (fo != null) break;
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        if ((fo == null) && tryInnerclasses) {
            // not found - will try without last .xxx to see if the last name is not an innerclass name
            int dotIndex = className.lastIndexOf('.');
            if (dotIndex != -1)
                return findFileForClass(className.substring(0, dotIndex), true);
        }
        return fo;
    }
    
    public static boolean checkProjectIsModifiedForManagement(Project project) {
        Element e = ProjectUtils.getAuxiliaryConfiguration(project).getConfigurationFragment("data", MANAGEMENT_NAME_SPACE, true);// NOI18N
        if (e != null) return true; // already modified, nothing more to do
        
        if (ManagementDialogs.getDefault().notify(
                new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(ManagementChecker.class, "WARN_BUILD_UPDATE"), // NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION)
                ) != NotifyDescriptor.OK_OPTION) {
            return false; // cancelled by the user
        }
        
        Element mgtFragment = XMLUtil.createDocument("ignore", null, null, null).createElementNS(MANAGEMENT_NAME_SPACE, "data");// NOI18N
        mgtFragment.setAttribute("version", "0.4");// NOI18N
        ProjectUtils.getAuxiliaryConfiguration(project).putConfigurationFragment(mgtFragment, true);
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
            return false;
        }
        
        try {
            GeneratedFilesHelper gfh = new GeneratedFilesHelper(project.getProjectDirectory());
            gfh.refreshBuildScript("nbproject/management-build-impl.xml", ManagementChecker.class.getResource("management-build-impl.xsl"), false);// NOI18N
        } catch (IOException e1) {
            return false;
        }
        
        String buildScript = ProjectUtilities.getProjectBuildScript(project);
        
        if (buildScript == null) {
            ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(ManagementChecker.class, "ERR_BUILD_NOT_FOUND"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE)
                    );
            return false;
        }
        
        if (!ProjectUtilities.backupBuildScript(project)) {
            if (ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(ManagementChecker.class, "ERR_BUILD_NOT_BACKUP"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE)
                    ) != NotifyDescriptor.OK_OPTION) {
                return false; // cancelled by the user
            }
        }
        
        StringBuffer newDataBuffer = new StringBuffer(buildScript.length() + 200);
        int importIndex = buildScript.indexOf(STANDARD_IMPORT_STRING);
        if (importIndex == -1) {
            // notify the user that the build script cannot be modified, and he should perform the change himself
            ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(ManagementChecker.class, "ERR_BUILD_NOT_UPDATED"), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE)
                    );
            return false;
        }
        String indent = "";// NOI18N
        int idx = importIndex-1;
        while (idx >= 0) {
            if (buildScript.charAt(idx) == ' ') indent = " " + indent;// NOI18N
            else if (buildScript.charAt(idx) == '\t') indent = "\t" + indent;// NOI18N
            else break;
            idx--;
        }
        newDataBuffer.append(buildScript.substring(0, importIndex+STANDARD_IMPORT_STRING.length()+1));
        newDataBuffer.append("\n");// NOI18N
        newDataBuffer.append(indent);
        newDataBuffer.append(MANAGEMENT_IMPORT_STRING);
        newDataBuffer.append(buildScript.substring(importIndex+STANDARD_IMPORT_STRING.length()+1));
        
        FileObject buildFile = project.getProjectDirectory().getFileObject("build.xml");// NOI18N
        FileLock lock = null;
        PrintWriter writer = null;
        try {
            lock = buildFile.lock();
            writer = new PrintWriter(buildFile.getOutputStream(lock));
            writer.println(newDataBuffer.toString());
            
        } catch (FileNotFoundException e1) {
            e1.printStackTrace(System.err);
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
        } finally {
            lock.releaseLock();
            if (writer != null)
                writer.close();
        }
        return true;
    }
    
}
