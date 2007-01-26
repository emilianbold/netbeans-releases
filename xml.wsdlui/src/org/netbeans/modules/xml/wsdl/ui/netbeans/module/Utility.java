/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.actions.schema.ExtensibilityElementCreatorVisitor;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.OptionalAttributeFinderVisitor;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.ErrorManager;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class Utility {
    
    public static String getNamespacePrefix(String namespace, WSDLModel model) {
        if (model != null && namespace != null) {
            return ((AbstractDocumentComponent) model.getDefinitions()).lookupPrefix(namespace);
        }
        return null;
    }
    
    public static String getNamespacePrefix(String namespace, WSDLComponent element) {
        if (element != null && namespace != null) {
            return ((AbstractDocumentComponent) element).lookupPrefix(namespace);
        }
        return null;
    }
    
    public static String getNamespaceURI(String prefix, WSDLComponent element) {
        if (element != null && prefix != null) {
            return ((AbstractDocumentComponent) element).lookupNamespaceURI(prefix, true);
        }
        return null;
    }
    public static String getNamespaceURI(String prefix, WSDLModel model) {
        if (model != null && prefix != null) {
            return ((AbstractDocumentComponent) model.getDefinitions()).lookupNamespaceURI(prefix, true);
        }
        return null;
    }
    
    public static Import getImport(String namespace, WSDLModel model) {
        Collection imports = model.getDefinitions().getImports();
        if (imports != null) {
            Iterator iter = imports.iterator();
            for (;iter.hasNext();) {
                Import existingImport = (Import) iter.next();
                if (existingImport.getNamespace().equals(namespace)) {
                    return existingImport;
                }
            }
        }
        return null;
    }
    
    public static Collection<WSDLModel> getImportedDocuments(WSDLModel model) {
        Collection<Import> imports = model.getDefinitions().getImports();
        Collection<WSDLModel> returnImports = new ArrayList<WSDLModel>();
        if (imports != null) {
            Iterator iter = imports.iterator();
            for (;iter.hasNext();) {
                Import existingImport = (Import) iter.next();
                List<WSDLModel> impModels = model.findWSDLModel(existingImport.getNamespace());
                returnImports.addAll(impModels);
            }
        }
        return returnImports;
    }
    
    public static Map getNamespaces(Definitions def) {
        return ((AbstractDocumentComponent) def).getPrefixes();
    }
    
    public static GlobalElement findGlobalElement(Schema schema, String elementName) {
        Collection gElem = schema.findAllGlobalElements();
        if (gElem !=null){
            Iterator iter = gElem.iterator();
            while (iter.hasNext()) {
                GlobalElement elem = (GlobalElement) iter.next();
                if (elem.getName().equals(elementName)) {
                    return elem;
                }
            }
        }
        return null;
    }
    
    public static GlobalType findGlobalType(Schema schema, String typeName) {
        Collection gTypes = schema.findAllGlobalTypes();
        if (gTypes !=null){
            Iterator iter = gTypes.iterator();
            while (iter.hasNext()) {
                GlobalType type = (GlobalType) iter.next();
                if (type.getName().equals(typeName)) {
                    return type;
                }
            }
        }
        return null;
    }
    
    public static String fromQNameToString(QName qname) {
        if (qname.getPrefix() != null && qname.getPrefix().trim().length() > 0) {
            return qname.getPrefix() + ":" + qname.getLocalPart();
        }
        return qname.getLocalPart();
    }
    
    public static String getNameAndDropPrefixIfInCurrentModel(String ns, String localPart, WSDLModel model) {
        if (ns == null || model == null)
            return localPart;
        
        
        if (!model.getDefinitions().getTargetNamespace().equals(ns)) {
            String prefix = getNamespacePrefix(ns, model);
            if (prefix != null) 
                return prefix + ":" + localPart;
        }

        return localPart;
    }    
    
    
    public static List<QName> getExtensionAttributes(WSDLComponent comp) {
        ArrayList<QName> result = new ArrayList<QName>();
        Map<QName, String> attrMap = comp.getAttributeMap();
        Set<QName> set = attrMap.keySet();
        if (set != null) {
            Iterator<QName> iter = set.iterator();
            while (iter.hasNext()) {
                QName name =  iter.next();
                String ns = name.getNamespaceURI();
                if (ns != null && ns.trim().length() > 0 && !ns.equals(((AbstractDocumentComponent) comp).getQName().getNamespaceURI())) {
                    result.add(name);
                }
            }
        }
        return result;
    }
    public static List<QName> getOptionalAttributes(WSDLComponent comp, Element elem) {
        ArrayList<QName> result = new ArrayList<QName>();
        Map<QName, String> attrMap = comp.getAttributeMap();
        Set<QName> set = attrMap.keySet();
        if (set != null) {
            Iterator<QName> iter = set.iterator();
            while (iter.hasNext()) {
                QName name =  iter.next();
                String ns = name.getNamespaceURI();
                if (ns != null && ns.trim().length() > 0 && !ns.equals(comp.getModel().getDefinitions().getTargetNamespace())) {
                    //extension attibute
                    //do nothing
                } else {
                    //not a extension attribute
                    OptionalAttributeFinderVisitor visitor = new OptionalAttributeFinderVisitor(name.getLocalPart());
                    elem.accept(visitor);
                    if (visitor.isOptional()) {
                        result.add(name);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Expands nodes on the treeview till given levels
     * @param tv the treeview object
     * @param level the level till which the nodes should be expanded. 0 means none.
     * @param rootNode the rootNode
     */
    public static void expandNodes(TreeView tv, int level, Node rootNode) {
        if (level == 0) return;
        
        Children children = rootNode.getChildren();
        if (children != null) {
            Node[]  nodes = children.getNodes();
            if (nodes != null) {
                for (int i= 0; i < nodes.length; i++) {
                    tv.expandNode(nodes[i]); //Expand node
                    expandNodes(tv, level - 1, nodes[i]); //expand children
                }
            }
        }
    }
    
    public static void addNamespacePrefix(Element schemaElement,
            WSDLComponent element,
            String prefix) {
        if(schemaElement != null) {
            Schema schema = schemaElement.getModel().getSchema();
            
            if(schema != null) {
                if(element != null) {
                    WSDLModel document = element.getModel();
                    Definitions definitions = document.getDefinitions();
                    String targetNamespace = schema.getTargetNamespace();
                    String computedPrefix = null;
                    
                    if(targetNamespace != null) {
                        if (Utility.getNamespacePrefix(targetNamespace, document) != null) {
                            //already exists, doesnt need to be added
                            return;
                        }
                        //Use the prefix (in parameter) or generate new one.
                        if(prefix != null) {
                            computedPrefix = prefix;
                        } else {
                            computedPrefix = NameGenerator.getInstance().generateNamespacePrefix(null, document.getDefinitions());
                        }
                        boolean isAlreadyInTransaction = Utility.startTransaction(document);
                        ((AbstractDocumentComponent)definitions).addPrefix(computedPrefix, schema.getTargetNamespace());
                        
                        try {
                            Utility.endTransaction(document, isAlreadyInTransaction);
                        } catch (IOException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                        
                        
                    }
                    
                }
            }
        }
        
    }
    
    public static void addExtensibilityElement(WSDLComponent element, Element schemaElement, String prefix) {
        Utility.addNamespacePrefix(schemaElement, element, prefix);
        ExtensibilityElementCreatorVisitor eeCreator = new ExtensibilityElementCreatorVisitor(element);
        schemaElement.accept(eeCreator);
    }
    
    public static boolean startTransaction(WSDLModel model) {
        boolean isInTransaction = model.isIntransaction();
        if (isInTransaction) return true;
        model.startTransaction();
        return false;
    }
    
    public static void endTransaction(WSDLModel model, boolean isInTransaction) throws IOException {
        if (isInTransaction) return;
        model.endTransaction();
    }
    
    public static Collection<Operation> getImplementableOperations(PortType portType, Binding binding) {
        if (portType == null || portType.getOperations() == null || portType.getOperations().size() == 0
                || binding == null) {
            return null;
        }
        List<Operation> listData = new ArrayList<Operation>(portType.getOperations().size());
        Set<String> bindingOperationsSet = new HashSet<String>();
        Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
        if (bindingOperations != null) {
            Iterator<BindingOperation> iter = bindingOperations.iterator();
            while (iter.hasNext()) {
                bindingOperationsSet.add(iter.next().getOperation().get().getName());
            }
            
        }
        Iterator it = portType.getOperations().iterator();
        
        while(it.hasNext()) {
            Operation operation = (Operation) it.next();
            if(operation.getName() != null) {
                if (!bindingOperationsSet.contains(operation.getName())) {
                    listData.add(operation);
                }
            }
        }
        
        return listData;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String,String> getPrefixes(WSDLComponent wsdlComponent) {
        AbstractDocumentComponent comp = ((AbstractDocumentComponent) wsdlComponent);
        Map<String,String> prefixes = comp.getPrefixes();
        while(comp.getParent() != null) {
            comp = (AbstractDocumentComponent) comp.getParent();
            prefixes.putAll(comp.getPrefixes());
        }
        
        return prefixes;
    }
    
    public static void splitExtensibilityElements(List<ExtensibilityElement> list, 
            Set<String> specialTargetNamespaces, 
            List<ExtensibilityElement> specialExtensibilityElements,
            List<ExtensibilityElement> nonSpecialExtensibilityElements) {
        if (specialExtensibilityElements == null) {
            specialExtensibilityElements = new ArrayList<ExtensibilityElement>();
        }
        if (nonSpecialExtensibilityElements == null) {
            nonSpecialExtensibilityElements = new ArrayList<ExtensibilityElement>();
        }
        
        if (list != null) {
            for (ExtensibilityElement element : list) {
                if (specialTargetNamespaces.contains(element.getQName().getNamespaceURI())) {
                    specialExtensibilityElements.add(element);
                } else {
                    nonSpecialExtensibilityElements.add(element);
                }
            }
        }
    }
    
    public static List<ExtensibilityElement> getSpecialExtensibilityElements(List<ExtensibilityElement> list, 
            String specialNamespace) {
        List<ExtensibilityElement> specialList = new ArrayList<ExtensibilityElement>();
        if (list != null) {
            for (ExtensibilityElement element : list) {
                if (specialNamespace.equals(element.getQName().getNamespaceURI())) {
                    specialList.add(element);
                }
            }
        }
        return specialList;
    }
    
    /**
     * This method finds the absolute index in the definitions where the component needs to be inserted, 
     * such that the component is at given index with respect to its kind.
     * 
     * For example, There are 5 messages, and one needs to insert another at index 4. Then this method will insert
     * it at some index on Definitions, which will make it look like the 4th Message.
     * 
     * it doesnt call startTransaction or endTransaction, so its the responsibility of the caller to do it.
     * 
     * @param index
     * @param model
     * @param compToInsert
     * @param propertyName
     */
    public static void insertIntoDefinitionsAtIndex(int index, WSDLModel model, WSDLComponent compToInsert, String propertyName) {
        assert model.isIntransaction() : "Need to call startTransaction on this model, before calling this method";
        //find index among all definitions elements. 
        //for inserting at index = 5, find index of the 4th PLT and insert after this index
        int defIndex = -1;
        int indexOfPreviousPLT = index - 1;
        List<WSDLComponent> comps = model.getDefinitions().getChildren();
        for (int i = 0; i < comps.size(); i++) {
            WSDLComponent comp = comps.get(i);
            if (compToInsert.getClass().isAssignableFrom(comp.getClass())) {
                if (indexOfPreviousPLT > defIndex) {
                    defIndex ++;
                } else {
                    ((AbstractComponent<WSDLComponent>) model.getDefinitions()).insertAtIndex(propertyName, compToInsert, i);
                    break;
                }
            }
        }
    }
}
