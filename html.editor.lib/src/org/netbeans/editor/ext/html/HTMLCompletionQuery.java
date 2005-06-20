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

package org.netbeans.editor.ext.html;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.net.URL;
import java.util.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;

import org.netbeans.editor.*;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.*;
import org.netbeans.editor.ext.CompletionQuery.ResultItem;
import org.netbeans.editor.ext.html.dtd.*;
import org.netbeans.editor.ext.html.javadoc.HelpManager;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 * HTML completion results finder
 *
 * @author Petr Nejedly
 * @version 1.00
 */
public class HTMLCompletionQuery implements CompletionQuery {
    
    private static boolean lowerCase;
    
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
        Class kitClass = Utilities.getKitClass(component);
        if (kitClass != null) {
            lowerCase = SettingsUtil.getBoolean(kitClass,
                    HTMLSettingsNames.COMPLETION_LOWER_CASE,
                    HTMLSettingsDefaults.defaultCompletionLowerCase);
        }
        BaseDocument doc = (BaseDocument)component.getDocument();
        if( doc.getLength() == 0 ) return null; // nothing to examine
        HTMLSyntaxSupport sup = (HTMLSyntaxSupport)support.get(HTMLSyntaxSupport.class);
        if( sup == null ) return null;// No SyntaxSupport for us, no hint for user
        
        DTD dtd = sup.getDTD();
        if( dtd == null ) return null; // We have no knowledge about the structure!
        
        
        try {
            TokenItem item = null;
            TokenItem prev = null;
            // are we inside token or between tokens
            boolean inside = false;
            
            item = sup.getTokenChain( offset, offset+1 );
            
            if( item != null ) { // inside document
                prev = item.getPrevious();
                // this part of the code is smartcase deciding
                if (prev != null){
                    TokenItem prevv = prev;
                    String prevvImage = prevv.getImage();
                    int index = prevvImage.length() - 1;
                    // is in the previous tag a letter?
                    if (prevv != null && sup.isTag(prevv) &&
                            prevv.getTokenID().getNumericID() == HTMLTokenContext.ARGUMENT_ID){
                        while (index > -1 && !Character.isLetter(prevvImage.charAt(index)))
                            index--;
                    } else
                        index = -1;
                    // if not find first tag with a letter
                    while (index == -1 && prevv != null){
                        while (prevv != null
                                && ((!sup.isTag(prevv)
                                && prevv.getTokenID().getNumericID() != HTMLTokenContext.ARGUMENT_ID)
                                || prevv.getImage().trim().equals(">"))){ // NOI18N
                            prevv = prevv.getPrevious();
                        }
                        if (prevv != null){
                            prevvImage = prevv.getImage();
                            index = 0;
                            while (index < prevvImage.length() && !Character.isLetter(prevvImage.charAt(index)))
                                index++;
                            if (index == prevvImage.length()){
                                index = -1;
                                prevv = prevv.getPrevious();
                            }
                        }
                    }
                    // is there a previous tag with a letter?
                    if (prevv != null && index != -1){
                        lowerCase = !Character.isUpperCase(prevvImage.charAt(index));
                    } else{
                        lowerCase = true;
                    }
                    
                }
                // end of smartcase deciding
                inside = item.getOffset() < offset;
            } else {                           // @ end of document
                prev = sup.getTokenChain( offset-1, offset ); //!!!
            }
            boolean begin = (prev == null && !inside);
/*
if( prev == null && !inside ) System.err.println( "Beginning of document, first token = " + item.getTokenID() );
else if( item == null ) System.err.println( "End of document, last token = " + prev.getTokenID() );
else if( ! inside ) System.err.println( "Between tokens " + prev.getTokenID() + " and " + item.getTokenID() );
else System.err.println( "Inside token " + item.getTokenID() );
 */
            
            if( begin ) return null;
            
            TokenID id = null;
            List l = null;
            int len = 1;
            int itemOffset = 0;
            String preText = null;
            if( inside ) {
                id = item.getTokenID();
                preText = item.getImage().substring( 0, offset - item.getOffset() );
                itemOffset = item.getOffset();
            } else {
                id = prev.getTokenID();
                preText = prev.getImage().substring( 0, offset - prev.getOffset() );
                itemOffset = prev.getOffset();
            }
/* Here are completion finders, each have its own set of rules and source of results
 * They are now written just for testing rules, I will rewrite them to more compact
 * and faster, tree form, as soon as i'll have them all.
 */
            
            /* Character reference finder */
            if( (id == HTMLTokenContext.TEXT || id == HTMLTokenContext.VALUE) && preText.endsWith( "&" ) ) { // NOI18N
                l = translateCharRefs( offset-len, len, dtd.getCharRefList( "" ) );
            } else if( id == HTMLTokenContext.CHARACTER ) {
                if( inside || !preText.endsWith( ";" ) ) { // NOI18N
                    len = offset - itemOffset;
                    l = translateCharRefs( offset-len, len, dtd.getCharRefList( preText ) );
                }
                /* Tag finder */
            } else if( id == HTMLTokenContext.TAG_OPEN) { // NOI18N
                len = offset - itemOffset + 1; // minus the < char length
                l = translateTags( itemOffset -1 , len, dtd.getElementList( preText ) );
                
                //test whether there is only one item in the CC list
                if(l.size() == 1) {
                    //test whether the CC is trying to complete an already COMPLETE token - the problematic situation
                    TagItem ti = (TagItem)l.get(0); //there should only one item
                    String itemText = ti.getItemText();
                    //itemText = itemText.substring(1, itemText.length() - 1); //remove the < > from the tag name
                    
                    if(preText.equals(itemText)) {
                        //now I have to look ahead to get know whether
                        //there are some attributes or an end of the tag
                        
                        //define how far to look ahead
                        int lookLenght = 10; //default - thought up
                        if(offset + lookLenght > doc.getLength()) lookLenght = doc.getLength() - offset;
                        
                        TokenItem aheadChainToken = sup.getTokenChain( offset, offset+lookLenght );
                        //test if next token is a whitespace and the next a tag token or an attribute token
                        if(aheadChainToken != null && aheadChainToken.getTokenID().getNumericID() == HTMLTokenContext.WS_ID) {
                            aheadChainToken = aheadChainToken.getNext();
                            if(aheadChainToken != null &&
                                    (aheadChainToken.getTokenID().getNumericID() == HTMLTokenContext.TAG_CLOSE_ID ||
                                    aheadChainToken.getTokenID().getNumericID() == HTMLTokenContext.ARGUMENT_ID )) {
                                //do not put the item into CC - otherwise it will break the completed tag
                                l = null;
                            }
                        }
                    }
                }
                
                
            } else if( /*id == HTMLTokenContext.TEXT && */ preText.endsWith( "<" ) ) { // NOI18N
                // There will be lookup for possible StartTags, in SyntaxSupport
                //                l = translateTags( offset-len, len, sup.getPossibleStartTags ( offset-len, "" ) );
                l = translateTags( offset-len, len, dtd.getElementList( "" ) );
                
                /* EndTag finder */
            } else if( id == HTMLTokenContext.TEXT && preText.endsWith( "</" ) ) { // NOI18N
                len = 2;
                l = sup.getPossibleEndTags( offset, "" );
            } else if( id == HTMLTokenContext.TAG_OPEN_SYMBOL && preText.endsWith( "</" ) ) { // NOI18N
                len = 2;
                l = sup.getPossibleEndTags( offset, "" );
            } else if( id == HTMLTokenContext.TAG_CLOSE) { // NOI18N
                len = offset - itemOffset;
                l = sup.getPossibleEndTags( offset, preText);
                
                /*Argument finder */
            /* TBD: It is possible to have arg just next to quoted value of previous
             * arg, these rules doesn't match start of such arg this case because
             * of need for matching starting quote
             */
            } else if( id == HTMLTokenContext.WS || id == HTMLTokenContext.ARGUMENT ) {
                SyntaxElement elem = null;
                try {
                    elem = sup.getElementChain( offset );
                    // #BUGFIX 25261 At the end of document the element is
                    // automatically null but that does not mean that the
                    // completion should return null. Only if element is null
                    // also for offset-1...
                    // + bugfix of #52909 - the > is recognized as SyntaxElement.TAG so we need to
                    // get a syntax element before, when cc is called before > in a tag e.g. <table w|>
                    if (elem == null || (elem.getType() == SyntaxElement.TYPE_TAG && ">".equals(elem.getText())) ) { // NOI18N
                        elem = sup.getElementChain( offset - 1 );
                    }
                    
                } catch( BadLocationException e ) {
                    return null;
                }
                
                if( elem == null ) return null;
                
                if( elem.getType() == SyntaxElement.TYPE_TAG ) { // not endTags
                    SyntaxElement.Tag tagElem = (SyntaxElement.Tag)elem;
                    
                    String tagName = tagElem.getName().toUpperCase();
                    DTD.Element tag = dtd.getElement( tagName );
                    
                    if( tag == null ) return null; // unknown tag
                    
                    String prefix = (id == HTMLTokenContext.ARGUMENT) ? preText : "";
                    len = prefix.length();
                    List possible = tag.getAttributeList( prefix ); // All attribs of given tag
                    Collection existing = tagElem.getAttributes(); // Attribs already used
                    
                    String wordAtCursor = "";
                    try{
                        wordAtCursor = Utilities.getWord(doc,Utilities.getWordStart(doc,offset));
                    } catch( BadLocationException e ) {
                    }
                    // #BUGFIX 25261 because of being at the end of document the
                    // wordAtCursor must be checked for null to prevent NPE
                    // below
                    if (wordAtCursor == null) {
                        wordAtCursor = "";
                    }
                    
                    l = new ArrayList();
                    for( Iterator i = possible.iterator(); i.hasNext(); ) {
                        DTD.Attribute attr = (DTD.Attribute)i.next();
                        String aName = attr.getName();
                        if( aName.equals( prefix )
                        || (!existing.contains( aName.toUpperCase()) && !existing.contains( aName.toLowerCase()))
                        || wordAtCursor.equals( aName ))
                            l.add( attr );
                    }
                    l = translateAttribs( offset-len, len, l, tag );
                }
                
                /* Value finder */
            /* Suggestion - find special-meaning attributes ( IMG src, A href,
             * color,.... - may be better resolved by attr type, may be moved
             * to propertysheet
             */
            } else if( id == HTMLTokenContext.VALUE || id == HTMLTokenContext.OPERATOR ||
                    id == HTMLTokenContext.WS && (inside ? prev : prev.getPrevious()).getTokenID() == HTMLTokenContext.OPERATOR
                    ) {
                SyntaxElement elem = null;
                try {
                    elem = sup.getElementChain( offset );
                } catch( BadLocationException e ) {
                    return null;
                }
                
                if( elem == null ) return null;
                
                // between Tag and error - common state when entering OOTL, e.g. <BDO dir=>
                if( elem.getType() == SyntaxElement.TYPE_ERROR ) {
                    elem = elem.getPrevious();
                    if( elem == null ) return null;
                }
                if( elem.getType() == SyntaxElement.TYPE_TAG ) {
                    SyntaxElement.Tag tagElem = (SyntaxElement.Tag)elem;
                    
                    String tagName = tagElem.getName().toUpperCase();
                    DTD.Element tag = dtd.getElement( tagName );
                    if( tag == null ) return null; // unknown tag
                    
                    TokenItem argItem = prev;
                    while( argItem != null && argItem.getTokenID() != HTMLTokenContext.ARGUMENT ) argItem = argItem.getPrevious();
                    if( argItem == null ) return null; // no ArgItem
                    String argName = argItem.getImage().toLowerCase();
                    
                    DTD.Attribute arg = tag.getAttribute( argName );
                    if( arg == null || arg.getType() != DTD.Attribute.TYPE_SET ) return null;
                    
                    if( id != HTMLTokenContext.VALUE ) {
                        len = 0;
                        l = translateValues( offset-len, len, arg.getValueList( "" ) );
                    } else {
                        len = offset - itemOffset;
                        
                        String quotationChar = null;
                        if(preText != null && preText.length() > 0) {
                            if(preText.substring(0,1).equals("'")) quotationChar = "'"; // NOI18N
                            if(preText.substring(0,1).equals("\"")) quotationChar = "\""; // NOI18N
                        }
                        
                        l = translateValues( offset-len, len, arg.getValueList( quotationChar == null ? preText : preText.substring(1)) , quotationChar );
                    }
                }
            }
            
            //System.err.println("l = " + l );
            if( l == null ) return null;
            else return new CompletionQuery.DefaultResult( component, "Results for DOCTYPE " + dtd.getIdentifier(), l, offset, len ); // NOI18N
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    List translateCharRefs( int offset, int length, List refs ) {
        List result = new ArrayList( refs.size() );
        String name;
        for( Iterator i = refs.iterator(); i.hasNext(); ) {
            name = ((DTD.CharRef)i.next()).getName();
            result.add( new CharRefItem( name , offset, length, name ));
        }
        return result;
    }
    
    List translateTags( int offset, int length, List tags ) {
        List result = new ArrayList( tags.size() );
        String name;
        for( Iterator i = tags.iterator(); i.hasNext(); ) {
            name = ((DTD.Element)i.next()).getName();
            result.add( new TagItem( name , offset, length, name ));
        }
        return result;
    }
    
    List translateAttribs( int offset, int length, List attribs, DTD.Element tag ) {
        List result = new ArrayList( attribs.size() );
        String tagName = tag.getName() + "#"; // NOI18N
        for( Iterator i = attribs.iterator(); i.hasNext(); ) {
            DTD.Attribute attrib = (DTD.Attribute)i.next();
            String name = attrib.getName();
            switch( attrib.getType() ) {
                case DTD.Attribute.TYPE_BOOLEAN:
                    result.add( new BooleanAttribItem( name, offset, length, attrib.isRequired(), tagName+name ) );
                    break;
                case DTD.Attribute.TYPE_SET:
                    result.add( new SetAttribItem( name, offset, length, attrib.isRequired(), tagName+name ) );
                    break;
                case DTD.Attribute.TYPE_BASE:
                    result.add( new PlainAttribItem( name, offset, length, attrib.isRequired(), tagName+name ) );
                    break;
            }
        }
        return result;
    }
    
    List translateValues( int offset, int length, List values ) {
        return translateValues(offset, length, values, null);
    }
    
    List translateValues( int offset, int length, List values, String quotationChar ) {
        if( values == null ) return new ArrayList( 0 );
        List result = new ArrayList( values.size() );
        for( Iterator i = values.iterator(); i.hasNext(); ) {
            result.add( new ValueItem(((DTD.Value)i.next()).getName(), offset, length, quotationChar ));
        }
        return result;
    }
    
    
    // Implementation of ResultItems for completion
    /** The simple result item operating over an instance of the string,
     * it is lightweight in the mean it doesn't allocate any new instances
     * of anything and every data creates lazily on request to avoid
     * creation of lot of string instances per completion result.
     */
    public static abstract class HTMLResultItem implements CompletionQuery.ResultItem,
    CompletionItem {
        
        /** The String on which is this ResultItem defined */
        String baseText;
        /** the remove and insert point for this item */
        int offset;
        /** The length of the text to be removed */
        int length;
        
        String helpID;
        
        private HTMLCompletionResultItemPaintComponent component;
        
        private static final int HTML_ITEMS_SORT_PRIORITY = 20;
        
        public HTMLResultItem( String baseText, int offset, int length ) {
            this.baseText = lowerCase ? baseText.toLowerCase() : baseText.toUpperCase();
            this.offset = offset;
            this.length = length;
            this. helpID = null;
        }
        
        public HTMLResultItem( String baseText, int offset, int length, String helpID ) {
            this(baseText, offset, length);
            this.helpID = helpID;
        }
        
        //-------------
        protected int selectionStartOffset = -1;
        protected int selectionEndOffset = -1;
        
        public int getSortPriority() {
            return HTML_ITEMS_SORT_PRIORITY;
        }
        public CharSequence getSortText() {
            return HTMLResultItem.this.getItemText();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            //TODO: the paint component should be caches somehow
            HTMLCompletionResultItemPaintComponent component = new HTMLCompletionResultItemPaintComponent.StringPaintComponent(getPaintColor());
            component.setSelected(isSelected);
            component.setString(getItemText());
            return component;
        }
        
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            Component renderComponent = getPaintComponent(false);
            return renderComponent.getPreferredSize().width;
        }
        
        public void render(Graphics g, Font defaultFont, Color defaultColor,
        Color backgroundColor, int width, int height, boolean selected) {
            Component renderComponent = getPaintComponent(selected);
            renderComponent.setFont(defaultFont);
            renderComponent.setForeground(defaultColor);
            renderComponent.setBackground(backgroundColor);
            renderComponent.setBounds(0, 0, width, height);
            ((HTMLCompletionResultItemPaintComponent)renderComponent).paintComponent(g);
        }
        
        protected Object getAssociatedObject() {
            return getItemText();
        }
        
        public static final String COMPLETION_SUBSTITUTE_TEXT= "completion-substitute-text"; //NOI18N
        
        static int substituteOffset = -1;
        
        public int getSubstituteOffset() {
            return substituteOffset;
        }
        
        public boolean instantSubstitution(JTextComponent c) {
            defaultAction(c);
            return true;
        }
        
        public CompletionTask createDocumentationTask() {
            return new AsyncCompletionTask(new DocQuery(this));
        }
        
        public CompletionTask createToolTipTask() {
            return null;
        }
        
        public int getImportance() {
            return 0;
        }
        
        public void processKeyEvent(KeyEvent evt) {
        }
        
        public void defaultAction(JTextComponent component) {
            int substOffset = getSubstituteOffset();
            if (substOffset == -1)
                substOffset = component.getCaretPosition();
            //ResultItem.toAdd = e.getActionCommand();
            Completion.get().hideAll();
            substituteText(component, substOffset, component.getCaretPosition() - substOffset, false);
        }
        
        boolean replaceText( JTextComponent component, String text ) {
            BaseDocument doc = (BaseDocument)component.getDocument();
            doc.atomicLock();
            try {
                doc.remove( offset, length );
                doc.insertString( offset, text, null);
            } catch( BadLocationException exc ) {
                return false;    //not sucessfull
            } finally {
                doc.atomicUnlock();
            }
            return true;
        }
        
        public boolean substituteCommonText( JTextComponent c, int a, int b, int subLen ) {
            return replaceText( c, getItemText().substring( 0, subLen ) );
        }
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            return replaceText( c, getItemText() );
        }
        
        /** @return Properly colored JLabel with text gotten from <CODE>getPaintText()</CODE>. */
        public Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus) {
            Component ret = getPaintComponent(isSelected);
            if (ret==null) return null;
            if (isSelected) {
                ret.setBackground(list.getSelectionBackground());
                ret.setForeground(list.getSelectionForeground());
            } else {
                ret.setBackground(list.getBackground());
                ret.setForeground(list.getForeground());
            }
            ret.getAccessibleContext().setAccessibleName(getItemText());
            ret.getAccessibleContext().setAccessibleDescription(getItemText());
            return ret;
        }
        
        /** The string used in painting by <CODE>getPaintComponent()</CODE>.
         * It defaults to delegate to <CODE>getItemText()</CODE>.
         * @return The String to be painted in Completion View.
         */
        String getPaintText() { return getItemText(); }
        
        abstract Color getPaintColor();
        
        /** @return The String used for looking up the common part of multiple
         * items and for default way of replacing the text */
        public String getItemText() { return baseText; }
        
        public String getHelpID() { return helpID; }
    }
    
    static class EndTagItem extends HTMLResultItem {
        
        public EndTagItem( String baseText, int offset, int length ) {
            super( baseText, offset, length );
        }
        
        Color getPaintColor() { return Color.blue; }
        
        public String getItemText() { return "</" + baseText + ">"; } // NOI18N
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            return super.substituteText( c, a, b, shift );
        }
    }
    
    private static class CharRefItem extends HTMLResultItem {
        
        public CharRefItem( String name, int offset, int length ) {
            super( name, offset, length );
            this.baseText = name;
        }
        
        public CharRefItem( String name, int offset, int length, String helpID ) {
            super( name, offset, length, helpID );
            this.baseText = name;
        }
        
        Color getPaintColor() { return Color.red.darker(); }
        
        public String getItemText() { return "&" + baseText + ";"; } // NOI18N
    }
    
    private static class TagItem extends HTMLResultItem {
        
        public TagItem( String name, int offset, int length ) {
            super( name, offset, length );
        }
        
        public TagItem( String name, int offset, int length, String helpID ) {
            super( name, offset, length, helpID );
        }
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            replaceText( c, "<" + baseText + (shift ? " >" : ">") ); // NOI18N
            if( shift ) {
                Caret caret = c.getCaret();
                caret.setDot( caret.getDot() - 1 );
            }
            return !shift; // flag == false;
        }
        
        Color getPaintColor() { return Color.blue; }
        
        public String getItemText() { return "<" + baseText + ">"; } // NOI18N
    }
    
    private  static class SetAttribItem extends HTMLResultItem {
        boolean required;
        
        public SetAttribItem( String name, int offset, int length, boolean required ) {
            super( name, offset, length );
            this.required = required;
        }
        
        public SetAttribItem( String name, int offset, int length, boolean required, String helpID ) {
            super( name, offset, length, helpID );
            this.required = required;
        }
        
        Color getPaintColor() { return required ? Color.red : Color.green.darker(); }
        
        String getPaintText() { return baseText; }
        
        public String getItemText() { return baseText; } //NOI18N
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            replaceText( c, baseText + "=\"\"" ); //NOI18N
            if( shift ) {
                Caret caret = c.getCaret();
                caret.setDot( caret.getDot() - 1 );
            }
            return false; // always refresh
        }
    }
    
    private static class BooleanAttribItem extends HTMLResultItem {
        
        boolean required;
        
        public BooleanAttribItem( String name, int offset, int length, boolean required ) {
            super( name, offset, length );
            this.required = required;
        }
        
        public BooleanAttribItem( String name, int offset, int length, boolean required, String helpID) {
            super( name, offset, length, helpID );
            this.required = required;
        }
        
        Color getPaintColor() { return required ? Color.red : Color.green.darker(); }
        
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            replaceText( c, shift ? baseText + " " : baseText ); // NOI18N
            return false; // always refresh
        }
    }
    
    private static class PlainAttribItem extends HTMLResultItem {
        
        boolean required;
        
        public PlainAttribItem( String name, int offset, int length, boolean required ) {
            super( name, offset, length );
            this.required = required;
        }
        
        public PlainAttribItem( String name, int offset, int length, boolean required, String helpID ) {
            super( name, offset, length, helpID );
            this.required = required;
        }
        
        Color getPaintColor() { return required ? Color.red : Color.green.darker(); }
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            replaceText( c, baseText + "=\"\"" ); //NOI18N
            if( shift ) {
                Caret caret = c.getCaret();
                caret.setDot( caret.getDot() - 1 );
            }
            return false; // always refresh
        }
    }
    
    private static class ValueItem extends HTMLResultItem {
        
        private String quotationChar = null;
        
        public ValueItem( String name, int offset, int length, String quotationChar) {
            this(name, offset, length);
            this.quotationChar = quotationChar;
        }
        
        public ValueItem( String name, int offset, int length ) {
            super( name, offset, length );
        }
        
        Color getPaintColor() { return Color.magenta; }
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            String quotedText = ((quotationChar == null) ? baseText : quotationChar + baseText + quotationChar);
            replaceText( c, shift ? quotedText + " " : quotedText ); // NOI18N
            return !shift;
        }
    }
    
    static class DocQuery extends AsyncCompletionQuery {
        
        private HTMLResultItem item;
        
        DocQuery(HTMLResultItem item) {
            this.item = item;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item != null) {
                resultSet.setDocumentation(new DocItem(item));
            }
            resultSet.finish();
        }
        
    }
    
    static class DocItem implements CompletionDocumentation {
        private HTMLResultItem ri;
        
        public DocItem(HTMLResultItem ri) {
            this.ri = ri;
        }
        
        public String getText() {
            String help = HelpManager.getDefault().getHelp(ri.getHelpID());
            return help;
        }
        
        public URL getURL() {
            return HelpManager.getDefault().getHelpURL(ri.getHelpID());
        }
        
        public CompletionDocumentation resolveLink(String link) {
            //????
            return null;
        }
        
        public Action getGotoSourceAction() {
            return null;
        }
    }
    
}

