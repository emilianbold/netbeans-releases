/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.view.join.JoinMainPanel;
import org.netbeans.modules.sql.framework.ui.view.join.ListTransferPanel;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * @author radval
 */
public class ETLCollaborationWizardJoinPanel extends JPanel implements WizardDescriptor.Panel {

    private ETLCollaborationWizard etlCWizard;

    // JoinMainPanel
    private JoinMainPanel joinMainPanel;

    /* Set <ChangeListeners> */
    private final Set listeners = new HashSet(1);

    /** Creates a new instance of ETLCollaborationWizardJoinPanel */
    public ETLCollaborationWizardJoinPanel(ETLCollaborationWizard owner, String title, IGraphView view) {
        this.etlCWizard = owner;
        if (title != null && title.trim().length() != 0) {
            setName(title);
        }

        this.setLayout(new BorderLayout());

        joinMainPanel = new JoinMainPanel(view, false);
        joinMainPanel.reset(view);

        this.add(joinMainPanel, BorderLayout.CENTER);
        this.joinMainPanel.setDividerLocation(210);
        joinMainPanel.setPreviewModifiable(true);
    }

    /**
     * Add a listener to changes of the panel's validity.
     * 
     * @param l the listener to add
     * @see #isValid
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * Get the component displayed in this panel.
     * 
     * @return the component
     */
    public java.awt.Component getComponent() {
        return this;
    }

    /**
     * Help for this panel. When the panel is active, this is used as the help for the
     * wizard dialog.
     * 
     * @return the help or <code>null</code> if no help is supplied
     */
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Test whether the panel is finished and it is safe to proceed to the next one. If
     * the panel is valid, the "Next" (or "Finish") button will be enabled.
     * <p>
     * <strong>Tip: </strong> if your panel is actually the component itself (so
     * {@link #getComponent}returns <code>this</code>), be sure to specifically
     * override this method, as the unrelated implementation in
     * {@link java.awt.Component#isValid}if not overridden could cause your wizard to
     * behave erratically.
     * 
     * @return <code>true</code> if the user has entered satisfactory information
     */
    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * Provides the wizard panel with the current data--either the default data or
     * already-modified settings, if the user used the previous and/or next buttons. This
     * method can be called multiple times on one instance of
     * <code>WizardDescriptor.Panel</code>.
     * <p>
     * The settings object is originally supplied to
     * {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}. In
     * the case of a <code>TemplateWizard.Iterator</code> panel, the object is in fact
     * the <code>TemplateWizard</code>.
     * 
     * @param settings the object representing wizard panel state
     * @exception IllegalStateException if the the data provided by the wizard are not
     *            valid.
     */
    public void readSettings(Object settings) {
        this.joinMainPanel.setDividerLocation(210);
        Set sTables = new HashSet();
        List sourceDb = etlCWizard.getSelectedSourceDb();
        Iterator it = sourceDb.iterator();

        while (it.hasNext()) {
            SQLDBModel dbModel = (SQLDBModel) it.next();
            List tables = dbModel.getTables();
            Iterator tableIt = tables.iterator();
            while (tableIt.hasNext()) {
                Object table = tableIt.next();
                if (table instanceof SQLDBTable && ((SQLDBTable) table).isEditable() && ((SQLDBTable) table).isSelected()) {
                    sTables.add(table);
                }
            }
        }

        List existingTargetTables = this.joinMainPanel.getTargetList();
        sTables.removeAll(existingTargetTables);
        this.joinMainPanel.setSourceList(new ArrayList(sTables));
    }

    /**
     * Remove a listener to changes of the panel's validity.
     * 
     * @param l the listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        this.listeners.remove(l);
    }

    public void setSourceList(Collection sList) {
        joinMainPanel.setSourceList(sList);
    }

    public void setTargetList(Collection tList) {
        joinMainPanel.setTargetList(tList);
    }

    /**
     * Provides the wizard panel with the opportunity to update the settings with its
     * current customized state. Rather than updating its settings with every change in
     * the GUI, it should collect them, and then only save them when requested to by this
     * method. Also, the original settings passed to {@link #readSettings}should not be
     * modified (mutated); rather, the object passed in here should be mutated according
     * to the collected changes, in case it is a copy. This method can be called multiple
     * times on one instance of <code>WizardDescriptor.Panel</code>.
     * <p>
     * The settings object is originally supplied to
     * {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}. In
     * the case of a <code>TemplateWizard.Iterator</code> panel, the object is in fact
     * the <code>TemplateWizard</code>.
     * 
     * @param settings the object representing wizard panel state
     */
    public void storeSettings(Object settings) {
        SQLJoinView joinView = this.joinMainPanel.getSQLJoinView();

        if (joinView != null && joinView.getSQLJoinTables().size() > 1) {
            WizardDescriptor wizard = null;
            if (settings instanceof ETLWizardContext) {
                ETLWizardContext wizardContext = (ETLWizardContext) settings;
                wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
            } else if (settings instanceof WizardDescriptor) {
                wizard = (WizardDescriptor) settings;
            }

            Object selectedOption = wizard.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            } else if (WizardDescriptor.PREVIOUS_OPTION == selectedOption) {
                // Null out any references to join views.
                wizard.putProperty(ETLCollaborationWizard.JOIN_VIEW, null);
                wizard.putProperty(ETLCollaborationWizard.JOIN_VIEW_VISIBLE_COLUMNS, null);

                // Mark any tables associated with the current join view as selected and
                // editable.
                List joinSourceTables = joinView.getSourceTables();
                List sourceDb = (List) wizard.getProperty(ETLCollaborationWizard.SOURCE_DB);
                Iterator it = sourceDb.iterator();
                while (it.hasNext()) {
                    SQLDBModel dbModel = (SQLDBModel) it.next();
                    Iterator sIt = joinSourceTables.iterator();
                    while (sIt.hasNext()) {
                        SourceTable sTable = (SourceTable) sIt.next();
                        if (dbModel.equals(sTable.getParent())) {
                            sTable.setEditable(true);
                            sTable.setSelected(true);
                        }
                    }
                }

                ListTransferPanel tp = joinMainPanel.getListTransferPanel();
                tp.actionPerformed(new ActionEvent(tp, ActionEvent.ACTION_PERFORMED, ListTransferPanel.LBL_REMOVE_ALL));

                // Zero out the source and target transfer panel lists.
                this.joinMainPanel.setSourceList(new ArrayList());
                this.joinMainPanel.setTargetList(new ArrayList());
            } else { // Next or finish option
                wizard.putProperty(ETLCollaborationWizard.JOIN_VIEW, joinView);
                wizard.putProperty(ETLCollaborationWizard.JOIN_VIEW_VISIBLE_COLUMNS, this.joinMainPanel.getTableColumnNodes());

                // NOW MARK TABLES WHICH ARE IN JOIN VIEW AS UNSELECTED AND UNEDITABLE
                List joinSourceTables = joinView.getSourceTables();
                List sourceDb = (List) wizard.getProperty(ETLCollaborationWizard.SOURCE_DB);
                Iterator it = sourceDb.iterator();
                while (it.hasNext()) {
                    SQLDBModel dbModel = (SQLDBModel) it.next();
                    Iterator sIt = joinSourceTables.iterator();
                    while (sIt.hasNext()) {
                        SourceTable sTable = (SourceTable) sIt.next();
                        if (dbModel.equals(sTable.getParent())) {
                            sTable.setEditable(false);
                            sTable.setSelected(false);
                        }
                    }
                }
            }
        }
    }
}

