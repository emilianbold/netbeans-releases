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
import java.util.*;
import java.text.*;

import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.file.*;
import org.netbeans.lib.cvsclient.util.*;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;

/**
 * Sends a new copy of a particular file, indicating that the file currently
 * checked-out needs to be updated with the one sent with this response.
 * @author  Robert Greig
 */
class UpdatedResponse implements Response {
    private static final boolean DEBUG = false;

    /**
     * The local path of the new file.
     */
    private String localPath;

    /**
     * The full repository path of the file.
     */
    private String repositoryPath;

    /**
     * The entry line.
     */
    private String entryLine;

    /**
     * The mode.
     */
    private String mode;

    /**
     * fullpath to the file being processed. Is used in Merged response when sending EnhancedMessageEvent
     */
    protected String localFile;

    /** 
     * The date Formatter used to parse and format dates.
     * Format is: "EEE MMM dd HH:mm:ss yyyy"
     */
    private DateFormat dateFormatter;
    
    /**
     * Process the data for the response.
     * @param r the buffered reader allowing the client to read the server's
     * response. Note that the actual response name has already been read
     * and the reader is positioned just before the first argument, if any.
     * @param services various services that are useful to response handlers
     * @throws ResponseException if something goes wrong handling this response
     */
    public void process(LoggedDataInputStream dis, ResponseServices services)
            throws ResponseException {
        try {
            localPath = dis.readLine();
            repositoryPath = dis.readLine();
            entryLine = dis.readLine();
            mode = dis.readLine();

            String nextLine = dis.readLine();

            boolean useGzip = (nextLine.charAt(0) == 'z');

            int length = Integer.parseInt(useGzip ? nextLine.substring(1)
                                          : nextLine);

            if (DEBUG) {
                System.err.println("Got update response."); //NOI18N
                System.err.println("LocalPath is          : " + localPath); //NOI18N
                System.err.println("Repository path is    : " + repositoryPath); //NOI18N
                System.err.println("Entries line is       : " + entryLine); //NOI18N
                System.err.println("Mode is               : " + mode); //NOI18N
                System.err.println("Next line (length) is : " + nextLine); //NOI18N
                System.err.println("File length is        : " + length); //NOI18N
            }

            // now read in the file
            final String filePath = services.convertPathname(localPath,
                                                             repositoryPath);

            final File newFile = new File(filePath);
            localFile = newFile.getAbsolutePath();

            if (services.getGlobalOptions().isExcluded(newFile)) {
                skip(dis, length);
                return;
            }
            
            if (this instanceof CreatedResponse) {
                if (newFile.exists()) {
                    skip(dis, length);
                    // Fire "C file.txt" type of event. This event is caught directly by clients of the library  
                    DefaultFileInfoContainer fic = new DefaultFileInfoContainer();
                    fic.setType("C");
                    fic.setFile(newFile);
                    services.getEventManager().fireCVSEvent(new FileInfoEvent(this, fic));
                    return;
                }
            }
            
            final Entry entry = new Entry(entryLine);

            FileHandler fileHandler = useGzip ? services.getGzipFileHandler()
                    : services.getUncompressedFileHandler();
            fileHandler.setNextFileDate(services.getNextFileDate());

            // check if the file is binary
            if (entry.isBinary()) {
                fileHandler.writeBinaryFile(filePath, mode, dis, length);
            }
            else {
                fileHandler.writeTextFile(filePath, mode, dis, length);
            }

            // we set the date the file was last modified in the Entry line
            // so that we can easily determine whether the file has been
            // untouched
            // for files with conflicts skip the setting of the conflict field.
            String conflictString = null;
            if ((entry.getConflict() != null) &&
                    (entry.getConflict().charAt(0) == Entry.HAD_CONFLICTS)) {
                if (entry.getConflict().charAt(1) ==
                        Entry.TIMESTAMP_MATCHES_FILE) {
                    final Date d = new Date(newFile.lastModified());
                    conflictString = getEntryConflict(d, true);
                }
                else {
                    conflictString = entry.getConflict().substring(1);
                }
            }
            else {
                final Date d = new Date(newFile.lastModified());
                conflictString = getEntryConflict(d, false);
            }
            entry.setConflict(conflictString);
            // fix for correctly merging files that are added in other branches.
            if (entry.isNewUserFile()) {
                entry.setConflict(Entry.DUMMY_TIMESTAMP);
            }
            // update the admin files (i.e. within the CVS directory)
            services.updateAdminData(localPath, repositoryPath, entry);

            // now fire the appropriate event
            if (newFile.exists()) {
                FileAddedEvent e = new FileAddedEvent(this, filePath);
                services.getEventManager().fireCVSEvent(e);
            }
            else {
                FileUpdatedEvent e = new FileUpdatedEvent(this, filePath);
                services.getEventManager().fireCVSEvent(e);
            }
            //System.err.println("Finished writing file");
        }
        catch (IOException e) {
            throw new ResponseException(e);
        }
    }

    private void skip(LoggedDataInputStream dis, int length) throws IOException {
        while (length > 0) {
            length -= dis.skip(length);
        
        }
    }

    /**
     * Returns the Conflict field for the file's entry.
     * Can be overriden by subclasses.
     * (For example the MergedResponse that sets the "result of merge" there.)
     * @param date the date to put in
     * @param hadConflicts if there were conflicts (e.g after merge)
     * @return the conflict field
     */
    protected String getEntryConflict(Date date, boolean hadConflicts) {
        return getDateFormatter().format(date);
    }
    
    /**
     * Returns the DateFormatter instance that parses and formats date Strings.
     * The exact format matches the one in Entry.getLastModifiedDateFormatter() method.
     *
     */
    protected DateFormat getDateFormatter() {
        if (dateFormatter == null) {
            dateFormatter = Entry.getLastModifiedDateFormatter();
        }
        return dateFormatter;
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
