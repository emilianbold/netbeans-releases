/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
/*package*/ class DirEntryLs implements DirEntry {

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
    private final char type;
    private final short access;
    private final String user;
    private final String group;
    private final long size;
    private final String timestamp;
    private final String link;

    private String cache;
    
    private static boolean wrongDateFormatReported = false;    
    
    public DirEntryLs(String name, String cache, String access, String user, String group, long size, String timestamp, String link) {
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
                switch (i % 3) {
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
            for (int i = 1; i < 9; i += 3) {
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

    @Override
    public FileType getFileType() {
        return FileType.fromChar(type);
    }

    @Override
    public boolean isLink() {
        return type == FileType.Symlink.toChar();
    }

    public boolean isPlainFile() {
        return ! isLink() && ! isDirectory();
    }
    
    @Override
    public boolean isDirectory() {
        return type == FileType.Directory.toChar();
    }
    
    @Override
    public boolean isSameType(DirEntry other) {
        return (other != null) && isLink() == other.isLink() && isDirectory() == other.isDirectory();
    }

    public boolean isSameGroup(DirEntry other) {
        if (other instanceof DirEntryLs) {
            return group.equals(((DirEntryLs) other).group);
        }
        return false;
    }

    public boolean isSameUser(DirEntry other) {
        if (other instanceof DirEntryLs) {
            return user.equals(((DirEntryLs) other).user);
        }
        return false;        
    }

    public boolean isSameLastModified(DirEntry other) {
        if (other instanceof DirEntryLs) {
            return timestamp.equals(((DirEntryLs) other).timestamp);
        }
        return false;        
    }
    
    @Override
    public Date getLastModified() {
        try {
            if (timestamp != null) {
                Date date = DirectoryReaderLs.getDate(timestamp);
                if (date != null) {
                    return date;
                }
            }
        } catch (ParseException ex) {
            // it can be normal, for example, for not fully supported remote OS (FreeBSD, Mac, AIX, etc)
            if (!wrongDateFormatReported) {
                wrongDateFormatReported = true;
                RemoteLogger.getInstance().log(Level.INFO, "Error parsing date string : " + timestamp, ex);
            }
        }
        return new Date(0); // consistent with File.lastModified(), which returns 0 for inexistent file
    }
    

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCache() {
        return cache;
    }

    @Override
    public void setCache(String cache) {
        this.cache = cache;
    }

//    public String getGroup() {
//        return group;
//    }
//    
//    public String getUser() {
//        return user;
//    }    

    @Override
    public String getLinkTarget() {
        return link;
    }

    @Override
    public long getSize() {
        return size;
    }

    private HostInfo getHostInfo(ExecutionEnvironment execEnv) {
        if (HostInfoUtils.isHostInfoAvailable(execEnv)) {
            try {
                return HostInfoUtils.getHostInfo(execEnv);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    @Override
    public boolean canRead(ExecutionEnvironment execEnv) {
        HostInfo hostInfo = getHostInfo(execEnv);
        if (hostInfo == null) {
            return false;
        }
        String[] groups = hostInfo.getAllGroups();
        if ((access & ALL_R) > 0) {
            return true;
        }
        if ((access & USR_R) > 0) {
            if (this.user.equals(execEnv.getUser())) {
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

    @Override
    public boolean canWrite(ExecutionEnvironment execEnv) {
        HostInfo hostInfo = getHostInfo(execEnv);
        if (hostInfo == null) {
            return false;
        }
        String[] groups = hostInfo.getAllGroups();
        if ((access & ALL_W) > 0) {
            return true;
        }
        if ((access & USR_W) > 0) {
            if (this.user.equals(execEnv.getUser())) {
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

    @Override
    public boolean canExecute(ExecutionEnvironment execEnv) {
        HostInfo hostInfo = getHostInfo(execEnv);
        if (hostInfo == null) {
            return false;
        }
        String[] groups = hostInfo.getAllGroups();
        if ((access & ALL_X) > 0) {
            return true;
        }
        if ((access & USR_X) > 0) {
            if (this.user.equals(execEnv.getUser())) {
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

    @Override
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
    
    public static DirEntry fromExternalForm(String line) throws FormatException {
        // array of entity creation parameters
        String[] params = new String[8];
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
            throw new FormatException("Wrong directory entry format: " + line, false); //NOI18N
        }
        long sz;
        try {
            sz = Long.parseLong(params[size]);
        } catch (NumberFormatException ex) {
            throw new FormatException("Wrong directory entry format: " + line, false); //NOI18N
        }
        return new DirEntryLs(params[name], params[cache], params[access], params[user], params[group], sz, params[timestamp], params[link]);
    }

    @Override
    public String toExternalForm() {
        StringBuilder sb = new StringBuilder();
        sb.append(escape(name));
        sb.append(' ');
        sb.append(escape(cache));
        sb.append(' ');
        sb.append(type);
        sb.append(getAccessAsString());
        sb.append(' ');
        sb.append(user);
        sb.append(' ');
        sb.append(group);
        sb.append(' ');
        sb.append(Long.toString(size));
        sb.append(' ');
        sb.append('"');
        sb.append(timestamp);
        sb.append('"');
        if (link != null && link.length() > 0) {
            sb.append(' ');
            sb.append(link);
        }
        return sb.toString();
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
    
}
