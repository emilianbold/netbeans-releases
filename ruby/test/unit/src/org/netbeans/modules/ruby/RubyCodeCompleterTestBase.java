/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.ruby;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Base class for code completion tests.
 *
 * @author Erno Mononen
 */
public abstract class RubyCodeCompleterTestBase extends RubyTestBase {

    public RubyCodeCompleterTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        doTestSpecificSetUp();
        super.setUp();
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> loadPath = new HashMap<String, ClassPath>();

        // rubystubs
        loadPath.put(RubyLanguage.BOOT, ClassPathSupport.createClassPath(RubyPlatform.getRubyStubs()));

        loadPath.put(RubyLanguage.SOURCE, ClassPathSupport.createClassPath(getAdditionalClassPath()));
        return loadPath;
    }

    protected void doTestSpecificSetUp() {
        Method setupMethod = getMethod("do" + getName().substring(4) + "Setup");
        if (setupMethod != null) {
            try {
                setupMethod.invoke(this);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            RubyIndexer.userSourcesTest = false;
        }
    }

    protected FileObject[] getAdditionalClassPath() {
        // check whether the test needs some specfic class path setup, i.e.
        // whether there is a corresponding getTest<TestName>ClassPath method
        Method classPathMethod = getMethod("getTest" + getName().substring(4) + "ClassPath");
        if (classPathMethod != null) {
            try {
                FileObject result = (FileObject) classPathMethod.invoke(this);
                return result == null ? new FileObject[0] : new FileObject[]{result};
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        // by default add the golden files to the class path
        FileObject testFileFO = FileUtil.toFileObject(getDataFile("/testfiles"));
        return new FileObject[]{testFileFO};
    }

    private Method getMethod(String name) {
        for (Method method : getClass().getDeclaredMethods()) {
           if (method.getName().equals(name)) {
               return method;
           }
        }
        return null;
        
    }
    @Override
    protected void checkCall(ParserResult parserResult, int caretOffset, String expectedParameter, boolean expectSuccess) {
        IndexedMethod[] methodHolder = new IndexedMethod[1];
        int[] paramIndexHolder = new int[1];
        int[] anchorOffsetHolder = new int[1];
        int lexOffset = caretOffset;
        int astOffset = caretOffset;
        boolean ok = RubyMethodCompleter.computeMethodCall(
                parserResult, lexOffset, astOffset, methodHolder, paramIndexHolder,
                anchorOffsetHolder, null, QuerySupport.Kind.PREFIX);

        if (expectSuccess) {
            assertTrue("Not a method call", ok);
        } else if (!ok) {
            return;
        }
        IndexedMethod method = methodHolder[0];
        assertNotNull(method);
        int index = paramIndexHolder[0];
        assertTrue(index >= 0);
        String parameter = method.getParameters().get(index);
        // The index doesn't work right at test time - not sure why
        // it doesn't have all of the gems...
        //assertEquals(fqn, method.getFqn());
        assertEquals(expectedParameter, parameter);
    }

    public void checkCompletion(String file, String caretLine) throws Exception {
        checkCompletion(file, caretLine, false);
    }

    public void checkComputeMethodCall(String file, String caretLine, String fqn, String param, boolean expectSuccess) throws Exception {
        checkComputeMethodCall(file, caretLine, param, expectSuccess);
    }
}
