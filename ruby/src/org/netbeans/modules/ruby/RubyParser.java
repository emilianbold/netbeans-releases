/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.common.IRubyWarnings;
import org.jruby.lexer.yacc.ISourcePosition;
import org.jruby.lexer.yacc.LexerSource;
import org.jruby.lexer.yacc.SyntaxException;
import org.jruby.parser.DefaultRubyParser;
import org.jruby.parser.RubyParserConfiguration;
import org.jruby.parser.RubyParserResult;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementHandle;
import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.ParseEvent;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.Parser;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.PositionManager;
import org.netbeans.api.gsf.SemanticAnalyzer;
import org.netbeans.api.gsf.Severity;
import org.netbeans.api.gsf.Severity;
import org.netbeans.api.gsf.SourceFileReader;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.AstRootElement;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.KeywordElement;
import org.netbeans.spi.gsf.DefaultError;
import org.netbeans.spi.gsf.DefaultPosition;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Wrapper around JRuby to parse a buffer into an AST.
 *
 * @todo Rename to RubyParser for symmetry with RubyLexer
 * @todo Idea: If you get a syntax error on the last line, it's probably a missing
 *   "end" much earlier. Go back and look for a method inside a method, and the outer
 *   method is probably missing an end (can use indentation to look for this as well).
 *   Create a quickfix to insert it.
 * 
 * @author Tor Norbye
 */
public class RubyParser implements Parser {
    private final PositionManager positions = new RubyPositionManager();

    /**
     * Creates a new instance of RubyParser
     */
    public RubyParser() {
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String)sequence;
        } else {
            return sequence.toString();
        }
    }

    /** Parse the given set of files, and notify the parse listener for each transition
     * (compilation results are attached to the events )
     */
    public void parseFiles(List<ParserFile> files, ParseListener listener, SourceFileReader reader) {
        for (ParserFile file : files) {
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

    /**
     * Try cleaning up the source buffer around the current offset to increase
     * likelihood of parse success. Initially this method had a lot of
     * logic to determine whether a parse was likely to fail (e.g. invoking
     * the isEndMissing method from bracket completion etc.).
     * However, I am now trying a parse with the real source first, and then
     * only if that fails do I try parsing with sanitized source. Therefore,
     * this method has to be less conservative in ripping out code since it
     * will only be used when the regular source is failing.
     */
    private boolean sanitizeSource(Context context, Sanitize sanitizing) {

        if (context.noDocument) {
            return false;
        }
        
        if (sanitizing == Sanitize.MISSING_END) {
            context.sanitizedSource = context.source + ";end";
            int start = context.source.length();
            context.sanitizedRange = new OffsetRange(start, start+4);
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
        if (context.doc == null) {
            ParserFile file = context.file;
            FileObject fileObject = file.getFileObject();

            if (fileObject == null) {
                context.noDocument = true;
                return false;
            }

           context.doc = AstUtilities.getBaseDocument(fileObject, false);
            if (context.doc == null) {
                context.noDocument = true;
                return false;
            }
        }
        
        BaseDocument doc = context.doc;
        
        if (offset > doc.getLength()) {
            return false;
        }

        try {
            // Sometimes the offset shows up on the next line
            if (Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset)) {
                offset = Utilities.getRowStart(doc, offset)-1;
                if (offset < 0) {
                    offset = 0;
                }
            }

            if (!(Utilities.isRowEmpty(doc, offset) || Utilities.isRowWhite(doc, offset))) {
                if ((sanitizing == Sanitize.EDITED_LINE) || (sanitizing == Sanitize.ERROR_LINE)) {
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = Utilities.getRowLastNonWhite(doc, offset);

                    if (lineEnd != -1) {
                        StringBuilder sb = new StringBuilder(doc.getLength());
                        int lineStart = Utilities.getRowStart(doc, offset);
                        int rest = lineStart + 1;

                        sb.append(doc.getText(0, lineStart));
                        sb.append('#');

                        if (rest < doc.getLength()) {
                            sb.append(doc.getText(rest, doc.getLength() - rest));
                        }
                        assert sb.length() == doc.getLength();

                        context.sanitizedRange = new OffsetRange(lineStart, lineEnd);
                        context.sanitizedSource = sb.toString();
                        return true;
                    }
                } else {
                    assert sanitizing == Sanitize.ERROR_DOT || sanitizing == Sanitize.EDITED_DOT;

                    // Try nuking dots/colons from this line
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = Utilities.getRowLastNonWhite(doc, offset);

                    if (lineEnd != -1) {
                        StringBuilder sb = new StringBuilder(doc.getLength());
                        int lineStart = Utilities.getRowStart(doc, offset);
                        String line = doc.getText(lineStart, lineEnd - lineStart + 1);
                        int removeChars = 0;
                        int removeOffset = lineEnd;

                        if (line.endsWith(".") || line.endsWith("(")) {
                            removeChars = 1;
                        } else if (line.endsWith("::")) {
                            removeChars = 2;
                            removeOffset = lineEnd - 1;
                        } else if (line.endsWith(",)")) {
                            // Handle lone comma in parameter list - e.g.
                            // type "foo(a," -> you end up with "foo(a,|)" which doesn't parse - but
                            // the line ends with ")", not "," !
                            // Just remove the comma
                            removeChars = 1;
                            removeOffset = lineEnd - 1;
                        } else if (line.endsWith(", )")) {
                            // Just remove the comma
                            removeChars = 1;
                            removeOffset = lineEnd - 2;
                        }
                        
                        if (removeChars == 0) {
                            return false;
                        }

                        int rest = removeOffset + removeChars;

                        sb.append(doc.getText(0, removeOffset));

                        for (int i = 0; i < removeChars; i++) {
                            sb.append(' ');
                        }

                        if (rest < doc.getLength()) {
                            sb.append(doc.getText(rest, doc.getLength() - rest));
                        }
                        assert sb.length() == doc.getLength();

                        context.sanitizedRange = new OffsetRange(removeOffset, removeOffset +
                                removeChars);
                        context.sanitizedSource = sb.toString();
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
    private ParserResult sanitize(final Context context,
        final Sanitize sanitizing) {

        switch (sanitizing) {
        case NEVER:
            return new RubyParseResult(context.file);

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
            if (context.errorOffset != -1) {
                return parseBuffer(context, Sanitize.ERROR_DOT);
            }

        // Fall through to try the next trick
        case ERROR_DOT:

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
            return new RubyParseResult(context.file);
        }
    }

    private void notifyError(Context context, String key,
        Severity severity, String description, String details, int offset, Sanitize sanitizing) {
        // Replace a common but unwieldy JRuby error message with a shorter one
        if (description.startsWith("syntax error, expecting	")) { // NOI18N
            int start = description.indexOf(" but found "); // NOI18N
            assert start != -1;
            start += 11;
            int end = description.indexOf("instead", start); // NOI18N
            assert end != -1;
            String found = description.substring(start, end);
            description = details = NbBundle.getMessage(RubyParser.class, "UnexpectedError", found);
        }
        
        Error error =
            new DefaultError(key, description, details, context.file.getFileObject(),
                new DefaultPosition(offset), new DefaultPosition(offset), severity);
        context.listener.error(error);

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = offset;
        }
    }

    ParserResult parseBuffer(final Context context, final Sanitize sanitizing) {
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

        Reader content = new StringReader(source);

        RubyParserResult result = null;

        final boolean ignoreErrors = sanitizedSource;

        try {
            IRubyWarnings warnings =
                new IRubyWarnings() {
                    public void warn(ISourcePosition position, String message) {
                        if (!ignoreErrors) {
                            notifyError(context, null, Severity.WARNING, message, null,
                                position.getStartOffset(), sanitizing);
                        }
                    }

                    public boolean isVerbose() {
                        return false;
                    }

                    public void warn(String message) {
                        if (!ignoreErrors) {
                            notifyError(context, null, Severity.WARNING, message, null, -1,
                                sanitizing);
                        }
                    }

                    public void warning(String message) {
                        if (!ignoreErrors) {
                            notifyError(context, null, Severity.WARNING, message, null, -1,
                                sanitizing);
                        }
                    }

                    public void warning(ISourcePosition position, String message) {
                        if (!ignoreErrors) {
                            notifyError(context, null, Severity.WARNING, message, null,
                                position.getStartOffset(), sanitizing);
                        }
                    }
                };

            //warnings.setFile(file);
            DefaultRubyParser parser = new DefaultRubyParser();
            parser.setWarnings(warnings);

            if (sanitizing == Sanitize.NONE) {
                context.errorOffset = -1;
            }

            String fileName = "";

            if ((context.file != null) && (context.file.getFileObject() != null)) {
                fileName = context.file.getFileObject().getNameExt();
            }

            LexerSource lexerSource = new LexerSource(fileName, content, 0, true);
            RubyParserConfiguration configuration = new RubyParserConfiguration();
            result = parser.parse(configuration, lexerSource);
        } catch (SyntaxException e) {
            int offset = e.getPosition().getStartOffset();

            // XXX should this be >, and = length?
            if (offset >= source.length()) {
                offset = source.length() - 1;

                if (offset < 0) {
                    offset = 0;
                }
            }

            if (!ignoreErrors) {
                notifyError(context, null, Severity.ERROR, e.getMessage(),
                    e.getLocalizedMessage(), offset, sanitizing);
            }
        }

        Node root = (result != null) ? result.getAST() : null;

        RootNode realRoot = null;

        if (root instanceof RootNode) {
            // Quick workaround for now to avoid NPEs all over when
            // code looks at RootNode, whose getPosition()==null.
            // Its bodynode is what used to be returned as the root!
            realRoot = (RootNode)root;
            root = realRoot.getBodyNode();
        }

        if (root != null) {
            context.sanitized = sanitizing;
            AstRootElement rootElement = new AstRootElement(context.file.getFileObject(), root, result);
            AstNodeAdapter ast = new AstNodeAdapter(null, root);
            RubyParseResult r = new RubyParseResult(context.file, rootElement, ast, root, realRoot, result);
            r.setSanitized(context.sanitized, context.sanitizedRange);
            r.setSource(source);
            return r;
        } else {
            return sanitize(context, sanitizing);
        }
    }
    
    public PositionManager getPositionManager() {
        return positions;
    }

    public SemanticAnalyzer getSemanticAnalysisTask() {
        return new SemanticAnalysis();
    }

    public org.netbeans.api.gsf.OccurrencesFinder getMarkOccurrencesTask(int caretPosition) {
        OccurrencesFinder finder = new OccurrencesFinder();
        finder.setCaretPosition(caretPosition);

        return finder;
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> ElementHandle<T> createHandle(CompilationInfo info, final T object) {
        if (object instanceof KeywordElement) {
            // Not tied to an AST - just pass it around
            return new RubyElementHandle(null, object, info.getFileObject());
        }

        // TODO - check for Ruby
        if (object instanceof IndexedElement) {
            // Probably a function in a "foreign" file (not parsed from AST),
            // such as a signature returned from the index of the Ruby libraries.
// TODO - make sure this is infrequent! getFileObject is expensive!            
// Alternatively, do this in a delayed fashion - e.g. pass in null and in getFileObject
// look up from index            
            return new RubyElementHandle(null, object, ((IndexedElement)object).getFileObject());
        }

        if (!(object instanceof AstElement)) {
            return null;
        }

        ParserResult result = info.getParserResult();

        if (result == null) {
            return null;
        }

        ParserResult.AstTreeNode ast = result.getAst();

        if (ast == null) {
            return null;
        }

        Node root = AstUtilities.getRoot(info);

        return new RubyElementHandle(root, object, info.getFileObject());
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> T resolveHandle(CompilationInfo info, ElementHandle<T> handle) {
        RubyElementHandle h = (RubyElementHandle)handle;
        Node oldRoot = h.root;
        Node oldNode;

        if (h.object instanceof KeywordElement || h.object instanceof IndexedElement) {
            // Not tied to a tree
            return (T)h.object;
        }

        if (h.object instanceof AstElement) {
            oldNode = ((AstElement)h.object).getNode(); // XXX Make it work for DefaultComObjects...
        } else {
            return null;
        }

        Node newRoot = AstUtilities.getRoot(info);

        // Find newNode
        Node newNode = find(oldRoot, oldNode, newRoot);

        if (newNode != null) {
            Element co = AstElement.create(newNode);

            return (T)co;
        }

        return null;
    }

    private Node find(Node oldRoot, Node oldObject, Node newRoot) {
        // Walk down the tree to locate oldObject, and in the process, pick the same child for newRoot
        @SuppressWarnings("unchecked")
        List<?extends Node> oldChildren = oldRoot.childNodes();
        @SuppressWarnings("unchecked")
        List<?extends Node> newChildren = newRoot.childNodes();
        Iterator<?extends Node> itOld = oldChildren.iterator();
        Iterator<?extends Node> itNew = newChildren.iterator();

        while (itOld.hasNext()) {
            if (!itNew.hasNext()) {
                return null; // No match - the trees have changed structure
            }

            Node o = itOld.next();
            Node n = itNew.next();

            if (o == oldObject) {
                // Found it!
                return n;
            }

            // Recurse
            Node match = find(o, oldObject, n);

            if (match != null) {
                return match;
            }
        }

        if (itNew.hasNext()) {
            return null; // No match - the trees have changed structure
        }

        return null;
    }

    private static class RubyElementHandle<T extends Element> extends ElementHandle<T> {
        private final Node root;
        private final T object;
        private final FileObject fileObject;

        private RubyElementHandle(Node root, T object, FileObject fileObject) {
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
        /** Try to cut out the error line */
        ERROR_LINE, 
        /** Try to cut out the current edited line, if known */
        EDITED_LINE,
        /** Attempt to add an "end" to the end of the buffer to make it compile */
        MISSING_END,
    }

    /** Parsing context */
    public static class Context {
        private final ParserFile file;
        private final ParseListener listener;
        private int errorOffset;
        /** True if we've looked for a document and this file doesn't have one */
        private boolean noDocument;
        private BaseDocument doc;
        private String source;
        private String sanitizedSource;
        private OffsetRange sanitizedRange = OffsetRange.NONE;
        private int caretOffset;
        private Sanitize sanitized = Sanitize.NONE;
        
        Context(ParserFile parserFile, ParseListener listener, String source, int caretOffset) {
            this.file = parserFile;
            this.listener = listener;
            this.source = source;
            this.caretOffset = caretOffset;

        }
        
        void setDocument(BaseDocument doc) {
            this.doc = doc;
        }

        @Override
        public String toString() {
            return "RubyParser.Context(" + file.toString() + ")"; // NOI18N
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
