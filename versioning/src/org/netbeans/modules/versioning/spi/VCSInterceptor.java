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
package org.netbeans.modules.versioning.spi;

import java.io.File;
import java.io.IOException;

/**
 * Versioning systems that need to intercept or listen to file system operations implement this class.  
 * 
 * @author Maros Sandor
 */
public abstract class VCSInterceptor {

    // ==================================================================================================
    // DELETE
    // ==================================================================================================
    
    /**
     * Notifies the interceptor that the file or folder is about to be deleted. The interceptor MUST NOT delete
     * the file here.
     * 
     * @param file file to be deleted
     * @return true if this interceptor wants to handle this operation (doDelete will be called), false otherwise
     */
    public boolean beforeDelete(File file) {
        return false;
    }

    /**
     * Called if beforeDelete() returns true and delegates the delete operation to this interceptor. The interceptor
     * may decide to either delete the file or leave it intact. In case it does not want to delete the file, it should
     * just return without doing anything.
     * 
     * @param file a file or an empty folder to delete
     * @throws IOException if the delete operation failed
     */
    public void doDelete(File file) throws IOException {
    }

    /**
     * Called after a file or folder is deleted. In case the file was deleted outside IDE, this is the only method called.
     * 
     * @param file deleted file
     */
    public void afterDelete(File file) {
    }
    
    // ==================================================================================================
    // MOVE
    // ==================================================================================================
    
    /**
     * Notifies the interceptor that the file or folder is about to be moved. The interceptor MUST NOT move
     * the file here.
     * 
     * @param from the file to be moved
     * @param to destination of the file being moved
     * @return true if this interceptor wants to handle this operation (doMove will be called), false otherwise
     */
    public boolean beforeMove(File from, File to) {
        return false;
    }

    /**
     * Called if beforeMove() returns true and delegates the move operation to this interceptor.
     * 
     * @param from the file to be moved
     * @param to destination of the file being moved
     * @throws IOException if the move operation failed
     */
    public void doMove(File from, File to) throws IOException {
    }

    /**
     * Called after a file or folder has beed moved. In case the file was moved outside IDE, this method is not called but 
     * a pair or afterDelete() / afterCreate() is called instead.
     * 
     * @param from original location of the file
     * @param to current location of the file
     */
    public void afterMove(File from, File to) {
    }
    
    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    /**
     * Notifies the interceptor that the file or folder is about to be created. The interceptor MUST NOT create
     * the file here.
     * 
     * Beware: It may happen on some filesystems that the file will be ALREADY created. If so, returning true from this method has no effect and
     * doCreate will NOT be called.
     * 
     * @param file file or folder to be created
     * @return true if this interceptor wants to handle this operation (doCreate will be called), false otherwise
     */
    public boolean beforeCreate(File file, boolean isDirectory) {
        return false;
    }

    /**
     * Called if beforeCreate() returns true and delegates the create operation to this interceptor.
     * 
     * @param file the file to create
     * @param isDirectory true if the new file should be a directory, false otherwise
     * @throws IOException if the create operation failed
     */
    public void doCreate(File file, boolean isDirectory) throws IOException {
    }

    /**
     * Called after a new file or folder has beed created. In case the file was created outside IDE, this is the only
     * method called.
     * 
     * @param file the new file
     */
    public void afterCreate(File file) {
    }
    
    // ==================================================================================================
    // CHANGE
    // ==================================================================================================

    /**
     * Called after a file changed.
     * 
     * @param file changed file
     */
    public void afterChange(File file) {
    }
    
    /**
     * Called before a file is changed.
     * 
     * @param file to be changed file
     */
    public void beforeChange(File file) {
    }
    
}
