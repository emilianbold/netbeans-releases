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

package org.netbeans.modules.websvc.axis2.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * 
 */
public class WsFromJavaPanel0 implements  WizardDescriptor.FinishablePanel<WizardDescriptor>, WizardProperties {

    private WsFromJavaGUIPanel0 component;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    /** Creates a new instance of WebServiceType */
    public WsFromJavaPanel0(Project project, WizardDescriptor wizardDescriptor) {
        this.project = project;
        this.wizardDescriptor = wizardDescriptor;
    }

    public Component getComponent() {
        if (component == null) {
            component = new WsFromJavaGUIPanel0(this);
        }
        
        return component;
    }
    
    Project getProject() {
        return project;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(WsFromJavaPanel0.class);
    }

    public void readSettings(WizardDescriptor settings) {
    }

    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(PROP_FROM_JAVA_TYPE, Boolean.valueOf(component.isEmptyWebService()));
        if (component.getJavaClass() != null) {
            settings.putProperty(PROP_JAVA_CLASS, component.getJavaClass());
        }
        settings.putProperty(PROP_GENERATE_WSDL, Boolean.valueOf(component.generateWsdl()));
    }

    public boolean isValid() {
//        Preferences prefs = AxisUtils.getPreferences();
//        String axisHome = prefs.get("AXIS_HOME",null); //NOI18N
//        if (axisHome == null || axisHome.length() == 0) {
//            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(WsFromJavaPanel0.class, "MSG_NoAxisHome")); // NOI18N
//            return false;
//        } else {
//            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
//        }
        return component.dataIsValid();
    }
    
    boolean isFromScratch() {
        return component.isFromScratch();
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
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

    public boolean isFinishPanel() {
        return component.isFinishable();
    }

//    public void stateChanged(ChangeEvent e) {
//        fireChange();
//    }
    
    void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator<ChangeListener> it = listeners.iterator();
        while (it.hasNext()) {
            it.next().stateChanged(e);
        }
    }

}
