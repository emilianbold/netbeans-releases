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

package org.netbeans.modules.bugzilla;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author tomas
 */
public class StorageTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME = "Beautiful";
    private static String QUERY_NAME = "Hilarious";

    public StorageTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.setProperty("netbeans.user", System.getProperty("nbjunit.workdir", "/tmp/"));
    }

    public void testStorage() throws MalformedURLException, CoreException, IOException {
        final IssueStorage storage = IssueStorage.getInstance();

        Map<String, Map<String, String>> m = new HashMap<String, Map<String, String>>();
        Map<String, String> attr1 = new HashMap<String, String>();
        attr1.put("dummy", "dummy1");
        Map<String, String> attr2 = new HashMap<String, String>();
        attr2.put("dummy", "dummy2");
        String id1 = "id1";
        String id2 = "id2";
        
        String url = "http://test/bugzilla";
        String qName = "SomeQuery";

        storage.storeQuery(url, qName, new String[] {id1, id2});
        storage.storeIssue(url, id1, true, attr1);
        storage.storeIssue(url, id2, true, attr2);

        Map<String, String> attr = storage.readIssue(url, id1);
        if(attr == null) fail("missing issue id [" + id1 + "]");
        assertAttribute(attr1, "dummy", "dummy1");
        attr = storage.readIssue(url, id2);
        if(attr == null) fail("missing issue id [" + id2 + "]");
        assertAttribute(attr2, "dummy", "dummy2");

    }

    private void assertAttribute(Map<String, String> attrs, String attr, String value) {
        String v = attrs.get(attr);
        if(v == null) fail("missing attribute [" + attr + "]");
        if(!v.equals(value)) fail("value [" + v + "] for attribute [" + attr + "] instead of [" + value + "]");
    }
}
