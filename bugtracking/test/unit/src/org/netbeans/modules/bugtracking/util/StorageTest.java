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

package org.netbeans.modules.bugtracking.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.IssueNode;

/**
 *
 * @author tomas
 */
public class StorageTest extends NbTestCase {

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

        Map<String, String> attr1 = new HashMap<String, String>();
        attr1.put("dummy1", "dummy3");
        attr1.put("dummy2", "dummy2");
        attr1.put("dummy3", "dummy1");
        Map<String, String> attr2 = new HashMap<String, String>();
        attr2.put("dummy5", "dummy7");
        attr2.put("dummy6", "dummy6");
        attr2.put("dummy7", "dummy5");
        String id1 = "id1";
        String id2 = "id2";

        String url = "http://test/bugzilla";
        String qName = "SomeQuery";
        Issue i1 = new DummyIssue(id1, attr1);
        Issue i2 = new DummyIssue(id2, attr2);

        storage.storeQuery(url, qName, new String[] {id1, id2});

        IssueCache.IssueEntry ie1 = new IssueCache.IssueEntry(i1, attr1, -1, false);
        IssueCache.IssueEntry ie2 = new IssueCache.IssueEntry(i2, attr2, -1, false);
        
        storage.storeIssue(url, ie1);
        storage.storeIssue(url, ie2);

        List<String> issues = storage.readQuery(url, qName);
        assertTrue(issues.contains(id1));
        assertTrue(issues.contains(id2));

        ie1 = new IssueCache.IssueEntry(i1, null, -1, false);
        ie2 = new IssueCache.IssueEntry(i2, null, -1, false);
        storage.readIssue(url, ie1);
        if(ie1.getSeenAttributes() == null) fail("missing issue id [" + id1 + "]");
        assertAttribute(ie1.getSeenAttributes(), "dummy1", "dummy3");
        assertAttribute(ie1.getSeenAttributes(), "dummy2", "dummy2");
        assertAttribute(ie1.getSeenAttributes(), "dummy3", "dummy1");
        storage.readIssue(url, ie2);
        if(ie2.getSeenAttributes() == null) fail("missing issue id [" + id2 + "]");
        assertAttribute(ie2.getSeenAttributes(), "dummy5", "dummy7");
        assertAttribute(ie2.getSeenAttributes(), "dummy6", "dummy6");
        assertAttribute(ie2.getSeenAttributes(), "dummy7", "dummy5");

        // create another query
        String qName2 = "SomeQuery2";
        storage.storeQuery(url, qName2, new String[] {id1, id2});
        issues = storage.readQuery(url, qName2);
        assertEquals(2, issues.size());

        // remove it
        storage.removeQuery(url, qName2);
        issues = storage.readQuery(url, qName2);
        // it's gone
        assertEquals(0, issues.size());
        // first query still exists
        issues = storage.readQuery(url, qName);
        assertEquals(2, issues.size());

    }

    private void assertAttribute(Map<String, String> attrs, String attr, String value) {
        String v = attrs.get(attr);
        if(v == null) fail("missing attribute [" + attr + "]");
        if(!v.equals(value)) fail("value [" + v + "] for attribute [" + attr + "] instead of [" + value + "]");
    }

    private static class DummyIssue extends Issue {
        private Map<String, String> m;
        private String id;

        public DummyIssue(String id, Map<String, String> m) {
            super(null);
            this.m = m;
            this.id = id;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public Map<String, String> getAttributes() {
            return m;
        }

        @Override
        public boolean refresh() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addComment(String comment, boolean closeAsFixed) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public IssueNode getNode() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override
        public String getSummary() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getRecentChanges() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getTooltip() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void attachPatch(File file, String description) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BugtrackingController getController() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
