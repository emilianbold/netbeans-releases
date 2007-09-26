/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test utility functions.
 * @author Jesse Glick
 */
public class UtilTest extends TestBase {

    public UtilTest(String name) {
        super(name);
    }

    private FileObject test1Xml;
    private FileObject test2Xml;
    private FileObject test3Xml;
    private FileObject test5Xml;

    protected void setUp() throws Exception {
        super.setUp();
        test1Xml = FileUtil.toFileObject(new File(datadir, "test1.xml"));
        assertNotNull("have test1.xml", test1Xml);
        test2Xml = FileUtil.toFileObject(new File(datadir, "test2.xml"));
        assertNotNull("have test2.xml", test2Xml);
        test3Xml = FileUtil.toFileObject(new File(datadir, "test3.xml"));
        assertNotNull("have test3.xml", test3Xml);
        test5Xml = FileUtil.toFileObject(new File(datadir, "test5.xml"));
        assertNotNull("have test5.xml", test5Xml);
    }
    
    public void testGetAntScriptName() throws Exception {
        assertEquals("correct name for test1.xml", "test1", Util.getAntScriptName(test1Xml));
        assertEquals("no name for test2.xml", null, Util.getAntScriptName(test2Xml));
        assertEquals("correct name for test3.xml", "test3", Util.getAntScriptName(test3Xml));
        assertEquals("no name for test5.xml", null, Util.getAntScriptName(test5Xml));
    }
    
    public void testGetAntScriptTargetNames() throws Exception {
        assertEquals("correct targets for test1.xml",
            Arrays.asList(new String[] {"another", "main", "other"}),
            Util.getAntScriptTargetNames(test1Xml));
        assertEquals("correct targets for test2.xml",
            Collections.singletonList("sometarget"),
            Util.getAntScriptTargetNames(test2Xml));
        assertEquals("correct targets for test3.xml",
            Arrays.asList(new String[] {"imported1", "imported2", "main"}),
            Util.getAntScriptTargetNames(test3Xml));
        assertEquals("no targets for test5.xml", null, Util.getAntScriptTargetNames(test5Xml));
    }

}
