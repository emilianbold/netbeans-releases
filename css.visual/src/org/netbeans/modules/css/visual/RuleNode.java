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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
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
import org.netbeans.modules.css.lib.api.properties.Token;
import org.netbeans.modules.css.lib.api.properties.UnitGrammarElement;
import org.netbeans.modules.css.model.api.*;
import org.netbeans.modules.css.visual.api.DeclarationInfo;
import org.netbeans.modules.css.visual.api.SortMode;
import org.netbeans.modules.css.visual.editors.PropertyValuesEditor;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

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
    private String filterText;
    private PropertyCategoryPropertySet[] propertySets;
    private RuleEditorPanel panel;
    private Map<PropertyDefinition, Declaration> addedDeclarations = new HashMap<PropertyDefinition, Declaration>();

    private Rule lastRule;
    
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

    //called by the RuleEditorPanel when user types into the filter text field
    void setFilterText(String prefix) {
        this.filterText = prefix;
        fireContextChanged(true); //recreate the property sets
    }

    //called by the RuleEditorPanel when any of the properties affecting 
    //the PropertySet-s generation changes.
    void fireContextChanged(boolean forceRefresh) {
        try {
            PropertyCategoryPropertySet[] oldSets = getCachedPropertySets();
            PropertyCategoryPropertySet[] newSets = createPropertySets();

            if (!forceRefresh) {
                //the client doesn't require the property sets to be really recreated,
                //we may try to update them only if possible

                //compare old and new sets, if they contain same sets with same properties,
                //then update the PropertyDefinition-s so they contain reference to the current
                //css model vertion.
                //
                //if there's a new PropertySet or one of the PropertySets contains more or less
                //properties than the original, then do not do the incremental update but
                //refresh the PropertySets completely.
                update:
                {
                    //old DeclarationProperty to new value map
                    if (oldSets.length == newSets.length) {
                        for (int i = 0; i < oldSets.length; i++) {
                            PropertyCategoryPropertySet o = oldSets[i];
                            PropertyCategoryPropertySet n = newSets[i];

                            Map<Declaration, DeclarationProperty> om = o.declaration2PropertyMap;
                            Map<Declaration, DeclarationProperty> nm = n.declaration2PropertyMap;

                            if (om.size() != nm.size()) {
                                break update;
                            }
                            //same number of declarations

                            //notice: the same order of the properties as in the last model
                            //is ensured by the getUniquePropertyName() method which adds 
                            //index of the property in the rule to its name.
                            
                            //create declaration name -> declaration maps se we may compare 
                            //(as the css source model elements do not comparable by equals/hashcode)
                            Map<String, Declaration> oName2DeclarationMap = new HashMap<String, Declaration>();
                            for (Declaration d : om.keySet()) {
                                oName2DeclarationMap.put(getDeclarationId(lastRule, d), d);
                            }
                            Map<String, Declaration> nName2DeclarationMap = new HashMap<String, Declaration>();
                            for (Declaration d : nm.keySet()) {
                                nName2DeclarationMap.put(getDeclarationId(getRule(), d), d);
                            }

                            //compare the names of the properties in the old and new map,
                            //they must be the same otherwise we wont' marge but recreate 
                            //the whole property sets
                            Collection<String> oldNames = oName2DeclarationMap.keySet();
                            Collection<String> newNames = nName2DeclarationMap.keySet();
                            Collection<String> comp = new HashSet<String>(oldNames);
                            if (comp.retainAll(newNames)) { //assumption: the collections size are the same
                                break update; //canot merge - the collections differ
                            }

                            for (String declarationName : oName2DeclarationMap.keySet()) {
                                Declaration oldD = oName2DeclarationMap.get(declarationName);
                                Declaration newD = nName2DeclarationMap.get(declarationName);

                                //update the existing DeclarationProperty with the fresh
                                //Declaration object from the new model instance
                                DeclarationProperty declarationProperty = om.get(oldD);
                                declarationProperty.updateDeclaration(newD);

                                //also update the declaration2PropertyMap itself 
                                //as we now use new Declaration object
                                om.remove(oldD);
                                om.put(newD, declarationProperty);
                            }

                        }
                        return;
                    }

                }
            }

            //refresh the sets completely
            propertySets = newSets;
            firePropertySetsChange(oldSets, newSets);
        } finally {
            this.lastRule = getRule();
        }
    }

    void fireDeclarationInfoChanged(Declaration declaration, DeclarationInfo declarationInfo) {
        DeclarationProperty dp = getDeclarationProperty(declaration);
        if (dp != null) {
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
        this.lastRule = getRule();
        return getCachedPropertySets();
    }

    private synchronized PropertyCategoryPropertySet[] getCachedPropertySets() {
        if (propertySets == null) {
            propertySets = createPropertySets();
        }
        return propertySets;
    }

    private boolean matchesFilterText(String text) {
        if (filterText == null) {
            return true;
        } else {
            return text.contains(filterText);
        }
    }

    private Collection<PropertyDefinition> filterByPrefix(Collection<PropertyDefinition> defs) {
        Collection<PropertyDefinition> filtered = new ArrayList<PropertyDefinition>();
        for (PropertyDefinition pd : defs) {
            if (matchesFilterText(pd.getName())) {
                filtered.add(pd);
            }
        }
        return filtered;
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
                if (addedDeclarations.containsValue(d)) {
                    continue; //skip those added declarations
                }
                //check the declaration
                org.netbeans.modules.css.model.api.Property property = d.getProperty();
                PropertyValue propertyValue = d.getPropertyValue();
                if (property != null && propertyValue != null) {
                    if (matchesFilterText(property.getContent().toString())) {
                        PropertyDefinition def = Properties.getProperty(property.getContent().toString());
                        PropertyCategory category;
                        if(def != null) {
                            category = def.getPropertyCategory();
                        } else {
                            category = PropertyCategory.UNKNOWN;
                        }

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
                    //now add all the remaining properties
                    List<PropertyDefinition> allInCat = new LinkedList<PropertyDefinition>(filterByPrefix(cat.getProperties()));
                    if (allInCat.isEmpty()) {
                        continue; //skip empty categories (when filtering)
                    }
                    Collections.sort(allInCat, PropertyUtils.PROPERTY_DEFINITIONS_COMPARATOR);

                    PropertyCategoryPropertySet propertySet = propertySetsMap.get(cat);
                    if (propertySet == null) {
                        propertySet = new PropertyCategoryPropertySet(cat);
                        sets.add(propertySet);
                    }

                    //remove already used
                    for (Declaration d : propertySet.getDeclarations()) {
                        PropertyDefinition def = Properties.getProperty(d.getProperty().getContent().toString());
                        allInCat.remove(def);
                    }

                    //add the rest of unused properties to the property set
                    for (PropertyDefinition pd : allInCat) {
                        Declaration alreadyAdded = addedDeclarations.get(pd);
                        if (alreadyAdded != null) {
                            propertySet.add(alreadyAdded, true);
                        } else {
                            propertySet.add(pd);
                        }
                    }

                }

            }

        } else {
            //not showCategories

            //do not create property sets since the natural ordering if no categorized view
            //is enabled does not work then.


            List<Declaration> filtered = new ArrayList<Declaration>();
            for (Declaration d : declarations) {
                if (addedDeclarations.containsValue(d)) {
                    continue; //skip those added declarations
                }
                //check the declaration
                org.netbeans.modules.css.model.api.Property property = d.getProperty();
                PropertyValue propertyValue = d.getPropertyValue();
                if (property != null && propertyValue != null) {
                    if (matchesFilterText(property.getContent().toString())) {
                        filtered.add(d);
                    }
                }
            }

            if (getSortMode() == SortMode.ALPHABETICAL) {
                Collections.sort(filtered, PropertyUtils.DECLARATIONS_COMPARATOR);
            }

            //just create one top level property set for virtual category (the items actually don't belong to the category)
            PropertyCategoryPropertySet set = new PropertyCategoryPropertySet(PropertyCategory.DEFAULT);
            set.addAll(filtered);

            //overrride the default descriptions
            set.setDisplayName(Bundle.rule_global_set_displayname());
            set.setShortDescription(Bundle.rule_global_set_tooltip());

            sets.add(set);

            if (isShowAllProperties()) {
                //Show all properties
                List<PropertyDefinition> all = new ArrayList<PropertyDefinition>(filterByPrefix(Properties.getProperties(true)));
                Collections.sort(all, PropertyUtils.PROPERTY_DEFINITIONS_COMPARATOR);

                //remove already used
                for (Declaration d : set.getDeclarations()) {
                    PropertyDefinition def = Properties.getProperty(d.getProperty().getContent().toString());
                    all.remove(def);
                }

                //add the rest of unused properties to the property set
                for (PropertyDefinition pd : all) {
                    Declaration alreadyAdded = addedDeclarations.get(pd);
                    if (alreadyAdded != null) {
                        set.add(alreadyAdded, true);
                    } else {
                        set.add(pd);
                    }
                }

            }
        }



        return sets.toArray(new PropertyCategoryPropertySet[0]);
    }
    
    /**
     * Returns an unique id of the property within current rule.
     * 
     * Format of the ID:
     * 
     * property name_S_D
     * 
     * Where:
     * "S" is the property index within the rule
     * "D" is the number of the property if there are more properties of same name
     * 
     * Example:
     * 
     * div {
     *    color: red;     // color_0
     *    font: courier;  // font_1
     *    color: green;   // color_2_1
     * }
     * 
     * @param property
     */
    private String getDeclarationId(Rule rule, Declaration declaration) {
        assert rule.getModel() == declaration.getModel();
        
        CharSequence searched = declaration.getProperty().getContent();
        Declarations ds = rule.getDeclarations();
        Collection<Declaration> declarations = ds != null ? ds.getDeclarations() : Collections.<Declaration>emptyList();
        
        int identityIndex = -1; 
        int index = -1;
        for(Declaration d : declarations ) {
            index++;
            CharSequence propName = d.getProperty().getContent();
            if(LexerUtils.equals(searched, propName, false, false)) {
                identityIndex++;
            }
            if(d == declaration) {
                break;
            }
        }
        assert identityIndex >= 0;
        StringBuilder b = new StringBuilder();
        b.append(searched);
        b.append('_');
        b.append(index);
        if(identityIndex > 0) {
            b.append('_');
            b.append(identityIndex);
        }
        return b.toString();
    }

    private String getPropertyDisplayName(Declaration declaration) {
        return declaration.getProperty().getContent().toString();
    }
    
    public void applyModelChanges() {
        final Model model = getModel();
        model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
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

    class PropertyCategoryPropertySet extends PropertySet {

        private List<Property> properties = new ArrayList<Property>();
        private Map<Declaration, DeclarationProperty> declaration2PropertyMap = new HashMap<Declaration, DeclarationProperty>();

        public PropertyCategoryPropertySet(PropertyCategory propertyCategory) {
            super(propertyCategory.name(), //NOI18N
                    propertyCategory.getDisplayName(),
                    propertyCategory.getShortDescription());
        }

        public void add(Declaration declaration, boolean markAsModified) {
            DeclarationProperty property = createDeclarationProperty(declaration, markAsModified);
            declaration2PropertyMap.put(declaration, property);
            properties.add(property);
        }

        public void addAll(Collection<Declaration> declarations) {
            for (Declaration d : declarations) {
                add(d, false);
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

    private Property createPropertyDefinitionProperty(PropertyDefinition definition) {
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

        if (!fixedElements.isEmpty()) {
            return new PropertyValuesEditor(pmodel, getModel(), fixedElements, unitElements, addNoneProperty);
        }

        return null;
    }

    private abstract class AbstractPDP<T> extends PropertySupport<T> {

        private PropertyDefinition def;
        private PropertyEditor editor;

        public AbstractPDP(PropertyDefinition def, PropertyEditor editor, String name, Class<T> type, String displayName, String shortDescription, boolean canR, boolean canW) {
            super(name, type, displayName, shortDescription, canR, canW);
            this.def = def;
            this.editor = editor;
        }

        public AbstractPDP(PropertyDefinition def, PropertyEditor editor, Class<T> clazz, String shortDescription) {
            super(def.getName(),
                    clazz,
                    def.getName(),
                    shortDescription,
                    true,
                    getRule().isValid());
            this.def = def;
            this.editor = editor;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return getEmptyValue();
        }

        @Override
        public void setValue(T val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (getEmptyValue().equals(val)) {
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
            Expression expr = factory.createExpression(convertToString(val));
            PropertyValue value = factory.createPropertyValue(expr);
            Declaration newDeclaration = factory.createDeclaration(property, value, false);

            declarations.addDeclaration(newDeclaration);

            //save the model to the source
            if (!isAddPropertyMode()) {
                applyModelChanges();
            } else {
                //add property mode - just refresh the content
                addedDeclarations.put(def, newDeclaration); //remember what we've added during this dialog cycle
                fireContextChanged(true);
            }
        }

        protected abstract String convertToString(T val);

        protected abstract T getEmptyValue();
    }
    
    private static String EMPTY = "";

    private class PlainPDP extends AbstractPDP<String> {

        public PlainPDP(PropertyDefinition def, PropertyEditor editor, String shortDescription) {
            super(def, editor, String.class, shortDescription);
        }

        @Override
        protected String convertToString(String val) {
            return val;
        }

        @Override
        protected String getEmptyValue() {
            return EMPTY;
        }
    }

    private class PropertyDefinitionProperty extends PlainPDP {

        public PropertyDefinitionProperty(PropertyDefinition def, PropertyEditor editor) {
            super(def,
                    editor,
                    Bundle.rule_properties_add_declaration_tooltip());
        }
    }

    private DeclarationProperty createDeclarationProperty(Declaration declaration, boolean markAsModified) {
        ResolvedProperty resolvedProperty = declaration.getResolvedProperty();
        return new DeclarationProperty(declaration, 
                getDeclarationId(getRule(), declaration), 
                getPropertyDisplayName(declaration), 
                markAsModified, 
                resolvedProperty == null ? null : createPropertyValueEditor(resolvedProperty.getPropertyModel(), 
                true));
    }

    @NbBundle.Messages({
        "property.value.unexpected.token=Erroneous property value, unexpected token {0} found",
        "property.value.not.resolved=Erroneous property value",
        "property.unknown=No such property",
        "property.description=Property {0} ({1} Module)"
    })
    public class DeclarationProperty extends PropertySupport {

        private final String propertyName;
        private final PropertyEditor editor;
        private final boolean markAsModified;
        
        private Declaration declaration;
        private DeclarationInfo info;
        private String shortDescription;
        
        private String valueSet;

        public DeclarationProperty(Declaration declaration, String propertyName, String propertyDisplayName, boolean markAsModified, PropertyEditor editor) {
            super(propertyName,
                    String.class,
                    propertyDisplayName,
                    null, true, getRule().isValid() && !isAddPropertyMode());
            this.propertyName = propertyName;
            this.declaration = declaration;
            this.markAsModified = markAsModified;
            this.editor = editor;

            checkForErrors();

            //one may set a custom inplace editor by 
            //setValue("inplaceEditor", new MyInplaceEditor());
            
        }

        public Declaration getDeclaration() {
            return declaration;
        }
        
        /**
         * Updates the {@link #info} field to {@link DeclarationInfo#ERRONEOUS} if
         * the active declaration contains errors.
         */
        private void checkForErrors() {
            //suppress the errors for just added property
            //it doesn't have the value yet, but this doesn't mean
            //we want to mark it as erroneous while adding the value
            if(getDeclaration().equals(panel.createdDeclaration)) {
                return ;
            }
            
            String property = declaration.getProperty().getContent().toString();
            PropertyModel model = Properties.getPropertyModel(property);
            if(model != null) {
                PropertyValue value = declaration.getPropertyValue();
                if(value != null) {
                    CharSequence content = value.getExpression().getContent();
                    ResolvedProperty rp = new ResolvedProperty(model, content);
                    if(!rp.isResolved()) {
                        info = DeclarationInfo.ERRONEOUS;
                        List<Token> unresolvedTokens = rp.getUnresolvedTokens();
                        if(!unresolvedTokens.isEmpty()) {
                            Token unexpectedToken = unresolvedTokens.iterator().next();
                            shortDescription = Bundle.property_value_unexpected_token(unexpectedToken.toString());
                        } else {
                            shortDescription = Bundle.property_value_not_resolved();
                        }
                        return ;
                    }
                }
                shortDescription = Bundle.property_description(property, model.getProperty().getCssModule().getDisplayName());
            } else {
                //flag as unknown
                info = DeclarationInfo.ERRONEOUS;
                shortDescription = Bundle.property_unknown();
            }
        }
        
        private void updateDeclaration(Declaration declaration) {
            assert getDeclarationId(getRule(), declaration).equals(propertyName);
            
            //update the declaration
            String oldValue = getValue();
            this.declaration = declaration;
            String newValue = getValue();

            /* Reset DeclarationInfo to default state (null) as the contract 
             * doesn't require/expect the RuleEditorController.setDeclarationInfo(...) 
             * to be called for each "plain" declaration with null DeclarationInfo argument.
            */
            DeclarationInfo oldInfo = info;
            info = null;
            
            String oldShortDescription = shortDescription;
            
            //possibly set the DeclarationInfo to ERRONEOUS
            checkForErrors();
            
            if(!shortDescription.equals(oldShortDescription)) {
                fireShortDescriptionChange(oldShortDescription, shortDescription);
            }
            
            //now we need to fire property name property change with some 
            //change so call setDeclarationInfo() which does property change
            //from null to current value and hence forces the PS to repaint 
            //the property
            if(info != oldInfo) {
                //DeclarationInfo has changed
                setDeclarationInfo(info);
            } else {
                //no change to DeclarationInfo 
                setDisplayName(getHtmlDisplayName());
                //and fire property change to the node 
                //this will trigger the property name and value repaint
                firePropertyChange(propertyName, oldValue, newValue);
            }
            
        }

        @Override
        public String getShortDescription() {
            return shortDescription;
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        public void setDeclarationInfo(DeclarationInfo info) {
            this.info = info;
            setDisplayName(getHtmlDisplayName());
            //force the property repaint - stupid way but there's
            //doesn't seem to be any better way
            firePropertyChange(propertyName, null, getValue());
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
                if (isAddPropertyMode() && !markAsModified) {
                    color = COLOR_CODE_GRAY;
                } else {
                    bold = true;
                }
            }
            if (isOverridden()) {
                strike = true;
            }
            if (isInactive()) {
                color = COLOR_CODE_GRAY;
                strike = true;
            }
            if (isErroneous()) {
                strike = true;
                color = COLOR_CODE_RED;
            }

            //render
            if (bold) {
                b.append("<b>");//NOI18N
            }
            if (strike) {
                b.append("<s>"); //use <del>?
            }
            if (color != null) {
                b.append("<font color="); //NOI18N
                b.append(color);
                b.append(">"); //NOI18N
            }

            b.append(getPropertyDisplayName(declaration));

            if (color != null) {
                b.append("</font>"); //NOI18N
            }
            if (strike) {
                b.append("</s>"); //use <del>?
            }
            if (bold) {
                b.append("</b>");//NOI18N
            }

            return b.toString();
        }

        @Override
        public String getValue() {
            if(valueSet != null) {
                return valueSet;
            }
            PropertyValue val = declaration.getPropertyValue();
            return val == null ? null : val.getExpression().getContent().toString().trim();
        }
        

        @Override
        public void setValue(final Object o) {
            assert SwingUtilities.isEventDispatchThread();
            
            final String asString = (String) o;
            if (asString == null || asString.isEmpty()) {
                return;
            }
            String currentValue = getValue();
            if (asString.equals(currentValue)) {
                //same value, ignore
                return;
            }

            this.valueSet = asString;
            SAVE_CHANGE_TASK.schedule(200);
            
        }
        
        private Task SAVE_CHANGE_TASK = RuleEditorPanel.RP.create(new Runnable() {
            @Override
            public void run() {
                Mutex.EVENT.readAccess(new Runnable() {

                    @Override
                    public void run() {
                        //all the access to valueSet field is safe as 
                        //the field is only set in setValue() which always
                        //runs id EDT
                        
                        //The tasks may schedule in such way that more than one tasks
                        //runs after the setValue(...) method called.
                        //In such case the first task sets the valueSet field to null
                        //and the other tasks cannot rule (they do not have anything
                        //to do anyway) so just quit in such case.
                        if(valueSet == null) {
                            return ;
                        }

                        Model model = getModel();
                        model.runWriteTask(new Model.ModelTask() {
                            @Override
                            public void run(StyleSheet styleSheet) {
                                if (NONE_PROPERTY_NAME.equals(valueSet)) {
                                    //remove the whole declaration
                                    Declarations declarations = (Declarations) declaration.getParent();
                                    declarations.removeDeclaration(declaration);
                                } else {
                                    //update the value
                                    RuleEditorPanel.LOG.log(Level.FINE, "updating property to {0}", valueSet);
                                    declaration.getPropertyValue().getExpression().setContent(valueSet);
                                }
                            }
                        });

                        if (!isAddPropertyMode()) {
                            //save changes
                            applyModelChanges();

                            //the model save request will cause the source model's 
                            //Model.CHANGES_APPLIED_TO_DOCUMENT property change event fired
                            //and the RuleEditorPanel's listener will SYNCHRONOUSLY
                            //refresh the css source model.
                            //
                            //...so now we have a new instance of model reflecting
                            //the changes made by the writetask above
                        }

                        valueSet = null; 
                    }
                    
                });

            }
        });
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
