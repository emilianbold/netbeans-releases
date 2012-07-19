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
import org.netbeans.modules.css.visual.api.SortMode;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * A node representing a CSS rule with no children. The node properties
 * represents the css rule properties.
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "rule.properties=Properties",
    "rule.properties.description=Properties of the css rule",
    "rule.properties.add.declaration.tooltip=Enter a value to add this property to the selected rule",
    "rule.global.set.displayname=All Categories",
    "rule.global.set.tooltip=Properties from All Categories"
    
})
public class RuleNode extends AbstractNode {

    private static final Comparator<PropertyDefinition> PROPERTY_DEFINITIONS_COMPARATOR = new Comparator<PropertyDefinition>() {
        @Override
        public int compare(PropertyDefinition pd1, PropertyDefinition pd2) {
            String pd1name = pd1.getName();
            String pd2name = pd2.getName();
            
             //sort the vendor spec. props below the common ones
            boolean d1vendor = Properties.isVendorSpecificPropertyName(pd1name);
            boolean d2vendor = Properties.isVendorSpecificPropertyName(pd2name);
            
            if(d1vendor && !d2vendor) {
                return +1;
            } else if(!d1vendor && d2vendor) {
                return -1;
            }
            
            return pd1name.compareTo(pd2name);
        }
    };
    
    private static final Comparator<Declaration> DECLARATIONS_COMPARATOR = new Comparator<Declaration>() {
        @Override
        public int compare(Declaration d1, Declaration d2) {
            String d1Name = d1.getProperty().getContent().toString();
            String d2Name = d2.getProperty().getContent().toString();
            
            //sort the vendor spec. props below the common ones
            boolean d1vendor = Properties.isVendorSpecificPropertyName(d1Name);
            boolean d2vendor = Properties.isVendorSpecificPropertyName(d2Name);
            
            if(d1vendor && !d2vendor) {
                return +1;
            } else if(!d1vendor && d2vendor) {
                return -1;
            }
            
            return d1Name.compareTo(d2Name);
        }
    };
    
    private PropertySet[] propertySets;
    private RuleEditorPanel panel;
    
    public RuleNode(RuleEditorPanel panel) {
        super(new RuleChildren());
        this.panel = panel;
    }

    public Model getModel() {
        return panel.getModel();
    }

    public Rule getRule() {
        return panel.getRule();
    }

    public boolean isShowAllProperties() {
        return panel.isShowAllProperties();
    }

    public boolean isShowCategories() {
        return panel.isShowCategories();
    }

    public SortMode getSortMode() {
        return panel.getSortMode();
    }

    //called by the RuleEditorPanel when any of the properties affecting 
    //the PropertySet-s generation changes.
    void fireContextChanged() {
        final PropertySet[] old = getPropertySets();
        propertySets = createPropertySets();
        firePropertySetsChange(old, propertySets);
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
        if(getModel() == null || getRule() == null) {
            return new PropertySet[]{};
        }
        Collection<PropertySet> sets = new ArrayList<PropertySet>();
        List<Declaration> declarations = getRule().getDeclarations() == null 
                ? Collections.<Declaration>emptyList()
                : getRule().getDeclarations().getDeclarations();
        
        
        if (isShowCategories()) {
            //create property sets for property categories

            Map<PropertyCategory, List<Declaration>> categoryToDeclarationsMap = new EnumMap<PropertyCategory, List<Declaration>>(PropertyCategory.class);
            for (Declaration d : declarations) {
                //check the declaration
                org.netbeans.modules.css.model.api.Property property = d.getProperty();
                PropertyValue propertyValue = d.getPropertyValue();
                if (property != null && propertyValue != null) {
                    PropertyDefinition def = Properties.getProperty(property.getContent().toString());
                    if (def != null) {
                        PropertyCategory category = def.getPropertyCategory();

                        List<Declaration> values = categoryToDeclarationsMap.get(category);
                        if (values == null) {
                            values = new LinkedList<Declaration>();
                            categoryToDeclarationsMap.put(category, values);
                        }
                        values.add(d);
                    }
                }
            }

            Map<PropertyCategory, PropertyCategoryPropertySet> propertySetsMap = new EnumMap<PropertyCategory, PropertyCategoryPropertySet>(PropertyCategory.class);
            for (Entry<PropertyCategory, List<Declaration>> entry : categoryToDeclarationsMap.entrySet()) {
                
                List<Declaration> categoryDeclarations = entry.getValue();
                if (getSortMode() == SortMode.ALPHABETICAL) {
                    Collections.sort(categoryDeclarations, DECLARATIONS_COMPARATOR);
                }
                
                PropertyCategoryPropertySet propertyCategoryPropertySet = new PropertyCategoryPropertySet(entry.getKey(), categoryDeclarations);
                propertySetsMap.put(entry.getKey(), propertyCategoryPropertySet);
                sets.add(propertyCategoryPropertySet);
            }

            if (isShowAllProperties()) {
                //Show all properties
                for (PropertyCategory cat : PropertyCategory.values()) {
                    PropertyCategoryPropertySet propertySet = propertySetsMap.get(cat);
                    if (propertySet == null) {
                        propertySet = new PropertyCategoryPropertySet(cat, Collections.<Declaration>emptyList());
                        sets.add(propertySet);
                    } 
                    //now add all the remaining properties
                    List<PropertyDefinition> allInCat = new LinkedList<PropertyDefinition>(cat.getProperties());
                    

                    Collections.sort(allInCat, PROPERTY_DEFINITIONS_COMPARATOR);

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

        } else {
            //not showCategories

            //do not create property sets since the natural ordering if no categorized view
            //is enabled does not work then.

            
            List<Declaration> filtered = new ArrayList<Declaration>();
            for (Declaration d : declarations) {
                //check the declaration
                org.netbeans.modules.css.model.api.Property property = d.getProperty();
                PropertyValue propertyValue = d.getPropertyValue();
                if (property != null && propertyValue != null) {
                    PropertyDefinition def = Properties.getProperty(property.getContent().toString());
                    if (def != null) {
                        filtered.add(d);
                    }
                }
            }

            if (getSortMode() == SortMode.ALPHABETICAL) {
                Collections.sort(filtered, DECLARATIONS_COMPARATOR);
            }

            //just create one top level property set for virtual category (the items actually doesn't belong to the category)
            PropertyCategoryPropertySet set = new PropertyCategoryPropertySet(PropertyCategory.DEFAULT, filtered);
            //overrride the default descriptions
            set.setDisplayName(Bundle.rule_global_set_displayname());
            set.setShortDescription(Bundle.rule_global_set_tooltip());
            
            sets.add(set);

            if (isShowAllProperties()) {
                //Show all properties
                List<PropertyDefinition> all = new ArrayList<PropertyDefinition>(Properties.getProperties(true));
                Collections.sort(all, PROPERTY_DEFINITIONS_COMPARATOR);

                //remove already used
                for (Declaration d : set.declarations) {
                    PropertyDefinition def = Properties.getProperty(d.getProperty().getContent().toString());
                    all.remove(def);
                }

                //add the rest of unused properties to the property set
                for (PropertyDefinition pd : all) {
                    set.properties.add(new PropertyDefinitionProperty(pd));
                }

            }
        }
        
        

        return sets.toArray(new PropertySet[0]);
    }

    public void applyModelChanges() {
        final Model model = getModel();
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

    private class PropertyDefinitionProperty extends PropertySupport<String> {

        private PropertyDefinition def;
        private static final String EMPTY = "";

        public PropertyDefinitionProperty(PropertyDefinition def) {
            super(def.getName(), String.class, def.getName(), Bundle.rule_properties_add_declaration_tooltip(), true, getRule().isValid());
            this.def = def;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return EMPTY;
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            //add a new declaration to the rule
            ElementFactory factory = getModel().getElementFactory();
            
            Rule rule = getRule();
            Declarations declarations = rule.getDeclarations();

            if(declarations == null) {
                //empty rule, create declarations node as well
                declarations = factory.createDeclarations();
                rule.setDeclarations(declarations);
            }

            org.netbeans.modules.css.model.api.Property property = factory.createProperty(def.getName());
            Expression expr = factory.createExpression(val);
            PropertyValue value = factory.createPropertyValue(expr);
            Declaration newDeclaration = factory.createDeclaration(property, value, false);

            declarations.addDeclaration(newDeclaration);

            //save the model to the source
            applyModelChanges();
        }
    }

    private class DeclarationProperty extends PropertySupport<String> {

        private Declaration declaration;

        public DeclarationProperty(Declaration declaration) {
            super(declaration.getProperty().getContent().toString(),
                    String.class,
                    declaration.getProperty().getContent().toString(),
                    null, true, getRule().isValid());
            this.declaration = declaration;
        }

        @Override
        public String getHtmlDisplayName() {
            return isShowAllProperties() 
                    ? new StringBuilder()
                    .append("<b>")
                    .append(declaration.getProperty().getContent())
                    .append("</b>")
                    .toString()
                    : super.getHtmlDisplayName();
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
