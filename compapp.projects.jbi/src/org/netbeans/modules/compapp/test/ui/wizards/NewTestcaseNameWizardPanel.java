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
package org.netbeans.modules.compapp.test.ui.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class NewTestcaseNameWizardPanel implements WizardDescriptor.ValidatingPanel, NewTestcaseConstants {
    private static final Logger mLog =
            Logger.getLogger("org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseNameWizardPanel"); // NOI18N
    
    /**
     * The visual mComponent that displays this panel. If you need to access the
     * mComponent from this class, just use getComponent().
     */
    private NewTestcaseNameVisualPanel mComponent;
    protected FileObject mTestDir;
    private String mTestcaseName;
    private WizardDescriptor wiz;
    
    public NewTestcaseNameWizardPanel(FileObject testDir) {
        mTestDir = testDir;
    }
    
    // Get the visual mComponent for the panel. In this template, the mComponent
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (mComponent == null) {
            mComponent = new NewTestcaseNameVisualPanel(this);
            String s = NbBundle.getMessage(NewTestcaseNameWizardPanel.class, "LBL_New_Testcase_Name"); // NOI18N
            String name = s;
            for (int idx = 1; true; idx++) {
                name = s + idx;
                if (mTestDir.getFileObject(name) == null) {
                    break;
                }
            }
            mTestcaseName = name;
            mComponent.getTestcaseNameTf().setText(mTestcaseName);
        }
        return mComponent;
    }
    
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
       // getComponent();

        
        return mComponent.valid(wiz);        
        // If it depends on some condition (form filled out...), then:
        // return someCondition();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent();
        // and uncomment the complicated stuff below.
    }
    /*
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
     **/

    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
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
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
        String s = (String)wiz.getProperty(TESTCASE_NAME);
        if (s != null) {
            mTestcaseName = s;
        }
    }
    public void storeSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
        wiz.putProperty(TESTCASE_NAME, mComponent.getTestcaseName());
        mLog.info(
                "Writing Testcase name to WizardDescriptor: "  // NOI18N
                + mComponent.getTestcaseName());
    }
    
    /**
     * Is called when Next of Finish buttons are clicked and
     * allows deeper check to find out that panel is in valid
     * state and it is ok to leave it.
     * 
     * This Panel is valid if
     * 1. mComponent.getTestcaseName() is not ""
     * 2. no existing testcase under Test node has name: mComponent.getTestcaseName()
     * 
     * 
     * @since 4.28
     * @throws WizardValidationException when validation fails
     */
    public void validate() throws WizardValidationException {
        // 1. mComponent.getSchemaName() is not ""
        String testcaseName = mComponent.getTestcaseName();
        if (testcaseName == null || testcaseName.trim().equals("")) { // NOI18N
            String msg = NbBundle.getMessage(NewTestcaseNameWizardPanel.class,
                    "LBL_Testcase_name_cannot_be_empty"); // NOI18N
            throw new WizardValidationException(mComponent.getTestcaseNameTf(), msg, msg);
        }
        
        // 2. no existing testcase under Test node has name: mComponent.getTestcaseName()
        if (mTestDir.getFileObject(testcaseName) != null) {
            String msg = NbBundle.getMessage(NewTestcaseNameWizardPanel.class, 
                    "LBL_Name_is_already_used_by_another_testcase"); // NOI18N
            throw new WizardValidationException(mComponent.getTestcaseNameTf(), msg, msg);
        }
    }
    
    private boolean isEmptyName() {
        if (mTestcaseName.trim().length() == 0) {
            return false;
        }
        return true;
    }
}