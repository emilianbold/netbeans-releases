/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.javadoc.search;

import java.io.*;
import java.lang.ref.*;
import java.text.Collator;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.text.html.parser.*;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;

import org.openide.util.RequestProcessor;


/**
 * Builds index of Javadoc filesystems.
 * @author Svata Dedic, Jesse Glick
 */
public class IndexBuilder implements Runnable, ChangeListener {

    private static final String[] INDEX_FILE_NAMES = {
        "overview-summary.html", // NOI18N
        "index.html", // NOI18N
        "index.htm", // NOI18N
    };

    private static IndexBuilder INSTANCE;

    private static RequestProcessor.Task    task;
    
    private static final ErrorManager err =
            ErrorManager.getDefault().getInstance("org.netbeans.modules.javadoc.search.IndexBuilder"); // NOI18N;
    
    private Reference cachedData;
    
    private JavadocRegistry jdocRegs;

    /**
     * WeakMap<FileSystem : info> of information extracted from filesystems.
     */
    Map     filesystemInfo = Collections.EMPTY_MAP;

    private static class Info {
        /**
         * Display name / title of the helpset
         */
        String      title;

        /**
         * Name of the index/overview file
         */
        String      indexFileName;
    }

    private IndexBuilder() {
        this.jdocRegs = JavadocRegistry.getDefault();
        this.jdocRegs.addChangeListener(this);
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("new IndexBuilder");
        }
    }

    /**
     * Get the default index builder instance.
     * It will start parsing asynch.
     */
    public synchronized static IndexBuilder getDefault() {
        if (INSTANCE != null)
            return INSTANCE;
        INSTANCE = new IndexBuilder();
        scheduleTask();
        return INSTANCE;
    }
    
    public void run() {
        cachedData = null;
        refreshIndex();
    }
    
    public void stateChanged (ChangeEvent event) {
        scheduleTask ();
    }

    /**
     * Get the important information from the index builder.
     * Waits for parsing to complete first, if necessary.
     * @param blocking {@code true} in case the current thread should wait for result
     * @return two lists, one of String display names, the other of FileObject indices
     *      or null in case it is non blocking call and result is not ready yet.
     */
    public List[] getIndices(boolean blocking) {
        if (blocking) {
            task.waitFinished();
        } else if (!task.isFinished()) {
            return null;
        }
        
        if (cachedData != null) {
            List[] data = (List[])cachedData.get();
            if (data != null) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("getIndices (cached)");
                }
                return data;
            }
        }
        
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("getIndices");
        }
        Map m = this.filesystemInfo;
        Iterator it = m.entrySet().iterator();
        final Collator c = Collator.getInstance();
        class Pair implements Comparable {
            public String display;
            public FileObject fo;
            public int compareTo(Object o) {
                return c.compare(display, ((Pair)o).display);
            }
        }
        SortedSet pairs = new TreeSet(); // SortedSet<Pair>
        for (int i = 0; i < m.size(); i++) {
            Map.Entry e = (Map.Entry)it.next();
            FileObject f = (FileObject)e.getKey();
            Info info = (Info)e.getValue();
            FileObject fo = f.getFileObject(info.indexFileName);
            if (fo == null)
                continue;
            Pair p = new Pair();
            p.display = info.title;
            p.fo = fo;
            pairs.add(p);
        }
        List display = new ArrayList(pairs.size());
        List fos = new ArrayList(pairs.size());
        it = pairs.iterator();
        while (it.hasNext()) {
            Pair p = (Pair)it.next();
            display.add(p.display);
            fos.add(p.fo);
        }
        List[] data = new List[] {display, fos};
        cachedData = new WeakReference(data);
        return data;
    }

    private void refreshIndex() {
        if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
            err.log("refreshIndex");
        }
        Map oldMap;
        synchronized (this) {
            oldMap = this.filesystemInfo;
        }
        //Enumeration e = FileSystemCapability.DOC.fileSystems();
        FileObject docRoots[] = jdocRegs.getDocRoots();
        // XXX needs to be able to listen to result; when it changes, call scheduleTask()
        Map m = new WeakHashMap();
//        long startTime = System.nanoTime();

        for ( int ifCount = 0; ifCount < docRoots.length; ifCount++ ) {
            FileObject fo = docRoots[ifCount];
            Info oldInfo = (Info)oldMap.get(fo);
            if (oldInfo != null) {
                // No need to reparse.
                m.put(fo, oldInfo);
                continue;
            }
            
            FileObject index = null;
            for (int i = 0; i < INDEX_FILE_NAMES.length; i++) {
                if ((index = fo.getFileObject(INDEX_FILE_NAMES[i])) != null) {
                    break;
                }
            }
            if (index == null || index.getName().equals("index")) { // NOI18N
                // For single-package doc sets, overview-summary.html is not present,
                // and index.html is less suitable (it is framed). Look for a package
                // summary.
                // [PENDING] Display name is not ideal, e.g. "org.openide.windows (NetBeans Input/Output API)"
                // where simply "NetBeans Input/Output API" is preferable... but standard title filter
                // regexps are not so powerful (to avoid matching e.g. "Servlets (Main Documentation)").
                FileObject packageList = fo.getFileObject("package-list"); // NOI18N
                if (packageList != null) {
                    try {
                        InputStream is = packageList.getInputStream();
                        try {
                            BufferedReader r = new BufferedReader(new InputStreamReader(is));
                            String line = r.readLine();
                            if (line != null && r.readLine() == null) {
                                // Good, exactly one line as expected. A package name.
                                String resName = line.replace('.', '/') + "/package-summary.html"; // NOI18N
                                FileObject pindex = fo.getFileObject(resName);
                                if (pindex != null) {
                                    index = pindex;
                                }
                                // else fall back to index.html if available
                            }
                        } finally {
                            is.close();
                        }
                    } catch (IOException ioe) {
                            // Oh well, skip this one.
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                    }
                }
            }
            if (index != null) {
                // Try to find a title.
                String title = parseTitle(index);
                if (title != null) {
                    JavadocSearchType st = jdocRegs.findSearchType( fo );
                    if (st == null)
                        continue;
                    title = st.getOverviewTitleBase(title);
                }
                if (title == null || "".equals(title)) { // NOI18N
                    String filename = FileUtil.getFileDisplayName(index);
                    if (filename.length() > 54) {
                        // trim to display 54 chars
                        filename = filename.substring(0, 10) + "[...]" // NOI18N
                                + filename.substring(filename.length() - 40, filename.length());
                    }
                    title = NbBundle.getMessage(IndexBuilder.class,
                            "FMT_NoOverviewTitle", new Object[] { filename }); // NOI18N
                }
                Info info = new Info();
                info.title = title == null ? fo.getName() : title;
                info.indexFileName = FileUtil.getRelativePath(fo, index);
                m.put(fo, info);
            }
            synchronized (this) {
                this.filesystemInfo = m;
            }
        }

//        long elapsedTime = System.nanoTime() - startTime;
//        System.out.println("\nElapsed time[nano]: " + elapsedTime);
    }
    
    /**
     * Attempt to find the title of an HTML file object.
     * May return null if there is no title tag, or "" if it is empty.
     */
    private String parseTitle(FileObject html) {
        String title = null;
        try {
            // #71979: html parser used again to fix encoding issues.
            // I have measured no difference if the parser or plain file reading
            // is used (#32551).
            // In case the parser is stopped as soon as it finds the title it is
            // even faster than the previous fix.
            InputStream is = new BufferedInputStream(html.getInputStream(), 1024);
            SimpleTitleParser tp = new SimpleTitleParser(is);
            try {
                tp.parse();
                title = tp.getTitle();
            } finally {
                is.close();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        return title != null? title.trim(): title;
    }

    private synchronized static void scheduleTask() {
        if (task == null) {
            task = new RequestProcessor("Javadoc Index Builder").create(getDefault()); // NOI18N
        }
        // Give it a small delay to avoid restarting too many times e.g. during
        // project switch:
        task.schedule(100);
    }

    static final class SimpleTitleParser {

        private char cc;
        private InputStream is;
        private String charset;
        private String title;
        private int state = CONTINUE;

        private static final int CONTINUE = 0;
        private static final int EXIT = 0;

        SimpleTitleParser(InputStream is) {
            this.is = is;
        }

        public String getTitle() {
            return title;
        }

        public void parse() throws IOException {
            readNext();
            while (state == CONTINUE) {
                switch (cc) {
                    case '<' : // start of tags
                        handleOpenBrace();
                        break;
                    case (char) -1 : // EOF
                        return;
                    default:
                        readNext();
                }
            }
        }

        private void readNext() throws IOException {
            cc = (char) is.read();
        }

        private void handleOpenBrace() throws IOException {
            StringBuilder sb = new StringBuilder();
            while (true) {
                readNext();
                switch (cc) {
                    case '>':  // end of tag
                        String tag = sb.toString().toLowerCase();
                        if (tag.startsWith("body")) { // NOI18N
                            state = EXIT;
                            return; // exit parsing, no title
                        } else if (tag.startsWith("meta")) { // NOI18N
                            handleMetaTag(tag);
                            return;
                        } else if (tag.startsWith("title")) { // NOI18N
                            handleTitleTag();
                            return;

                        }
                        return;
                    case (char) -1:  // EOF
                        return;
                    case ' ':
                        if (sb.length() == 0) // ignore leading spaces
                            break;
                    default:
                        sb.append(cc);
                }
            }

        }

        private void handleMetaTag(String txt) {
            // parse something like
            // <META http-equiv="Content-Type" content="text/html; charset=euc-jp">
            // see http://www.w3.org/TR/REC-html32#meta
            String name = ""; // NOI18N
            String value = ""; // NOI18N

            char tc;
            char[] txts = txt.toCharArray();
            int offset = 5; // skip "meta "
            int start = offset;
            int state = 0;
            while (offset < txts.length) {
                tc = txt.charAt(offset);
                if (tc == '=' && state == 0) { // end of name
                    name = String.valueOf(txts, start, offset++ - start).trim();
                    state = 1;
                } else if (state == 1 && (tc == '"' || tc == '\'')) { // start of value
                    start = ++offset;
                    state = 2;
                } else if (state == 2 && (tc == '"' || tc == '\'')) { // end of value
                    value = String.valueOf(txts, start, offset++ - start);
                    if ("content".equals(name)) { // NOI18N
                        break;
                    }
                    name = ""; // NOI18N
                    state = 0;
                    start = offset;
                } else {
                    ++offset;
                }

            }

            StringTokenizer tk = new StringTokenizer(value, ";"); // NOI18N
            while (tk.hasMoreTokens()) {
                String str = tk.nextToken().trim();
                if (str.startsWith("charset")) {        //NOI18N
                    str = str.substring(7).trim();
                    if (str.charAt(0) == '=') {
                        this.charset = str.substring(1).trim();
                        return;
                    }
                }
            }
        }

        private void handleTitleTag() throws IOException {
            byte[] buf = new byte[200];
            int offset = 0;
            while (true) {
                readNext();
                switch (cc) {
                    case (char) -1:  // EOF
                        return;
                    case '>': // </title>
                        if ("</title".equals(new String(buf, offset - 7, 7).toLowerCase())) {
                            // title is ready
                            // XXX maybe we should also resolve entities like &gt;
                            state = EXIT;
                            if (charset == null) {
                                title = new String(buf, 0, offset - 7).trim();
                            } else {
                                title = new String(buf, 0, offset - 7, charset).trim();
                            }
                            return;
                        }
                    default:
                        cc = (cc == '\n' || cc == '\r')? ' ': cc;
                        if (offset == buf.length) {
                            buf = enlarge(buf);
                        }
                        buf[offset++] = (byte) cc;

                }
            }
        }

        private static byte[] enlarge(byte[] b) {
            byte[] b2 = new byte[b.length + 200];
            for (int i = 0; i < b.length; i++) {
                b2[i] = b[i];
            }
            return b2;
        }
    }

}
