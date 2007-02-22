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
