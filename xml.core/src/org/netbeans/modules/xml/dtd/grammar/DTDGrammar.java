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

package org.netbeans.modules.xml.dtd.grammar;

import java.util.*;
import javax.swing.Icon;

import org.w3c.dom.*;

import org.openide.util.enum.*;

import org.netbeans.modules.xml.api.model.*;
import org.netbeans.modules.xml.spi.dom.*;

/**
 * Rather simple query implemetation based on DTD grammar.
 * It is produced by {@link DTDParser}.
 * Hints given by this grammar do not guarantee that valid XML document is created.
 *
 * @author  Petr Kuzel
 */
public class DTDGrammar implements GrammarQuery {
    
    // element name keyed
    Map elementDecls, attrDecls;
    
    // Map<elementName:String, ContentModel || null>
    // this map is filled asynchronously as it takes some time
    Map contentModels;
    
    // Map<elementname + " " + attributename, List<String>>
    Map attrEnumerations;
    
    Set entities, notations;
    
    /** Creates new DTDGrammar */
    DTDGrammar(Map elementDecls, Map contentModels, Map attrDecls, Map enums, Set entities, Set notations) {
        this.elementDecls = elementDecls;
        this.attrDecls = attrDecls;
        this.entities = entities;
        this.notations = notations;
        this.attrEnumerations = enums;
        this.contentModels = contentModels;
    }

    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @return list of <code>CompletionResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    public Enumeration queryEntities(String prefix) {
        if (entities == null) return EmptyEnumeration.EMPTY;
        
        QueueEnumeration list = new QueueEnumeration();
        Iterator it = entities.iterator();
        while ( it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                list.put(new MyEntityReference(next));
            }
        }

        // add well-know build-in entity names
        
        if ("lt".startsWith(prefix)) list.put(new MyEntityReference("lt"));
        if ("gt".startsWith(prefix)) list.put(new MyEntityReference("gt"));
        if ("apos".startsWith(prefix)) list.put(new MyEntityReference("apos"));
        if ("quot".startsWith(prefix)) list.put(new MyEntityReference("quot"));
        if ("amp".startsWith(prefix)) list.put(new MyEntityReference("amp"));
        
        return list;
    }
    
    /**
     * @stereotype query
     * @output list of results that can be queried on name, and attributes
     * @time Performs fast up to 300 ms.
     * @param ctx represents virtual attribute <code>Node</code> to be replaced. Its parent is a element node.
     * @return list of <code>CompletionResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
     *        Every list member represents one possibility.
     */
    public Enumeration queryAttributes(HintContext ctx) {
        if (attrDecls == null) return EmptyEnumeration.EMPTY;
        
        Element el = ((Attr)ctx).getOwnerElement();
        NamedNodeMap existingAttributes = el.getAttributes();
        if (el == null) return EmptyEnumeration.EMPTY;
        
        Set possibleAttributes = (Set) attrDecls.get(el.getTagName());
        if (possibleAttributes == null) return EmptyEnumeration.EMPTY;
        
        String prefix = ctx.getCurrentPrefix();
        
        QueueEnumeration list = new QueueEnumeration();
        Iterator it = possibleAttributes.iterator();
        while ( it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                if (existingAttributes.getNamedItem(next) == null) {
                    list.put(new MyAttr(next));
                }
            }
        }
        
        return list;
    }
    
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param ctx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
     * @return list of <code>CompletionResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes
     *        Every list member represents one possibility.
     */
    public Enumeration queryElements(HintContext ctx) {
        if (elementDecls == null) return EmptyEnumeration.EMPTY;;
        
        Node node = ((Node)ctx).getParentNode();        
        Set elements;
        
        if (node instanceof Element) {
            Element el = (Element) node;
            if (el == null) return EmptyEnumeration.EMPTY;;
            
            ContentModel model = (ContentModel) contentModels.get(el.getTagName());
            if (model != null) {                
                Enumeration en = model.whatCanFollow(new PreviousEnumeration(el, ctx));
                if (en == null) return EmptyEnumeration.EMPTY;
                return en;
            }
            // simple fallback
            elements = (Set) elementDecls.get(el.getTagName());
        } else if (node instanceof Document) {
            elements = elementDecls.keySet();  //??? should be one from DOCTYPE if exist
        } else {
            return EmptyEnumeration.EMPTY;
        }
                
        if (elements == null) return EmptyEnumeration.EMPTY;;
        String prefix = ctx.getCurrentPrefix();
        
        QueueEnumeration list = new QueueEnumeration();
        Iterator it = elements.iterator();
        while ( it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                list.put(new MyElement(next));
            }
        }
        
        return list;                        
    }
    
    /**
     * Allow to get names of <b>declared notations</b>.
     * @return list of <code>CompletionResult</code>s (NOTATION_NODEs)
     */
    public Enumeration queryNotations(String prefix) {
        if (notations == null) return EmptyEnumeration.EMPTY;;
        
        QueueEnumeration list = new QueueEnumeration();
        Iterator it = notations.iterator();
        while ( it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                list.put(new MyNotation(next));
            }
        }
        
        return list;
    }
       
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @input ctx represents virtual Node that has to be replaced (parent can be either Attr or Element), its own attributes does not name sense, it can be used just as the navigation start point.
     * @return list of <code>CompletionResult</code>s (TEXT_NODEs) that can be queried on name, and attributes.
     *        Every list member represents one possibility.
     */
    public Enumeration queryValues(HintContext ctx) {
        if (attrEnumerations.isEmpty()) return EmptyEnumeration.EMPTY;
        
        if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            String attributeName = ctx.getNodeName();
            Element element = ((Attr)ctx).getOwnerElement();
            if (element == null) return EmptyEnumeration.EMPTY;
            
            String elementName = element.getNodeName();
            String key = elementName + " " + attributeName;
            List values = (List) attrEnumerations.get(key);
            if (values == null) return EmptyEnumeration.EMPTY;
            
            String prefix = ctx.getCurrentPrefix();
            QueueEnumeration en = new QueueEnumeration();
            Iterator it = values.iterator();
            while (it.hasNext()) {
                String next = (String) it.next();
                if (next.startsWith(prefix)) {
                    en.put(new MyText(next));
                }
            }
            return en;
        }
        return EmptyEnumeration.EMPTY;
    }
    
    public java.awt.Component getCustomizer(HintContext ctx) {
        return null;
    }
    
    public boolean hasCustomizer(HintContext ctx) {
        return false;
    }

    public org.openide.nodes.Node.Property[] getProperties(HintContext ctx) {
        return null;
    }
    

    /** For debug purposes only. */
    public String toString() {
        return "DTD grammar";
//        return "DTDGrammar:\nelements: " + elementDecls.keySet() + "\nattributes:" + attrDecls.keySet() + "";
    }

    /**
     * Lazy evaluated enumeration of previous element siblings.
     */
    private static class PreviousEnumeration implements Enumeration {
        
        private final Node parent;
        private final Node last;
        private Node next;
        
        PreviousEnumeration(Node parent, Node last) {
            this.parent = parent;
            this.last = last;
            
            // init next
            
            next = parent.getFirstChild();
            while (next != null) {                
                if (next.getNodeType() == Node.ELEMENT_NODE) break;
                next = next.getNextSibling();
            }            
            if (next == last) next = null;
        }
        
        public boolean hasMoreElements() {
            return next != null;
        }

        public Object nextElement() {
            if (next == null) throw new NoSuchElementException();
            try {
                return next;
            } finally {
                while (next != null) {
                    next = next.getNextSibling();
                    if (next.getNodeType() == Node.ELEMENT_NODE) break;
                }
                if (next == last) next = null;
            }
        }
    }
    
    // Result classes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    
    private static abstract class AbstractResultNode extends AbstractNode implements GrammarResult {
        
        public Icon getIcon(int kind) {
            return null;
        }
        
        /**
         * @output provide additional information simplifiing decision
         */
        public String getDescription() {
            return getNodeName() + " desc";
        }
        
        /**
         * @output text representing name of suitable entity
         * //??? is it really needed
         */
        public String getText() {
            return getNodeName();
        }
        
        /**
         * @output name that is presented to user
         */
        public String getDisplayName() {
            return getNodeName() + " disp";
        }
        
    }
    
    private static class MyEntityReference extends AbstractResultNode implements EntityReference {
        
        private String name;
        
        MyEntityReference(String name) {
            this.name = name;
        }
        
        public short getNodeType() {
            return Node.ENTITY_REFERENCE_NODE;
        }
        
        public String getNodeName() {
            return name;
        }
                
    }
    
    private static class MyElement extends AbstractResultNode implements Element {
        
        private String name;
        
        MyElement(String name) {
            this.name = name;
        }
        
        public short getNodeType() {
            return Node.ELEMENT_NODE;
        }
        
        public String getNodeName() {
            return name;
        }
        
        public String getTagName() {
            return name;
        }
        
    }

    private static class MyAttr extends AbstractResultNode implements Attr {
        
        private String name;
        
        MyAttr(String name) {
            this.name = name;
        }
        
        public short getNodeType() {
            return Node.ATTRIBUTE_NODE;
        }
        
        public String getNodeName() {
            return name;
        }
        
        public String getName() {
            return name;                
        }

        public String getValue() {
            return null;  //??? what spec says
        }
        
        
    }

    private static class MyNotation extends AbstractResultNode implements Notation {
        
        private String name;
        
        MyNotation(String name) {
            this.name = name;
        }
        
        public short getNodeType() {
            return Node.NOTATION_NODE;
        }
        
        public String getNodeName() {
            return name;
        }
                        
    }
    
    private static class MyText extends AbstractResultNode implements Text {
        
        private String data;
        
        MyText(String data) {
            this.data = data;
        }
        
        public short getNodeType() {
            return Node.TEXT_NODE;
        }

        public String getNodeValue() {
            return getData();
        }
        
        public String getData() throws DOMException {
            return data;
        }

        public int getLength() {
            return data == null ? -1 : data.length();
        }    
    }
        
}
