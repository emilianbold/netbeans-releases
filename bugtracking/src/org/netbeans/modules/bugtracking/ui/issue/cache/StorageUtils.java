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
package org.netbeans.modules.bugtracking.ui.issue.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.bugtracking.util.TextUtils;

/**
 *
 * @author tomas
 */
public final class StorageUtils {

    private StorageUtils() {}
    
    static DataOutputStream getDataOutputStream(File file, boolean append) throws IOException, InterruptedException {
        return new DataOutputStream(getFileOutputStream(file, append));
    }

    static DataInputStream getDataInputStream(File file) throws IOException, InterruptedException {
        return new DataInputStream(getFileInputStream(file));
    }

    static FileOutputStream getFileOutputStream(File file, boolean append) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new FileOutputStream(file, append);
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    static FileInputStream getFileInputStream(File file) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new FileInputStream(file);
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    static void copyStreams(OutputStream out, InputStream in) throws IOException {
        byte [] buffer = new byte[4096];
        for (;;) {
            int n = in.read(buffer);
            if (n < 0) break;
            out.write(buffer, 0, n);
        }
    }

    static File getNameSpaceFolder(File storage, String url) {
        File folder = new File(storage, TextUtils.encodeURL(url));
        if(!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    static DataOutputStream getQueryOutputStream(File queryFile) throws IOException, InterruptedException {
        return getDataOutputStream(queryFile, false);
    }

    static DataInputStream getQueryInputStream(File queryFile) throws IOException, InterruptedException {
        if(!queryFile.exists()) return null;
        return getDataInputStream(queryFile);
    }


    static class FileLocks {
        private static FileLocks instance;
        private synchronized static FileLocks getInstance() {
            if(instance == null) {
                instance = new FileLocks();
            }
            return instance;
        }
        private final Map<String, FileLock> locks = new HashMap<String, FileLock>();
        static FileLock getLock(File file) {
            synchronized(getInstance().locks) {
                FileLock fl = getInstance().locks.get(file.getAbsolutePath());
                if(fl == null) {
                    fl = getInstance().new FileLock(file);
                }
                getInstance().locks.put(file.getAbsolutePath(), fl);
                return fl;
            }
        }
        class FileLock {
            private final File file;
            public FileLock(File file) {
                this.file = file;
            }
            void release() {
                synchronized(getInstance().locks) {
                    getInstance().locks.remove(file.getAbsolutePath());
                }
            }
        }
    }

    
}
