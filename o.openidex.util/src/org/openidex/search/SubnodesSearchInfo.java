/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.nodes.Node;

/**
 *
 * @author  Marian Petras
 */
final class SubnodesSearchInfo implements SearchInfo {

    /** */
    private final Node node;

    /**
     *
     */
    public SubnodesSearchInfo(final Node node) {
        this.node = node;
    }

    /**
     */
    public boolean canSearch() {
        final Node[] nodes = node.getChildren().getNodes();
        for (int i = 0; i < nodes.length; i++) {
            SearchInfo searchInfo = Utils.getSearchInfo(nodes[i]);
            if (searchInfo != null && searchInfo.canSearch()) {
                return true;
            }
        }
        return false;
    }

    /**
     */
    public Iterator objectsToSearch() {
        final Node[] nodes = node.getChildren().getNodes();
        if (nodes.length == 0) {
            return SimpleSearchInfo.EMPTY_SEARCH_INFO.objectsToSearch();
        }
        
        List searchInfoElements = new ArrayList(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            SearchInfo subInfo = Utils.getSearchInfo(nodes[i]);
            if (subInfo != null && subInfo.canSearch()) {
                searchInfoElements.add(subInfo);
            }
        }
        
        final int size = searchInfoElements.size();
        switch (size) {
            case 0:
                return Collections.EMPTY_LIST.iterator();
            case 1:
                return ((SearchInfo) searchInfoElements.get(0))
                       .objectsToSearch();
            default:
                return new CompoundSearchIterator(
                        (SearchInfo[])
                        searchInfoElements.toArray(new SearchInfo[size]));
        }
    }

}
