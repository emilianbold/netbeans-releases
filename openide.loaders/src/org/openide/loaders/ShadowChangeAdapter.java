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

import java.util.EventObject;

import org.openide.util.Lookup;
import org.openide.filesystems.*;

/** Adapter for listening on changes of fileobjects and refreshing data
* shadows/broken links
*
* @author Ales Kemr
*/
class ShadowChangeAdapter extends Object implements OperationListener {

    /** Creates new ShadowChangeAdapter */
    ShadowChangeAdapter() {

        /* listen on loader pool to refresh datashadows after
        * create/delete dataobject
        */
        DataLoaderPool.getDefault().addOperationListener(this);
    }

    /** Checks for BrokenDataShadows */
    static void checkBrokenDataShadows(EventObject ev) {
        BrokenDataShadow.checkValidity(ev);
    }
    
    /** Checks for DataShadows */
    static void checkDataShadows(EventObject ev) {
        DataShadow.checkValidity(ev);
    }
    
    /** Object has been recognized by
     * {@link DataLoaderPool#findDataObject}.
     * This allows listeners
     * to attach additional cookies, etc.
     *
     * @param ev event describing the action
    */
    public void operationPostCreate(OperationEvent ev) {
        checkBrokenDataShadows(ev);
    }
    
    /** Object has been successfully copied.
     * @param ev event describing the action
    */
    public void operationCopy(OperationEvent.Copy ev) {
    }
    
    /** Object has been successfully moved.
     * @param ev event describing the action
    */
    public void operationMove(OperationEvent.Move ev) {
        checkDataShadows(ev);
        checkBrokenDataShadows(ev);
    }
    
    /** Object has been successfully deleted.
     * @param ev event describing the action
    */
    public void operationDelete(OperationEvent ev) {
        checkDataShadows(ev);
    }
    
    /** Object has been successfully renamed.
     * @param ev event describing the action
    */
    public void operationRename(OperationEvent.Rename ev) {
        checkDataShadows(ev);
        checkBrokenDataShadows(ev);
    }
    
    /** A shadow of a data object has been created.
     * @param ev event describing the action
    */
    public void operationCreateShadow(OperationEvent.Copy ev) {
    }
    
    /** New instance of an object has been created.
     * @param ev event describing the action
    */
    public void operationCreateFromTemplate(OperationEvent.Copy ev) {
        checkBrokenDataShadows(ev);
    }
    
}
