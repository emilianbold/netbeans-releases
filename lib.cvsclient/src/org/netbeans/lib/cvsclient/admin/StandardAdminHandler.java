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
import org.netbeans.lib.cvsclient.file.FileUtils;

/**
 * A handler for administrative information that maintains full compatibility
 * with the one employed by the original C implementation of a CVS client.
 * <p>This implementation strives to provide complete compatibility with
 * the standard CVS client, so that operations on locally checked-out
 * files can be carried out by either this library or the standard client
 * without causing the other to fail. Any such failure should be considered
 * a bug in this library.
 * @author  Robert Greig
 */
public class StandardAdminHandler implements AdminHandler {

    private static final Object ksEntries = new Object();
    
    private static Runnable t9yBeforeRename;

    /**
     * Create or update the administration files for a particular file.
     * This will create the CVS directory if necessary, and the
     * Root and Repository files if necessary. It will also update
     * the Entries file with the new entry
     * @param localDirectory the local directory where the file in question
     * lives (the absolute path). Must not end with a slash.
     * @param entry the entry object for that file. If null, there is no
     * entry to add, and the Entries file will not have any entries added to
     * it (it will be created if it does not exist, however).
     */
    public void updateAdminData(String localDirectory, String repositoryPath,
                                Entry entry, GlobalOptions globalOptions)
            throws IOException {
        // add this directory to the list of those to check for emptiness if
        // the prune option is specified

        final File CVSdir = new File(localDirectory, "CVS"); //NOI18N

        CVSdir.mkdirs();

        // now ensure that the Root and Repository files exist
        File rootFile = new File(CVSdir, "Root"); //NOI18N
        if (!rootFile.exists()) {
            PrintWriter w = new PrintWriter(new FileWriter(rootFile));
            try {
                w.println(globalOptions.getCVSRoot());
            } finally {
                w.close();
            }
        }
        File repositoryFile = new File(CVSdir, "Repository"); //NOI18N
        if (!repositoryFile.exists()) {
            PrintWriter w = new PrintWriter(new FileWriter(repositoryFile));
            try {
                if (entry != null && !entry.isDirectory()) {
                    // If there is a file entry, the repository path is for a file!
                    int length = entry.getName().length();
                    repositoryPath = repositoryPath.substring(0, repositoryPath.length() - length);
                }
                if (repositoryPath.endsWith("/")) { //NOI18N
                    repositoryPath = repositoryPath.substring(0,
                                                              repositoryPath.length() - 1);
                }
                if (repositoryPath.length() == 0) {
                    repositoryPath = "."; //NOI18N
                }
                // we write out the relative path to the repository file
                w.println(repositoryPath);
            } finally {
                w.close();
            }
        }

        final File entriesFile = new File(CVSdir, "Entries"); //NOI18N

        // We assume that if we do not have an Entries file, we need to add
        // the file to the parent CVS directory as well as create the
        // Entries file for this directory
        if (entriesFile.createNewFile()) {
            // need to know if we had to create any directories so that we can
            // update the CVS/Entries file in the *parent* director
            addDirectoryToParentEntriesFile(CVSdir);

            // We have created a new Entries file, so put a D in it to
            // indicate that we understand directories.
            // TODO: investigate what the point of this is. The command-line
            // CVS client does it
            final Writer w = new BufferedWriter(new FileWriter(entriesFile));
            try {
                w.write("D"); //NOI18N
            } finally {
                w.close();
            }
        }

        // Update the Entries file
        if (entry != null) {
            updateEntriesFile(entriesFile, entry);
        }
    }

    /**
     * Simply delegates to File.exists(), does not provide any virtual files.
     * 
     * @param file file to test for existence
     * @return true if the file exists on disk, false otherwise
     */ 
    public boolean exists(File file) {
        return file.exists();
    }

    /**
     * Add a directory entry to the parent directory's Entries file, if it
     * exists. Any line containing only a D is deleted from the parent
     * Entries file.
     * @param CVSdir the full path to the CVS directory for the directory
     * that has just been added
     */
    private void addDirectoryToParentEntriesFile(File CVSdir)
            throws IOException {
        synchronized (ksEntries) {
            File parentCVSEntries = seekEntries(CVSdir.getParentFile().getParentFile());

            // only update if the file exists. The file will not exist in the
            // case where this is the top level of the module
            if (parentCVSEntries != null) {
                final File directory = parentCVSEntries.getParentFile();
                final File tempFile = new File(directory, "Entries.Backup"); //NOI18N
                tempFile.createNewFile();
                BufferedReader reader = null;
                BufferedWriter writer = null;
                try {
                    reader = new BufferedReader(new FileReader(parentCVSEntries));
                    writer = new BufferedWriter(new FileWriter(tempFile));

                    String line;

                    // As in the updateEntriesFile method the new Entry
                    // only may be written, if it does not exist
                    boolean written = false;

                    Entry directoryEntry = new Entry();
                    directoryEntry.setName(CVSdir.getParentFile().getName());
                    directoryEntry.setDirectory(true);

                    while ((line = reader.readLine()) != null) {
                        if (line.trim().equals("D")) { //NOI18N
                            // do not write out this line
                            continue;
                        }

                        final Entry currentEntry = new Entry(line);
                        if (currentEntry.getName() != null
                                && currentEntry.getName().equals(directoryEntry.getName())) {
                            writer.write(directoryEntry.toString());
                            written = true;
                        }
                        else {
                            writer.write(line);
                        }
                        writer.newLine();
                    }

                    if (!written) {
                        writer.write(directoryEntry.toString());
                        writer.newLine();
                    }
                } finally {
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                }

                if (t9yBeforeRename != null) t9yBeforeRename.run();
                FileUtils.renameFile(tempFile, parentCVSEntries);
            }
        }
    }

    /**
     * Update the specified Entries file with a given entry
     * This method currently does the following, in order:
     * <OL><LI>Create a new temporary file, Entries.temp</LI>
     * <LI>Iterate through each line of the original file, checking
     * if it matches the entry of the entry to be updated. If not, simply
     * copy the line otherwise write the new entry and discard the old one</LI>
     * <LI>Once all lines have been written, close both files</LI>
     * <LI>Move the original file to Entries.temp2</LI>
     * <LI>Move the file Entries.temp to Entries</LI>
     * <LI>Delete Entries.temp2</LI></UL>
     * Note that in the case where the Entries file does not exist, it
     * is simply created and the entry appended immediately.
     * This all ensures that if a failure occurs at any stage, the original
     * Entries file can be retrieved
     * @param originalFile the original Entries file, which need not exist yet
     * @param entry the specific entry to update
     * @throws IOException if an error occurs writing the files
     */
    private void updateEntriesFile(File originalFile, Entry entry)
            throws IOException {
        synchronized (ksEntries) {
            final File directory = originalFile.getParentFile();
            final File tempFile = new File(directory, "Entries.Backup"); //NOI18N
            tempFile.createNewFile();

            BufferedReader reader = null;
            BufferedWriter writer = null;
            try {
                reader = new BufferedReader(new FileReader(originalFile));
                writer = new BufferedWriter(new FileWriter(tempFile));
                String line;
                // indicates whether we have written the entry that was passed in
                // if we finish copying the file without writing it, then it is
                // a new entry and must simply be append at the end
                boolean written = false;
                while ((line = reader.readLine()) != null) {
                    final Entry currentEntry = new Entry(line);
                    if ((currentEntry.getName() != null) &&
                            currentEntry.getName().equals(entry.getName())) {
                        writer.write(entry.toString());
                        written = true;
                    }
                    else {
                        writer.write(line);
                    }
                    writer.newLine();
                }
                if (!written) {
                    writer.write(entry.toString());
                    writer.newLine();
                }
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }

            if (t9yBeforeRename != null) t9yBeforeRename.run();
            FileUtils.renameFile(tempFile, originalFile);
        }
    }

    /**
     * Get the Entry for the specified file, if one exists
     * @param f the file
     * @throws IOException if the Entries file cannot be read
     */
    public Entry getEntry(File file) throws IOException {
        final File entriesFile = seekEntries(file.getParentFile()); //NOI18N
        // if there is no Entries file we cannot very well get any Entry
        // from it
        if (entriesFile == null) {
            return null;
        }

        processEntriesDotLog(new File(file.getParent(), "CVS")); //NOI18N

        BufferedReader reader = null;
        Entry entry = null;
        boolean found = false;
        try {
            reader = new BufferedReader(new FileReader(entriesFile));
            String line;
            while (!found && ((line = reader.readLine()) != null)) {
                entry = new Entry(line);
                // can have a name of null in the case of the single
                // D entry line spec, indicating no subdirectories
                if (entry.getName() != null) {
                    // file equality and string equality are not the same thing
                    File entryFile = new File(file.getParentFile(), entry.getName());
                    found = entryFile.equals(file);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (!found) {
            return null;
        }

        return entry;
    }

    /**
     * Get the entries for a specified directory.
     * @param directory the directory for which to get the entries
     * @return an array of Entry objects
     */
    public Entry[] getEntriesAsArray(File directory)
            throws IOException {
        List entries = new LinkedList();

        final File entriesFile = seekEntries(directory);
        // if there is no Entries file we just return the empty iterator
        if (entriesFile == null) {
            return new Entry[0];
        }

        processEntriesDotLog(new File(directory, "CVS")); //NOI18N

        BufferedReader reader = null;
        Entry entry = null;
        try {
            reader = new BufferedReader(new FileReader(entriesFile));
            String line;
            while ((line = reader.readLine()) != null) {
                entry = new Entry(line);
                // can have a name of null in the case of the single
                // D entry line spec, indicating no subdirectories
                if (entry.getName() != null) {
                    entries.add(entry);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        Entry[] toReturnArray = new Entry[entries.size()];
        toReturnArray = (Entry[])entries.toArray(toReturnArray);
        return toReturnArray;
    }

    /**
     * Get the entries for a specified directory.
     * @param directory the directory for which to get the entries (CVS/Entries is appended)
     * @return an iterator of Entry objects
     */
    public Iterator getEntries(File directory)
            throws IOException {
        List entries = new LinkedList();

        final File entriesFile = seekEntries(directory);
        // if there is no Entries file we just return the empty iterator
        if (entriesFile == null) {
            return entries.iterator();
        }

        processEntriesDotLog(new File(directory, "CVS")); //NOI18N

        BufferedReader reader = null;
        Entry entry = null;
        try {
            reader = new BufferedReader(new FileReader(entriesFile));
            String line;
            while ((line = reader.readLine()) != null) {
                entry = new Entry(line);
                // can have a name of null in the case of the single
                // D entry line spec, indicating no subdirectories
                if (entry.getName() != null) {
                    entries.add(entry);
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        return entries.iterator();
    }

    /**
     * Set the Entry for the specified file
     * @param f the file whose entry is being updated
     * @throws IOException if an error occurs writing the details
     */
    public void setEntry(File file, Entry entry)
            throws IOException {
        String parent = file.getParent();
        File entriesFile = seekEntries(parent);
        if (entriesFile == null) {
            entriesFile = new File(parent, "CVS/Entries"); //NOI18N
        }
        processEntriesDotLog(new File(parent, "CVS")); //NOI18N
        updateEntriesFile(entriesFile, entry);
    }

    /**
     * Remove the Entry for the specified file
     * @param f the file whose entry is to be removed
     * @throws IOException if an error occurs writing the Entries file
     */
    public void removeEntry(File file) throws IOException {
        synchronized (ksEntries) {
            final File entriesFile = seekEntries(file.getParent());

            // if there is no Entries file we cannot very well remove an Entry
            // from it
            if (entriesFile == null) {
                return;
            }

            processEntriesDotLog(new File(file.getParent(), "CVS")); //NOI18N

            final File directory = file.getParentFile();
            final File tempFile = new File(directory, "Entries.Backup"); //NOI18N
            tempFile.createNewFile();
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try {
                reader = new BufferedReader(new FileReader(entriesFile));
                writer = new BufferedWriter(new FileWriter(tempFile));
                String line;
                // allows us to determine whether we need to put in a "D" line. We do
                // that if we remove the last directory from the Entries file
                boolean directoriesExist = false;
                while ((line = reader.readLine()) != null) {
                    final Entry currentEntry = new Entry(line);
                    if ((currentEntry.getName() != null) &&

                            !currentEntry.getName().equals(file.getName())) {
                        writer.write(currentEntry.toString());
                        writer.newLine();
                        directoriesExist = directoriesExist ||
                                currentEntry.isDirectory();
                    }
                }
                if (!directoriesExist) {
                    writer.write("D"); //NOI18N
                    writer.newLine();
                }
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }

            if (t9yBeforeRename != null) t9yBeforeRename.run();
            FileUtils.renameFile(tempFile, entriesFile);
        }
    }

    /**
     * Get the repository path for a given directory, for example in
     * the directory /home/project/foo/bar, the repository directory
     * might be /usr/cvs/foo/bar. The repository directory is commonly
     * stored in the file <pre>Repository</pre> in the CVS directory on
     * the client. (This is the case in the standard CVS command-line tool).
     * However, the path stored in that file is relative to the repository
     * path
     * @param directory the directory
     * @param the repository path on the server, e.g. /home/bob/cvs. Must not
     * end with a slash.
     */
    public String getRepositoryForDirectory(String directory,
                                            String repository)
            throws IOException {
        // if there is no "CVS/Repository" file, try to search up the file-
        // hierarchy
        File repositoryFile = null;
        String repositoryDirs = ""; //NOI18N
        File dirFile = new File(directory);
        while (true) {
            // if there is no Repository file we cannot very well get any
            // repository from it
            if (dirFile == null
                    || dirFile.getName().length() == 0
                    || !dirFile.exists()) {
                throw new FileNotFoundException("Repository file not found " + //NOI18N
                                                "for directory " + directory); //NOI18N
            }

            repositoryFile = new File(dirFile, "CVS/Repository"); //NOI18N
            if (repositoryFile.exists()) {
                break;
            }
            repositoryDirs = '/' + dirFile.getName() + repositoryDirs;
            dirFile = dirFile.getParentFile();
        }
        BufferedReader reader = null;
        // fileRepository is the value of the repository read from the
        // Repository file
        String fileRepository = null;
        try {
            reader = new BufferedReader(new FileReader(repositoryFile));
            fileRepository = reader.readLine();
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (fileRepository == null) {
            fileRepository = ""; //NOI18N
        }

        fileRepository += repositoryDirs;
        // absolute repository path ?
        if (fileRepository.startsWith("/")) { //NOI18N
            return fileRepository;
        }

        // #69795 'normalize' repository path
        if (fileRepository.startsWith("./")) {  // NOI18N
            fileRepository = fileRepository.substring(2);
        }
        // otherwise the cvs is using relative repository path
        // must be a forward slash, regardless of the local filing system
        return repository + '/' + fileRepository;
    }

    /**
     * Update the Entries file using information in the Entries.Log file
     * (if present). If Entries.Log is not present, this method does
     * nothing.
     * @param directory the directory that contains the Entries file
     * @throws IOException if an error occurs reading or writing the files
     */
    private void processEntriesDotLog(File directory) throws IOException {
        
        synchronized (ksEntries) {
            final File entriesDotLogFile = new File(directory, "Entries.Log"); //NOI18N
            if (!entriesDotLogFile.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(
                    entriesDotLogFile));

            // make up a list of changes to be made based on what is in
            // the .log file. Then apply them all later
            List additionsList = new LinkedList();
            HashSet removalSet = new HashSet();

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("A ")) { //NOI18N
                        final Entry entry = new Entry(line.substring(2));
                        additionsList.add(entry);
                    }
                    else if (line.startsWith("R ")) { //NOI18N
                        final Entry entry = new Entry(line.substring(2));
                        removalSet.add(entry.getName());
                    }
                    // otherwise ignore the line since we don't understand it
                }
            } finally {
                reader.close();
            }

            if ((additionsList.size() > 0) || (removalSet.size() > 0)) {
                final File backup = new File(directory, "Entries.Backup"); //NOI18N
                final BufferedWriter writer = new BufferedWriter(new FileWriter(
                        backup));
                final File entriesFile = new File(directory, "Entries"); //NOI18N
                reader = new BufferedReader(new FileReader(entriesFile));

                try {
                    // maintain a count of the number of directories so that
                    // we know whether to write the "D" line
                    int directoryCount = 0;

                    while ((line = reader.readLine()) != null) {
                        // we will write out the directory "understanding" line
                        // later, if necessary
                        if (line.trim().equals("D")) { //NOI18N
                            continue;
                        }

                        final Entry entry = new Entry(line);

                        if (entry.isDirectory()) {
                            directoryCount++;
                        }

                        if (!removalSet.contains(entry.getName())) {
                            writer.write(entry.toString());
                            writer.newLine();
                            if (entry.isDirectory()) {
                                directoryCount--;
                            }
                        }
                    }
                    Iterator it = additionsList.iterator();
                    while (it.hasNext()) {
                        final Entry entry = (Entry)it.next();
                        if (entry.isDirectory()) {
                            directoryCount++;
                        }
                        writer.write(entry.toString());
                        writer.newLine();
                    }
                    if (directoryCount == 0) {
                        writer.write("D"); //NOI18N
                        writer.newLine();
                    }
                } finally {
                    try {
                        reader.close();
                    } finally {
                        writer.close();
                    }
                }

                if (t9yBeforeRename != null) t9yBeforeRename.run();
                FileUtils.renameFile(backup, entriesFile);
            }
            entriesDotLogFile.delete();
        }
    }

    /**
     * Get all the files contained within a given
     * directory that are <b>known to CVS</b>.
     * @param directory the directory to look in
     * @return a set of all files.
     */
    public Set getAllFiles(File directory) throws IOException {
        TreeSet fileSet = new TreeSet();
        BufferedReader reader = null;
        try {
            final File entriesFile = seekEntries(directory); //NOI18N

            // if for any reason we don't have an Entries file just return
            // with the empty set.
            if (entriesFile == null) {
                return fileSet; // Premature return
            }

            reader = new BufferedReader(new FileReader(entriesFile));
            String line;

            while ((line = reader.readLine()) != null) {
                final Entry entry = new Entry(line);

                if (entry.getName() != null) {
                    final File f = new File(directory, entry.getName());
                    if (f.isFile()) {
                        fileSet.add(f);
                    }
                }
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        return fileSet;
    }

    // XXX where is pairing setStickyTagForDirectory?
    /**
     * Checks for presence of CVS/Tag file and returns it's value.
     * @return the value of CVS/Tag file for the specified directory
     *         null if file doesn't exist
     */
    public String getStickyTagForDirectory(File directory) {
        BufferedReader reader = null;
        File tagFile = new File(directory, "CVS/Tag"); //NOI18N

        try {
            reader = new BufferedReader(new FileReader(tagFile));
            String tag = reader.readLine();
            return tag;
        }
        catch (IOException ex) {
            // silently ignore??
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    // silently ignore
                }
            }
        }
        return null;
    }

    /**
     * If Entries do not exist restore them from backup.
     *
     * @param  folder path where to seek CVS/Entries
     * @return CVS/Entries file or null
     */
    private static File seekEntries(File folder) {
        synchronized(ksEntries) {
            File entries = new File(folder, "CVS/Entries"); // NOI18N
            if (entries.exists()) {
                return entries;
            } else {
                File backup = new File(folder, "CVS/Entries.Backup"); // NOI18N
                if (backup.exists()) {
                    try {                    
                        if (t9yBeforeRename != null) t9yBeforeRename.run();
                        FileUtils.renameFile(backup, entries);
                        return entries;
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
            return null;
        }
    }

    private static File seekEntries(String folder) {
        return seekEntries(new File(folder));
    }
    
    static void t9yBeforeRenameSync(Runnable run) {
        t9yBeforeRename = run;
    }
}
