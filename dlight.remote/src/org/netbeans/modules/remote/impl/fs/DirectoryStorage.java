/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps information about all files that reside in the directory
 * @author Vladimir Kvashin
 */
public class DirectoryStorage {

    public static class FormatException extends Exception {

        private final boolean expexted;

        public FormatException(String text, boolean expected) {
            super(text);
            this.expexted = expected;
        }

        public FormatException(String string, Throwable thrwbl) {
            super(string, thrwbl);
            expexted = false;
        }

        public boolean isExpexted() {
            return expexted;
        }
    }

    private final Map<String, DirEntry> entries = new HashMap<String, DirEntry>();
    private final File file;
    private static final int VERSION = 2;
    /* Incompatible version to discard */
    private static final int ODD_VERSION = 1;

    public DirectoryStorage(File file) {
        this.file = file;
    }

    /**
     * Format is:
     *      name cache access user group size "timestamp" link
     * Note that
     *      access contains file type as well (leftmost character)
     *      name is escaped (i.e. " " is replaced by "\\ ", "\\" by "\\\\")
     *      timestamp is quoted,
     *      access and timestamp is as in ls output on remote system
     * @throws IOException
     */
    public void load() throws IOException, FormatException {
        synchronized (this) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                // check version
                String line = br.readLine();
                String prefix = "VERSION="; // NOI18N
                if (line == null || ! line.startsWith(prefix)) {
                    throw wrongFormatException(line);
                }
                int version;
                try {
                    version = Integer.parseInt(line.substring(prefix.length()));
                } catch (NumberFormatException nfe) {
                    throw new FormatException("wrong version format " + file.getAbsolutePath(), nfe); // NOI18N
                }
                if (version > VERSION) {
                    throw new FormatException("attributes file version " + version +  //NNOI18N
                            " not supported: " + file.getAbsolutePath(), false); //NOI18N
                }
                if (version < ODD_VERSION) {
                    throw new FormatException("Discarding old attributes file version " + version +  //NNOI18N
                            ' ' + file.getAbsolutePath(), true); //NOI18N
                }
                while ((line = br.readLine()) != null) {
                    if (line.length() == 0) {
                        continue; // just in case, ignore empty lines
                    }
                    DirEntry entry = parseLine(line);
                    entries.put(entry.getName(), entry);
                }
             } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
    }

    private DirEntry parseLine(String line) throws FormatException {
        // array of entity creation parameters
        String[] params = new String[8];
        FileType fileType;
        // this array indices
        int name = 0;
        int cache = 1;
        int access = 2;
        int user = 3;
        int group = 4;
        int size = 5;
        int timestamp = 6;
        int link = 7;
        // buffer to accumulate current text
        StringBuilder currText = new StringBuilder();
        // index aka state
        int currIndex = name;
        boolean escape = false;
        cycle:
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            switch (c) {
                case '\\':
                    if (((currIndex == name) || (currIndex == cache)) && ! escape) {
                        escape = true;
                    } else {
                        currText.append(c);
                        escape = false;
                    }
                    break;
                case ' ':
                    if (currIndex == timestamp || escape) {
                        currText.append(c);
                    } else {
                        params[currIndex] = currText.toString();
                        currText = new StringBuilder();
                        currIndex++;
                    }
                    escape = false;
                    break;
                case '"':
                    if (currIndex == timestamp ) {
                        if (currText.length() == 0) {
                            // first quote - just skip it
                        } else {
                            params[timestamp] = currText.toString();
                            String t = line.substring(i+1).trim();
                            params[link] = (t.length() == 0) ? null : t;
                            break cycle;
                        }
                    } else if (currIndex + 1 == timestamp ) {
                        currIndex = timestamp;
                    } else {
                        currText.append(c);
                    }
                    escape = false;
                    break;
                default:
                    currText.append(c);
                    escape = false;
                    break;
            }
        }
        if (currIndex < link - 1) {
            throw wrongFormatException(line);
        }
        long sz;
        try {
            sz = Long.parseLong(params[size]);
        } catch (NumberFormatException ex) {
            throw wrongFormatException(line);
        }
        return new DirEntryImpl(params[name], params[cache], params[access], params[user], params[group], sz, params[timestamp], params[link]);
    }

    public void store() throws IOException {
        BufferedWriter wr = null;
        synchronized (this) {
            try {
                wr = new BufferedWriter(new FileWriter(file));
                wr.write("VERSION=" + VERSION + "\n"); //NOI18N
                for (DirEntry entry : entries.values()) {
                    entry.write(wr);
                    wr.write('\n');
                }
                wr.close();
                wr = null;
            } finally {
                if (wr != null) {
                    wr.close();
                }
            }
        }
    }

    public DirEntry getEntry(String fileName) {
        synchronized (this) {
            return entries.get(fileName);
        }
    }
    
    public  DirEntry removeEntry(String fileName) {
        synchronized (this) {
            return entries.remove(fileName);
        }
    }

    void setEntries(Collection<DirEntry> newEntries) {
        synchronized (this) {
            this.entries.clear();
            for (DirEntry entry : newEntries) {
                entries.put(entry.getName(), entry);
            }
        }
    }

    public List<DirEntry> list() {
        synchronized (this) {
            return new ArrayList<DirEntry>(entries.values());
        }
    }

    public int size() {
        synchronized (this) {
            return entries.size();
        }
    }

    /*package*/ void testAddEntry(DirEntry entry) {
        synchronized (this) {
            entries.put(entry.getName(), entry);
        }
    }

    private FormatException wrongFormatException(String line) {
        return new FormatException("Wrong file format " + file.getAbsolutePath() + " line " + line, false); //NOI18N)
    }
}
