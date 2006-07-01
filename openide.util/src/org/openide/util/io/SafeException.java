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

package org.openide.util.io;

/** Special IOException that is used to signal that the write operation
* failed but the underlaying stream is not corrupted and can be used
* for next operations.
*
*
* @author Jaroslav Tulach, Jesse Glick
*/
public class SafeException extends FoldingIOException {
    private static final long serialVersionUID = 4365154082401463604L;

    /** the exception encapsulated */
    private Exception ex;

    /** Default constructor.
    */
    public SafeException(Exception ex) {
        super(ex, null);
        this.ex = ex;
    }

    /** @return the encapsulated exception.
    */
    public Exception getException() {
        return ex;
    }

    public Throwable getCause() {
        return ex;
    }

}
