/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
public class PanelConfigureProject implements WizardDescriptor.Panel<WizardDescriptor>, NewMakeProjectWizardIterator.Name, WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private WizardDescriptor wizardDescriptor;
    private String name;
    private int type;
    private PanelConfigureProjectVisual component;
    private String title;
    private String wizardTitle;
    private String wizardACSD;
    private boolean initialized = false;
    private boolean showMakefileTextField;

    /** Create the wizard panel descriptor. */
    public PanelConfigureProject(String name, int type, String wizardTitle, String wizardACSD, boolean showMakefileTextField) {
        this.name = name;
        this.type = type;
        this.wizardTitle = wizardTitle;
        this.wizardACSD = wizardACSD;
        this.showMakefileTextField = showMakefileTextField;
        title = NbBundle.getMessage(PanelConfigureProject.class, "LAB_ConfigureProject"); // NOI18N
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new PanelConfigureProjectVisual(this, this.name, this.wizardTitle, this.wizardACSD, showMakefileTextField, type);
        }
        return component;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public HelpCtx getHelp() {
        if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
            return new HelpCtx("NewAppWizard"); // NOI18N
        } else if (type == NewMakeProjectWizardIterator.TYPE_DYNAMIC_LIB) {
            return new HelpCtx("NewDynamicLibWizard"); // NOI18N
        } else if (type == NewMakeProjectWizardIterator.TYPE_STATIC_LIB) {
            return new HelpCtx("NewStaticLibWizard"); // NOI18N
        } else if (type == NewMakeProjectWizardIterator.TYPE_MAKEFILE) {
            return new HelpCtx("NewMakeWizardP1"); // NOI18N
        } else {
            return new HelpCtx("CreatingCorC++Project"); // NOI18N
        }
    }

    @Override
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
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

    @Override
    public void readSettings(WizardDescriptor settings) {
        if (initialized) {
            return;
        }
        wizardDescriptor = settings;
        component.read(wizardDescriptor);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null) {
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
        }
        initialized = true;
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        WizardDescriptor d = settings;
        component.store(d);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
