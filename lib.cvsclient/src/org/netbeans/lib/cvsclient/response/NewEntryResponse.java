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

import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Indicates that a file has been successfully operated on, e.g. checked in,
 * added etc. is the same as Checked-in but operates on modified files..
 * @author  Milos Kleint
 */
class NewEntryResponse implements Response {
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
            String localPath = dis.readLine();
            //System.err.println("Pathname is: " + localPath);
            String repositoryPath = dis.readLine();
            //System.err.println("Repository path is: " + repositoryPath);
            String entriesLine = dis.readLine();
            //System.err.println("New entries line is: " + entriesLine);
            
            String absPath = services.convertPathname(localPath, repositoryPath);            
            if (services.getGlobalOptions().isExcluded(new File(absPath))) {
                return;
            }            
            
            // we set the date the file was last modified in the Entry line
            // so that we can easily determine whether the file has been
            // untouched
            final File theFile = new File(absPath);
//            final Date d = new Date(theFile.lastModified());
            final Entry entry = new Entry(entriesLine);
            entry.setConflict(Entry.DUMMY_TIMESTAMP);

            services.setEntry(theFile, entry);
        }
        catch (IOException e) {
            throw new ResponseException((Exception)e.fillInStackTrace(), e.getLocalizedMessage());
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
