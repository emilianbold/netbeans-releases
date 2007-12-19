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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl;

import org.netbeans.modules.php.rt.providers.impl.local.*;
import org.netbeans.modules.php.rt.ui.AddHostWizard;
import org.openide.WizardValidationException;

/**
 *
 * @author avk
 */
public abstract class WebServerHttpWizardComponent extends WebServerHttpPanelVisual {

    public WebServerHttpWizardComponent( WebServerHttpWizardPanel panel ) {
        super();
        myPanel = panel;

        // used to show already defined values
        if (panel.getWizard() != null){
            read(panel.getWizard());
        }

        initListeners();
    }

    protected abstract void read( AddHostWizard wizard );
    
    protected abstract void store( AddHostWizard wizard );

    public void stateChanged() {
        getPanel().stateChanged();
    }

    boolean isContentValid() {
        setErrorMessage("");
        return super.doContentValidation();
    }
    
    void doFinalValidation() throws WizardValidationException {
        super.doFinalContentValidation();
    }
    
    protected void setErrorMessage(String message) {
        if (getWizard() == null) {
            return;
        }
        getWizard().putProperty(WizardConstants.WIZARD_PANEL_ERROR_MESSAGE, message);
    }

    
    protected AddHostWizard getWizard() {
        return getPanel().getWizard();
    }


    private WebServerHttpWizardPanel getPanel() {
        return myPanel;
    }
    

    private WebServerHttpWizardPanel myPanel;

}
