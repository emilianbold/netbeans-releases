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

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.MethodTree;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.api.ejbjar.EjbReference;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.ejbcore.test.EnterpriseReferenceContainerImpl;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class CallEjbGeneratorTest extends TestBase {
    
    private TestModule referencedEjb21Module;
    private EjbReference ejbReference;
        
    public CallEjbGeneratorTest(String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        this.referencedEjb21Module = createTestModule("EJBModule2_1_4", EjbProjectConstants.J2EE_14_LEVEL);
        this.ejbReference = EjbReference.create(
            "statelesslr.StatelessLRBean2",
            EjbRef.EJB_REF_TYPE_SESSION,
            "statelesslr.StatelessLRLocal2",
            "statelesslr.StatelessLRLocalHome2",
            "statelesslr.StatelessLRRemote2",
            "statelesslr.StatelessLRRemoteHome2",
            referencedEjb21Module.getEjbModule()
            );
    }
    
    public void testAddReference_LocalEE14FromEjbEE14() throws IOException {
        TestModule referencingModule = createEjb21Module();
        
        FileObject referencingFO = referencingModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        
        CallEjbGenerator generator = CallEjbGenerator.create(ejbReference, "StatelessLRBean2", true);
        generator.addReference(
                referencingFO,
                "statelesslr.StatelessLRBean",
                referencedEjb21Module.getSources()[0].getFileObject("statelesslr/StatelessLRBean2.java"),
                "statelesslr.StatelessLRBean2",
                null,
                false,
                false,
                referencedEjb21Module.getProject()
                );
        
        EnterpriseReferenceContainerImpl erc = referencingModule.getEnterpriseReferenceContainerImpl();
        assertNotNull(erc.getLocalEjbReference());
        assertEquals("StatelessLRBean2", erc.getLocalEjbRefName());
        assertEquals("statelesslr.StatelessLRBean", erc.getLocalReferencingClass());

        final String generatedMethodBody =
        "{\n" +
        "    try {\n" +
        "        javax.naming.Context c = new javax.naming.InitialContext();\n" +
        "        statelesslr.StatelessLRLocalHome2 rv = (statelesslr.StatelessLRLocalHome2)c.lookup(\"java:comp/env/StatelessLRBean2\");\n" +
        "        return rv.create();\n" +
        "    } catch (javax.naming.NamingException ne) {\n" +
        "        java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, \"exception caught\", ne);\n" +
        "        throw new RuntimeException(ne);\n" +
        "    } catch (javax.ejb.CreateException ce) {\n" +
        "        java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, \"exception caught\", ce);\n" +
        "        throw new RuntimeException(ce);\n" +
        "    }\n" +
        "}";
        
        JavaSource javaSource = JavaSource.forFileObject(referencingFO);
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.PARSED);
                SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                ExecutableElement method = getMethod(sourceUtils.getTypeElement(), "lookupStatelessLRBean2");
                assertNotNull(method);
                MethodTree methodTree = controller.getTrees().getTree(method);
                assertEquals(generatedMethodBody, methodTree.getBody().toString());
            }
        }, true);
        
    }
    
}
