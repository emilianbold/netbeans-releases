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

package org.netbeans.modules.xml.wsdl.ui.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.ui.category.Category;
import org.netbeans.modules.xml.xam.ui.search.Query;
import org.netbeans.modules.xml.xam.ui.search.SearchException;
import org.netbeans.modules.xml.xam.ui.search.SearchProvider;
import org.netbeans.modules.xml.xam.ui.search.WildcardStringMatcher;
import org.openide.util.NbBundle;

/**
 * Implements a SearchProvider that compares the value of the name attribute
 * with the query string, using a case-insensitive string comparison.
 *
 * @author Nathan Fiedler
 */
public class ComponentNameSearchProvider extends ChildVisitor
        implements SearchProvider {
    /** The last query submitted by the user, if any, lower-cased. */
    private String phrase;
    /** True if the phrase contains wildcards (e.g. * or ?). */
    private boolean wildcarded;
    /** Model in which to perform the search. */
    private WSDLModel model;
    /** List of matching components. */
    private List<Object> results;
    /** Provides the selected component, if needed. */
    private Category category;
    /** The compiled regular expression pattern, if provided. */
    private Pattern pattern;

    /**
     * Creates a new instance of ComponentNameSearchProvider.
     *
     * @param  model     model in which to perform search.
     * @param  category  provides the selected component.
     */
    public ComponentNameSearchProvider(WSDLModel model, Category category) {
        this.model = model;
        this.category = category;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ComponentNameSearchProvider.class,
                "LBL_SearchProvider_ComponentName");
    }

    public String getInputDescription() {
        return NbBundle.getMessage(ComponentNameSearchProvider.class,
                "HELP_SearchProvider_ComponentName");
    }

    public String getShortDescription() {
        return NbBundle.getMessage(ComponentNameSearchProvider.class,
                "HINT_SearchProvider_ComponentName");
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
        // Search for named components with the given name.
        if (query.useSelected()) {
            WSDLComponent component = Providers.getSelectedComponent(category);
            if (component != null) {
                component.accept(this);
            } else {
                // Maybe it is a category node that is selected.
                Class<? extends WSDLComponent> childType =
                        Providers.getSelectedChildType(category);
                if (childType != null) {
                    List<? extends WSDLComponent> components =
                            model.getDefinitions().getChildren(childType);
                    for (WSDLComponent comp : components) {
                        comp.accept(this);
                    }
                }
            }
        } else {
            model.getDefinitions().accept(this);
        }
        return results;
    }

    protected void visitComponent(WSDLComponent comp) {
        if (comp instanceof Named) {
            Named ncontainer = (Named) comp;
            String name = ncontainer.getName();
            if (name != null) {
                if (phrase != null) {
                    name = name.toLowerCase();
                    if (wildcarded) {
                        if (WildcardStringMatcher.match(name, phrase)) {
                            results.add(comp);
                        }
                    } else if (name.indexOf(phrase) > -1) {
                        results.add(comp);
                    }
                } else if (pattern != null) {
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.find()) {
                        results.add(comp);
                    }
                }
            }
        }
        // Visit the children last, to get results in breadth-first order.
        super.visitComponent(comp);
    }
}
