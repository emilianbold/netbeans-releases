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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

public class BluePrint4SampleWizardIterator extends BluePrintSampleWizardIterator {
    private static final long serialVersionUID = 1L;
    
    public BluePrint4SampleWizardIterator() {}
    
    public static BluePrintSampleWizardIterator createIterator() {
        return new BluePrint4SampleWizardIterator();
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(BluePrintSampleWizardIterator.class, "MSG_CreateBPELBluePrint4Project"),
        };
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new BluePrintSampleWizardPanel(BluePrintSampleWizardIterator.BLUE_PRINT4), 
        };
    }
    
     public String getCompositeApplicationArchiveName() {
         return BluePrintSampleWizardIterator.BLUE_PRINT4_COMP_APP;
     }
}
