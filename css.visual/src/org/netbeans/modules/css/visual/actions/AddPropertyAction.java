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
package org.netbeans.modules.css.visual.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelUtils;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "label.add.property=Add Property"
})
public class AddPropertyAction extends AbstractAction {

    private RuleEditorPanel panel;

    public AddPropertyAction(RuleEditorPanel panel) {
        super(Bundle.label_add_property());
        this.panel = panel;
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            //use the default rule editor panel with some modifications
            final RuleEditorPanel addPropertyPanel = new RuleEditorPanel(true);

            //create a new model instance and do all the modifications in the dialog
            //in this model. If the user confirms changes the model will be persisted
            //in the corresponding file and RuleEditor will be refreshed based on
            //an event from the parsing task.
            final AtomicReference<Model> model_ref = new AtomicReference<Model>();
            Snapshot snapshot = panel.getModel().getLookup().lookup(Snapshot.class);
            ParserManager.parse(Collections.singleton(snapshot.getSource()), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/css");
                    if (ri != null) {
                        CssCslParserResult result = (CssCslParserResult) ri.getParserResult();
                        model_ref.set(Model.createModel(result.getWrappedCssParserResult()));
                    }
                }
            });
            final Model model = model_ref.get();
            
            final AtomicReference<Rule> rule_ref = new AtomicReference<Rule>();
            //resolve the rule to the new model
            model.runReadTask(new Model.ModelTask() {

                @Override
                public void run(StyleSheet styleSheet) {
                    ModelUtils utils = new ModelUtils(model);
                    rule_ref.set(utils.findMatchingRule(panel.getModel(), panel.getRule()));
                }
            });
            
            Rule rule = rule_ref.get();
            
            addPropertyPanel.setModel(model);
            addPropertyPanel.setRule(rule);
            
            addPropertyPanel.setShowAllProperties(true);
            addPropertyPanel.setShowCategories(true);

            addPropertyPanel.updateFiltersPresenters();

            Dialog dialog = DialogDisplayer.getDefault().createDialog(
                    new DialogDescriptor(addPropertyPanel, Bundle.label_add_property(), true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("OK".equals(e.getActionCommand())) {
                        addPropertyPanel.node.applyModelChanges();
                    }
                }
            }));

            dialog.setVisible(true);
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
