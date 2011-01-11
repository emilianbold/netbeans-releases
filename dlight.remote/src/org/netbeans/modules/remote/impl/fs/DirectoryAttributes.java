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
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps directory files attributes.
 * File is identified by its short name
 * @author Vladimir Kvashin
 */
public final class DirectoryAttributes {

    private final File file;
    private final Map<String, Boolean> attrs = new HashMap<String, Boolean>();
    private static final int VERSION = 1;

    public  DirectoryAttributes(File file) {
        this.file = file;
    }

    public synchronized void load() throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            // check version
            String line = br.readLine();
            String prefix = "VERSION="; // NOI18N
            if (line == null || ! line.startsWith(prefix)) {
                throw wrongFormatException();
            }
            int version;
            try {
                version = Integer.parseInt(line.substring(prefix.length()));
            } catch (NumberFormatException nfe) {
                throw new IOException("wrong version format " + file.getAbsolutePath(), nfe); // NOI18N
            }
            if (version > VERSION) {
                throw new IOException("attributes file version " + version +  //NNOI18N
                        " not supported: " + file.getAbsolutePath()); //NOI18N
            }
            while ((line = br.readLine()) != null) {
                if (line.length() == 0) {
                    continue; // just in case, ignore empty lines
                }
                if (line.length() < 3) {
                    throw wrongFormatException();
                }
                if (line.charAt(line.length() - 2) != '=') {
                    throw wrongFormatException();
                }
                char c = line.charAt(line.length() - 1);
                if (c != 'w' && c != 'r') {
                    throw wrongFormatException();
                }
                String fileName = line.substring(0, line.length() - 2);
                setWritable(fileName, c == 'w');
            }
         } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    private IOException wrongFormatException() {
        return new IOException("Wrong file format " + file.getAbsolutePath()); //NOI18N)
    }

    public synchronized void store() throws IOException {
        BufferedWriter wr = null;
        try {
            wr = new BufferedWriter(new FileWriter(file));
            wr.write("VERSION=" + VERSION + "\n"); //NOI18N
            for (Map.Entry<String, Boolean> entry : attrs.entrySet()) {
                wr.write(entry.getKey());
                wr.write('=');
                wr.write(entry.getValue().booleanValue() ? 'w' : 'r');
                wr.write('\n');
            }
        } finally {
            if (wr != null) {
                wr.close();
            }
        }
    }

    /** */
    public synchronized boolean isWritable(String fileName) {
        Boolean result = attrs.get(fileName);
        return (result == null) ? true : result.booleanValue();
    }

    public synchronized boolean exists(String fileName) {
        // TODO: in this case it's time to get rid of empty files!
        return Boolean.valueOf(attrs.containsKey(fileName));
    }

    /** */
    public synchronized void setWritable(String fileName, boolean writable) {
        attrs.put(fileName, Boolean.valueOf(writable));
    }
}
