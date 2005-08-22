/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign;

import java.util.*;
import org.w3c.dom.*;

/**
 * Class responsible for loading and saving of layout model.
 *
 * @author Jan Stola
 */
public class LayoutPersistenceManager implements LayoutConstants {
    /** Layout model to load/save. */
    private LayoutModel layoutModel;
    /** Currently processed layout root. */
    private LayoutComponent root;
    /** Currently processed dimension. */
    private int dimension;
    /** Map from component IDs to names or vice versa. */
    private Map idNameMap;
    /** Determines whether constants should be replaces by human readable expressions. */
    private boolean humanReadable;
    /** Size of current indent. */
    private int indent;
    /** String buffer used to save layout. */
    private StringBuffer sb;
    
    // elements names
    static final String XML_DIMENSION_LAYOUT = "DimensionLayout"; // NOI18N
    static final String XML_GROUP = "Group"; // NOI18N
    static final String XML_COMPONENT = "Component"; // NOI18N
    static final String XML_EMPTY_SPACE = "EmptySpace"; // NOI18N
    
    // attributes names
    static final String ATTR_DIMENSION_DIM = "dim"; // NOI18N
    static final String ATTR_GROUP_TYPE = "type"; // NOI18N
    static final String ATTR_SIZE_MIN = "min"; // NOI18N
    static final String ATTR_SIZE_PREF = "pref"; // NOI18N
    static final String ATTR_SIZE_MAX = "max"; // NOI18N
    static final String ATTR_ALIGNMENT = "alignment"; // NOI18N
    static final String ATTR_GROUP_ALIGNMENT = "groupAlignment"; // NOI18N
    static final String ATTR_LINK_SIZE = "linkSize"; // NOI18N
    static final String ATTR_COMPONENT_ID = "id"; // NOI18N
    static final String ATTR_ATTRIBUTES = "attributes"; // NOI18N
    
    // attribute values
    static final String VALUE_DIMENSION_HORIZONTAL = "horizontal"; // NOI18N
    static final String VALUE_DIMENSION_VERTICAL = "vertical"; // NOI18N
    static final String VALUE_ALIGNMENT_LEADING = "leading"; // NOI18N
    static final String VALUE_ALIGNMENT_TRAILING = "trailing"; // NOI18N
    static final String VALUE_ALIGNMENT_CENTER = "center"; // NOI18N
    static final String VALUE_ALIGNMENT_BASELINE = "baseline"; // NOI18N
    static final String VALUE_SIZE_PREFERRED = "$pref"; // NOI18N
    static final String VALUE_SIZE_MAX = "Short.MAX_VALUE"; // NOI18N
    static final String VALUE_GROUP_PARALLEL = "parallel"; // NOI18N
    static final String VALUE_GROUP_SEQUENTIAL = "sequential"; // NOI18N
    
    /**
     * Creates new <code>LayoutPersistenceManager</code>.
     *
     * @param layoutModel layout model to load/save.
     */
    LayoutPersistenceManager(LayoutModel layoutModel) {
        this.layoutModel = layoutModel;
    }

    /**
     * Returns dump of the layout model.
     *
     * @param indent determines size of indentation.
     * @param root container layout model should be dumped.
     * @param humanReadable determines whether constants should be replaced
     * by human readable expressions.
     * @return dump of the layout model.
     */
    String saveLayout(int indent, LayoutComponent root, Map idToNameMap, boolean humanReadable) {
        this.root = root;
        this.indent = indent;
        this.idNameMap = idToNameMap;
        this.humanReadable = humanReadable;
        sb = new StringBuffer();
        for (int i=0; i < DIM_COUNT; i++) {
            indent().append('<').append(XML_DIMENSION_LAYOUT);
            sb.append(' ').append(ATTR_DIMENSION_DIM).append("=\""); // NOI18N
            if (humanReadable) {
                switch (i) {
                    case HORIZONTAL: sb.append(VALUE_DIMENSION_HORIZONTAL); break;
                    case VERTICAL: sb.append(VALUE_DIMENSION_VERTICAL); break;
                    default: sb.append(i); break;
                }
            } else {
                sb.append(i);
            }
            sb.append("\">\n"); // NOI18N
            LayoutInterval interval = root.getLayoutRoot(i);
            saveInterval(interval, i);
            indent().append("</").append(XML_DIMENSION_LAYOUT).append(">\n"); // NOI18N
        }
        return sb.toString();
    }
    
    /**
     * Dumps the information about the given layout interval.
     *
     * @param interval layout interval to dump.
     */
    private void saveInterval(LayoutInterval interval, int dimension) {
        indent++;
        indent();
        if (interval.isGroup()) {
            sb.append('<').append(XML_GROUP).append(' ');
            sb.append(ATTR_GROUP_TYPE).append("=\""); // NOI18N
            if (humanReadable) {
                sb.append(interval.isParallel() ? VALUE_GROUP_PARALLEL : VALUE_GROUP_SEQUENTIAL);
            } else {
                sb.append(interval.getType());
            }
            sb.append("\""); // NOI18N
            saveAlignment(interval.getRawAlignment(), false);
            if (interval.isParallel()) {
                saveAlignment(interval.getGroupAlignment(), true);
            }
            saveSize(interval.getMinimumSize(), ATTR_SIZE_MIN);
            saveSize(interval.getMaximumSize(), ATTR_SIZE_MAX);
            saveAttributes(interval.getAttributes());
            sb.append(">\n"); // NOI18N
            indent++;
            Iterator iter = interval.getSubIntervals();
            while (iter.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)iter.next();
                saveInterval(subInterval, dimension);
            }
            indent--;
            indent().append("</").append(XML_GROUP).append(">\n"); // NOI18N
        } else {
            if (interval.isComponent()) {
                String name = interval.getComponent().getId();
                if (idNameMap != null) {
                    name = (String)idNameMap.get(name);
                    assert (name != null);
                }
                sb.append('<').append(XML_COMPONENT).append(' ');
                sb.append(ATTR_COMPONENT_ID).append("=\"").append(name).append("\""); // NOI18N
                sb.append(' ');
                sb.append(ATTR_LINK_SIZE).append("=\"").append(interval.getComponent().getLinkSizeId(dimension)).append("\""); // NOI18N
                saveAlignment(interval.getRawAlignment(), false);
            } else if (interval.isEmptySpace()) {
                sb.append('<').append(XML_EMPTY_SPACE);
            } else {
                assert false;
            }
            saveSize(interval.getMinimumSize(), ATTR_SIZE_MIN);
            saveSize(interval.getPreferredSize(), ATTR_SIZE_PREF);
            saveSize(interval.getMaximumSize(), ATTR_SIZE_MAX);
            saveAttributes(interval.getAttributes());
            sb.append("/>\n");
        }
        indent--;
    }
    
    /**
     * Saves group/interval alignemnt.
     *
     * @param alignemnt alignment to save.
     * @param group determines whether it is a group alignment.
     */
    private void saveAlignment(int alignment, boolean group) {
        String attrPrefix = " " + (group ? ATTR_GROUP_ALIGNMENT : ATTR_ALIGNMENT) + "=\""; // NOI18N
        if (humanReadable) {
            if (alignment != DEFAULT) {
                sb.append(attrPrefix);
                switch (alignment) {
                    case LEADING: sb.append(VALUE_ALIGNMENT_LEADING); break;
                    case TRAILING: sb.append(VALUE_ALIGNMENT_TRAILING); break;
                    case CENTER: sb.append(VALUE_ALIGNMENT_CENTER); break;
                    case BASELINE: sb.append(VALUE_ALIGNMENT_BASELINE); break;
                    default: assert false;
                }
                sb.append("\""); // NOI18N
            }
        } else {
            sb.append(attrPrefix).append(alignment).append("\""); // NOI18N
        }
    }
    
    /**
     * Saves size parameter of some layout interval.
     *
     * @param size value of the size parameter.
     * @param attr name of the size parameter.
     */
    private void saveSize(int size, String attr) {
        String attrPrefix = " " + attr + "=\""; // NOI18N            
        if (humanReadable) {
            if (size != NOT_EXPLICITLY_DEFINED) {
                sb.append(attrPrefix);
                if (size == USE_PREFERRED_SIZE) {
                    sb.append(VALUE_SIZE_PREFERRED);
                } else {
                    if (size == Short.MAX_VALUE) {
                        sb.append(VALUE_SIZE_MAX);
                    } else {
                        sb.append(size);
                    }
                }
                sb.append("\""); // NOI18N
            }
        } else {
            sb.append(attrPrefix).append(size).append("\""); // NOI18N
        }
    }

    /**
     * Saves attributes of some layout interval.
     *
     * @param attributes attributes of some layout interval.
     */
    private void saveAttributes(int attributes) {
        sb.append(' ').append(ATTR_ATTRIBUTES).append("=\""); // NOI18N
        sb.append(attributes).append("\""); // NOI18N
    }
    
    /**
     * Performs indentation.
     *
     * @return indented <code>StringBuffer</code>.
     */
    private StringBuffer indent() {
        char[] spaces = new char[2*indent];
        Arrays.fill(spaces, ' ');
        return sb.append(spaces);
    }
    
    /**
     * Loads the layout of the given container.
     *
     * @param rootId ID of the layout root (the container whose layout should be loaded).
     * @param dimLayoutList nodes holding the information about the layout.
     * @param nameToIdMap map from component names to component IDs.
     */
    public void loadModel(String rootId, NodeList dimLayoutList, Map nameToIdMap) {
        this.idNameMap = nameToIdMap;
        this.root = layoutModel.getLayoutComponent(rootId);
        for (int i=0; i<dimLayoutList.getLength(); i++) {
            Node dimLayoutNode = dimLayoutList.item(i);
            if (!(dimLayoutNode instanceof Element))
                continue;
            Node dimAttrNode = dimLayoutNode.getAttributes().getNamedItem(ATTR_DIMENSION_DIM);
            dimension = integerFromNode(dimAttrNode);
            LayoutInterval dimLayoutInterval = root.getLayoutRoot(dimension);
            NodeList childs = dimLayoutNode.getChildNodes();
            for (int j=0; j<childs.getLength(); j++) {
                Node node = childs.item(j);
                if (node instanceof Element) {
                    loadGroup(dimLayoutInterval, node, dimension);
                    break;
                }
            }
        }
    }
    
    /**
     * Loads layout of the given group.
     *
     * @param group group whose layout information should be loaded.
     * @param groupNode node holding the information about the layout of the group.
     */
    private void loadGroup(LayoutInterval group, Node groupNode, int dimension) {
        NamedNodeMap attrMap = groupNode.getAttributes();
        Node alignmentNode = attrMap.getNamedItem(ATTR_ALIGNMENT);
        Node groupAlignmentNode = attrMap.getNamedItem(ATTR_GROUP_ALIGNMENT);
        Node minNode = attrMap.getNamedItem(ATTR_SIZE_MIN);
        Node maxNode = attrMap.getNamedItem(ATTR_SIZE_MAX);
        int alignment = integerFromNode(alignmentNode);
        group.setAlignment(alignment);
        if (group.isParallel()) {
            int groupAlignment = integerFromNode(groupAlignmentNode);
            if (groupAlignment != DEFAULT) {
                group.setGroupAlignment(groupAlignment);
            }
        }
        int min = integerFromNode(minNode);
        int max = integerFromNode(maxNode);
        group.setMinimumSize(min);
        group.setMaximumSize(max);
        loadAttributes(group, attrMap);
        NodeList subNodes = groupNode.getChildNodes();
        for (int i=0; i<subNodes.getLength(); i++) {
            Node subNode = subNodes.item(i);
            if (!(subNode instanceof Element))
                continue;
            String nodeName = subNode.getNodeName();
            if (XML_GROUP.equals(nodeName)) {
                Node typeNode = subNode.getAttributes().getNamedItem(ATTR_GROUP_TYPE);
                int type = integerFromNode(typeNode);
                LayoutInterval subGroup = new LayoutInterval(type);
                group.add(subGroup, -1);
                loadGroup(subGroup, subNode, dimension);
            } else if (XML_EMPTY_SPACE.equals(nodeName)) {
                loadEmptySpace(group, subNode);
            } else {
                assert XML_COMPONENT.equals(nodeName);
                loadComponent(group, subNode, dimension);
            }
        }
    }
    
    /**
     * Loads information about empty space.
     *
     * @param parent layout parent of the empty space.
     * @param spaceNode node with the information about the empty space.
     */
    private void loadEmptySpace(LayoutInterval parent, Node spaceNode) {
        LayoutInterval space = new LayoutInterval(SINGLE);
        NamedNodeMap attrMap = spaceNode.getAttributes();
        loadSizes(space, attrMap);
        loadAttributes(space, attrMap);
        parent.add(space, -1);
    }
    
    /**
     * Loads information about component.
     *
     * @param parent layout parent of the loaded layout interval.
     * @param componentNode node with the information about the component.
     * @param dimension loaded dimension
     */
    private void loadComponent(LayoutInterval parent, Node componentNode, int dimension) {
        NamedNodeMap attrMap = componentNode.getAttributes();
        String name = attrMap.getNamedItem(ATTR_COMPONENT_ID).getNodeValue();
        Node linkSizeId = attrMap.getNamedItem(ATTR_LINK_SIZE);
        String id = (String)idNameMap.get(name);
        Node alignmentNode = attrMap.getNamedItem(ATTR_ALIGNMENT);
        int alignment = integerFromNode(alignmentNode);
        LayoutComponent layoutComponent = layoutModel.getLayoutComponent(id);
        if (layoutComponent == null) {
            layoutComponent = new LayoutComponent(id, false  /*PENDING*/);
        }
        if (layoutComponent.getParent() == null) {
            layoutModel.addComponent(layoutComponent, root, -1);
        }
        LayoutInterval interval = layoutComponent.getLayoutInterval(dimension);
        interval.setAlignment(alignment);
        if (linkSizeId != null) {
            layoutModel.addComponentToLinkSizedGroup(integerFromNode(linkSizeId), layoutComponent.getId(), dimension);
        }
        loadSizes(interval, attrMap);
        loadAttributes(interval, attrMap);
        parent.add(interval, -1);
    }
    
    /**
     * Loads size information of the given interval.
     *
     * @param interval layout interval whose size information should be loaded.
     * @param attrMap map with size information.
     */
    private void loadSizes(LayoutInterval interval, org.w3c.dom.NamedNodeMap attrMap) {
        Node minNode = attrMap.getNamedItem(ATTR_SIZE_MIN);
        Node prefNode = attrMap.getNamedItem(ATTR_SIZE_PREF);
        Node maxNode = attrMap.getNamedItem(ATTR_SIZE_MAX);
        int min = integerFromNode(minNode);
        int pref = integerFromNode(prefNode);
        int max = integerFromNode(maxNode);
        interval.setSizes(min, pref, max);
    }
    
    /**
     * Loads attributes of the given interval.
     *
     * @param interval layout interval whose attributes should be loaded.
     * @param attrMap map with attribute information.
     */
    private void loadAttributes(LayoutInterval interval, org.w3c.dom.NamedNodeMap attrMap) {
        Node attributesNode = attrMap.getNamedItem(ATTR_ATTRIBUTES);
        int attributes = 0;
        if (attributesNode != null) {
            attributes = integerFromNode(attributesNode);
        }
        interval.setAttributes(attributes);
    }
    
    /**
     * Extracts integer value from the given node.
     *
     * @param node node that has integer as its value.
     * @return integer value extracted from the given node.
     */
    private static int integerFromNode(Node node) {
        String nodeStr = node.getNodeValue();
        return Integer.parseInt(nodeStr);
    }

}
