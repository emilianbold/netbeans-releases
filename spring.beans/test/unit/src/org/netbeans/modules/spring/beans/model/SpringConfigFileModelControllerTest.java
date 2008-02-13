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

package org.netbeans.modules.spring.beans.model;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.TestUtils;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.DocumentRead;
import org.netbeans.modules.spring.beans.model.SpringConfigFileModelController.DocumentWrite;
import org.netbeans.modules.spring.beans.model.impl.ConfigFileSpringBeanSource;

/**
 *
 * @author Andrei Badea
 */
public class SpringConfigFileModelControllerTest extends ConfigFileTestCase {

    public SpringConfigFileModelControllerTest(String testName) {
        super(testName);
    }

    public void testReadParse() throws Exception {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);

        final ParseCountingBeanSource beanSource = new ParseCountingBeanSource();
        final SpringConfigFileModelController controller = new SpringConfigFileModelController(configFile, beanSource);

        ExclusiveAccess.getInstance().runSyncTask(new  Callable<Void>() {
            public Void call() throws IOException {
                DocumentRead docRead = controller.getDocumentRead();
                docRead.getBeanSource();
                // Document was parsed, since this was the very first access.
                assertEquals(1, beanSource.getAndResetParseCount());
                docRead.getBeanSource();
                docRead.getBeanSource();
                // Subsequence getBeanSource() call should not cause any parsing.
                assertEquals(0, beanSource.getAndResetParseCount());
                docRead = controller.getDocumentRead();
                docRead.getBeanSource();
                // Subsequent getDocumentRead() calls should not cause any parsing,
                // since there have been no changes to the file.
                assertEquals(0, beanSource.getAndResetParseCount());
                return null;
            }
        });
    }

    public void testWriteParse() throws Exception {
        String contents = TestUtils.createXMLConfigText("");
        TestUtils.copyStringToFile(contents, configFile);

        final ParseCountingBeanSource beanSource = new ParseCountingBeanSource();
        final SpringConfigFileModelController controller = new SpringConfigFileModelController(configFile, beanSource);

        ExclusiveAccess.getInstance().runSyncTask(new  Callable<Void>() {
            public Void call() throws IOException {
                beanSource.getAndResetParseCount();
                DocumentWrite docWrite = controller.getDocumentWrite();
                docWrite.open();
                try {
                    docWrite.getBeanSource();
                    // Document was parsed, since this was the very first access.
                    assertEquals(1, beanSource.getAndResetParseCount());
                    docWrite.getBeanSource();
                    docWrite.getBeanSource();
                    // Subsequence getBeanSource() call should not cause any parsing.
                    assertEquals(0, beanSource.getAndResetParseCount());
                } finally {
                    docWrite.close();
                }
                docWrite = controller.getDocumentWrite();
                docWrite.open();
                try {
                    docWrite.getBeanSource();
                    // Subsequent getDocumentWrite() should parse, even though the file was not changed.
                    assertEquals(1, beanSource.getAndResetParseCount());
                } finally {
                    docWrite.close();
                }
                return null;
            }
        });
    };

    private static final class ParseCountingBeanSource extends ConfigFileSpringBeanSource {

        private int parseCount;

        @Override
        public void parse(BaseDocument document) throws IOException {
            parseCount++;
            super.parse(document);
        }

        public int getAndResetParseCount() {
            int result = parseCount;
            parseCount = 0;
            return result;
        }
    }
}
