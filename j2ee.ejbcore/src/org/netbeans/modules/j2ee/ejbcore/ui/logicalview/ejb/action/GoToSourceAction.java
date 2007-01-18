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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
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

    private final String className;
    private final ClassPath classPath;
    private final String actionName;
    
    public GoToSourceAction(ClassPath classPath, String className, String actionName) {
        this.classPath = classPath;
        this.className = className;
        this.actionName = actionName;
    }

    public void actionPerformed(ActionEvent actionEvent) {
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
    private void openSourceFO(FileObject fileObject){
        if (fileObject == null) {
            return;
        }
        DataObject dob;
        try {
            dob = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            dob = null;
        }
        
        if (dob != null) {
            EditCookie editCookie = (EditCookie)dob.getCookie(EditCookie.class);
            if (editCookie != null) {
                editCookie.edit();
            } else {
                OpenCookie openCookie = (OpenCookie)dob.getCookie(OpenCookie.class);
                if (openCookie != null) {
                    openCookie.open();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

}