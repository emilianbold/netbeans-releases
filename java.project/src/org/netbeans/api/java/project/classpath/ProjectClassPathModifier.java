/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.openide.util.Parameters;

/**
 * An API for project's classpaths modification.
 * An client can use this interface to add/remove classpath element (folder, archive, library, subproject)
 * to/from the project's classpath. Not all operations on all project's classpath are supported, if the project
 * type does not support a modification of a given classpath the UnsupportedOperationException is thrown.
 * @since org.netbeans.modules.java.project/1 1.10
 */
public class ProjectClassPathModifier {
    
    private ProjectClassPathModifier() {}
    
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
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.addLibraries (libraries, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            boolean result = false;
            for (Library library : libraries) {
                result |= extensible.pcpe.addLibrary(library);
            }
            return result;
        }
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
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.removeLibraries (libraries, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            throw new UnsupportedOperationException("Cannot remove libraries using " + extensible); // NOI18N
        }
    }
    
    /**
     * Adds archive files or folders into the project's classpath if the
     * entries are not already there.
     * @param classPathRoots roots to be added, each root has to be either a root of an archive or a folder url
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
        Parameters.notNull("classPathRoots", classPathRoots);
        final Extensible extensible = findExtensible(projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.addRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            boolean result = false;
            for (URL urlToAdd : classPathRoots) {
                Parameters.notNull("classPathRoots", urlToAdd);
                if ("jar".equals(urlToAdd.getProtocol())) {
                    urlToAdd = FileUtil.getArchiveFile (urlToAdd);
                }
                final FileObject fo = URLMapper.findFileObject(urlToAdd);
                if (fo == null) {
                    throw new UnsupportedOperationException ("Adding of a non existent root is not supported by project.");  //NOI18N
                }
                result |= extensible.pcpe.addArchiveFile (fo);
            }
            return result;
        }
    }
    
    /**
     * Adds archive files or folders into the project's classpath if the
     * entries are not already there.
     * @param classPathRoots roots to be added, each root has to be either a root of an archive or a folder url; URI can be relative
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, @see ClassPath
     * @return true in case the classpath was changed, (at least one classpath root was added to the classpath),
     * the value false is returned when all the classpath roots are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of a root to the classpath of the given type.
     * @since org.netbeans.modules.java.project/1 1.16
     */
    @SuppressWarnings("deprecation")
    public static boolean addRoots (final URI[] classPathRoots, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible(projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.addRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            boolean result = false;
            final Project project = FileOwnerQuery.getOwner(projectArtifact);
            final File projectFolderFile = FileUtil.toFile(project.getProjectDirectory());
            for (URI uri : classPathRoots) {
                URI urlToAdd = LibrariesSupport.getArchiveFile(uri);
                if (urlToAdd == null) {
                    urlToAdd = uri;
                }
                final FileObject fo;
                if (urlToAdd.isAbsolute()) {
                    fo = FileUtil.toFileObject(new File(urlToAdd));
                } else {
                    File f = PropertyUtils.resolveFile(projectFolderFile, LibrariesSupport.convertURIToFilePath(urlToAdd));
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
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.removeRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            throw new UnsupportedOperationException("Cannot remove roots from " + extensible); // NOI18N
        }
    }
    
    /**
     * Removes archive files or folders from the project's classpath if the
     * entries are included on it.
     * @param classPathRoots roots to be removed, each root has to be either a root of an archive or a folder; URI can be relative
     * @param projectArtifact a file from whose classpath the roots should be removed
     * @param classPathType the type of classpath, @see ClassPath
     * @return true in case the classpath was changed, (at least one classpath root was removed from the classpath),
     * the value false is returned when none of the classpath roots was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of a root from the classpath of the given type.
     * @since org.netbeans.modules.java.project/1 1.16
     */
    public static boolean removeRoots (final URI[] classPathRoots, final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.removeRoots (classPathRoots, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            throw new UnsupportedOperationException("Cannot remove roots from " + extensible); // NOI18N
        }
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
        assert artifacts.length == artifactElements.length;
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.addAntArtifacts (artifacts, artifactElements, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            boolean result = false;
            for (int i=0; i< artifacts.length; i++) {
                result |= extensible.pcpe.addAntArtifact (artifacts[i], artifactElements[i]);
            }
            return result;
        }
    }

    /**
     * Adds projects into project's classpath if the
     * artifacts are not already on it.
     * <p>
     *  It's not guaranteed that the source and target project will connect in cases when each is of different class of project. Eg.
     * Ant-based vs Maven project types. A way to check is to attempt to retrieve AntArtifact from the source and target projects..
     *
     * @param projects to be added
     * @param projectArtifact a file whose classpath should be extended
     * @param classPathType the type of classpath to be extended, see {@link org.netbeans.api.java.classpath.ClassPath}
     * @return true in case the classpath was changed, (at least one artifact was added to the classpath),
     * the value false is returned when all the artifacts are already included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * adding of an artifact to the classpath of the given type.
     * @since org.netbeans.modules.java.project/1 1.24
     */
    public static boolean addProjects (final Project[] projects,
            final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.addProjects (projects, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            throw new UnsupportedOperationException("Cannot add project as dependency. Missing ProjectClassPathModifierImplementation service in project type.");
        }
    }
    
    /**
     * Removes artifacts (e.g. subprojects) from project's classpath if the
     * artifacts are included on it.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param projectArtifact a file from whose classpath the dependent projects should be removed
     * @param classPathType the type of classpath, {@link  org.netbeans.api.java.classpath.ClassPath}
     * @return true in case the classpath was changed, (at least one artifact was removed from the classpath),
     * the value false is returned when none of the artifacts was included on the classpath.
     * @exception IOException in case the project metadata cannot be changed
     * @exception UnsupportedOperationException is thrown when the project does not support
     * removing of an artifact from the classpath of the given type.
     */
    public static boolean removeAntArtifacts (final AntArtifact[] artifacts, final URI[] artifactElements,
            final FileObject projectArtifact, final String classPathType) throws IOException, UnsupportedOperationException {
        final Extensible extensible = findExtensible (projectArtifact, classPathType);
        if (extensible.pcmi != null) {
            assert extensible.sg != null;
            assert extensible.classPathType != null;
            return ProjectClassPathModifierAccessor.INSTANCE.removeAntArtifacts (artifacts, artifactElements, extensible.pcmi, extensible.sg, extensible.classPathType);
        } else {
            throw new UnsupportedOperationException("Cannot remove artifacts from " + extensible); // NOI18N
        }
    }

    /**
     * Returns {@link ProjectClassPathModifier#Extensible} for given project artifact and classpath type. 
     * An Extensible implies a classpath to be extended. Different project type may provide different types
     * of Extensible.
     * @param projectArtifact a file owned by SourceGroup whose classpath should be changed
     * @param classPathType a classpath type, @see ClassPath
     * @return an Extensible. In case when the project supports the {@link ProjectClassPathModifierImplementation},
     * this interface is used to find an Extensible. If this interface is not provided, but project provides
     * the deprecated {@link org.netbeans.spi.java.project.classpath.ProjectClassPathExtender} interface and classpath type is {@link ClassPath@COMPILE} the
     * single Extensible, without assigned SourceGroup, is returned.
     * @throws UnsupportedOperationException In case when neither {@link ProjectClassPathModifierImplementation} nor
     *                                       {@link org.netbeans.spi.java.project.classpath.ProjectClassPathExtender}
     * is supported, or no project can be associated with the project artifact.
     */
    @SuppressWarnings("deprecation")        //NOI18N
    private static Extensible findExtensible(final FileObject projectArtifact, final String classPathType) throws UnsupportedOperationException {
        assert projectArtifact != null;
        assert classPathType != null;
        final Project project = FileOwnerQuery.getOwner(projectArtifact);
        if (project == null) {
            throw new UnsupportedOperationException("No project found to correspond to " + FileUtil.getFileDisplayName(projectArtifact)); // NOI18N
        }
        final ProjectClassPathModifierImplementation pm = project.getLookup().lookup(ProjectClassPathModifierImplementation.class);
        if (pm != null) {            
            final SourceGroup[] sgs = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleSourceGroups(pm);
            assert sgs != null   : "Class: " + pm.getClass() + " returned null as source groups.";    //NOI18N
            for (SourceGroup sg : sgs) {
                if ((projectArtifact == sg.getRootFolder() || FileUtil.isParentOf(sg.getRootFolder(),projectArtifact)) && sg.contains(projectArtifact)) {
                    final String[] types = ProjectClassPathModifierAccessor.INSTANCE.getExtensibleClassPathTypes(pm,sg);
                    assert types != null : "Class: " + pm.getClass() + " returned null as classpath types.";    //NOI18N
                    for (String type : types) {
                        if (classPathType.equals(type)) {
                            String label = "ProjectClassPathModifierImplementation for " + classPathType + " on " + FileUtil.getFileDisplayName(sg.getRootFolder()); // NOI18N
                            return new Extensible(pm, sg, type,label);
                        }
                    }
                }
            }
            throw new UnsupportedOperationException("Project in " + FileUtil.getFileDisplayName(project.getProjectDirectory()) + " of " + project.getClass() +
                    " has a ProjectClassPathModifierImplementation but it will not handle " + classPathType + " for " + FileUtil.getFileDisplayName(projectArtifact) +
                    " extensible source groups: " + sourceGroupsToString(sgs)); // NOI18N
        } else {
            final org.netbeans.spi.java.project.classpath.ProjectClassPathExtender pe =
                    project.getLookup().lookup(org.netbeans.spi.java.project.classpath.ProjectClassPathExtender.class);
            if (pe != null) {
                if (classPathType.equals(ClassPath.COMPILE)) {
                    return new Extensible(pe, "ProjectClassPathExtender for " + FileUtil.getFileDisplayName(project.getProjectDirectory())); // NOI18N
                } else {
                    throw new UnsupportedOperationException("Project in " + FileUtil.getFileDisplayName(project.getProjectDirectory()) + " of " + project.getClass() +
                            " has a ProjectClassPathExtender in its lookup but no ProjectClassPathModifierImplementation to handle " + classPathType); // NOI18N
                }
            } else {
                throw new UnsupportedOperationException("Project in " + FileUtil.getFileDisplayName(project.getProjectDirectory()) + " of " + project.getClass() +
                        " has neither a ProjectClassPathModifierImplementation nor a ProjectClassPathExtender in its lookup"); // NOI18N
            }
        }
    }

    private static String sourceGroupsToString(final SourceGroup[] sgs) {
        final StringBuilder sb = new StringBuilder();
        for(SourceGroup sg : sgs) {
            if (sb.length()!=0) {
                sb.append(':'); //NOI18N
            }
            sb.append(FileUtil.getFileDisplayName(sg.getRootFolder()));
        }
        return sb.toString();
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
        /** for error messages only */
        private final String label;
        
        private Extensible(final ProjectClassPathModifierImplementation pcmi, final SourceGroup sg, final String classPathType, String label) {
            assert pcmi != null;
            assert sg != null;
            assert classPathType != null;
            this.pcmi = pcmi;
            this.sg = sg;
            this.classPathType = classPathType;
            this.pcpe = null;
            this.label = label;
        }
        
        @SuppressWarnings("deprecation")        //NOI18N
        private Extensible(final org.netbeans.spi.java.project.classpath.ProjectClassPathExtender pcpe, String label) {
            assert pcpe != null;
            this.pcpe = pcpe;
            this.pcmi = null;
            this.sg = null;
            this.classPathType = ClassPath.COMPILE;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

    }

}
