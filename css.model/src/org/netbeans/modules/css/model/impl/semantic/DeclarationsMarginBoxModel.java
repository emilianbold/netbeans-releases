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
package org.netbeans.modules.css.model.impl.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import org.netbeans.modules.css.lib.api.properties.model.*;
import org.netbeans.modules.css.model.api.ElementFactory;
import org.netbeans.modules.css.model.api.*;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "CTL_MarginDisplayName=Margin", // NOI18N
    "CTL_MarginDescription=Margin Box Model", // NOI18N
    "CTL_MarginCategory=Box" //NOI18N
})
public class DeclarationsMarginBoxModel implements EditableBox<MarginWidth> {

    private static final String MODEL_NAME = "margin"; //NOI18N
    private Declarations declarations;
    private final CascadedBox<MarginWidth> cascadedBox = new CascadedBox<MarginWidth>();
    private final Collection<Declaration> involved = new ArrayList<Declaration>();
    private final SemanticModelListenerSupport LISTENERS = new SemanticModelListenerSupport();

    public DeclarationsMarginBoxModel(Declarations element) {
        this.declarations = element;
        
        updateModel();
    }

    private void updateModel() {
        for (Declaration declaration : declarations.getDeclarations()) {
            ModelBuilderNodeVisitor<MarginWidth> modelvisitor = new ModelBuilderNodeVisitor<MarginWidth>(PropertyModelId.MARGIN);
            declaration.getResolvedProperty().getParseTree().accept(modelvisitor);
            Box<MarginWidth> model = (Box<MarginWidth>) modelvisitor.getModel();
            if (model != null) {
                cascadedBox.addBox(model);
                involved.add(declaration);
            }
        }

        LISTENERS.fireModelChanged();

    }

    @Override
    public void setEdge(Edge edge, MarginWidth value) {
        MarginWidth current = getEdge(edge);
        if (current == null && value == null || current != null && current.equals(value)) {
            return; //no change
        }

        //merge the original Box with the new edge setting
        EnumMap<Edge, MarginWidth> map = new EnumMap<Edge, MarginWidth>(Edge.class);
        map.put(Edge.TOP, getEdge(Edge.TOP));
        map.put(Edge.BOTTOM, getEdge(Edge.BOTTOM));
        map.put(Edge.LEFT, getEdge(Edge.LEFT));
        map.put(Edge.RIGHT, getEdge(Edge.RIGHT));
        map.put(edge, value);

        //remove the existing declarations
        for (Declaration d : involved) {
            declarations.removeDeclaration(d);
        }

        ElementFactory f = Model.getElementFactory();
        Property p = f.createProperty("margin"); //NOI18N

        //TODO remove the hardcoding - make the algorithm generic

        MarginWidth t = map.get(Edge.TOP);
        MarginWidth r = map.get(Edge.RIGHT);
        MarginWidth b = map.get(Edge.BOTTOM);
        MarginWidth l = map.get(Edge.LEFT);

        if (t == null || r == null || b == null || l == null) {
            //use single properties
            for(Edge e : Edge.values()) {
                MarginWidth mw = map.get(e);
                if(mw != null) {
                    CharSequence propVal = mw.getTextRepresentation();
                    Expression expr = f.createExpression(propVal);
                    PropertyValue pv = f.createPropertyValue(expr);
                    
                    String propertyName = String.format("margin-%s", e.name().toLowerCase()); //NOI18N
                    Property prop = f.createProperty(propertyName);
                    Declaration newD = f.createDeclaration(prop, pv, false);
                    
                    declarations.addDeclaration(newD);
                    
                }
            }
            
            

            
            
        } else {

            StringBuilder sb = new StringBuilder();
            //all edges defined use aggregated notation
            if (t.equals(b)) {
                if (l.equals(r)) {
                    if (t.equals(l)) {
                        //TRBL
                        sb.append(t.getTextRepresentation());
                    } else {
                        //TB LT
                        sb.append(t.getTextRepresentation());
                        sb.append(" ");
                        sb.append(l.getTextRepresentation());
                    }
                } else {
                    //TB L R - no such perm.
                    sb.append(t.getTextRepresentation());
                    sb.append(" ");
                    sb.append(r.getTextRepresentation());
                    sb.append(" ");
                    sb.append(b.getTextRepresentation());
                    sb.append(" ");
                    sb.append(l.getTextRepresentation());

                }
            } else if (l.equals(r)) {
                //T LR B
                sb.append(t.getTextRepresentation());
                sb.append(" ");
                sb.append(l.getTextRepresentation());
                sb.append(" ");
                sb.append(b.getTextRepresentation());
            } else {
                //T R B L
                sb.append(t.getTextRepresentation());
                sb.append(" ");
                sb.append(r.getTextRepresentation());
                sb.append(" ");
                sb.append(b.getTextRepresentation());
                sb.append(" ");
                sb.append(l.getTextRepresentation());
            }

            Expression e = f.createExpression(sb);
            PropertyValue pv = f.createPropertyValue(e);

            Declaration newD = f.createDeclaration(p, pv, false);

            declarations.addDeclaration(newD);
        }

        updateModel();
    }

    @Override
    public MarginWidth getEdge(Edge edge) {
        return cascadedBox.getEdge(edge);
    }

    @Override
    public void addListener(SemanticModelListener listener) {
        LISTENERS.add(listener);
    }

    @Override
    public void removeListener(SemanticModelListener listener) {
        LISTENERS.remove(listener);
    }

    @Override
    public String getName() {
        return MODEL_NAME;
    }

    @Override
    public String getDisplayName() {
        return Bundle.CTL_MarginDisplayName();
    }

    @Override
    public String getDescription() {
        return Bundle.CTL_MarginDescription();
    }

    @Override
    public String getCategoryName() {
        return Bundle.CTL_MarginCategory();
    }
}
