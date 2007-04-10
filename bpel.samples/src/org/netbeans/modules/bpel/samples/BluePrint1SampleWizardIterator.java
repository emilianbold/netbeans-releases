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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.samples;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class BluePrint1SampleWizardIterator extends BluePrintSampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public BluePrint1SampleWizardIterator() {}
    
    public static BluePrintSampleWizardIterator createIterator() {
        return new BluePrint1SampleWizardIterator();
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(BluePrintSampleWizardIterator.class, "MSG_CreateBPELBluePrint1Project"),
        };
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new BluePrintSampleWizardPanel(BluePrintSampleWizardIterator.BLUE_PRINT1), 
        };
    }
    
     public String getCompositeApplicationArchiveName() {
         return BluePrintSampleWizardIterator.BLUE_PRINT1_COMP_APP;
     }

     public String getCompositeApplicationName() {
         return BluePrintSampleWizardIterator.BLUE_PRINT1_APP;
     }
}

