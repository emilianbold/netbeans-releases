/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.completion;

import java.util.*;
import java.awt.Color;
import java.net.URL;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;

import org.w3c.dom.*;
import org.xml.sax.*;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.text.syntax.dom.*;
import org.netbeans.modules.xml.spi.model.*;

import org.netbeans.modules.xml.core.lib.Convertors;
import javax.swing.Icon;
import org.openide.util.Task;
import org.openide.TopManager;
import org.openide.util.RequestProcessor;

/**
 * Consults grammar and presents list of possible choices
 * in particular document context.
 *
 * @author Petr Nejedly
 * @author Sandeep Randhawa
 * @author Petr Kuzel
 * @version 1.00
 */

class XMLCompletionQuery implements CompletionQuery {

    // the name of a property indentifing cached query
    private static final String DOCUMENT_GRAMMAR_BINDING_PROP = "doc-bind-query";
    
    // shared context instance, - we are always called from AWT thread
    private DefaultContext ctx = new DefaultContext();
    
    // document that is completed
    private BaseDocument doc;
        
    /** Perform the query on the given component. The query usually
     * gets the component's document, the caret position and searches back
     * to examine surrounding context. Then it returns the result.
     * @param component the component to use in this query.
     * @param offset position in the component's document to which the query will
     *   be performed. Usually it's a caret position.
     * @param support syntax-support that will be used during resolving of the query.
     * @return result of the query or null if there's no result.
     */
    public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support) {

        // assert
        
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException("Called from non-AWT thread: " + Thread.currentThread().getName());  //NOI18N
        }
        
        doc = (BaseDocument)component.getDocument();
        XMLSyntaxSupport sup = (XMLSyntaxSupport)support.get(XMLSyntaxSupport.class);
        if( sup == null ) return null;// No SyntaxSupport for us, no hint for user
        
        try {
            TokenItem token = null;            
            boolean boundary = false; // are we inside token or between tokens
            
            token = sup.getPreviousToken( offset);
            if( token != null ) { // inside document
                boundary = token.getOffset() + token.getImage().length() == offset;
            } else {
                return null; //??? start of document no choice now, but should be prolog if not followed by it
            }

            // find out last typed chars that can hint
            
            int itemOffset = token.getOffset();
            String preText = null;
            
            if ( boundary == false ) {
                preText = token.getImage().substring( 0, offset - token.getOffset() );
                if ("".equals(preText)) throw new IllegalStateException("Cannot get token prefix at " + offset);
            } else {
//                System.err.println("Adjusting: " + token + " " + token.getImage());
                if (token.getTokenID().getNumericID() == XMLDefaultTokenContext.TAG_ID) {
                    preText = token.getImage();
                } else {
                    preText = "";
                }
            }

            int erase = preText.length();
            
            SyntaxElement element = sup.getElementChain( offset);
            if (element == null) throw new IllegalStateException("There exists a token therefore a syntax element must exist at " + offset + ", too.");

            //??? completion request doen originate from area covered by DOM, enable text editor specifics branch?
            if (element instanceof SyntaxNode) {
                List list = query((SyntaxNode) element, token, preText);
                if (list != null && list.isEmpty() == false) {
                    String title = org.openide.util.NbBundle.getMessage(XMLCompletionQuery.class, "MSG_result", new Object[] {preText});
                    return new CompletionQuery.DefaultResult( component, title, list, offset - erase, erase );
                } else {
                    return null;  //??? end tag handling
                }
            } else {
                return null;  //??? prolog finder
            }

            
            
/* Here are completion finders, each have its own set of rules and source of results
 * They are now written just for testing rules, I will rewrite them to more compact
 * and faster, tree form, as soon as i'll have them all.
 */
            
            /* Character reference finder */
            
/*            
            if( (id == XMLTokenContext.TEXT 
            || id == XMLTokenContext.VALUE 
            || id == XMLTokenContext.DECLARATION) && (preText.endsWith( "&" ))) {
                l = translateCharRefs(offset-eraseLen, eraseLen, grammarHelper.getEntityList("") );
            } else if( id == XMLTokenContext.CHARACTER || id == XMLTokenContext.DECLARATION) {
                if( inside || !preText.endsWith( ";" ) ) {
                    eraseLen = offset - itemOffset;
                    l = translateCharRefs( offset-eraseLen, eraseLen, grammarHelper.getEntityList( preText.substring(1)));
                }
                
                
                //parameter entity finder
            }else if( (id == XMLTokenContext.TEXT || id == XMLTokenContext.DECLARATION) && (preText.endsWith( "%" ))) {
                l = translateCharRefs(offset-eraseLen, eraseLen, grammarHelper.getParameterEntityList("") );
            } else if( id == XMLTokenContext.CHARACTER || id == XMLTokenContext.DECLARATION) {
                if( inside || !preText.endsWith( ";" ) ) {
                    eraseLen = offset - itemOffset;
                    l = translateCharRefs( offset-eraseLen, eraseLen, grammarHelper.getParameterEntityList( preText.substring(1)));
                }
                
                // Tag finder 
            } else if( id == XMLTokenContext.TEXT && preText.endsWith( "<" ) ||
            id == XMLTokenContext.TAG && preText.startsWith( "<" ) && !preText.startsWith( "</" )){
                
                // There will be lookup for possible StartTags, in SyntaxSupport
                String levelRootTagName = null;
                List children = null;
                boolean fullyValid = false;
                
                if(id == XMLTokenContext.TEXT)
                    preText = "";
                else{
                    eraseLen = offset - itemOffset;
                    preText = preText.substring(1);
                }
                
                children = sup.getPreviousLevelTags(offset);
                
                if(children.isEmpty())
                    return null;
                else
                    levelRootTagName = (String)children.remove(0);
                
                int insertAt = children.size();
                
                if((fullyValid = SettingsUtil.getBoolean(XMLKit.class, XMLCompletion.FULLY_VALID,false))){
                    children.addAll(insertAt,sup.getFollowingLevelTags(offset));
                }
                
                Map possiblesMap = grammarHelper.whatCanGoHere(levelRootTagName, children, insertAt, preText, fullyValid);
                l = translateTags(offset-eraseLen, eraseLen, possiblesMap);
                
                if(l.isEmpty())
                    l.add(new XMLGrammarQuery.EndTagItem(sup.getEndTag(offset,""),offset - eraseLen, eraseLen));
            } else if( id == XMLTokenContext.TEXT && preText.endsWith( "</" ) ) {
                eraseLen = 2;
                l = new ArrayList();
                l.add(new XMLGrammarQuery.EndTagItem(sup.getEndTag(offset, ""), offset - eraseLen, eraseLen));
            } else if( id == XMLTokenContext.TAG && preText.startsWith( "</" ) ) {
                eraseLen = offset - itemOffset;
                l = new ArrayList();
                l.add(new XMLGrammarQuery.EndTagItem(sup.getEndTag(offset, preText.substring(2)), offset-eraseLen, eraseLen));
                
                // Argument finder 
         * TBD: It is possible to have arg just next to quoted value of previous
         * arg, these rules doesn't match start of such arg this case because
         * of need for matching starting quote
         *
            } else if( id == XMLTokenContext.WS || id == XMLTokenContext.ARGUMENT ) {
                SyntaxElement elem = null;
                try {
                    elem = sup.getElementChain( offset );
                } catch( BadLocationException e ) {
                    return null;
                }
                
                if( elem instanceof SyntaxElement.Tag || elem instanceof SyntaxElement.EmptyTag) { // not endTags, uncovers Error and null too
                    
                    String tagName = ((SyntaxElement.Tag)elem).getName();
                    Collection existing = ((SyntaxElement.Tag)elem).getAttributes(); // Attribs already used
                    Map attrMap = grammarHelper.getAttlistFor(tagName, preText, existing); // All attribs of given tag
                    l = translateAttribs( offset-eraseLen, eraseLen, attrMap );
                }
                
                // Value finder
         * Suggestion - find special-meaning attributes ( IMG src, A href,
         * color,.... - may be better resolved by attr type, may be moved
         * to propertysheet
         *
            } else if( id == XMLTokenContext.VALUE || id == XMLTokenContext.OPERATOR ||
            id == XMLTokenContext.WS && (inside ? prev : prev.getPrevious()).getTokenID() == XMLTokenContext.OPERATOR
            ) {
                SyntaxElement elem = null;
                try {
                    elem = sup.getElementChain( offset );
                } catch( BadLocationException e ) {
                    return null;
                }
                
                if( elem instanceof SyntaxElement.Error) elem = elem.getPrevious(); // between Tag and error - common state when entering OOTL, e.g. <BDO dir=>
                String tagName = ((SyntaxElement.Tag)elem).getName();
                
                TokenItem argItem = prev;
                while( argItem != null && argItem.getTokenID() != XMLTokenContext.ARGUMENT ) argItem = argItem.getPrevious();
                
                if( argItem == null ) return null; // no ArgItem
                String argName = argItem.getImage();
                
                if( id != XMLTokenContext.VALUE ) {
                    eraseLen = 0;
                    l = translateValues(offset-eraseLen, eraseLen, grammarHelper.getAttValues(tagName, argName,  "" ) );
                } else {
                    eraseLen = offset - itemOffset;
                    if(preText.startsWith("\""))
                        preText = preText.substring(1);
                    l = translateValues(offset-eraseLen, eraseLen, grammarHelper.getAttValues(tagName, argName,  preText ) );
                }
            }
            //System.err.println("l = " + l );
            if( l == null ) return null;
            //else return new XMLResult( component, l, offset-eraseLen, eraseLen );
            else return new GrammarQuery.DefaultResult( component, "Results:", l, offset - eraseLen, eraseLen );
 */
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return null;
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
     */
    private List query(SyntaxNode element, TokenItem token, String text) {
        int id = token.getTokenID().getNumericID();
        
        switch ( id) {
            case XMLDefaultTokenContext.TEXT_ID:
                if ( text.endsWith("<" )) {
                    ctx.init(element, "");
                    return queryElements();
                } else if ( text.endsWith("</" )) {
                    ctx.init(element, "");
                    return queryElements();  //??? pairing
                } else if ( text.startsWith("&")) {
                    ctx.init(element, text.substring(1));
                    return queryEntities();
                } else {
                    ctx.init(element, text); //??? join all previous texts? no they are dom nodes
                    return queryValues();
                }
//                break;
                
            case XMLDefaultTokenContext.TAG_ID:
                if ( StartTag.class.equals(element.getClass()) 
                || EmptyTag.class.equals(element.getClass())) {
                    if (text.equals("")) {
                        if (token.getImage().endsWith(">")) {
                            ctx.init(element, text);
                            return queryValues();
                        } else {
                            ctx.init(element, text);
                            return queryElements();
                        }
                    } else if (text.endsWith("/>")) {
                        ctx.init(element, "");
                        return queryValues();                        
                    } else if (text.endsWith(">")) {
                        ctx.init(element, "");
                        return queryValues();
                    } else if (text.startsWith("</")) {
                        ctx.init(element, text.substring(2));
                        return queryElements();
                    } else if (text.startsWith("<")) {
                        ctx.init(element, text.substring(1));
                        return queryElements();
                    }
                } else {
                    // end tag, pairing tag completion if not at boundary
                    if ("".equals(text) && token.getImage().endsWith(">")) {
                        ctx.init(element, text);
                        return queryValues();
                    }
                }
                break;
                
            case XMLDefaultTokenContext.VALUE_ID:
                if (text.endsWith("&")) {
                    ctx.init(element, "");
                    return queryEntities();
                } else if ("".equals(text)) {   //??? improve check to addres inner '"'
                    String image = token.getImage();
                    char ch = image.charAt(image.length()-1);
                    
                    // findout if it is closing '
                    
                    if (ch == '\'' || ch == '"') {
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
                            ctx.init(element, text);
                            return queryValues();
                        }
                    } else {
                        ctx.init(element, text);
                        return queryValues();                        
                    }
                } else {
                    ctx.init(element, text);
                    return queryValues();
                }
                break;
                
            case XMLDefaultTokenContext.OPERATOR_ID:
                if ("".equals(text)) {
                    if ("=".equals(token.getImage())) {
                        ctx.init(element, "");
                        return queryValues();
                    }
                }
                break;

            case XMLDefaultTokenContext.WS_ID:
                if (StartTag.class.equals(element.getClass()) 
                || EmptyTag.class.equals(element.getClass())) {
                    ctx.init(element, "");
                    return queryAttributes();
                } else {
                    // end tag no WS and no attributes
                    throw new IllegalArgumentException("No whitespace allowed in end tag!");
                }
//                break;
                
            case XMLDefaultTokenContext.ARGUMENT_ID:
                if (StartTag.class.equals(element.getClass()) 
                || EmptyTag.class.equals(element.getClass())) {
                    ctx.init(element, text);
                    return queryAttributes();
                }
                break;
                
            case XMLDefaultTokenContext.CHARACTER_ID:  // entity reference
                if (text.startsWith("&#")) {
                    // character ref, ignore
                } else if (text.startsWith("&")) {
                    ctx.init(element, text.substring(1));
                    return queryEntities();                    
                } else if ("".equals(text)) {
                    if (token.getImage().endsWith(";")) {
                        ctx.init(element, text);
                        return queryValues();
                    }
                }
                break;
                
            default:
                
        }
        
//        System.err.println("Cannot complete: " + element + "\n\t" + token + "\n\t" + text);
        return null;
    }

    
    // Delegate queriing to performer ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private List queryEntities() {
        NodeList res = getPerformer().queryEntities(ctx.getCurrentPrefix());
        return translateEntityRefs(res);
    }
    
    private List queryElements() {
        NodeList res = getPerformer().queryElements(ctx);
        return translateElements(res);
    }

    private List queryAttributes() {
        NodeList res = getPerformer().queryAttributes(ctx);
        return translateAttributes(res);
    }

    private List queryValues() {
        NodeList res = getPerformer().queryValues(ctx);
        return translateValues(res);
    }
    

    private List queryNotations() {  //!!! to be implemented
        NodeList res = getPerformer().queryNotations(ctx.getCurrentPrefix());
        return null;
    }
    
    // Translate general results to editor ones ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    private List translateEntityRefs(NodeList refs ) {
        int len = refs.getLength();
        List result = new ArrayList( len );
        for( int i = 0; i<len ; i++ ) {
            //!!! result.add( new EntityRefCItem( ((GrammarResult)refs.item(i)).getNodeName()));
        }
        return result;
    }
    
    private List translateElements(NodeList els ) {
        int len = els.getLength();
        List result = new ArrayList(len);
        for( int i = 0; i<len ; i++ ) {
            ElementCItem eci = new ElementCItem( ((GrammarResult)els.item(i)).getNodeName());
            ElementItem ei = new ElementItem(eci);
            result.add( ei );
        }
        return result;
    }
    
    
    private List translateAttributes(NodeList attrs ) {
        int len = attrs.getLength();
        List result = new ArrayList(len);
        for( int i = 0; i<len ; i++ ) {
            AttributeCItem aci = new AttributeCItem( ((GrammarResult)attrs.item(i)).getNodeName());
            AttributeItem ai = new AttributeItem(aci);
            result.add( ai );
        }
        return result;
    }
    
    private List translateValues(NodeList values ) {
        int len = values.getLength();
        List result = new ArrayList(len);
        for( int i = 0; i<len ; i++ ) {
            //!!! result.add( new ValueCItem( ((GrammarResult)values.item(i)).getNodeName()));
        }
        return result;
    }

    /**
     * Obtain reqistered query, cache results in document property <code>PROP_DOCUMENT_QUERY</code>.
     */
    GrammarQuery getPerformer() {

        GrammarDesc desc = (GrammarDesc) doc.getProperty(DOCUMENT_GRAMMAR_BINDING_PROP);

        if (desc == null) {  
            desc = new GrammarDesc();
            doc.putProperty(DOCUMENT_GRAMMAR_BINDING_PROP, desc);
        }
        
        return desc.getGrammar();
    }
    
    //??? should listen on internal DTD at least
    private class GrammarDesc {
        private RequestProcessor.Task fetchingTask;
        private long timestamp;
        private GrammarQuery grammar;
        
        GrammarQuery getGrammar() {
            fetchGrammar();
            if (grammar == null) {
                return EmptyQuery.INSTANCE;
            } else {
                return grammar;
            }
        }
        
        /**
         * Async grammar fetching
         */
        void fetchGrammar() {
            if (fetchingTask != null) return;  // just fetching
            
            if (grammar != null) return;  //??? out of date check needed
            
            timestamp = System.currentTimeMillis();

            RequestProcessor rp = new RequestProcessor("tmp/XML grammar fetching"); //NOI18N
            fetchingTask = rp.create(new Runnable() {  
                public void run() {
                    String status = org.openide.util.NbBundle.getMessage(XMLCompletionQuery.class, "MSG_loading");
                    TopManager.getDefault().setStatusText(status);
        
                    //!!! hardcoded DTD grammar, replace with lookup
                    
                    InputSource in = Convertors.documentToInputSource(doc);
                    grammar = new org.netbeans.modules.xml.text.completion.dtd.DTDParser().parse(in);
                    fetchingTask = null;
                    
                    status = org.openide.util.NbBundle.getMessage(XMLCompletionQuery.class, "MSG_loading_done");
                    TopManager.getDefault().setStatusText(status);
                }
            });
            fetchingTask.schedule(0);
            
            // for fast fetchers return first results immediatelly
            try {
                Thread.sleep(300);
            } catch (Exception ex) {
                // ignore
            }
        }

        
        
    }
    
    // Editor Result Items ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    /**
     * One abstract result item.
     */
    private static abstract class XMLResultItem implements CompletionQuery.ResultItem {
        /** The Component used as a rubberStamp for painting items */
        static javax.swing.JLabel rubberStamp = new javax.swing.JLabel();
        static {
            rubberStamp.setOpaque( true );
        }
                
        protected XCItem item;
        
        public XMLResultItem( XCItem item ) {
            this.item = item;
        }
        
        boolean replaceText( JTextComponent component, String text, int offset, int len) {
            BaseDocument doc = (BaseDocument)component.getDocument();
            doc.atomicLock();
            try {
                doc.remove( offset, len );
                doc.insertString( offset, text, null);
            } catch( BadLocationException exc ) {
                return false;    //not sucessfull
            } finally {
                doc.atomicUnlock();
            }
            return true;
        }
        
        public boolean substituteCommonText( JTextComponent c, int offset, int len, int subLen ) {
            return replaceText( c, item.getReplacementText(-1).substring( 0, subLen ), offset, len );
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            return substituteText(c, offset, len, java.awt.event.InputEvent.SHIFT_MASK);
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, int modifiers ){
            return replaceText(c, item.getReplacementText(modifiers), offset, len);
        }
        
        /** @return Properly colored JLabel with text gotten from <CODE>getPaintText()</CODE>. */
        public java.awt.Component getPaintComponent( javax.swing.JList list, boolean isSelected, boolean cellHasFocus ) {
            // The space is prepended to avoid interpretation as HTML Label
            if (getIcon() != null) rubberStamp.setIcon(getIcon());
            
            rubberStamp.setText( getItemText() );
            if (isSelected) {
                rubberStamp.setBackground(item.selectionBackground);
                rubberStamp.setForeground(item.selectionForeground);
            } else {
                rubberStamp.setBackground(item.background);
                rubberStamp.setForeground(item.foreground);
            }
            return rubberStamp;
        }
        
        protected Icon getIcon(){
            return item.icon;
        }
        
        public java.lang.String getItemText() {
            return item.displayText;
        }
    }
    
    private static class ElementItem extends XMLResultItem {
        
        public ElementItem( ElementCItem item) {
            super( item );
        }
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            return substituteText(c, a, b, java.awt.event.InputEvent.SHIFT_MASK);
        }
        
        public boolean substituteText( JTextComponent c, int a, int b, int modifiers ){
            String replacementText = item.getReplacementText(modifiers);
            replaceText(c, replacementText, a, b);
            Caret caret = c.getCaret();
            
            boolean shift = (modifiers & java.awt.event.InputEvent.SHIFT_MASK) != 0;
            
//            if(shift)
//                caret.setDot( caret.getDot() - (replacementText.length() - replacementText.indexOf('>') + 1));
//            else
//                caret.setDot( caret.getDot() - (replacementText.length() - replacementText.indexOf('>') + 3) );
            return !shift; //???
        }
        
        
    }

    // Customized behaviour for particular subclasses
    
    private  static class AttributeItem extends XMLResultItem {
        
        public AttributeItem( AttributeCItem item) {
            super( item );
        }
        
        public boolean substituteText( JTextComponent c, int offset, int len, boolean shift ) {
            super.substituteText( c, 0, 0, shift );  //???
            return false; // always refresh
        }
    }
    
    private static class AttributeValueItem extends XMLResultItem {
        
        public AttributeValueItem( ValueCItem item) {
            super( item );
        }
    }
    
    private static class EntityRefItem extends XMLResultItem {
        
        public EntityRefItem( EntityRefCItem item ) {
            super( item );
        }
        
    }

    
    //??? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    // should be end tag modleled as Result item?
    // there is always just one choice!
    
    private static class EndTagItem extends XMLResultItem {
        
        public EndTagItem( XCItem item, int offset, int length ) {
            super( item );
        }
        
        public String getItemText(){
            return "</" + super.getItemText() + '>';
        }
    }


    private class EndTagCItem extends XCItem{
        public EndTagCItem(String displayText){
            super(displayText);
        }

        public String getReplacementText(int modifiers) {
            return "</" + displayText + '>';
        }        
    }
    
}
