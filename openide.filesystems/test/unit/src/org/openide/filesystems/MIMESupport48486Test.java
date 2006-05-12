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

import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

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

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = TestUtilHid.createLocalFileSystem(getName(), new String[]{"A.opqr", });
        mimeFo = lfs.findResource("A.opqr");
        assertNotNull(mimeFo);
        MockServices.setServices(MamaResolver.class);
        Lookup.getDefault().lookup(MamaResolver.class).fo = mimeFo;
    }

    public void testMimeResolverDeadlock() throws Exception {
        mimeFo.getMIMEType();
    }

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
                RequestProcessor.getDefault().post(this).waitFinished();
            }
            return null;
        }
    }

}
