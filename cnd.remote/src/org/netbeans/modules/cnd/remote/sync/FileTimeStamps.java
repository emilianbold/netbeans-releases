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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author Vladimir Kvashin
 */
public class FileTimeStamps {

    private final Properties data;
    private final File dataFile;
    private final ExecutionEnvironment executionEnvironment;

    public FileTimeStamps(File privProjectStorageDir, ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
        data = new Properties();
        String dataFileName = "timestamps-" + executionEnvironment.getHost() + //NOI18N
                '-' + executionEnvironment.getUser()+ //NOI18N
                '-' + executionEnvironment.getSSHPort(); //NOI18N
        dataFile = new File(privProjectStorageDir, dataFileName);
        if (dataFile.exists()) {
            try {
                long time = System.currentTimeMillis();
                final FileInputStream is = new FileInputStream(dataFile);
                data.load(new BufferedInputStream(is));
                is.close();
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) {
                    time = System.currentTimeMillis() - time;
                    System.out.printf("\treading %d timestamps from %s took %d ms\n", data.size(), dataFile.getAbsolutePath(), time); // NOI18N
                }
            } catch (IOException e) {
                e.printStackTrace();
                data.clear();
            }
        }
    }

    public boolean isChanged(File file) {
        String strValue = data.getProperty(getFileKey(file), "-1");
        long lastTimeStamp;
        try {
            lastTimeStamp = Long.parseLong(strValue);
        } catch (NumberFormatException nfe) {
            lastTimeStamp = -1;
        }
        long currTimeStamp = file.lastModified();
        return currTimeStamp != lastTimeStamp;
    }

    public void rememberTimeStamp(File file) {
        data.put(getFileKey(file), Long.toString(file.lastModified()));
    }

    public void dropTimeStamp(File file) {
        data.put(getFileKey(file), Long.MIN_VALUE);
    }

    private String getFileKey(File file) {
        return file.getAbsolutePath();
    }

    public void flush()  {
        File dir = dataFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs(); // no ret value check - the code below will throw exception
        }
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(dataFile));
            data.store(os, null);
            os.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            dataFile.delete();
        }
    }
}
