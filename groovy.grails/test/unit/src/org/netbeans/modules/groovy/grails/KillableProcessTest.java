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

package org.netbeans.modules.groovy.grails;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class KillableProcessTest extends NbTestCase {

    public KillableProcessTest(String name) {
        super(name);
    }

    public void testPidLineProcessor() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String line = "cmd /c \"\"C:\\Program Files (x86)\\Java stuff\\Grails-1.1\\bin\\grails.bat\" run-app REM NB:0D:\\sandbox\\grails\\Webapp\"  6008       ";
        KillableProcess.PidLineProcessor processor = new KillableProcess.PidLineProcessor(latch,
                "run-app", "0D:\\sandbox\\grails\\Webapp");
        processor.processLine(line);
        latch.await(10, TimeUnit.MILLISECONDS);
        assertEquals(6008, processor.getPid());

        latch = new CountDownLatch(1);
        line = "cmd /c C:\\software\\grails-1.1\\bin\\grails.bat run-app REM NB:0C:\\Users\\sickboy\\Documents\\NetBeansProjects\\GrailsApplication2  6116";
        processor = new KillableProcess.PidLineProcessor(latch,
                "run-app", "0C:\\Users\\sickboy\\Documents\\NetBeansProjects\\GrailsApplication2");
        processor.processLine(line);
        latch.await(10, TimeUnit.MILLISECONDS);
        assertEquals(6116, processor.getPid());

        latch = new CountDownLatch(1);
        line = "cmd /c \"\"C:\\Program Files (x86)\\Java stuff\\Grails-1.1\\bin\\grails.bat\" dev run-app REM NB:0D:\\sandbox\\web-app\"  7596       ";
        processor = new KillableProcess.PidLineProcessor(latch,
                "run-app", "0D:\\sandbox\\web-app");
        processor.processLine(line);
        latch.await(10, TimeUnit.MILLISECONDS);
        assertEquals(7596, processor.getPid());
    }
}
