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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.completion.doxygensupport;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeExitStatus;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author thp
 */
public class ManDocumentation {

//    private static final Logger LOG = Logger.getLogger(ManDocumentation.class.getName());
//    private static String manPath = null;
//
//    private static String getPath(String cmd) {
//        String path = null;
//        path = Path.findCommand(cmd);
//        if (path == null) {
//            if (new File("/usr/bin/" + cmd).exists()) { // NOI18N
//                path = "/usr/bin/" + cmd; // NOI18N
//            }
//        }
//        if (path == null) {
//            if (new File("/bin/" + cmd).exists()) { // NOI18N
//                path = "/bin/" + cmd; // NOI18N
//            }
//        }
//        return path;
//    }
//
//    private static String getManPath() {
//        if (manPath == null) {
//            manPath = getPath("man"); // NOI18N
//        }
//        return manPath;
//    }

    public static CompletionDocumentation getDocumentation(CsmObject obj, CsmFile file) throws IOException {
        if (obj instanceof CsmFunction) {
            return getDocumentation(((CsmFunction) obj).getName().toString(), file);
        }

        return null;
    }

    public static CompletionDocumentation getDocumentation(String name, CsmFile file) throws IOException {
        return getDocumentation(name, 3, file);
        /**Supposing all functions goes from chapter 3*/
    }

    public static CompletionDocumentation getDocumentation(String name, int chapter, CsmFile file) throws IOException {
        String doc = getDocumentationForName(name, chapter, file);

        if (doc == null) {
            return null;
        }

        return new CompletionDocumentationImpl(doc, file);
    }

    public static String getDocumentationForName(String name, int chapter, CsmFile file) throws IOException {
        File cache = getCacheFile(name, chapter);

        if (cache.exists()) {
            return readFile(cache);
        }

        String doc = createDocumentationForName(name, chapter, file);

        if (doc != null) {
            OutputStream out = null;

            try {
                out = new FileOutputStream(cache);

                out.write(doc.getBytes());
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            return doc;
        }

        return null;
    }

//    public static String constructWarning(CsmObject obj) {
//        if (obj instanceof CsmFunction) {
//            StringBuilder w = new StringBuilder();
//
//            if (getManPath() == null) { // NOI18N
//                w.append("<p>"); // NOI18N
//                w.append(getString("MAN_NOT_INSTALLED")); // NOI18N
//                w.append("</p>\n"); // NOI18N
//            }
//
//            return w.toString();
//        }
//
//        return "";
//    }
    private static File getCacheDir() {
        String nbuser = System.getProperty("netbeans.user"); //XXX // NOI18N
        File cache = new File(nbuser, "var/cache/cnd/manpages"); // NOI18N

        cache.mkdirs();

        return cache;
    }

    private static File getCacheFile(String name, int chapter) {
        File res = new File(getCacheDir(), name + "." + chapter); // NOI18N

        return res;
    }

    static NativeProject getNativeProject(CsmFile csmFile) {
        NativeProject nativeProject = null;
        if (csmFile != null) {
            CsmProject csmProject = csmFile.getProject();
            if (csmProject.getPlatformProject() instanceof NativeProject) {
                nativeProject = (NativeProject) csmProject.getPlatformProject();
            } else {
                loop:
                for (CsmProject project : CsmModelAccessor.getModel().projects()) {
                    for (CsmProject lib : project.getLibraries()) {
                        if (lib.equals(csmProject)) {
                            if (project.getPlatformProject() instanceof NativeProject) {
                                nativeProject = (NativeProject) project.getPlatformProject();
                                break loop;
                            }
                        }
                    }
                }
            }
        }
        return nativeProject;
    }

    private static String createDocumentationForName(String name, int chapter, CsmFile file) throws IOException {
        //NativeFileItem nfi = CsmFileInfoQuery.getDefault().getNativeFileItem(file);
        NativeProject np = getNativeProject(file);
        if (np == null) {
            return "";
        }
        NativeExitStatus exitStatus = np.execute("man", new String[]{"MANWIDTH=" + Man2HTML.MAX_WIDTH}, name); // NOI18N
        StringReader sr;
        if (exitStatus != null) {
            if (exitStatus.isOK() && exitStatus.output.length() > 0) {
                sr = new StringReader(exitStatus.output);
            } else {
                throw new IOException(exitStatus.error);
            }
        } else {
            return null;
        }
        BufferedReader br = new BufferedReader(sr);
        String text = new Man2HTML(br).getHTML();
        br.close();
        sr.close();
        return text;
    }
    private static final Map<String, String> TRANSLATE;

    static {
        TRANSLATE = new HashMap<String, String>();

        TRANSLATE.put("&minus;", "-"); // NOI18N
        TRANSLATE.put("&lsquo;", "'"); // NOI18N
        TRANSLATE.put("&rsquo;", "'"); // NOI18N
    }

    private static String readFile(File f) throws IOException {
        InputStream fin = null;
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            fin = new FileInputStream(f);

            if (f.getName().endsWith(".gz")) { // NOI18N
                in = new GZIPInputStream(fin);
            } else {
                in = fin;
            }

            FileUtil.copy(in, out);

            return out.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            try {
                out.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static final class CompletionDocumentationImpl implements CompletionDocumentation {

        private String doc;
        private CsmFile file;

        public CompletionDocumentationImpl(String doc, CsmFile file) {
            this.doc = doc;
            this.file = file;
        }

        @Override
        public String getText() {
            return doc;
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public CompletionDocumentation resolveLink(String link) {
            String[] parts = link.split("\\?"); // NOI18N

            if (parts.length != 2) {
                return null;
            }

            String[] chapterAndName = parts[1].split("\\+"); // NOI18N

            if (chapterAndName.length != 2) {
                return null;
            }

            int chapter = Integer.parseInt(chapterAndName[0]);
            String name = chapterAndName[1];

            try {
                return ManDocumentation.getDocumentation(name, chapter, file);
            } catch (IOException ioe) {
                return new CompletionDocumentationImpl(ioe.getMessage(), file);
            }
        }

        @Override
        public Action getGotoSourceAction() {
            return null;
        }
    }

    private static String getString(String s) {
        return NbBundle.getBundle(ManDocumentation.class).getString(s);
    }
}
