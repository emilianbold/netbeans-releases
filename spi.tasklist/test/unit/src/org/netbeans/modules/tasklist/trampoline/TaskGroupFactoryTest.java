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

package org.netbeans.modules.tasklist.trampoline;

import java.util.List;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.tasklist.trampoline.TaskGroupFactory;



/** 
 * Tests for TaskGroup class.
 * 
 * @author S. Aubrecht
 */
public class TaskGroupFactoryTest extends NbTestCase {
    
    public static final String TASK_GROUP_NAME_A = "nb-tasklist-unittestA";
    public static final String TASK_GROUP_DISPLAY_NAME_A = "unitTestGroupLabelA";
    public static final String TASK_GROUP_DESCRIPTION_A = "unitTestGroupDescriptionA";
    
    public static final String TASK_GROUP_NAME_B = "nb-tasklist-unittestB";
    public static final String TASK_GROUP_DISPLAY_NAME_B = "unitTestGroupLabelB";
    public static final String TASK_GROUP_DESCRIPTION_B = "unitTestGroupDescriptionB";
    
    public static final String TASK_GROUP_NAME_C = "nb-tasklist-unittestC";
    public static final String TASK_GROUP_DISPLAY_NAME_C = "unitTestGroupLabelC";
    public static final String TASK_GROUP_DESCRIPTION_C = "unitTestGroupDescriptionC";
    
    static {
        String[] layers = new String[] {"org/netbeans/modules/tasklist/trampoline/resources/mf-layer.xml"};//NOI18N
        IDEInitializer.setup(layers,new Object[0]);
    }

    public TaskGroupFactoryTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TaskGroupFactoryTest.class);
        
        return suite;
    }

    public void testGetGroup() {
        TaskGroupFactory factory = TaskGroupFactory.getDefault();
        
        List<? extends TaskGroup> groups = factory.getGroups();
        
        assertEquals( 2, groups.size() );
        
        TaskGroup tgA = groups.get( 0 );
        assertEquals( TASK_GROUP_NAME_A, tgA.getName());
        assertEquals( TASK_GROUP_DISPLAY_NAME_A, tgA.getDisplayName());
        assertEquals( TASK_GROUP_DESCRIPTION_A, tgA.getDescription());
        
        TaskGroup tgB = groups.get( 1 );
        assertEquals( TASK_GROUP_NAME_B, tgB.getName());
        assertEquals( TASK_GROUP_DISPLAY_NAME_B, tgB.getDisplayName());
        assertEquals( TASK_GROUP_DESCRIPTION_B, tgB.getDescription());
        
        assertFalse( tgA.equals( tgB ) );
        
        TaskGroup group = factory.getGroup( TASK_GROUP_NAME_A );
        assertNotNull( group );
        assertEquals( TASK_GROUP_NAME_A, group.getName());
        
        group = factory.getGroup( TASK_GROUP_NAME_B );
        assertNotNull( group );
        
        group = factory.getGroup( "unknown group name" );
        assertNull( group );
        
        assertNotNull( factory.getDefaultGroup() );
        
        try {
            factory.getGroup( null );
            fail( "null group name is not acceptable" );
        } catch( AssertionError e ) {
            //expected
        }
    }

    public void testCreate() {
        TaskGroup group = TaskGroupFactory.create( TASK_GROUP_NAME_C, 
                "org.netbeans.modules.tasklist.trampoline.resources.Bundle", 
                "LBL_unittest_groupC", 
                "HINT_unittest_groupC", 
                "ICON_unittestgroupC");

        assertNotNull( group );
        assertEquals( TASK_GROUP_NAME_C, group.getName());
        assertEquals( TASK_GROUP_DISPLAY_NAME_C, group.getDisplayName());
        assertEquals( TASK_GROUP_DESCRIPTION_C, group.getDescription());
    }
}

