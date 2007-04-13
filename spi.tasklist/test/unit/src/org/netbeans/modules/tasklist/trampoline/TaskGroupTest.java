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

import java.awt.Image;
import java.awt.image.BufferedImage;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.tasklist.trampoline.TaskGroupFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;



/** 
 * Tests for TaskGroup class.
 * 
 * @author S. Aubrecht
 */
public class TaskGroupTest extends NbTestCase {

    public TaskGroupTest (String name) {
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
        TestSuite suite = new NbTestSuite(TaskGroupTest.class);
        
        return suite;
    }

    public void testGetters() {
        String name = "group name";
        String displayName = "group display name";
        String description = "group description";
        Image icon = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
        
        TaskGroup group = new TaskGroup( name, displayName, description, icon );
        
        assertEquals( name, group.getName() );
        assertEquals( displayName, group.getDisplayName() );
        assertEquals( description, group.getDescription() );
        assertEquals( icon, group.getIcon() );
    }

    public void testNullValues() {
        String name = "group name";
        String displayName = "group display name";
        String description = "group description";
        Image icon = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
        
        try {
            new TaskGroup(null, displayName, description, icon );
            fail( "name cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
        
        try {
            new TaskGroup(name, null, description, icon );
            fail( "display name cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
        
        try {
            new TaskGroup(name, displayName, null, icon );
        } catch( AssertionError e ) {
            fail( "null description is allowed" );
        }
        
        try {
            new TaskGroup(name, displayName, description, null );
            fail( "icon cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
    }
}

