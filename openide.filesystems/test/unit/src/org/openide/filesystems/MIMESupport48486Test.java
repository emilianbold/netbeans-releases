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
   * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
