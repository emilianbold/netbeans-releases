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

/** Exception signalling that the data object for this file cannot
* be created because there already is an object for the primary file.
*
* @author Jaroslav Tulach
* @version 0.10, Mar 30, 1998
*/
public class DataObjectExistsException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4719319528535266801L;
    /** data object */
    private DataObject obj;

    /** Create new exception.
    * @param obj data object which already exists
    */
    public DataObjectExistsException (DataObject obj) {
        this.obj = obj;
    }

    /** Get the object which already exists.
     * @return the data object
    */
    public DataObject getDataObject () {
        //
        // we have to consult the DataObjectPool to check whether
        // the constructor of our DataObject has finished
        //
        DataObjectPool.getPOOL().waitNotified (obj);
        
        // now it should be safe to return the objects
        return obj;
    }

    /** Performance trick */
    public /*final*/ Throwable fillInStackTrace() {
        return this;
    }
}
