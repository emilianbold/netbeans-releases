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
package org.netbeans.modules.ruby;

import org.netbeans.modules.gsf.api.ParserResult.AstTreeNode;
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
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.ruby.elements.Element;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.RubyElement;
import org.netbeans.modules.gsf.spi.DefaultError;
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
 * @todo Only look for missing-end if there's an unexpected end
 * @todo If you get a "class definition in method body" error, there's a missing
 *   end - prior to the class!
 * @todo "syntax error, unexpected tRCURLY" means that I also have a missing end,
 *   but we encountered a } before we got to it. I need to be bracketing this stuff.
 * 
 * @author Tor Norbye
 */
public final class RubyParser implements Parser {
    private final PositionManager positions = createPositionManager();

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
    public void parseFiles(Parser.Job job) {
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
                if (caretOffset != -1 && job.translatedSource != null) {
                    caretOffset = job.translatedSource.getAstOffset(caretOffset);
                }
                Context context = new Context(file, listener, source, caretOffset, job.translatedSource);
                result = parseBuffer(context, Sanitize.NONE);
            } catch (IOException ioe) {
                listener.exception(ioe);
            }

            ParseEvent doneEvent = new ParseEvent(ParseEvent.Kind.PARSE, file, result);
            listener.finished(doneEvent);
        }
    }

    protected PositionManager createPositionManager() {
        return new RubyPositionManager();
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

        if (sanitizing == Sanitize.MISSING_END) {
            context.sanitizedSource = context.source + ";end";
            int start = context.source.length();
            context.sanitizedRange = new OffsetRange(start, start+4);
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
                int start = RubyUtils.getRowFirstNonWhite(doc, offset);
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
            if (RubyUtils.isRowEmpty(doc, offset) || RubyUtils.isRowWhite(doc, offset)) {
                offset = RubyUtils.getRowStart(doc, offset)-1;
                if (offset < 0) {
                    offset = 0;
                }
            }

            if (!(RubyUtils.isRowEmpty(doc, offset) || RubyUtils.isRowWhite(doc, offset))) {
                if ((sanitizing == Sanitize.EDITED_LINE) || (sanitizing == Sanitize.ERROR_LINE)) {
                    // See if I should try to remove the current line, since it has text on it.
                    int lineEnd = RubyUtils.getRowLastNonWhite(doc, offset);

                    if (lineEnd != -1) {
                        StringBuilder sb = new StringBuilder(doc.length());
                        int lineStart = RubyUtils.getRowStart(doc, offset);
                        int rest = lineStart + 1;

                        sb.append(doc.substring(0, lineStart));
                        sb.append('#');

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
                    int lineStart = RubyUtils.getRowStart(doc, offset);
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
    private RubyParseResult sanitize(final Context context,
        final Sanitize sanitizing) {

        switch (sanitizing) {
        case NEVER:
            return createParseResult(context.file, null, null, null, null);

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
            return createParseResult(context.file, null, null, null, null);
        }
    }

    protected void notifyError(Context context, String key,
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
        
        // Initialize keys for errors needing it
        if (key == null) {
            key = description;
        }
        
        Error error =
            new DefaultError(key, description, details, context.file.getFileObject(),
                offset, offset, severity);
        context.listener.error(error);

        if (sanitizing == Sanitize.NONE) {
            context.errorOffset = offset;
        }
    }

    protected RubyParseResult parseBuffer(final Context context, final Sanitize sanitizing) {
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
            AstNodeAdapter ast = new AstNodeAdapter(null, root);
            RubyParseResult r = createParseResult(context.file, ast, root, realRoot, result);
            r.setSanitized(context.sanitized, context.sanitizedRange, context.sanitizedContents);
            r.setSource(source);
            return r;
        } else {
            return sanitize(context, sanitizing);
        }
    }
    
    protected RubyParseResult createParseResult(ParserFile file, AstTreeNode ast, Node root,
        RootNode realRoot, RubyParserResult jrubyResult) {
        return new RubyParseResult(this, file, ast, root, realRoot, jrubyResult);
    }
    
    public PositionManager getPositionManager() {
        return positions;
    }

    @SuppressWarnings("unchecked")
    public static RubyElement resolveHandle(CompilationInfo info, ElementHandle handle) {
        if (handle instanceof AstElement) {
            AstElement element = (AstElement)handle;
            CompilationInfo oldInfo = element.getInfo();
            if (oldInfo == info) {
                return element;
            }
            Node oldNode = element.getNode();
            Node oldRoot = AstUtilities.getRoot(oldInfo);
            
            Node newRoot = AstUtilities.getRoot(info);
            if (newRoot == null) {
                return null;
            }

            // Find newNode
            Node newNode = find(oldRoot, oldNode, newRoot);

            if (newNode != null) {
                AstElement co = AstElement.create(info, newNode);

                return co;
            }
        } else if (handle instanceof RubyElement) {
            return (RubyElement)handle;
        }

        return null;
    }

    private static Node find(Node oldRoot, Node oldObject, Node newRoot) {
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
    public static class Context {
        private final ParserFile file;
        private final ParseListener listener;
        private int errorOffset;
        private String source;
        private String sanitizedSource;
        private OffsetRange sanitizedRange = OffsetRange.NONE;
        private String sanitizedContents;
        private int caretOffset;
        private Sanitize sanitized = Sanitize.NONE;
        private TranslatedSource translatedSource;
        
        public Context(ParserFile parserFile, ParseListener listener, String source, int caretOffset, TranslatedSource translatedSource) {
            this.file = parserFile;
            this.listener = listener;
            this.source = source;
            this.caretOffset = caretOffset;
            this.translatedSource = translatedSource;
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
