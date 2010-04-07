/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.wsdl.bindingsupport.spi;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Interface required to be implemented by custom wizard extension implementation.
 *
 * @author skini
 */
public abstract class WSDLWizardDescriptorPanel implements WizardDescriptor.FinishablePanel{

    private ChangeSupport changeSupport = new ChangeSupport(this);
    private WSDLWizardContext context;
    

    public WSDLWizardDescriptorPanel(WSDLWizardContext context) {
        this.context = context;
    }
    
    
    /**
     * Returns the WSDLWizardContext.
     * @return WSDLWizardContext object
     */ 
    public final WSDLWizardContext getWSDLWizardContext() {
        return context;
    }
    
    /**
     * Return name of the panel.
     * 
     * @return String
     */
    public abstract String getName();
    
    /**
     * Return the gui component needed to show in the wizard.
     * This method will be called multiple times, so cache the component.
     * 
     * @return Component
     */
    public abstract Component getComponent();

    /**
     * Return the help context, to show the help page for this panel.
     * 
     * @return HelpCtx
     */
    public abstract HelpCtx getHelp();
    
    /**
     * Controls the Next and Finish buttons on the wizard. 
     * Returning false, will disable both Next and Finish Buttons on the wizard.
     * 
     * @return true if Next and Finish needs to be enabled
     */
    public abstract boolean isValid();

    /**
     * If true, then the wizard is finishable at this step.
     * 
     * @return true if finishable
     */
    public abstract boolean isFinishPanel();

    /**
     * Passes the settings Object, in this case, its TemplateWizard object.
     * It can be used to retrieve values from the settings.
     * Called before the panel is about to be shown on the wizard.
     * 
     * @param settings
     */
    public abstract void readSettings(Object settings);

    /**
     * Called when Previous, Next or Finish is pressed, to store information in the TemplateWizard object.
     * 
     * @param settings
     */
    public abstract void storeSettings(Object settings);
    
    
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    /** 
     * Use to fireChange events, this refreshes all the GUI components in the current step.
     * 
     */
    public final void fireChange() {
        changeSupport.fireChange();
    }

}
