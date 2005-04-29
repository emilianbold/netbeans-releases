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

package org.openide.filesystems;


import org.netbeans.junit.*;
import org.openide.util.Lookup;

/**
 * Simulate deadlock from issue 48486.
 *
 * @author Radek Matous
 */
public class MIMESupport48486Test extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private FileSystem lfs;
    private FileObject mimeFo;

    /**
     * Creates new DataFolderTest
     */
    public MIMESupport48486Test(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(MIMESupport48486Test.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.MIMESupport48486Test$Lkp");
        MamaResolver mr = (MamaResolver) Lookup.getDefault().lookup(MIMEResolver.class);

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = TestUtilHid.createLocalFileSystem(getName(), new String[]{"A.opqr", });
        mimeFo = lfs.findResource("A.opqr");
        assertNotNull(mimeFo);
        assertNotNull(Lookup.getDefault().getClass().toString(), mr);
        mr.fo = mimeFo;
    }

    public void testMimeResolverDeadlock() throws Exception {
        mimeFo.getMIMEType();
    }


    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }

        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            ic.add(new MamaResolver());
        }

    } // end of Lkp

    public static final class MamaResolver extends MIMEResolver implements Runnable {
        boolean isRecursiveCall = false;
        FileObject fo = null;

        public void run() {
            assert this.fo != null;
            isRecursiveCall = true;
            fo.getMIMEType();
        }

        public String findMIMEType(FileObject fo) {
            if (!isRecursiveCall) {
                org.openide.util.RequestProcessor.getDefault().post(this).waitFinished();
            }
            return null;
        }
    }

}
  
  
  