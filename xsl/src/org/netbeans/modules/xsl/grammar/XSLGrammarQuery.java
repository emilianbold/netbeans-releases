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
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import org.apache.xpath.XPathAPI;

import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.api.model.*;
import org.openide.util.enum.*;
import org.netbeans.modules.xml.dtd.grammar.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.modules.xsl.cookies.ScenarioCookie;
import org.netbeans.modules.xsl.scenario.XSLScenario;
import org.openide.TopManager;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.FolderLookup;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

import org.w3c.dom.*;
import org.w3c.dom.NodeList;
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
    
    private DataObject dataObject;
    
    private ScenarioCookie scenarioCookie;
    
    /** Contains a mapping from XSL namespace element names to set of names of
     * allowed XSL children. Neither the element name keys nor the names in the
     * value set should contain the namespace prefix.
     */
    private static Map elementDecls;
    
    /** Contains a mapping from XSL namespace element names to set of names of
     * allowed XSL attributes for that element.  The element name keys should
     * not contain the namespace prefix.
     */
    private static Map attrDecls;
    
    /** A Set of XSL attributes which should be allowd for result elements*/
    private static Set resultElementAttr;
    
    /** An object which indicates that result element should be allowed in a element Set */
    private static String resultElements = "RESULT_ELEMENTS_DUMMY_STRING"; // NOI18N
    
    /** A Set of elements which should be allowed at template level in XSL stylesheet */
    private static Set template;
    
    /** Contains a mapping from XSL namespace element names to an attribute name which
     * should contain XPath expression.  The element name keys should
     * not contain the namespace prefix.
     */
    private static Map exprAttributes;
    
    /** A set containing all functions allowed in XSLT */
    private static Set xslFunctions;
    
    /** A set containing XPath axes */
    private static Set xpathAxes;
    
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
    
    // we cannot parse SGML DTD for HTML, let emulate it by XHTML DTD
    private final static String XHTML_PUBLIC_ID = "-//W3C//DTD XHTML 1.0 Transitional//EN";
    
    /** Folder which stores instances of custom external XSL customizers */
    private static final String CUSTOMIZER_FOLDER = "Plugins/XML/XSLCustomizer";// NOI18N
    
    private XSLCustomizer customizer = null;
    
    /** Creates a new instance of XSLGrammarQuery */
    public XSLGrammarQuery(DataObject dataObject) {
        this.dataObject = dataObject;
        scenarioCookie = (ScenarioCookie)dataObject.getCookie(ScenarioCookie.class);
    }
    
    //////////////////////////////////////////7
    // Getters for the static members
    
    private static Map getElementDecls() {
        if (elementDecls == null) {
            elementDecls = new HashMap();
            attrDecls = new HashMap();
            
            // Commonly used variables
            Set emptySet = new TreeSet();
            String spaceAtt = "xml:space";
            Set tmpSet;
            
            ////////////////////////////////////////////////
            // Initialize common sets
            
            Set charInstructions = new TreeSet(Arrays.asList(new String[]{"apply-templates", // NOI18N
            "call-template","apply-imports","for-each","value-of", // NOI18N
            "copy-of","number","choose","if","text","copy", // NOI18N
            "variable","message","fallback"}));
            
            Set instructions = new TreeSet(charInstructions);
            instructions.addAll(Arrays.asList(new String[]{"processing-instruction", // NOI18N
            "comment","element","attribute"}));
            
            Set charTemplate = charInstructions; // We don't care about PCDATA
            
            template = new TreeSet(instructions);
            template.add(resultElements);
            
            Set topLevel = new TreeSet(Arrays.asList(new String[]{"import","include","strip-space", // NOI18N
            "preserve-space","output","key","decimal-format","attribute-set", // NOI18N
            "variable","param","template","namespace-alias"}));
            
            Set topLevelAttr = new TreeSet(Arrays.asList(new String[]{"extension-element-prefixes",
            "exclude-result-prefixes","id","version",spaceAtt}));
            
            resultElementAttr = new TreeSet(Arrays.asList(new String[]{"extension-element-prefixes",
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
            attrDecls.put("import", new TreeSet(Arrays.asList(new String[]{"href"})));
            
            // xxsl:include
            elementDecls.put("include", emptySet);
            attrDecls.put("include", new TreeSet(Arrays.asList(new String[]{"href"})));
            
            // xsl:strip-space
            elementDecls.put("strip-space", emptySet);
            attrDecls.put("strip-space", new TreeSet(Arrays.asList(new String[]{"elements"})));
            
            // xsl:preserve-space
            elementDecls.put("preserve-space", emptySet);
            attrDecls.put("preserve-space", new TreeSet(Arrays.asList(new String[]{"elements"})));
            
            // xsl:output
            elementDecls.put("output", emptySet);
            attrDecls.put("output", new TreeSet(Arrays.asList(new String[]{"method",
            "version","encoding","omit-xml-declaration","standalone","doctype-public",
            "doctype-system","cdata-section-elements","indent","media-type"})));
            
            // xsl:key
            elementDecls.put("key", emptySet);
            attrDecls.put("key", new TreeSet(Arrays.asList(new String[]{"name","match","use"})));
            
            // xsl:decimal-format
            elementDecls.put("decimal-format", emptySet);
            attrDecls.put("decimal-format", new TreeSet(Arrays.asList(new String[]{"name",
            "decimal-separator","grouping-separator","infinity","minus-sign","NaN",
            "percent","per-mille","zero-digit","digit","pattern-separator"})));
            
            // xsl:namespace-alias
            elementDecls.put("namespace-alias", emptySet);
            attrDecls.put("namespace-alias", new TreeSet(Arrays.asList(new String[]{
                "stylesheet-prefix","result-prefix"})));
                
            // xsl:template
            tmpSet = new TreeSet(instructions);
            tmpSet.add(resultElements);
            tmpSet.add("param");
            elementDecls.put("template", tmpSet);
            attrDecls.put("template", new TreeSet(Arrays.asList(new String[]{
                "match","name","priority","mode",spaceAtt})));

            // xsl:value-of
            elementDecls.put("value-of", emptySet);
            attrDecls.put("value-of", new TreeSet(Arrays.asList(new String[]{
            "select","disable-output-escaping"})));

            // xsl:copy-of
            elementDecls.put("copy-of", emptySet);
            attrDecls.put("copy-of", new TreeSet(Arrays.asList(new String[]{"select"})));

            // xsl:number
            elementDecls.put("number", emptySet);
            attrDecls.put("number", new TreeSet(Arrays.asList(new String[]{
                "level","count","from","value","format","lang","letter-value",
                "grouping-separator","grouping-size"})));
                            
            // xsl:apply-templates
            elementDecls.put("apply-templates", new TreeSet(Arrays.asList(new String[]{
                "sort","with-param"})));
            attrDecls.put("apply-templates", new TreeSet(Arrays.asList(new String[]{
                "select","mode"})));

            // xsl:apply-imports
            elementDecls.put("apply-imports", emptySet);
            attrDecls.put("apply-imports", emptySet);

            // xsl:for-each
            tmpSet = new TreeSet(instructions);
            tmpSet.add(resultElements);
            tmpSet.add("sort");
            elementDecls.put("for-each", tmpSet);
            attrDecls.put("for-each", new TreeSet(Arrays.asList(new String[]{
            "select",spaceAtt})));

            // xsl:sort
            elementDecls.put("sort", emptySet);
            attrDecls.put("sort", new TreeSet(Arrays.asList(new String[]{
                "select","lang","data-type","order","case-order"})));

            // xsl:if
            elementDecls.put("if", template);
            attrDecls.put("if", new TreeSet(Arrays.asList(new String[]{"test",spaceAtt})));

            // xsl:choose
            elementDecls.put("choose", new TreeSet(Arrays.asList(new String[]{
                "when","otherwise"})));
            attrDecls.put("choose", new TreeSet(Arrays.asList(new String[]{spaceAtt})));

            // xsl:when
            elementDecls.put("when", template);
            attrDecls.put("when", new TreeSet(Arrays.asList(new String[]{
                "test",spaceAtt})));

            // xsl:otherwise
            elementDecls.put("otherwise", template);
            attrDecls.put("otherwise", new TreeSet(Arrays.asList(new String[]{spaceAtt})));

            // xsl:attribute-set
            elementDecls.put("sort", new TreeSet(Arrays.asList(new String[]{"attribute"})));
            attrDecls.put("attribute-set", new TreeSet(Arrays.asList(new String[]{
                "name","use-attribute-sets"})));

            // xsl:call-template
            elementDecls.put("call-template", new TreeSet(Arrays.asList(new String[]{"with-param"})));
            attrDecls.put("call-template", new TreeSet(Arrays.asList(new String[]{"name"})));

            // xsl:with-param
            elementDecls.put("with-param", template);
            attrDecls.put("with-param", new TreeSet(Arrays.asList(new String[]{
                "name","select"})));

            // xsl:variable
            elementDecls.put("variable", template);
            attrDecls.put("variable", new TreeSet(Arrays.asList(new String[]{
                "name","select"})));

            // xsl:param
            elementDecls.put("param", template);
            attrDecls.put("param", new TreeSet(Arrays.asList(new String[]{
                "name","select"})));

            // xsl:text
            elementDecls.put("text", emptySet);
            attrDecls.put("text", new TreeSet(Arrays.asList(new String[]{
                "disable-output-escaping"})));

            // xsl:processing-instruction
            elementDecls.put("processing-instruction", charTemplate);
            attrDecls.put("processing-instruction", new TreeSet(Arrays.asList(new String[]{
                "name",spaceAtt})));

            // xsl:element
            elementDecls.put("element", template);
            attrDecls.put("element", new TreeSet(Arrays.asList(new String[]{
                "name","namespace","use-attribute-sets",spaceAtt})));

            // xsl:attribute
            elementDecls.put("attribute", charTemplate);
            attrDecls.put("attribute", new TreeSet(Arrays.asList(new String[]{
                "name","namespace",spaceAtt})));

            // xsl:comment
            elementDecls.put("comment", charTemplate);
            attrDecls.put("comment", new TreeSet(Arrays.asList(new String[]{spaceAtt})));

            // xsl:copy
            elementDecls.put("copy", template);
            attrDecls.put("copy", new TreeSet(Arrays.asList(new String[]{
                spaceAtt,"use-attribute-sets"})));

            // xsl:message
            elementDecls.put("message", template);
            attrDecls.put("message", new TreeSet(Arrays.asList(new String[]{
                spaceAtt,"terminate"})));

            // xsl:fallback
            elementDecls.put("fallback", template);
            attrDecls.put("fallback", new TreeSet(Arrays.asList(new String[]{spaceAtt})));
        }
        return elementDecls;
    }
    
    private static Map getAttrDecls() {
        if (attrDecls == null) {
            getElementDecls();
        }
        return attrDecls;
    }
    
    private static Set getResultElementAttr() {
        if (resultElementAttr == null) {
            getElementDecls();
        }
        return resultElementAttr;
    }
    
    private static Set getTemplate() {
        if (template == null) {
            getElementDecls();
        }
        return template;
    }
    
    private static Set getXslFunctions() {
        if (xslFunctions == null) {
            xslFunctions = new TreeSet(Arrays.asList(new String[]{
                "boolean(","ceiling(","concat(", "contains(","count(","current()","document(",
                "false()", "floor(","format-number(","generate-id(",
                "id(","local-name(","key(","lang(","last()","name(","namespace-uri(", "normalize-space(",
                "not(","number(","position()","round(","starts-with(","string(",
                "string-length(", "substring(","substring-after(","substring-before(", "sum(",
                "system-property(","translate(",   "true()","unparsed-entity-uri("}));
        }
        return xslFunctions;
    }
    
    private static Set getXPathAxes() {
        if (xpathAxes == null) {
            xpathAxes = new TreeSet(Arrays.asList(new String[]{"ancestor::", "ancestor-or-self::",
            "attribute::", "child::", "descendant::", "descendant-or-self::", "following::",
            "following-sibling::", "namespace::", "parent::", "preceding::",
            "preceding-sibling::", "self::"}));
        }
        return xpathAxes;
    }
    
    private static Map getExprAttributes() {
        if (exprAttributes == null) {
            exprAttributes = new HashMap();
            exprAttributes.put("key", "use");
            exprAttributes.put("value-of", "select");
            exprAttributes.put("copy-of", "select");
            exprAttributes.put("number", "value");
            exprAttributes.put("apply-templates", "select");
            exprAttributes.put("for-each", "select");
            exprAttributes.put("sort", "select");
            exprAttributes.put("if", "test");
            exprAttributes.put("when", "test");
            exprAttributes.put("with-param", "select");
            exprAttributes.put("variable", "select");
            exprAttributes.put("param", "select");
        }
        return exprAttributes;
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // GrammarQuery interface fulfillment
    
    /**
     * Support completions of elements defined by XSLT spec and by the <output>
     * doctype attribute (in result space).
     */
    public Enumeration queryElements(HintContext ctx) {
        Node node = ((Node)ctx).getParentNode();
        
        String prefix = ctx.getCurrentPrefix();
        QueueEnumeration list = new QueueEnumeration();
        
        if (node instanceof Element) {
            Element el = (Element) node;
            updateProperties(el);
            if (prefixList.size() == 0) return EmptyEnumeration.EMPTY;
            
            String firstXslPrefixWithColon = prefixList.get(0) + ":";
            Set elements;
            if (el.getTagName().startsWith(firstXslPrefixWithColon)) {
                String parentNCName = el.getTagName().substring(firstXslPrefixWithColon.length());
                elements = (Set) getElementDecls().get(parentNCName);
            } else {
                // Children of result elements should always be the template set
                elements = getTemplate();
            }
            
            // First we add the Result elements
            if (elements != null  && resultGrammarQuery != null && elements.contains(resultElements)) {
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
                    addXslElementsToEnum(list, getElementDecls().keySet(), curPrefix, prefix);
                } else {
                    String parentName = curName.substring(curPrefix.length());
                    elements = (Set) getElementDecls().get(parentName);
                    addXslElementsToEnum(list, elements, curPrefix, prefix);
                }
            }
            
        } else if (node instanceof Document) {
            //??? it should be probably only root element name
            addXslElementsToEnum(list, getElementDecls().keySet(), prefixList.get(0) + ":", prefix);
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
        
        updateProperties(el);
        
        
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
            possibleAttributes = (Set) getAttrDecls().get(el.getTagName().substring(curXslPrefix.length()));
        } else {
            // XSL Attributes of Result element
            possibleAttributes = new TreeSet();
            if (prefixList.size() > 0) {
                Iterator it = getResultElementAttr().iterator();
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
       if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            updateProperties(((Attr)ctx).getOwnerElement());
            if (prefixList.size() == 0) return EmptyEnumeration.EMPTY;
            String xslNamespacePrefix = prefixList.get(0) + ":";
            
            String prefix = ctx.getCurrentPrefix();
            
            Attr attr = (Attr)ctx;
            
            boolean isXPath = false;
            String elName = attr.getOwnerElement().getNodeName();
            if (elName.startsWith(xslNamespacePrefix)) {
                String key = elName.substring(xslNamespacePrefix.length());
                String xpathAttrName = (String)getExprAttributes().get(key);
                if (xpathAttrName != null && xpathAttrName.equals(attr.getNodeName())) {
                    // This is an XSLT element which should contain XPathExpression
                    isXPath = true;
                }
            }
            
            String preExpression = "";
            
            if (!isXPath) {
                // Check if we are inside { } for attribute value
                String nodeValue = attr.getNodeValue();
                int exprStart = nodeValue.lastIndexOf('{', prefix.length() - 1);
                int exprEnd = nodeValue.indexOf('}', prefix.length());
                Util.THIS.debug("exprStart: " + exprStart);
                Util.THIS.debug("exprEnd: " + exprEnd);
                if (exprStart != -1 && exprEnd != -1) {
                    isXPath = true;
                    preExpression = prefix.substring(0, exprStart + 1);
                    prefix = prefix.substring(exprStart + 1);
                }
                
            }
            
            if (isXPath) {
                // This is an XPath expression
                QueueEnumeration list = new QueueEnumeration();
                
                int curIndex = prefix.length();
                while (curIndex > 0) {
                    curIndex--;
                    char curChar = prefix.charAt(curIndex);
                    if (curChar == '(' || curChar == ',' || curChar == ' ') {
                        curIndex++;
                        break;
                    }
                }
                
                preExpression += prefix.substring(0, curIndex);
                String subExpression = prefix.substring(curIndex);
                
                int lastDiv = subExpression.lastIndexOf('/');
                String subPre = "";
                String subRest = "";
                if (lastDiv != -1) {
                    subPre = subExpression.substring(0, lastDiv + 1);
                    subRest = subExpression.substring(lastDiv + 1);
                } else {
                    subRest = subExpression;
                }
                
                // At this point we need to consult transformed document or
                // its grammar.
                
                Object selScenarioObj = scenarioCookie.getModel().getSelectedItem();
                if (selScenarioObj instanceof XSLScenario) {
                    XSLScenario scenario = (XSLScenario)selScenarioObj;
                    Document doc = null;
                    try {
                        doc = scenario.getSourceDocument(dataObject);
                    } catch(Exception e) {
                        // We don't care, ignore
                    }
                    
                    if (doc != null) {
                        Element docElement = doc.getDocumentElement();
                        
                        Set childNodeNames = new TreeSet();
                        
                        String combinedXPath;
                        if (subPre.startsWith("/")) {
                            // This is an absolute XPath
                            combinedXPath = subPre;
                        } else {
                            // This is a relative XPath
                            
                            // Traverse up the documents tree looking for xsl:for-each
                            String xslForEachName = xslNamespacePrefix + "for-each"; // NOI18N
                            List selectAttrs = new LinkedList();
                            Node curNode = attr.getOwnerElement();
                            if (curNode != null) {
                                // We don't want to add select of our selfs
                                curNode = curNode.getParentNode();
                            }
                            
                            while (curNode != null && !(curNode instanceof Document)) {
                                if (curNode.getNodeName().equals(xslForEachName)) {
                                    selectAttrs.add(0, ((Element)curNode).getAttribute("select"));
                                }
                                
                                curNode = curNode.getParentNode();
                            }
                            
                            combinedXPath = "";
                            for (int ind = 0; ind < selectAttrs.size(); ind++) {
                                combinedXPath += selectAttrs.get(ind) + "/";
                            }
                            combinedXPath += subPre;
                        }
                        
                        try {
                            NodeList nodeList = XPathAPI.selectNodeList(doc, combinedXPath + "child::*");
                            for (int ind = 0; ind < nodeList.getLength(); ind++) {
                                Node curResNode = nodeList.item(ind);
                                childNodeNames.add(curResNode.getNodeName());
                            }
                            
                            nodeList = XPathAPI.selectNodeList(doc, combinedXPath + "@*");
                            for (int ind = 0; ind < nodeList.getLength(); ind++) {
                                Node curResNode = nodeList.item(ind);
                                childNodeNames.add("@" + curResNode.getNodeName());
                            }
                        } catch (Exception e) {
                            Util.THIS.debug("Ignored during XPathAPI operations", e);
                            // We don't care, ignore
                        }
                        
                        addItemsToEnum(list, childNodeNames, subRest, preExpression + subPre);
                    }
                }
                
                addItemsToEnum(list, getXPathAxes(), subRest, preExpression + subPre);
                addItemsToEnum(list, getXslFunctions(), subExpression, preExpression);
                
                return list;
            }
        }
        
        return EmptyEnumeration.EMPTY;
    }
    
    public Enumeration queryEntities(String prefix) {
        QueueEnumeration list = new QueueEnumeration();
        
        // add well-know build-in entity names
        
        if ("lt".startsWith(prefix)) list.put(new MyEntityReference("lt"));     // NOI18N
        if ("gt".startsWith(prefix)) list.put(new MyEntityReference("gt"));     // NOI18N
        if ("apos".startsWith(prefix)) list.put(new MyEntityReference("apos")); // NOI18N
        if ("quot".startsWith(prefix)) list.put(new MyEntityReference("quot")); // NOI18N
        if ("amp".startsWith(prefix)) list.put(new MyEntityReference("amp"));   // NOI18N
        
        return list;
    }
    
    public Enumeration queryNotations(String prefix) {
        return EmptyEnumeration.EMPTY;
    }
    
    public java.awt.Component getCustomizer(HintContext ctx) {
        if (customizer == null) {
            try {
                // Load the XSLCustomizer from the XML layer
                FileSystem fs = Repository.getDefault().getDefaultFileSystem();
                FileObject fo = fs.findResource(CUSTOMIZER_FOLDER); 
                DataObject df = DataObject.find(fo);
                if (!(df instanceof DataObject.Container)) {
                    return null;
                }

                FolderLookup lookup =
                    new FolderLookup((DataObject.Container) df);
                Lookup.Template template =
                    new Lookup.Template(XSLCustomizer.class);

                Lookup.Item lookupItem = lookup.getLookup().lookupItem(template);
                if (lookupItem == null) {
                    return null;
                }

                customizer=(XSLCustomizer)lookupItem.getInstance();
            } catch(Exception e) {
                return null;
            }
        }
        
        if (customizer == null) {
            return null;
        }
        
        customizer.setDataObject(dataObject);
        customizer.setContextNode(ctx);
        return customizer.getComponent();
    }
    
    public boolean hasCustomizer(HintContext ctx) {
		//Check if the node is an attribute
		if(ctx.getNodeType() != Node.ATTRIBUTE_NODE) {
			return false;
		}
        
        return getCustomizer(ctx) != null;
    }
    
    public org.openide.nodes.Node.Property[] getProperties(final HintContext ctx) {
        
        if (ctx.getNodeType() != Node.ATTRIBUTE_NODE || ctx.getNodeValue() == null) {
            return null;
        }
        
        PropertySupport attrNameProp = new PropertySupport("Attribute name", String.class,
        "Attribute name", "The name of the selected attribute", true, false) {
            public void setValue(Object value) {
                // Dummy
            }
            public Object getValue() {
                return ctx.getNodeName();
            }
            
        };
        
        PropertySupport attrValueProp = new PropertySupport("Attribute value", String.class,
        "Attribute value", "The value of the selected attribute", true, true) {
            public void setValue(Object value) {
                ctx.setNodeValue((String)value);
            }
            public Object getValue() {
                return ctx.getNodeValue();
            }
            
        };
        
        return new org.openide.nodes.Node.Property[]{attrNameProp, attrValueProp};
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Private helper methods
    
    /**
     * Looks up registered XSLCustomizer objects which will be used by this object 
     */
    private Lookup.Item getCustomizerLookupItem() {
        try {
            // Load the XSLCustomizer from the XML layer
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            FileObject fo = fs.findResource(CUSTOMIZER_FOLDER); 
            DataObject df = DataObject.find(fo);
            if (!(df instanceof DataObject.Container)) {
                return null;
            }

            FolderLookup lookup =
                new FolderLookup((DataObject.Container) df);
            Lookup.Template template =
                new Lookup.Template(XSLCustomizer.class);
            return lookup.getLookup().lookupItem(template);
        } catch(Exception e) {
            return null;
        }
    }
    
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
                if (next != resultElements) {
                    String nextText = namespacePrefix + (String)next;
                    if (nextText.startsWith(startWith)) {
                        enum.put(new MyElement(nextText));
                    }
                }
            }
        }
    }
    
    private void addItemsToEnum(QueueEnumeration enum, Set set, String startWith, String prefix) {
        Iterator it = set.iterator();
        while ( it.hasNext()) {
            String nextText = (String)it.next();
            if (nextText.startsWith(startWith)) {
                enum.put(new MyText(prefix + nextText));
            }
        }
    }
    
    /**
     * This method traverses up the document tree, investigates it and updates
     * prefixList, resultGrammarQuery, lastDoctypeSystem or lastDoctypePublic
     * members if necessery.
     * @param curNode the node which from wich the traversing should start.
     */
    private void updateProperties(Node curNode) {
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
                    String outputMethod = outputEl.getAttribute("method");
                                        
                    String curDoctypePublic = outputEl.getAttribute("doctype-public");
                    String curDoctypeSystem = outputEl.getAttribute("doctype-system");
                    
                    if ("html".equals(outputMethod)) {                          // NOI18N
                        // html is special case that can be emulated using XHTML
                        curDoctypePublic = XHTML_PUBLIC_ID;
                    } else if ("text".equals(outputMethod)) {                   // NOI18N
                        // user error, ignore
                        break;
                    }
                    
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
        
        InputSource inputSource = null;
        UserCatalog catalog = UserCatalog.getDefault();
        if (catalog != null) {
            EntityResolver resolver = catalog.getEntityResolver();
            if (resolver != null) {
                try {
                    inputSource = resolver.resolveEntity(publicId, systemId);
                } catch(SAXException e) {
                } catch(IOException e) {
                } // Will be handled below
            }
        }
        
        if (inputSource == null) {
            try {
                java.net.URL url = new java.net.URL(systemId);
                inputSource = new InputSource(url.openStream());
                inputSource.setPublicId(publicId);
                inputSource.setSystemId(systemId);
            } catch(IOException e) {
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