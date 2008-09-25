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

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.net.URL;
import java.util.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.dtd.*;
import org.netbeans.editor.ext.html.javadoc.HelpManager;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;

/**
 * HTML completion results finder
 *
 * @author Petr Nejedly
 * @author Marek Fukala
 * @version 1.10
 */
public class HTMLCompletionQuery  {
    
    private static final String SCRIPT_TAG_NAME = "SCRIPT"; //NOI18N
    private static final String STYLE_TAG_NAME = "STYLE"; //NOI18N
    
    private static final String XHTML_PUBLIC_ID = "-//W3C//DTD XHTML 1.0 Strict//EN";
    
    private static boolean lowerCase;
    private static boolean isXHTML = false;
    
    private static final HTMLCompletionQuery DEFAULT = new HTMLCompletionQuery();
    
    public static HTMLCompletionQuery getDefault() {
        return DEFAULT;
    }
    
    /** Perform the query on the given component. The query usually
     * gets the component's document, the caret position and searches back
     * to examine surrounding context. Then it returns the result.
     * @param component the component to use in this query.
     * @param offset position in the component's document to which the query will
     *   be performed. Usually it's a caret position.
     * @param support syntax-support that will be used during resolving of the query.
     * @return result of the query or null if there's no result.
     */
    public List<CompletionItem> query(JTextComponent component, int offset) {
        Class kitClass = Utilities.getKitClass(component);
        BaseDocument doc = (BaseDocument)component.getDocument();
    
        //temporarily disabled functionality since we do not have any UI in preferences to change it.
//        if (kitClass != null) {
//            lowerCase = SettingsUtil.getBoolean(kitClass,
//                    HTMLSettingsNames.COMPLETION_LOWER_CASE,
//                    HTMLSettingsDefaults.defaultCompletionLowerCase);
//        }
        lowerCase = true;
        
        if( doc.getLength() == 0 ) return null; // nothing to examine
        HTMLSyntaxSupport sup = HTMLSyntaxSupport.get(doc);
        
        if( sup == null ) return null;// No SyntaxSupport for us, no hint for user
        
        DTD dtd = sup.getDTD();
        if( dtd == null ) return null; // We have no knowledge about the structure!
        
        if(XHTML_PUBLIC_ID.equalsIgnoreCase(dtd.getIdentifier())) {
            //we are completing xhtml document
            isXHTML = true;
        }
        
        doc.readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(doc);
            TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
            if(ts == null) {
                //HTML language is not top level one
                ts = hi.tokenSequence();
                ts.move(offset);
                if (ts.moveNext() || ts.movePrevious()) {
                    ts = ts.embedded(HTMLTokenId.language());
                } else { // no tokens
                    return null;
                }
            }
            
            if(ts == null) {
                //no HTML token on the offset
                return null;
            }
            
            ts.move(offset);
            if(!ts.moveNext() && !ts.movePrevious()) {
                return null; //no token found
            }
            
            Token item = ts.token();
            
            // are we inside token or between tokens
            boolean inside = ts.offset() < offset;
            
            if(!inside) { //use the previous token
                if(ts.movePrevious()) {
                    item = ts.token();
                } else {
                    return null; //no previous token - shouldn't happen
                }
            }
            
            Token tok = item;
            //scan the token chain before the
            while(!(tok.id() == HTMLTokenId.TAG_OPEN || tok.id() == HTMLTokenId.TAG_CLOSE) && ts.movePrevious()) {
                tok = ts.token();
            }
            
            //we found an open or close tag or encountered beginning of the file
            if(ts.index() > 0) {
                //found the tag
                String tagName = tok.text().toString();
                for(int i = 0;i < tagName.length(); i++) {
                    char ch = tagName.charAt(i);
                    if(Character.isLetter(ch)) {
                        lowerCase = isXHTML || !Character.isUpperCase(tagName.charAt(i));
                        break;
                    }
                }
            } //else use the setting value
            
            //rewind token sequence back
            ts.move(item.offset(hi));
            
            //get text before cursor
            int itemOffset = item.offset(hi);
            int diff = offset - itemOffset;
            String preText = item.text().toString();
            
            if(diff < preText.length()) {
                preText = preText.substring( 0, offset - itemOffset );
            }
            TokenId id = item.id();
            
            List<CompletionItem> result = null;
            int len = 1;
            
            /* Character reference finder */
            int ampIndex = preText.lastIndexOf('&'); //NOI18N
            if((id == HTMLTokenId.TEXT || id == HTMLTokenId.VALUE) && ampIndex > -1) {
                len = preText.length() - ampIndex;
                String refNamePrefix = preText.substring(ampIndex + 1);
                result = translateCharRefs( offset-len, len, dtd.getCharRefList( refNamePrefix ) );
                
            } else if( id == HTMLTokenId.CHARACTER ) {
                if( inside || !preText.endsWith( ";" ) ) { // NOI18N
                    len = offset - itemOffset;
                    result = translateCharRefs( offset-len, len, dtd.getCharRefList( preText.substring(1) ) );
                }
                /* Tag finder */
            } else if( id == HTMLTokenId.TAG_OPEN) { // NOI18N
                len = offset - itemOffset + 1; // minus the < char length
                result = translateTags( itemOffset -1 , len, dtd.getElementList( preText ) );
                
                //test whether there is only one item in the CC list
                if(result.size() == 1) {
                    //test whether the CC is trying to complete an already COMPLETE token - the problematic situation
                    TagItem ti = (TagItem)result.get(0); //there should only one item
                    String itemText = ti.getItemText();
                    //itemText = itemText.substring(1, itemText.length() - 1); //remove the < > from the tag name
                    
                    if(preText.equals(itemText)) {
                        //now I have to look ahead to get know whether
                        //there are some attributes or an end of the tag
                        
                        ts.move(offset);
                        ts.moveNext();
                        Token t = ts.token();
                        
                        //test if next token is a whitespace and the next a tag token or an attribute token
                        if(t.id() == HTMLTokenId.WS) {
                            if(ts.moveNext()) {
                                t = ts.token();
                                if((t.id() == HTMLTokenId.TAG_CLOSE || t.id() == HTMLTokenId.ARGUMENT )) {
                                    //do not put the item into CC - otherwise it will break the completed tag
                                    result = null;
                                }
                            }
                        }
                    }
                }
                
                
            } else if( id != HTMLTokenId.BLOCK_COMMENT &&  preText.endsWith( "<" ) ) { // NOI18N
                // There will be lookup for possible StartTags, in SyntaxSupport
                //                l = translateTags( offset-len, len, sup.getPossibleStartTags ( offset-len, "" ) );
                result = translateTags( offset-len, len, dtd.getElementList( "" ) );
                
                /* EndTag finder */
            } else if( id == HTMLTokenId.TEXT && preText.endsWith( "</" ) ) { // NOI18N
                len = 2;
                result = sup.getPossibleEndTags( offset, "" );
            } else if( id == HTMLTokenId.TAG_OPEN_SYMBOL && preText.endsWith( "</" ) ) { // NOI18N
                len = 2;
                result = sup.getPossibleEndTags( offset, "" );
            } else if( id == HTMLTokenId.TAG_CLOSE) { // NOI18N
                len = offset - itemOffset;
                result = sup.getPossibleEndTags( offset, preText);
                
                /*Argument finder */
            /* TBD: It is possible to have arg just next to quoted value of previous
             * arg, these rules doesn't match start of such arg this case because
             * of need for matching starting quote
             */
            } else if(id == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                result = sup.getAutocompletedEndTag(offset);
                
            } else if( id == HTMLTokenId.WS || id == HTMLTokenId.ARGUMENT ) {
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
                
                if( elem.getElementOffset() == offset) {
                    //we are at the border between two syntax elements, 
                    //but need to use the previous one
                    //
                    //for example: < a hre|<td>...</td>
                    
                    elem = sup.getElementChain(offset - 1);
                }
                
                if( elem.getType() == SyntaxElement.TYPE_TAG ) { // not endTags
                    SyntaxElement.Tag tagElem = (SyntaxElement.Tag)elem;
                    
                    String tagName = tagElem.getName().toUpperCase();
                    DTD.Element tag = dtd.getElement( tagName );
                    
                    if( tag == null ) return null; // unknown tag
                    
                    String prefix = (id == HTMLTokenId.ARGUMENT) ? preText : "";
                    len = prefix.length();
                    List possible = tag.getAttributeList( prefix ); // All attribs of given tag
                    Collection<SyntaxElement.TagAttribute> existing = tagElem.getAttributes(); // Attribs already used
                    Collection<String> existingAttrsNames = new ArrayList<String>(existing.size());
                    for(SyntaxElement.TagAttribute ta : existing) {
                        existingAttrsNames.add(ta.getName());
                    }
                    
                    String wordAtCursor = (item == null) ? null : item.text().toString();
                    // #BUGFIX 25261 because of being at the end of document the
                    // wordAtCursor must be checked for null to prevent NPE
                    // below
                    if (wordAtCursor == null) {
                        wordAtCursor = "";
                    }
                    
                    List<DTD.Attribute> attribs = new ArrayList<DTD.Attribute>();
                    for( Iterator i = possible.iterator(); i.hasNext(); ) {
                        DTD.Attribute attr = (DTD.Attribute)i.next();
                        String aName = attr.getName();
                        if( aName.equals( prefix )
                        || (!existingAttrsNames.contains( aName.toUpperCase()) && !existingAttrsNames.contains( aName.toLowerCase(Locale.ENGLISH)))
                        || (wordAtCursor.equals( aName ) && prefix.length() > 0)) {
                            attribs.add( attr );
                        }
                    }
                    result = translateAttribs( offset-len, len, attribs, tag );
                }
                
                /* Value finder */
            /* Suggestion - find special-meaning attributes ( IMG src, A href,
             * color,.... - may be better resolved by attr type, may be moved
             * to propertysheet
             */
            } else if( id == HTMLTokenId.VALUE || id == HTMLTokenId.OPERATOR || id == HTMLTokenId.WS ) {
                
                if(id == HTMLTokenId.WS) {
                    //is the token before an operator? '<div color= |red>'
                    ts.move(item.offset(hi));
                    ts.movePrevious();
                    Token t = ts.token();
                    if(t.id() != HTMLTokenId.OPERATOR) {
                        return null;
                    }
                }
                
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
                    
                    ts.move(item.offset(hi));
                    ts.moveNext();
                    Token argItem = ts.token();
                    while(argItem.id() != HTMLTokenId.ARGUMENT && ts.movePrevious()) {
                        argItem = ts.token();
                    }
                    
                    if(argItem.id() != HTMLTokenId.ARGUMENT) return null; // no ArgItem
                    
                    String argName = argItem.text().toString().toLowerCase(Locale.ENGLISH);
                    
                    DTD.Attribute arg = tag.getAttribute( argName );
                    if( arg == null || arg.getType() != DTD.Attribute.TYPE_SET ) return null;
                    
                    if( id != HTMLTokenId.VALUE ) {
                        len = 0;
                        result = translateValues( offset-len, len, arg.getValueList( "" ) );
                    } else {
                        len = offset - itemOffset;
                        
                        String quotationChar = null;
                        if(preText != null && preText.length() > 0) {
                            if(preText.substring(0,1).equals("'")) quotationChar = "'"; // NOI18N
                            if(preText.substring(0,1).equals("\"")) quotationChar = "\""; // NOI18N
                        }
                        
                        result = translateValues( offset-len, len, arg.getValueList( quotationChar == null ? preText : preText.substring(1)) , quotationChar );
                    }
                }
            } else if( id == HTMLTokenId.SCRIPT) {
                result = addEndTag(SCRIPT_TAG_NAME, preText, offset);
            } else if( id == HTMLTokenId.STYLE) {
                result = addEndTag(STYLE_TAG_NAME, preText, offset);
            }
            
        return result;
            
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ble);
        } finally {
            doc.readUnlock();
        }
        
        return null;
    }
    
    private List<CompletionItem> addEndTag(String tagName, String preText, int offset) {
        int commonLength = getLastCommonCharIndex("</" + tagName + ">", preText.toUpperCase().trim()); //NOI18N
        if (commonLength == -1) {
            commonLength = 0;
        }
        if (commonLength == preText.trim().length()) {
            ArrayList<CompletionItem> items = new ArrayList<CompletionItem>(1);
            items.add(new EndTagItem(lowerCase ? tagName.toLowerCase(Locale.ENGLISH) : tagName, offset - commonLength, commonLength));
            return items;
        }
        return null;
    }
    
    private int getLastCommonCharIndex(String base, String pattern) {
        int i = 0;
        for(; i < base.length() && i < pattern.length(); i++) {
            if(base.charAt(i) != pattern.charAt(i)) {
                i--;
                break;
            }
        }
        return i;
    }
    
    List<CompletionItem> translateCharRefs( int offset, int length, List refs ) {
        List result = new ArrayList( refs.size() );
        String name;
        for( Iterator i = refs.iterator(); i.hasNext(); ) {
            name = ((DTD.CharRef)i.next()).getName();
            result.add( new CharRefItem( name , offset, length, name ));
        }
        return result;
    }
    
    List<CompletionItem> translateTags( int offset, int length, List tags ) {
        List result = new ArrayList( tags.size() );
        String name;
        for( Iterator i = tags.iterator(); i.hasNext(); ) {
            name = ((DTD.Element)i.next()).getName();
            result.add( new TagItem( name , offset, length, name ));
        }
        return result;
    }
    
    List<CompletionItem> translateAttribs( int offset, int length, List<DTD.Attribute> attribs, DTD.Element tag ) {
        List<CompletionItem> result = new ArrayList<CompletionItem>( attribs.size() );
        String tagName = tag.getName() + "#"; // NOI18N
        for(DTD.Attribute attrib : attribs) {
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
    
    List<CompletionItem> translateValues( int offset, int length, List values ) {
        return translateValues(offset, length, values, null);
    }
    
    List<CompletionItem> translateValues( int offset, int length, List values, String quotationChar ) {
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
    public static abstract class HTMLResultItem implements CompletionItem {
        
        /** The String on which is this ResultItem defined */
        String baseText;
        /** the remove and insert point for this item */
        int offset;
        /** The length of the text to be removed */
        int length;
        
        String helpID;
        
        boolean shift = false;
        
        private HTMLCompletionItemPC component;
        
        private static final int HTML_ITEMS_SORT_PRIORITY = 20;
        
        public HTMLResultItem( String baseText, int offset, int length ) {
            this.baseText = lowerCase ? baseText.toLowerCase(Locale.ENGLISH) : baseText.toUpperCase();
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
        
        public CharSequence getInsertPrefix() {
            return getItemText();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            //TODO: the paint component should be caches somehow
            HTMLCompletionItemPC component = new HTMLCompletionItemPC.StringPaintComponent(getPaintColor());
            component.setSelected(isSelected);
            component.setString(getItemText());
            return component;
        }
        
        public int getPreferredWidth(Graphics g, Font defaultFont) {
            HTMLCompletionItemPC renderComponent = (HTMLCompletionItemPC)getPaintComponent(false);
            return renderComponent.getPreferredWidth(g, defaultFont);
        }
        
        public void render(Graphics g, Font defaultFont, Color defaultColor,
                Color backgroundColor, int width, int height, boolean selected) {
            Component renderComponent = getPaintComponent(selected);
            renderComponent.setFont(defaultFont);
            renderComponent.setForeground(defaultColor);
            renderComponent.setBackground(backgroundColor);
            renderComponent.setBounds(0, 0, width, height);
            ((HTMLCompletionItemPC)renderComponent).paintComponent(g);
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
        
        public void processKeyEvent(KeyEvent e) {
            shift = (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED && e.isShiftDown());
        }
        
        public void defaultAction(JTextComponent component) {
            int substOffset = getSubstituteOffset();
            if (substOffset == -1)
                substOffset = component.getCaretPosition();
            
            if(!shift) Completion.get().hideAll();
            substituteText(component, substOffset, component.getCaretPosition() - substOffset, shift);
        }
        
        boolean replaceText(final JTextComponent component, final String text) {
            final BaseDocument doc = (BaseDocument) component.getDocument();
            final boolean[] result = new boolean[1];
            result[0] = true;
            doc.runAtomic(new Runnable() {
                public void run() {
                    try {
                        //test whether we are trying to insert sg. what is already present in the text
                        String currentText = doc.getText(offset, (doc.getLength() - offset) < text.length() ? (doc.getLength() - offset) : text.length());
                        if (!text.equals(currentText)) {
                            //remove common part
                            doc.remove(offset, length);
                            doc.insertString(offset, text, null);
                        } else {
                            int newCaretPos = component.getCaret().getDot() + text.length() - length;
                            //#82242 workaround - the problem is that in some situations
                            //1) result item is created and it remembers the remove length
                            //2) document is changed
                            //3) RI is substituted.
                            //this situation shouldn't happen imho and is a problem of CC infrastructure
                            component.setCaretPosition(newCaretPos < doc.getLength() ? newCaretPos : doc.getLength());
                        }
                    } catch (BadLocationException ble) {
                        result[0] = false;
                    }
                }
            });
            return result[0];
        }
        
        protected void reformat(JTextComponent component, String text) {
            //does nothing by default; is overriden in EndTag
        }
        
        public boolean substituteCommonText( JTextComponent c, int a, int b, int subLen ) {
            String text = getItemText().substring( 0, subLen );
            boolean replaced = replaceText( c,  text);
            reformat(c, text);
            return replaced;
        }
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            String text = getItemText();
            boolean replaced = replaceText( c, text );
            reformat(c, text);
            return replaced;
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
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            String className = this.getClass().getName();
            className = className.substring(className.lastIndexOf('.') + 1); //cut off the package
            sb.append(className);
            sb.append('(');
            sb.append(getItemText());
            sb.append(';');
            sb.append(getSubstituteOffset());
            sb.append(';');
            sb.append(getHelpID());
            sb.append(')');
            
            return sb.toString();
        }
        
    }
    
    public static class AutocompleteEndTagItem extends EndTagItem {
        public AutocompleteEndTagItem(String baseText, int offset) {
            this(baseText, offset, true);
        }
        
        public AutocompleteEndTagItem(String baseText, int offset, boolean changeCase) {
            super(baseText, offset, 0);
            if(!changeCase) {
                this.baseText = baseText; //#87218 hotfix - set the original value
            }
        }
        
        @Override()
        boolean replaceText(JTextComponent component, String text) {
            boolean replaced = super.replaceText(component, text);
            if(replaced) {
                component.setCaretPosition(offset);
            }
            return replaced;
        }

        @Override
        public CharSequence getInsertPrefix() {
            //disable instant substitution
            return null;
        }

        @Override
        public boolean instantSubstitution(JTextComponent c) {
            return false; //do not complete even if we are the only item in the completion
        }
        
        
    }
    
    static class NonHTMLEndTagItem extends EndTagItem {
        
        public NonHTMLEndTagItem( String baseText, int offset, int length, int order ) {
            super( baseText, offset, length, null, order );
            this.baseText = baseText; //ufff, ugly ... reset the original text back in super we change the case 
        }
        
    }
    
    static class EndTagItem extends HTMLResultItem {
        
        private int order = 0;
        
        public EndTagItem( String baseText, int offset, int length ) {
            super( baseText, offset, length );
        }
        
        public EndTagItem( String baseText, int offset, int length, String helpID ) {
            super( baseText, offset, length, helpID );
        }
        
        public EndTagItem( String baseText, int offset, int length, String helpID, int order ) {
            this(baseText, offset, length, helpID);
            this.order = order;
        }
        
        public CharSequence getSortText() {
            return getSortText(this.order);
        }
        
        private String getSortText(int index) {
            int zeros = index > 100 ? 0 : index > 10 ? 1 : 2;
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < zeros; i++) {
                sb.append('0');
            }
            sb.append("" + index);
            return sb.toString();
        }
        
        Color getPaintColor() { return Color.blue; }
        
        public String getItemText() { return "</" + baseText + ">"; } // NOI18N
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            return super.substituteText( c, a, b, shift );
        }
        
        @Override
        protected void reformat(JTextComponent component, String text) {
            final BaseDocument doc = (BaseDocument) component.getDocument();
            final int dotPos = component.getCaretPosition();

            TokenHierarchy tokenHierarchy = TokenHierarchy.get(doc);
            for (final LanguagePath languagePath : (Set<LanguagePath>) tokenHierarchy.languagePaths()) {
                if (languagePath.innerLanguage() == HTMLTokenId.language()) {
                    doc.runAtomic(new Runnable() {
                        public void run() {
                            HtmlIndenter.indentEndTag(doc, languagePath, dotPos, baseText);
                        }
                    });

//            //PUT BACK ONCE WE PROPERY IMPLEMENT HTML INDENT TASK
//            final Indent indent = Indent.get(doc);
//            indent.lock();
//            try {
//                doc.runAtomic(new Runnable() {
//
//                    public void run() {
//                        try {
//                            int startOffset = Utilities.getRowStart(doc, dotPos);
//                            int endOffset = Utilities.getRowEnd(doc, dotPos);
//                            indent.reindent(startOffset, endOffset);
//                        } catch (BadLocationException ex) {
//                            //ignore
//                        }
//                    }
//                });
//            } finally {
//                indent.unlock();
//            }

                }

            }
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
        
        public CharSequence getSortText() {
            String itext = getItemText();
            return itext.endsWith(";") ? itext.substring(0, itext.length() - 1) : itext;
        }
        
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
            Completion.get().showCompletion(); //show the completion to possibly offer an end tag (end tag autocompletion feature)
            return !shift; // flag == false;
        }
        
        Color getPaintColor() { return Color.blue; }
        
        public String getItemText() {
            return "<" + baseText + ">";
        } // NOI18N
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
            Caret caret = c.getCaret();
            caret.setDot( caret.getDot() - 1 );
            
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
            Caret caret = c.getCaret();
            caret.setDot( caret.getDot() - 1 );
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
        
        public CharSequence getInsertPrefix() {
            if(quotationChar == null) {
                return super.getInsertPrefix();
            } else {
                return quotationChar + super.getInsertPrefix();
            }
        }
        
        Color getPaintColor() { return Color.magenta; }
        
        public boolean substituteText( JTextComponent c, int a, int b, boolean shift ) {
            //check whether there is already a " char after the CC offset
            BaseDocument doc = (BaseDocument)c.getDocument();
            boolean hasQuote = false;
            try {
                String currentText = doc.getText(c.getCaretPosition(), 1);
                hasQuote = "\"".equals(currentText);
            }catch(BadLocationException ble) {
                //do nothing
            }
            String quotedText = ((quotationChar == null) ? baseText : quotationChar + baseText + (hasQuote ? "" : quotationChar));
            replaceText( c, quotedText );
            return !shift;
        }
    }
    
    static class DocQuery extends AsyncCompletionQuery {
        
        private HTMLResultItem item;
        
        DocQuery(HTMLResultItem item) {
            this.item = item;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            if (item != null &&
                    item.getHelpID() != null &&
                    HelpManager.getDefault().findHelpItem(item.getHelpID()) != null) {
                resultSet.setDocumentation(new DocItem(item));
            }
            resultSet.finish();
        }
        
    }
    
    static class LinkDocItem implements CompletionDocumentation {
        private URL url;
        
        public LinkDocItem(URL url) {
            this.url = url;
        }
        
        public String getText() {
            return null;
            /*
            String anchor = HelpManager.getDefault().getAnchorText(url);
            if(anchor != null)
                return HelpManager.getDefault().getHelpText(url, anchor);
            else
                return HelpManager.getDefault().getHelpText(url);
             */
        }
        
        public URL getURL() {
            return url;
        }
        
        public CompletionDocumentation resolveLink(String link) {
            return new LinkDocItem(HelpManager.getDefault().getRelativeURL(url, link));
        }
        
        public Action getGotoSourceAction() {
            return null;
        }
        
    }
    
    
    public static class DocItem implements CompletionDocumentation {
        private String name;
        
        public DocItem(HTMLResultItem ri) {
            this(ri.getHelpID());
        }
        
        public DocItem(String name) {
            this.name = name;
        }
        
        public String getText() {
            String help = HelpManager.getDefault().getHelp(name);
            return help;
        }
        
        public URL getURL() {
            return HelpManager.getDefault().getHelpURL(name);
        }
        
        public CompletionDocumentation resolveLink(String link) {
            String currentLink = HelpManager.getDefault().findHelpItem(name).getFile();
            return new LinkDocItem(HelpManager.getDefault().getRelativeURL(HelpManager.getDefault().getHelpURL(name), link));
        }
        
        public Action getGotoSourceAction() {
            return null;
        }
    }
    
}

