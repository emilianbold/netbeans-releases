/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.project.libraries;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.openide.util.lookup.Lookups;
/**
 *
 * @author  Tomas Zezula
 */
public class LibraryTest extends NbTestCase {
    
    private LibraryManagerTest.TestLibraryProvider lp;
    
    /** Creates a new instance of LibraryManagerTest */
    public LibraryTest (String testName) {
        super (testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        lp = new LibraryManagerTest.TestLibraryProvider ();
        TestUtil.setLookup (Lookups.fixed(new Object[] {lp}));
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetLibraries () throws Exception {        
        LibraryManager lm = LibraryManager.getDefault();
        Library[] libs = lm.getLibraries();
        LibraryImplementation[] impls = LibraryManagerTest.createTestLibs ();
        lp.setLibraries(impls);        
        libs = lm.getLibraries();
        assertEquals ("Libraries count", 2, libs.length);
        LibraryManagerTest.assertLibsEquals (libs, impls);
        LibraryManagerTest.TestListener tl = new LibraryManagerTest.TestListener ();
        libs[0].addPropertyChangeListener(tl);
        impls[0].setName("NewLibrary1");
        LibraryManagerTest.assertEventsEquals(tl.getEventNames(), new String[] {Library.PROP_NAME});
        tl.reset();
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        
        impls[0].setDescription("NewLibrary1Description");
        LibraryManagerTest.assertEventsEquals(tl.getEventNames(), new String[] {Library.PROP_DESCRIPTION});
        tl.reset();
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        List urls = new ArrayList ();
        urls.add (new URL ("file:/lib/libnew1.so"));
        urls.add (new URL ("file:/lib/libnew2.so"));
        impls[0].setContent ("bin",urls);        
        LibraryManagerTest.assertEventsEquals(tl.getEventNames(), new String[] {Library.PROP_CONTENT});
        tl.reset();
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
        urls = new ArrayList ();
        urls.add (new URL ("file:/src/new/src/"));
        impls[0].setContent ("src",urls);        
        LibraryManagerTest.assertEventsEquals(tl.getEventNames(), new String[] {Library.PROP_CONTENT});
        tl.reset();
        LibraryManagerTest.assertLibsEquals (new Library[] {libs[0]}, new LibraryImplementation[] {impls[0]});
    }    
    
}
