/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote;

import java.util.Collection;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.api.remote.RemoteFileTestCase;
import org.netbeans.modules.cnd.remote.mapper.MappingsTestCase;
import org.netbeans.modules.cnd.remote.support.DownloadTestCase;
import org.netbeans.modules.cnd.remote.sync.RemoteBinaryServiceTestCase;
import org.netbeans.modules.cnd.remote.support.RemoteUtilTestCase;
import org.netbeans.modules.cnd.remote.support.ServerListTestCase;
import org.netbeans.modules.cnd.remote.support.TransportTestCase;
import org.netbeans.modules.cnd.remote.support.UploadTestCase;
import org.netbeans.modules.cnd.remote.sync.ZipSyncWorkerTestCase;
import org.netbeans.modules.cnd.remote.ui.wizard.HostSetupTestCase;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;

/**
 *
 * @author Sergey Grinev
 */
public class RemoteDevelopmentTest extends CndBaseTestSuite {

//    static {
//        System.setProperty("cnd.remote.testuserinfo", "rdtest:********@endif.russia");
//        System.setProperty("cnd.remote.logger.level", "0");
//        System.setProperty("nativeexecution.support.logger.level", "0");
//    }

    public static final String PLATFORMS_SECTION = "remote.platforms";

    public RemoteDevelopmentTest(Class testClass) {
        this(testClass.getName(), testClass);
    }

    // Why are tests just Test, not NativeExecutionBaseTestCase?
    // to allow add warnings (TestSuite.warning() returns test stub with warning)
    public RemoteDevelopmentTest(String name, Test... tests) {
        setName(name);
        for (Test test : tests) {
            addTest(test);
        }
    }

    // Why are tests just Test, not NativeExecutionBaseTestCase?
    // to allow add warnings (TestSuite.warning() returns test stub with warning)
    public RemoteDevelopmentTest(String name, Collection<Test> tests) {
        setName(name);
        for (Test test : tests) {
            addTest(test);
        }
    }

    public RemoteDevelopmentTest() {
        this("Remote Development", // NOI18N
             MappingsTestCase.class,
             TransportTestCase.class,
             UploadTestCase.class,
             DownloadTestCase.class,
             RemoteBinaryServiceTestCase.class,
             RemoteUtilTestCase.class,
             ServerListTestCase.class,
             ZipSyncWorkerTestCase.class,
             HostSetupTestCase.class,
             RemoteFileTestCase.class);
    }


    private RemoteDevelopmentTest(String name, Class... testClasses) {
        super(name, PLATFORMS_SECTION, testClasses);
    }

    public static Test suite() {
        TestSuite suite = new RemoteDevelopmentTest();
        return suite;
    }
}
