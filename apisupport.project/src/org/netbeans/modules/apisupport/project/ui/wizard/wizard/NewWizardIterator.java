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

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Wizard for creating new Wizards.
 *
 * @author Martin Krauskopf
 */
final class NewWizardIterator extends BasicWizardIterator {
    
    private static final long serialVersionUID = 1L;
    private DataModel data;
    
    private NewWizardIterator() {}
    
    public static NewWizardIterator createIterator() {
        return new NewWizardIterator();
    }
    
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        String[] paths = cmf.getCreatedPaths();
        Set set = new HashSet();
        for (int i = 0; i < paths.length; i++) {
            FileObject fo = data.getProject().getProjectDirectory().getFileObject(paths[i]);
            set.add(fo);
        }
        return set;
    }
    
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new WizardTypePanel(wiz, data),
            new NameIconLocationPanel(wiz, data)
        };
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
}
