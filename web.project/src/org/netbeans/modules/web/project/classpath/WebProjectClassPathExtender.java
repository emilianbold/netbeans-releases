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

package org.netbeans.modules.web.project.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;

import org.netbeans.modules.web.project.ui.customizer.AntArtifactChooser;

public class WebProjectClassPathExtender implements ProjectClassPathExtender {
    
    private static final String CP_CLASS_PATH = "javac.classpath"; //NOI18N
    private static final String DEFAULT_WEB_MODULE_ELEMENT_NAME = ClassPathSupport.TAG_WEB_MODULE_LIBRARIES;

    private final WebProjectClassPathModifier delegate;

    public WebProjectClassPathExtender (final WebProjectClassPathModifier delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }
    
    public boolean addLibrary(final Library library) throws IOException {
        return addLibraries(CP_CLASS_PATH, new Library[] { library }, DEFAULT_WEB_MODULE_ELEMENT_NAME);
    }
    
    public boolean addLibraries(final String classPathId, final Library[] libraries, final String webModuleElementName) throws IOException {
        return this.delegate.handleLibraries(libraries, classPathId, webModuleElementName, WebProjectClassPathModifier.ADD);
    }

    public boolean addArchiveFile(final FileObject archiveFile) throws IOException {
        return addArchiveFiles(CP_CLASS_PATH, new FileObject[] { archiveFile }, DEFAULT_WEB_MODULE_ELEMENT_NAME);
    }

    public boolean addArchiveFiles(final String classPathId, FileObject[] archiveFiles, final String webModuleElementName) throws IOException {
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
        return this.delegate.handleRoots(archiveFileURLs, classPathId, webModuleElementName, WebProjectClassPathModifier.ADD);
    }
    
    // TODO: AB: AntArtifactItem should not be in LibrariesChooser
    
    public boolean addAntArtifact (AntArtifact artifact, URI artifactElement) throws IOException {
        return addAntArtifacts(CP_CLASS_PATH, new AntArtifactChooser.ArtifactItem[] { new AntArtifactChooser.ArtifactItem(artifact, artifactElement) }, DEFAULT_WEB_MODULE_ELEMENT_NAME);
    }

    public boolean addAntArtifacts(final String classPathId, final AntArtifactChooser.ArtifactItem[] artifactItems, final String webModuleElementName) throws IOException {
        AntArtifact[] artifacts = new AntArtifact[artifactItems.length];
        URI[] artifactElements = new URI[artifactItems.length];
        for (int i = 0; i < artifactItems.length; i++) {
            artifacts[i] = artifactItems[i].getArtifact();
            artifactElements[i] = artifactItems[i].getArtifactURI();
        }
        return this.delegate.handleAntArtifacts(artifacts, artifactElements, classPathId, webModuleElementName, WebProjectClassPathModifier.ADD);
    }
    
}
