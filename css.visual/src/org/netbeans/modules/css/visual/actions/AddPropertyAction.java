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
import javax.swing.AbstractAction;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
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
        //use the default rule editor panel with some modifications
        final RuleEditorPanel addPropertyPanel = new RuleEditorPanel(true);
        addPropertyPanel.setModel(panel.getModel());
        addPropertyPanel.setRule(panel.getRule());
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
    }

    
}
