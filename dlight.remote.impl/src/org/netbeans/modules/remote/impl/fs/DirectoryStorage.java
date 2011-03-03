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

    private final Map<String, DirEntry> entries = new HashMap<String, DirEntry>();
    private final File file;
    private static final int VERSION = RemoteDirectory.getLsViaSftp() ? 3 : 2;
    /* Incompatible version to discard */
    private static final int ODD_VERSION = RemoteDirectory.getLsViaSftp() ? 3 : 2;

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
        synchronized (DirectoryStorage.this) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                // check version
                String line = br.readLine();
                String prefix = "VERSION="; // NOI18N
                if (line == null || ! line.startsWith(prefix)) {
                    throw new FormatException("Wrong file format " + file.getAbsolutePath() + " line " + line, false); //NOI18N)
                }
                int version;
                try {
                    version = Integer.parseInt(line.substring(prefix.length()));
                } catch (NumberFormatException nfe) {
                    throw new FormatException("wrong version format " + file.getAbsolutePath(), nfe); // NOI18N
                }
                if (version > VERSION) {
                    throw new FormatException("directory cache file version " + version +  //NNOI18N
                            " not supported: " + file.getAbsolutePath(), true); //NOI18N
                }
                if (version < ODD_VERSION) {
                    throw new FormatException("Discarding old directory cache file version " + version +  //NNOI18N
                            ' ' + file.getAbsolutePath(), true); //NOI18N
                }
                while ((line = br.readLine()) != null) {
                    if (line.length() == 0) {
                        continue; // just in case, ignore empty lines
                    }
                    DirEntry entry = RemoteDirectory.getLsViaSftp() ? 
                            DirEntrySftp.fromExternalForm(line) : DirEntryLs.fromExternalForm(line);
                    entries.put(entry.getName(), entry);
                }
             } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
    }

    public void touch() throws IOException {
        if (file.exists()) {
            file.setLastModified(System.currentTimeMillis());
        } else {
            store();
        }
    }
    
    public void store() throws IOException {
        BufferedWriter wr = null;
        synchronized (this) {
            try {
                wr = new BufferedWriter(new FileWriter(file));
                wr.write("VERSION=" + VERSION + "\n"); //NOI18N
                for (DirEntry entry : entries.values()) {
                    wr.write(entry.toExternalForm());
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DirectoryStorage");
        sb.append(" file=").append(file.getAbsolutePath());
        sb.append(" entries.size()=").append(entries.size()).append("\n");
        int cnt = 0;
        for (DirEntry entry : entries.values()) {
            if (cnt > 0) {
                sb.append('\n');
            }
            if (cnt++ <= 10) {
                sb.append(entry);
            } else {
                sb.append("...");
                break;
            }
        }
        return sb.toString();
    }    
}
