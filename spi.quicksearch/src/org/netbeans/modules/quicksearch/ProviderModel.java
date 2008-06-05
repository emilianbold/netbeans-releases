/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.quicksearch;

import java.util.HashSet;
import java.util.List;
import org.netbeans.spi.quicksearch.CategoryDescription;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Dafe Simonek
 */
final class ProviderModel {

    private List<Category> categories;
    
    private HashSet<String> knownCommands;

    /**
     * Get the value of categories
     *
     * @return the value of categories
     */
    public List<Category> getCategories() {
        return this.categories;
    }

    /**
     * Set the value of categories
     *
     * @param newcategories new value of categories
     */
    public void setCategories(List<Category> newCategories) {
        this.categories = newCategories;
        knownCommands = new HashSet<String>();
        for (Category cat:categories) {
            knownCommands.add(cat.getCommandPrefix());
        }
    }
    
    public boolean isKnownCommand(String command) {
        return knownCommands.contains(command);
    }

    public Category getCategory (String commandPrefix) {
        // TBD
        return null;
    }
    
    static class Category {
        
        private String name;
        
        private List<SearchProvider> providers;

        public Category(String name, List<SearchProvider> providers) {
            this.name = name;
            this.providers = providers;
        }
        
        public List<SearchProvider> getProviders () {
            return providers;
        }
        
        public void setProviders (List<SearchProvider> newProviders) {
            this.providers = newProviders;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String newName) {
            this.name = newName;
        }
        
        public String getDisplayName() {
            for (SearchProvider prov : providers) {
                CategoryDescription desc = getCatDesc(prov);
                if (desc != null) {
                    String displayName = desc.getDisplayName();
                    if (displayName != null) {
                        return displayName;
                    }
                }
            }
            // fallback if no provider specifies display name
            return null;
        }

        public String getCommandPrefix() {
            for (SearchProvider prov : providers) {
                CategoryDescription desc = getCatDesc(prov);
                if (desc != null) {
                    String prefix = desc.getCommandPrefix();
                    if (prefix != null) {
                        return prefix;
                    }
                }
            }
            // fallback if no provider specifies display name
            return null;
        }
        
        private static CategoryDescription getCatDesc (SearchProvider provider) {
            Lookup lkp = provider.getLookup();
            if (lkp == null) {
                return null;
            }
            return lkp.lookup(CategoryDescription.class);
        }

        
    } // end of Category

}
