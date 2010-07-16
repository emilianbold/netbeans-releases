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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.facelets;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.jsf.editor.index.JsfBinaryIndexer;
import org.netbeans.modules.web.jsf.editor.tld.LibraryDescriptorException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * Access to facelet library descriptors in bundled web.jsf20 library's jsf-impl.jar
 * Also provides some useful methods for getting default library's displayname or
 * default prefix.
 *
 * @author marekfukala
 */
public class DefaultFaceletLibraries {

    public static DefaultFaceletLibraries INSTANCE;
    private Collection<FileObject> libraryDescriptorsFiles;
    private Map<String, FaceletsLibraryDescriptor> librariesDescriptors;

    public static synchronized DefaultFaceletLibraries getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultFaceletLibraries();
        }
        return INSTANCE;
    }

    public DefaultFaceletLibraries() {
        init();
    }

    private void init() {
        File jsfImplJar = InstalledFileLocator.getDefault().locate(
                "modules/ext/jsf-2_0/jsf-impl.jar", //NOI18N
                "org.netbeans.modules.web.jsf20", false); //NOI18N
        assert jsfImplJar != null;

        FileObject jsfImplJarFo = FileUtil.getArchiveRoot(FileUtil.toFileObject(jsfImplJar));
        libraryDescriptorsFiles = JsfBinaryIndexer.findLibraryDescriptors(jsfImplJarFo, ".taglib.xml"); //NOI18N

    }

    public Collection<FileObject> getLibrariesDescriptorsFiles() {
        return this.libraryDescriptorsFiles;
    }

    public synchronized Map<String, FaceletsLibraryDescriptor> getLibrariesDescriptors() {
        if(librariesDescriptors == null) {
            librariesDescriptors = new HashMap<String, FaceletsLibraryDescriptor>();
            parseLibraries();
        }
        return librariesDescriptors;
    }

    private void parseLibraries() {
        for(FileObject lfo : getLibrariesDescriptorsFiles()) {
            FaceletsLibraryDescriptor descritor;
            try {
                descritor = FaceletsLibraryDescriptor.create(lfo);
                librariesDescriptors.put(descritor.getURI(), descritor);
            } catch (LibraryDescriptorException ex) {
                Logger.global.log(Level.WARNING, "Error parsing facelets library " +
                        FileUtil.getFileDisplayName(lfo) + " in jsf-impl.jar from bundled web.jsf20 library", ex);
            }
        }

    }


    private static Map<String, LibraryInfo> LIBRARY_INFOS;

    private static synchronized LibraryInfo getLibraryInfo(String libraryUri) {
        if(LIBRARY_INFOS == null) {
            LIBRARY_INFOS = new HashMap<String, LibraryInfo>();

            LIBRARY_INFOS.put("http://java.sun.com/jsf/facelets", new LibraryInfo("Facelets", "ui")); //NOI18N
            LIBRARY_INFOS.put("http://mojarra.dev.java.net/mojarra_ext", new LibraryInfo("Mojarra Extensions", "mj")); //NOI18N
            LIBRARY_INFOS.put("http://java.sun.com/jsf/composite", new LibraryInfo("Composite Components", "cc")); //NOI18N
            LIBRARY_INFOS.put("http://java.sun.com/jsf/html", new LibraryInfo("Html Basic", "h")); //NOI18N
            LIBRARY_INFOS.put("http://java.sun.com/jsf/core", new LibraryInfo("Jsf Core", "f")); //NOI18N
            LIBRARY_INFOS.put("http://java.sun.com/jsp/jstl/core", new LibraryInfo("Jstl Core", "c")); //NOI18N
        }

        return LIBRARY_INFOS.get(libraryUri);
    }

    public static String getLibraryDisplayName(String uri) {
        LibraryInfo li = getLibraryInfo(uri);
        return li != null ? li.getDisplayName() : null;
    }

    public static String getLibraryDefaultPrefix(String uri) {
        LibraryInfo li = getLibraryInfo(uri);
        return li != null ? li.getDefaultPrefix() : null;
    }

    private static class LibraryInfo {
        public String displayName;
        public String defaultPrefix;

        public LibraryInfo(String displayName, String defaultPrefix) {
            this.displayName = displayName;
            this.defaultPrefix = defaultPrefix;
        }

        public String getDefaultPrefix() {
            return defaultPrefix;
        }

        public String getDisplayName() {
            return displayName;
        }

    }
}
