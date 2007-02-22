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
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;
import org.netbeans.modules.xml.xam.ui.category.Category;
import org.netbeans.modules.xml.xam.ui.search.Query;
import org.netbeans.modules.xml.xam.ui.search.SearchException;
import org.netbeans.modules.xml.xam.ui.search.SearchProvider;
import org.netbeans.modules.xml.xam.ui.search.WildcardStringMatcher;
import org.openide.util.NbBundle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Implements a SearchProvider that compares the name of each attribute
 * with the query string, using a case-insensitive string comparison.
 *
 * @author Nathan Fiedler
 */
public class AttributeNameSearchProvider extends DeepSchemaVisitor
        implements SearchProvider {
    /** The last query submitted by the user, if any, lower-cased. */
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
     * Creates a new instance of AttributeNameSearchProvider.
     *
     * @param  model     schema model in which to perform search.
     * @param  category  provides the selected component.
     */
    public AttributeNameSearchProvider(SchemaModel model, Category category) {
        this.model = model;
        this.category = category;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AttributeNameSearchProvider.class,
                "LBL_SearchProvider_AttributeName");
    }

    public String getInputDescription() {
        return NbBundle.getMessage(AttributeNameSearchProvider.class,
                "HELP_SearchProvider_AttributeName");
    }

    public String getShortDescription() {
        return NbBundle.getMessage(AttributeNameSearchProvider.class,
                "HINT_SearchProvider_AttributeName");
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
        // Search for components with the given attribute name.
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

    protected void visitChildren(SchemaComponent comp) {
        NamedNodeMap attrs = comp.getPeer().getAttributes();
        for (int ii = 0; ii < attrs.getLength(); ii++) {
            Node attr = attrs.item(ii);
            String name = attr.getNodeName();
            if (phrase != null) {
                name = name.toLowerCase();
                if (wildcarded) {
                    if (WildcardStringMatcher.match(name, phrase)) {
                        results.add(comp);
                    }
                } else if (name.indexOf(phrase) > -1) {
                    results.add(comp);
                    break;
                }
            } else if (pattern != null) {
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    results.add(comp);
                    break;
                }
            }
        }
        // Visit the children last, to get results in breadth-first order.
        super.visitChildren(comp);
    }
}
