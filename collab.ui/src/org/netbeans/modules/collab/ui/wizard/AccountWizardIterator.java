/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.wizard;

import java.awt.Dimension;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.event.*;

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
                new AccountTypePanel(), new AccountDisplayNamePanel(), new AccountServerPanel(),
                
                //			new AccountProxyPanel(),
                new AccountUserInfoPanel(), new AccountInfoPanel()
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
            tempPanel.add(newAccountPanels[i]);
            newAccountPanels[i].validate();

            tempPanel.doLayout();

            Dimension dimension = newAccountPanels[i].getSize();
            Dimension preferredDimension = newAccountPanels[i].getPreferredSize();
            maxWidth = Math.max(maxWidth, dimension.width);

            //			maxWidth=Math.max(maxWidth,preferredDimension.width);
            maxHeight = Math.max(maxHeight, dimension.height);

            //			maxHeight=Math.max(maxHeight,preferredDimension.height);
            tempPanel.remove(newAccountPanels[i]);
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
        currentPanels[index].setPreferredSize(settings.getPreferredPanelSize());
        currentPanels[index].putClientProperty("WizardPanel_contentSelectedIndex", new Integer(index)); // NOI18N
        currentPanels[index].putClientProperty("WizardPanel_contentData", getSteps()); // NOI18N
    }

    /**
     *
     *
     */
    public String[] getSteps() {
        if (index == 0) {
            return new String[] { currentPanels[0].getName(), "..." };
        }

        if (steps == null) {
            steps = new String[currentPanels.length];

            for (int i = 0; i < steps.length; i++)
                steps[i] = currentPanels[i].getName();
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
                if (((AccountTypePanel) currentPanels[0]).isExistingAccountSelected()) {
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
