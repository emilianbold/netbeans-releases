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

import java.beans.PropertyEditor;
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
import org.netbeans.modules.css.lib.api.properties.FixedTextGrammarElement;
import org.netbeans.modules.css.lib.api.properties.GrammarElementVisitor;
import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.PropertyModel;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.UnitGrammarElement;
import org.netbeans.modules.css.model.api.*;
import org.netbeans.modules.css.visual.api.DeclarationInfo;
import org.netbeans.modules.css.visual.api.SortMode;
import org.netbeans.modules.css.visual.editors.PropertyValuesEditor;
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

    private static String COLOR_CODE_GRAY = "777777";
    private static String COLOR_CODE_RED = "ff7777";
    
    public static String NONE_PROPERTY_NAME = "<none>";
    
    
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
        DeclarationProperty dp = getDeclarationProperty(declaration);
        if(dp != null) {
            dp.setDeclarationInfo(declarationInfo);
        }
    }
    
    DeclarationProperty getDeclarationProperty(Declaration declaration) {
        for (PropertyCategoryPropertySet set : getCachedPropertySets()) {
            DeclarationProperty declarationProperty = set.getDeclarationProperty(declaration);
            if (declarationProperty != null) {
                return declarationProperty;
            }
        }
        return null;
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
        if (getModel() == null || getRule() == null) {
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
                    Collections.sort(categoryDeclarations, PropertyUtils.DECLARATIONS_COMPARATOR);
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


                    Collections.sort(allInCat, PropertyUtils.PROPERTY_DEFINITIONS_COMPARATOR);

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
                Collections.sort(filtered, PropertyUtils.DECLARATIONS_COMPARATOR);
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
                Collections.sort(all, PropertyUtils.PROPERTY_DEFINITIONS_COMPARATOR);

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
            DeclarationProperty property = createDeclarationProperty(declaration);
            declaration2PropertyMap.put(declaration, property);
            properties.add(property);
        }

        public void addAll(Collection<Declaration> declarations) {
            for (Declaration d : declarations) {
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
            properties.add(createPropertyDefinitionProperty(propertyDefinition));
        }

        @Override
        public Property<String>[] getProperties() {
            return properties.toArray(new Property[]{});
        }
    }

     private PropertyDefinitionProperty createPropertyDefinitionProperty(PropertyDefinition definition) {
        PropertyModel pmodel = Properties.getPropertyModel(definition.getName());
        return new PropertyDefinitionProperty(definition, createPropertyValueEditor(pmodel, false));
    }
    
     private PropertyValuesEditor createPropertyValueEditor(PropertyModel pmodel, boolean addNoneProperty) {
        GroupGrammarElement rootElement = pmodel.getGrammarElement();
        final Collection<UnitGrammarElement> unitElements = new ArrayList<UnitGrammarElement>();
        final Collection<FixedTextGrammarElement> fixedElements = new ArrayList<FixedTextGrammarElement>();

        rootElement.accept(new GrammarElementVisitor() {

            @Override
            public void visit(UnitGrammarElement element) {
                unitElements.add(element);
            }

            @Override
            public void visit(FixedTextGrammarElement element) {
                fixedElements.add(element);
            }
            
        });
        
        if(!fixedElements.isEmpty()) {
            return  new PropertyValuesEditor(fixedElements, unitElements, addNoneProperty);
        }
        
        return null;
     }
     
    private class PropertyDefinitionProperty extends PropertySupport<String> {

        private PropertyDefinition def;
        private static final String EMPTY = "";
        
        private PropertyEditor editor;

        public PropertyDefinitionProperty(PropertyDefinition def, PropertyEditor editor) {
            super(def.getName(), String.class, def.getName(), Bundle.rule_properties_add_declaration_tooltip(), true, getRule().isValid());
            this.def = def;
            this.editor = editor;
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return EMPTY;
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (EMPTY.equals(val)) {
                return; //no change
            }

            //add a new declaration to the rule
            ElementFactory factory = getModel().getElementFactory();

            Rule rule = getRule();
            Declarations declarations = rule.getDeclarations();

            if (declarations == null) {
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
            if (!isAddPropertyMode()) {
                applyModelChanges();
            } else {
                fireContextChanged();
            }
        }
    }

    private DeclarationProperty createDeclarationProperty(Declaration declaration) {
        ResolvedProperty resolvedProperty = declaration.getResolvedProperty();
        return new DeclarationProperty(declaration, createPropertyValueEditor(resolvedProperty.getPropertyModel(), true));
    }

    class DeclarationProperty extends PropertySupport {

        private Declaration declaration;
        private DeclarationInfo info;
        private PropertyEditor editor;

        public DeclarationProperty(Declaration declaration, PropertyEditor editor) {
            super(declaration.getProperty().getContent().toString(),
                    String.class,
                    declaration.getProperty().getContent().toString(),
                    null, true, getRule().isValid() && !isAddPropertyMode());
            this.declaration = declaration;
            this.editor = editor;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        public void setDeclarationInfo(DeclarationInfo info) {
            String old = getHtmlDisplayName();
            this.info = info;
            fireDisplayNameChange(old, getHtmlDisplayName());
        }

        private boolean isOverridden() {
            return info != null && info == DeclarationInfo.OVERRIDDEN;
        }
        
        private boolean isInactive() {
            return info != null && info == DeclarationInfo.INACTIVE;
        }

        private boolean isErroneous() {
            return info != null && info == DeclarationInfo.ERRONEOUS;
        }

        @Override
        public String getHtmlDisplayName() {
            StringBuilder b = new StringBuilder();
            
            String color = null;
            boolean bold = false;
            boolean strike = false;

            if (isShowAllProperties()) {
                if(isAddPropertyMode()) {
                    color = COLOR_CODE_GRAY;
                } else {
                    bold = true;
                }
            }
            if(isOverridden()) {
                strike = true;
            }
            if(isInactive()) {
                color = COLOR_CODE_GRAY;
                strike = true;
            }
            if(isErroneous()) {
                color = COLOR_CODE_RED;
            }

            //render
            if(bold) {
                b.append("<b>");//NOI18N
            }
            if (strike) {
                b.append("<s>"); //use <del>?
            }
            if(color != null) {
                b.append("<font color="); //NOI18N
                b.append(color);
                b.append(">"); //NOI18N
            }
            
            b.append(declaration.getProperty().getContent());
            
            if(color != null) {
                b.append("</font>"); //NOI18N
            }
            if (strike) {
                b.append("</s>"); //use <del>?
            }
            if(bold) {
                b.append("</b>");//NOI18N
            }

            return b.toString();
        }

        @Override
        public String getValue() {
            return declaration.getPropertyValue().getExpression().getContent().toString();
        }

        @Override
        public void setValue(Object o) {
            String val = (String)o;
            if (val.isEmpty() || NONE_PROPERTY_NAME.equals(val)) {
                //remove the whole declaration
                Declarations declarations = (Declarations) declaration.getParent();
                declarations.removeDeclaration(declaration);
            } else {
                //update the value
                declaration.getPropertyValue().getExpression().setContent(val);
            }

            if (!isAddPropertyMode()) {
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
