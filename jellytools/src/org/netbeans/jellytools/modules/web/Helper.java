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
