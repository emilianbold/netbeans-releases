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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.common;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor.FinishablePanel;

/**
 *
 * Class used to become non finishable wizard panel in finishable wizard panel
 *
 */
public class FinishableDelegatedWizardPanel
        implements WizardDescriptor.Panel, FinishablePanel {

    /**
     * The wrapped panel
     */
    protected final WizardDescriptor.Panel delegate;
    /**
     * The panel which allows Finish button enabled.
     */
    protected final WizardDescriptor.Panel finishDelegate;
    
    /**
     * Construct a new instance.
     * @param delegate <CODE>WizardDescriptor.Panel</CODE> Panel which wants to be finishable.
     * @param finishDelegate <CODE>WizardDescriptor.Panel</CODE> Panel which realize the finish actions.
     */
    public FinishableDelegatedWizardPanel(WizardDescriptor.Panel delegate,
            GenericWizardPanel finishDelegate) {
        super();
        this.delegate = delegate;
        this.finishDelegate = finishDelegate;
    }
    
    /**
     * Returns if this panel is finishable.
     * @return <CODE>boolean</CODE> true only if this panel is finishable.
     */
    public boolean isFinishPanel() {
        return ((FinishablePanel) finishDelegate).isFinishPanel();
    }
    
    /**
     * Returns the panel component.
     * @return <CODE>Component</CODE> the panel component.
     */
    public Component getComponent() {
        return delegate.getComponent();
    };
    
    /**
     * Returns the corresponding help context.
     * @return <CODE>HelpCtx</CODE> the corresponding help context.
     */
    public HelpCtx getHelp() {
        return delegate.getHelp();
    }
    
    /**
     * Returns if the user is able to go to next step and to finish the wizard.
     * @return <CODE>boolean</CODE> true only if the user can go to next step 
     * and finish the wizard.
     */
    public boolean isValid() {
        return delegate.isValid();
    }
    
    /**
     * This method is called when a step is loaded.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
    public void readSettings(Object settings) {
        delegate.readSettings(settings);
        finishDelegate.storeSettings(settings);
        finishDelegate.readSettings(settings);
    }
    
    /**
     * This method is used to force the delegate panel to load wizard informations.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
    public void readAllSettings(Object settings) {
        delegate.readSettings(settings);
    }
    
    /**
     * This method is called when the user quit a step.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
    public void storeSettings(Object settings) {
        finishDelegate.storeSettings(settings);
    }
    
    /**
     * This method is used to force the delegate panel to store user informations 
     * into the wizard informations.
     * @param settings <CODE>Object</CODE> an object containing the wizard informations.
     */
    public void storeAllSettings(Object settings) {
        delegate.storeSettings(settings);
    }
    
    /** Add a listener to changes of the panel's validity.
      * @param l <CODE>ChangeListener</CODE> the listener to add
      */
    public final void addChangeListener(ChangeListener l) {
        delegate.addChangeListener(l);
    }
    
    /** Remove a listener to changes of the panel's validity.
      * @param l <CODE>ChangeListener</CODE> the listener to remove
      */
    public final void removeChangeListener(ChangeListener l) {
        delegate.removeChangeListener(l);
    }
} 

