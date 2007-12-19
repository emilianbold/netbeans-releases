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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.rt.ui;

import java.awt.Component;

import javax.swing.JComponent;
import org.netbeans.modules.php.rt.providers.impl.AbstractPanel;
import org.netbeans.modules.php.rt.spi.providers.UiConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class ServerTypeChooserPanel extends AbstractPanel  implements FinishablePanel {

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getComponent()
     */
    public Component getComponent() {
        if (myComponent == null) {
            myComponent = new ServerTypeChooserVisual(this);

            initComponent();
        }
        return myComponent;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#isValid()
     */
    public boolean isValid() {
        if ( getVisual() != null ) {
            return getVisual().isContentValid();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#readSettings(java.lang.Object)
     */
    public void readSettings( Object settings ) {
        myWizard = (AddHostWizard)settings;
        if ( getVisual() != null ) {
            getVisual().read( myWizard );
        }
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#storeSettings(java.lang.Object)
     */
    public void storeSettings( Object settings ) {
        getVisual().store((AddHostWizard)settings);
    }

    public boolean isFinishPanel() {
        return true;
    }

    AddHostWizard getWizard(){
        return myWizard;
    }

    private ServerTypeChooserVisual getVisual(){
        return myComponent;
    }
    
    
    private void initComponent() {
        String[] initialSteps = getWizard().getInitialSteps();
        String[] steps = new String[initialSteps.length + 1];
        System.arraycopy(initialSteps, 0, steps, 0, initialSteps.length);
        steps[initialSteps.length] = AddHostWizard.ELIPSIS;
        getVisual().putClientProperty( AddHostWizard.PROP_CONTENT_DATA, steps);

        getVisual().putClientProperty(AddHostWizard.SELECTED_INDEX, 0);
        
        /*
         * Provide a name of the step in the title bar.
         */
        getVisual().setName(NbBundle.getBundle( ServerTypeChooserPanel.class).
                getString( AddHostWizard.LBL_CHOOSE_SERVER ));
        
        readSettings(getWizard());
        
        getVisual().updateWizardSteps();
    }
    
    void updateWizard(WebServerProvider provider){
        if (provider == null){
            return;
        }
        
        String[] initialSteps = getWizard().getInitialSteps();
        String[] providerSteps = getProviderSteps(provider);
        
        String[] steps = new String[initialSteps.length + providerSteps.length];
        
        System.arraycopy(initialSteps, 0, steps, 0, initialSteps.length);
        System.arraycopy(providerSteps, 0, steps, initialSteps.length, providerSteps.length);
        
        getVisual().putClientProperty( AddHostWizard.PROP_CONTENT_DATA, steps);
        updateProviderStepsInfo(provider, steps);
    }
    
    private void updateProviderStepsInfo(WebServerProvider provider, 
            String[] steps)
    {
        if (steps.length == 0 ){
            return;
        }
        
        UiConfigProvider uiProvider = provider.getConfigProvider();
        Panel[] panels = uiProvider.getPanels();

        int startIndex = getWizard().getInitialSteps().length;
        for (int i = 0; i < panels.length; i++) {
            Component component = panels[i].getComponent();
            if (component instanceof JComponent) {
                JComponent jComp = (JComponent) component;
                int stepIndex = i+startIndex;
                jComp.putClientProperty(AddHostWizard.SELECTED_INDEX, stepIndex);
                jComp.putClientProperty(AddHostWizard.PROP_CONTENT_DATA, steps);
            }
        }
    }
    
    private String[] getProviderSteps(WebServerProvider provider){
        String[] names = null;
        UiConfigProvider uiProvider = provider.getConfigProvider();
        assert uiProvider != null;
        Panel[] panels = uiProvider.getPanels();
        
        if (panels == null){
            names = new String[]{};
        } else {
            names = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component component = panels[i].getComponent();
                names[i] = component.getName();
            }
        }
        return names;
    }
    
    private ServerTypeChooserVisual  myComponent;
    
    private AddHostWizard myWizard;

}
