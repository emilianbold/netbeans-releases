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

package org.netbeans.modules.xsl.grammar;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;

import org.netbeans.modules.xml.spi.model.*;
import org.openide.util.enum.*;

import org.w3c.dom.*;

/**
 *
 * @author  asgeir@dimonsoftware.com
 */
public class XSLGrammarQuery implements GrammarQuery{

    private final static Map elementDecls = new HashMap();
    
    private final static Map attrDecls = new HashMap();
    
    private final static Set resultElementAttr;
    
    private final static Object resultElements = new Object();
    
    private final static Set emptySet = new HashSet();
    
    private static Set template;
    
    private final static String spaceAtt = "xml:space";


    private static final String[] aCharInstructions = {"apply-templates", // NOI18N
        "call-template","apply-imports","for-each","value-of", // NOI18N
        "copy-of","number","choose","if","text","copy", // NOI18N
        "variable","message","fallback"}; // NOI18N
        
    private static final String[] aInstructionsExtra = {"processing-instruction", // NOI18N
        "comment","element","attribute"}; // NOI18N
        
    private static final String[] aTopLevel = {"import","include","strip-space", // NOI18N
        "preserve-space","output","key","decimal-format","attribute-set", // NOI18N
        "variable","param","template","namespace-alias"}; // NOI18N
    
    private static final String[] aTopLevelAttr = {"extension-element-prefixes",
        "exclude-result-prefixes","id","version",spaceAtt};
        
    // Those attributes are in the xsl namespace
    private static final String[] aResultElementsAttr = {"extension-element-prefixes",
        "exclude-result-prefixes","use-attribute-sets","version"};
        
    static {
        Set tmpSet;
        
        Set charInstructions = new HashSet(Arrays.asList(aCharInstructions));
      
        Set instructions = new HashSet(charInstructions);
        instructions.addAll(Arrays.asList(aInstructionsExtra));
        
        Set charTemplate = charInstructions; // We don't care about PCDATA
        
        template = new HashSet(instructions);
        template.add(resultElements);
        
        Set topLevel = new HashSet(Arrays.asList(aTopLevel));
        
        Set topLevelAttr = new HashSet(Arrays.asList(aTopLevelAttr));
        resultElementAttr = new HashSet(Arrays.asList(aResultElementsAttr));
                
        // xsl:stylesheet
        elementDecls.put("stylesheet", topLevel);
        attrDecls.put("stylesheet", topLevelAttr);

        // xsl:transform
        elementDecls.put("transform", topLevel);
        attrDecls.put("transform", topLevelAttr);
        
        // xsl:import
        elementDecls.put("import", emptySet);
        attrDecls.put("import", new HashSet(Arrays.asList(new String[]{"href"})));

        // xxsl:include
        elementDecls.put("include", emptySet);
        attrDecls.put("include", new HashSet(Arrays.asList(new String[]{"href"})));

        // xsl:strip-space
        elementDecls.put("strip-space", emptySet);
        attrDecls.put("strip-space", new HashSet(Arrays.asList(new String[]{"elements"})));

        // xsl:preserve-space
        elementDecls.put("preserve-space", emptySet);
        attrDecls.put("preserve-space", new HashSet(Arrays.asList(new String[]{"elements"})));

        // xsl:output
        elementDecls.put("output", emptySet);
        attrDecls.put("output", new HashSet(Arrays.asList(new String[]{"method",
            "version","encoding","omit-xml-declaration","standalone","doctype-public",
            "doctype-system","cdata-section-elements","indent","media-type"})));

        // xsl:key  
        elementDecls.put("key", emptySet);
        attrDecls.put("key", new HashSet(Arrays.asList(new String[]{"name","match","use"})));

        // xsl:decimal-format
        elementDecls.put("decimal-format", emptySet);
        attrDecls.put("decimal-format", new HashSet(Arrays.asList(new String[]{"name",
            "decimal-separator","grouping-separator","infinity","minus-sign","NaN",
            "percent","per-mille","zero-digit","digit","pattern-separator"})));

        // xsl:namespace-alias
        elementDecls.put("namespace-alias", emptySet);
        attrDecls.put("namespace-alias", new HashSet(Arrays.asList(new String[]{
            "stylesheet-prefix","result-prefix"})));

        // xsl:template
        tmpSet = new HashSet(instructions);
        tmpSet.add(resultElements);
        tmpSet.add("param");
        elementDecls.put("template", tmpSet);
        attrDecls.put("template", new HashSet(Arrays.asList(new String[]{
            "match","name","priority","mode",spaceAtt})));

        // xsl:value-of
        elementDecls.put("value-of", emptySet);
        attrDecls.put("value-of", new HashSet(Arrays.asList(new String[]{
            "select","disable-output-escaping"})));

        // xsl:copy-of
        elementDecls.put("copy-of", emptySet);
        attrDecls.put("copy-of", new HashSet(Arrays.asList(new String[]{"select"})));

        // xsl:number
        elementDecls.put("number", emptySet);
        attrDecls.put("number", new HashSet(Arrays.asList(new String[]{
            "level","count","from","value","format","lang","letter-value",
            "grouping-separator","grouping-size"})));

        // xsl:apply-templates
        elementDecls.put("apply-templates", new HashSet(Arrays.asList(new String[]{
            "sort","with-param"})));
        attrDecls.put("apply-templates", new HashSet(Arrays.asList(new String[]{
            "select","mode"})));

        // xsl:apply-imports
        elementDecls.put("apply-imports", emptySet);
        attrDecls.put("apply-imports", emptySet);

        // xsl:for-each
        tmpSet = new HashSet(instructions);
        tmpSet.add(resultElements);
        tmpSet.add("sort");
        elementDecls.put("for-each", tmpSet);
        attrDecls.put("for-each", new HashSet(Arrays.asList(new String[]{
            "select",spaceAtt})));
            
        // xsl:sort
        elementDecls.put("sort", emptySet);
        attrDecls.put("sort", new HashSet(Arrays.asList(new String[]{
            "select","lang","data-type","order","case-order"})));
            
        // xsl:if
        elementDecls.put("if", template);
        attrDecls.put("if", new HashSet(Arrays.asList(new String[]{"test",spaceAtt})));
            
        // xsl:choose
        elementDecls.put("choose", new HashSet(Arrays.asList(new String[]{
            "when","otherwise"})));
        attrDecls.put("choose", new HashSet(Arrays.asList(new String[]{spaceAtt})));
                        
        // xsl:when
        elementDecls.put("when", template);
        attrDecls.put("when", new HashSet(Arrays.asList(new String[]{
            "test",spaceAtt})));
                        
        // xsl:otherwise
        elementDecls.put("otherwise", template);
        attrDecls.put("otherwise", new HashSet(Arrays.asList(new String[]{spaceAtt})));
                        
        // xsl:attribute-set
        elementDecls.put("sort", new HashSet(Arrays.asList(new String[]{"attribute"})));
        attrDecls.put("attribute-set", new HashSet(Arrays.asList(new String[]{
            "name","use-attribute-sets"})));
                        
        // xsl:call-template
        elementDecls.put("call-template", new HashSet(Arrays.asList(new String[]{"with-param"})));
        attrDecls.put("call-template", new HashSet(Arrays.asList(new String[]{"name"})));
                        
        // xsl:with-param
        elementDecls.put("with-param", template);
        attrDecls.put("with-param", new HashSet(Arrays.asList(new String[]{
            "name","select"})));
                        
        // xsl:variable
        elementDecls.put("variable", template);
        attrDecls.put("variable", new HashSet(Arrays.asList(new String[]{
            "name","select"})));
                        
        // xsl:param
        elementDecls.put("param", template);
        attrDecls.put("param", new HashSet(Arrays.asList(new String[]{
            "name","select"})));
                        
        // xsl:text
        elementDecls.put("text", emptySet);
        attrDecls.put("text", new HashSet(Arrays.asList(new String[]{
            "disable-output-escaping"})));
                        
        // xsl:processing-instruction
        elementDecls.put("processing-instruction", charTemplate);
        attrDecls.put("processing-instruction", new HashSet(Arrays.asList(new String[]{
            "name",spaceAtt})));
                        
        // xsl:element
        elementDecls.put("element", template);
        attrDecls.put("element", new HashSet(Arrays.asList(new String[]{
            "name","namespace","use-attribute-sets",spaceAtt})));
                        
        // xsl:attribute
        elementDecls.put("attribute", charTemplate);
        attrDecls.put("attribute", new HashSet(Arrays.asList(new String[]{
            "name","namespace",spaceAtt})));
                        
        // xsl:comment
        elementDecls.put("comment", charTemplate);
        attrDecls.put("comment", new HashSet(Arrays.asList(new String[]{spaceAtt})));
                        
        // xsl:copy
        elementDecls.put("copy", template);
        attrDecls.put("copy", new HashSet(Arrays.asList(new String[]{
            spaceAtt,"use-attribute-sets"})));
                        
        // xsl:message
        elementDecls.put("message", template);
        attrDecls.put("message", new HashSet(Arrays.asList(new String[]{
            spaceAtt,"terminate"})));
                        
        // xsl:fallback
        elementDecls.put("fallback", template);
        attrDecls.put("fallback", new HashSet(Arrays.asList(new String[]{spaceAtt})));
                        
    }
    
    private String xslNamespacePrefix;
       
    /** Creates a new instance of XSLGrammarQuery */
    public XSLGrammarQuery(String xslNamespacePrefix) {
        this.xslNamespacePrefix = xslNamespacePrefix;
    }
    
    /**
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @param ctx represents virtual element Node that has to be replaced, its own attributes does not name sense, it can be used just as the navigation start point.
     * @return enumeration of <code>GrammarResult</code>s (ELEMENT_NODEs) that can be queried on name, and attributes
     *         Every list member represents one possibility.  
     */
    public Enumeration queryElements(HintContext ctx) {        
        Node node = ((Node)ctx).getParentNode();        
        Set elements;
        
        String xslNamespaceWithColon = xslNamespacePrefix + ":";
        if (node instanceof Element) {
            Element el = (Element) node;
            if (el.getTagName().startsWith(xslNamespaceWithColon)) {
                String parentName = el.getTagName().substring(xslNamespaceWithColon.length());
                elements = (Set) elementDecls.get(parentName);
            } else {
                // Children of result elements should always be the template set
                elements = template;
            }
         } else if (node instanceof Document) {
            elements = elementDecls.keySet();
        } else {
            return EmptyEnumeration.EMPTY;
        }
                        
        if (elements == null) return EmptyEnumeration.EMPTY;
        String prefix = ctx.getCurrentPrefix();
        QueueEnumeration list = new QueueEnumeration();
                
        if (prefix.startsWith(xslNamespaceWithColon) || xslNamespaceWithColon.startsWith(prefix)) {
            // Add XSL elements
//            boolean namespaceOrLessEntered = prefix.length() <= xslNamespaceWithColon.length();
            Iterator it = elements.iterator();
            while ( it.hasNext()) {
                Object next = it.next();
                if (next instanceof String) {
                    String nextText = xslNamespaceWithColon + (String)next;
                    if (nextText.startsWith(prefix)) {
                        list.put(new MyElement(nextText));
                    }
                }
            }
        }
        
        if (elements.contains(resultElements)) {
            Set dummyResultSet = new HashSet(Arrays.asList(new String[]{
                        "html","body","head","p","table","tr","td","br"}));        
            Iterator it = dummyResultSet.iterator();
            while ( it.hasNext()) {
                Object next = it.next();
                if (((String)next).startsWith(prefix)) {
                    list.put(new MyElement((String)next));
                }
             }
        }
        
        return list;                        
    }

    /**
     * @stereotype query
     * @output list of results that can be queried on name, and attributes
     * @time Performs fast up to 300 ms. 
     * @param ctx represents virtual attribute <code>Node</code> to be replaced. Its parent is a element node.
     * @return enumeration of <code>GrammarResult</code>s (ATTRIBUTE_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.  
     */
    public Enumeration queryAttributes(HintContext ctx) {
        Element el = ((Attr)ctx).getOwnerElement();
        if (el == null) return EmptyEnumeration.EMPTY;
        NamedNodeMap existingAttributes = el.getAttributes();
        String xslNamespaceWithColon = xslNamespacePrefix + ":";
        
        Set possibleAttributes;
        if (el.getTagName().startsWith(xslNamespaceWithColon)) {
            // Attributes of XSL element
            possibleAttributes = (Set) attrDecls.get(el.getTagName().substring(xslNamespaceWithColon.length()));
        } else {
            // Attributes of Result element
            possibleAttributes = new HashSet(resultElementAttr.size());
            Iterator it = resultElementAttr.iterator();
            while ( it.hasNext()) {
                possibleAttributes.add(xslNamespaceWithColon + (String) it.next());
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
     * Return options for value at given context.
     * It could be also used for completing of value parts such as Ant or XSLT property names (how to trigger it?).
     * @semantics Navigates through read-only Node tree to determine context and provide right results.
     * @postconditions Let ctx unchanged
     * @time Performs fast up to 300 ms.
     * @stereotype query
     * @input ctx represents virtual Node that has to be replaced (parent can be either Attr or Element), its own attributes does not name sense, it can be used just as the navigation start point.
     * @return enumeration of <code>GrammarResult</code>s (TEXT_NODEs) that can be queried on name, and attributes.
     *         Every list member represents one possibility.  
     */
    public Enumeration queryValues(HintContext ctx) {
       return EmptyEnumeration.EMPTY;
    }

    /**
     * Allow to get names of <b>parsed general entities</b>.
     * @return enumeration of <code>GrammarResult</code>s (ENTITY_REFERENCE_NODEs)
     */
    public Enumeration queryEntities(String prefix) {
       return EmptyEnumeration.EMPTY;
    }

    /**
     * Allow to get names of <b>declared notations</b>.
     * @return enumeration of <code>GrammarResult</code>s (NOTATION_NODEs)
     */    
    public Enumeration queryNotations(String prefix) {
        return EmptyEnumeration.EMPTY;
    }
    
    // Result classes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    private static abstract class AbstractResultNode implements Node, GrammarResult {
        
        private static final String domExText = "This read-only implementation supports DOM level 1 Core and XML module.";  //NOI18N;
        
        public String getNodeName() {
            return null;
        }

        /**
         * @return false
         */
        public boolean isSupported(String feature, String version) {
            return "1.0".equals(version);
        }

        public void setPrefix(String str) throws org.w3c.dom.DOMException {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public String getPrefix() {
            return null;    // some client determines DOM1 by NoSuchMethodError
        }

        public org.w3c.dom.Node getPreviousSibling() {
            return null;
        }

        //!!! rather abstract to force all to reimplement
        public abstract short getNodeType();

        public org.w3c.dom.Document getOwnerDocument() {
            // let it be the first item
            return null;
        }

        public org.w3c.dom.Node replaceChild(org.w3c.dom.Node node, org.w3c.dom.Node node1) throws org.w3c.dom.DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public org.w3c.dom.Node cloneNode(boolean param) {
            return (Node) this;  //we are immutable, only problem with references may appear
        }

        public org.w3c.dom.Node getNextSibling() {
            return null;
        }

        public org.w3c.dom.Node insertBefore(org.w3c.dom.Node node, org.w3c.dom.Node node1) throws org.w3c.dom.DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public String getNamespaceURI() {
            return null;    // some client determines DOM1 by NoSuchMethodError
        }

        public org.w3c.dom.NamedNodeMap getAttributes() {
            return null;
        }

        public org.w3c.dom.NodeList getChildNodes() {       
            return null;
        }

        public String getNodeValue() throws org.w3c.dom.DOMException {
            // attribute, text, pi data
            return null;
        }

        public org.w3c.dom.Node appendChild(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public String getLocalName() {
            return null;    // some client determines DOM1 by NoSuchMethodError
        }

        public org.w3c.dom.Node getParentNode() {
            return null;
        }

        public void setNodeValue(String str) throws org.w3c.dom.DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public org.w3c.dom.Node getLastChild() {
            return null;
        }

        public boolean hasAttributes() {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public void normalize() {
            // ignore
        }

        public org.w3c.dom.Node removeChild(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        /**
         * @return false
         */
        public boolean hasChildNodes() {
            return false;
        }

        /**
         * @return null
         */
        public org.w3c.dom.Node getFirstChild() {
            return null;
        }


        // A bonus Element interface ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`

        public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public String getAttributeNS(String namespaceURI, String localName) {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public String getAttribute(String name) {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public Attr getAttributeNode(String name) {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public boolean hasAttribute(String name) {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public String getTagName() {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
       }

        public Attr getAttributeNodeNS(String namespaceURI, String localName) {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public void removeAttribute(String name) throws DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public void setAttribute(String name, String value) throws DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public NodeList getElementsByTagName(String name) {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public boolean hasAttributeNS(String namespaceURI, String localName) {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public Attr setAttributeNode(Attr newAttr) throws DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }


        // A bonus Attr implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        public boolean getSpecified() {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public String getName() {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public Element getOwnerElement() {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public void setValue(String value) throws DOMException {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        public String getValue() {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }

        // Notation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        public String getPublicId() {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }        

        public String getSystemId() {
           throw new DOMException(DOMException.NOT_SUPPORTED_ERR, domExText);
        }
        
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
}