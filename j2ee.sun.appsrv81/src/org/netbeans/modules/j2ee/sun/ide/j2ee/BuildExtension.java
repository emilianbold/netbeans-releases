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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author vkraemer
 */
public class BuildExtension {
    
    /** Creates a new instance of BuildExtension */
    private BuildExtension() {
    }
    
    static void copyTemplate(Project proj) throws IOException {
        FileObject projDir = proj.getProjectDirectory();
        FileObject jnlpBuildFile = projDir.getFileObject("nbproject/extendArchiveGF.xml"); // NOI18N
        if (jnlpBuildFile == null) {
            FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
            FileObject templateFO = sfs.findResource("Templates/SunResources/extendArchiveGF.xml"); // NOI18N
            if (templateFO != null) {
                FileUtil.copyFile(templateFO, projDir.getFileObject("nbproject"), "extendArchiveGF"); // NOI18N
            }
        }
    }
    
    static void removeTemplate(Project proj) throws IOException {
        FileObject projDir = proj.getProjectDirectory();
        FileObject jnlpBuildFile = projDir.getFileObject("nbproject/extendArchiveGF.xml"); // NOI18N
        if (jnlpBuildFile != null) {
            jnlpBuildFile.delete();
        }
    }

    static void extendBuildXml(Project proj, String target) throws IOException {
        FileObject projDir = proj.getProjectDirectory();
        final FileObject buildXmlFO = projDir.getFileObject("build.xml"); // NOI18N
        if (null == buildXmlFO) {
            // eject
            return;
        }
        File buildXmlFile = FileUtil.toFile(buildXmlFO);
        try {
            XMLUtil.parse(new InputSource(buildXmlFile.toURI().toString()), false, true, null, null);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        FileObject jnlpBuildFile = projDir.getFileObject("nbproject/extendArchiveGF.xml"); // NOI18N
        AntBuildExtender extender = proj.getLookup().lookup(AntBuildExtender.class);
        if (extender != null) {
            if (extender.getExtension("gfarchiveextend") == null) { // NOI18N
                AntBuildExtender.Extension ext = extender.addExtension("gfarchiveextend", jnlpBuildFile); // NOI18N
                ext.addDependency(target, "-extend-archive"); // NOI18N
            }
            ProjectManager.getDefault().saveProject(proj);
        } else {
            Logger.getLogger(BuildExtension.class.getName()).log(Level.FINER,
                    "Trying to include GF build snippet in project type that doesn't support AntBuildExtender API contract."); // NOI18N
        }
    }
    
    static void abbreviateBuildXml(Project proj, String target) throws IOException {
        FileObject projDir = proj.getProjectDirectory();
        final FileObject buildXmlFO = projDir.getFileObject("build.xml"); // NOI18N
        if (null == buildXmlFO) {
            // eject
            return;
        }
        File buildXmlFile = FileUtil.toFile(buildXmlFO);
        try {
            XMLUtil.parse(new InputSource(buildXmlFile.toURI().toString()), false, true, null, null);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        }
        FileObject jnlpBuildFile = projDir.getFileObject("nbproject/extendArchiveGF.xml"); // NOI18N
        AntBuildExtender extender = proj.getLookup().lookup(AntBuildExtender.class);
        if (extender != null && jnlpBuildFile != null) {
            AntBuildExtender.Extension ext = extender.getExtension("gfarchiveextend");
            if (extender.getExtension("gfarchiveextend") != null) { // NOI18N
                ext.removeDependency(target, "-extend-archive"); // NOI18N
                extender.removeExtension("gfarchiveextend"); // NOI18N
            }
            ProjectManager.getDefault().saveProject(proj);
        } else if (null == extender) {
            Logger.getLogger(BuildExtension.class.getName()).log(Level.FINER,
                    "Trying to remove GF build snippet in project type that doesn't support AntBuildExtender API contract."); // NOI18N
        }
    }
}
