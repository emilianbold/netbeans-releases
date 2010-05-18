/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.xml.schema.wizard;

//java imports
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//netbeans imports
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;


/**
 * This class represents the data for the schema panel wizard.
 * Read http://performance.netbeans.org/howto/dialogs/wizard-panels.html.
 * 
 * @author  Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaAdditionalInfoPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private SchemaAdditionalInfoGUI gui;
    private TemplateWizard templateWizard;

    /**
     * Empty constructor.
     */
    SchemaAdditionalInfoPanel() {
        super();
    }
    
    /**
     * Returns the template wizard.
     */
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    } 

    /**
     * Returns the GUI associated with this WizardDescriptor.
     * This is where, the gui panel gets created.
     */
    public Component getComponent() {
        if (gui == null) {
            gui = new SchemaAdditionalInfoGUI();
        }
        return gui;
    }

    /**
     * Returns the help context.
     */
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * If true, enables the FINISH button, else not.
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Allows addition of listeners.
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * Allows deletion of listeners.
     */
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    /**
     *
     */
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    /**
     *
     */
    public void readSettings( Object settings ) {
        templateWizard = (TemplateWizard)settings;
	gui.attachListenerToFileName(templateWizard);
    }
    
    /**
     *
     */
    public void storeSettings(Object settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }
    
    /**
     *
     */
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }        
    
}
