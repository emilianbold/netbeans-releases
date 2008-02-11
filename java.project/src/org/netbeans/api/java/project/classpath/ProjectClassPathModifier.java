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

package org.netbeans.api.java.project.classpath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.project.classpath.ProjectClassPathModifierAccessor;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * An API for project's classpaths modification.
 * An client can use this interface to add/remove classpath element (folder, archive, library, subproject)
 * to/from the project's classpath. Not all operations on all project's classpath are supported, if the project
 * type does not support a modification of a given classpath the UnsupportedOperationException is thrown.
 * @since org.netbeans.modules.java.project/1 1.10
 */
public class ProjectClassPathModifier {
    
    
    
    private ProjectClassPathModifier () {
        
    }        
    
    /**
     * Adds libraries into the project's classpath if the
     * libraries are not already included.
     * @param libraries to be added
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, @see ClassPath
     * @return true in case the classpath was changed (at least one library was added to the classpath),
     * the value false is returned when all the libraries are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a library to the classpath of the given type.
     */
    @SuppressWarnings("deprecation")    //NOI18N
    public static  boolean addLibraries (final Library[] libraries, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible != null) {            
            if (extensible.pcmi != null) {
                assert extensible.sg != null;
                assert extensible.classPathType != null;
                return ProjectClassPathModifierAccessor.INSTANCE.addLibraries (libraries, extensible.pcmi, extensible.sg, extensible.classPathType);
            }
            else if (extensible.pcpe != null) {
                boolean result = false;
                for (int i=0; i< libraries.length; i++) {
                    result |= extensible.pcpe.addLibrary (libraries[i]);
                }
                return result;
            }
        }
        throw new UnsupportedOperationException("No PCPE/PCPMI found for " + classPathType + " in " + projectArtifact); // NOI18N
    }
    
    
    /**
     * Removes libraries from the project's classpath if the
     * libraries are included on it.
     * @param libraries to be removed
     * @param projectArtifact a file from whose classpath the libraries should be removed
     * @param classPathType the type of classpath, @see ClassPath
     * @return true in case the classpath was changed, (at least one library was removed from the classpath),
     * the value false is returned when none of the libraries was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a library from the classpath of the given type.
     */
    public static boolean removeLibraries (final Library[] libraries, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible != null && extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.removeLibraries (libraries, extensible.pcmi, extensible.sg, extensible.classPathType);
        }
        throw new UnsupportedOperationException ();
    }
    
    /**
     * Adds archive files or folders into the project's classpath if the
     * entries are not already there.
     * @param classPathRoots roots to be added, each root has to be either a root of an archive or a folder url can be absolute or relative
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, @see ClassPath
     * @return true in case the classpath was changed, (at least one classpath root was added to the classpath),
     * the value false is returned when all the classpath roots are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a root to the classpath of the given type.
     */
    @SuppressWarnings("deprecation")        //NOI18N
    public static boolean addRoots (final URL[] classPathRoots, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible(projectArtifact, classPathType);
        if (extensible != null) {
            if (extensible.pcmi != null) {
                assert extensible.sg != null;
                assert extensible.classPathType != null;
                return ProjectClassPathModifierAccessor.INSTANCE.addRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
            }
            else if (extensible.pcpe != null) {
                boolean result = false;
                final Project project = FileOwnerQuery.getOwner(projectArtifact);
                final File projectFolderFile = FileUtil.toFile(project.getProjectDirectory());
                for (int i=0; i< classPathRoots.length; i++) {
                    URL urlToAdd = classPathRoots[i];
                    if ("jar".equals(urlToAdd.getProtocol())) {
                        urlToAdd = FileUtil.getArchiveFile (urlToAdd);
                    }
                    final FileObject fo;
                    if (LibrariesSupport.isAbsoluteURL(urlToAdd)) {
                        fo = URLMapper.findFileObject(urlToAdd);
                    } else {
                        File f = PropertyUtils.resolveFile(projectFolderFile, LibrariesSupport.convertURLToFilePath(urlToAdd));
                        fo = FileUtil.toFileObject(f);
                    }
                    if (fo == null) {
                        throw new UnsupportedOperationException ("Adding of a non existent root is not supported by project.");  //NOI18N
                    }
                    result |= extensible.pcpe.addArchiveFile (fo);
                }
                return result;
            }
        }
        throw new UnsupportedOperationException ();
    }
    
    /**
     * Removes archive files or folders from the project's classpath if the
     * entries are included on it.
     * @param classPathRoots roots to be removed, each root has to be either a root of an archive or a folder
     * @param projectArtifact a file from whose classpath the roots should be removed
     * @param classPathType the type of classpath, @see ClassPath
     * @return true in case the classpath was changed, (at least one classpath root was removed from the classpath),
     * the value false is returned when none of the classpath roots was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a root from the classpath of the given type.
     */
    public static boolean removeRoots (final URL[] classPathRoots, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible != null && extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.removeRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
        }
        throw new UnsupportedOperationException ();
    }
    
    /**
     * Adds artifacts (e.g. subprojects) into project's classpath if the
     * artifacts are not already on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts. 
     * (must be owned by the artifact and be relative to it)
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, @see ClassPath
     * @return true in case the classpath was changed, (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of an artifact to the classpath of the given type.
     */
    @SuppressWarnings("deprecation")        //NOI18N
    public static boolean addAntArtifacts (final AntArtifact[] artifacts, final URI[] artifactElements,
            final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible != null) {
            assert artifacts.length == artifactElements.length;
            if (extensible.pcmi != null) {
                assert extensible.sg != null;
                assert extensible.classPathType != null;
                return ProjectClassPathModifierAccessor.INSTANCE.addAntArtifacts (artifacts, artifactElements, extensible.pcmi, extensible.sg, extensible.classPathType);
            }
            else if (extensible.pcpe != null) {
                boolean result = false;
                for (int i=0; i< artifacts.length; i++) {
                    result |= extensible.pcpe.addAntArtifact (artifacts[i], artifactElements[i]);
                }
                return result;
            }
        }
        throw new UnsupportedOperationException ();
    }
    
    /**
     * Removes artifacts (e.g. subprojects) from project's classpath if the
     * artifacts are included on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param projectArtifact a file from whose classpath the dependent projects should be removed
     * @param classPathType the type of classpath, @see ClassPath
     * @return true in case the classpath was changed, (at least one artifact was removed from the classpath),
     * the value false is returned when none of the artifacts was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of an artifact from the classpath of the given type.
     */
    public static boolean removeAntArtifacts (final AntArtifact[] artifacts, final URI[] artifactElements,
            final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible != null && extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.removeAntArtifacts (artifacts, artifactElements, extensible.pcmi, extensible.sg, extensible.classPathType);
        }
        throw new UnsupportedOperationException ();
    }
    

    /**
     * Returns {@link ProjectClassPathModifier#Extensible} for given project artifact and classpath type. 
     * An Extensible implies a classpath to be extended. Different project type may provide different types
     * of Extensible.
     * @param projectArtifact a file owned by SourceGroup whose classpath should be changed
     * @param classPathType a classpath type, @see ClassPath
     * @return an  Extensible or null. In case when the project supports the {@link ProjectClassPathModifierImplementation},
     * this interface is used to find an Extensible. If this interface is not provided, but project provides
     * the deprecated {@link ProjectClassPathExtender} interface and classpath type is {@link ClassPath@COMPILE} the 
     * single Extensible, without assigned SourceGroup, is returned.
     * In case when neither {@link ProjectClassPathModifierImplementation} nor {@link ProjectClassPathExtender}
     * is supported null is returned.
     */
    @SuppressWarnings("deprecation")        //NOI18N
    private static Extensible findExtensible (final FileObject fo, final String classPathType) {
        assert fo != null;
        assert classPathType != null;
        final Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        final ProjectClassPathModifierImplementation pm = project.getLookup().lookup(ProjectClassPathModifierImplementation.class);
        if (pm != null) {            
            final SourceGroup[] sgs = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleSourceGroups(pm);
            assert sgs != null   : "Class: " + pm.getClass() + " returned null as source groups.";    //NOI18N
            for (SourceGroup sg : sgs) {
                if ((fo == sg.getRootFolder() || FileUtil.isParentOf(sg.getRootFolder(),fo)) && sg.contains(fo)) {
                    final String[] types = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleClassPathTypes(pm,sg);
                    assert types != null : "Class: " + pm.getClass() + " returned null as classpath types.";    //NOI18N
                    for (String type : types) {
                        if (classPathType.equals(type)) {
                            return new Extensible (pm, sg, type);
                        }
                    }
                }
            }
        }
        else if (classPathType.equals(ClassPath.COMPILE)) {
            final org.netbeans.spi.java.project.classpath.ProjectClassPathExtender pe = 
                    project.getLookup().lookup(org.netbeans.spi.java.project.classpath.ProjectClassPathExtender.class);
            if (pe != null) {
                return new Extensible (pe);
            }
        }
        return null;
    }
    
    
    /**
     * Extensible represents a classpath which may be changed by the
     * {@link ProjectClassPathModifier}. It encapsulates the compilation
     * unit and class path type, @see ClassPath.
     */
    private static final class Extensible {
        
        private final String classPathType;       
        private final SourceGroup sg;
        private final ProjectClassPathModifierImplementation pcmi;
        @SuppressWarnings("deprecation")        //NOI18N
        private final org.netbeans.spi.java.project.classpath.ProjectClassPathExtender pcpe;
        
        
        private Extensible (final ProjectClassPathModifierImplementation pcmi , final SourceGroup sg, final String classPathType) {
            assert pcmi != null;
            assert sg != null;
            assert classPathType != null;
            this.pcmi = pcmi;
            this.sg = sg;
            this.classPathType = classPathType;
            this.pcpe = null;
        }
        
        @SuppressWarnings("deprecation")        //NOI18N
        private Extensible (final org.netbeans.spi.java.project.classpath.ProjectClassPathExtender pcpe) {
            assert pcpe != null;
            this.pcpe = pcpe;
            this.pcmi = null;
            this.sg = null;
            this.classPathType = ClassPath.COMPILE;
        }
    }    

}
