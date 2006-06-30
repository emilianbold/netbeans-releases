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

package org.netbeans.modules.java.project.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.openide.ErrorManager;

/**
 *
 * @author tom
 */
public abstract class ProjectClassPathModifierAccessor {

    public static ProjectClassPathModifierAccessor INSTANCE;
    
    static {
        Class c = ProjectClassPathModifierImplementation.class;
        try {
            Class.forName (c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /** Creates a new instance of ProjectClassPathModifierAccessor */
    public ProjectClassPathModifierAccessor() {
    }
    
    public abstract SourceGroup[] getExtensibleSourceGroups (ProjectClassPathModifierImplementation m);
    
    public abstract String[] getExtensibleClassPathTypes (ProjectClassPathModifierImplementation m, SourceGroup sg);
    
    public abstract boolean addLibraries (Library[] libraries, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
                
    public abstract boolean removeLibraries (Library[] libraries, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
        
    public abstract boolean addRoots (URL[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
       
    public abstract boolean removeRoots (URL[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    public abstract boolean addAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;

    public abstract boolean removeAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
}
