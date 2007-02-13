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

package org.netbeans.spi.project.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DataFilesProviderImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.openide.filesystems.FileObject;

/**
 * Allows gathering information for various project operations.
 *
 * @author Jan Lahoda
 * @since 1.7
 */
public final class ProjectOperations {
    
    private ProjectOperations() {
    }
    
    /**Return list of files that are considered metadata files and folders for the given project.
     * Returns meaningful values only if some of the <code>is*Supported</code> methods
     * return <code>true</code>.
     *
     * @param prj project to test
     * @return list of metadata files/folders
     */
    public static List<FileObject> getMetadataFiles(Project prj) {
        List<FileObject> result = new ArrayList<FileObject>();
        
        for (DataFilesProviderImplementation i : prj.getLookup().lookupAll(DataFilesProviderImplementation.class)) {
            result.addAll(i.getMetadataFiles());
            assert !result.contains(null) : "Nulls in " + result + " from " + i;
        }
        
        return result;
    }
            
    /**Return list of files that are considered source files and folders for the given project.
     * Returns meaningful values only if some of the <code>is*Supported</code> methods
     * return <code>true</code>.
     *
     * @param prj project to test
     * @return list of data files/folders
     */
    public static List<FileObject> getDataFiles(Project prj) {
        List<FileObject> result = new ArrayList<FileObject>();
        
        for (DataFilesProviderImplementation i : prj.getLookup().lookupAll(DataFilesProviderImplementation.class)) {
            result.addAll(i.getDataFiles());
            assert !result.contains(null) : "Nulls in " + result + " from " + i;
        }
        
        return result;
    }
    
    /**Test whether the delete operation is supported on the given project.
     * 
     * @param prj project to test
     * @return <code>true</code> if the project supports delete operation,
     *         <code>false</code> otherwise
     */
    public static boolean isDeleteOperationSupported(Project prj) {
        return prj.getLookup().lookup(DeleteOperationImplementation.class) != null;
    }
    
    /**Notification that the project is about to be deleted.
     * Should be called immediately before the project is deleted.
     *
     * The project is supposed to do all required cleanup to allow the project to be deleted.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     */
    public static void notifyDeleting(Project prj) throws IOException {
        for (DeleteOperationImplementation i : prj.getLookup().lookupAll(DeleteOperationImplementation.class)) {
            i.notifyDeleting();
        }
    }
    
    /**Notification that the project has been deleted.
     * Should be called immediately after the project is deleted.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     */
    public static void notifyDeleted(Project prj) throws IOException {
        for (DeleteOperationImplementation i : prj.getLookup().lookupAll(DeleteOperationImplementation.class)) {
            i.notifyDeleted();
        }
    }
    
    /**Test whether the copy operation is supported on the given project.
     * 
     * @param prj project to test
     * @return <code>true</code> if the project supports the copy operation,
     *         <code>false</code> otherwise
     */
    public static boolean isCopyOperationSupported(Project prj) {
        return prj.getLookup().lookup(CopyOperationImplementation.class) != null;
    }
    
    /**Notification that the project is about to be copyied.
     * Should be called immediatelly before the project is copied.
     *
     * The project is supposed to do all required cleanup to allow the project to be copied.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     */
    public static void notifyCopying(Project prj) throws IOException {
        for (CopyOperationImplementation i : prj.getLookup().lookupAll(CopyOperationImplementation.class)) {
            i.notifyCopying();
        }
    }
    
    /**Notification that the project has been copied.
     * Should be called immediatelly after the project is copied.
     *
     * The project is supposed to do all necessary fixes to the project's structure to
     * form a valid project.
     *
     * Both original and newly created project (copy) are notified, in this order.
     *
     * @param original original project
     * @param nue      new project (copy)
     * @param originalPath the project folder of the original project (for consistency with notifyMoved)
     * @param name     new name of the project
     * @throws IOException is some error occurs
     */
    public static void notifyCopied(Project original, Project nue, File originalPath, String name) throws IOException {
        for (CopyOperationImplementation i : original.getLookup().lookupAll(CopyOperationImplementation.class)) {
            i.notifyCopied(null, originalPath, name);
        }
        for (CopyOperationImplementation i : nue.getLookup().lookupAll(CopyOperationImplementation.class)) {
            i.notifyCopied(original, originalPath, name);
        }
    }
    
    /**Notification that the project is about to be moved.
     * Should be called immediately before the project is moved.
     *
     * The project is supposed to do all required cleanup to allow the project to be moved.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     */
    public static void notifyMoving(Project prj) throws IOException {
        for (MoveOperationImplementation i : prj.getLookup().lookupAll(MoveOperationImplementation.class)) {
            i.notifyMoving();
        }
    }
    
    /**Notification that the project has been moved.
     * Should be called immediatelly after the project is moved.
     *
     * The project is supposed to do all necessary fixes to the project's structure to
     * form a valid project.
     *
     * Both original and moved project are notified, in this order.
     *
     * @param original original project
     * @param nue      moved project
     * @param originalPath the project folder of the original project
     * @param name     new name of the project
     * @throws IOException is some error occurs
     */
    public static void notifyMoved(Project original, Project nue, File originalPath, String name) throws IOException {
        for (MoveOperationImplementation i : original.getLookup().lookupAll(MoveOperationImplementation.class)) {
            i.notifyMoved(null, originalPath, name);
        }
        for (MoveOperationImplementation i : nue.getLookup().lookupAll(MoveOperationImplementation.class)) {
            i.notifyMoved(original, originalPath, name);
        }
    }
    
    /**
     * Tests whether the move or rename operations are supported on the given project.
     * 
     * @param prj project to test
     * @return <code>true</code> if the project supports the move operation,
     *         <code>false</code> otherwise
     */
    public static boolean isMoveOperationSupported(Project prj) {
        return true; // XXX ???
    }
    
}
