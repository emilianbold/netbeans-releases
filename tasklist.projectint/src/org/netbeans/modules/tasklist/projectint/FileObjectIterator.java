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

package org.netbeans.modules.tasklist.projectint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Iterates all files and sub-folders under the given root folders.
 * 
 * @author S. Aubrecht
 */
class FileObjectIterator implements Iterator<FileObject> {
    
    private Collection<FileObject> roots;
    private Collection<FileObject> editedFiles;
    
    private Iterator<FileObject> rootsIterator;
    private Iterator<FileObject> editedFilesIterator;
    private Enumeration<? extends FileObject> rootChildrenEnum;
    
    /** Creates a new instance of FileObjectIterator */
    public FileObjectIterator( Collection<FileObject> roots, Collection<FileObject> editedFiles ) {
        this.roots = roots;
        this.editedFiles = editedFiles;
    }
    
    public boolean hasNext() {
        if( null == rootsIterator ) {
            checkEditedFiles();
            rootsIterator = roots.iterator();
            return rootsIterator.hasNext();
        }
        return (null != rootChildrenEnum && rootChildrenEnum.hasMoreElements()) 
                || rootsIterator.hasNext() 
                || editedFilesIterator.hasNext();
    }
    
    public FileObject next() {
        //make sure opened files are scanned first
        if( editedFilesIterator.hasNext() ) {
            return editedFilesIterator.next();
        }
        
        FileObject result = null;
        if( null == rootChildrenEnum || !rootChildrenEnum.hasMoreElements() ) {
            if( rootsIterator.hasNext() ) {
                result = rootsIterator.next();
                rootChildrenEnum = result.getChildren( true );
            } else {
                throw new NoSuchElementException();
            }
        } else {
            result = rootChildrenEnum.nextElement();
        }
        return result;
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    private void checkEditedFiles() {
        if( null != editedFiles ) {
            ArrayList<FileObject> editedFilesUnderRoots = new ArrayList<FileObject>( editedFiles.size() );
            for( FileObject fo : editedFiles ) {
                if( isUnderRoots( fo ) ) {
                    editedFilesUnderRoots.add( fo );
                }
            }
            editedFiles = null;
            editedFilesIterator = editedFilesUnderRoots.iterator();
        } else {
            editedFilesIterator = new EmptyIterator();
        }
    }
    
    private boolean isUnderRoots( FileObject fo ) {
        for( FileObject root : roots ) {
            if( FileUtil.isParentOf( root, fo ) )
                return true;
        }
        return false;
    }
}
