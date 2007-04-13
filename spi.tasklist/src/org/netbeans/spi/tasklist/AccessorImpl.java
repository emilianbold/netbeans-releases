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

package org.netbeans.spi.tasklist;

import java.awt.Image;
import java.awt.event.ActionListener;
import org.netbeans.modules.tasklist.trampoline.Accessor;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;

/**
 * API trampoline
 * 
 * @author S. Aubrecht
 */
class AccessorImpl extends Accessor {
    
    public String getDescription( Task t ) {
        return t.getDescription();
    }

    public FileObject getResource(Task t) {
        return t.getResource();
    }

    public TaskGroup getGroup(Task t) {
        return t.getGroup();
    }

    public int getLine(Task t) {
        return t.getLine();
    }
    
    public ActionListener getActionListener(Task t) {
        return t.getActionListener();
    }

    public String getDisplayName(TaskScanningScope scope) {
        return scope.getDisplayName();
    }

    public String getDescription(TaskScanningScope scope) {
        return scope.getDescription();
    }

    public Image getIcon(TaskScanningScope scope) {
        return scope.getIcon();
    }
    
    public boolean isDefault( TaskScanningScope scope ) {
        return scope.isDefault();
    }

    public String getDisplayName(FileTaskScanner scanner) {
        return scanner.getDisplayName();
    }

    public String getDescription(FileTaskScanner scanner) {
        return scanner.getDescription();
    }

    public String getOptionsPath(FileTaskScanner scanner) {
        return scanner.getOptionsPath();
    }

    public String getDisplayName(PushTaskScanner scanner) {
        return scanner.getDisplayName();
    }

    public String getDescription(PushTaskScanner scanner) {
        return scanner.getDescription();
    }

    public String getOptionsPath(PushTaskScanner scanner) {
        return scanner.getOptionsPath();
    }

    public TaskScanningScope.Callback createCallback(TaskManager tm, TaskScanningScope scope) {
        return new TaskScanningScope.Callback( tm, scope );
    }

    public FileTaskScanner.Callback createCallback(TaskManager tm, FileTaskScanner scanner) {
        return new FileTaskScanner.Callback( tm, scanner );
    }

    public PushTaskScanner.Callback createCallback(TaskManager tm, PushTaskScanner scanner) {
        return new PushTaskScanner.Callback( tm, scanner );
    }
}
