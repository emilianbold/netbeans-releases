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

package org.netbeans.modules.groovy.support;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Support for extending project build script
 * 
 * @todo now supports only Java SE projects
 * 
 * @author Martin Adamek
 */
public class AntHelper {

    private static final String EXTENSIBLE_TARGET_NAME = "-pre-pre-compile"; // NOI18N
    private static final String GROOVY_EXTENSION_ID = "groovy"; // NOI18N
    private static final String GROOVY_BUILD_XML = "org/netbeans/modules/groovy/support/resources/groovy-build.xml"; // NOI18N
    
    private final Project project;
    
    public AntHelper(Project project) {
        this.project = project;
    }
    
    public void enableGroovy() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        List<String> extensibleTargets = extender.getExtensibleTargets();
        if (extender != null && extensibleTargets.contains(EXTENSIBLE_TARGET_NAME)) {
            AntBuildExtender.Extension extension = extender.getExtension(GROOVY_EXTENSION_ID);
            if (extension == null) {
                FileObject destDirFO = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
                try {
                    FileObject destFileFO = destDirFO.createData("groovy-build", "xml"); // NOI18N
                    copyResource(GROOVY_BUILD_XML, destFileFO);
                    extension = extender.addExtension(GROOVY_EXTENSION_ID, destFileFO);
                    extension.addDependency(EXTENSIBLE_TARGET_NAME, "-groovy-init-macrodef-javac"); // NOI18N
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }

    public boolean isGroovyEnabled() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        return extender.getExtension(GROOVY_EXTENSION_ID) != null;
    }
    
    private void copyResource(final String res, final FileObject to) throws IOException {
        InputStream inputStream = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream(res));
        try {
            FileLock lock = to.lock();
            try {
                OutputStream outputStream = new BufferedOutputStream(to.getOutputStream(lock));
                try {
                    FileUtil.copy(inputStream, outputStream);
                } finally {
                    outputStream.close();
                }
            } finally {
                lock.releaseLock();
            }
        } finally {
            inputStream.close();
        }
    }
    
}
