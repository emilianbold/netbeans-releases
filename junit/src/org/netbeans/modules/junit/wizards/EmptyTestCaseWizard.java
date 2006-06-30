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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import org.netbeans.modules.junit.GuiUtils;
import org.netbeans.modules.junit.JUnitSettings;
import org.openide.loaders.TemplateWizard;

/**
 * Wizard for an empty test case.
 *
 * @author  Marian Petras
 */
public class EmptyTestCaseWizard extends TemplateWizard {

    /** name of property &quot;package&quot; */
    static final String PROP_PACKAGE = "package";                       //NOI18N
    /** name of property &quot;class name&quot; */
    static final String PROP_CLASS_NAME = "className";                  //NOI18N
    
    // PENDING - should not be hard-coded:
    static final String TESTS_ROOT_NAME = "test";               //NOI18N
    
    /**
     * initializes the settings for the settings panel
     */
    public void initialize() {
        JUnitSettings settings = JUnitSettings.getDefault();
        
        putProperty(GuiUtils.CHK_SETUP,
                    Boolean.valueOf(settings.isGenerateSetUp()));
        putProperty(GuiUtils.CHK_TEARDOWN,
                    Boolean.valueOf(settings.isGenerateTearDown()));
        putProperty(GuiUtils.CHK_HINTS,
                    Boolean.valueOf(settings.isBodyComments()));
    }
    
}
