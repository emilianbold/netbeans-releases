/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.code.Symbol;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 */
public class Issue247469Test extends NbTestCase {

    public Issue247469Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        clearWorkDir();
        FileUtil.setMIMEType(FileObjects.CLASS, ClassParser.MIME_TYPE);
        MockMimeLookup.setInstances(MimePath.get(ClassParser.MIME_TYPE), new ClassParserFactory());
        TestJavaPlatformProviderImpl.ALLOW_INSTALL_FOLDERS = true;
//        System.setProperty("CachingArchiveProvider.disableCtSym", "true");
    }

    @Override
    protected void tearDown() throws Exception {
        TestJavaPlatformProviderImpl.ALLOW_INSTALL_FOLDERS = false;
        super.tearDown();
    }

    public void testIssue247469() throws IOException {
        final JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
        assertNotNull(jp);
        final ClasspathInfo cpInfo = ClasspathInfo.create(jp.getBootstrapLibraries(), ClassPath.EMPTY, ClassPath.EMPTY);
        assertNotNull(cpInfo);
        final JavaSource js = JavaSource.create(cpInfo);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(final CompilationController cc) throws Exception {
                final PackageElement packageElement = cc.getElements().getPackageElement("java.lang"); // NOI18N
                for (Element elem : packageElement.getEnclosedElements()) {
                    System.out.println(elem.getSimpleName().toString() + " completer: " + ((Symbol)elem).completer + elem.asType().getKind());
                    if ("ProcessBuilder$1".equals(elem.getSimpleName().toString())) { // NOI18N
                        TypeElement te = (TypeElement) elem;
                        assertEquals(NestingKind.ANONYMOUS, te.getNestingKind());
                        break;
                    }
                }
            }
        }, true);
    }
}
