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

import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.text.syntax.dom.*;
import org.netbeans.modules.xml.api.model.*;

import javax.swing.Icon;
import org.netbeans.modules.xml.text.syntax.dom.SyntaxNode;
import org.openide.TopManager;

/**
 * Consults grammar and presents list of possible choices
 * in particular document context.
 * <p>
 * <b>Warning:</b> It is public for unit test purposes only!
 *
 * @author Petr Nejedly
 * @author Sandeep Randhawa
 * @author Petr Kuzel
 * @author Asgeir Orn Asgeirsson
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

        // assert precondition
        
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException("Called from non-AWT thread: " + Thread.currentThread().getName());  //NOI18N
        }
        
        BaseDocument doc = (BaseDocument)component.getDocument();
        XMLSyntaxSupport sup = (XMLSyntaxSupport)support.get(XMLSyntaxSupport.class);
        if( sup == null ) return null;// No SyntaxSupport for us, no hint for user
        
        try {
            System.out.println("+++ About to create SyntaxQueryHelper");
            SyntaxQueryHelper helper = new SyntaxQueryHelper(sup, offset);
            System.out.println("+++ Created SyntaxQueryHelper");

            // completion request originates from area covered by DOM, 
            if (helper.getCompletionType() != SyntaxQueryHelper.COMPLETION_TYPE_DTD) {
            System.out.println("+++ Find list for type: " + helper.getCompletionType());
                List list = null;
                switch (helper.getCompletionType()) {
                    case SyntaxQueryHelper.COMPLETION_TYPE_ATTRIBUTE:
                        list = queryAttributes(helper, doc, sup);
                        break;
                    case SyntaxQueryHelper.COMPLETION_TYPE_VALUE:
                        list = queryValues(helper, doc, sup);
                        break;
                    case SyntaxQueryHelper.COMPLETION_TYPE_ELEMENT:
            System.out.println("+++ queryElements");
                        list = queryElements(helper, doc, sup);
                        break;
                    case SyntaxQueryHelper.COMPLETION_TYPE_ENTITY:
                        list = queryEntities(helper, doc, sup);
                        break;
                    case SyntaxQueryHelper.COMPLETION_TYPE_NOTATION:
                        list = queryNotations(helper, doc, sup);
                        break;
                }
            System.out.println("+++ list: " + list);
                
                 if (list != null && list.isEmpty() == false) {
                    String debugMsg = Boolean.getBoolean("netbeans.debug.xml") ? " " + helper.getOffset() + "-" + helper.getEraseCount() : "";
                    String title = Util.THIS.getString("MSG_result", new Object[] {helper.getPreText()}) + debugMsg;
                    return new CompletionQuery.DefaultResult( component, title, list, helper.getOffset() - helper.getEraseCount(), helper.getEraseCount() );
                } else {
                    
                    // auto complete end tag without showing popup
                    
                    if (helper.getPreText().endsWith("</") && helper.getToken().getTokenID() == TEXT) { // NOI18N
                        list = findStartTag((SyntaxNode)helper.getSyntaxElement());
                        if (list != null && list.size() == 1) {
                            ElementResultItem item = (ElementResultItem)list.get(0);
                            item.substituteText(component, helper.getOffset(), 0, 0);
                        }
                    }
                    
                    return null;
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
                return null; 
            }

        } catch (BadLocationException e) {
            Util.THIS.debug(e);
        }

        return null;
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
        
        return ((GrammarManager)grammarBindingObj).getGrammar(300);
    }       
    
    // Delegate queriing to performer ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private List queryEntities(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {
        Enumeration res = getPerformer(doc, sup).queryEntities(helper.getContext().getCurrentPrefix());
        return translateEntityRefs(res);
    }
    
    private List queryElements(SyntaxQueryHelper helper, Document doc, XMLSyntaxSupport sup) {
        Enumeration res = getPerformer(doc, sup).queryElements(helper.getContext());
        return translateElements(res);
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
    
    private List translateElements(Enumeration els ) {
        List result = new ArrayList(13);
        while (els.hasMoreElements()) {
            GrammarResult next = (GrammarResult) els.nextElement();
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
     */
    private static List findStartTag(SyntaxNode text) {
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

        XMLResultItem res = new ElementResultItem(name);
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("    result=" + res);
        }

        List list = new ArrayList(1);
        list.add (res);

        return list;
    }
}
