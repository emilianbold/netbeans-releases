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
package org.netbeans.modules.web.livehtml;

import org.netbeans.modules.web.domdiff.Change;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.web.domdiff.DiffTest;

/**
 *
 */
public class ModelTest extends NbTestCase{
    
    private AnalysisStorage analysisStorage;
    
    public ModelTest() {
        super(ModelTest.class.getName());
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        analysisStorage = AnalysisStorage.getInstance();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getModel method, of class Model.
     */
    @Test
    public void testOrigins() throws IOException {
        AnalysisStorage.isUnitTesting = true;
        File f = new File(getWorkDir(), ""+System.currentTimeMillis());
        f.mkdir();
        Analysis m = new Analysis(f);
        HtmlSource source = DiffTest.getHtmlSource(getDataDir(), "model/test001-v1.html");
        m.storeDocumentVersion("0", source.getSourceCode().toString(), "[{}]", true);
        HtmlSource source2 = DiffTest.getHtmlSource(getDataDir(), "model/test001-v2.html");
        m.storeDocumentVersion("1", source2.getSourceCode().toString(), "[{}]", true);
        HtmlSource source3 = DiffTest.getHtmlSource(getDataDir(), "model/test001-v3.html");
        m.storeDocumentVersion("2", source3.getSourceCode().toString(), "[{}]", true);
        HtmlSource source4 = DiffTest.getHtmlSource(getDataDir(), "model/test001-v4.html");
        m.storeDocumentVersion("3", source4.getSourceCode().toString(), "[{}]", true);
        
        Revision r = m.getRevision(1);
        DiffTest.assertAddChange(192, 8, "id=\"new\"", source2, r.getChanges().get(0));
        DiffTest.assertAddChange(201, 14, "data-url=\"new\"", source2, r.getChanges().get(1));
        DiffTest.assertAddChange(356, 25, "id=\"new-form-placeholder\"", source2, r.getChanges().get(2));
        
        r = m.getRevision(2);
        HtmlSource s3 = new HtmlSource(r.getContent());
        //Change.dump(r.getChanges());
        assertEquals(9, r.getChanges().size());
        DiffTest.assertRemoveChange(175, "data-role=\"page\" ", source2, r.getChanges().get(0));
        ModelTest.assertOrigin(192, 8, "id=\"new\"", 1, s3, r.getChanges().get(1));
        DiffTest.assertAddChange(201, 9, "class=\"y\"", s3, r.getChanges().get(2));
        ModelTest.assertOrigin(211, 14, "data-url=\"new\"", 1, s3, r.getChanges().get(3));
        DiffTest.assertAddChange(284, 33, "<span>Add New Manufacturer</span>", s3, r.getChanges().get(4));
        DiffTest.assertRemoveChange(392, "", source2, r.getChanges().get(5));
        DiffTest.assertAddChange(392, 8, "CHANGED-", s3, r.getChanges().get(6));
        DiffTest.assertAddChange(431, 26, "<div>\n                    ", s3, r.getChanges().get(7));
        DiffTest.assertAddChange(501, 23, "\n                </div>", s3, r.getChanges().get(8));
        
        r = m.getRevision(3);
        HtmlSource s4 = new HtmlSource(r.getContent());
        //Change.dump(r.getChanges());
        assertEquals(7, r.getChanges().size());
        ModelTest.assertOrigin(175, 8, "id=\"new\"", 1, s4, r.getChanges().get(0));
        ModelTest.assertOrigin(184, 9, "class=\"y\"", 2, s4, r.getChanges().get(1));
        ModelTest.assertOrigin(194, 14, "data-url=\"new\"", 1, s4, r.getChanges().get(2));
        ModelTest.assertOrigin(267, 6, "<span>", 2, s4, r.getChanges().get(3));
        ModelTest.assertOrigin(362, 33, "id=\"new-form-CHANGED-placeholder\"", 2, s4, r.getChanges().get(4));
        ModelTest.assertOrigin(414, 5, "<div>", 2, s4, r.getChanges().get(5));
        DiffTest.assertAddChange(455, 13, "class=\"pokus\"", s4, r.getChanges().get(6));
        
    }

    public static void assertOrigin(int from, int len, String text, int rev, HtmlSource s, Change ch) {
        assertEquals(text, s.getSourceCode().subSequence(from, from+len));
        assertEquals(rev, ch.getRevisionIndex());
    }
    
}
