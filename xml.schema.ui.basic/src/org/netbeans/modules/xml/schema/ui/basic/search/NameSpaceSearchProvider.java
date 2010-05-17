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

package org.netbeans.modules.xml.schema.ui.basic.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.xam.ui.category.Category;
import org.netbeans.modules.xml.xam.ui.search.Query;
import org.netbeans.modules.xml.xam.ui.search.SearchException;
import org.netbeans.modules.xml.xam.ui.search.SearchProvider;
import org.netbeans.modules.xml.xam.ui.search.WildcardStringMatcher;
import org.openide.util.NbBundle;

/**
 * Implements a SearchProvider that looks for components belonging to a
 * schema imported under a particular namespace, using a case-insensitive
 * wildcard comparison.
 *
 * @author Nathan Fiedler
 */
public class NameSpaceSearchProvider extends DeepSchemaVisitor
        implements SearchProvider {
    /** The last query submitted by the user, if any, lower-cased; may
     * contain wildcards. */
    private String phrase;
    /** True if the phrase contains wildcards (e.g. * or ?). */
    private boolean wildcarded;
    /** Model in which to perform the search. */
    private SchemaModel model;
    /** List of matching schema components. */
    private List<Object> results;
    /** Provides the selected component, if needed. */
    private Category category;
    /** The compiled regular expression pattern, if provided. */
    private Pattern pattern;

    /**
     * Creates a new instance of NameSpaceSearchProvider.
     *
     * @param  model     schema model in which to perform search.
     * @param  category  provides the selected component.
     */
    public NameSpaceSearchProvider(SchemaModel model, Category category) {
        this.model = model;
        this.category = category;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(NameSpaceSearchProvider.class,
                "LBL_SearchProvider_NameSpace");
    }

    public String getInputDescription() {
        return NbBundle.getMessage(NameSpaceSearchProvider.class,
                "HELP_SearchProvider_NameSpace");
    }

    public String getShortDescription() {
        return NbBundle.getMessage(NameSpaceSearchProvider.class,
                "HINT_SearchProvider_NameSpace");
    }

    public List<Object> search(Query query) throws SearchException {
        if (query.isRegularExpression()) {
            try {
                pattern = Pattern.compile(query.getQuery());
                phrase = null;
            } catch (PatternSyntaxException pse) {
                throw new SearchException(pse.getMessage(), pse);
            }

        } else {
            pattern = null;
            phrase = query.getQuery().toLowerCase();
            wildcarded = WildcardStringMatcher.containsWildcards(phrase);
        }
        results = new ArrayList<Object>();
        // Search for named references with the given namespace.
        if (query.useSelected()) {
            SchemaComponent component = Providers.getSelectedComponent(category);
            if (component != null) {
                component.accept(this);
            } else {
                // Maybe it is a category node that is selected.
                Class<? extends SchemaComponent> childType =
                        Providers.getSelectedChildType(category);
                if (childType != null) {
                    List<? extends SchemaComponent> components =
                            model.getSchema().getChildren(childType);
                    for (SchemaComponent comp : components) {
                        comp.accept(this);
                    }
                }
            }
        } else {
            model.getSchema().accept(this);
        }
        return results;
    }

    /**
     * Compares the given namespace to the query phrase provided by the user.
     *
     * @param  comp       component to add to result set, if matched.
     * @param  namespace  the name space of the given component.
     */
    private void match(SchemaComponent comp, String namespace) {
        if (namespace != null) {
            if (phrase != null) {
                namespace = namespace.toLowerCase();
                if (wildcarded) {
                    if (WildcardStringMatcher.match(namespace, phrase)) {
                        results.add(comp);
                    }
                } else if (namespace.indexOf(phrase) > -1) {
                    results.add(comp);
                }
            } else if (pattern != null) {
                Matcher matcher = pattern.matcher(namespace);
                if (matcher.find()) {
                    results.add(comp);
                }
            }
        }
    }

    public void visit(AttributeReference ar) {
        super.visit(ar);
        match(ar, ar.getRef().getEffectiveNamespace());
    }

    public void visit(AttributeGroupReference agr) {
        super.visit(agr);
        match(agr, agr.getGroup().getEffectiveNamespace());
    }

    public void visit(ElementReference er) {
        super.visit(er);
        match(er, er.getRef().getEffectiveNamespace());
    }

    public void visit(GroupReference gr) {
        super.visit(gr);
        match(gr, gr.getRef().getEffectiveNamespace());
    }
}
