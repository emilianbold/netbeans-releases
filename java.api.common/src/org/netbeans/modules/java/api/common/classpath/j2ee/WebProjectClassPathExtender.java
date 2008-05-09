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

package org.netbeans.modules.java.api.common.classpath.j2ee;

import java.io.IOException;
import java.net.URL;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.classpath.ProjectClassPathModifierSupport.Operation;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;

// will be moved to j2ee.common
/**
 *
 * @author Tomas Mysik
 * @deprecated
 */
@Deprecated
public abstract class WebProjectClassPathExtender implements ProjectClassPathExtender {

    private static final String CP_CLASS_PATH = "javac.classpath"; // NOI18N
    private static final String DEFAULT_WEB_MODULE_ELEMENT_NAME = WebClassPathSupport.TAG_WEB_MODULE_LIBRARIES;

    private final WebProjectClassPathModifier delegate;

    public WebProjectClassPathExtender(final WebProjectClassPathModifier delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    public boolean addLibrary(final Library library) throws IOException {
        return addLibraries(CP_CLASS_PATH, new Library[] { library }, DEFAULT_WEB_MODULE_ELEMENT_NAME);
    }

    public boolean addLibraries(final String classPathId, final Library[] libraries, final String webModuleElementName)
            throws IOException {
        return delegate.handleLibraries(libraries, classPathId, webModuleElementName, Operation.ADD);
    }

    public boolean addArchiveFile(final FileObject archiveFile) throws IOException {
        return addArchiveFiles(CP_CLASS_PATH, new FileObject[] { archiveFile }, DEFAULT_WEB_MODULE_ELEMENT_NAME);
    }

    public boolean addArchiveFiles(final String classPathId, FileObject[] archiveFiles,
            final String webModuleElementName) throws IOException {
        for (int i = 0; i < archiveFiles.length; i++) {
            FileObject archiveFile = archiveFiles[i];
            if (FileUtil.isArchiveFile(archiveFile)) {
                archiveFiles[i] = FileUtil.getArchiveRoot(archiveFile);
            }
        }
        URL[] archiveFileURLs = new URL[archiveFiles.length];
        for (int i = 0; i < archiveFiles.length; i++) {
            archiveFileURLs[i] = archiveFiles[i].getURL();
        }
        return delegate.handleRoots(archiveFileURLs, classPathId, webModuleElementName, Operation.ADD);
    }

    // TODO: AB: AntArtifactItem should not be in LibrariesChooser

    // has to be probably handled in project module itself
    /*public boolean addAntArtifact (AntArtifact artifact, URI artifactElement) throws IOException {
        return addAntArtifacts(CP_CLASS_PATH, new AntArtifactChooser.ArtifactItem[] { new AntArtifactChooser.ArtifactItem(artifact, artifactElement) }, DEFAULT_WEB_MODULE_ELEMENT_NAME);
    }

    public boolean addAntArtifacts(final String classPathId, final AntArtifactChooser.ArtifactItem[] artifactItems, final String webModuleElementName) throws IOException {
        AntArtifact[] artifacts = new AntArtifact[artifactItems.length];
        URI[] artifactElements = new URI[artifactItems.length];
        for (int i = 0; i < artifactItems.length; i++) {
            artifacts[i] = artifactItems[i].getArtifact();
            artifactElements[i] = artifactItems[i].getArtifactURI();
        }
        return delegate.handleAntArtifacts(artifacts, artifactElements, classPathId, webModuleElementName, WebProjectClassPathModifier.ADD);
    }*/
}
