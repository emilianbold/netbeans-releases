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

import java.awt.Color;
import java.io.CharConversionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.ruby.lexer.RubyCommentTokenId;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;


/**
 * Digest rdoc-formatter strings and format them as html,
 * A bit hacky at the moment, but it tries to understand most
 * of the conventions described here:
 * http://rdoc.sourceforge.net/doc/index.html
 * and produce reasonably similar HTML. It also relies on
 * the RubyCommentLexer.
 *
 * @todo Handle definition-style labelled blocks (I only do tabular ones at this point)
 *   where you have double brackets:  [[foo]]
 * @todo Only recognize call-seqs if they are the first pre block in the comment AND
 *   there is no preceeding text!
 * @todo Add italics around class names in the callseqs
 * @todo Swing Text often breaks up symbols in tables where it's on the left side; try 
 *   wrapping these in &lt;nobr&gt; tags. (Look at ClassMethods.paginate for example)
 * @todo When syntax highlighting potential ruby snippets, look for the common "=>" pattern
 *   and only attempt to tokenize the left hand side (and POSSIBLY) the right hand side)
 *   Look at the abbrev methods for example. It contains:
 *    <pre>
 *    #   %w{ car cone }.abbrev   #=> { "ca" => "car", "car" => "car",
 *    #                                 "co" => "cone", "con" => cone",
 *    #                                 "cone" => "cone" }
 *    </pre>
 *   Here I should tokenize the LHS and the RHS separately
 *  
 *
 * @author Tor Norbye
 */
class RDocFormatter {
    private boolean inVerbatim;
    private boolean inBulletedList;
    private boolean inLabelledList;
    private boolean inNumberedList;
    private List<String> code;
    private boolean firstVerbatim = true;
    private String seqName;
    private boolean wroteSignature = false;

    /**  State during rdoc generation: in suppressed comments (#--) section */
    private boolean noComment;
    private final StringBuilder sb = new StringBuilder(500);

    /** Creates a new instance of RDocFormatter */
    public RDocFormatter() {
    }

    /** Set method name associated with this rdoc, if any. Will be used by
     * the rdoc analyzer to highlight method calls in call-seqs, if any.
     * (Call-seq are method signatures baked into the rdoc, usually from
     * C comments in the standard library.)
     */
    public void setSeqName(String seqName) {
        this.seqName = seqName;
    }

    /** Return true if the formatted comment appears to have a signature that was processed */
    public boolean wroteSignature() {
        return wroteSignature;
    }
    
    public void appendLine(String text) {
        if (text.equals("#--")) { // NOI18N
            noComment = true;

            return;
        } else if (text.equals("#++")) { // NOI18N
            noComment = false;

            return;
        } else if (text.startsWith(TypeAnalyzer.PARAM_HINT_ARG) ||
                text.startsWith(TypeAnalyzer.PARAM_HINT_RETURN)) {
            // Don't include param hints in the documentation.
            // TODO: Try to include these correlated to the actual parameter list in the logical view.
            return;
        }

        if (noComment) {
            return;
        }

        if (text.startsWith("# ")) {
            text = text.substring(2);
        } else if (text.equals("#")) { // empty comment line
            text = "";
        }

        process(text);
    }

    private void process(String text) {
        // Use the lexer! The following is naive since it will
        // do something about URLs, multiplication (*), etc.
        if (text.length() == 0) {
            finishSection();

            return;
        }
        
        if (text.startsWith("* ") || text.startsWith("- ")) { // NOI18N
            if (!inBulletedList) {
                sb.append("<ul>\n"); // NOI18N
                inBulletedList = true;
            }

            sb.append("<li>"); // NOI18N
            appendTokenized(text.substring(text.indexOf(' ') + 1));

            return;
        } else if (text.matches("^[0-9]+\\. .*")) {
            if (!inNumberedList) {
                sb.append("<ol>\n"); // NOI18N
                inNumberedList = true;
            }

            sb.append("<li>"); // NOI18N
            appendTokenized(text.substring(text.indexOf(' ') + 1));

            return;
        } else if (text.matches("^\\[[\\S]+\\] .+")) { // NOI18N
            // Labelled list:  [+foo+] whatever
            if (!inLabelledList) {
                sb.append("<table>\n"); // NOI18N
                inLabelledList = true;
            } else {
                sb.append("</td></tr>\n"); // NOI18N
            }

            sb.append("<tr><td valign=\"top\">"); // NOI18N

            int index = text.indexOf("] ");
            appendTokenized(text.substring(1, index)); // label between []'s
            sb.append("</td><td>");
            appendTokenized(text.substring(index + 2));

            return;
        } else if (text.matches("^[\\S]+:: .*")) { // NOI18N
            // Labelled list:  foo::
            if (!inLabelledList) {
                sb.append("<table>\n"); // NOI18N
                inLabelledList = true;
            } else {
                sb.append("</td></tr>\n"); // NOI18N
            }

            sb.append("<tr><td valign=\"top\">"); // NOI18N

            int index = text.indexOf(":: "); // NOI18N
            appendTokenized(text.substring(0, index)); // label
            sb.append("</td><td>");
            appendTokenized(text.substring(index + 3));

            return;
        } else if (!inBulletedList && !inNumberedList && !inLabelledList &&
                text.length() > 0 && Character.isWhitespace(text.charAt(0))) { // Indented text in list is in same paragraph

            if (!inVerbatim) {
                sb.append("<pre>\n"); // NOI18N
                inVerbatim = true;
                code = new ArrayList<String>();
            }

            appendTokenized(text);

            return;
        } else if (text.startsWith("=")) { // NOI18N
            // Generate a heading
            // Count ='s

            int i = 0;

            for (; i < text.length(); i++) {
                if (text.charAt(i) != '=') {
                    break;
                }
            }

            if (i <= 6) {
                sb.append("<h"); // NOI18N
                sb.append(Integer.toString(i));
                sb.append(">"); // NOI18N
                sb.append(text.substring(i));
                sb.append("</h"); // NOI18N
                sb.append(Integer.toString(i));
                sb.append(">\n"); // NOI18N

                return;
            }

            // Normal line with lots of ='s
            appendTokenized(text);

            return;
        } else if (text.startsWith("#---")) { // NOI18N
                                              // Generate a separator
                                              // See if the line contains only -'s

            int i = 1;
            int n = text.length();

            for (; i < n; i++) {
                if (text.charAt(i) != '-') {
                    break;
                }
            }

            if (i == n) {
                sb.append("<hr>\n"); // NOI18N

                return;
            }

            appendTokenized(text);

            return;
        } else {
            if (text.startsWith("####")) {
                // Generate a separator
                // See if the line contains only #'s
                int i = 1;
                int n = text.length();

                for (; i < n; i++) {
                    if (text.charAt(i) != '#') {
                        break;
                    }
                }

                if (i == n) {
                    sb.append("<hr>\n");

                    return;
                }
            } else if (inVerbatim) {
                finishSection();
            }

            appendTokenized(text);

            return;
        }
    }

    private void appendTokenized(String text) {
        if (inVerbatim) {
            // We need to buffer up the text such that we can lex it as a unit
            // (and determine when done with the section if it's code or regular text)
            code.add(text);

            return;
        }

        firstVerbatim = false;
        
        appendTokenized(sb, text);
        
        if (inVerbatim) {
            sb.append("<br>");
        } else {
            sb.append(" "); // Ensure adjacent lines are separated by space.
        }
    }

    
    private void appendTokenized(StringBuilder sb, String text) {
        TokenHierarchy hi = TokenHierarchy.create(text, RubyCommentTokenId.language());

        TokenSequence ts = hi.tokenSequence();

        // If necessary move ts to the requested offset
        int offset = 0;
        ts.move(offset);

        if (ts.moveNext()) {
            do {
                Token t = ts.token();

                if ((t.id() == RubyCommentTokenId.COMMENT_TEXT) ||
                        (t.id() == RubyCommentTokenId.COMMENT_TODO)) {
                    try {
                        sb.append(XMLUtil.toElementContent(t.text().toString()));
                    } catch (CharConversionException cce) {
                        Exceptions.printStackTrace(cce);
                    }
                } else if (t.id() == RubyCommentTokenId.COMMENT_HTMLTAG) {
                    String s = t.text().toString();
                    char c = s.charAt(0);

                    if (c == '+') {
                        sb.append("<tt>");
                        sb.append(s.substring(1, s.length() - 1));
                        sb.append("</tt>");
                    } else {
                        sb.append(s);
                    }
                } else if (t.id() == RubyCommentTokenId.COMMENT_LINK) {
                    String s = t.text().toString();
                    sb.append("<a href=\"");
                    sb.append(s);
                    sb.append("\">");

                    if (s.startsWith("#")) {
                        s = s.substring(1); // Chop off leading #
                                            // Method reference
                                            // TODO - generate special URL for local methods here?
                    }

                    sb.append(s);
                    sb.append("</a>");
                } else if (t.id() == RubyCommentTokenId.COMMENT_ITALIC) {
                    sb.append("<i>");

                    String s = t.text().toString();
                    char c = s.charAt(0);

                    if (c == '_') {
                        sb.append(s.substring(1, s.length() - 1));
                    } else {
                        sb.append(t.text());
                    }

                    sb.append("</i>");
                } else if (t.id() == RubyCommentTokenId.COMMENT_BOLD) {
                    sb.append("<b>");

                    String s = t.text().toString();
                    char c = s.charAt(0);

                    if (c == '*') {
                        sb.append(s.substring(1, s.length() - 1));
                    } else {
                        sb.append(t.text());
                    }

                    sb.append("</b>");
                } else if (t.id() == RubyCommentTokenId.COMMENT_RDOC) {
                    // Do nothing - swallow these so they don't show up in the html
                }
            } while (ts.moveNext());
        }
    }
    
    private void finishSection() {
        if (inVerbatim) {
            boolean addHr = false;
            if ((code != null) && (code.size() > 0)) {
                if (formatAsRuby(code)) {
                    // Process code and format as Ruby
                    String html = getRubyHtml(code);
                    if (html != null) {
                        sb.append(html);
                    } else {
                        // Some kind of error; normal append
                        for (String s : code) {
                            try {
                                sb.append(XMLUtil.toElementContent(s));
                            } catch (CharConversionException cce) {
                                Exceptions.printStackTrace(cce);
                            }
                            sb.append("<br>"); // NOI18N
                        }
                    }
                } else {
                    if (isCallSeq(code)) {
                        String html = getCallSeqHtml(code);
                        sb.append(html);
                        addHr = true;
                        wroteSignature = true;
                    } else {
                        for (String s : code) {
                            appendTokenized(sb, s);
                            sb.append("<br>"); // NOI18N
                        }
                    }
                }
                code = null;
            }

            sb.append("</pre>\n"); // NOI18N
            if (addHr) {
               sb.append("<hr>\n"); // NOI18N
            }
            inVerbatim = false;
            firstVerbatim = false;
        }

        if (inBulletedList) {
            sb.append("</ul>\n"); // NOI18N
            inBulletedList = false;
        }

        if (inNumberedList) {
            sb.append("</ol>\n"); // NOI18N
            inBulletedList = false;
        }

        if (inLabelledList) {
            sb.append("</td></tr>\n</table>"); // NOI18N
            inLabelledList = false;
        }
    }

    public String toHtml() {
        finishSection();

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String getRubyHtml(List<String> source) {
        StringBuilder ruby = new StringBuilder(500);

        for (String s : source) {
            ruby.append(s);
            ruby.append("\n"); // NOI18N
        }
        
        Language<? extends TokenId> language = RubyTokenId.language();
        String mimeType = RubyMimeResolver.RUBY_MIME_TYPE;
        if (ruby.indexOf(" <%") != -1) { // NOI18N
            mimeType = "application/x-httpd-eruby"; // RHTML
            Collection<LanguageProvider> providers = (Collection<LanguageProvider>) Lookup.getDefault().lookupAll(LanguageProvider.class);
            for (LanguageProvider provider : providers) {
                language = provider.findLanguage(mimeType);
                if (language != null) {
                    break;
                }
            }

            if (language == null) {
                mimeType = RubyMimeResolver.RUBY_MIME_TYPE;
                language = RubyTokenId.language();
            }
        } else if (source.get(0).trim().startsWith("<")) {
            // Looks like markup (other than RHTML) - don't colorize it
            // since we don't know how
            return null;
        }
        
        StringBuilder buffer = new StringBuilder(1500);
        boolean errors = appendSequence(buffer, ruby.toString(), language, mimeType);
        
        // TODO: See
        // link_to_unless_current
        // in ActionView - it doesn't get highlighted right. Perhaps I should
        // retry rendering with RHTML if I see errors in Ruby handling? (and it
        // looks like it starts with HTML?)
        // Another common pattern seems to be a Ruby call (which should be shown as
        // Ruby) followed by a "=>" (indicating result) followed by something
        // which should be treated as RHTML/HTML. Perhaps I can split my output
        // processing?
        
        return errors ? null : buffer.toString();
    } 

    @SuppressWarnings("unchecked")
    private boolean appendSequence(StringBuilder sb, String text, 
            Language<? extends TokenId> language, String mimeType) {
        // XXX is this getting called twice?    
        MimePath mimePath = MimePath.parse(mimeType);
        Lookup lookup = MimeLookup.getLookup(mimePath);
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);

        TokenHierarchy hi = TokenHierarchy.create(text, language);
        TokenSequence ts = hi.tokenSequence();

        int offset = 0;
        ts.move(offset);

        if (ts.moveNext()) {
            do {
                Token t = ts.token();
                String tokenText = t.text().toString();
                
                // TODO - make style classes instead of inlining everything as font!
                String category = t.id().name();
                String primaryCategory = t.id().primaryCategory();
                
                if ("error".equals(primaryCategory)) { // NOI18N
                    // Abort: an error token means the output probably isn't
                    // code, or it's code or markup but in a different language
                    // than we're trying to process it as
                    return true;
                }

                AttributeSet attribs = fcs.getTokenFontColors(category);
                String escapedText = tokenText;
                try {
                    escapedText = XMLUtil.toElementContent(tokenText);
                } catch (CharConversionException cce) {
                    Exceptions.printStackTrace(cce);
                }

                if (attribs == null) {
                    category = primaryCategory;
                    attribs = fcs.getTokenFontColors(category);

                }

                TokenSequence embedded = ts.embedded();
                if (embedded != null) {
                    //embedded.languagePath().mimePath();
                    String embeddedMimeType = MimePath.parse(embedded.languagePath().mimePath()).getPath();
                    Color bg = null;
                    if (attribs != null) {
                        bg = (Color)attribs.getAttribute(StyleConstants.Background);
                        if (bg != null) {
                            sb.append("<span style=\"background:"); // NOI18N
                            sb.append(getHtmlColor(bg));
                            sb.append("\">"); // NOI18N
                        }
                    }
                    appendSequence(sb, tokenText, embedded.language(), embeddedMimeType);
                    if (bg != null) {
                        sb.append("</span>"); // NOI18N
                    }
                    continue;
                }

                if (attribs == null) {
                    sb.append(escapedText);

                    continue;
                }

                if (escapedText.indexOf('\n') != -1) {
                    escapedText = escapedText.replace("\n", "<br>"); // NOI18N
                }

                if (t.id() == GsfTokenId.WHITESPACE) {
                    sb.append(escapedText);
                } else {
                    sb.append("<span style=\""); // NOI18N

                    Color fg = (Color)attribs.getAttribute(StyleConstants.Foreground);

                    if (fg != null) {
                        sb.append("color:"); // NOI18N
                        sb.append(getHtmlColor(fg));
                        sb.append(";"); // NOI18N
                    }

                    Color bg = (Color)attribs.getAttribute(StyleConstants.Background);
                    
                    if (bg != null) {
                        sb.append("background:"); // NOI18N
                        sb.append(getHtmlColor(bg));
                        sb.append(";"); // NOI18N
                    }

                    Boolean b = (Boolean)attribs.getAttribute(StyleConstants.Bold);

                    if ((b != null) && b.booleanValue()) {
                        sb.append("font-weight:bold;"); // NOI18N
                    }

                    b = (Boolean)attribs.getAttribute(StyleConstants.Italic);

                    if ((b != null) && b.booleanValue()) {
                        sb.append("font-style:italic;"); // NOI18N
                    }

                    // TODO - underline, strikethrough, ... and FONTS!
                    sb.append("\">"); // NOI18N
                    sb.append(escapedText);
                    sb.append("</span>"); // NOI18N
                }
            } while (ts.moveNext());
        }
        
        return false;
    }

    /** 
     * Call seq: try to format the call seq in a special
     * way: bold the call-seq name, and also left justify all
     */
    private String getCallSeqHtml(List<String> code) {
        StringBuilder sb = new StringBuilder();
        
        // First determine how much to truncate from the left hand side
        int min = Integer.MAX_VALUE;
        for (String s : code) {
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                // TODO - make this work better for Tabs!
                if (c != ' ') {
                    if (i < min) {
                        min = i;
                    }
                    break;
                }
            }
        }
        
        if (min == Integer.MAX_VALUE) {
            min = 0;
        }

        for (String s : code) {
            // Truncate shared left hand side
            if (min > 0) {
                s = s.substring(min);
            }
            // Attempt
            if (seqName != null) {
                int index = s.indexOf(seqName);
                if (index != -1 && (s.length() > index+seqName.length())) {
                    char c = s.charAt(index+seqName.length());
                    if (!(c == ' ' || c == '(' || c == '{' || c == '[')) {
                        index = -1;
                    }
                }
                if (index != -1) {
                    String lhs = s.substring(0,index);
                    String rhs = ""; // NOI18N
                    if (s.length() > index+seqName.length()) {
                        rhs = s.substring(index+seqName.length());
                    }
                    try {
                        sb.append(XMLUtil.toElementContent(lhs));
                        sb.append("<b>"); // NOI18N
                        sb.append(XMLUtil.toElementContent(seqName));
                        sb.append("</b>"); // NOI18N
                        sb.append(XMLUtil.toElementContent(rhs));
                        sb.append("<br>"); // NOI18N
                    } catch (CharConversionException cce) {
                        Exceptions.printStackTrace(cce);
                    }
                    continue;
                }
            }
            appendTokenized(sb, s);
            sb.append("<br>"); // NOI18N
        }
        return sb.toString();
    }
    
    private static String getHtmlColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        StringBuffer result = new StringBuffer();
        result.append('#');

        String rs = Integer.toHexString(r);
        String gs = Integer.toHexString(g);
        String bs = Integer.toHexString(b);

        if (r < 0x10) {
            result.append('0');
        }

        result.append(rs);

        if (g < 0x10) {
            result.append('0');
        }

        result.append(gs);

        if (b < 0x10) {
            result.append('0');
        }

        result.append(bs);

        return result.toString();
    }
    
    private boolean isCallSeq(List<String> source) {
        if (firstVerbatim) {
            // See if it looks like a call seq - check the first line
            // TODO: MAke the code be a List<String> instead!!
            String first = source.get(0);
            if (first.indexOf("=>") != -1 || first.indexOf("->") != -1) { // NOI18N
                return true;
            }
        }
        
        return false;
    }
    
    private boolean formatAsRuby(List<String> source) {
        // Check for "---" to see if the preformatted text is a table
        // Avoid looking at call-seq lines (first preformatted section, in case it contains => or ->
        if (isCallSeq(source)) {
            return false;
        }

        for (String s : source) {
            // ASCII formatted tables such as the File.fnmatch
            if (s.indexOf("---") != -1) { // NOI18N
                return false;
            }
            
            if (s.indexOf(" | ") != -1) { // NOI18N
                return false;
            }
        }

        return true;
    }
}
