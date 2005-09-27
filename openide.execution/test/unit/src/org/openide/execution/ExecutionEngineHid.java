/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.execution;

import junit.framework.*;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
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

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
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
