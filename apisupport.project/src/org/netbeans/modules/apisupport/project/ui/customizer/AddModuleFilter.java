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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements filtering for Add Module Dependency panel.
 * @author Jesse Glick
 */
final class AddModuleFilter {
    
    private final Set/*<ModuleDependency>*/ universe;
    
    /**
     * Construct a filter given a list of possible dependencies.
     */
    public AddModuleFilter(Set/*<ModuleDependency>*/ universe) {
        this.universe = universe;
    }
    
    /**
     * Find matches for a search string.
     */
    public Set/*<ModuleDependency>*/ getMatches(String text) {
        // To test "Please wait" use:
        //try{Thread.sleep(2000);}catch(InterruptedException e){}
        String textLC = text.toLowerCase(Locale.US);
        Set/*<ModuleDependency>*/ matches = new TreeSet();
        Iterator it = universe.iterator();
        while (it.hasNext()) {
            ModuleDependency dep = (ModuleDependency) it.next();
            Set/*<String>*/ tokens = dep.getFilterTokens();
            Iterator it2 = tokens.iterator();
            while (it2.hasNext()) {
                String token = (String) it2.next();
                if (token.indexOf(textLC) != -1) {
                    matches.add(dep);
                    break;
                }
            }
        }
        return matches;
    }
    
}
