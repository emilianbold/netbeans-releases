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
import java.util.EnumMap;
import org.netbeans.modules.css.model.api.semantic.box.Box;
import org.netbeans.modules.css.model.api.semantic.box.BoxElement;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.box.EditableBox;
import org.netbeans.modules.css.model.api.*;

/**
 *
 * @author marekfukala
 */
public abstract class DeclarationsBoxModelBase implements EditableBox {

    private final Model model;
    private final Declarations declarations;
    private Collection<Declaration> involved;
    private Box box;
    
    public DeclarationsBoxModelBase(Model model, 
            Declarations element, 
            Collection<Declaration> involved, 
            Box box) {
        this.model = model;
        this.declarations = element;
        this.involved = involved;
        this.box = box;
    }

    //all edges property like margin
    protected abstract String getPropertyName();

    //for single edge properties like margin-left
    protected abstract String getPropertyName(Edge edge);
    
    @Override
    public BoxElement getEdge(Edge edge) {
        return box.getEdge(edge);
    }
    
    @Override
    public void setEdge(Edge edge, BoxElement value) {
        BoxElement current = getEdge(edge);
        if (current == null && value == null || current != null && current.equals(value)) {
            return; //no change
        }

        //merge the original Box with the new edge setting
        EnumMap<Edge, BoxElement> map = new EnumMap<Edge, BoxElement>(Edge.class);
        map.put(Edge.TOP, getEdge(Edge.TOP));
        map.put(Edge.BOTTOM, getEdge(Edge.BOTTOM));
        map.put(Edge.LEFT, getEdge(Edge.LEFT));
        map.put(Edge.RIGHT, getEdge(Edge.RIGHT));
        map.put(edge, value);

        //remove the existing declarations
        for (Declaration d : involved) {
            declarations.removeDeclaration(d);
        }
        
        //reinitialize the involved elements
        involved = new ArrayList<Declaration>();

        ElementFactory f = model.getElementFactory();
        Property p = f.createProperty(getPropertyName()); //NOI18N

        //TODO remove the hardcoding - make the algorithm generic

        BoxElement t = map.get(Edge.TOP);
        BoxElement r = map.get(Edge.RIGHT);
        BoxElement b = map.get(Edge.BOTTOM);
        BoxElement l = map.get(Edge.LEFT);

        //reinitialize the box
        box = new BoxWithDifferentEdges(t, r, b, l);
        
        if (t == null || r == null || b == null || l == null) {
            //use single properties
            for (Edge e : Edge.values()) {
                BoxElement mw = map.get(e);
                if (mw != null) {
                    CharSequence propVal = mw.asText();
                    Expression expr = f.createExpression(propVal);
                    PropertyValue pv = f.createPropertyValue(expr);

                    String propertyName = getPropertyName(e);
                    Property prop = f.createProperty(propertyName);
                    Declaration newD = f.createDeclaration(prop, pv, false);

                    declarations.addDeclaration(newD);
                    involved.add(newD);

                }
            }

        } else {

            StringBuilder sb = new StringBuilder();
            //all edges defined use aggregated notation
            if (t.equals(b)) {
                if (l.equals(r)) {
                    if (t.equals(l)) {
                        //TRBL
                        sb.append(t.asText());
                    } else {
                        //TB LT
                        sb.append(t.asText());
                        sb.append(" ");
                        sb.append(l.asText());
                    }
                } else {
                    //TB L R - no such perm.
                    sb.append(t.asText());
                    sb.append(" ");
                    sb.append(r.asText());
                    sb.append(" ");
                    sb.append(b.asText());
                    sb.append(" ");
                    sb.append(l.asText());

                }
            } else if (l.equals(r)) {
                //T LR B
                sb.append(t.asText());
                sb.append(" ");
                sb.append(l.asText());
                sb.append(" ");
                sb.append(b.asText());
            } else {
                //T R B L
                sb.append(t.asText());
                sb.append(" ");
                sb.append(r.asText());
                sb.append(" ");
                sb.append(b.asText());
                sb.append(" ");
                sb.append(l.asText());
            }

            Expression e = f.createExpression(sb);
            PropertyValue pv = f.createPropertyValue(e);

            Declaration newD = f.createDeclaration(p, pv, false);

            declarations.addDeclaration(newD);
            involved.add(newD);
        }

    }

}
