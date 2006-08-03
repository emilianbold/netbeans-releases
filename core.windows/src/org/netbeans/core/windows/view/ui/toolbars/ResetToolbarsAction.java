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
