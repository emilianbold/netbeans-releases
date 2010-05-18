/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.openide.filesystems;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 * Simulates issue 50852.
 *
 * @author Jiri Skrivanek
 */
public class VoidValue132801Test extends NbTestCase {

    public VoidValue132801Test(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(VoidValue132801Test.class));
    }

    /**
     * Setups variables.
     */
    @Override
    protected void setUp() throws Exception {
    }
    private static String[] resources = new String[]{
        "/fold10/fold11/fold12/file12.txt",
        "/fold20/fold21/fold22",
        "/fold20/fold23.txt"
    };

    protected String[] getResources(String testName) {
        return resources;
    }

    /** Test VoidValue attribute is not copied (#132801).
     * - create MultiFileSystem
     * - set String and VoidValue attributes for file object
     * - copy file object
     * - check String attribute is copied and VoidValue attribute not
     */
    public void testCopyVoidValue132801() throws Exception {
        System.setProperty("workdir", getWorkDirPath());
        clearWorkDir();
        FileSystem lfs = TestUtilHid.createLocalFileSystem("mfs1" + getName(), new String[]{"/fold/file1"});
        FileSystem xfs = TestUtilHid.createXMLFileSystem(getName(), new String[]{"/xmlFold/xmlFile1"});
        FileSystem mfs = new MultiFileSystem(new FileSystem[]{lfs, xfs});

        FileObject file1FO = mfs.findResource("/fold/file1");
        String stringValue = "abc";
        file1FO.setAttribute("STRING-ATTR", stringValue);
        MultiFileObject.VoidValue voidValue = new MultiFileObject.VoidValue();
        file1FO.setAttribute("VOIDVALUE-ATTR", voidValue);
        assertTrue("VoidValue not set as attribute.", file1FO.getAttribute("VOIDVALUE-ATTR") instanceof MultiFileObject.VoidValue);
        FileObject file11FO = file1FO.copy(file1FO.getParent(), "file1_1", "");
        assertEquals("String attribute is not copied.", stringValue, file11FO.getAttribute("STRING-ATTR"));
        assertNull("VoidValue should not be copied.", file11FO.getAttribute("VOIDVALUE-ATTR"));
    }
}
  
  
  
