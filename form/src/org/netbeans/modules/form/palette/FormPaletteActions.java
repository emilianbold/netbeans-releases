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

package org.netbeans.modules.form.palette;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.spi.palette.PaletteActions;

/**
 *
 * @author sa154850
 */
public class FormPaletteActions extends PaletteActions {
    
    /** Creates a new instance of FormPaletteProvider */
    public FormPaletteActions() {
    }

    public Action[] getImportActions() {
        
        Action[] res = new Action[3];
        
        res[0] = new AbstractAction( PaletteUtils.getBundleString("CTL_AddJAR_Button") ) { // NOI18N
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        BeanInstaller.installBeans(ClassSource.JAR_SOURCE);
                    }
                 };
        res[0].putValue( Action.LONG_DESCRIPTION, 
                 PaletteUtils.getBundleString("ACSD_AddJAR_Button") ); // NOI18N
        
        res[1] = new AbstractAction( PaletteUtils.getBundleString("CTL_AddLibrary_Button") ) { // NOI18N
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        BeanInstaller.installBeans(ClassSource.LIBRARY_SOURCE);
                    }
                };
        res[1].putValue( Action.LONG_DESCRIPTION, 
                 PaletteUtils.getBundleString("ACSD_AddLibrary_Button") ); // NOI18N
        
        res[2] = new AbstractAction( PaletteUtils.getBundleString("CTL_AddProject_Button") ) { // NOI18N
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        BeanInstaller.installBeans(ClassSource.PROJECT_SOURCE);
                    }
                };
        res[2].putValue( Action.LONG_DESCRIPTION, 
                 PaletteUtils.getBundleString("ACSD_AddProject_Button") ); // NOI18N
        
        return res;
    }

    public Action[] getCustomCategoryActions(org.openide.util.Lookup category) {
        return new Action[0]; //TODO implement this
    }

    public Action[] getCustomItemActions(org.openide.util.Lookup item) {
        return new Action[0]; //TODO implement this
    }

    public Action[] getCustomPaletteActions() {
        return new Action[0]; //TODO implement this
    }

    public Action getPreferredAction(org.openide.util.Lookup item) {
        return null; //TODO implement this
    }
}
