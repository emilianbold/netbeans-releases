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

import java.io.*;

import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles standard message responses
 * @author  Robert Greig
 */
class MessageResponse implements Response {

    private boolean terminating = false;

    private String firstWord;

    public MessageResponse() {
        // do nothing
    }
    
    public MessageResponse(String initialWord) {
        firstWord = initialWord;
    }
    
    /**
     * Process the data for the response.
     * @param dis the data inputstream allowing the client to read the server's
     * response. Note that the actual response name has already been read
     * and the inputstream is positioned just before the first argument, if any.
     */
    public void process(LoggedDataInputStream dis, ResponseServices services)
            throws ResponseException {
        try {
            ByteArray bytes = dis.readLineBytes();
            String line = bytes.getStringFromBytes();
            if (firstWord != null) {
                line = firstWord + " " + line; //NOI18N
            }
            terminating |= line.endsWith(SpecialResponses.SERVER_ABORTED);
            terminating |= line.endsWith(SpecialResponses.SERVER_ABORTED_2);
            terminating |= line.endsWith(SpecialResponses.SERVER_ABORTED_3);
            terminating &= dis.available() == 0;  // heuristics to relax SpecialResponses in commit messages...
            MessageEvent event = new MessageEvent(this, line, bytes.getBytes(), false);
            services.getEventManager().fireCVSEvent(event);
        }
        catch (EOFException ex) {
            throw new ResponseException(ex, ResponseException.getLocalMessage("CommandException.EndOfFile", null)); //NOI18N
        }
        catch (IOException ex) {
            throw new ResponseException(ex);
        }
    }

    /**
     * Is this a terminal response, i.e. should reading of responses stop
     * after this response. This is true for responses such as OK or
     * an error response
     * <p>
     * @see SpecialResponses.SERVER_ABORTED
     */
    public boolean isTerminalResponse() {
        return terminating;
    }
}
