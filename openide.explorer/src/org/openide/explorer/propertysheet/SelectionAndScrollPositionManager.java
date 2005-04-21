/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.explorer.propertysheet;

import java.util.HashMap;
import java.util.Map;


/**
 * Keeps a global list of scroll positions and selected tabs for the property
 * sheet.
 */
class SelectionAndScrollPositionManager {
    private static Map groupsToNodes = new HashMap();
    private static Map namesToPositions = new HashMap();
    private static final Integer zero = new Integer(0);
    private String lastSelectedGroup = "";
    private String nodeName = null;

    /**
     * Stores the current node name.
     *
     * @param name
     */
    public void setCurrentNodeName(String name) {
        nodeName = name;
    }

    public String getCurrentNodeName() {
        return nodeName;
    }

    public String getLastSelectedGroupName() {
        return lastSelectedGroup;
    }

    /**
     * Store the current scroll position.
     *
     * @param pos A scroll position.  Will only be stored if > 0.
     * @param name A node or tab name
     */
    public void storeScrollPosition(int pos, String name) {
        if (pos > 0) {
            synchronized (namesToPositions) {
                namesToPositions.put(name, new Integer(pos));
            }
        }
    }

    /**
     * Stores the last selected group
     * @param group
     */
    public void storeLastSelectedGroup(String group) {
        if (nodeName != null) {
            synchronized (groupsToNodes) {
                lastSelectedGroup = group;
                groupsToNodes.put(nodeName, group);
            }
        }
    }

    /**
     * Fetch the remembered group selection name.  The returned value
     * may or may not actually be present in the list of tab names for
     * a given node.
     * <p>
     * If no value is stored for this node, will return the last selected
     * group name for a node that does have groups, such that if a given
     * tab is selected and the selected node changes to another node which
     * is unknown but has the same list of tab names, the selected tab
     * will not suddenly change.
     * <p>
     * If the name is not found, the caller should use PropUtils.basicPropsTabName()
     * as the third fallback for selection, setting the selection thus to the
     * Properties tab.
     *
     * @param name The name of a node
     * @return A name of a group.
     */
    public String getGroupNameForNodeName(String name) {
        String result = null;

        synchronized (groupsToNodes) {
            result = (String) groupsToNodes.get(name);
        }

        if (result == null) {
            result = lastSelectedGroup;
        }

        return result;
    }

    public int getScrollPositionForNodeName(String name) {
        Integer result = zero;
        Integer found = (Integer) namesToPositions.get(name);

        if (found != null) {
            result = found;
        }

        return result.intValue();
    }
}
