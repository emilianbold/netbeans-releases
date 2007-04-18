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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;



/** 
 * Tests for TaskGroup class.
 * 
 * @author S. Aubrecht
 */
public class EmptyScanningScopeTest extends NbTestCase {
    
    static {
        IDEInitializer.setup(new String[0],new Object[0]);
    }

    public EmptyScanningScopeTest (String name) {
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
        TestSuite suite = new NbTestSuite(EmptyScanningScopeTest.class);
        
        return suite;
    }

    public void testIterator() {
        EmptyScanningScope scope = new EmptyScanningScope();
        
        Iterator<FileObject> iterator = scope.iterator();
        
        assertNotNull( iterator );
        assertFalse( iterator.hasNext() );
        try {
            iterator.next();
            fail( "iterator must be empty" );
        } catch( NoSuchElementException e ) {
            //that's what we want
        }
    }

    public void testGetLookup() {
        EmptyScanningScope scope = new EmptyScanningScope();
        
        assertEquals( Lookup.EMPTY, scope.getLookup() );
    }

    public void testIsInScope() throws IOException {
        EmptyScanningScope scope = new EmptyScanningScope();
        FileObject fo = Repository.getDefault().getDefaultFileSystem().getRoot();
        assertNotNull( fo );
        assertFalse( scope.isInScope( fo ) );
    }
}

