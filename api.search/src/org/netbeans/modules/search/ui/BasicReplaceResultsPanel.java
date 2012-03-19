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
package org.netbeans.modules.search.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.netbeans.modules.search.BasicComposition;
import org.netbeans.modules.search.ContextView;
import org.netbeans.modules.search.Manager;
import org.netbeans.modules.search.ReplaceTask;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.ResultView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
public class BasicReplaceResultsPanel extends BasicAbstractResultsPanel {

    private JButton replaceButton;

    public BasicReplaceResultsPanel(ResultModel resultModel,
            BasicComposition composition, List<FileObject> rootFiles,
            Node infoNode) {
        super(resultModel, composition, true, rootFiles,
                new ResultsOutlineSupport(true, true, resultModel, rootFiles,
                infoNode));
        init();
    }

    private void init() {
        JPanel leftPanel = new JPanel();
        replaceButton = new JButton();
        replaceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replace();
            }
        });
        setButtonText();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 1));
        buttonPanel.add(replaceButton);
        replaceButton.setMaximumSize(replaceButton.getPreferredSize());
        leftPanel.add(resultsOutlineSupport.getOutlineView());
        leftPanel.add(buttonPanel);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(new ContextView(resultModel,
                getExplorerManager()));

        getContentPanel().add(splitPane);
        initResultModelListener();
    }

    private void replace() {
        ReplaceTask taskReplace =
                new ReplaceTask(resultModel.getMatchingObjects());
        replaceButton.setEnabled(false);
        Manager.getInstance().scheduleReplaceTask(taskReplace);
    }

    private void initResultModelListener() {
        resultModel.addPropertyChangeListener(new ModelListener());
    }

    private class ModelListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ResultModel.PROP_VALID)) {
                replaceButton.setText(NbBundle.getMessage(ResultView.class,
                        "TEXT_BUTTON_REPLACE_INVALID"));                //NOI18N
                replaceButton.setEnabled(false);
            } else if (evt.getPropertyName().equals(ResultModel.PROP_SELECTION)
                    && resultModel.isValid()) {
                setButtonText();
            }
        }
    }

    private void setButtonText() {
        int matches = resultModel.getSelectedMatchesCount();
        replaceButton.setText(NbBundle.getMessage(ResultView.class,
                "TEXT_BUTTON_REPLACE", matches));                       //NOI18N
        replaceButton.setEnabled(matches > 0);
    }
}
