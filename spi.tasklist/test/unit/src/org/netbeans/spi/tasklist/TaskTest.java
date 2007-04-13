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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.tasklist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.tasklist.trampoline.TaskGroupFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;



/** 
 * Tests for Task class.
 * 
 * @author S. Aubrecht
 */
public class TaskTest extends NbTestCase {

    public static final String TASK_GROUP_NAME = "nb-tasklist-unittest";
    public static final String TASK_GROUP_DISPLAY_NAME = "unitTestGroupLabel";
    public static final String TASK_GROUP_DESCRIPTION = "unitTestGroupDescription";
    
    static {
        String[] layers = new String[] {"org/netbeans/spi/tasklist/resources/mf-layer.xml"};//NOI18N
        IDEInitializer.setup(layers,new Object[0]);
    }
    
    public TaskTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        assertNotNull( "make sure we have a task group ready for testing", 
                TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME ) );
    }

    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TaskTest.class);
        
        return suite;
    }

    public void testGetters() {
        String description = "task description";
        int lineNo = 123;
        FileObject resource = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        Task t = Task.create(resource, TASK_GROUP_NAME, description, lineNo );
        
        assertEquals( description, t.getDescription() );
        assertEquals( lineNo, t.getLine() );
        assertEquals( resource, t.getResource() );
        assertEquals( TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME), t.getGroup() );
        assertNull( t.getActionListener() );
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        t = Task.create(resource, TASK_GROUP_NAME, description, al );
        
        assertEquals( description, t.getDescription() );
        assertEquals( -1, t.getLine() );
        assertEquals( resource, t.getResource() );
        assertEquals( TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME), t.getGroup() );
        assertEquals( al, t.getActionListener() );
    }

    public void testNullValues() {
        String description = "task description";
        int lineNo = 123;
        FileObject resource = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        try {
            Task.create(null, TASK_GROUP_NAME, description, lineNo );
            fail( "resource cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
        
        try {
            Task.create(resource, null, description, lineNo );
            fail( "group name cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
        
        try {
            Task.create(resource, TASK_GROUP_NAME, null, lineNo );
            fail( "description cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
    }

    public void testNegativeLineNumberAllowed() {
        String description = "task description";
        int lineNo = -1;
        FileObject resource = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        Task t = Task.create(resource, TASK_GROUP_NAME, description, lineNo );
        
        assertEquals( description, t.getDescription() );
        assertEquals( lineNo, t.getLine() );
        assertEquals( resource, t.getResource() );
        assertEquals( TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME), t.getGroup() );
    }

    public void testUnknownTaskGroup() {
        String description = "task description";
        int lineNo = 123;
        FileObject resource = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        Task t = Task.create(resource, "unknown task group name", description, lineNo );
        
        assertEquals( description, t.getDescription() );
        assertEquals( lineNo, t.getLine() );
        assertEquals( resource, t.getResource() );
        assertEquals( TaskGroupFactory.getDefault().getDefaultGroup(), t.getGroup() );
    }

    public void testEquals() {
        String description = "task description";
        int lineNo = 123;
        FileObject resource = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        Task t1 = Task.create(resource, TASK_GROUP_NAME, description, lineNo );
        Task t2 = Task.create(resource, TASK_GROUP_NAME, description, lineNo );
        
        assertEquals( t1, t2 );
        assertEquals( t1.hashCode(), t2.hashCode() );
    }
}

