/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.mimelookup;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
public class MimePathLookupTest extends NbTestCase {
    
    /** Creates a new instance of MimePathLookupTest */
    public MimePathLookupTest(String name) {
        super(name);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(new String[0], getWorkDir(), new Object[] {},
            getClass().getClassLoader(), 
            new Class [] { 
                DefaultMimeDataProvider.class, 
            }
        );
    }
    
    public void testAddingMimeDataProvider() throws Exception {
        checkAddingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkAddingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private void checkAddingMimeDataProvider(String instanceFile, Class markerClass) throws Exception {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        
        Collection markers = lookup.lookupAll(markerClass);
        assertEquals("There should be no markers", 0, markers.size());
        
        // Add the data provider
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        markers = lookup.lookupAll(markerClass);
        assertEquals("No markers found", 1, markers.size());
    }

    public void testAddingMimeDataProvider2() throws Exception {
        checkAddingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkAddingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private void checkAddingMimeDataProvider2(String instanceFile, Class markerClass) throws Exception {
        MimePath path = MimePath.get("text/x-java");
        
        Lookup.Result result = MimeLookup.getLookup(path).lookupResult(markerClass);
        Collection markers = result.allInstances();
        assertEquals("There should be no markers", 0, markers.size());

        L listener = new L();
        result.addLookupListener(listener);
        assertEquals("There should be no changes received", 0, listener.resultChangedCnt);
        
        // Add the data provider
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        assertEquals("No changes received", 1, listener.resultChangedCnt);
        markers = result.allInstances();
        assertEquals("No markers found", 1, markers.size());
    }
    
    public void testRemovingMimeDataProvider() throws Exception {
        checkRemovingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkRemovingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private void checkRemovingMimeDataProvider(String instanceFile, Class markerClass) throws Exception {
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();

        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        
        Collection markers = lookup.lookupAll(markerClass);
        assertEquals("No markers found", 1, markers.size());

        TestUtilities.deleteFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        markers = lookup.lookupAll(markerClass);
        assertEquals("There should be no markers", 0, markers.size());
    }

    public void testRemovingMimeDataProvider2() throws Exception {
        checkRemovingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkRemovingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private void checkRemovingMimeDataProvider2(String instanceFile, Class markerClass) throws Exception {
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();

        MimePath path = MimePath.get("text/x-java");
        Lookup.Result result = MimeLookup.getLookup(path).lookupResult(markerClass);
        Collection markers = result.allInstances();
        assertEquals("No markers found", 1, markers.size());

        L listener = new L();
        result.addLookupListener(listener);
        assertEquals("There should be no changes received", 0, listener.resultChangedCnt);

        // Remove the data provider
        TestUtilities.deleteFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        assertEquals("No changes received", 1, listener.resultChangedCnt);
        markers = result.allInstances();
        assertEquals("There should be no markers", 0, markers.size());
    }
    
    private static class L implements LookupListener {
        public int resultChangedCnt = 0;
        
        public void resultChanged(LookupEvent ev) {
            resultChangedCnt++;
        }
    } // End of L class
}
