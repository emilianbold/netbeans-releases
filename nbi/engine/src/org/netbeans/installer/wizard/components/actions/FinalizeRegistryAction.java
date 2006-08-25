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
 *  
 * $Id$
 */
package org.netbeans.installer.wizard.components.actions;

import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.panels.ProgressPanel;

/**
 *
 * @author Kirill Sorokin
 */
public class FinalizeRegistryAction extends DefaultWizardAction {
    private ProgressPanel panel = new ProgressPanel();
    
    public void execute() {
        try {
            ProductRegistry.getInstance().finalizeRegistry(new Progress(panel));
        } catch (FinalizationException e) {
            ErrorManager.getInstance().notify(ErrorLevel.ERROR, "Cannot finalize registry", e);
        }
    }
    
    public WizardPanel getUI() {
        return panel;
    }
}
