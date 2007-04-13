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

package org.netbeans.modules.tasklist.impl;

import java.awt.Image;
import java.awt.event.ActionListener;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;

/**
 *
 * @author S. Aubrecht
 */
public class Accessor {
    
    /** Creates a new instance of Accessor */
    private Accessor() {
    }
    
    public static FileObject getResource( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getResource( t );
    }
    
    public static String getDescription( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDescription( t );
    }
    
    public static TaskGroup getGroup( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getGroup( t );
    }
    
    public static int getLine( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getLine( t );
    }
    
    public static ActionListener getActionListener( Task t ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getActionListener( t );
    }
    
    
    
    public static String getDisplayName( TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDisplayName( scope );
    }
    
    public static String getDescription( TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDescription( scope );
    }
    
    public static Image getIcon( TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getIcon( scope );
    }
    
    public static boolean isDefault( TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.isDefault( scope );
    }
    
    public static TaskScanningScope.Callback createCallback( TaskManager tm, TaskScanningScope scope ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.createCallback( tm, scope );
    }
    
    public static TaskScanningScope getEmptyScope() {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getEmptyScope();
    }



    
    public static String getDisplayName( FileTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDisplayName( scanner );
    }
    
    public static String getDescription( FileTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDescription( scanner );
    }
    
    public static String getOptionsPath( FileTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getOptionsPath( scanner );
    }
    
    public static FileTaskScanner.Callback createCallback( TaskManager tm, FileTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.createCallback( tm, scanner );
    }


    
    public static String getDisplayName( PushTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDisplayName( scanner );
    }
    
    public static String getDescription( PushTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getDescription( scanner );
    }
    
    public static String getOptionsPath( PushTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.getOptionsPath( scanner );
    }
    
    public static PushTaskScanner.Callback createCallback( TaskManager tm, PushTaskScanner scanner ) {
        return org.netbeans.modules.tasklist.trampoline.Accessor.DEFAULT.createCallback( tm, scanner );
    }
}
