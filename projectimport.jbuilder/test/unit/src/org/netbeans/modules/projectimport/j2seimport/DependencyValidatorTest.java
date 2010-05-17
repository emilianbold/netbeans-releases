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

package org.netbeans.modules.projectimport.j2seimport;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Radek Matous
 */
public class DependencyValidatorTest extends NbTestCase {

    static {
        System.setProperty("projectimport.logging.level", "WARNING");
    }

    public DependencyValidatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DependencyValidatorTest.class);
        
        return suite;
    }

    /**
     * Test of isValid method, of class org.netbeans.modules.projectimport.jbuilder.elementary.utilities.DependencyValidator.
     */
    public void testIsValid() throws Exception {
        FileObject dir = FileUtil.toFileObject(getWorkDir());
        AbstractProject d = new AbstractProject("d", dir);
        AbstractProject c = new AbstractProject("c", dir);
        c.addDependency(d);
        AbstractProject b = new AbstractProject("b", dir);
        b.addDependency(c);
        AbstractProject a = new AbstractProject("a", dir);
        a.addDependency(b);
        
        
        
        
        DependencyValidator dv = DependencyValidator.checkProject(a);
        assertTrue(dv.isValid());
        
        dv = DependencyValidator.checkProject(b);
        assertTrue(dv.isValid());

        dv = DependencyValidator.checkProject(c);
        assertTrue(dv.isValid());

        dv = DependencyValidator.checkProject(d);
        assertTrue(dv.isValid());        
        
        
        d.addDependency(a);
        dv = DependencyValidator.checkProject(a);
        assertFalse(dv.isValid());
        
        dv = DependencyValidator.checkProject(b);
        assertFalse(dv.isValid());

        dv = DependencyValidator.checkProject(c);
        assertFalse(dv.isValid());

        dv = DependencyValidator.checkProject(d);
        assertFalse(dv.isValid());        
    }
    
}
