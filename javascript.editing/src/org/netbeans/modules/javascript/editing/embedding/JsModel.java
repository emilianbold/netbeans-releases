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
package org.netbeans.modules.javascript.editing.embedding;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.EditHistory;
import org.netbeans.modules.gsf.api.IncrementalEmbeddingModel;
import org.netbeans.modules.javascript.editing.JsAnalyzer;

/**
 * Creates a JavaScript model for a set of HTML fragments.
 * 
 * Hacked for now; just builds JavaScript directly from the RHTML file.
 * 
 * @todo Preserve script includes in the index somehow so I can figure out what files are included
 * @todo Preserve DOM elements somehow
 * @todo In RHTML files, add imports of the files in public/javascript...
 * 
 * @author Tor Norbye
 */
public class JsModel {

    private static final Logger LOGGER = Logger.getLogger(JsModel.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    private static final String RHTML_MIME_TYPE = "application/x-httpd-eruby"; // NOI18N
    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N
    private static final String JSP_MIME_TYPE = "text/x-jsp"; // NOI18N
    private static final String PHP_MIME_TYPE = "text/x-php5"; // NOI18N
    
    // If you change this, update the testcase reference 
    // in javascript.hints/test/unit/data/testfiles/generated.js
    // Also sync Rhino's Parser.java patched class
    private static final String GENERATED_IDENTIFIER = " __UNKNOWN__ "; // NOI18N
    
    private final Document doc;
    private final ArrayList<CodeBlockData> codeBlocks = new ArrayList<CodeBlockData>();
    private String jsCode;
    //private String sourceCode; // For debugging purposes
    private boolean documentDirty = true;
    /** Caching */
    private int prevAstOffset; // Don't need to initialize: the map 0 => 0 is correct
    /** Caching */
    private int prevLexOffset;

    public static JsModel get(Document doc) {
        JsModel model = (JsModel) doc.getProperty(JsModel.class);
        if (model == null) {
            model = new JsModel(doc);
            doc.putProperty(JsModel.class, model);
        }

        return model;
    }

    private JsModel(Document doc) {
        this.doc = doc;

        if (doc != null) { // null in some unit tests
            TokenHierarchy hi = TokenHierarchy.get(doc);
            hi.addTokenHierarchyListener(new TokenHierarchyListener() {

                public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
                    documentDirty = true;
                }
            });
        }
    }

    public String getJsCode() {
        if (documentDirty) {
            documentDirty = false;

            // Debugging
            //try {
            //    sourceCode = doc.getText(0, doc.getLength());
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
            codeBlocks.clear();
            StringBuilder buffer = new StringBuilder();

            BaseDocument d = (BaseDocument) doc;
            try {
                d.readLock();
                TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get(doc);
                TokenSequence tokenSequence = tokenHierarchy.tokenSequence(); //get top level token sequence
                //TokenSequence<TokenId> tokenSequence = tokenHierarchy.tokenSequence(); //get top level token sequence

                // Depend on RhtmlTokenId?
                //String RHTML_MIME_TYPE = RhtmlTokenId.MIME_TYPE;
                //String HTML_MIME_TYPE = HTMLKit.HTML_MIME_TYPE;

                String mimeType = (String) doc.getProperty("mimeType");
                if (RHTML_MIME_TYPE.equals(mimeType)) {
                    extractJavaScriptFromRhtml((TokenSequence<? extends TokenId>) tokenSequence, buffer);
                } else if (HTML_MIME_TYPE.equals(mimeType)) {
                    @SuppressWarnings("unchecked")
                    TokenSequence<? extends HTMLTokenId> hts = (TokenSequence<? extends HTMLTokenId>) tokenSequence;
                    extractJavaScriptFromHtml(hts, buffer, new JsAnalyzerState());
                } else if (JSP_MIME_TYPE.equals(mimeType)) {
                    extractJavaScriptFromJsp((TokenSequence<? extends TokenId>) tokenSequence, buffer);
                } else if (PHP_MIME_TYPE.equals(mimeType)) {
                    extractJavaScriptFromPHP((TokenSequence<? extends TokenId>) tokenSequence, buffer);
                }
            } finally {
                d.readUnlock();
            }
            jsCode = buffer.toString();
        }

        if (LOG) {
            LOGGER.log(Level.FINE, "===== VIRTUAL JavaScript SOURCE =====");
            LOGGER.log(Level.FINE, jsCode);
            LOGGER.log(Level.FINE, "=====================================");
        }

        return jsCode;
    }

//    static Set<String> EVENT_HANDLER_NAMES = new HashSet<String>();
//    static {
//        // See http://www.w3.org/TR/html401/interact/scripts.html
//        EVENT_HANDLER_NAMES.add("onload"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onunload"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onclick"); // NOI18N
//        EVENT_HANDLER_NAMES.add("ondblclick"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onmousedown"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onmouseup"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onmouseover"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onmousemove"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onmouseout"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onfocus"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onblur"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onkeypress"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onkeydown"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onkeyup"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onsubmit"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onreset"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onselect"); // NOI18N
//        EVENT_HANDLER_NAMES.add("onchange"); // NOI18N
//    }
    /** Create a JavaScript model of the given JSP buffer.
     * @todo Make this more general purpose (so it can be used from HTML, JSP etc.)
     * @param outputBuffer The buffer to emit the translation to
     * @param tokenHierarchy The token hierarchy for the RHTML code
     * @param tokenSequence  The token sequence for the RHTML code
     */
    private void extractJavaScriptFromJsp(TokenSequence<? extends TokenId> tokenSequence, StringBuilder outputBuffer) {
        StringBuilder buffer = outputBuffer;

        //TODO - implement the "classpath" import for other projects
        //how is the javascript classpath done????????/

        JsAnalyzerState state = new JsAnalyzerState();

        while (tokenSequence.moveNext()) {
            Token<? extends TokenId> token = tokenSequence.token();

            if (token.id().primaryCategory().equals("text")) { // NOI18N
                TokenSequence<? extends HTMLTokenId> ts = tokenSequence.embedded(HTMLTokenId.language());
                if (ts == null) {
                    continue;
                }
                extractJavaScriptFromHtml(ts, buffer, state);
                
            } else if (token.id().primaryCategory().equals("expression-language") 
                    || token.id().primaryCategory().equals("scriptlet")
                    || token.id().primaryCategory().equals("symbol") && "/>".equals(token.text().toString())) { // NOI18N
                //The test for jsp /> symbol means 
                //that we just encountered an end of jsp tag without body
                //so it is possible/likely the tag generates something
                
                //TODO Add a list of know tags and adjust the heuristics according
                //to the tag declaration. It may work nicely using the 
                //JSP parser for getting custom jsp tags metadata.
                
                //TODO The whole implementation of the JsJspModel
                //should be in separate file in JSP module and should 
                //depend on both lexers instead of these string dependencies.
                
                if (state.in_inlined_javascript || state.in_javascript) {
                    int sourceStart = tokenSequence.offset();
                    int sourceEnd = sourceStart + token.length();
                    int generatedStart = buffer.length();
                    buffer.append(GENERATED_IDENTIFIER);
                    int generatedEnd = buffer.length();
                    CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                            generatedEnd);
                    codeBlocks.add(blockData);
                }
            }
        }

    }

    private void extractJavaScriptFromPHP(TokenSequence<? extends TokenId> tokenSequence, StringBuilder outputBuffer) {
        StringBuilder buffer = outputBuffer;

        //TODO - implement the "classpath" import for other projects
        //how is the javascript classpath done????????/

        JsAnalyzerState state = new JsAnalyzerState();

        while (tokenSequence.moveNext()) {
            Token<? extends TokenId> token = tokenSequence.token();

            if (token.id().name().equals("T_INLINE_HTML")) { // NOI18N
                TokenSequence<? extends HTMLTokenId> ts = tokenSequence.embedded(HTMLTokenId.language());
                if (ts == null) {
                    continue;
                }
                extractJavaScriptFromHtml(ts, buffer, state);
            }
            if (state.in_inlined_javascript || state.in_javascript) {
                int sourceStart = tokenSequence.offset();

                //find end of the php code
                while (tokenSequence.moveNext()) {

                    token = tokenSequence.token();

                    if (token.id().name().equals("T_INLINE_HTML")) {
                        //we are out of php code
                        tokenSequence.movePrevious();
                        int sourceEnd = sourceStart + token.length();
                        int generatedStart = buffer.length();
                        buffer.append(GENERATED_IDENTIFIER);
                        int generatedEnd = buffer.length();
                        CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                                generatedEnd);
                        codeBlocks.add(blockData);
                        break;
                    }


                }
            }

        }
    }

    /** Create a JavaScript model of the given RHTML buffer.
     * @todo Make this more general purpose (so it can be used from HTML, JSP etc.)
     * @param outputBuffer The buffer to emit the translation to
     * @param tokenHierarchy The token hierarchy for the RHTML code
     * @param tokenSequence  The token sequence for the RHTML code
     */
    private void extractJavaScriptFromRhtml(TokenSequence<? extends TokenId> tokenSequence,
            StringBuilder outputBuffer) {
        StringBuilder buffer = outputBuffer;
        // Add a super class such that code completion, goto declaration etc.
        // knows where to pull the various link_to etc. methods from
//        // Pretend that this code is an extension to ActionView::Base such that
//        // code completion, go to declaration etc. sees the inherited methods from
//        // ActionView -- link_to and friends.
//        buffer.append("class ActionView::Base\n"); // NOI18N
//        // TODO Try to include the helper class as well as the controller fields too;
//        // for now this logic is hardcoded into Js's code completion engine (CodeCompleter)
//
//        // Erubis uses _buf; I've seen eruby using something else (_erbout?)
//        buffer.append("_buf='';"); // NOI18N
//        codeBlocks.add(new CodeBlockData(0, 0, 0, buffer.length()));
/* This could be a huge bottleneck - see http://www.netbeans.org/issues/show_bug.cgi?id=134329
        FileObject fo = GsfUtilities.findFileObject(doc);
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                StringBuilder sb = new StringBuilder();
                FileObject javascriptFolder = project.getProjectDirectory().getFileObject("public/javascripts"); // NOI18N
                if (javascriptFolder != null) {
                    addJavaScriptFiles(javascriptFolder, sb);
                    if (sb.length() > 0) {
                        // Insert a file link
                        //int sourceStart = ts.offset(); 
                        int sourceStart = 0;
                        String path = sb.toString();
                        String insertText = JsAnalyzer.NETBEANS_IMPORT_FILE + "(" + path + ");\n"; // NOI18N
                        // This corresponds to a 0-size block in the source
                        int sourceEnd = sourceStart;
                        int generatedStart = buffer.length();
                        buffer.append(insertText);
                        int generatedEnd = buffer.length();
                        CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                                generatedEnd);
                        codeBlocks.add(blockData);
                    }
                }
            }
        }
*/

        JsAnalyzerState state = new JsAnalyzerState();

        // Rails automatically inserts certain JavaScript libraries:
        //  <script type="text/javascript" src="javascripts/prototype.js"></script>
        //  <script type="text/javascript" src="javascripts/effects.js"></script>        
        // I need to insert includes for these
        // Actually, see 
        //  actionpack-2.0.2/lib/action_view/helpers/asset_tag_helper.rb
        // and #javascript_include_tag in particular for details on inclusions.
        // I need to go and model these guys - which could come from layout
        // pages etc.
        //./app/views/admin/themes/index.rhtml:2:<%= javascript_include_tag 'mephisto/dialog' %>
        //./app/views/layouts/application.rhtml:8:    <%= javascript_include_tag 'mephisto/prototype', 'mephisto/effects', 'mephisto/dragdrop', 'mephisto/lowpro', 'mephisto/application' %>
        //./app/views/layouts/simple.rhtml:8:    <%= javascript_include_tag 'mephisto/prototype', 'mephisto/effects', 'mephisto/lowpro', 'mephisto/application' %>
        // For now, just recursively include all the JavaScript files in public/javascript/
        //FileObject fo = 


        while (tokenSequence.moveNext()) {
            Token<? extends TokenId> token = tokenSequence.token();

            // Conversion algorithm:
            // Translate <html> content into into variables assigned to the right types?
            //    e.g. anything with "id" should be set, as should <TBD>
            // Concatenate <script> blocks verbatim
            // In Ruby code, look for  javascript_include_tag :defaults
            // which will include the files in rails' public/javascripts/.
            // Handle <link> script includes. Should I worry about anything else ("this" file
            //   being run in the context of something else).
            //  TODO: Ruby - look for embedded Ruby sections like this:
            // ./admin/users/_toggles.rhtml:3:      <%= check_box_tag user.dom_id(:user_toggle), user.id,  user.deleted_at.nil?, :onclick => "UserForm.toggle(this);" %>


            if (token.id().primaryCategory().equals("html")) { // NOI18N
                TokenSequence<? extends HTMLTokenId> ts = tokenSequence.embedded(HTMLTokenId.language());
                if (ts == null) {
                    continue;
                }
                extractJavaScriptFromHtml(ts, buffer, state);
            } else if (token.id().primaryCategory().equals("ruby")) { // NOI18N
                // TODO - look for JavaScript context in Ruby, like this onclick handler:
                // <%= submit_tag 'Upload', :onclick => "Element.show('upload-spinner')" %>
                // TODO insert missing portions in embedded javascript,
                //  example1:   <script>puts "Hello <%= get_name %>"</script>
                //  example2:   <input id="<%= id %>">

                // TODO - make sure it's NOT an erb comment!!!
                if (state.in_inlined_javascript || state.in_javascript) {
                    int sourceStart = tokenSequence.offset();
                    int sourceEnd = sourceStart + token.length();
                    int generatedStart = buffer.length();
                    buffer.append(GENERATED_IDENTIFIER);
                    int generatedEnd = buffer.length();
                    CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                            generatedEnd);
                    codeBlocks.add(blockData);
                }
            }
        }

//        // Close off the class
//        // eruby also ends with this statement: _buf.to_s
//        String end = "\nend\n"; // NOI18N
//        buffer.append(end);
//        if (doc != null) {
//            codeBlocks.add(new CodeBlockData(doc.getLength(), doc.getLength(), buffer.length()-end.length(), buffer.length()));
//        }
    }

    //internal JS analyzer states
    protected static final String JS = "in_javascript";
    protected static final String JS_INLINED = "in_inlined_javascript";
//    private static final String QUTE_CUT = "quote_cut";    

    
    /** @return True iff we're still in the middle of an embedded token */
    private void extractJavaScriptFromHtml(TokenSequence<? extends HTMLTokenId> ts,
            StringBuilder buffer, JsAnalyzerState state) {
        // NOI18N
        // Process the HTML content: look for embedded script blocks,
        // as well as JavaScript event handlers in element attributes.
        ts.moveStart();
        tokens:
        while (ts.moveNext()) {
            Token<? extends HTMLTokenId> htmlToken = ts.token();
            TokenId htmlId = htmlToken.id();
            if (htmlId == HTMLTokenId.SCRIPT) {
                state.in_javascript = true;
                // Emit the block verbatim
                int sourceStart = ts.offset();
                String text = htmlToken.text().toString();
                
                // Make sure it doesn't start with <!--, if it does, remove it
                // (this is a mechanism used in files to gracefully handle older browsers)
                int start = 0;
                for (; start < text.length(); start++) {
                    char c = text.charAt(start);
                    if (!Character.isWhitespace(c)) {
                        break;
                    }
                }
                if (start < text.length() && text.startsWith("<!--", start)) {
                    int lineEnd = text.indexOf('\n', start);
                    if (lineEnd != -1) {
                        lineEnd++; //skip the \n
                        sourceStart += lineEnd;
                        text = text.substring(lineEnd);
                    }
                }

                int sourceEnd = sourceStart + text.length();
                int generatedStart = buffer.length();
                buffer.append(text);
                int generatedEnd = buffer.length();
                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                        generatedEnd);
                codeBlocks.add(blockData);
            } else if (htmlId == HTMLTokenId.TAG_OPEN) {
                String text = htmlToken.text().toString();

                // TODO - if we see a <script src="someurl"> block that also
                // has a nonempty body, warn - the body will be ignored!!
                // (This should be a quickfix)
                if ("script".equals(text)) {
                    // Look for "<script src=" and if found, locate any includes.
                    // Quit when I find TAG_CLOSE or run out of tokens
                    // (for files with errors)
                    TokenSequence<? extends HTMLTokenId> ets = ts.subSequence(ts.offset());
                    ets.moveStart();
                    boolean foundSrc = false;
                    boolean foundType = false;
                    String type = null;
                    String src = null;
                    while (ets.moveNext()) {
                        Token<? extends HTMLTokenId> t = ets.token();
                        TokenId id = t.id();
                        // TODO - if we see a DEFER attribute here record that somehow
                        // such that I can have a quickfix look to make sure you don't try
                        // to mess with the document!
                        if (id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                            break;
                        } else if (foundSrc || foundType) {
                            if (id == HTMLTokenId.ARGUMENT) {
                                break;
                            } else if (id == HTMLTokenId.VALUE) {
                                // Found a script src
                                if (foundSrc) {
                                    src = t.toString();
                                } else {
                                    assert foundType;
                                    type = t.toString();
                                }
                                foundSrc = false;
                                foundType = false;
                            }
                        } else if (id == HTMLTokenId.ARGUMENT) {
                            String val = t.toString();
                            if ("src".equals(val)) {
                                foundSrc = true;
                            } else if ("type".equals(val)) {
                                foundType = true;
                            }
                        }
                    }
                    if (src != null) {
                        if (type == null || type.toLowerCase().indexOf("javascript") != -1) {
                            if (src.length() > 2 && src.startsWith("\"") && src.endsWith("\"")) {
                                src = src.substring(1, src.length() - 1);
                            }
                            if (src.length() > 2 && src.startsWith("'") && src.endsWith("'")) {
                                src = src.substring(1, src.length() - 1);
                            }

                            // Insert a file link
                            int sourceStart = ts.offset();
                            String insertText = JsAnalyzer.NETBEANS_IMPORT_FILE + "('" + src + "');\n"; // NOI18N
                            // This corresponds to a 0-size block in the source
                            int sourceEnd = sourceStart;
                            int generatedStart = buffer.length();
//                            if (buffer.length() > 0 && !Character.isWhitespace(buffer.charAt(buffer.length()-1))) {
//                                buffer.append("\n");
//                            }
                            buffer.append(insertText);
                            int generatedEnd = buffer.length();
                            CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                                    generatedEnd);
                            codeBlocks.add(blockData);
                        }
                    }
                }

            } else if (state.in_javascript && htmlId == HTMLTokenId.TEXT) {
                int sourceStart = ts.offset();
                String text = htmlToken.text().toString();
                int sourceEnd = sourceStart + text.length();
                int generatedStart = buffer.length();
                buffer.append(text);
                int generatedEnd = buffer.length();
                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                        generatedEnd);
                codeBlocks.add(blockData);
            } else if (htmlId == HTMLTokenId.VALUE_JAVASCRIPT) {

                int sourceStart = ts.offset();
                int sourceEnd = sourceStart + htmlToken.length();
                
                // Look for well known attribute names that are associated
                // with JavaScript event handlers
                String value = htmlToken.toString();
                // Strip surrounding "'s
                if (value.length() >= 1 ) {
                    char fch = value.charAt(0);
//                    char lch = value.charAt(value.length() -1);
                    if((fch == '\'' || fch == '"') && !state.in_inlined_javascript) {
                        state.opening_quotation_stripped = true;
                        value = value.substring(1);
                        sourceStart++; //skip the quotation
                        sourceEnd--; //skip the quotation
                    }
                }
                
//              if (htmlId == HTMLTokenId.ARGUMENT) {
//                String name = htmlToken.toString();
//                if (EVENT_HANDLER_NAMES.contains(name)) {
//                    // Look for VALUE, possibly skipping OPERATOR and WS
//                    // Spit out the attribute value in a function
//                    String value = null;
//                    while (ts.moveNext()) {
//                        TokenId tid = ts.token().id();
//                        if (tid == HTMLTokenId.VALUE) {
//                            value = ts.token().text().toString();
//                            break;
//                        } else if (tid != HTMLTokenId.WS && tid != HTMLTokenId.OPERATOR) {
//                            ts.movePrevious();
//                            continue tokens;
//                        }
//                    }
//                    if (value == null) {
//                        continue;
//                    }
//                    if (value.length() > 2 &&
//                            value.charAt(0) == '"' && value.charAt(value.length()-1) == '"') {
//                        value = value.substring(1, value.length()-1);
//                    }
//                }

                int ampIndex = value.indexOf('&');
                if (ampIndex != -1) {
                    // XML attributes are escaped
                    // TODO - worry about case here? &AMP; etc?
                    value = value.replace("&lt;", "<");
                    value = value.replace("&apos;", "'");
                    value = value.replace("&quot;", "\"");
                    value = value.replace("&amp;", "&");
                }

                
                String text = value;
//                int sourceEnd = sourceStart + text.length();
                

                if(!state.in_inlined_javascript) {
                    //first inlined JS section - add the prelude
                    
                    // Add a function context around the event handler
                    // such that it gets proper function context (e.g.
                    // it can return values, the way event handlers can)
                    int beforePrelude = buffer.length();
                    buffer.append(";function(){\n"); // NOI18N
                    // TODO: Assign "this = " some kind of object type
                    // based on where we are
                    int afterPrelude = buffer.length();
                    codeBlocks.add(new CodeBlockData(sourceStart, sourceStart, beforePrelude,
                            afterPrelude));
                } 
                
                state.in_inlined_javascript = true;

                //subsequent inlined section - just copy the js code
                // Emit the block verbatim
                int generatedStart = buffer.length();
                buffer.append(text);
                int generatedEnd = buffer.length();
                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd,
                        generatedStart, generatedEnd);
                codeBlocks.add(blockData);
                
                state.lastCodeBlock = blockData;

            } else if(state.in_inlined_javascript && htmlId != HTMLTokenId.VALUE_JAVASCRIPT) {
                //end of inlined javascript section - add postlude

                if(state.opening_quotation_stripped) {
                    //remove the ending quotation from the buffer and adjust the last codeblock
                    CodeBlockData last = state.lastCodeBlock;
                    char lastChar = buffer.charAt(buffer.length() - 1);
                    if (lastChar == '\'' || lastChar == '"') {
                        //ok, remove the ending quotation
                        buffer.deleteCharAt(buffer.length() - 1);
                        last.generatedEnd--;
                    } else {
                        //strange, opening quotation stripped but no ending quotation found???
                    }
                }
                
                state.in_inlined_javascript = false;
                state.opening_quotation_stripped = false;
                state.lastCodeBlock = null;
                
                int sourceStart = ts.offset();
                
                // Finish the surrounding function context
                int beforePostlude = buffer.length();
                // Add a newline to reduce the possiblity of the parser
                // error recovery code to get confused (since it tries removing
                // the currently edited line from input etc., and we don't want
                // it to take out this } )
                buffer.append("\n}\n"); // NOI18N
                int afterPostlude = buffer.length();
                codeBlocks.add(new CodeBlockData(sourceStart, sourceStart, beforePostlude,
                        afterPostlude));
            } else if (htmlId == HTMLTokenId.TAG_CLOSE && "script".equals(htmlToken.toString())) {
                buffer.append("\n");
            } else {
                // TODO - stash some other DOM stuff into the JavaScript
                // file such that I can refer to them from within JavaScript
                state.in_javascript = false;
            }
        }
    }

    public int sourceToGeneratedPos(int sourceOffset) {
        // Caching
        if (prevLexOffset == sourceOffset) {
            return prevAstOffset;
        }
        prevLexOffset = sourceOffset;

        // TODO - second level of caching on the code block to catch
        // nearby searches

        // Not checking dirty flag here; sourceToGeneratedPos() should apply
        // to the positions as they were when we generated the ruby code

        CodeBlockData codeBlock = getCodeBlockAtSourceOffset(sourceOffset);

        if (codeBlock == null) {
            return -1; // no embedded java code at the offset
        }

        int offsetWithinBlock = sourceOffset - codeBlock.sourceStart;
        int generatedOffset = codeBlock.generatedStart + offsetWithinBlock;
        if (generatedOffset <= codeBlock.generatedEnd) {
            prevAstOffset = generatedOffset;
        } else {
            prevAstOffset = codeBlock.generatedEnd;
        }

        return prevAstOffset;
    }

    public int generatedToSourcePos(int generatedOffset) {
        // Caching
        if (prevAstOffset == generatedOffset) {
            return prevLexOffset;
        }
        prevAstOffset = generatedOffset;
        // TODO - second level of caching on the code block to catch
        // nearby searches


        // Not checking dirty flag here; generatedToSourcePos() should apply
        // to the positions as they were when we generated the ruby code

        CodeBlockData codeBlock = getCodeBlockAtGeneratedOffset(generatedOffset);

        if (codeBlock == null) {
            return -1; // no embedded java code at the offset
        }

        int offsetWithinBlock = generatedOffset - codeBlock.generatedStart;
        int sourceOffset = codeBlock.sourceStart + offsetWithinBlock;
        if (sourceOffset <= codeBlock.sourceEnd) {
            prevLexOffset = sourceOffset;
        } else {
            prevLexOffset = codeBlock.sourceEnd;
        }

        return prevLexOffset;
    }

    IncrementalEmbeddingModel.UpdateState incrementalUpdate(EditHistory history) {
        // Clear cache
        //prevLexOffset = prevAstOffset = 0;
        prevLexOffset = history.convertOriginalToEdited(prevLexOffset);
        
        int offset = history.getStart();
        int limit = history.getOriginalEnd();
        int delta = history.getSizeDelta();

        boolean codeOverlaps = false;
        for (CodeBlockData codeBlock : codeBlocks) {
            // Block not affected by move
            if (codeBlock.sourceEnd < offset) {
                continue;
            }
            if (codeBlock.sourceStart > limit) {
                codeBlock.sourceStart += delta;
                codeBlock.sourceEnd += delta;
                continue;
            }
            if (codeBlock.sourceStart <= offset && codeBlock.sourceEnd >= limit) {
                codeBlock.sourceEnd += delta;
                codeOverlaps = true;
                continue;
            }
            return IncrementalEmbeddingModel.UpdateState.FAILED;
        }
        
        return codeOverlaps ? IncrementalEmbeddingModel.UpdateState.UPDATED : IncrementalEmbeddingModel.UpdateState.COMPLETED;
    }

    //private void addJavaScriptFiles(FileObject file, StringBuilder sb) {
    //    if (file.isFolder()) {
    //        for (FileObject child : file.getChildren()) {
    //            addJavaScriptFiles(child, sb);
    //        }
    //    } else if (JsUtils.isJsFile(file)) {
    //        if (sb.length() > 0) {
    //            sb.append(","); // NOI18N
    //        }
    //        sb.append("\""); // NOI18N
    //        String src = file.getNameExt(); // TODO - fuller path?
    //        sb.append(src);
    //        sb.append("\""); // NOI18N
    //    }
    //}

    private CodeBlockData getCodeBlockAtSourceOffset(int offset) {
        for (CodeBlockData codeBlock : codeBlocks) {
            if (codeBlock.sourceStart <= offset && codeBlock.sourceEnd >= offset) {
                return codeBlock;
            }
        }
        return null;
    }

    private CodeBlockData getCodeBlockAtGeneratedOffset(int offset) {
        // TODO - binary search!! they are ordered!
        for (CodeBlockData codeBlock : codeBlocks) {
            if (codeBlock.generatedStart <= offset && codeBlock.generatedEnd >= offset) {
                return codeBlock;
            }
        }
        
        return null;
    }

    public static boolean isGeneratedIdentifier(String ident) {
        return GENERATED_IDENTIFIER.trim().equals(ident);
    }
    
    static class JsAnalyzerState {
        boolean in_javascript = false;
        boolean in_inlined_javascript = false;
        boolean opening_quotation_stripped = false;
        CodeBlockData lastCodeBlock =  null;
    }
    
    private class CodeBlockData {

        /** Start of section in RHTML file */
        private int sourceStart;
        /** End of section in RHTML file */
        private int sourceEnd;
        /** Start of section in generated Js */
        private int generatedStart;
        /** End of section in generated Js */
        private int generatedEnd;

        public CodeBlockData(int sourceStart, int sourceEnd, int generatedStart, int generatedEnd) {
            this.sourceStart = sourceStart;
            this.generatedStart = generatedStart;
            this.sourceEnd = sourceEnd;
            this.generatedEnd = generatedEnd;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("CodeBlockData[");
            sb.append("\n  SOURCE(" + sourceStart + "," + sourceEnd + ")");
            //sb.append("=\"");
            //sb.append(sourceCode.substring(sourceStart, sourceEnd));
            //sb.append("\"");
            sb.append(",\n  JAVASCRIPT(" + generatedStart + "," + generatedEnd + ")");
            //sb.append("=\"");
            //sb.append(jsCode.substring(generatedStart,generatedEnd));
            //sb.append("\"");
            sb.append("]");

            return sb.toString();
        }
    }

    // For debugging only; pass in "rubyCode" or "rhtmlCode" in JsModel to print
    //private String debugPos(String code, int pos) {
    //    if (pos == -1) {
    //        return "<-1:notfound>";
    //    }
    //
    //    int start = pos-15;
    //    if (start < 0) {
    //        start = 0;
    //    }
    //    int end = pos+15;
    //    if (end > code.length()) {
    //        end = code.length();
    //    }
    //
    //    return code.substring(start, pos) + "^" + code.substring(pos, end);
    //}
}
