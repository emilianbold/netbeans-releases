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

import org.openide.loaders.TemplateWizard;

/**
 *
 * @author  Marian Petras
 */
public class SimpleTestCaseWizard extends TemplateWizard {
    
    static final String PROP_CLASS_TO_TEST = "classToTest";     //NOI18N
    
    /** Creates a new instance of SimpleTestCaseWizard */
    public SimpleTestCaseWizard() {
    }
    
    /**
     * initializes the settings for the settings panel
     */
    protected void initialize() {
        //PENDING:
        /*
        JUnitSettings settings = JUnitSettings.getDefault();
        
        putProperty(GuiUtils.CHK_SETUP,
                    Boolean.valueOf(settings.isGenerateSetUp()));
        putProperty(GuiUtils.CHK_TEARDOWN,
                    Boolean.valueOf(settings.isGenerateTearDown()));
        putProperty(GuiUtils.CHK_HINTS,
                    Boolean.valueOf(settings.isBodyComments()));
        putProperty(PROP_TEMPLATE,
                    settings.getClassTemplate());
         */
    }
    
}
