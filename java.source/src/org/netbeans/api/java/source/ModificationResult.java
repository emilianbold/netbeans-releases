/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.api.java.source;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.Log;
import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.source.ModificationResult.CreateChange;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavaSourceSupportAccessor;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.SourceFileManager;
import org.netbeans.modules.java.source.save.ElementOverlay;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Class that collects changes built during a modification task run.
 *
 * @author Dusan Balek
 */
public final class ModificationResult {

    private static final Logger LOG = Logger.getLogger(ModificationResult.class.getName());
    
    private Collection<Source> sources;
    private boolean committed;
    Map<FileObject, List<Difference>> diffs = new HashMap<FileObject, List<Difference>>();
    Map<?, int[]> tag2Span = new IdentityHashMap<Object, int[]>();
    
    /** Creates a new instance of ModificationResult */
    ModificationResult(final JavaSource js) {
        this.sources = js != null ? JavaSourceAccessor.getINSTANCE().getSources(js) : null;
    }

    private ModificationResult(Collection<Source> sources) {
        this.sources = sources;
    }

    private final Throwable creator;
    
    {
        boolean keepStackTrace = false;
        assert keepStackTrace = true;
        creator = keepStackTrace ? new Throwable() : null;
    }
    
    // API of the class --------------------------------------------------------

    /**
     * Runs a task over given sources, the task has an access to the {@link WorkingCopy}
     * using the {@link WorkingCopy#get(org.netbeans.modules.parsing.spi.Parser.Result)} method.
     * @param sources on which the given task will be performed
     * @param task to be performed
     * @return the {@link ModificationResult}
     * @throws org.netbeans.modules.parsing.spi.ParseException
     * @since 0.42
     */
    public static @NonNull ModificationResult runModificationTask(final @NonNull Collection<Source> sources, final @NonNull UserTask task) throws ParseException {
        final ModificationResult result = new ModificationResult(sources);
        final ElementOverlay overlay = ElementOverlay.getOrCreateOverlay();
        ParserManager.parse(sources, new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                resultIterator = JavacParser.MIME_TYPE.equals(resultIterator.getSnapshot().getMimeType()) ? resultIterator : findEmbeddedJava(resultIterator);
                if (resultIterator != null) {
                    Parser.Result parserResult = resultIterator.getParserResult();
                    final CompilationController cc = CompilationController.get(parserResult);
                    assert cc != null;
                    final WorkingCopy copy = new WorkingCopy (cc.impl, overlay);
                    assert WorkingCopy.instance == null;
                    WorkingCopy.instance = new WeakReference<WorkingCopy>(copy);
                    try {
                        task.run(resultIterator);
                    } finally {
                        WorkingCopy.instance = null;
                    }
                    final JavacTaskImpl jt = copy.impl.getJavacTask();
                    Log.instance(jt.getContext()).nerrors = 0;
                    final List<ModificationResult.Difference> diffs = copy.getChanges(result.tag2Span);
                    if (diffs != null && diffs.size() > 0)
                        result.diffs.put(copy.getFileObject(), diffs);
                }
            }
            private ResultIterator findEmbeddedJava(final ResultIterator theMess) throws ParseException {
                final Collection<Embedding> todo = new LinkedList<Embedding>();
                //BFS should perform better than DFS in this dark.
                for (Embedding embedding : theMess.getEmbeddings()) {
                    if (JavacParser.MIME_TYPE.equals(embedding.getMimeType()))
                        return theMess.getResultIterator(embedding);
                    else
                        todo.add(embedding);
                }
                for (Embedding embedding : todo) {
                    ResultIterator result = findEmbeddedJava(theMess.getResultIterator(embedding));
                    if (result != null)
                        return result;
                }
                return null;
            }
        });
        return result;
    }
    
    public @NonNull Set<? extends FileObject> getModifiedFileObjects() {
        return diffs.keySet();
    }
    
    public List<? extends Difference> getDifferences(@NonNull FileObject fo) {
        return diffs.get(fo);
    }
    
    public @NonNull Set<File> getNewFiles() {
        Set<File> newFiles = new HashSet<File>();
        for (List<Difference> ds:diffs.values()) {
            for (Difference d: ds) {
                if (d.getKind() == Difference.Kind.CREATE) {
                    newFiles.add(org.openide.util.Utilities.toFile(((CreateChange) d).getFileObject().toUri()));
                }
            }
        }
        return newFiles;
    }
    
    static LinkedList<Throwable> lastCommitted = new LinkedList<Throwable>();

    /**
     * Once all of the changes have been collected, this method can be used
     * to commit the changes to the source files
     */
    public void commit() throws IOException {
        if (this.committed) {
            throw new IllegalStateException ("Calling commit on already committed Modificationesult."); //NOI18N
        }
        try {
            IndexingController.getDefault().enterProtectedMode();
            try {
//                RepositoryUpdater.getDefault().lockRU();
                for (Map.Entry<FileObject, List<Difference>> me : diffs.entrySet()) {
                    commit(me.getKey(), me.getValue(), null);
                }
            } finally {
//                RepositoryUpdater.getDefault().unlockRU();
                Set<FileObject> alreadyRefreshed = new HashSet<FileObject>();
                try {
                    if (sources != null) {
                        final SourceFileManager.ModifiedFiles modifiedFiles = SourceFileManager.getModifiedFiles();
                        for (Source source : sources) {
                            Utilities.revalidate(source);
                            final FileObject srcFile = source.getFileObject();
                            alreadyRefreshed.add(srcFile);
                            modifiedFiles.fileModified(srcFile.toURI());
                        }
                    }
                } finally {
                    IndexingController.getDefault().exitProtectedMode(null);
                }
                for (FileObject currentlyVisibleInEditor : JavaSourceSupportAccessor.ACCESSOR.getVisibleEditorsFiles()) {
                    if (!alreadyRefreshed.contains(currentlyVisibleInEditor)) {
                        Source source = Source.create(currentlyVisibleInEditor);
                        if (source != null) {
                            Utilities.revalidate(source);
                        }
                    }
                }
            }
            while (lastCommitted.size() > 10) {
                lastCommitted.removeLast();
            }
            lastCommitted.addFirst(creator);
        } finally {
            this.committed = true;
            this.sources = null;
        }
    }
    
    static void commit (final FileObject fo, final List<Difference> differences, final Writer out) throws IOException {
        DataObject dObj = DataObject.find(fo);
        EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
        // if editor cookie was found and user does not provided his own
        // writer where he wants to see changes, commit the changes to 
        // found document.
        if (ec != null && out == null) {
            final StyledDocument doc = ec.getDocument();
            if (doc != null) {
                final IOException[] exceptions = new IOException [1];
                NbDocument.runAtomic(doc, new Runnable () {
                    public void run () {
                        try {
                            commit2 (doc, differences, out);
                        } catch (IOException ex) {
                            exceptions [0] = ex;
                        }
                    }
                });
                if (exceptions [0] != null) {
                    LOG.log(Level.INFO, "ModificationResult commit failed with an exception: ", exceptions[0]);
                    int s = lastCommitted.size();
                    for (Throwable t : lastCommitted) {
                        LOG.log(Level.INFO, "Previous commit number " + s--, t);
                    }
                    throw exceptions [0];
                }
                return;
            }
        }
        Reader in = null;
        Writer out2 = out;
        JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(fo);
        try {
            Charset encoding = FileEncodingQuery.getEncoding(fo);
            boolean[] hasReturnChar = new boolean[1];
            in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fo.asBytes()), encoding));
            if (filter != null) {
                in = filter.filterReader(in);
            }
            in = new FilteringReader(in, hasReturnChar);
            // initialize standard commit output stream, if user
            // does not provide his own writer
            boolean ownOutput = out != null;
            if (out2 == null) {
                out2 = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream(), encoding));
                //going through filter only when writing to disk. When creating source for getResultingSource,
                //it is not passed through the write-filter (as that is what refactoring preview expects).
                if (filter != null) {
                    out2 = filter.filterWriter(out2);
                }
                out2 = new FilteringWriter(out2, hasReturnChar);
            }
            int offset = 0;                
            for (Difference diff : differences) {
                if (diff.isExcluded())
                    continue;
                if (Difference.Kind.CREATE == diff.getKind()) {
                    if (!ownOutput) {
                        createUnit(diff, null);
                    }
                    continue;
                }
                int pos = diff.getStartPosition().getOffset();
                int toread = pos - offset;
                char[] buff = new char[toread];
                int n;
                int rc = 0;
                while ((n = in.read(buff,0, toread - rc)) > 0 && rc < toread) {
                    out2.write(buff, 0, n);
                    rc+=n;
                    offset += n;
                }
                switch (diff.getKind()) {
                    case INSERT:
                        out2.write(diff.getNewText());
                        break;
                    case REMOVE:
                        int len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        in.skip(len);
                        offset += len;
                        break;
                    case CHANGE:
                        len = diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset();
                        in.skip(len);
                        offset += len;
                        out2.write(diff.getNewText());
                        break;
                }
            }                    
            char[] buff = new char[1024];
            int n;
            while ((n = in.read(buff)) > 0) {
                out2.write(buff, 0, n);
            }
        } finally {
            if (in != null)
                in.close();
            if (out2 != null)
                out2.close();
        }            
    }

    private static void commit2 (final StyledDocument doc, final List<Difference> differences, Writer out) throws IOException {
        for (Difference diff : differences) {
            if (diff.isExcluded())
                continue;
            switch (diff.getKind()) {
                case INSERT:
                case REMOVE:
                case CHANGE:
                    processDocument(doc, diff);
                    break;
                case CREATE:
                    createUnit(diff, out);
                    break;
            }
        }
    }
    
    private static void processDocument(final StyledDocument doc, final Difference diff) throws IOException {
        final BadLocationException[] blex = new BadLocationException[1];
        Runnable task = new Runnable() {

            public void run() {
                DocumentListener l = null;
                try {
                    // The listener was added while trying to fix #63323
                    // IMPORTANT: it has to be removed immediatelly after the change to stop marking the events
                    // as ignoring caret changes
                    if (doc instanceof BaseDocument) {
                        l = new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                DocumentUtilities.putEventProperty(e, "caretIgnore", Boolean.TRUE); // NOI18N
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                DocumentUtilities.putEventProperty(e, "caretIgnore", Boolean.TRUE); // NOI18N
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                                DocumentUtilities.putEventProperty(e, "caretIgnore", Boolean.TRUE); // NOI18N
                            }
                        };
                        doc.addDocumentListener(l);
                    }
                    processDocumentLocked(doc, diff);
                } catch (BadLocationException ex) {
                    blex[0] = ex;
                } finally {
                    if (l != null) {
                        doc.removeDocumentListener(l);
                    }
                }
            }
        };
        if (diff.isCommitToGuards()) {
            NbDocument.runAtomic(doc, task);
        } else {
            try {
                NbDocument.runAtomicAsUser(doc, task);
            } catch (BadLocationException ex) {
                blex[0] = ex;
            }
        }
        if (blex[0] != null) {
            IOException ioe = new IOException();
            ioe.initCause(blex[0]);
            throw ioe;
        }
    }
    
    private static void processDocumentLocked(Document doc, Difference diff) throws BadLocationException {
        switch (diff.getKind()) {
            case INSERT:
                doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                break;
            case REMOVE:
                doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                break;
            case CHANGE:
                doc.remove(diff.getStartPosition().getOffset(), diff.getEndPosition().getOffset() - diff.getStartPosition().getOffset());
                doc.insertString(diff.getStartPosition().getOffset(), diff.getNewText(), null);
                break;
        }
    }

    private static void createUnit(Difference diff, Writer out) {
        CreateChange change = (CreateChange) diff;
        Writer w = out;
        try {
            if (w == null) {
                change.getFileObject().openOutputStream();
                w = change.getFileObject().openWriter();
            }
            w.append(change.getNewText());
        } catch (IOException e) {
            Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * Returned string represents preview of resulting source. No difference
     * really is applied. Respects {@code isExcluded()} flag of difference.
     * 
     * @param   there can be more resulting source, user has to specify
     *          which wants to preview.
     * @return  if changes are applied source looks like return string
     * @throws  IllegalArgumentException if the provided {@link FileObject} is not
     *                                   modified in this {@link ModificationResult}
     */
    public @NonNull String getResultingSource(@NonNull FileObject fileObject) throws IOException, IllegalArgumentException {
        Parameters.notNull("fileObject", fileObject);

        if (!getModifiedFileObjects().contains(fileObject)) {
            throw new IllegalArgumentException("File: " + FileUtil.getFileDisplayName(fileObject) + " is not modified in this ModificationResult");
        }
        
        StringWriter writer = new StringWriter();
        commit(fileObject, diffs.get(fileObject), writer);
        
        return writer.toString();
    }

    /**
     * Provides span of tree tagged with {@code tag}
     * @param tag
     * @return borders in target document
     * @since 0.37
     */
    public @NullUnknown int[] getSpan(@NonNull Object tag) {
        return tag2Span.get(tag);
    }
    
    public static class Difference {
              Kind kind;
        final PositionRef startPos;
        final PositionRef endPos;
              String oldText;
              String newText;
        final String description;
        private boolean excluded;
        private boolean ignoreGuards = false;

        Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText, String description) {
            this.kind = kind;
            this.startPos = startPos;
            this.endPos = endPos;
            this.oldText = oldText;
            this.newText = newText;
            this.description = description;
            this.excluded = false;
        }
        
        Difference(Kind kind, PositionRef startPos, PositionRef endPos, String oldText, String newText) {
            this(kind, startPos, endPos, oldText, newText, null);
        }
        
        public @NonNull Kind getKind() {
            return kind;
        }
        
        public @NonNull PositionRef getStartPosition() {
            return startPos;
        }
        
        public @NonNull PositionRef getEndPosition() {
            return endPos;
        }
        
        public @NonNull String getOldText() {
            return oldText;
        }
        
        public @NonNull String getNewText() {
            return newText;
        }
        
        public boolean isExcluded() {
            return excluded;
        }
        
        public void exclude(boolean b) {
            excluded = b;
        }

        /**
         * Gets flag if it is possible to write to guarded sections.
         * @return {@code true} in case the difference may be written even into
         *          guarded sections.
         * @see #guards(boolean)
         * @since 0.33
         */
        public boolean isCommitToGuards() {
            return ignoreGuards;
        }
        
        /**
         * Sets flag if it is possible to write to guarded sections.
         * @param b flag if it is possible to write to guarded sections
         * @since 0.33
         */
        public void setCommitToGuards(boolean b) {
            ignoreGuards = b;
        }

        @Override
        public String toString() {
            return kind + "<" + startPos.getOffset() + ", " + endPos.getOffset() + ">: " + oldText + " -> " + newText;
        }
        public String getDescription() {
            return description;
        }
        
        public static enum Kind {
            INSERT,
            REMOVE,
            CHANGE,
            CREATE;
        }
    }
    
    static class CreateChange extends Difference {
        JavaFileObject fileObject;
        
        CreateChange(JavaFileObject fileObject, String text) {
            super(Kind.CREATE, null, null, null, text, 
                    NbBundle.getMessage(ModificationResult.class, "TXT_CreateFile", fileObject.getName()));
            this.fileObject = fileObject;
        }

        public JavaFileObject getFileObject() {
            return fileObject;
        }

        @Override
        public String toString() {
            return kind + "Create File: " + fileObject.getName() + "; contents = \"\n" + newText + "\"";
        }
    }
    
    private static final class FilteringReader extends Reader {
        
        private final Reader delegate;
        private final boolean[] hasReturnChar;
        private boolean beforeFirstLine = true;

        public FilteringReader(Reader delegate, boolean[] hasReturnChar) {
            this.delegate = delegate;
            this.hasReturnChar = hasReturnChar;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int read;
            int j;
            
            do {
                read = delegate.read(cbuf, off, len);
                j = 0;

                for (int i = off; i < off + read; i++) {
                    if (cbuf[i] != '\r') {
                        cbuf[j++] = cbuf[i];
                        if (beforeFirstLine && cbuf[i] == '\n') {
                            beforeFirstLine = false;
                        }
                    } else if (beforeFirstLine) {
                        hasReturnChar[0] = true;
                        beforeFirstLine = false;
                    }
                }
            } while (j == 0 && read > 0);
            
            return j;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
    
    private static final class FilteringWriter extends Writer {
        private final boolean[] hasReturnChar;
        private final Writer delegate;

        public FilteringWriter(Writer delegate, boolean[] hasReturnChar) {
            this.hasReturnChar = hasReturnChar;
            this.delegate = delegate;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (hasReturnChar[0]) {
                char[] buf = new char[len * 2];
                int j = 0;
                
                for (int i = off; i < off + len; i++) {
                    if (cbuf[i] == '\n') {
                        buf[j++] = '\r';
                        buf[j++] = '\n';
                    } else {
                        buf[j++] = cbuf[i];
                    }
                }
                
                delegate.write(buf, 0, j);
            } else {
                delegate.write(cbuf, off, len);
            }
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
        
    }
}
