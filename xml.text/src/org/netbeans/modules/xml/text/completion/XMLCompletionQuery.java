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

package org.netbeans.modules.xml.text.completion;

import java.util.*;
import java.awt.Color;
import java.net.URL;
import java.io.IOException;
import java.util.Enumeration;

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
import org.openide.loaders.DataLoader;
import org.openide.loaders.UniFileLoader; 
import org.openide.filesystems.FileObject;
import org.openide.loaders.ExtensionList;
import org.openide.ErrorManager;

import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.text.syntax.dom.*;
import org.netbeans.modules.xml.api.model.*;
import org.netbeans.modules.xml.spi.dom.UOException;

import javax.swing.Icon;
import org.netbeans.modules.xml.text.syntax.dom.SyntaxNode;

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
    
    // remember last thread that invoked query method
    private ThreadLocal thread;

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

            // completion request originates from area covered by DOM, 
            if (helper.getCompletionType() != SyntaxQueryHelper.COMPLETION_TYPE_DTD) {
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
                        break;
                }

                if (list != null && list.isEmpty() == false) {
                    String debugMsg = Boolean.getBoolean("netbeans.debug.xml") ? " " + helper.getOffset() + "-" + helper.getEraseCount() : "";
                    String title = Util.THIS.getString("MSG_result", helper.getPreText()) + debugMsg;

                     // add to the list end tag if detected '<'
                     // unless following end tag is of the same name

                     if (helper.getPreText().endsWith("<") && helper.getToken().getTokenID() == TEXT) { // NOI18N
                         List startTags = findStartTag((SyntaxNode)helper.getSyntaxElement(), "/"); // NOI18N

                         boolean addEndTag = list.isEmpty() == false;
                         if (addEndTag) {
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
                         }
                         if (addEndTag) {
                             list.addAll(startTags);
                         }
                     }

                    return new CompletionQuery.DefaultResult(
                            component,
                            title,
                            list,
                            helper.getOffset() - helper.getEraseCount(),
                            helper.getEraseCount()
                    );
                } else {
                    
                    // auto complete end tag without showing popup
                    
                    if (helper.getPreText().endsWith("</") && helper.getToken().getTokenID() == TEXT) { // NOI18N
                        list = findStartTag((SyntaxNode)helper.getSyntaxElement());
                        if (list != null && list.size() == 1) {
                            ElementResultItem item = (ElementResultItem)list.get(0);
                            item.substituteText(component, helper.getOffset(), 0, 0);
                            return null;
                        }
                    }

                    if (list == null) { // broken document
                        return cannotSuggest(component, sup.requestedAutoCompletion());
                    } else { // grammar has no suggestion
                        return noSuggestion(component, sup.requestedAutoCompletion());
                    }
                }
            } else {
                // prolog, internal DTD no completition yet
                if (helper.getToken().getTokenID() == PI_CONTENT) {
                    if (helper.getPreText().endsWith("encoding=")) {                        // NOI18N
                        List encodings = new ArrayList(2);
                        encodings.add(new XMLResultItem("\"UTF-8\""));          // NOI18N
                        encodings.add(new XMLResultItem("\"UTF-16\""));         // NOI18N
                        return new CompletionQuery.DefaultResult(
                            component,
                            Util.THIS.getString("MSG_encoding_comp"),
                            encodings,
                            helper.getOffset(),
                            0
                        );
                    }
                }
                return noSuggestion(component, sup.requestedAutoCompletion());
            }

        } catch (BadLocationException e) {
            Util.THIS.debug(e);
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
        return new CompletionQuery.DefaultResult(
            component,
            Util.THIS.getString("BK0002"),
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
        return new CompletionQuery.DefaultResult(
            component,
            Util.THIS.getString("BK0003"),
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
            String typedPrefix = ctx.getCurrentPrefix();
            Enumeration res = performer.queryElements(ctx);
            return translateElements(res, typedPrefix, performer);
        } 
        catch(UOException e){
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }

    private List queryAttributes(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {
        Enumeration res = getPerformer(doc, sup).queryAttributes(helper.getContext());
        return translateAttributes(res, helper.isBoundary());
    }

    private List queryValues(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {
        Enumeration res = getPerformer(doc, sup).queryValues(helper.getContext());
        return translateValues(res);
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
            EntityRefResultItem ref = new EntityRefResultItem(next);
            result.add( ref );
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
            ElementResultItem ei = new ElementResultItem(next);
            result.add( ei );
        }
        return result;
    }
    
    
    private List translateAttributes(Enumeration attrs, boolean boundary) {
        List result = new ArrayList(13);
        while (attrs.hasMoreElements()) {
            GrammarResult next = (GrammarResult) attrs.nextElement();            
            AttributeResultItem attr = new AttributeResultItem(next, boundary == false);
            result.add( attr );
        }
        return result;
    }
    
    private List translateValues(Enumeration values ) {
        List result = new ArrayList(3);
        while (values.hasMoreElements()) {
            GrammarResult next = (GrammarResult) values.nextElement();
            ValueResultItem val = new ValueResultItem(next);
            result.add( val );
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
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLCompletionQuery.findStartTag: text=" + text);

        Node parent = text.getParentNode();
        if (parent == null) {
            return Collections.EMPTY_LIST;
        }

        String name = parent.getNodeName();
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("    name=" + name);
        }
        if ( name == null ) {
            return Collections.EMPTY_LIST;
        }

        XMLResultItem res = new ElementResultItem(prefix + name);
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("    result=" + res);
        }

        List list = new ArrayList(1);
        list.add (res);

        return list;
    }

    private static List findStartTag(SyntaxNode text) {
        return findStartTag(text, "");
    }
}
