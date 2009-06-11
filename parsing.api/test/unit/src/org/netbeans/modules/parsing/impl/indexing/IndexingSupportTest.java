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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class IndexingSupportTest extends NbTestCase {

    private FileObject root;
    private FileObject cache;
    private FileObject f1;
    private FileObject f2;

    public IndexingSupportTest (final String name) {
        super (name);
    }

    @Override
    public void setUp () throws Exception {
        this.clearWorkDir();
        final File wdf = getWorkDir();
        final FileObject wd = FileUtil.toFileObject(wdf);
        assert wd != null;
        root = FileUtil.createFolder(wd,"src");
        assert root != null;
        cache = FileUtil.createFolder(wd, "cache");
        assert cache != null;
        CacheFolder.setCacheFolder(cache);
        f1 = FileUtil.createData(root,"folder/a.foo");
        assert f1 != null;
        f2 = FileUtil.createData(root,"folder/b.foo");
        assert f2 != null;
    }

    public void testIndexingSupportInstances () throws Exception {
        final Context ctx1 = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(root.getURL()), root.getURL(), "fooIndexer", 1, null, false, false, false, null);
        assertNotNull(ctx1);
        final Context ctx2 = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(root.getURL()), root.getURL(), "embIndexer", 1, null, false, false, false, null);
        assertNotNull(ctx2);

        final IndexingSupport is1 = IndexingSupport.getInstance(ctx1);
        assertNotNull(is1);
        final IndexingSupport is2 = IndexingSupport.getInstance(ctx2);
        assertNotNull(is2);

        assertSame(is1, SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx1));
        assertSame(is2, SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx2));
    }

    public void testIndexingQuerySupport () throws Exception {
        // index
        final Context ctx = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(root.getURL()), root.getURL(), "fooIndexer", 1, null, false, false, false, null);
        assertNotNull(ctx);
        final Indexable i1 = SPIAccessor.getInstance().create(new FileObjectIndexable(root, f1));
        final IndexingSupport is = IndexingSupport.getInstance(ctx);
        assertNotNull(is);
        IndexDocument doc1 = is.createDocument(i1);
        assertNotNull(doc1);
        doc1.addPair("class", "String", true, true);
        doc1.addPair("package", "java.lang", true, true);        
        is.addDocument(doc1);
        final Indexable i2 = SPIAccessor.getInstance().create(new FileObjectIndexable(root, f2));
        IndexDocument doc2 = is.createDocument(i2);
        assertNotNull(doc2);
        doc2.addPair("class", "Object", true, true);
        doc2.addPair("package", "java.lang", true, true);        
        is.addDocument(doc2);
        SupportAccessor.getInstance().store(is);

        // query
        Constructor<QuerySupport> c = QuerySupport.class.getDeclaredConstructor(FileObject.class, String.class, Integer.TYPE);
        c.setAccessible(true);
        QuerySupport qs = c.newInstance(root,"fooIndexer",1);
        Collection<? extends IndexResult> result = qs.query("class", "String", QuerySupport.Kind.EXACT, "class", "package");
        assertEquals(1, result.size());
        assertEquals("String", result.iterator().next().getValue("class"));
        assertEquals("java.lang", result.iterator().next().getValue("package"));
        assertEquals(f1, result.iterator().next().getFile());
        assertEquals(f1.getURL(), result.iterator().next().getUrl());
        result = qs.query("class", "Str", QuerySupport.Kind.PREFIX, "class", "package");
        assertEquals(1, result.size());
        assertEquals("String", result.iterator().next().getValue("class"));
        assertEquals("java.lang", result.iterator().next().getValue("package"));
        result = qs.query("class", "S.*g", QuerySupport.Kind.REGEXP, "class", "package");
        assertEquals(1, result.size());
        assertEquals("String", result.iterator().next().getValue("class"));
        assertEquals("java.lang", result.iterator().next().getValue("package"));
        result = qs.query("class", "S", QuerySupport.Kind.CAMEL_CASE, "class", "package");
        assertEquals(1, result.size());
        assertEquals("String", result.iterator().next().getValue("class"));
        assertEquals("java.lang", result.iterator().next().getValue("package"));
        result = qs.query("class", "", QuerySupport.Kind.PREFIX, "class", "package");
        assertEquals(2, result.size());
        IndexResult[] ir = new IndexResult[2];
        ir = result.toArray(ir);
        assertEquals("String", ir[0].getValue("class"));
        assertEquals("java.lang", ir[0].getValue("package"));
        assertEquals("Object", ir[1].getValue("class"));
        assertEquals("java.lang", ir[1].getValue("package"));
        result = qs.query("class", "F", QuerySupport.Kind.PREFIX, "class", "package");
        assertEquals(0, result.size());
    }

}
