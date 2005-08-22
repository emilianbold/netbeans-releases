/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DataFilesProviderImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.openide.util.Lookup;

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
     * Returns meaningfull values only if some of the <code>is*Supported</code> methods
     * return <code>true</code>.
     *
     * @param prj project to test
     * @return list of metadata files/folders
     */
    public static List/*<FileObject>*/ getMetadataFiles(Project prj) {
        List/*<FileObject>*/ result = new ArrayList();
        
        for (Iterator i = getProjectsOperationsImplementation(prj).iterator(); i.hasNext(); ) {
            result.addAll(((DataFilesProviderImplementation) i.next()).getMetadataFiles());
        }
        
        return result;
    }
            
    /**Return list of files that are considered source files and folders for the given project.
     * Returns meaningfull values only if some of the <code>is*Supported</code> methods
     * return <code>true</code>.
     *
     * @param prj project to test
     * @return list of data files/folders
     */
    public static List/*<FileObject>*/ getDataFiles(Project prj) {
        List/*<FileObject>*/ result = new ArrayList();
        
        for (Iterator i = getProjectsOperationsImplementation(prj).iterator(); i.hasNext(); ) {
            result.addAll(((DataFilesProviderImplementation) i.next()).getDataFiles());
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
        return !getDeleteOperationImplementation(prj).isEmpty();
    }
    
    /**Notification that the project is about to be deleted.
     * Should be called immediatelly before the project is deleted.
     *
     * The project is supposed to do all required cleanup to allow the project to be deleted.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     */
    public static void notifyDeleting(Project prj) throws IOException {
        for (Iterator i = getDeleteOperationImplementation(prj).iterator(); i.hasNext(); ) {
            ((DeleteOperationImplementation) i.next()).notifyDeleting();
        }
    }
    
    /**Notification that the project has been deleted.
     * Should be called immediatelly after the project is deleted.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     */
    public static void notifyDeleted(Project prj) throws IOException {
        for (Iterator i = getDeleteOperationImplementation(prj).iterator(); i.hasNext(); ) {
            ((DeleteOperationImplementation) i.next()).notifyDeleted();
        }
    }
    
    /**Test whether the copy operation is supported on the given project.
     * 
     * @param prj project to test
     * @return <code>true</code> if the project supports the copy operation,
     *         <code>false</code> otherwise
     */
    public static boolean isCopyOperationSupported(Project prj) {
        return !getCopyOperationImplementation(prj).isEmpty();
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
        for (Iterator i = getCopyOperationImplementation(prj).iterator(); i.hasNext(); ) {
            ((CopyOperationImplementation) i.next()).notifyCopying();
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
        for (Iterator i = getCopyOperationImplementation(original).iterator(); i.hasNext(); ) {
            ((CopyOperationImplementation) i.next()).notifyCopied(original, originalPath, name);
        }
        for (Iterator i = getCopyOperationImplementation(nue).iterator(); i.hasNext(); ) {
            ((CopyOperationImplementation) i.next()).notifyCopied(original, originalPath, name);
        }
    }
    
    /**Notification that the project is about to be moved.
     * Should be called immediatelly before the project is moved.
     *
     * The project is supposed to do all required cleanup to allow the project to be moved.
     *
     * @param prj project to notify
     * @throws IOException is some error occurs
     */
    public static void notifyMoving(Project prj) throws IOException {
        for (Iterator i = getMoveOperationImplementation(prj).iterator(); i.hasNext(); ) {
            ((MoveOperationImplementation) i.next()).notifyMoving();
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
        for (Iterator i = getMoveOperationImplementation(original).iterator(); i.hasNext(); ) {
            ((MoveOperationImplementation) i.next()).notifyMoved(original, originalPath, name);
        }
        for (Iterator i = getMoveOperationImplementation(nue).iterator(); i.hasNext(); ) {
            ((MoveOperationImplementation) i.next()).notifyMoved(original, originalPath, name);
        }
    }
    
    /**Test whether the move operation is supported on the given project.
     * 
     * @param prj project to test
     * @return <code>true</code> if the project supports the move operation,
     *         <code>false</code> otherwise
     */
    public static boolean isMoveOperationSupported(Project prj) {
        return true;
    }
    
    private static Collection/*<DeleteOperationImplementation>*/ getDeleteOperationImplementation(Project prj) {
        return prj.getLookup().lookup(new Lookup.Template(DeleteOperationImplementation.class)).allInstances();
    }
    
    private static Collection/*<DataFilesProviderImplementation>*/ getProjectsOperationsImplementation(Project prj) {
        return prj.getLookup().lookup(new Lookup.Template(DataFilesProviderImplementation.class)).allInstances();
    }
    
    private static Collection/*<CopyOperationImplementation>*/ getCopyOperationImplementation(Project prj) {
        return prj.getLookup().lookup(new Lookup.Template(CopyOperationImplementation.class)).allInstances();
    }
    
    private static Collection/*<MoveOperationImplementation>*/ getMoveOperationImplementation(Project prj) {
        return prj.getLookup().lookup(new Lookup.Template(MoveOperationImplementation.class)).allInstances();
    }
    
}
