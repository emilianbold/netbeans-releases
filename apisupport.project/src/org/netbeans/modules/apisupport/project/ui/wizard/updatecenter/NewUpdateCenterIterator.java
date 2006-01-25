/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.updatecenter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;

/**
 * Wizard for creating new update catalog.
 *
 * @author Jiri Rechtacek
 */
final class NewUpdateCenterIterator extends BasicWizardIterator {
    
    private static final long serialVersionUID = 1L;
    private DataModel data;
    
    public static NewUpdateCenterIterator createIterator() {
        return new NewUpdateCenterIterator();
    }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.refreshCreatedModifiedFiles();
        cmf.run();
        Set result = Collections.EMPTY_SET;
        if (cmf.getCreatedPaths ().length > 0) {
            result = getCreatedFiles(cmf, data.getProject());
        }
        return result;
    }

    protected BasicWizardIterator.Panel[] createPanels (WizardDescriptor wiz) {
        data = new DataModel (wiz);
        return new BasicWizardIterator.Panel[] {
            new UpdateCenterRegistrationPanel (wiz, data)
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
        
}
