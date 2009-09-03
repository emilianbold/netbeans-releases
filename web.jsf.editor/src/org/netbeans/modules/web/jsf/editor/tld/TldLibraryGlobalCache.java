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
package org.netbeans.modules.web.jsf.editor.tld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

/**
 * TODO use indexing
 *
 * @author marekfukala
 */
public class TldLibraryGlobalCache {

    private static TldLibraryGlobalCache INSTANCE;

    public static synchronized TldLibraryGlobalCache getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new TldLibraryGlobalCache();
        }
        return INSTANCE;
    }
    //classpath entry -> libraries map
    private final Map<FileObject, Collection<TldLibrary>> LIBRARIES = new WeakHashMap<FileObject, Collection<TldLibrary>>();
    private Collection<TldLibrary> DEFAULT_LIBRARIES;

    public Collection<TldLibrary> getLibraries(FileObject classpathRoot) {
        synchronized (LIBRARIES) {
            Collection<TldLibrary> cached = LIBRARIES.get(classpathRoot);
            if (cached == null) {
                //no entry for this jar so far
                LIBRARIES.put(classpathRoot, findLibraries(classpathRoot));
                cached = LIBRARIES.get(classpathRoot);
            }
            return cached;
        }
    }

    private Collection<TldLibrary> findLibraries(FileObject classpathRoot) {
        List<TldLibrary> libs = new ArrayList<TldLibrary>();
        for (FileObject file : findLibraryDescriptors(classpathRoot)) {
            try {
                //found library, create a new instance and cache it
                libs.add(TldLibrary.create(file));
            } catch (TldLibraryException ex) {
                Logger.global.info(ex.getMessage());
            }
        }
        return libs;
    }

    public static Collection<FileObject> findLibraryDescriptors(FileObject classpathRoot) {
        Collection<FileObject> files = new ArrayList<FileObject>();
        Enumeration<? extends FileObject> fos = classpathRoot.getFolders(false);
        while (fos.hasMoreElements()) {
            FileObject fo = fos.nextElement();
            if ("META-INF".equals(fo.getName())) { //NOI18N
                //look for tag library definition files (.taglib.xml)
                for (FileObject file : fo.getChildren()) {
                    if (file.getNameExt().toLowerCase(Locale.US).endsWith(".tld")) { //NOI18N
                        //found library, create a new instance and cache it
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    public synchronized Collection<TldLibrary> getDefaultLibraries() {
        if (DEFAULT_LIBRARIES == null) {
            DEFAULT_LIBRARIES = new ArrayList<TldLibrary>();
            try {
                DEFAULT_LIBRARIES.add(
                        TldLibrary.create(this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/web/jsf/editor/resources/composite.tld"))); //NOI18N
                DEFAULT_LIBRARIES.add(
                        TldLibrary.create(this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/web/jsf/editor/resources/ui.tld"))); //NOI18N
            } catch (TldLibraryException ex) {
                //warn user, this should not happen
                Logger.global.warning(ex.getMessage());
            }
        }
        return DEFAULT_LIBRARIES;
    }
}
