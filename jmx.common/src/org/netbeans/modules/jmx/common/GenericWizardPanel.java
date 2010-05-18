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
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 *
 * Class handling the standard Agent wizard panel
 *
 */
public abstract class GenericWizardPanel
        implements WizardDescriptor.Panel {

    /**
     * Returns the panel component.
     * @return <CODE>Component</CODE> the panel component.
     */
    public abstract Component getComponent() ;
    
    /**
     * Returns the corresponding help context.
     * @return <CODE>HelpCtx</CODE> the corresponding help context.
     */
    public HelpCtx getHelp() {
        return null;
    }
    
    /**
     * Returns if the user is able to go to next step and to finish the wizard.
     * @return <CODE>boolean</CODE> true only if the user can go to next step 
     * and finish the wizard.
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * Called to read information from the wizard map in order to populate
     * the GUI correctly.
     * @param settings <CODE>Object</CODE> an object containing 
     * the wizard informations.
     */
    public void readSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
    }
    
    /**
     * Called to store information from the GUI into the wizard map.
     * @param settings <CODE>Object</CODE> an object containing 
     * the wizard informations.
     */
    public void storeSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
    }
    
    private final Set listeners = new HashSet(1); // Set<ChangeListener>
    
    /** Add a listener to changes of the panel's validity.
     * @param l <CODE>ChangeListener</CODE> the listener to add
     */
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
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
} 

