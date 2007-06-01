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
import org.netbeans.modules.j2ee.dd.api.common.PortComponentRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Martin Adamek
 */
public class SessionImplTest extends EjbJarTestCase {

    public SessionImplTest(String testName) {
        super(testName);
    }

    public void testSession() throws InterruptedException, IOException {
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
                "@Stateful(name=\"SatisfiedEmployee\")" +
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
        TestUtilities.copyStringToFileObject(srcFO, "foo/Employer.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateful()" +
                "@Local(EmployerLocal.class)" +
                "@Remote({EmployerRemote.class, EmployerRemoteAdvanced.class})" +
                "public class Employer implements EmployerLocal, EmployerRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerLocal.java",
                "package foo;" +
                "public interface EmployerLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerRemote.java",
                "package foo;" +
                "public interface EmployerRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/EmployerRemoteAdvanced.java",
                "package foo;" +
                "public interface EmployerRemoteAdvanced {" +
                "}");

        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Customer
                Session session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Customer");
                assertEquals("foo.Customer", session.getEjbClass());
                assertEquals("Customer", session.getEjbName());
                assertEquals(Session.SESSION_TYPE_STATELESS, session.getSessionType());
                assertEquals(1, session.getBusinessLocal().length);
                assertEquals("foo.CustomerLocal", session.getBusinessLocal()[0]);
                // test Employee
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "SatisfiedEmployee");
                assertEquals("foo.Employee", session.getEjbClass());
                assertEquals("SatisfiedEmployee", session.getEjbName());
                assertEquals(Session.SESSION_TYPE_STATEFUL, session.getSessionType());
                assertEquals(1, session.getBusinessLocal().length);
                assertEquals("foo.EmployeeLocal", session.getBusinessLocal()[0]);
                assertEquals(1, session.getBusinessRemote().length);
                assertEquals("foo.EmployeeRemote", session.getBusinessRemote()[0]);
                // test Employer
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Employer");
                assertEquals("foo.Employer", session.getEjbClass());
                assertEquals("Employer", session.getEjbName());
                assertEquals(Session.SESSION_TYPE_STATEFUL, session.getSessionType());
                assertEquals(1, session.getBusinessLocal().length);
                assertEquals("foo.EmployerLocal", session.getBusinessLocal()[0]);
                assertEquals(2, session.getBusinessRemote().length);
                assertEquals("foo.EmployerRemote", session.getBusinessRemote()[0]);
                assertEquals("foo.EmployerRemoteAdvanced", session.getBusinessRemote()[1]);
                return null;
            }
        });

    }
    
    public void testGetEjbRef() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooLocal.java",
                "package foo;" +
                "public interface FooLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooLocal2.java",
                "package foo;" +
                "public interface FooLocal2 {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooRemote.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Remote()" +
                "public interface FooRemote {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo1.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo1 implements FooLocal {" +
                "@EJB()" +
                "private FooLocal run;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo2.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo2 implements FooLocal {" +
                "@EJB()" +
                "private void setIter(FooRemote it) {};" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo3.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo3 implements FooLocal {" +
                "@EJB(beanInterface=FooLocal.class, description=\"dsc\", mappedName=\"mFoo\", beanName=\"bFoo\", name=\"Foo\")" +
                "private FooRemote prop;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo4.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo4 implements FooLocal {" +
                "@EJB(beanInterface=FooLocal.class, description=\"dsc\", mappedName=\"mFoo\", beanName=\"bFoo\", name=\"Foo\")" +
                "private void setIter(FooRemote it) {};" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo5.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@EJB(beanInterface=FooLocal.class, description=\"dsc1\", mappedName=\"mFoo\", beanName=\"bFoo\", name=\"Foo\")" +
                "@Stateless()" +
                "public class Foo5 implements FooLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo6.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@EJBs({" +
                "@EJB(beanInterface=FooLocal.class, description=\"dsc1\", mappedName=\"mFoo1\", beanName=\"bFoo1\", name=\"Foo1\")," +
                "@EJB(beanInterface=FooRemote.class, description=\"dsc2\", mappedName=\"mFoo2\", beanName=\"bFoo2\", name=\"Foo2\")," +
                "@EJB(beanInterface=FooLocal2.class, description=\"dsc3\", mappedName=\"mFoo3\", beanName=\"bFoo3\", name=\"Foo3\")" +
                "})" +
                "@Stateless()" +
                "public class Foo6 implements FooLocal {" +
                "}");
        
        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Foo1
                Session session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo1");
                assertEquals(1, session.getEjbLocalRef().length);
                // test Foo2
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo2");
                assertEquals(1, session.getEjbRef().length);
                // test Foo3
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo3");
                assertEquals(1, session.getEjbRef().length);
                // test Foo4
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo4");
                assertEquals(1, session.getEjbRef().length);
                // test Foo5
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo5");
                assertEquals(1, session.getEjbLocalRef().length);
                // test Foo6
                session = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo6");
                assertEquals(2, session.getEjbLocalRef().length);
                assertEquals(1, session.getEjbRef().length);
                
                return null;
            }
        });

    }
    public void testGetServiceRef() throws IOException, InterruptedException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooService.java",
                "package foo;" +
                "@javax.jws.WebService()" +
                "public class FooService {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooServicePort.java",
                "package foo;" +
                "public interface FooServicePort {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooServicePort1.java",
                "package foo;" +
                "public interface FooServicePort1 {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/FooLocal.java",
                "package foo;" +
                "public interface FooLocal {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo1.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo1 implements FooLocal {" +
                "@javax.xml.ws.WebServiceRef()" +
                "private FooService service;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo2.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo2 implements FooLocal {" +
                "@javax.xml.ws.WebServiceRef(value=foo.FooService.class, name=\"service/Foo\", wsdlLocation=\"http://www.netbeans.org/FooService?wsdl\")" +
                "private FooServicePort port;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo3.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo3 implements FooLocal {" +
                "@javax.xml.ws.WebServiceRef(foo.FooService.class)" +
                "private FooServicePort port;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Foo4.java",
                "package foo;" +
                "import javax.ejb.*;" +
                "@Stateless()" +
                "public class Foo4 implements FooLocal {" +
                "@javax.xml.ws.WebServiceRef(name=\"service/Foo\",value=foo.FooService.class)" +
                "private FooServicePort port;" +
                "@javax.xml.ws.WebServiceRef(name=\"service/Foo\",value=foo.FooService.class)" +
                "private FooServicePort1 port1;" +
                "}");
        
        createEjbJarModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws VersionNotSupportedException {
                // test Foo1
                Session session1 = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo1");
                assertEquals(1, session1.getServiceRef().length);
                assertEquals(1, session1.sizeServiceRef());
                ServiceRef serviceRef1 = session1.getServiceRef(0);
                assertEquals("service/FooService", serviceRef1.getServiceRefName());
                assertEquals("foo.FooService", serviceRef1.getServiceInterface());
                assertNull(serviceRef1.getWsdlFile());
                assertEquals(0,serviceRef1.sizePortComponentRef());
 
                Session session2 = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo2");
                assertEquals(1, session2.getServiceRef().length);
                assertEquals(1, session2.sizeServiceRef());
                ServiceRef serviceRef2 = session2.getServiceRef(0);
                assertEquals("service/Foo", serviceRef2.getServiceRefName());
                assertEquals("foo.FooService", serviceRef2.getServiceInterface());
                assertEquals("http://www.netbeans.org/FooService?wsdl",serviceRef2.getWsdlFile().toASCIIString());
                assertEquals(1,serviceRef2.sizePortComponentRef());
                PortComponentRef portComponentRef2 = serviceRef2.getPortComponentRef(0);
                assertEquals("foo.FooServicePort",portComponentRef2.getServiceEndpointInterface());

                Session session3 = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo3");
                assertEquals(1, session3.getServiceRef().length);
                assertEquals(1, session3.sizeServiceRef());
                ServiceRef serviceRef3 = session3.getServiceRef(0);
                assertEquals("service/FooService", serviceRef3.getServiceRefName());
                assertEquals("foo.FooService", serviceRef3.getServiceInterface());
                assertEquals(1,serviceRef3.sizePortComponentRef());
                PortComponentRef portComponentRef3 = serviceRef3.getPortComponentRef(0);
                assertEquals("foo.FooServicePort",portComponentRef3.getServiceEndpointInterface());
                
                Session session4 = (Session) getEjbByEjbName(metadata.getRoot().getEnterpriseBeans().getSession(), "Foo4");
                assertEquals(1, session4.getServiceRef().length);
                assertEquals(1, session4.sizeServiceRef());
                ServiceRef serviceRef4 = session4.getServiceRef(0);
                assertEquals("service/Foo", serviceRef4.getServiceRefName());
                assertEquals("foo.FooService", serviceRef4.getServiceInterface());
                assertEquals(2,serviceRef4.sizePortComponentRef());
                PortComponentRef portComponentRef4 = serviceRef4.getPortComponentRef(0);
                assertEquals("foo.FooServicePort",portComponentRef4.getServiceEndpointInterface());
                PortComponentRef portComponentRef41 = serviceRef4.getPortComponentRef(1);
                assertEquals("foo.FooServicePort1",portComponentRef41.getServiceEndpointInterface());
                return null;
            }
        });

    }

}
