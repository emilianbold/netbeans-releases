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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBConnectionFactory;
import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.ui.wizard.ChooseTablePanel;
import org.netbeans.modules.etl.ui.view.wizardsloader.FileSelectionLoaderPanel;
import org.netbeans.modules.dm.virtual.db.ui.wizard.SpreadsheetChooserPanel;
import org.netbeans.modules.dm.virtual.db.ui.wizard.TableDetailsPanel;
import org.netbeans.modules.dm.virtual.db.ui.wizard.VirtualDBTableWizardIterator;
import org.netbeans.modules.dm.virtual.db.ui.wizard.TableDefinitionPanel;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.wizardsloader.SelectLoaderTypePanel;
import org.netbeans.modules.etl.ui.view.wizardsloader.SelectOrCreateDBPanel;
import org.netbeans.modules.etl.ui.view.wizardsloader.SelectSchemaGenPanel;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;

/**
 * A wizard iterator (sequence of panels). Used to create a wizard. Create one or more
 * panels from template as needed too.
 * 
 */
public abstract class ETLWizardIterator implements WizardDescriptor.InstantiatingIterator {

    /** Tracks index of current panel */
    protected transient int index = 0;

    /* Set <ChangeListener> */
    private transient Set listeners = new HashSet(1);

    /* Contains panels to be iterated */
    private transient WizardDescriptor.Panel[] panels = null;
    private transient WizardDescriptor wiz;
    private List<String> tables = new ArrayList<String>();
    private int tracker = -1;
    private static transient final Logger mLogger = Logger.getLogger(ETLWizardIterator.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // implement fireChangeEvent().
    /**
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener
     */
    public final void addChangeListener(ChangeListener l) {

        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public int getIndex() {
        return index;
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#current
     */
    public WizardDescriptor.Panel current() {
        return getPanels(this.wiz)[index];
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#hasNext
     */
    public boolean hasNext() {
        return index < getPanels(this.wiz).length - 1;
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#hasPrevious
     */
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#name
     */
    public abstract String name();

    /**
     * @see org.openide.WizardDescriptor.Iterator#nextPanel
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        if (current() instanceof SelectLoaderTypePanel) {
            Boolean isbasicetl = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BASIC_ETL_LOADER);
            ETLCollaborationWizard.CREATE_DEFAULT_ETL_NODE = true;
            if (isbasicetl != null) {
                if (isbasicetl) {
                    index = 8; //Go to Select JDBC Source Tables
                } else {
                    index++;
                }
            }
        } else if (current() instanceof SelectOrCreateDBPanel) {
            Boolean isbulkloader = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BULK_LOADER);
            if (isbulkloader != null) {
                if (isbulkloader) {
                    index = 11; //Go to Select JDBC Target Tables
                } else {
                    index++;
                }
            }
        } else if (current() instanceof FileSelectionLoaderPanel) {
            tables = (List<String>) wiz.getProperty(VirtualDBTableWizardIterator.TABLE_LIST);
            if (tables.size() > 0) {
                tracker = 0;
                index++;
            } else {
                index = 8; //Select JDBC targets
            }
        } else if (current() instanceof TableDefinitionPanel) {
            if (++tracker < tables.size()) {
                index = 4;
            } else {
                //Create All the Tables
                createExternalTables(wiz);
                index++;
            }
        } else if (current() instanceof SpreadsheetChooserPanel || current() instanceof ChooseTablePanel) {
            index = 6;
        } else if (current() instanceof TableDetailsPanel) {
            VirtualDBTable tbl = (VirtualDBTable) wiz.getProperty(VirtualDBTableWizardIterator.PROP_CURRENTTABLE);
            String type = tbl.getParserType();
            if (type.equals("WEB")) {
                index = 5;
            } else if (type.equals("SPREADSHEET")) {
                index = 4;
            } else {
                index = 6;
            }
        }else if (((current() instanceof ETLCollaborationWizardTransferFinishPanel)) && (index == 8)) { //Source Table Chooser 
            Boolean isbasicetl = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BASIC_ETL_LOADER);
            if (!isbasicetl){
                //index++;index++;
                if (((ETLCollaborationWizardTransferPanel) current()).hasEnoughTablesForJoin()) {
                    index++;
                    index++;
                } else {
                    index++;
                }
            }else{
                index++;
            }
        }
        else if ((current() instanceof ETLCollaborationWizardTransferFinishPanel) && (index == 11)) { //Target Table Chooser 
            Boolean isbulkloader = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BULK_LOADER);
            Boolean isbasicetl = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BASIC_ETL_LOADER);
            if (isbulkloader) {
                ETLCollaborationWizard.CREATE_DEFAULT_ETL_NODE = false;
                index = 12; //Bulk Loader src file selection
            } else {
                if (isbasicetl) {
                    //XXX - Should disable next here. User must finish here.
                } else {
                    ETLCollaborationWizard.CREATE_DEFAULT_ETL_NODE = false;
                    index = 13;
                }
            }
        } else if (current() instanceof ETLCollaborationWizardJoinFinishPanel) {
            Boolean isbasicetl = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BASIC_ETL_LOADER);
            if (isbasicetl) {
                index = 11; //Go to Select JDBC Target Tables
            } else {
                index++;
            }
        } else {
            index++;

        }

        if (current() instanceof TableDetailsPanel) {
            wiz.putProperty(VirtualDBTableWizardIterator.TABLE_INDEX, String.valueOf(tracker));
        }
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#previousPanel
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        if ((current() instanceof ETLCollaborationWizardTransferFinishPanel) && (index == 11)) {
            //Target JDBC DB Selector
            Boolean isbulkloader = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BULK_LOADER);
            Boolean isbasicetl = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BASIC_ETL_LOADER);
            if (isbulkloader) {
                index = 3;
            } else if (isbasicetl) {
                index = 9;
            } else {
                index--; //Advanced ETL
            }
        } else if ((current() instanceof ETLCollaborationWizardTransferFinishPanel) && (index == 8)) {
            //Source JDBC DB Selector
            Boolean isbasicetl = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BASIC_ETL_LOADER);
            if (isbasicetl) {
                index = 1;
            }else{
                index--;
            }
        }else if ((current() instanceof SelectSchemaGenPanel)){
            Boolean isbasicetl = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BASIC_ETL_LOADER);
            if (!isbasicetl){
                index--;index--;
            }else
            {
                index --;
            }
        }else if (index == 13) {
            Boolean isbulkloader = (Boolean) wiz.getProperty(ETLCollaborationWizard.IS_BULK_LOADER);
            if (isbulkloader) {
                index = 12;
            } else {
                ETLCollaborationWizard.CREATE_DEFAULT_ETL_NODE = true;
                index = 11;
            }
        } else {
            index--;
        }
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#removeChangeListener
     */
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * Creates list of panels to be displayed.
     * 
     * @return List of panels
     */
    protected abstract List createPanels(WizardDescriptor wiz);

    /**
     * Creates array of step descriptions
     * 
     * @return array of Strings representing task summaries for each panel
     */
    protected abstract String[] createSteps();

    /**
     * Gets panels to be displayed.
     * 
     * @return array of WizardDescriptor.Panel objects
     */
    protected final WizardDescriptor.Panel[] getPanels(WizardDescriptor wiz) {
        if (panels == null) {
            List myPanels = createPanels(wiz);

            WizardDescriptor.Panel[] pnlArray = new WizardDescriptor.Panel[myPanels.size()];
            panels = (WizardDescriptor.Panel[]) myPanels.toArray(pnlArray);
        }
        return panels;
    }

    /**
     * Gets list of steps corresponding to each panel
     * 
     * @return array of Strings summarizing the task in each panel
     */
    protected final String[] getSteps() {
        return createSteps();
    }

    public void initialize(WizardDescriptor wiz) {
        this.panels = getPanels(wiz);
        this.wiz = wiz;

        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components

                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).

                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N

            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
    }

    public Set instantiate() throws IOException {
        return new HashSet();
    }

    public void createExternalTables(WizardDescriptor wizardDescriptor) {
        String error = null;
        boolean status = false;
        String jdbcUrl = (String) wizardDescriptor.getProperty("url");
        VirtualDatabaseModel model = (VirtualDatabaseModel) wizardDescriptor.getProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
        List<String> urls = (List<String>) wizardDescriptor.getProperty(VirtualDBTableWizardIterator.URL_LIST);
        List<String> filesrctables = (List<String>) wizardDescriptor.getProperty(VirtualDBTableWizardIterator.TABLE_LIST);
        Connection conn = null;
        Statement stmt = null;
        String dbDir = (DBExplorerUtil.parseConnUrl(jdbcUrl))[1];
        try {
            conn = VirtualDBConnectionFactory.getInstance().getConnection(jdbcUrl);
            if (conn != null) {
                conn.setAutoCommit(true);
                stmt = conn.createStatement();
            }

            if (model != null) {
                Iterator tablesIt = model.getTables().iterator();
                while (tablesIt.hasNext()) {
                    VirtualDBTable table = (VirtualDBTable) tablesIt.next();
                    int i = filesrctables.indexOf(table.getName());
                    ((VirtualDBTable) table).setOrPutProperty(PropertyKeys.FILENAME, urls.get(i));
                    String sql = table.getCreateStatementSQL();
                    stmt.execute(sql);
                }
            }
            status = true;
        } catch (Exception ex) {
            ErrorManager.getDefault().log(ex.getMessage());
            status = false;
        } finally {
            if (conn != null) {
                try {
                    if (stmt != null) {
                        stmt.execute("shutdown");
                    }
                    conn.close();
                    File dbExplorerNeedRefresh = new File(dbDir + "/dbExplorerNeedRefresh");
                    dbExplorerNeedRefresh.createNewFile();
                } catch (SQLException ex) {
                    conn = null;
                } catch (Exception ex) {
                    // ignore
                    }
            }
        }

        if (status) {
            String nbBundle2 = mLoc.t("BUND503: Table(s) successfully created.");
            NotifyDescriptor d = new NotifyDescriptor.Message(nbBundle2.substring(15), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } else {
            String nbBundle3 = mLoc.t("BUND504: Table creation failed.");
            String msg = nbBundle3.substring(15);
            if (error != null) {
                msg = msg + "CAUSE:" + error;
            }
            NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }

    }
}
