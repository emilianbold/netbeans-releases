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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.insync.java;

import com.sun.crypto.provider.AESCipher;
import com.sun.source.tree.ExpressionTree;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.visualweb.insync.InsyncTestBase;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author jdeva
 */
public class ExpressionUtilsTest extends InsyncTestBase {
    public ExpressionUtilsTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(ExpressionUtilsTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }   

    Method getInitMethod() {
        FileObject fObj = getJavaFile(getPageBeans()[0]);
        JavaClass javaClass = JavaClass.getJavaClass(fObj);
        return javaClass.getMethod("_init", new Class[]{});        
    }
    
    /**
     * Test of getValue method, of class ExpressionUtils.
     */
    public void testGetValue() {
        System.out.println("getValue");
        String[] types = {
            "com.sun.rave.faces.converter.SqlDateConverter",
            "com.sun.webui.jsf.model.SingleSelectOptionsList"
        };
        createBeans(types);     
        Method initMethod = getInitMethod();
        String body = 
        "sqlDateConverter1.setTimeStyle(\"medium\");\n" +
        "sqlDateConverter1.setTimeZone(java.util.TimeZone.getTimeZone(\"America/Mendoza\"))" +
        "singleSelectOptionsList1.setSelectedValue(\"item1\");" +
        "singleSelectOptionsList1.setOptions(new com.sun.webui.jsf.model.Option[]{new com.sun.webui.jsf.model.Option(\"item1\", \"Item 1\")," + 
        "new com.sun.webui.jsf.model.Option(\"item2\", \"Item 2\")});\n";        
        initMethod.replaceBody(body);
        Object[] results = {
            "medium",
            java.util.TimeZone.getTimeZone("America/Mendoza"),
            "item1",
            new com.sun.webui.jsf.model.Option[]{new com.sun.webui.jsf.model.Option("item1", "Item 1"), 
            new com.sun.webui.jsf.model.Option("item2", "Item 2")}
        };        
        
        for(int i = 0;i < results.length-1; i++) {
            Object result = testGetValueImpl(initMethod.getPropertySetStatements().get(i), 
                    initMethod.getJavaClass().getFileObject());
            assertEquals(result, results[i]);
        }
        Object result = testGetValueImpl(initMethod.getPropertySetStatements().get(results.length-1), 
                initMethod.getJavaClass().getFileObject());
        assert(result.getClass().isArray());
    }
    
    private Object testGetValueImpl(final Statement stmt, FileObject fileObject) {
        return ReadTaskWrapper.execute(new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                java.lang.reflect.Method m = null;
                ExpressionTree exprTree = null;
                try {
                    m = stmt.getClass().getDeclaredMethod("getArgument", new Class[]{CompilationInfo.class});
                    m.setAccessible(true);
                    exprTree = (ExpressionTree) m.invoke(stmt, new Object[]{cinfo});
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                return ExpressionUtils.getValue(cinfo, exprTree);
            }
        }, fileObject);
    }

    /**
     * Test of getArgumentSource method, of class ExpressionUtils.
     */
    public void testGetArgumentSource() {
        System.out.println("getArgumentSource");

        String[] types = {
            "com.sun.rave.faces.converter.SqlDateConverter"
        };
        String[] results = {
            "\"medium\"",
            "java.util.TimeZone.getTimeZone(\"America/Mendoza\")"
        };
        createBeans(types);     
        Method initMethod = getInitMethod();
        String body = 
        "sqlDateConverter1.setTimeStyle(\"medium\");\n" +
        "sqlDateConverter1.setTimeZone(java.util.TimeZone.getTimeZone(\"America/Mendoza\"))";
        
        initMethod.replaceBody(body);
        
        for(int i = 0;i < results.length; i++) {
            Object result = testGetArgumentSourceImpl(initMethod.getPropertySetStatements().get(i), 
                    initMethod.getJavaClass().getFileObject());
            assertEquals(result, results[i]);
        }
    }
    
    private Object testGetArgumentSourceImpl(final Statement stmt, FileObject fileObject) {
        return ReadTaskWrapper.execute(new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                java.lang.reflect.Method m = null;
                ExpressionTree exprTree = null;
                try {
                    m = stmt.getClass().getDeclaredMethod("getArgument", new Class[]{CompilationInfo.class});
                    m.setAccessible(true);
                    exprTree = (ExpressionTree) m.invoke(stmt, new Object[]{cinfo});
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                return ExpressionUtils.getArgumentSource(cinfo, exprTree);
            }
        }, fileObject);
    }    
}
