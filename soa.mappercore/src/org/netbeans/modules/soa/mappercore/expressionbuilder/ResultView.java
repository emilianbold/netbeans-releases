/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.mappercore.expressionbuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphListener;

/**
 *
 * @author anjeleevich
 */
class ResultView extends JPanel implements ExpressionBuilderView {
    private final ExpressionBuilder expressionBuilder;
    private ExpressionBuilderModel expressionBuilderModel;
    
    private Graph graph;

    private final Listeners listeners = new Listeners();

    public ResultView(ExpressionBuilder expressionBuilder) {
        this.expressionBuilder = expressionBuilder;
        this.expressionBuilder.addPropertyChangeListener(ExpressionBuilder
                .MODEL_PROPERTY, listeners);

        setPreferredSize(new Dimension(48, 48));
        setBorder(BORDER);

        updateExpressionBuilderModel();
        updateGraph();
    }

    public ExpressionBuilder getExpressionBuilder() {
        return expressionBuilder;
    }

    public ExpressionBuilderModel getExpressionBuilderModel() {
        return expressionBuilderModel;
    }

    public JComponent getViewComponent() {
        return this;
    }

    private void updateExpressionBuilderModel() {
        ExpressionBuilderModel oldModel = this.expressionBuilderModel;
        ExpressionBuilderModel newModel = expressionBuilder.getModel();

        if (oldModel != newModel) {
            this.expressionBuilderModel = newModel;
            if (oldModel != null) {
                oldModel.removePropertyChangeListener(ExpressionBuilderModel
                        .GRAPH_PROPERTY, listeners);
            }

            if (newModel != null) {
                newModel.addPropertyChangeListener(ExpressionBuilderModel
                        .GRAPH_PROPERTY, listeners);
            }

            updateGraph();
        }
    }

    private void updateGraph() {
        Graph oldGraph = this.graph;
        Graph newGraph = (expressionBuilderModel == null) ? null : expressionBuilderModel.getGraph();

        if (oldGraph != newGraph) {
            this.graph = newGraph;
            if (oldGraph != null) {
                oldGraph.removeGraphListener(listeners);
            }

            if (newGraph != null) {
                newGraph.addGraphListener(listeners);
            }
        }
    }

    private class Listeners implements PropertyChangeListener, GraphListener {
        public void propertyChange(PropertyChangeEvent evt) {
            final Object source = evt.getSource();
            final String name = evt.getPropertyName();

            if (source == expressionBuilder) {
                if (ExpressionBuilder.MODEL_PROPERTY.equals(name)) {
                    updateExpressionBuilderModel();
                }
            }
        }

        public void graphBoundsChanged(Graph graph) {

        }

        public void graphLinksChanged(Graph graph) {

        }

        public void graphContentChanged(Graph graph) {
            
        }
    }

    private static final Border BORDER = new Border() {
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height)
        {
            int x2 = x + width - 1;
            int y2 = y + height - 1;

            g.setColor(new Color(0xFFFFFF));
            g.drawLine(x + 1, y, x + 1, y2);

            g.setColor(c.getBackground().darker());
            g.drawLine(x, y, x, y2);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 1, 0, 1);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    };
}
