/*
 * FormPaletteProvider.java
 *
 * Created on 1. èerven 2005, 17:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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
        return new Action[] {
            new AbstractAction( PaletteUtils.getBundleString("CTL_AddJAR_Button") ) { // NOI18N
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    BeanInstaller.installBeans(ClassSource.JAR_SOURCE);
                }
            },
                    
            new AbstractAction( PaletteUtils.getBundleString("CTL_AddLibrary_Button") ) { // NOI18N
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    BeanInstaller.installBeans(ClassSource.LIBRARY_SOURCE);
                }
            },
                    
            new AbstractAction( PaletteUtils.getBundleString("CTL_AddProject_Button") ) { // NOI18N
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    BeanInstaller.installBeans(ClassSource.PROJECT_SOURCE);
                }
            }
        };
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
