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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
class LayoutPersistenceManager implements LayoutConstants {
    /** Layout model to load/save. */
    private LayoutModel layoutModel;
    /** Currently processed layout root. */
    private LayoutComponent root;
    /** Currently processed dimension. */
    private int dimension;
    /** Map from component IDs to names or vice versa. */
    private Map<String, String> idNameMap;
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
     * Returns dump of the layout interval.
     *
     * @param indent determines size of indentation.
     * @param root container layout model should be dumped.
     * @param humanReadable determines whether constants should be replaced
     * by human readable expressions.
     * @return dump of the layout model.
     */
    String saveIntervalLayout(int indent, LayoutInterval interval, int dimension) {
        this.indent = indent;
        humanReadable = true;
        sb = new StringBuffer();
        saveInterval(interval, dimension);
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
                saveLinkSize(interval.getComponent().getLinkSizeId(dimension));
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
            sb.append("/>\n"); // NOI18N
        }
        indent--;
    }

    /**
     * Saves linkSize group identifier
     *
     * @param linksizeid 
     */
    private void saveLinkSize(int linkSizeId) {
        if (linkSizeId != NOT_EXPLICITLY_DEFINED) {
            sb.append(" ").append(ATTR_LINK_SIZE).append("=\"").append(linkSizeId).append("\""); // NOI18N
        }
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
            if (alignment != DEFAULT) {
                sb.append(attrPrefix).append(alignment).append("\""); // NOI18N
            }
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
            if (size != NOT_EXPLICITLY_DEFINED) {
                sb.append(attrPrefix).append(size).append("\""); // NOI18N
            }
        }
    }

    /**
     * Saves attributes of some layout interval.
     *
     * @param attributes attributes of some layout interval.
     */
    private void saveAttributes(int attributes) {
        if (!humanReadable)
            attributes &= LayoutInterval.ATTR_PERSISTENT_MASK;
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
     * Loads the layout of the given container. Does not load containers
     * recursively, is called for each container separately.
     *
     * @param rootId ID of the layout root (the container whose layout should be loaded).
     * @param dimLayoutList nodes holding the information about the layout.
     * @param nameToIdMap map from component names to component IDs.
     */
    void loadModel(String rootId, NodeList dimLayoutList, Map nameToIdMap)
        throws java.io.IOException
    {
        this.idNameMap = nameToIdMap;
        resetMissingName(); // prepare for error recovery
        LayoutComponent root = layoutModel.getLayoutComponent(rootId);
        if (root == null) {
            root = new LayoutComponent(rootId, true);
            layoutModel.addRootComponent(root);
        }
        this.root = root;

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

        correctMissingName(); // recover from missing component name if needed
    }

    /**
     * Loads layout of the given group.
     *
     * @param group group whose layout information should be loaded.
     * @param groupNode node holding the information about the layout of the group.
     */
    private void loadGroup(LayoutInterval group, Node groupNode, int dimension)
        throws java.io.IOException
    {
        NamedNodeMap attrMap = groupNode.getAttributes();
        Node alignmentNode = attrMap.getNamedItem(ATTR_ALIGNMENT);
        Node groupAlignmentNode = attrMap.getNamedItem(ATTR_GROUP_ALIGNMENT);
        Node minNode = attrMap.getNamedItem(ATTR_SIZE_MIN);
        Node maxNode = attrMap.getNamedItem(ATTR_SIZE_MAX);
        int alignment = (alignmentNode == null) ? DEFAULT : integerFromNode(alignmentNode);
        group.setAlignment(alignment);
        if (group.isParallel()) {
            int groupAlignment = (groupAlignmentNode == null) ? DEFAULT : integerFromNode(groupAlignmentNode);
            if (groupAlignment != DEFAULT) {
                group.setGroupAlignment(groupAlignment);
            }
        }
        int min = (minNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(minNode);
        int max = (maxNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(maxNode);
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
        if (dimension == VERTICAL)
            checkAndFixGroup(group);
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
    private void loadComponent(LayoutInterval parent, Node componentNode, int dimension)
        throws java.io.IOException
    {
        NamedNodeMap attrMap = componentNode.getAttributes();
        String name = attrMap.getNamedItem(ATTR_COMPONENT_ID).getNodeValue();
        Node linkSizeId = attrMap.getNamedItem(ATTR_LINK_SIZE);
        String id = (String)idNameMap.get(name);
        if (id == null) { // try to workaround the missing name error (issue 77092)
            id = useTemporaryId(name);
        }
        Node alignmentNode = attrMap.getNamedItem(ATTR_ALIGNMENT);
        int alignment = (alignmentNode == null) ? DEFAULT : integerFromNode(alignmentNode);
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
        int min = (minNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(minNode);
        int pref = (prefNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(prefNode);
        int max = (maxNode == null) ? NOT_EXPLICITLY_DEFINED : integerFromNode(maxNode);
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
            attributes &= LayoutInterval.ATTR_PERSISTENT_MASK;
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

    // -----
    // error recovery

    /**
     * This method is used during loading to check the alignment validity of
     * given group and its subintervals. It checks use of BASELINE alignment of
     * the group and the subintervals. Some invalid combinations were allowed by
     * GroupLayout in version 1.0. See issue 78035 for details. This method also
     * fixes the invalid combinations and sets the 'corrected' flag. This is
     * needed for loading because wrong layouts still might exist from the past
     * (and we still can't quite exclude it can't be created even now).
     * BASELINE group can only contain BASELINE intervals, and vice versa,
     * BASELINE interval can only be placed in BASELINE group.
     * BASELINE can be set only on individual components.
     * LEADING, TRAILING and CENTER alignments can be combined freely.
     */
    void checkAndFixGroup(LayoutInterval group) {
        if (group.isParallel()) {
            int groupAlign = group.getGroupAlignment();
            int baselineCount = 0;

            Iterator iter = group.getSubIntervals();
            while (iter.hasNext()) {
                LayoutInterval subInterval = (LayoutInterval)iter.next();
                if (subInterval.getAlignment() == BASELINE) {
                    if (!subInterval.isComponent()) {
                        subInterval.setAlignment(groupAlign == BASELINE ? LEADING : DEFAULT);
                        layoutModel.setCorrected();
                        System.err.println("WARNING: Invalid use of BASELINE [1], corrected automatically"); // NOIO18N
                    }
                    else baselineCount++;
                }
            }

            if (baselineCount > 0) {
                if (baselineCount < group.getSubIntervalCount()) {
                    // separate baseline intervals to a subgroup
                    LayoutInterval subGroup = new LayoutInterval(PARALLEL);
                    subGroup.setGroupAlignment(BASELINE);
                    for (int i=0; i < group.getSubIntervalCount(); ) {
                        LayoutInterval subInterval = group.getSubInterval(i);
                        if (subInterval.getAlignment() == BASELINE) {
                            group.remove(i);
                            subGroup.add(subInterval, -1);
                        }
                        else i++;
                    }
                    if (groupAlign == BASELINE) {
                        group.setGroupAlignment(LEADING);
                    }
                    group.add(subGroup, -1);
                    layoutModel.setCorrected();
                    System.err.println("WARNING: Invalid use of BASELINE [2], corrected automatically"); // NOIO18N
                }
                else if (groupAlign != BASELINE) {
                    group.setGroupAlignment(BASELINE);
                    layoutModel.setCorrected();
                    System.err.println("WARNING: Invalid use of BASELINE [3], corrected automatically"); // NOIO18N
                }
            }
            else if (groupAlign == BASELINE && group.getSubIntervalCount() > 0) {
                group.setGroupAlignment(LEADING);
                layoutModel.setCorrected();
                System.err.println("WARNING: Invalid use of BASELINE [4], corrected automatically"); // NOIO18N
            }
        }
    }

    // The following code tries to fix a missing component name in the layout
    // definition. Due to a bug (see issues 77092, 76749) it may happen that
    // one component in the layout XML has "null" or duplicate name (while it is
    // correct in the metacomponent). If it is the only wrong name in the
    // container then it can be deduced from the map provided for converting
    // names to IDs (it is the only ID not used).

    private final String TEMPORARY_ID = "<temp_id>"; // NOI18N
    private String missingNameH;
    private String missingNameV;

    private void resetMissingName() {
        missingNameH = missingNameV = null;
    }

    private String useTemporaryId(String name) throws java.io.IOException {
        if (dimension == HORIZONTAL) {
            if (missingNameH == null && (missingNameV == null || missingNameV.equals(name))) {
                missingNameH = name;
                return TEMPORARY_ID;
            }
        }
        else if (dimension == VERTICAL) {
            if (missingNameV == null && (missingNameH == null || missingNameH.equals(name))) {
                missingNameV = name;
                return TEMPORARY_ID;
            }
        }
        throw new java.io.IOException("Undefined component referenced in layout: "+name); // NOI18N
    }

    private void correctMissingName() throws java.io.IOException {
        if (missingNameH == null && missingNameV == null)
            return; // no problem

        if (missingNameH != null && missingNameV != null && missingNameH.equals(missingNameV)
            && idNameMap.size() == root.getSubComponentCount())
        {   // we have one unknown name in each dimension, let's infer it from the idNameMap
            for (Map.Entry<String, String> e : idNameMap.entrySet()) { // name -> id
                if (layoutModel.getLayoutComponent(e.getValue()) == null) {
                    LayoutComponent comp = layoutModel.getLayoutComponent(TEMPORARY_ID);
                    layoutModel.changeComponentId(comp, e.getValue());
                    layoutModel.setCorrected();
                    System.err.println("WARNING: Invalid component name in layout: "+missingNameH // NOI18N
                            +", corrected automatically to: "+e.getKey()); // NOI18N
                    resetMissingName();
                    return;
                }
            }
        }

        layoutModel.removeComponent(TEMPORARY_ID, true);
        resetMissingName();
        throw new java.io.IOException("Undefined component referenced in layout: " // NOI18N
                + (missingNameH != null ? missingNameH : missingNameV));
    }
}
