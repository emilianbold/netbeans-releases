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

import java.io.BufferedWriter;
import java.io.IOException;
import org.netbeans.modules.remote.support.RemoteLogger;

/**
 *
 * @author Vladimir Kvashin
 */
/*package*/ class DirEntryImpl implements DirEntry {

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
    
    public DirEntryImpl(String name, String cache, String access, String user, String group, long size, String timestamp, String link) {
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

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
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

    @Override
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

    @Override
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
    
    @Override
    public void write(BufferedWriter wr) throws IOException {
        wr.write(escape(name));
        wr.write(' ');
        wr.write(escape(cache));
        wr.write(' ');
        wr.write(type);
        wr.write(getAccessAsString());
        wr.write(' ');
        wr.write(user);
        wr.write(' ');
        wr.write(group);
        wr.write(' ');
        wr.write(Long.toString(size));
        wr.write(' ');
        wr.write('"');
        wr.write(timestamp);
        wr.write('"');
        if (link != null && link.length() > 0) {
            wr.write(' ');
            wr.write(link);
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
    
}
