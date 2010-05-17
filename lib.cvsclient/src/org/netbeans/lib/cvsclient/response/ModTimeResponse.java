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
package org.netbeans.lib.cvsclient.response;

import java.text.*;
import java.util.*;
import org.netbeans.lib.cvsclient.admin.*;

import org.netbeans.lib.cvsclient.util.*;

/**
 * Sets the modification time of the next file sent to a specified time.
 * @author  Robert Greig
 */
class ModTimeResponse implements Response {

    /**
     * The formatter responsible for converting server dates to our own dates
     */
    protected static final SimpleDateFormat dateFormatter;

    /**
     * The way the server formats dates
     */
    protected static final String SERVER_DATE_FORMAT = "dd MMM yyyy HH:mm:ss"; //NOI18N

    static {
        dateFormatter = new SimpleDateFormat(SERVER_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(Entry.getTimeZone());
    }

    /**
     * Process the data for the response.
     * @param dis the data inputstream allowing the client to read the server's
     * response. Note that the actual response name has already been read
     * and the input stream is positioned just before the first argument, if
     * any.
     */
    public void process(LoggedDataInputStream dis, ResponseServices services)
            throws ResponseException {
        try {
            String dateString = dis.readLine();

            // we assume the date is in GMT, this appears to be the case
            // We remove the ending because SimpleDateFormat does not parse
            // +xxxx, only GMT+xxxx and this avoid us having to do String
            // concat
            Date date = dateFormatter.parse(
                                   dateString.substring(0, dateString.length() - 6));
            if (date.getTime() < 0) {
                // now we're in trouble - see #18232 issue.
                // we need to adjust the modified time..
                // so that the resulting date.getTime() is not negative.
                // The problem occurs when the sent year has only 2 digits.
                // this happens with old versions of cvs.
                if (date.getYear() < 100 && date.getYear() >= 70) {
                    date.setYear(date.getYear() + 1900);
                } 
                else if (date.getYear() >= 0 && date.getYear() < 70) {
                    date.setYear(date.getYear() + 2000);
                } 
                else {
                    date.setYear(2000 + date.getYear());
                    // for values less than zero let's assume
                    // that we need to substract the value from 2000
/*                    throw new ResponseException(
                       "Cannot adjust negative time value (" + dateString + ")", //NOI18N 
                       ResponseException.getLocalMessage("ModTimeResponse.badDate", //NOI18N
                                           new Object[] {dateString}));
 */
                }
            }
            services.setNextFileDate(date);
        }
        catch (Exception e) {
            throw new ResponseException(e);
        }
    }

    /**
     * Is this a terminal response, i.e. should reading of responses stop
     * after this response. This is true for responses such as OK or
     * an error response
     */
    public boolean isTerminalResponse() {
        return false;
    }
}
