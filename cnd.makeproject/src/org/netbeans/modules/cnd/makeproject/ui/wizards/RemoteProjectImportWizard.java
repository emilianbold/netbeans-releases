/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public class RemoteProjectImportWizard {
    public static final String PROPERTY_REMOTE_PROJECTS = "RemoteImportedProjects"; // Collection<ImportedProject> // NOI18N

    private boolean cancelled;
    private final RemoteProjectImportWizardIterator iterator;

    public RemoteProjectImportWizard() {
        iterator = new RemoteProjectImportWizardIterator();
    }
    
    public static void showImportWizard() {
        RemoteProjectImportWizard wizard = new RemoteProjectImportWizard();
        wizard.importProjects();
    }

    private void importProjects() {
        final WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        iterator.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                        iterator.getErrorMessage());
            }
        });        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(NbBundle.getMessage(RemoteProjectImportWizard.class, "RemoteProjectImportWizard.title"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            // do something
        }
    }
    
    /**
     * Returns whether user canceled the wizard.
     */
    private boolean isCancelled() {
        return cancelled;
    }
    
    /** Returns project selected by user with the help of the wizard. */
    private List<ImportedProject> getProjectsToImport() {
        return iterator.getProjects();
    }
    
    public static final class ImportedProject {

        private final String remoteProjectFolder;
        private final ExecutionEnvironment remoteEnv;
        private final String localProjectFolder;

        public ImportedProject(ExecutionEnvironment env, String remotePrjFolder, String localPrjFolder) {
            assert env != null;
            this.remoteEnv = env;
            assert remotePrjFolder != null;
            this.remoteProjectFolder = remotePrjFolder;
            assert localPrjFolder != null;
            this.localProjectFolder = localPrjFolder;
        }

        public ExecutionEnvironment getRemoteEnvironment() {
            return remoteEnv;
        }

        public String getRemoteProjectFolder() {
            return remoteProjectFolder;
        }

        public String getLocalProjectDestinationFolder() {
            return localProjectFolder;
        }

        @Override
        public String toString() {
            return "ImportedProject{" + "remoteProjectFolder=" + remoteProjectFolder + ", remoteEnv=" + remoteEnv + ", localProjectFolder=" + localProjectFolder + '}'; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ImportedProject other = (ImportedProject) obj;
            if (this.remoteProjectFolder != other.remoteProjectFolder && (this.remoteProjectFolder == null || !this.remoteProjectFolder.equals(other.remoteProjectFolder))) {
                return false;
            }
            if (this.remoteEnv != other.remoteEnv && (this.remoteEnv == null || !this.remoteEnv.equals(other.remoteEnv))) {
                return false;
            }
            if (this.localProjectFolder != other.localProjectFolder && (this.localProjectFolder == null || !this.localProjectFolder.equals(other.localProjectFolder))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + (this.remoteProjectFolder != null ? this.remoteProjectFolder.hashCode() : 0);
            hash = 47 * hash + (this.remoteEnv != null ? this.remoteEnv.hashCode() : 0);
            hash = 47 * hash + (this.localProjectFolder != null ? this.localProjectFolder.hashCode() : 0);
            return hash;
        }
    }
}
