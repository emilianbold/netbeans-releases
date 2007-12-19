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
package org.netbeans.modules.php.rt.providers.impl;

import java.awt.Component;

import org.netbeans.modules.php.rt.ui.AddHostWizard;
import org.openide.WizardValidationException;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.ValidatingPanel;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public abstract class WebServerHttpWizardPanel extends AbstractPanel 
        implements FinishablePanel, ValidatingPanel
{
    
    private static final String CONFIG_HTTP_SERVER     
                = "LBL_ConfigHttpServer";               // NOI18N

    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.FinishablePanel#isFinishPanel()
     */
    public boolean isFinishPanel() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getComponent()
     */
    public Component getComponent() {
        // TODO init only once
        initComponent();
        return getVisual();
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#isValid()
     */
    public boolean isValid() {
        if ( getVisual() == null ) {
            return false;
        }
        return getVisual().isContentValid( );
    }
    
    public void validate() throws WizardValidationException {
        if ( getVisual() == null ) {
            return;
        }
        getVisual().doFinalValidation();
    }


    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#readSettings(java.lang.Object)
     */
    public void readSettings( Object settings ) {
        AddHostWizard wizard = (AddHostWizard)settings;
        myWizard = wizard;
        
        if ( getVisual() != null ) {
            getVisual().read( wizard );
        }
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#storeSettings(java.lang.Object)
     */
    public void storeSettings( Object settings ) {
        AddHostWizard wizard = (AddHostWizard)settings;

        getVisual().store( wizard );
    }
    
    AddHostWizard getWizard(){
        return myWizard;
    }
    
    protected void uninitialize() {
        /*
         * TODO : there can be better way to uninit : we can put 
         * myVisual into some Reference ( WeakReference or PhantomReference )
         * and set to null myVisual. In the method getComponent() one
         * can check myVisual, reference variable and in the case 
         * thet both are null instantiate new Component.
         * But in this case we need reset old values that are used 
         * in Visual component ( via setDefaults() ) each time when 
         * read() method is called with empty Host property.
         */
        myWizard = null;
    }
    
    protected abstract WebServerHttpWizardComponent getVisual();

    private void initComponent() {
        /*
         * Provide a name of the step in the title bar.
         */
        getVisual().setName(NbBundle.getBundle( WebServerHttpWizardPanel.class).
                getString( CONFIG_HTTP_SERVER  ));
    }
    
    private AddHostWizard myWizard;
    
    
}
