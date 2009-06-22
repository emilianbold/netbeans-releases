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

package org.netbeans.modules.j2ee.persistence.wizard.dao;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.sourcetestsupport.SourceTestSupport;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for <code>EjbFacadeWizardIterator</code>.
 *
 * @author Erno Mononen
 */
public class EjbFacadeWizardIteratorTest extends SourceTestSupport {
    
    public EjbFacadeWizardIteratorTest(String testName) {
        super(testName);
    }
    
    
    public void setUp() throws Exception{
        super.setUp();
        File javaxEjb = new File(getWorkDir(), "javax" + File.separator + "ejb");
        javaxEjb.mkdirs();
        TestUtilities.copyStringToFile(new File(javaxEjb, "Stateless.java"), "package javax.ejb; public @interface Stateless{}");
        TestUtilities.copyStringToFile(new File(javaxEjb, "Local.java"), "package javax.ejb; public @interface Local{}");
        TestUtilities.copyStringToFile(new File(javaxEjb, "Remote.java"), "package javax.ejb; public @interface Remote{}");
    }

    /**
     * sme problem with annotation creation
     * TODO: need additional investigation
     * @throws Exception
     */
//    public void testCreateInterface() throws Exception {
//
//        final String name = "Test";
//        final String annotationType = "javax.ejb.Remote";
//        final String pkgName = "foo";
//        File pkg = new File(getWorkDir(), pkgName);
//        pkg.mkdir();
//        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
//
//        String golden =
//                "package " + pkgName +";\n\n" +
//                "import " + annotationType + ";\n\n" +
//                "@" + JavaIdentifiers.unqualify(annotationType) + "\n" +
//                "public interface " + name + " {\n" +
//                "}";
//        FileObject result = wizardIterator.createInterface(name, annotationType, FileUtil.toFileObject(pkg));
//        assertEquals(golden, TestUtilities.copyFileObjectToString(result));
//    }
    
    public void testAddMethodToInterface() throws Exception {
        File testFile = new File(getWorkDir(), "Test.java");
        String originalContent =
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public interface Test {\n" +
                "}";
        
        TestUtilities.copyStringToFile(testFile, originalContent);
        
        String golden =
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public interface Test {\n\n" +
                "    void testMethod(Object entity);\n" +
                "}";
        
        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
        GenerationOptions options = new GenerationOptions();
        options.setMethodName("testMethod");
        options.setReturnType("void");
        options.setParameterName("entity");
        options.setParameterType("Object");
        wizardIterator.addMethodToInterface(Collections.<GenerationOptions>singletonList(options), FileUtil.toFileObject(testFile));
        assertEquals(golden, TestUtilities.copyFileToString(testFile));
        
    }
    
    /**
     * sme problem with annotation creation
     * TODO: need additional investigation
     * @throws Exception
     */
//    public void testGenerate() throws Exception {
//        File testFile = new File(getWorkDir(), "Test.java");
//        String originalContent =
//                "package org.netbeans.test;\n\n" +
//                "import java.util.*;\n\n" +
//                "@javax.persistence.Entity\n" +
//                "public class Test {\n" +
//                "}";
//
//        final String pkgName = "foo";
//        File pkg = new File(getWorkDir(), pkgName);
//        pkg.mkdir();
//
//        TestUtilities.copyStringToFile(testFile, originalContent);
//        EjbFacadeWizardIterator wizardIterator = new EjbFacadeWizardIterator();
//        Set<FileObject> result = wizardIterator.generate(
//                FileUtil.toFileObject(pkg), "Test", pkgName,
//                true, true, ContainerManagedJTAInjectableInEJB.class);
//
//        assertEquals(3, result.size());
//
//        for (FileObject each : result){
//            assertFile(FileUtil.toFile(each), getGoldenFile(each.getNameExt()));
//        }
//
//    }
    
}
