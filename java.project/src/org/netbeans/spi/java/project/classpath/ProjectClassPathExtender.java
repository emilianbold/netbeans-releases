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
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.openide.filesystems.FileObject;

/**
 * Interface for project's compile classpath extension.
 * A project can provide this interface in its {@link org.netbeans.api.project.Project#getLookup lookup} to
 * allow clients to extend its compilation classpath
 * by a new classpath element (JAR, folder, dependent project, or library).
 * @since org.netbeans.modules.java.project/1 1.3 
 */
public interface ProjectClassPathExtender {

    /**
     * Adds a library into the project's compile classpath if the
     * library is not already included.
     * @param library to be added
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata cannot be changed
     */
    boolean addLibrary(Library library) throws IOException;

    /**
     * Adds an archive file or folder into the project's compile classpath if the
     * entry is not already there.
     * @param archiveFile ZIP/JAR file to be added
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata cannot be changed
     */
    boolean addArchiveFile(FileObject archiveFile) throws IOException;

    /**
     * Adds an artifact (e.g. subproject) into project's compile classpath if the
     * artifact is not already on it.
     * @param artifact to be added
     * @param artifactElement the URI of the build output
     *                        (must be owned by the artifact and be relative to it)
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata cannot be changed
     */
    boolean addAntArtifact(AntArtifact artifact, URI artifactElement) throws IOException;

}
