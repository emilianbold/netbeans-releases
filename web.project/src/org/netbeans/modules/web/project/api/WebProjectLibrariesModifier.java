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

package org.netbeans.modules.web.project.api;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;

public interface WebProjectLibrariesModifier {
    
    /**
     * Adds libraries into the project. These libraries will not be added into the projects's classpath
     * but will be included in the created WAR file.
     * @param libraries to be added
     * @param path the libraries path in the WAR file
     * @return true in case the library was added (at least one library was added),
     * the value false is returned when all the libraries are already included.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addPackageLibraries(Library[] libraries, String path) throws IOException;

    /**
     * Adds libraries into the project's classpath if the libraries are not already included. These libraries
     * will not be included in the created WAR file.
     * @param libraries to be added
     * @return true in case the classpath was changed (at least one library was added to the classpath),
     * the value false is returned when all the libraries are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addCompileLibraries(Library[] libraries) throws IOException;
    
    /**
     * Adds artifacts (e.g. subprojects) into the project. These artifacts will not be added into the projects's classpath
     * but will be included in the created WAR file.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param path the artifacts path in the WAR file
     * @return true in case the artifacts was added (at least one artifact was added),
     * the value false is returned when all the artifacts are already included.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addPackageAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, String path) throws IOException;

    /**
     * Adds artifacts (e.g. subprojects) into the project's classpath if the artifacts are not already included.
     * These artifacts will not be included in the created WAR file.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @return true in case the classpath was changed (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addCompileAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements) throws IOException;
    
    /**
     * Adds archive files or folders into the project. These archive files or folders will not be added into
     * the projects's classpath but will be included in the created WAR file.
     * @param roots to be added
     * @param path the archive files or folders path in the WAR file
     * @return true in case the archive files or folders was added (at least one archive file or folder was added),
     * the value false is returned when all the archive files or folders are already included.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addPackageRoots(URL[] roots, String path) throws IOException;

    /**
     * Adds archive files or folders into the project's classpath if the archive files or folders are not already
     * included. These archive files or folders will not be included in the created WAR file.
     * @param roots to be added
     * @return true in case the classpath was changed (at least one archive file or folder was added to the classpath),
     * the value false is returned when all the archive files or folders are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addCompileRoots(URL[] roots) throws IOException;
    
    //TODO: implement remove* methods
    
}
