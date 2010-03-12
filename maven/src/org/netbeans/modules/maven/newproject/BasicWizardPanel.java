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

package org.netbeans.modules.maven.newproject;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 * @author mkleint
 */
public class BasicWizardPanel implements WizardDescriptor.Panel,
        WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private BasicPanelVisual component;

    private final String[] eeLevels;
    private final Archetype[] archs;
    private final boolean isFinish;
    private boolean additional;
    private final ValidationGroup validationGroup;
    
    /** Creates a new instance of templateWizardPanel */
    public BasicWizardPanel(ValidationGroup vg, String[] eeLevels, Archetype[] archs, boolean isFinish, boolean additional) {
        this.archs = archs;
        this.eeLevels = eeLevels;
        this.isFinish = isFinish;
        this.additional = additional;
        this.validationGroup = vg;
    }

    public BasicWizardPanel(ValidationGroup vg) {
        this(vg, new String[0], null, true, true);
    }

    public BasicWizardPanel(ValidationGroup vg, boolean isFinish) {
        this(vg, new String[0], null, isFinish, false);
    }

    ValidationGroup getValidationGroup() {
        return validationGroup;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new BasicPanelVisual(this);
            component.setName(NbBundle.getMessage(BasicWizardPanel.class, "LBL_CreateProjectStep2"));
        }
        return component;
    }

    boolean areAdditional() {
        return additional;
    }

    Archetype[] getArchetypes() {
        return archs;
    }

    String[] getEELevels() {
        return eeLevels;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(BasicWizardPanel.class);
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
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
    }
    
    public boolean isFinishPanel() {
        return isFinish;
    }
    
    public boolean isValid() {
        getComponent();
        return validationGroup.validateAll().equals(Problem.NO_PROBLEM);
    }
    
}
