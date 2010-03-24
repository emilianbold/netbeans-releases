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
package org.netbeans.modules.web.common.api;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Various web utilities
 *
 * @author marekfukala
 */
public class WebUtils {

    static boolean UNIT_TESTING = false;
    static FileObject WEB_ROOT;

    /**
     * Resolves the relative or absolute link from the base file
     *
     * @param source The base file
     * @param importedFileName the link
     * @return
     */
    public static FileObject resolve(FileObject source, String importedFileName) {
        FileReference ref = resolveToReference(source, importedFileName);
        return ref == null ? null : ref.target();
    }

    /**
     * Resolves the relative or absolute link from the base file
     *
     * @param source The base file
     * @param importedFileName the link
     * @return FileReference instance which is a reference descriptor
     */
    public static FileReference resolveToReference(FileObject source, String importedFileName) {
        try {
            URI u = new URI(importedFileName);
            File file = null;

            if (u.isAbsolute()) {
                //do refactor only file resources
                if ("file".equals(u.getScheme())) { //NOI18N
                    try {
                        //the IAE is thrown for invalid URIs quite frequently
                        file = new File(u);
                    } catch (IllegalArgumentException iae) {
                        //no-op
                    }
                }
            } else {
                //no schema specified
                file = new File(importedFileName);
            }

            if (file != null) {

                if (!isAbsoluteFile(file)) {
                    //relative to the current file's folder - let's resolve
                    FileObject parent = source.getParent();
                    if(parent != null) {
                        FileObject resolvedFileObject = parent.getFileObject(importedFileName);
                        //test if the link is resolved to something else than the parent file,
                        //which may happen at least in the case of empty importedFileName string
                        if (resolvedFileObject != null &&
                                resolvedFileObject.isValid() &&
                                !resolvedFileObject.equals(parent)) {
                            //normalize the file (may contain xxx/../../yyy parts which
                            //causes that fileobject representing the same file are not equal
                            File resolvedFile = FileUtil.toFile(resolvedFileObject);
                            FileObject resolvedFileObjectInCanonicalForm = FileUtil.toFileObject(resolvedFile.getCanonicalFile());
                            //find out the base folder - bottom most folder of the link
                            FileObject linkBase = findRelativeLinkBase(source, importedFileName);
                            FileReference ref = new FileReference(source, resolvedFileObjectInCanonicalForm, linkBase, importedFileName, FileReferenceType.RELATIVE);
                            return ref;
                        }
                    }
                } else {
                    //absolute web path
                    FileObject webRoot = ProjectWebRootQuery.getWebRoot(source); //find web root
                    if(UNIT_TESTING) {
                        webRoot = WEB_ROOT;
                    }
                    if(webRoot != null) {
                        //resolve the link relative to the web root
                        FileObject resolved = webRoot.getFileObject(file.getAbsolutePath());
                        if (resolved != null && resolved.isValid()) {
                            FileReference ref = new FileReference(source, resolved, webRoot, importedFileName, FileReferenceType.ABSOLUTE);
                            return ref;
                        }
                    }
                }
            }

        } catch (URISyntaxException ex) {
            //simply a bad link, return null, no need to report the exception
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.INFO, "Cannot resolve import '" + importedFileName + "' from file " + source.getPath(), e); //NOI18N
        }
        return null;
    }

    //windows File.isAbsolute() workaround
    private static boolean isAbsoluteFile(File file) {
        String filePath = file.getPath();
        if(filePath.startsWith("/")) { //NOI18N
            return true;
        }

        return file.isAbsolute();
    }

    private static FileObject findRelativeLinkBase(FileObject source, String link) {
        //Example:
        //
        //  root
        //   +---A
        //       +---file0
        //       +---B
        //           +---C
        //           |   +---file1
        //           |
        //           +---D
        //               +---file2
        //
        //If there is a link ../C/file1 in file2 the bottom most folder is B
        //If there is a link ../../file0 in file2 the bottom most folder is A
        //If there is a link B/C/file1 in file0 the bottom most folder is A
        assert !source.isFolder() : "The source file " + source.getPath() + " is not a folder!"; //NOI18N
        assert !link.startsWith("/") : "The relative link " + link + "starts with a slash!"; //NOI18N
        if(link.startsWith("./")) { //NOI18N
            link = link.substring(2); //cut off the ./
        }
        StringTokenizer st = new StringTokenizer(link, "/");
        FileObject base = source.getParent();
        while(st.hasMoreTokens()) {
            String part = st.nextToken();
            if(part.equals("..")) {
                base = base.getParent();
                if(base == null) {
                    //cannot resolve
                    break;
                }
            } else {
                //we are in the ascending path part, return the current base folder
                return base;
            }
        }
        return null;

    }

    /** finds first ResultIterator of the given mimetype */
    public static ResultIterator getResultIterator(ResultIterator ri, String mimetype) {
        if (ri.getSnapshot().getMimeType().equals(mimetype)) {
            return ri;
        }
        for (Embedding e : ri.getEmbeddings()) {
            ResultIterator eri = ri.getResultIterator(e);
            if (e.getMimeType().equals(mimetype)) {
                return eri;
            } else {
                ResultIterator eeri = getResultIterator(eri, mimetype);
                if (eeri != null) {
                    return eeri;
                }
            }
        }
        return null;
    }

    public static String unquotedValue(CharSequence value) {
        CharSequence unquoted = isValueQuoted(value) ? value.subSequence(1, value.length() - 1) : value;
        return unquoted.toString();
    }

    public static boolean isValueQuoted(CharSequence value) {
        if (value.length() < 2) {
            return false;
        } else {
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"')
                    && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }

    /**
     * Returns hex color code in the #xxyyzz form.
     */
    public static String toHexCode(Color color) {
        return new StringBuilder().append('#').append(toTwoDigitsHexCode(color.getRed())).append(toTwoDigitsHexCode(color.getGreen())).append(toTwoDigitsHexCode(color.getBlue())).toString();
    }

    private static String toTwoDigitsHexCode(int code) {
        StringBuilder sb = new StringBuilder(Integer.toHexString(code));
        if (sb.length() == 1) {
            sb.insert(0, '0');
        }
        return sb.toString();
    }

    /**
     * Returns a relative path from source to target in one web-like project.
     * ProjectWebRootQuery must return the same folder for both arguments.
     *
     * @param source normalized FileObject in canonical form
     * @param target normalized FileObject in canonical form
     * @return
     */
    public static String getRelativePath(FileObject source, FileObject target) {
        if(!source.isData()) {
            throw new IllegalArgumentException("The source file " + source.getPath() + " is not a data file!");
        }
        if(!target.isData()) {
            throw new IllegalArgumentException("The target file " + target.getPath() + " is not a data file!");
        }
        if(!UNIT_TESTING) {
            FileObject root1 = ProjectWebRootQuery.getWebRoot(source);
            FileObject root2 = ProjectWebRootQuery.getWebRoot(target);
            if(root1 == null) {
                throw new IllegalArgumentException("Cannot find web root for source file " + source.getPath()); //NOI18N
            }
            if(root2 == null) {
                throw new IllegalArgumentException("Cannot find web root for target file " + target.getPath()); //NOI18N
            }
            if(!root1.equals(root2)) {
                throw new IllegalArgumentException("Source " + source.getPath() +  "and target " + //NOI18N
                        target.getPath() + " files have no common web root!"); //NOI18N
            }
        }

        //link: ../../folder/file.txt
        List<FileObject> targetPathFiles = new ArrayList<FileObject>();
        FileObject file = target;
        while ((file = file.getParent()) != null) {
            assert file.isFolder();
            targetPathFiles.add(0, file);
        }

        //now iterate the target parent's until we find a common folder
        FileObject common = null;
        file = source;
        StringBuilder link = new StringBuilder();
        while ((file = file.getParent()) != null) {
            if (targetPathFiles.contains(file)) {
                common = file;
                break;
            } else {
                link.append("../");//NOI18N
            }
        }
        if (common == null) {
            //no common ancestor
            return null;
        }

        int commonIndexInSourcePath = targetPathFiles.indexOf(common);
        assert commonIndexInSourcePath >= 0;
        assert targetPathFiles.size() > commonIndexInSourcePath;

        for (int i = commonIndexInSourcePath + 1; i < targetPathFiles.size(); i++) {
            FileObject pathMember = targetPathFiles.get(i);
            link.append(pathMember.getName());
            link.append('/'); //NOI18N
        }

        link.append(target.getNameExt());

        return link.toString();
    }

}
