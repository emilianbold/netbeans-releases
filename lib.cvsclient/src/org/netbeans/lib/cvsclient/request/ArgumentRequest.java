/*****************************************************************************
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
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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
 *
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.request;

/**
 * The Argument request. The server saves the specified argument for use in
 * a future argument-using command
 * @author  Robert Greig
 */
public class ArgumentRequest extends Request {
    /**
     * The argument to pass in
     */
    private final String argument;
    private final boolean escapeLeadingDash;

    /**
     * Create a new request
     * @param theArgument the argument to use
     */
    public ArgumentRequest(String argument) {
        this(argument, false);
    }

    /**
     * Create a new request
     * @param theArgument the argument to use
     * @param escapeLeadingDash true to escape leasding dash in the argument with another "--" Argument request, false otherwise
     */
    public ArgumentRequest(String argument, boolean escapeLeadingDash) {
        this.escapeLeadingDash = escapeLeadingDash;
        if (argument == null) {
            throw new IllegalArgumentException("argument must not be null!"); // NOI18N
        }

        this.argument = argument;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     */
    public String getRequestString() {
        if (escapeLeadingDash && argument.startsWith("-")) {
            return "Argument --\nArgument " + argument + "\n"; //NOI18N
        }
        return "Argument " + argument + "\n"; //NOI18N
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     */
    public boolean isResponseExpected() {
        return false;
    }
}