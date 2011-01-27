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
import org.netbeans.modules.remote.support.RemoteLogger;

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

    public static class Entry {

        private static final short USR_R = 256;
        private static final short USR_W = 128;
        private static final short USR_X = 64;
        private static final short GRP_R = 32;
        private static final short GRP_W = 16;
        private static final short GRP_X = 8;
        private static final short ALL_R = 4;
        private static final short ALL_W = 2;
        private static final short ALL_X = 1;

        private final String name;
        private String cache;

        private char type;
        private short access;

        private final String user;
        private final String group;
        private final long size;
        private final String timestamp;
        private final String link;

        public Entry(String name, String cache, String access, String user, String group, long size, String timestamp, String link) {
            if (name == null) {
                throw new NullPointerException("Null name"); //NOI18N
            }
            boolean assertions = false;
            assert (assertions = true);
            if (assertions) {
                String assertionText = "Wrong access format: " + access; //NOI18N
                RemoteLogger.assertTrue(access.length() >= 10, assertionText);
                for (int i = 1; i < 0; i++) {
                    char c = access.charAt(i);
                    switch (i%3) {
                        case 1:
                            RemoteLogger.assertTrue(c == 'r' || c == '-', assertionText);
                            break;
                        case 2:
                            RemoteLogger.assertTrue(c == 'w' || c == '-', assertionText);
                            break;
                        case 0:
                            RemoteLogger.assertTrue(c == 'x' || c == '-' || c == 's' || c == 'S' || c == 't' || c == 'T', assertionText);
                            break;
                    }
                }
                for (int i = 1; i < 9; i+= 3) {
                    char c = access.charAt(i);
                }
                RemoteLogger.assertTrue(FileType.fromChar(access.charAt(0)) != null, "Can't get file type from access string: " + access); //NOI18N
            }
            this.type = access.charAt(0);
            this.name = name;
            this.cache = cache;
            this.access = stringToAcces(access);
            this.user = user;
            this.group = group;
            this.size = size;
            this.timestamp = timestamp;
            this.link = link;
        }

        public FileType getFileType() {
            return FileType.fromChar(type);
        }

        public String getName() {
            return name;
        }

        public String getCache() {
            return cache;
        }

        public void setCache(String cache) {
            this.cache = cache;
        }

        public String getGroup() {
            return group;
        }

        public String getLink() {
            return link;
        }

        public long getSize() {
            return size;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getUser() {
            return user;
        }

        public boolean canRead(String user, String... groups) {
            if ((access & ALL_R) > 0) {
                return true;
            }
            if ((access & USR_R) > 0) {
                if (this.user.equals(user)) {
                    return true;
                }
            }
            if ((access & GRP_R) > 0 && groups != null) {
                for (String g : groups) {
                    if (group.equals(g)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean canWrite(String user, String... groups) {
            if ((access & ALL_W) > 0) {
                return true;
            }
            if ((access & USR_W) > 0) {
                if (this.user.equals(user)) {
                    return true;
                }
            }
            if ((access & GRP_W) > 0 && groups != null) {
                for (String g : groups) {
                    if (group.equals(g)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean canExecute(String user, String... groups) {
            if ((access & ALL_X) > 0) {
                return true;
            }
            if ((access & USR_X) > 0) {
                if (this.user.equals(user)) {
                    return true;
                }
            }
            if ((access & GRP_X) > 0 && groups != null) {
                for (String g : groups) {
                    if (group.equals(g)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public String getAccessAsString() {
            char[] accessChars = new char[9];

            accessChars[0] = ((access & USR_R) == 0) ? '-' : 'r';
            accessChars[1] = ((access & USR_W) == 0) ? '-' : 'w';
            accessChars[2] = ((access & USR_X) == 0) ? '-' : 'x';

            accessChars[3] = ((access & GRP_R) == 0) ? '-' : 'r';
            accessChars[4] = ((access & GRP_W) == 0) ? '-' : 'w';
            accessChars[5] = ((access & GRP_X) == 0) ? '-' : 'x';

            accessChars[6] = ((access & ALL_R) == 0) ? '-' : 'r';
            accessChars[7] = ((access & ALL_W) == 0) ? '-' : 'w';
            accessChars[8] = ((access & ALL_X) == 0) ? '-' : 'x';

            return new String(accessChars);
        }

        private short stringToAcces(String accessString) {
            short result = 0;

            // 0-th character is file type => start with 1
            result |= (accessString.charAt(1) == 'r') ? USR_R : 0;
            result |= (accessString.charAt(2) == 'w') ? USR_W : 0;
            result |= (accessString.charAt(3) == 'x') ? USR_X : 0;

            result |= (accessString.charAt(4) == 'r') ? GRP_R : 0;
            result |= (accessString.charAt(5) == 'w') ? GRP_W : 0;
            result |= (accessString.charAt(6) == 'x') ? GRP_X : 0;

            result |= (accessString.charAt(7) == 'r') ? ALL_R : 0;
            result |= (accessString.charAt(8) == 'w') ? ALL_W : 0;
            result |= (accessString.charAt(9) == 'x') ? ALL_X : 0;

            return result;
        }

        @Override
        public String toString() {
            return name + ' ' + getAccessAsString() + ' ' + user + ' ' + group + ' ' + timestamp + ' ' + link;
        }
    }

    private final Map<String, Entry> entries = new HashMap<String, Entry>();
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
                    Entry entry = parseLine(line);
                    entries.put(entry.name, entry);
                }
             } finally {
                if (br != null) {
                    br.close();
                }
            }
        }
    }

    private String escape(String text) {
        if (text.indexOf(' ') < 0 && text.indexOf('\\') < 0) {
            return text;
        } else {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                switch (c) {
                    case ' ':
                        result.append("\\ "); //NOI18N
                        break;
                    case '\\':
                        result.append("\\\\"); //NOI18N
                        break;
                    default:
                        result.append(c); //NOI18N
                        break;
                }
            }
            return result.toString();
        }
    }

    private Entry parseLine(String line) throws FormatException {
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
        return new Entry(params[name], params[cache], params[access], params[user], params[group], sz, params[timestamp], params[link]);
    }

    public void store() throws IOException {
        BufferedWriter wr = null;
        synchronized (this) {
            try {
                wr = new BufferedWriter(new FileWriter(file));
                wr.write("VERSION=" + VERSION + "\n"); //NOI18N
                for (Entry entry : entries.values()) {
                    wr.write(escape(entry.name));
                    wr.write(' ');
                    wr.write(escape(entry.cache));
                    wr.write(' ');
                    wr.write(entry.type);
                    wr.write(entry.getAccessAsString());
                    wr.write(' ');
                    wr.write(entry.user);
                    wr.write(' ');
                    wr.write(entry.group);
                    wr.write(' ');
                    wr.write(Long.toString(entry.size));
                    wr.write(' ');
                    wr.write('"');
                    wr.write(entry.timestamp);
                    wr.write('"');
                    if (entry.link != null && entry.link.length() > 0) {
                        wr.write(' ');
                        wr.write(entry.link);
                    }
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

    public Entry getEntry(String fileName) {
        synchronized (this) {
            return entries.get(fileName);
        }
    }
    
    public  Entry removeEntry(String fileName) {
        synchronized (this) {
            return entries.remove(fileName);
        }
    }

    void setEntries(Collection<Entry> newEntries) {
        synchronized (this) {
            this.entries.clear();
            for (Entry entry : newEntries) {
                entries.put(entry.name, entry);
            }
        }
    }

    public List<Entry> list() {
        synchronized (this) {
            return new ArrayList<Entry>(entries.values());
        }
    }

    public int size() {
        synchronized (this) {
            return entries.size();
        }
    }

    /*package*/ void testAddEntry(Entry entry) {
        synchronized (this) {
            entries.put(entry.name, entry);
        }
    }

    private FormatException wrongFormatException(String line) {
        return new FormatException("Wrong file format " + file.getAbsolutePath() + " line " + line, false); //NOI18N)
    }
}
