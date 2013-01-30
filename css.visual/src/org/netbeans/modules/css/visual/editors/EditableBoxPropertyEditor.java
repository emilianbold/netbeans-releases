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
package org.netbeans.modules.css.visual.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.netbeans.modules.css.model.api.semantic.box.BoxElement;
import org.netbeans.modules.css.model.api.semantic.Edge;
import org.netbeans.modules.css.model.api.semantic.box.EditableBox;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class EditableBoxPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {

    private static final String SEPARATOR = " "; //NOI18N
    private static final String NO_VALUE = "-"; //NOI18N
    private static final String OVERRIDES = "x"; //NOI18N
    EditableBox editableBox;
    EditableBoxModelProperty property;

    public EditableBoxPropertyEditor(EditableBoxModelProperty property) {
        this.property = property;
        editableBox = property.getEditableBox();
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics g, Rectangle rect) {
        Font originalFont = g.getFont();
        int originalFontSize = originalFont.getSize();
        Font derivedFont = originalFont.deriveFont((float)(originalFontSize * 0.75));
        g.setFont(derivedFont);

        int height = (int) rect.getHeight();
        int width = (int) rect.getWidth() / 4;

        int fontHeight = derivedFont.getSize();
        int textY = height - ((height - fontHeight) / 2);
                
        Color color = g.getColor();
        
        for (Edge e : Edge.values()) {
            BoxElement element = editableBox.getEdge(e);

            //box
            g.setColor(Color.WHITE);
            g.fillRect(e.ordinal() * width, 0, width, height);
            
            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(e.ordinal() * width, 0, width, height);

            if (element != null) {
                if (element == BoxElement.EMPTY) {
                    //paint a cross
                    int x = e.ordinal() * width;
                    g.drawLine(x, 0, x + width, height);
                    g.drawLine(x, height, x + width, 0);
                } else {
                    g.setColor(color);
                    g.drawString(element.asText(), e.ordinal() * width + 2, textY);
                }
            }


        }



//        super.paintValue(g, rect);
    }

    @Override
    public Component getCustomEditor() {
        return new EditableBoxCustomEditor(this);
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public String getAsText() {
        StringBuilder b = new StringBuilder();
        for (Edge e : Edge.values()) {
            BoxElement mw = editableBox.getEdge(e);

            if (mw == null) {
                b.append(NO_VALUE);
            } else if (BoxElement.EMPTY == mw) {
                b.append(OVERRIDES);
            } else {
                b.append(mw.asText());
            }
            b.append(SEPARATOR);
        }
        return b.toString();
    }

    @Override
    public void setAsText(String string) throws IllegalArgumentException {
        StringTokenizer st = new StringTokenizer(string, SEPARATOR);
        int defined = 0;
        Map<Edge, String> edges = new EnumMap<Edge, String>(Edge.class);
        String[] values = new String[4];
        while (st.hasMoreTokens()) {
            values[defined++] = st.nextToken();
        }

        if (defined == 0) {
            throw new IllegalArgumentException("Too few arguments");
        }

        if (defined == 1) {
            //T == R == B == L
            edges.put(Edge.TOP, values[0]);
            edges.put(Edge.BOTTOM, values[0]);
            edges.put(Edge.RIGHT, values[0]);
            edges.put(Edge.LEFT, values[0]);
        } else if (defined == 2) {
            //T == B , L == R
            edges.put(Edge.TOP, values[0]);
            edges.put(Edge.BOTTOM, values[0]);
            edges.put(Edge.RIGHT, values[1]);
            edges.put(Edge.LEFT, values[1]);
        } else if (defined == 3) {
            //T == B, L, R
            edges.put(Edge.TOP, values[0]);
            edges.put(Edge.BOTTOM, values[0]);
            edges.put(Edge.RIGHT, values[1]);
            edges.put(Edge.LEFT, values[2]);
        } else {
            //T, B, L, R
            edges.put(Edge.TOP, values[0]);
            edges.put(Edge.RIGHT, values[1]);
            edges.put(Edge.BOTTOM, values[2]);
            edges.put(Edge.LEFT, values[3]);
        }

        for (Edge e : Edge.values()) {
            String token = edges.get(e);
            BoxElement element;
            if (OVERRIDES.equalsIgnoreCase(token)) {
                element = BoxElement.EMPTY;
            } else if (NO_VALUE.equalsIgnoreCase(token)) {
                element = null;
            } else {
                element = editableBox.createElement(token);
                if (element == null) {
                    throw new IllegalArgumentException(String.format("Invalid value %s", token));
                }
            }
            editableBox.setEdge(e, element);
        }

        setPropertyValue();
    }

    void setPropertyValue() {
        try {
            property.setValue(editableBox);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        //xxx workaround I think
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addVetoableChangeListener(new VetoableChangeListener() {

            @Override
            public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
                setPropertyValue();
            }
        });
    }
}
