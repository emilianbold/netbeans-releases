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

package org.netbeans.modules.xml.text.completion;

import java.util.Enumeration;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.HintContext;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.text.syntax.dom.*;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.openide.util.WeakListeners;
import org.openide.ErrorManager;

/**
 * Helper class used in XMLCompletionQuery and other classes that use grammar
 * Instances of this class must only be constructed and used from the AWT
 * dispatch thread, because this implementation is not reentrant (see ctx field).
 *
 * @author  asgeir@dimonsoftware.com
 */
final class SyntaxQueryHelper {
    
    public final static int COMPLETION_TYPE_UNKNOWN = 0;
    public final static int COMPLETION_TYPE_ATTRIBUTE = 1;
    public final static int COMPLETION_TYPE_VALUE = 2;
    public final static int COMPLETION_TYPE_ELEMENT = 3;
    public final static int COMPLETION_TYPE_ENTITY = 4;
    public final static int COMPLETION_TYPE_NOTATION = 5;
    public final static int COMPLETION_TYPE_DTD = 6;

    /** Currect oken or previous one if at token boundary */
    private TokenItem token = null;
    
    private String preText = "";
    
    private int erase = 0;
    
    private int tunedOffset = 0;
    
    private SyntaxElement element;
    
    private int completionType = 0;
    
    private boolean tokenBoundary;

    private DefaultContext ctx = new DefaultContext();

    /** Creates a new instance of SyntaxQueryHelper */
    public SyntaxQueryHelper(XMLSyntaxSupport sup, int offset) throws BadLocationException, IllegalStateException {
        tunedOffset = offset;
        token = sup.getPreviousToken( tunedOffset);
        if( token != null ) { // inside document
            tokenBoundary = token.getOffset() + token.getImage().length() == tunedOffset;
        } else {
            //??? start of document no choice now, but should be prolog if not followed by it
            throw new BadLocationException("No token found at current position", offset); // NOI18N
        }

        // find out last typed chars that can hint

        int itemOffset = token.getOffset();
        preText = "";
        erase = 0;
        int eraseRight = 0;
        int id = token.getTokenID().getNumericID();

        // determine last typed text, prefix text

        if ( tokenBoundary == false ) {

            preText = token.getImage().substring( 0, tunedOffset - token.getOffset() );
            if ("".equals(preText)) throw new IllegalStateException("Cannot get token prefix at " + tunedOffset);

            // manipulate tunedOffset to delete rest of an old name
            // for cases where it iseasy to locate original name end

            if (sup.lastTypedChar() != '<' && sup.lastTypedChar() != '&') {
                switch (id) {

                    case XMLDefaultTokenContext.TAG_ID:
                    case XMLDefaultTokenContext.CHARACTER_ID:
                    case XMLDefaultTokenContext.ARGUMENT_ID:

                        int i = token.getImage().length();
                        int tail = i - (tunedOffset - itemOffset);
                        tunedOffset += tail;
                        eraseRight = tail;
                        break;
                }
            }
         } else {
           switch (id) {
                case XMLDefaultTokenContext.TEXT_ID:
                case XMLDefaultTokenContext.TAG_ID:
                case XMLDefaultTokenContext.ARGUMENT_ID:
                case XMLDefaultTokenContext.CHARACTER_ID:
                case XMLCompletionQuery.PI_CONTENT_ID:
                    preText = token.getImage();
                    break;                        
            }
         }

        // adjust how much do you want to erase from the preText

        switch (id) {
            case XMLDefaultTokenContext.TAG_ID:
                // do not erase start delimiters
                erase = preText.length() - 1 + eraseRight;
                break;
            case XMLDefaultTokenContext.CHARACTER_ID:
                //entity references
                erase = preText.length() + -1 + eraseRight;
                break;
            case XMLDefaultTokenContext.ARGUMENT_ID:
                erase = preText.length() + eraseRight;
                break;
            case XMLDefaultTokenContext.VALUE_ID:
                erase = preText.length();
                if (erase > 0 && (preText.charAt(0) == '\'' || preText.charAt(0) == '"')) {
                    // Because of attribute values, preText is adjusted in initContext
                    erase--;
                } else
                break;
        }

        element =  sup.getElementChain( tunedOffset);
        
        if (element == null) throw new IllegalStateException("There exists a token therefore a syntax element must exist at " + offset + ", too.");

        // completion request originates from area covered by DOM, 
        if (element instanceof SyntaxNode && ((SyntaxNode)element).getNodeType() != Node.DOCUMENT_TYPE_NODE) {
            completionType = initContext();
        } else {
            // prolog, internal DTD no completition yet
            completionType = COMPLETION_TYPE_DTD;
        }
    }
    
    /**
     * Find out what to complete: attribute, value, element, entity or notation?
     * <p>
     * <pre>
     * Triggering criteria:
     *
     * ELEMENT      TOKEN (,=seq)   PRETEXT         QUERY
     * -------------------------------------------------------------------
     * Text         text            &lt;            element name
     * Text         text            &lt;/           pairing end element
     * StartTag     tag             &lt;prefix      element name
     * StartTag     ws                              attribute name
     * StartTag     attr, operator  =               quoted attribute value
     * StartTag     value           'prefix         attribute value
     * StartTag     tag             >               element value
     * Text         text            &amp;           entity ref name     
     * StartTag     value           &amp;           entity ref name
     * </pre>
     *
     * @return the type of completion which is one of 
     *          COMPLETION_TYPE_UNKNOWN = 0,
     *          COMPLETION_TYPE_ATTRIBUTE = 1,
     *          COMPLETION_TYPE_VALUE = 2,
     *          COMPLETION_TYPE_ELEMENT = 3,
     *          COMPLETION_TYPE_ENTITY = 4,
     *          COMPLETION_TYPE_NOTATION = 5.
     */
    private int initContext() {
        int id = token.getTokenID().getNumericID();
        SyntaxNode syntaxNode = (SyntaxNode)element;

        switch ( id) {
            case XMLDefaultTokenContext.TEXT_ID:
                if ( preText.endsWith("<" )) {
                    ctx.init(syntaxNode, "");
                    return COMPLETION_TYPE_ELEMENT;
                } else if ( preText.startsWith("&")) {
                    ctx.init(syntaxNode, preText.substring(1));
                    return COMPLETION_TYPE_ENTITY;
                } else {
                    //??? join all previous texts? 
                    // No they are DOM nodes.
                    ctx.init(syntaxNode, preText);
                    return COMPLETION_TYPE_VALUE;
                }
//                break;
                
            case XMLDefaultTokenContext.TAG_ID:
                if ( StartTag.class.equals(syntaxNode.getClass()) 
                || EmptyTag.class.equals(syntaxNode.getClass())) {
                    if (preText.equals("")) {  
                        //??? should not occure
                        if (token.getImage().endsWith(">")) {
                            ctx.init(syntaxNode, preText);
                            return COMPLETION_TYPE_VALUE;
                        } else {
                            ctx.init(syntaxNode, preText);
                            return COMPLETION_TYPE_ELEMENT;
                        }
                    } else if (preText.endsWith("/>")) {
                        ctx.init(syntaxNode, "");
                        return COMPLETION_TYPE_VALUE;                        
                    } else if (preText.endsWith(">")) {
                        ctx.init(syntaxNode, "");
                        return COMPLETION_TYPE_VALUE;
                    } else if (preText.startsWith("</")) {
                        //??? replace immediatelly?
                        ctx.init(syntaxNode, preText.substring(2));
                        return COMPLETION_TYPE_ELEMENT;
                    } else if (preText.startsWith("<")) {
                        ctx.init(syntaxNode, preText.substring(1));
                        return COMPLETION_TYPE_ELEMENT;
                    }
                } else if(EndTag.class.equals(syntaxNode.getClass()) && preText.startsWith("</")){
                    //endtag
                    ctx.init(syntaxNode, preText.substring(2));
                    return COMPLETION_TYPE_ELEMENT;
                } else {
                    // pairing tag completion if not at boundary
                    if ("".equals(preText) && token.getImage().endsWith(">")) {
                        ctx.init(syntaxNode, preText);
                        return COMPLETION_TYPE_VALUE;
                    }
                }
                break;
                
            case XMLDefaultTokenContext.VALUE_ID:
                if (preText.endsWith("&")) {
                    ctx.init(syntaxNode, "");
                    return COMPLETION_TYPE_ENTITY;
                } else if ("".equals(preText)) {   //??? improve check to addres inner '"'
                    String image = token.getImage();
                    char ch = image.charAt(image.length()-1);
                    
                    // findout if it is closing '
                    
                    if (ch == '\'' || ch == '"') {
                        
                        if (image.charAt(0) == ch && image.length() > 1) {
                            // we got whole quoted value as single token ("xxx"|)
                            return COMPLETION_TYPE_UNKNOWN;                            
                        }

                        boolean closing = false;
                        TokenItem prev = token.getPrevious();

                        while (prev != null) {
                            int tid = prev.getTokenID().getNumericID();
                            if (tid == XMLDefaultTokenContext.VALUE_ID) {
                                closing = true;
                                break;
                            } else if (tid == XMLDefaultTokenContext.CHARACTER_ID) {
                                prev = prev.getPrevious();
                            } else {
                                break;
                            }
                        }
                        if (closing == false) {
                            ctx.init(syntaxNode, preText);
                            return COMPLETION_TYPE_VALUE;
                        }
                    } else {
                        ctx.init(syntaxNode, preText);
                        return COMPLETION_TYPE_VALUE;                        
                    }
                } else {
                    // This is probably an attribute value
                    // Let's find the matching attribute node and use it to initialize the context
                    NamedNodeMap attrs = syntaxNode.getAttributes();
                    int maxOffsetLessThanCurrent = -1;
                    Node curAttrNode = null;
                    for (int ind = 0; ind < attrs.getLength(); ind++) {
                        AttrImpl attr = (AttrImpl)attrs.item(ind);
                        int attrTokOffset = attr.getFirstToken().getOffset();
                        if (attrTokOffset > maxOffsetLessThanCurrent && attrTokOffset < token.getOffset()) {
                            maxOffsetLessThanCurrent = attrTokOffset;
                            curAttrNode = attr;
                        }
                    }

                    // eliminate "'",'"' delimiters
                    if (preText.length() > 0) {
                        preText = preText.substring(1);
                    }
                    if (curAttrNode != null) {
                        ctx.init(curAttrNode, preText);
                    } else {
                        ctx.init(syntaxNode, preText);
                    }
                    return COMPLETION_TYPE_VALUE;
                }
                break;
                
            case XMLDefaultTokenContext.OPERATOR_ID:
                if ("".equals(preText)) {
                    if ("=".equals(token.getImage())) {
                        ctx.init(syntaxNode, "");
                        return COMPLETION_TYPE_VALUE;
                    }
                }
                break;

            case XMLDefaultTokenContext.WS_ID:
                if ((StartTag.class.equals(syntaxNode.getClass()) 
                 || EmptyTag.class.equals(syntaxNode.getClass())) 
                 && !token.getImage().startsWith("/")) {
                    ctx.init((Element)syntaxNode, ""); // GrammarQuery.v2 takes Element ctx 
                    return COMPLETION_TYPE_ATTRIBUTE;
                } else {
                    // end tag no attributes to complete
                    return COMPLETION_TYPE_UNKNOWN;
                }
//                break;
                
            case XMLDefaultTokenContext.ARGUMENT_ID:
                if (StartTag.class.equals(syntaxNode.getClass()) 
                || EmptyTag.class.equals(syntaxNode.getClass())) {
                    //try to find the current attribute 
                    Tag tag = (Tag)syntaxNode;
                    NamedNodeMap nnm = tag.getAttributes();
                    for(int i = 0; i < nnm.getLength(); i++) {
                        AttrImpl attrNode = (AttrImpl)nnm.item(i);
                        if(attrNode.getFirstToken().getOffset() == token.getOffset()) {
                            ctx.init(attrNode, preText);
                        }
                    }
                    if(!ctx.isInitialized()) {
                        ctx.init((Element)syntaxNode, preText); // GrammarQuery.v2 takes Element ctx
                    } 
                    return COMPLETION_TYPE_ATTRIBUTE;
                }
                break;
                
            case XMLDefaultTokenContext.CHARACTER_ID:  // entity reference
                if (preText.startsWith("&#")) {
                    // character ref, ignore
                    return COMPLETION_TYPE_UNKNOWN;
                } else if (preText.endsWith(";")) {
                        ctx.init(syntaxNode, "");
                        return COMPLETION_TYPE_VALUE;
                } else if (preText.startsWith("&")) {
                    ctx.init(syntaxNode, preText.substring(1));
                    return COMPLETION_TYPE_ENTITY;
                } else if ("".equals(preText)) {
                    if (token.getImage().endsWith(";")) {
                        ctx.init(syntaxNode, preText);
                        return COMPLETION_TYPE_VALUE;
                    }
                }
                break;
                
            default:

        }
        
//        System.err.println("Cannot complete: " + syntaxNode + "\n\t" + token + "\n\t" + preText);
        return COMPLETION_TYPE_UNKNOWN;
    }
    
    public HintContext getContext() {
        if (completionType != COMPLETION_TYPE_UNKNOWN && completionType != COMPLETION_TYPE_DTD) {
            return ctx;
        } else {
            return null;
        }
    }

    /** Current token or previous one if at token boundary. */
    public TokenItem getToken() {
        return token;
    }
    
    public String getPreText() {
        return preText;
    }
    
    public int getEraseCount() {
        return erase;
    }
    
    public int getOffset() {
        return tunedOffset;
    }
    
    public SyntaxElement getSyntaxElement() {
        return element;
    }
    
    public int getCompletionType() {
        return completionType;
    }

    /** token boundary */
    public boolean isBoundary() {
        return tokenBoundary;
    }


}
