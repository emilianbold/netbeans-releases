/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class Md5checker {

    public static enum Result {
        INEXISTENT,
        UPTODATE,
        DIFFERS
    }

    public static class CheckSumException extends Exception {
        public CheckSumException(String message) {
            super(message);
        }
    }

    private final ExecutionEnvironment executionEnvironment;

    public Md5checker(ExecutionEnvironment env) {
        this.executionEnvironment = env;
    }

    public Result check(File localFile, String remotePath)
            throws NoSuchAlgorithmException, IOException, CheckSumException {

        // Find out remote command for calculating md5 sum

        String cmd;
        String[] args;

        final HostInfo hostIinfo = HostInfoUtils.getHostInfo(executionEnvironment);
        if (hostIinfo == null) {
            throw new CheckSumException("Can not get HostInfo for " + executionEnvironment); // NOI18N
        }
        final OSFamily oSFamily = hostIinfo.getOSFamily();
        switch (oSFamily) {
            case LINUX:
                cmd = "/usr/bin/md5sum"; // NOI18N
                args = new String[] { "-b", remotePath };
                break;
            case SUNOS:
                cmd = "/usr/bin/digest"; // NOI18N
                args = new String[] {"-a", "md5", remotePath }; //NOI18N
                break;
            default:
                throw new NoSuchAlgorithmException("Unexpected OS: " + oSFamily); // NOI18N
        }
        // Get remote check sum
        ExitStatus res = ProcessUtils.execute(executionEnvironment, cmd, args);
        String[] lines = res.output.split("\n"); // NOI18N
        if (lines.length < 1 || lines[0].length() < 1) {
            //throw new CheckSumException("The output of the '" + cmd + "' command is empty"); //NOI18N
            // TODO: should we check existence via a separate command?
            // it's easy to do, but will take some execution timem while result will be the same: copy the file
            return Result.INEXISTENT;
        }
        String line = lines[0];
        String[] parts = line.split(" "); // NOI18N
        if (parts.length == 0) {
            throw new CheckSumException("Line shouldn't be empty"); // NOI18N
        }
        String remoteCheckSum = parts[0];
        String localCheckSum = getLocalChecksum(localFile);
        if (remoteCheckSum.equals(localCheckSum)) {
            return Result.UPTODATE;
        }
        return Result.DIFFERS;
    }

    private String getLocalChecksum(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        if (file != null && file.exists()) {
            MessageDigest md5 = MessageDigest.getInstance("MD5"); // NOI18N
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            try {
                byte[] buf = new byte[8192];
                int read;
                while ((read = is.read(buf)) != -1) {
                    md5.update(buf, 0, read);
                }
            } finally {
                is.close();
            }
            byte[] checkSum = md5.digest();
            return toHexString(checkSum);
        } else {
            return null;
        }
    }


    private static String toHexString(byte[] data) {
        char[] result = new char[data.length*2];
        for (int i = 0; i < data.length; i++) {
            //buf.append(String.format("%x", data[i]));
            for (int j = 0; j < 2; j++) {
                int half = (j == 0) ? (data[i] & 0x0F0) >>> 4 : data[i] & 0x0F;
                if (0 <= half && half <= 9) {
                    result[2*i+j] = (char) ('0' + half);
                } else {
                    result[2*i+j] = (char) ('a' + (half - 10));
                }
            }
        }
        return new String(result);
    }
}
