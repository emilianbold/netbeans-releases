/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.rest.wizard.fromdb;

import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.DatabaseTablesPanel;
import org.netbeans.modules.websvc.rest.wizard.AbstractPanel;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Proxy to the J2EE persistence wizard, with an additional check that
 * our module requires.
 *
 * @author Nathan Fiedler
 */
public class DatabaseTablesWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {
    /** Our delegate panel which does the real work. */
    private DatabaseTablesPanel.WizardPanel wizardPanel;
    /** Descriptor, needed for displaying messages. */
    private WizardDescriptor wizardDescriptor;

    /**
     * Constructs the proxy wizard panel.
     *
     * @param  title  title for the wizard panel.
     * @param  desc   wizard descriptor.
     */
    public DatabaseTablesWizardPanel(String title, WizardDescriptor desc) {
        wizardPanel = new DatabaseTablesPanel.WizardPanel(title);
        wizardDescriptor = desc;
    }

    public Component getComponent() {
        return wizardPanel.getComponent();
    }

    public HelpCtx getHelp() {
        return wizardPanel.getHelp();
    }

    public void readSettings(WizardDescriptor settings) {
        wizardPanel.readSettings(settings);
    }

    public void storeSettings(WizardDescriptor settings) {
        wizardPanel.storeSettings(settings);
    }

    public boolean isValid() {
        boolean valid = wizardPanel.isValid();
        if (valid) {
            // Additional checks that we wish to perform.
            Project project = Templates.getProject(wizardDescriptor);
            if (Util.getPersistenceUnit(wizardDescriptor, project) == null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(AbstractPanel.class,
                        "MSG_EntitySelectionPanel_NoPersistenceUnit"));
                return false;
            }
        }
        return valid;
    }

    public void addChangeListener(ChangeListener l) {
        wizardPanel.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        wizardPanel.removeChangeListener(l);
    }

    public void stateChanged(ChangeEvent e) {
        wizardPanel.stateChanged(e);
    }
}
