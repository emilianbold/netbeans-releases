/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import junit.framework.AssertionFailedError;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import java.io.InputStream;

/** Test Plain top manager. Must be run externally.
 * @author Jesse Glick
 */
public class PatchByteCodeTest extends NbTestCase {
    
    public PatchByteCodeTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(PatchByteCodeTest.class));
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testBeanTreeViewLoad () throws Exception {
        checkPatching (
            "org.openide.explorer.view.BeanTreeView", 
            "BeanTreeView.data"
        );
    }
        
    private void checkPatching (
        String className, String resource
    ) throws Exception {
        InputStream is = getClass ().getResourceAsStream (resource);
        assertNotNull ("Resource has been found " + resource, is);
        
        byte[] arr = new byte[is.available ()];
        int l = is.read (arr);
        assertEquals ("Read exactly as much as expected", l, arr.length);
        
        String replaceName = PatchByteCodeTest.class.getName ();
        
        byte[] res = PatchByteCode.enhance(arr, replaceName.replace ('.', '/'));
        PatchClassLoader loader = new PatchClassLoader (className, res);
        
        Class c = loader.loadClass ("org.openide.explorer.view.BeanTreeView");
        
        assertEquals (
            "Superclass changed appropriatelly", 
            replaceName,
            c.getSuperclass().getName ()
        );
    }        
    
    
    private static final class PatchClassLoader extends ClassLoader {
        private String res;
        private byte[] arr;
        
        public PatchClassLoader (String res, byte[] arr) {
            super (PatchClassLoader.class.getClassLoader ());
            
            this.res = res;
            this.arr = arr;
        }
        
        protected synchronized Class loadClass(String name, boolean resolve) 
        throws ClassNotFoundException {
            if (res.equals (name)) {
                byte[] patch = PatchByteCode.patch(arr);
                return defineClass (name, patch, 0, patch.length);
            } else {
                return super.loadClass (name, resolve);
            }
        }
    }
}
