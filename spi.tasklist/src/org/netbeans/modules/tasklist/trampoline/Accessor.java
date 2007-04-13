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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tasklist.trampoline;

import java.awt.Image;
import java.awt.event.ActionListener;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;


/**
 * API trampoline
 * 
 * @author S. Aubrecht
 */
public abstract class Accessor {
    
    public static Accessor DEFAULT;
    
    static {
        // invokes static initializer of Item.class
        // that will assign value to the DEFAULT field above
        Class c = Task.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            assert false : ex;
        }
//        assert DEFAULT != null : "The DEFAULT field must be initialized";
    }
    
    public abstract String getDescription( Task t );
    
    public abstract FileObject getResource( Task t );
    
    public abstract TaskGroup getGroup( Task t );
    
    public abstract int getLine( Task t );
    
    public abstract ActionListener getActionListener( Task t );
    
    
    public abstract String getDisplayName( TaskScanningScope scope );
    
    public abstract String getDescription( TaskScanningScope scope );
    
    public abstract Image getIcon( TaskScanningScope scope );
    
    public abstract boolean isDefault( TaskScanningScope scope );
    
    public abstract TaskScanningScope.Callback createCallback( TaskManager tm, TaskScanningScope scope );
    
    
    public abstract String getDisplayName( FileTaskScanner scanner );
    
    public abstract String getDescription( FileTaskScanner scanner );
    
    public abstract String getOptionsPath( FileTaskScanner scanner );
    
    public abstract FileTaskScanner.Callback createCallback( TaskManager tm, FileTaskScanner scanner );
    
    
    public abstract String getDisplayName( PushTaskScanner scanner );
    
    public abstract String getDescription( PushTaskScanner scanner );
    
    public abstract String getOptionsPath( PushTaskScanner scanner );
    
    public abstract PushTaskScanner.Callback createCallback( TaskManager tm, PushTaskScanner scanner );

    private static TaskScanningScope EMPTY_SCOPE = null;
    public static TaskScanningScope getEmptyScope() {
        if( null == EMPTY_SCOPE )
            EMPTY_SCOPE = new EmptyScanningScope();
        return EMPTY_SCOPE;
    }
}

