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
package org.netbeans.modules.java.j2seproject.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;

@Deprecated
public class J2SEProjectClassPathExtender implements ProjectClassPathExtender {
    
    private static final String CP_CLASS_PATH = "javac.classpath"; //NOI18N

    private final J2SEProjectClassPathModifier delegate;

    public J2SEProjectClassPathExtender (final J2SEProjectClassPathModifier delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    public boolean addLibrary(final Library library) throws IOException {
        return addLibrary(CP_CLASS_PATH, library);
    }

    public boolean addLibrary(final String type, final Library library) throws IOException {
        return this.delegate.handleLibraries (new Library[] {library},type, J2SEProjectClassPathModifier.ADD);
    }

    public boolean addArchiveFile(final FileObject archiveFile) throws IOException {
        return addArchiveFile(CP_CLASS_PATH,archiveFile);
    }

    public boolean addArchiveFile(final String type, FileObject archiveFile) throws IOException {
        if (FileUtil.isArchiveFile(archiveFile)) {
            archiveFile = FileUtil.getArchiveRoot (archiveFile);
        }
        return this.delegate.handleRoots(new URL[] {archiveFile.getURL()},type,J2SEProjectClassPathModifier.ADD);
    }

    public boolean addAntArtifact(final AntArtifact artifact, final URI artifactElement) throws IOException {
        return addAntArtifact(CP_CLASS_PATH,artifact, artifactElement);
    }

    public boolean addAntArtifact(final String type, final AntArtifact artifact, final URI artifactElement) throws IOException {
        return this.delegate.handleAntArtifacts(new AntArtifact[] {artifact}, new URI[] {artifactElement},type,J2SEProjectClassPathModifier.ADD);
    }

}
