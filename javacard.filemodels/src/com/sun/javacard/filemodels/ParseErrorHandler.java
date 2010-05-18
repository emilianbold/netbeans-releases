/*
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
 * by Oracle in the GPL Version 2 section of the License file that
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
package com.sun.javacard.filemodels;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles parse errors when reading a file model.  Some usages of file models
 * will want to be strict and abort processing when they encounter a bad entry;
 * others (an IDE for instance) will want to retrieve what they can regardless,
 * since they will rewrite the data correctly later.
 *
 * @Author Tim Boudreau
 */
public interface ParseErrorHandler {
    /**
     * Handle an exception throw during the parse process (the exception may
     * be a wrapped SAXException).  This code can log a warning, show the user
     * an error message, save the error for later display to the user and
     * continue processing, or rethrow the exception to abort processing.
     * @param ioe
     * @throws IOException
     */
    void handleError (IOException ioe) throws IOException;

    /**
     * Handle an invalid AID defined in a file.  It is often preferable to continue
     * parsing in this situation and allow the user to enter a valid AID if running
     * in the IDE;  if running in an Ant task, this should probably fail.
     * @param aidParseError The exception which should have a reasonable localized message
     * @param aidString The string version of the invalid AID
     */
    void handleBadAIDError (IllegalArgumentException aidParseError, String aidString);

    /**
     * Called if the XML parsing process if a tag it was not expecting to find was
     * encountered.  Can ignore, or throw an IOException or log the problem.
     * @param elementName The unexpected element name
     * @throws IOException An exception, if an unexpected element should cause processing to abort
     */
    void unrecognizedElementEncountered (String elementName) throws IOException;

    public static final ParseErrorHandler DEFAULT = new ParseErrorHandler() {
        public void handleError (IOException ioe) throws IOException {
            throw ioe;
        }

        public void handleBadAIDError(IllegalArgumentException aidParseError, String aidString) throws IllegalArgumentException {
//            Logger.getLogger(ParseErrorHandler.class.getName()).log (Level.INFO,
//                    "Bad AID encountered in file: " + aidString, aidParseError); //NOI18N
            throw aidParseError;
        }

        public void unrecognizedElementEncountered(String elementName) throws IOException {
            throw new IOException ("Unknown tag " + elementName + " encountered");
        }
    };

    public static final ParseErrorHandler NULL = new ParseErrorHandler() {

        public void handleError(IOException ioe) throws IOException {
            Logger.getLogger(getClass().getName()).log(Level.INFO, null, ioe);
        }

        public void handleBadAIDError(IllegalArgumentException aidParseError, String aidString) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, aidString, aidParseError);
        }

        public void unrecognizedElementEncountered(String elementName) throws IOException {
            //do nothing
        }
    };
}
