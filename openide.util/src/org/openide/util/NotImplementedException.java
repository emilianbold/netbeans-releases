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
package org.openide.util;


/** Should be thrown when a feature is not implemented.
* Usage of this exception should allow us to distingush between
* errors and unimplemented features.
* <P>
* Also this exception can easily be located in source code. That is
* why finding of unimplemented features should be simplified.
*
* @author Jaroslav Tulach
* @version 0.10 September 25, 1997
*/
public class NotImplementedException extends RuntimeException {
    static final long serialVersionUID = 465319326004943323L;

    /** Creates new exception NotImplementedException
    */
    public NotImplementedException() {
        super();
    }

    /** Creates new exception NotImplementedException with text specified
    * string s.
    * @param s the text describing the exception
    */
    public NotImplementedException(String s) {
        super(s);
    }
}
