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
package org.netbeans.lib.cvsclient.request;

import org.netbeans.lib.cvsclient.admin.*;

/**
 * Sends an entry to the server, to tell the server which version of a file
 * is on the local machine. The filename is relative to the most recent
 * Directory request. Note that if an <pre>Entry</pre> request is sent
 * without <pre>Modified</pre>, <pre>Is-Modified</pre> or <pre>Unchanged</pre>
 * it means that the file is lost. Also note that if <pre>Modified</pre>,
 * <pre>Is-Modified</pre> or </pre>Unchanged</pre> is sent with <pre>Entry
 * </pre> then </pre>Entry</pre> must be sent first.
 * @author  Robert Greig
 * @see org.netbeans.lib.cvsclient.request.DirectoryRequest
 * @see org.netbeans.lib.cvsclient.request.ModifiedRequest
 * @see org.netbeans.lib.cvsclient.request.ModifiedRequest
 * @see org.netbeans.lib.cvsclient.request.UnchangedRequest
 */
public class EntryRequest extends Request {
    /**
     * The Entry sent by this request
     */
    private Entry entry;

    /**
     * Create an EntryRequest
     * @param theEntry the Entry to send
     */
    public EntryRequest(Entry theEntry) {
        if (theEntry == null)
            throw new IllegalArgumentException("EntryRequest: Entry must not " +
                                               "be null");
        entry = theEntry;
    }

    /**
     * Get the request String that will be passed to the server
     * @return the request String
     * @throws UnconfiguredRequestException if the request has not been
     * properly configured
     */
    public String getRequestString() throws UnconfiguredRequestException {
        return "Entry " + entry.toString() + "\n"; //NOI18N
    }

    /**
     * Is a response expected from the server?
     * @return true if a response is expected, false if no response if
     * expected
     */
    public boolean isResponseExpected() {
        return false;
    }

    public Entry getEntry() {
        return entry;
    }

}
