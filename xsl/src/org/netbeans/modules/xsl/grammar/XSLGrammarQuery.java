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

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;

import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.api.model.*;
import org.openide.util.enum.*;
import org.netbeans.modules.xml.dtd.grammar.*;
import org.netbeans.modules.xml.spi.dom.*;

import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class implements code completion for XSL transformation files.
 * XSL elements in the completion are hardcoded from the XSLT spec, but the
 * result elements are gathered from the "doctype-public" and "doctype-system"
 * attributes of the xsl:output element.
 *
 * @author  asgeir@dimonsoftware.com
 */
public class XSLGrammarQuery implements GrammarQuery{

    /** Contains a mapping from XSL namespace element names to set of names of 
     * allowed XSL children. Neither the element name keys nor the names in the
     * value set should contain the namespace prefix.
     */
    private final static Map elementDecls = new HashMap();
    
    /** Contains a mapping from XSL namespace element names to set of names of
     * allowed XSL attributes for that element.  The element name keys should 
     * not contain the namespace prefix.
     */
    private final static Map attrDecls = new HashMap();
    
    /** A Set of XSL attributes which should be allowd for result elements*/
    private final static Set resultElementAttr;
    
    /** An object which indicates that result element should be allowed in a element Set */
    private final static Object resultElements = new Object();
        
    /** A Set of elements which should be allowed at template level in XSL stylesheet */ 
    private static Set template;
           
    /** A list of prefixes using the "http://www.w3.org/1999/XSL/Transform" namespace
     * defined in the context XSL document.  The first prefix in the list is the actual XSL
     * transformation prefix, which is normally defined on the xsl:stylesheet element.
     */
    private List prefixList = new LinkedList();
    
    /** A GrammarQuery for the result elements created for the doctype-public" and 
     * "doctype-system" attributes of the xsl:output element.*/
    private GrammarQuery resultGrammarQuery;
    
    /** The value of the system identifier of the DTD which was used when
     * resultGrammarQuery was previously created */
    private String lastDoctypeSystem;
    
    /** The value of the public identifier of the DTD which was used when
     * resultGrammarQuery was previously created */
    private String lastDoctypePublic;
    
    // Initialization of the static members above
    static {
        // Commonly used variables
        Set emptySet = new HashSet();
        String spaceAtt = "xml:space";
        Set tmpSet;
        
        ////////////////////////////////////////////////
        // Initialize common sets
        
        Set charInstructions = new HashSet(Arrays.asList(new String[]{"apply-templates", // NOI18N
            "call-template","apply-imports","for-each","value-of", // NOI18N
            "copy-of","number","choose","if","text","copy", // NOI18N
            "variable","message","fallback"}));
      
        Set instructions = new HashSet(charInstructions);
        instructions.addAll(Arrays.asList(new String[]{"processing-instruction", // NOI18N
            "comment","element","attribute"}));
        
        Set charTemplate = charInstructions; // We don't care about PCDATA
        
        template = new HashSet(instructions);
        template.add(resultElements);
        
        Set topLevel = new HashSet(Arrays.asList(new String[]{"import","include","strip-space", // NOI18N
            "preserve-space","output","key","decimal-format","attribute-set", // NOI18N
            "variable","param","template","namespace-alias"}));
        
        Set topLevelAttr = new HashSet(Arrays.asList(new String[]{"extension-element-prefixes",
            "exclude-result-prefixes","id","version",spaceAtt}));
            
        resultElementAttr = new HashSet(Arrays.asList(new String[]{"extension-element-prefixes",
            "exclude-result-prefixes","use-attribute-sets","version"}));
         
        ////////////////////////////////////////////////
        // Add items to elementDecls and attrDecls maps
            
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
           
    /** Creates a new instance of XSLGrammarQuery */
    public XSLGrammarQuery()     {
    }

////////////////////////////////////////////////////////////////////////////////
// GrammarQuery interface fulfillment
    
    public Enumeration queryElements(HintContext ctx) {        
        Node node = ((Node)ctx).getParentNode();        
        
        String prefix = ctx.getCurrentPrefix();
        QueueEnumeration list = new QueueEnumeration();
                
        if (node instanceof Element) {
            Element el = (Element) node;
            updateProperies(el);
            if (prefixList.size() == 0) return EmptyEnumeration.EMPTY;
            
            String firstXslPrefixWithColon = prefixList.get(0) + ":";
            Set elements;
            if (el.getTagName().startsWith(firstXslPrefixWithColon)) {
                String parentName = el.getTagName().substring(firstXslPrefixWithColon.length());
                elements = (Set) elementDecls.get(parentName);
            } else {
                // Children of result elements should always be the template set
                elements = template;
            }
            
            // First we add the Result elements
            if (elements.contains(resultElements) && resultGrammarQuery != null) {
                ResultHintContext resultHintContext = new ResultHintContext(ctx, firstXslPrefixWithColon, null);
                Enumeration resultEnum = resultGrammarQuery.queryElements(resultHintContext);
                while (resultEnum.hasMoreElements()) {
                    list.put(resultEnum.nextElement());
                }
            }

            // Then we add the XSLT elements of the first prefix (normally of the stylesheet node).
            addXslElementsToEnum(list, elements, prefixList.get(0) + ":", prefix);
            
            // Finally we add xsl namespace elements with other prefixes than the first one
            for (int prefixInd = 1; prefixInd < prefixList.size(); prefixInd++) {
                String curPrefix = (String)prefixList.get(prefixInd) + ":";
                Node curNode = el;
                String curName = null;
                while(curNode != null && null != (curName = curNode.getNodeName()) && !curName.startsWith(curPrefix)) {
                    curNode = curNode.getParentNode();
                }
                
                if (curName == null) {
                    // This must be the document node
                    addXslElementsToEnum(list, elementDecls.keySet(), curPrefix, prefix);
                } else {
                    String parentName = curName.substring(curPrefix.length());
                    elements = (Set) elementDecls.get(parentName);
                    addXslElementsToEnum(list, elements, curPrefix, prefix);
                }
            }
            
         } else if (node instanceof Document) {
            addXslElementsToEnum(list, elementDecls.keySet(), prefixList.get(0) + ":", prefix);
        } else {
            return EmptyEnumeration.EMPTY;
        }        
              
        return list;                        
    }
    
    public Enumeration queryAttributes(HintContext ctx) {
        Element el = ((Attr)ctx).getOwnerElement();
        if (el == null) return EmptyEnumeration.EMPTY;
        String elTagName = el.getTagName();
        NamedNodeMap existingAttributes = el.getAttributes();
         
        updateProperies(el);
        
       
        String curXslPrefix = null;
        for (int ind = 0; ind < prefixList.size(); ind++) {
            if (elTagName.startsWith((String)prefixList.get(ind))){
                curXslPrefix = (String)prefixList.get(ind) + ":";
                break;
            }
        }
                
        Set possibleAttributes;
        if (curXslPrefix != null) {
            // Attributes of XSL element
            possibleAttributes = (Set) attrDecls.get(el.getTagName().substring(curXslPrefix.length()));
        } else {
            // XSL Attributes of Result element
            possibleAttributes = new HashSet(resultElementAttr.size());
            if (prefixList.size() > 0) {
                Iterator it = resultElementAttr.iterator();
                while ( it.hasNext()) {
                    possibleAttributes.add((String)prefixList.get(0) + ":" + (String) it.next());
                }
            }
        }
        if (possibleAttributes == null) return EmptyEnumeration.EMPTY;
        
        String prefix = ctx.getCurrentPrefix();
        
        QueueEnumeration list = new QueueEnumeration();
        
        if (resultGrammarQuery != null) {
            Enumeration enum = resultGrammarQuery.queryAttributes(ctx);
            while(enum.hasMoreElements()) {
                GrammarResult resNode = (GrammarResult)enum.nextElement();
                if (!possibleAttributes.contains(resNode.getNodeName())) {
                    list.put(resNode);
                }
            }
        }
        
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

    public Enumeration queryValues(HintContext ctx) {
       return EmptyEnumeration.EMPTY;
    }

    public Enumeration queryEntities(String prefix) {
       return EmptyEnumeration.EMPTY;
    }

    public Enumeration queryNotations(String prefix) {
        return EmptyEnumeration.EMPTY;
    }
    
////////////////////////////////////////////////////////////////////////////////
// Private helper methods    
    
    /**
     * @param enum the Enumeration which the element should be added to
     * @param set a set containing strings which should be added (with prefix) to the enum
     * @param namespacePrefix a prefix at the form "xsl:" which should be added in front 
     *          of the names in the set.
     * @param startWith Elements should only be added to enum if they start with this string
     */
    private void addXslElementsToEnum(QueueEnumeration enum, Set set, String namespacePrefix, String startWith) {
        if (startWith.startsWith(namespacePrefix) || namespacePrefix.startsWith(startWith)) {
            Iterator it = set.iterator();
            while ( it.hasNext()) {
                Object next = it.next();
                if (next instanceof String) {
                    String nextText = namespacePrefix + (String)next;
                    if (nextText.startsWith(startWith)) {
                        enum.put(new MyElement(nextText));
                    }
                }
            }
        }
    }
    
    /**
     * This method traverses up the document tree, investigates it and updates 
     * prefixList, resultGrammarQuery, lastDoctypeSystem or lastDoctypePublic
     * members if necessery.
     * @param curNode the node which from wich the traversing should start.
     */
    private void updateProperies(Node curNode) {
        prefixList.clear();
        
        // Traverse up the documents tree
        Node rootNode = curNode;
        while (curNode != null && !(curNode instanceof Document)) {
            
            // Update the xsl namespace prefix list
            NamedNodeMap attributes = curNode.getAttributes();
            for (int ind = 0; ind < attributes.getLength(); ind++) {
                Attr attr = (Attr)attributes.item(ind);
                String attrName = attr.getName();
                if (attrName != null && attrName.startsWith("xmlns:")) {
                    if (attr.getValue().equals("http://www.w3.org/1999/XSL/Transform")) {
                        prefixList.add(0, attrName.substring(6));
                    }
                }
            }
            
            
            rootNode = curNode;
            curNode = rootNode.getParentNode();
        }
        
        boolean outputFound = false;
        if (prefixList.size() > 0) {
            String outputElName = (String)prefixList.get(0) + ":output";
            Node childOfRoot = rootNode.getFirstChild();
            while (childOfRoot != null) {
                String childNodeName = childOfRoot.getNodeName();
                if (childNodeName != null && childNodeName.equals(outputElName)) {
                    Element outputEl = (Element)childOfRoot;
                    String curDoctypePublic = outputEl.getAttribute("doctype-public");
                    String curDoctypeSystem = outputEl.getAttribute("doctype-system");
                    
                    if (curDoctypePublic != null && !curDoctypePublic.equals(lastDoctypePublic) || 
                      curDoctypePublic == null && lastDoctypePublic != null ||
                      curDoctypeSystem != null && !curDoctypeSystem.equals(lastDoctypeSystem) || 
                      curDoctypeSystem == null && lastDoctypeSystem != null) {
                        setOutputDoctype(curDoctypePublic, curDoctypeSystem);
                    }
                    
                    outputFound = true;
                    break;
                }
                childOfRoot = childOfRoot.getNextSibling();
            }
        }
        
        if (!outputFound) {
            setOutputDoctype(null, null);
        }
    }

    /**
     * Updates resultGrammarQuery by parsing the DTD specified by publicId and 
     * systemId. lastDoctypeSystem and lastDoctypePublic are assigned to the new values.
     * @param publicId the public identifier of the DTD
     * @param publicId the system identifier of the DTD
     */
    private void setOutputDoctype(String publicId, String systemId) {
        lastDoctypePublic = publicId;
        lastDoctypeSystem = systemId;
        
        if (publicId == null && systemId == null) {
            resultGrammarQuery = null;
            return;
        }
        
        UserCatalog catalog = UserCatalog.getDefault();
        EntityResolver resolver = catalog.getEntityResolver();
        InputSource inputSource = null;
//        System.out.println("setOutputDoctype.resolver: " + resolver);
        if (resolver != null) {
            try {
                inputSource = resolver.resolveEntity(publicId, systemId);
            } catch(SAXException e) {
//                System.out.println("setOutputDoctype.SAXException: " + e.getMessage());
            } catch(IOException e) {
//                System.out.println("setOutputDoctype.IOException: " + e.getMessage());
            } // Will be handled below
        }
        
        if (inputSource == null) {
            try {
                java.net.URL url = new java.net.URL(systemId);
                inputSource = new InputSource(url.openStream());
                inputSource.setPublicId(publicId);
                inputSource.setSystemId(systemId);
            } catch(IOException e) {
//                System.out.println("setOutputDoctype.IOException: " + e.getMessage());
                resultGrammarQuery = null;
                return;
            }
        }

        DTDParser dtdParser = new DTDParser(true);
        resultGrammarQuery = dtdParser.parse(inputSource);
        
    }
    
////////////////////////////////////////////////////////////////////////////////
// Private helper classes    
    
    private class ResultHintContext extends ResultNode implements HintContext {
        private String currentPrefix;
        
        public ResultHintContext(HintContext peer, String ignorePrefix, String onlyUsePrefix) {
            super(peer, ignorePrefix, onlyUsePrefix);
            currentPrefix = peer.getCurrentPrefix();
        }
        
        public String getCurrentPrefix() {
            return currentPrefix;
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

}