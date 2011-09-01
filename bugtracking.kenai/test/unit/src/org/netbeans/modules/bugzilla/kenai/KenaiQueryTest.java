/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.kenai;

import org.netbeans.modules.bugzilla.query.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;

/**
 *
 * @author tomas
 */
public class KenaiQueryTest extends NbTestCase implements TestConstants, QueryConstants {

    public KenaiQueryTest(String arg0) {
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
        
        Kenai kenai = KenaiManager.getDefault().createKenai("testjava.net", "https://testjava.net");
        BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), ".test-kenai")));
        String username = br.readLine();
        String password = br.readLine();
        
        String proxy = br.readLine();
        String port = br.readLine();

        if(proxy != null) {
            System.setProperty("https.proxyHost", proxy);
            System.setProperty("https.proxyPort", port);
        }
            
        br.close();

        kenai.login(username, password.toCharArray(), false);
        
        // XXX MYLYN
//        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
//        try {
//            bcp.start(null);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        cleanupStoredIssues();
    }

    // XXX shoud be on the spi
    public void testLastRefresh() {
        String parameters = "query_format=advanced&" +
                "short_desc_type=allwordssubstr&" +
                "short_desc=whatever112233445566778899&" +
                "product=TestProduct";
        String qname = "kq" + System.currentTimeMillis();
        KenaiQuery q = new KenaiQuery(qname, QueryTestUtil.getRepository(), parameters, "kp", true, false);
        long lastRefresh = q.getLastRefresh();
        assertEquals(0, lastRefresh);

        long ts = System.currentTimeMillis();

        ts = System.currentTimeMillis();
        q.refresh();
        assertTrue(q.getLastRefresh() >= ts);

        ts = System.currentTimeMillis();
        q.refresh();
        lastRefresh = q.getLastRefresh();
        assertTrue(lastRefresh >= ts);

        // emulate restart
        q = new KenaiQuery(qname, QueryTestUtil.getRepository(), parameters, "kp", true, false);;
        assertEquals((int)(lastRefresh/1000), (int)(q.getLastRefresh()/1000));

    }

    private void cleanupStoredIssues() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        QueryTestUtil.getRepository().getIssueCache().storeArchivedQueryIssues(QUERY_NAME, new String[0]);
        QueryTestUtil.getRepository().getIssueCache().storeQueryIssues(QUERY_NAME, new String[0]);
    }

    private class QueryListener implements PropertyChangeListener {
        int saved = 0;
        int removed = 0;
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(Query.EVENT_QUERY_REMOVED)) {
                removed++;
            }
            if(evt.getPropertyName().equals(Query.EVENT_QUERY_SAVED)) {
                saved++;
            }
        }
        void reset() {
            saved = 0;
            removed = 0;
        }

    }
}
