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

/** Listener to operations on data objects. Can be attached to
* the {@link DataLoaderPool} and will receive information about operations taken on all
* {@link DataObject}s.
*
* @author Jaroslav Tulach
*/
public interface OperationListener extends java.util.EventListener {
    /** Object has been recognized by
    * {@link DataLoaderPool#findDataObject}.
    * This allows listeners
    * to attach additional cookies, etc.
    *
    * @param ev event describing the action
    */
    public void operationPostCreate (OperationEvent ev);

    /** Object has been successfully copied.
    * @param ev event describing the action
    */
    public void operationCopy (OperationEvent.Copy ev);

    /** Object has been successfully moved.
    * @param ev event describing the action
    */
    public void operationMove (OperationEvent.Move ev);

    /** Object has been successfully deleted.
    * @param ev event describing the action
    */
    public void operationDelete (OperationEvent ev);

    /** Object has been successfully renamed.
    * @param ev event describing the action
    */
    public void operationRename (OperationEvent.Rename ev);

    /** A shadow of a data object has been created.
    * @param ev event describing the action
    */
    public void operationCreateShadow (OperationEvent.Copy ev);

    /** New instance of an object has been created.
    * @param ev event describing the action
    */
    public void operationCreateFromTemplate (OperationEvent.Copy ev);
}
