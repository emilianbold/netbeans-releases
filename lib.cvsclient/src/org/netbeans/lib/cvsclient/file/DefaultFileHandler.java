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
 * Provides a basic implementation of FileHandler, and does much of the
 * handling of reading and writing files and performing CRLF conversions.
 * @author  Robert Greig
 */
public class DefaultFileHandler
        implements FileHandler {
    /**
     * Whether to emit debug information.
     */
    private static final boolean DEBUG = false;

    /**
     * The size of chunks read from disk.
     */
    private static final int CHUNK_SIZE = 32768;

    /**
     * The date the next file written should be marked as being modified on.
     */
    private Date modifiedDate;

    private TransmitTextFilePreprocessor transmitTextFilePreprocessor;
    private WriteTextFilePreprocessor writeTextFilePreprocessor;
    private WriteTextFilePreprocessor writeRcsDiffFilePreprocessor;

    private GlobalOptions globalOptions;

    private final boolean ignoreModeFromServer = System.getProperty("netbeans.cvs.ignoreModeFromServer") != null;
    
    /**
     * Creates a DefaultFileHandler.
     */
    public DefaultFileHandler() {
        setTransmitTextFilePreprocessor(new DefaultTransmitTextFilePreprocessor());
        setWriteTextFilePreprocessor(new DefaultWriteTextFilePreprocessor());
        setWriteRcsDiffFilePreprocessor(new WriteRcsDiffFilePreprocessor());
    }

    /**
     * Returns the preprocessor for transmitting text files.
     */
    public TransmitTextFilePreprocessor getTransmitTextFilePreprocessor() {
        return transmitTextFilePreprocessor;
    }

    /**
     * Sets the preprocessor for transmitting text files.
     * The default one changes all line endings to Unix-lineendings (cvs default).
     */
    public void setTransmitTextFilePreprocessor(TransmitTextFilePreprocessor transmitTextFilePreprocessor) {
        this.transmitTextFilePreprocessor = transmitTextFilePreprocessor;
    }

    /**
     * Gets the preprocessor for writing text files after getting (and un-gzipping) from server.
     */
    public WriteTextFilePreprocessor getWriteTextFilePreprocessor() {
        return writeTextFilePreprocessor;
    }

    /**
     * Sets the preprocessor for writing text files after getting (and un-gzipping) from server.
     */
    public void setWriteTextFilePreprocessor(WriteTextFilePreprocessor writeTextFilePreprocessor) {
        this.writeTextFilePreprocessor = writeTextFilePreprocessor;
    }

    /**
     * Gets the preprocessor for merging text files after getting
     *  (and un-gzipping) the diff received from server.
     */
    public WriteTextFilePreprocessor getWriteRcsDiffFilePreprocessor() {
        return writeRcsDiffFilePreprocessor;
    }

    /**
     * Sets the preprocessor for merging text files after getting
     *  (and un-gzipping) the diff received from server.
     */
    public void setWriteRcsDiffFilePreprocessor(WriteTextFilePreprocessor writeRcsDiffFilePreprocessor) {
        this.writeRcsDiffFilePreprocessor = writeRcsDiffFilePreprocessor;
    }

    /**
     * Get the string to transmit containing the file transmission length.
     *
     * @return a String to transmit to the server (including carriage return)
     * @param length the amount of data that will be sent
     */
    protected String getLengthString(long length) {
        return String.valueOf(length) + "\n";  //NOI18N
    }

    protected Reader getProcessedReader(File f)
            throws IOException {
        return new FileReader(f);
    }

    protected InputStream getProcessedInputStream(File file) throws IOException {
        return new FileInputStream(file);
    }

    /**
     * Get any requests that must be sent before commands are sent, to init
     * this file handler.
     *
     * @return an array of Requests that must be sent
     */
    public Request[] getInitialisationRequests() {
        return null;
    }

    /**
     * Transmit a text file to the server, using the standard CVS protocol
     * conventions. CR/LFs are converted to the Unix format.
     *
     * @param file the file to transmit
     * @param dos the data outputstream on which to transmit the file
     */
    public void transmitTextFile(File file, LoggedDataOutputStream dos)
            throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File is either null or " +
                                               "does not exist. Cannot transmit.");
        }

        File fileToSend = file;

        final TransmitTextFilePreprocessor transmitTextFilePreprocessor =
                                              getTransmitTextFilePreprocessor();

        if (transmitTextFilePreprocessor != null) {
            fileToSend = transmitTextFilePreprocessor.getPreprocessedTextFile(file);
        }

        BufferedInputStream bis = null;
        try {
            // first write the length of the file
            long length = fileToSend.length();
            dos.writeBytes(getLengthString(length), "US-ASCII");

            bis = new BufferedInputStream(new FileInputStream(fileToSend));
            // now transmit the file itself
            byte[] chunk = new byte[CHUNK_SIZE];
            while (length > 0) {
                int bytesToRead = (length >= CHUNK_SIZE) ? CHUNK_SIZE
                                                         : (int)length;
                int count = bis.read(chunk, 0, bytesToRead);
                if (count == -1) {
                    throw new IOException("Unexpected end of stream from "+fileToSend+".");
                }
                length -= count;
                dos.write(chunk, 0, count);
            }
            dos.flush();
        }
        finally {
            if (bis != null) {
                try {
                    bis.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (transmitTextFilePreprocessor != null) {
                transmitTextFilePreprocessor.cleanup(fileToSend);
            }
        }
    }

    /**
     * Transmit a binary file to the server, using the standard CVS protocol
     * conventions.
     * @param file the file to transmit
     * @param dos the data outputstream on which to transmit the file
     */
    public void transmitBinaryFile(File file, LoggedDataOutputStream dos)
            throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File is either null or " +
                                               "does not exist. Cannot transmit.");
        }

        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            // first write the length of the file
            long length = file.length();

            dos.writeBytes(getLengthString(length), "US-ASCII");

            // now transmit the file itself
            byte[] chunk = new byte[CHUNK_SIZE];
            while (length > 0) {
                int bytesToRead = (length >= CHUNK_SIZE) ? CHUNK_SIZE
                                                         : (int)length;
                int count = bis.read(chunk, 0, bytesToRead);
                if (count == -1) {
                    throw new IOException("Unexpected end of stream from "+file+".");
                }
                length -= count;
                dos.write(chunk, 0, count);
            }
            dos.flush();
        }
        finally {
            if (bis != null) {
                try {
                    bis.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Write (either create or replace) a file on the local machine with
     * one read from the server.
     * @param path the absolute path of the file, (including the file name).
     * @param mode the mode of the file
     * @param dis the stream to read the file from, as bytes
     * @param length the number of bytes to read
     */
    public void writeTextFile(String path, String mode,
                              LoggedDataInputStream dis, int length)
            throws IOException {
        writeAndPostProcessTextFile(path, mode, dis, length,
                                    getWriteTextFilePreprocessor());
    }

    /**
     * Merge a text file on the local machine with
     * the diff from the server. (it uses the RcsDiff response format
     *  - see cvsclient.ps for details)
     * @param path the absolute path of the file, (including the file name).
     * @param mode the mode of the file
     * @param dis the stream to read the file from, as bytes
     * @param length the number of bytes to read
     */
    public void writeRcsDiffFile(String path, String mode, LoggedDataInputStream dis,
                                 int length) throws IOException {
        writeAndPostProcessTextFile(path, mode, dis, length,
                                    getWriteRcsDiffFilePreprocessor());
    }

    /**
     * Common code for writeTextFile() and writeRcsDiffFile() methods.
     * Differs only in the passed file processor.
     */
    private void writeAndPostProcessTextFile(String path, String mode, LoggedDataInputStream dis,
                                             int length, WriteTextFilePreprocessor processor) throws IOException {
        if (DEBUG) {
            System.err.println("[writeTextFile] writing: " + path); //NOI18N
            System.err.println("[writeTextFile] length: " + length); //NOI18N
            System.err.println("Reader object is: " + dis.hashCode()); //NOI18N
        }

        File file = new File(path);

        boolean readOnly = resetReadOnly(file, mode);

        createNewFile(file);
        // For CRLF conversion, we have to read the file
        // into a temp file, then do the conversion. This is because we cannot
        // perform a sequence of readLines() until we've read the file from
        // the server - the file transmission is not followed by a newline.
        // Bah.
        File tempFile = File.createTempFile("cvsCRLF", "tmp"); //NOI18N

        try {
            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(tempFile));
                byte[] chunk = new byte[CHUNK_SIZE];
                while (length > 0) {
                    int count = (length >= CHUNK_SIZE) ? CHUNK_SIZE
                                                       :length;
                    count = dis.read(chunk, 0, count);
                    if (count == -1) {
                        throw new IOException("Unexpected end of stream: " + path + "\nMissing " + length + " bytes. Probably network communication failure.\nPlease try again.");  // NOI18N
                    }
                    length -= count;
                    if (DEBUG) {
                        System.err.println("Still got: " + length + " to read"); //NOI18N
                    }
                    os.write(chunk, 0, count);
                }
            }
            finally {
                if (os != null) {
                    try {
                        os.close();
                    }
                    catch (IOException ex) {
                        // ignore
                    }
                }
            }

            // Here we read the temp file in again, doing any processing required
            // (for example, unzipping). We must not convert bytes to characters
            // because it would break characters that are not in the current encoding
            InputStream tempInput = getProcessedInputStream(tempFile);

            try {
                //BUGLOG - assert the processor is not null..
                processor.copyTextFileToLocation(tempInput, file, new StreamProvider(file));
            } finally {
                tempInput.close();
            }

            if (modifiedDate != null) {
                file.setLastModified(modifiedDate.getTime());
                modifiedDate = null;
            }
        }
        finally {
            tempFile.delete();
        }

        if (readOnly) {
            FileUtils.setFileReadOnly(file, true);
        }
    }

    /**
     * Write (either create or replace) a binary file on the local machine with
     * one read from the server.
     * @param path the absolute path of the file, (including the file name).
     * @param mode the mode of the file
     * @param dis the stream to read the file from, as bytes
     * @param length the number of bytes to read
     */
    public void writeBinaryFile(String path, String mode,
                                LoggedDataInputStream dis, int length)
            throws IOException {
        if (DEBUG) {
            System.err.println("[writeBinaryFile] writing: " + path); //NOI18N
            System.err.println("[writeBinaryFile] length: " + length); //NOI18N
            System.err.println("Reader object is: " + dis.hashCode()); //NOI18N
        }

        File file = new File(path);

        boolean readOnly = resetReadOnly(file, mode);

        createNewFile(file);
        // FUTURE: optimisation possible - no need to use a temp file if there
        // is no post processing required (e.g. unzipping). So perhaps enhance
        // the interface to allow this stage to be optional
        File cvsDir = new File(file.getParentFile(), "CVS");
        cvsDir.mkdir();
        File tempFile = File.createTempFile("cvsPostConversion", "tmp", cvsDir); //NOI18N

        try {
            BufferedOutputStream bos =
            new BufferedOutputStream(new FileOutputStream(tempFile));

            byte[] chunk = new byte[CHUNK_SIZE];
            try {
                while (length > 0) {
                    int bytesToRead = (length >= CHUNK_SIZE) ? CHUNK_SIZE
                            : (int)length;
                    int count = dis.read(chunk, 0, bytesToRead);
                    if (count == -1) {
                        throw new IOException("Unexpected end of stream: " + path + "\nMissing " + length + " bytes. Probably network communication failure.\nPlease try again.");  // NOI18N
                    }
                    if (count < 0) {
                        break;
                    }

                    length -= count;
                    if (DEBUG) {
                        System.err.println("Still got: " + length + " to read"); //NOI18N
                    }
                    bos.write(chunk, 0, count);
                }
            } finally {
                bos.close();
            }

            // Here we read the temp file in, taking the opportunity to process
            // the file, e.g. unzip the data
            BufferedInputStream tempIS =
                     new BufferedInputStream(getProcessedInputStream(tempFile));
            bos = new BufferedOutputStream(createOutputStream(file));

            try {
                for (int count = tempIS.read(chunk, 0, CHUNK_SIZE);
                     count > 0;
                     count = tempIS.read(chunk, 0, CHUNK_SIZE)) {
                    bos.write(chunk, 0, count);
                }
            } finally {
                bos.close();
                tempIS.close();
            }

            // now we need to modifiy the timestamp on the file, if specified
            if (modifiedDate != null) {
                file.setLastModified(modifiedDate.getTime());
                modifiedDate = null;
            }
        }
        finally {
            tempFile.delete();
        }

        if (readOnly) {
            FileUtils.setFileReadOnly(file, true);
        }
    }

    /** Extension point allowing subclasses to change file creation logic.*/
    protected boolean createNewFile(File file) throws IOException {
        file.getParentFile().mkdirs();
        return file.createNewFile();
    }

    /**
     * Extension point allowing subclasses to change file write logic.
     * The stream is close()d after usage.
     */
    protected OutputStream createOutputStream(File file) throws IOException {
        return new FileOutputStream(file);
    }

    private class StreamProvider implements OutputStreamProvider {
        private final File file;

        public StreamProvider(File file) {
            this.file = file;
        }

        public OutputStream createOutputStream() throws IOException {
            return DefaultFileHandler.this.createOutputStream(file);
        }
    }

    private boolean resetReadOnly(File file, String cvsMode) throws java.io.IOException {
        boolean isReadOnly = file.exists() && !file.canWrite(); 
        if (isReadOnly) {
            FileUtils.setFileReadOnly(file, false);
        }
        boolean readOnlyFromClient = globalOptions != null && globalOptions.isCheckedOutFilesReadOnly();
        boolean readOnlyFromServer = false;
        if (!ignoreModeFromServer && cvsMode != null) {
            // u=rw,g=rw,o=rw
            int idx1 = cvsMode.indexOf('=');
            int idx2 = cvsMode.indexOf(',');
            readOnlyFromServer = idx2 > idx1 && cvsMode.substring(idx1 + 1, idx2).indexOf('w') == -1;
        }
        return readOnlyFromClient || readOnlyFromServer;
    }

    /**
     * Remove the specified file from the local disk.
     *
     * @param pathname the full path to the file to remove
     * @throws IOException if an IO error occurs while removing the file
     */
    public void removeLocalFile(String pathname)
            throws IOException {
        File fileToDelete = new File(pathname);
        if (fileToDelete.exists() && !fileToDelete.delete()) {
            System.err.println("Could not delete file " +
                               fileToDelete.getAbsolutePath());
        }
    }

    public void copyLocalFile(String pathname, String newName)
            throws IOException {
        File sourceFile = new File(pathname);
        File destinationFile = new File(sourceFile.getParentFile(), newName);
        FileUtils.copyFile(sourceFile, destinationFile);
    }

    /**
     * Set the modified date of the next file to be written.
     * The next call to writeFile will use this date.
     *
     * @param modifiedDate the date the file should be marked as modified
     */
    public void setNextFileDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * Sets the global options.
     * This can be useful to detect, whether local files should be made read-only.
     */
    public void setGlobalOptions(GlobalOptions globalOptions) {
        BugLog.getInstance().assertNotNull(globalOptions);

        this.globalOptions = globalOptions;
        transmitTextFilePreprocessor.setTempDir(globalOptions.getTempDir());
    }
}
