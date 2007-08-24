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

package org.netbeans.modules.mercurial.ui.status;

import org.netbeans.modules.mercurial.util.*;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

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
        super(NbBundle.getBundle(OpenInEditorAction.class).getString("CTL_Synchronize_Popup_OpenInEditor")); // NOI18N
        setEnabled(isActionEnabled());
    }

    private boolean isActionEnabled() {
        for (File file : HgUtils.getCurrentContext(null).getRootFiles()) {
            if (file.canRead()) return true;
        }
        return false;
    }

    public void actionPerformed(ActionEvent e) {
        for (File file : HgUtils.getCurrentContext(null).getRootFiles()) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                try {
                    openDataObjectByCookie(DataObject.find(fo));
                } catch (DataObjectNotFoundException ex) {
                    // ignore this error and try to open other files too
                }
            }
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
        if ((cookieClass == EditorCookie.class)
                || (cookieClass == EditorCookie.Observable.class)) {
            ((EditorCookie) cookie).open();
        } else if (cookieClass == OpenCookie.class) {
            ((OpenCookie) cookie).open();
        } else if (cookieClass == EditCookie.class) {
            ((EditCookie) cookie).edit();
        } else if (cookieClass == ViewCookie.class) {
            ((ViewCookie) cookie).view();
        } else {
            throw new IllegalArgumentException("Reopen #58766: " + cookieClass); // NOI18N
        }
        return true;
    }
}
