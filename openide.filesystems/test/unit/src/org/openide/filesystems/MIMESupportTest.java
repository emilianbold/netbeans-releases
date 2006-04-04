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

package org.openide.filesystems;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jaroslav Tulach, Radek Matous
 */
public class MIMESupportTest extends NbTestCase {
    private TestLookup lookup;
    public MIMESupportTest(String testName) {
        super(testName);
    }

    static {
        System.setProperty("org.openide.util.Lookup", MIMESupportTest.TestLookup.class.getName());
        assertEquals(MIMESupportTest.TestLookup.class, Lookup.getDefault().getClass());
        
    }
    
    protected void setUp() throws Exception {
        lookup = (MIMESupportTest.TestLookup)Lookup.getDefault();
        lookup.init();
    }

    public void testFindMIMETypeCanBeGarbageCollected() throws IOException {
        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "Ahoj.bla");
        
        String expResult = "content/unknown";
        String result = FileUtil.getMIMEType(fo);
        assertEquals("some content found", expResult, result);
        
        WeakReference r = new WeakReference(fo);
        fo = null;
        assertGC("Can be GCed", r);
    }
    
    public void testBehaviourWhemLookupResultIsChanging() throws Exception {
        MIMESupportTest.TestResolver testR = new MIMESupportTest.TestResolver("a/a");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());
        
        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "mysterious.lenka");
        
        assertEquals("content/unknown",fo.getMIMEType());
        
        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));        
        assertEquals(testR.getMime(),fo.getMIMEType());
        
        testR = new MIMESupportTest.TestResolver("b/b");
        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));        
        assertEquals(testR.getMime(),fo.getMIMEType());
    }

    private class TestResolver extends MIMEResolver {
        private String mime;
        private TestResolver(String mime) {            
            this.mime = mime;
        }
        
        public String findMIMEType(FileObject fo) {
            return mime;
        }        
        
        private String getMime() {
            return mime;
        }
    }
    
    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            init();
        }
        
        private void init() {
            setLookups(new Lookup[] {});
        }
        
        private void setLookups(Object instance) {
            setLookups(new Lookup[] {getInstanceLookup(instance)});
        }
        
        private Lookup getInstanceLookup(final Object instance) {
            InstanceContent instanceContent = new InstanceContent();
            instanceContent.add(instance);
            Lookup instanceLookup = new AbstractLookup(instanceContent);
            return instanceLookup;
        }        
    }    
    
}
