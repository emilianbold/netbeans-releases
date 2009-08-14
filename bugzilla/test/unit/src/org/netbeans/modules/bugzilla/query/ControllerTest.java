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

package org.netbeans.modules.bugzilla.query;

import java.util.logging.LogRecord;
import org.netbeans.modules.bugzilla.*;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;

/**
 *
 * @author tomas
 */
public class ControllerTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME = "Beautiful";
    private static String QUERY_NAME = "Hilarious";

    public ControllerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testParameters() throws MalformedURLException, CoreException, InterruptedException {
        LogHandler h = new LogHandler("Finnished populate query controller");
        Bugzilla.LOG.addHandler(h);
        String parametersUrl = getParametersUrl();
        BugzillaQuery q = new BugzillaQuery(QUERY_NAME, QueryTestUtil.getRepository(), parametersUrl, true, false, true);
        QueryController c = q.getController();
        
        // wait while populate
        int timeout = 60000;
        long ts = System.currentTimeMillis();
        while(!h.done) {
            Thread.sleep(500);
            if(ts + timeout < System.currentTimeMillis()) throw new IllegalStateException("timeout");
        }

        String[] parametersGiven = parametersUrl.split("&");
        String params = c.getUrlParameters();
        assertTrue(params.startsWith("&"));
        params = params.substring(1, params.length() - 1);
        String[] parametersReturned = params.split("&");
        assertEquals(parametersGiven.length, parametersReturned.length);

        Set<String> s = new HashSet<String>(parametersReturned.length);
        for (String string : parametersReturned) {
            s.add(string);
        }
        for (int i = 1; i < parametersGiven.length; i++) { // skip the first elemenent - its = ""
            String p = parametersReturned[i];
            if(!s.contains(p)) {
                fail("missing parameter [" + p + "]");
            }
        }
    }

    private String getParametersUrl() {
        return  "&short_desc_type=allwordssubstr&short_desc=xxx" +
                "&product=TestProduct" +
                "&component=TestComponent" +
                "&version=unspecified" +
                "&long_desc_type=substring&long_desc=xxx" +
                "&keywords_type=allwords&keywords=xxx" +
                "&bug_status=NEW" +
                "&resolution=FIXED" +
                "&priority=P1" +
                "&emailassigned_to1=1&emailreporter1=1&emailcc1=1&emaillongdesc1=1&emailtype1=substring&email1=xxx" +
                "&chfieldfrom=2009-01-01&chfieldto=Now" +
                    "&chfield=[Bug+creation]" +
                    "&chfield=alias" +
                    "&chfield=assigned_to" +
                    "&chfield=cclist_accessible" +
                    "&chfield=component" +
                    "&chfield=deadline" +
                    "&chfield=everconfirmed" +
                    "&chfield=rep_platform" +
                    "&chfield=remaining_time" +
                    "&chfield=work_time" +
                    "&chfield=keywords" +
                    "&chfield=estimated_time" +
                    "&chfield=op_sys" +
                    "&chfield=priority" +
                    "&chfield=product" +
                    "&chfield=qa_contact" +
                    "&chfield=reporter_accessible" +
                    "&chfield=resolution" +
                    "&chfield=bug_severity" +
                    "&chfield=bug_status" +
                    "&chfield=short_desc" +
                    "&chfield=target_milestone" +
                    "&chfield=bug_file_loc" +
                    "&chfield=version" +
                    "&chfield=votes" +
                    "&chfield=status_whiteboard" +
                    "&chfieldvalue=xxx";
    }

    private class LogHandler extends Handler {
        final String msg;
        boolean done = false;
        public LogHandler(String msg) {
            this.msg = msg;
        }

        @Override
        public void publish(LogRecord record) {
            if(!done) {
                done = record.getMessage().startsWith(msg);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }
}
