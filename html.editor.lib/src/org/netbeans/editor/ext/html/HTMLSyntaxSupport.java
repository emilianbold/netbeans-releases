
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
package org.netbeans.editor.ext.html;

import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

import org.netbeans.editor.BaseDocument;

import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.editor.ext.html.dtd.InvalidateEvent;
import org.netbeans.editor.ext.html.dtd.InvalidateListener;
import org.netbeans.editor.ext.html.dtd.InvalidateListener;

import org.openide.ErrorManager;


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
            TokenSequence ts = hi.tokenSequence();
            
            int diff = ts.move(offset);
            if(diff == Integer.MAX_VALUE) return null; //no token found
            
            Token token = ts.token();
            
            // if the carret is after HTML tag ( after char '>' ), ship inside the tag
            if (token.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                if(ts.moveIndex(ts.index() - 2)) {
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
            }
        } else {
            //ts is rewinded out of tokens
        }
        return false;
    }
    
    private final int getTokenEnd( TokenHierarchy thi, Token item ) {
        return item.offset(thi) + item.text().length();
    }
    
    /** Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just after the offset.
     * @param offset offset in document where to search for SyntaxElement
     * @return SyntaxElement surrounding or laying after the offset
     * or <CODE>null</CODE> if there is no element there (end of document)
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {
        BaseDocument document = getDocument();
        document.readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(document);
            TokenSequence ts = hi.tokenSequence();
            
            int diff = ts.move(offset);
            if(diff == Integer.MAX_VALUE) return null; //no token found
            
            Token item = ts.token();
            
            if(ts.language() != HTMLTokenId.language()) {
                //TODO - resolve embedded case
                //now just the case where HTML is top level language is supported
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "HTMLSyntaxSupport.getElementChain() - now just the case where HTML is top level language is supported!!!");
                return null;
            }
            
            int beginning = item.offset(hi);
            
            if( item.id() == HTMLTokenId.CHARACTER ) {
                do {
                    item = ts.token();
                    beginning = item.offset(hi);
                } while(item.id() == HTMLTokenId.CHARACTER && ts.movePrevious());
                
                // now item is either HTMLSyntax.VALUE or we're in text, or at BOF
                if( item.id() != HTMLTokenId.VALUE && item.id() != HTMLTokenId.TEXT ) {
                    return getNextElement( beginning );
                } // else ( for VALUE or TEXT ) fall through
            }
            
            if( item.id() == HTMLTokenId.WS || item.id() == HTMLTokenId.ARGUMENT ||     // these are possible only in Tags
                    item.id() == HTMLTokenId.OPERATOR || item.id() == HTMLTokenId.VALUE ) { // so find boundary
                while(ts.movePrevious() && !isTag(item = ts.token()));
                return getNextElement( item.offset(hi) );       // TAGC
            }
            
            if( item.id() == HTMLTokenId.TEXT ) {
                do {
                    beginning = ts.token().offset(hi);
                } while ( ts.movePrevious() && (ts.token().id() == HTMLTokenId.TEXT || ts.token().id() == HTMLTokenId.CHARACTER));
                
                return getNextElement( beginning ); // from start of Commment
            }
            
            if( item.id() == HTMLTokenId.SCRIPT) {
                //we have just one big token for script
                return getNextElement( ts.token().offset(hi));
            }
            
            
            if( isTag(item)) {
                if( item.id() == HTMLTokenId.TAG_OPEN ||
                        item.id() == HTMLTokenId.TAG_OPEN_SYMBOL)  return getNextElement( item.offset(hi) );  // TAGO/ETAGO // NOI18N
                else {
                    do {
                        if(!ts.movePrevious()) {
                            return getNextElement(item.offset(hi));
                        }
                        item = ts.token();
                    } while( item.id() != HTMLTokenId.TAG_OPEN_SYMBOL);
                    
                    return getNextElement( item.offset(hi) );       // TAGC
                }
            }
            
            if( item.id() == HTMLTokenId.ERROR )
                return new SyntaxElement( this, item.offset(hi), getTokenEnd( hi, item ), SyntaxElement.TYPE_ERROR );
            
            if( item.id() == HTMLTokenId.BLOCK_COMMENT ) {
                while( item.id() == HTMLTokenId.BLOCK_COMMENT && !item.text().toString().startsWith( "<!--" ) && ts.movePrevious()) { // NOI18N
                    item = ts.token();
                }
                return getNextElement( item.offset(hi)); // from start of Commment
            }
            
            if( item.id() == HTMLTokenId.DECLARATION || item.id() == HTMLTokenId.SGML_COMMENT ) {
                while( item.id() != HTMLTokenId.DECLARATION || !item.text().toString().startsWith( "<!" ) && ts.movePrevious()) { // NOI18N
                    item = ts.token();
                }
                return getNextElement( item.offset(hi) ); // from start of Commment
            }
        } finally {
            document.readUnlock();
        }
        return null;
    }
    
    /** The way how to get previous SyntaxElement in document. It is not intended
     * for direct usage, and thus is not public. Usually, it is called from
     * SyntaxElement's method getPrevious()
     */
    SyntaxElement getPreviousElement( int offset ) throws BadLocationException {
        return offset == 0 ? null : getElementChain( offset - 1 );
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
    
    /** Get the next element from given offset. Should only be called from
     * SyntaxElements obtained by getElementChain, or by getElementChain itself.
     * @return SyntaxElement startting at offset, or null, if EoD
     */
    SyntaxElement getNextElement( int offset ) throws BadLocationException {
        BaseDocument document = getDocument();
        document.readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(document);
            TokenSequence ts = hi.tokenSequence();
            
            int diff = ts.move(offset);
            if(diff >= ts.token().length() || diff == Integer.MAX_VALUE) return null; //no token found
            
            Token item = ts.token();
            int lastOffset = getTokenEnd( hi, item );
            
            if( item.id() == HTMLTokenId.BLOCK_COMMENT ) {
                //find block comment end
                do {
                    lastOffset = getTokenEnd( hi, ts.token() );
                } while( ts.token().id() == HTMLTokenId.BLOCK_COMMENT && ts.moveNext() );
                
                return new SyntaxElement( this, offset, lastOffset, SyntaxElement.TYPE_COMMENT );
            }
            
            if( item.id() == HTMLTokenId.DECLARATION ) {
                // Compose whole declaration, leaving out included comments
                StringBuffer sb = new StringBuffer( item.text() );
                
                while(item.id() == HTMLTokenId.DECLARATION || item.id() == HTMLTokenId.SGML_COMMENT ) {
                    lastOffset = getTokenEnd( hi, item );
                    if(!ts.moveNext()) {
                        break;
                    }
                    item = ts.token();
                    if( item.id() == HTMLTokenId.DECLARATION )
                        sb.append( item.text().toString() );
                }
                
                String image = sb.toString();
                
                // not a DOCTYPE declaration
                if( ! image.startsWith( "<!DOCTYPE" ) )     // NOI18N
                    return new SyntaxElement.Declaration( this, offset, lastOffset,
                            null, null, null );
                
                // Cut off the <!DOCTYPE substring and possible ws
                image = image.substring( 9 ).trim();
                
                int index = image.indexOf( ' ' );
                if( index < 0 ) return new SyntaxElement.Declaration(
                        this, offset, lastOffset, null, null, null );
                
                String rootElem = image.substring( 0, index );
                
                image = image.substring( index ).trim();
                
                if( image.startsWith( "PUBLIC" ) ) {    // NOI18N   Public ID
                    image = image.substring( 6 ).trim();
                    sb = new StringBuffer( image );
                    String pi = getQuotedString( sb );
                    if( pi != null ) {
                        String si = getQuotedString( sb );
                        return new SyntaxElement.Declaration(
                                this, offset, lastOffset, rootElem, pi, si );
                    }
                } else if( image.startsWith( "SYSTEM" ) ) { // NOI18N   System ID
                    image = image.substring( 6 ).trim();
                    sb = new StringBuffer( image );
                    String si = getQuotedString( sb );
                    if( si != null ) {
                        return new SyntaxElement.Declaration(
                                this, offset, lastOffset, rootElem, null, si );
                    }
                }
                return new SyntaxElement.Declaration(
                        this, offset, lastOffset, null, null, null );
            }
            
            if( item.id() == HTMLTokenId.ERROR )
                return new SyntaxElement( this, item.offset(hi), lastOffset, SyntaxElement.TYPE_ERROR );
            
            if( item.id() == HTMLTokenId.TEXT || item.id() == HTMLTokenId.CHARACTER ) {
                do {
                    lastOffset = getTokenEnd( hi, item );
                    item = ts.token();
                } while ( ts.moveNext() && (item.id() == HTMLTokenId.TEXT || item.id() == HTMLTokenId.CHARACTER ));
                
                return new SyntaxElement( this, offset, lastOffset, SyntaxElement.TYPE_TEXT );
            }
            
            if( item.id() == HTMLTokenId.SCRIPT) {
                //we have just one big token for script
                return new SyntaxElement( this, offset, getTokenEnd( hi, item), SyntaxElement.TYPE_SCRIPT );
            }
            
            
            if( item.id() == HTMLTokenId.TAG_CLOSE || (item.id() == HTMLTokenId.TAG_OPEN_SYMBOL && 
                    item.text().toString().equals("</"))) { //NOI18N
                // endtag // NOI18N
                String name = item.text().toString();
                
                if(item.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                    ts.moveNext();  //move to the tag name if necessary
                    name = ts.token().text().toString();
                }
                ts.moveNext(); //move inside the tag
                item = ts.token();
                
                do {
                    item = ts.token();
                    lastOffset = getTokenEnd( hi, item );
                } while(item.id() == HTMLTokenId.WS && ts.moveNext() );
                
                if( item.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {   // with this tag // NOI18N
                    return new SyntaxElement.Named( this, offset, getTokenEnd( hi, item ), SyntaxElement.TYPE_ENDTAG, name );
                } else {                                                            // without this tag
                    return new SyntaxElement.Named( this, offset, lastOffset, SyntaxElement.TYPE_ENDTAG, name );
                }
            }
            
            if( item.id() == HTMLTokenId.TAG_OPEN || (item.id() == HTMLTokenId.TAG_OPEN_SYMBOL &&
                    !item.text().toString().equals("</"))) { //NOI18N) 
                // starttag
                String name = item.text().toString();
                ArrayList attrs = new ArrayList();
                
                if(item.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                    ts.moveNext();  //move to the tag name if necessary
                    name = ts.token().text().toString();
                }
                //move inside the tag (after tag name)
                ts.moveNext();
                
                item = ts.token();
                //TODO: be able to create SyntaxElement for tags containing JSP scriptlet or EL
                do {
                    item = ts.token();
                    if( item.id() == HTMLTokenId.ARGUMENT ) attrs.add( item.text().toString() );  // log all attributes
                    lastOffset = getTokenEnd( hi, item );
                } while ((item.id() == HTMLTokenId.WS ||
                        item.id() == HTMLTokenId.ARGUMENT || item.id() == HTMLTokenId.OPERATOR ||
                        item.id() == HTMLTokenId.VALUE || item.id() == HTMLTokenId.CHARACTER ) && ts.moveNext()
                        ); /* !item.getTokenContextPath().contains(HTMLTokenId.contextPath) */
                
                if( item.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {   // with this tag // NOI18N
                    return new SyntaxElement.Tag( this, offset, getTokenEnd( hi, item ), name, attrs, item.text().toString().equals("/>"));
                } else {                                                            // without this tag
                    return new SyntaxElement.Tag( this, offset, lastOffset, name, attrs );
                }
                
            }
        } finally {
            document.readUnlock();
        }
        
        return null;
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
        for( ; elem != null; elem = elem.getPrevious() ) {
            if( elem.getType() == SyntaxElement.TYPE_ENDTAG) { // NOI18N
                DTD.Element tag = dtd.getElement( ((SyntaxElement.Named)elem).getName().toUpperCase() );
                if(tag != null && !tag.isEmpty()) stack.push( ((SyntaxElement.Named)elem).getName().toUpperCase() );
            } else if(elem.getType() == SyntaxElement.TYPE_TAG) { //now </ and > are returned as SyntaxElement.TAG so I need to filter them  NOI18N
                DTD.Element tag = dtd.getElement( ((SyntaxElement.Tag)elem).getName().toUpperCase() );
                
                if( tag == null ) continue; // Unknown tag - ignore
                if( tag.isEmpty() ) continue; // ignore empty Tags - they are like start and imediate end
                
                String name = tag.getName();
                
                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes - can close this tag
                    if( name.startsWith( prefix ) && !found.contains( name ) ) {    // add only new items
                        found.add( name );
                        result.add( new HTMLCompletionQuery.EndTagItem( name, offset-2-prefixLen, prefixLen+2, name, itemsCount ) );
                    }
                    if( ! tag.hasOptionalEnd() ) break;  // If this tag have required EndTag, we can't go higher until completing this tag
                } else {                        // not empty - we match content of stack
                    if( stack.peek().equals( name ) ) { // match - close this branch of document tree
                        stack.pop();
                    } else if( ! tag.hasOptionalEnd() ) break; // we reached error in document structure, give up
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
                if( dtdElem == null || !dtdElem.isEmpty()) {
                    HTMLCompletionQuery.ResultItem eti = new HTMLCompletionQuery.AutocompleteEndTagItem(tagName, offset);
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
                    TokenSequence ts = hi.tokenSequence();
                    
                    int diff = ts.move(dotPos-1);
                    if(diff != Integer.MAX_VALUE) {
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
                HTMLSyntaxSupport sup = (HTMLSyntaxSupport)doc.getSyntaxSupport().get(HTMLSyntaxSupport.class);
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
    
    
}
