/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.text.MessageFormat;
import org.netbeans.api.project.Project;
//import org.netbeans.spi.project.ui.templates.support.InstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;

import org.openide.loaders.TemplateWizard;

public final class NewProjectWizard extends TemplateWizard {
        
    private Project p;
    private FileObject templatesFO;

    public NewProjectWizard (FileObject fo) {
        this.templatesFO = fo;
        setTitle( "New Project" );
        setTitleFormat( new MessageFormat( "{0}") );
        initWizard ();
    }
    
    private void initWizard () {
        DataFolder templates = DataFolder.findFolder( templatesFO );
        setTemplatesFolder( templates ); 
    }
}
