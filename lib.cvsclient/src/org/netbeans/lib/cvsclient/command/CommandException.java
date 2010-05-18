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
package org.netbeans.lib.cvsclient.command;

import java.io.*;
import java.text.*;
import java.util.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * This exception is thrown when an error occurs while executing a command.
 * It is nearly always a container for another exception.
 * @author  Robert Greig
 */
public class CommandException extends Exception {
    private Exception underlyingException;
    private String localizedMessage;
    private String message;

    public CommandException(Exception underlyingException, String localizedMessage) {
        this.underlyingException = underlyingException;
        this.localizedMessage = localizedMessage;
    }

    public CommandException(String message, String localizedMessage) {
        super(message);
        this.message = message;
        this.localizedMessage = localizedMessage;
    }

    public Exception getUnderlyingException() {
        return underlyingException;
    }

    public void printStackTrace() {
        if (underlyingException != null) {
            underlyingException.printStackTrace();
        }
        else {
            super.printStackTrace();
        }
    }

    public void printStackTrace(PrintStream stream) {
        if (underlyingException != null) {
            underlyingException.printStackTrace(stream);
        }
        else {
            super.printStackTrace(stream);
        }
    }

    public void printStackTrace(PrintWriter writer) {
        if (underlyingException != null) {
            underlyingException.printStackTrace(writer);
        }
        else {
            super.printStackTrace(writer);
        }
    }

    public String getLocalizedMessage() {
        if (localizedMessage == null) {
            return message;
        }
        return localizedMessage;
    }

    public String getMessage() {
        if (message == null) {
            return localizedMessage;
        }
        return message;
    }

    protected static String getBundleString(String key) {
        String value = null;
        try {
            ResourceBundle bundle = BundleUtilities.getResourceBundle(CommandException.class, "Bundle"); // NOI18N
            if (bundle != null) {
                value = bundle.getString(key);
            }
        }
        catch (MissingResourceException exc) {
        }
        return value;
    }

    public static String getLocalMessage(String key) {
        return getLocalMessage(key, null);
    }

    public static String getLocalMessage(String key, Object[] arguments) {
        String locMessage = CommandException.getBundleString(key);
        if (locMessage == null) {
            return null;
        }
        if (arguments != null) {
            locMessage = MessageFormat.format(locMessage, arguments);
        }
        return locMessage;
    }
}
