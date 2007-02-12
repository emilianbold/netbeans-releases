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

package org.netbeans.modules.masterfs.providers;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Encapsulate a group of individual factory methods that are responsible for creating objects
 * of specific interfaces. If subclassed and provided by
 * {@link AnnotationProvider#getInterceptionListener} then
 * individual instances will be called by <code>MasterFileSystem</code>
 * There may exist more than one instance of <code>ProvidedExtensions</code>
 * at a given moment and therefore there is defined for
 * every method wheter will be called by <code>MasterFileSystem</code>
 * for every present instance or just for the first one.
 *
 * @see ProvidedExtensions.IOHandler
 * @see InterceptionListener
 *
 * @author Radek Matous
 */
public class ProvidedExtensions implements InterceptionListener {
    /**
     * Return instance of {@link ProvidedExtensions.IOHandler}
     * that is responsible for moving the file or null.
     *
     * Just the first non null instance of <code>IOHandler</code> is used by
     *  <code>MasterFileSystem</code>
     *
     * @param from file to be moved
     * @param to target to move this file to
     * @return instance of {@link ProvidedExtensions.IOHandler} 
     * that is responsible for moving the file or null
     */
    public ProvidedExtensions.IOHandler getMoveHandler(
            File from, File to) {
        return null;
    }
    
    /*
     * Return instance of {@link ProvidedExtensions.IOHandler}
     * that is responsible for renaming the file or null.
     *
     * Just the first non null instance of <code>IOHandler</code> is used by
     *  <code>MasterFileSystem</code>
     *
     * @param from file to be renamed
     * @param newName new name of file
     * @return instance of {@link ProvidedExtensions.IOHandler} 
     * that is responsible for renaming the file or null
     */
    public ProvidedExtensions.IOHandler getRenameHandler(
            File from, String newName) {
        return null;
    }

    /*
     * Return instance of {@link ProvidedExtensions.DeleteHandler}
     * that is responsible for deleting the file or null.
     *
     * Just the first non null instance of <code>DeleteHandler</code> is used by
     *  <code>MasterFileSystem</code>
     *
     * @param f file or folder to be deleted
     * @return instance of {@link ProvidedExtensions.DeleteHandler} 
     * that is responsible for deleting the file or null
     */    
    public ProvidedExtensions.DeleteHandler getDeleteHandler(File f) {
        return null;
    }
    
    
    public interface IOHandler {
        /**
         * @throws java.io.IOException if handled operation isn't successful
         */
        void handle() throws IOException;
    }
    
    public interface DeleteHandler {
        /**
         * Deletes the file or directory denoted by this abstract pathname.  If
         * this pathname denotes a directory, then the directory must be empty in
         * order to be deleted.
         *
         * @return  <code>true</code> if and only if the file or directory is
         *          successfully deleted; <code>false</code> otherwise
         */
        boolean delete(File file);
    }
    
        
    public void createSuccess(FileObject fo) {}    
    public void createFailure(FileObject parent, String name, boolean isFolder) {}   
    public void beforeCreate(FileObject parent, String name, boolean isFolder) {}    
    public void deleteSuccess(FileObject fo) {}    
    public void deleteFailure(FileObject fo) {}
    public void beforeDelete(FileObject fo) {}


    /*
     * Called by <code>MasterFileSystem</code> when <code>FileObject</code>
     * is going to be modified by asking for <code>OutputStream<code>
     * @see org.openide.filesystems.FileObject#getOutputStream
     * @param fo file which is going to be notified
     * @since 1.10
     */        
    public void beforeChange(FileObject fo) {}    
}
