/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.mappercore.expressionbuilder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphListener;

/**
 *
 * @author anjeleevich
 */
class CanvasView extends JPanel {
    private ExpressionBuilder expressionBuilder;

    private ExpressionBuilderModel expressionBuilderModel;
    private Graph graph;

    private JScrollPane scrollPane;

    private final Listeners listeners = new Listeners();

    public CanvasView(ExpressionBuilder expressionBuilder) {
        this.expressionBuilder = expressionBuilder;
        this.expressionBuilder.addPropertyChangeListener(ExpressionBuilder
                .MODEL_PROPERTY, listeners);

        scrollPane = new JScrollPane(this);
        scrollPane.setBorder(null);

        updateExpressionBuilderModel();
    }

    public ExpressionBuilder getExpressionBuilder() {
        return expressionBuilder;
    }

    public ExpressionBuilderModel getExpressionBuilderModel() {
        return expressionBuilderModel;
    }

    public JComponent getView() {
        return scrollPane;
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
}
