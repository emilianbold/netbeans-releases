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
package org.netbeans.lib.cvsclient;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.file.*;
import org.netbeans.lib.cvsclient.request.*;
import org.netbeans.lib.cvsclient.response.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Clients that provide the ability to execute commands must implement this
 * interface. All commands use this interface to get details about the
 * environment in which it is being run, and to perform administrative
 * functions such as obtaining Entry lines for specified files.
 * @author  Robert Greig
 */
public interface ClientServices {
    /**
     * Process all the requests.
     *
     * @param requests the requets to process
     */
    void processRequests(List requests) throws IOException,
            UnconfiguredRequestException, ResponseException,
            CommandAbortedException;

    /**
     * Get the repository used for this connection.
     *
     * @return the repository, for example /home/bob/cvs
     */
    String getRepository();

    /**
     * Get the repository path for a given directory, for example in
     * the directory /home/project/foo/bar, the repository directory
     * might be /usr/cvs/foo/bar. The repository directory is commonly
     * stored in the file <pre>Repository</pre> in the CVS directory on
     * the client. (This is the case in the standard CVS command-line tool)
     *
     * @param directory the directory
     */
    String getRepositoryForDirectory(String directory)
            throws IOException;

    /**
     * Semantically equivalent to {@link #getRepositoryForDirectory(String)} but does not try to recover from
     * missing CVS/Repository file.
     *
     * @param directory the directory to get repository for
     * @return repository path that corresponds to the given local working directory or null if local directory
     * is not versioned or does not exist
     * @throws IOException if the repository cannot be determined by reading CVS/Repository file
     */
    String getRepositoryForDirectory(File directory) throws IOException;
    
    /**
     * Get the local path that the command is executing in.
     *
     * @return the local path
     */
    String getLocalPath();

    /**
     * Get the Entry for the specified file, if one exists.
     *
     * @param file the file
     *
     * @throws IOException if the Entries file cannot be read
     */
    Entry getEntry(File file) throws IOException;

    /**
     * Get the entries for a specified directory.
     *
     * @param directory the directory for which to get the entries
     *
     * @return an iterator of Entry objects
     */
    Iterator getEntries(File directory) throws IOException;

    /**
     * Create or update the administration files for a particular file
     * This will create the CVS directory if necessary, and the
     * Root and Repository files if necessary. It will also update
     * the Entries file with the new entry
     *
     * @param localDirectory the local directory, relative to the directory
     *                       in which the command was given, where the file in
     *                       question lives
     * @param entry the entry object for that file
     *
     * @throws IOException if there is an error writing the files
     */
    void updateAdminData(String localDirectory, String repositoryPath,
                         Entry entry)
            throws IOException;

    /**
     * Get all the files contained within a given
     * directory that are <b>known to CVS</b>.
     *
     * @param directory the directory to look in
     *
     * @return a set of all files.
     */
    Set getAllFiles(File directory) throws IOException;

    /**
     * Returns true if no command was sent before.
     * This is used, because the server rejects some doubled commands.
     */
    boolean isFirstCommand();

    /**
     * Set whether this is the first command. Normally you do not need to set
     * this yourself - after execution the first command will have set this to
     * false.
     */
    void setIsFirstCommand(boolean first);

    /**
     * Removes the Entry for the specified file.
     */
    void removeEntry(File file) throws IOException;

    /**
     * Sets the specified IgnoreFileFilter to use to ignore non-cvs files.
     * TS, 2001-11-23: really needed in the interface (it's never used)?
     */
    void setIgnoreFileFilter(IgnoreFileFilter filter);

    /**
     * Returns the IgnoreFileFilter used to ignore non-cvs files.
     * TS, 2001-11-23: really needed in the interface (it's never used)?
     */
    IgnoreFileFilter getIgnoreFileFilter();

    /**
     * Returnes true to indicate, that the file specified by directory and nonCvsFile
     * should be ignored.
     */
    boolean shouldBeIgnored(File directory, String nonCvsFile);

    //
    // allow the user of the Client to define the FileHandlers
    //

    /**
     * Set the uncompressed file handler.
     */
    void setUncompressedFileHandler(FileHandler handler);

    /**
     * Set the handler for Gzip data.
     */
    void setGzipFileHandler(FileHandler handler);

    /**
     * Checks for presence of CVS/Tag file and returns it's value.
     *
     * @return the value of CVS/Tag file for the specified directory
     *          null if file doesn't exist
     */
    String getStickyTagForDirectory(File directory);

    /**
     * Ensures, that the connection is open.
     *
     * @throws AuthenticationException if it wasn't possible to connect
     */
    void ensureConnection() throws AuthenticationException;
    
    /**
     * Returns the wrappers map associated with the CVS server
     * The map is valid only after the connection is established
     */
    Map getWrappersMap() throws CommandException;
    
    /**
     * Get the global options that are set to this client.
     * Individual commands can get the global options via this method.
     */
    GlobalOptions getGlobalOptions();

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
    
    /**
     * Tests whether command execution should be aborted. Commands are encouraged to regulary
     * poll this value if they expect to block for a long time in their code.
     * 
     * @return true if currently running command should abort, false otherwise
     */ 
    boolean isAborted();
}
