/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.syntax;

import java.lang.ref.*;
import java.util.*;
import java.io.*;

import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import org.netbeans.modules.xml.text.syntax.dom.*;
import org.openide.ErrorManager;
import org.openide.util.WeakListeners;

/**
 * Creates higher level syntax elements (DOM nodes) above token chain.
 * <p>
 * It also defines rules for auto code completion poping up (Editor architecture issue).
 *
 * @author  Petr Nejedly - original HTML code
 * @author  Sandeep S. Randhawa - XML port
 * @author  Petr Kuzel - use before strategy, use tokens whenever possible
 * @version 0.8
 */
public class XMLSyntaxSupport extends ExtSyntaxSupport implements XMLTokenIDs {
    
    private Reference reference = new SoftReference(null);  // cached helper
    private String systemId = null;  // cached refernce to DTD
    private String publicId = null;  // cached refernce to DTD
    private volatile boolean requestedAutoCompletion = false;

    /** Holds last character user have typed. */
    private char lastInsertedChar = 'X';  // NOI18N

    private final DocumentMonitor documentMonitor;

    /** Creates new XMLSyntaxSupport */
    public XMLSyntaxSupport(BaseDocument doc) {
        super(doc);

        // listener has same lifetime as this class
        documentMonitor = new DocumentMonitor();
        DocumentListener l = WeakListeners.document(documentMonitor, doc);
        doc.addDocumentListener(l);

    }

    /**
     * Get token at given offet or previous one if at token boundary.
     * It does not lock the document.
     * @param offset valid position in document
     * @return TokenItem or <code>null</code> at the document beginning.
     */
    public TokenItem getPreviousToken( int offset) throws BadLocationException {
        
        if (offset == 0) return null;
        if (offset < 0) throw new BadLocationException("Offset " + offset + " cannot be less than 0.", offset);  //NOI18N
        
        // find first token item at the offset
        
        TokenItem item = null;        
        int step = 11;
        int len = getDocument().getLength();  //??? read lock
        if (offset > len) throw new BadLocationException("Offset " + offset + " cannot be higher that document length " + len + " .", offset );  //NOI18N
        int from = Math.min(len, offset);
        int to = Math.min(len, offset);                

        // go ahead to document beginning
        
        while ( item == null) {
            from = Math.max( from - step, 0);
            if ( from == 0) {
                to = Math.min(to + step, len);
            }
            item = getTokenChain( from, to);
            if ( from == 0 && to == len && item == null) {
                throw new IllegalStateException("Token at " + offset + " cannot be located!\nInspected range:[" + from + ", " + to + "].");  //NOI18N
            }
        }
        
        // if we are are at token boundary or at the fist document tokem all is OK
        // otherwise the offset actually resides in some next token
        
        while (item.getOffset() + item.getImage().length() < offset) {  // it must cross or touch it
            TokenItem next = item.getNext();
            if (next == null) {
                if (item.getOffset() + item.getImage().length() >= len) {
                    return item;  // we are at boundary at the end of document
                } else {
                    throw new IllegalStateException("Token at " + offset + " cannot be located!\nPrevious token: " + item);  //NOI18N
                }
            }
            item = next;
        }

        return item;
    }
    
    /** 
     * Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just before the offset.
     * @param offset Offset in document where to search for SyntaxElement.
     * @return SyntaxElement Element surrounding or laying BEFORE the offset
     * or <code>null</code> at document begining.
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {

        TokenItem item = getPreviousToken( offset);
        if (item == null) return null;
    
        // locate SyntaxElement start boundary by traversing previous tokens
        // then create element starting from that boundary
        
        TokenID id = item.getTokenID();
        TokenItem first = item;
        
        // reference can be in attribute or in content
        
        if( id == CHARACTER ) {
            while( id == CHARACTER ) {                
                item = item.getPrevious();
                if (item == null) break;
                id = item.getTokenID();
                first = item;
            }
            
            // now item is either XMLSyntax.VALUE or we're in text, or at BOF
            if( id != VALUE && id != TEXT && id != CDATA_SECTION ) {

                // #34453 it may start of element tag or end of start tag (skip attributtes)
                if( id == XMLDefaultTokenContext.TAG ) {
                    if( item.getImage().startsWith( "<" ) ) {
                        return createElement( item );  // TAGO/ETAGO
                    } else {
                        do {
                            item = item.getPrevious();
                            id = item.getTokenID();
                        } while( id != XMLDefaultTokenContext.TAG );
                        return createElement( item );       // TAGC
                    }
                }

                return createElement( first );
            } // else ( for VALUE or TEXT ) fall through
        }
        
        // these are possible only in containers (tags or doctype)
        if ( id == XMLDefaultTokenContext.WS 
          || id == XMLDefaultTokenContext.ARGUMENT 
          || id == XMLDefaultTokenContext.OPERATOR
          || id == XMLDefaultTokenContext.VALUE)  // or doctype
        {
            while (true) {
                item = item.getPrevious();
                id = item.getTokenID();
                if (id == XMLDefaultTokenContext.TAG) break;
                if (id == XMLDefaultTokenContext.DECLARATION 
                    && item.getImage().trim().length() > 0) break;
                if (isInPI(id, false)) break;
            };
        }
                
        if( id == TEXT || id == CDATA_SECTION) {
            
            while( id == TEXT || id == CHARACTER || id == CDATA_SECTION) {
                first = item;
                item = item.getPrevious();                
                if (item == null)  break;                
                id = item.getTokenID();                
            }
            return createElement( first ); // from start of continuous text
        }

        //
        // it may start of element tag or end of start tag (skip attributtes)
        //
        if( id == XMLDefaultTokenContext.TAG ) {
            if( item.getImage().startsWith( "<" ) ) {
                return createElement( item );  // TAGO/ETAGO
            } else {
                do {
                    item = item.getPrevious();
                    id = item.getTokenID();
                } while( id != XMLDefaultTokenContext.TAG );
                return createElement( item );       // TAGC
            }
        }
        
        if( id == XMLDefaultTokenContext.ERROR )
            return new SyntaxElement.Error( this, item, getTokenEnd( item ) );
        
        if( id == XMLDefaultTokenContext.BLOCK_COMMENT ) {
            while( id == XMLDefaultTokenContext.BLOCK_COMMENT && !item.getImage().startsWith( "<!--" ) ) { // NOI18N
                first = item;
                item = item.getPrevious();
                id = item.getTokenID();
            }
            return createElement( first ); // from start of Commment
        }
        
        
        if ( id == XMLDefaultTokenContext.DECLARATION ) {
            while(true) { 
                first = item;                
                if (id == XMLDefaultTokenContext.DECLARATION 
                  && item.getImage().startsWith("<!"))                          // NOI18N
                {
                      break;
                }
                item = item.getPrevious();
                if (item == null) break;
                id = item.getTokenID();
            }
            return createElement( first ); 
        }

        // PI detection
        
        if (isInPI(id, false)) {
            do {
                item = item.getPrevious();
                id = item.getTokenID();
            } while (id != XMLDefaultTokenContext.PI_START);
        }
        
        if (id == XMLDefaultTokenContext.PI_START) {
            return createElement(item);
        }
                
        return null;
    }
        
    // return if in PI exluding PI_START and including WSes
    private boolean isInPI(TokenID id, boolean includeWS) {
        return id == XMLDefaultTokenContext.PI_TARGET 
            || id == XMLDefaultTokenContext.PI_CONTENT 
            || id == XMLDefaultTokenContext.PI_END 
            || (includeWS && id == XMLDefaultTokenContext.WS);
    }
    
    /** 
     * Create elements starting with given item. 
     * 
     * @param  item or null if EOD
     * @return SyntaxElement startting at offset, or null, if EoD
     */    
    public SyntaxElement createElement( TokenItem item ) throws BadLocationException {
        
        if( item == null ) return null; // on End of Document

//        System.err.println("Creating element for: "  + item.getTokenID().getName() + " " + item.getImage());
        
        TokenID id = item.getTokenID();
        TokenItem first = item;
        int lastOffset = getTokenEnd( item );
        switch (id.getNumericID()) {
                
            case XMLDefaultTokenContext.BLOCK_COMMENT_ID:
                
                while( id == XMLDefaultTokenContext.BLOCK_COMMENT ) {
                    lastOffset = getTokenEnd( item );
                    item = item.getNext();
                    if( item == null ) break; //EoD
                    id = item.getTokenID();
                }
                return new CommentImpl( this, first, lastOffset );
        
            case XMLDefaultTokenContext.DECLARATION_ID:
                
                // we treat internal DTD as one syntax element
                boolean seekforDTDEnd = false;;
                while( id == XMLDefaultTokenContext.DECLARATION 
                    || id == XMLDefaultTokenContext.VALUE
                    || seekforDTDEnd)
                {                                        
                    lastOffset = getTokenEnd( item );
                    if (seekforDTDEnd) {
                        if (item.getImage().endsWith("]>")) {
                            break;
                        }
                    } else if (id == DECLARATION) {
                        seekforDTDEnd = item.getImage().endsWith("[");
                    }
                    item = item.getNext();
                    if( item == null ) break; //EoD
                    id = item.getTokenID();                    
                }
                return new DocumentTypeImpl( this, first, lastOffset);
        
            case XMLDefaultTokenContext.ERROR_ID:
                
                return new SyntaxElement.Error( this, first, lastOffset);
        
            case TEXT_ID:
            case CHARACTER_ID:
            case CDATA_SECTION_ID:    
                
                while( id == TEXT || id == CHARACTER || id == CDATA_SECTION) {
                    lastOffset = getTokenEnd( item );
                    item = item.getNext();
                    if( item == null ) break; //EoD
                    id = item.getTokenID();
                }
                return new TextImpl( this, first, lastOffset );
        
            case XMLDefaultTokenContext.TAG_ID:
                
                String text = item.getImage();
                if ( text.startsWith( "</" ) ) {                 // endtag      // NOI18N
                    String name = text.substring( 2 );
                    item = item.getNext();
                    id = item == null ? null : item.getTokenID();

                    while( id == XMLDefaultTokenContext.WS ) {
                        lastOffset = getTokenEnd( item );
                        item = item.getNext();
                        id = item == null ? null : item.getTokenID();
                    }

                    if( id == XMLDefaultTokenContext.TAG && item.getImage().equals( ">" ) ) {   // with this tag
                        return new EndTag( this, first, getTokenEnd( item ), name );
                    } else {                                                            // without this tag
                        return new EndTag( this, first, lastOffset, name );
                    }
                } else {                                                                // starttag
                    String name = text.substring( 1 );
                    ArrayList attrs = new ArrayList();

                    // skip attributes
                    
                    item = item.getNext();
                    id = item == null ? null : item.getTokenID();
                    
                    while( id == XMLDefaultTokenContext.WS
                        || id == XMLDefaultTokenContext.ARGUMENT
                        || id == XMLDefaultTokenContext.OPERATOR
                        || id == XMLDefaultTokenContext.VALUE
                        || id == XMLDefaultTokenContext.CHARACTER) 
                    {
                        if ( id == XMLDefaultTokenContext.ARGUMENT ) {
                            attrs.add( item.getImage() );  // remember all attributes
                        }
                        lastOffset = getTokenEnd( item );
                        item = item.getNext();
                        if (item == null) break;
                        id = item.getTokenID();
                    }

                    // empty or start tag handling

                    if( id  == XMLDefaultTokenContext.TAG && (item.getImage().equals( "/>") || item.getImage().equals(">") || item.getImage().equals("?>"))){
                        if(item.getImage().equals("/>"))
                            return new EmptyTag( this, first, getTokenEnd( item ), name, attrs );
                        else if(item.getImage().equals("?>"))
                            return new EmptyTag( this, first, getTokenEnd( item ), name, attrs );
                        else
                            return new StartTag( this, first, getTokenEnd( item ), name, attrs );
                    } else {                                                            // without this tag
                        return new StartTag( this, first, lastOffset, name, attrs );
                    }
                }

            case XMLDefaultTokenContext.PI_START_ID:
                do {
                    lastOffset = getTokenEnd( item );
                    item = item.getNext();
                    if( item == null ) break; //EoD
                    id = item.getTokenID();
                } while( isInPI(id, true));
                return new ProcessingInstructionImpl( this, first, lastOffset);
                
            default:
                // BadLocationException
        }
        
        throw new BadLocationException( "Cannot create SyntaxElement at " + item, item.getOffset() );  //NOI18N
    }
        
    // ~~~~~~~~~~~~~~~~~ utility methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * Locate DOCTYPE from the start of document.
     */
//    public SyntaxElement.Declaration getDeclarationElement(){
//        int offset = 5;
//        SyntaxElement elem = null;
//        
//        try {
//            while(true){  //??? optimalize stop on first element
//                elem = getElementChain(offset);
//                if(elem instanceof SyntaxElement.Declaration || elem == null)
//                    break;
//                offset += elem.getElementLength()+1;
//            }
//        } catch (BadLocationException ble) {
//            org.openide.TopManager.getDefault().notifyException(ble);
//        }
//        return elem != null ? (SyntaxElement.Declaration)elem : null;
//    }


    /**
     * Look for pairing closing tag.
     *
     * @param offset where to start the search
     * @return name of pairing start tag
     */
    public String getEndTag(int offset) throws BadLocationException {
        SyntaxElement elem = getElementChain( offset );
        
        if( elem != null ) {
            elem = elem.getPrevious();  // we need smtg. before our </
        } else {    // End of Document
            if( offset > 0 ) {
                elem = getElementChain( offset-1 );
            } else { // beginning of document too, not much we can do on empty doc
                return "";
            }
        }
        
        int counter = 0;
        for( ; elem != null; elem = elem.getPrevious() ) {
            //EMPTY TAG MUST MUST come before Tagcuz it extends from Tag
            if(elem instanceof EmptyTag)
                continue;
            else if(elem instanceof StartTag )
                counter++;
            else if(elem instanceof EndTag)
                counter--;
            else
                continue;
            
            if(counter == 1 ){
                String name = ((StartTag)elem).getTagName();
                return name;
            }
        }
        return "";
    }
    
    
    /**
     * @param  offset of a child in parent
     * @return all tags used as children of given parent that precedes the offset
     */
    public List getPreviousLevelTags( int offset) throws BadLocationException {
        List result = new ArrayList();
        Stack stack = new Stack();
        Vector children = new Vector();
        
        SyntaxElement elem = getElementChain( offset );
        if( elem != null ) {
            elem = elem.getPrevious();  // we need smtg. before our </
        } else {    // End of Document
            if( offset > 0 ) {
                elem = getElementChain( offset-1 );
            } else { // beginning of document too, not much we can do on empty doc
                return result;
            }
        }
        
        for( ; elem != null; elem = elem.getPrevious() ) {
            if( elem instanceof EndTag )
                stack.push( ((EndTag)elem).getTagName() );
            else if( elem instanceof EmptyTag ) {
                if(stack.size()==0)
                    //here we r a child of level root element so add him
                    children.add(((EmptyTag)elem).getTagName() );
                continue;
            }else if( elem instanceof Tag ) {
                String name = ((Tag)elem).getTagName();
                
                //                if(name.equals(prefix))
                //                    continue;
                
                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes - can close this tag
                    result.add(name);
                    
                    for(int k=children.size();k>0;k--){
                        result.add(children.get(k-1));
                    }
                    
                    return result;
                } else                         // not empty - we match content of stack
                    if( stack.peek().equals( name ) ) { // match - close this branch of document tree
                        
                        if(stack.size()==1)
                            //we need this name to add to our list of tags before the point of insertion
                            //we r at depth 1
                            children.add(name);
                        
                        stack.pop();
                    }
            }
        }
        result.clear();
        return result;
    }

    /**
     * @param  offset of a child in parent
     * @return all tags used as children of given parent that follows the offset
     */    
    public List getFollowingLevelTags(int offset)throws BadLocationException{
        Stack stack = new Stack();
        Vector children = new Vector();
        
        SyntaxElement elem = getElementChain( offset );
        if( elem != null ) {
            elem = elem.getNext();  // we need smtg. before our </
        } else {    // End of Document
            if( offset > 0 ) {
                elem = getElementChain( offset-1 );
            } else { // beginning of document too, not much we can do on empty doc
                return new ArrayList();
            }
        }
        
        for( ; elem != null; elem = elem.getNext() ) {
            if( elem instanceof EmptyTag ) {
                if(stack.size()==0)
                    //here we r a child of level root element so add him
                    children.add(((EmptyTag)elem).getTagName() );
                continue;
            }else if( elem instanceof Tag ) {
                stack.push( ((Tag)elem).getTagName() );
            }else if( elem instanceof EndTag ){
                String name = ((EndTag)elem).getTagName();
                
                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes and can return the children now
                    return children;
                } else if( stack.peek().equals( name ) ) { // not empty - we match content of stack
                    // match - close this branch of document tree
                    if(stack.size()==1)
                        //we need this name to add to our list of tags before the point of insertion
                        //we r at depth 1
                        children.add(name);
                    
                    stack.pop();
                }
            }
        }
        children.clear();
        return children;
    }

    
    /**
     * Defines <b>auto-completion</b> popup trigering criteria.
     * @param typedText single last typed char
     *
     */
    public int checkCompletion(JTextComponent target, String typedText, boolean visible ) {

        requestedAutoCompletion = false;

        if( !visible ) {
            int retVal = COMPLETION_CANCEL;
            switch( typedText.charAt( typedText.length()-1 ) ) {
                case '/':
                    int dotPos = target.getCaret().getDot();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    if (dotPos >= 2) { // last char before inserted slash
                        try {
                            String txtBeforeSpace = doc.getText(dotPos-2, 2);
                            if( txtBeforeSpace.equals("</") )  // NOI18N
                                retVal = COMPLETION_POPUP;
                        } catch (BadLocationException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                    break;
                    
                case '<':
                case '&':
                case '"':
                case '\'':
                    retVal = COMPLETION_POPUP;
                    break;
             }
            if (retVal == COMPLETION_POPUP) requestedAutoCompletion = true;
            return retVal;
        } else { // the pane is already visible
            switch (typedText.charAt(0)) {
                case '>':
                case ';':
                    return COMPLETION_HIDE;
            }
            //requestedAutoCompletion = true;
            return COMPLETION_POST_REFRESH; //requery it
        }
    }

    /**
     * Return true is this syntax requested auto completion.
     * XMLCompletionQuery can utilize it to not show needless 'No suggestion.'.
     */
    public boolean requestedAutoCompletion() {
        return requestedAutoCompletion;
    }

    /**
     * @return end offset of given item
     */
    static int getTokenEnd( TokenItem item ) {
        return item.getOffset() + item.getImage().length();
    }

    /** Returns last inserted character. It's most likely one recently typed by user. */
    public final char lastTypedChar() {
        return lastInsertedChar;
    }

    /** Keep track of last typed character */
    private class DocumentMonitor implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
        }

        public void insertUpdate(DocumentEvent e) {
            int start = e.getOffset();
            int len = e.getLength();
            try {
                String s = e.getDocument().getText(start + len - 1, 1);
                lastInsertedChar = s.charAt(0);
            } catch (BadLocationException e1) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e1);
            }
        }

        public void removeUpdate(DocumentEvent e) {
        }
    }
}

