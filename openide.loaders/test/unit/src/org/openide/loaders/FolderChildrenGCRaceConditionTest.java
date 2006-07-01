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

package org.openide.loaders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.lang.ref.WeakReference;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;

import org.openide.filesystems.*;

import org.netbeans.junit.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.RequestProcessor;


public class FolderChildrenGCRaceConditionTest extends LoggingTestCaseHid {
    public FolderChildrenGCRaceConditionTest() {
        super("");
    }
    
    public FolderChildrenGCRaceConditionTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    	super.setUp();
        clearWorkDir();

        FileObject[] arr = Repository.getDefault().getDefaultFileSystem().getRoot().getChildren();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete();
        }
    }
    
    public void testChildrenCanBeSetToNullIfGCKicksIn () throws Exception {
        FileObject f = FileUtil.createData(Repository.getDefault().getDefaultFileSystem().getRoot(), "folder/node.txt");
        
        DataFolder df = DataFolder.findFolder(f.getParent());
        Node n = df.getNodeDelegate();
        
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Ok, one", 1, arr.length);
        final WeakReference ref = new WeakReference(arr[0]);
        arr = null;
        
        class R implements Runnable {
            public void run() {
                ErrorManager.getDefault().log("Ready to GC");
                assertGC("Node can go away in the worst possible moment", ref);
                ErrorManager.getDefault().log("Gone");
            }
        }
        R r = new R();
        RequestProcessor.Task t = new RequestProcessor("Inter", 1, true).post(r);
        
        registerSwitches(
            "THREAD:FolderChildren_Refresh MSG:Children computed" +
            "THREAD:FolderChildren_Refresh MSG:notifyFinished.*" +
            "THREAD:Inter MSG:Gone.*" +
            "THREAD:Finalizer MSG:RMV.*" +
            "THREAD:FolderChildren_Refresh MSG:Clearing the ref.*" +
            "", 200);
        
        int cnt = n.getChildren().getNodes(true).length;
        
        t.cancel();
        
        assertEquals("Count is really one", 1, cnt);
    }
   
}