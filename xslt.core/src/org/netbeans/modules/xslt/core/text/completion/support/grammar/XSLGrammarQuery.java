/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xslt.core.text.completion.support.grammar;

import java.io.IOException;
import java.util.*;
import javax.swing.Icon;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.api.model.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.modules.xslt.core.text.completion.support.api.XSLCustomizer;
import org.openide.loaders.DataObject;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
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
public final class XSLGrammarQuery implements GrammarQuery, XSLGrammarConstants {
    // namespace that this grammar supports
    public static final String XSLT_NAMESPACE_URI = "http://www.w3.org/1999/XSL/Transform"; // NOI18N

    // we cannot parse SGML DTD for HTML, let emulate it by XHTML DTD
    private final static String XHTML_PUBLIC_ID = System.getProperty(
        "netbeans.xsl.html.public", "-//W3C//DTD XHTML 1.0 Transitional//EN");  // NOI18N

    // we cannot parse SGML DTD for HTML, let emulate it by XHTML DTD
    private final static String XHTML_SYSTEM_ID = System.getProperty(
        "netbeans.xsl.html.system", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"); // NOI18N

    /** Folder which stores instances of custom external XSL customizers */
    private static final String CUSTOMIZER_FOLDER = "Plugins/XML/XSLCustomizer"; // NOI18N

    private static final String DEFAULT_XSL_VERSION = XSL_VERSION_1_0;

    /*
     * This map contains instances of XSLGrammarProducers for each supported XSL version.
     * A key - [String] XSL version, a value - [XSLGrammarProducer]
     */
    private static final Map<String, XSLGrammarProducer> mapXslGrammarProducers = 
        new HashMap<String, XSLGrammarProducer>(3);
    
    private DataObject dataObject;

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

    private XSLCustomizer customizer = null;

    private ResourceBundle bundle = NbBundle.getBundle(XSLGrammarQuery.class);

    /** Creates a new instance of XSLGrammarQuery */
    public XSLGrammarQuery(DataObject dataObject) {
        this.dataObject = dataObject;
    }

    private static final XSLGrammarProducer getXslGrammarProducer(Object currentContext,
        COMPLETION_QUERY_TYPE currentCompletionQueryType) {
        String xslVersion = XSLGrammarUtil.getCurrentXslVersion(currentContext, 
            currentCompletionQueryType);
        if (xslVersion == null) xslVersion = DEFAULT_XSL_VERSION;
        XSLGrammarProducer xslGrammarProducer = mapXslGrammarProducers.get(xslVersion);
        if (xslGrammarProducer == null) {
            if (xslVersion.equals(XSL_VERSION_1_0))
                xslGrammarProducer = new XSLGrammarProducerImpl_1_0();
            if (xslVersion.equals(XSL_VERSION_1_1))
                xslGrammarProducer = new XSLGrammarProducerImpl_1_1();
            if (xslVersion.equals(XSL_VERSION_2_0))
                xslGrammarProducer = new XSLGrammarProducerImpl_2_0();
            
            mapXslGrammarProducers.put(xslVersion, xslGrammarProducer);
        }
        return xslGrammarProducer;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // GrammarQuery interface fulfillment

    /**
     * Support completions of elements defined by XSLT spec and by the <output>
     * doctype attribute (in result space).
     */
    public Enumeration<GrammarResult> queryElements(HintContext ctx) {
        XSLGrammarProducer xslGrammerProducer = getXslGrammarProducer(ctx, 
            COMPLETION_QUERY_TYPE.QueryTagName);
        
        Node node = ((Node)ctx).getParentNode();

        String prefix = ctx.getCurrentPrefix();
        QueueEnumeration list = new QueueEnumeration();

        if (node instanceof Element) {
            Element el = (Element) node;
            updateProperties(el);
            if (prefixList.size() == 0) return org.openide.util.Enumerations.empty();

            String firstXslPrefixWithColon = prefixList.get(0) + ":"; // NOI18N
            Set elements;
            if (el.getTagName().startsWith(firstXslPrefixWithColon)) {
                String parentNCName = el.getTagName().substring(firstXslPrefixWithColon.length());
                elements = (Set) xslGrammerProducer.getElementDecls().get(parentNCName);
            } else {
                // Children of result elements should always be the template set
                elements = xslGrammerProducer.getTemplate();
            }

            // First we add the Result elements
            if (elements != null  && resultGrammarQuery != null && elements.contains(RESULT_ELEMENTS)) {
                ResultHintContext resultHintContext = new ResultHintContext(ctx, firstXslPrefixWithColon, null);
                Enumeration resultEnum = resultGrammarQuery.queryElements(resultHintContext);
                while (resultEnum.hasMoreElements()) {
                    list.put(resultEnum.nextElement());
                }
            }

            // Then we add the XSLT elements of the first prefix (normally of the stylesheet node).
            addXslElementsToEnum(list, elements, prefixList.get(0) + ":", prefix); // NOI18N

            // Finally we add xsl namespace elements with other prefixes than the first one
            for (int prefixInd = 1; prefixInd < prefixList.size(); prefixInd++) {
                String curPrefix = (String)prefixList.get(prefixInd) + ":"; // NOI18N
                Node curNode = el;
                String curName = null;
                while(curNode != null && null != (curName = curNode.getNodeName()) && !curName.startsWith(curPrefix)) {
                    curNode = curNode.getParentNode();
                }

                if (curName == null) {
                    // This must be the document node
                    addXslElementsToEnum(list, xslGrammerProducer.getElementDecls().keySet(), 
                        curPrefix, prefix);
                } else {
                    String parentName = curName.substring(curPrefix.length());
                    elements = (Set) xslGrammerProducer.getElementDecls().get(parentName);
                    addXslElementsToEnum(list, elements, curPrefix, prefix);
                }
            }

        } else if (node instanceof Document) {
            //??? it should be probably only root element name
            if (prefixList.size() == 0) return org.openide.util.Enumerations.empty();
            addXslElementsToEnum(list, xslGrammerProducer.getElementDecls().keySet(), 
                prefixList.get(0) + ":", prefix); // NOI18N
        } else {
            return org.openide.util.Enumerations.empty();
        }

        return list;
    }

    public Enumeration<GrammarResult> queryAttributes(HintContext ctx) {
        XSLGrammarProducer xslGrammerProducer = getXslGrammarProducer(ctx, 
            COMPLETION_QUERY_TYPE.QueryAttributeName);
        
        Element el = null;
        // Support two versions of GrammarQuery contract
        if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            el = ((Attr)ctx).getOwnerElement();
        } else if (ctx.getNodeType() == Node.ELEMENT_NODE) {
            el = (Element) ctx;
        }
        if (el == null) return org.openide.util.Enumerations.empty();

        String elTagName = el.getTagName();
        NamedNodeMap existingAttributes = el.getAttributes();

        updateProperties(el);


        String curXslPrefix = null;
        for (int ind = 0; ind < prefixList.size(); ind++) {
            if (elTagName.startsWith((String)prefixList.get(ind) + ":")){ // NOI18N
                curXslPrefix = (String)prefixList.get(ind) + ":"; // NOI18N
                break;
            }
        }

        Set possibleAttributes;
        if (curXslPrefix != null) {
            // Attributes of XSL element
            possibleAttributes = (Set) xslGrammerProducer.getAttrDecls().get(
                el.getTagName().substring(curXslPrefix.length()));
        } else {
            // XSL Attributes of Result element
            possibleAttributes = new TreeSet<String>();
            if (prefixList.size() > 0) {
                Iterator it = xslGrammerProducer.getResultElementAttr().iterator();
                while ( it.hasNext()) {
                    possibleAttributes.add((String)prefixList.get(0) + ":" + (String) it.next()); // NOI18N
                }
            }
        }
        if (possibleAttributes == null) return org.openide.util.Enumerations.empty();

        String prefix = ctx.getCurrentPrefix();

        QueueEnumeration list = new QueueEnumeration();

        if (resultGrammarQuery != null) {
            Enumeration enum2 = resultGrammarQuery.queryAttributes(ctx);
            while(enum2.hasMoreElements()) {
                GrammarResult resNode = (GrammarResult)enum2.nextElement();
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

    public Enumeration<GrammarResult> queryValues(HintContext ctx) {
        XSLGrammarProducer xslGrammerProducer = getXslGrammarProducer(ctx, 
            COMPLETION_QUERY_TYPE.QueryAttributeValue);
        
       if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            updateProperties(((Attr)ctx).getOwnerElement());
            if (prefixList.size() == 0) return org.openide.util.Enumerations.empty();
            String xslNamespacePrefix = prefixList.get(0) + ":"; // NOI18N

            String prefix = ctx.getCurrentPrefix();

            Attr attr = (Attr)ctx;

            boolean isXPath = false;
            String elName = attr.getOwnerElement().getNodeName();
            if (elName.startsWith(xslNamespacePrefix)) {
                String key = elName.substring(xslNamespacePrefix.length());
                String xpathAttrName = 
                    (String) xslGrammerProducer.getExprAttributes().get(key);
                if (xpathAttrName != null && xpathAttrName.equals(attr.getNodeName())) {
                    // This is an XSLT element which should contain XPathExpression
                    isXPath = true;
                }

                // consult awailable public IDs with users catalog
                if ("output".equals(key)) {                             // NOI18N
                    if ("doctype-public".equals(attr.getName())) {      // NOI18N
                        UserCatalog catalog = UserCatalog.getDefault();
                        if (catalog == null) return org.openide.util.Enumerations.empty();
                        QueueEnumeration en = new QueueEnumeration();
                        Iterator it = catalog.getPublicIDs();
                        while (it.hasNext()) {
                            String next = (String) it.next();
                            if (next != null && next.startsWith(prefix)) {
                                en.put(new  MyText(next));
                            }
                        }
                        return en;
                    }
                }
            }

            String preExpression = ""; // NOI18N

            if (!isXPath) {
                // Check if we are inside { } for attribute value
                String nodeValue = attr.getNodeValue();
                int exprStart = nodeValue.lastIndexOf('{', prefix.length() - 1); // NOI18N
                int exprEnd = nodeValue.indexOf('}', prefix.length()); // NOI18N
                //Util.THIS.debug("exprStart: " + exprStart); // NOI18N
                //Util.THIS.debug("exprEnd: " + exprEnd); // NOI18N
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
                    if (curChar == '(' || curChar == ',' || curChar == ' ') { // NOI18N
                        curIndex++;
                        break;
                    }
                }

                preExpression += prefix.substring(0, curIndex);
                String subExpression = prefix.substring(curIndex);

                int lastDiv = subExpression.lastIndexOf('/'); // NOI18N
                String subPre = ""; // NOI18N
                String subRest = ""; // NOI18N
                if (lastDiv != -1) {
                    subPre = subExpression.substring(0, lastDiv + 1);
                    subRest = subExpression.substring(lastDiv + 1);
                } else {
                    subRest = subExpression;
                }

                // At this point we need to consult transformed document or
                // its grammar.
// [93792] +
//              Object selScenarioObj = scenarioCookie.getModel().getSelectedItem();
// [93792] -
                /*
                if (selScenarioObj instanceof XSLScenario) {
                    XSLScenario scenario = (XSLScenario)selScenarioObj;
                    Document doc = null;
                    try {
                        doc = scenario.getSourceDocument(dataObject, false);
                    } catch(Exception e) {
                        // We don't care, ignore
                    }

                    if (doc != null) {
                        Element docElement = doc.getDocumentElement();

                        Set childNodeNames = new TreeSet();

                        String combinedXPath;
                        if (subPre.startsWith("/")) { // NOI18N
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
                                    selectAttrs.add(0, ((Element)curNode).getAttribute("select")); // NOI18N
                                }

                                curNode = curNode.getParentNode();
                            }

                            combinedXPath = ""; // NOI18N
                            for (int ind = 0; ind < selectAttrs.size(); ind++) {
                                combinedXPath += selectAttrs.get(ind) + "/"; // NOI18N
                            }
                            combinedXPath += subPre;
                        }

                        try {
                            NodeList nodeList = XPathAPI.selectNodeList(doc, combinedXPath + "child::*"); // NOI18N
                            for (int ind = 0; ind < nodeList.getLength(); ind++) {
                                Node curResNode = nodeList.item(ind);
                                childNodeNames.add(curResNode.getNodeName());
                            }

                            nodeList = XPathAPI.selectNodeList(doc, combinedXPath + "@*"); // NOI18N
                            for (int ind = 0; ind < nodeList.getLength(); ind++) {
                                Node curResNode = nodeList.item(ind);
                                childNodeNames.add("@" + curResNode.getNodeName()); // NOI18N
                            }
                        } catch (Exception e) {
                            Util.THIS.debug("Ignored during XPathAPI operations", e); // NOI18N
                            // We don't care, ignore
                        }

                        addItemsToEnum(list, childNodeNames, subRest, preExpression + subPre);
                    }
                }*/

                addItemsToEnum(list, xslGrammerProducer.getXPathAxes(), subRest, 
                    preExpression + subPre);
                addItemsToEnum(list, xslGrammerProducer.getXslFunctions(), subExpression, 
                    preExpression);

                return list;
            }
        }
        return org.openide.util.Enumerations.empty();
    }

    public GrammarResult queryDefault(HintContext ctx) {
        //??? XSLT defaults are missing
        if (resultGrammarQuery == null) return null;
        return resultGrammarQuery.queryDefault(ctx);
    }

    public boolean isAllowed(Enumeration en) {
        return true; //!!! not implemented
    }

    public Enumeration<GrammarResult> queryEntities(String prefix) {
        QueueEnumeration list = new QueueEnumeration();

        // add well-know build-in entity names

        if ("lt".startsWith(prefix)) list.put(new MyEntityReference("lt"));     // NOI18N
        if ("gt".startsWith(prefix)) list.put(new MyEntityReference("gt"));     // NOI18N
        if ("apos".startsWith(prefix)) list.put(new MyEntityReference("apos")); // NOI18N
        if ("quot".startsWith(prefix)) list.put(new MyEntityReference("quot")); // NOI18N
        if ("amp".startsWith(prefix)) list.put(new MyEntityReference("amp"));   // NOI18N

        return list;
    }

    public Enumeration<GrammarResult> queryNotations(String prefix) {
        return org.openide.util.Enumerations.empty();
    }

    public java.awt.Component getCustomizer(HintContext ctx) {
        if (customizer == null) {
            customizer = lookupCustomizerInstance();
            if (customizer == null) {
                return null;
            }
        }

        return customizer.getCustomizer(ctx, dataObject);
    }

    public boolean hasCustomizer(HintContext ctx) {
        if (customizer == null) {
            customizer = lookupCustomizerInstance();
            if (customizer == null) {
                return false;
            }
        }

        return customizer.hasCustomizer(ctx);
    }

    public org.openide.nodes.Node.Property[] getProperties(final HintContext ctx) {

        if (ctx.getNodeType() != Node.ATTRIBUTE_NODE || ctx.getNodeValue() == null) {
            return null;
        }

        PropertySupport attrNameProp = new PropertySupport("Attribute name", String.class,  // NOI18N
        bundle.getString("BK0001"), bundle.getString("BK0002"), true, false) {
            public void setValue(Object value) {
                // Dummy
            }
            public Object getValue() {
                return ctx.getNodeName();
            }

        };

        PropertySupport attrValueProp = new PropertySupport("Attribute value", String.class, // NOI18N
        bundle.getString("BK0003"), bundle.getString("BK0004"), true, true) {
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
    private static XSLCustomizer lookupCustomizerInstance() {
        Lookup.Template lookupTemplate =
            new Lookup.Template(XSLCustomizer.class);

        Lookup.Item lookupItem = Lookups.forPath(CUSTOMIZER_FOLDER).lookupItem(lookupTemplate);
        if (lookupItem == null) {
            return null;
        }

        return (XSLCustomizer)lookupItem.getInstance();
    }

    /**
     * @param enumX the Enumeration which the element should be added to
     * @param elements a set containing strings which should be added (with prefix) to the enum or <code>null</null>
     * @param namespacePrefix a prefix at the form "xsl:" which should be added in front
     *          of the names in the elements.
     * @param startWith Elements should only be added to enum if they start with this string
     */
    private static void addXslElementsToEnum(QueueEnumeration enumX, Set elements, String namespacePrefix, String startWith) {
        if (elements == null) return;
        if (startWith.startsWith(namespacePrefix) || namespacePrefix.startsWith(startWith)) {
            Iterator it = elements.iterator();
            while ( it.hasNext()) {
                Object next = it.next();
                if (next != RESULT_ELEMENTS) {
                    String nextText = namespacePrefix + (String)next;
                    if (nextText.startsWith(startWith)) {
                        // TODO pass true for empty elements
                        enumX.put(new MyElement(nextText, false));
                    }
                }
            }
        }
    }

    private static void addItemsToEnum(QueueEnumeration enumX, Set set, String startWith, String prefix) {
        Iterator it = set.iterator();
        while ( it.hasNext()) {
            String nextText = (String)it.next();
            if (nextText.startsWith(startWith)) {
                enumX.put(new MyText(prefix + nextText));
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
                if (attrName != null && attrName.startsWith("xmlns:")) {  // NOI18N
                    if (attr.getValue().equals(XSLT_NAMESPACE_URI)) {
                        prefixList.add(0, attrName.substring(6));
                    }
                }
            }


            rootNode = curNode;
            curNode = rootNode.getParentNode();
        }

        boolean outputFound = false;
        if (prefixList.size() > 0) {
            String outputElName = (String)prefixList.get(0) + ":output"; // NOI18N
            Node childOfRoot = rootNode.getFirstChild();
            while (childOfRoot != null) {
                String childNodeName = childOfRoot.getNodeName();
                if (childNodeName != null && childNodeName.equals(outputElName)) {
                    Element outputEl = (Element)childOfRoot;
                    String outputMethod = outputEl.getAttribute("method"); // NOI18N

                    String curDoctypePublic = outputEl.getAttribute("doctype-public"); // NOI18N
                    String curDoctypeSystem = outputEl.getAttribute("doctype-system"); // NOI18N

                    if ("html".equals(outputMethod)  // NOI18N
                        && (curDoctypePublic == null || curDoctypePublic.length() == 0)
                        && (curDoctypeSystem == null || curDoctypeSystem.length() == 0)) {                          // NOI18N
                        // html is special case that can be emulated using XHTML
                        curDoctypePublic = XHTML_PUBLIC_ID;
                        curDoctypeSystem = XHTML_SYSTEM_ID;
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

        resultGrammarQuery = DTDUtil.parseDTD(true, inputSource);

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
         * @return provide additional information simplifiing decision
         */
        public String getDescription() {
            return NbBundle.getMessage(XSLGrammarQuery.class, "BK0005");
        }

        /**
         * @return text representing name of suitable entity
         * //??? is it really needed
         */
        public String getText() {
            return getNodeName();
        }

        /**
         * @return name that is presented to user
         */
        public String getDisplayName() {
            return null;
        }

        public boolean isEmptyElement() {
            return false;
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

        @Override
        public String getNodeName() {
            return name;
        }
    }

    private static class MyElement extends AbstractResultNode implements Element {
        private String name;
        private boolean empty;

        MyElement(String name, boolean empty) {
            this.name = name;
            this.empty = empty;
        }

        @Override
        public short getNodeType() {
            return Node.ELEMENT_NODE;
        }

        @Override
        public String getNodeName() {
            return name;
        }

        @Override
        public String getTagName() {
            return name;
        }

        @Override
        public boolean isEmptyElement() {
            return empty;
        }
    }

    private static class MyAttr extends AbstractResultNode implements Attr {
        private String name;

        MyAttr(String name) {
            this.name = name;
        }

        @Override
        public short getNodeType() {
            return Node.ATTRIBUTE_NODE;
        }

        @Override
        public String getNodeName() {
            return name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return null;  //??? what spec says
        }
    }

    private static class MyText extends AbstractResultNode implements Text {
        private String data;

        MyText(String data) {
            this.data = data;
        }

        @Override
        public short getNodeType() {
            return Node.TEXT_NODE;
        }

        @Override
        public String getNodeValue() {
            return getData();
        }

        @Override
        public String getData() throws DOMException {
            return data;
        }

        @Override
        public String getDisplayName() {
            return getData();
        }
        
        @Override
        public int getLength() {
            return data == null ? -1 : data.length();
        }
    }

    private static class QueueEnumeration implements Enumeration {
        private java.util.LinkedList list = new LinkedList ();
        
        public boolean hasMoreElements () {
            return !list.isEmpty ();
        }        
        
        public Object nextElement () {
            return list.removeFirst ();
        }        

        public void put (Object[] arr) {
            list.addAll (Arrays.asList (arr));
        }
        public void put (Object o) {
            list.add (o);
        }
        
    } // end of QueueEnumeration
}