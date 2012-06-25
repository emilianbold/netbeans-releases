/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.livehtml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.html.editor.lib.api.HtmlParseResult;
import org.netbeans.modules.html.editor.lib.api.HtmlParser;
import org.netbeans.modules.html.editor.lib.api.HtmlParserFactory;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.ParseException;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.web.livehtml.diff.Diff;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

public class Model {

    private static File storageRoot;
    private static Map<URL, Model> cache = new HashMap<URL, Model>();

    private File root;
    private ChangeSupport support = new ChangeSupport(this);
    private List<String> timestamps;
    private String dataToStore = null;
    
    private Diff lastDiff;
    
    private RequestProcessor RP = new RequestProcessor("Live HTML Model", 1);
    
    static boolean isUnitTesting = false;
            
    private static synchronized File getStorageRoot() {
        if (storageRoot == null) {
            storageRoot = getTempDirectory();
        }
        return storageRoot;
    }
    
    public static synchronized Model getModel(URL url, boolean forceNew) {
        Model model = cache.get(url);
        if (model == null || forceNew) {
            File f = getChangesStorageRoot(getStorageRoot());
            //StringBuilder content = fetchFileContent(url);
            model = new Model(f, null/*content.toString()*/);
            cache.put(url, model);
        }
        return model;
    }
    
    public static synchronized void releaseModel(URL url) {
        Model m = cache.remove(url);
        if (m != null) {
            m.deleteAll();
        }
    }

    Model(File root, String initialContent) {
        this.root = root;
        timestamps = new ArrayList<String>();
        if (initialContent != null) {
            long l = System.currentTimeMillis();
            store("content", l, initialContent);
            timestamps.add(Long.toString(l));
        }
    }
    
    public void storeDocumentVersion(final long timestamp, final String content, final String stackTrace) {
        final String data = dataToStore;
        dataToStore = null;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                store("content", timestamp, content);
                store("stacktrace", timestamp, stackTrace);
                if (data != null) {
                    store("data", timestamp, data);
                }
                int total = timestamps.size();
                if (total > 0) {
                    parse(content, timestamp);
                }
                addTimeStamp(timestamp);
            }
        };
        if (isUnitTesting) {
            r.run();
        } else {
            RP.post(r);
        }
    }
    
    public void storeDataEvent(long timestamp, String data) {
        if (dataToStore == null) {
            dataToStore = data;
        } else {
            dataToStore += "\n\n"+data;
        }
    }
    
    private void addTimeStamp(long timestamp) {
        timestamps.add(Long.toString(timestamp));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                support.fireChange();
            }
        });
    }
    
    public int getChangesCount() {
        return timestamps.size()-1;
    }

    public static Lookup getParserLookup() {
        Properties p = new Properties();
        p.setProperty("add_text_nodes", "true");
        return Lookups.fixed(p);
    }
    
    public Revision getChange(int changeIndex, boolean beautify) {
        StringBuilder content = read("content", timestamps.get(changeIndex+1));
        StringBuilder stacktrace = read("stacktrace", timestamps.get(changeIndex));
        StringBuilder data = read("data", timestamps.get(changeIndex));
        StringBuilder diff = read("diff", timestamps.get(changeIndex+1));
        StringBuilder beautifiedDiff = read("bdiff", timestamps.get(changeIndex+1));
        StringBuilder beautifiedContent = read("bcontent", timestamps.get(changeIndex+1));
        
        StringBuilder editorContent;
        List<Change> changes;
        if (beautify) {
            editorContent = beautifiedContent;
            changes = Change.decodeFromJSON(beautifiedDiff.toString());
        } else {
            editorContent = content;
            changes = Change.decodeFromJSON(diff.toString());
        }
        Revision rev = new Revision(editorContent, stacktrace, changes, data);
        return rev;
    }
    
    private void parse(String content, long timestamp) {
        int total = timestamps.size();
        StringBuilder previousContent = read("content", timestamps.get(total-1));
        HtmlParser parser = HtmlParserFactory.findParser(HtmlVersion.getDefaultVersion());
        try {
            HtmlSource s1 = new HtmlSource(previousContent);
            HtmlSource s2 = new HtmlSource(content);
            HtmlParseResult previousResult = parser.parse(s1, HtmlVersion.getDefaultVersion(), getParserLookup());
            HtmlParseResult currentResult = parser.parse(s2, HtmlVersion.getDefaultVersion(), getParserLookup());
            Diff d = new Diff(s1, s2, 
                    (OpenTag)previousResult.root().children().iterator().next(), 
                    (OpenTag)currentResult.root().children().iterator().next());
            List<Change> changes = d.compare(lastDiff, total-1);
            store("diff", timestamp, Change.encodeToJSON(changes));
            
            List<Change> beautifiedChanges = Change.decodeFromJSON(Change.encodeToJSON(changes));
            StringBuilder beautifiedContent = beautify(new HtmlSource(content), 
                    (OpenTag)currentResult.root().children().iterator().next(), beautifiedChanges);
            store("bdiff", timestamp, Change.encodeToJSON(beautifiedChanges));
            store("bcontent", timestamp, beautifiedContent.toString());
            
            lastDiff = d;
            
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot parse "+root.getAbsolutePath()+" ["+timestamp+","+timestamps.get(total-1)+"]", t);
        }
    }
    
    private StringBuilder read(String type, String timestamp) {
        File storeFile = new File(root, timestamp+"."+type);
        if (!storeFile.exists()) {
            return null;
        }
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(storeFile)));
            ZipEntry entry;
            while ( (entry = zis.getNextEntry()) != null ) {
                if( entry.getName().equals(type) ) {
                    return fetchFileContent(zis);
                }
            }
            throw new RuntimeException("should never happen");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private void store(String type, long timestamp, String content) {
        File storeFile = new File(root, Long.toString(timestamp)+"."+type);
        try {
            assert !storeFile.exists() : "should not exist yet! "+storeFile;
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(storeFile)));
            ZipEntry entry = new ZipEntry(type);
            zos.putNextEntry(entry);
            byte b[] = content.getBytes();
            zos.write(b, 0, b.length);
            zos.flush();
            zos.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static File getChangesStorageRoot(File liveHTMLRoot) {
        long fileHash = System.currentTimeMillis();
        File f = new File(liveHTMLRoot, Long.toString(fileHash % 173 + 172));   // NOI18N
        if (!f.exists()) {
            f.mkdirs();
        }
        while (true) {
            File ff = new File(f, Long.toString(fileHash));
            if (ff.exists()) {
                fileHash++;
            } else {
                ff.mkdirs();
                return ff;
            }
        }
    }

    private static File getTempDirectory() {
        File f;
        try {
            f = File.createTempFile("livehtml", "test");
        } catch (IOException ex) {
            throw new RuntimeException("cannot create temp file", ex);
        }
        return new File(f.getParentFile(), "livehtml");
    }

    private static StringBuilder fetchFileContent(URL url) {
        try {
            return fetchFileContent(url.openStream());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private static StringBuilder fetchFileContent(InputStream is) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is)); // NOI18N
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    private void deleteAll() {
        for (File f : root.listFiles()) {
            f.delete();
        }
        root.delete();
    }
    
    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }

    private StringBuilder beautify(HtmlSource htmlSource, OpenTag openTag, List<Change> changes) {
        StringBuilder sb = new StringBuilder(htmlSource.getSourceCode());
        eraseNewLines(openTag, sb);
        ArrayList<IndentChange> updates = new ArrayList<IndentChange>();
        int indent = 0;
        collectIndents(openTag, updates, indent);
        applyIndents(updates, sb, changes);
        return sb;
    }

    private void eraseNewLines(Element element, StringBuilder sb) {
        if (!(element instanceof Named) || !(element instanceof Node) || ("script".equals(((Named)element).name()))) {
            return;
        }
        Node n = (Node)element;
        for (Element e : n.children()) {
            if (e.type() == ElementType.TEXT) {
                eraseNewLinesFromElement(sb, e.from(), e.to());
            } else {
                eraseNewLines(e, sb);
            }
        }
    }
    
    private void eraseNewLinesFromElement(StringBuilder sb, int from, int to) {
        for (int i = from; i < to; i++) {
            if (sb.charAt(i) == '\n' || sb.charAt(i) == '\r'|| sb.charAt(i) == '\t') {
                sb.setCharAt(i, ' ');
            }
        }
    }

    private void collectIndents(Element element, ArrayList<IndentChange> updates, int indent) {
        if (indent != 0 && element.from() != -1) {
            updates.add(new IndentChange(element.from(), indent));
        }
        if (!(element instanceof Node)) {
            return;
        }
        Node n = (Node)element;
        for (Element e : n.children()) {
            if (e.type() == ElementType.TEXT) {
                continue;
            }
            collectIndents(e, updates, indent+1);
        }
    }

    private void applyIndents(ArrayList<IndentChange> indents, StringBuilder sb, List<Change> changes) {
        for (int i = indents.size()-1; i >= 0; i--) {
            IndentChange change = indents.get(i);
            StringBuilder indent = getIndent(change.indent);
            sb.insert(change.offset, indent);
            updateChanges(changes, change.offset, indent.length());
        }
    }

    private StringBuilder getIndent(int indent) {
        StringBuilder s = new StringBuilder("\n");
        for (int i = 0; i < indent; i++) {
            s.append(' ');
        }
        return s;
    }

    private void updateChanges(List<Change> changes, int offset, int length) {
        for (int i = changes.size()-1; i >= 0; i--) {
            Change ch = changes.get(i);
            int outerBoundary = ch.getOffset();
            if (ch.isAdd()) {
                outerBoundary = ch.getEndOffsetOfNewText();
            }
            if (ch.getOffset() >= offset) {
                ch.increment(length);
            } else if (outerBoundary != ch.getOffset() && ch.getOffset() < offset && outerBoundary > offset) {
                ch.incrementLength(length);
            }
            
            if (outerBoundary < offset) {
                break;
            }
        }
    }
    
    private static class IndentChange {
        int offset;
        int indent;

        public IndentChange(int offset, int indent) {
            this.offset = offset;
            this.indent = indent;
        }
        
    }
    
}
