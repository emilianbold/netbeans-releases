/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.java.project.classpath;

import java.io.IOException;
import java.net.URI;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;





/**
 * Interface for project's compile classpath extension
 * Project can provide this interface in its lookup to
 * allow clients to extend its compilation classpath
 * by new classpath element (jar, folder, dependent project, library).
 * @since org.netbeans.modules.java.project/1 1.3 
 */
public interface ProjectClassPathExtender {

    /**
     * Adds Library into project's compile classpath if the
     * library is not already included.
     * @param library to be added, not null.
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata can not be stored.
     */
    public boolean addLibrary (Library library) throws IOException;

    /**
     * Adds archive file into project's compile classpath if the
     * file is not already on it.
     * @param archiveFile to be added, can't be null.
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata can not be stored.
     */
    public boolean addArchiveFile (FileObject archiveFile) throws IOException;


    /**
     * Adds AntArtifact into project's compile classpath if the
     * artifact is not already on it.
     * @param artifact to be added, can't be null.
     * @param artifactElement the URI of the build output, the artifactElement
     * has to be owned by the artifact and must be relative to it.
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata can not be stored.
     */
    public boolean addAntArtifact (AntArtifact artifact, URI artifactElement) throws IOException;

}
