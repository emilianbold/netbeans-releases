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
import org.netbeans.modules.css.visual.api.DeclarationInfo;
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
    
    private PropertyCategoryPropertySet[] propertySets;
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
    
    public boolean isAddPropertyMode() {
        return panel.isAddPropertyMode();
    }

    //called by the RuleEditorPanel when any of the properties affecting 
    //the PropertySet-s generation changes.
    void fireContextChanged() {
        final PropertySet[] old = getPropertySets();
        propertySets = createPropertySets();
        firePropertySetsChange(old, propertySets);
    }
    
    void fireDeclarationInfoChanged(Declaration declaration, DeclarationInfo declarationInfo) {
        for(PropertyCategoryPropertySet set : getCachedPropertySets()) {
            DeclarationProperty declarationProperty = set.getDeclarationProperty(declaration);
            if(declarationProperty != null) {
                declarationProperty.setDeclarationInfo(declarationInfo);
                break;
            }
        }
    }
    
    @Override
    public synchronized PropertySet[] getPropertySets() {
        return getCachedPropertySets();
    }
    
    private synchronized PropertyCategoryPropertySet[] getCachedPropertySets() {
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
    private PropertyCategoryPropertySet[] createPropertySets() {
        if(getModel() == null || getRule() == null) {
            return new PropertyCategoryPropertySet[]{};
        }
        Collection<PropertyCategoryPropertySet> sets = new ArrayList<PropertyCategoryPropertySet>();
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
                
                PropertyCategoryPropertySet propertyCategoryPropertySet = new PropertyCategoryPropertySet(entry.getKey());
                propertyCategoryPropertySet.addAll(categoryDeclarations);
                
                propertySetsMap.put(entry.getKey(), propertyCategoryPropertySet);
                sets.add(propertyCategoryPropertySet);
            }

            if (isShowAllProperties()) {
                //Show all properties
                for (PropertyCategory cat : PropertyCategory.values()) {
                    PropertyCategoryPropertySet propertySet = propertySetsMap.get(cat);
                    if (propertySet == null) {
                        propertySet = new PropertyCategoryPropertySet(cat);
                        sets.add(propertySet);
                    } 
                    //now add all the remaining properties
                    List<PropertyDefinition> allInCat = new LinkedList<PropertyDefinition>(cat.getProperties());
                    

                    Collections.sort(allInCat, PROPERTY_DEFINITIONS_COMPARATOR);

                    //remove already used
                    for (Declaration d : propertySet.getDeclarations()) {
                        PropertyDefinition def = Properties.getProperty(d.getProperty().getContent().toString());
                        allInCat.remove(def);
                    }

                    //add the rest of unused properties to the property set
                    for (PropertyDefinition pd : allInCat) {
                        propertySet.add(pd);
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
            PropertyCategoryPropertySet set = new PropertyCategoryPropertySet(PropertyCategory.DEFAULT);
            set.addAll(filtered);
            
            //overrride the default descriptions
            set.setDisplayName(Bundle.rule_global_set_displayname());
            set.setShortDescription(Bundle.rule_global_set_tooltip());
            
            sets.add(set);

            if (isShowAllProperties()) {
                //Show all properties
                List<PropertyDefinition> all = new ArrayList<PropertyDefinition>(Properties.getProperties(true));
                Collections.sort(all, PROPERTY_DEFINITIONS_COMPARATOR);

                //remove already used
                for (Declaration d : set.getDeclarations()) {
                    PropertyDefinition def = Properties.getProperty(d.getProperty().getContent().toString());
                    all.remove(def);
                }

                //add the rest of unused properties to the property set
                for (PropertyDefinition pd : all) {
                    set.add(pd);
                }

            }
        }
        
        

        return sets.toArray(new PropertyCategoryPropertySet[0]);
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

        private List<Property> properties = new ArrayList<Property>();
        
        private Map<Declaration, DeclarationProperty> declaration2PropertyMap = new HashMap<Declaration, DeclarationProperty>();

        public PropertyCategoryPropertySet(PropertyCategory propertyCategory) {
            super(propertyCategory.name(), //NOI18N
                    propertyCategory.getDisplayName(),
                    propertyCategory.getShortDescription());
        }
        
        public void add(Declaration declaration) {
            DeclarationProperty property = new DeclarationProperty(declaration);
            declaration2PropertyMap.put(declaration, property);
            properties.add(property);
        }
        
        public void addAll(Collection<Declaration> declarations) {
            for(Declaration d : declarations) {
                add(d);
            }
        }
        
        public Collection<Declaration> getDeclarations() {
            return declaration2PropertyMap.keySet();
        }
        
        public DeclarationProperty getDeclarationProperty(Declaration declaration) {
            return declaration2PropertyMap.get(declaration);
        }
        
        public void add(PropertyDefinition propertyDefinition) {
            properties.add(new PropertyDefinitionProperty(propertyDefinition));
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
            if(EMPTY.equals(val)) {
                return ; //no change
            }
            
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
            if(!isAddPropertyMode()) {
                applyModelChanges();
            } else {
                fireContextChanged();
            }
        }
    }

    private class DeclarationProperty extends PropertySupport<String> {

        private Declaration declaration;
        private DeclarationInfo info;

        public DeclarationProperty(Declaration declaration) {
            super(declaration.getProperty().getContent().toString(),
                    String.class,
                    declaration.getProperty().getContent().toString(),
                    null, true, getRule().isValid() && !isAddPropertyMode());
            this.declaration = declaration;
        }
        
        public void setDeclarationInfo(DeclarationInfo info) {
            String old = getHtmlDisplayName();
            this.info = info;
            fireDisplayNameChange(old, getHtmlDisplayName());
        }
        
        private boolean isOverridden() {
            return info != null && info == DeclarationInfo.OVERRIDDEN;
        }

        @Override
        public String getHtmlDisplayName() {
            StringBuilder b = new StringBuilder();
            
            if(isShowAllProperties()) {
                b.append(isAddPropertyMode() ? "<font color=777777>" : "<b>"); //NOI18N
            }
            if(isOverridden()) {
                b.append("<s>"); //use <del>?
            }
            
            b.append(declaration.getProperty().getContent());
            
            if(isOverridden()) {
                b.append("</s>"); //use <del>?
            }
            if(isShowAllProperties()) {
                b.append(isAddPropertyMode() ? "</font>" : "</b>"); //NOI18N
            }
            
            return b.toString();
        }
            
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return declaration.getPropertyValue().getExpression().getContent().toString();
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if(val.isEmpty()) {
                //remove the whole declaration
                Declarations declarations = (Declarations)declaration.getParent();
                declarations.removeDeclaration(declaration);
            } else {
                //update the value
                declaration.getPropertyValue().getExpression().setContent(val);
            }
            
            if(!isAddPropertyMode()) {
                applyModelChanges();
            } else {
                fireContextChanged();
            }
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
