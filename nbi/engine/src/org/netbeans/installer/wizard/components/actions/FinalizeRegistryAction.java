/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.components.actions;

import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Kirill Sorokin
 */
public class FinalizeRegistryAction extends WizardAction {
    public FinalizeRegistryAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        setProperty(REGISTRY_FINALIZATION_FAILED_PROPERTY,
                DEFAULT_REGISTRY_FINALIZATION_FAILED_MESSAGE);
    }
    
    public void execute() {
        try {
            Registry.getInstance().finalizeRegistry(new Progress());
        } catch (FinalizationException e) {
            ErrorManager.notifyError(StringUtils.format(
                    getProperty(REGISTRY_FINALIZATION_FAILED_PROPERTY)), e);
        }
    }
    
    public WizardActionUi getWizardUi() {
        return null; // this action does not have a ui
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            FinalizeRegistryAction.class,
            "FRA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            FinalizeRegistryAction.class,
            "FRA.description"); // NOI18N
    public static final String DEFAULT_REGISTRY_FINALIZATION_FAILED_MESSAGE =
            ResourceUtils.getString(
            FinalizeRegistryAction.class,
            "FRA.registry.finalization.failed"); // NOI18N
    public static final String REGISTRY_FINALIZATION_FAILED_PROPERTY =
            "registry.finalization.failed"; // NOI18N
}
