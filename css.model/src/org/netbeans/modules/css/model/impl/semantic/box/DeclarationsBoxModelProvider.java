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
package org.netbeans.modules.css.model.impl.semantic.box;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.semantic.PModel;
import org.netbeans.modules.css.model.api.semantic.box.Box;
import org.netbeans.modules.css.model.api.semantic.box.BoxElement;
import org.netbeans.modules.css.model.api.semantic.box.BoxType;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.box.EditableBox;
import org.netbeans.modules.css.model.impl.semantic.ModelBuilderNodeVisitor;
import org.netbeans.modules.css.model.impl.semantic.NodeModel;
import org.netbeans.modules.css.model.impl.semantic.PropertyModelId;

/**
 *
 * @author marekfukala
 */
public class DeclarationsBoxModelProvider implements EditableBoxProvider {

    private Model model;
    private Declarations declarations;

    public DeclarationsBoxModelProvider(Model model, Declarations element) {
        this.model = model;
        this.declarations = element;
    }

    @Override
    public EditableBox getBox(BoxType boxType) {
        Collection<Declaration> involved = new ArrayList<Declaration>();
        BoxElement[] boxe = new BoxElement[4];

        //adjust the boxe edges according to box models obtained from each declaration
        for (Declaration declaration : declarations.getDeclarations()) {
            ModelBuilderNodeVisitor modelvisitor = new ModelBuilderNodeVisitor(PropertyModelId.BOX);
            
            ResolvedProperty resolvedProperty = declaration.getResolvedProperty();
            if(resolvedProperty == null || !resolvedProperty.isResolved()) {
                //some invalid/erroneous declarations
                continue;
            }
            
            resolvedProperty.getParseTree().accept(modelvisitor);

            //get all models which has anything to do with the box model
            Collection<BoxProvider> providers = modelvisitor.getModels(BoxProvider.class);

            for (BoxProvider p : providers) {
                //XXX fix this - the box itself must provide the isValid() info, 
                //not the underlying classes!
                if(p instanceof NodeModel) {
                    NodeModel nmodel = (NodeModel)p;
                    if(!nmodel.isValid()) {
                        continue;
                    }
                }
                
                Box box = p.getBox(boxType);
                if (box != null) {
                    for (Edge e : Edge.values()) {
                        BoxElement boxElement = box.getEdge(e);
                        if (boxElement != null) {
                            //including BoxElement.EMPTY - which erases the value
                            boxe[e.ordinal()] = boxElement;
                        }
                    }
                    involved.add(declaration);
                }
            }
        }

        switch (boxType) {
            case MARGIN:
                return new DeclarationsMarginModel(model, declarations, involved, new BI(boxe));
            case PADDING:
                return new DeclarationsPaddingModel(model, declarations, involved, new BI(boxe));
            case BORDER_COLOR:
                return new DeclarationsBorderColorModel(model, declarations, involved, new BI(boxe));
            case BORDER_STYLE:
                return new DeclarationsBorderStyleModel(model, declarations, involved, new BI(boxe));
            case BORDER_WIDTH:
                return new DeclarationsBorderWidthModel(model, declarations, involved, new BI(boxe));

            default:
                //fallback, I must implement EBI for border-*
                return new EBI(boxe, boxType);
        }

    }

    private static class BI implements Box {

        private BoxElement[] elements;

        public BI(BoxElement[] elements) {
            this.elements = elements;
        }

        @Override
        public BoxElement getEdge(Edge edge) {
            return elements[edge.ordinal()];
        }
    }

    private static class EBI implements EditableBox, PModel {

        private BoxElement[] elements;
        private BoxType type;

        public EBI(BoxElement[] elements, BoxType type) {
            this.elements = elements;
            this.type = type;
        }

        @Override
        public void setEdge(Edge edge, BoxElement value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BoxElement getEdge(Edge edge) {
            return elements[edge.ordinal()];
        }

        @Override
        public String getName() {
            return type.name();
        }

        @Override
        public String getDisplayName() {
            return type.getDisplayName();
        }

        @Override
        public String getDescription() {
            return String.format("%s Box Model", getDisplayName());
        }

        @Override
        public String getCategoryName() {
            return "Box";
        }

        @Override
        public BoxElement createElement(CharSequence text) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
