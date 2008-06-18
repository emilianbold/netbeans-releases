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
package org.netbeans.modules.db.dataview.editor.wizard;

import org.netbeans.modules.db.dataview.editor.wizard.DataViewWizardDescriptor;
import org.netbeans.modules.db.dataview.editor.wizard.DataViewWizardContext;
import org.netbeans.modules.db.dataview.editor.wizard.DataViewWizard;
import org.netbeans.modules.db.dataview.editor.wizard.DataViewWizardIterator;
import org.netbeans.modules.db.dataview.editor.wizard.SimpleTargetChooserPanel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Wizard to collect name and participating tables information to be used in creating a
 * new DataView collaboration.
 */
public class DataViewCollaborationWizard extends DataViewWizard {   

    public DataViewCollaborationWizard() {
        initialize();
    }

    class Descriptor extends DataViewWizardDescriptor {

        public Descriptor(WizardDescriptor.Iterator iter) {
            super(iter, context);
        }
    }

    class WizardIterator extends DataViewWizardIterator {

        private WizardDescriptor.Panel collaborationNamePanel;
        private List panels;
        private DataViewCollaborationWizard mWizard;

        public WizardIterator(DataViewCollaborationWizard wizard) {
            this.mWizard = wizard;            
            String nbBundle1 = "Enter a Unique Name for This Collaboration.";
            collaborationNamePanel = new DataViewCollaborationWizardNameFinishPanel(DataViewCollaborationWizard.this,nbBundle1);
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
         * Overrides parent implementation to skip join panel if fewer than two source
         * tables are selected.
         *
         */
        @Override
        public void previousPanel() {
            super.previousPanel(); // Otherwise use parent implementation.
        }


        protected List createPanels(WizardDescriptor wiz) {
            Project project = Templates.getProject(wiz);            
            if (project != null) {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(
                        Sources.TYPE_GENERIC);

                if ((groups == null) || (groups.length < 1)) {
                    groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                }

                collaborationNamePanel = new SimpleTargetChooserPanel(project, groups, null, false);
            }


            panels = new ArrayList(2);
            if (collaborationNamePanel != null) {
                panels.add(collaborationNamePanel);
            }
            return Collections.unmodifiableList(panels);
        }

        public void storeSettings(Object settings, List models) {
            WizardDescriptor wd = null;
            if (settings instanceof DataViewWizardContext) {
                DataViewWizardContext wizardContext = (DataViewWizardContext) settings;
                wd = (WizardDescriptor) wizardContext.getProperty(DataViewWizardContext.WIZARD_DESCRIPTOR);

            } else if (settings instanceof WizardDescriptor) {
                wd = (WizardDescriptor) settings;
            }
        }

        protected String[] createSteps() {
            try {
                String nbBundle1 = "Choose File Type";
                String nbBundle2 = "Enter Collaboration Name";
                
              
                return new String[]{
                    nbBundle1,
                    nbBundle2,
                };
            } catch (MissingResourceException e) {
                java.util.logging.Logger.getLogger(DataViewCollaborationWizard.class.getName()).info("MissingResourceException "+e.getMessage());
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
                return Collections.singleton(dobj.getPrimaryFile());
            }
            return new HashSet();
        }
    }

    private WizardDescriptor descriptor;
    /* Wizard iterator; handles display and movement among wizard panels */
    private DataViewWizardIterator iterator;

    public static WizardDescriptor.Iterator newTemplateIterator() {
        DataViewCollaborationWizard wizard = new DataViewCollaborationWizard();
        return wizard.getIterator();
    }

    /**
     * @see DataViewWizard#getDescriptor
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
     * @see DataViewWizard#getIterator
     */
    public WizardDescriptor.Iterator getIterator() {
        return iterator;
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

    @Override
    protected String getDialogTitle() {
        String nbBundle9 = "New Collaboration Definition Wizard (DataView)";
        return nbBundle9;
    }

    
    public static final String COLLABORATION_NAME = "collaboration_name";
}

