/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
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
