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

import java.io.IOException;
import org.openide.util.Exceptions;


/** Localized IOException for filesystems.
*
* @author Jaroslav Tulach
*/
final class FSException extends IOException {
    /** name of resource to use for localized message */

    //  private String resource;

    /** arguments to pass to the resource */
    private Object[] args;

    /** Creates new FSException. */
    private FSException(String resource, Object[] args) {
        super(resource);
        this.args = args;
    }

    /** Message should be meaning full, but different from localized one.
    */
    public String getMessage() {
        return " " + getLocalizedMessage(); // NOI18N
    }

    /** Localized message.
    */
    public String getLocalizedMessage() {
        String res = super.getMessage();
        String format = FileSystem.getString(res);

        if (args != null) {
            return java.text.MessageFormat.format(format, args);
        } else {
            return format;
        }
    }

    /** Creates the localized exception.
    * @param resource to take localization string from
    * @exception the exception
    */
    public static void io(String resource) throws IOException {
        FSException fsExc = new FSException(resource, null);
        Exceptions.attachLocalizedMessage(fsExc, fsExc.getLocalizedMessage());
        throw fsExc;
    }

    public static void io(String resource, Object[] args)
    throws IOException {
        FSException fsExc = new FSException(resource, args);
        Exceptions.attachLocalizedMessage(fsExc, fsExc.getLocalizedMessage());
        throw fsExc;
    }

    public static void io(String resource, Object arg1)
    throws IOException {
        FSException fsExc = new FSException(resource, new Object[] { arg1 });
        Exceptions.attachLocalizedMessage(fsExc, fsExc.getLocalizedMessage());
        throw fsExc;
    }

    public static void io(String resource, Object arg1, Object arg2)
    throws IOException {
        FSException fsExc = new FSException(resource, new Object[] { arg1, arg2 });
        Exceptions.attachLocalizedMessage(fsExc, fsExc.getLocalizedMessage());
        throw fsExc;
    }

    public static void io(String resource, Object arg1, Object arg2, Object arg3)
    throws IOException {
        FSException fsExc = new FSException(resource, new Object[] { arg1, arg2, arg3 });
        Exceptions.attachLocalizedMessage(fsExc, fsExc.getLocalizedMessage());
        throw fsExc;
    }

    public static void io(String resource, Object arg1, Object arg2, Object arg3, Object arg4)
    throws IOException {
        FSException fsExc = new FSException(resource, new Object[] { arg1, arg2, arg3, arg4 });
        Exceptions.attachLocalizedMessage(fsExc, fsExc.getLocalizedMessage());
        throw fsExc;
    }
}
