/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.profiler.categorization.api;

import org.netbeans.modules.profiler.categorization.spi.CategoryDefinitionProcessor;
import org.netbeans.lib.profiler.marker.Marker;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.modules.profiler.utilities.Visitable;
import org.netbeans.modules.profiler.utilities.Visitor;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
final public class ProjectCategorization extends Categorization {
    private Lookup.Provider project;

    public ProjectCategorization(Lookup.Provider project) {
        super();
        this.project = project;
    }

    @Override
    protected void buildCategories(CategoryContainer root) {
        for (CategoryBuilder builder : project.getLookup().lookupAll(CategoryBuilder.class)) {
            root.addAll(builder.getRootCategory().getSubcategories());
        }
    }

    /**
     * A categorization is only available if there is a {@linkplain CategoryBuilder}
     * associated with it
     * @return Returns TRUE only if there is a {@linkplain CategoryBuilder} registered
     *         in the project lookup
     */
    @Override
    public boolean isAvailable() {
        return isAvailable(this.project);
    }

    /**
     * A categorization is only available if there is a {@linkplain CategoryBuilder}
     * associated with it.
     * The static method is defined here so the availability can be checked without
     * unnecessary creation of the {@linkplain ProjectCategorization} instance
     * @return Returns TRUE only if there is a {@linkplain CategoryBuilder} registered
     *         in the project lookup
     */
    public static boolean isAvailable(Lookup.Provider project) {
        if (project == null) return false;
        return project.getLookup().lookup(CategoryBuilder.class) != null;
    }

    @Override
    public MarkMapping[] getMappings() {
        CategoryDefinitionProcessor mp = project.getLookup().lookup(CategoryDefinitionProcessor.class);
        if (mp != null) {
            getRoot().accept(new Visitor<Visitable<Category>, Void, CategoryDefinitionProcessor>() {

                @Override
                public Void visit(Visitable<Category> visitable, CategoryDefinitionProcessor parameter) {
                    visitable.getValue().processDefinitionsWith(parameter);
                    return null;
                }
            }, mp);
            return ((Marker)mp).getMappings();
        }
        return new MarkMapping[0];
    }
}
