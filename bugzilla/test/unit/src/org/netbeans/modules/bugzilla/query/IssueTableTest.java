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

import java.lang.reflect.Field;
import java.text.MessageFormat;
import junit.framework.Test;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.IssuetableTestFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugzilla.TestConstants;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;

/**
 *
 * @author tomas
 */
public class IssueTableTest extends IssuetableTestFactory implements QueryConstants, TestConstants {

    public IssueTableTest(Test test) {
        super(test);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(org.netbeans.modules.bugtracking.issuetable.IssueTableTestCase.class);
        return new IssueTableTest(suite);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", System.getProperty("java.io.tmpdir"));
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.setProperty("netbeans.t9y.bugzilla.force.refresh.delay", "please!");
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    @Override
    public Query createQuery() {
        final String summary = "summary" + System.currentTimeMillis();

        final BugzillaRepository repo = QueryTestUtil.getRepository();

        String p =  MessageFormat.format(PARAMETERS_FORMAT, summary);
        final BugzillaQuery q = new BugzillaQuery(QUERY_NAME, repo, p, false, false, true); // false = not saved
        assertEquals(0,q.getIssues().length);
        return q;
    }

    @Override
    public IssueTable getTable(Query q) {
        try {
            BugtrackingController c = q.getController();
            Field f = c.getClass().getDeclaredField("issueTable");
            f.setAccessible(true);
            return (IssueTable) f.get(c);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int getColumnsCountBeforeSave() {
        return 7;
    }

    @Override
    public int getColumnsCountAfterSave() {
        return 9;
    }


}
