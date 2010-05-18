/*****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.connection;

import java.util.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * This exception is thrown when a connection with the server cannot be made,
 * for whatever reason.
 * It may be that the username and/or password are incorrect or it could be
 * that the port number is incorrect. Note that authentication is not
 * restricted here to mean security.
 * @author  Robert Greig
 */
public class AuthenticationException extends Exception {
    /**
     * The underlying cause of this exception, if any.
     */
    private Throwable underlyingThrowable;

    private String message;

    private String localizedMessage;

    /**
     * Construct an AuthenticationException with a message giving more details
     * of what went wrong.
     * @param message the message describing the error
     **/
    public AuthenticationException(String message, String localizedMessage) {
        super(message);
        this.message = message;
        this.localizedMessage = localizedMessage;
    }

    /**
     * Construct an AuthenticationException with a message and an
     * underlying exception.
     * @param message the message describing what went wrong
     * @param e the underlying exception
     */
    public AuthenticationException(String message,
                                   Throwable underlyingThrowable,
                                   String localizedMessage) {
        this(message, localizedMessage);
        initCause(underlyingThrowable);
    }

    /**
     * Construct an AuthenticationException with an underlying
     * exception.
     * @param t the underlying throwable that caused this exception
     */
    public AuthenticationException(Throwable underlyingThrowable,
                                   String localizedMessage) {
        this.localizedMessage = localizedMessage;
        initCause(underlyingThrowable);
    }

    /**
     * Get the underlying throwable that is responsible for this exception.
     * @return the underlying throwable, if any (may be null).
     */
    public Throwable getUnderlyingThrowable() {
        return getCause();
    }

    public String getLocalizedMessage() {
        if (localizedMessage == null) {
            return message;
        }
        return localizedMessage;
    }

    public String getMessage() {
        return message;
    }

    protected static String getBundleString(String key) {
        String value = null;
        try {
            ResourceBundle bundle = BundleUtilities.getResourceBundle(AuthenticationException.class, "Bundle"); //NOI18N
            if (bundle != null) {
                value = bundle.getString(key);
            }
        }
        catch (MissingResourceException exc) {
        }
        return value;
    }
}
