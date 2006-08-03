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

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import javax.swing.AbstractAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public class ResetToolbarsAction extends AbstractAction {
    
    /** Creates a new instance of ResetToolbarsAction */
    public ResetToolbarsAction() {
        super( NbBundle.getMessage(ResetToolbarsAction.class, "CTL_ResetToolbarsAction") ); //NOI18N
    }

    public void actionPerformed(ActionEvent e) {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = fs.findResource( "Toolbars" ); //NOI18N
        Object attr = fo.getAttribute( "removeWritables" ); //NOI18N
        if( null != attr && attr instanceof Callable ) {
            try {
                ((Callable)attr).call();
            } catch (Exception ex) {
                //TODO handle exception
                ex.printStackTrace();
            }
        }
    }
    
}
