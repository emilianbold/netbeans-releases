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

package org.netbeans.modules.xml.text.completion;

import java.util.*;
import java.util.Enumeration;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;

import org.w3c.dom.*;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;
import org.openide.ErrorManager;

import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.text.syntax.dom.*;
import org.netbeans.modules.xml.api.model.*;
import org.netbeans.modules.xml.spi.dom.UOException;
import org.netbeans.modules.xml.text.bracematch.XMLBraceMatcher;
import org.netbeans.modules.xml.text.syntax.dom.SyntaxNode;
import org.openide.util.NbBundle;

/**
 * Consults grammar and presents list of possible choices
 * in particular document context.
 * <p>
 * <b>Warning:</b> It is public for unit test purposes only!
 *
 * @author Petr Nejedly
 * @author Sandeep Randhawa
 * @author Petr Kuzel
 * @author asgeir@dimonsoftware.com
 * @version 1.01
 */

public class XMLCompletionQuery implements CompletionQuery, XMLTokenIDs {
    
    // the name of a property indentifing cached query
    public static final String DOCUMENT_GRAMMAR_BINDING_PROP = "doc-bind-query";
    
    /**
     * Perform the query on the given component. The query usually
     * gets the component's document, the caret position and searches back
     * to examine surrounding context. Then it returns the result.
     * <p>
     * It is also called after every keystroke while opened completion
     * popup. So some result cache could be used. It is not easy at
     * this level because of BACKSPACE that can extend result.
     *
     * @param component the component to use in this query.
     * @param offset position in the component's document to which the query will
     *   be performed. Usually it's a caret position.
     * @param support syntax-support that will be used during resolving of the query.
     * @return result of the query or null if there's no result.
     */
    public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support) {
        
        // assert precondition, actually serial access required
        
//        synchronized (this) {
//            if (thread == null) {
//                thread = new ThreadLocal();
//                thread.set(thread);
//            }
//            if (thread != thread.get()) {
//                // unfortunatelly RP can probably provide serialization even
//                // if delegating to multiple threads
//                // throw new IllegalStateException("Serial access required!");     //NOI18N
//            }
//        }
        
        // perform query
        
        BaseDocument doc = (BaseDocument)component.getDocument();
        if (doc == null) return null;
        XMLSyntaxSupport sup = (XMLSyntaxSupport)support.get(XMLSyntaxSupport.class);
        if( sup == null ) return null;// No SyntaxSupport for us, no hint for user
        
        try {
            SyntaxQueryHelper helper = new SyntaxQueryHelper(sup, offset);
            
            //supposing that there wont be more cc items result sets in one moment hence
            //using the static field, otherwise the substitute offset would have to be set to all CC items
            XMLResultItem.substituteOffset = helper.getOffset() - helper.getEraseCount();
            
            // completion request originates from area covered by DOM,
            if (helper.getCompletionType() != SyntaxQueryHelper.COMPLETION_TYPE_DTD) {
                List all = new ArrayList();
                List list = null;
                switch (helper.getCompletionType()) {
                    case SyntaxQueryHelper.COMPLETION_TYPE_ATTRIBUTE:
                        list = queryAttributes(helper, doc, sup);
                        break;
                    case SyntaxQueryHelper.COMPLETION_TYPE_VALUE:
                        list = queryValues(helper, doc, sup);
                        break;
                    case SyntaxQueryHelper.COMPLETION_TYPE_ELEMENT:
                        list = queryElements(helper, doc, sup);
                        break;
                    case SyntaxQueryHelper.COMPLETION_TYPE_ENTITY:
                        list = queryEntities(helper, doc, sup);
                        break;
                    case SyntaxQueryHelper.COMPLETION_TYPE_NOTATION:
                        list = queryNotations(helper, doc, sup);
                    case SyntaxQueryHelper.COMPLETION_TYPE_UNKNOWN:
                        return null; //do not show the CC
                }
                
                if(list == null) {
                    // broken document
                    return cannotSuggest(component, sup.requestedAutoCompletion());
                }

                if (helper.getCompletionType() == SyntaxQueryHelper.COMPLETION_TYPE_VALUE) {
                    //might be the end tag autocompletion
                    if(helper.getToken().getTokenID() == XMLDefaultTokenContext.TAG) {
                        SyntaxElement se = helper.getSyntaxElement();
                        if(se instanceof StartTag) {
                            String tagName = ((StartTag)se).getNodeName();
                            if(tagName != null && !XMLBraceMatcher.hasEndTag(doc, offset, tagName))
                                list.add(new EndTagAutocompletionResultItem(tagName));
                        }
                    }
                }
                
                if (list.isEmpty() && helper.getPreText().startsWith("</")) { // NOI18N
                    List stlist = findStartTag((SyntaxNode)helper.getSyntaxElement(), !helper.getPreText().endsWith("/") ? "/" : "");
                    if (stlist != null && !stlist.isEmpty()) {
                        ElementResultItem item = (ElementResultItem)stlist.get(0); //we always get just one item
                        if(!XMLBraceMatcher.hasEndTag(doc, offset, item.getItemText()) &&
                           (!item.getItemText().startsWith("/") ||
                            item.getItemText().startsWith(helper.getPreText().substring(1)))) {
                            String title = NbBundle.getMessage(XMLCompletionQuery.class, "MSG_result", helper.getPreText());
                            return new XMLCompletionResult(component, title,
                                    stlist, helper.getOffset(), 0);
                        }
                    }
                }
                
                String debugMsg = Boolean.getBoolean("netbeans.debug.xml") ? " " + helper.getOffset() + "-" + helper.getEraseCount() : "";
                String title = NbBundle.getMessage(XMLCompletionQuery.class, "MSG_result", helper.getPreText()) + debugMsg;
                
                // add to the list end tag if detected '<'
                // unless following end tag is of the same name
                
                if (helper.getPreText().endsWith("<") && helper.getToken().getTokenID() == TEXT) { // NOI18N
                    List startTags = findStartTag((SyntaxNode)helper.getSyntaxElement(), "/"); // NOI18N
                    
                    boolean addEndTag = true;
                    SyntaxNode ctx = (SyntaxNode)helper.getSyntaxElement();
                    SyntaxElement nextElement = ctx != null ? ctx.getNext() : null;
                    if (nextElement instanceof EndTag) {
                        EndTag endtag = (EndTag) nextElement;
                        String nodename = endtag.getNodeName();
                        if (nodename != null && startTags.isEmpty() == false) {
                            ElementResultItem item = (ElementResultItem)startTags.get(0);
                            if (("/" + nodename).equals(item.getItemText())) {  // NOI18N
                                addEndTag = false;
                            }
                        }
                    }
                    
                    if (addEndTag) {
                        //prevent autocompletion of the only CC item.
                        if(!list.isEmpty() || startTags.size() > 1) {
                            all.addAll(startTags);
                        }
                    }
                    
                }
                
                all.addAll(list);
                
                if(all.isEmpty()) {
                    return noSuggestion(component, sup.requestedAutoCompletion());
                } else {
                    return new XMLCompletionResult(
                            component,
                            title,
                            all,
                            helper.getOffset() - helper.getEraseCount(),
                            helper.getEraseCount()
                            );
                }
                
            } else {
                // prolog, internal DTD no completition yet
                if (helper.getToken().getTokenID() == PI_CONTENT) {
                    if (helper.getPreText().endsWith("encoding=")) {                        // NOI18N
                        List encodings = new ArrayList(2);
                        encodings.add(new XMLResultItem("\"UTF-8\""));          // NOI18N
                        encodings.add(new XMLResultItem("\"UTF-16\""));         // NOI18N
                        return new XMLCompletionResult(
                                component,
                                NbBundle.getMessage(XMLCompletionQuery.class, "MSG_encoding_comp"),
                                encodings,
                                helper.getOffset(),
                                0
                                );
                    }
                }
                return noSuggestion(component, sup.requestedAutoCompletion());
            }
            
        } catch (BadLocationException e) {
            //Util.THIS.debug(e);
        }
        
        // nobody knows what happened...
        return noSuggestion(component, sup.requestedAutoCompletion());
    }
        
    /**
     * Contruct result indicating that grammar is not able to give
     * a hint because document is too broken or invalid. Grammar
     * knows that it's broken.
     */
    private static Result cannotSuggest(JTextComponent component, boolean auto) {
        if (auto) return null;
        return new XMLCompletionResult(
                component,
                NbBundle.getMessage(XMLCompletionQuery.class, "BK0002"),
                Collections.EMPTY_LIST,
                0,
                0
                );
    }
    
    /**
     * Contruct result indicating that grammar is not able to give
     * a hint because in given context is not nothing allowed what
     * the grammar know of. May grammar is missing at all.
     */
    private static Result noSuggestion(JTextComponent component, boolean auto) {
        if (auto) return null;
        return new XMLCompletionResult(
                component,
                NbBundle.getMessage(XMLCompletionQuery.class, "BK0003"),
                Collections.EMPTY_LIST,
                0,
                0
                );
    }
    
    // Grammar binding ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * Obtain grammar manager, cache results in document property
     * <code>PROP_DOCUMENT_QUERY</code>. It is always called from single
     * thread.
     */
    public static GrammarQuery getPerformer(Document doc, XMLSyntaxSupport sup) {
        
        Object grammarBindingObj = doc.getProperty(DOCUMENT_GRAMMAR_BINDING_PROP);
        
        if (grammarBindingObj == null) {
            grammarBindingObj = new GrammarManager(doc, sup);
            doc.putProperty(DOCUMENT_GRAMMAR_BINDING_PROP, grammarBindingObj);
        }
        
        return ((GrammarManager)grammarBindingObj).getGrammar();
    }
    
    // Delegate queriing to performer ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private List queryEntities(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {
        Enumeration res = getPerformer(doc, sup).queryEntities(helper.getContext().getCurrentPrefix());
        return translateEntityRefs(res);
    }
    
    private List queryElements(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {
        try {
            GrammarQuery performer = getPerformer(doc, sup);
            HintContext ctx = helper.getContext();
            //66607 hacky fix - see the issue comment
            if(helper.getPreText().startsWith("</")) {
                return Collections.EMPTY_LIST;
            }
            String typedPrefix = ctx.getCurrentPrefix();
            Enumeration res = performer.queryElements(ctx);
            return translateElements(res, typedPrefix, performer);
        } catch(UOException e){
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    private List queryAttributes(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {
        Enumeration res = getPerformer(doc, sup).queryAttributes(helper.getContext());
        return translateAttributes(res, helper.isBoundary());
    }
    
    private List queryValues(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {
        try {
            Enumeration res = getPerformer(doc, sup).queryValues(helper.getContext());
            return translateValues(res);
        } catch (Exception ex) {            
            //issue #118136: just catch and return
            return null;
        }
    }
    
    private List queryNotations(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {  //!!! to be implemented
        Enumeration res = getPerformer(doc, sup).queryNotations(helper.getContext().getCurrentPrefix());
        return null;
    }
    
    // Translate general results to editor ones ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private List translateEntityRefs(Enumeration refs ) {
        List result = new ArrayList(133);
        while ( refs.hasMoreElements() ) {
            GrammarResult next = (GrammarResult) refs.nextElement();
            if(next != null && next.getNodeName() != null) {
                EntityRefResultItem ref = new EntityRefResultItem(next);
                result.add( ref );
            }
        }
        return result;
    }
    
    /** Translate results perfromer (DOM nodes) format to CompletionQuery.ResultItems format. */
    private List translateElements(Enumeration els, String prefix, GrammarQuery perfomer) {
        List result = new ArrayList(13);
        while (els.hasMoreElements()) {
            GrammarResult next = (GrammarResult) els.nextElement();
            if (prefix.equals(next.getNodeName())) {
// XXX It's probably OK that perfomer has returned it, we just do not want to visualize it
//                ErrorManager err =ErrorManager.getDefault();
//                err.log(ErrorManager.WARNING, "Grammar " + perfomer.getClass().getName() + " result '"  + prefix + "' eliminated to avoid #28224.");  // NOi18N
                continue;
            }
            if(next != null && next.getNodeName() != null) {
                ElementResultItem ei = new ElementResultItem(next);
                result.add( ei );
            }
        }
        return result;
    }
    
    
    private List translateAttributes(Enumeration attrs, boolean boundary) {
        List result = new ArrayList(13);
        while (attrs.hasMoreElements()) {
            GrammarResult next = (GrammarResult) attrs.nextElement();
            if(next != null && next.getNodeName() != null) {
                AttributeResultItem attr = new AttributeResultItem(next, false);
                result.add( attr );
            }
        }
        return result;
    }
    
    private List translateValues(Enumeration values ) {
        List result = new ArrayList(3);
        while (values.hasMoreElements()) {
            GrammarResult next = (GrammarResult) values.nextElement();
            if(next != null && next.getDisplayName() != null) {
                ValueResultItem val = new ValueResultItem(next);
                result.add( val );
            }
        }
        return result;
    }
    
    
    /**
     * User just typed <sample>&lt;/</sample> so we must locate
     * paing start tag.
     * @param text pointer to starting context
     * @param prefix that is prepended to created ElementResult e.g. '</'
     * @return list with one ElementResult or empty.
     */
    private static List findStartTag(SyntaxNode text, String prefix) {
        //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("XMLCompletionQuery.findStartTag: text=" + text);
        
        Node parent = text.getParentNode();
        if (parent == null) {
            return Collections.EMPTY_LIST;
        }
        
        String name = parent.getNodeName();
        //if ( Util.THIS.isLoggable() ) Util.THIS.debug("    name=" + name);
        if ( name == null ) {
            return Collections.EMPTY_LIST;
        }
        
        XMLResultItem res = new ElementResultItem(prefix + name);
        //if ( Util.THIS.isLoggable() ) Util.THIS.debug("    result=" + res);
        
        List list = new ArrayList(1);
        list.add(res);
        
        return list;
    }
    
    private static List findStartTag(SyntaxNode text) {
        return findStartTag(text, "");
    }
    
    public static class XMLCompletionResult extends CompletionQuery.DefaultResult {
        private int substituteOffset;
        public XMLCompletionResult(JTextComponent component, String title, List data, int offset, int len ) {
            super(component, title, data, offset, len);
            substituteOffset = offset;
        }
        
        public int getSubstituteOffset() {
            return substituteOffset;
        }
    }
    
}
