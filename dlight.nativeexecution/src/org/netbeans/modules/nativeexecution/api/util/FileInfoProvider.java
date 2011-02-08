package org.netbeans.modules.nativeexecution.api.util;

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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.openide.util.Exceptions;

/**
 *
 * @author Vladimir Kvashin
 */
public class FileInfoProvider {
   
    public static final class StatInfo {

        private final String name;
        private final boolean directory;
        private final boolean link;
        
        private final int gid;
        private final int uid;

        private final String linkTarget;

        private final int access;
        private final Date lastModified;

        /*package*/ StatInfo(String name, int uid, int gid, boolean directory, boolean link, String linkTarget, int access, Date lastModified) {
            this.name = name;
            this.gid = gid;
            this.uid = uid;
            this.access = access & ACCESS_MASK;
            this.directory = directory;
            this.link = link;
            this.linkTarget = linkTarget;
            this.lastModified = lastModified;
        }
        
        public int getAccess() {
            return access;
        }

        public int getGropupId() {
            return gid;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public String getLinkTarget() {
            return linkTarget;
        }

        public String getName() {
            return name;
        }

        public int getUserId() {
            return uid;
        }

        public boolean isDirectory() {
            return directory;
        }

        public boolean isLink() {
            return link;
        }
        
        public String toExternalForm() {
            StringBuilder sb = new StringBuilder();
            sb.append(escape(name)).append(' '); // 0
            sb.append(accessToString(access)).append(' '); // 1
            sb.append(directory).append(' '); // 2
            sb.append(link).append(' '); // 3
            sb.append(gid).append(' '); // 4
            sb.append(uid).append(' '); // 5
            sb.append(lastModified.getTime()).append(' '); // 6
            sb.append(escape(linkTarget)).append(' '); // 7
            return sb.toString();
        }
        
        public static StatInfo fromExternalForm(String externalForm) {
            String[] parts = externalForm.split(" +"); // NOI18N
            String name = unescape(parts[0]);
            int acc = stringToAcces(parts[1]);
            boolean dir = Boolean.parseBoolean(parts[2]);
            boolean link = Boolean.parseBoolean(parts[3]);
            int gid = Integer.parseInt(parts[4]);
            int uid = Integer.parseInt(parts[5]);
            long time = Long.parseLong(parts[6]);
            String linkTarget = unescape(parts[7]);
            return new StatInfo(name, uid, gid, dir, link, linkTarget, acc, new Date(time));
        }
        
        private boolean can(ExecutionEnvironment env, short all_mask, short grp_mask, short usr_mask) {
            if ((access & all_mask) > 0) {
                return true;
            }
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                    if ((access & usr_mask) > 0) {
                        if (this.uid == hostInfo.getUserId()) {
                            return true;
                        }
                    }
                    if ((access & grp_mask) > 0) {
                        for (int currGid : hostInfo.getAllGroupIDs()) {
                            if (gid == currGid) {
                                return true;
                            }
                        }
                    }
                } catch (IOException ex) {
                    // should be never thrown, since we checked isHostInfoAvailable() first
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    // should be never thrown, since we checked isHostInfoAvailable() first
                    // however we never report CancellationException
                }
            }
            return false;
        }

        public boolean canRead(ExecutionEnvironment env) {
            return can(env, ALL_R, GRP_R, USR_R);
        }

        
        public boolean canWrite(ExecutionEnvironment env) {
            return can(env, ALL_W, GRP_W, USR_W);
        }

        public boolean canExecute(ExecutionEnvironment env) {
            return can(env, ALL_X, GRP_X, USR_X);
        }        

        @Override
        public String toString() {
            return name + ' ' + uid + ' ' + gid + ' '+ accessToString(access) + ' ' + directory + ' ' + lastModified + ' ' + (link ? " -> " + linkTarget : ""); // NOI18N
        }
        
        
    }
    
    public static Future<StatInfo> stat(ExecutionEnvironment env, String absPath) {
        return SftpSupport.getInstance(env).stat(absPath, new PrintWriter(System.err));
    }
    
    public static Future<StatInfo> stat(ExecutionEnvironment env, String absPath, Writer error) {
        return SftpSupport.getInstance(env).stat(absPath, error);
    }

    public static Future<StatInfo[]> ls(ExecutionEnvironment env, String absPath) {
        return ls(env, absPath, new PrintWriter(System.err));
    }
        
    public static Future<StatInfo[]> ls(ExecutionEnvironment env, String absPath, Writer error) {
        return SftpSupport.getInstance(env).ls(absPath, error);
    }
        
    private static final short ACCESS_MASK = 0x1FF;
    private static final short USR_R = 256;
    private static final short USR_W = 128;
    private static final short USR_X = 64;
    private static final short GRP_R = 32;
    private static final short GRP_W = 16;
    private static final short GRP_X = 8;
    private static final short ALL_R = 4;
    private static final short ALL_W = 2;
    private static final short ALL_X = 1;    
    
    private static short stringToAcces(String accessString) {
        if (accessString.length() < 9) {
            throw new IllegalArgumentException("wrong access string: " + accessString); // NOI18N
        }
        short result = 0;

        result |= (accessString.charAt(0) == 'r') ? USR_R : 0;
        result |= (accessString.charAt(1) == 'w') ? USR_W : 0;
        result |= (accessString.charAt(2) == 'x') ? USR_X : 0;

        result |= (accessString.charAt(3) == 'r') ? GRP_R : 0;
        result |= (accessString.charAt(4) == 'w') ? GRP_W : 0;
        result |= (accessString.charAt(5) == 'x') ? GRP_X : 0;

        result |= (accessString.charAt(6) == 'r') ? ALL_R : 0;
        result |= (accessString.charAt(7) == 'w') ? ALL_W : 0;
        result |= (accessString.charAt(8) == 'x') ? ALL_X : 0;

        return result;
    }    
    
    private static String accessToString(int access) {
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
    
    private static String escape(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            text = text.replace(" ", "\\ "); // NOI18N
            return text;
        }
    }

    private static String unescape(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            text = text.replace("\\ ", " "); // NOI18N
            return text;
        }
    }
}
