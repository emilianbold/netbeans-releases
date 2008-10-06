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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;

/**
 *
 * @author schmidtm
 */
public class GetArtifactNameStep implements  WizardDescriptor.Panel<WizardDescriptor>, 
                                                WizardDescriptor.ValidatingPanel<WizardDescriptor>,
                                                WizardDescriptor.FinishablePanel<WizardDescriptor>
                                                {

    private GetArtifactNamePanel component;
    private WizardDescriptor wizardDescriptor;
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    boolean        serverRunning = false;
    boolean        serverConfigured = true;
    GrailsProject project;
    SourceCategory cat;

    public GetArtifactNameStep(boolean serverRunning, boolean serverConfigured, GrailsProject project, SourceCategory cat) {
        this.serverRunning = serverRunning;
        this.serverConfigured = serverConfigured;
        this.project = project;
        this.cat = cat;
    }

    public Component getComponent() {
        if (component == null) {
            component = new GetArtifactNamePanel(this, cat);
        }
        return component;
    }

    public HelpCtx getHelp() {
        // I am returning name of this class + dot + grails command name, that is something that should
        // be quite stable and independent
        return new HelpCtx(GetArtifactNameStep.class.getName() + "." + cat.getCommand());
    }

    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;        
        component.read (wizardDescriptor);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFiletWizard to modify the title
        Object substitute = ((JComponent)component).getClientProperty ("NewFileWizard_Title"); // NOI18N
        if (substitute != null) {
            wizardDescriptor.putProperty ("NewFileWizard_Title", substitute); // NOI18N
        }
    }

    public void storeSettings(WizardDescriptor settings) {
        WizardDescriptor d = settings;
        component.store(d);
    }

    public boolean isValid() {
        getComponent();
        if(!serverConfigured) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(NewGrailsProjectWizardIterator.class,
                "NewGrailsProjectWizardIterator.NoGrailsServerConfigured"));
            return false;
        }
        if (serverRunning) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(NewGrailsProjectWizardIterator.class,
                "GetProjectLocationStep.ServerIsRunning"));
            return false;
        }
        if (!component.valid(wizardDescriptor)) {
            return false;
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
        return true;
    }

    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public void validate() throws WizardValidationException {
        getComponent ();
        component.validate (wizardDescriptor);
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    public String getArtifactName(){
        return component.getArtifactName();
        }
    
    public String getFileName(){
        return component.getFileName();
    }
    
    public GrailsProject getGrailsProject() {
        return project;
    }
    
}
