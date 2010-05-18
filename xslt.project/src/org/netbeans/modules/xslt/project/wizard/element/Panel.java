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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xslt.project.wizard.element;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.netbeans.api.project.Project;

/**
 * @author Vladimir Yaroslavskiy
 * @author Vitaly Bychkov
 * @version 2007.08.31
 */
abstract class Panel extends WizardSettingsPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    protected Panel(Project project, Panel parent) {
        super(project);
        myChangeSupport = new ChangeSupport(this);
        myParent = parent;
    }

    protected String getComponentName() {
        return null;
    }

    protected Panel getNext() {
        finishEditing();
        return null;
    }
    
    protected Panel getParent() {
        return myParent;
    }

    protected final Panel getPrevious() {
        finishEditing();
        return myParent;
    }

    /**
     * In some cases(e.g.: tables) it is required manually finish editing
     * 
     */
    protected void finishEditing() {
    }
    
    public JPanel getComponent() {
        if (myComponent == null) {
            myComponent = createMainPanel();
            String name = getComponentName();
            myComponent.setName(name);

            String[] steps = new String[]{NAME_TYPE, NAME_WSDL, NAME_XSLT};
            myComponent.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N

            for (int i = 0; i < steps.length; i++) {
                if (name.equals(steps[i])) {
                    myComponent.putClientProperty(
                            WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i - 1)); // NOI18N
                }
            }
        }
        return myComponent;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        createPanel(panel, c);

//  panel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.red));
        return panel;
    }

    public void validate() throws WizardValidationException {
        finishEditing();
        String error = getError();

        if (error == null) {
            error = getLongRunningError();
            setExistLongRunningError(error != null);
        }
        
        if (error != null) {
            throw new WizardValidationException(myComponent, error, error);
        }
    }
    
    protected String getLongRunningError() {
        return null;
    }

    public boolean isValid() {
        return true;
    }

    public HelpCtx getHelp() {
        return new HelpCtx("xslt_project_addxsl"); // NOI18N
    }

    public void addChangeListener(ChangeListener listener) {
        myChangeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        myChangeSupport.removeChangeListener(listener);
    }

    public void fireChange() {
        myChangeSupport.fireChange();
    }
    
    protected final boolean isExistLongRunningError() {
        return existLongRunningError;
    }
    
    protected final void setExistLongRunningError(boolean existError) {
        existLongRunningError = existError;
    }
    
    private boolean existLongRunningError = false;
    private ChangeSupport myChangeSupport;
    private JPanel myComponent;
    private Panel myParent;
}
