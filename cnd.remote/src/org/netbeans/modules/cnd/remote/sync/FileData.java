/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;

/**
 * Stores information about controlled files
 *
 * NB: the class is NOT thread safe
 *
 * @author Vladimir Kvashin
 */
public final class FileData {

    private final Properties data;
    private final File dataFile;

    private static final String VERSION = "1.1"; // NOI18N
    private static final String VERSION_KEY = "VERSION"; // NOI18N

    //
    //  Public stuff
    //

    public static final class FileInfo {
        public final long timestamp;
        public final FileState state;

        public FileInfo(FileState mode, long timestamp) {
            this.state = mode;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return state.toString() + timestamp;
        }

    }

    public FileData(File privProjectStorageDir, ExecutionEnvironment executionEnvironment) {
        data = new Properties();
        String dataFileName = "timestamps-" + executionEnvironment.getHost() + //NOI18N
                '-' + executionEnvironment.getUser()+ //NOI18N
                '-' + executionEnvironment.getSSHPort(); //NOI18N
        dataFile = new File(privProjectStorageDir, dataFileName);
        if (!Boolean.getBoolean("cnd.remote.timestamps.clear")) {
            try {
                load();
                if (!VERSION.equals(data.get(VERSION_KEY))) {
                    data.clear();
                }
            } catch (IOException ex) {
                data.clear();
                Exceptions.printStackTrace(ex);
            }
        }
    }

//    /**
//     * Initial filling
//     * NB: file should be absolute and NORMALIZED!
//     */
//    public void addFile(File file) {
//        CndUtils.assertNormalized(file);
//        String key = getFileKey(file);
//        FileInfo info = getFileInfo(key);
//        if (info == null) {
//            setFileInfo(file, FileState.INITIAL);
//        } else {
//            switch (info.state) {
//                case COPIED: // fall through
//                case TOUCHED:
//                    if (file.lastModified() != info.timestamp) {
//                        setFileInfo(file, FileState.INITIAL);
//                    }
//                    break;
//                case INITIAL: // fall through
//                case UNCONTROLLED:
//                    // nothing
//                    break;
//                default:
//                    CndUtils.assertTrue(false, "Unexpected state: " + info.state); //NOI18N
//                    setFileInfo(file, FileState.INITIAL);
//            }
//        }
//    }

    public FileState getState(File file) {
        FileInfo info = getFileInfo(file);
        return (info == null) ? FileState.UNCONTROLLED : info.state;
    }

    public void setState(File file, FileState state) {
        setFileInfo(file, state);
    }

    public FileInfo getFileInfo(File file) {
        return getFileInfo(getFileKey(file));
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void store()  {
        File dir = dataFile.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.printf("Error creating directory %s\n", dir.getAbsolutePath());
            }
        }
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(dataFile));
            data.setProperty(VERSION_KEY, VERSION);
            data.store(os, null);
            os.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            if (!dataFile.delete()) {
                System.err.printf("Error deleting file %s\n", dataFile.getAbsolutePath());
            }
        }
    }

    public void clear() {
        data.clear();
    }

    //
    //  Private stuff
    //

    private void load() throws IOException {
        if (dataFile.exists()) {
            long time = System.currentTimeMillis();
            final FileInputStream is = new FileInputStream(dataFile);
            BufferedInputStream bs = new BufferedInputStream(is);
            try {
                data.load(bs);
            } finally {
                bs.close();
            }
            if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) {
                time = System.currentTimeMillis() - time;
                System.out.printf("reading %d timestamps from %s took %d ms\n", data.size(), dataFile.getAbsolutePath(), time); // NOI18N
            }
        }
    }

    private FileInfo getFileInfo(String fileKey) {
        String strValue = data.getProperty(fileKey, null);
        if (strValue != null && strValue.length() > 0) {
            FileState state;
            char prefix = strValue.charAt(0);
            state = FileState.fromId(prefix);
            strValue = strValue.substring(1);
            try {
                long timeStamp = Long.parseLong(strValue);
                return new FileInfo(state, timeStamp);
            } catch (NumberFormatException nfe) {
                RemoteUtil.LOGGER.warning(String.format("Incorrect status/timestamp format \"%s\" for %s", strValue, fileKey)); //NOI18N
            }
        }
        return null;
    }

    private void setFileInfo(File file, FileState state) {
        String key = getFileKey(file);
        char prefix = state.id;
        data.put(key, String.format("%c%d", prefix, file.lastModified())); // NOI18N
    }

    private String getFileKey(File file) {
        String key = file.getAbsolutePath();
        if (!CndFileUtils.isSystemCaseSensitive()) {
            key = key.toLowerCase();
        }
        return key;
    }
}
