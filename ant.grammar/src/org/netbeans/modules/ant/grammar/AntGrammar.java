/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.grammar;

import java.util.*;
import javax.swing.Icon;
import org.apache.tools.ant.module.api.IntrospectedInfo;

import org.w3c.dom.*;

import org.openide.util.enum.*;

import org.netbeans.modules.xml.api.model.*;
import org.netbeans.modules.xml.spi.dom.*;

/**
 * Rather simple query implemetation based on static Ant introspection info.
 * Hints given by this grammar cannot guarantee that valid XML document is created.
 *
 * @author  Petr Kuzel
 */
class AntGrammar implements GrammarQuery {
        
    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @return list of <code>CompletionResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    public Enumeration queryEntities(String prefix) {
        QueueEnumeration list = new QueueEnumeration();
        
        // add well-know build-in entity names
        
        if ("lt".startsWith(prefix)) list.put(new MyEntityReference("lt"));
        if ("gt".startsWith(prefix)) list.put(new MyEntityReference("gt"));
        if ("apos".startsWith(prefix)) list.put(new MyEntityReference("apos"));
        if ("quot".startsWith(prefix)) list.put(new MyEntityReference("quot"));
        if ("amp".startsWith(prefix)) list.put(new MyEntityReference("amp"));
        
        return list;
    }
    
    private static String getTaskClassFor(String elementName) {
        Map defs = getAntGrammar().getDefs("task");
        return (String) defs.get(elementName);
    }

    private static String getTypeClassFor(String elementName) {
        Map defs = getAntGrammar().getDefs("type");
        return (String) defs.get(elementName);        
    }
    
    private static IntrospectedInfo getAntGrammar() {
        return IntrospectedInfo.getDefaults();
        
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
        
        Element ownerElement = null;
        // Support both versions of GrammarQuery contract
        if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            ownerElement = ((Attr)ctx).getOwnerElement();
        } else if (ctx.getNodeType() == Node.ELEMENT_NODE) {
            ownerElement = (Element) ctx;
        }
        if (ownerElement == null) return EmptyEnumeration.EMPTY;
        
        NamedNodeMap existingAttributes = ownerElement.getAttributes();        
        List possibleAttributes = null;
        // XXX This is wrong. Should be based on parents up to document list,
        // not solely on element name. That would permit it to correctly handle
        // nested elements, etc.
        String elementName = ownerElement.getTagName();
        
        if ("project".equals(elementName)) {
            possibleAttributes = new LinkedList();
            possibleAttributes.add("default");
            possibleAttributes.add("name");
            possibleAttributes.add("basedir");
        } else if ("target".equals(elementName)) {
            possibleAttributes = new LinkedList();
            possibleAttributes.add("name");
            possibleAttributes.add("depends");
            possibleAttributes.add("description");
            possibleAttributes.add("if");
            possibleAttributes.add("unless");
        } else {
            boolean type = false;
            String clazz = getTaskClassFor(elementName);
            if (clazz == null) {
                clazz = getTypeClassFor(elementName);
                type = true;
            }                        
            if (clazz != null) {
                possibleAttributes = new LinkedList();
                if (type) {
                    possibleAttributes.add("id");
                }
                possibleAttributes.addAll(new TreeSet(getAntGrammar().getAttributes(clazz).keySet()));
                if (!type) {
                    possibleAttributes.add("id");
                }
                if (!type) {
                    // Currently IntrospectedInfo includes this in the props for a type,
                    // though it excludes it for tasks. So for now add it explicitly
                    // only to tasks.
                    possibleAttributes.add("description");
                }
                if (!type) {
                    possibleAttributes.add("taskname");
                }
            }
        }
        
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
        
        Node parent = ((Node)ctx).getParentNode();
        if (parent == null) return EmptyEnumeration.EMPTY;
        
        List elements = null;
        
        if (parent.getNodeType() == ctx.ELEMENT_NODE) {
            String parentName = ((Element) parent).getTagName();

            if ("project".equals(parentName)) {
                elements = new LinkedList();
                elements.add("target");
                elements.add("property");
                elements.add("taskdef");
                elements.add("typedef");
                elements.add("description");
                elements.addAll(new TreeSet(getAntGrammar().getDefs("type").keySet()));
            } else if ("target".equals(parentName)) {
                elements = new ArrayList(new TreeSet(getAntGrammar().getDefs("task").keySet()));
            } else {
                String clazz = getTaskClassFor(parentName);
                if (clazz == null) {
                    clazz = getTypeClassFor(parentName);
                }                                
                if (clazz != null) {
                    elements = new ArrayList(new TreeSet(getAntGrammar().getElements(clazz).keySet()));
                }
            }
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
        return EmptyEnumeration.EMPTY;
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
        return EmptyEnumeration.EMPTY;
    }

    // return defaults, no way to query them
    public GrammarResult queryDefault(final HintContext ctx) {
        return null;
    }
    
    // it is not yet implemented
    public boolean isAllowed(Enumeration en) {
        return true;
    }
    
    // customizers section ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
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
        return "Ant grammar";                                                   // NOI18N
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
