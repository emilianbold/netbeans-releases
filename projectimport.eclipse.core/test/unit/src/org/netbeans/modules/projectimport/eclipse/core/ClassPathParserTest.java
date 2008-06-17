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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;

/**
 * @author Martin Krauskopf
 */
public class ClassPathParserTest extends NbTestCase {
    
    public ClassPathParserTest(String testName) {
        super(testName);
    }
    
    public void testParse_71770() throws Exception {
        DotClassPath cp = DotClassPathParser.parse(new File(getDataDir(), "71770.classpath"), Collections.<Link>emptyList());
        assertEquals("17 classpath entries", 17, cp.getClassPathEntries().size());
        assertEquals("1 sources entry", 1, cp.getSourceRoots().size());
        assertNotNull("non empty output", cp.getOutput());
        assertNotNull("non empty JDK", cp.getJREContainer());
        
        DotClassPathEntry entry = cp.getClassPathEntries().get(0);
        assertEquals("lib", entry.getProperty("kind"));
        assertEquals("C:/MyProjects/JavaAPI/integrationServerApi.jar", entry.getProperty("path"));
        assertEquals("jar:file:/C:/MyProjects/JavaAPI/docs/javaApiDoc.jar!/", entry.getProperty("javadoc_location"));
    }
    
    public void testAccessrulesDoesNotCauseException() throws Exception { // #91669
        DotClassPath cp = DotClassPathParser.parse(new File(getDataDir(), "91669.classpath"), Collections.<Link>emptyList());
        assertEquals("one classpath entries", 0, cp.getClassPathEntries().size());
        assertNotNull("non empty JDK", cp.getJREContainer());
    }
    
}
