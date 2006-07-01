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

package org.openide.execution;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import junit.framework.TestCase;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/** A piece of the test compatibility suite for the execution APIs.
 *
 * @author Jaroslav Tulach
 */
public class ExecutionEngineHid extends TestCase {
    
    public ExecutionEngineHid(String testName) {
        super(testName);
    }
    
    public void testGetDefault() {
        ExecutionEngine result = ExecutionEngine.getDefault();
        assertNotNull(result);
    }
    
    public void testExecuteIsNonBlocking() throws Exception {
        class Block implements Runnable {
            public boolean here;
            
            
            public synchronized void run() {
                here = true;
                notifyAll();
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            
            public synchronized void waitForRun() throws InterruptedException {
                while(!here) {
                    wait();
                }
                notifyAll();
            }
        }
        Block block = new Block();
        
        
        ExecutionEngine instance = ExecutionEngine.getDefault();
        ExecutorTask result = instance.execute("My task", block, InputOutput.NULL);
        
        assertFalse("Of course it is not finished, as it is blocked", result.isFinished());
        block.waitForRun();
        
        int r = result.result();
        assertEquals("Default result is 0", 0, r);
    }
    
}
