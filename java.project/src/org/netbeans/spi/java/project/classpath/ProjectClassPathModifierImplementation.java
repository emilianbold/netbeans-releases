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

package org.netbeans.spi.java.project.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.project.classpath.ProjectClassPathModifierAccessor;
import org.openide.filesystems.FileObject;

/**
 * An SPI for project's classpaths modification.
 * A project can provide subclass of this class in its {@link org.netbeans.api.project.Project#getLookup lookup} to
 * allow clients to add or remove new classpath elements (JAR, folder, dependent project, or library) to its 
 * classpaths.
 * @since org.netbeans.modules.java.project/1 1.10
 */
public abstract class ProjectClassPathModifierImplementation {
    
    static {
        ProjectClassPathModifierAccessor.INSTANCE = new Accessor ();
    }    
    
    protected ProjectClassPathModifierImplementation () {
    }
    
    
    /**
     * Returns the {@link SourceGroup}s providing classpath(s)
     * which may be modified.
     * @return (possibly empty) array of {@link SourceGroup}, never returns null
     */
    protected abstract SourceGroup [] getExtensibleSourceGroups ();
    
    
    /**
     * Returns the types of classpaths for given {@link SourceGroup} which may be modified.
     * @param sourceGroup for which the classpath types should be returned
     * @return (possibly empty) array of classpath types, never returns null
     */
    protected abstract String[] getExtensibleClassPathTypes (SourceGroup sourceGroup);
            
    /**
     * Adds libraries into the project's classpath if the
     * libraries are not already included.
     * @param libraries to be added
     * @param sourceGroup of type {@link org.netbeans.api.java.project.JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the library should be added to,
     * eg {@link org.netbeans.api.java.classpath.ClassPath.COMPILE}
     * @return true in case the classpath was changed (at least one library was added to the classpath),
     * the value false is returned when all the libraries are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a library to the classpath of the given type.
     */
    protected abstract boolean addLibraries (Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    
    /**
     * Removes libraries from the project's classpath if the
     * libraries are included on it.
     * @param libraries to be removed
     * @param sourceGroup of type {@link org.netbeans.api.java.project.JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the library should be removed from,
     * eg {@link org.netbeans.api.java.classpath.ClassPath.COMPILE}
     * @return true in case the classpath was changed, (at least one library was removed from the classpath),
     * the value false is returned when none of the libraries was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a library from the classpath of the given type.
     */
    protected abstract boolean removeLibraries (Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    /**
     * Adds archive files or folders into the project's classpath if the
     * entries are not already there.
     * @param classPathRoots roots to be added, each root has to be either a root of an archive or a folder
     * @param sourceGroup of type {@link org.netbeans.api.java.project.JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the root should be added to,
     * eg {@link org.netbeans.api.java.classpath.ClassPath.COMPILE}
     * @return true in case the classpath was changed, (at least one classpath root was added to the classpath),
     * the value false is returned when all the classpath roots are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a root to the classpath of the given type.
     */
    protected abstract boolean addRoots (URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    /**
     * Removes archive files or folders from the project's classpath if the
     * entries are included on it.
     * @param classPathRoots roots to be removed, each root has to be either a root of an archive or a folder
     * @param sourceGroup of type {@link org.netbeans.api.java.project.JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the root should be removed from,
     * eg {@link org.netbeans.api.java.classpath.ClassPath.COMPILE}
     * @return true in case the classpath was changed, (at least one classpath root was removed from the classpath),
     * the value false is returned when none of the classpath roots was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a root from the classpath of the given type.
     */
    protected abstract boolean removeRoots (URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    /**
     * Adds artifacts (e.g. subprojects) into project's classpath if the
     * artifacts are not already on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts. 
     * (must be owned by the artifact and be relative to it)
     * @param sourceGroup of type {@link org.netbeans.api.java.project.JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the artifact should be added to,
     * eg {@link org.netbeans.api.java.classpath.ClassPath.COMPILE}
     * @return true in case the classpath was changed, (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of an artifact to the classpath of the given type.
     */
    protected abstract boolean addAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    /**
     * Removes artifacts (e.g. subprojects) from project's classpath if the
     * artifacts are included on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param sourceGroup of type {@link org.netbeans.api.java.project.JavaProjectConstants#SOURCES_TYPE_JAVA}
     * identifying the compilation unit to change
     * @param type the type of the classpath the artifact should be removed from,
     * eg {@link org.netbeans.api.java.classpath.ClassPath.COMPILE}
     * @return true in case the classpath was changed, (at least one artifact was removed from the classpath),
     * the value false is returned when none of the artifacts was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of an artifact from the classpath of the given type.
     */
    protected abstract boolean removeAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;

    
    private static class Accessor extends ProjectClassPathModifierAccessor {        
        
        public SourceGroup[] getExtensibleSourceGroups(final ProjectClassPathModifierImplementation m) {
            assert m != null;
            return m.getExtensibleSourceGroups();
        }
        
        public String[] getExtensibleClassPathTypes (final ProjectClassPathModifierImplementation m, SourceGroup sg) {
            assert m != null;
            assert sg != null;
            return m.getExtensibleClassPathTypes(sg);
        }
        
        public boolean removeLibraries(Library[] libraries, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.removeLibraries(libraries, sourceGroup, type);
        }

        public boolean removeAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.removeAntArtifacts(artifacts, artifactElements, sourceGroup, type);
        }

        public boolean addLibraries (Library[] libraries, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.addLibraries(libraries, sourceGroup, type);
        }

        public boolean addAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.addAntArtifacts (artifacts, artifactElements, sourceGroup, type);
        }

        public boolean removeRoots (URL[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.removeRoots(classPathRoots, sourceGroup, type);
        }

        public boolean addRoots (URL[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
            assert m!= null;
            return m.addRoots (classPathRoots, sourceGroup, type);
        }       
                        
    }
}
