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

package org.netbeans.modules.csl.spi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;

/**
 * Misc utilities to avoid code duplication among the various language plugins
 *
 * @author Tor Norbye
 */
public final class GsfUtilities {
    private static final Logger LOG = Logger.getLogger(GsfUtilities.class.getName());

    private GsfUtilities() { // Utility class only, no instances
    }

    public static int getLineIndent(BaseDocument doc, int offset) {
        try {
            return IndentUtils.lineIndent(doc, Utilities.getRowStart(doc, offset));
        } catch (BadLocationException ble) {
            LOG.log(Level.WARNING, null, ble);
            return 0;
        }
    }

    /** Adjust the indentation of the line containing the given offset to the provided
     * indentation, and return the new indent.
     *
     * Copied from Indent module's "modifyIndent"
     */
    public static void setLineIndentation(BaseDocument doc, int lineOffset, int newIndent) throws BadLocationException {
        int lineStartOffset = Utilities.getRowStart(doc, lineOffset);

        // Determine old indent first together with oldIndentEndOffset
        int indent = 0;
        int tabSize = -1;
        CharSequence docText = DocumentUtilities.getText(doc);
        int oldIndentEndOffset = lineStartOffset;
        while (oldIndentEndOffset < docText.length()) {
            char ch = docText.charAt(oldIndentEndOffset);
            if (ch == '\n') {
                break;
            } else if (ch == '\t') {
                if (tabSize == -1) {
                    tabSize = IndentUtils.tabSize(doc);
                }
                // Round to next tab stop
                indent = (indent + tabSize) / tabSize * tabSize;
            } else if (Character.isWhitespace(ch)) {
                indent++;
            } else { // non-whitespace
                break;
            }
            oldIndentEndOffset++;
        }

        String newIndentString = IndentUtils.createIndentString(doc, newIndent);
        // Attempt to match the begining characters
        int offset = lineStartOffset;
        for (int i = 0; i < newIndentString.length() && lineStartOffset + i < oldIndentEndOffset; i++) {
            if (newIndentString.charAt(i) != docText.charAt(lineStartOffset + i)) {
                offset = lineStartOffset + i;
                newIndentString = newIndentString.substring(i);
                break;
            }
        }

        // Replace the old indent
        if (offset < oldIndentEndOffset) {
            doc.remove(offset, oldIndentEndOffset - offset);
        }
        if (newIndentString.length() > 0) {
            doc.insertString(offset, newIndentString, null);
        }
    }


    public static JTextComponent getOpenPane() {
        JTextComponent pane = EditorRegistry.lastFocusedComponent();

        return pane;
    }

    public static JTextComponent getPaneFor(FileObject fo) {
        JTextComponent pane = getOpenPane();
        if (pane != null && findFileObject(pane) == fo) {
            return pane;
        }

        for (JTextComponent c : EditorRegistry.componentList()) {
            if (findFileObject(c) == fo) {
                return c;
            }
        }

        return null;
    }

    public static BaseDocument getDocument(FileObject fileObject, boolean openIfNecessary) {
        return getDocument(fileObject, openIfNecessary, false);
    }

    /**
     * Load the document for the given fileObject.
     * @param fileObject the file whose document we want to obtain
     * @param openIfNecessary If true, block if necessary to open the document. If false, will only return the
     *    document if it is already open.
     * @param skipLarge If true, check the file size, and if the file is really large (defined by
     *    openide.loaders), then skip it (otherwise we could end up with a large file warning).
     * @return
     */
    public static BaseDocument getDocument(FileObject fileObject, boolean openIfNecessary, boolean skipLarge) {
        if (skipLarge) {
            // Make sure we're not dealing with a huge file!
            // Causes issues like 132306
            // openide.loaders/src/org/openide/text/DataEditorSupport.java
            // has an Env#inputStream method which posts a warning to the user
            // if the file is greater than 1Mb...
            //SG_ObjectIsTooBig=The file {1} seems to be too large ({2,choice,0#{2}b|1024#{3} Kb|1100000#{4} Mb|1100000000#{5} Gb}) to safely open. \n\
            //  Opening the file could cause OutOfMemoryError, which would make the IDE unusable. Do you really want to open it?

            // Apparently there is a way to handle this
            // (see issue http://www.netbeans.org/issues/show_bug.cgi?id=148702 )
            // but for many cases, the user probably doesn't want really large files as indicated
            // by the skipLarge parameter).
            if (fileObject.getSize () > 1024 * 1024) {
                return null;
            }
        }

        try {
            EditorCookie ec = DataLoadersBridge.getDefault().getCookie(fileObject, EditorCookie.class);
            if (ec != null) {
                if (openIfNecessary) {
                    try {
                        return (BaseDocument) ec.openDocument();
                    } catch (UserQuestionException uqe) {
                        uqe.confirmed();
                        return (BaseDocument) ec.openDocument();
                    }
                } else {
                    return (BaseDocument) ec.getDocument();
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }

        return null;
    }

    @Deprecated // Use getDocument instead
    public static BaseDocument getBaseDocument(FileObject fileObject, boolean forceOpen) {
        return getDocument(fileObject, forceOpen);
    }

    public static FileObject findFileObject(Document doc) {
        DataObject dobj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);

        if (dobj == null) {
            return null;
        }

        return dobj.getPrimaryFile();
    }

    public static FileObject findFileObject(JTextComponent target) {
        Document doc = target.getDocument();
        return findFileObject(doc);
    }

    // Copied from UiUtils. Shouldn't this be in a common library somewhere?
    public static boolean open(final FileObject fo, final int offset, final String search) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        doOpen(fo, offset, search);
                    }
                });

            return true; // not exactly accurate, but....
        }

        return doOpen(fo, offset, search);
    }

    // Private methods ---------------------------------------------------------
    private static boolean doOpen(FileObject fo, int offset, String search) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getCookie(EditorCookie.class);
            LineCookie lc = od.getCookie(LineCookie.class);

            // If the caller hasn't specified an offset, and the document is
            // already open, don't jump to a particular line!
            if (offset == -1 && ec.getDocument() != null && search == null) {
                ec.open();
                return true;
            }

            // Simple text search if no known offset (e.g. broken/unparseable source)
            if ((ec != null) && (search != null) && (offset == -1)) {
                StyledDocument doc = ec.openDocument();

                try {
                    String text = doc.getText(0, doc.getLength());
                    int caretDelta = search.indexOf('^');
                    if (caretDelta != -1) {
                        search = search.substring(0, caretDelta) + search.substring(caretDelta+1);
                    } else {
                        caretDelta = 0;
                    }
                    offset = text.indexOf(search);
                    if (offset != -1) {
                        offset += caretDelta;
                    }
                } catch (BadLocationException ble) {
                    LOG.log(Level.WARNING, null, ble);
                }
            }

            if ((ec != null) && (lc != null) && (offset != -1)) {
                StyledDocument doc = ec.openDocument();

                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;

                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);

                        if (l != null) {
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, column);

                            return true;
                        }
                    }
                }
            }

            OpenCookie oc = od.getCookie(OpenCookie.class);

            if (oc != null) {
                oc.open();

                return true;
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        return false;
    }

    public static void extractZip(final FileObject extract, final FileObject dest) throws IOException {
        File extractFile = FileUtil.toFile(extract);
        extractZip(dest, new BufferedInputStream(new FileInputStream(extractFile)));
    }

    // Based on openide/fs' FileUtil.extractJar
    private static void extractZip(final FileObject fo, final InputStream is)
    throws IOException {
        FileSystem fs = fo.getFileSystem();

        fs.runAtomicAction(
            new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    extractZipImpl(fo, is);
                }
            }
        );
    }

    /** Does the actual extraction of the Jar file.
     */
    // Based on openide/fs' FileUtil.extractJarImpl
    private static void extractZipImpl(FileObject fo, InputStream is)
    throws IOException {
        ZipEntry je;

        ZipInputStream jis = new ZipInputStream(is);

        while ((je = jis.getNextEntry()) != null) {
            String name = je.getName();

            if (name.toLowerCase().startsWith("meta-inf/")) {
                continue; // NOI18N
            }

            if (je.isDirectory()) {
                FileUtil.createFolder(fo, name);

                continue;
            }

            // copy the file
            FileObject fd = FileUtil.createData(fo, name);
            FileLock lock = fd.lock();

            try {
                OutputStream os = fd.getOutputStream(lock);

                try {
                    FileUtil.copy(jis, os);
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
    }

    /** Return true iff we're editing code templates */
    public static boolean isCodeTemplateEditing(Document doc) {
        // Copied from editor/codetemplates/src/org/netbeans/lib/editor/codetemplates/CodeTemplateInsertHandler.java
        String EDITING_TEMPLATE_DOC_PROPERTY = "processing-code-template"; // NOI18N
        String CT_HANDLER_DOC_PROPERTY = "code-template-insert-handler"; // NOI18N

        return doc.getProperty(EDITING_TEMPLATE_DOC_PROPERTY) == Boolean.TRUE ||
                doc.getProperty(CT_HANDLER_DOC_PROPERTY) != null;
    }

    public static boolean isRowWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search forwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    break;
                }
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static boolean isRowEmpty(CharSequence text, int offset) throws BadLocationException {
        try {
            if (offset < text.length()) {
                char c = text.charAt(offset);
                if (!(c == '\n' || (c == '\r' && (offset == text.length()-1 || text.charAt(offset+1) == '\n')))) {
                    return false;
                }
            }

            if (!(offset == 0 || text.charAt(offset-1) == '\n')) {
                // There's previous stuff on this line
                return false;
            }

            return true;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowLastNonWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Find end of line
            int i = offset;
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n' || (c == '\r' && (i == text.length()-1 || text.charAt(i+1) == '\n'))) {
                    break;
                }
            }
            // Search backwards to find last nonspace char from offset
            for (i--; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowFirstNonWhite(CharSequence text, int offset) throws BadLocationException {
        try {
            // Find start of line
            int i = offset-1;
            if (i < text.length()) {
                for (; i >= 0; i--) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        break;
                    }
                }
                i++;
            }
            // Search forwards to find first nonspace char from offset
            for (; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return -1;
                }
                if (!Character.isWhitespace(c)) {
                    return i;
                }
            }

            return -1;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowStart(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset-1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i+1;
                }
            }

            return 0;
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static int getRowEnd(CharSequence text, int offset) throws BadLocationException {
        try {
            // Search backwards
            for (int i = offset; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    return i;
                }
            }

            return text.length();
        } catch (Exception ex) {
            BadLocationException ble = new BadLocationException(offset + " out of " + text.length(), offset);
            ble.initCause(ex);
            throw ble;
        }
    }

    public static boolean endsWith(StringBuilder sb, String s) {
        int len = s.length();

        if (sb.length() < len) {
            return false;
        }

        for (int i = sb.length()-len, j = 0; j < len; i++, j++) {
            if (sb.charAt(i) != s.charAt(j)) {
                return false;
            }
        }

        return true;
    }

    public static String truncate(String s, int length) {
        assert length > 3; // Not for short strings
        if (s.length() <= length) {
            return s;
        } else {
            return s.substring(0, length-3) + "...";
        }
    }

    /**
     * Gets the last known offset of the editor caret.
     *
     * @param snapshot The snapshot to get the offset for.
     * @param event The event that can contain offset information. Can be <code>null</code>.
     *
     * @return The last know caret offset or -1.
     */
    public static int getLastKnownCaretOffset(Snapshot snapshot, EventObject event) {
        // Try scheduler event first
        if (event instanceof CursorMovedSchedulerEvent) {
            return ((CursorMovedSchedulerEvent) event).getCaretOffset();
        }

        // Then look through all existing editor pane
        Document snapshotDoc = snapshot.getSource().getDocument(false);
        if (snapshotDoc != null) {
            for(JTextComponent jtc : EditorRegistry.componentList()) {
                if (snapshotDoc == jtc.getDocument()) {
                    return jtc.getCaretPosition();
                }
            }
        } else {
            FileObject snapshotFile = snapshot.getSource().getFileObject();
            if (snapshotFile != null) {
                for(JTextComponent jtc : EditorRegistry.componentList()) {
                    if (snapshotFile == NbEditorUtilities.getFileObject(jtc.getDocument())) {
                        return jtc.getCaretPosition();
                    }
                }
            }
        }

        // Finally, try the enforced caret offset (eg. enforced by tests)
        Integer enforcedCaretOffset = enforcedCaretOffsets.get(snapshot.getSource());
        if (enforcedCaretOffset != null) {
            return enforcedCaretOffset;
        }

        return -1;
    }

    // this is called from tests
    /* package */ static void setLastKnowCaretOffset(Source source, int offset) {
        enforcedCaretOffsets.put(source, offset);
    }

    private static final Map<Source, Integer> enforcedCaretOffsets = new WeakHashMap<Source, Integer>();

    /**
     * Gets classpath roots relevant for a file. This method tries to find
     * classpath roots for a given files. It looks at classpaths specified by
     * <code>sourcePathIds</code>, <code>libraryPathIds</code> and
     * <code>binaryLibraryPathIds</code> parameters.
     *
     * <p>The roots collected from <code>binaryLibraryPathIds</code> will be translated
     * by the <code>SourceForBinaryQuery</code> in order to find relevant sources root.
     * The roots collected from <code>libraryPathIds</code> are expected to be
     * libraries in their sources form (ie. no translation).
     *
     * @param f The file to find roots for.
     * @param sourcePathIds The IDs of source classpath to look at.
     * @param libraryPathIds The IDs of library classpath to look at.
     * @param binaryLibraryPathIds The IDs of binary library classpath to look at.
     * 
     * @return The collection of roots for a given file. It may be empty, but never <code>null</code>.
     */
    public static Collection<FileObject> getRoots(
            FileObject f,
            Collection<String> sourcePathIds,
            Collection<String> libraryPathIds,
            Collection<String> binaryLibraryPathIds)
    {
        Collection<FileObject> roots = new HashSet<FileObject>();
        Set<String> [] knownPathIds = null;

        if (sourcePathIds == null) {
            knownPathIds = getKnownPathIds();
            sourcePathIds = knownPathIds[0];
        }

        if (libraryPathIds == null) {
            if (knownPathIds == null) {
                knownPathIds = getKnownPathIds();
            }
            libraryPathIds = knownPathIds[1];
        }

        if (binaryLibraryPathIds == null) {
            if (knownPathIds == null) {
                knownPathIds = getKnownPathIds();
            }
            binaryLibraryPathIds = knownPathIds[2];
        }

        collectClasspathRoots(f, sourcePathIds, false, roots);
        collectClasspathRoots(f, libraryPathIds, false, roots);
        collectClasspathRoots(f, binaryLibraryPathIds, true, roots);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Roots for file " + f //NOI18N
                    + ", sourcePathIds=" + sourcePathIds //NOI18N
                    + ", libraryPathIds=" + libraryPathIds //NOI18N
                    + ", binaryPathIds=" + binaryLibraryPathIds //NOI18N
                    + ": " + roots); //NOI18N
        }

        return roots != null ? roots : Collections.<FileObject>emptySet();
    }

    /**
     * Gets classpath roots relevant for a project. This method tries to find
     * classpath with <code>sourcePathIds</code>, <code>libraryPathIds</code> and
     * <code>binaryPathIds</code> supplied by the <code>project</code>.
     *
     * <p>The roots collected from <code>binaryLibraryPathIds</code> will be translated
     * by the <code>SourceForBinaryQuery</code> in order to find relevant sources root.
     * The roots collected from <code>libraryPathIds</code> are expected to be
     * libraries in their sources form (ie. no translation).
     *
     * @param project The project to find the roots for. Can be <code>null</code> in
     *   which case the method searches in all registered classpaths.
     * @param sourcePathIds The IDs of source classpath to look at.
     * @param libraryPathIds The IDs of library classpath to look at.
     * @param binaryLibraryPathIds The IDs of binary library classpath to look at.
     *
     * @return The collection of roots for a given project. It may be empty, but never <code>null</code>.
     */
    public static Collection<FileObject> getRoots(
            Project project,
            Collection<String> sourcePathIds,
            Collection<String> libraryPathIds,
            Collection<String> binaryLibraryPathIds)
    {
        Set<FileObject> roots = new HashSet<FileObject>();
        Set<String> [] knownPathIds = null;

        if (sourcePathIds == null) {
            knownPathIds = getKnownPathIds();
            sourcePathIds = knownPathIds[0];
        }

        if (libraryPathIds == null) {
            if (knownPathIds == null) {
                knownPathIds = getKnownPathIds();
            }
            libraryPathIds = knownPathIds[1];
        }

        if (binaryLibraryPathIds == null) {
            if (knownPathIds == null) {
                knownPathIds = getKnownPathIds();
            }
            binaryLibraryPathIds = knownPathIds[2];
        }

        collectClasspathRoots(null, sourcePathIds, false, roots);
        collectClasspathRoots(null, libraryPathIds, false, roots);
        collectClasspathRoots(null, binaryLibraryPathIds, true, roots);

        if (project != null) {
            Set<FileObject> rootsInProject = new HashSet<FileObject>();
            for(FileObject root : roots) {
                if (FileOwnerQuery.getOwner(root) == project) {
                    rootsInProject.add(root);
                }
            }
            roots = rootsInProject;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Roots for project " + project //NOI18N
                    + ", sourcePathIds=" + sourcePathIds //NOI18N
                    + ", libraryPathIds=" + libraryPathIds //NOI18N
                    + ", binaryPathIds=" + binaryLibraryPathIds //NOI18N
                    + ": " + roots); //NOI18N
        }

        return roots;
    }

    private static void collectClasspathRoots(FileObject file, Collection<String> pathIds, boolean binaryPaths, Collection<FileObject> roots) {
        for(String id : pathIds) {
            Collection<FileObject> classpathRoots = getClasspathRoots(file, id);
            if (binaryPaths) {
                // Filter out roots that do not have source files available
                for(FileObject f : classpathRoots) {
                    SourceForBinaryQuery.Result2 result;
                    try {
                        result = SourceForBinaryQuery.findSourceRoots2(f.getURL());
                    } catch (FileStateInvalidException fsie) {
                        LOG.warning("Ignoring invalid binary Path root: " + f.getPath()); //NOI18N
                        LOG.log(Level.FINE, null, fsie);
                        continue;
                    }

                    if (result.preferSources() && result.getRoots().length > 0) {
                        roots.addAll(Arrays.asList(result.getRoots()));
                    } else {
                        roots.add(f);
                    }
                }
            } else {
                roots.addAll(classpathRoots);
            }
        }
    }

    private static Collection<FileObject> getClasspathRoots(FileObject file, String classpathId) {
        Collection<FileObject> roots = Collections.<FileObject>emptySet();

        if (file != null) {
            ClassPath classpath = ClassPath.getClassPath(file, classpathId);
            if (classpath != null) {
                roots = Arrays.asList(classpath.getRoots());
            }
        } else {
            roots = new HashSet<FileObject>();
            Set<ClassPath> classpaths = GlobalPathRegistry.getDefault().getPaths(classpathId);
            for(ClassPath classpath : classpaths) {
                roots.addAll(Arrays.asList(classpath.getRoots()));
            }
        }

        return roots;
    }

    private static Set<String> [] getKnownPathIds() {
        Set<String> sids = new HashSet<String>();
        Set<String> lids = new HashSet<String>();
        Set<String> blids = new HashSet<String>();

        Collection<? extends PathRecognizer> recognizers = Lookup.getDefault().lookupAll(PathRecognizer.class);
        for(PathRecognizer r : recognizers) {
            Set<String> ids = r.getSourcePathIds();
            if (ids != null) {
                sids.addAll(ids);
            }

            ids = r.getLibraryPathIds();
            if (ids != null) {
                lids.addAll(ids);
            }

            ids = r.getBinaryLibraryPathIds();
            if (ids != null) {
                blids.addAll(ids);
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Known Path Ids: source=" + sids + ", library=" + lids + ", binary-library=" + blids); //NOI18N
        }

        return new Set [] { sids, lids, blids };
    }
}
