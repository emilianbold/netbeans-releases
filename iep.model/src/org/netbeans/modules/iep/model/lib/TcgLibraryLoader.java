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

package org.netbeans.modules.iep.model.lib;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.openide.util.NbBundle;

public class TcgLibraryLoader {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TcgLibraryLoader.class.getName());

    public static final String TITLE = NbBundle.getMessage(TcgLibraryLoader.class,"TcgLibraryLoader.TcgComponent_Type_Library");

    public static final String ICON_NAME = "";

    public static TcgComponentTypeGroup loadLibraries() {
        List libraryXMLList = new ArrayList();
        String tcgLibPath = Configuration.getVarByName("tcglib.path");
        StringTokenizer st = new StringTokenizer(tcgLibPath, ";");
        while (st.hasMoreTokens()) {
            libraryXMLList.add(st.nextToken());
        }
        return loadLibraries(libraryXMLList);
    }

    /**
     * Load all the library.xml files in the subdirectories of $vmRoot
     *
     * @return the root component_type_group: '/' with the tree rooted at it
     */
    public static TcgComponentTypeGroup loadLibraries(List libraryXMLList) {
        //mLog.debug("loadLibraries libraryXMLList: " + libraryXMLList);
        TcgComponentTypeGroup libRoot =
            new TcgComponentTypeGroupImpl(TITLE, TITLE, TITLE, ICON_NAME);
        try {
            //mLog.debug("Pass 1");
            // Pass 1
            // 1. populate the tree rooted at libRoot with component_type_group
            // 2. get the denpency among all the component_types, and that among property_groups
            Map topLevelItemMap = new HashMap();
            for (int i = 0; i < libraryXMLList.size(); i++) {
                String libraryPath = (String) libraryXMLList.get(i);
                loadComponentTypeLibrary(libraryPath, libRoot, topLevelItemMap);
            }

            //mLog.debug("Pass 2");
            // Pass 2
            // 1. Load each top-level property_group
            // 2. Load each top-level component_type and its components, and insert it into
            //   its component_type_group
            loadTopLevelPropertyGroups(topLevelItemMap);
            loadTopLevelComponentTypes(topLevelItemMap);
        } catch (Exception e) {
            mLog.warning(e.getMessage());
            e.printStackTrace();
        }
        return libRoot;
    }

    //==========================================================================

    // PASS 1

    /**
     * This takes the file path of libary.xml and recurses through the children
     * until each one is added to a root TcgComponentTypeGroup.
     *
     * @param libraryPath the file path of library.xml
     *
     * @return Returns a TcgComponentTypeGroup object based on the doc passed in
     *
     * @exception ParseXmlException Description of the Exception
     */
    private static void loadComponentTypeLibrary(
        String libraryPath,
        TcgComponentTypeGroup libRoot, Map topLevelItemMap)
        throws ParseXmlException {
        //mLog.debug("loadComponentTypeLibrary libraryPath: " + libraryPath);
        try {
            Document doc = parseXML(libraryPath);
            Element root =
                (Element) doc.getDocumentElement();
            TcgComponentTypeGroup ctg =
                loadComponentTypeGroup(root, "/", topLevelItemMap);

            if (ctg == null) {
                throw new ParseXmlException("TcgModelManager.BAD_COMPONENT_ERROR",
                                            "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                            new Object[] {libraryPath});
            }
            libRoot.addComponentTypeGroup(ctg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParseXmlException("TcgModelManager.PARSE_XML_FAILED",
                                        "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                        new Object[] {libraryPath}, e);
        }
    }

    /**
     * Takes a component_type_group Element, recursively build the component_type_group
     * tree rooted at it, and register any property_group definitions, and top-level
     * component_types found
     * 
     * @param root the component_type_group Element
     * @param componentTypePath the path from '/' component_type_group to this component_type_group
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     *
     * @return the component_type_group and its sub-tree
     */
    private static TcgComponentTypeGroup loadComponentTypeGroup(
        Element root, 
        String componentTypePath, Map topLevelItemMap)
        throws ParseXmlException {
        NamedNodeMap attrs = root.getAttributes();
        String name = attrs.getNamedItem("name").getNodeValue();
        String title = attrs.getNamedItem("title").getNodeValue();
        String description = attrs.getNamedItem("description").getNodeValue();
        String icon = attrs.getNamedItem("icon").getNodeValue();
        if ((name == null) || (title == null)
            || name.equals("") || title.equals("")
            || (icon == null)) {
            throw new ParseXmlException("TcgLibraryLoader.ATTRIBUTES_NOT_FOUND",
                                        "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                        new Object[] {root});
        }

        TcgComponentTypeGroup ctg = new TcgComponentTypeGroupImpl(name, title, description, icon);
        componentTypePath = componentTypePath + name + "/";
        topLevelItemMap.put(componentTypePath, ctg);
        //mLog.debug("loadComponentTypeGroup  adding key: " + componentTypePath + " ctg: " + ctg);

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String childName = child.getNodeName();
            if (Node.ELEMENT_NODE == child.getNodeType()) {
                if (childName.equals("component_type_group")) {
                    ctg.addComponentTypeGroup(
                        loadComponentTypeGroup(
                            (Element) child, componentTypePath, topLevelItemMap));
                } else if (childName.equals("component_type")) {
                    registerTopLevelComponentType(
                        (Element) child, componentTypePath, topLevelItemMap);
                } else if (childName.equals("property_group")) {
                    registerTopLevelPropertyGroup(
                        (Element) child, componentTypePath, topLevelItemMap);
                }
            }
        }
        return ctg;
    }

    /**
     * Takes a property Element, creates a TopLevelItem object for it, and
     * registers this item at topLevelItemMap
     *
     * @param root the property Element
     * @param topLevelPath the full path from '/' component_type_group to this property
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     */
    private static void registerTopLevelPropertyGroup(
        Element root,
        String topLevelPath, Map topLevelItemMap) {
        NamedNodeMap attrs = root.getAttributes();
        try {
            Node node = attrs.getNamedItem("name");
            String name = node.getNodeValue();
            TopLevelItem item = new TopLevelItem(
                topLevelPath, name, root, TopLevelItem.PG);
            NodeList props = root.getChildNodes();
            for (int i = 0; i < props.getLength(); i++) {
                Node prop = props.item(i);
                if (prop.getNodeName().equals("property_group")) {
                    Node ref = prop.getAttributes().getNamedItem("ref");
                    String s = ref.getNodeValue();
                    if (s.indexOf('/') < 0) {
                        // expands to full name if super class is in the same package
                        s = topLevelPath + s;
                    }
                    item.mDependencySet.add(s);
                }
            }
            topLevelItemMap.put(topLevelPath + name, item);
            //mLog.debug("registerTopLevelPropertyGroup adding key: " + topLevelPath + name + " item: " + item);
        } catch (Exception e) {
            mLog.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Takes a component_type Element, creates a TopLevelItem object for it, and
     * registers this item at topLevelItemMap
     *
     * @param root the component_type Element
     * @param topLevelPath the full path from '/' component_type_group to this component_type
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     */
    private static void registerTopLevelComponentType(
        Element root,
        String topLevelPath, Map topLevelItemMap) {
        NamedNodeMap attrs = root.getAttributes();
        try {
            Node node = attrs.getNamedItem("name");
            String name = node.getNodeValue();
            TopLevelItem item = new TopLevelItem(
                topLevelPath, name, root, TopLevelItem.CT);
            findTcgComponentTypeDependency(root, topLevelPath, item.mDependencySet);
            topLevelItemMap.put(topLevelPath + name, item);
            //mLog.debug("registerTopLevelComponentType adding key: " + topLevelPath + name + " item: " + item);
        } catch (Exception e) {
            mLog.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Takes a component_type Element, recursively find the top-level component_types that this 
     * component_type depends on directly through "extends", or indirectly throgh its contained
     * component_types.
     * 
     * @param root the Element for a component_type
     * @param topLevelPath the full name of the top-level component_type that either be this component_type
     *        or contains this component_type
     * @param dependencySet the Set that will store all the component_types this component_typs depends on
     */
    private static void findTcgComponentTypeDependency(
        Element root,
        String topLevelPath, Set dependencySet) {
        NamedNodeMap attrs = root.getAttributes();
        Node node = attrs.getNamedItem("extends");
        if (node != null) {
            String s = node.getNodeValue();
            if (s.indexOf('/') < 0) {
                // expands to full name if super class is in the same package
                s = topLevelPath + s;
            }
            dependencySet.add(s);
        }
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (Node.ELEMENT_NODE == child.getNodeType()) {
                if (child.getNodeName().equals("component_type")) {
                    findTcgComponentTypeDependency(
                        (Element) child, topLevelPath, dependencySet);
                }
            }
        }
    }

    /**
     * This is a helper method for unit testing. Takes in the xml file location
     * in the string format populates the document from it and returns the
     * Document.
     *
     * @param libraryPath java.lang.String
     *
     * @return Returns an dom Element object for the root node
     *
     * @exception ParseXmlException Description of the Exception
     */
    private static Document parseXML(String libraryPath)
        throws ParseXmlException {
        //mLog.debug("libraryPath: " + libraryPath);
        Document doc = null;
        try {
            InputStream is = IOUtil.getResourceAsStream(libraryPath);
            doc = DOMUtil.createDocument(
                true, new InputSource(is));
        } catch (Exception e) {
            throw new ParseXmlException("TcgLibraryLoader.XML_LOAD_ERROR", 
                                        "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                        new Object[] {libraryPath}, e);
        }
        return doc;
    }

    //==========================================================================

    // PASS 2

    /**
     * Load all the property_groups
     *
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     */
    private static void loadTopLevelPropertyGroups(Map topLevelItemMap) {
        for (Iterator it = topLevelItemMap.values().iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof TopLevelItem) {
                TopLevelItem item = (TopLevelItem) o;
                if (item.mType.equals(TopLevelItem.PG)) {
                    loadTopLevelPropertyGroup(item, topLevelItemMap);
                }
            }
        }
    }

    /**
     * Load each top-level component_type, and its subcomponents, and put it into its component_type_group
     *
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     */
    private static void loadTopLevelComponentTypes(Map topLevelItemMap) throws ComponentTypeLoadException {
        for (Iterator it = topLevelItemMap.values().iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof TopLevelItem) {
                TopLevelItem item = (TopLevelItem) o;
                if (item.mType.equals(TopLevelItem.CT)) {
                    loadTopLevelComponentType(item, topLevelItemMap);
                }
            }
        }
    }

    /**
     * Recursively load the property_group that the item refers, and all property_groups on
     * this property_group depends on
     *
     * @param item the TopLevelItem for the property_group
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     */
    private static void loadTopLevelPropertyGroup(
        TopLevelItem item, Map topLevelItemMap) {
        // If the item is already loaded, return
        if (topLevelItemMap.get(item.mPkg + item.mName) instanceof List) {
            return;
        }
        List propTypeList = new ArrayList();
        // If the item has a dependencySet, load items in the dependencySet first
        if (!item.mDependencySet.isEmpty()) {
            for (Iterator it = item.mDependencySet.iterator(); it.hasNext();) {
                Object k = it.next();
                Object v = topLevelItemMap.get(k);
                if (v instanceof List) {
                    propTypeList.addAll((List) v);
                } else {
                    loadTopLevelPropertyGroup(
                        (TopLevelItem) v, topLevelItemMap);
                    propTypeList.addAll(
                        (List) topLevelItemMap.get(k));
                }
            }
        }
        // Load the item adding to propTypeList
        NodeList props = item.mElement.getChildNodes();
        for (int i = 0; i < props.getLength(); i++) {
            Node prop = props.item(i);
            if (prop.getNodeName().equals("property")) {
                loadProperty(
                    (Element) prop, propTypeList);
            }
        }
        topLevelItemMap.put(item.mPkg + item.mName, propTypeList);
        //mLog.debug("loadTopLevelPropertyGroup adding key: " + item.mPkg + item.mName + " propTypeList: " + propTypeList);
    }

    /**
     * Recursively load each component_types on this item's dependencySet list, and add
     * it to its component_type_group. Then load this item's component_type, and its
     * elements
     *
     * @param item the TopLevelItem corresponding to the top-level component_type
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     *
     */
    private static void loadTopLevelComponentType (TopLevelItem item, Map topLevelItemMap) 
        throws ComponentTypeLoadException {
        // If the item is already loaded, return
        if (topLevelItemMap.get(item.mPkg + item.mName) instanceof TcgComponentType) {
            return;
        }    
        // If the item has a dependencySet, load items in the dependencySet first
        if (!item.mDependencySet.isEmpty()) {
            for (Iterator it = item.mDependencySet.iterator(); it.hasNext();) {
                Object k = it.next();
                Object v = topLevelItemMap.get(k);
                if (v instanceof TcgComponentType) {
                    // continue
                } else {
                    loadTopLevelComponentType(
                        (TopLevelItem) v, topLevelItemMap);
                }
            }
        }
        // Load the item adding to the component type group
        TcgComponentType compType =
            loadComponentType(
                item.mElement, item.mPkg, item.mPkg, topLevelItemMap);
        topLevelItemMap.put(item.mPkg + item.mName, compType);
        //mLog.debug("loadTopLevelComponentType  adding key: " + item.mPkg + item.mName + " compType: " + compType);

        TcgComponentTypeGroup compTypeGroup =
            (TcgComponentTypeGroup) topLevelItemMap.get(item.mPkg);
        compTypeGroup.addComponentType(compType);
    }

    /**
     * This takes a component_type Element and retrieves all the child elements
     * (component_type, property_group_ref, property and code) to recursively build 
     * a TcgComponentType and its subtree.
     *
     * @param root org.w3c.dom.Element for the component_type
     * @param componentTypePath the real path of component_type
     * @param topLevelPath the real path of the top-level component_type that contains this component_type
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     *
     * @return Returns a TcgComponentType object based on the root Element
     *         passed in
     */
    private static TcgComponentType loadComponentType(
        Element root, String componentTypePath, String topLevelPath, Map topLevelItemMap) 
        throws ComponentTypeLoadException {
        //mLog.info("loadComponentType componentTypePath: " + componentTypePath + " topLevelPath: " + topLevelPath);
        TcgComponentType superType = null;
        TcgComponentType component = null;
        java.util.List codeList = new ArrayList();
        java.util.List propTypeList = new ArrayList();
        java.util.List compList = new ArrayList();
        String name = null;
        String title = null;
        String description = null;
        String icon = null;
        TcgComponentValidator validator = null;
        boolean allowsChildren = false;
        boolean visible = false;

        NamedNodeMap attrs = root.getAttributes();
        try {
            Node node = attrs.getNamedItem("extends");
            if (node != null) {
                String s = node.getNodeValue();
                if (s.indexOf('/') < 0) {
                    // expands to full name if super class is in the same package
                    s = topLevelPath + s;
                }
                superType =
                    (TcgComponentType) topLevelItemMap.get(s);
                //mLog.debug("extends: " + s);
                propTypeList.addAll(superType.getPropertyTypeList());    
            }
            // FIXME These can throw NullPointerException if superType == null
            node = attrs.getNamedItem("name");
            name = node == null ? superType.getName() : node.getNodeValue();
            //mLog.debug("name: " + name);

            node = attrs.getNamedItem("title");
            title = node == null ? superType.getTitle() : node.getNodeValue();

            node = attrs.getNamedItem("description");
            description = node == null ? superType.getDescription() : node.getNodeValue();

            node = attrs.getNamedItem("icon");
            icon = node == null ? superType.getIconName(): node.getNodeValue();

            node = attrs.getNamedItem("allowsChildren");
            allowsChildren = node == null ? superType.getAllowsChildren() :
                (Boolean.valueOf(node.getNodeValue())).booleanValue();

            node = attrs.getNamedItem("visible");
            visible = node == null ? superType.isVisible() :
                (Boolean.valueOf(node.getNodeValue())).booleanValue();

            node = attrs.getNamedItem("validator");
            validator = node == null ? superType.getValidator(): 
                (TcgComponentValidator)Class.forName(node.getNodeValue()).newInstance();
        } catch (Throwable e) {
            mLog.warning(e.getMessage());
            e.printStackTrace();
            throw new ComponentTypeLoadException("TcgLibraryLoader.FAIL_TO_LOAD_COMPONENT_TYPE", 
                                        "org.netbeans.modules.iep.editor.tcg.model.Bundle",
                                        new Object[]{componentTypePath}, e);
        }

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String childName = child.getNodeName();
            if (Node.ELEMENT_NODE == child.getNodeType()) {
                if (childName.equals("component_type")) {
                    compList.add(
                        loadComponentType(
                            (Element) child, componentTypePath + name + "|", 
                            topLevelPath, topLevelItemMap));
                } else if (childName.equals("property")) {
                    loadProperty(
                        (Element) child, propTypeList);
                } else if (childName.equals("property_group")) {
                    propTypeList.addAll(
                        loadPropertyGroupRef(
                            (Element) child, topLevelPath, topLevelItemMap));
                } else if (childName.equals("code")) {
                    attrs = child.getAttributes();

                    String filePath = attrs.getNamedItem("file").getNodeValue();
                    TcgCodeType codeType =
                        new TcgCodeTypeImpl(
                            attrs.getNamedItem("type").getNodeValue(), filePath);
                    codeList.add(codeType);
                }
            }
        }

        TcgCodeType[] codeTypes =
            (TcgCodeType[]) codeList.toArray(new TcgCodeType[0]);
        TcgPropertyType[] propTypes =
            (TcgPropertyType[]) propTypeList.toArray(new TcgPropertyType[0]);
        TcgComponentType[] compTypes =
            (TcgComponentType[]) compList.toArray(new TcgComponentType[0]);

        if (superType == null) {
            component = new TcgComponentTypeImpl(name,
                                                 componentTypePath + name,
                                                 title, 
                                                 description,
                                                 icon,
                                                 allowsChildren, 
                                                 visible,
                                                 codeTypes, 
                                                 propTypes,
                                                 compTypes,
                                                 validator);
        } else {
           component = superType.duplicate(name,
                                           componentTypePath + name,
                                           title, 
                                           description,
                                           icon,
                                           allowsChildren,
                                           visible, 
                                           codeList, 
                                           propTypeList,
                                           compList,
                                           validator);
        }
        return component;
    }

    /**
     * This takes a DOM Element for a property_group_ref, and the symbol table 
     * Retrieve the property list of the corresponding property_group, add/override
     * the properties specified in the property_group_ref, and return the list
     * 
     *
     * @param root org.w3c.dom.Element for the property_group_ref
     * @param topLevelPath used to resolve the full name of the referred property_group
     *        when ref uses just the name of the referred property_group
     * @param topLevelItemMap symbol table containing all the component_type_groups, property_group 
     *        definitions, and top-level component_type definitions
     *
     * @return a List of TcgPropertyType
     */
    private static List loadPropertyGroupRef(
        Element root,
        String topLevelPath, Map topLevelItemMap) {
        List propTypeList = new ArrayList();
        NamedNodeMap attrs = root.getAttributes();
        Node node = attrs.getNamedItem("ref");
        if (node != null) {
            List refList = new ArrayList();
            String s = node.getNodeValue();
            // expands to full name if super class is in the same package
            if (s.indexOf('/') < 0) {
                s = topLevelPath + s;
            }
            Object o = topLevelItemMap.get(s);
            if (o != null) {
                refList = (List) o;
            }
            //mLog.debug("loadPropertyGroupRef key: " + topLevelPath + s + " refList: " + refList);
            propTypeList.addAll(refList);
            NodeList props = root.getChildNodes();
            for (int i = 0; i < props.getLength(); i++) {
                Node prop = props.item(i);
                if (prop.getNodeName().equals("property")) {
                    loadProperty(
                        (Element) prop, propTypeList);
                }
            }
        }
        return propTypeList;
    }

    /**
     * This takes a DOM Element for a property and populates all the attributes
     * Replace the property in propTypeList if it has the same name as the new one's
     *
     * @param root org.w3c.dom.Element
     * @param propTypeList the property list to update
     */
    private static void loadProperty(Element root, List propTypeList) {
        if (root.getNodeName().equals("property")) {
            TcgPropertyType propType = null;
            NamedNodeMap attrs = root.getAttributes();
            String name = attrs.getNamedItem("name").getNodeValue();
            for (int i = 0; i < propTypeList.size(); i++) {
                TcgPropertyType prototype =
                    (TcgPropertyType) propTypeList.get(i);
                if (prototype.getName().equals(name)) {
                    propTypeList.remove(i);
                    propType = newTcgPropertyType(prototype, root);
                    propTypeList.add(i, propType);
                    break;
                 }
            }
            if (propType == null) {
                String script = null;
                NodeList scripts = root.getElementsByTagName("script");
                if (scripts.getLength() > 0) {
                    script = loadScript(
                        (Element) scripts.item(0));
                }
                propType = new TcgPropertyTypeImpl(
                    getNodeValue(attrs, "name"),
                    getNodeValue(attrs, "title"),
                    getNodeValue(attrs, "type"),
                    getNodeValue(attrs, "description"),
                    getNodeValue(attrs, "editor"),
                    getNodeValue(attrs, "renderer"),
                    getNodeValue(attrs, "default"),
                    getNodeValue(attrs, "access"),
                    (Boolean.valueOf(getNodeValue(attrs, "multiple"))).booleanValue(),
                    (Boolean.valueOf(getNodeValue(attrs, "required"))).booleanValue(),
                    script,
                    getNodeValue(attrs, "category"),
                    (Boolean.valueOf(getNodeValue(attrs, "transient"))).booleanValue());
                propTypeList.add(propType);
            }
        }
    }

    /**
     * Create a TcgPropertyType with initial values from prototype, then override any using
     * those from values
     *
     * @param prototype the prototype to provide initial values
     * @param values the values to override the initial values
     *
     * @return a newly created TcgPropertyType
     */
    private static TcgPropertyType newTcgPropertyType(
        TcgPropertyType prototype, Element root) {
        NamedNodeMap attrs = root.getAttributes();

        String name = attrs.getNamedItem("name")==null?
            prototype.getName() : 
            attrs.getNamedItem("name").getNodeValue();

        String title = attrs.getNamedItem("title")==null?
            prototype.getTitle() : 
            attrs.getNamedItem("title").getNodeValue();

        String description = attrs.getNamedItem("description")==null?
            prototype.getDescription() : 
            attrs.getNamedItem("description").getNodeValue();

        String editor = attrs.getNamedItem("editor")==null?
            prototype.getEditorName() : 
            attrs.getNamedItem("editor").getNodeValue();

        String renderer = attrs.getNamedItem("renderer")==null?
            prototype.getRendererName() : 
            attrs.getNamedItem("renderer").getNodeValue();

        Object defaultValue = attrs.getNamedItem("default")==null?
            prototype.getDefaultValue() : 
            prototype.getType().parse(attrs.getNamedItem("default").getNodeValue());

        String access = attrs.getNamedItem("access")==null?
            prototype.getAccess() : 
            attrs.getNamedItem("access").getNodeValue();

        boolean required = attrs.getNamedItem("required")==null?
            prototype.isRequired() : 
            (Boolean.valueOf(attrs.getNamedItem(  
                "required").getNodeValue())).booleanValue();

        String script = null;
        NodeList scripts = root.getElementsByTagName("script");
        if (scripts.getLength() > 0) {
            script = loadScript(
                (Element) scripts.item(0));
        } else {
            script = prototype.getScript();
        }

        String category = attrs.getNamedItem("category")==null?
            prototype.getCategory() : 
            attrs.getNamedItem("category").getNodeValue();

        boolean isTransient = attrs.getNamedItem("transient")==null?
            prototype.isTransient() : 
            (Boolean.valueOf(attrs.getNamedItem(  
                "transient").getNodeValue())).booleanValue();

        return new TcgPropertyTypeImpl(prototype, name, title, description, editor,
            renderer, defaultValue, access, required, script, category, isTransient);
    }

    /**
     * @return attrs.getNamedItem(name).getNodeValue() if it exists, o.w. ""
     */
    private static String getNodeValue(NamedNodeMap attrs, String name) {
        String ret = "";
        Node node = attrs.getNamedItem(name);
        if (node != null) {
            ret = node.getNodeValue();
        }
        return ret;
    }

    /**
     * @return the text between elem's tags
     */
    private static String loadScript(Element node) {
        String ret = DOMUtil.getText(node);
        return ret;
    }

    //==========================================================================

    /**
     * Represents either a property_group definition or a top-level component_type
     * It is used to build the dependencySet relationship among property_group definitions
     * and top-level component_types
     */
    private static class TopLevelItem {
        public static final String CT = "component_type";
        public static final String PG = "property_group";
        
        public String mPkg;
        public String mName;
        public Element mElement;
        public String mType;
        public Set mDependencySet;
        public TopLevelItem(String pkg, String name, Element element, String type) {
            mPkg = pkg;
            mName = name;
            mElement = element;
            mType = type;
            mDependencySet = new HashSet();
        }

        public String toString() {
            return "TopLevelItem[type: " + mType + " package: " + mPkg + " name: " + mName + " element: " + mElement + " dependencySet: " + mDependencySet + "]";
        }
    }
}
