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

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author anjeleevich
 */
public class ExpressionBuilder extends JComponent {
    private ExpressionBuilderModel model = null;

    private JButton closeButton;
    private JToolBar toolBar;

    private TitleView titleView;
    private TreeView treeView;
    private CanvasView canvasView;
    private ResultView resultView;
    private SourcesView sourcesView;

    private JPanel centerPanel;

    public ExpressionBuilder(ExpressionBuilderModel model) {
        titleView = new TitleView(this);
        treeView = new TreeView(this);
        canvasView = new CanvasView(this);
        resultView = new ResultView(this);
        sourcesView = new SourcesView(this);

        closeButton = new JButton();
        closeButton.setVisible(false);

        toolBar = new JToolBar();
        toolBar.setBorder(new EmptyBorder(2, 4, 2, 4));
        toolBar.setFloatable(false);
        toolBar.setOpaque(false);
        toolBar.add(titleView.getViewComponent());
        toolBar.add(Box.createHorizontalGlue()).setFocusable(false);
        toolBar.add(closeButton);

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(canvasView.getView(), BorderLayout.CENTER);
        centerPanel.add(treeView.getViewComponent(), BorderLayout.WEST);
        centerPanel.add(resultView.getViewComponent(), BorderLayout.EAST);
        centerPanel.add(sourcesView.getViewComponent(), BorderLayout.SOUTH);

        setLayout(new BorderLayout());

        add(toolBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        setModel(model);
    }

    public void addCloseActionListener(ActionListener actionListener) {
        closeButton.addActionListener(actionListener);
        closeButton.setVisible(true);
    }

    public void removeCloseActionListener(ActionListener actionListener) {
        closeButton.removeActionListener(actionListener);
        ActionListener[] listeners = closeButton.getActionListeners();
        closeButton.setVisible(listeners != null && listeners.length > 0);
    }
    
    public ExpressionBuilderModel getModel() {
        return model;
    }

    public void setModel(ExpressionBuilderModel model) {
        ExpressionBuilderModel oldModel = getModel();
        ExpressionBuilderModel newModel = getModel();

        if (oldModel != newModel) {
            this.model = newModel;
            firePropertyChange(MODEL_PROPERTY, oldModel, newModel);
        }
    }

    TreeView getTreeView() {
        return treeView;
    }

    ResultView getResultView() {
        return resultView;
    }

    CanvasView getCanvasView() {
        return canvasView;
    }

    public static final String CLOSE_ACTION_COMMAND 
            = "CloseExpressionBuilder";

    public static final String MODEL_PROPERTY
            = "ExpressionBuilderModelProperty";
}
