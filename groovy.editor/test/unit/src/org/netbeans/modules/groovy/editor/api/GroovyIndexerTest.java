/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.modules.groovy.editor.api.GroovyIndexer;
import org.netbeans.modules.groovy.editor.api.GroovyIndex;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.modules.groovy.editor.api.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.api.elements.IndexedMethod;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.openide.filesystems.FileObject;

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

    public void testQueryPath() throws Exception {
        GroovyIndexer indexer = new GroovyIndexer();
        assertTrue(indexer.acceptQueryPath("/foo/bar/baz"));
        assertFalse(indexer.acceptQueryPath("/foo/jruby/lib/ruby/gems/1.8/gems"));
        assertFalse(indexer.acceptQueryPath("/foo/netbeans/ruby2/rubystubs/0.2"));
    }

    public void testIndex1() throws Exception {
        checkIndexer("testfiles/BookmarkController.groovy");
    }

    public void testIndex2() throws Exception {
        checkIndexer("testfiles/Hello.groovy");
    }

    public void testMethods1() throws Exception {
        FileObject fo = getTestFile("testfiles/GroovyClass1.groovy");
        GsfTestCompilationInfo info = getInfo(fo);
        GroovyIndex index = new GroovyIndex(info.getIndex(GroovyTokenId.GROOVY_MIME_TYPE));

        // get methods starting with 'm'
        Set<IndexedMethod> methods = index.getMethods("m", "demo.GroovyClass1", NameKind.PREFIX, EnumSet.allOf(SearchScope.class));
        assertEquals(3, methods.size());

        // get all methods from class
        methods = index.getMethods(".*", "demo.GroovyClass1", NameKind.REGEXP, EnumSet.allOf(SearchScope.class));
        assertEquals(4, methods.size());
    }

    public void testClasses() throws Exception {
        FileObject fo = getTestFile("testfiles/Hello.groovy");
        GsfTestCompilationInfo info = getInfo(fo);
        GroovyIndex index = new GroovyIndex(info.getIndex(GroovyTokenId.GROOVY_MIME_TYPE));

        // get all classes
        Set<IndexedClass> classes = index.getClasses(".*", NameKind.REGEXP, true, false, false);
        assertEquals(6, classes.size());
    }

}
