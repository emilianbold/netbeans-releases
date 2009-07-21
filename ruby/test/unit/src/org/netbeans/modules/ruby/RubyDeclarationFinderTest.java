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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby;

import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tor Norbye
 */
public class RubyDeclarationFinderTest extends RubyTestBase {
    
    public RubyDeclarationFinderTest(String testName) {
        super(testName);
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        return rubyTestsClassPath();
    }

    public void testDeclaration1() throws Exception {
        checkDeclaration("testfiles/resolv.rb", "r.each_name(add^ress) {|name|", "def each_name(^address)");
    }

    public void testDeclaration2() throws Exception {
        checkDeclaration("testfiles/resolv.rb", "yield na^me.to_s", "r.each_name(address) {|^name|");
    }

    public void testDeclaration3() throws Exception {
        checkDeclaration("testfiles/resolv.rb", "class UnconnectedUDP < Reque^ster", "^class Requester");
    }

    public void testDeclaration4() throws Exception {
        checkDeclaration("testfiles/declaration.rb", "attr_a^ccessor :symbol", "stub_module.rb", 9339);
    }

    public void testDeclaration5() throws Exception {
        checkDeclaration("testfiles/declaration.rb", "ope^nssl", "openssl.rb", 0);
    }

    public void testDeclaration6() throws Exception {
        checkDeclaration("testfiles/declaration.rb", "File.fil^e?", "stub_file.rb", 12373);
    }

    public void testAttrDeclaration() throws Exception {
        checkDeclaration("testfiles/attr_declaration.rb", "attr_accessor :b^az", "attr_declaration.rb", 45);
    }

    public void testAttrDeclaration2() throws Exception {
        checkDeclaration("testfiles/attr_declaration.rb", "attr_accessor :th^ud", "attr_declaration.rb", 58);
    }

    public void testAttrDeclaration3() throws Exception {
        checkDeclaration("testfiles/attr_declaration.rb", "b.b^az", "attr_declaration.rb", 26);
    }

    public void testAttrDeclaration4() throws Exception {
        checkDeclaration("testfiles/attr_declaration.rb", "c.t^hud", "attr_declaration.rb", 114);
    }

    public void testSymbolToLocalMethod() throws Exception {
        checkDeclaration("testfiles/symbol_to_method.rb", ":foo_^bar", "symbol_to_method.rb", 59);
    }

    public void testSymbolToInheritedMethod() throws Exception {
        checkDeclaration("testfiles/symbol_to_method.rb", ":foo_bar_b^ax", "symbol_to_method_base.rb", 12);
    }

    public void testSymbolToInheritedField() throws Exception {
        checkDeclaration("testfiles/symbol_to_method.rb", ":b^ax_field", "symbol_to_method_base.rb", 32);
    }



    //public void testDeclaration6() throws Exception {
    //    checkDeclaration("testfiles/declaration.rb", "File.safe_un^link", "ftools.rb", 1);
    //}

    public void testTestDeclaration1() throws Exception {
        // Make sure the test file is indexed
        FileObject fo = getTestFile("testfiles/testfile.rb");
        //TestFoo/test_bar => test/test_foo.rb:99
        DeclarationLocation loc = RubyDeclarationFinder.getTestDeclaration(fo, "TestFoo/test_bar", false);
        assertTrue(loc != DeclarationLocation.NONE);
        assertEquals("testfile.rb", loc.getFileObject().getNameExt());
        assertEquals(38, loc.getOffset());
    }

    public void testTestDeclaration2() throws Exception {
        // Make sure the test file is indexed
        FileObject fo = getTestFile("testfiles/testfile.rb");
        //MosModule::TestBaz/test_qux => test/test_baz.rb:88
        DeclarationLocation loc = RubyDeclarationFinder.getTestDeclaration(fo, "MosModule::TestBaz/test_qux", false);
        assertTrue(loc != DeclarationLocation.NONE);
        assertEquals("testfile.rb", loc.getFileObject().getNameExt());
        assertEquals(119, loc.getOffset());
    }

    public void testTestDeclarationIssue152703() throws Exception {
        // Make sure the test file is indexed
        FileObject fo = getTestFile("testfiles/rd_threads_and_frames_test.rb");
//        GsfTestCompilationInfo info = getInfo(fo);
//        assertNotNull(AstUtilities.getRoot(info));
//        info.getIndex(RubyInstallation.RUBY_MIME_TYPE);

        DeclarationLocation loc = RubyDeclarationFinder.getTestDeclaration(fo, "RDThreadsAndFrames/test_frames", false, false);
        assertTrue(loc != DeclarationLocation.NONE);
        assertEquals("rd_threads_and_frames_test.rb", loc.getFileObject().getNameExt());
        assertEquals(7230, loc.getOffset());
    }

    public void testTestClassDeclaration() throws Exception {
        // Make sure the test file is indexed
        FileObject fo = getTestFile("testfiles/testfile.rb");
        //TestFoo/test_bar => test/test_foo.rb:0 (offset for the class declaration)
        DeclarationLocation loc = RubyDeclarationFinder.getTestDeclaration(fo, "TestFoo/test_bar", true);
        assertTrue(loc != DeclarationLocation.NONE);
        assertEquals("testfile.rb", loc.getFileObject().getNameExt());
        assertEquals(0, loc.getOffset());
    }

    public void testTestClassDeclaration2() throws Exception {
        // Make sure the test file is indexed
        FileObject fo = getTestFile("testfiles/testfile.rb");
        DeclarationLocation loc = RubyDeclarationFinder.getTestDeclaration(fo, "MosModule::TestBaz/test_qux", true);
        assertTrue(loc != DeclarationLocation.NONE);
        assertEquals("testfile.rb", loc.getFileObject().getNameExt());
        assertEquals(79, loc.getOffset());
    }

    public void testTestClassDeclarationWithNonExistingMethod() throws Exception {
        // Make sure the test file is indexed
        FileObject fo = getTestFile("testfiles/testfile.rb");
        // tests that the class declaration is found even if the given method doesn't exist
        DeclarationLocation loc = RubyDeclarationFinder.getTestDeclaration(fo, "TestFoo/a_non_existing_method", true);
        assertTrue(loc != DeclarationLocation.NONE);
        assertEquals("testfile.rb", loc.getFileObject().getNameExt());
        assertEquals(0, loc.getOffset());

        // tests that NONE is returned for a non-existing method when we don't want the location for the class
        loc = RubyDeclarationFinder.getTestDeclaration(fo, "MosModule::TestBaz/a_non_existing_method", false);
        assertEquals(DeclarationLocation.NONE, loc);
    }

    // I don't actually get multiple locations for a single method out of the index
    //public void testTestDeclaration3() throws Exception {
    //    // Make sure the test file is indexed
    //    FileObject fo = getTestFile("testfiles/testfile.rb");
    //    GsfTestCompilationInfo info = getInfo(fo);
    //    assertNotNull(AstUtilities.getRoot(info));
    //    info.getIndex(RubyInstallation.RUBY_MIME_TYPE);
    //    // Force init of the index for both files that we care about
    //    RubyIndex.get(info.getIndex(RubyInstallation.RUBY_MIME_TYPE)).getMethods("a", "b", NameKind.EXACT_NAME, RubyIndex.SOURCE_SCOPE);
    //
    //    fo = getTestFile("testfiles/testfile2.rb");
    //    info = getInfo(fo);
    //    assertNotNull(AstUtilities.getRoot(info));
    //    info.getIndex(RubyInstallation.RUBY_MIME_TYPE);
    //    // Force init of the index for both files that we care about
    //    RubyIndex.get(info.getIndex(RubyInstallation.RUBY_MIME_TYPE)).getMethods("a", "b", NameKind.EXACT_NAME, RubyIndex.SOURCE_SCOPE);
    //
    //    //MosModule::TestBaz/test_qux => test/test_baz.rb:88
    //    DeclarationLocation loc = RubyDeclarationFinder.getTestDeclaration(fo, "MosModule::TestBaz/test_two");
    //    assertTrue(loc != DeclarationLocation.NONE);
    //    assertEquals(1, loc.getAlternativeLocations().size());
    //    AlternativeLocation alternate = loc.getAlternativeLocations().get(0);
    //    DeclarationLocation loc2 = alternate.getLocation();
    //    assertTrue(loc2 != DeclarationLocation.NONE);
    //
    //    if (loc.getFileObject().getNameExt().equals("testfile2.rb")) {
    //        // Swap the two
    //        DeclarationLocation tmp = loc2;
    //        loc2 = loc;
    //        loc = tmp;
    //    }
    //
    //    assertEquals("testfile.rb", loc.getFileObject().getNameExt());
    //    assertEquals("testfile2.rb", loc.getFileObject().getNameExt());
    //    assertEquals(10, loc.getOffset());
    //    assertEquals(20, loc2.getOffset());
    //}
}
