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
package org.netbeans.modules.cnd.repository.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CheckGetAfterRemoveTest extends GetPutTestBase {

    public CheckGetAfterRemoveTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(CheckGetAfterRemoveTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetAfterRemove() throws InterruptedException {
        SmallKey smallKey = new SmallKey("small_1");
        LargeKey largeKey = new LargeKey("large_1");
        RepositoryAccessor.getRepository().openUnit(smallKey.getUnitId(), smallKey.getUnit());
        RepositoryAccessor.getRepository().openUnit(largeKey.getUnitId(), smallKey.getUnit());
        _test(smallKey, new Value("small_obj_1"));
        _test(largeKey, new Value("large_obj_1"));
    }
    private final AtomicBoolean readFlag = new AtomicBoolean(false);
    private volatile CountDownLatch writeLatch;

    @Override
    protected void onReadHook(Factory factory, Persistent obj) {
        readFlag.set(true);
    }

    @Override
    protected void onWriteHook(Factory factory, Persistent obj) {
        sleep(1000);
        writeLatch.countDown();
    }

    private void _test(Key key, Value value) throws InterruptedException {
        writeLatch = new CountDownLatch(1);
        repository.startup(0);
        repository.put(key, value);

        Persistent v2 = repository.get(key);

        assertNotNull(v2);
        assertEquals(value, v2);

        writeLatch.await();
        long time = System.currentTimeMillis();
        readFlag.set(false);
        repository.remove(key);
        while ((v2 = repository.get(key)) != null) {
            assertFalse("get shouldn't cause reading object from disk after remove", readFlag.get());
            assertNotNull(v2);
            assertEquals(value, v2);
            if (System.currentTimeMillis() - time > 30000) {
                break;
            }
        }
        assertFalse("get shouldn't cause reading object from disk after remove", readFlag.get());
        repository.debugClear();
    }
}
