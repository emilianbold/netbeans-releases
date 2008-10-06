
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
package org.netbeans.editor.ext.html;

import javax.swing.text.Document;
import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.dtd.InvalidateEvent;
import org.netbeans.editor.ext.html.dtd.InvalidateListener;
import org.netbeans.spi.editor.completion.CompletionItem;


/**
 * Support methods for HTML document syntax analyzes
 *
 * @author Petr Nejedly
 * @author Marek Fukala
 */
public class HTMLSyntaxSupport extends ExtSyntaxSupport implements InvalidateListener {
    private static final String FALLBACK_DOCTYPE =
            "-//W3C//DTD HTML 4.01 Transitional//EN";  // NOI18N
    
    private DTD dtd;
    private String docType;
    
    public static synchronized HTMLSyntaxSupport get(Document doc) {
        HTMLSyntaxSupport sup = (HTMLSyntaxSupport)doc.getProperty(HTMLSyntaxSupport.class);
        if(sup == null) {
            sup = new HTMLSyntaxSupport((BaseDocument)doc);
            doc.putProperty(HTMLSyntaxSupport.class, sup);
        }
        return sup;
    }
    
    /** Creates new HTMLSyntaxSupport */
    public HTMLSyntaxSupport( BaseDocument doc ) {
        super(doc);
    }
    
    /** Reset our cached DTD if no longer valid.
     */
    public void dtdInvalidated(InvalidateEvent evt) {
        if( dtd != null && evt.isInvalidatedIdentifier( docType ) ) {
            dtd = null;
        }
    }
    
    public DTD getDTD() {
        String type = getDocType();
        if( type == null ) type = FALLBACK_DOCTYPE;
        
        if( dtd != null && type == docType ) return dtd;
        
        docType = type;
        dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD( docType, null );
        
        if(dtd == null) {
            //use default for unknown doctypes
            dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD( FALLBACK_DOCTYPE, null );
        }
        return dtd;
    }
    
    protected String getDocType() {
        try {
            SyntaxElement elem = getElementChain( 0 );
            
            if( elem == null ) return null; // empty document
            
            int type = elem.getType();
            
            while( type != SyntaxElement.TYPE_DECLARATION
                    && type != SyntaxElement.TYPE_TAG ) {
                elem = elem.getNext();
                if( elem == null ) break;
                type = elem.getType();
            }
            
            if( type == SyntaxElement.TYPE_DECLARATION )
                return ((SyntaxElement.Declaration)elem).getPublicIdentifier();
            
            return null;
        } catch( BadLocationException e ) {
            return null;
        }
    }
    
    
    
    /** Find matching tags with the current position.
     * @param offset position of the starting tag
     * @param simple whether the search should skip comment and possibly other areas.
     *  This can be useful when the speed is critical, because the simple
     *  search is faster.
     * @return array of integers containing starting and ending position
     *  of the block in the document. Null is returned if there's
     *  no matching block.
     */
    @Override public int[] findMatchingBlock(int offset, boolean simpleSearch)
    throws BadLocationException {
        BaseDocument document = getDocument();
        document.readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(document);
            TokenSequence ts = tokenSequence(hi, offset);
            if(ts == null) {
                //no suitable token sequence found
                return null;
            }
            
            ts.move(offset);
            if(!ts.moveNext() && !ts.movePrevious()) {
                return null; //no token found
            }
            
            Token token = ts.token();
            
            // if the carret is after HTML tag ( after char '>' ), ship inside the tag
            if (token.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                ts.moveIndex(ts.index() - 2);
                if(ts.moveNext()) {
                    token = ts.token();
                }
            }
            
            boolean isInside = false;  // flag, whether the carret is somewhere in a HTML tag
            if (isTagButNotSymbol(token)) {
                isInside = true; // the carret is somewhere in '<htmltag' or '</htmltag'
            } else {
                if(ts.moveNext()) {
                    token = ts.token();
                    if(token.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                        //we are on opening symbol < or </
                        //so go to the next token which should be a TAG
                        //if the token is null or nor TAG there is nothing to match
                        if((token.id() == HTMLTokenId.TAG_CLOSE)
                        || (token.id() == HTMLTokenId.TAG_OPEN)) {
                            isInside = true; // we found a tag
                        } else {
                            return null;
                        }
                    } else {
                        //we are on closing symbol > or />
                        // find out whether the carret is inside an HTML tag
                        //try to find the beginning of the tag.
                        boolean found = false;
                        while(!isTagButNotSymbol(token) && token.id() != HTMLTokenId.TAG_CLOSE_SYMBOL && ts.movePrevious()) {
                            token = ts.token();
                        }
                        
                        if (ts.index() != -1 && isTagButNotSymbol(token)) {
                            isInside = true;
                        }
                    }
                } else {
                    return null; //no token
                }
            }
            
            if (ts.index() != -1 && isTagButNotSymbol(token) && isInside){
                int start; // possition where the matched tag starts
                int end;   // possition where the matched tag ends
                int poss = -1; // how many the same tags is inside the mathed tag
                
                String tag = token.text().toString().toLowerCase().trim();
                //test whether we are in a close tag
                if (token.id() == HTMLTokenId.TAG_CLOSE) {
                    //we are in a close tag
                    do {
                        token = ts.token();
                        if (isTagButNotSymbol(token)) {
                            String tagName = token.text().toString().toLowerCase().trim();
                            if (tagName.equals(tag)
                            && (token.id() == HTMLTokenId.TAG_OPEN)
                            && !isSingletonTag(ts)) {
                                //it's an open tag
                                if (poss == 0){
                                    //get offset of previous token: < or </
                                    ts.movePrevious();
                                    start = ts.token().offset(hi);
                                    ts.moveIndex(ts.index() + 2);
                                    ts.moveNext();
                                    Token tok = ts.token();
                                    end = tok.offset(hi)+ (tok.id() == HTMLTokenId.TAG_CLOSE_SYMBOL ? tok.text().length() : 0);
                                    return new int[] {start, end};
                                } else{
                                    poss--;
                                }
                            } else {
                                //test whether the tag is a close tag for the 'tag' tagname
                                if ((tagName.indexOf(tag) > -1)
                                && !isSingletonTag(ts)) {
                                    poss++;
                                }
                            }
                        }
                    } while(ts.movePrevious());
                    
                } else{
                    //we are in an open tag
                    if (tag.charAt(0) == '>')
                        return null;
                    
                    //We need to find out whether the open tag is a singleton tag or not.
                    //In the first case no matching is needed
                    if(isSingletonTag(ts)) return null;
                    
                    do {
                        token = ts.token();
                        if (isTagButNotSymbol(token)) {
                            String tagName = token.text().toString().toLowerCase().trim();
                            if (tagName.equals(tag)
                            && token.id() == HTMLTokenId.TAG_CLOSE){
                                if (poss == 0) {
                                    //get offset of previous token: < or </
                                    end = token.offset(hi) + token.text().length() + 1;
                                    ts.movePrevious();
                                    start = ts.token().offset(hi);
                                    
                                    do {
                                        token = ts.token();
                                    } while(ts.moveNext() && token.id() != HTMLTokenId.TAG_CLOSE_SYMBOL);
                                    
                                    if (ts.index() != -1) {
                                        end = token.offset(hi)+token.text().length();
                                    }
                                    return new int[] {start, end};
                                } else
                                    poss--;
                            } else{
                                if (tagName.equals(tag)
                                && !isSingletonTag(ts)) {
                                    poss++;
                                }
                            }
                        }
                    } while (ts.moveNext());
                }
            }
            
            ts.move(offset); //reset the token sequence to the original position
            if(!(ts.moveNext() || ts.movePrevious())) {
                return null; //no token
            }
            token = ts.token();
            
            //match html comments
            if(ts.index() != -1 && token.id() == HTMLTokenId.BLOCK_COMMENT) {
                String tokenImage = token.text().toString();
                if(tokenImage.startsWith("<!--") && (offset < (token.offset(hi)) + "<!--".length())) { //NOI18N
                    //start html token - we need to find the end token of the html comment
                    do {
                        token = ts.token();
                        tokenImage = token.text().toString();
                        if((token.id() == HTMLTokenId.BLOCK_COMMENT)) {
                            if(tokenImage.endsWith("-->")) {//NOI18N
                                //found end token
                                int end = token.offset(hi) + tokenImage.length();
                                int start = end - "-->".length(); //NOI18N
                                return new int[] {start, end};
                            }
                        } else break;
                    } while(ts.moveNext());
                }
                
                if(tokenImage.endsWith("-->") && (offset >= (token.offset(hi)) + tokenImage.length() - "-->".length())) { //NOI18N
                    //end html token - we need to find the start token of the html comment
                    do {
                        token = ts.token();
                        if((token.id() == HTMLTokenId.BLOCK_COMMENT)) {
                            if(token.text().toString().startsWith("<!--")) { //NOI18N
                                //found end token
                                int start = token.offset(hi);
                                int end = start + "<!--".length(); //NOI18N
                                return new int[] {start, end};
                            }
                        } else break;
                        
                    } while(ts.movePrevious());
                }
            } //eof match html comments
            
        } finally {
            document.readUnlock();
        }
        return null;
    }
    
    /** Finds out whether the given {@link TokenSequence}'s actual token is a part of a singleton tag (e.g. <div style=""/>).
     * @ts TokenSequence positioned on a token within a tag
     * @return true is the token is a part of singleton tag
     */
    public boolean isSingletonTag(TokenSequence ts) {
        int tsIndex = ts.index(); //backup ts state
        if(tsIndex != -1) { //test if we are on a token
            try {
                do {
                    Token ti = ts.token();
                    if(ti.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                        if("/>".equals(ti.text().toString())) { // NOI18N
                            //it is a singleton tag => do not match
                            return true;
                        }
                        if(">".equals(ti.text().toString())) {
                            break; // NOI18N
                        }
                    }
                    //break the loop on TEXT or on another open tag symbol
                    //(just to prevent long loop in case the tag is not closed)
                    if((ti.id() == HTMLTokenId.TEXT)
                    || (ti.id() == HTMLTokenId.TAG_OPEN_SYMBOL)) {
                        break;
                    }
                } while(ts.moveNext());
            }finally{
                ts.moveIndex(tsIndex); //backup the TokenSequence position
                ts.moveNext();
            }
        } else {
            //ts is rewinded out of tokens
        }
        return false;
    }
    
    /** The way how to get previous SyntaxElement in document. It is not intended
     * for direct usage, and thus is not public. Usually, it is called from
     * SyntaxElement's method getPrevious()
     */
    public SyntaxElement getPreviousElement( int offset ) throws BadLocationException {
        return offset == 0 ? null : getElementChain( offset - 1 );
    }
      
    /** Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just after the offset.
     *
     * @param offset offset in document where to search for SyntaxElement
     * @return SyntaxElement surrounding or laying after the offset
     * or <CODE>null</CODE> if there is no element there (end of document)
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {
        getDocument().readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(getDocument());
            TokenSequence<HTMLTokenId> ts = getJoinedHtmlSequence(getDocument());
            
            if(ts == null) {
                return  null;
            }
            //html token found
            ts.move(offset);
            if(!ts.moveNext() && !ts.movePrevious()) return null; //no token found
            
            Token item = ts.token();
            
            int beginning = ts.offset();
            
            if(beginning > offset) {
                //the offset is not in html content, the next token begins after the offset
                return null;
            }
            
            if( item.id() == HTMLTokenId.CHARACTER ) {
                do {
                    item = ts.token();
                    beginning = ts.offset();
                } while(item.id() == HTMLTokenId.CHARACTER && ts.movePrevious());
                
                // now item is either HTMLSyntax.VALUE or we're in text, or at BOF
                if( item.id() != HTMLTokenId.VALUE && item.id() != HTMLTokenId.TEXT ) {
                    return getNextElement(  beginning );
                } // else ( for VALUE or TEXT ) fall through
            }
            
            if( item.id() == HTMLTokenId.WS || item.id() == HTMLTokenId.ARGUMENT ||     // these are possible only in Tags
                    item.id() == HTMLTokenId.OPERATOR || item.id() == HTMLTokenId.VALUE ) { // so find boundary
                //find beginning of the tag
                while(ts.movePrevious() && !isTagSymbol(item = ts.token())) {
                    //just skip tokens
                };
                return getNextElement(  item.offset(hi) );       // TAGC
            }
            
            if( item.id() == HTMLTokenId.TEXT ) {
                do {
                    beginning = ts.offset();
                } while ( ts.movePrevious() && (ts.token().id() == HTMLTokenId.TEXT || ts.token().id() == HTMLTokenId.CHARACTER));
                
                return getNextElement(  beginning ); // from start of Commment
            }
            
            if( item.id() == HTMLTokenId.SCRIPT) {
                //we have just one big token for script
                return getNextElement(  ts.token().offset(hi));
            }
            
            if( item.id() == HTMLTokenId.STYLE) {
                //we have just one big token for script
                return getNextElement(  ts.token().offset(hi));
            }
            
            
            if( isTag(item)) {
                if( item.id() == HTMLTokenId.TAG_OPEN ||
                        item.id() == HTMLTokenId.TAG_OPEN_SYMBOL)  return getNextElement(  item.offset(hi) );  // TAGO/ETAGO // NOI18N
                else {
                    do {
                        if(!ts.movePrevious()) {
                            return getNextElement( item.offset(hi));
                        }
                        item = ts.token();
                    } while( item.id() != HTMLTokenId.TAG_OPEN_SYMBOL);
                    
                    return getNextElement(  item.offset(hi) );       // TAGC
                }
            }
            
            if( item.id() == HTMLTokenId.ERROR )
                return new SyntaxElement( this, item.offset(hi), getTokenEnd( hi, item ), SyntaxElement.TYPE_ERROR );
            
            if( item.id() == HTMLTokenId.BLOCK_COMMENT ) {
                while( item.id() == HTMLTokenId.BLOCK_COMMENT && !item.text().toString().startsWith( "<!--" ) && ts.movePrevious()) { // NOI18N
                    item = ts.token();
                }
                return getNextElement(  item.offset(hi)); // from start of Commment
            }
            
            if( item.id() == HTMLTokenId.DECLARATION || item.id() == HTMLTokenId.SGML_COMMENT ) {
                while((item.id() != HTMLTokenId.DECLARATION || !item.text().toString().startsWith( "<!" )) && ts.movePrevious()) { // NOI18N
                    item = ts.token();
                }
                return getNextElement(  item.offset(hi) ); // from start of Commment
            }
        } finally {
            getDocument().readUnlock();
        }
        return null;
    }
    
    
    public SyntaxElement getNextElement(int offset) throws javax.swing.text.BadLocationException {
        getDocument().readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(getDocument());
            TokenSequence ts = getJoinedHtmlSequence(getDocument());
            if(ts == null) {
                return  null;
            }
            
            ts.move(offset);
            if (!ts.moveNext())
                return null;
            org.netbeans.api.lexer.Token item = ts.token();
            int lastOffset = getTokenEnd(hi, item);
            
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.BLOCK_COMMENT) {
                do {
                    lastOffset = getTokenEnd(hi, ts.token());
                } while (ts.token().id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.BLOCK_COMMENT &&
                        ts.moveNext());
                return new SyntaxElement(this, offset, lastOffset,
                        SyntaxElement.TYPE_COMMENT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.DECLARATION) {
                java.lang.StringBuffer sb = new java.lang.StringBuffer(item.text());
                
                while (item.id() == HTMLTokenId.DECLARATION || 
                       item.id() == HTMLTokenId.SGML_COMMENT ||
                       item.id() == HTMLTokenId.WS) {
                    lastOffset = getTokenEnd(hi, item);
                    if (!ts.moveNext()) {
                        break;
                    }
                    item = ts.token();
                    if (item.id() == HTMLTokenId.DECLARATION || item.id() == HTMLTokenId.WS) {
                        sb.append(item.text().toString());
                    }
                }
                java.lang.String image = sb.toString();
                
                if (!image.startsWith("<!DOCTYPE"))
                    return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                            offset,
                            lastOffset,
                            null,
                            null,
                            null);
                image = image.substring(9).trim();
                int index = image.indexOf(' ');
                
                if (index < 0)
                    return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                            offset,
                            lastOffset,
                            null,
                            null,
                            null);
                java.lang.String rootElem = image.substring(0, index);
                
                image = image.substring(index).trim();
                if (image.startsWith("PUBLIC")) {
                    image = image.substring(6).trim();
                    sb = new java.lang.StringBuffer(image);
                    java.lang.String pi = getQuotedString(sb);
                    
                    if (pi != null) {
                        java.lang.String si = getQuotedString(sb);
                        
                        return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                                offset,
                                lastOffset,
                                rootElem,
                                pi,
                                si);
                    }
                } else if (image.startsWith("SYSTEM")) {
                    image = image.substring(6).trim();
                    sb = new java.lang.StringBuffer(image);
                    java.lang.String si = getQuotedString(sb);
                    
                    if (si != null) {
                        return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                                offset,
                                lastOffset,
                                rootElem,
                                null,
                                si);
                    }
                }
                return new org.netbeans.editor.ext.html.SyntaxElement.Declaration(this,
                        offset,
                        lastOffset,
                        null,
                        null,
                        null);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.ERROR)
                return new SyntaxElement(this, item.offset(hi), lastOffset,
                        SyntaxElement.TYPE_ERROR);
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TEXT ||
                    item.id() == org.netbeans.api.html.lexer.HTMLTokenId.CHARACTER) {
                do {
                    lastOffset = getTokenEnd(hi, item);
                    item = ts.token();
                } while (ts.moveNext() &&
                        (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TEXT ||
                        item.id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.CHARACTER));
                return new SyntaxElement(this, offset, lastOffset,
                        SyntaxElement.TYPE_TEXT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.SCRIPT) {
                return new SyntaxElement(this, offset, getTokenEnd(hi, item),
                        SyntaxElement.TYPE_SCRIPT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.STYLE) {
                return new SyntaxElement(this, offset, getTokenEnd(hi, item),
                        SyntaxElement.TYPE_STYLE);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE || (item.id() ==
                    org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN_SYMBOL &&
                    item.text().toString().equals("</"))) {
                java.lang.String name = item.text().toString();
                
                if (item.id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN_SYMBOL) {
                    ts.moveNext();
                    name = ts.token().text().toString();
                }
                ts.moveNext();
                item = ts.token();
                do {
                    item = ts.token();
                    lastOffset = getTokenEnd(hi, item);
                } while (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.WS &&
                        ts.moveNext());
                if (item.id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    return new org.netbeans.editor.ext.html.SyntaxElement.Named(this,
                            offset,
                            getTokenEnd(hi,
                            item),
                            SyntaxElement.TYPE_ENDTAG,
                            name);
                } else {
                    return new org.netbeans.editor.ext.html.SyntaxElement.Named(this,
                            offset,
                            lastOffset,
                            SyntaxElement.TYPE_ENDTAG,
                            name);
                }
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN ||
                    (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN_SYMBOL &&
                    !item.text().toString().equals("</"))) {
                java.lang.String name = item.text().toString();
                ArrayList<SyntaxElement.TagAttribute> attrs = new ArrayList<SyntaxElement.TagAttribute>();
                
                if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN_SYMBOL) {
                    ts.moveNext();
                    name = ts.token().text().toString();
                }
                ts.moveNext();
                item = ts.token();
                
                //find tag attributes
                Token attrNameToken = null;
                do {
                    item = ts.token();
                    if (item.id() == HTMLTokenId.ARGUMENT) {
                        //attribute name
                        attrNameToken = item;
                    } else if (item.id() == HTMLTokenId.VALUE && attrNameToken != null) {
                        //found attribute value after attribute name
                        
                        //there may be entity reference inside the attribute value
                        //e.g. onclick="alert('hello&nbsp;world');" which divides the value
                        //into more html tokens
                        StringBuffer value = new StringBuffer();
                        Token t = null;
                        do {
                            t = ts.token();
                            value.append(t.text().toString());
                        } while (ts.moveNext() && (ts.token().id() == HTMLTokenId.VALUE || ts.token().id() == HTMLTokenId.CHARACTER));
                        
                        SyntaxElement.TagAttribute tagAttr =
                                new SyntaxElement.TagAttribute(attrNameToken.text().toString(),
                                value.toString(),
                                attrNameToken.offset(hi),
                                item.offset(hi));
                        attrs.add(tagAttr);
                        attrNameToken = null;
                    }
                    lastOffset = getTokenEnd(hi, item);
                } while ((item.id() == org.netbeans.api.html.lexer.HTMLTokenId.WS ||
                        item.id() == org.netbeans.api.html.lexer.HTMLTokenId.ARGUMENT ||
                        item.id() == org.netbeans.api.html.lexer.HTMLTokenId.OPERATOR ||
                        item.id() == org.netbeans.api.html.lexer.HTMLTokenId.VALUE ||
                        item.id() == org.netbeans.api.html.lexer.HTMLTokenId.CHARACTER) &&
                        ts.moveNext());
                
                if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    return new org.netbeans.editor.ext.html.SyntaxElement.Tag(this,
                            offset,
                            getTokenEnd(hi,
                            item),
                            name,
                            attrs,
                            item.text().toString().equals("/>"));
                } else {
                    return new org.netbeans.editor.ext.html.SyntaxElement.Tag(this,
                            offset,
                            lastOffset,
                            name,
                            attrs);
                }
            }
            
        } finally {
            getDocument().readUnlock();
        }
        return null;
    }
    
    public static boolean isTagSymbol(Token t) {
        return (( t.id() == HTMLTokenId.TAG_OPEN_SYMBOL) ||
                ( t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL));
    }
    
    public static boolean isTag(Token t) {
        return (( t.id() == HTMLTokenId.TAG_OPEN ) ||
                ( t.id() == HTMLTokenId.TAG_CLOSE ) ||
                ( t.id() == HTMLTokenId.TAG_OPEN_SYMBOL) ||
                ( t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL));
    }
    
    public static boolean isTagButNotSymbol(Token t) {
        return (( t.id() == HTMLTokenId.TAG_OPEN) ||
                ( t.id() == HTMLTokenId.TAG_CLOSE));
    }
    
    
    private static int getTokenEnd( TokenHierarchy thi, Token item ) {
        List<Token> parts = item.joinedParts();
        if(parts != null) {
            //non continuos token, take end offset from the last token part
            item = parts.get(parts.size() - 1);
        } 
        return item.offset(thi) + item.text().length();
    }
    
    /**
     * Beware, changes data
     */
    private static String getQuotedString( StringBuffer data ) {
        int startIndex = 0;
        if (data == null || data.length() == 0) return null;
        while( data.charAt( startIndex ) == ' ' ) startIndex++;
        
        char stopMark = data.charAt( startIndex++ );
        if( stopMark == '"' || stopMark == '\'' ) {
            for( int index = startIndex; index < data.length(); index++ )
                if( data.charAt( index ) == stopMark ) {
                    String quoted = data.substring( startIndex, index );
                    data.delete( 0, index + 1 );
                    return quoted;
                }
        }
        
        return null;
    }
    
    private static TokenSequence tokenSequence(TokenHierarchy hi, int offset) {
        TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
        if(ts == null) {
            //HTML language is not top level one
            ts = hi.tokenSequence();
            ts.move(offset);
            if(!ts.moveNext() && !ts.movePrevious()) {
                return null; //no token found
            } else {
                ts = ts.embedded(HTMLTokenId.language());
            }
        }
        return ts;
    }
    
    public List getPossibleEndTags( int offset, String prefix ) throws BadLocationException {
        prefix = prefix.toUpperCase();
        int prefixLen = prefix.length();
        SyntaxElement elem = getElementChain( offset );
        Stack stack = new Stack();
        List result = new ArrayList();
        Set found = new HashSet();
        DTD dtd = getDTD();
        
        if(elem == null) {
            if(offset > 0) {
                elem = getElementChain( offset - 1);
                if(elem == null) return result;
            } else return result;
        }
        
        int itemsCount = 0;
        for( elem= elem.getPrevious() ; elem != null; elem = elem.getPrevious() ) {
            if( elem.getType() == SyntaxElement.TYPE_ENDTAG) { // NOI18N
                String tagName = ((SyntaxElement.Named)elem).getName().trim();
                //HACK: if there is just close tag opening symbol (</) a syntax element of
                //end tag is created for it. Such SE has empty name
                if(tagName.length() == 0) {
                    continue;
                } 
                DTD.Element tag = dtd.getElement( tagName.toUpperCase() );
                if(tag != null) {
                    stack.push( ((SyntaxElement.Named)elem).getName().toUpperCase() );
                } else {
                    stack.push(tagName); //non-html tag, store it with the original case
                }
            } else if(elem.getType() == SyntaxElement.TYPE_TAG) { //now </ and > are returned as SyntaxElement.TAG so I need to filter them  NOI18N
                if(((SyntaxElement.Tag)elem).isEmpty() ) {
                    continue; // ignore empty Tags - they are like start and imediate end
                }
                
                String tagName = ((SyntaxElement.Named)elem).getName();
                DTD.Element tag = dtd.getElement( tagName.toUpperCase() );
                
                if(tag != null) {
                    tagName = tag.getName();
                    
                    if(tag.isEmpty() && tag.hasOptionalEnd()) {
                        continue; //forbidden end tag
                    }
                }
                
                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes - can close this tag
                    if( tagName.startsWith( prefix ) && !found.contains( tagName ) ) {    // add only new items
                        found.add( tagName );
                        if(tag != null) {
                            //html item
                            result.add( new HTMLCompletionQuery.EndTagItem( tagName, offset-2-prefixLen, prefixLen+2, tagName, itemsCount ) );
                        } else {
                            //non html item
                            result.add( new HTMLCompletionQuery.NonHTMLEndTagItem( tagName, offset-2-prefixLen, prefixLen+2, itemsCount ) );
                        }
                    }
                    if( tag != null && !tag.hasOptionalEnd() ) break;  // If this tag have required EndTag, we can't go higher until completing this tag
                } else {                        // not empty - we match content of stack
                    if( stack.peek().equals( tagName ) ) { // match - close this branch of document tree
                        stack.pop();
                    } else if( tag != null && ! tag.hasOptionalEnd() ) break; // we reached error in document structure, give up
                }
            }
        }
        
        return result;
    }
    
    public List getAutocompletedEndTag(int offset) {
        List l = new ArrayList();
        try {
            SyntaxElement elem = getElementChain( offset - 1);
            if(elem != null && elem.getType() == SyntaxElement.TYPE_TAG) {
                String tagName = ((SyntaxElement.Named)elem).getName();
                //check if the tag has required endtag
                Element dtdElem = getDTD().getElement(tagName.toUpperCase());
                if(!((SyntaxElement.Tag)elem).isEmpty() && (dtdElem == null || !dtdElem.isEmpty())) {
                    CompletionItem eti = new HTMLCompletionQuery.AutocompleteEndTagItem(tagName, offset, dtdElem != null);
                    l.add(eti);
                }
            }
        }catch(BadLocationException e) {
            //just ignore
        }
        return l;
    }
    
    public int checkCompletion(JTextComponent target, String typedText, boolean visible ) {
        int retVal = COMPLETION_CANCEL;
        int dotPos = target.getCaret().getDot();
        BaseDocument doc = (BaseDocument)target.getDocument();
        switch( typedText.charAt( typedText.length()-1 ) ) {
            case '/':
                if (dotPos >= 2) { // last char before inserted slash
                    try {
                        String txtBeforeSpace = doc.getText(dotPos-2, 2);
                        if( txtBeforeSpace.equals("</") )  // NOI18N
                            return COMPLETION_POPUP;
                    } catch (BadLocationException e) {}
                }
                break;
            case ' ':
                doc.readLock();
                try {
                    TokenHierarchy hi = TokenHierarchy.get(doc);
                    TokenSequence ts = tokenSequence(hi, dotPos - 1);
                    if(ts == null) {
                        //no suitable token sequence found
                        return COMPLETION_POST_REFRESH;
                    }
                    
                    ts.move(dotPos-1);
                    if(ts.moveNext() || ts.movePrevious()) {
                        if(ts.token().id() == HTMLTokenId.WS) {
                            return COMPLETION_POPUP;
                        }
                    }
                }finally {
                    doc.readUnlock();
                }
                break;
            case '<':
            case '&':
                return COMPLETION_POPUP;
            case ';':
                return COMPLETION_HIDE;
            case '>':
                try {
                    //check if the cursor is behind an open tag
                    SyntaxElement se = getElementChain(dotPos-1);
                    if(se != null && se.getType() == SyntaxElement.TYPE_TAG) {
                        return COMPLETION_POPUP;
                    }
                }catch(BadLocationException e) {
                    //do nothing
                }
                return COMPLETION_HIDE;
                
        }
        return COMPLETION_POST_REFRESH;
        
    }
    
    public static LanguagePath findTopMostHtml(Document doc) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        for(LanguagePath path : (Set<LanguagePath>)th.languagePaths()) {
            if(path.innerLanguage() == HTMLTokenId.language()) { //is this always correct???
                return path;
            }
        }
        return null;
    }
    
    /** returns top most joined html token seuence for the document. */
    public static TokenSequence<HTMLTokenId> getJoinedHtmlSequence(Document doc) {
         LanguagePath path = findTopMostHtml(doc);
         if(path == null) {
             return null;
         }
         
         return getJoinedHtmlSequence(doc, path);
    }
    
    /*
     * supposes html tokens are always joined - just one joined sequence over the document!
     */
    public static TokenSequence<HTMLTokenId> getJoinedHtmlSequence(Document doc, LanguagePath languagePath) {
        //find html token sequence, in joined version if embedded
        TokenHierarchy th = TokenHierarchy.get(doc);
        List<TokenSequence> tslist = th.tokenSequenceList(languagePath, 0, Integer.MAX_VALUE);
        if(tslist.isEmpty()) {
            return  null; //no such sequence
        }
        TokenSequence first = tslist.get(0);
        first.moveStart(); 
        first.moveNext(); //should return true
        
        List<TokenSequence> embedded = th.embeddedTokenSequences(first.offset(), false);
        TokenSequence sequence = null;
        for (TokenSequence ts : embedded) {
            if (ts.language() == HTMLTokenId.language()) {
                if (sequence == null) {
                    //html is top level
                    sequence = ts;
                    break;
                } else {
                    //the sequence is my master language
                    //get joined html sequence from it
                    sequence = sequence.embeddedJoined(HTMLTokenId.language());
                    assert sequence != null;
                    break;
                }
            }
            sequence = ts;
        }
        return sequence;
    }
    
    
    
}
