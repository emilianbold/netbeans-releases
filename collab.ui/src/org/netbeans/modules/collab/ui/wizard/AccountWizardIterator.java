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
package org.netbeans.modules.collab.ui.wizard;

import java.awt.Dimension;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AccountWizardIterator extends Object implements WizardDescriptor.Iterator {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private AccountWizardSettings settings;
    private WizardPanelBase[] currentPanels;
    private WizardPanelBase[] newAccountPanels;
    private WizardPanelBase[] existingAccountPanels;
    private int index;
    private String[] steps;
    private Set changeListeners = new HashSet();

    /**
     *
     *
     */
    public AccountWizardIterator(AccountWizardSettings settings) {
        super();
        this.settings = settings;

        newAccountPanels = new WizardPanelBase[] {
                new AccountTypeWizardPanel(), new AccountDisplayNameWizardPanel(), new AccountServerWizardPanel(),
                
                //			new AccountProxyPanel(),
                new AccountUserInfoWizardPanel(), new AccountInfoWizardPanel()
            };

        existingAccountPanels = new WizardPanelBase[] {
                newAccountPanels[0], newAccountPanels[1], newAccountPanels[2],
                
                //			newAccountPanels[3],
                newAccountPanels[newAccountPanels.length - 1]
            };

        // Find the largest panel dimensions
        int maxWidth = 0;
        int maxHeight = 0;
        JPanel tempPanel = new JPanel();

        for (int i = 0; i < newAccountPanels.length; i++) {
            tempPanel.add(newAccountPanels[i].getComponent());
            newAccountPanels[i].getComponent().validate();

            tempPanel.doLayout();

            Dimension dimension = newAccountPanels[i].getComponent().getSize();
            Dimension preferredDimension = newAccountPanels[i].getComponent().getPreferredSize();
            maxWidth = Math.max(maxWidth, dimension.width);

            //			maxWidth=Math.max(maxWidth,preferredDimension.width);
            maxHeight = Math.max(maxHeight, dimension.height);

            //			maxHeight=Math.max(maxHeight,preferredDimension.height);
            tempPanel.remove(newAccountPanels[i].getComponent());
        }

        settings.setPreferredPanelSize(new Dimension(maxWidth, maxHeight));

        // Select the current panels
        currentPanels = settings.isNewAccount() ? newAccountPanels : existingAccountPanels;
    }

    /**
     *
     *
     */
    public AccountWizardSettings getSettings() {
        return settings;
    }

    /**
     *
     *
     */
    public String name() {
        if (index == 0) {
            return NbBundle.getMessage(
                AccountWizardIterator.class, "TITLE_x_of_unknown", // NOI18N
                new Integer(index + 1)
            );
        } else {
            return NbBundle.getMessage(
                AccountWizardIterator.class, "TITLE_x_of_y", // NOI18N
                new Integer(index + 1), new Integer(currentPanels.length)
            );
        }
    }

    /**
     *
     *
     */
    public WizardDescriptor.Panel current() {
        updateCurrentPanel();

        return currentPanels[index];
    }

    /**
     *
     *
     */
    public int getIndex() {
        return index;
    }

    /**
     *
     *
     */
    public void setIndex(int value) {
        index = value;
        fireStateChanged();
    }

    /**
     *
     *
     */
    protected void updateCurrentPanel() {
        currentPanels[index].getComponent().setPreferredSize(settings.getPreferredPanelSize());
        if (currentPanels[index].getComponent() instanceof JComponent) {
            JComponent c = (JComponent) currentPanels[index].getComponent();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(index)); // NOI18N
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getSteps()); // NOI18N
        }
    }

    /**
     *
     *
     */
    public String[] getSteps() {
        if (index == 0) {
            return new String[] { currentPanels[0].getComponent().getName(), "..." };
        }

        if (steps == null) {
            steps = new String[currentPanels.length];

            for (int i = 0; i < steps.length; i++)
                steps[i] = currentPanels[i].getComponent().getName();
        }

        return steps;
    }

    /**
     *
     *
     */
    public boolean hasNext() {
        return index < (currentPanels.length - 1);
    }

    /**
     *
     *
     */
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     *
     *
     */
    public void nextPanel() {
        if (++index < currentPanels.length) {
            if (index == 1) {
                // Check if account is new, switch panels
                if (((AccountTypePanel) currentPanels[0].getComponent()).isExistingAccountSelected()) {
                    currentPanels = existingAccountPanels;
                } else {
                    currentPanels = newAccountPanels;
                }

                // Clear the current steps
                steps = null;
            }

            fireStateChanged();
        } else {
            index--;
        }
    }

    /**
     *
     *
     */
    public void previousPanel() {
        if (--index < 0) {
            index = 0;
        }

        fireStateChanged();
    }

    /**
     *
     *
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     *
     *
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     *
     *
     */
    public void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);

        for (Iterator i = changeListeners.iterator(); i.hasNext();) {
            try {
                ((ChangeListener) i.next()).stateChanged(event);
            } catch (Exception e) {
                Debug.debugNotify(e);
            }
        }
    }
}
