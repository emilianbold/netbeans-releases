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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author anjeleevich
 */
class TreeView extends JTree implements ExpressionBuilderView {

    private final ExpressionBuilder expressionBuilder;
    private ExpressionBuilderModel expressionBuilderModel;

    private JScrollPane scrollPane;
    private LeftPanel leftPanel;

    private final Listeners listeners = new Listeners();

    public TreeView(ExpressionBuilder expressionBuilder) {
        this.expressionBuilder = expressionBuilder;
        this.expressionBuilder.addPropertyChangeListener(ExpressionBuilder
                .MODEL_PROPERTY, listeners);

        scrollPane = new JScrollPane(this);
        scrollPane.setBorder(null);
        
        leftPanel = new LeftPanel(this);

        updateExpressionBuilderModel();
        updateTreeModel();
    }

    public ExpressionBuilder getExpressionBuilder() {
        return expressionBuilder;
    }

    public ExpressionBuilderModel getExpressionBuilderModel() {
        return expressionBuilderModel;
    }

    public JComponent getViewComponent() {
        return leftPanel;
    }

    private void updateExpressionBuilderModel() {
        ExpressionBuilderModel oldModel = this.expressionBuilderModel;
        ExpressionBuilderModel newModel = expressionBuilder.getModel();

        if (oldModel != newModel) {
            this.expressionBuilderModel = newModel;
            
            if (oldModel != null) {
                oldModel.removePropertyChangeListener(ExpressionBuilderModel
                        .TREE_MODEL_PROPERTY, listeners);
            }

            if (newModel != null) {
                newModel.addPropertyChangeListener(ExpressionBuilderModel
                        .TREE_MODEL_PROPERTY, listeners);
            }
            updateTreeModel();
        }
    }

    private void updateTreeModel() {
        TreeModel newTreeModel = (expressionBuilderModel == null) ? null
                : expressionBuilderModel.getTreeModel();
        if (newTreeModel == null) newTreeModel = EMPTY_TREE_MODEL;
        setModel(newTreeModel);
    }

    private class Listeners implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            final Object source = evt.getSource();
            final String name = evt.getPropertyName();

            if (source == expressionBuilder) {
                if (ExpressionBuilder.MODEL_PROPERTY.equals(name)) {
                    updateExpressionBuilderModel();
                }
            } else if (source == expressionBuilderModel) {
                if (ExpressionBuilderModel.TREE_MODEL_PROPERTY.equals(name)) {
                    updateTreeModel();
                }
            }
        }
    }

    private static final TreeModel EMPTY_TREE_MODEL
            = new DefaultTreeModel(null);
}
