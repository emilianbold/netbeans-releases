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

package org.netbeans.modules.groovy.editor.parser;

import groovy.lang.GroovyClassLoader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.ParserResult.AstTreeNode;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.groovy.editor.AstNodeAdapter;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.GroovyUtils;
import org.netbeans.modules.groovy.editor.elements.AstElement;
import org.netbeans.modules.groovy.editor.elements.AstRootElement;
import org.netbeans.modules.groovy.editor.elements.CommentElement;
import org.netbeans.modules.groovy.editor.elements.IndexedElement;
import org.netbeans.modules.groovy.editor.elements.KeywordElement;
import org.netbeans.modules.gsf.spi.DefaultError;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @todo class referencing
 * 
 * @author Martin Adamek
 */
public class GroovyParser implements Parser {

    private final PositionManager positions = createPositionManager();

    public void parseFiles(Job job) {
        ParseListener listener = job.listener;
        SourceFileReader reader = job.reader;
        for (ParserFile file : job.files) {
            ParseEvent beginEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, null);
            listener.started(beginEvent);
            
            ParserResult result = null;

            try {
                CharSequence buffer = reader.read(file);
                String source = asString(buffer);
                int caretOffset = reader.getCaretOffset(file);
                Context context = new Context(file, listener, source, caretOffset);
                result = parseBuffer(context, Sanitize.NONE);
            } catch (IOException ioe) {
                listener.exception(ioe);
            }

            ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
            listener.finished(doneEvent);
        }
    }

    public PositionManager getPositionManager() {
        return positions;
    }

    protected GroovyParserResult createParseResult(ParserFile file, AstRootElement rootElement, AstTreeNode ast) {
        return new GroovyParserResult(this, file, rootElement, ast);
    }
    
    private boolean sanitizeSource(Context context, Sanitize sanitizing) {

        if (sanitizing == Sanitize.MISSING_END) {
            context.sanitizedSource = context.source + "}";
            int start = context.source.length();
            context.sanitizedRange = new OffsetRange(start, start+1);
            context.sanitizedContents = "";
            return true;
        }

        int offset = context.caretOffset;

        // Let caretOffset represent the offset of the portion of the buffer we'll be operating on
        if ((sanitizing == Sanitize.ERROR_DOT) || (sanitizing == Sanitize.ERROR_LINE)) {
            offset = context.errorOffset;
        }

        // Don't attempt cleaning up the source if we don't have the buffer position we need
        if (offset == -1) {
            return false;
        }

        // The user might be editing around the given caretOffset.
        // See if it looks modified
        // Insert an end statement? Insert a } marker?
        String doc = context.source;
        if (offset > doc.length()) {
            return false;
        }

        if (sanitizing == Sanitize.BLOCK_START) {
            try {
                int start = GroovyUtils.getRowFirstNonWhite(doc, offset);
                if (start != -1 && 
                        start+2 < doc.length() &&
                        doc.regionMatches(start, "if", 0, 2)) {
                    // TODO - check lexer
                    char c = 0;
                    if (start+2 < doc.length()) {
                        c = doc.charAt(start+2);
                    }
                    if (!Character.isLetter(c)) {
                        int removeStart = start;
                        int removeEnd = removeStart+2;
                        StringBuilder sb = new StringBuilder(doc.length());
                        sb.append(doc.substring(0, removeStart));
                        for (int i = removeStart; i < removeEnd; i++) {
                            sb.append(' ');
                        }
                        if (removeEnd < doc.length()) {
                            sb.append(doc.substring(removeEnd, doc.length()));
                        }
                        assert sb.length() == doc.length();
                        context.sanitizedRange = new OffsetRange(removeStart, removeEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(removeStart, removeEnd);
                        return true;
                    }
                }
                
                return false;
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                return false;
            }
        }
        
        try {
            // Sometimes the offset shows up on the next line
            if (GroovyUtils.isRowEmpty(doc, offset) || GroovyUtils.isRowWhite(doc, offset)) {
                offset = GroovyUtils.getRowStart(doc, offset)-1;
                if (offset < 0) {
                    offset = 0;
                }
            }

            if (!(GroovyUtils.isRowEmpty(doc, offset) || GroovyUtils.isRowWhite(doc, offset))) {
                if ((sanitizing == Sanitize.EDITED_LINE) || (sanitizing == Sanitize.ERROR_LINE)) {
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = GroovyUtils.getRowLastNonWhite(doc, offset);

                    if (lineEnd != -1 && offset < (doc.length() - 1)) {
                        StringBuilder sb = new StringBuilder(doc.length());
                        int lineStart = GroovyUtils.getRowStart(doc, offset);
                        int rest = lineStart + 2;

                        sb.append(doc.substring(0, lineStart));
                        sb.append("//");

                        if (rest < doc.length()) {
                            sb.append(doc.substring(rest, doc.length()));
                        }
                        assert sb.length() == doc.length();

                        context.sanitizedRange = new OffsetRange(lineStart, lineEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(lineStart, lineEnd);
                        return true;
                    }
                } else {
                    assert sanitizing == Sanitize.ERROR_DOT || sanitizing == Sanitize.EDITED_DOT;
                    // Try nuking dots/colons from this line
                    // See if I should try to remove the current line, since it has text on it.
                    int lineStart = GroovyUtils.getRowStart(doc, offset);
                    int lineEnd = offset-1;
                    while (lineEnd >= lineStart && lineEnd < doc.length()) {
                        if (!Character.isWhitespace(doc.charAt(lineEnd))) {
                            break;
                        }
                        lineEnd--;
                    }
                    if (lineEnd > lineStart) {
                        StringBuilder sb = new StringBuilder(doc.length());
                        String line = doc.substring(lineStart, lineEnd + 1);
                        int removeChars = 0;
                        int removeEnd = lineEnd+1;

                        if (line.endsWith(".") || line.endsWith("(")) { // NOI18N
                            removeChars = 1;
                        } else if (line.endsWith(",")) { // NOI18N                            removeChars = 1;
                            removeChars = 1;
                        } else if (line.endsWith(",:")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith(", :")) { // NOI18N
                            removeChars = 3;
                        } else if (line.endsWith(", ")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith("=> :")) { // NOI18N
                            removeChars = 4;
                        } else if (line.endsWith("=>:")) { // NOI18N
                            removeChars = 3;
                        } else if (line.endsWith("=>")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith("::")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith(":")) { // NOI18N
                            removeChars = 1;
                        } else if (line.endsWith("@@")) { // NOI18N
                            removeChars = 2;
                        } else if (line.endsWith("@")) { // NOI18N
                            removeChars = 1;
                        } else if (line.endsWith(",)")) { // NOI18N
                            // Handle lone comma in parameter list - e.g.
                            // type "foo(a," -> you end up with "foo(a,|)" which doesn't parse - but
                            // the line ends with ")", not "," !
                            // Just remove the comma
                            removeChars = 1;
                            removeEnd--;
                        } else if (line.endsWith(", )")) { // NOI18N
                            // Just remove the comma
                            removeChars = 1;
                            removeEnd -= 2;
                        }
                        
                        if (removeChars == 0) {
                            return false;
                        }

                        int removeStart = removeEnd-removeChars;

                        sb.append(doc.substring(0, removeStart));

                        for (int i = 0; i < removeChars; i++) {
                            sb.append(' ');
                        }

                        if (removeEnd < doc.length()) {
                            sb.append(doc.substring(removeEnd, doc.length()));
                        }
                        assert sb.length() == doc.length();

                        context.sanitizedRange = new OffsetRange(removeStart, removeEnd);
                        context.sanitizedSource = sb.toString();
                        context.sanitizedContents = doc.substring(removeStart, removeEnd);
                        return true;
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return false;
    }
    
    @SuppressWarnings("fallthrough")
    private GroovyParserResult sanitize(final Context context,
        final Sanitize sanitizing) {

        switch (sanitizing) {
        case NEVER:
            return createParseResult(context.file, null, null);

        case NONE:

            // We've currently tried with no sanitization: try first level
            // of sanitization - removing dots/colons at the edited offset.
            // First try removing the dots or double colons around the failing position
            if (context.caretOffset != -1) {
                return parseBuffer(context, Sanitize.EDITED_DOT);
            }

        // Fall through to try the next trick
        case EDITED_DOT:

            // We've tried editing the caret location - now try editing the error location
            // (Don't bother doing this if errorOffset==caretOffset since that would try the same
            // source as EDITED_DOT which has no better chance of succeeding...)
            if (context.errorOffset != -1 && context.errorOffset != context.caretOffset) {
                return parseBuffer(context, Sanitize.ERROR_DOT);
            }

        // Fall through to try the next trick
        case ERROR_DOT:

            // We've tried removing dots - now try removing the whole line at the error position
            if (context.caretOffset != -1) {
                return parseBuffer(context, Sanitize.BLOCK_START);
            }
            
        // Fall through to try the next trick
        case BLOCK_START:
            
            // We've tried removing dots - now try removing the whole line at the error position
            if (context.errorOffset != -1) {
                return parseBuffer(context, Sanitize.ERROR_LINE);
            }

        // Fall through to try the next trick
        case ERROR_LINE:

            // Messing with the error line didn't work - we could try "around" the error line
            // but I'm not attempting that now.
            // Finally try removing the whole line around the user editing position
            // (which could be far from where the error is showing up - but if you're typing
            // say a new "def" statement in a class, this will show up as an error on a mismatched
            // "end" statement rather than here
            if (context.caretOffset != -1) {
                return parseBuffer(context, Sanitize.EDITED_LINE);
            }

        // Fall through to try the next trick
        case EDITED_LINE:
            return parseBuffer(context, Sanitize.MISSING_END);
            
        // Fall through for default handling
        case MISSING_END:
        default:
            // We're out of tricks - just return the failed parse result
            return createParseResult(context.file, null, null);
        }
    }

    public static ElementHandle createHandle(CompilationInfo info, final Element object) {
        if (object instanceof KeywordElement || object instanceof CommentElement) {
            // Not tied to an AST - just pass it around
            return new GroovyElementHandle(null, object, info.getFileObject());
        }

        // TODO - check for Ruby
        if (object instanceof IndexedElement) {
            // Probably a function in a "foreign" file (not parsed from AST),
            // such as a signature returned from the index of the Ruby libraries.
// TODO - make sure this is infrequent! getFileObject is expensive!            
// Alternatively, do this in a delayed fashion - e.g. pass in null and in getFileObject
// look up from index            
            return new GroovyElementHandle(null, object, ((IndexedElement)object).getFileObject());
        }

        if (!(object instanceof AstElement)) {
            return null;
        }

        // XXX Gotta fix this
        if (info == null) {
            return null;
        }
        
        ParserResult result = AstUtilities.getParseResult(info);

        if (result == null) {
            return null;
        }

        ASTNode root = AstUtilities.getRoot(info);

        return new GroovyElementHandle(root, object, info.getFileObject());
    }

    @SuppressWarnings("unchecked")
    public static ElementHandle createHandle(ParserResult result, final AstElement object) {
        ASTNode root = AstUtilities.getRoot(result);

        return new GroovyElementHandle(root, object, result.getFile().getFileObject());
    }
    
    public static Element resolveHandle(CompilationInfo info, ElementHandle handle) {
        GroovyElementHandle h = (GroovyElementHandle)handle;
        ASTNode oldRoot = h.root;
        ASTNode oldNode;

        if (h.object instanceof KeywordElement || h.object instanceof IndexedElement || h.object instanceof CommentElement) {
            // Not tied to a tree
            return h.object;
        }

        if (h.object instanceof AstElement) {
            oldNode = ((AstElement)h.object).getNode(); // XXX Make it work for DefaultComObjects...
        } else {
            return null;
        }

        ASTNode newRoot = AstUtilities.getRoot(info);
        if (newRoot == null) {
            return null;
        }

        // Find newNode
        ASTNode newNode = find(oldRoot, oldNode, newRoot);

        if (newNode != null) {
            Element co = AstElement.create(newNode);

            return co;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public GroovyParserResult parseBuffer(final Context context, final Sanitize sanitizing) {
        boolean sanitizedSource = false;
        String source = context.source;
        if (!((sanitizing == Sanitize.NONE) || (sanitizing == Sanitize.NEVER))) {
            boolean ok = sanitizeSource(context, sanitizing);

            if (ok) {
                assert context.sanitizedSource != null;
                sanitizedSource = true;
                source = context.sanitizedSource;
            } else {
                // Try next trick
                return sanitize(context, sanitizing);
            }
        }
        
        final boolean ignoreErrors = sanitizedSource;

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = -1;
        }
        
        String fileName = "";
        if ((context.file != null) && (context.file.getFileObject() != null)) {
            fileName = context.file.getFileObject().getNameExt();
        }
        
        FileObject fo = context.file.getFileObject();
        ClassPath cp = ClassPathSupport.createProxyClassPath(
                ClassPath.getClassPath(fo, ClassPath.BOOT),
                ClassPath.getClassPath(fo, ClassPath.COMPILE),
                ClassPath.getClassPath(fo, ClassPath.SOURCE)
                );
        CompilerConfiguration configuration = new CompilerConfiguration();
        ClassLoader parentLoader = cp == null ? null : cp.getClassLoader(true);
        GroovyClassLoader classLoader = new GroovyClassLoader(parentLoader, configuration);
        CompilationUnit compilationUnit = new CompilationUnit(configuration, null, classLoader);
        InputStream inputStream = new ByteArrayInputStream(source.getBytes());
        compilationUnit.addSource(fileName, inputStream);

        try {
            compilationUnit.compile(Phases.SEMANTIC_ANALYSIS); // which phase should be used?
        } catch (Throwable e) {
            int offset = -1;
            String errorMessage = e.getMessage();
            String localizedMessage = e.getLocalizedMessage();
            
            ErrorCollector errorCollector = compilationUnit.getErrorCollector();
            if (errorCollector.hasErrors()) {
                Message message = errorCollector.getLastError();
                if (message instanceof SyntaxErrorMessage) {
                    SyntaxException se = ((SyntaxErrorMessage)message).getCause();

                    offset = AstUtilities.getOffset(context.document, se.getStartLine(), se.getStartColumn());
                    errorMessage = se.getMessage();
                    localizedMessage = se.getLocalizedMessage();
                }
            }
            
            // XXX should this be >, and = length?
            if (offset >= source.length()) {
                offset = source.length() - 1;

                if (offset < 0) {
                    offset = 0;
                }
            }

            if (!ignoreErrors) {
                notifyError(context, null, Severity.ERROR, errorMessage, localizedMessage, offset, sanitizing);
            }
        }

        CompileUnit compileUnit = compilationUnit.getAST();
        List<ModuleNode> modules = compileUnit.getModules();

        // there are more modules if class references another class,
        // there is one module per class
        ModuleNode module = null;
        for (ModuleNode moduleNode : modules) {
            if (fileName.equals(moduleNode.getContext().getName())) {
                module = moduleNode;
            }
        }

        handleErrorCollector(compilationUnit.getErrorCollector(), context, module, ignoreErrors, sanitizing);
        
        if (module != null) {
            context.sanitized = sanitizing;
            AstRootElement astRootElement = new AstRootElement(context.file.getFileObject(), module);
            AstNodeAdapter ast = new AstNodeAdapter(null, module, context.document);
            GroovyParserResult r = createParseResult(context.file, astRootElement, ast);
            r.setSanitized(context.sanitized, context.sanitizedRange, context.sanitizedContents);
            return r;
        } else {
            return sanitize(context, sanitizing);
        }
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String)sequence;
        } else {
            return sequence.toString();
        }
    }

    private PositionManager createPositionManager() {
        return new GroovyPositionManager();
    }

    private static void notifyError(Context context, String key, Severity severity, String description, String details, 
            int offset, Sanitize sanitizing) {
        notifyError(context, key, severity, description, details, offset, offset, sanitizing);
    }

    private static void notifyError(Context context, String key, Severity severity, String description, String details, 
            int startOffset, int endOffset, Sanitize sanitizing) {
        // Initialize keys for errors needing it
        if (key == null) {
            key = description;
        }
        
        Error error =
            new DefaultError(key, description, details, context.file.getFileObject(),
                startOffset, endOffset, severity);
        context.listener.error(error);

        context.errorOffset = startOffset;

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = startOffset;
        }
    }

    private static void handleErrorCollector(ErrorCollector errorCollector, Context context, ModuleNode moduleNode, boolean ignoreErrors, Sanitize sanitizing) {
        if (!ignoreErrors && errorCollector != null) {
            List errors = errorCollector.getErrors();
            if (errors != null) {
                for (Object object : errors) {
                    if (object instanceof SyntaxErrorMessage) {
                        SyntaxException ex = ((SyntaxErrorMessage)object).getCause();
                        String sourceLocator = ex.getSourceLocator();
                        String name = moduleNode != null ? moduleNode.getContext().getName() : null;
                        if (sourceLocator != null && name != null && sourceLocator.equals(name)) {
                            int startOffset = AstUtilities.getOffset(context.document, ex.getStartLine(), ex.getStartColumn());
                            int endOffset = AstUtilities.getOffset(context.document, ex.getLine(), ex.getEndColumn());
                            notifyError(context, null, Severity.ERROR, ex.getMessage(), null, startOffset, endOffset, sanitizing);
                        }
                    } else if (object instanceof SimpleMessage) {
                        String message = ((SimpleMessage)object).getMessage();
                        notifyError(context, null, Severity.ERROR, message, null, -1, sanitizing);
                    } else {
                        notifyError(context, null, Severity.ERROR, "Error", null, -1, sanitizing);
                    }
                }
            }
        }
    }
    
    private static ASTNode find(ASTNode oldRoot, ASTNode oldObject, ASTNode newRoot) {
        // Walk down the tree to locate oldObject, and in the process, pick the same child for newRoot
        @SuppressWarnings("unchecked")
        List<?extends ASTNode> oldChildren = AstUtilities.children(oldRoot);
        @SuppressWarnings("unchecked")
        List<?extends ASTNode> newChildren = AstUtilities.children(newRoot);
        Iterator<?extends ASTNode> itOld = oldChildren.iterator();
        Iterator<?extends ASTNode> itNew = newChildren.iterator();

        while (itOld.hasNext()) {
            if (!itNew.hasNext()) {
                return null; // No match - the trees have changed structure
            }

            ASTNode o = itOld.next();
            ASTNode n = itNew.next();

            if (o == oldObject) {
                // Found it!
                return n;
            }

            // Recurse
            ASTNode match = find(o, oldObject, n);

            if (match != null) {
                return match;
            }
        }

        if (itNew.hasNext()) {
            return null; // No match - the trees have changed structure
        }

        return null;
    }

    private static class GroovyElementHandle implements ElementHandle {
        private final ASTNode root;
        private final Element object;
        private final FileObject fileObject;

        private GroovyElementHandle(ASTNode root, Element object, FileObject fileObject) {
            this.root = root;
            this.object = object;
            this.fileObject = fileObject;
        }

        public boolean signatureEquals(ElementHandle handle) {
            // XXX TODO
            return false;
        }

        public FileObject getFileObject() {
            if (object instanceof IndexedElement) {
                return ((IndexedElement)object).getFileObject();
            }

            return fileObject;
        }

        public String getMimeType() {
            return "text/x-groovy";
        }

        public String getName() {
            return object.getName();
        }

        public String getIn() {
            return object.getIn();
        }

        public ElementKind getKind() {
            return object.getKind();
        }

        public Set<Modifier> getModifiers() {
            return object.getModifiers();
        }
    }

    /** Attempts to sanitize the input buffer */
    public static enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER, 
        /** Perform no sanitization */
        NONE, 
        /** Try to remove the trailing . or :: at the caret line */
        EDITED_DOT, 
        /** Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line */
        ERROR_DOT, 
        /** Try to remove the initial "if" or "unless" on the block
         * in case it's not terminated
         */
        BLOCK_START,
        /** Try to cut out the error line */
        ERROR_LINE, 
        /** Try to cut out the current edited line, if known */
        EDITED_LINE,
        /** Attempt to add an "end" to the end of the buffer to make it compile */
        MISSING_END,
    }

    /** Parsing context */
    public static final class Context {
        private final ParserFile file;
        private final ParseListener listener;
        private int errorOffset;
        private String source;
        private String sanitizedSource;
        private OffsetRange sanitizedRange = OffsetRange.NONE;
        private String sanitizedContents;
        private int caretOffset;
        private Sanitize sanitized = Sanitize.NONE;
        private BaseDocument document;
        
        public Context(ParserFile parserFile, ParseListener listener, String source, int caretOffset) {
            this.file = parserFile;
            this.listener = listener;
            this.source = source;
            this.caretOffset = caretOffset;
            this.document = AstUtilities.getBaseDocument(file.getFileObject(), true);
        }
        
        @Override
        public String toString() {
            return "GroovyParser.Context(" + file.toString() + ")"; // NOI18N
        }
        
        public OffsetRange getSanitizedRange() {
            return sanitizedRange;
        }

        public Sanitize getSanitized() {
            return sanitized;
        }
        
        public String getSanitizedSource() {
            return sanitizedSource;
        }
        
        public int getErrorOffset() {
            return errorOffset;
        }
    }

}
