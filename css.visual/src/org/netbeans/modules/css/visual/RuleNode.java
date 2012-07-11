/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.visual;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.model.api.*;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a CSS rule with no children. The node properties
 * represents the css rule properties.
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "rule.properties=Properties",
    "rule.properties.description=Properties of the css rule",
    "rule.properties.add.declaration.tooltip=Enter a value to add this property to the selected rule"
})
public class RuleNode extends AbstractNode {

    private PropertySet[] propertySets;
    private Model model;
    private Rule rule;
    private boolean showAllProperties;

    public RuleNode(Model model, Rule rule, boolean showAllProperties) {
        super(new RuleChildren(), Lookups.fixed(rule));
        this.model = model;
        this.rule = rule;
        this.showAllProperties = showAllProperties;
    }

    @Override
    public synchronized PropertySet[] getPropertySets() {
        if (propertySets == null) {
            propertySets = createPropertySets();
        }
        return propertySets;
    }

    /**
     * Creates property sets of the node.
     *
     * @return property sets of the node.
     */
    private PropertySet[] createPropertySets() {
        Collection<PropertySet> sets = new ArrayList<PropertySet>();

        Collection<Declaration> declarations = rule.getDeclarations().getDeclarations();

        Map<PropertyCategory, Collection<Declaration>> categoryToDeclarationsMap = new EnumMap<PropertyCategory, Collection<Declaration>>(PropertyCategory.class);
        for (Declaration d : declarations) {
            //check the declaration
            org.netbeans.modules.css.model.api.Property property = d.getProperty();
            PropertyValue propertyValue = d.getPropertyValue();
            if (property != null && propertyValue != null) {
                PropertyDefinition def = Properties.getProperty(property.getContent().toString());
                if (def != null) {
                    PropertyCategory category = def.getPropertyCategory();

                    Collection<Declaration> values = categoryToDeclarationsMap.get(category);
                    if (values == null) {
                        values = new LinkedList<Declaration>();
                        categoryToDeclarationsMap.put(category, values);
                    }
                    values.add(d);
                }
            }
        }

        Map<PropertyCategory, PropertyCategoryPropertySet> existing = new EnumMap<PropertyCategory, PropertyCategoryPropertySet>(PropertyCategory.class);
        for (Entry<PropertyCategory, Collection<Declaration>> entry : categoryToDeclarationsMap.entrySet()) {
            PropertyCategoryPropertySet propertyCategoryPropertySet = new PropertyCategoryPropertySet(entry.getKey(), entry.getValue());
            existing.put(entry.getKey(), propertyCategoryPropertySet);
            sets.add(propertyCategoryPropertySet);
        }

        if (showAllProperties) {
            //Show all properties
            //
            //add the existing - unused properties separator to the already added caregories
            //and create set for unused categories
            for (PropertyCategory cat : PropertyCategory.values()) {
                PropertyCategoryPropertySet propertySet = existing.get(cat);
                if (propertySet == null) {
                    propertySet = new PropertyCategoryPropertySet(cat, Collections.<Declaration>emptyList());
                    sets.add(propertySet);
                } else {
                    //hack: add a separator
                    propertySet.properties.add(new SeparatorHackProperty());
                }
                //now add all the remaining properties
                List<PropertyDefinition> allInCat = new LinkedList<PropertyDefinition>(cat.getProperties());

                Collections.sort(allInCat, new Comparator<PropertyDefinition>() {
                    @Override
                    public int compare(PropertyDefinition pd1, PropertyDefinition pd2) {
                        return pd1.getName().compareTo(pd2.getName());
                    }
                });

                //remove already used
                for (Declaration d : propertySet.declarations) {
                    PropertyDefinition def = Properties.getProperty(d.getProperty().getContent().toString());
                    allInCat.remove(def);
                }

                //add the rest of unused properties to the property set
                for (PropertyDefinition pd : allInCat) {
                    propertySet.properties.add(new PropertyDefinitionProperty(pd));
                }

            }

        }

        return sets.toArray(new PropertySet[0]);
    }

    public void applyModelChanges() {
        final NbEditorDocument doc = (NbEditorDocument) model.getLookup().lookup(Document.class);
        if (doc == null) {
            return;
        }
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                doc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            model.applyChanges();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });

            }
        });
    }

    private class PropertyCategoryPropertySet extends PropertySet {

        private List<Property> properties;
        private Collection<Declaration> declarations;

        public PropertyCategoryPropertySet(PropertyCategory propertyCategory, Collection<Declaration> declarations) {
            super(propertyCategory.name(), //NOI18N
                    propertyCategory.getDisplayName(),
                    propertyCategory.getShortDescription());
            this.declarations = declarations;

            properties = new ArrayList<Property>(declarations.size());
            for (Declaration d : declarations) {
                properties.add(new DeclarationProperty(d));
            }
        }

        @Override
        public Property<String>[] getProperties() {
            return properties.toArray(new Property[]{});
        }
    }

    private static class SeparatorHackProperty extends PropertySupport.ReadOnly<String> {

        private static final String EMPTY = ""; //huh that's really nice :-)

        public SeparatorHackProperty() {
            super(EMPTY, String.class, EMPTY, "All the properties below are not declared in the selected rule"); //XXX no i18n since the is likely to be removed
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return EMPTY;
        }
    }

    private class PropertyDefinitionProperty extends PropertySupport.ReadWrite<String> {

        private PropertyDefinition def;
        private static final String EMPTY = "";

        public PropertyDefinitionProperty(PropertyDefinition def) {
            super(def.getName(), String.class, def.getName(), Bundle.rule_properties_add_declaration_tooltip());
            this.def = def;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return EMPTY;
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            //add a new declaration to the rule
            Declarations declarations = rule.getDeclarations();

            ElementFactory factory = model.getElementFactory();

            org.netbeans.modules.css.model.api.Property property = factory.createProperty(def.getName());
            Expression expr = factory.createExpression(val);
            PropertyValue value = factory.createPropertyValue(expr);
            Declaration newDeclaration = model.getElementFactory().createDeclaration(property, value, false);

            declarations.addDeclaration(newDeclaration);

            //save the model to the source
            applyModelChanges();
        }
    }

    private class DeclarationProperty extends PropertySupport.ReadWrite<String> {

        private Declaration declaration;

        public DeclarationProperty(Declaration declaration) {
            super(declaration.getProperty().getContent().toString(),
                    String.class,
                    declaration.getProperty().getContent().toString(),
                    null);
            this.declaration = declaration;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return declaration.getPropertyValue().getExpression().getContent().toString();
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            declaration.getPropertyValue().getExpression().setContent(val);
            applyModelChanges();
        }
    }

    /**
     * Empty children keys
     */
    private static class RuleChildren extends Children.Keys {

        @Override
        protected Node[] createNodes(Object key) {
            return new Node[]{};
        }
    }
}
