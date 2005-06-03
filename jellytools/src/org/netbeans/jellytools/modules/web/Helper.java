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

package org.netbeans.jellytools.modules.web;

import org.netbeans.jellytools.Bundle;

/** Helper class for this package.
 *
 * @author Martin.Schovanek@sun.com
 */
public class Helper {
    
    /** Avoid to create instance, contains only static helper method */
    private Helper() {
    }
    
    
    /** Returns the "New Web Application with Existing Ant Script" wizard title.
     */
    public static String freeFormWizardTitle() {
        String newLbl = Bundle.getStringTrimmed(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewProjectWizard_Subtitle");
        String webAppLbl = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.freeform.ui.Bundle",
                "TXT_NewWebFreeformProjectWizardIterator_NewProjectWizardTitle");
        return Bundle.getStringTrimmed(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewProjectWizard_MessageFormat",
                new Object[] {newLbl, webAppLbl});
    }
}
