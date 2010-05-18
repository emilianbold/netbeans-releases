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
package org.netbeans.lib.cvsclient.file;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.request.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles the reading and writing of files to and from the server. Different
 * implementations of this interface can use different formats for sending or
 * receiving the files, for example gzipped format.
 * @author  Robert Greig
 */
public interface FileHandler {
    /**
     * Transmit a text file to the server, using the standard CVS protocol
     * conventions. CR/LFs are converted to the Unix format.
     * @param file the file to transmit
     * @param dos the data outputstream on which to transmit the file
     */
    void transmitTextFile(File file, LoggedDataOutputStream dos)
        throws IOException;

    /**
     * Transmit a binary file to the server, using the standard CVS protocol
     * conventions.
     * @param file the file to transmit
     * @param dos the data outputstream on which to transmit the file
     */
    void transmitBinaryFile(File file, LoggedDataOutputStream dos)
        throws IOException;

     /**
     * Write (either create or replace) a text file on the local machine with
     * one read from the server.
     * @param path the absolute path of the file, (including the file name).
     * @param mode the mode of the file
     * @param dis the stream to read the file from, as bytes
     * @param length the number of bytes to read
     */
    void writeTextFile(String path, String mode, LoggedDataInputStream dis,
                   int length) throws IOException;

    /**
     * Merge a text file on the local machine with
     * the diff from the server. (it uses the RcsDiff response format
     *  - see cvsclient.ps for details)
     * @param path the absolute path of the file, (including the file name).
     * @param mode the mode of the file
     * @param dis the stream to read the file from, as bytes
     * @param length the number of bytes to read
     */
    void writeRcsDiffFile(String path, String mode, LoggedDataInputStream dis,
                          int length) throws IOException;

    /**
     * Write (either create or replace) a text file on the local machine with
     * one read from the server.
     * @param path the absolute path of the file, (including the file name).
     * @param mode the mode of the file
     * @param dis the stream to read the file from, as bytes
     * @param length the number of bytes to read
     */
    void writeBinaryFile(String path, String mode, LoggedDataInputStream dis,
                         int length) throws IOException;

    /**
     * Remove the specified file from the local disk.
     * If the file does not exist, the operation does nothing.

     * @param pathname the full path to the file to remove
     * @throws IOException if an IO error occurs while removing the file
     */
    void removeLocalFile(String pathname) throws IOException;

    /**
     * Copy a local file to new destination.
     * If the destination file exists, the file is overwritten.
     * 
     * @param pathname the full path to the file to copy
     * @param newName the new name of the file's copy (not the full path)
     * @throws IOException if an IO error occurs while copying the file
     */
    void copyLocalFile(String pathname, String newName) throws IOException;
    
    /**
     * Set the modified date of the next file to be written. The next call
     * to writeFile will use this date.
     * @param modifiedDate the date the file should be marked as modified
     */
    void setNextFileDate(Date modifiedDate);

    /**
     * Get any requests that must be sent before commands are sent, to init
     * this file handler.
     * @return an array of Requests that must be sent
     */
    Request[] getInitialisationRequests();

    /**
     * Sets the global options.
     * This can be useful to detect, whether local files should be made read-only.
     */
    void setGlobalOptions(GlobalOptions globalOptions);
}
