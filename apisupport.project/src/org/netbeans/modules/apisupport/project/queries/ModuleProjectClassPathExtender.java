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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Makes sure you can safely use natural layout in forms you develop for your module.
 * @author Jesse Glick
 * @see "#62942"
 */
public final class ModuleProjectClassPathExtender extends ProjectClassPathModifierImplementation {

    private static final String LIBRARY_NAME = "swing-layout"; // NOI18N
    private static final String MODULE_NAME = "org.jdesktop.layout"; // NOI18N

    private final NbModuleProject project;

    /**
     * Create new extender.
     * @param project a module project
     */
    public ModuleProjectClassPathExtender(NbModuleProject project) {
        this.project = project;
    }

    protected SourceGroup[] getExtensibleSourceGroups() {
        for (SourceGroup g : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (g.getRootFolder() == project.getSourceDirectory()) {
                return new SourceGroup[] {g};
            }
        }
        return new SourceGroup[0];
    }

    protected String[] getExtensibleClassPathTypes(SourceGroup sourceGroup) {
        return new String[] {ClassPath.COMPILE};
    }

    protected boolean addLibraries(Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        boolean cpChanged = false;
        if (libraries.length == 0) {
            return false;
        }
        for (Library library : libraries) {
            if (!library.getName().equals(LIBRARY_NAME)) {
                IOException e = new IOException("unknown lib " + library.getName()); // NOI18N
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_unsupported_library", library.getDisplayName()));
                throw e;
            }
        }
        for (Library library : libraries) {
            ModuleEntry entry = project.getModuleList().getEntry(MODULE_NAME);
            if (entry != null) {
                cpChanged = Util.addDependency(project, MODULE_NAME);
            } else {
                IOException e = new IOException("no module " + MODULE_NAME); // NOI18N
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_could_not_find_module", MODULE_NAME));
                throw e;
            }
        }
        if (cpChanged) {
            ProjectManager.getDefault().saveProject(project);
        }
        return cpChanged;
    }

    protected boolean removeLibraries(Library[] libraries, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException(); // XXX could be supported
    }

    protected boolean addRoots(URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        UnsupportedOperationException e = new UnsupportedOperationException("not implemented: " + Arrays.asList(classPathRoots)); // NOI18N
        // XXX handle >1 args
        String displayName = classPathRoots.length > 0 ? classPathRoots[0].toString() : "<nothing>";
        if (classPathRoots.length > 0 && "file".equals(classPathRoots[0].getProtocol())) { // NOI18N
            try {
                displayName = new File(classPathRoots[0].toURI()).getAbsolutePath();
            } catch (URISyntaxException x) {
                assert false : x;
            }
        }
        Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_jar", displayName));
        throw e;
    }

    protected boolean removeRoots(URL[] classPathRoots, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    protected boolean addAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        // XXX ideally would check to see if it was owned by a NBM project in this universe...
        UnsupportedOperationException e = new UnsupportedOperationException("not implemented: " + Arrays.asList(artifactElements)); // NOI18N
        // XXX handle >1 args
        String displayName = artifactElements.length > 0 ? artifactElements[0].toString() : "<nothing>";
        if (artifactElements.length > 0 && "file".equals(artifactElements[0].getScheme())) { // NOI18N
            displayName = new File(artifactElements[0]).getAbsolutePath();
        }
        Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_jar", displayName));
        throw e;
    }

    protected boolean removeAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

}
