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
package org.netbeans.installer.wizard.components.panels;

import org.netbeans.installer.wizard.components.sequences.InstallSequence;

/**
 *
 * @author Kirill Sorokin
 */
public class PostInstallSummaryPanel extends TextPanel {
    public void initialize() {
        getBackButton().setEnabled(false);
        getCancelButton().setEnabled(false);
        
        String text = "We honestly hope that everything completed successfully as no actual checks are performed for the prototype. Sorry for any inconvenience caused.";
        
        boolean installSuccessful = new Boolean(System.getProperty(InstallSequence.LAST_INSTALLATION_ACTION_SUCCESSFUL_PROPERTY));
        
        if (!installSuccessful) {
            text += "\n\nSome errors were encountered during installation process. Installation was not completed.";
        }
        
        setProperty(TEXT_PROPERTY, text);
        
        super.initialize();
    }    
}
