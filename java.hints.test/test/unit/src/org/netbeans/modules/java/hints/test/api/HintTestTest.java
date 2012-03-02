/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.test.api;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class HintTestTest {

    public HintTestTest() {
    }

    @Test
    public void testTestingNonJavaChanges() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test { }\n")
                .input("test/test.txt", "1\n2\n", false)
                .run(HintTestTest.class)
                .findWarning("1:13-1:17:verifier:Test")
                .applyFix()
                .assertVerbatimOutput("test/test.txt", "2\n3\n");
    }

    @Hint(displayName="testingNonJavaChanges", description="testingNonJavaChanges", category="test")
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription testingNonJavaChanges(HintContext ctx) {
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "Test", new TestingNonJavaChangesFix(ctx.getInfo(), ctx.getPath()).toEditorFix());
    }

    private static final class TestingNonJavaChangesFix extends JavaFix {

        public TestingNonJavaChangesFix(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override protected String getText() {
            return "Test";
        }

        @Override protected void performRewrite(TransformationContext ctx) {
            try {
                FileObject resource = ctx.getWorkingCopy().getFileObject().getParent().getFileObject("test.txt");
                Assert.assertNotNull(resource);
                Reader r = new InputStreamReader(ctx.getResourceContent(resource), "UTF-8");
                ByteArrayOutputStream outData = new ByteArrayOutputStream();
                Writer w = new OutputStreamWriter(outData, "UTF-8");
                int read;

                while ((read = r.read()) != -1) {
                    if (read != '\n') read++;
                    w.write(read);
                }

                r.close();
                w.close();

                OutputStream out = ctx.getResourceOutput(resource);

                out.write(outData.toByteArray());

                out.close();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

    }
}
