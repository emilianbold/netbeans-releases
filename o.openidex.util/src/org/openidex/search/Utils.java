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

import java.util.Collection;
import java.util.Iterator;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author  Marian Petras
 */
public class Utils {
    
    /** */
    private static Lookup.Result searchInfoProviders;

    /** Creates a new instance of Utils */
    public Utils() {
    }
    
    /**
     */
    public static boolean hasSearchInfo(Node node) {
        
        /* 1st try - is the SearchInfo object in the node's lookup? */
        if (node.getLookup().lookup(SearchInfo.class) != null) {
            return true;
        }
    
        /* 2nd try - is the SearchInfo object defined externally? */
        if (searchInfoProviders == null) {
            searchInfoProviders = Lookup.getDefault().lookup(
                new Lookup.Template(SearchInfoProvider.class));
        }
        Collection providers = searchInfoProviders.allInstances();
        if (!providers.isEmpty()) {
            for (Iterator i = providers.iterator(); i.hasNext(); ) {
                SearchInfoProvider infoProvider = (SearchInfoProvider) i.next();

                String[] supportedNodeTypes = infoProvider.getSupportedNodeTypes();
                if (supportedNodeTypes != null
                        && !isListedString(node.getClass().getName(),
                                           supportedNodeTypes)) {
                    continue;
                }
                //String projectType = getProjectType(node);
                //if (projectType != null) {
                //    String[] supportedProjectTypes
                //            = infoProvider.getSupportedProjectTypes();
                //    if (supportedProjectTypes != null
                //            && !isListedString(projectType,
                //                               supportedProjectTypes))  {
                //        continue;
                //    }
                //}
                if (infoProvider.hasSearchInfo(node)) {
                    return true;
                }
            }
        }

        /* 3rd try - does the node represent a DataObject.Container? */
        return node.getLookup().lookup(DataObject.Container.class) != null;
}
    
    /**
     */
    public static SearchInfo getSearchInfo(Node node) {
        SearchInfo info;

        /* 1st try - is the SearchInfo object in the node's lookup? */
        info = (SearchInfo) node.getLookup().lookup(SearchInfo.class);
        if (info != null) {
            return info;
        }

        /* 2nd try - is the SearchInfo object defined externally? */
        if (searchInfoProviders == null) {
            searchInfoProviders = Lookup.getDefault().lookup(
                new Lookup.Template(SearchInfo.class));
        }
        Collection providers = searchInfoProviders.allInstances();
        if (!providers.isEmpty()) {
            for (Iterator i = providers.iterator(); i.hasNext(); ) {
                SearchInfoProvider infoProvider = (SearchInfoProvider) i.next();

                String[] supportedNodeTypes = infoProvider.getSupportedNodeTypes();
                if (supportedNodeTypes != null
                        && !isListedString(node.getClass().getName(),
                                           supportedNodeTypes)) {
                    continue;
                }
                info = infoProvider.getSearchInfo(node);
                if (info != null) {
                    return info;
                }
            }
        }

        /* 3rd try - does the node represent a DataObject.Container? */
        Object container = node.getLookup().lookup(DataObject.Container.class);
        return (container != null)
               ? new SimpleSearchInfo((DataObject.Container) container, true)
               : SimpleSearchInfo.EMPTY_SEARCH_INFO;
    }

    /**
     */
    private static boolean isListedString(String o, String[] list) {
        if (list == null || list.length == 0) {
            return false;
        }
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(o)) {
                return true;
            }
        }
        return false;
    }
    
}
