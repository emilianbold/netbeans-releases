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

package org.netbeans.bluej;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.bluej.classpath.ClassPathProviderImpl;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author mkleint
 */
public class BJClassPathExtender implements ProjectClassPathExtender{

    private BluejProject project;
    
    /** Creates a new instance of BJClassPathExtender */
    public BJClassPathExtender(BluejProject proj) {
        project = proj;
    }

    public boolean addLibrary(org.netbeans.api.project.libraries.Library library) throws IOException {
        FileObject libs = project.getProjectDirectory().getFileObject("+libs"); //NOI18N
        if (libs == null) {
            libs = project.getProjectDirectory().createFolder("+libs"); //NOI18N
        }
        Iterator it = library.getContent("classpath").iterator(); //NOI18N
        while (it.hasNext()) {
            URL url = (URL) it.next();
            if (FileUtil.getArchiveFile(url) != null) {
                url = FileUtil.getArchiveFile(url);
            }
            FileObject fo = URLMapper.findFileObject(url);
            FileObject newLib = libs.getFileObject(fo.getNameExt());
            if (newLib == null) {
                FileUtil.copyFile(fo, libs, fo.getName());
            }
        }
        ClassPathProviderImpl prov = (ClassPathProviderImpl) project.getLookup().lookup(ClassPathProviderImpl.class);
        prov.getBluejCPImpl().fireChange();
        return true;
    }

    public boolean addArchiveFile(FileObject archiveFile) throws IOException {
        FileObject libs = project.getProjectDirectory().getFileObject("+libs"); //NOI18N
        if (libs == null) {
            libs = project.getProjectDirectory().createFolder("+libs"); //NOI18N
        }
        FileObject newLib = libs.getFileObject(archiveFile.getNameExt());
        if (newLib == null) {
            FileUtil.copyFile(archiveFile, libs, archiveFile.getName());
        }
        ClassPathProviderImpl prov = (ClassPathProviderImpl) project.getLookup().lookup(ClassPathProviderImpl.class);
        prov.getBluejCPImpl().fireChange();
        return true;
    }

    public boolean addAntArtifact(AntArtifact artifact, URI artifactElement) throws IOException {
        throw new IOException("It is not possible to create project dependencies in BlueJ projects. Please convert the project to J2SE Project first.");
    }
    
}
