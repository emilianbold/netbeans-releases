/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public class TimeStamps {

    private static String TIME_STAMPS_FILE = "timestamps.properties";           //NOI18N

    private Properties props = new Properties();

    private TimeStamps (final URL root) {
        assert root != null;
        load ();
    }
    //where
    private void load () {
        File cacheDir = getCacheDir();
        File f = new File (cacheDir,TIME_STAMPS_FILE);
        if (f.canRead()) {
            try {
                final InputStream in = new FileInputStream(f);
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                //In case of IOException props are empty, everything is scanned
                e.printStackTrace();
            }
        }
    }

    public void store () {
        File cacheDir = getCacheDir();
        File f = new File (cacheDir,TIME_STAMPS_FILE);
        try {
            OutputStream out = new FileOutputStream(f);
            try {
                props.store(out, "");
            } finally {
                out.close();
            }
        } catch (IOException e) {
            //In case of IOException props are not stored, everything is scanned next time
            e.printStackTrace();
        }
    }

    private File getCacheDir () {
        return null;
    }

    public boolean isUpToDate (final File f) {
        String relative = null;
        long fts = f.lastModified();
        String value = (String) props.setProperty(relative,Long.toString(fts));
        if (value == null) {
            return false;
        }
        long lts = Long.parseLong(value);        
        return lts >= fts;
    }

    public boolean isUpToDate (final FileObject f) {
        String relative = null;
        long fts = f.lastModified().getTime();
        String value = (String) props.setProperty(relative,Long.toString(fts));
        if (value == null) {
            return false;
        }
        long lts = Long.parseLong(value);        
        return lts >= fts;
    }

    public TimeStamps forRoot (final URL root) {
        assert root != null;
        return new TimeStamps(root);
    }

}
