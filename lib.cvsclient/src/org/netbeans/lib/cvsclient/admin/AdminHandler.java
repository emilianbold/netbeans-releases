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
package org.netbeans.lib.cvsclient.admin;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;

/**
 * Handles the maintaining and reading of administration information on the
 * local machine. The standard CVS client does this by putting various files in
 * a CVS directory underneath each checked-out directory. How the files are
 * laid out and managed is not specified by the protocol document. <P>Hence it
 * is envisaged that, eventually, a client could add additional files for
 * higher performance or even change the mechanism for storing the information
 * completely.
 * @author  Robert Greig
 */
public interface AdminHandler {
    /**
     * Create or update the administration files for a particular file.
     * This will create the CVS directory if necessary, and the
     * Root and Repository files if necessary. It will also update
     * the Entries file with the new entry
     * @param localDirectory the local directory where the file in question
     * lives (the absolute path). Must not end with a slash.
     * @param repositoryPath the path of the file in the repository
     * @param entry the entry object for that file
     * @param globalOptions the global command options
     */
    void updateAdminData(String localDirectory, String repositoryPath,
                         Entry entry, GlobalOptions globalOptions)
            throws IOException;

    /**
     * Get the Entry for the specified file, if one exists
     * @param file the file
     * @throws IOException if the Entries file cannot be read
     */
    Entry getEntry(File file) throws IOException;

    /**
     * Get the entries for a specified directory.
     * @param directory the directory for which to get the entries
     * @return an iterator of Entry objects
     */
    Iterator getEntries(File directory) throws IOException;

    /**
     * Set the Entry for the specified file
     * @param file the file
     * @param entry the new entry
     * @throws IOException if an error occurs writing the details
     */
    void setEntry(File file, Entry entry) throws IOException;

    /**
     * Get the repository path for a given directory, for example in
     * the directory /home/project/foo/bar, the repository directory
     * might be /usr/cvs/foo/bar. The repository directory is commonly
     * stored in the file <pre>Repository</pre> in the CVS directory on
     * the client. (This is the case in the standard CVS command-line tool)
     * @param directory the directory
     * @param the repository path on the server, e.g. /home/bob/cvs. Must not
     * end with a slash.
     */
    String getRepositoryForDirectory(String directory, String repository)
            throws IOException;

    /**
     * Remove the Entry for the specified file
     * @param file the file whose entry is to be removed
     * @throws IOException if an error occurs writing the Entries file
     */
    void removeEntry(File file) throws IOException;

    /**
     * Get all the files contained within a given
     * directory that are <b>known to CVS</b>.
     * @param directory the directory to look in
     * @return a set of all files.
     */
    Set getAllFiles(File directory) throws IOException;

    /**
     * Checks for presence of CVS/Tag file and returns it's value.
     * @return the value of CVS/Tag file for the specified directory (including leading "T")
     *          null if file doesn't exist
     */
    String getStickyTagForDirectory(File directory);

    /**
     * Tests for existence of the given file. Normally this method
     * delegates to File.exists() but it may also return true for files
     * that exists only virtually (in memory). Is such case the file/directory
     * will not exist on disk but its metadata will be available via getEntries() methods.   
     * 
     * @param file file to test for existence
     * @return true if the file exists, false otherwise
     */ 
    boolean exists(File file);
}
