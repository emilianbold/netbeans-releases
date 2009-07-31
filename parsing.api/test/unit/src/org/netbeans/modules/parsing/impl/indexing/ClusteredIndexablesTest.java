/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.parsing.spi.indexing.Indexable;

/**
 *
 * @author vita
 */
public class ClusteredIndexablesTest {

    public ClusteredIndexablesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimple() {
        List<IndexableImpl> textPlains = Arrays.asList(new IndexableImpl [] {
            new TestIndexable("foo/indexable1", "text/plain"),
            new TestIndexable("foo/indexable2", "text/plain"),
            new TestIndexable("foo/indexable3", "text/plain"),
        });
        List<IndexableImpl> javas = Arrays.asList(new IndexableImpl [] {
            new TestIndexable("java/indexable1", "text/x-java"),
            new TestIndexable("java/indexable2", "text/x-java"),
        });
        List<IndexableImpl> xmls = Arrays.asList(new IndexableImpl [] {
            new TestIndexable("xml/indexable1", "text/xml"),
            new TestIndexable("xml/indexable2", "text/xml"),
            new TestIndexable("xml/indexable3", "text/xml"),
            new TestIndexable("xml/indexable4", "text/xml"),
            new TestIndexable("xml/indexable5", "text/xml"),
        });

        List<IndexableImpl> indexables = new ArrayList<IndexableImpl>();
        indexables.addAll(textPlains);
        indexables.addAll(javas);
        indexables.addAll(xmls);
        Collections.shuffle(indexables);

        ClusteredIndexables ci = new ClusteredIndexables(indexables);

        // when asking for all mime types we should get everything
        List<Indexable> all = toList(ci.getIndexablesFor(null));
        check("Wrong all indexables", indexables, all);

        List<Indexable> tp = toList(ci.getIndexablesFor("text/plain"));
        check("Wrong text/plain indexables", textPlains, tp);

        List<Indexable> j = toList(ci.getIndexablesFor("text/x-java"));
        check("Wrong text/x-java indexables", javas, j);

        List<Indexable> x = toList(ci.getIndexablesFor("text/xml"));
        check("Wrong text/xml indexables", xmls, x);

        List<Indexable> allAgain = toList(ci.getIndexablesFor(null));
        check("Wrong all indexables", indexables, allAgain);
    }

    private void check(String message, Collection<IndexableImpl> indexableImpls, Collection<Indexable> indexables) {
        Assert.assertEquals(message, indexableImpls.size(), indexables.size());

        Map<String, String> iiMap = new HashMap<String, String>();
        for(IndexableImpl ii : indexableImpls) {
            iiMap.put(ii.getRelativePath(), ii.getMimeType());
        }

        Map<String, String> iMap = new HashMap<String, String>();
        for(Indexable i : indexables) {
            iMap.put(i.getRelativePath(), i.getMimeType());
        }

        Assert.assertEquals(message, iiMap, iMap);
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        Assert.assertNotNull(iterable);
        List<T> list = new ArrayList<T>();
        for(T o : iterable) {
            list.add(o);
        }
        return list;
    }

    private static final class TestIndexable implements IndexableImpl {

        private final String relativePath;
        private final String mimeType;

        public TestIndexable(String relativePath, String mimeType) {
            this.relativePath = relativePath;
            this.mimeType = mimeType;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public URL getURL() {
            try {
                return new URL(relativePath);
            } catch (MalformedURLException ex) {
                return null;
            }
        }

        public String getMimeType() {
            return mimeType;
        }

        public boolean isTypeOf(String mimeType) {
            return mimeType.equals(this.mimeType);
        }

    } // End of TestIndexable class
}