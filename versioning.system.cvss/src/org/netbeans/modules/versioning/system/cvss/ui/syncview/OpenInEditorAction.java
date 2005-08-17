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

package org.netbeans.modules.versioning.system.cvss.ui.syncview;

import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.versioning.system.cvss.util.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Opens the file under {@link SyncFileNode} in editor.
 * 
 * @author Maros Sandor
 */
public class OpenInEditorAction extends AbstractAction {

    public OpenInEditorAction() {
        super(NbBundle.getBundle(OpenInEditorAction.class).getString("CTL_Synchronize_Popup_OpenInEditor"));
        setEnabled(isActionEnabled());
    }

    private boolean isActionEnabled() {
        File [] files = Utils.getCurrentContext().getFiles();
        return files.length == 1 && files[0].canRead();
    }

    public void actionPerformed(ActionEvent e) {
        File file = Utils.getCurrentContext().getFiles()[0];
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return;
        try {
            openDataObjectByCookie(DataObject.find(fo));
        } catch (DataObjectNotFoundException ex) {
        }
    }
    
    private final boolean openDataObjectByCookie(DataObject dataObject) {
        Node.Cookie cookie;
        Class cookieClass;
        if ((((cookie = dataObject.getCookie(cookieClass = EditorCookie.Observable.class)) != null
                            || (cookie = dataObject.getCookie(cookieClass = EditorCookie.class)) != null))
                || (cookie = dataObject.getCookie(cookieClass = OpenCookie.class)) != null
                || (cookie = dataObject.getCookie(cookieClass = EditCookie.class)) != null
                || (cookie = dataObject.getCookie(cookieClass = ViewCookie.class)) != null) {
            return openByCookie(cookie, cookieClass);
        }
        return false;
    }
    
    private boolean openByCookie(Node.Cookie cookie, Class cookieClass) {
        if ((cookieClass == EditorCookie.Observable.class)
                || (cookieClass == EditorCookie.Observable.class)) {
            ((EditorCookie) cookie).open();
        } else if (cookieClass == OpenCookie.class) {
            ((OpenCookie) cookie).open();
        } else if (cookieClass == EditCookie.class) {
            ((EditCookie) cookie).edit();
        } else if (cookieClass == ViewCookie.class) {
            ((ViewCookie) cookie).view();
        } else {
            throw new IllegalArgumentException();
        }
        return true;
    }
}
