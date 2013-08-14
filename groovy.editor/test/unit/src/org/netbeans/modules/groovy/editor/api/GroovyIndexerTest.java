/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class GroovyIndexerTest extends GroovyTestBase {

    
    public GroovyIndexerTest(String testName) {
        super(testName);
    }

    @Override
    public String prettyPrintValue(String key, String value) {
        return value;
    }

    public void testIsIndexable1() throws Exception {
        checkIsIndexable("testfiles/BookmarkController.groovy", true);
    }
    
    public void testIndex1() throws Exception {
        checkIndexer("testfiles/BookmarkController.groovy");
    }

    public void testIndex2() throws Exception {
        checkIndexer("testfiles/Hello.groovy");
    }
    
    /*******************************************
     *            INDEXER METHODS
     *******************************************/
    public void testGetMethods() throws Exception {
        GroovyIndex index = initIndex();
        
        Set<IndexedMethod> methods = index.getMethods("m", "demo.GroovyClass1", QuerySupport.Kind.PREFIX);
        assertEquals(3, methods.size());

        methods = index.getMethods(".*", "demo.GroovyClass1", QuerySupport.Kind.REGEXP);
        assertEquals(4, methods.size());
    }
    
    public void testGetClasses() throws Exception {
        GroovyIndex index = initIndex();
        
        Set<IndexedClass> classes = index.getClasses(".*", QuerySupport.Kind.REGEXP);
        assertEquals(5, classes.size());
    }
    
//    public void testQueryPath() throws Exception {
//        GroovyIndexer indexer = new GroovyIndexer();
//        assertTrue(indexer.acceptQueryPath("/foo/bar/baz"));
//        assertFalse(indexer.acceptQueryPath("/foo/target/bar"));
//        assertFalse(indexer.acceptQueryPath("/foo/art/bar"));
//    }

//    public void testProperties() throws Exception {
//        FileObject fo = getTestFile("testfiles/indexer/Properties1.groovy");
//        GsfTestCompilationInfo info = getInfo(fo);
//        GroovyIndex index = new GroovyIndex(info.getIndex(GroovyTokenId.GROOVY_MIME_TYPE));
//
//        // FIXME
//    }
    
    private GroovyIndex initIndex() throws Exception {
        indexFile(getTestPath());
        return GroovyIndex.get(createSourceClassPathsForTest());
    }
    
    private List<FileObject> createSourceClassPathsForTest() {
        List<FileObject> list = new ArrayList<FileObject>();
        
        File folder = new File(getDataDir(), getTestFolderPath());
        list.add(FileUtil.toFileObject(folder));
        
        return list;
    }

    private String getTestFolderPath() {
        return "testfiles/index/" + getTestName(); //NOI18N
    }
    
    private String getTestPath() {
        return getTestFolderPath() + "/" + getTestName() + ".groovy"; //NOI18N
    }

    private String getTestName() {
        String name = getName();
        int indexOf = name.indexOf("_");
        if (indexOf != -1) {
            name = name.substring(0, indexOf);
        }
        return name;
    }
}
