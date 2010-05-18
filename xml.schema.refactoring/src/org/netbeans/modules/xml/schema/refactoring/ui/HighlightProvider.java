/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
