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
