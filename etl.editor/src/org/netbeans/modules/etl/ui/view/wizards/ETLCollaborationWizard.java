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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.SwingUtilities;
import net.java.hulp.i18n.Logger;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Wizard to collect name and participating tables information to be used in creating a
 * new ETL collaboration.
 */
public class ETLCollaborationWizard extends ETLWizard {

    private static transient final Logger mLogger = Logger.getLogger(ETLCollaborationWizard.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public ETLCollaborationWizard() {
        initialize();
    }

    class Descriptor extends ETLWizardDescriptor {

        public Descriptor(WizardDescriptor.Iterator iter) {
            super(iter, context);
        }
    }

    class WizardIterator extends ETLWizardIterator {

        private WizardDescriptor.Panel collaborationNamePanel;
        private ETLCollaborationWizardJoinFinishPanel joinSelectionPanel;
        private List panels;
        private ETLCollaborationWizardTransferFinishPanel sourceTableSelectionPanel;
        private ETLCollaborationWizardTransferFinishPanel targetTableSelectionPanel;
        private ETLCollaborationWizard mWizard;

        public WizardIterator(ETLCollaborationWizard wizard) {
            this.mWizard = wizard;
            //
            // NOTE: If the order of source and target panels are changed, please update
            // the values of SOURCE_PANEL_INDEX and TARGET_PANEL_INDEX appropriately.
            //
            // These variables are used to determine how to skip past the join panel
            // between source and target panels if fewer than two source tables are
            // selected.
            //
            String nbBundle1 = mLoc.t("BUND053: Enter a Unique Name for This Collaboration.");
            collaborationNamePanel = new ETLCollaborationWizardNameFinishPanel(ETLCollaborationWizard.this, nbBundle1.substring(15));
        }

        public String name() {
            return "";
        }

        @Override
        public void initialize(WizardDescriptor wiz) {
            this.mWizard.setDescriptor(wiz);
            super.initialize(wiz);
        }

        /**
         * Overrides parent implementation to test for duplicate collab name before
         * advancing to next panel, and skip join panel if fewer than two source tables
         * are selected.
         *
         * @see org.openide.WizardDescriptor.Iterator#nextPanel
         */
        @Override
        public void nextPanel() {
            if (current().equals(sourceTableSelectionPanel)) { // Currently in source Database panel.
                ETLCollaborationWizardTransferPanel xferPanel = (ETLCollaborationWizardTransferPanel) current();

                // Skip join panel if we don't have two or more tables selected,
                // and no joins have been created.

                if (!xferPanel.hasEnoughTablesForJoin() && descriptor.getProperty(ETLCollaborationWizard.JOIN_VIEW) == null) {
                    super.nextPanel();
                    super.nextPanel();
                    return;
                }
            }

            super.nextPanel(); // Otherwise allow advance.
        }

        /**
         * Overrides parent implementation to skip join panel if fewer than two source
         * tables are selected.
         *
         */
        @Override
        public void previousPanel() {
            if (current().equals(targetTableSelectionPanel)) {

                // Skip join panel if we don't have two or more tables selected in the
                // source panel, and no joins have been created.
                if (!sourceTableSelectionPanel.hasEnoughTablesForJoin() && descriptor.getProperty(ETLCollaborationWizard.JOIN_VIEW) == null) {
                    super.previousPanel();
                    super.previousPanel();
                    return;
                }
            }
            super.previousPanel(); // Otherwise use parent implementation.
        }

        private List getModelConnections() {
            List model = new ArrayList();
            DBExplorerUtil.recreateMissingFlatfileConnectionInDBExplorer();
            model.addAll(DBExplorerUtil.getDatabasesForCurrentProject());

            DatabaseConnection[] conns = ConnectionManager.getDefault().getConnections();
            if (conns.length > 0) {
                for (int i = 0; i < conns.length; i++) {
                    if (conns[i] == null) {
                        model.add("<NULL>");
                    } else {
                        if (!(conns[i].getName().contains("jdbc:axiondb"))) {
                            model.add(conns[i]);
                        }
                    }
                }
            } else if (model.size() == 0) {
                model.add("<None>");
            }
            return model;
        }

        protected List createPanels(WizardDescriptor wiz) {
            List srcModel = new ArrayList();
            List destModel = new ArrayList();

            Project project = Templates.getProject(wiz);
            String prj_locn = FileUtil.toFile(project.getProjectDirectory()).getPath();
            String prj_name = FileUtil.toFile(project.getProjectDirectory()).getName();
            try {
                prj_locn = project.getProjectDirectory().getFileSystem().getRoot().toString() + prj_locn;
                ETLEditorSupport.PRJ_PATH = prj_locn;
                ETLEditorSupport.PRJ_NAME = prj_name;

            } catch (FileStateInvalidException ex) {
                //Exceptions.printStackTrace(ex);
            }
            List dbModels = getModelConnections();
            storeSettings(wiz, dbModels);
            //java.util.logging.Logger.getLogger(ETLCollaborationWizard.class.getName()).info("ETLCollaborationWizard prj_locn ******* "+prj_locn);
            if (project != null) {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(
                        Sources.TYPE_GENERIC);

                if ((groups == null) || (groups.length < 1)) {
                    groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                }

                collaborationNamePanel = new SimpleTargetChooserPanel(project, groups, null, false);
            }
            String nbBundle2 = mLoc.t("BUND054: Select Source Tables");
            sourceTableSelectionPanel = new ETLCollaborationWizardTransferFinishPanel(nbBundle2.substring(15), dbModels, srcModel, true);
            String nbBundle3 = mLoc.t("BUND055: Select Source Tables to Create Join.");
            joinSelectionPanel = new ETLCollaborationWizardJoinFinishPanel(ETLCollaborationWizard.this, nbBundle3.substring(15), null);
            String nbBundle4 = mLoc.t("BUND056: Select Target Tables");
            targetTableSelectionPanel = new ETLCollaborationWizardTransferFinishPanel(nbBundle4.substring(15), dbModels, destModel, false);

            panels = new ArrayList(4);
            if (collaborationNamePanel != null) {
                panels.add(collaborationNamePanel);
            }

            panels.add(sourceTableSelectionPanel);
            panels.add(joinSelectionPanel);
            panels.add(targetTableSelectionPanel);
            return Collections.unmodifiableList(panels);
        }

        public void storeSettings(Object settings, List models) {
            WizardDescriptor wd = null;
            if (settings instanceof ETLWizardContext) {
                ETLWizardContext wizardContext = (ETLWizardContext) settings;
                wd = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);

            } else if (settings instanceof WizardDescriptor) {
                wd = (WizardDescriptor) settings;
            }

            if (wd != null) {
                // Don't commit if user didn't click next.
                // if (wd.getValue() != WizardDescriptor.NEXT_OPTION) {
                // return;
                // }
                wd.putProperty(ETLCollaborationWizard.DATABASE_SOURCES, models.toArray());
                wd.putProperty(ETLCollaborationWizard.DATABASE_TARGETS, models.toArray());
            }
        }

        protected String[] createSteps() {
            try {
                String nbBundle5 = mLoc.t("BUND057: Enter Collaboration Name");
                String nbBundle6 = mLoc.t("BUND054: Select Source Tables");
                String nbBundle7 = mLoc.t("BUND058: Select Source Tables for Join");
                String nbBundle8 = mLoc.t("BUND056: Select Target Tables");
                String nbBundle10 = mLoc.t("BUND059: Choose File Type");
                return new String[]{
                            //TODO - need make wizard steps text match actual panel being viewed
                            nbBundle10.substring(15), //TODO - use bundle property
                            nbBundle5.substring(15),
                            nbBundle6.substring(15),
                            nbBundle7.substring(15),
                            nbBundle8.substring(15),
                        };
            } catch (MissingResourceException e) {
                mLogger.errorNoloc(mLoc.t("EDIT029: Could not locate steps strings.{0}", LOG_CATEGORY), e);
                return new String[]{};
            }
        }

        @Override
        public Set instantiate() throws IOException {
            commit();

            FileObject dir = Templates.getTargetFolder(descriptor);
            if (dir != null) {
                DataFolder df = DataFolder.findFolder(dir);
                FileObject template = Templates.getTemplate(descriptor);

                DataObject dTemplate = DataObject.find(template);
                DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(descriptor));
                if (dobj instanceof ETLDataObject) {
                    final ETLDataObject etlDataObj = (ETLDataObject) dobj;
                    Runnable run = new Runnable() {

                        public void run() {
                            etlDataObj.initialize(descriptor);
                            if (etlDataObj.getNodeDelegate() != null) {
                                OpenCookie openCookie = etlDataObj.getNodeDelegate().getCookie(OpenCookie.class);
                                openCookie.open();
                            }
                        }
                    };

                    SwingUtilities.invokeLater(run);
                }

                return Collections.singleton(dobj.getPrimaryFile());
            }
            return new HashSet();
        }
    }
    /** Key name used to reference database sources in wizard context. */
    public static final String DATABASE_SOURCES = "database_sources";
    /** Key name used to reference database sources in wizard context. */
    public static final String DATABASE_TARGETS = "database_targets";
    /** Key name used to reference collaboration name in wizard context. */
    public static final String COLLABORATION_NAME = "collaboration_name";
    /** Key name used to reference List of destination Database in wizard context. */
    public static final String TARGET_DB = "destination_dbs";
    /** Key name used to reference List of destination tables in wizard context. */
    public static final String DESTINATION_TABLES = "destination_tables";
    /** Key name used to reference SQLJoinView if any created by user */
    public static final String JOIN_VIEW = "join_view";
    /** Key name used to reference List of visible columns selected by user */
    public static final String JOIN_VIEW_VISIBLE_COLUMNS = "join_view_visible_columns";
    /** Key name used to reference Project in wizard context. */
    public static final String PROJECT = "project";
    /** Key name used to reference Collection of runtime input args in wizard context. */
    public static final String RUNTIME_INPUTS = "runtime_inputs";
    /** Key name used to reference List of source Database in wizard context. */
    public static final String SOURCE_DB = "source_dbs";
    public static final int SOURCE_PANEL_INDEX = 2;
    /** Key name used to reference List of source tables in wizard context. */
    public static final String SOURCE_TABLES = "source_tables";
    public static final int TARGET_PANEL_INDEX = 4;
    /* Log4J category string */
    private static final String LOG_CATEGORY = ETLCollaborationWizard.class.getName();
    /* Defines panels to be displayed */
    private WizardDescriptor descriptor;
    /* Wizard iterator; handles display and movement among wizard panels */
    private ETLWizardIterator iterator;

    public static WizardDescriptor.Iterator newTemplateIterator() {
        ETLCollaborationWizard wizard = new ETLCollaborationWizard();
        return wizard.getIterator();
    }

    /**
     * @see ETLWizard#getDescriptor
     */
    public WizardDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new Descriptor(iterator);
        }
        return descriptor;
    }

    public void setDescriptor(WizardDescriptor wd) {
        this.descriptor = wd;
    }

    /**
     * @see ETLWizard#getIterator
     */
    public WizardDescriptor.Iterator getIterator() {
        return iterator;
    }

    /**
     * Gets List of destination Databases as selected by user.
     *
     * @return List (possibly empty) of selected destination Databases
     */
    public List getSelectedDestinationDb() {
        return getSelectedDbOfType(ETLCollaborationWizard.TARGET_DB);
    }

    /**
     * Gets List of source Databases as selected by user.
     *
     * @return List (possibly empty) of selected source Databases
     */
    public List getSelectedSourceDb() {
        return getSelectedDbOfType(ETLCollaborationWizard.SOURCE_DB);
    }

    public SQLJoinView getSQLJoinView() {
        return (SQLJoinView) descriptor.getProperty(JOIN_VIEW);
    }

    public List getTableColumnNodes() {
        return (List) descriptor.getProperty(JOIN_VIEW_VISIBLE_COLUMNS);
    }

    /**
     * Initializes iterator and descriptor for this wizard.
     */
    public void initialize() {
        iterator = new WizardIterator(this);
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

    /**
     * Performs processing to handle committal of data gathered by this wizard.
     */
    protected void commit() {
    }

    /**
     * @see org.netbeans.modules.etl.ui.view.wizards.ETLWizard#getDialogTitle()
     */
    @Override
    protected String getDialogTitle() {
        String nbBundle9 = mLoc.t("BUND060: New Collaboration Definition Wizard (ETL)");
        return nbBundle9.substring(15);
    }

    private List getSelectedDbOfType(String typeKey) {
        return (List) descriptor.getProperty(typeKey);
    }
}

