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

package org.netbeans.modules.xml.schema.refactoring.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.ui.highlight.Highlight;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightGroup;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightManager;
import org.netbeans.modules.xml.xam.Component;
import org.openide.util.Lookup;

/**
 * Provides utility methods for the Find Usages highlighting provider implementations.
 * copied from org.netbeans.modules.xml.schema.ui.basic.search.Providers
 *
 * @author Jeri Lockhart
 *
 */
public class HighlightProvider {
    
    public static final String FIND_USAGES = "find-usages";

    /**
     * Creates a new instance of HighlightProvider.
     */
    private HighlightProvider() {
    }

    /**
     * Finds all of the search-related highlight groups and hides and removes them.
     *
     * @param  lookup   where to get the HighlightManager.
     */
    public static void hideResults(Lookup lookup) {
        HighlightManager hm = HighlightManager.getDefault();
        List<HighlightGroup> groups = hm.getHighlightGroups(FIND_USAGES);
        if (groups != null) {
            for (HighlightGroup group : groups) {
                hm.removeHighlightGroup(group);
            }
        }
    }

    /**
     * Create the HighlightGroup to contain the set of matching components.
     * The parents of the results will be a different highlight type than
     * the results themselves.
     *
     * @param  results  set of matching results.
     * @param  lookup   where to get the HighlightManager.
     */
    public static void showResults(Set<SchemaComponent> results, Lookup lookup) {
        HighlightManager hm = HighlightManager.getDefault();
        HighlightGroup group = new HighlightGroup(FIND_USAGES);
        // Generate Highlight instances for each matching result, and its 
        // parents, grand parents, and so on, up to the root component.
        Iterator<SchemaComponent> iter = results.iterator();
        while (iter.hasNext()) {
            SchemaComponent comp = iter.next();
            FindUsagesHighlight h = new FindUsagesHighlight(comp, Highlight.FIND_USAGES_RESULT);
            group.addHighlight(h);
            SchemaComponent parent = comp.getParent();
            while (parent != null) {
                h = new FindUsagesHighlight(parent, Highlight.FIND_USAGES_RESULT_PARENT);
                group.addHighlight(h);
                parent = parent.getParent();
            }
        }
        hm.addHighlightGroup(group);
    }
    
    public static class FindUsagesHighlight extends Highlight{
        public FindUsagesHighlight(Component comp, String searchResults) {
            super(comp, searchResults);
        }
    }
}
