/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jdbcwizard.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Wizard to collect name and participating tables information to be used in creating a new JDBC
 * collaboration.
 */
public class JDBCCollaborationWizard extends JDBCWizard {
    /**
     * 
     *
     */
    public JDBCCollaborationWizard() {
    }

    class Descriptor extends JDBCWizardDescriptor {
        public Descriptor(final WizardDescriptor.Iterator iter) {
            super(iter, JDBCCollaborationWizard.this.context);
        }
    }

    class WizardIterator extends JDBCWizardIterator {
        private WizardDescriptor.Panel collaborationNamePanel;

        private JDBCWizardSelectionPanel dataSelectionPanel;

        private JNDINameFinishPanel jndiNamePanel;

        private List panels;

        private JDBCWizardTransferPanel sourceTableSelectionPanel;

        private JDBCCollaborationWizard mWizard;

        public WizardIterator(final JDBCCollaborationWizard wizard) {
            this.mWizard = wizard;

            this.collaborationNamePanel = new JDBCWizardNameFinishPanel(JDBCCollaborationWizard.this, NbBundle.getMessage(
                    JDBCCollaborationWizard.class, "TITLE_tblwizard_name"));

        }

        public String name() {
            return "";
        }

        @Override
        public void initialize(final WizardDescriptor wiz) {
            this.mWizard.setDescriptor(wiz);
            super.initialize(wiz);
        }

        /**
         * Overrides parent implementation to test for duplicate collab name before advancing to
         * next panel, and skip join panel if fewer than two source tables are selected.
         * 
         * @see org.openide.WizardDescriptor.Iterator#nextPanel
         */
        @Override
        public void nextPanel() {
            if (this.current().equals(this.sourceTableSelectionPanel)) { // Currently in source OTDs panel.
                final JDBCWizardTransferPanel xferPanel = (JDBCWizardTransferPanel) this.current();

            }

            super.nextPanel(); // Otherwise allow advance.

        }

        /**
         * Overrides parent implementation to skip join panel if fewer than two source tables are
         * selected.
         * 
         * @see org.openide.WizardDescriptor.Iterator#previousPanel
         */
        @Override
        public void previousPanel() {
            super.previousPanel(); // Otherwise use parent implementation.
        }

        @SuppressWarnings({"unchecked"})
        protected List createPanels(final WizardDescriptor wiz) {
            final List srcModel = new ArrayList();
            final List destModel = new ArrayList();

            final List dbModels = new ArrayList();

            final List sourceDBModels = new ArrayList();
            final List targetDBModels = new ArrayList();

            Project project = Templates.getProject(wiz);
            if (project != null) {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);

                if (groups == null || groups.length < 1) {
                    groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                }

                this.collaborationNamePanel = new SimpleTargetChooserPanel(project, groups, null, false);
            }

            this.dataSelectionPanel = new JDBCWizardSelectionPanel(NbBundle.getMessage(JDBCCollaborationWizard.class,
                    "TITLE_tblwizard_selecttableobjects"));
            ((JDBCWizardSelectionPanelUI) this.dataSelectionPanel.getComponent ()).initialize();
            this.sourceTableSelectionPanel = new JDBCWizardTransferPanel(NbBundle.getMessage(JDBCCollaborationWizard.class,
                    "TITLE_tblwizard_selectsources"));

            this.jndiNamePanel = new JNDINameFinishPanel(NbBundle.getMessage(JDBCCollaborationWizard.class,
                    "STEP_tblwizard_jndiname"));

            this.panels = new ArrayList(4);
            if (this.collaborationNamePanel != null) {
                this.panels.add(this.collaborationNamePanel);
            }
            this.panels.add(this.dataSelectionPanel);
            this.panels.add(this.sourceTableSelectionPanel);
            this.panels.add(this.jndiNamePanel);
            return Collections.unmodifiableList(this.panels);
        }

        protected String[] createSteps() {
            try {
                return new String[] {
                        // TODO - need make wizard steps text match actual panel being viewed
                        "Choose File Type", // TODO - use bundle property
                        NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_tblwizard_name"),
                        NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_tblwizard_select"),
                        NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_tblwizard_sources"),
                        NbBundle.getMessage(JDBCCollaborationWizard.class, "STEP_tblwizard_jndiname") };
            } catch (final MissingResourceException e) {
                return new String[] {};
            }
        }
    }

    /** Key name used to reference database sources in wizard context. */
    public static final String DATABASE_SOURCES = "database_sources";

    /** Key name used to reference JNDI Name. */
    public static final String JNDI_NAME = "jndi_name";

    public static final String DBTYPE = "dbtype";

    /** Key name used to reference collaboration name in wizard context. */
    public static final String COLLABORATION_NAME = "collaboration_name";

    /** Key name used to reference Project in wizard context. */
    public static final String PROJECT = "project";

    /** Key name used to reference Collection of runtime input args in wizard context. */
    public static final String RUNTIME_INPUTS = "runtime_inputs";

    /** Key name used to reference List of source OTDs in wizard context. */
    public static final String SOURCE_OTDS = "source_otds";

    public static final int SOURCE_PANEL_INDEX = 2;

    /** Key name used to reference List of source tables in wizard context. */
    public static final String SOURCE_TABLES = "source_tables";

    /* Defines panels to be displayed */
    private WizardDescriptor descriptor;

    /* Wizard iterator; handles display and movement among wizard panels */
    private JDBCWizardIterator iterator;

    private Project project;

    public JDBCCollaborationWizard(Project prj) {
        this.project = prj;
    }

    public static WizardDescriptor.Iterator newTemplateIterator() {
        final JDBCCollaborationWizard wizard = new JDBCCollaborationWizard();
        wizard.initialize();
        return wizard.getIterator();
    }

    /**
     * @see JDBCWizard#getDescriptor
     */
    public WizardDescriptor getDescriptor() {
        if (this.descriptor == null) {
            this.descriptor = new Descriptor(this.iterator);
        }
        return this.descriptor;
    }

    public void setDescriptor(final WizardDescriptor wd) {
        this.descriptor = wd;
    }

    /**
     * @see JDBCWizard#getIterator
     */
    public WizardDescriptor.Iterator getIterator() {
        return this.iterator;
    }

    // public Project getProject() {
    // return project;
    // }

    /**
     * Initializes iterator and descriptor for this wizard.
     */
    public void initialize() {
        this.iterator = new WizardIterator(this);

    }

    /**
     * Performs processing to handle cancellation of this wizard.
     */
    protected void cancel() {
    }

    /**
     * Performs processing to cleanup any resources used by this wizard.
     */
    protected void cleanup() {
    }

    @Override
    protected String getDialogTitle() {
        return NbBundle.getMessage(JDBCCollaborationWizard.class, "TITLE_dlg_new_collab");
    }

}
