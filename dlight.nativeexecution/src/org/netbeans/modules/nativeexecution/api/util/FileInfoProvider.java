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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.Future;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

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
            this.access = access;
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
        
        @Override
        public String toString() {
            return name + ' ' + uid + ' ' + gid + ' '+ accessToString(access) + ' ' + directory + ' ' + lastModified + ' ' + (link ? " -> " + linkTarget : ""); // NOI18N
        }                
    }
    
    public static Future<StatInfo> stat(ExecutionEnvironment env, String absPath) {
        return SftpSupport.stat(env, absPath, new PrintWriter(System.err));
    }
    
    public static Future<StatInfo> stat(ExecutionEnvironment env, String absPath, Writer error) {
        return SftpSupport.stat(env, absPath, error);
    }

    public static Future<StatInfo[]> ls(ExecutionEnvironment env, String absPath) {
        return ls(env, absPath, new PrintWriter(System.err));
    }
        
    public static Future<StatInfo[]> ls(ExecutionEnvironment env, String absPath, Writer error) {
        return SftpSupport.ls(env, absPath, error);
    }

    private static final short USR_R = 256;
    private static final short USR_W = 128;
    private static final short USR_X = 64;
    private static final short GRP_R = 32;
    private static final short GRP_W = 16;
    private static final short GRP_X = 8;
    private static final short ALL_R = 4;
    private static final short ALL_W = 2;
    private static final short ALL_X = 1;    
    
//    private static short stringToAcces(String accessString) {
//        short result = 0;
//
//        // 0-th character is file type => start with 1
//        result |= (accessString.charAt(1) == 'r') ? USR_R : 0;
//        result |= (accessString.charAt(2) == 'w') ? USR_W : 0;
//        result |= (accessString.charAt(3) == 'x') ? USR_X : 0;
//
//        result |= (accessString.charAt(4) == 'r') ? GRP_R : 0;
//        result |= (accessString.charAt(5) == 'w') ? GRP_W : 0;
//        result |= (accessString.charAt(6) == 'x') ? GRP_X : 0;
//
//        result |= (accessString.charAt(7) == 'r') ? ALL_R : 0;
//        result |= (accessString.charAt(8) == 'w') ? ALL_W : 0;
//        result |= (accessString.charAt(9) == 'x') ? ALL_X : 0;
//
//        return result;
//    }    
    
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
    
}
