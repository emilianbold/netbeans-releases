/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.wizards;

import javax.swing.event.ChangeListener;
import javax.swing.*;

/**
 * UI that allows to configure selected root
 * and manage preconfigured roots pool.
 *
 * @author Petr Kuzel
 */
public final class RootWizard {

    private final RepositoryStep repositoryStep;

    private RootWizard(RepositoryStep step) {
        this.repositoryStep = step;
    }

    /**
     * Creates root configuration wizard with UI
     * that allows to set password, proxy, external
     * command, etc. (depends on root type).
     *
     * @return RootWizard
     */
    public static RootWizard configureRoot(String root) {
        RepositoryStep step = new RepositoryStep(root);
        step.applyStandaloneLayout();

        return new RootWizard(step);
    }

    /**
     * Gets UI panel representing RootWizard.
     */
    public JPanel getPanel() {
        RepositoryPanel repositoryPanel = (RepositoryPanel) repositoryStep.getComponent();
        return repositoryPanel;
    }

    /**
     * Propagates configuration changes (after heavy validation check)
     * from UI into {@link org.netbeans.modules.versioning.system.cvss.settings.CvsRootSettings}.
     *
     * @return <code>null</codE> on successfull commit otherwise error message.
     */
    public String commit() {
        repositoryStep.prepareValidation();
        repositoryStep.validateBeforeNext();
        if (repositoryStep.isValid()) {
            repositoryStep.storeValidValues();
            return null;
        }
        return repositoryStep.getErrorMessage();
    }

    /** Return result of light-weight validation.*/
    public boolean isValid() {
        return repositoryStep.isValid();
    }

    /** Allows to listen on valid. */
    public void addChangeListener(ChangeListener l) {
        repositoryStep.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        repositoryStep.removeChangeListener(l);
    }
}
