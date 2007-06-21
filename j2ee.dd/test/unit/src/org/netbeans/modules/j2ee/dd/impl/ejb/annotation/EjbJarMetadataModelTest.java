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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.impl.ejb.annotation;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Martin Adamek
 */
public class EjbJarMetadataModelTest extends CommonTestCase {
    
    public EjbJarMetadataModelTest(String testName) {
        super(testName);
    }

    public void testModel() throws IOException, InterruptedException {
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Customer.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Customer implements CustomerLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/CustomerLocal.java",
                "package foo;" +
                "public interface CustomerLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateful()" +
                "public class Employee implements EmployeeLocal, EmployeeRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployeeLocal.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Local()" +
                "public interface EmployeeLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployeeRemote.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Remote()" +
                "public interface EmployeeRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Dispatcher.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "import javax.jms.*;" +
                "@MessageDriven()" +
                "public class Dispatcher implements MessageListener {" +
                "public void onMessage(Message message) {}" +
                "}");
        
        final String expectedResult = "foo";
        
        String result = createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                
                EjbJar ejbJar = metadata.getRoot();
                EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
                assertNotNull(enterpriseBeans);
                Ejb[] ejbs = enterpriseBeans.getEjbs();
                assertEquals(3, ejbs.length);
                Session[] sessions = enterpriseBeans.getSession();
                assertEquals(2, sessions.length);
                MessageDriven[] messageDrivens = enterpriseBeans.getMessageDriven();
                assertEquals(1, messageDrivens.length);
                AssemblyDescriptor assemblyDescriptor = ejbJar.getSingleAssemblyDescriptor();
                assertNotNull(assemblyDescriptor);

                return expectedResult;
            }
        });
        
        assertSame(expectedResult, result);
    }
    
}
