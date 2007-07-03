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

package org.netbeans.modules.ruby.rhtml.editor.completion;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.retouche.source.ClasspathInfo;
import org.netbeans.api.retouche.source.Source;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;
import org.openide.filesystems.FileObject;

/**
 * Creates a Ruby model for an RHTML file. Simulates ERB to generate Ruby from
 * the RHTML. This is registered as a CompilationInfo EmbeddingModel, such that
 * clients iterating over the content can compute proper offsets.
 *
 * This class attaches itself to a document, and listens on changes. When
 * a client asks for the Ruby source of the RHTML file, it lazily generates it
 * if the document has been modified.
 *
 * @author Marek Fukala
 * @author Tor Norbye
 */
public class RhtmlModel {
    private final Document doc;
    private final ArrayList<CodeBlockData> codeBlocks = new ArrayList<CodeBlockData>();
    private String rubyCode;
    //private String rhtmlCode; // For debugging purposes
    private RhtmlEmbeddingModel embeddingModel;
    private boolean documentDirty = true;
    private FileObject fo;
 
    RhtmlModel(Document doc, FileObject fo, RhtmlEmbeddingModel embeddingModel) {
        this.doc = doc;
        this.fo = fo;
        this.embeddingModel = embeddingModel;

        if (doc != null) { // null in some unit tests
            TokenHierarchy hi = TokenHierarchy.get(doc);
            hi.addTokenHierarchyListener(new TokenHierarchyListener() {
                public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
                    documentDirty = true;
                }
            });
        }
    }
    
    public Source getSource() throws IllegalArgumentException {
        if (doc == null) {
            throw new IllegalArgumentException ("doc == null");  //NOI18N
        }
        @SuppressWarnings("unchecked")
        Reference<Source> ref = (Reference<Source>)doc.getProperty(Source.class);
        Source js = ref != null ? ref.get() : null;
        if (js == null) {
            List<FileObject> fos = Collections.singletonList(fo);
            js = Source.createFromModel(ClasspathInfo.create(fo), fos, embeddingModel);
        }
        return js;
    }

    public String getRubyCode() {
        if (documentDirty) {
            documentDirty = false;
            
            // Debugging
            //try {
            //    rhtmlCode = doc.getText(0, doc.getLength());
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
            codeBlocks.clear();
            StringBuilder buffer = new StringBuilder();
            TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get(doc);
            TokenSequence<RhtmlTokenId> tokenSequence = tokenHierarchy.tokenSequence(RhtmlTokenId.language()); //get top level token sequence

            eruby(buffer, tokenHierarchy, tokenSequence);

            rubyCode = buffer.toString();
        }
        
        return rubyCode;
    }
    
    /** Perform eruby translation 
     * @param outputBuffer The buffer to emit the translation to
     * @param tokenHierarchy The token hierarchy for the RHTML code
     * @param tokenSequence  The token sequence for the RHTML code
     */
    public void eruby(StringBuilder outputBuffer,
            TokenHierarchy<Document> tokenHierarchy,            
            TokenSequence<RhtmlTokenId> tokenSequence) {
        StringBuilder buffer = outputBuffer;
        // Add a super class such that code completion, goto declaration etc.
        // knows where to pull the various link_to etc. methods from
        
        // Pretend that this code is an extension to ActionView::Base such that
        // code completion, go to declaration etc. sees the inherited methods from
        // ActionView -- link_to and friends.
        buffer.append("class ActionView::Base\n"); // NOI18N
        // TODO Try to include the helper class as well as the controller fields too;
        // for now this logic is hardcoded into Ruby's code completion engine (CodeCompleter)

        // Erubis uses _buf; I've seen eruby using something else (_erbout?)
        buffer.append("_buf='';"); // NOI18N
        codeBlocks.add(new CodeBlockData(0, 0, 0, buffer.length()));

        boolean skipNewline = false;
        while(tokenSequence.moveNext()) {
            Token<RhtmlTokenId> token = tokenSequence.token();

            if (token.id() == RhtmlTokenId.HTML){
                int sourceStart = token.offset(tokenHierarchy);
                int sourceEnd = sourceStart + token.length();
                int generatedStart = buffer.length();

                String text = token.text().toString();
                
                // If there is leading whitespace in this token followed by a newline,
                // emit it directly first, then insert my buffer append. Otherwise,
                // insert a semicolon if we're on the same line as the previous output.
                boolean found = false;
                int i = 0;
                for (; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        i++; // include it
                        found = true;
                        break;
                    } else if (!Character.isWhitespace(c)) {
                        break;
                    }
                }

                if (found) {
                    buffer.append(text.substring(0, i));
                    text = text.substring(i);
                } else {
                    buffer.append(';');
                }
                buffer.append("_buf << '"); // NOI18N
                if (skipNewline && text.startsWith("\n")) { // NOI18N
                    text = text.substring(1);
                    sourceEnd--;
                }
                // Escape 's in the document so they don't escape out of the ruby code
                // I don't have to do this on lines that are in comments... But no big harm
                text = text.replace("'", "\\'");
                buffer.append(text);
                buffer.append("'; "); // NOI18N
                int generatedEnd = buffer.length();

                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart, generatedEnd);
                codeBlocks.add(blockData);

                skipNewline = false;
            } else if (token.id() == RhtmlTokenId.RUBY){
                int sourceStart = token.offset(tokenHierarchy);
                int sourceEnd = sourceStart + token.length();
                int generatedStart = buffer.length();

                String text = token.text().toString();
                skipNewline = false;
                if (text.endsWith("-")) { // NOI18N
                    text = text.substring(0, text.length()-1);
                    skipNewline = true;
                }
                // Escape 's in the document so they don't escape out of the ruby code
                // I don't have to do this on lines that are in comments... But no big harm
                text = text.replace("'", "\\'");
                buffer.append(text);

                int generatedEnd = buffer.length();

                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart, generatedEnd);
                codeBlocks.add(blockData);

                skipNewline = false;
            } else if (token.id() == RhtmlTokenId.RUBY_EXPR) {
                buffer.append("_buf << ("); // NOI18N
                int sourceStart = token.offset(tokenHierarchy);
                int sourceEnd = sourceStart + token.length();
                int generatedStart = buffer.length();

                String text = token.text().toString();
                skipNewline = false;
                if (text.endsWith("-")) { // NOI18N
                    text = text.substring(0, text.length()-1);
                    skipNewline = true;
                }
                buffer.append(text);
                int generatedEnd = buffer.length();

                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart, generatedEnd);
                codeBlocks.add(blockData);

                buffer.append(").to_s;"); // NOI18N
            }
        }

        // Close off the class
        // eruby also ends with this statement: _buf.to_s
        String end = "\nend\n"; // NOI18N
        buffer.append(end);
        if (doc != null) {
            codeBlocks.add(new CodeBlockData(doc.getLength(), doc.getLength(), buffer.length()-end.length(), buffer.length()));
        }
    }
    
    public int sourceToGeneratedPos(int sourceOffset){
        // Not checking dirty flag here; sourceToGeneratedPos() should apply
        // to the positions as they were when we generated the ruby code
        
        CodeBlockData codeBlock = getCodeBlockAtSourceOffset(sourceOffset);
        
        if (codeBlock == null){
            return -1; // no embedded java code at the offset
        }
        
        int offsetWithinBlock = sourceOffset - codeBlock.sourceStart;
        int generatedOffset = codeBlock.generatedStart+offsetWithinBlock;
        if (generatedOffset <= codeBlock.generatedEnd) {
            return generatedOffset;
        } else {
            return codeBlock.generatedEnd;
        }
    }
    
    public int generatedToSourcePos(int generatedOffset) {
        // Not checking dirty flag here; generatedToSourcePos() should apply
        // to the positions as they were when we generated the ruby code

        CodeBlockData codeBlock = getCodeBlockAtGeneratedOffset(generatedOffset);
        
        if (codeBlock == null){
            return -1; // no embedded java code at the offset
        }
        
        int offsetWithinBlock = generatedOffset - codeBlock.generatedStart;
        int sourceOffset = codeBlock.sourceStart+offsetWithinBlock;
        if (sourceOffset <= codeBlock.sourceEnd) {
            return sourceOffset;
        } else {
            return codeBlock.sourceEnd;
        }
    }
    
    private CodeBlockData getCodeBlockAtSourceOffset(int offset){
        for (CodeBlockData codeBlock : codeBlocks){
            if (codeBlock.sourceStart <= offset && codeBlock.sourceEnd >= offset){
                return codeBlock;
            }
        }
        return null;
    }

    private CodeBlockData getCodeBlockAtGeneratedOffset(int offset){
        for (CodeBlockData codeBlock : codeBlocks){
            if (codeBlock.generatedStart <= offset && codeBlock.generatedEnd >= offset){
                return codeBlock;
            }
        }
        return null;
    }
    
    private class CodeBlockData {
        /** Start of section in RHTML file */
        private int sourceStart;
        /** End of section in RHTML file */
        private int sourceEnd;
        /** Start of section in generated Ruby */
        private int generatedStart;
        /** End of section in generated Ruby */
        private int generatedEnd;
        
        public CodeBlockData(int sourceStart, int sourceEnd, int generatedStart, int generatedEnd) {
            this.sourceStart = sourceStart;
            this.generatedStart = generatedStart;
            this.sourceEnd = sourceEnd;
            this.generatedEnd = generatedEnd;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("CodeBlockData[");
            sb.append("\n  RHTML(" + sourceStart+","+sourceEnd+")");
            //sb.append("=\"");
            //sb.append(rhtmlCode.substring(sourceStart, sourceEnd));
            //sb.append("\"");
            sb.append(",\n  RUBY(" + generatedStart + "," + generatedEnd + ")");
            //sb.append("=\"");
            //sb.append(rubyCode.substring(generatedStart,generatedEnd));
            //sb.append("\"");
            sb.append("]");
            
            return sb.toString();
        }
    }

    // For debugging only; pass in "rubyCode" or "rhtmlCode" in RhtmlModel to print
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
