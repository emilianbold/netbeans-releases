/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.versioning.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander Simon
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    private static Map<VCSFileProxy, Charset> fileToCharset;


    /**
     * Retrieves the Charset for the referenceFile and associates it weakly with
     * the given file. A following getAssociatedEncoding() call for the file
     * will then return the referenceFile-s Charset.
     *
     * @param referenceFile the file which charset has to be used when encoding
     * file
     * @param file file to be encoded with the referenceFile-s charset
     *
     */
    public static void associateEncoding(VCSFileProxy referenceFile, VCSFileProxy file) {
        FileObject fo = referenceFile.toFileObject();
        if (fo == null || fo.isFolder()) {
            return;
        }
        Charset c = FileEncodingQuery.getEncoding(fo);
        if (c == null) {
            return;
        }
        if (fileToCharset == null) {
            fileToCharset = new WeakHashMap<>();
        }
        synchronized (fileToCharset) {
            fileToCharset.put(file, c);
        }
    }

    /**
     * Returns a charset for the given file if it was previously registered via
     * associateEncoding()
     *
     * @param fo file for which the encoding has to be retrieved
     * @return the charset the given file has to be encoded with
     */
    public static Charset getAssociatedEncoding(FileObject fo) {
        try {
            if (fileToCharset == null || fileToCharset.isEmpty() || fo == null || fo.isFolder()) {
                return null;
            }
            VCSFileProxy file = VCSFileProxy.createFileProxy(fo);
            if (file == null) {
                return null;
            }
            synchronized (fileToCharset) {
                return fileToCharset.get(file);
            }
        } catch (Throwable t) {
            LOG.log(Level.INFO, null, t);

            return null;
        }
    }

    /**
     * Helper method to get an array of Strings from preferences.
     *
     * @param prefs storage
     * @param key key of the String array
     * @return List<String> stored List of String or an empty List if the key was not found (order is preserved)
     */
    public static List<String> getStringList (Preferences prefs, String key) {
        List<String> retval = new ArrayList<String>();
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key)) {
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1)); //NOI18N
                    retval.add(idx + "." + prefs.get(k, null)); //NOI18N
                }
            }
            List<String> rv = new ArrayList<String>(retval.size());
            rv.addAll(retval);
            for (String s : retval) {
                int pos = s.indexOf('.');
                int index = Integer.parseInt(s.substring(0, pos));
                rv.set(index, s.substring(pos + 1));
            }
            return rv;
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
            return new ArrayList<String>(0);
        }
    }

    
    /**
     * Stores a List of Strings into Preferences node under the given key.
     *
     * @param prefs storage
     * @param key key of the String array
     * @param value List of Strings to write (order will be preserved)
     */
    public static void put (Preferences prefs, String key, List<String> value) {
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key + ".")) { //NOI18N
                    prefs.remove(k);
                }
            }
            int idx = 0;
            for (String s : value) {
                prefs.put(key + "." + idx++, s); //NOI18N
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
        }
    }
    
}
