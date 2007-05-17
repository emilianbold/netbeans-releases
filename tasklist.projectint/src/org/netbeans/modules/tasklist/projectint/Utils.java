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

package org.netbeans.modules.tasklist.projectint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Utilities
 * 
 * @author S. Aubrecht
 */
class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }

    /**
     * Find files opened in editor so that they can be scanned first to improve user-perceived performance.
     */
    static Collection<FileObject> collectEditedFiles() {
        Collection<TopComponent> comps = new ArrayList<TopComponent>( TopComponent.getRegistry().getOpened() );
        
        HashSet<FileObject> collectedFiles = new HashSet<FileObject>( comps.size() );
        
        for( final TopComponent tc : comps ) {
            if( WindowManager.getDefault().isOpenedEditorTopComponent( tc ) ) {
                DataObject dob = tc.getLookup().lookup( DataObject.class );
                if( null != dob ) {
                    FileObject fo = dob.getPrimaryFile();
                    if( null != fo ) {
                        collectedFiles.add( fo );
                    }
                }
            }
        }
        return collectedFiles;
    }
}
