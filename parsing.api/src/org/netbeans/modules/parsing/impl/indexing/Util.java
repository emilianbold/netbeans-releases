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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public final class Util {


    /*tests*/ static Set<String> allMimeTypes;

    public static Set<String> getAllMimeTypes () {
        return allMimeTypes != null ? allMimeTypes : EditorSettings.getDefault().getAllMimeTypes();
    }

    public static boolean canBeParsed(String mimeType) {
        if (mimeType == null || "content/unknown".equals(mimeType) || !Util.getAllMimeTypes().contains(mimeType)) { //NOI18N
            return false;
        }

        int slashIdx = mimeType.indexOf('/'); //NOI18N
        assert slashIdx != -1 : "Invalid mimetype: '" + mimeType + "'"; //NOI18N

        String type = mimeType.substring(0, slashIdx);
        if (type.equals("application")) { //NOI18N
            if (!mimeType.equals("application/x-httpd-eruby") && !mimeType.equals("application/xml-dtd")) { //NOI18N
                return false;
            }
        } else if (!type.equals("text")) { //NOI18N
            return false;
        }

//            if (allLanguagesParsersCount == -1) {
//                Collection<? extends ParserFactory> allLanguagesParsers = MimeLookup.getLookup(MimePath.EMPTY).lookupAll(ParserFactory.class);
//                allLanguagesParsersCount = allLanguagesParsers.size();
//            }
//            Collection<? extends ParserFactory> parsers = MimeLookup.getLookup(mimeType).lookupAll(ParserFactory.class);
//            if (parsers.size() - allLanguagesParsersCount > 0) {
//                return true;
//            }
//
//            // Ideally we should check that there are EmbeddingProviders registered for the
//            // mimeType, but let's assume that if there are TaskFactories they are either
//            // ordinary scheduler tasks or EmbeddingProviders. The former would most likely
//            // mean that there is also a Parser and would have been caught in the previous check.
//            if (allLanguagesTasksCount == -1) {
//                Collection<? extends TaskFactory> allLanguagesTasks = MimeLookup.getLookup(MimePath.EMPTY).lookupAll(TaskFactory.class);
//                allLanguagesTasksCount = allLanguagesTasks.size();
//            }
//            Collection<? extends TaskFactory> tasks = MimeLookup.getLookup(mimeType).lookupAll(TaskFactory.class);
//            if (tasks.size() - allLanguagesTasksCount > 0) {
//                return true;
//            }

        return true;
    }

    public static StackTraceElement findCaller(StackTraceElement[] elements, Object... classesToFilterOut) {
        loop: for (StackTraceElement e : elements) {
            if (e.getClassName().equals(Util.class.getName()) || e.getClassName().startsWith("java.lang.")) { //NOI18N
                continue;
            }

            if (classesToFilterOut != null && classesToFilterOut.length > 0) {
                for(Object c : classesToFilterOut) {
                    if (c instanceof Class && e.getClassName().startsWith(((Class) c).getName())) {
                        continue loop;
                    } else if (c instanceof String && e.getClassName().startsWith((String) c)) {
                        continue loop;
                    }
                }
            } else {
                if (e.getClassName().startsWith("org.netbeans.modules.parsing.")) { //NOI18N
                    continue;
                }
            }

            return e;
        }
        return null;
    }

    /**
     * Resolves URL within a root.
     * @param root the root to resolve the URL in.
     * @param relativePath the relative path under the root.
     * @param isDirectory true if the relativePath is known to point to directory,
     * false if the relativePath is known to point to file, null if nothing is known
     * about the target.
     * @return
     * @throws MalformedURLException 
     * @throws IllegalStateException when file ends with '/'
     */
    public static URL resolveUrl(
            @NonNull final URL root,
            @NonNull final String relativePath,
            @NullAllowed Boolean isDirectory) throws MalformedURLException, IllegalStateException {
        try {
            if ("file".equals(root.getProtocol())) { //NOI18N
                if (isDirectory == Boolean.FALSE &&
                    (relativePath.isEmpty() || relativePath.charAt(relativePath.length()-1) == '/')) {  //NOI18N
                    throw new IllegalStateException(
                        MessageFormat.format("relativePath: {0}", relativePath));   //NOI18N
                }
                final Boolean isDirectoryFin = isDirectory;
                // Performance optimization for File.toURI() which calls this method
                // and the original implementation calls into native method
                return Utilities.toURI(new File(Utilities.toFile(root.toURI()), relativePath) {
                    
                    @Override
                    public File getAbsoluteFile() {
                        if (isAbsolute()) {
                            return this;
                        } else {
                            return super.getAbsoluteFile();
                        }
                    }
                                                            
                    @Override
                    public boolean isDirectory() {
                        return isDirectoryFin == null ?
                            super.isDirectory() :
                            isDirectoryFin;
                    }
                }).toURL();
            } else {
                return new URL(root, relativePath);
            }
        } catch (URISyntaxException use) {
            MalformedURLException mue = new MalformedURLException("Can't resolve URL: root=" + root + ", relativePath=" + relativePath); //NOI18N
            mue.initCause(use);
            throw mue;
        }
    }

    public static boolean containsAny(Collection<? extends String> searchIn, Collection<? extends String> searchFor) {
        if (searchIn != null && searchFor != null) {
            for(String s : searchFor) {
                if (searchIn.contains(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static FileObject getFileObject(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof FileObject) {
            return (FileObject)sdp;
        }
        if (sdp instanceof DataObject) {
            return ((DataObject)sdp).getPrimaryFile();
        }
        return null;
    }

    public static boolean isParentOf(
            @NonNull final File folder,
            @NonNull final File file) {
        return file.getAbsolutePath().startsWith(folder.getAbsolutePath());
    }

    private Util() {
    }
}
