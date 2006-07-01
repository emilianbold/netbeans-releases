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
package org.openide.filesystems;


/** Exception raised when a file is already locked.
*
* @see FileObject#lock
*
* @author Jaroslav Tulach
* @version 0.10 September 11, 1997
*/
public class FileAlreadyLockedException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4466344756249789982L;

    /** Creates new <code>FileAlreadyLockedException</code>.
    */
    public FileAlreadyLockedException() {
        super();
    }

    /** Creates new <code>FileAlreadyLockedException</code> with specified text.
    *
    * @param s the text describing the exception
    */
    public FileAlreadyLockedException(String s) {
        super(s);
    }
}
