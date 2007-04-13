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

import java.util.List;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;

/**
 * Task List framework callback.
 * 
 * @author S. Aubrecht
 */
public abstract class TaskManager {
    
    /**
     * 
     * @param scanner 
     * @param files 
     */
    public abstract void refresh( FileTaskScanner scanner, FileObject... files );
    
    /**
     * 
     * @param scanner 
     */
    public abstract void refresh( FileTaskScanner scanner );
    
    /**
     * 
     * @param scope 
     */
    public abstract void refresh( TaskScanningScope scope );
    
    /**
     * 
     * @param scanner 
     */
    public abstract void started( PushTaskScanner scanner );
    
    /**
     * 
     * @param scanner 
     */
    public abstract void finished( PushTaskScanner scanner );
    
    /**
     * 
     * @param scanner 
     * @param resource 
     * @param tasks 
     */
    public abstract void setTasks( PushTaskScanner scanner, FileObject resource, List<? extends Task> tasks );
    
    /**
     * 
     * @param scanner 
     */
    public abstract void clearAllTasks( PushTaskScanner scanner );
}
