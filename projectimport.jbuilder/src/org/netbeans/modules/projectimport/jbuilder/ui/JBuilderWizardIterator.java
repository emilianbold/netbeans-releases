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

package org.netbeans.modules.projectimport.jbuilder.ui;

import java.io.IOException;
import java.util.Set;
import org.netbeans.modules.projectimport.j2seimport.ui.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class JBuilderWizardIterator extends BasicWizardIterator {    
    public static JBuilderWizardIterator createIterator() {
        return new JBuilderWizardIterator();
    }
            
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {            
        return new BasicWizardIterator.Panel[] {new JBWizardPanel(wiz, (JBWizardData)data)};
        
    }
    
    protected String getTitle() {
        return NbBundle.getMessage(JBuilderWizardIterator.class, "CTL_WizardTitle"); // NOI18N                
    }
}
