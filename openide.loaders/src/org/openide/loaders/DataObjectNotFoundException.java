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

import org.openide.filesystems.FileObject;

/** Exception signalling that the data object for a given file object could not
* be found in {@link DataObject#find}.
*
* @author Jaroslav Tulach
*/
public class DataObjectNotFoundException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1646623156535839081L;
    /** data object */
    private FileObject obj;

    /** Create a new exception.
    * @param obj the file that does not have a data object
    */
    public DataObjectNotFoundException (FileObject obj) {
        super (obj.toString ());
        this.obj = obj;
    }

    /** Get the file which does not have a data object.
     * @return the file
    */
    public FileObject getFileObject () {
        return obj;
    }
}
