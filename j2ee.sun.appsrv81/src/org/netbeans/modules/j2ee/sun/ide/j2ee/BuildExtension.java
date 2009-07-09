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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
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
            FileObject templateFO = FileUtil.getConfigFile("Templates/SunResources/extendArchiveGF.xml"); // NOI18N
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
            Logger.getLogger(BuildExtension.class.getName()).log(Level.FINER,
                    null,ex);
            // the build xml file is broken... we probably don't want to try to
            // edit it programatically.
            return;
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
            Logger.getLogger(BuildExtension.class.getName()).log(Level.FINER,
                    null,ex);
            // the build xml file is broken... we probably don't want to try to
            // edit it programatically.
            return;
        }  catch (IOException ex) {
            Logger.getLogger(BuildExtension.class.getName()).log(Level.FINER,
                    null,ex);
            // the build xml file is broken... we probably don't want to try to
            // edit it programatically.
            return;
        }
        AntBuildExtender extender = proj.getLookup().lookup(AntBuildExtender.class);
        if (extender != null) {
            AntBuildExtender.Extension ext = extender.getExtension("gfarchiveextend");  // NOI18N
            if (ext != null) {
                try {
                    ext.removeDependency(target, "-extend-archive"); // NOI18N
                } catch (IllegalArgumentException iae) {
                    Logger.getLogger(BuildExtension.class.getName()).log(Level.FINER,null,
                            iae);
                }
                extender.removeExtension("gfarchiveextend"); // NOI18N
            }
            ProjectManager.getDefault().saveProject(proj);
        } else if (null == extender) {
            Logger.getLogger(BuildExtension.class.getName()).log(Level.FINER,
                    "Trying to remove GF build snippet in project type that doesn't support AntBuildExtender API contract."); // NOI18N
        }
    }
}
