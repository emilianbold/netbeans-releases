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

package org.netbeans.modules.web.client.javascript.debugger.filesystem;

import java.net.URL;
import java.util.ArrayList;
import java.util.WeakHashMap;
import org.openide.filesystems.FileObject;

/**
 *
 * @author quynguyen
 */
public final class URLFileObjectFactory {
    private static WeakHashMap<URLContentProvider, URLFileSystem> fsCache;

    public static synchronized URLFileSystem getFileSystem(URLContentProvider provider) {
        if (fsCache == null) {
            fsCache = new WeakHashMap<URLContentProvider, URLFileSystem>();
        }

        URLFileSystem fs = fsCache.get(provider);

        if (fs == null) {
            fs = new URLFileSystem();
            fs.setContentProvider(provider);

            Object src = provider.getSource();
            if (src != null) {
                fs.setSessionId(src.toString());
            }

            fsCache.put(provider, fs);
        }

        return fs;
    }

    public static synchronized URLFileObject getFileObject(URLContentProvider provider, URL contentURL) {
        URLFileSystem fs = getFileSystem(provider);
        
        String externalForm = contentURL.toExternalForm();
        if (externalForm.endsWith("/")) {
            externalForm = externalForm.substring(0, externalForm.length()-1);
        }
        
        URLFileObject result = (URLFileObject)fs.findResource(externalForm);
        if (result == null) {
            return fs.addURL(contentURL);
        }else {
            return result;
        }
    }

    public static synchronized URLFileObject findFileObject(URLContentProvider provider, URL contentURL) {
        URLFileSystem fs = getFileSystem(provider);
        
        String externalForm = contentURL.toExternalForm();
        if (externalForm.endsWith("/")) {
            externalForm = externalForm.substring(0, externalForm.length()-1);
        }
        
        return (URLFileObject)fs.findResource(externalForm);
    }
    
    public static synchronized FileObject[] findFileObjects(URL url) {
        if (fsCache == null || fsCache.size() == 0) {
            return null;
        }else {
            ArrayList<FileObject> matchedObjects = new ArrayList<FileObject>();

            String externalForm = url.toExternalForm();
            for (URLFileSystem fs : fsCache.values()) {
                if (fs != null) {
                    FileObject fo = fs.findResource(externalForm);
                    if (fo != null) {
                        matchedObjects.add(fo);
                    }
                }
            }

            return matchedObjects.toArray(new FileObject[matchedObjects.size()]);
        }
    }
}
