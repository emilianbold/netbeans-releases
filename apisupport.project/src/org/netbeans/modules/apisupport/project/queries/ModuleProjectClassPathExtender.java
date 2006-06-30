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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Makes sure you can safely use natural layout in forms you develop for your module.
 * @author Jesse Glick
 * @see "#62942"
 */
public final class ModuleProjectClassPathExtender implements ProjectClassPathExtender {
    
    private static final String LIBRARY_NAME = "swing-layout"; // NOI18N
    private static final String MODULE_NAME = "org.jdesktop.layout"; // NOI18N
    
    private final NbModuleProject project;
    
    public ModuleProjectClassPathExtender(NbModuleProject project) {
        this.project = project;
    }
    
    public boolean addLibrary(Library library) throws IOException {
        boolean cpChanged = false;
        if (library.getName().equals(LIBRARY_NAME)) {
            ModuleEntry entry = project.getModuleList().getEntry(MODULE_NAME);
            if (entry != null) {
                cpChanged = Util.addDependency(project, MODULE_NAME);
            } else {
                IOException e = new IOException("no module " + MODULE_NAME); // NOI18N
                Util.err.annotate(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_could_not_find_module", MODULE_NAME));
                throw e;
            }
        } else {
            IOException e = new IOException("unknown lib " + library.getName()); // NOI18N
            Util.err.annotate(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_unsupported_library", library.getDisplayName()));
            throw e;
        }
        if (cpChanged) {
            ProjectManager.getDefault().saveProject(project);
        }
        return cpChanged;
    }
    
    public boolean addArchiveFile(FileObject archiveFile) throws IOException {
        IOException e = new IOException("not implemented: " + archiveFile); // NOI18N
        Util.err.annotate(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_jar", FileUtil.getFileDisplayName(archiveFile)));
        throw e;
    }
    
    public boolean addAntArtifact(AntArtifact artifact, URI artifactElement) throws IOException {
        // XXX ideally would check to see if it was owned by a NBM project in this universe...
        IOException e = new IOException("not implemented: " + artifactElement); // NOI18N
        String displayName;
        if ("file".equals(artifactElement.getScheme())) { // NOI18N
            displayName = new File(artifactElement).getAbsolutePath();
        } else {
            displayName = artifactElement.toString();
        }
        Util.err.annotate(e, NbBundle.getMessage(ModuleProjectClassPathExtender.class, "ERR_jar", displayName));
        throw e;
    }
    
}
