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

package org.netbeans.modules.xml.text;

import java.util.*;

import java.util.logging.ErrorManager;
import javax.swing.text.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.openide.util.WeakListeners;

/**
 * Creates higher level syntax elements (DOM nodes) above token chain.
 *
 * @author  Samaresh Panda
 */
public class XMLSyntaxSupport {
    
    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";
    /** Holds last character user have typed. */
    private char lastInsertedChar = 'X';  // NOI18N
    private final DocumentMonitor documentMonitor;
    private BaseDocument document;
    private static WeakHashMap<BaseDocument, XMLSyntaxSupport> supportMap = null;
    
    /** Creates new XMLSyntaxSupport */
    private XMLSyntaxSupport(BaseDocument doc) {
        // listener has same lifetime as this class
        documentMonitor = new DocumentMonitor();
        DocumentListener l = WeakListeners.document(documentMonitor, doc);
        doc.addDocumentListener(l);
    }

    public static XMLSyntaxSupport getSyntaxSupport(BaseDocument doc) {
        XMLSyntaxSupport support = supportMap.get(doc);
        if(support != null)
            return support;

        support = new XMLSyntaxSupport(doc);
        supportMap.put(doc, support);
        return support;
    }
    
    /**
     * Get token at given offet or previous one if at token boundary.
     * It does not lock the document.
     * @param offset valid position in document
     * @return TokenItem or <code>null</code> at the document beginning.
     */
    public Token getPreviousToken( int offset) throws BadLocationException {
//
//        if (offset == 0) return null;
//        if (offset < 0) throw new BadLocationException("Offset " + offset + " cannot be less than 0.", offset);  //NOI18N
//
//        // find first token item at the offset
//
//        TokenItem item = null;
//        int step = 11;
//        int len = getDocument().getLength();  //??? read lock
//        if (offset > len) throw new BadLocationException("Offset " + offset + " cannot be higher that document length " + len + " .", offset );  //NOI18N
//        int from = Math.min(len, offset);
//        int to = Math.min(len, offset);
//
//        // go ahead to document beginning
//
//        while ( item == null) {
//            from = Math.max( from - step, 0);
//            if ( from == 0) {
//                to = Math.min(to + step, len);
//            }
//            item = getTokenChain( from, to);
//            if ( from == 0 && to == len && item == null) {
//                throw new IllegalStateException("Token at " + offset + " cannot be located!\nInspected range:[" + from + ", " + to + "].");  //NOI18N
//            }
//        }
//
//        // if we are are at token boundary or at the fist document tokem all is OK
//        // otherwise the offset actually resides in some next token
//
//        while (item.getOffset() + item.getImage().length() < offset) {  // it must cross or touch it
//            TokenItem next = item.getNext();
//            if (next == null) {
//                if (item.getOffset() + item.getImage().length() >= len) {
//                    return item;  // we are at boundary at the end of document
//                } else {
//                    throw new IllegalStateException("Token at " + offset + " cannot be located!\nPrevious token: " + item);  //NOI18N
//                }
//            }
//            item = next;
//        }
//
//        return item;
        return null;
    }
    
    /**
     * Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just before the offset.
     * @param offset Offset in document where to search for SyntaxElement.
     * @return SyntaxElement Element surrounding or laying BEFORE the offset
     * or <code>null</code> at document begining.
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {
//
//        TokenItem item = getPreviousToken( offset);
//        if (item == null) return null;
//
//        // locate SyntaxElement start boundary by traversing previous tokens
//        // then create element starting from that boundary
//
//        TokenID id = item.getTokenID();
//        TokenItem first = item;
//
//        // reference can be in attribute or in content
//
//        if( id == CHARACTER ) {
//            while( id == CHARACTER ) {
//                item = item.getPrevious();
//                if (item == null) break;
//                id = item.getTokenID();
//                first = item;
//            }
//
//            // #62654 incorrect syntax element create for reference when it is right after a tag ( <atag>&ref;... )
//            if(id == XMLDefaultTokenContext.TAG && item.getImage().endsWith(">")) {
//                return createElement(item.getNext());
//            }
//
//            // now item is either XMLSyntax.VALUE or we're in text, or at BOF
//            if( id != VALUE && id != TEXT && id != CDATA_SECTION ) {
//                // #34453 it may start of element tag or end of start tag (skip attributtes)
//                if( id == XMLDefaultTokenContext.TAG ) {
//                    if( item.getImage().startsWith( "<" ) ) {
//                        return createElement( item );  // TAGO/ETAGO
//                    } else {
//                        do {
//                            item = item.getPrevious();
//                            id = item.getTokenID();
//                        } while( id != XMLDefaultTokenContext.TAG );
//                        return createElement( item );       // TAGC
//                    }
//                }
//                return createElement( first );
//            } // else ( for VALUE or TEXT ) fall through
//
//        }
//
//        // these are possible only in containers (tags or doctype)
//        if ( id == XMLDefaultTokenContext.WS
//                || id == XMLDefaultTokenContext.ARGUMENT
//                || id == XMLDefaultTokenContext.OPERATOR
//                || id == XMLDefaultTokenContext.VALUE)  // or doctype
//        {
//            while (true) {
//                item = item.getPrevious();
//                id = item.getTokenID();
//                if (id == XMLDefaultTokenContext.TAG) break;
//                if (id == XMLDefaultTokenContext.DECLARATION
//                        && item.getImage().trim().length() > 0) break;
//                if (isInPI(id, false)) break;
//            };
//        }
//
//        if( id == TEXT) {
//
//            while( id == TEXT || id == CHARACTER ) {
//                first = item;
//                item = item.getPrevious();
//                if (item == null)  break;
//                id = item.getTokenID();
//            }
//            return createElement( first ); // from start of continuous text
//        }
//
//        if( id == CDATA_SECTION) {
//            //the entire CDATA section is a one big fat token :-)
//            return createElement( item );
//        }
//
//        //
//        // it may start of element tag or end of start tag (skip attributtes)
//        //
//        if( id == XMLDefaultTokenContext.TAG ) {
//            if( item.getImage().startsWith( "<" ) ) {
//                return createElement( item );  // TAGO/ETAGO
//            } else {
//                do {
//                    item = item.getPrevious();
//                    id = item.getTokenID();
//                } while( id != XMLDefaultTokenContext.TAG );
//                return createElement( item );       // TAGC
//            }
//        }
//
//        if( id == XMLDefaultTokenContext.ERROR )
//            return new SyntaxElement.Error( this, item, getTokenEnd( item ) );
//
//        if( id == XMLDefaultTokenContext.BLOCK_COMMENT ) {
//            while( id == XMLDefaultTokenContext.BLOCK_COMMENT && !item.getImage().startsWith( "<!--" ) ) { // NOI18N
//                first = item;
//                item = item.getPrevious();
//                id = item.getTokenID();
//            }
//            return createElement( first ); // from start of Commment
//        }
//
//
//        if ( id == XMLDefaultTokenContext.DECLARATION ) {
//            while(true) {
//                first = item;
//                if (id == XMLDefaultTokenContext.DECLARATION
//                        && item.getImage().startsWith("<!"))                          // NOI18N
//                {
//                    break;
//                }
//                item = item.getPrevious();
//                if (item == null) break;
//                id = item.getTokenID();
//            }
//            return createElement( first );
//        }
//
//        // PI detection
//
//        if (isInPI(id, false)) {
//            do {
//                item = item.getPrevious();
//                id = item.getTokenID();
//            } while (id != XMLDefaultTokenContext.PI_START);
//        }
//
//        if (id == XMLDefaultTokenContext.PI_START) {
//            return createElement(item);
//        }
//
        return null;
    }
    
//    // return if in PI exluding PI_START and including WSes
//    private boolean isInPI(TokenID id, boolean includeWS) {
//        return id == XMLDefaultTokenContext.PI_TARGET
//                || id == XMLDefaultTokenContext.PI_CONTENT
//                || id == XMLDefaultTokenContext.PI_END
//                || (includeWS && id == XMLDefaultTokenContext.WS);
//    }
    
    /**
     * Create elements starting with given item.
     *
     * @param  item or null if EOD
     * @return SyntaxElement startting at offset, or null, if EoD
     */
    private SyntaxElement createElement( Token item ) throws BadLocationException {

//
//        if( item == null ) return null; // on End of Document
//
////        System.err.println("Creating element for: "  + item.getTokenID().getName() + " " + item.getImage());
//
//        TokenID id = item.getTokenID();
//        TokenItem first = item;
//        int lastOffset = getTokenEnd( item );
//        switch (id.getNumericID()) {
//
//            case XMLDefaultTokenContext.BLOCK_COMMENT_ID:
//
//                while( id == XMLDefaultTokenContext.BLOCK_COMMENT ) {
//                    lastOffset = getTokenEnd( item );
//                    item = item.getNext();
//                    if( item == null ) break; //EoD
//                    id = item.getTokenID();
//                }
//                return new CommentImpl( this, first, lastOffset );
//
//            case XMLDefaultTokenContext.DECLARATION_ID:
//
//                // we treat internal DTD as one syntax element
//                boolean seekforDTDEnd = false;;
//                while( id == XMLDefaultTokenContext.DECLARATION
//                        || id == XMLDefaultTokenContext.VALUE
//                        || seekforDTDEnd) {
//                    lastOffset = getTokenEnd( item );
//                    if (seekforDTDEnd) {
//                        if (item.getImage().endsWith("]>")) {
//                            break;
//                        }
//                    } else if (id == DECLARATION) {
//                        seekforDTDEnd = item.getImage().endsWith("[");
//                    }
//                    item = item.getNext();
//                    if( item == null ) break; //EoD
//                    id = item.getTokenID();
//                }
//                return new DocumentTypeImpl( this, first, lastOffset);
//
//            case XMLDefaultTokenContext.ERROR_ID:
//
//                return new SyntaxElement.Error( this, first, lastOffset);
//
//            case TEXT_ID:
//            case CHARACTER_ID:
//
//                while( id == TEXT || id == CHARACTER || id == CDATA_SECTION) {
//                    lastOffset = getTokenEnd( item );
//                    item = item.getNext();
//                    if( item == null ) break; //EoD
//                    id = item.getTokenID();
//                }
//                return new TextImpl( this, first, lastOffset );
//
//            case CDATA_SECTION_ID:
//                return new CDATASectionImpl( this, first, first.getOffset() + first.getImage().length() );
//
//            case XMLDefaultTokenContext.TAG_ID:
//
//                String text = item.getImage();
//                if ( text.startsWith( "</" ) ) {                 // endtag      // NOI18N
//                    String name = text.substring( 2 );
//                    item = item.getNext();
//                    id = item == null ? null : item.getTokenID();
//

//                    while( id == XMLDefaultTokenContext.WS ) {
//                        lastOffset = getTokenEnd( item );
//                        item = item.getNext();
//                        id = item == null ? null : item.getTokenID();
//                    }
//
//                    if( id == XMLDefaultTokenContext.TAG && item.getImage().equals( ">" ) ) {   // with this tag
//                        return new EndTag( this, first, getTokenEnd( item ), name );
//                    } else {                                                            // without this tag
//                        return new EndTag( this, first, lastOffset, name );
//                    }
//                } else {                                                                // starttag
//                    String name = text.substring( 1 );
//                    ArrayList attrs = new ArrayList();
//
//                    // skip attributes
//
//                    item = item.getNext();
//                    id = item == null ? null : item.getTokenID();
//
//                    while( id == XMLDefaultTokenContext.WS
//                            || id == XMLDefaultTokenContext.ARGUMENT
//                            || id == XMLDefaultTokenContext.OPERATOR
//                            || id == XMLDefaultTokenContext.VALUE
//                            || id == XMLDefaultTokenContext.CHARACTER) {
//                        if ( id == XMLDefaultTokenContext.ARGUMENT ) {
//                            attrs.add( item.getImage() );  // remember all attributes
//                        }
//                        lastOffset = getTokenEnd( item );
//                        item = item.getNext();
//                        if (item == null) break;
//                        id = item.getTokenID();
//                    }
//
//                    // empty or start tag handling
//
//                    if( id  == XMLDefaultTokenContext.TAG && (item.getImage().equals( "/>") || item.getImage().equals(">") || item.getImage().equals("?>"))){
//                        if(item.getImage().equals("/>"))
//                            return new EmptyTag( this, first, getTokenEnd( item ), name, attrs );
//                        else if(item.getImage().equals("?>"))
//                            return new EmptyTag( this, first, getTokenEnd( item ), name, attrs );
//                        else
//                            return new StartTag( this, first, getTokenEnd( item ), name, attrs );
//                    } else {                                                            // without this tag
//                        return new StartTag( this, first, lastOffset, name, attrs );
//                    }
//                }
//
//            case XMLDefaultTokenContext.PI_START_ID:
//                do {
//                    lastOffset = getTokenEnd( item );
//                    item = item.getNext();
//                    if( item == null ) break; //EoD
//                    id = item.getTokenID();
//                } while( isInPI(id, true));
//                return new ProcessingInstructionImpl( this, first, lastOffset);
//
//            default:
//                // BadLocationException
//        }
//
//        throw new BadLocationException( "Cannot create SyntaxElement at " + item, item.getOffset() );  //NOI18N
        return null;
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
     * @return end offset of given item
     */
//    static int getTokenEnd( TokenItem item ) {
//        return item.getOffset() + item.getImage().length();
//    }
    
//    /** Returns last inserted character. It's most likely one recently typed by user. */
//    public final char lastTypedChar() {
//        return lastInsertedChar;
//    }
    
//    /** Finds out whether the given tagTokenItem is a part of a singleton tag (e.g. <div style=""/>).
//     * @tagTokenItem a token item whithin a tag
//     * @return true is the token is a part of singleton tag
//     */
//    public boolean isSingletonTag(TokenItem tagTokenItem) {
//        TokenItem ti = tagTokenItem;
//        while(ti != null) {
//            if(ti.getTokenID() == XMLTokenIDs.TAG) {
//                if("/>".equals(ti.getImage())) { // NOI18N
//                    return true;
//                }
//                if(">".equals(ti.getImage())) return false; // NOI18N
//            }
//            //break the loop on TEXT or on another open tag symbol
//            //(just to prevent long loop in case the tag is not closed)
//            if(ti.getTokenID() == XMLTokenIDs.TEXT) break;
//
//
//            ti = ti.getNext();
//        }
//        return false;
//    }
//
    
    
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
//                ErrorManager err = ErrorManager.getDefault();
//                err.notify(e1);
            }
        }
        
        public void removeUpdate(DocumentEvent e) {
        }
    }
}

