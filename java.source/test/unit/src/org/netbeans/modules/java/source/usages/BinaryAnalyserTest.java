/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.usages;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.BinaryAnalyser.Result;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class BinaryAnalyserTest extends NbTestCase {

    public BinaryAnalyserTest(String name) {
        super(name);
    }

    @Override
    public void setUp() {
        System.setProperty("org.netbeans.modules.java.source.usages.BinaryAnalyser.fullIndex", "true");
    }

    public void testAnnotationsIndexed() throws Exception {
        ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void>() {
            public Void run() throws IOException, InterruptedException {
                FileObject workDir = SourceUtilsTestUtil.makeScratchDir(BinaryAnalyserTest.this);
                FileObject indexDir = workDir.createFolder("index");
                File binaryAnalyzerDataDir = new File(getDataDir(), "Annotations.jar");

                Index index = LuceneIndex.create(FileUtil.toFile(indexDir));
                BinaryAnalyser a = new BinaryAnalyser(index);

                assertEquals(Result.FINISHED, a.start(FileUtil.getArchiveRoot(binaryAnalyzerDataDir.toURI().toURL()), new AtomicBoolean(), new AtomicBoolean()));

                a.finish();

                assertReference(index, "annotations.NoArgAnnotation", "usages.ClassAnnotations", "usages.MethodAnnotations", "usages.FieldAnnotations");
                assertReference(index, "annotations.ArrayOfStringArgAnnotation", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");
                assertReference(index, "annotations.TestEnum", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");
                assertReference(index, "java.util.List", "usages.ClassAnnotations", "usages.ClassArrayAnnotations", "usages.MethodAnnotations", "usages.MethodArrayAnnotations", "usages.FieldAnnotations", "usages.FieldArrayAnnotations");

                return null;
            }
        });
    }

    private void assertReference(Index index, String refered, String... in) throws IOException, InterruptedException {
        List<String> result = index.getUsagesFQN(refered, EnumSet.of(UsageType.TYPE_REFERENCE), Index.BooleanOperator.AND);

        assertTrue(result.containsAll(Arrays.asList(in)));
    }

}
