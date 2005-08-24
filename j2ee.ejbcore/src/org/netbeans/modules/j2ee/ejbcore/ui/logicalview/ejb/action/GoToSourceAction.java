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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.awt.Toolkit;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin Adamek
 */
public class GoToSourceAction extends AbstractAction implements Presenter.Popup {

    private String className;
    private ClassPath classPath;
    private String actionName;
    
    public GoToSourceAction(ClassPath classPath, String className, String actionName) {
        this.classPath = classPath;
        this.className = className;
        this.actionName = actionName;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        openSourceFO(getFileObject());
    }

    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return actionName;
        }
        else {
            return super.getValue(key);
        }
    }

    public JMenuItem getPopupPresenter() {
        return new JMenuItem (this);
    }

    // private helpers =========================================================
    
    private FileObject getFileObject() {
        assert className != null: "cannot find null className"; //NOI18N
        return classPath.findResource(className.replace('.', '/') + ".java"); //NOI18N
    }
    
    /*
     * from NbJavaFastOpen
     */
    private void openSourceFO(FileObject fo){
        DataObject dob;
        try {
            dob = DataObject.find(fo);
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            dob = null;
        }
        
        if (dob != null) {
            EditCookie ec = (EditCookie)dob.getCookie(EditCookie.class);
            if (ec != null) {
                ec.edit();
            } else {
                OpenCookie oc = (OpenCookie)dob.getCookie(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

}