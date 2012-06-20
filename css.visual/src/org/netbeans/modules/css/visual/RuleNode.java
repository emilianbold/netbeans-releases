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
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.css.model.api.semantic.SemanticModel;
import org.netbeans.modules.css.model.api.*;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a CSS rule with no children. The node properties
 * represents the css rule properties.
 *
 * @author marekfukala
 */
public class RuleNode extends AbstractNode {

    private PropertySet[] propertySets;
    private Model model;
    private Rule rule;

    public RuleNode(Model model, Rule rule) {
        super(new RuleChildren(), Lookups.fixed(rule));
        this.model = model;
        this.rule = rule;
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
        //rule properties property set
        sets.add(new RulePropertySet(rule));

        //semantic models property sets
        Declarations declarations = rule.getDeclarations();
        if (declarations != null) {
            Map<String, Collection<SemanticModel>> category2models = new HashMap<String, Collection<SemanticModel>>();
            Collection<? extends SemanticModel> models = declarations.getSemanticModels();
            //create a property set for each model catogory and put all models to them
            for (SemanticModel semanticModel : models) {
                String categoryName = semanticModel.getCategoryName();
                Collection<SemanticModel> subCol = category2models.get(categoryName);
                if (subCol == null) {
                    subCol = new ArrayList<SemanticModel>();
                    category2models.put(categoryName, subCol);
                }
                subCol.add(semanticModel);
            }

            for (String categoryName : category2models.keySet()) {
                sets.add(new SemanticModelCategoryPropertySet(categoryName, category2models.get(categoryName)));
            }

        }

        return sets.toArray(new PropertySet[0]);
    }

    public void applyModelChanges() {
            final NbEditorDocument doc = (NbEditorDocument)model.getLookup().lookup(Document.class);
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
    
    private class SemanticModelCategoryPropertySet extends PropertySet {

        private Property<?>[] properties;

        public SemanticModelCategoryPropertySet(String categoryName, Collection<SemanticModel> models) {
            super(categoryName, categoryName, null);

            Collection<Property> props = new ArrayList<Property>(models.size());

            for (SemanticModel semanticModel : models) {
                Property prop = SemanticModelPERegistry.getProperty(RuleNode.this, semanticModel);
                if (prop != null) {
                    props.add(prop);
                }
            }

            properties = props.toArray(new Property[]{});
        }

        @Override
        public Property<?>[] getProperties() {
            return properties;
        }
    }

    private class RulePropertySet extends PropertySet {

        private Property<?>[] properties;

        public RulePropertySet(Rule rule) {
            super("properties", "Properties", "Properties of the selected css rule");

            if (rule.getDeclarations() == null) {
                //empty rule
                properties = new Property[0];
                return;
            }

            Collection<Declaration> declarations = rule.getDeclarations().getDeclarations();
            Collection<Property> props = new ArrayList<Property>(declarations.size());
            for (Declaration d : declarations) {
                //check the declaration
                if (d.getProperty() != null && d.getPropertyValue() != null) {
                    props.add(new SingleValueProperty(d));
                }
            }
            properties = props.toArray(new Property[]{});
        }

        @Override
        public Property<?>[] getProperties() {
            return properties;
        }
    }

    private class SingleValueProperty extends PropertySupport.ReadWrite<String> {

        private String value;
        private Declaration declaration;

        public SingleValueProperty(Declaration declaration) {
            super(declaration.getProperty().getContent().toString(),
                    String.class,
                    declaration.getProperty().getContent().toString(),
                    null);

            this.declaration = declaration;
            this.value = declaration.getPropertyValue().getExpression().getContent().toString();
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return value;
        }

        @Override
        public boolean canWrite() {
            return true;
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.value = val;

            declaration.getPropertyValue().getExpression().setContent(value);
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
