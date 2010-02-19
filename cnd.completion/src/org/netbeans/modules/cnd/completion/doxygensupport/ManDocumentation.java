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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ManDocumentation {

    private static final Logger LOG = Logger.getLogger(ManDocumentation.class.getName());
    
    public static CompletionDocumentation getDocumentation(CsmObject obj) {
        if (obj instanceof CsmFunction) {
            return getDocumentation(((CsmFunction) obj).getName().toString());
        }

        return null;
    }

    public static CompletionDocumentation getDocumentation(String name) {
        return getDocumentation(name, 3/**Supposing all functions goes from chapter 3*/);
    }

    public static CompletionDocumentation getDocumentation(String name, int chapter) {
        String doc = getDocumentationForName(name, chapter);
        
        if (doc == null) {
            return null;
        }
        
        return new CompletionDocumentationImpl(doc);
    }

    public static String getDocumentationForName(String name, int chapter) {
        try {
            File cache = getCacheFile(name, chapter);

            if (cache.exists()) {
                return readFile(cache);
            }

            String doc = createDocumentationForName(name, chapter);

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
        } catch (IOException e) {
            LOG.log(Level.FINE, null, e);
        }

        return null;
    }

    public static String constructWarning(CsmObject obj) {
        if (obj instanceof CsmFunction) {
            StringBuilder w = new StringBuilder();

            if (resolvePath(manPageRelativePath("printf", 3)) == null) { // NOI18N
                w.append("<p>"); // NOI18N
                w.append(getString("MAN_NOT_INSTALLED")); // NOI18N
                w.append("</p>\n"); // NOI18N
            }

            if (!new File("/usr/bin/man2html").exists() /*TODO: should be canExecute()*/) { // NOI18N
                w.append("<p><tt>man2html</tt> "); // NOI18N
                w.append(getString("IS_REQUIRED")); // NOI18N
                w.append("</p>\n"); // NOI18N
            }

            return w.toString();
        }

        return "";
    }

    private static File getCacheDir() {
        String nbuser = System.getProperty("netbeans.user"); //XXX // NOI18N
        File cache = new File(nbuser, "var/cache/cpplite"); // NOI18N

        cache.mkdirs();

        return cache;
    }

    private static File getCacheFile(String name, int chapter) {
        File res = new File(getCacheDir(), name + "." + chapter); // NOI18N

        return res;
    }

    private static String createDocumentationForName(String name, int chapter) throws IOException {
        String text = readManPage(manPageRelativePath(name, chapter));

        if (text == null) {
            return null;
        }

        text = soElim(text, 0);

        InputStream in = new ByteArrayInputStream(text.getBytes());

        try {
            Process p = Runtime.getRuntime().exec(new String[] {
                "/usr/bin/man2html", // NOI18N
                        "-", // NOI18N
            });

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier inC = new StreamCopier("in", in, p.getOutputStream()); // NOI18N
            StreamCopier outC = new StreamCopier("out", p.getInputStream(), out); // NOI18N
            StreamCopier errC = new StreamCopier("err", p.getErrorStream(), out); // NOI18N

            inC.start();
            outC.start();
            errC.start();

            p.waitFor();

            inC.join();
            outC.join();
            errC.join();

            String result = out.toString();

            for (Iterator e = TRANSLATE.entrySet().iterator(); e.hasNext(); ) {
                Map.Entry entry = (Map.Entry) e.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                result = result.replace(key, value); //1.5 API!!!!
            }

            int htmlStart = result.indexOf("<HTML>"); // NOI18N

            if (htmlStart != (-1))
                result = result.substring(htmlStart);

            return result;
        } catch (InterruptedException e) {
            Exceptions.printStackTrace(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        return null;
    }

    private static String manPageRelativePath(String name, int chapter) {
        return "man" + chapter + "/" + name + "." + chapter; // NOI18N
    }

    private static File resolvePath(String path) {
        File f = new File("/usr/share/man/", path); // NOI18N

        if (!f.exists()) {
            f = new File("/usr/share/man/", path + ".gz"); // NOI18N
        }

        if (f.exists())
            return f;

        return null;
    }

    private static String readManPage(String relativePath) throws IOException {
        File f = resolvePath(relativePath);

        if (f == null)
            return null;

        return readFile(f);
    }

    private static final int MAX_DEPTH = 10;

    private static String soElim(String text, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IOException("Too deep includes."); // NOI18N
        }

        Pattern soPattern = Pattern.compile("^\\.so (.*)$", Pattern.MULTILINE); // NOI18N
        Matcher m = soPattern.matcher(text);
        StringBuffer result = new StringBuffer();
        int lastOccurrenceEnd = 0;

        while (m.find()) {
            result.append(text.substring(lastOccurrenceEnd, m.start()));
            lastOccurrenceEnd = m.end();

            String path = m.group(1);
            String included = readManPage(path);

            if (included != null) {
                result.append(soElim(included, depth + 1));
            } else {
                LOG.log(Level.WARNING, "Cannot resolve man page: {0}", path); // NOI18N
            }
        }

        result.append(text.substring(lastOccurrenceEnd));

        return result.toString();
    }

    private static final class StreamCopier extends Thread {

        private InputStream ins;
        private OutputStream out;

        public StreamCopier(String name, InputStream ins, OutputStream out) {
            this.ins = ins;
            this.out  = out;
        }

        public void run() {
            try {
                int read;

                while ((read = ins.read()) != (-1)) {
                    System.err.write(read);
                    out.write(read);
                }

                out.close();
            } catch (IOException e) {
                LOG.log(Level.FINE, null, e);
            }
        }
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

        public CompletionDocumentationImpl(String doc) {
            this.doc = doc;
        }

        public String getText() {
            return doc;
        }

        public URL getURL() {
            return null;
        }

        public CompletionDocumentation resolveLink(String link) {
            String[] parts = link.split("\\?"); // NOI18N

            if (parts.length != 2)
                return null;

            String[] chapterAndName = parts[1].split("\\+"); // NOI18N

            if (chapterAndName.length != 2)
                return null;

            int chapter = Integer.parseInt(chapterAndName[0]);
            String name = chapterAndName[1];

            return ManDocumentation.getDocumentation(name, chapter);
        }

        public Action getGotoSourceAction() {
            return null;
        }
    }

    private static String getString(String s) {
        return NbBundle.getBundle(ManDocumentation.class).getString(s);
    }
}