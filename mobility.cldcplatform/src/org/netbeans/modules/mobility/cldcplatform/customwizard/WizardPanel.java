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

package org.netbeans.modules.mobility.cldcplatform.customwizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public class WizardPanel implements WizardDescriptor.FinishablePanel {
    
    final private ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    final private ComponentDescriptor componentDescriptor;
    private WizardDescriptor wizardDescriptor;
    
    public WizardPanel(ComponentDescriptor componentDescriptor) {
        this.componentDescriptor = componentDescriptor;
        componentDescriptor.setWizardPanel(this);
    }
    
    public boolean isFinishPanel() {
        return componentDescriptor.isFinishPanel();
    }
    
    public Component getComponent() {
        return componentDescriptor.getComponent();
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(getComponent().getClass());
    }
    
    public void readSettings(final Object object) {
        wizardDescriptor = (WizardDescriptor) object;
        componentDescriptor.readSettings(wizardDescriptor);
    }
    
    public void storeSettings(final Object object) {
        wizardDescriptor = (WizardDescriptor) object;
        componentDescriptor.storeSettings(wizardDescriptor);
    }
    
    public boolean isValid() {
        return componentDescriptor.isPanelValid();
    }
    
    public void addChangeListener(final ChangeListener changeListener) {
        listeners.add(changeListener);
    }
    
    public void removeChangeListener(final ChangeListener changeListener) {
        listeners.remove(changeListener);
    }
    
    public void fireChanged() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( final ChangeListener l : listeners ) {
            l.stateChanged(e);
        }
    }
    
    public void setErrorMessage(final Class clazz, final String message) {
        if (wizardDescriptor != null)
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message != null ? NbBundle.getMessage(clazz, message) : null); // NOI18N
    }
    
    public Object getProperty(final String property) {
        return wizardDescriptor.getProperty(property);
    }
    
    public void putProperty(final String property, final Object value) {
        wizardDescriptor.putProperty(property, value);
    }
    
    public interface ComponentDescriptor {
        
        public void setWizardPanel(WizardPanel wizardPanel);
        
        public void readSettings(WizardDescriptor wizardDescriptor);
        
        public void storeSettings(WizardDescriptor wizardDescriptor);
        
        public JComponent getComponent();
        
        public boolean isPanelValid();
        
        public boolean isFinishPanel();
        
    }
    
}
