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

package org.netbeans.modules.spring.api.beans;

import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.ConfigFileManagerAccessor;
import org.netbeans.modules.spring.beans.ConfigFileTestCase;
import org.netbeans.modules.spring.beans.SpringScopeAccessor;
import org.netbeans.modules.spring.beans.TestUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class SpringScopeTest extends ConfigFileTestCase {

    public SpringScopeTest(String testName) {
        super(testName);
    }

    public void testGetConfigModel() throws Exception {
        String contents = TestUtils.createXMLConfigText("<bean id='foo' name='bar baz' class='org.example.Foo'/>");
        TestUtils.copyStringToFile(contents, configFile);
        FileObject configFO = FileUtil.toFileObject(configFile);
        ConfigFileManager manager = ConfigFileManagerAccessor.DEFAULT.createConfigFileManager(new DefaultConfigFileManagerImpl());
        SpringScope scope = SpringScopeAccessor.DEFAULT.createSpringScope(manager);

        SpringConfigModel model = SpringScopeAccessor.DEFAULT.getConfigModel(scope, configFO);
        final int[] beanCount = { 0 };
        model.runReadAction(new Action<SpringBeans>() {
            public void run(SpringBeans parameter) {
                beanCount[0] = parameter.getBeans(configFile).size();
            }
        });
        assertEquals(1, beanCount[0]);
        assertEquals(1, scope.file2AdHocModel.size());

        SpringConfigModel anotherModel = SpringScopeAccessor.DEFAULT.getConfigModel(scope, configFO);
        assertSame(model, anotherModel);

        FileLock lock = configFO.lock();
        try {
            configFO.rename(lock, "tmp", "xml");
        } finally {
            lock.releaseLock();
        }
        assertEquals(0, scope.file2AdHocModel.size());
    }
}
