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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.etl.ui.view.wizardsloader;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.wizards.ETLCollaborationWizard;
import org.netbeans.modules.etl.ui.view.wizards.ETLWizardContext;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Panel for configuring table details such as table type,etc.
 * @author karthik
 */
public class SelectedTableMapperPanel implements WizardDescriptor.FinishablePanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private WizardDescriptor wd;
    private List<SQLDBModel> ttmodellist = null;
    private List srcmodellist = null;
    private boolean isBulkLoader = false;
    private static transient final Logger mLogger = Logger.getLogger(SelectedTableMapperPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public Component getComponent() {
        if (component == null) {
            component = new SelectedTableMapperVisualPanel(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        return canAdvance();
    }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    public void readSettings(Object settings) {
        WizardDescriptor wizard = null;
        if (settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);

        } else if (settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }

        wd = wizard;
        isBulkLoader = (Boolean) wizard.getProperty(ETLCollaborationWizard.IS_BULK_LOADER);

        if (isBulkLoader) {
            //Get Bulk Loader Source Files Path
            srcmodellist = (List) wizard.getProperty(ETLCollaborationWizard.BULK_LOADER_SRC_DATA_FILEOBJ_LIST);
        } else {
            //Get Source Database Models
            srcmodellist = (List) wizard.getProperty(ETLCollaborationWizard.SOURCE_DB);
        }

        //Get Target Database Models
        ttmodellist = (List) wizard.getProperty(ETLCollaborationWizard.TARGET_DB);
        //Get Collaboration name
        String collabname = (String) wizard.getProperty(ETLCollaborationWizard.COLLABORATION_NAME);
        ((SelectedTableMapperVisualPanel) getComponent()).createComponentGraphics(srcmodellist, ttmodellist, collabname, isBulkLoader);

        //Disable Constraints by default for Bulk Loader
        if (isBulkLoader) {
            // Set Disable constraints by default
            ((SelectedTableMapperVisualPanel) getComponent()).setVisualConstraintsDisable(true);            
        }
        
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wizard = null;
        if (settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
        } else if (settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }

        if (wizard != null) {
            final Object selectedOption = wizard.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            }

            boolean isAdvancingPanel = (selectedOption == WizardDescriptor.FINISH_OPTION);
            if (isAdvancingPanel) {
                if (this.isBulkLoader) {
                    //Generate New Source Model from the file selections made
                    BulkLoaderFileSelectionModeller bulkloadmodel = new BulkLoaderFileSelectionModeller(wizard);
                    List<SQLDBModel> srcmodels = bulkloadmodel.generateSourceModel(((SelectedTableMapperVisualPanel) getComponent()).getMapperModel());
                    wizard.putProperty(ETLCollaborationWizard.SOURCE_DB, srcmodels);
                    ((SelectedTableMapperVisualPanel) getComponent()).getMapperModel().generateETLArtifacts();
                } else {
                    List<SQLDBModel> srcmastermodellist = (List) wizard.getProperty(ETLCollaborationWizard.SOURCE_DB);
                    ((SelectedTableMapperVisualPanel) getComponent()).getMapperModel().generateUserMappedModels(srcmastermodellist);
                }
            }
        }
    }

    protected boolean isWizardInBulkLoadMode() {
        return this.isBulkLoader;
    }

    protected WizardDescriptor getWizardDescriptor() {
        return wd;
    }

    private boolean canAdvance() {
        return ((SelectedTableMapperVisualPanel) getComponent()).canAdvance();
    }

    public boolean isFinishPanel() {
        return true;
    }
}
