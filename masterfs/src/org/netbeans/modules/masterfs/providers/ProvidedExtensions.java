/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    
    public interface IOHandler {
        /**
         * @throws java.io.IOException if handled operation isn't successful
         */
        void handle() throws IOException;
    }
        
    public void createSuccess(FileObject fo) {}    
    public void createFailure(FileObject parent, String name, boolean isFolder) {}   
    public void beforeCreate(FileObject parent, String name, boolean isFolder) {}    
    public void deleteSuccess(FileObject fo) {}    
    public void deleteFailure(FileObject fo) {}
    public void beforeDelete(FileObject fo) {}
}
