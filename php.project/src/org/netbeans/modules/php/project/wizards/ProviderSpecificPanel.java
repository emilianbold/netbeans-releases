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
package org.netbeans.modules.php.project.wizards;

import java.awt.Component;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
final class ProviderSpecificPanel implements Panel, FinishablePanel {
    
    ProviderSpecificPanel() {
        init( );
    }
        
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener( ChangeListener listener ) {
        synchronized ( myListeners ) {
            myListeners.add(listener);
        }
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener( ChangeListener listener ) {
        synchronized ( myListeners ) {
            myListeners.remove(listener);
        }
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getComponent()
     */
    public Component getComponent() {
        return myComponent;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#getHelp()
     */
    public HelpCtx getHelp() {
        return new HelpCtx( ProviderSpecificPanel.class );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#isValid()
     */
    public boolean isValid() {
        return getVisual().dataIsValid( getDescriptor() );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#readSettings(java.lang.Object)
     */
    public void readSettings( Object settings ) {
        myDescriptor = (WizardDescriptor) settings;
        
        getVisual().read ( getDescriptor() );
        
        /*
         * Copied from Make project configuration panel
         */
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = getVisual().getClientProperty(
                PhpConfigureProjectVisual.NEW_PROJECT_WIZARD_TITLE);
        if (substitute != null) {
            getDescriptor().putProperty(
                    PhpConfigureProjectVisual.NEW_PROJECT_WIZARD_TITLE, substitute);
        }
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Panel#storeSettings(java.lang.Object)
     */
    public void storeSettings( Object settings ) {
        WizardDescriptor descriptor = (WizardDescriptor) settings;
        getVisual().store(descriptor);
        ((WizardDescriptor) descriptor).putProperty(
                PhpConfigureProjectVisual.NEW_PROJECT_WIZARD_TITLE, null); 
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.FinishablePanel#isFinishPanel()
     */
    public boolean isFinishPanel() {
        return true;
    }
    
    final void fireChangeEvent() {
        ChangeListener[] listeners;
        synchronized ( myListeners) {
            listeners = myListeners.toArray( 
                    new ChangeListener[ myListeners.size() ] );
        }
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
    
    private ProviderPanelVisual getVisual() {
        return myComponent;
    }
    
    private WizardDescriptor getDescriptor() {
        return myDescriptor;
    }
    
    private void init( ) {
        myComponent = new ProviderPanelVisual( this );
        
        /*String[] initialSteps = getDescriptor().getInitialSteps();
        String[] steps = new String[initialSteps.length + 1];
        System.arraycopy(initialSteps, 0, steps, 0, initialSteps.length);
        steps[initialSteps.length] = AddHostWizard.ELIPSIS;
        getVisual().putClientProperty( AddHostWizard.PROP_CONTENT_DATA, steps);

        getVisual().putClientProperty(AddHostWizard.SELECTED_INDEX, 0);*/
        
        /*
         * Provide a name of the step in the title bar.
         */
        getVisual().setName(NbBundle.getBundle( NewPhpProjectWizardIterator.class).
                getString( NewPhpProjectWizardIterator.STEP_WEB_SERVER));
    }


    private ProviderPanelVisual myComponent;
    
    private final Collection<ChangeListener> myListeners = 
        new LinkedList<ChangeListener>();
    
    private WizardDescriptor myDescriptor;
    
}
