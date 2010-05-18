/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.ui.axinodes;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.axinodes.NodeType.BadgeModificator;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIContainer;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.datatype.NumberBase;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class AxiomUtils {
    
    private static final String XPATH_ATTR_INDICATOR = "@"; // NOI18N
    private static final String XPATH_SEPARATOR = "/"; // NOI18N
    private static final String NAMESPACE_SEPARATOR = ":"; // NOI18N
    private static final String DEFAULT_PREFIX = "ns"; // NOI18N
    
    /**
     * Looks for an Axiom component which corresponds to the global schema component.
     * Tye type paramether helps to narrow the search.
     */
    public static AXIComponent findGlobalComponent(AXIDocument axiDocument,
            Class<? extends AXIComponent> type, SchemaComponent schemaComp) {
        List<? extends AXIComponent> children;
        if (type == null) {
            children = axiDocument.getChildren();
        } else {
            children = axiDocument.getChildren(type);
        }
        //
        for(AXIComponent globalChild : children) {
            if(globalChild.getPeer() == schemaComp) {
                return globalChild;
            }
        }
        return null;
    }
    
    
    /**
     * Retruves subcomponents of the specifed axiom component and
     * builds Nodes for each one.
     */
    public static Node[] processAxiComponent(
            AXIComponent axiomComponent, Lookup lookup) {
        if (axiomComponent == null) {
            return new Node[0];
        }
        //
        NodeFactory nodeFactory =
                (NodeFactory)lookup.lookup(NodeFactory.class);
        assert nodeFactory != null : "Node factory has to be specified"; // NOI18N
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        //
        List<AbstractElement> elementsList =
                axiomComponent.getChildElements();
        for (AbstractElement element : elementsList) {
            if (element instanceof Element) {
                Node newNode = nodeFactory.createNode(
                        NodeType.ELEMENT, element, lookup);
                if (newNode != null) {
                    nodesList.add(newNode);
                }
            }
        }
        //
        if (axiomComponent instanceof AXIContainer) {
            List<AbstractAttribute> attributesList =
                    ((AXIContainer)axiomComponent).getAttributes();
            for (AbstractAttribute attribute : attributesList) {
                if (attribute instanceof Attribute) {
                    Node newNode = nodeFactory.createNode(
                            NodeType.ATTRIBUTE, attribute, lookup);
                    if (newNode != null) {
                        nodesList.add(newNode);
                    }
                }
            }
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
    /**
     * TODO This calculator works wrong because of the
     * axiComponent.getParentElement() method returns null if the component is
     * the reference to a global element. So an incomplete XPath can be constructed.
     * @deprecated
     */
    public static String calculateSimpleXPath(AXIComponent axiComponent) {
        //
        // Collects Path Items first
        ArrayList<PathItem> path = new ArrayList<PathItem>();
        while (axiComponent != null) {
            String compName = null;
            //
            if (axiComponent instanceof Element) {
                compName = ((Element)axiComponent).getName();
            } else if (axiComponent instanceof Attribute) {
                compName = ((Attribute)axiComponent).getName();
            } else if (axiComponent instanceof AXIType) {
                compName = ((AXIType)axiComponent).getName();
            }
            //
            String namespace = getNamespace(axiComponent);

            //
            if (compName != null && compName.length() != 0) {
                PathItem pathItem = new PathItem(axiComponent, namespace, compName, null);
                path.add(pathItem);
            } else {
                path.clear();
            }
            //
            axiComponent = axiComponent.getParentElement();
        }
        //
        // Check namespaces.
        // If namespace of a PathItem is the same as its parent has,
        // then it doesn't necessary to show it.
        // If a namespace is necessary, then try to obtain a prefix for it.
//        ListIterator<PathItem> itr = path.listIterator();
//        String prevItemNamespace = null;
//        while (itr.hasNext()) {
//            PathItem pathItem = itr.next();
//            //
//            if (prevItemNamespace != null) {
//                if (prevItemNamespace.equals(pathItem.myNamespace)) {
//                    pathItem.myNamespace = null;
//                    continue;
//                }
//            }
//            //
////            pathItem.myNamespace;
//            //
//            prevItem = pathItem;
//        }
        //
        //
        StringBuffer result = new StringBuffer();
        ListIterator<PathItem> itr = path.listIterator(path.size());
        while (itr.hasPrevious()) {
            PathItem pathItem = itr.previous();
            result.append(XPATH_SEPARATOR).append(pathItem.myLocalName);
        }
        //
        return result.toString();
    }
    
    /**
     * Prepares XPath for the specified AXIOM node.
     */
    public static List<PathItem> prepareSimpleXPath(final AxiomNode axiNode) {
        //
        // Collects Path Items first
        ArrayList<PathItem> path = new ArrayList<PathItem>();
        Node currNode = axiNode;
        AxiomNode lastProcessedAxiomNode = null;
        while (currNode != null && currNode instanceof AxiomNode) {
            lastProcessedAxiomNode = (AxiomNode)currNode;
            processNode(lastProcessedAxiomNode.getReference(), null, path);
            //
            currNode = currNode.getParentNode();
        }
        //
        // Add parent elements to ensure that the XPath would be absolute
        if (lastProcessedAxiomNode != null) {
            AXIComponent axiComponent = lastProcessedAxiomNode.getReference();
            if (axiComponent != null) {
                AXIComponent parentAxiComponent = axiComponent.getParent();
                while (true) {
                    if (parentAxiComponent == null) {
                        break;
                    }
                    //
                    processNode(parentAxiComponent, null, path);
                    //
                    parentAxiComponent = parentAxiComponent.getParent();
                }
            }
        }
        //
        return path;
    }
    public static void processNode(final AXIComponent axiComponent, 
            String predicate, final ArrayList<PathItem> path) {
        String compName = null;
        //
        if (axiComponent instanceof Element) {
            compName = ((Element)axiComponent).getName();
        } else if (axiComponent instanceof Attribute) {
            compName = ((Attribute)axiComponent).getName();
        } else if (axiComponent instanceof AXIType) {
            compName = ((AXIType)axiComponent).getName();
        }
        //
        if (compName != null && compName.length() != 0) {
            String namespace = getNamespace(axiComponent);

            //
            PathItem pathItem = new PathItem(
                    axiComponent, namespace, compName, predicate);
            path.add(pathItem);
        }
    }
    
    public static String getNamespace(AXIComponent axiComponent) {
        if (axiComponent == null) {
            return null;
        }
        String ns = isUnqualified(axiComponent) ? null : axiComponent.getTargetNamespace();

        // referenced components in axi model has target namespace the same as is for schema where it is referenced but not defined
        if (axiComponent.isShared()) {
            AXIComponent refComponent = axiComponent.getSharedComponent();
            SchemaComponent refEl = refComponent != null ? refComponent.getPeer() : null;
            NamedComponentReference<GlobalElement> origRefEl = null;
            if (refEl instanceof ElementReference) {
                origRefEl = ((ElementReference) refEl).getRef();
            } 
            
            if (origRefEl != null) {
                ns = origRefEl.getEffectiveNamespace();
            }
        } 
        
        return ns;
    }
    
    public static String calculateSimpleXPath(
            final AxiomNode axiNode, AbstractDocumentComponent adc) {
        return calculateSimpleXPath(prepareSimpleXPath(axiNode), adc);
    }
    
    /**
     * This method can add new prefix definitions ot the specified WSDL.
     */
    public static String calculateSimpleXPath(
            final List<PathItem> path, AbstractDocumentComponent adc) {
        //
        Map<String, String> prefixesMap = adc.getPrefixes();
        Map<String, String> inversedPrefixesMap = getInverseMap(prefixesMap);
        //
        // Process namespaces.
        // For each namespace looks for a prefix.
        // Store the prefix in the PathItem if it's found.
        // If prefix isn't found then a new prefix definition is registered in the WSDL.
        ListIterator<PathItem> itr = path.listIterator();
        String prevItemNamespace = null;
        while (itr.hasNext()) {
            PathItem pathItem = itr.next();
            if (pathItem.myNamespace != null && pathItem.myNamespace.length() > 0){
                //check added to handle unqualified elements
                String prefix = getUniquePrefix(adc, prefixesMap,
                        inversedPrefixesMap, pathItem.myNamespace, DEFAULT_PREFIX);
                pathItem.myNamespacePrefix = prefix;
            } else {
                pathItem.myNamespacePrefix = "";
            }
        }
        //
        // Builds result string
        StringBuffer result = new StringBuffer();
        itr = path.listIterator(path.size());
        while (itr.hasPrevious()) {
            PathItem pathItem = itr.previous();
            result.append(XPATH_SEPARATOR);
            
            if (pathItem.myAxiComp instanceof Attribute) {
                result.append(XPATH_ATTR_INDICATOR);
            }
            if (pathItem.myNamespacePrefix != null && pathItem.myNamespacePrefix.length() > 0) {
                result.append(pathItem.myNamespacePrefix);
                result.append(NAMESPACE_SEPARATOR);
            }
            //
            result.append(pathItem.myLocalName);
            //
            if (pathItem.myPredicate != null) {
                result.append(pathItem.myPredicate);
            }
        }
        //
        return result.toString();
    }
    
    /**
     * Tries obtain a prefix for the specified namespace URI.
     * If the prefix isn't declared then registers a new unique prefix
     * in the specified document.
     *
     * Returns the prefix. Prefix is always unique and should be not null.
     */
    public static String getUniquePrefix(
            AbstractDocumentComponent adc,
            String namespaceUri,
            String prefixNamePattern) {
        //
        Map<String, String> prefixesMap = adc.getPrefixes();
        Map<String, String> inversedPrefixesMap = getInverseMap(prefixesMap);
        //
        return getUniquePrefix(adc, prefixesMap, inversedPrefixesMap,
                namespaceUri, prefixNamePattern);
    }
    
    /**
     *
     * If the method creates a new prefix, it puts it to the both maps.
     * So it can be used repeatedly without necessity of duplicative request
     * of the prefixes' map.
     *
     */
    private static String getUniquePrefix(
            AbstractDocumentComponent adc,
            Map<String, String> prefixesMap, // from prefix to namespace
            Map<String, String> inversedPrefixesMap, // from namespace to prefix
            String namespaceUri,
            String prefixNamePattern) {
        //
        String prefix = inversedPrefixesMap.get(namespaceUri);
        if (prefix != null && prefix.length() > 0) {
            return prefix;
        }
        //
        int counter = 1;
        String newPrefixCandidate;
        while (true) {
            newPrefixCandidate = prefixNamePattern + counter;
            if (!prefixesMap.containsKey(newPrefixCandidate)) {
                break;
            }
            counter++;
        }
        //
        AbstractDocumentModel model = adc.getModel();
        if (!model.isIntransaction()) {
            model.startTransaction();
        }
        try {
            adc.addPrefix(newPrefixCandidate, namespaceUri);
        } finally {
            if (model.isIntransaction()) {
                model.endTransaction();
            }
        }
        prefixesMap.put(newPrefixCandidate, namespaceUri);
        inversedPrefixesMap.put(namespaceUri, newPrefixCandidate);
        //
        return newPrefixCandidate;
    }
    
    public static class PathItem {
        public AXIComponent myAxiComp;
        public String myNamespacePrefix;
        public String myNamespace;
        public String myLocalName;
        public String myPredicate;
        
        public PathItem(AXIComponent axiComp, String namespace, 
                String name, String predicate) {
            myAxiComp = axiComp;
            myNamespace = namespace;
            myLocalName = name;
            myPredicate = predicate;
        }
        
        public QName constructQName() {
            return new QName(myNamespace, myLocalName, myNamespacePrefix);
        }
    }
    
    /**
     * Constructs the inverse map where keys and values are change each others.
     */
    public static <A,B> Map<B,A> getInverseMap(Map<A,B> map) {
        if (map == null) {
            return null;
        }
        //
        Map<B,A> resultMap = new HashMap<B,A>(map.size());
        for (Map.Entry<A,B> entry : map.entrySet()) {
            A key = entry.getKey();
            B value = entry.getValue();
            //
            if (value != null && key != null) {
                resultMap.put(value, key);
            }
        }
        //
        return resultMap;
    }
    
    public static SchemaComponent getPartType(Part part) {
        SchemaComponent result = null;
        //
        NamedComponentReference<GlobalElement> elementRef = part.getElement();
        if (elementRef != null) {
            result = elementRef.get();
        }
        //
        if (result == null) {
            NamedComponentReference<? extends GlobalType> typeRef = part.getType();
            if (typeRef != null) {
                result = typeRef.get();
            }
        }
        //
        return result;
    }
    
    public static String getElementMultiplicityStr(Element element) {
        if (element != null) {
            String min = element.getMinOccurs();
            String max = element.getMaxOccurs();
            if (min.equals("1") && max.equals("1")) { // NOI18N
                return null;
            }
            if (NumberBase.UNBOUNDED_STRING.equals(max)) {
                max = "*"; // NOI18N
            }
            return "[" + min + ".." + max + "]"; // NOI18N
        }
        return null;
    }
    
    public static BadgeModificator getElementBadge(Element element) {
        if (element != null) {
            String min = element.getMinOccurs();
            String max = element.getMaxOccurs();
            if (min.equals("1") && max.equals("1")) { // NOI18N
                return BadgeModificator.SINGLE;
            }
            //
            boolean isOptional = min.equals("0"); // NOI18N
            //
            boolean isRepeating = false;
            if (NumberBase.UNBOUNDED_STRING.equals(max)) {
                isRepeating = true;
            } else {
                try {
                    int maxInt = Integer.parseInt(max);
                    if (maxInt > 1) {
                        isRepeating = true;
                    }
                } catch (NumberFormatException ex) {
                    // DO NOTHING HERE
                }
            }
            //
            if (isOptional && isRepeating) {
                return BadgeModificator.OPTIONAL_REPEATING;
            } else if (isOptional) {
                return BadgeModificator.OPTIONAL;
            } else if (isRepeating) {
                return BadgeModificator.REPEATING;
            }
        }
        return BadgeModificator.SINGLE;
    }
    
    public static String getAttributeTooltip(Attribute attribute) {
        if (attribute == null) {
            return null;
        }
        //
        String result;
        AXIType type = attribute.getType();
        String typeName = type != null ? type.getName() : null;
        String isOptionalText = (attribute.getUse() == Use.OPTIONAL) ?
            "OPTIONAL" : null;  // NOI18N
        //
        if (typeName == null) {
            result = attribute.getName(); // NOI18N
        } else {
            result = SoaUtil.getFormattedHtmlString(true,
                    new SoaUtil.TextChunk(attribute.getName()),
                    new SoaUtil.TextChunk(isOptionalText, SoaUtil.HTML_GRAY),
                    new SoaUtil.TextChunk(typeName, SoaUtil.HTML_GRAY));
        }
        //
        return result;
    }
    
    public static String getElementTooltip(Element element) {
        if (element == null) {
            return null;
        }
        //
        String result;
        AXIType type = element.getType();
        String typeName = type != null ? type.getName() : null;
        String multiplisity = getElementMultiplicityStr(element);
        //
        result = SoaUtil.getFormattedHtmlString(true,
                new SoaUtil.TextChunk(element.getName()),
                new SoaUtil.TextChunk(multiplisity, SoaUtil.HTML_GRAY),
                new SoaUtil.TextChunk(typeName, SoaUtil.HTML_GRAY));
        //
        return result;
    }
    
    /**
     * this function TRIES to access
     * attributeFormDefault|elementFormDefault attribute in chema, defining the given element
     * @type is element to check. may be Attribute or Element
     * @returns true if it was found and equals to "unqualified""
     **/
    public static  boolean isUnqualified(AXIComponent type){
        //ltl bit paranoic tests to avoid NPEs.
        if (type == null) {
            return false;
        }
        
        if (type.isGlobal()){
            return false;
        }
        
        SchemaComponent peer = type.getPeer();
        if (peer == null){
            return false;
        }
        
        Form form = null;
        if (peer instanceof LocalElement){
            form = ((LocalElement) peer).getFormEffective();
        } else if (peer instanceof LocalAttribute){
            form = ((LocalAttribute) peer).getFormEffective();
            
        }
        return Form.UNQUALIFIED.equals(form);
    }
    
}
