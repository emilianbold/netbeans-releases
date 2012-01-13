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

package org.netbeans.modules.queries;

import java.io.File;
import org.netbeans.junit.NbTestCase;

/**
 * Test of ParentChildCollocationQuery impl.
 *
 * @author David Konecny
 */
public class ParentChildCollocationQueryTest extends NbTestCase {

    public ParentChildCollocationQueryTest(String testName) {
        super(testName);
    }

    public void testAreCollocated() throws Exception {
        clearWorkDir();
        File base = getWorkDir();
        File proj1 = new File(base, "proj1");
        proj1.mkdirs();
        File proj3 = new File(proj1, "proj3");
        proj3.mkdirs();
        File proj2 = new File(base, "proj2");
        proj2.mkdirs();
        
        ParentChildCollocationQuery query = new ParentChildCollocationQuery();
        assertTrue("Must be collocated", query.areCollocated(proj1.toURI(), proj3.toURI()));
        assertTrue("Must be collocated", query.areCollocated(proj3.toURI(), proj1.toURI()));
        assertFalse("Cannot be collocated", query.areCollocated(proj1.toURI(), proj2.toURI()));
        assertFalse("Cannot be collocated", query.areCollocated(proj2.toURI(), proj1.toURI()));
        
        // folder does not exist:
        File proj4 = new File(base, "proj");
        assertFalse("Cannot be collocated", query.areCollocated(proj1.toURI(), proj4.toURI()));
        assertFalse("Cannot be collocated", query.areCollocated(proj4.toURI(), proj1.toURI()));
        proj4.mkdirs();
        assertFalse("Cannot be collocated", query.areCollocated(proj1.toURI(), proj4.toURI()));
        assertFalse("Cannot be collocated", query.areCollocated(proj4.toURI(), proj1.toURI()));
        
        // files do not exist:
        File file1 = new File(base, "file1.txt");
        File file2 = new File(base, "file1");
        assertFalse("Cannot be collocated", query.areCollocated(file1.toURI(), file2.toURI()));
        assertFalse("Cannot be collocated", query.areCollocated(file2.toURI(), file1.toURI()));
        
        // passing the same parameter
        assertTrue("A file must be collocated with itself", query.areCollocated(proj1.toURI(), proj1.toURI()));
    }

}
