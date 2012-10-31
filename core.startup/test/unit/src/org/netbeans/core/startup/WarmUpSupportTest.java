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
package org.netbeans.core.startup;

import java.awt.EventQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class WarmUpSupportTest extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(WarmUpSupportTest.class.getName());
    public static final CountDownLatch in = new CountDownLatch(1);
    
    public WarmUpSupportTest(String s) {
        super(s);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }
    
    public static Test suite() {
        System.setProperty("warmup.delay", "6000");
        System.setProperty("warmup.success", "not yet");
        NbTestSuite s = new NbTestSuite();
        s.addTest(NbModuleSuite.emptyConfiguration().addTest(WarmUpSupportTest.class, "testEmpty").
            gui(true).suite()
        );
        s.addTest(new WarmUpSupportTest("testVerifyProperty"));
        return s;
    }

    public void testEmpty() throws Exception {
        in.await();
    }
    
    public void testVerifyProperty() throws Exception {
        LOG.info("testVerifyProperty");
        for (int i = 0; i < 10 && !"in edt".equals(System.getProperty("warmup.success")); i++) {
            LOG.log(Level.INFO, "Jump to EDT, round {0}", i);
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                }
            });
            LOG.info("Wait a second");
            Thread.sleep(1000);
            LOG.info("Wait is over");
        }
        assertEquals("The WarmUpSupportTask was executed", "in edt", System.getProperty("warmup.success"));
    }
}
