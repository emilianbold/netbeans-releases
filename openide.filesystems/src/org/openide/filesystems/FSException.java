/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
