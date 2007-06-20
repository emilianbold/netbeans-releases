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
     * Removes libraries from the project. These libraries will be removed from the list of libraries which are expected
     * to be included in the created WAR file.
     * @param libraries to be removed
     * @param path the libraries path in the WAR file
     * @return true in case the library was removed (at least one library was removed),
     * the value false is returned when all the libraries were already removed.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removePackageLibraries(Library[] libraries, String path) throws IOException;

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
     * Removes libraries from the project's classpath if the libraries were not already removed.
     * @param libraries to be removed
     * @return true in case the classpath was changed (at least one library was removed from the classpath),
     * the value false is returned when all the libraries were already removed from the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removeCompileLibraries(Library[] libraries) throws IOException;
    
    /**
     * Adds artifacts (e.g. subprojects) into the project. These artifacts will not be added into the projects's classpath
     * but will be included in the created WAR file.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param path the artifacts path in the WAR file
     * @return true in case the artifacts were added (at least one artifact was added),
     * the value false is returned when all the artifacts are already included.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addPackageAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, String path) throws IOException;

    /**
     * Removes artifacts (e.g. subprojects) from the project. These artifacts will be removed from the list of artifacts which are expected
     * to be included in the created WAR file.
     * @param artifacts to be removed
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param path the artifacts path in the WAR file
     * @return true in case the artifacts were removed (at least one artifact was removed),
     * the value false is returned when all the artifacts were already removed.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removePackageAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, String path) throws IOException;

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
     * Removes artifacts (e.g. subprojects) from the project's classpath if the artifacts were not already removed.
     * @param artifacts to be removed
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @return true in case the classpath was changed (at least one artifact was removed from the classpath),
     * the value false is returned when all the artifacts were already removed from the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removeCompileAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements) throws IOException;
    
    /**
     * Adds archive files or folders into the project. These archive files or folders will not be added into
     * the projects's classpath but will be included in the created WAR file.
     * @param roots to be added
     * @param path the archive files or folders path in the WAR file
     * @return true in case the archive files or folders were added (at least one archive file or folder was added),
     * the value false is returned when all the archive files or folders are already included.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addPackageRoots(URL[] roots, String path) throws IOException;

    /**
     * Removes archive files or folders from the project. These archive files or folders will be removed from the list
     * of archive files or folders which are expected to be included in the created WAR file.
     * @param roots to be removed
     * @param path the archive files or folders path in the WAR file
     * @return true in case the archive files or folders were removed (at least one archive file or folder was removed),
     * the value false is returned when all the archive files or folders were already removed.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removePackageRoots(URL[] roots, String path) throws IOException;

    /**
     * Adds archive files or folders into the project's classpath if the archive files or folders are not already
     * included. These archive files or folders will not be included in the created WAR file.
     * @param roots to be added
     * @return true in case the classpath was changed (at least one archive file or folder was added to the classpath),
     * the value false is returned when all the archive files or folders are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addCompileRoots(URL[] roots) throws IOException;
    
    /**
     * Removes archive files or folders from the project's classpath if the archive files or folders were not already
     * removed.
     * @param roots to be removed
     * @return true in case the classpath was changed (at least one archive file or folder was removed from the classpath),
     * the value false is returned when all the archive files or folders were already removed from the classpath.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removeCompileRoots(URL[] roots) throws IOException;
}
