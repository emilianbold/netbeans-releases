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

package org.openide.loaders;

/** Listener that watches progress of recognizing objects
* in a folder. The listener may even influence the data object recognition
* and, in such a way, act as a filter.
*
* <p>Normally the methods of this class are called in the process of a task
* to collect the data objects within a folder, e.g. in
* {@link FolderList#computeChildrenList(FolderListListener)}. In such a task
* implementations of {@link #process(DataObject, java.util.List)} may act as
* filters by not added the data object to the result list. Implementations
* of {@link #finished(java.util.List)} may be used to inform the caller about
* the result of the task and for further processing of the result. E.g.
* {@link FolderList#computeChildrenList(FolderListListener)} has as its return
* value the task to compute the list and not the computed children. An
* implementation of {@link #finished(java.util.List)} may be used by the caller
* of {@link FolderList#computeChildrenList(FolderListListener)} to get informed
* about the result of children computation.</p>
*
* @author Jaroslav Tulach
*/
interface FolderListListener {
    /** Another object has been recognized.
    * @param obj the object recognized
    * @param arr array where the implementation should add the 
    *    object
    */
    public void process (DataObject obj, java.util.List arr);

    /** All objects has been recognized.
    * @param arr list of DataObjects
    */
    public void finished (java.util.List arr);
}
