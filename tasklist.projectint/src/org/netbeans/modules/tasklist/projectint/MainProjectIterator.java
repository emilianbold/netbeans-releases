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

package org.netbeans.modules.tasklist.projectint;

import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;

/**
 * Iterate all resources (files and folders) that are under the current main project
 * and under opened projects that depend on the main one.
 * The iretator is empty when no project has been set as the main one.
 * 
 * @author S. Aubrecht
 */
class MainProjectIterator implements Iterator<FileObject> {
    
    private Iterator<FileObject> iterator;
    
    /** Creates a new instance of MainProjectIterator */
    public MainProjectIterator() {
    }
    
    public boolean hasNext() {
        initialize();
        return iterator.hasNext();
    }

    public FileObject next() {
        initialize();
        return iterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void initialize() {
        if( null == iterator ) {
            iterator = createIterator();
        }
    }
    
    protected Iterator<FileObject> createIterator() {
        Project mainProject = OpenProjects.getDefault().getMainProject();
        if( null == mainProject ) {
            return new EmptyIterator();
        }
        
        FileObjectIterator it = new FileObjectIterator();
        
        addProject( mainProject, it );
        
        addDependantProjects( mainProject, it );
        
        return it;
    }
    
    private void addDependantProjects( Project mainProject, FileObjectIterator iterator ) {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for( int i=0; i<projects.length; i++ ) {
            if( projects[i].equals( mainProject ) )
                continue;
            
            SubprojectProvider subProjectProvider = projects[i].getLookup().lookup( SubprojectProvider.class );
            if( null != subProjectProvider && subProjectProvider.getSubprojects().contains( mainProject ) ) {
                addProject( projects[i], iterator );
            }
        }
    }
    
    private void addProject( Project p, FileObjectIterator it ) {
        Sources sources = ProjectUtils.getSources( p );
        SourceGroup[] groups = sources.getSourceGroups( Sources.TYPE_GENERIC );
        for( SourceGroup group : groups ) {
            FileObject rootFolder = group.getRootFolder();
            it.addRoot( rootFolder );
        }
    }
}
