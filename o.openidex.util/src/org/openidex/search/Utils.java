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
import java.util.List;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author  Marian Petras
 */
final class Utils {

    /**
     */
    static SearchInfo getSearchInfo(Node node) {
        /* 1st try - is the SearchInfo object in the node's lookup? */
        SearchInfo info = (SearchInfo)
                          node.getLookup().lookup(SearchInfo.class);
        if (info != null) {
            return info;
        }

        /* 2nd try - does the node represent a DataObject.Container? */
        Object container = node.getLookup().lookup(DataFolder.class);
        if (container == null) {
            return null;
        } else {
            return SearchInfoFactory.createSearchInfo(
                    ((DataFolder) container).getPrimaryFile(),
                    true,                       //recursive
                    new FileObjectFilter[] {
                            SearchInfoFactory.VISIBILITY_FILTER,
                            SearchInfoFactory.SHARABILITY_FILTER });
        }
    }
    
}
