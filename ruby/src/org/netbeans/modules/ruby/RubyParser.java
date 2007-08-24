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
import org.netbeans.api.gsf.ElementKind;
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
    private int currentErrorOffset;
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
                int offset = reader.getCaretOffset(file);
                result = parseBuffer(file, offset, -1, buffer, listener, Sanitize.NONE);
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
    private String getSanitizedSource(ParserFile file, int caretOffset, int errorOffset,
        String buffer, OffsetRange[] sanitizedRange, Sanitize sanitizing) {
        // Let caretOffset represent the offset of the portion of the buffer we'll be operating on
        if ((sanitizing == Sanitize.ERROR_DOT) || (sanitizing == Sanitize.ERROR_LINE)) {
            caretOffset = errorOffset;
        }

        // Don't attempt cleaning up the source if we don't have the buffer position we need
        if (caretOffset == -1) {
            return buffer;
        }

        // The user might be editing around the given caretOffset.
        // See if it looks modified
        // Insert an end statement? Insert a } marker?
        FileObject fileObject = file.getFileObject();

        if (fileObject == null) {
            return buffer;
        }

        BaseDocument doc = AstUtilities.getBaseDocument(fileObject, false);

        if (doc == null) {
            return buffer;
        }

        if (caretOffset > doc.getLength()) {
            return buffer;
        }

        try {
            // Sometimes the offset shows up on the next line
            if (Utilities.isRowEmpty(doc, caretOffset) || Utilities.isRowWhite(doc, caretOffset)) {
                caretOffset = Utilities.getRowStart(doc, caretOffset)-1;
                if (caretOffset < 0) {
                    caretOffset = 0;
                }
            }

            if (!(Utilities.isRowEmpty(doc, caretOffset) || Utilities.isRowWhite(doc, caretOffset))) {
                if ((sanitizing == Sanitize.EDITED_LINE) || (sanitizing == Sanitize.ERROR_LINE)) {
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = Utilities.getRowLastNonWhite(doc, caretOffset);

                    if (lineEnd != -1) {
                        StringBuilder sb = new StringBuilder(doc.getLength());
                        int lineStart = Utilities.getRowStart(doc, caretOffset);
                        int rest = lineStart + 1;

                        sb.append(doc.getText(0, lineStart));
                        sb.append('#');

                        if (rest < doc.getLength()) {
                            sb.append(doc.getText(rest, doc.getLength() - rest));
                        }
                        assert sb.length() == doc.getLength();

                        sanitizedRange[0] = new OffsetRange(lineStart, lineEnd);

                        return sb.toString();
                    }
                } else {
                    assert sanitizing == Sanitize.ERROR_DOT;

                    // Try nuking dots/colons from this line
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = Utilities.getRowLastNonWhite(doc, caretOffset);

                    if (lineEnd != -1) {
                        StringBuilder sb = new StringBuilder(doc.getLength());
                        int lineStart = Utilities.getRowStart(doc, caretOffset);
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

                        int rest = removeOffset + removeChars;

                        sb.append(doc.getText(0, removeOffset));

                        for (int i = 0; i < removeChars; i++) {
                            sb.append(' ');
                        }

                        if (rest < doc.getLength()) {
                            sb.append(doc.getText(rest, doc.getLength() - rest));
                        }
                        assert sb.length() == doc.getLength();

                        sanitizedRange[0] = new OffsetRange(removeOffset, removeOffset +
                                removeChars);

                        return sb.toString();
                    }
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return buffer;
    }

    private void notifyError(ParseListener listener, ParserFile file, String key,
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
            new DefaultError(key, description, details, file.getFileObject(),
                new DefaultPosition(offset), new DefaultPosition(offset), severity);
        listener.error(error);

        if (sanitizing == Sanitize.NONE) {
            currentErrorOffset = offset;
        }
    }

    @SuppressWarnings("fallthrough")
    public ParserResult parseBuffer(final ParserFile file, int caretOffset, int errorOffset,
        final CharSequence sequence, final ParseListener listener, final Sanitize sanitizing) {
        String source = asString(sequence);
        boolean sanitizedSource = false;
        OffsetRange[] sanitizedRangeHolder = new OffsetRange[] { OffsetRange.NONE };

        if (!((sanitizing == Sanitize.NONE) || (sanitizing == Sanitize.NEVER))) {
            String s =
                getSanitizedSource(file, caretOffset, errorOffset, source, sanitizedRangeHolder,
                    sanitizing);

            if (s != source) {
                sanitizedSource = true;
                source = s;
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
                            notifyError(listener, file, null, Severity.WARNING, message, null,
                                position.getStartOffset(), sanitizing);
                        }
                    }

                    public boolean isVerbose() {
                        return false;
                    }

                    public void warn(String message) {
                        if (!ignoreErrors) {
                            notifyError(listener, file, null, Severity.WARNING, message, null, -1,
                                sanitizing);
                        }
                    }

                    public void warning(String message) {
                        if (!ignoreErrors) {
                            notifyError(listener, file, null, Severity.WARNING, message, null, -1,
                                sanitizing);
                        }
                    }

                    public void warning(ISourcePosition position, String message) {
                        if (!ignoreErrors) {
                            notifyError(listener, file, null, Severity.WARNING, message, null,
                                position.getStartOffset(), sanitizing);
                        }
                    }
                };

            //warnings.setFile(file);
            DefaultRubyParser parser = new DefaultRubyParser();
            parser.setWarnings(warnings);

            if (sanitizing == Sanitize.NONE) {
                currentErrorOffset = -1;
            }

            String fileName = "";

            if ((file != null) && (file.getFileObject() != null)) {
                fileName = file.getFileObject().getNameExt();
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
                notifyError(listener, file, null, Severity.ERROR, e.getMessage(),
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
            AstRootElement rootElement = new AstRootElement(file.getFileObject(), root, result);
            AstNodeAdapter ast = new AstNodeAdapter(null, root);
            RubyParseResult r = new RubyParseResult(file, rootElement, ast, root, realRoot, result);
            r.setSanitizedRange(sanitizedRangeHolder[0]);
            r.setSource(source);

            return r;
        } else {
            switch (sanitizing) {
            case NEVER:
                return new RubyParseResult(file);

            case NONE:

                // We've currently tried with no sanitization: try first level
                // of sanitization - removing dots/colons at the error offset
                // First try removing the dots or double colons around the failing error position
                if (currentErrorOffset != -1) {
                    return parseBuffer(file, caretOffset, currentErrorOffset, source, listener,
                        Sanitize.ERROR_DOT);
                }

            // Fall through to try the next trick
            case ERROR_DOT:

                // We've tried removing dots - now try removing the whole line at the error position
                if (caretOffset != -1) {
                    return parseBuffer(file, caretOffset, errorOffset, source, listener,
                        Sanitize.ERROR_LINE);
                }

            // Fall through to try the next trick
            case ERROR_LINE:

                // Messing with the error line didn't work - we could try "around" the error line
                // but I'm not attempting that now.
                // Finally try removing the whole line around the user editing position
                // (which could be far from where the error is showing up - but if you're typing
                // say a new "def" statement in a class, this will show up as an error on a mismatched
                // "end" statement rather than here
                if (caretOffset != -1) {
                    return parseBuffer(file, caretOffset, errorOffset, source, listener,
                        Sanitize.EDITED_LINE);
                }

            // Fall through for default handling
            case EDITED_LINE:default:

                // We're out of tricks - just return the failed parse result
                return new RubyParseResult(file);
            }
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
    enum Sanitize {
        /** Only parse the current file accurately, don't try heuristics */
        NEVER, 
        /** Perform no sanitization */
        NONE, 
        /** Try to remove the trailing . or :: at the error position, or the prior
         * line, or the caret line */
        ERROR_DOT, 
        /** Try to cut out the error line */
        ERROR_LINE, 
        /** Try to cut out the current edited line, if known */
        EDITED_LINE;
    }
}
