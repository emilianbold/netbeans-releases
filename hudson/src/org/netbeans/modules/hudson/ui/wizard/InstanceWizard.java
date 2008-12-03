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

package org.netbeans.modules.hudson.ui.wizard;

import java.text.MessageFormat;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Hudson wizard descriptor
 * 
 * @author Michal Mocnak
 */
public class InstanceWizard extends WizardDescriptor
{
    
    public InstanceWizard() {
        setPanelsAndSettings(new InstanceWizardIterator(this), this);
        
        putProperty(PROP_AUTO_WIZARD_STYLE, true);
        putProperty(PROP_CONTENT_DISPLAYED, true);
        putProperty(PROP_CONTENT_NUMBERED, true);
        
        setTitle(NbBundle.getMessage(InstanceWizard.class, "LBL_InstanceWiz_Title"));
        setTitleFormat(new MessageFormat(NbBundle.getMessage(InstanceWizard.class, "LBL_InstanceWiz_TitleFormat")));
    }

    String name, url, sync;
    
    /**
     * Sets error message what should be displayed
     *
     * @param message
     */
    void setErrorMessage(String message) {
        putProperty(PROP_ERROR_MESSAGE, message);
    }
}