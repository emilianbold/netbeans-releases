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
    
    private TestModule referencedModule;
    private EjbReference ejbReference;
        
    public CallEjbGeneratorTest(String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected void setUp() throws IOException {
        super.setUp();
        this.referencedModule = createEjb21Module();
        this.ejbReference = EjbReference.create(
            "statelesslr.StatelessLRBean",
            EjbRef.EJB_REF_TYPE_SESSION,
            "statelesslr.StatelessLRLocal",
            "statelesslr.StatelessLRLocalHome",
            "statelesslr.StatelessLRRemote",
            "statelesslr.StatelessLRRemoteHome",
            referencedModule.getEjbModule()
            );
    }
    
    public void testAddReference_LocalEE14FromEjbEE14() throws IOException {
        TestModule referencingModule = createTestModule("EJBModule2_1_4", EjbProjectConstants.J2EE_14_LEVEL);
        
        FileObject referencingFO = referencingModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean2.java");
        
        CallEjbGenerator generator = CallEjbGenerator.create(ejbReference, "StatelessLRBean", true);
        generator.addReference(
                referencingFO,
                "statelesslr.StatelessLRBean2",
                referencedModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java"),
                "statelesslr.StatelessLRBean",
                null,
                false,
                false,
                referencedModule.getProject()
                );
        
        EnterpriseReferenceContainerImpl erc = referencingModule.getEnterpriseReferenceContainerImpl();
        assertNotNull(erc.getLocalEjbReference());
        assertEquals("StatelessLRBean", erc.getLocalEjbRefName());
        assertEquals("statelesslr.StatelessLRBean2", erc.getLocalReferencingClass());

        final String generatedMethodBody =
        "{\n" +
        "    try {\n" +
        "        javax.naming.Context c = new javax.naming.InitialContext();\n" +
        "        statelesslr.StatelessLRLocalHome rv = (statelesslr.StatelessLRLocalHome)c.lookup(\"java:comp/env/StatelessLRBean\");\n" +
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
                ExecutableElement method = getMethod(sourceUtils.getTypeElement(), "lookupStatelessLRBean");
                assertNotNull(method);
                MethodTree methodTree = controller.getTrees().getTree(method);
                assertEquals(generatedMethodBody, methodTree.getBody().toString());
            }
        }, true);
        
    }
    
}
