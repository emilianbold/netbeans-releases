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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.clearcase.client.status;

import junit.framework.TestCase;

/**
 *
 * @author tomas
 */
public class FileVersionSelectorTest extends TestCase {
    
    public FileVersionSelectorTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of fromString method, of class FileVersionSelector.
     */
    public void testCrap() {
        FileVersionSelector fvs = FileVersionSelector.fromString(null);
        assertNull(fvs);        
        fvs = FileVersionSelector.fromString("");
        assertNull(fvs);
        fvs = FileVersionSelector.fromString("   ");
        assertNull(fvs);        
        
        fvs = FileVersionSelector.fromString("/");
        assertNotNull(fvs);        
        assertFileSelector(fvs, "", FileVersionSelector.INVALID_VERSION, "/", false);
               
        fvs = FileVersionSelector.fromString("/xxx");
        assertNotNull(fvs);        
        assertFileSelector(fvs, "", FileVersionSelector.INVALID_VERSION, "/xxx", false);
        
        fvs = FileVersionSelector.fromString("/ xxx");
        assertNotNull(fvs);        
        assertFileSelector(fvs, "", FileVersionSelector.INVALID_VERSION, "/ xxx", false);        
    }

    public void testDateFormat() {
        FileVersionSelector fvs = FileVersionSelector.fromString("24-Mar.11:32.412");
        assertNotNull(fvs);
        assertFileSelector(fvs, null, FileVersionSelector.INVALID_VERSION, "24-Mar.11:32.412", false);
    }
    
    public void testUptodate() {
        FileVersionSelector fvs = FileVersionSelector.fromString("/main/1");
        assertNotNull(fvs);
        assertFileSelector(fvs, "/main", 1, "/main/1", false);                
    }

    public void testLabeled() {
        FileVersionSelector fvs = FileVersionSelector.fromString("/main/BLOOD");
        assertNotNull(fvs);
        assertFileSelector(fvs, "/main", FileVersionSelector.INVALID_VERSION, "/main/BLOOD", false);        
    }
    
    public void testCheckedout() {
        FileVersionSelector fvs = FileVersionSelector.fromString("/main/CHECKEDOUT");
        assertNotNull(fvs);
        assertFileSelector(fvs, "/main", FileVersionSelector.CHECKEDOUT_VERSION, "/main/CHECKEDOUT", true);        
    }

    private void assertFileSelector(FileVersionSelector fvs, String path, long version, String selector, boolean  checkedout) {
        assertEquals(path, fvs.getPath());
        assertEquals(version, fvs.getVersionNumber());
        assertEquals(selector, fvs.getVersionSelector());
        assertEquals(checkedout, fvs.isCheckedout());
    }

}
